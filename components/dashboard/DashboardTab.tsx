"use client";
import { useEffect, useState } from "react";
import { motion, AnimatePresence } from "framer-motion";
import {
  Users,
  History,
  Crown,
  Youtube,
  ChevronDown,
  ChevronRight,
  Shield,
  Plus,
  RefreshCw,
  X,
} from "lucide-react";
import SummaryCardComponent from "./SummaryCard";
import AuditPaginationBar from "./AuditPaginationBar";
import { StatusBadge } from "@/components/ui/StatusBadge";
import { useAuditLogs, type AuditLogItem } from "@/hooks";
import type {
  SummaryCard,
  AdminRow,
  CreditLogEntry,
  CreditActivity,
} from "@/hooks/useDashboardData";
import {
  getMaxMembers,
  isUltraType,
  isGoogleType,
  daysUntil,
  formatDateVN,
  expiryColor,
  expiryLabel,
  formatStorage,
  formatAuditAction,
  formatAuditStatus,
  getAuditStatusBadgeKind,
} from "@/lib/utils";

interface Props {
  cards: SummaryCard[];
  initialLoading: boolean;
  admins: AdminRow[];
  creditLogs: CreditLogEntry[];
  creditActivities: CreditActivity[];
  goToFamily: () => void;
  setSelectedAdminId: (id: string) => void;
  onAddAdmin: (familyType: string) => void;
}

const FAMILY_GROUPS = [
  {
    id: "ultra",
    label: "Fam Ultra",
    icon: Crown,
    color: "text-violet-400",
    accent: "violet",
    gradient: "from-violet-500/20 to-purple-500/20",
    badge: "bg-violet-500/15 text-violet-400",
  },
  {
    id: "pro",
    label: "Fam Pro",
    icon: Crown,
    color: "text-cyan-400",
    accent: "cyan",
    gradient: "from-cyan-500/20 to-blue-500/20",
    badge: "bg-cyan-500/15 text-cyan-400",
  },
  {
    id: "youtube",
    label: "Fam YouTube",
    icon: Youtube,
    color: "text-red-400",
    accent: "red",
    gradient: "from-red-500/20 to-rose-500/20",
    badge: "bg-red-500/15 text-red-400",
  },
] as const;

