"use client";
import { useState, useCallback, useRef, useEffect } from "react";
import { extractCreditFromSync, isSyncableAccountStatus } from "@/lib/utils";
import { notifyDashboardDataChanged, subscribeDashboardDataChanged } from "@/lib/browser/dashboard-events";
import { useToast } from "./useToast";
import { useAutoSync } from "./useAutoSync";

import type {
  CreditTransaction,
  FamilyMemberCredit,
} from "@/lib/scanner/credit-activity";

export interface CreditActivity {
  adminEmail: string;
  adminName: string;
  remaining: number;
  refreshDate: number | null;
  transactions: CreditTransaction[];
  familyMembers: FamilyMemberCredit[];
  totalTransactions: number;
  error?: string;
}

export interface CreditLogEntry {
  id: string;
  adminEmail: string;
  adminName: string;
  memberName: string;
  creditBefore: number;
  creditAfter: number;
  diff: number;
  createdAt: string;
}

export interface AdminRow {
  id: string;
  email: string;
  fullName: string;
  role: string;
  memberCount: number;
  remainingCredit: number;
  monthlyCredit: number;
  usedStorageMB: number;
  storageTB: number;
  planName: string;
  planExpiresAt: string | null;
  has2FA: boolean;
  familyType: string;
  lastSyncAt: string | null;
  lastSyncStatus: string;
  lastSyncError: string;
  accountStatus: string;
  profileData?: string;
  memberExpiry?: {
    valid: number;
    nearExpiry: number;
    expired: number;
    renewed: number;
    invited: number;
    manager: number;
    noExpiry: number;
    tracked: number;
  };
  memberReports?: Array<{
    key: string;
    email: string;
    name: string;
    role: string;
    status: string;
    endDate: string | null;
    renewed: boolean;
    category: "valid" | "nearExpiry" | "expired" | "invited" | "noExpiry";
    daysLeft: number | null;
  }>;
}

export interface SummaryCard {
  id: string;
  label: string;
  value: string;
  sub: string;
  borderColor: string;
  valueColor: string;
  icon: React.ReactNode;
}

type BuildCardsFn = (s: {
  farmAccounts: number;
  totalMembers: number;
  totalCredit: number;
  remainingCredit: number;
  totalStorage: string;
  usedStorage: string;
}) => SummaryCard[];

type SyncCreditPayload = {
  remaining: number;
  refreshDate: number | null;
  transactions: CreditTransaction[];
  familyMembers: FamilyMemberCredit[];
  totalTransactions: number;
  error?: string;
};

type FetchStatsOptions = {
  silent?: boolean;
};

function asNumber(value: unknown, fallback = 0): number {
  return typeof value === "number" && Number.isFinite(value) ? value : fallback;
}

function asNullableNumber(value: unknown): number | null {
  return typeof value === "number" && Number.isFinite(value) ? value : null;
}

function toSyncCreditPayload(value: unknown): SyncCreditPayload | null {
  if (!value || typeof value !== "object") return null;
  const data = value as Record<string, unknown>;
  return {
    remaining: asNumber(data.remaining),
    refreshDate: asNullableNumber(data.refreshDate),
    transactions: Array.isArray(data.transactions)
      ? (data.transactions as CreditTransaction[])
      : [],
    familyMembers: Array.isArray(data.familyMembers)
      ? (data.familyMembers as FamilyMemberCredit[])
      : [],
    totalTransactions: asNumber(data.totalTransactions),
    error: typeof data.error === "string" ? data.error : undefined,
  };
}

function sleep(ms: number) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

