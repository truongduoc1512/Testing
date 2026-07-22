"use client";
import { useMemo, useState, type ReactNode } from "react";
import { motion } from "framer-motion";
import {
  AlertTriangle,
  CalendarClock,
  CheckCircle2,
  ChevronDown,
  ChevronRight,
  CircleSlash,
  Crown,
  Inbox,
  Skull,
  UserRoundCheck,
  Users,
  Youtube,
} from "lucide-react";
import type { AdminRow } from "@/hooks/useDashboardData";
import { daysUntil, expiryColor, expiryLabel, formatDateVN, getMaxMembers } from "@/lib/utils";
import { StatusBadge } from "@/components/ui/StatusBadge";
import type { StatusBadgeKind } from "@/lib/constants/status";

interface Props {
  admins: AdminRow[];
  initialLoading: boolean;
  onAdminClick: (id: string) => void;
}

type MemberReport = NonNullable<AdminRow["memberReports"]>[number];
type MemberCategory = MemberReport["category"];
type ReportCategoryId =
  | "needs"
  | "empty"
  | "full"
  | "dead"
  | "valid"
  | "nearExpiry"
  | "expired"
  | "invited"
  | "noExpiry";
type ReportMemberItem = { admin: AdminRow; member: MemberReport };

const MEMBER_CATEGORIES: ReportCategoryId[] = [
  "valid",
  "nearExpiry",
  "expired",
  "invited",
  "noExpiry",
];

const FAMILY_ORDER = ["ultra", "pro", "youtube"] as const;

const CATEGORY_DEFS = [
  {
    id: "needs",
    label: "Cần xử lý",
    description: "Member gần hạn, hết hạn, đang chờ hoặc thiếu ngày hạn",
    icon: AlertTriangle,
    colorClass: "text-amber-300",
    activeClass: "border-amber-500/35 bg-amber-500/10 text-amber-100",
  },
  {
    id: "empty",
    label: "Fam trống",
    description: "Fam có slot trống (chưa max)",
    icon: Inbox,
    colorClass: "text-sky-300",
    activeClass: "border-sky-500/35 bg-sky-500/10 text-sky-100",
  },
  {
    id: "full",
    label: "Fam full",
    description: "Member count đã đạt giới hạn fam",
    icon: Users,
    colorClass: "text-red-300",
    activeClass: "border-red-500/35 bg-red-500/10 text-red-100",
  },
  {
    id: "dead",
    label: "Fam dead",
    description: "Account sync báo dead",
    icon: Skull,
    colorClass: "text-red-300",
    activeClass: "border-red-500/35 bg-red-500/10 text-red-100",
  },
  {
    id: "valid",
    label: "Còn hạn",
    description: "Member còn trên 7 ngày hoặc đã gia hạn",
    icon: CheckCircle2,
    colorClass: "text-emerald-300",
    activeClass: "border-emerald-500/35 bg-emerald-500/10 text-emerald-100",
  },
  {
    id: "nearExpiry",
    label: "Gần hạn",
    description: "Member còn 1-7 ngày",
    icon: CalendarClock,
    colorClass: "text-orange-300",
    activeClass: "border-orange-500/35 bg-orange-500/10 text-orange-100",
  },
  {
    id: "expired",
    label: "Hết hạn",
    description: "Member đã hết hạn",
    icon: CircleSlash,
    colorClass: "text-red-300",
    activeClass: "border-red-500/35 bg-red-500/10 text-red-100",
  },
  {
    id: "invited",
    label: "Đang chờ",
    description: "Member ở trạng thái invited",
    icon: UserRoundCheck,
    colorClass: "text-violet-300",
    activeClass: "border-violet-500/35 bg-violet-500/10 text-violet-100",
  },
] as const;

const FAMILY_META: Record<
  string,
  { label: string; icon: typeof Crown; iconClass: string; badgeClass: string }
> = {
  ultra: {
    label: "Ultra",
    icon: Crown,
    iconClass: "text-violet-300",
    badgeClass: "border-violet-500/25 bg-violet-500/10 text-violet-300",
  },
  pro: {
    label: "Pro",
    icon: Crown,
    iconClass: "text-cyan-300",
    badgeClass: "border-cyan-500/25 bg-cyan-500/10 text-cyan-300",
  },
  youtube: {
    label: "YouTube",
    icon: Youtube,
    iconClass: "text-red-300",
    badgeClass: "border-red-500/25 bg-red-500/10 text-red-300",
  },
};

