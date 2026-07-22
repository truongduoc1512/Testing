import { NextResponse } from "next/server";
import { prisma } from "@/lib/db";
import { enqueueSyncJob, getAdminSyncStatus } from "@/lib/sync-queue";
import { apiError } from "@/lib/logger";
import { safeErrorMessage } from "@/lib/safe-error";
import { requireOwnedAdminAccess } from "@/lib/api/admin-route";
import { syncLimiter } from "@/lib/rate-limit";

export async function POST(
  request: Request,
  { params }: { params: Promise<{ id: string }> },
) {
  try {
    const guard = await requireOwnedAdminAccess({
      request,
      params,
      action: "sync",
      limiter: syncLimiter,
    });
    if ("response" in guard) return guard.response;
    const { session, adminId } = guard;

    const admin = await prisma.adminAccount.findFirst({
      where: { id: adminId, createdBy: session.sub },
    });
    if (!admin) return NextResponse.json({ error: "Not found" }, { status: 404 });

    const familyType = admin.familyType || "ultra";

    const result = await enqueueSyncJob(adminId, familyType);
    return NextResponse.json(result.body, { status: result.status });
  } catch (e) {
    apiError("POST /api/admin/[id]/sync", e);
    return NextResponse.json(
      { error: safeErrorMessage(e, "Failed to enqueue sync") },
      { status: 500 },
    );
  }
}

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
    });
    if (!admin) return NextResponse.json({ error: "Not found" }, { status: 404 });

    return NextResponse.json(getAdminSyncStatus(adminId));
  } catch (e) {
    apiError("/api/admin/[id]/sync", e);
    return NextResponse.json(
      { error: safeErrorMessage(e, "Server error") },
      { status: 500 },
    );
  }
}
