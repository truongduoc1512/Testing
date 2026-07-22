import { NextResponse } from "next/server";
import { prisma } from "@/lib/db";
import { safeDecrypt } from "@/lib/crypto";
import { apiError } from "@/lib/logger";
import { safeErrorMessage } from "@/lib/safe-error";
import { get2FACode } from "@/lib/scanner/scanner-utils";
import { googleClock } from "@/lib/time/google-clock-instance";
import { requireOwnedAdminAccess } from "@/lib/api/admin-route";

export async function GET(
  request: Request,
  { params }: { params: Promise<{ id: string }> },
) {
  try {
    const guard = await requireOwnedAdminAccess({
      request,
      params,
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

    const totpEnabled = Boolean(admin.has2FA || admin.totpSecret);
    if (!totpEnabled) {
      return NextResponse.json({
        totpEnabled: false,
        remaining: null,
        period: 30,
        lastUpdatedAt: admin.updatedAt?.toISOString() ?? null,
      });
    }

    await googleClock.ensureFresh();

    const ts = googleClock.now();
    const remaining = 30 - (Math.floor(ts / 1000) % 30);
    const secret = safeDecrypt(admin.totpSecret || "");
    const code = secret ? await get2FACode(secret) : null;
    const nextCode = secret ? await get2FACode(secret, ts + 30_000) : null;

    return NextResponse.json({
      totpEnabled: true,
      code,
      nextCode,
      remaining,
      period: 30,
      serverTime: ts,
      lastUpdatedAt: admin.updatedAt?.toISOString() ?? null,
    });
  } catch (e) {
    apiError("/api/admin/[id]/totp", e);
    return NextResponse.json(
      { error: safeErrorMessage(e, "Failed to read TOTP status") },
      { status: 500 },
    );
  }
}
