"use client";
import { useState, useEffect, useCallback, useRef } from "react";
import { useToast } from "./useToast";
import { isSyncableAccountStatus } from "@/lib/utils";
import { notifyDashboardDataChanged, subscribeDashboardDataChanged } from "@/lib/browser/dashboard-events";
import type { AdminFormData } from "@/components/dashboard/AddAdminModal";

export interface AdminDetail {
  id: string;
  email: string;
  displayName: string;
  hasPassword: boolean;
  has2FA: boolean;
  monthlyCredit: number;
  remainingCredit: number;
  storageTB: number;
  usedStorageMB: number;
  memberCount: number;
  accountStatus: string;
  planName: string;
  planExpiresAt: string | null;
  familyType: string;
  lastSyncAt: string | null;
  lastSyncStatus: string;
  lastSyncError: string;
  note: string;
  members: MemberRow[];
}

export interface MemberRow {
  id: string;
  email: string;
  name: string;
  role: string;
  status: string;
  joinedAt: string | null;
  creditLimit?: number;
  lastCreditUsed?: number;
  startDate?: string | null;
  endDate?: string | null;
  renewed?: boolean;
  googleUserId?: string;
}

export interface Credentials {
  googlePassword: string;
  totpSecret: string;
  hasPassword: boolean;
  hasTotp: boolean;
  totpEnabled: boolean;
  lastUpdatedAt: string | null;
}

interface UseAdminDetailOptions {
  adminId: string;
  onClose: () => void;
  onRefresh?: () => void;
  onBackgroundRefreshNeeded?: () => void;
  onSyncStart?: (adminId: string) => void;
  globalSyncing?: boolean;
}

type FetchAdminOptions = {
  silent?: boolean;
};

