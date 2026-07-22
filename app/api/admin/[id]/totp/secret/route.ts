import { NextResponse } from "next/server";
import { prisma } from "@/lib/db";
import { safeDecrypt } from "@/lib/crypto";
import { apiError } from "@/lib/logger";
import { safeErrorMessage } from "@/lib/safe-error";
import { requireOwnedAdminAccess } from "@/lib/api/admin-route";
import { totpLimiter } from "@/lib/rate-limit";

export async function GET(
  request: Request,
  { params }: { params: Promise<{ id: string }> },
) {
  try {
    const guard = await requireOwnedAdminAccess({
      request,
      params,
      action: "totp-secret",
      limiter: totpLimiter,
    });
    if ("response" in guard) return guard.response;
    const { session, adminId } = guard;

    const admin = await prisma.adminAccount.findFirst({
      where: { id: adminId, createdBy: session.sub },
      select: { totpSecret: true, has2FA: true, updatedAt: true },
    });

    if (!admin) {
      return NextResponse.json({ error: "Not found" }, { status: 404 });
    }

    return NextResponse.json({
      hasTotp: Boolean(admin.totpSecret),
      totpEnabled: Boolean(admin.has2FA || admin.totpSecret),
      totpSecret: safeDecrypt(admin.totpSecret || ""),
      lastUpdatedAt: admin.updatedAt?.toISOString() ?? null,
    });
  } catch (e) {
    apiError("/api/admin/[id]/totp/secret", e);
    return NextResponse.json(
      { error: safeErrorMessage(e, "Failed to read TOTP metadata") },
      { status: 500 },
    );
  }
}
