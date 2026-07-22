import { NextResponse } from "next/server";
import { prisma } from "@/lib/db";
import { closePaymentProfile } from "@/lib/scanner/google-profile";
import { safeDecrypt, encryptValue } from "@/lib/crypto";
import { getSession } from "@/lib/auth/session";
import { apiError, apiInfo } from "@/lib/logger";
import { safeErrorMessage } from "@/lib/safe-error";
import { resolveOwnedAdminId } from "@/lib/admin-access";
import { buildRateLimitKey, profileLimiter, rateLimitResponse } from "@/lib/rate-limit";
import type { Cookie } from "@/lib/scanner/google-http";
import { formatAuditAdminLabel, writeAuditLog } from "@/lib/audit-log";

export const maxDuration = 120;

export async function POST(
  request: Request,
  { params }: { params: Promise<{ id: string }> },
) {
  try {
    const session = await getSession();
    if (!session) return NextResponse.json({ error: "Unauthorized" }, { status: 401 });

    const { id: routeId } = await params;
    const adminId = await resolveOwnedAdminId(session.sub, routeId);
    if (!adminId) return NextResponse.json({ error: "Not found" }, { status: 404 });

    const key = buildRateLimitKey(request, session.sub, adminId, "profile-close-payment");
    const limit = profileLimiter.check(key);
    if (!limit.allowed) return rateLimitResponse(limit.resetMs);

    const adminAccount = await prisma.adminAccount.findFirst({
      where: { id: adminId, createdBy: session.sub },
      include: { session: true },
    });

    if (!adminAccount) {
      return NextResponse.json({ error: "Admin account not found" }, { status: 404 });
    }
    apiInfo("/api/admin/[id]/profile/close-payment", "start", {
      adminId,
    });

    const ft = adminAccount.familyType || "ultra";
    if (ft === "gpt") {
      return NextResponse.json(
        { error: "This feature is only for Google accounts." },
        { status: 400 },
      );
    }

    const googleSession = adminAccount.session;
    let cookies: Cookie[] = [];
    if (googleSession?.cookies) {
      try {
        const cookiesStr = safeDecrypt(googleSession.cookies);
        const parsed = JSON.parse(cookiesStr);
        if (Array.isArray(parsed)) cookies = parsed;
      } catch (e) {
        apiError("/api/admin/[id]/profile/close-payment", e);
      }
    }

    const result = await closePaymentProfile(cookies, adminAccount);
    apiInfo("/api/admin/[id]/profile/close-payment", "scanner_result", {
      success: result.success,
    });

    if (result.success) {
      if (!result.newCookies || result.newCookies.length === 0) {
        apiError(
          "/api/admin/[id]/profile/close-payment",
          new Error("close-payment succeeded without refreshed cookies"),
        );
        return NextResponse.json(
          {
            success: false,
            message: "Không làm mới được phiên sau khi đóng hồ sơ thanh toán.",
          },
          { status: 500 },
        );
      }

      try {
        await prisma.googleSession.update({
          where: { adminId },
          data: {
            cookies: encryptValue(JSON.stringify(result.newCookies)),
          },
        });
      } catch (e) {
        apiError("/api/admin/[id]/profile/close-payment", e);
        return NextResponse.json(
          {
            success: false,
            message: "Không lưu được phiên đã làm mới.",
          },
          { status: 500 },
        );
      }
    } else if (result.newCookies && result.newCookies.length > 0) {
      try {
        await prisma.googleSession.update({
          where: { adminId },
          data: {
            cookies: encryptValue(JSON.stringify(result.newCookies)),
          },
        });
      } catch (e) {
        apiError("/api/admin/[id]/profile/close-payment", e);
      }
    }

    await writeAuditLog({
      userId: session.sub,
      actorEmail: session.email,
      action: "profile.payment.close",
      targetType: "admin",
      targetId: adminId,
      status: result.success ? "success" : "failure",
      message: result.success
        ? `Đã đóng hồ sơ thanh toán của admin ${formatAuditAdminLabel(adminAccount)}`
        : `Không đóng được hồ sơ thanh toán của admin ${formatAuditAdminLabel(adminAccount)}`,
      metadata: {
        adminEmail: adminAccount.email,
        adminName: adminAccount.displayName,
        familyType: ft,
        refreshedSession: Boolean(result.newCookies?.length),
      },
    });

    return NextResponse.json({
      success: result.success,
      message: result.success
        ? "Đã đóng hồ sơ thanh toán."
        : result.message || "Không đóng được hồ sơ thanh toán.",
    });
  } catch (error: unknown) {
    apiError("/api/admin/[id]/profile/close-payment", error);
    return NextResponse.json(
      {
        success: false,
        message: safeErrorMessage(error, "Không đóng được hồ sơ thanh toán"),
      },
      { status: 500 },
    );
  }
}
