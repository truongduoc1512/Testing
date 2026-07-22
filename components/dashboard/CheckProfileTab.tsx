"use client";
import { useMemo, useRef, useState } from "react";
import { motion, AnimatePresence } from "framer-motion";
import {
  UserCircle,
  Mail,
  Crown,
  Youtube,
  Trash2,
  Share2,
  ChevronDown,
} from "lucide-react";
import type { AdminRow } from "@/hooks/useDashboardData";
import { useGoogleProfile } from "@/hooks/useGoogleProfile";
import type { PaymentProfile } from "@/hooks/useGoogleProfile";
import { useClickOutside } from "@/hooks/useClickOutside";
import { useCheckProfileSelection, useToast } from "@/hooks";
import { formatDateVN, parseUsagePercent } from "@/lib/utils";

interface Props {
  admins: AdminRow[];
}

const PROFILE_FAMILY_GROUPS = [
  {
    id: "ultra",
    label: "Fam Ultra",
    icon: Crown,
    iconClass: "text-violet-400",
    iconBoxClass: "bg-violet-500/20 text-violet-400",
    activeClass: "border-violet-500/50 bg-violet-500/15 text-violet-100",
    countClass: "text-violet-300",
  },
  {
    id: "pro",
    label: "Fam Pro",
    icon: Crown,
    iconClass: "text-emerald-400",
    iconBoxClass: "bg-emerald-500/20 text-emerald-400",
    activeClass: "border-emerald-500/50 bg-emerald-500/15 text-emerald-100",
    countClass: "text-emerald-300",
  },
  {
    id: "youtube",
    label: "Fam YouTube",
    icon: Youtube,
    iconClass: "text-red-400",
    iconBoxClass: "bg-red-500/20 text-red-400",
    activeClass: "border-red-500/50 bg-red-500/15 text-red-100",
    countClass: "text-red-300",
  },
] as const;

const PROFILE_FAMILY_FILTERS = [
  {
    id: "all",
    label: "Tất cả",
    icon: UserCircle,
    iconClass: "text-cyan-400",
    activeClass: "border-cyan-500/50 bg-cyan-500/15 text-cyan-100",
    countClass: "text-cyan-300",
  },
  ...PROFILE_FAMILY_GROUPS,
] as const;

type ProfileFamilyType = (typeof PROFILE_FAMILY_GROUPS)[number]["id"];
type ProfileFamilyFilter = (typeof PROFILE_FAMILY_FILTERS)[number]["id"];

function getProfileFamilyType(familyType?: string | null): ProfileFamilyType {
  if (familyType === "pro" || familyType === "youtube") return familyType;
  return "ultra";
}

function getProfileFamilyMeta(familyType?: string | null) {
  const resolvedType = getProfileFamilyType(familyType);
  return (
    PROFILE_FAMILY_GROUPS.find((group) => group.id === resolvedType) ??
    PROFILE_FAMILY_GROUPS[0]
  );
}

