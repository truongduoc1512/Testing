"use client";
import { RefreshCw } from "lucide-react";
import AnimatedNumber from "./AnimatedNumber";
import {
  getMaxMembers,
  isUltraType,
  isGoogleType,
  formatStorage,
  isSyncableAccountStatus,
  getAdminBadgeKind,
} from "@/lib/utils";
import type { AdminRow } from "@/hooks/useDashboardData";
import { StatusBadge } from "@/components/ui/StatusBadge";

interface Props {
  admin: AdminRow;
  globalSyncing: boolean;
  onSync: (id: string) => void;
  onClick: () => void;
}

function resolveTotalStorageLabel(planName: string, storageTB: number): string {
  const match = planName.match(/([\d]+(?:[.,]\d+)?)\s*(TB|GB)/i);
  if (match) {
    const value = Number(match[1].replace(",", "."));
    if (Number.isFinite(value)) {
      const unit = match[2].toUpperCase();
      const labelValue =
        Math.abs(value - Math.round(value)) < 1e-9 ? String(Math.round(value)) : value.toFixed(1);
      return `${labelValue} ${unit}`;
    }
  }
  if (storageTB > 0) return `${storageTB} TB`;
  return "Không xác định";
}

function resolveTotalStorageMB(planName: string, storageTB: number): number | null {
  const match = planName.match(/([\d]+(?:[.,]\d+)?)\s*(TB|GB)/i);
  if (match) {
    const value = Number(match[1].replace(",", "."));
    if (Number.isFinite(value) && value > 0) {
      const unit = match[2].toUpperCase();
      return unit === "TB" ? value * 1024 * 1024 : value * 1024;
    }
  }
  if (storageTB > 0) return storageTB * 1024 * 1024;
  return null;
}

