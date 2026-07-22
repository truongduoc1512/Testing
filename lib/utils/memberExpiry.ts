import { daysUntil, expiryColor, expiryLabel } from "./date";

type MemberExpiryLike = {
  endDate: string | Date | null | undefined;
  role: string;
  status: string;
  renewed?: boolean;
};

export type MemberExpiryMeta = {
  daysLeft: number;
  colorClass: string;
  badgeClass: string;
  label: string;
};

function getExpiryBadgeClass(daysLeft: number): string {
  if (daysLeft <= 0) {
    return "border border-red-500/25 bg-red-500/10 text-red-300";
  }
  if (daysLeft <= 7) {
    return "border border-orange-500/25 bg-orange-500/10 text-orange-300";
  }
  if (daysLeft <= 30) {
    return "border border-yellow-500/25 bg-yellow-500/10 text-yellow-300";
  }
  return "border border-emerald-500/25 bg-emerald-500/10 text-emerald-300";
}

function getRenewedLabel(daysLeft: number | null): string {
  if (daysLeft === null || !Number.isFinite(daysLeft)) {
    return "Đã gia hạn";
  }
  return `Đã gia hạn · ${expiryLabel(daysLeft).toLowerCase()}`;
}

export function getMemberExpiryMeta(
  member: MemberExpiryLike,
  fallbackEndDate?: string | Date | null,
): MemberExpiryMeta | null {
  const effectiveEndDate = member.endDate ?? fallbackEndDate;
  if (member.role.includes("MANAGER")) return null;
  if (member.status === "invited") return null;

  if (!effectiveEndDate) {
    if (!member.renewed) return null;
    return {
      daysLeft: Number.POSITIVE_INFINITY,
      colorClass: "text-emerald-300",
      badgeClass: "border border-emerald-500/25 bg-emerald-500/10 text-emerald-300",
      label: getRenewedLabel(null),
    };
  }

  const daysLeft = daysUntil(effectiveEndDate);
  if (!Number.isFinite(daysLeft)) return null;

  if (daysLeft <= 0) {
    return {
      daysLeft,
      colorClass: "text-red-300",
      badgeClass: "border border-red-500/25 bg-red-500/10 text-red-300",
      label: expiryLabel(daysLeft),
    };
  }

  if (member.renewed) {
    return {
      daysLeft,
      colorClass: "text-emerald-300",
      badgeClass: "border border-emerald-500/25 bg-emerald-500/10 text-emerald-300",
      label: getRenewedLabel(daysLeft),
    };
  }

  return {
    daysLeft,
    colorClass: expiryColor(daysLeft),
    badgeClass: getExpiryBadgeClass(daysLeft),
    label: expiryLabel(daysLeft),
  };
}