export default function CheckProfileTab({ admins }: Props) {
  const { googleAdmins, selectedId, selectedAdmin, selectAdmin } =
    useCheckProfileSelection(admins);
  const [showAccountDropdown, setShowAccountDropdown] = useState(false);
  const [activeFamilyFilter, setActiveFamilyFilter] =
    useState<ProfileFamilyFilter>("all");
  const accountDropdownRef = useRef<HTMLDivElement>(null);
  const enableSharing = selectedAdmin?.familyType !== "youtube";
  const selectedFamilyMeta = getProfileFamilyMeta(selectedAdmin?.familyType);
  const SelectedFamilyIcon = selectedFamilyMeta.icon;

  const familyCounts = useMemo(() => {
    const counts: Record<ProfileFamilyType, number> = {
      ultra: 0,
      pro: 0,
      youtube: 0,
    };

    googleAdmins.forEach((admin) => {
      counts[getProfileFamilyType(admin.familyType)] += 1;
    });

    return counts;
  }, [googleAdmins]);

  const filteredGoogleAdmins = useMemo(() => {
    if (activeFamilyFilter === "all") return googleAdmins;
    return googleAdmins.filter(
      (admin) => getProfileFamilyType(admin.familyType) === activeFamilyFilter,
    );
  }, [activeFamilyFilter, googleAdmins]);

  const groupedDropdownAdmins = useMemo(
    () =>
      PROFILE_FAMILY_GROUPS.map((group) => ({
        ...group,
        admins: googleAdmins.filter(
          (admin) => getProfileFamilyType(admin.familyType) === group.id,
        ),
      })).filter((group) => group.admins.length > 0),
    [googleAdmins],
  );

  const activeFilterMeta =
    PROFILE_FAMILY_FILTERS.find((filter) => filter.id === activeFamilyFilter) ??
    PROFILE_FAMILY_FILTERS[0];

  const handleFamilyFilterClick = (filterId: ProfileFamilyFilter) => {
    setActiveFamilyFilter(filterId);
    if (filterId === "all") return;

    const selectedMatchesFilter =
      selectedAdmin && getProfileFamilyType(selectedAdmin.familyType) === filterId;
    if (selectedMatchesFilter) return;

    const firstAdminInFamily = googleAdmins.find(
      (admin) => getProfileFamilyType(admin.familyType) === filterId,
    );
    if (firstAdminInFamily) selectAdmin(firstAdminInFamily.id);
  };

  useClickOutside(accountDropdownRef, () => setShowAccountDropdown(false));
  const {
    profileData,
    isFetching,
    fetchProfile,
    closePayment,
    isClosingPayment,
    confirmCloseId,
    setConfirmCloseId,
    isAdminFetched,
    isAdminFetching,
    sharingStatus,
    isTogglingSharing,
    toggleSharing,
    memberStorage,
    isFetchingMemberStorage,
  } = useGoogleProfile({
    adminId: selectedId,
    initialData: selectedAdmin?.profileData || null,
    enableSharing,
  });

  const { toast, show: pushToast, dismiss: dismissToast } = useToast();
  const showToast = (type: "success" | "error", message: string) => {
    pushToast(message, type, 4000);
  };
  const profileErrors = profileData?.errors ?? [];

  const handleClosePayment = async () => {
    const result = await closePayment();
    if (result.success) {
      showToast("success", "Đóng hồ sơ thanh toán thành công!");
    } else {
      showToast("error", result.message || "Thất bại khi đóng hồ sơ thanh toán.");
    }
  };

  const handleToggleSharing = async (enable: boolean) => {
    const result = await toggleSharing(enable);
    if (result.success) {
      showToast(
        "success",
        enable ? "Đã bật chia sẻ Google One" : "Đã tắt chia sẻ Google One",
      );
    } else {
      showToast("error", result.error || "Thất bại khi thay đổi trạng thái chia sẻ.");
    }
  };

  const renderAdminOption = (admin: AdminRow) => {
    const familyMeta = getProfileFamilyMeta(admin.familyType);
    const AdminFamilyIcon = familyMeta.icon;

    return (
      <button
        key={admin.id}
        onClick={() => {
          selectAdmin(admin.id);
          setShowAccountDropdown(false);
        }}
        className={`flex w-full items-center gap-3 rounded-lg p-2.5 text-left transition-all ${
          selectedId === admin.id
            ? "bg-cyan-500/15 ring-1 ring-cyan-500/50"
            : "hover:bg-white/[0.04]"
        }`}
      >
        <div
          className={`flex h-8 w-8 shrink-0 items-center justify-center rounded-lg ${familyMeta.iconBoxClass}`}
        >
          <AdminFamilyIcon className="h-4 w-4" />
        </div>
        <div className="min-w-0 flex-1">
          <div className="truncate text-sm font-semibold text-white">
            {admin.fullName || admin.email}
          </div>
          {isAdminFetching(admin.id) ? (
            <div className="text-xs text-cyan-400">Đang kiểm tra...</div>
          ) : isAdminFetched(admin.id, admin.profileData) ? (
            <div className="text-xs text-emerald-400">Đã kiểm tra</div>
          ) : (
            <div className="truncate text-xs text-slate-400">{admin.email}</div>
          )}
        </div>
      </button>
    );
  };

  return (
    <div className="relative space-y-6">
      {toast && (
        <motion.div
          initial={{ opacity: 0, y: -20, scale: 0.95 }}
          animate={{ opacity: 1, y: 0, scale: 1 }}
          exit={{ opacity: 0, y: -20, scale: 0.95 }}
          className={`fixed left-4 right-4 top-4 z-50 flex items-center gap-3 rounded-xl border px-4 py-3.5 shadow-2xl backdrop-blur-xl sm:left-auto sm:right-6 sm:top-6 sm:max-w-md sm:px-5 ${
            toast.type === "success"
              ? "border-emerald-500/30 bg-emerald-500/10 text-emerald-300"
              : "border-red-500/30 bg-red-500/10 text-red-300"
          }`}
        >
          <span className="text-sm font-medium">{toast.msg}</span>
          <button
            onClick={dismissToast}
            className="ml-2 inline-flex min-h-11 min-w-11 items-center justify-center rounded-lg text-xs opacity-60 transition-opacity hover:bg-white/[0.06] hover:opacity-100"
          >
            ✕
          </button>
        </motion.div>
      )}
      <div className="glass-card rounded-2xl p-4 sm:p-5">
        <h2 className="section-header text-sm">
          <UserCircle className="h-4 w-4 text-cyan-400" />
          Kiểm Tra Hồ Sơ Google
        </h2>
        <p className="mt-1 text-xs text-slate-500">
          Kiểm tra toàn bộ thông tin tài khoản (Gói đăng ký, Hồ sơ, Dung lượng...) cho
          Google Flow.
        </p>
      </div>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
        <div className="glass-card rounded-2xl p-4 lg:col-span-1">
          <h3 className="mb-4 text-xs font-bold uppercase tracking-wider text-slate-500">
            Chọn tài khoản Google
          </h3>
          {googleAdmins.length === 0 ? (
            <div className="py-4 text-center text-xs text-slate-500">
              Không có tài khoản Google nào (Ultra/Pro/YouTube).
            </div>
          ) : (
            <div className="space-y-2" ref={accountDropdownRef}>
              <div className="grid grid-cols-2 gap-2 sm:grid-cols-4 lg:grid-cols-2">
                {PROFILE_FAMILY_FILTERS.map((filter) => {
                  const FilterIcon = filter.icon;
                  const count =
                    filter.id === "all" ? googleAdmins.length : familyCounts[filter.id];
                  const active = activeFamilyFilter === filter.id;
                  const disabled = count === 0;

                  return (
                    <button
                      key={filter.id}
                      type="button"
                      onClick={() => handleFamilyFilterClick(filter.id)}
                      disabled={disabled}
                      className={`flex min-h-12 items-center gap-2 rounded-xl border px-3 py-2.5 text-left transition-all ${
                        active
                          ? filter.activeClass
                          : "border-white/[0.08] bg-white/[0.03] text-slate-400 hover:bg-white/[0.06]"
                      } ${disabled ? "cursor-not-allowed opacity-45" : ""}`}
                    >
                      <FilterIcon
                        className={`h-4 w-4 shrink-0 ${active ? filter.iconClass : "text-slate-500"}`}
                      />
                      <span className="min-w-0 flex-1 truncate text-xs font-semibold">
                        {filter.label}
                      </span>
                      <span
                        className={`shrink-0 text-[11px] font-bold ${
                          active ? filter.countClass : "text-slate-500"
                        }`}
                      >
                        {count}
                      </span>
                    </button>
                  );
                })}
              </div>

              <div className="relative">
                <button
                  onClick={() => setShowAccountDropdown((prev) => !prev)}
                  className={`flex w-full items-center gap-3 rounded-xl border px-3 py-2.5 text-left transition-all ${
                    showAccountDropdown
                      ? "border-cyan-500/50 bg-cyan-500/10"
                      : "border-white/[0.08] bg-white/[0.04] hover:bg-white/[0.06]"
                  }`}
                >
                  <div
                    className={`flex h-9 w-9 shrink-0 items-center justify-center rounded-lg ${selectedFamilyMeta.iconBoxClass}`}
                  >
                    <SelectedFamilyIcon className="h-4 w-4" />
                  </div>
                  <div className="min-w-0 flex-1">
                    <div className="truncate text-sm font-semibold text-white">
                      {selectedAdmin
                        ? selectedAdmin.fullName || selectedAdmin.email
                        : "Chọn tài khoản"}
                    </div>
                    <div className="truncate text-xs text-slate-400">
                      {selectedAdmin
                        ? selectedAdmin.email
                        : activeFamilyFilter === "all"
                          ? `${googleAdmins.length} tài khoản`
                          : `${filteredGoogleAdmins.length} tài khoản ${activeFilterMeta.label}`}
                    </div>
                  </div>
                  <ChevronDown
                    className={`h-4 w-4 shrink-0 text-slate-400 transition-transform ${
                      showAccountDropdown ? "rotate-180" : ""
                    }`}
                  />
                </button>

                <AnimatePresence>
                  {showAccountDropdown && (
                    <motion.div
                      initial={{ opacity: 0, scale: 0.98, y: -4 }}
                      animate={{ opacity: 1, scale: 1, y: 0 }}
                      exit={{ opacity: 0, scale: 0.98, y: -4 }}
                      transition={{ duration: 0.15 }}
                      className="glass-card mt-2 overflow-hidden rounded-xl shadow-2xl lg:absolute lg:left-0 lg:right-0 lg:top-[calc(100%+8px)] lg:z-40 lg:mt-0"
                    >
                      <div className="max-h-56 space-y-1 overflow-y-auto p-1.5 lg:max-h-72">
                        {activeFamilyFilter === "all" ? (
                          groupedDropdownAdmins.map((group) => {
                            const GroupIcon = group.icon;

                            return (
                              <div key={group.id} className="space-y-1">
                                <div className="flex items-center gap-2 px-2.5 py-1.5 text-[10px] font-bold uppercase tracking-wider text-slate-500">
                                  <GroupIcon
                                    className={`h-3.5 w-3.5 ${group.iconClass}`}
                                  />
                                  <span>{group.label}</span>
                                  <span className={`ml-auto ${group.countClass}`}>
                                    {group.admins.length}
                                  </span>
                                </div>
                                {group.admins.map(renderAdminOption)}
                              </div>
                            );
                          })
                        ) : filteredGoogleAdmins.length > 0 ? (
                          filteredGoogleAdmins.map(renderAdminOption)
                        ) : (
                          <div className="px-3 py-6 text-center text-xs text-slate-500">
                            Không có tài khoản trong {activeFilterMeta.label}.
                          </div>
                        )}
                      </div>
                    </motion.div>
                  )}
                </AnimatePresence>
              </div>
            </div>
          )}
        </div>

        <div className="lg:col-span-2">
          {!selectedAdmin ? (
            <div className="glass-card flex h-full min-h-[400px] flex-col items-center justify-center rounded-2xl border-dashed border-white/[0.1] p-8 text-center">
              <UserCircle className="mb-4 h-16 w-16 text-slate-600" />
              <p className="text-sm font-medium text-slate-400">
                Chọn một tài khoản ở danh sách bên trái để xem chi tiết.
              </p>
            </div>
          ) : (
            <motion.div
              key={selectedAdmin.id}
              initial={{ opacity: 0, scale: 0.98 }}
              animate={{ opacity: 1, scale: 1 }}
              className="glass-card space-y-5 rounded-2xl p-4 sm:space-y-6 sm:p-6"
            >
              <div className="border-b border-white/[0.06] pb-5 sm:pb-6">
                <div className="flex items-start gap-3 sm:gap-4">
                  <div className="flex min-w-0 flex-1 items-center gap-3 sm:gap-4">
                    <div className="flex h-14 w-14 shrink-0 items-center justify-center rounded-2xl bg-gradient-to-br from-cyan-500/20 to-blue-500/20 shadow-inner sm:h-16 sm:w-16">
                      <Mail className="h-7 w-7 text-cyan-400 sm:h-8 sm:w-8" />
                    </div>
                    <div className="min-w-0 flex-1">
                      <h2
                        title={selectedAdmin.fullName || "Tài khoản admin"}
                        className="truncate text-lg font-bold tracking-tight text-white sm:overflow-visible sm:text-clip sm:whitespace-normal sm:text-xl"
                      >
                        {selectedAdmin.fullName || "Tài khoản admin"}
                      </h2>
                      <p
                        title={selectedAdmin.email}
                        className="truncate text-sm text-cyan-300/80"
                      >
                        {selectedAdmin.email}
                      </p>
                    </div>
                  </div>
                  <div className="ml-auto flex shrink-0 items-center gap-2 pt-1">
                    <FamilyTypeBadge
                      familyType={selectedAdmin.familyType}
                      planName={
                        profileData?.subscription?.planFullName ||
                        profileData?.subscription?.planName
                      }
                    />
                    {(selectedAdmin.accountStatus === "live" ||
                      selectedAdmin.accountStatus === "dead") && (
                      <span className="flex shrink-0 items-center gap-1.5 whitespace-nowrap text-xs text-slate-500">
                        {selectedAdmin.accountStatus === "live" ? (
                          <span className="text-emerald-400 drop-shadow-[0_0_8px_rgba(52,211,153,0.5)]">
                            Live
                          </span>
                        ) : (
                          <span className="text-red-400 drop-shadow-[0_0_8px_rgba(248,113,113,0.5)]">
                            Dead
                          </span>
                        )}
                      </span>
                    )}
                  </div>
                </div>
              </div>

              <div className="flex flex-col items-stretch gap-3 sm:flex-row sm:items-center sm:justify-between">
                <button
                  onClick={fetchProfile}
                  disabled={isFetching}
                  className="flex min-h-11 items-center justify-center gap-2 rounded-xl bg-cyan-500/10 px-6 py-2.5 text-sm font-semibold text-cyan-400 transition-all duration-200 hover:bg-cyan-500/20 disabled:cursor-not-allowed disabled:opacity-50 sm:w-auto"
                >
                  {isFetching ? (
                    "Đang kiểm tra..."
                  ) : (
                    <>
                      <UserCircle className="h-4 w-4" /> Kiểm tra tài khoản
                    </>
                  )}
                </button>
              </div>

              {profileData && (
                <motion.div
                  initial={{ opacity: 0, y: 10 }}
                  animate={{ opacity: 1, y: 0 }}
                  className="space-y-4"
                >
                  <div className="flex items-center justify-between">
                    <h3 className="text-xs font-bold uppercase tracking-wider text-slate-400">
                      Thông tin hiện tại
                    </h3>
                  </div>

                  {profileData?.error ? (
                    <div className="rounded-xl border border-red-500/20 bg-red-500/5 p-4">
                      <p className="text-sm text-red-300">{profileData.error}</p>
                    </div>
                  ) : (
                    <>
                      <SubscriptionCard data={profileData.subscription} />
                      <ProfileMetaCard
                        language={profileData.language}
                        checkedAt={profileData.checkedAt}
                        sharingError={enableSharing ? profileData.sharingError : null}
                      />
                      <MemberStorageCard
                        data={memberStorage}
                        loading={isFetchingMemberStorage}
                      />
                      {enableSharing && (
                        <SharingToggleCard
                          sharingStatus={sharingStatus}
                          isToggling={isTogglingSharing}
                          onToggle={handleToggleSharing}
                        />
                      )}
                      <PaymentProfileCard
                        data={profileData.paymentProfile}
                        confirmCloseId={confirmCloseId}
                        isClosingPayment={isClosingPayment}
                        onConfirmClose={(id) => setConfirmCloseId(id)}
                        onCancelClose={() => setConfirmCloseId(null)}
                        onClosePayment={handleClosePayment}
                      />

                      {profileErrors.length > 0 && (
                        <div className="rounded-xl border border-amber-500/20 bg-amber-500/5 p-4">
                          <h4 className="mb-2 text-[11px] font-bold uppercase tracking-wider text-amber-400">
                            Cảnh báo
                          </h4>
                          {profileErrors.map((err, idx) => (
                            <p key={idx} className="text-xs text-amber-300/80">
                              • {err}
                            </p>
                          ))}
                        </div>
                      )}
                    </>
                  )}
                </motion.div>
              )}
            </motion.div>
          )}
        </div>
      </div>
    </div>
  );
}

