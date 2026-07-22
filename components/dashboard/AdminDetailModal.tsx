"use client";
import { useState } from "react";
import { motion, AnimatePresence } from "framer-motion";
import {
  X,
  Copy,
  RefreshCw,
  UserPlus,
  Trash2,
  Edit3,
  ClipboardCopy,
  XCircle,
  Eye,
  EyeOff,
} from "lucide-react";
import AddAdminModal from "./AddAdminModal";
import MemberDetailModal from "./MemberDetailModal";
import InviteModal from "./InviteModal";
import SetCreditModal from "./SetCreditModal";
import AnimatedNumber from "./AnimatedNumber";
import SyncToast from "./SyncToast";
import { useCopy } from "@/hooks/useCopy";
import { useTotp } from "@/hooks/useTotp";
import { useAdminDetail } from "@/hooks/useAdminDetail";
import { ConfirmModal } from "@/components/ui/ConfirmModal";
import { StatusBadge } from "@/components/ui/StatusBadge";
import {
  getMaxMembers,
  isUltraType,
  getMemberExpiryMeta,
  isSyncableAccountStatus,
  getAdminBadgeKind,
} from "@/lib/utils";

interface Props {
  adminId: string;
  onClose: () => void;
  onSyncStart?: (adminId: string) => void;
  onRefresh?: () => void;
  onBackgroundRefreshNeeded?: () => void;
  globalSyncing?: boolean;
}

