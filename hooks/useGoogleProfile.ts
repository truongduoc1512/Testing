"use client";
import { useState, useCallback, useEffect, useMemo, useRef } from "react";
import {
  GOOGLE_MEMBER_STORAGE_CACHE_KEY,
  GOOGLE_PROFILES_CACHE_KEY,
  GOOGLE_SHARING_STATUS_CACHE_KEY,
  readPersistedValue,
  writePersistedValue,
} from "@/lib/browser/persist";
import { notifyDashboardDataChanged } from "@/lib/browser/dashboard-events";

export interface ProfileCache {
  loading: boolean;
  data?: GoogleProfileData;
  error?: string;
}

interface UseGoogleProfileOptions {
  adminId: string | null;
  initialData?: string | null;
  enableSharing?: boolean;
}

export interface LiveMemberStorageUsage {
  name: string;
  email: string;
  googleUserId?: string;
  usedText: string | null;
  usedBytes: number | null;
}

export interface LiveFamilyStorageUsage {
  totalUsedText: string | null;
  totalUsedBytes: number | null;
  members: LiveMemberStorageUsage[];
  error?: string;
}

export interface ProfileSubscription {
  planFullName?: string;
  planName?: string;
  expiresAt?: string | null;
}

export interface PaymentProfile {
  exists?: boolean;
  profileName?: string;
  profileId?: string;
  profileIdFormatted?: string;
  country?: string;
  email?: string;
}

export interface GoogleProfileData {
  success?: boolean;
  email?: string;
  language?: string;
  checkedAt?: string;
  subscription?: ProfileSubscription;
  paymentProfile?: PaymentProfile;
  memberStorage?: LiveFamilyStorageUsage;
  sharingStatus?: boolean | null;
  sharingError?: string | null;
  errors?: string[];
  error?: string;
}

interface ToggleSharingResponse {
  success?: boolean;
  sharing?: boolean | null;
  error?: string;
}

const STORAGE_KEY = GOOGLE_PROFILES_CACHE_KEY;
const MEMBER_STORAGE_KEY = GOOGLE_MEMBER_STORAGE_CACHE_KEY;
const SHARING_STATUS_KEY = GOOGLE_SHARING_STATUS_CACHE_KEY;

