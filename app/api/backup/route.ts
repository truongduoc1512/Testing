import { NextResponse } from "next/server";
import { getSession } from "@/lib/auth/session";
import { createBackup } from "@/lib/backup";
import { apiError } from "@/lib/logger";
import { ACCOUNT_EMAIL } from "@/lib/env";
import { backupLimiter, buildRateLimitKey, rateLimitResponse } from "@/lib/rate-limit";
import { safeErrorMessage } from "@/lib/safe-error";
import { writeAuditLog } from "@/lib/audit-log";

export async function POST(request: Request) {
  try {
    const session = await getSession();
    if (!session) return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
    const ownerEmail = ACCOUNT_EMAIL.trim().toLowerCase();
    const sessionEmail =
      typeof session.email === "string" ? session.email.trim().toLowerCase() : "";
    if (!ownerEmail || !sessionEmail || sessionEmail !== ownerEmail) {
      return NextResponse.json({ error: "Forbidden" }, { status: 403 });
    }

    const key = buildRateLimitKey(request, session.sub, "backup");
    const limit = backupLimiter.check(key);
    if (!limit.allowed) return rateLimitResponse(limit.resetMs);

    await createBackup();
    await writeAuditLog({
      userId: session.sub,
      actorEmail: session.email,
      action: "backup.create",
      targetType: "backup",
      status: "success",
      message: "Đã tạo backup dữ liệu",
    });
    return NextResponse.json({ ok: true });
  } catch (e) {
    apiError("POST /api/backup", e);
    return NextResponse.json(
      { error: safeErrorMessage(e, "Backup failed") },
      { status: 500 },
    );
  }
}
