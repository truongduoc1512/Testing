"use client";
import { useCallback, useEffect, useMemo, useState } from "react";
import type { AdminRow } from "./useDashboardData";
import {
  CHECK_PROFILE_SELECTED_ADMIN_ID_KEY,
  readPersistedValue,
  removePersistedValue,
  writePersistedValue,
} from "@/lib/browser/persist";

const DEFAULT_STORAGE_KEY = CHECK_PROFILE_SELECTED_ADMIN_ID_KEY;

interface UseCheckProfileSelectionResult {
  googleAdmins: AdminRow[];
  selectedId: string | null;
  selectedAdmin: AdminRow | undefined;
  selectAdmin: (id: string | null) => void;
}

export function useCheckProfileSelection(
  admins: AdminRow[],
  storageKey = DEFAULT_STORAGE_KEY,
): UseCheckProfileSelectionResult {
  const googleAdmins = useMemo(() => admins, [admins]);
  const [selectedId, setSelectedId] = useState<string | null>(null);

  const selectAdmin = useCallback(
    (id: string | null) => {
      setSelectedId(id);
      try {
        if (id) {
          writePersistedValue(storageKey, id);
        } else {
          removePersistedValue(storageKey);
        }
      } catch (error) {
        void error;
      }
    },
    [storageKey],
  );

  useEffect(() => {
    if (googleAdmins.length === 0) {
      if (selectedId !== null) selectAdmin(null);
      return;
    }

    if (selectedId && googleAdmins.some((a) => a.id === selectedId)) return;

    let restoredId: string | null = null;
    try {
      restoredId = readPersistedValue(storageKey);
    } catch (error) {
      void error;
    }

    if (restoredId && googleAdmins.some((a) => a.id === restoredId)) {
      setSelectedId(restoredId);
      return;
    }

    const firstCheckedId = googleAdmins.find((a) => Boolean(a.profileData))?.id ?? null;
    if (firstCheckedId) {
      selectAdmin(firstCheckedId);
    } else {
      selectAdmin(googleAdmins[0]?.id ?? null);
    }
  }, [googleAdmins, selectedId, selectAdmin, storageKey]);

  const selectedAdmin = useMemo(
    () => googleAdmins.find((a) => a.id === selectedId),
    [googleAdmins, selectedId],
  );

  return {
    googleAdmins,
    selectedId,
    selectedAdmin,
    selectAdmin,
  };
}