function AdminMiniCard({
  admin,
  group,
  onClick,
}: {
  admin: AdminRow;
  group: (typeof FAMILY_GROUPS)[number];
  onClick: () => void;
}) {
  const maxMembers = getMaxMembers(admin.familyType);
  const isUltra = isUltraType(admin.familyType);
  const isGoogle = isGoogleType(admin.familyType);
  const initial =
    admin.fullName?.charAt(0)?.toUpperCase() || admin.email.charAt(0).toUpperCase();
  const usedCredit = Math.max(0, admin.monthlyCredit - admin.remainingCredit);
  const creditPercent =
    admin.monthlyCredit > 0 ? Math.round((usedCredit / admin.monthlyCredit) * 100) : 0;

  const avatarColors: Record<string, string> = {
    violet: "bg-violet-600",
    cyan: "bg-cyan-600",
    red: "bg-red-600",
    emerald: "bg-emerald-600",
  };
  const avatarBg = avatarColors[group.accent] || "bg-indigo-500";

  return (
    <motion.div
      initial={{ opacity: 0, scale: 0.95 }}
      animate={{ opacity: 1, scale: 1 }}
      transition={{ duration: 0.2 }}
      onClick={onClick}
      className="group cursor-pointer rounded-xl border border-white/[0.06] bg-white/[0.03] p-4 transition-all duration-200 hover:border-white/[0.12] hover:bg-white/[0.05] hover:shadow-[0_0_20px_rgba(124,58,237,0.06)]"
    >
      <div className="mb-3 flex items-center gap-3">
        <div
          className={`flex h-9 w-9 shrink-0 items-center justify-center rounded-lg text-sm font-bold text-white shadow-md ${avatarBg}`}
        >
          {initial}
        </div>
        <div className="min-w-0 flex-1">
          <p className="truncate text-[13px] font-bold text-white">{admin.fullName}</p>
          <p className="truncate text-[11px] text-slate-400">{admin.email}</p>
        </div>
        {admin.has2FA && <Shield className="h-3.5 w-3.5 shrink-0 text-emerald-400" />}
      </div>

      <div className="flex items-center justify-between text-xs">
        <div className="flex items-center gap-1.5">
          <Users className="h-3.5 w-3.5 text-slate-400" />
          {admin.memberCount === 0 && !isUltra ? (
            admin.lastSyncAt ? (
              <span className="font-semibold text-amber-400">Chưa tạo</span>
            ) : (
              <span className="font-semibold text-blue-400">Chưa sync</span>
            )
          ) : (
            <>
              <span className="font-bold text-white">{admin.memberCount}</span>
              <span className="text-slate-400">/{maxMembers}</span>
              {admin.memberCount >= maxMembers && (
                <span className="rounded bg-red-500/20 px-1.5 py-0.5 text-[10px] font-bold text-red-400">
                  Full
                </span>
              )}
            </>
          )}
        </div>

        <div className="flex items-center gap-2">
          {isUltra && (
            <>
              <span className="font-bold text-emerald-400">
                {admin.remainingCredit.toLocaleString("vi-VN")}
              </span>
              <div className="h-1.5 w-12 overflow-hidden rounded-full bg-white/[0.08]">
                <svg
                  viewBox="0 0 100 4"
                  className="h-full w-full"
                  preserveAspectRatio="none"
                  aria-hidden="true"
                >
                  <rect
                    x="0"
                    y="0"
                    width={Math.max(0, Math.min(100, creditPercent))}
                    height="4"
                    rx="2"
                    className={creditPercent > 80 ? "fill-red-500" : "fill-emerald-500"}
                  />
                </svg>
              </div>
            </>
          )}

          {isGoogle && (
            <span className="text-slate-300">
              <span className="font-semibold">{formatStorage(admin.usedStorageMB)}</span>
              <span className="text-slate-500">
                {" "}
                / {admin.planName || `${admin.storageTB}TB`}
              </span>
            </span>
          )}

        </div>
      </div>

      {admin.planExpiresAt && (
        <div className="mt-2 text-right text-[11px]">
          {(() => {
            const days = daysUntil(admin.planExpiresAt);
            return (
              <span
                className={`font-semibold ${expiryColor(days)}`}
                title={formatDateVN(admin.planExpiresAt)}
              >
                {expiryLabel(days)}
              </span>
            );
          })()}
        </div>
      )}
    </motion.div>
  );
}

