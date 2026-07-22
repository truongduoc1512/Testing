import { NextResponse } from "next/server";
import { prisma } from "@/lib/db";
import { apiError } from "@/lib/logger";
import { hashKey } from "@/lib/crypto";
import { safeErrorMessage } from "@/lib/safe-error";
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
      select: { id: true, email: true, displayName: true },
    });
    if (!admin) return NextResponse.json({ error: "Not found" }, { status: 404 });

    const logs = await prisma.creditLog.findMany({
      where: { adminId },
      orderBy: { createdAt: "desc" },
      take: 50,
    });

    return NextResponse.json({
      logs: logs.map((l) => ({
        id: `log_${hashKey(l.id).slice(0, 16)}`,
        memberName: l.memberName,
        creditBefore: l.creditBefore,
        creditAfter: l.creditAfter,
        diff: l.diff,
        createdAt: l.createdAt,
      })),
    });
  } catch (e) {
    apiError("/api/admin/[id]/credit-logs", e);
    return NextResponse.json(
      { error: safeErrorMessage(e, "Server error") },
      { status: 500 },
    );
  }
}
