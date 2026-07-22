import { NextResponse } from "next/server";
import bcrypt from "bcryptjs";
import { registerSchema } from "@/lib/validation/register.schema";
import { parseZodErrors } from "@/lib/validation/parseErrors";
import { consumeVerifiedToken } from "@/lib/auth/otp-store";
import { prisma } from "@/lib/db";
import { buildRateLimitKey, createRateLimiter, rateLimitResponse } from "@/lib/rate-limit";
import { apiError } from "@/lib/logger";
import { safeErrorMessage } from "@/lib/safe-error";

const MAX_USERS = 5;

const registerLimiter = createRateLimiter("register-create", {
  maxRequests: 3,
  windowMs: 10 * 60 * 1000,
});

export async function POST(request: Request) {
  try {
    const key = buildRateLimitKey(request, "register-create");
    const limit = registerLimiter.check(key);
    if (!limit.allowed) return rateLimitResponse(limit.resetMs);

    let body: unknown;
    try {
      body = await request.json();
    } catch {
      return NextResponse.json({ error: "Invalid JSON" }, { status: 400 });
    }

    const result = registerSchema.safeParse(body);
    if (!result.success) {
      return NextResponse.json(
        { error: "Validation failed", fields: parseZodErrors(result.error) },
        { status: 400 },
      );
    }

    const { email, fullName, password, token } = result.data;
    const normalizedEmail = email.trim().toLowerCase();

    if (!consumeVerifiedToken(token, normalizedEmail)) {
      return NextResponse.json(
        { error: "Token xác minh không hợp lệ hoặc đã hết hạn" },
        { status: 403 },
      );
    }

    const [existingUser, totalUsers] = await Promise.all([
      prisma.user.findUnique({ where: { email: normalizedEmail } }),
      prisma.user.count(),
    ]);
    if (existingUser) {
      return NextResponse.json(
        { error: "Email đã được đăng ký" },
        { status: 409 },
      );
    }
    if (totalUsers >= MAX_USERS) {
      return NextResponse.json(
        { error: "Hệ thống đã đủ người dùng" },
        { status: 403 },
      );
    }

    const hashedPassword = await bcrypt.hash(password, 12);

    await prisma.user.create({
      data: {
        email: normalizedEmail,
        fullName: fullName.trim(),
        password: hashedPassword,
        emailVerified: true,
      },
    });

    return NextResponse.json(
      { message: "Đăng ký thành công" },
      { status: 201 },
    );
  } catch (e) {
    apiError("POST /api/auth/register", e);
    return NextResponse.json(
      { error: safeErrorMessage(e, "Internal server error") },
      { status: 500 },
    );
  }
}
