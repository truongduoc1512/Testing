export const NEAR_EXPIRY_DAYS = 7;

export type StatusBadgeKind =
  | "active"
  | "needAction"
  | "full"
  | "dead"
  | "expired"
  | "nearExpired"
  | "noFamily"
  | "syncFailed"
  | "syncing"
  | "unsynced"
  | "unknown";

export const STATUS_BADGE_META: Record<
  StatusBadgeKind,
  { label: string; className: string }
> = {
  active: {
    label: "Hoạt động",
    className: "border-emerald-500/25 bg-emerald-500/10 text-emerald-300",
  },
  needAction: {
    label: "Cần xử lý",
    className: "border-amber-500/25 bg-amber-500/10 text-amber-300",
  },
  full: {
    label: "Full",
    className: "border-red-500/25 bg-red-500/10 text-red-300",
  },
  dead: {
    label: "Dead",
    className: "border-red-500/30 bg-red-500/15 text-red-300",
  },
  expired: {
    label: "Hết hạn",
    className: "border-red-500/25 bg-red-500/10 text-red-300",
  },
  nearExpired: {
    label: "Gần hạn",
    className: "border-orange-500/25 bg-orange-500/10 text-orange-300",
  },
  noFamily: {
    label: "Chưa có gia đình",
    className: "border-sky-500/25 bg-sky-500/10 text-sky-300",
  },
  syncFailed: {
    label: "Sync failed",
    className: "border-red-500/25 bg-red-500/10 text-red-300",
  },
  syncing: {
    label: "Đang đồng bộ",
    className: "border-blue-500/25 bg-blue-500/10 text-blue-300",
  },
  unsynced: {
    label: "Chưa đồng bộ",
    className: "border-slate-500/25 bg-slate-500/10 text-slate-300",
  },
  unknown: {
    label: "Không xác định",
    className: "border-slate-500/25 bg-slate-500/10 text-slate-300",
  },
};
