import { prisma } from "@/lib/db";
import type { SyncResult } from "@/lib/scanner/google-one";
import type { FamilyMemberCredit } from "@/lib/scanner/credit-activity";

const TRANSIENT_DB_ERROR_PATTERNS = [
  "TransientTransactionError",
  "forcibly closed by the remote host",
  "os error 10054",
  "ECONNRESET",
  "timed out",
  "Raw query failed",
];

function toSafeInt(value: unknown): number | null {
  if (typeof value !== "number" || !Number.isFinite(value)) return null;
  return Math.round(value);
}

function isTransientDbError(error: unknown): boolean {
  const message = error instanceof Error ? error.message : String(error);
  return TRANSIENT_DB_ERROR_PATTERNS.some((p) => message.includes(p));
}

function sleep(ms: number) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

export async function syncResult(
  adminId: string,
  result: SyncResult,
  familyCredits?: FamilyMemberCredit[],
) {
  const now = new Date();

  const runTransaction = () =>
    prisma.$transaction(
      async (tx) => {
        if (result.members.length > 0) {
          const existing = await tx.familyMember.findMany({
            where: { adminId },
          });
          const existingByEmail = new Map(existing.map((m) => [m.email, m]));
          const scannedEmails = new Set(result.members.map((m) => m.email));

          for (const member of result.members) {
            const existingMember = existingByEmail.get(member.email);
            const shouldBackfillJoinedAt =
              member.status === "active" && !existingMember?.joinedAt;

            await tx.familyMember.upsert({
              where: {
                adminId_email: { adminId, email: member.email },
              },
              update: {
                name: member.name || undefined,
                role: member.role,
                status: member.status,
                inviteId: member.inviteId || undefined,
                googleUserId: member.googleUserId || undefined,
                removedAt: null,
                ...(shouldBackfillJoinedAt
                  ? { joinedAt: existingMember?.startDate ?? now }
                  : {}),
              },
              create: {
                adminId,
                email: member.email,
                name: member.name,
                role: member.role,
                status: member.status,
                inviteId: member.inviteId,
                googleUserId: member.googleUserId,
                joinedAt: member.status === "active" ? now : null,
              },
            });
          }

          for (const ex of existing) {
            if (!scannedEmails.has(ex.email)) {
              if (ex.status === "invited" || ex.status === "removed") {
                await tx.familyMember.delete({ where: { id: ex.id } });
              } else if (ex.status === "active") {
                await tx.familyMember.update({
                  where: { id: ex.id },
                  data: { status: "removed", removedAt: now },
                });
              }
            }
          }
        }

        const activeMembers = await tx.familyMember.count({
          where: { adminId, status: "active" },
        });

        const admin = await tx.adminAccount.findUnique({
          where: { id: adminId },
          select: { monthlyCredit: true },
        });
        const normalizedCredits = toSafeInt(result.credits);
        const autoSetMonthly =
          admin?.monthlyCredit === 0 && normalizedCredits !== null
            ? normalizedCredits
            : undefined;

        if (familyCredits && familyCredits.length > 0) {
          const dbMembers = await tx.familyMember.findMany({
            where: { adminId },
            select: { id: true, name: true, lastCreditUsed: true },
          });
          const memberMap = new Map(dbMembers.map((m) => [m.name, m]));

          for (const fc of familyCredits) {
            const dbMember = memberMap.get(fc.name);
            if (!dbMember) continue;
            const normalizedUsed = toSafeInt(fc.totalUsed);
            if (normalizedUsed === null || normalizedUsed === 0) continue;

            const oldUsed = dbMember.lastCreditUsed;
            const newUsed = normalizedUsed;
            const diff = newUsed - oldUsed;

            if (diff !== 0) {
              await tx.creditLog.create({
                data: {
                  adminId,
                  memberName: fc.name,
                  creditBefore: oldUsed,
                  creditAfter: newUsed,
                  diff,
                },
              });
            }

            await tx.familyMember.update({
              where: { id: dbMember.id },
              data: { lastCreditUsed: newUsed },
            });
          }
        }

        await tx.adminAccount.update({
          where: { id: adminId },
          data: {
            memberCount: activeMembers,
            ...(result.planName ? { planName: result.planName } : {}),
            ...(result.planExpiresAt ? { planExpiresAt: result.planExpiresAt } : {}),
            ...(normalizedCredits !== null ? { remainingCredit: normalizedCredits } : {}),
            ...(typeof result.usedStorageMB === "number"
              ? { usedStorageMB: Math.max(0, Math.round(result.usedStorageMB)) }
              : {}),
            ...(autoSetMonthly !== undefined ? { monthlyCredit: autoSetMonthly } : {}),
            lastSyncAt: now,
          },
        });

        return { activeMembers };
      },
      { maxWait: 10000, timeout: 30000 },
    );

  let lastError: unknown;
  for (let attempt = 0; attempt < 3; attempt++) {
    try {
      return await runTransaction();
    } catch (error) {
      lastError = error;
      const canRetry = attempt < 2 && isTransientDbError(error);
      if (!canRetry) throw error;
      await sleep(300 * (attempt + 1));
    }
  }

  throw lastError;
}