const EMPTY_EXPIRY = {
  valid: 0,
  nearExpiry: 0,
  expired: 0,
  renewed: 0,
  invited: 0,
  manager: 0,
  noExpiry: 0,
  tracked: 0,
};

function getExpiry(admin: AdminRow) {
  return admin.memberExpiry ?? EMPTY_EXPIRY;
}

function isEmptyFam(admin: AdminRow) {
  if (isDeadFam(admin)) return false;
  const max = getMaxMembers(admin.familyType);
  return admin.memberCount > 0 && admin.memberCount < max;
}

function isFullFam(admin: AdminRow) {
  return admin.memberCount >= getMaxMembers(admin.familyType);
}

function isDeadFam(admin: AdminRow) {
  return admin.accountStatus === "dead";
}

function isMemberCategory(categoryId: ReportCategoryId): categoryId is MemberCategory {
  return MEMBER_CATEGORIES.includes(categoryId);
}

function isMemberListCategory(categoryId: ReportCategoryId) {
  return categoryId === "needs" || isMemberCategory(categoryId);
}

function getFamilyKey(familyType?: string | null) {
  const key = familyType || "ultra";
  return FAMILY_META[key] ? key : "ultra";
}

function getFamilyMeta(familyType?: string | null) {
  return FAMILY_META[getFamilyKey(familyType)];
}

function getFamilyTitle(familyType?: string | null) {
  return `Fam ${getFamilyMeta(familyType).label}`;
}

function getFilteredAdmins(admins: AdminRow[], categoryId: ReportCategoryId) {
  return admins.filter((admin) => {
    if (categoryId === "empty") return isEmptyFam(admin);
    if (categoryId === "full") return isFullFam(admin);
    if (categoryId === "dead") return isDeadFam(admin);
    return false;
  });
}

function getFilteredMembers(admins: AdminRow[], categoryId: ReportCategoryId): ReportMemberItem[] {
  return admins
    .flatMap((admin) =>
      (admin.memberReports ?? []).map((member) => ({
        admin,
        member,
      })),
    )
    .filter(({ member }) => {
      if (categoryId === "needs") {
        return (
          member.category === "nearExpiry" ||
          member.category === "expired" ||
          member.category === "invited" ||
          member.category === "noExpiry"
        );
      }

      return isMemberCategory(categoryId) && member.category === categoryId;
    });
}

function groupAdminsByFamily(admins: AdminRow[]) {
  return FAMILY_ORDER.map((familyType) => ({
    familyType,
    items: admins.filter((admin) => getFamilyKey(admin.familyType) === familyType),
  })).filter((group) => group.items.length > 0);
}

function groupMembersByFamily(items: ReportMemberItem[]) {
  return FAMILY_ORDER.map((familyType) => ({
    familyType,
    items: items.filter(({ admin }) => getFamilyKey(admin.familyType) === familyType),
  })).filter((group) => group.items.length > 0);
}

function getCategoryTotal(admins: AdminRow[], categoryId: ReportCategoryId) {
  if (categoryId === "empty") return admins.filter(isEmptyFam).length;
  if (categoryId === "full") return admins.filter(isFullFam).length;
  if (categoryId === "dead") return admins.filter(isDeadFam).length;
  if (categoryId === "needs") return getFilteredMembers(admins, categoryId).length;

  return admins.reduce((sum, admin) => sum + getExpiry(admin)[categoryId], 0);
}

function getRiskScore(admin: AdminRow) {
  const expiry = getExpiry(admin);
  return (
    (isDeadFam(admin) ? 200 : 0) +
    expiry.expired * 100 +
    expiry.nearExpiry * 50 +
    expiry.noExpiry * 30 +
    (isEmptyFam(admin) ? 20 : 0) +
    (isFullFam(admin) ? 10 : 0)
  );
}

function getMemberSortScore(member: MemberReport) {
  if (member.category === "expired") return 500 + Math.abs(member.daysLeft ?? 0);
  if (member.category === "nearExpiry") return 400 - (member.daysLeft ?? 0);
  if (member.category === "noExpiry") return 300;
  if (member.category === "invited") return 200;
  return 100 - (member.daysLeft ?? 99);
}