function ProfileMetaCard({
  language,
  checkedAt,
  sharingError,
}: {
  language?: string;
  checkedAt?: string;
  sharingError?: string | null;
}) {
  return (
    <div className="rounded-xl border border-white/[0.06] bg-white/[0.02] p-4">
      <h4 className="mb-3 text-[11px] font-bold uppercase tracking-wider text-sky-400">
        Metadata lần kiểm tra
      </h4>
      <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
        <div>
          <span className="text-[10px] uppercase tracking-wider text-slate-500">
            Ngôn ngữ tài khoản
          </span>
          <p className="mt-0.5 text-sm font-semibold text-white">
            {language || "Không có dữ liệu"}
          </p>
        </div>
        <div>
          <span className="text-[10px] uppercase tracking-wider text-slate-500">
            Thời điểm kiểm tra
          </span>
          <p className="mt-0.5 text-sm font-semibold text-white">
            {checkedAt ? new Date(checkedAt).toLocaleString("vi-VN") : "Không có dữ liệu"}
          </p>
        </div>
      </div>
      {sharingError ? (
        <p className="mt-3 rounded-lg border border-amber-500/20 bg-amber-500/5 px-3 py-2 text-xs text-amber-300">
          Cảnh báo chia sẻ: {sharingError}
        </p>
      ) : null}
    </div>
  );
}

