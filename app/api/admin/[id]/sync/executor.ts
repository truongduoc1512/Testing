import { prisma } from "@/lib/db";
import { isDeadGoogleErrorMessage, syncFamilyGroup } from "@/lib/scanner/google-one";
import { syncResult } from "@/lib/scanner/sync";
import { safeDecrypt } from "@/lib/crypto";
import { fetchCreditActivity } from "@/lib/scanner/credit-activity";
import { isDeadGptErrorMessage } from "@/lib/scanner/gpt-auth";
import { syncGptWorkspace } from "@/lib/scanner/chatgpt";
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

async function syncGpt(admin: AdminAccount) {
  const result = await syncGptWorkspace(
    admin.id,
    admin.email,
    safeDecrypt(admin.googlePassword),
    safeDecrypt(admin.totpSecret),
    () => {},
  );

  if (result.status === "failed") {
    const isDead = isDeadGptErrorMessage(result.error || "");
    if (result.error) {
      apiError("syncGpt", new Error(result.error));
    }
    if (isDead) {
      await prisma.adminAccount.update({
        where: { id: admin.id },
        data: {
          accountStatus: "dead",
          lastSyncAt: new Date(),
          lastSyncStatus: "failed",
          lastSyncError: "Sync failed",
        },
      });
    }
    return {
      status: 500,
      body: {
        error: "Sync failed",
        accountStatus: isDead ? "dead" : undefined,
      },
    };
  }

  const allMembers = [
    ...result.members.map((m) => ({
      name: m.name,
      email: m.email,
      role: m.role === "account-owner" ? "FAMILY MANAGER" : "Member",
      status: "active" as const,
      inviteId: m.id,
    })),
    ...result.invites.map((inv) => ({
      name: inv.email,
      email: inv.email,
      role: "Member",
      status: "invited" as const,
      inviteId: inv.id,
    })),
  ];

  await syncResult(admin.id, {
    status: result.status,
    members: allMembers,
    manager: result.members.find((m) => m.role === "account-owner")?.email || null,
    planName: result.plan,
    planExpiresAt: result.planExpiresAt ? new Date(result.planExpiresAt) : null,
    credits: null,
    error: result.error ? "Sync failed." : "",
    duration: result.duration,
  });

  await prisma.adminAccount.update({
    where: { id: admin.id },
    data: { accountStatus: "live", lastSyncStatus: result.status, lastSyncError: "" },
  });

  const members = await getDbMembers(admin.id);

  return {
    status: 200,
    body: {
      sync: {
        status: result.status,
        membersFound: result.members.length,
        invitesFound: result.invites.length,
        duration: result.duration,
      },
      members,
      stats: {
        memberCount: result.members.length,
        plan: result.plan,
        seatsUsed: result.seatsUsed,
      },
      credit: null,
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

  if (familyType === "gpt") return syncGpt(admin);
  if (familyType === "youtube" || familyType === "pro") return syncGoogle(admin, true);
  return syncGoogle(admin, false);
}