export function useDashboardData(buildCards: BuildCardsFn) {
  const [cards, setCards] = useState<SummaryCard[]>([]);
  const [admins, setAdmins] = useState<AdminRow[]>([]);
  const [syncing, setSyncing] = useState(false);
  const syncAllRef = useRef(false);
  const syncLockRef = useRef(false);
  const [creditActivities, setCreditActivities] = useState<CreditActivity[]>([]);
  const [creditLogs, setCreditLogs] = useState<CreditLogEntry[]>([]);
  const activitiesRef = useRef<CreditActivity[]>([]);
  const [initialLoading, setInitialLoading] = useState(true);
  const fetchingStatsRef = useRef(false);

  const { toast: globalToast, show: showGlobalToast, showPersistent } = useToast();

  const fetchStats = useCallback(async (options: FetchStatsOptions = {}) => {
    if (options.silent && fetchingStatsRef.current) return;
    fetchingStatsRef.current = true;
    try {
      const response = await fetch("/api/dashboard/stats");
      if (!response.ok) {
        throw new Error("Không tải được dữ liệu dashboard");
      }
      const data = await response.json();
      const s = data.summary;
      setCards(buildCards(s));
      setAdmins(
        (data.admins ?? []).sort((a: AdminRow, b: AdminRow) =>
          a.fullName.localeCompare(b.fullName),
        ),
      );
      if (data._c && Array.isArray(data._c) && data._c.length > 0) {
        const normalized: CreditActivity[] = data._c
          .map((item: unknown): CreditActivity | null => {
            if (!item || typeof item !== "object") return null;
            const obj = item as Record<string, unknown>;
            const adminEmail =
              typeof obj.adminEmail === "string" ? obj.adminEmail : "";
            const adminName = typeof obj.adminName === "string" ? obj.adminName : "";
            const payload = toSyncCreditPayload(obj);
            if (!adminEmail || !adminName || !payload) return null;
            return {
              adminEmail,
              adminName,
              remaining: payload.remaining,
              refreshDate: payload.refreshDate,
              transactions: payload.transactions,
              familyMembers: payload.familyMembers,
              totalTransactions: payload.totalTransactions,
              error: payload.error,
            } satisfies CreditActivity;
          })
          .filter((item: CreditActivity | null): item is CreditActivity => item !== null);

        setCreditActivities(normalized);
        activitiesRef.current = normalized;
      }
      if (data.creditLogs) setCreditLogs(data.creditLogs);
    } catch (error) {
      const message = error instanceof Error ? error.message : "Không tải được dữ liệu dashboard";
      if (!options.silent) showGlobalToast(message, "error");
    } finally {
      fetchingStatsRef.current = false;
      setInitialLoading(false);
    }
  }, [buildCards, showGlobalToast]);

  const updateCreditFromSync = useCallback(
    async (adminEmail: string, adminName: string, creditData: unknown) => {
      const parsedCredit = toSyncCreditPayload(creditData);
      if (!parsedCredit) return;
      const newActivity: CreditActivity = {
        adminEmail,
        adminName,
        remaining: parsedCredit.remaining,
        refreshDate: parsedCredit.refreshDate,
        transactions: parsedCredit.transactions,
        familyMembers: parsedCredit.familyMembers,
        totalTransactions: parsedCredit.totalTransactions,
        error: parsedCredit.error,
      };
      const filtered = activitiesRef.current.filter((a) => a.adminEmail !== adminEmail);
      const updated = [...filtered, newActivity];
      activitiesRef.current = updated;
      setCreditActivities(updated);
      await fetch("/api/dashboard/stats", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ _c: updated }),
      }).catch(() => {
        showGlobalToast("Không lưu được hoạt động credit", "error");
      });
    },
    [showGlobalToast],
  );

  const syncBatch = useCallback(
    async (targets: AdminRow[], toastPrefix: string) => {
      const syncableTargets = targets.filter((admin) =>
        isSyncableAccountStatus(admin.accountStatus),
      );
      if (syncableTargets.length === 0 || syncLockRef.current) return;
      syncLockRef.current = true;
      setSyncing(true);
      syncAllRef.current = syncableTargets.length > 1;

      const BATCH_SIZE = 4;
      const BATCH_COOLDOWN_MS = 2000;

      showPersistent(`${toastPrefix}...`, "info");

      try {
        const postSyncWithRetry = async (adminId: string, maxRetry = 1) => {
          let attempt = 0;
          while (true) {
            const res = await fetch(`/api/admin/${adminId}/sync`, { method: "POST" });
            if (res.status !== 429) return res;
            if (attempt >= maxRetry) return res;
            const retryAfter = Number(res.headers.get("Retry-After") || "1");
            const waitMs = Math.max(1000, Math.min(30000, retryAfter * 1000));
            await sleep(waitMs);
            attempt++;
          }
        };

        let successCount = 0;
        let failureCount = 0;

        for (let i = 0; i < syncableTargets.length; i += BATCH_SIZE) {
          const batch = syncableTargets.slice(i, i + BATCH_SIZE);
          await Promise.all(
            batch.map(async (admin) => {
              try {
                const res = await postSyncWithRetry(admin.id, 1);
                if (res.status === 429 || !res.ok) {
                  failureCount++;
                  return;
                }
                successCount++;
                const creditData = await extractCreditFromSync(res);
                if (creditData) {
                  await updateCreditFromSync(admin.email, admin.fullName, creditData);
                }
              } catch (error) {
                failureCount++;
                const message = error instanceof Error ? error.message : "Lỗi đồng bộ";
                showGlobalToast(message, "error");
              }
            }),
          );

          const hasMore = i + BATCH_SIZE < syncableTargets.length;
          if (hasMore) {
            await sleep(BATCH_COOLDOWN_MS);
          }
        }

        await fetchStats();
        notifyDashboardDataChanged("sync");

        if (failureCount > 0) {
          const msg = `${toastPrefix} hoàn tất ${successCount}/${syncableTargets.length}, lỗi ${failureCount}`;
          showGlobalToast(msg, successCount > 0 ? "info" : "error");
        } else {
          showGlobalToast(`${toastPrefix} hoàn tất`, "success");
        }
      } finally {
        syncLockRef.current = false;
        setSyncing(false);
        syncAllRef.current = false;
      }
    },
    [fetchStats, updateCreditFromSync, showGlobalToast, showPersistent],
  );

  const handleSyncAdmin = useCallback(
    async (adminId: string) => {
      const admin = admins.find((a) => a.id === adminId);
      if (!admin) return;
      if (!isSyncableAccountStatus(admin.accountStatus)) return;
      await syncBatch([admin], "Đang đồng bộ");
    },
    [admins, syncBatch],
  );

  const handleSyncAll = useCallback(
    async (familyType?: string) => {
      const targets = familyType
        ? admins.filter((a) => (a.familyType || "ultra") === familyType)
        : admins;
      await syncBatch(targets, "Đang đồng bộ tất cả");
    },
    [admins, syncBatch],
  );

  const handleSyncSelected = useCallback(
    async (adminIds: string[]) => {
      const idSet = new Set(adminIds);
      const targets = admins.filter((admin) => idSet.has(admin.id));
      await syncBatch(targets, "Đang đồng bộ đã chọn");
    },
    [admins, syncBatch],
  );

  const autoSyncCallback = useCallback(async () => {
    const syncableAdmins = admins.filter((admin) =>
      isSyncableAccountStatus(admin.accountStatus),
    );
    await syncBatch(syncableAdmins, "Tự động đồng bộ");
  }, [admins, syncBatch]);

  const hasSyncableAdmin = admins.some((admin) =>
    isSyncableAccountStatus(admin.accountStatus),
  );
  const { nextAutoSync } = useAutoSync(autoSyncCallback, !syncing && hasSyncableAdmin);

  useEffect(() => {
    void fetchStats();
  }, [fetchStats]);

  useEffect(
    () => subscribeDashboardDataChanged(() => void fetchStats({ silent: true })),
    [fetchStats],
  );

  return {
    cards,
    admins,
    syncing,
    syncAllRef,
    creditActivities,
    creditLogs,
    initialLoading,
    globalToast,
    fetchStats,
    handleSyncAdmin,
    handleSyncAll,
    handleSyncSelected,
    nextAutoSync,
  };
}