export function useGoogleProfile({
  adminId,
  initialData,
  enableSharing = true,
}: UseGoogleProfileOptions) {
  const [profilesCache, setProfilesCache] = useState<Record<string, ProfileCache>>({});
  const [isClosingPayment, setIsClosingPayment] = useState(false);
  const [confirmCloseId, setConfirmCloseId] = useState<string | null>(null);
  const sharingHydrationRef = useRef<Record<string, boolean>>({});
  const [sharingStatusCache, setSharingStatusCache] = useState<
    Record<string, boolean | null>
  >({});
  const [isTogglingSharing, setIsTogglingSharing] = useState(false);
  const [memberStorageCache, setMemberStorageCache] = useState<
    Record<string, LiveFamilyStorageUsage>
  >({});
  const [isFetchingMemberStorage, setIsFetchingMemberStorage] = useState(false);

  const initialSnapshot = useMemo(() => {
    if (!initialData) return null;
    try {
      return JSON.parse(initialData) as GoogleProfileData;
    } catch (error) {
      void error;
      return null;
    }
  }, [initialData]);

  useEffect(() => {
    try {
      const saved = readPersistedValue(STORAGE_KEY);
      if (saved) {
        setProfilesCache(JSON.parse(saved));
      }
      const savedMemberStorage = readPersistedValue(MEMBER_STORAGE_KEY);
      if (savedMemberStorage) {
        setMemberStorageCache(JSON.parse(savedMemberStorage));
      }
      const savedSharingStatus = readPersistedValue(SHARING_STATUS_KEY);
      if (savedSharingStatus) {
        setSharingStatusCache(JSON.parse(savedSharingStatus));
      }
    } catch (error) {
      void error;
    }
  }, []);

  const updateCache = useCallback(
    (updater: (prev: Record<string, ProfileCache>) => Record<string, ProfileCache>) => {
      setProfilesCache((prev) => {
        const next = updater(prev);
        try {
          writePersistedValue(STORAGE_KEY, JSON.stringify(next));
        } catch (error) {
          void error;
        }
        return next;
      });
    },
    [],
  );

  const updateMemberStorageCache = useCallback(
    (
      updater: (
        prev: Record<string, LiveFamilyStorageUsage>,
      ) => Record<string, LiveFamilyStorageUsage>,
    ) => {
      setMemberStorageCache((prev) => {
        const next = updater(prev);
        try {
          writePersistedValue(MEMBER_STORAGE_KEY, JSON.stringify(next));
        } catch (error) {
          void error;
        }
        return next;
      });
    },
    [],
  );

  const updateSharingStatusCache = useCallback(
    (
      updater: (prev: Record<string, boolean | null>) => Record<string, boolean | null>,
    ) => {
      setSharingStatusCache((prev) => {
        const next = updater(prev);
        try {
          writePersistedValue(SHARING_STATUS_KEY, JSON.stringify(next));
        } catch (error) {
          void error;
        }
        return next;
      });
    },
    [],
  );

  const currentProfile = profilesCache[adminId || ""];
  const cachedSharingStatus = adminId ? sharingStatusCache[adminId] : undefined;
  const snapshotSharingStatus =
    enableSharing && typeof initialSnapshot?.sharingStatus === "boolean"
      ? initialSnapshot.sharingStatus
      : null;
  const memberStorage = adminId
    ? memberStorageCache[adminId] || initialSnapshot?.memberStorage || null
    : null;
  const sharingStatus =
    enableSharing && adminId
      ? typeof cachedSharingStatus === "boolean"
        ? cachedSharingStatus
        : snapshotSharingStatus
      : null;
  const isFetching = currentProfile?.loading || false;

  const profileData = useMemo<GoogleProfileData | null>(() => {
    if (currentProfile?.data) return currentProfile.data;
    if (currentProfile?.error) return { error: currentProfile.error };
    return initialSnapshot;
  }, [currentProfile?.data, currentProfile?.error, initialSnapshot]);

  const clearMemberStorageForAdmin = useCallback(
    (id: string) => {
      updateMemberStorageCache((prev) => {
        if (!(id in prev)) return prev;
        const next = { ...prev };
        delete next[id];
        return next;
      });
    },
    [updateMemberStorageCache],
  );

  const setProfileLoading = useCallback(
    (id: string) => {
      updateCache((prev) => ({
        ...prev,
        [id]: {
          ...(prev[id] || {}),
          loading: true,
        },
      }));
    },
    [updateCache],
  );

  const hydrateSharingStatus = useCallback(async () => {
    if (!adminId || !enableSharing) return;
    try {
      const res = await fetch(`/api/admin/${adminId}/toggle-sharing`);
      if (!res.ok) return;
      const data = (await res.json()) as ToggleSharingResponse;
      updateSharingStatusCache((prev) => ({
        ...prev,
        [adminId]: typeof data?.sharing === "boolean" ? data.sharing : null,
      }));
    } catch (error) {
      void error;
    }
  }, [adminId, enableSharing, updateSharingStatusCache]);

  useEffect(() => {
    if (!adminId || !profileData || !enableSharing) return;
    if (typeof sharingStatus === "boolean") {
      sharingHydrationRef.current[adminId] = true;
      return;
    }
    if (sharingHydrationRef.current[adminId]) return;
    sharingHydrationRef.current[adminId] = true;
    void hydrateSharingStatus();
  }, [adminId, enableSharing, profileData, sharingStatus, hydrateSharingStatus]);

  const fetchProfile = useCallback(async () => {
    if (!adminId) return;
    setProfileLoading(adminId);
    setIsFetchingMemberStorage(true);
    try {
      const res = await fetch(`/api/admin/${adminId}/profile`);
      const data = (await res.json()) as GoogleProfileData;
      if (!res.ok) {
        throw new Error(data?.error || `HTTP ${res.status}`);
      }

      updateCache((prev) => ({ ...prev, [adminId]: { loading: false, data } }));
      if (enableSharing) {
        updateSharingStatusCache((prev) => ({
          ...prev,
          [adminId]: typeof data?.sharingStatus === "boolean" ? data.sharingStatus : null,
        }));
      }
      const usageData = data?.memberStorage;
      if (usageData && typeof usageData === "object") {
        updateMemberStorageCache((prev) => ({
          ...prev,
          [adminId]: {
            totalUsedText: usageData.totalUsedText ?? null,
            totalUsedBytes: usageData.totalUsedBytes ?? null,
            members: Array.isArray(usageData.members) ? usageData.members : [],
            error: usageData.error || undefined,
          },
        }));
      } else {
        clearMemberStorageForAdmin(adminId);
      }
    } catch (error: unknown) {
      void error;
      updateCache((prev) => ({
        ...prev,
        [adminId]: { loading: false, error: "Không tải được dữ liệu hồ sơ." },
      }));
      if (enableSharing) {
        updateSharingStatusCache((prev) => ({
          ...prev,
          [adminId]: null,
        }));
      }
      clearMemberStorageForAdmin(adminId);
    } finally {
      setIsFetchingMemberStorage(false);
    }
  }, [
    adminId,
    clearMemberStorageForAdmin,
    enableSharing,
    setProfileLoading,
    updateCache,
    updateSharingStatusCache,
  ]);

  const closePayment = useCallback(async (): Promise<{
    success: boolean;
    message?: string;
  }> => {
    if (!adminId) return { success: false, message: "Chưa chọn tài khoản admin." };

    setIsClosingPayment(true);
    try {
      const res = await fetch(`/api/admin/${adminId}/profile/close-payment`, {
        method: "POST",
      });
      const data = (await res.json()) as { success?: boolean; message?: string; error?: string };
      if (res.ok && data.success) {
        await fetchProfile();
        notifyDashboardDataChanged("profile-payment-close");
        return { success: true };
      }
      return {
        success: false,
        message: data.message || data.error,
      };
    } catch (error: unknown) {
      void error;
      return { success: false, message: "Yêu cầu thất bại." };
    } finally {
      setIsClosingPayment(false);
      setConfirmCloseId(null);
    }
  }, [adminId, fetchProfile]);

  const toggleSharing = useCallback(
    async (
      enable: boolean,
    ): Promise<{
      success: boolean;
      error?: string;
    }> => {
      if (!adminId) return { success: false, error: "Chưa chọn tài khoản admin." };
      if (!enableSharing)
        return { success: false, error: "Tài khoản này không hỗ trợ chia sẻ." };
      setIsTogglingSharing(true);
      try {
        const res = await fetch(`/api/admin/${adminId}/toggle-sharing`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ enable }),
        });
        const data = (await res.json()) as ToggleSharingResponse;
        if (res.ok && data.success) {
          updateSharingStatusCache((prev) => ({
            ...prev,
            [adminId]: enable,
          }));
          notifyDashboardDataChanged("profile-sharing-toggle");
          return { success: true };
        }
        return { success: false, error: data.error };
      } catch (error: unknown) {
        void error;
        return { success: false, error: "Yêu cầu thất bại." };
      } finally {
        setIsTogglingSharing(false);
      }
    },
    [adminId, enableSharing, updateSharingStatusCache],
  );

  useEffect(() => {
    setIsFetchingMemberStorage(false);
  }, [adminId]);

  const isAdminFetched = useCallback(
    (id: string, adminProfileData?: string | null) => {
      if (profilesCache[id]?.data || profilesCache[id]?.error) return true;
      if (adminProfileData) return true;
      return false;
    },
    [profilesCache],
  );

  const isAdminFetching = useCallback(
    (id: string) => {
      return !!profilesCache[id]?.loading;
    },
    [profilesCache],
  );

  return {
    profileData,
    isFetching,
    profilesCache,
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
  };
}