export function useAdminDetail({
  adminId,
  onClose,
  onRefresh,
  onBackgroundRefreshNeeded,
  onSyncStart,
  globalSyncing = false,
}: UseAdminDetailOptions) {
  const [admin, setAdmin] = useState<AdminDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [showPassword, setShowPassword] = useState(false);
  const [credentials, setCredentials] = useState<Credentials | null>(null);
  const [credLoading, setCredLoading] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [showInvite, setShowInvite] = useState(false);
  const [inviteEmail, setInviteEmail] = useState("");
  const [inviting, setInviting] = useState(false);
  const [inviteResult, setInviteResult] = useState<{
    msg: string;
    type: "success" | "error";
  } | null>(null);
  const [revokingId, setRevokingId] = useState<string | null>(null);
  const [setCreditMember, setSetCreditMember] = useState<{
    googleUserId?: string;
    name: string;
    email: string;
    currentLimit: number;
  } | null>(null);
  const [creditInput, setCreditInput] = useState("0");
  const [settingCredit, setSettingCredit] = useState(false);
  const [creatingFamily, setCreatingFamily] = useState(false);
  const { toast: syncLog, show: showToast } = useToast(5_000);
  const fetchingAdminRef = useRef(false);

  const fetchAdmin = useCallback(async (options: FetchAdminOptions = {}) => {
    if (options.silent && fetchingAdminRef.current) return;
    fetchingAdminRef.current = true;
    try {
      const res = await fetch(`/api/admin/${adminId}`);
      if (!res.ok) throw new Error("Không tải được chi tiết admin");
      setAdmin(await res.json());
    } catch (error) {
      if (!options.silent) {
        showToast(error instanceof Error ? error.message : "Không tải được chi tiết admin", "error");
      }
    } finally {
      fetchingAdminRef.current = false;
    }
    setLoading(false);
  }, [adminId, showToast]);

  useEffect(() => {
    fetchAdmin();
  }, [fetchAdmin]);

  useEffect(
    () => subscribeDashboardDataChanged(() => void fetchAdmin({ silent: true })),
    [fetchAdmin],
  );

  const prevSyncingRef = useRef(globalSyncing);
  useEffect(() => {
    if (prevSyncingRef.current && !globalSyncing) fetchAdmin();
    prevSyncingRef.current = globalSyncing;
  }, [globalSyncing, fetchAdmin]);

  const handleSync = useCallback(() => {
    if (globalSyncing) return;
    if (!isSyncableAccountStatus(admin?.accountStatus)) return;
    onSyncStart?.(adminId);
  }, [admin?.accountStatus, adminId, globalSyncing, onSyncStart]);

  const fetchCredentials = useCallback(async () => {
    if (credentials || credLoading) return;
    setCredLoading(true);
    try {
      const res = await fetch(`/api/admin/${adminId}/credentials`);
      if (res.ok) setCredentials(await res.json());
    } catch {
      showToast("Không tải được credential", "error");
    }
    setCredLoading(false);
  }, [adminId, credentials, credLoading, showToast]);

  const handleTogglePassword = useCallback(() => {
    if (!showPassword && !credentials) fetchCredentials();
    setShowPassword((prev) => !prev);
  }, [showPassword, credentials, fetchCredentials]);

  const copyAll = useCallback(async () => {
    if (!admin) return;
    let creds = credentials;
    if (!creds) {
      setCredLoading(true);
      try {
        const res = await fetch(`/api/admin/${adminId}/credentials`);
        if (res.ok) {
          creds = await res.json();
          setCredentials(creds);
        }
      } catch {
        showToast("Không tải được credential", "error");
      }
      setCredLoading(false);
    }
    const text = [
      `Email: ${admin.email}`,
      `Pass: ${creds?.googlePassword || "N/A"}`,
      `2FA: ${creds?.totpSecret || "N/A"}`,
    ].join("\n");
    return text;
  }, [admin, adminId, credentials, showToast]);

  const handleDelete = useCallback(async () => {
    setDeleting(true);
    try {
      const res = await fetch(`/api/admin/${adminId}`, { method: "DELETE" });
      if (res.ok) {
        showToast("Đã xóa admin", "success");
        notifyDashboardDataChanged("admin-delete");
        onRefresh?.();
        onClose();
      } else {
        const data = await res.json().catch(() => null);
        showToast(data?.error || "Xóa admin thất bại", "error");
      }
    } catch {
      showToast("Lỗi kết nối", "error");
    }
    setDeleting(false);
  }, [adminId, onClose, onRefresh, showToast]);

  const handleEditSubmit = useCallback(
    async (data: AdminFormData) => {
      try {
        const res = await fetch(`/api/admin/${adminId}`, {
          method: "PUT",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(data),
        });
        if (res.ok) {
          setShowEditModal(false);
          await fetchAdmin();
          notifyDashboardDataChanged("admin-update");
          onRefresh?.();
          showToast("Đã cập nhật admin", "success");
        } else {
          const err = await res.json().catch(() => null);
          showToast(err?.error || "Cập nhật admin thất bại", "error");
        }
      } catch {
        showToast("Lỗi kết nối", "error");
      }
    },
    [adminId, fetchAdmin, onRefresh, showToast],
  );

  const closeInviteModal = useCallback(() => {
    setShowInvite(false);
    setInviteResult(null);
    setInviteEmail("");
  }, []);

  const handleInvite = useCallback(async () => {
    const emails = inviteEmail
      .split(/[,;\s]+/)
      .filter((e) => e.includes("@"))
      .map((e) => e.trim().toLowerCase());
    if (emails.length === 0) return;

    const existingEmails = new Set(
      (admin?.members || []).map((m) => m.email.toLowerCase()),
    );
    const duplicates = emails.filter((e) => existingEmails.has(e));
    const newEmails = emails.filter((e) => !existingEmails.has(e));

    if (newEmails.length === 0) {
      setInviteResult({
        msg: `❌ ${duplicates.join(", ")} đã có trong danh sách`,
        type: "error",
      });
      return;
    }

    setInviting(true);
    setInviteResult(null);
    let toastResult: { msg: string; type: "success" | "error" } | null = null;
    try {
      const res = await fetch(`/api/admin/${adminId}/invite`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ emails: newEmails }),
      });
      const data = await res.json();
      if (res.ok && data.results) {
        const ok = data.results.filter((r: { success: boolean }) => r.success).length;
        const fail = data.results.filter((r: { success: boolean }) => !r.success).length;
        const failErrors = data.results
          .filter((r: { success: boolean }) => !r.success)
          .map((r: { error?: unknown }) =>
            typeof r.error === "string" ? r.error : JSON.stringify(r.error),
          )
          .filter(Boolean);
        if (ok > 0) {
          toastResult = {
            msg: `✅ Đã gửi ${ok} lời mời${fail > 0 ? ` · ${fail} thất bại` : ""}`,
            type: "success",
          };
        } else {
          toastResult = {
            msg: `❌ ${failErrors[0] || "Lỗi gửi lời mời"}`,
            type: "error",
          };
        }
        setInviting(false);
        setInviteEmail("");
        setShowInvite(false);
        setInviteResult(null);
        await fetchAdmin();
        notifyDashboardDataChanged("member-invite");
        onBackgroundRefreshNeeded?.();
      } else {
        const errMsg =
          typeof data.error === "string" ? data.error : JSON.stringify(data.error);
        toastResult = { msg: `❌ ${errMsg || "Lỗi gửi lời mời"}`, type: "error" };
      }
    } catch {
      toastResult = { msg: "❌ Lỗi kết nối", type: "error" };
    }
    setInviting(false);
    setInviteEmail("");
    setShowInvite(false);
    setInviteResult(null);
    if (toastResult) showToast(toastResult.msg, toastResult.type);
  }, [adminId, admin, inviteEmail, fetchAdmin, onBackgroundRefreshNeeded, showToast]);

  const handleRevokeInvite = useCallback(
    async (memberGoogleId: string | undefined, memberEmail: string) => {
      const revokeKey = memberGoogleId || memberEmail;
      setRevokingId(revokeKey);
      try {
        const res = await fetch(`/api/admin/${adminId}/revoke-invite`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({
            memberGoogleId: memberGoogleId || null,
            memberEmail,
          }),
        });
        if (res.ok) {
          showToast("✅ Thu hồi thành công", "success");
          await fetchAdmin();
          notifyDashboardDataChanged("member-invite-revoke");
          onBackgroundRefreshNeeded?.();
        } else {
          const data = await res.json();
          showToast(`❌ ${data.error || "Lỗi thu hồi"}`, "error");
        }
      } catch {
        showToast("❌ Lỗi kết nối", "error");
      }
      setRevokingId(null);
    },
    [adminId, fetchAdmin, onBackgroundRefreshNeeded, showToast],
  );

  const handleCreateFamily = useCallback(async () => {
    if (!admin) return;
    setCreatingFamily(true);
    try {
      const res = await fetch(`/api/admin/${adminId}/create-family`, { method: "POST" });
      const data = await res.json();
      if (res.ok) {
        showToast("Đã tạo nhóm gia đình!", "success");
        await fetchAdmin();
        notifyDashboardDataChanged("family-create");
        onBackgroundRefreshNeeded?.();
      } else {
        showToast(data.error || "Tạo thất bại", "error");
      }
    } catch {
      showToast("Lỗi kết nối", "error");
    }
    setCreatingFamily(false);
  }, [admin, adminId, fetchAdmin, onBackgroundRefreshNeeded, showToast]);

  const handleSetCredit = useCallback(async () => {
    if (!admin || !setCreditMember) return;
    setSettingCredit(true);
    try {
      const res = await fetch(`/api/admin/${adminId}`, {
        method: "PATCH",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          memberGoogleId: setCreditMember.googleUserId || null,
          memberEmail: setCreditMember.email,
          creditLimit: Math.max(0, parseInt(creditInput) || 0),
        }),
      });
      if (res.ok) {
        await fetchAdmin();
        notifyDashboardDataChanged("member-credit-limit");
        onBackgroundRefreshNeeded?.();
        showToast(`Đã set credit ${creditInput} cho ${setCreditMember.name}`, "success");
      } else {
        const err = await res.json();
        showToast(`${err.error || "Lỗi set credit"}`, "error");
      }
    } catch {
      showToast("Lỗi kết nối", "error");
    }
    setSettingCredit(false);
    setSetCreditMember(null);
  }, [admin, adminId, setCreditMember, creditInput, fetchAdmin, onBackgroundRefreshNeeded, showToast]);

  return {
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
  };
}