export default function FamilyReportTab({ admins, initialLoading, onAdminClick }: Props) {
  const [activeCategory, setActiveCategory] = useState<ReportCategoryId>("needs");

  const reportStats = useMemo(() => {
    const emptyFam = admins.filter(isEmptyFam).length;
    const fullFam = admins.filter(isFullFam).length;
    const deadFam = admins.filter(isDeadFam).length;
    const needAction = getFilteredMembers(admins, "needs").length;

    return admins.reduce(
      (acc, admin) => {
        const expiry = getExpiry(admin);
        acc.nearExpiryMembers += expiry.nearExpiry;
        acc.expiredMembers += expiry.expired;
        return acc;
      },
      {
        needAction,
        emptyFam,
        fullFam,
        deadFam,
        nearExpiryMembers: 0,
        expiredMembers: 0,
      },
    );
  }, [admins]);

  const activeMeta =
    CATEGORY_DEFS.find((category) => category.id === activeCategory) ?? CATEGORY_DEFS[0];

  const visibleAdmins = useMemo(
    () =>
      getFilteredAdmins(admins, activeCategory).sort(
        (a, b) => getRiskScore(b) - getRiskScore(a) || a.fullName.localeCompare(b.fullName),
      ),
    [activeCategory, admins],
  );

  const visibleMembers = useMemo(() => {
    if (!isMemberListCategory(activeCategory)) return [];
    return getFilteredMembers(admins, activeCategory).sort(
      (a, b) =>
        getMemberSortScore(b.member) - getMemberSortScore(a.member) ||
        (a.member.name || a.member.email).localeCompare(b.member.name || b.member.email),
    );
  }, [activeCategory, admins]);

  const visibleCount = isMemberListCategory(activeCategory)
    ? visibleMembers.length
    : visibleAdmins.length;
  const visibleAdminGroups = useMemo(
    () => groupAdminsByFamily(visibleAdmins),
    [visibleAdmins],
  );
  const visibleMemberGroups = useMemo(
    () => groupMembersByFamily(visibleMembers),
    [visibleMembers],
  );

  if (initialLoading) {
    return (
      <div className="space-y-4">
        <div className="glass-card animate-pulse rounded-2xl p-5">
          <div className="h-5 w-40 rounded bg-white/[0.06]" />
          <div className="mt-3 h-3 w-64 rounded bg-white/[0.04]" />
        </div>
        <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 xl:grid-cols-4">
          {Array.from({ length: 4 }).map((_, index) => (
            <div key={index} className="glass-card animate-pulse rounded-xl p-3">
              <div className="h-3 w-20 rounded bg-white/[0.06]" />
              <div className="mt-2 h-5 w-10 rounded bg-white/[0.08]" />
            </div>
          ))}
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      <div className="glass-card rounded-2xl p-4">
        <div className="grid min-w-0 grid-cols-[repeat(auto-fit,minmax(6.75rem,1fr))] gap-2">
          <MiniSummary label="Cần xử lý" value={reportStats.needAction} tone="amber" />
          <MiniSummary label="Fam trống" value={reportStats.emptyFam} tone="sky" />
          <MiniSummary label="Fam full" value={reportStats.fullFam} tone="red" />
          <MiniSummary label="Fam dead" value={reportStats.deadFam} tone="red" />
          <MiniSummary
            label="Gần/hết hạn"
            value={reportStats.nearExpiryMembers + reportStats.expiredMembers}
            tone="orange"
          />
        </div>
      </div>

      <div className="grid grid-cols-[repeat(auto-fit,minmax(7.25rem,1fr))] gap-2">
        {CATEGORY_DEFS.map((category) => {
          const Icon = category.icon;
          const active = activeCategory === category.id;
          const value = getCategoryTotal(admins, category.id);

          return (
            <button
              key={category.id}
              type="button"
              onClick={() => setActiveCategory(category.id)}
              title={category.description}
              className={`flex min-h-11 items-center gap-2 rounded-xl border px-2.5 py-2 text-left transition-all ${
                active
                  ? category.activeClass
                  : "border-white/[0.08] bg-white/[0.03] text-slate-400 hover:bg-white/[0.06]"
              }`}
            >
              <Icon className={`h-4 w-4 shrink-0 ${active ? category.colorClass : "text-slate-500"}`} />
              <span className="min-w-0 flex-1 truncate text-xs font-semibold">
                {category.label}
              </span>
              <span className="shrink-0 rounded-full bg-white/[0.07] px-1.5 py-0.5 text-[10px] font-bold">
                {value}
              </span>
            </button>
          );
        })}
      </div>

      <motion.section
        key={activeCategory}
        initial={{ opacity: 0, y: 8 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.18 }}
        className="space-y-3"
      >
        <div className="flex flex-col gap-1 sm:flex-row sm:items-end sm:justify-between">
          <div className="min-w-0">
            <h3 className="text-sm font-bold text-white">{activeMeta.label}</h3>
          </div>
          <span className="text-xs text-slate-500">{visibleCount} dòng phù hợp</span>
        </div>

        {visibleCount === 0 ? (
          <div className="rounded-xl border border-white/[0.06] bg-white/[0.02] px-4 py-10 text-center text-sm text-slate-500">
            Không có dữ liệu trong nhóm này.
          </div>
        ) : isMemberListCategory(activeCategory) ? (
          <div className="space-y-4">
            {visibleMemberGroups.map(({ familyType, items }) => (
              <FamilySection
                key={familyType}
                familyType={familyType}
                count={items.length}
              >
                <div className="grid grid-cols-1 gap-3 xl:grid-cols-2">
                  {items.map(({ admin, member }) => (
                    <ReportMemberRow
                      key={`${admin.id}-${member.key}`}
                      admin={admin}
                      member={member}
                      onClick={() => onAdminClick(admin.id)}
                    />
                  ))}
                </div>
              </FamilySection>
            ))}
          </div>
        ) : (
          <div className="space-y-4">
            {visibleAdminGroups.map(({ familyType, items }) => (
              <FamilySection
                key={familyType}
                familyType={familyType}
                count={items.length}
              >
                <div className="grid grid-cols-1 gap-3 xl:grid-cols-2">
                  {items.map((admin) => (
                    <ReportAdminRow
                      key={admin.id}
                      admin={admin}
                      onClick={() => onAdminClick(admin.id)}
                    />
                  ))}
                </div>
              </FamilySection>
            ))}
          </div>
        )}
      </motion.section>
    </div>
  );
}

function FamilySection({
  familyType,
  count,
  children,
}: {
  familyType: string;
  count: number;
  children: ReactNode;
}) {
  const [collapsed, setCollapsed] = useState(true);
  const familyMeta = getFamilyMeta(familyType);
  const FamilyIcon = familyMeta.icon;

  return (
    <section className="overflow-hidden rounded-xl border border-white/[0.06] bg-white/[0.015]">
      <button
        type="button"
        onClick={() => setCollapsed((value) => !value)}
        aria-expanded={!collapsed}
        aria-label={`${collapsed ? "Mở" : "Thu gọn"} ${getFamilyTitle(familyType)}`}
        className="flex w-full items-center gap-2 px-3 py-2.5 text-left transition-colors hover:bg-white/[0.035]"
      >
        <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg border border-white/[0.08] bg-white/[0.04]">
          <FamilyIcon className={`h-4 w-4 ${familyMeta.iconClass}`} />
        </div>
        <div className="min-w-0 flex-1">
          <div className="flex flex-wrap items-center gap-2">
            <h4 className="text-sm font-bold text-white">{getFamilyTitle(familyType)}</h4>
            <span className={`rounded-full border px-2 py-0.5 text-[10px] font-bold ${familyMeta.badgeClass}`}>
              {familyMeta.label}
            </span>
          </div>
          <p className="text-[11px] text-slate-500">{count} dòng phù hợp</p>
        </div>
        <span className="rounded-full border border-white/[0.06] bg-white/[0.04] px-2 py-0.5 text-[11px] font-bold text-slate-300">
          {count}
        </span>
        {collapsed ? (
          <ChevronRight className="h-4 w-4 shrink-0 text-slate-500" />
        ) : (
          <ChevronDown className="h-4 w-4 shrink-0 text-slate-500" />
        )}
      </button>

      {!collapsed && (
        <motion.div
          initial={{ opacity: 0, y: -4 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.16 }}
          className="border-t border-white/[0.06] p-3"
        >
          {children}
        </motion.div>
      )}
    </section>
  );
}

function MiniSummary({
  label,
  value,
  tone,
}: {
  label: string;
  value: number;
  tone: "amber" | "sky" | "red" | "orange";
}) {
  const toneClass = {
    amber: "text-amber-300",
    sky: "text-sky-300",
    red: "text-red-300",
    orange: "text-orange-300",
  }[tone];

  return (
    <div className="min-w-24 rounded-xl border border-white/[0.06] bg-white/[0.03] px-3 py-2">
      <p className={`text-lg font-extrabold leading-tight ${toneClass}`}>
        {value.toLocaleString("vi-VN")}
      </p>
      <p className="truncate text-[10px] font-semibold uppercase tracking-wider text-slate-500">
        {label}
      </p>
    </div>
  );
}

function ReportMemberRow({
  admin,
  member,
  onClick,
}: {
  admin: AdminRow;
  member: MemberReport;
  onClick: () => void;
}) {
  const familyMeta = getFamilyMeta(admin.familyType);
  const FamilyIcon = familyMeta.icon;
  const expiryText =
    member.category === "invited"
      ? "Đang chờ nhận lời mời"
      : member.category === "noExpiry"
        ? "Thiếu ngày hạn"
        : member.daysLeft === null
          ? member.renewed
            ? "Đã gia hạn"
            : "Không có dữ liệu"
          : expiryLabel(member.daysLeft);
  const expiryTone =
    member.category === "expired"
      ? "red"
      : member.category === "nearExpiry"
        ? "orange"
        : member.category === "invited"
          ? "violet"
          : member.category === "noExpiry"
            ? "slate"
            : "emerald";

  return (
    <button
      type="button"
      onClick={onClick}
      className="rounded-xl border border-white/[0.06] bg-white/[0.025] p-3 text-left transition-all hover:border-white/[0.12] hover:bg-white/[0.05]"
    >
      <div className="flex items-start gap-3">
        <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg border border-white/[0.08] bg-white/[0.04]">
          <UserRoundCheck className="h-4 w-4 text-slate-300" />
        </div>
        <div className="min-w-0 flex-1">
          <p className="truncate text-sm font-bold text-white">
            {member.name || member.email}
          </p>
          <p className="truncate text-xs text-slate-400">{member.email}</p>
        </div>
        <span className={`rounded-full border px-2 py-0.5 text-[10px] font-bold ${familyMeta.badgeClass}`}>
          {familyMeta.label}
        </span>
      </div>

      <div className="mt-3 grid grid-cols-1 gap-2 sm:grid-cols-3">
        <CompactStat label="Hạn member" value={expiryText} tone={expiryTone} />
        <CompactStat
          label="End date"
          value={member.endDate ? formatDateVN(member.endDate) : "Không có"}
          tone={member.category === "noExpiry" ? "slate" : expiryTone}
        />
        <CompactStat label="Chủ fam" value={admin.fullName || admin.email} tone="slate" />
      </div>

      <div className="mt-3 flex items-center gap-2 border-t border-white/[0.06] pt-2.5 text-[11px] text-slate-500">
        <FamilyIcon className={`h-3.5 w-3.5 ${familyMeta.iconClass}`} />
        <span className="truncate">{admin.email}</span>
      </div>
    </button>
  );
}

function ReportAdminRow({ admin, onClick }: { admin: AdminRow; onClick: () => void }) {
  const expiry = getExpiry(admin);
  const maxMembers = getMaxMembers(admin.familyType);
  const familyMeta = getFamilyMeta(admin.familyType);
  const FamilyIcon = familyMeta.icon;
  const isFull = isFullFam(admin);
  const isDead = isDeadFam(admin);
  const planDaysLeft = admin.planExpiresAt ? daysUntil(admin.planExpiresAt) : null;
  const memberExpiryText =
    expiry.expired > 0
      ? `${expiry.expired} hết hạn`
      : expiry.nearExpiry > 0
        ? `${expiry.nearExpiry} gần hạn`
        : expiry.noExpiry > 0
          ? `${expiry.noExpiry} thiếu hạn`
          : expiry.valid > 0
            ? `${expiry.valid} còn hạn`
            : expiry.invited > 0
              ? `${expiry.invited} chờ`
              : "Không có";
  const memberExpiryTone =
    expiry.expired > 0
      ? "red"
      : expiry.nearExpiry > 0
        ? "orange"
        : expiry.noExpiry > 0
          ? "slate"
          : "emerald";
  const planLabel =
    admin.planExpiresAt && planDaysLeft !== null ? expiryLabel(planDaysLeft) : "Không có dữ liệu";
  const planTone = planDaysLeft !== null && planDaysLeft <= 30 ? "orange" : "slate";
  const signals = [
    isEmptyFam(admin) ? { tone: "sky" as const, label: "Fam trống" } : null,
    isFull ? { tone: "red" as const, label: "Fam full" } : null,
    isDead ? { tone: "red" as const, label: "Fam dead" } : null,
    expiry.expired > 0 ? { tone: "red" as const, label: `${expiry.expired} member hết hạn` } : null,
    expiry.nearExpiry > 0
      ? { tone: "orange" as const, label: `${expiry.nearExpiry} member gần hạn` }
      : null,
    expiry.noExpiry > 0
      ? { tone: "slate" as const, label: `${expiry.noExpiry} member thiếu hạn` }
      : null,
  ].filter(
    (signal): signal is { tone: "sky" | "red" | "orange" | "slate"; label: string } =>
      Boolean(signal),
  );
  const badgeKindByTone: Record<"sky" | "red" | "orange" | "slate", StatusBadgeKind> = {
    sky: "noFamily",
    red: "dead",
    orange: "nearExpired",
    slate: "unknown",
  };

  return (
    <button
      type="button"
      onClick={onClick}
      className="rounded-xl border border-white/[0.06] bg-white/[0.025] p-3 text-left transition-all hover:border-white/[0.12] hover:bg-white/[0.05]"
    >
      <div className="flex items-start gap-3">
        <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg border border-white/[0.08] bg-white/[0.04]">
          <FamilyIcon className={`h-4 w-4 ${familyMeta.iconClass}`} />
        </div>
        <div className="min-w-0 flex-1">
          <div className="flex flex-wrap items-center gap-2">
            <p className="truncate text-sm font-bold text-white">
              {admin.fullName || admin.email}
            </p>
            <span className={`rounded-full border px-2 py-0.5 text-[10px] font-bold ${familyMeta.badgeClass}`}>
              {familyMeta.label}
            </span>
          </div>
          <p className="truncate text-xs text-slate-400">{admin.email}</p>
        </div>
      </div>

      <div className="mt-3 grid grid-cols-1 gap-2 sm:grid-cols-3">
        <CompactStat
          label="Slot"
          value={`${admin.memberCount}/${maxMembers}`}
          tone={isEmptyFam(admin) ? "sky" : isFull ? "red" : "slate"}
        />
        <CompactStat label="Hạn member" value={memberExpiryText} tone={memberExpiryTone} />
        <CompactStat
          label="Trạng thái"
          value={isDead ? "Dead" : planLabel}
          tone={isDead ? "red" : planTone}
        />
      </div>

      {signals.length > 0 && (
        <div className="mt-2 flex flex-wrap gap-1.5">
          {signals.slice(0, 4).map((signal) => (
            <StatusBadge
              key={`${signal.tone}-${signal.label}`}
              kind={badgeKindByTone[signal.tone]}
              label={signal.label}
            />
          ))}
        </div>
      )}

      <div className="mt-3 flex flex-col gap-1 border-t border-white/[0.06] pt-2.5 text-[11px] text-slate-500 sm:flex-row sm:items-center sm:justify-between">
        <span>
          Sync:{" "}
          <span className="text-slate-300">
            {admin.lastSyncAt ? new Date(admin.lastSyncAt).toLocaleString("vi-VN") : "Chưa đồng bộ"}
          </span>
        </span>
        <span>
          Plan date:{" "}
          {admin.planExpiresAt && planDaysLeft !== null ? (
            <span className={expiryColor(planDaysLeft)}>
              {formatDateVN(admin.planExpiresAt)}
            </span>
          ) : (
            <span className="text-slate-400">Không có dữ liệu</span>
          )}
        </span>
      </div>
    </button>
  );
}

function CompactStat({
  label,
  value,
  tone,
}: {
  label: string;
  value: string;
  tone: "sky" | "red" | "emerald" | "orange" | "violet" | "slate";
}) {
  const toneClass = {
    sky: "text-sky-300",
    red: "text-red-300",
    emerald: "text-emerald-300",
    orange: "text-orange-300",
    violet: "text-violet-300",
    slate: "text-slate-300",
  }[tone];

  return (
    <div className="rounded-lg border border-white/[0.05] bg-white/[0.025] px-3 py-2">
      <p className="text-[10px] font-semibold uppercase tracking-wider text-slate-500">
        {label}
      </p>
      <p className={`mt-0.5 truncate text-xs font-bold ${toneClass}`}>{value}</p>
    </div>
  );
}
