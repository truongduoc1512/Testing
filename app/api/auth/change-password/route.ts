import { NextResponse } from "next/server";
import bcrypt from "bcryptjs";
import { z } from "zod";
import { prisma } from "@/lib/db";
import { getSession } from "@/lib/auth/session";
import { parseZodErrors } from "@/lib/validation/parseErrors";
import { signToken } from "@/lib/auth/jwt";
import { COOKIE_NAME, COOKIE_MAX_AGE } from "@/lib/auth/constants";
import { apiError } from "@/lib/logger";
import { safeErrorMessage } from "@/lib/safe-error";
import {
  buildRateLimitKey,
  changePasswordLimiter,
  rateLimitResponse,
} from "@/lib/rate-limit";
import { writeAuditLog } from "@/lib/audit-log";

const changePasswordSchema = z
  .object({
    oldPassword: z
      .string()
      .min(1, "Vui lòng nhập mật khẩu hiện tại")
      .max(128, "Mật khẩu quá dài"),
    newPassword: z
      .string()
      .min(8, "Tối thiểu 8 ký tự")
      .max(128, "Mật khẩu quá dài")
      .regex(/[a-z]/, "Phải có chữ thường")
      .regex(/[A-Z]/, "Phải có chữ hoa")
      .regex(/[0-9]/, "Phải có chữ số"),
    confirmPassword: z.string().min(1, "Vui lòng xác nhận mật khẩu"),
  })
  .refine((d) => d.newPassword === d.confirmPassword, {
    message: "Mật khẩu xác nhận không khớp",
    path: ["confirmPassword"],
  })
  .refine((d) => d.oldPassword !== d.newPassword, {
    message: "Mật khẩu mới phải khác mật khẩu hiện tại",
    path: ["newPassword"],
  });

export async function POST(request: Request) {
  try {
    const session = await getSession();
    if (!session) {
      return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
    }

    const key = buildRateLimitKey(request, session.sub, "change-password");
    const limit = changePasswordLimiter.check(key);
    if (!limit.allowed) return rateLimitResponse(limit.resetMs);

    let body: unknown;
    try {
      body = await request.json();
    } catch {
      return NextResponse.json({ error: "Invalid JSON" }, { status: 400 });
    }
    const result = changePasswordSchema.safeParse(body);
    if (!result.success) {
      return NextResponse.json(
        { error: "Validation failed", fields: parseZodErrors(result.error) },
        { status: 400 },
      );
    }

    const { oldPassword, newPassword } = result.data;

    const user = await prisma.user.findUnique({
      where: { id: session.sub },
    });

    if (!user) {
      return NextResponse.json({ error: "User not found" }, { status: 404 });
    }

    const match = await bcrypt.compare(oldPassword, user.password);
    if (!match) {
      return NextResponse.json(
        {
          error: "Validation failed",
          fields: { oldPassword: "Mật khẩu hiện tại không đúng" },
        },
        { status: 400 },
      );
    }

    const hashed = await bcrypt.hash(newPassword, 12);
    const updatedUser = await prisma.user.update({
      where: { id: session.sub },
      data: { password: hashed, tokenVersion: { increment: 1 } },
      select: { tokenVersion: true },
    });

    const newToken = await signToken({
      sub: session.sub,
      email: session.email,
      fullName: session.fullName,
      tokenVersion: updatedUser.tokenVersion,
    });
    const response = NextResponse.json({ message: "Đổi mật khẩu thành công" });
    response.cookies.set(COOKIE_NAME, newToken, {
      httpOnly: true,
      secure: process.env.NODE_ENV === "production",
      sameSite: "strict",
      maxAge: COOKIE_MAX_AGE,
      path: "/",
    });
    await writeAuditLog({
      userId: session.sub,
      actorEmail: session.email,
      action: "auth.password.change",
      targetType: "user",
      targetId: session.sub,
      status: "success",
      message: "Đã đổi mật khẩu",
    });
    return response;
  } catch (e) {
    apiError("POST /api/auth/change-password", e);
    return NextResponse.json(
      { error: safeErrorMessage(e, "Internal server error") },
      { status: 500 },
    );
  }
}