export default function AdminCard({ admin, onSync, onClick, globalSyncing }: Props) {
  const initial =
    admin.fullName?.charAt(0)?.toUpperCase() || admin.email.charAt(0).toUpperCase();
  const maxMembers = getMaxMembers(admin.familyType);
  const isUltra = isUltraType(admin.familyType);
  const isGoogle = isGoogleType(admin.familyType);
  const usedStorageLabel = formatStorage(admin.usedStorageMB);
  const totalStorageLabel = resolveTotalStorageLabel(admin.planName || "", admin.storageTB);
  const totalStorageMB = resolveTotalStorageMB(admin.planName || "", admin.storageTB);
  const usagePercent =
    totalStorageMB && totalStorageMB > 0
      ? Math.max(0, Math.min(100, (admin.usedStorageMB / totalStorageMB) * 100))
      : null;
  const usagePercentLabel = usagePercent === null ? "--" : `${usagePercent.toFixed(2)}%`;
  const usageRingPercent = usagePercent ?? 0;
  const memberPercent =
    Number.isFinite(maxMembers) && maxMembers > 0
      ? Math.max(0, Math.min(100, (admin.memberCount / maxMembers) * 100))
      : null;
  const memberPercentLabel = memberPercent === null ? "--" : `${Math.round(memberPercent)}%`;
  const memberRingPercent = memberPercent ?? 0;
  const syncDisabled = globalSyncing || !isSyncableAccountStatus(admin.accountStatus);

  const handleSync = (e: React.MouseEvent) => {
    e.stopPropagation();
    if (syncDisabled) return;
    onSync(admin.id);
  };

  return (
    <div
      onClick={onClick}
      className="glass-card relative cursor-pointer overflow-hidden rounded-2xl p-5 transition-all duration-300 hover:border-white/[0.08] hover:shadow-[0_0_24px_rgba(124,58,237,0.08)]"
    >

      <div className="absolute right-3 top-3 flex gap-1">
        <button
          onClick={handleSync}
          disabled={syncDisabled}
          className="group/sync badge-sm bg-emerald-500/15 text-emerald-400 transition-all hover:bg-emerald-500/25 disabled:opacity-50"
        >
          <RefreshCw
            className={`h-2.5 w-2.5 ${globalSyncing ? "animate-spin" : "transition-transform duration-500 group-hover/sync:rotate-180"}`}
          />
          Đồng bộ
        </button>
        <StatusBadge kind={getAdminBadgeKind(admin)} />
      </div>

      <div className="mb-4 flex items-center gap-3 pr-[100px]">
        <div
          className="flex h-11 min-w-11 shrink-0 items-center justify-center rounded-xl bg-indigo-500 text-lg font-bold text-white shadow-lg ring-2 ring-white/10"
        >
          {initial}
        </div>
        <div className="min-w-0 overflow-hidden">
          <p className="truncate text-[15px] font-bold text-white">{admin.fullName}</p>
          <p className="truncate text-[11px] text-slate-400">{admin.email}</p>
        </div>
      </div>

      {isUltra ? (
        <>
          <div className="mb-2.5 grid grid-cols-2 gap-2.5">
            <div className="stat-cell border border-emerald-500/20 bg-emerald-500/10">
              <p className="stat-cell-label">Credit còn</p>
              <p className="text-[22px] font-extrabold text-emerald-400">
                <AnimatedNumber value={admin.remainingCredit} />
              </p>
            </div>
            <div className="stat-cell border border-indigo-500/20 bg-indigo-500/10">
              <p className="stat-cell-label">Bộ nhớ</p>
              <p className="text-[22px] font-extrabold text-indigo-400">
                {admin.usedStorageMB >= 1024
                  ? `${(admin.usedStorageMB / 1024).toFixed(1)} GB`
                  : `${admin.usedStorageMB} MB`}
              </p>
            </div>
          </div>
          <div className="mb-3 grid grid-cols-2 gap-2.5">
            <div className="stat-cell border border-amber-500/20 bg-amber-500/10">
              <p className="stat-cell-label">Đã dùng</p>
              <p className="text-lg font-bold text-amber-400">
                <AnimatedNumber
                  value={Math.max(0, admin.monthlyCredit - admin.remainingCredit)}
                />
              </p>
            </div>
            <div className="stat-cell border border-white/[0.08] bg-white/[0.04]">
              <p className="stat-cell-label">Thành viên</p>
              <p className="text-lg font-bold text-white">
                {admin.memberCount}
                {maxMembers === Infinity ? (
                  ""
                ) : (
                  <span className="text-xs text-slate-500">/{maxMembers}</span>
                )}
              </p>
            </div>
          </div>
          <div className="mt-1 h-1 w-full overflow-hidden rounded-full bg-white/[0.06]">
            <BarMeter
              value={
                admin.monthlyCredit > 0
                  ? Math.min(
                      ((admin.monthlyCredit - admin.remainingCredit) / admin.monthlyCredit) * 100,
                      100,
                    )
                  : 0
              }
              colorClass={
                admin.monthlyCredit > 0 &&
                (admin.monthlyCredit - admin.remainingCredit) / admin.monthlyCredit > 0.8
                  ? "fill-red-500"
                  : "fill-emerald-500"
              }
            />
          </div>
        </>
      ) : isGoogle ? (
        <>
          <div className="mb-2.5 grid grid-cols-2 gap-2.5">
            <div className="stat-cell border border-indigo-500/20 bg-indigo-500/10">
              <p className="stat-cell-label">Bộ nhớ</p>
              <div className="mt-1 flex items-center gap-3">
                <div className="relative h-16 w-16 shrink-0">
                  <RingMeter value={usageRingPercent} colorClass="stroke-indigo-400" />
                  <div className="absolute inset-[6px] flex items-center justify-center rounded-full bg-[#2a2058]/90 ring-1 ring-white/10">
                    <span className="text-[10px] font-semibold leading-none text-indigo-100">
                      {usagePercentLabel}
                    </span>
                  </div>
                </div>
                <div className="min-w-0">
                  <p className="text-[20px] font-extrabold leading-tight text-indigo-300">
                    {usedStorageLabel}
                  </p>
                  <p className="text-[12px] font-semibold text-white/95">/ {totalStorageLabel}</p>
                </div>
              </div>
            </div>
            <div className="stat-cell border border-white/[0.08] bg-white/[0.04]">
              <p className="stat-cell-label">{admin.memberCount === 0 ? "Gia đình" : "Thành viên"}</p>
              {admin.memberCount === 0 ? (
                <div className="mt-1 flex h-16 items-center justify-center text-center">
                  <p
                    className={`text-sm font-semibold ${
                      admin.lastSyncAt ? "text-amber-400" : "text-blue-400"
                    }`}
                  >
                    {admin.lastSyncAt ? "Chưa tạo gia đình" : "Chưa đồng bộ"}
                  </p>
                </div>
              ) : (
                <div className="mt-1 flex items-center gap-3">
                  <div className="relative h-16 w-16 shrink-0">
                    <RingMeter value={memberRingPercent} colorClass="stroke-emerald-400" />
                    <div className="absolute inset-[6px] flex items-center justify-center rounded-full bg-[#1d2f4d]/90 ring-1 ring-white/10">
                      <span className="text-xs font-bold text-emerald-100">{memberPercentLabel}</span>
                    </div>
                  </div>
                  <div className="min-w-0">
                    <p className="text-[20px] font-extrabold leading-tight text-white">
                      {admin.memberCount}
                      {maxMembers === Infinity ? (
                        ""
                      ) : (
                        <span className="text-sm text-slate-300">/{maxMembers}</span>
                      )}
                    </p>
                  </div>
                </div>
              )}
            </div>
          </div>
        </>
      ) : (
        <div className="mb-2.5 flex items-center justify-between">
          <div className="stat-cell flex-1 border border-white/[0.08] bg-white/[0.04]">
            {admin.accountStatus === "dead" ? (
              <>
                <p className="stat-cell-label">TRẠNG THÁI</p>
                <p className="text-sm font-semibold text-red-400">
                  Tài khoản bị vô hiệu hóa
                </p>
              </>
            ) : admin.memberCount === 0 ? (
              <>
                <p className="stat-cell-label">
                  {admin.lastSyncAt ? "Gia đình" : "Trạng thái"}
                </p>
                <p
                  className={`text-sm font-semibold ${admin.lastSyncAt ? "text-amber-400" : "text-blue-400"}`}
                >
                  {admin.lastSyncAt ? "Chưa tạo" : "Chưa đồng bộ"}
                </p>
              </>
            ) : (
              <>
                <p className="stat-cell-label">Thành viên</p>
                <p className="text-[22px] font-extrabold text-white">
                  {admin.memberCount}
                  {maxMembers === Infinity ? (
                    ""
                  ) : (
                    <span className="text-xs text-slate-500">/{maxMembers}</span>
                  )}
                </p>
              </>
            )}
          </div>
        </div>
      )}
    </div>
  );
}

function RingMeter({
  value,
  colorClass,
}: {
  value: number;
  colorClass: string;
}) {
  const radius = 15;
  const circumference = 2 * Math.PI * radius;
  const clamped = Math.max(0, Math.min(100, value));
  const dashOffset = circumference * (1 - clamped / 100);

  return (
    <svg viewBox="0 0 36 36" className="absolute inset-0 -rotate-90" aria-hidden="true">
      <circle
        cx="18"
        cy="18"
        r={radius}
        strokeWidth="4"
        className="fill-none stroke-white/15"
      />
      <circle
        cx="18"
        cy="18"
        r={radius}
        strokeWidth="4"
        strokeLinecap="round"
        strokeDasharray={circumference}
        strokeDashoffset={dashOffset}
        className={`fill-none transition-all duration-500 ${colorClass}`}
      />
    </svg>
  );
}

function BarMeter({
  value,
  colorClass,
}: {
  value: number;
  colorClass: string;
}) {
  const clamped = Math.max(0, Math.min(100, value));

  return (
    <svg viewBox="0 0 100 4" className="h-full w-full" preserveAspectRatio="none" aria-hidden="true">
      <rect x="0" y="0" width={clamped} height="4" rx="2" className={colorClass} />
    </svg>
  );
}
