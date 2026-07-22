import { NextResponse } from "next/server";
import { prisma } from "@/lib/db";
import { getSession } from "@/lib/auth/session";
import { apiError } from "@/lib/logger";
import { safeErrorMessage } from "@/lib/safe-error";
import { auditLogLimiter, buildRateLimitKey, rateLimitResponse } from "@/lib/rate-limit";
import { parseAuditMetadata } from "@/lib/audit-log";

const HIDDEN_AUDIT_ACTIONS = ["admin.sync", "admin.create"] as const;

export async function GET(request: Request) {
  try {
    const session = await getSession();
    if (!session) return NextResponse.json({ error: "Unauthorized" }, { status: 401 });

    const key = buildRateLimitKey(request, session.sub, "audit-logs");
    const limit = auditLogLimiter.check(key);
    if (!limit.allowed) return rateLimitResponse(limit.resetMs);

    const { searchParams } = new URL(request.url);
    const takeParam = Number(searchParams.get("take") || "30");
    const take = Math.max(1, Math.min(100, Number.isFinite(takeParam) ? takeParam : 30));
    const cursor = searchParams.get("cursor") || undefined;

    const logs = await prisma.auditLog.findMany({
      where: {
        userId: session.sub,
        action: { notIn: [...HIDDEN_AUDIT_ACTIONS] },
      },
      orderBy: { createdAt: "desc" },
      take: take + 1,
      ...(cursor ? { cursor: { id: cursor }, skip: 1 } : {}),
    });

    const hasMore = logs.length > take;
    const items = hasMore ? logs.slice(0, take) : logs;
    const nextCursor = hasMore ? items[items.length - 1]?.id : undefined;

    return NextResponse.json({
      logs: items.map((log) => ({
        id: log.id,
        actorEmail: log.actorEmail,
        action: log.action,
        targetType: log.targetType,
        targetId: log.targetId,
        status: log.status,
        message: log.message,
        metadata: parseAuditMetadata(log.metadata),
        createdAt: log.createdAt,
      })),
      nextCursor,
    });
  } catch (error) {
    apiError("GET /api/audit-logs", error);
    return NextResponse.json(
      { error: safeErrorMessage(error, "Failed to load audit logs") },
      { status: 500 },
    );
  }
}

export async function DELETE(request: Request) {
  try {
    const session = await getSession();
    if (!session) return NextResponse.json({ error: "Unauthorized" }, { status: 401 });

    const key = buildRateLimitKey(request, session.sub, "audit-logs-delete");
    const limit = auditLogLimiter.check(key);
    if (!limit.allowed) return rateLimitResponse(limit.resetMs);

    const result = await prisma.auditLog.deleteMany({
      where: { userId: session.sub },
    });

    return NextResponse.json({ deletedCount: result.count });
  } catch (error) {
    apiError("DELETE /api/audit-logs", error);
    return NextResponse.json(
      { error: safeErrorMessage(error, "Failed to clean audit logs") },
      { status: 500 },
    );
  }
}