function MemberStorageCard({
  data,
  loading,
}: {
  data: {
    totalUsedText: string | null;
    members: Array<{
      name: string;
      email: string;
      usedText: string | null;
      usedBytes: number | null;
    }>;
    error?: string;
  } | null;
  loading: boolean;
}) {
  const usagePercent = parseUsagePercent(data?.totalUsedText ?? null);

  return (
    <div className="rounded-xl border border-white/[0.06] bg-white/[0.02] p-4">
      <h4 className="mb-3 flex items-center gap-2 text-[11px] font-bold uppercase tracking-wider text-indigo-400">
        Dung lượng tổng
      </h4>
      {loading ? (
        <div className="flex items-center gap-2">
          <div className="h-4 w-4 animate-spin rounded-full border-2 border-indigo-400 border-t-transparent" />
          <span className="text-xs text-slate-400">Đang kiểm tra dung lượng...</span>
        </div>
      ) : !data ? (
        <p className="text-xs text-slate-400">
          Chưa có dữ liệu. Bấm &quot;Kiểm tra tài khoản&quot; để tải.
        </p>
      ) : (
        <>
          {usagePercent ? (
            <>
              <div className="mb-3 flex items-baseline justify-between">
                <p className="text-sm text-slate-300">
                  <span className="text-lg font-bold text-white">{usagePercent.usedLabel}</span>
                  <span className="text-slate-500"> / {usagePercent.totalLabel}</span>
                </p>
                <span className={`text-sm font-bold ${
                  usagePercent.percent > 80 ? "text-red-400" : usagePercent.percent > 50 ? "text-amber-400" : "text-indigo-400"
                }`}>
                  {usagePercent.percent.toFixed(1)}%
                </span>
              </div>
              <div className="h-2.5 overflow-hidden rounded-full bg-white/[0.06]">
                <div
                  className={`h-full rounded-full transition-all duration-500 ${
                    usagePercent.percent > 80
                      ? "bg-gradient-to-r from-red-500 to-red-400 shadow-[0_0_12px_rgba(239,68,68,0.4)]"
                      : usagePercent.percent > 50
                        ? "bg-gradient-to-r from-amber-500 to-amber-400 shadow-[0_0_12px_rgba(245,158,11,0.3)]"
                        : "bg-gradient-to-r from-indigo-500 to-cyan-400 shadow-[0_0_12px_rgba(99,102,241,0.4)]"
                  }`}
                  style={{ width: `${Math.max(0.5, Math.min(100, usagePercent.percent))}%` }}
                />
              </div>
            </>
          ) : (
            <p className="text-sm text-slate-400">
              Tổng đang dùng:{" "}
              <span className="font-semibold text-white">
                {data.totalUsedText || "Chưa có gia đình"}
              </span>
            </p>
          )}
          {data.error && <p className="mt-3 text-[11px] text-amber-400">{data.error}</p>}
        </>
      )}
    </div>
  );
}

