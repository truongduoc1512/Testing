import { NextResponse } from "next/server";
import { verifyOtpSchema } from "@/lib/validation/register.schema";
import { parseZodErrors } from "@/lib/validation/parseErrors";
import { verifyOtp } from "@/lib/auth/otp-store";
import { buildRateLimitKey, createRateLimiter, rateLimitResponse } from "@/lib/rate-limit";
import { apiError } from "@/lib/logger";
import { safeErrorMessage } from "@/lib/safe-error";

const otpVerifyLimiter = createRateLimiter("register-verify", {
  maxRequests: 10,
  windowMs: 5 * 60 * 1000,
});

export async function POST(request: Request) {
  try {
    const key = buildRateLimitKey(request, "register-verify");
    const limit = otpVerifyLimiter.check(key);
    if (!limit.allowed) return rateLimitResponse(limit.resetMs);

    let body: unknown;
    try {
      body = await request.json();
    } catch {
      return NextResponse.json({ error: "Invalid JSON" }, { status: 400 });
    }

    const result = verifyOtpSchema.safeParse(body);
    if (!result.success) {
      return NextResponse.json(
        { error: "Validation failed", fields: parseZodErrors(result.error) },
        { status: 400 },
      );
    }

    const { email, code } = result.data;
    const normalizedEmail = email.trim().toLowerCase();

    const verification = await verifyOtp(normalizedEmail, code);
    if (!verification.success) {
      return NextResponse.json(
        { error: verification.error },
        { status: 401 },
      );
    }

    return NextResponse.json({
      message: "Email verified",
      token: verification.token,
    });
  } catch (e) {
    apiError("POST /api/auth/register/verify-otp", e);
    return NextResponse.json(
      { error: safeErrorMessage(e, "Internal server error") },
      { status: 500 },
    );
  }
}
