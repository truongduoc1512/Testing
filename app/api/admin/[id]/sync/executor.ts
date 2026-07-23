import { prisma } from "@/lib/db";
import { isDeadGoogleErrorMessage, syncFamilyGroup } from "@/lib/scanner/google-one";
import { syncResult } from "@/lib/scanner/sync";
import { safeDecrypt } from "@/lib/crypto";
import { fetchCreditActivity } from "@/lib/scanner/credit-activity";
import { apiError } from "@/lib/logger";
import type { AdminAccount } from "@prisma/client";

const TRANSIENT_DB_ERROR_PATTERNS = [
  "TransientTransactionError",
  "forcibly closed by the remote host",
  "os error 10054",
  "ECONNRESET",
  "timed out",
  "Raw query failed",
];

function isTransientDbError(error: unknown): boolean {
  const message = error instanceof Error ? error.message : String(error);
  return TRANSIENT_DB_ERROR_PATTERNS.some((p) => message.includes(p));
}

function sleep(ms: number) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

async function withTransientDbRetry<T>(fn: () => Promise<T>): Promise<T> {
  let lastError: unknown;
  for (let attempt = 0; attempt < 3; attempt++) {
    try {
      return await fn();
    } catch (error) {
      lastError = error;
      const canRetry = attempt < 2 && isTransientDbError(error);
      if (!canRetry) throw error;
      await sleep(300 * (attempt + 1));
    }
  }
  throw lastError;
}

async function getDbMembers(adminId: string, activeOnly = false) {
  return withTransientDbRetry(() =>
    prisma.familyMember.findMany({
      where: { adminId, status: activeOnly ? "active" : { not: "removed" } },
      orderBy: { createdAt: "asc" },
      select: {
        email: true,
        name: true,
        role: true,
        status: true,
        joinedAt: true,
      },
    }),
  );
}

async function syncGoogle(admin: AdminAccount, skipCredit: boolean) {
  const result = await syncFamilyGroup(
    admin.id,
    admin.email,
    safeDecrypt(admin.googlePassword),
    safeDecrypt(admin.totpSecret),
    skipCredit,
    admin.familyType || "ultra",
  );

  if (result.status === "failed" && isDeadGoogleErrorMessage(result.error || "")) {
    await prisma.adminAccount.update({
      where: { id: admin.id },
      data: {
        accountStatus: "dead",
        lastSyncAt: new Date(),
        lastSyncStatus: "failed",
        lastSyncError: "Sync failed",
      },
    });
    return {
      status: 500,
      body: {
        error: "Sync failed",
        accountStatus: "dead",
      },
    };
  }

  let creditData = null;
  if (!skipCredit) {
    try {
      const gs = await prisma.googleSession.findUnique({ where: { adminId: admin.id } });
      if (gs?.cookies) {
        const cookies = JSON.parse(safeDecrypt(gs.cookies));
        if (Array.isArray(cookies) && cookies.length > 0) {
          creditData = await fetchCreditActivity(cookies);
        }
      }
    } catch (e) {
      apiError("syncGoogle", e, { context: "creditData fetch" });
    }
  }

  await syncResult(admin.id, result, creditData?.familyMembers);
  await prisma.adminAccount.update({
    where: { id: admin.id },
    data: {
      ...(result.status !== "failed" ? { accountStatus: "live" } : {}),
      lastSyncStatus: result.status,
      lastSyncError: result.status === "failed" ? "Sync failed." : "",
    },
  });
  const members = await getDbMembers(admin.id, true);

  return {
    status: 200,
    body: {
      sync: {
        status: result.status,
        membersFound: result.members.length,
        duration: result.duration,
        error: result.error ? "Sync failed." : undefined,
      },
      members,
      stats: { memberCount: members.length, manager: result.manager },
      credit: creditData,
    },
  };
}

export async function executeSyncForAdmin(
  adminId: string,
  familyType: string,
): Promise<{ status: number; body: unknown }> {
  const admin = await withTransientDbRetry(() =>
    prisma.adminAccount.findUnique({ where: { id: adminId } }),
  );
  if (!admin) return { status: 404, body: { error: "Not found" } };

  if (!["ultra", "pro", "youtube"].includes(familyType)) {
    return { status: 400, body: { error: "Unsupported account type" } };
  }
  if (familyType === "youtube" || familyType === "pro") return syncGoogle(admin, true);
  return syncGoogle(admin, false);
}