function FamilyTypeBadge({
  familyType,
  planName,
}: {
  familyType?: string;
  planName?: string;
}) {
  let resolvedType = familyType;
  if (planName) {
    const p = planName.toLowerCase();
    if (p.includes("youtube")) resolvedType = "youtube";
    else if (p.includes("pro") || p.includes("ai pro")) resolvedType = "pro";
    else resolvedType = "ultra";
  }

  const config =
    resolvedType === "youtube"
      ? {
          label: "YTB Pre",
          cls: "bg-red-500/10 text-red-400 border-red-500/20",
        }
      : resolvedType === "pro"
        ? {
            label: "Gemini Pro",
            cls: "bg-emerald-500/10 text-emerald-400 border-emerald-500/20",
          }
        : {
            label: "Gemini Ultra",
            cls: "bg-violet-500/10 text-violet-400 border-violet-500/20",
          };
  return (
    <span
      title={planName || config.label}
      className={`inline-flex max-w-full shrink-0 whitespace-nowrap rounded-full border px-3 py-1 text-[11px] font-bold uppercase leading-tight tracking-wider sm:text-xs ${config.cls}`}
    >
      {config.label}
    </span>
  );
}

function SubscriptionCard({
  data,
}: {
  data?: { planFullName?: string; planName?: string; expiresAt?: string | null };
}) {
  return (
    <div className="rounded-xl border border-white/[0.06] bg-white/[0.02] p-4">
      <h4 className="mb-3 flex items-center gap-2 text-[11px] font-bold uppercase tracking-wider text-cyan-400">
        <Crown className="h-3.5 w-3.5" />
        Gói đăng ký (Subscription)
      </h4>
      <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
        <div>
          <span className="text-[10px] uppercase tracking-wider text-slate-500">
            Tên gói
          </span>
          <p className="mt-0.5 text-base font-bold text-white">
            {data?.planFullName || data?.planName || "Không xác định"}
          </p>
        </div>
        <div>
          <span className="text-[10px] uppercase tracking-wider text-slate-500">
            Ngày hết hạn / Gia hạn
          </span>
          <p className="mt-0.5 text-base font-bold text-white">
            {data?.expiresAt ? formatDateVN(data.expiresAt) : "Không có dữ liệu"}
          </p>
        </div>
      </div>
    </div>
  );
}

