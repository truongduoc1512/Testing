import { NextResponse } from "next/server";
import { sendOtpSchema } from "@/lib/validation/register.schema";
import { parseZodErrors } from "@/lib/validation/parseErrors";
import {
  generateOtp,
  storeOtp,
  checkCooldown,
  checkLockout,
  checkResendLimit,
} from "@/lib/auth/otp-store";
import { sendOtpEmail } from "@/lib/auth/send-otp-email";
import { prisma } from "@/lib/db";
import { buildRateLimitKey, createRateLimiter, rateLimitResponse } from "@/lib/rate-limit";
import { apiError } from "@/lib/logger";
import { safeErrorMessage } from "@/lib/safe-error";

const MAX_USERS = 5;

const otpSendLimiter = createRateLimiter("register-otp", {
  maxRequests: 10,
  windowMs: 60 * 60 * 1000,
});

export async function POST(request: Request) {
  try {
    const key = buildRateLimitKey(request, "register-otp");
    const limit = otpSendLimiter.check(key);
    if (!limit.allowed) return rateLimitResponse(limit.resetMs);

    let body: unknown;
    try {
      body = await request.json();
    } catch {
      return NextResponse.json({ error: "Invalid JSON" }, { status: 400 });
    }

    const result = sendOtpSchema.safeParse(body);
    if (!result.success) {
      return NextResponse.json(
        { error: "Validation failed", fields: parseZodErrors(result.error) },
        { status: 400 },
      );
    }

    const { email } = result.data;
    const normalizedEmail = email.trim().toLowerCase();

    const lockout = await checkLockout(normalizedEmail);
    if (lockout.locked) {
      return NextResponse.json(
        { error: `Tài khoản tạm khóa. Thử lại sau ${lockout.minutesLeft} phút.` },
        { status: 429 },
      );
    }

    const resend = await checkResendLimit(normalizedEmail);
    if (!resend.allowed) {
      return NextResponse.json(
        { error: `Đã gửi tối đa 3 lần. Thử lại sau ${resend.hoursLeft} giờ.` },
        { status: 429 },
      );
    }

    if (await checkCooldown(normalizedEmail)) {
      return NextResponse.json(
        { error: "Đợi 60 giây trước khi gửi lại" },
        { status: 429 },
      );
    }

    const [existingUser, totalUsers] = await Promise.all([
      prisma.user.findUnique({ where: { email: normalizedEmail } }),
      prisma.user.count(),
    ]);
    if (existingUser) {
      return NextResponse.json(
        { error: "Email đã được đăng ký", fields: { email: "Email đã được đăng ký" } },
        { status: 409 },
      );
    }
    if (totalUsers >= MAX_USERS) {
      return NextResponse.json(
        { error: "Hệ thống đã đủ người dùng. Vui lòng liên hệ", contactUrl: "https://zalo.me/0325093767", contactLabel: "Zalo" },
        { status: 403 },
      );
    }

    const otp = generateOtp();
    const mailResult = await sendOtpEmail(normalizedEmail, otp);
    if (!mailResult.success) {
      return NextResponse.json(
        { error: mailResult.error || "Lỗi gửi email" },
        { status: 500 },
      );
    }

    await storeOtp(normalizedEmail, otp);

    return NextResponse.json({ message: "OTP sent" });
  } catch (e) {
    apiError("POST /api/auth/register/send-otp", e);
    return NextResponse.json(
      { error: safeErrorMessage(e, "Internal server error") },
      { status: 500 },
    );
  }
}
