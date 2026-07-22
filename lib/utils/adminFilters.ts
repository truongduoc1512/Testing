import { NEAR_EXPIRY_DAYS, type StatusBadgeKind } from "@/lib/constants/status";
import { daysUntil } from "./date";
import { getMaxMembers } from "./family";
import { normalizeAccountStatus } from "./accountStatus";

type ExpirySummary = {
  nearExpiry?: number;
  expired?: number;
  noExpiry?: number;
};

export type AdminListItem = {
  id: string;
  email: string;
  fullName: string;
  memberCount: number;
  familyType: string;
  accountStatus: string;
  lastSyncAt: string | null;
  lastSyncStatus?: string | null;
  lastSyncError?: string | null;
  usedStorageMB: number;
  storageTB: number;
  planName: string;
  planExpiresAt: string | null;
  memberExpiry?: ExpirySummary;
  memberReports?: Array<{
    email: string;
    name: string;
  }>;
};

export function normalizeAdminSearchText(value: string | null | undefined) {
  return (value || "")
    .toLowerCase()
    .normalize("NFD")
    .replace(/\p{Diacritic}/gu, "")
    .trim();
}

export function getAdminSearchText(
  admin: Pick<AdminListItem, "email" | "fullName" | "memberReports">,
) {
  const memberText = (admin.memberReports ?? [])
    .map((member) => `${member.name} ${member.email}`)
    .join(" ");
  return normalizeAdminSearchText(`${admin.fullName} ${admin.email} ${memberText}`);
}

export function findMatchedAdminMember(
  admin: Pick<AdminListItem, "email" | "fullName" | "memberReports">,
  query: string,
) {
  const normalizedQuery = normalizeAdminSearchText(query);
  if (!normalizedQuery) return null;

  const ownerText = normalizeAdminSearchText(`${admin.fullName} ${admin.email}`);
  if (ownerText.includes(normalizedQuery)) return null;

  return (
    admin.memberReports?.find((member) =>
      normalizeAdminSearchText(`${member.name} ${member.email}`).includes(
        normalizedQuery,
      ),
    ) ?? null
  );
}

export function searchAdminsByQuery<T extends AdminListItem>(
  admins: T[],
  query: string,
  limit = 8,
) {
  const normalizedQuery = normalizeAdminSearchText(query);
  if (!normalizedQuery) return [];

  return [...admins]
    .filter((admin) => getAdminSearchText(admin).includes(normalizedQuery))
    .sort((a, b) => (a.fullName || a.email).localeCompare(b.fullName || b.email))
    .slice(0, limit);
}

export function isAdminDead(admin: Pick<AdminListItem, "accountStatus">) {
  return normalizeAccountStatus(admin.accountStatus) === "dead";
}

export function isAdminSyncFailed(
  admin: Pick<AdminListItem, "lastSyncStatus" | "lastSyncError" | "accountStatus">,
) {
  const status = normalizeAccountStatus(admin.lastSyncStatus || admin.accountStatus);
  return Boolean(admin.lastSyncError) || status === "failed" || status === "sync_failed";
}

export function isAdminFull(admin: Pick<AdminListItem, "familyType" | "memberCount">) {
  const maxMembers = getMaxMembers(admin.familyType);
  return Number.isFinite(maxMembers) && admin.memberCount >= maxMembers;
}

export function isAdminNoFamily(admin: Pick<AdminListItem, "memberCount" | "accountStatus">) {
  return !isAdminDead(admin) && admin.memberCount === 0;
}

function getPlanDaysLeft(admin: Pick<AdminListItem, "planExpiresAt">) {
  if (!admin.planExpiresAt) return null;
  const daysLeft = daysUntil(admin.planExpiresAt);
  return Number.isFinite(daysLeft) ? daysLeft : null;
}

export function hasNearExpiry(admin: AdminListItem) {
  const memberNear = (admin.memberExpiry?.nearExpiry ?? 0) > 0;
  const planDaysLeft = getPlanDaysLeft(admin);
  return (
    memberNear ||
    (planDaysLeft !== null && planDaysLeft > 0 && planDaysLeft <= NEAR_EXPIRY_DAYS)
  );
}

export function hasExpired(admin: AdminListItem) {
  const memberExpired = (admin.memberExpiry?.expired ?? 0) > 0;
  const planDaysLeft = getPlanDaysLeft(admin);
  return memberExpired || (planDaysLeft !== null && planDaysLeft <= 0);
}

export function getAdminBadgeKind(admin: AdminListItem): StatusBadgeKind {
  if (isAdminDead(admin)) return "dead";
  if (isAdminSyncFailed(admin)) return "syncFailed";
  if (!admin.lastSyncAt) return "unsynced";
  if (hasExpired(admin)) return "expired";
  if (hasNearExpiry(admin)) return "nearExpired";
  if (isAdminFull(admin)) return "full";
  if (isAdminNoFamily(admin)) return "noFamily";
  return "active";
}