interface PaymentProfileCardProps {
  data?: PaymentProfile;
  confirmCloseId: string | null;
  isClosingPayment: boolean;
  onConfirmClose: (id: string) => void;
  onCancelClose: () => void;
  onClosePayment: () => void;
}

function PaymentProfileCard({
  data,
  confirmCloseId,
  isClosingPayment,
  onConfirmClose,
  onCancelClose,
  onClosePayment,
}: PaymentProfileCardProps) {
  return (
    <div className="rounded-xl border border-white/[0.06] bg-white/[0.02] p-4">
      <h4 className="mb-3 flex items-center gap-2 text-[11px] font-bold uppercase tracking-wider text-amber-400">
        Trạng thái hồ sơ thanh toán
      </h4>

      <div className="mb-3">
        {data?.exists ? (
          <span className="inline-flex items-center gap-1.5 rounded-full bg-emerald-500/10 px-3 py-1.5 text-xs font-bold text-emerald-400 border border-emerald-500/20">
            Đang tồn tại hồ sơ thanh toán
          </span>
        ) : (
          <span className="inline-flex items-center gap-1.5 rounded-full bg-red-500/10 px-3 py-1.5 text-xs font-bold text-red-400 border border-red-500/20">
            Hồ sơ thanh toán trống / Đã xóa
          </span>
        )}
      </div>

      {data?.exists && (
        <>
          <div className="grid grid-cols-1 gap-3 rounded-lg border border-white/[0.04] bg-white/[0.02] p-3 sm:grid-cols-2">
            <ProfileField label="Tên hồ sơ" value={data.profileName} />
            <ProfileField
              label="Profile ID"
              value={data.profileIdFormatted || data.profileId}
              mono
            />
            <ProfileField label="Quốc gia" value={data.country} />
            {data.email && <ProfileField label="Email" value={data.email} />}
          </div>
          <div className="mt-3">
            {confirmCloseId === data.profileId ? (
              <div className="flex flex-col gap-2 sm:flex-row sm:items-center">
                <button
                  onClick={onClosePayment}
                  disabled={isClosingPayment}
                  className="inline-flex min-h-11 items-center justify-center gap-2 rounded-lg bg-red-500 px-4 py-2 text-xs font-bold text-white transition-all hover:bg-red-600 disabled:cursor-not-allowed disabled:opacity-50"
                >
                  {isClosingPayment ? (
                    "Đang đóng..."
                  ) : (
                    <>
                      <Trash2 className="h-3.5 w-3.5" /> Xác nhận đóng
                    </>
                  )}
                </button>
                <button
                  onClick={onCancelClose}
                  disabled={isClosingPayment}
                  className="inline-flex min-h-11 items-center justify-center gap-2 rounded-lg bg-slate-800 px-4 py-2 text-xs font-bold text-slate-300 transition-all hover:bg-slate-700 disabled:cursor-not-allowed disabled:opacity-50"
                >
                  Hủy
                </button>
              </div>
            ) : (
              <button
                onClick={() => {
                  if (data.profileId) onConfirmClose(data.profileId);
                }}
                disabled={!data.profileId}
                className="inline-flex min-h-11 w-full items-center justify-center gap-2 rounded-lg border border-red-500/20 bg-red-500/10 px-4 py-2 text-xs font-bold text-red-400 transition-all hover:border-red-500/40 hover:bg-red-500/20 disabled:opacity-50 sm:w-auto"
              >
                <Trash2 className="h-3.5 w-3.5" />
                Đóng hồ sơ thanh toán
              </button>
            )}
          </div>
        </>
      )}
    </div>
  );
}