function GroupSection({
  group,
  admins,
  onAdminClick,
  onAddAdmin,
}: {
  group: (typeof FAMILY_GROUPS)[number];
  admins: AdminRow[];
  onAdminClick: (id: string) => void;
  onAddAdmin: (familyType: string) => void;
}) {
  const [collapsed, setCollapsed] = useState(true);
  const Icon = group.icon;

  const totalMembers = admins.reduce((s, a) => s + a.memberCount, 0);
  const totalCredit = isUltraType(group.id)
    ? admins.reduce((s, a) => s + a.remainingCredit, 0)
    : null;
  const fullCount = admins.filter(
    (a) => a.memberCount >= getMaxMembers(a.familyType),
  ).length;

  return (
    <div className="overflow-hidden rounded-xl border border-white/[0.06] bg-white/[0.02]">
      <button
        onClick={() => setCollapsed(!collapsed)}
        className={`flex w-full items-center gap-3 px-4 py-3 text-left transition-colors hover:bg-white/[0.03]`}
      >
        <div
          className={`flex h-8 w-8 items-center justify-center rounded-lg bg-gradient-to-br ${group.gradient}`}
        >
          <Icon className={`h-4 w-4 ${group.color}`} />
        </div>
        <div className="min-w-0 flex-1">
          <div className="flex items-center gap-2">
            <span className="text-sm font-semibold text-white">{group.label}</span>
            <span
              className={`rounded-md px-1.5 py-0.5 text-[10px] font-bold ${group.badge}`}
            >
              {admins.length}
            </span>
          </div>
          <div className="flex items-center gap-3 text-[10px] text-slate-500">
            <span>{totalMembers} thành viên</span>
            {totalCredit !== null && (
              <span>· {totalCredit.toLocaleString("vi-VN")} credits</span>
            )}
            {fullCount > 0 && <span className="text-red-400">· {fullCount} đầy</span>}
          </div>
        </div>
        <div
          className="shrink-0 rounded-lg p-1.5 text-slate-400 transition-all hover:bg-white/[0.08] hover:text-white"
          onClick={(e) => {
            e.stopPropagation();
            onAddAdmin(group.id);
          }}
          title={`Thêm acc ${group.label}`}
        >
          <Plus className="h-4 w-4" />
        </div>
        <div className="shrink-0 text-slate-500 transition-transform">
          {collapsed ? (
            <ChevronRight className="h-4 w-4" />
          ) : (
            <ChevronDown className="h-4 w-4" />
          )}
        </div>
      </button>

      <AnimatePresence>
        {!collapsed && (
          <motion.div
            initial={{ height: 0, opacity: 0 }}
            animate={{ height: "auto", opacity: 1 }}
            exit={{ height: 0, opacity: 0 }}
            transition={{ duration: 0.2 }}
            className="overflow-hidden"
          >
            <div className="grid grid-cols-1 gap-3 px-3 pb-3 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
              {admins.map((admin) => (
                <AdminMiniCard
                  key={admin.id}
                  admin={admin}
                  group={group}
                  onClick={() => onAdminClick(admin.id)}
                />
              ))}
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
}

function RecentAuditRow({
  log,
  onOpen,
}: {
  log: AuditLogItem;
  onOpen: (log: AuditLogItem) => void;
}) {
  const date = new Date(log.createdAt);
  const displayDate = Number.isNaN(date.getTime())
    ? log.createdAt
    : date.toLocaleDateString("vi-VN");

  return (
    <button
      type="button"
      onClick={() => onOpen(log)}
      aria-label={`Xem chi tiết nhật ký ${formatAuditAction(log.action)}`}
      className="grid w-full grid-cols-[2.25rem_minmax(0,1fr)] gap-3 rounded-lg px-2 py-3 text-left transition-colors hover:bg-white/[0.03] focus:outline-none focus:ring-2 focus:ring-cyan-500/40 sm:grid-cols-[2.25rem_minmax(0,1fr)_auto]"
    >
      <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-violet-500/15 text-violet-300">
        <History className="h-4 w-4" />
      </div>
      <div className="min-w-0 flex-1">
        <div className="flex flex-wrap items-center gap-2">
          <p className="truncate text-sm font-semibold text-white">
            {formatAuditAction(log.action)}
          </p>
          <StatusBadge
            kind={getAuditStatusBadgeKind(log.status)}
            label={formatAuditStatus(log.status)}
          />
        </div>
        <p className="mt-1 line-clamp-2 text-xs text-slate-400">
          {log.message || `${log.targetType} · ${log.action}`}
        </p>
      </div>
      <span className="col-start-2 text-[11px] text-slate-500 sm:col-start-auto sm:shrink-0">
        {displayDate}
      </span>
    </button>
  );
}

function AuditLogDetailModal({
  log,
  onClose,
}: {
  log: AuditLogItem;
  onClose: () => void;
}) {
  const badgeKind = getAuditStatusBadgeKind(log.status);
  const timeParts = getAuditTimeParts(log.createdAt);

  useEffect(() => {
    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === "Escape") onClose();
    };
    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [onClose]);

  return (
    <div
      className="fixed inset-0 z-[10000] flex items-end bg-black/65 p-3 backdrop-blur-sm sm:items-center sm:justify-center sm:p-4"
      onClick={onClose}
    >
      <div
        role="dialog"
        aria-modal="true"
        aria-labelledby="dashboard-audit-log-detail-title"
        className="max-h-[calc(100dvh-1.5rem)] w-full overflow-y-auto rounded-2xl border border-white/[0.08] bg-[#101729] p-4 shadow-2xl sm:max-w-lg sm:p-5"
        onClick={(event) => event.stopPropagation()}
      >
        <div className="flex items-start gap-3">
          <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl border border-violet-500/25 bg-violet-500/10 text-violet-300">
            <History className="h-5 w-5" />
          </div>
          <div className="min-w-0 flex-1">
            <h3
              id="dashboard-audit-log-detail-title"
              className="break-words text-base font-bold text-white"
            >
              {formatAuditAction(log.action)}
            </h3>
            <div className="mt-2 flex flex-wrap items-center gap-2">
              <StatusBadge kind={badgeKind} label={formatAuditStatus(log.status)} />
              <span className="text-xs text-slate-500">
                {timeParts.time} {timeParts.date}
              </span>
            </div>
          </div>
          <button
            type="button"
            onClick={onClose}
            aria-label="Đóng chi tiết nhật ký"
            className="inline-flex min-h-11 min-w-11 items-center justify-center rounded-xl text-slate-400 transition-colors hover:bg-white/[0.06] hover:text-white"
          >
            <X className="h-4 w-4" />
          </button>
        </div>

        <div className="mt-4 rounded-xl border border-white/[0.06] bg-white/[0.03] p-3">
          <p className="whitespace-pre-wrap break-words text-sm leading-6 text-slate-200">
            {log.message || `${log.targetType} · ${log.action}`}
          </p>
        </div>
      </div>
    </div>
  );
}

function getAuditTimeParts(createdAt: string) {
  const date = new Date(createdAt);
  if (Number.isNaN(date.getTime())) {
    return { time: createdAt || "Không có dữ liệu", date: "Không có dữ liệu" };
  }

  return {
    time: date.toLocaleTimeString("vi-VN", {
      hour: "2-digit",
      minute: "2-digit",
      second: "2-digit",
      hour12: false,
    }),
    date: date.toLocaleDateString("vi-VN"),
  };
}

export default function DashboardTab({
  cards,
  initialLoading,
  admins,
  creditLogs,
  creditActivities,
  goToFamily,
  setSelectedAdminId,
  onAddAdmin,
}: Props) {
  const [selectedAuditLog, setSelectedAuditLog] = useState<AuditLogItem | null>(null);
  const {
    logs: auditLogs,
    loading: auditLoading,
    refresh: refreshAudit,
    page: auditPage,
    hasNext: auditHasNext,
    hasPrev: auditHasPrev,
    goNext: auditGoNext,
    goPrev: auditGoPrev,
  } = useAuditLogs(10);
  const grouped = FAMILY_GROUPS.map((group) => ({
    group,
    admins: admins.filter((a) => (a.familyType || "ultra") === group.id),
  })).filter((g) => g.admins.length > 0);
  const hasActivityData =
    auditLoading ||
    auditLogs.length > 0 ||
    creditLogs.length > 0 ||
    creditActivities.length > 0;
  const totalCreditTransactions = creditActivities.reduce(
    (sum, activity) => sum + Math.max(0, activity.totalTransactions || 0),
    0,
  );
  const totalRemainingCredit = creditActivities.reduce(
    (sum, activity) => sum + Math.max(0, activity.remaining || 0),
    0,
  );
  const latestCreditRefresh = creditActivities.reduce<number | null>(
    (latest, activity) => {
      if (!activity.refreshDate) return latest;
      const ts =
        activity.refreshDate < 10_000_000_000
          ? activity.refreshDate * 1000
          : activity.refreshDate;
      if (!Number.isFinite(ts)) return latest;
      return latest === null ? ts : Math.max(latest, ts);
    },
    null,
  );
  const latestCreditRefreshLabel = latestCreditRefresh
    ? new Date(latestCreditRefresh).toLocaleString("vi-VN")
    : "Chưa có dữ liệu";

  return (
    <>
      <div className="mb-6 grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
        {initialLoading && cards.length === 0
          ? Array.from({ length: 4 }).map((_, i) => (
              <div key={i} className="glass-card animate-pulse rounded-2xl p-5">
                <div className="mb-3 h-3 w-20 rounded bg-white/[0.06]" />
                <div className="mb-1 h-8 w-16 rounded bg-white/[0.08]" />
                <div className="h-2 w-24 rounded bg-white/[0.04]" />
              </div>
            ))
          : cards.map((card) => <SummaryCardComponent key={card.id} card={card} />)}
      </div>

      {initialLoading ? (
        <>
          <div className="glass-card mb-6 animate-pulse rounded-2xl p-5">
            <div className="mb-4 flex items-center justify-between">
              <div className="h-5 w-40 rounded bg-white/[0.06]" />
              <div className="h-8 w-28 rounded-xl bg-white/[0.06]" />
            </div>
            <div className="space-y-3">
              <div className="h-12 w-full rounded-xl bg-white/[0.03]" />
              <div className="h-12 w-full rounded-xl bg-white/[0.03]" />
            </div>
          </div>

          <div className="glass-card animate-pulse rounded-2xl p-5">
            <div className="mb-4 h-5 w-44 rounded bg-white/[0.06]" />
            <div className="space-y-3">
              <div className="h-10 w-full rounded bg-white/[0.03]" />
              <div className="h-10 w-full rounded bg-white/[0.03]" />
            </div>
          </div>
        </>
      ) : (
        <>
          <motion.section
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.25 }}
            className="glass-card mb-6 rounded-2xl p-5"
          >
            <div className="mb-4 flex items-center justify-between">
              <h2 className="section-header">
                <Users className="h-5 w-5 text-cyan-400" />
                Tổng quan Acc Fam
              </h2>
              <button onClick={goToFamily} className="group btn-violet">
                <Users className="h-3.5 w-3.5 transition-transform duration-200 group-hover:scale-110" />
                Xem danh sách
              </button>
            </div>

            {admins.length === 0 ? (
              <div className="flex flex-col items-center justify-center py-12 text-slate-500">
                <Users className="mb-3 h-10 w-10 text-slate-600" />
                <p className="text-sm">
                  Chưa có admin nào.{" "}
                  <button onClick={goToFamily} className="text-cyan-400 hover:underline">
                    Thêm admin đầu tiên
                  </button>
                </p>
              </div>
            ) : (
              <div className="space-y-3">
                {grouped.map(({ group, admins: groupAdmins }) => (
                  <GroupSection
                    key={group.id}
                    group={group}
                    admins={groupAdmins}
                    onAdminClick={setSelectedAdminId}
                    onAddAdmin={onAddAdmin}
                  />
                ))}
              </div>
            )}
          </motion.section>

          <motion.section
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.25 }}
            className="glass-card rounded-2xl p-5"
          >
            <div className="mb-4 flex items-center justify-between">
              <h2 className="section-header">
                <History className="h-5 w-5 text-slate-400" />
                Hoạt động gần đây
              </h2>
              <button
                type="button"
                onClick={() => void refreshAudit()}
                disabled={auditLoading}
                className="inline-flex min-h-9 items-center gap-2 rounded-xl border border-white/[0.08] bg-white/[0.04] px-3 text-xs font-semibold text-slate-300 transition-colors hover:bg-white/[0.08] disabled:opacity-50"
              >
                <RefreshCw
                  className={`h-3.5 w-3.5 ${auditLoading ? "animate-spin" : ""}`}
                />
                Tải lại
              </button>
            </div>

            {!hasActivityData ? (
              <div className="flex flex-col items-center justify-center py-8 text-slate-500">
                <History className="mb-3 h-8 w-8 text-slate-600" />
                <p className="text-sm">
                  Chưa có hoạt động nào. Đồng bộ để bắt đầu theo dõi.
                </p>
              </div>
            ) : (
              <div className="space-y-5">
                {auditLoading && auditLogs.length === 0 ? (
                  <div className="space-y-2">
                    {Array.from({ length: 3 }).map((_, index) => (
                      <div
                        key={index}
                        className="h-14 animate-pulse rounded-xl bg-white/[0.04]"
                      />
                    ))}
                  </div>
                ) : auditLogs.length > 0 ? (
                  <div className="space-y-1">
                    <h3 className="mb-2 text-xs font-semibold uppercase tracking-wider text-violet-300">
                      Thao tác hệ thống
                    </h3>
                    {auditLogs.map((log) => (
                      <RecentAuditRow
                        key={log.id}
                        log={log}
                        onOpen={setSelectedAuditLog}
                      />
                    ))}
                    <AuditPaginationBar
                      page={auditPage}
                      hasPrev={auditHasPrev}
                      hasNext={auditHasNext}
                      loading={auditLoading}
                      onPrev={() => void auditGoPrev()}
                      onNext={() => void auditGoNext()}
                    />
                  </div>
                ) : null}

                {creditActivities.length > 0 && (
                  <div className="rounded-xl border border-cyan-500/20 bg-cyan-500/5 p-3">
                    <h3 className="mb-1 text-xs font-semibold uppercase tracking-wider text-cyan-300">
                      Hoạt động credit
                    </h3>
                    <p className="text-xs text-slate-300">
                      Remaining là credit còn lại. Transactions là số lượt biến động
                      credit (trừ credit khi dùng hoặc cộng credit khi hoàn/trả).
                    </p>
                    <div className="mt-2 flex flex-wrap gap-3 text-[11px] text-slate-400">
                      <span>
                        Tổng credit còn lại:{" "}
                        <span className="font-semibold text-slate-200">
                          {totalRemainingCredit.toLocaleString("vi-VN")}
                        </span>
                      </span>
                      <span>
                        Tổng transactions:{" "}
                        <span className="font-semibold text-slate-200">
                          {totalCreditTransactions.toLocaleString("vi-VN")}
                        </span>
                      </span>
                      <span>
                        Mốc refresh gần nhất:{" "}
                        <span className="font-semibold text-slate-200">
                          {latestCreditRefreshLabel}
                        </span>
                      </span>
                    </div>
                  </div>
                )}

                {creditLogs.length > 0 && (
                  <div className="space-y-1">
                    <h3 className="mb-2 text-xs font-semibold uppercase tracking-wider text-slate-500">
                      Lịch sử thay đổi credit
                    </h3>
                    {creditLogs.slice(0, 10).map((log) => {
                      const date = new Date(log.createdAt);
                      const dateStr = date.toLocaleDateString("vi-VN");
                      const diffLabel =
                        log.diff > 0
                          ? `+${log.diff}`
                          : log.diff < 0
                            ? `${log.diff}`
                            : "0";
                      return (
                        <div
                          key={log.id}
                          className="flex items-center gap-3 rounded-lg px-2 py-2.5 transition-colors hover:bg-white/[0.03]"
                        >
                          <div
                            className={`flex h-9 w-9 shrink-0 items-center justify-center rounded-full text-sm font-bold ${log.diff < 0 ? "bg-red-500/20 text-red-300" : "bg-emerald-500/20 text-emerald-300"}`}
                          >
                            {log.adminName.charAt(0).toLowerCase()}
                          </div>
                          <div className="min-w-0 flex-1">
                            <p className="truncate text-sm text-slate-200">
                              <span className="font-semibold text-white">
                                {log.adminName}
                              </span>
                            </p>
                            <p className="truncate text-xs text-slate-400">
                              Credit: {log.creditBefore} → {log.creditAfter}
                            </p>
                          </div>
                          <div className="shrink-0 text-right">
                            <span
                              className={`text-sm font-bold ${log.diff < 0 ? "text-red-400" : log.diff > 0 ? "text-emerald-400" : "text-slate-500"}`}
                            >
                              {diffLabel}
                            </span>
                            <p className="text-[11px] text-slate-200">{dateStr}</p>
                          </div>
                        </div>
                      );
                    })}
                  </div>
                )}
              </div>
            )}
          </motion.section>
        </>
      )}
      {selectedAuditLog ? (
        <AuditLogDetailModal
          log={selectedAuditLog}
          onClose={() => setSelectedAuditLog(null)}
        />
      ) : null}
    </>
  );
}