export default function AdminDetailModal({
  adminId,
  onClose,
  onSyncStart,
  onRefresh,
  onBackgroundRefreshNeeded,
  globalSyncing = false,
}: Props) {
  const {
    admin,
    loading,
    credentials,
    credLoading,
    syncLog,
    showPassword,
    handleSync,
    handleTogglePassword,
    copyAll,
    fetchAdmin,
    deleting,
    handleDelete,
    showEditModal,
    setShowEditModal,
    handleEditSubmit,
    showInvite,
    setShowInvite,
    inviteEmail,
    setInviteEmail,
    inviting,
    inviteResult,
    closeInviteModal,
    handleInvite,
    revokingId,
    handleRevokeInvite,
    creatingFamily,
    handleCreateFamily,
    setCreditMember,
    setSetCreditMember,
    creditInput,
    setCreditInput,
    settingCredit,
    handleSetCredit,
  } = useAdminDetail({
    adminId,
    onClose,
    onRefresh,
    onBackgroundRefreshNeeded,
    onSyncStart,
    globalSyncing,
  });

  const {
    code: totp,
    countdown: totpCountdown,
    progressRef: totpProgressRef,
  } = useTotp(adminId, !!admin?.has2FA);
  const [copied, copyToClipboard] = useCopy();
  const [selectedMember, setSelectedMember] = useState<
    NonNullable<typeof admin>["members"][0] | null
  >(null);
  const [deleteConfirmOpen, setDeleteConfirmOpen] = useState(false);

  const maxMembers = getMaxMembers(admin?.familyType);
  const isFull = admin ? admin.members.length >= maxMembers : false;
  const isUltra = isUltraType(admin?.familyType);
  const isAccountManager = admin?.members.some(
    (m) => m.email === admin.email && m.role.includes("MANAGER"),
  ) ?? false;
  const syncDisabled = globalSyncing || !isSyncableAccountStatus(admin?.accountStatus);

  const handleCopyAll = async () => {
    const text = await copyAll();
    if (text) copyToClipboard(text, "all");
  };

  const handleMemberUpdated = () => {
    void fetchAdmin();
    onBackgroundRefreshNeeded?.();
  };

  if (loading)
    return (
      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        exit={{ opacity: 0 }}
        className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm"
        onClick={onClose}
      >
        <div className="h-10 w-10 animate-spin rounded-full border-2 border-violet-400 border-t-transparent" />
      </motion.div>
    );

  if (!admin) return null;

  const usedPercent =
    admin.monthlyCredit > 0
      ? Math.round(
          (Math.max(0, admin.monthlyCredit - admin.remainingCredit) /
            admin.monthlyCredit) *
            100,
        )
      : 0;
  const initial =
    admin.displayName?.charAt(0)?.toUpperCase() || admin.email.charAt(0).toUpperCase();
  const totpFormatted = totp ? `${totp.slice(0, 3)} ${totp.slice(3)}` : "--- ---";

  return (
    <>
      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        exit={{ opacity: 0 }}
        className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 p-4 backdrop-blur-sm"
        onClick={onClose}
      >
        <motion.div
          initial={{ scale: 0.9, opacity: 0 }}
          animate={{ scale: 1, opacity: 1 }}
          exit={{ scale: 0.9, opacity: 0 }}
          transition={{ type: "spring", damping: 25 }}
          onClick={(e) => e.stopPropagation()}
          role="dialog"
          aria-modal="true"
          aria-labelledby="admin-detail-title"
          className="relative max-h-[calc(100vh-2rem)] w-full max-w-xl overflow-y-auto rounded-2xl border border-white/[0.06] bg-[#0f1729] shadow-2xl"
        >

          <button
            onClick={onClose}
            aria-label="Đóng chi tiết admin"
            className="absolute right-3 top-3 z-10 rounded-lg p-1.5 text-slate-400 transition-colors hover:bg-white/[0.06] hover:text-white"
          >
            <X className="h-4 w-4" />
          </button>

          <div className="space-y-3 px-5 py-4">

            <div className="flex flex-col items-center">
              <div
                className="mb-1 flex h-11 w-11 items-center justify-center rounded-xl bg-indigo-500 text-lg font-bold text-white shadow-lg ring-2 ring-white/10"
              >
                {initial}
              </div>
              <p id="admin-detail-title" className="text-base font-bold text-white">{admin.displayName}</p>
              <p className="text-xs text-slate-400">{admin.email}</p>
              <div className="mt-2">
                <StatusBadge kind={getAdminBadgeKind({
                  id: admin.id,
                  email: admin.email,
                  fullName: admin.displayName,
                  memberCount: admin.memberCount,
                  familyType: admin.familyType,
                  accountStatus: admin.accountStatus,
                  lastSyncAt: admin.lastSyncAt,
                  lastSyncStatus: admin.lastSyncStatus,
                  lastSyncError: admin.lastSyncError,
                  usedStorageMB: admin.usedStorageMB,
                  storageTB: admin.storageTB,
                  planName: admin.planName,
                  planExpiresAt: admin.planExpiresAt,
                })} />
              </div>
            </div>

            <div className="rounded-xl border border-white/[0.06] bg-white/[0.02] px-4 py-3">
              <div className="mb-2 flex items-center justify-between">
                <span className="text-sm font-semibold text-violet-400">
                  Tài khoản
                </span>
                <button
                  onClick={handleCopyAll}
                  className="flex items-center gap-1 rounded-lg bg-white/[0.06] px-2.5 py-0.5 text-xs text-slate-300 transition-colors hover:bg-white/[0.1]"
                >
                  <ClipboardCopy className="h-3 w-3" />
                  {copied === "all" ? "Đã sao chép!" : "Sao chép"}
                </button>
              </div>
              <div className="space-y-1.5 font-mono text-sm">
                <p className="text-slate-300">
                  <span className="text-slate-500">Email: </span>
                  <span className="text-white">{admin.email}</span>
                </p>
                                <div className="flex items-center justify-between text-slate-300">
                  <div>
                    <span className="text-slate-500">Mật khẩu: </span>
                    <span className="text-white">
                      {credLoading
                        ? "Đang tải..."
                        : showPassword && credentials
                          ? credentials.googlePassword
                          : "••••••••"}
                    </span>
                  </div>
                  <button
                    type="button"
                    onClick={handleTogglePassword}
                    aria-label={showPassword ? "Ẩn credential" : "Hiện credential"}
                    className="text-slate-400 transition-colors hover:text-white"
                  >
                    {showPassword ? (
                      <EyeOff className="h-4 w-4" />
                    ) : (
                      <Eye className="h-4 w-4" />
                    )}
                  </button>
                </div>
                <p className="text-slate-300">
                  <span className="text-slate-500">2FA: </span>
                  <span className="text-white">
                    {admin.has2FA
                      ? showPassword && credentials
                        ? credentials.totpSecret
                        : "••••••••"
                      : "-"}
                  </span>
                </p>
              </div>
            </div>

            {admin.has2FA && (
              <div className="rounded-xl border border-white/[0.06] bg-white/[0.02] px-4 py-3">
                <div className="mb-1.5 flex items-center justify-between">
                  <span className="text-sm font-semibold text-violet-400">TOTP</span>
                  <span className="text-xs text-slate-400">{totpCountdown}s</span>
                </div>
                <div className="mb-2 flex items-center gap-3">
                  {totp ? (
                    <>
                      <span className="font-mono text-3xl font-extrabold tracking-[0.15em] text-white">
                        {totpFormatted}
                      </span>
                      <button
                        onClick={() => copyToClipboard(totp, "totp")}
                        aria-label="Sao chép mã TOTP"
                        className="rounded-lg bg-violet-500/20 p-1.5 text-violet-300 transition-colors hover:bg-violet-500/30 hover:text-white"
                      >
                        <Copy className="h-4 w-4" />
                      </button>
                    </>
                  ) : (
                    <span className="text-xs text-slate-400">
                      Mã OTP đang được ẩn phía máy chủ.
                    </span>
                  )}
                </div>
                <div className="relative h-1.5 w-full overflow-hidden rounded-full bg-white/[0.07] shadow-inner shadow-black/30">
                  <div className="absolute inset-0 bg-gradient-to-r from-transparent via-white/[0.08] to-transparent" />
                  <div
                    ref={totpProgressRef}
                    className="absolute inset-y-0 left-0 w-full origin-left rounded-full bg-gradient-to-r from-violet-500 via-fuchsia-400 to-cyan-300 shadow-[0_0_14px_rgba(168,85,247,0.65)] will-change-transform"
                    style={{ transform: "scaleX(1)" }}
                  >
                    <span className="absolute right-0 top-1/2 h-2 w-2 -translate-y-1/2 rounded-full bg-white/90 shadow-[0_0_12px_rgba(255,255,255,0.9)]" />
                  </div>
                </div>
              </div>
            )}

            <div className="flex items-center gap-2">
              <button
                onClick={handleSync}
                disabled={syncDisabled}
                className="flex items-center gap-1.5 rounded-lg border border-white/[0.1] bg-white/[0.04] px-3 py-1.5 text-xs font-semibold text-white transition-all hover:bg-white/[0.08] disabled:opacity-50"
              >
                <RefreshCw
                  className={`h-3.5 w-3.5 ${globalSyncing ? "animate-spin" : ""}`}
                />
                Đồng bộ ngay
              </button>
              <span className="text-[11px] text-slate-500">
                {admin.lastSyncAt
                  ? `Lần cuối: ${new Date(admin.lastSyncAt).toLocaleString("vi-VN")}`
                  : "Chưa đồng bộ"}
              </span>
            </div>

            {isUltra && (
              <div className="grid grid-cols-2 gap-2">
                <div className="rounded-xl border border-emerald-500/20 bg-emerald-500/[0.06] px-3 py-2 text-center">
                  <p className="text-[9px] uppercase tracking-widest text-slate-400">
                    Credit còn
                  </p>
                  <p className="text-xl font-extrabold text-emerald-400">
                    <AnimatedNumber value={admin.remainingCredit} />
                  </p>
                  <p className="text-[10px] text-slate-500">
                    / {admin.monthlyCredit.toLocaleString("vi-VN")}
                  </p>
                </div>
                <div className="rounded-xl border border-amber-500/20 bg-amber-500/[0.06] px-3 py-2 text-center">
                  <p className="text-[9px] uppercase tracking-widest text-slate-400">
                    Đã dùng
                  </p>
                  <p className="text-xl font-extrabold text-amber-400">
                    <AnimatedNumber
                      value={Math.max(0, admin.monthlyCredit - admin.remainingCredit)}
                    />
                  </p>
                  <p className="text-[10px] text-slate-500">{usedPercent}%</p>
                </div>
              </div>
            )}

            <div>
              <div className="mb-1.5 flex items-center justify-between">
                <span className="text-sm font-semibold text-white">
                  {admin.members.length === 0 ? (
                    "Gia đình"
                  ) : (
                    <>
                      Thành viên ({admin.members.length}
                      {maxMembers === Infinity ? "" : `/${maxMembers}`})
                    </>
                  )}
                  {admin.members.length > 0 && isFull ? (
                    <span className="ml-1 rounded bg-red-500/20 px-1.5 py-0.5 text-xs font-bold text-red-400">
                      Đã đầy
                    </span>
                  ) : (
                    admin.members.length > 0 &&
                    maxMembers !== Infinity && (
                      <>
                        {" "}
                        ·{" "}
                        <span className="text-emerald-400">
                          {maxMembers - admin.members.length} slot trống
                        </span>
                      </>
                    )
                  )}
                </span>
                {admin.members.length > 0 && !isFull && isAccountManager && (
                  <button
                    onClick={() => setShowInvite(true)}
                    className="flex items-center gap-1 rounded-lg border border-cyan-500/30 bg-cyan-500/10 px-2.5 py-0.5 text-xs font-semibold text-cyan-300 transition-all hover:bg-cyan-500/20"
                  >
                    <UserPlus className="h-3 w-3" />
                    Thêm TV
                  </button>
                )}
              </div>

              {admin.members.length === 0 ? (
                <div className="flex flex-col items-center gap-2 py-3">
                  {admin.lastSyncAt ? (
                    <>
                      <p className="text-sm text-amber-400">Chưa tạo gia đình</p>
                      {["ultra", "pro", "youtube"].includes(
                        admin.familyType || "ultra",
                      ) && (
                        <button
                          onClick={handleCreateFamily}
                          disabled={creatingFamily}
                          className="flex items-center gap-2 rounded-lg border border-violet-500/30 bg-violet-500/15 px-4 py-2 text-sm font-semibold text-violet-300 transition-all hover:bg-violet-500/25 disabled:opacity-50"
                        >
                          {creatingFamily ? (
                            <RefreshCw className="h-4 w-4 animate-spin" />
                          ) : (
                            <UserPlus className="h-4 w-4" />
                          )}
                          {creatingFamily ? "Đang tạo..." : "Tạo gia đình"}
                        </button>
                      )}
                    </>
                  ) : (
                    <>
                      <p className="text-sm text-blue-400">
                        Vui lòng đồng bộ để lấy thông tin
                      </p>
                      <button
                        onClick={handleSync}
                        disabled={syncDisabled}
                        className="flex items-center gap-2 rounded-lg border border-blue-500/30 bg-blue-500/15 px-4 py-2 text-sm font-semibold text-blue-300 transition-all hover:bg-blue-500/25 disabled:opacity-50"
                      >
                        <RefreshCw
                          className={`h-4 w-4 ${globalSyncing ? "animate-spin" : ""}`}
                        />
                        {globalSyncing ? "Đang đồng bộ..." : "Đồng bộ ngay"}
                      </button>
                    </>
                  )}
                </div>
              ) : (
                <div className="space-y-2">
                  {admin.members.map((m) => {
                    const expiryMeta = getMemberExpiryMeta({
                      endDate: m.endDate,
                      role: m.role,
                      status: m.status,
                      renewed: m.renewed,
                    }, admin.planExpiresAt);

                    return (
                      <div
                        key={m.id}
                        onClick={() => setSelectedMember(m)}
                        className="flex cursor-pointer items-center justify-between rounded-xl border border-white/[0.04] bg-white/[0.02] px-4 py-2.5 transition-all hover:border-white/[0.1] hover:bg-white/[0.05]"
                      >
                        <div>
                          <div className="flex flex-wrap items-center gap-2">
                            <p className="text-sm font-semibold text-white">
                              {m.name || m.email}
                            </p>
                            {expiryMeta && (
                              <span
                                className={`rounded-full px-2 py-0.5 text-[10px] font-semibold ${expiryMeta.badgeClass}`}
                              >
                                {expiryMeta.label}
                              </span>
                            )}
                          </div>
                          {m.name && m.name !== m.email && (
                            <p className="text-xs text-slate-400">{m.email}</p>
                          )}
                        </div>
                        <div className="flex items-center gap-2">
                          {m.status === "invited" && isAccountManager && (
                            <button
                              onClick={(e) => {
                                e.stopPropagation();
                                handleRevokeInvite(m.googleUserId, m.email);
                              }}
                              disabled={revokingId === (m.googleUserId || m.email)}
                              className="flex items-center gap-1 rounded-md border border-red-500/20 bg-red-500/10 px-2 py-0.5 text-[10px] font-medium text-red-400 transition-all hover:bg-red-500/20 disabled:opacity-50"
                              title="Thu hồi lời mời"
                            >
                              {revokingId === (m.googleUserId || m.email) ? (
                                <RefreshCw className="h-3 w-3 animate-spin" />
                              ) : (
                                <XCircle className="h-3 w-3" />
                              )}
                              Thu hồi
                            </button>
                          )}
                          {isUltra &&
                            !m.role.includes("MANAGER") &&
                            m.status !== "invited" &&
                            (() => {
                              const used = Math.abs(m.lastCreditUsed ?? 0);
                              const limit = m.creditLimit ?? 0;
                              const overLimit = limit > 0 && used > limit;
                              return (
                                <span
                                  className={`rounded-lg px-3 py-1 text-sm font-mono border ${overLimit ? "border-red-500/40 bg-red-500/10" : "border-white/[0.08] bg-white/[0.04]"}`}
                                >
                                  <span
                                    className={`font-bold ${overLimit ? "text-red-400" : "text-amber-400"}`}
                                  >
                                    {used}
                                  </span>
                                  <span className="text-slate-500"> / </span>
                                  <span
                                    className={
                                      limit
                                        ? "text-emerald-400 font-bold"
                                        : "text-slate-500"
                                    }
                                  >
                                    {limit ? limit : "∞"}
                                  </span>
                                  {overLimit && (
                                    <span
                                      className="ml-1 text-red-400"
                                      title="Vượt giới hạn"
                                    >
                                      !
                                    </span>
                                  )}
                                </span>
                              );
                            })()}
                          {isUltra &&
                            !m.role.includes("MANAGER") &&
                            m.status !== "invited" && (
                              <button
                                onClick={(e) => {
                                  e.stopPropagation();
                                  setSetCreditMember({
                                    googleUserId: m.googleUserId,
                                    name: m.name || m.email,
                                    email: m.email,
                                    currentLimit: m.creditLimit ?? 0,
                                  });
                                  setCreditInput(String(m.creditLimit ?? 0));
                                }}
                                className="rounded-md border border-amber-500/25 bg-amber-500/10 px-2 py-0.5 text-[10px] font-semibold text-amber-400 transition-all hover:bg-amber-500/20"
                              >
                                Đặt
                              </button>
                            )}
                          <span
                            className={`rounded-md px-2 py-0.5 text-[10px] font-semibold ${
                              m.role.includes("MANAGER")
                                ? "bg-amber-500/15 text-amber-400"
                                : m.status === "disabled"
                                  ? "bg-red-500/15 text-red-400"
                                  : m.status === "invited"
                                    ? "bg-orange-500/15 text-orange-400"
                                    : "bg-white/[0.06] text-slate-400"
                            }`}
                          >
                            {m.role.includes("MANAGER")
                              ? "Quản lý"
                              : m.status === "disabled"
                                ? "Vô hiệu hóa"
                                : m.status === "invited"
                                  ? "Đang chờ"
                                  : "Thành viên"}
                          </span>
                        </div>
                      </div>
                    );
                  })}
                </div>
              )}
            </div>
          </div>

          <div className="flex justify-end gap-2 border-t border-white/[0.06] px-5 py-2.5">
            <button
              onClick={() => setDeleteConfirmOpen(true)}
              disabled={deleting}
              className="flex items-center gap-1.5 rounded-xl border border-red-500/30 bg-red-500/10 px-5 py-2 text-sm font-semibold text-red-400 transition-all hover:bg-red-500/20 disabled:opacity-50"
            >
              <Trash2 className="h-4 w-4" />
              {deleting ? "Đang xóa..." : "Xóa"}
            </button>
            <button
              onClick={() => {
                setShowEditModal(true);
              }}
              className="flex items-center gap-1.5 rounded-xl border border-white/[0.1] bg-white/[0.04] px-5 py-2 text-sm font-semibold text-white transition-all hover:bg-white/[0.08]"
            >
              <Edit3 className="h-4 w-4" /> Sửa
            </button>
            <button
              onClick={() => {
                onClose();
              }}
              className="rounded-xl border border-white/[0.1] bg-white/[0.04] px-5 py-2 text-sm font-semibold text-white transition-all hover:bg-white/[0.08]"
            >
              Đóng
            </button>
          </div>
        </motion.div>
      </motion.div>

      <AnimatePresence>
        {showEditModal && admin && (
          <AddAdminModal
            mode="edit"
            initialData={{
              email: admin.email,
              displayName: admin.displayName,
              googlePassword: credentials?.googlePassword || "",
              totpSecret: credentials?.totpSecret || "",
              monthlyCredit: admin.monthlyCredit,
              storageTB: admin.storageTB,
              familyType: admin.familyType || "ultra",
              note: admin.note,
            }}
            forcedFamilyType={admin.familyType || "ultra"}
            onClose={() => setShowEditModal(false)}
            onSubmit={handleEditSubmit}
          />
        )}
      </AnimatePresence>

      {!globalSyncing && <SyncToast log={syncLog} />}

      <ConfirmModal
        open={deleteConfirmOpen}
        title="Xóa admin này?"
        description="Tài khoản admin và dữ liệu member liên quan sẽ bị xóa. Thao tác này không ảnh hưởng credential/TOTP của các tài khoản khác."
        confirmText="Xóa"
        cancelText="Hủy"
        loading={deleting}
        variant="danger"
        onCancel={() => setDeleteConfirmOpen(false)}
        onConfirm={() => {
          void handleDelete();
          setDeleteConfirmOpen(false);
        }}
      />

      {showInvite && (
        <InviteModal
          inviteEmail={inviteEmail}
          setInviteEmail={setInviteEmail}
          inviting={inviting}
          inviteResult={inviteResult}
          onClose={closeInviteModal}
          onInvite={handleInvite}
        />
      )}

      {setCreditMember && admin && (
        <SetCreditModal
          member={setCreditMember}
          creditInput={creditInput}
          setCreditInput={setCreditInput}
          settingCredit={settingCredit}
          monthlyCredit={admin.monthlyCredit}
          onClose={() => setSetCreditMember(null)}
          onSubmit={handleSetCredit}
        />
      )}

      <AnimatePresence>
        {selectedMember && admin && (
          <MemberDetailModal
            adminId={adminId}
            member={{
              ...selectedMember,
              startDate: selectedMember.startDate ?? null,
              endDate: selectedMember.endDate ?? null,
              renewed: selectedMember.renewed ?? false,
            }}
            familyType={admin.familyType}
            onClose={() => setSelectedMember(null)}
            onUpdated={handleMemberUpdated}
          />
        )}
      </AnimatePresence>
    </>
  );
}