function SharingToggleCard({
  sharingStatus,
  isToggling,
  onToggle,
}: {
  sharingStatus: boolean | null;
  isToggling: boolean;
  onToggle: (enable: boolean) => void;
}) {
  return (
    <div className="rounded-xl border border-white/[0.06] bg-white/[0.02] p-4">
      <h4 className="mb-3 flex items-center gap-2 text-[11px] font-bold uppercase tracking-wider text-blue-400">
        <Share2 className="h-3.5 w-3.5" />
        Chia sẻ Google One với gia đình
      </h4>

      {sharingStatus === null ? (
        <p className="text-xs text-slate-400">
          Bấm &quot;Kiểm tra tài khoản&quot; để cập nhật trạng thái chia sẻ.
        </p>
      ) : (
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-3">
            <span
              className={`inline-flex items-center gap-1.5 rounded-full px-3 py-1.5 text-xs font-bold border ${
                sharingStatus
                  ? "bg-emerald-500/10 text-emerald-400 border-emerald-500/20"
                  : "bg-slate-500/10 text-slate-400 border-slate-500/20"
              }`}
            >
              {sharingStatus ? "Đang bật chia sẻ" : "Đang tắt chia sẻ"}
            </span>
            {isToggling && (
              <div className="h-3.5 w-3.5 animate-spin rounded-full border-2 border-blue-400 border-t-transparent" />
            )}
          </div>

          <button
            onClick={() => onToggle(!sharingStatus)}
            disabled={isToggling}
            className={`relative inline-flex h-7 w-12 shrink-0 cursor-pointer items-center rounded-full transition-colors duration-300 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-offset-[#0f1729] disabled:cursor-not-allowed disabled:opacity-50 ${
              sharingStatus
                ? "bg-emerald-500 focus:ring-emerald-500/50 shadow-[0_0_12px_rgba(52,211,153,0.3)]"
                : "bg-slate-600 focus:ring-slate-500/50"
            }`}
          >
            <span
              className={`inline-block h-5 w-5 transform rounded-full bg-white shadow-md transition-transform duration-300 ${
                sharingStatus ? "translate-x-[22px]" : "translate-x-[2px]"
              }`}
            />
          </button>
        </div>
      )}
    </div>
  );
}

function ProfileField({
  label,
  value,
  mono,
}: {
  label: string;
  value?: string;
  mono?: boolean;
}) {
  return (
    <div>
      <span className="text-[10px] uppercase tracking-wider text-slate-500">{label}</span>
      <p
        className={`break-words text-sm font-semibold text-slate-200 ${
          mono ? "font-mono" : ""
        }`}
      >
        {value || "Không có dữ liệu"}
      </p>
    </div>
  );
}
