"use client";

import { useCallback, useEffect, useRef, useState } from "react";
import { useAsyncAction } from "./useAsyncAction";
import { notifyDashboardDataChanged, subscribeDashboardDataChanged } from "@/lib/browser/dashboard-events";

export type AuditLogItem = {
  id: string;
  actorEmail: string;
  action: string;
  targetType: string;
  targetId: string;
  status: "success" | "failure" | "partial" | "info" | string;
  message: string;
  metadata?: Record<string, unknown>;
  createdAt: string;
};

interface AuditLogResponse {
  logs: AuditLogItem[];
  nextCursor?: string;
}

async function fetchAuditLogs(take: number, cursor?: string): Promise<AuditLogResponse> {
  const params = new URLSearchParams({ take: String(take) });
  if (cursor) params.set("cursor", cursor);
  const response = await fetch(`/api/audit-logs?${params}`, { cache: "no-store" });
  const data = await response.json().catch(() => ({}));
  if (!response.ok) {
    throw new Error(data.error || "Không tải được nhật ký hoạt động");
  }
  return {
    logs: Array.isArray(data.logs) ? (data.logs as AuditLogItem[]) : [],
    nextCursor: data.nextCursor || undefined,
  };
}

async function deleteAuditLogs(): Promise<{ deletedCount: number }> {
  const response = await fetch("/api/audit-logs", { method: "DELETE" });
  const data = await response.json().catch(() => ({}));
  if (!response.ok) {
    throw new Error(data.error || "Không thể xóa nhật ký hoạt động");
  }
  return {
    deletedCount: Number.isFinite(Number(data.deletedCount)) ? Number(data.deletedCount) : 0,
  };
}

export function useAuditLogs(pageSize = 10) {
  const [logs, setLogs] = useState<AuditLogItem[]>([]);
  const [page, setPage] = useState(1);
  const [hasNext, setHasNext] = useState(false);
  const cursorHistoryRef = useRef<(string | undefined)[]>([undefined]);
  const nextCursorRef = useRef<string | undefined>(undefined);
  const refreshInFlightRef = useRef(false);

  const applyResult = useCallback((result: AuditLogResponse) => {
    setLogs(result.logs);
    setHasNext(!!result.nextCursor);
    nextCursorRef.current = result.nextCursor;
  }, []);

  const fetchPage = useCallback(async (cursor?: string) => {
    const result = await fetchAuditLogs(pageSize, cursor);
    applyResult(result);
    return result;
  }, [pageSize, applyResult]);

  const { loading, error, run } = useAsyncAction(fetchPage);
  const {
    loading: clearing,
    error: clearError,
    run: runDelete,
  } = useAsyncAction(deleteAuditLogs);

  const resetPagination = useCallback(() => {
    cursorHistoryRef.current = [undefined];
    nextCursorRef.current = undefined;
    setPage(1);
  }, []);

  const refresh = useCallback(async (options?: { silent?: boolean }) => {
    resetPagination();

    if (options?.silent) {
      if (refreshInFlightRef.current) return [];
      refreshInFlightRef.current = true;
      try {
        const result = await fetchAuditLogs(pageSize);
        applyResult(result);
        return result.logs;
      } catch {
        return [];
      } finally {
        refreshInFlightRef.current = false;
      }
    }

    try {
      const result = await run(undefined);
      applyResult(result);
      return result.logs;
    } catch {
      return [];
    }
  }, [run, pageSize, applyResult, resetPagination]);

  useEffect(() => {
    void refresh();
  }, [refresh]);

  useEffect(
    () => subscribeDashboardDataChanged(() => void refresh({ silent: true })),
    [refresh],
  );

  const goNext = useCallback(async () => {
    if (!hasNext || !nextCursorRef.current) return;
    const cursor = nextCursorRef.current;
    try {
      const result = await run(cursor);
      applyResult(result);
      setPage((p) => {
        const newPage = p + 1;
        cursorHistoryRef.current[newPage - 1] = cursor;
        return newPage;
      });
    } catch {}
  }, [hasNext, run, applyResult]);

  const goPrev = useCallback(async () => {
    if (page <= 1) return;
    const prevPage = page - 1;
    const cursor = cursorHistoryRef.current[prevPage - 1];
    try {
      const result = await run(cursor);
      applyResult(result);
      setPage(prevPage);
    } catch {}
  }, [page, run, applyResult]);

  const clear = useCallback(async () => {
    const result = await runDelete();
    setLogs([]);
    setHasNext(false);
    resetPagination();
    notifyDashboardDataChanged("audit-clear");
    return result;
  }, [runDelete, resetPagination]);

  return {
    logs,
    loading,
    error,
    refresh,
    clear,
    clearing,
    clearError,
    page,
    hasNext,
    hasPrev: page > 1,
    goNext,
    goPrev,
  };
}
