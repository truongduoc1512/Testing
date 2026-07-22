import { NextResponse } from "next/server";
import bcrypt from "bcryptjs";
import { loginSchema } from "@/lib/validation/auth.schema";
import { parseZodErrors } from "@/lib/validation/parseErrors";
import { prisma } from "@/lib/db";
import { signToken } from "@/lib/auth/jwt";
import { COOKIE_NAME, COOKIE_MAX_AGE } from "@/lib/auth/session";
import { buildRateLimitKey, loginLimiter, rateLimitResponse } from "@/lib/rate-limit";
import { apiError } from "@/lib/logger";
import { safeErrorMessage } from "@/lib/safe-error";

export async function POST(request: Request) {
  try {
    let body: unknown;
    try {
      body = await request.json();
    } catch {
      return NextResponse.json({ error: "Invalid JSON" }, { status: 400 });
    }
    const emailHint =
      body && typeof body === "object" && "email" in body && typeof body.email === "string"
        ? body.email.trim().toLowerCase()
        : "unknown";
    const key = buildRateLimitKey(request, "login", emailHint);
    const limit = loginLimiter.check(key);
    if (!limit.allowed) return rateLimitResponse(limit.resetMs);

    const result = loginSchema.safeParse(body);
    if (!result.success) {
      return NextResponse.json(
        { error: "Validation failed", fields: parseZodErrors(result.error) },
        { status: 400 },
      );
    }

    const { email, password } = result.data;

    const user = await prisma.user.findUnique({
      where: { email },
    });

    if (!user) {
      return NextResponse.json(
        {
          error: "Invalid credentials",
          fields: { email: "Email hoặc mật khẩu không đúng" },
        },
        { status: 401 },
      );
    }

    const passwordMatch = await bcrypt.compare(password, user.password);

    if (!passwordMatch) {
      return NextResponse.json(
        {
          error: "Invalid credentials",
          fields: { email: "Email hoặc mật khẩu không đúng" },
        },
        { status: 401 },
      );
    }

    const token = await signToken({
      sub: user.id,
      email: user.email,
      fullName: user.fullName,
      tokenVersion: user.tokenVersion,
    });

    const response = NextResponse.json(
      {
        message: "Login successful",
        user: {
          email: user.email,
          fullName: user.fullName,
        },
      },
      { status: 200 },
    );

    response.cookies.set(COOKIE_NAME, token, {
      httpOnly: true,
      secure: process.env.NODE_ENV === "production",
      sameSite: "strict",
      maxAge: COOKIE_MAX_AGE,
      path: "/",
    });

    return response;
  } catch (e) {
    apiError("POST /api/auth/login", e);
    return NextResponse.json(
      { error: safeErrorMessage(e, "Internal server error") },
      { status: 500 },
    );
  }
}
