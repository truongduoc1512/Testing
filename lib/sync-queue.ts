import { apiError, apiWarn } from "@/lib/logger";

type SyncJob = {
  adminId: string;
  familyType: string;
  retries: number;
  startedAt?: number;
  resolve: (value: { status: number; body: unknown }) => void;
  reject: (err: Error) => void;
};

const MAX_RETRIES = 2;
const SYNC_TIMEOUT_MS = 5 * 60 * 1000;
const RETRY_BACKOFF_MS = [800, 1800];
const CONCURRENCY = 3;
const queue: SyncJob[] = [];
const running = new Map<string, SyncJob>();
const queued = new Set<string>();
let active = 0;

const TRANSIENT_ERROR_PATTERNS = [
  "TransientTransactionError",
  "forcibly closed by the remote host",
  "os error 10054",
  "ECONNRESET",
  "timed out",
  "Raw query failed",
];

function isTransientSyncError(error: unknown): boolean {
  const message = error instanceof Error ? error.message : String(error);
  return TRANSIENT_ERROR_PATTERNS.some((pattern) => message.includes(pattern));
}

function processQueue() {
  while (active < CONCURRENCY && queue.length > 0) {
    const job = queue.shift()!;
    queued.delete(job.adminId);
    job.startedAt = Date.now();
    running.set(job.adminId, job);
    active++;
    executeSync(job).finally(() => {
      active--;
      running.delete(job.adminId);
      processQueue();
    });
  }
}

async function executeSync(job: SyncJob) {
  try {
    const { executeSyncForAdmin } = await import("@/app/api/admin/[id]/sync/executor");
    job.resolve(await executeSyncForAdmin(job.adminId, job.familyType));
  } catch (error) {
    const canRetry = job.retries < MAX_RETRIES && isTransientSyncError(error);
    if (canRetry) {
      job.retries++;
      const backoffMs = RETRY_BACKOFF_MS[job.retries - 1];
      apiWarn("sync-queue", `Retry ${job.retries}/${MAX_RETRIES} in ${backoffMs}ms`, {
        adminId: job.adminId,
      });
      queued.add(job.adminId);
      setTimeout(() => {
        queue.push(job);
        processQueue();
      }, backoffMs);
      return;
    }
    const syncError = error instanceof Error ? error : new Error(String(error));
    apiError("sync-queue", syncError, { adminId: job.adminId, retries: job.retries });
    job.reject(syncError);
  }
}

export function enqueueSyncJob(adminId: string, familyType: string) {
  if (running.has(adminId) || queued.has(adminId)) {
    return Promise.resolve({
      status: 429,
      body: { error: "Đang đồng bộ, vui lòng chờ..." },
    });
  }

  return new Promise<{ status: number; body: unknown }>((resolve, reject) => {
    queue.push({ adminId, familyType, retries: 0, resolve, reject });
    queued.add(adminId);
    processQueue();
  });
}

export function getQueueStats() {
  return { google: { active, queued: queue.length, max: CONCURRENCY }, total: running.size + queued.size };
}

export function getAdminSyncStatus(adminId: string) {
  const runningJob = running.get(adminId);
  if (runningJob) {
    return {
      state: "running" as const,
      familyType: runningJob.familyType,
      retries: runningJob.retries,
      startedAt: runningJob.startedAt ?? null,
    };
  }
  const position = queue.findIndex((job) => job.adminId === adminId);
  if (position >= 0) return { state: "queued" as const, familyType: "google" as const, position: position + 1 };
  return { state: "idle" as const };
}

setInterval(() => {
  const now = Date.now();
  for (const [adminId, job] of running) {
    if (job.startedAt && now - job.startedAt > SYNC_TIMEOUT_MS) {
      apiError("sync-queue", new Error("Sync job timed out"), { adminId });
      running.delete(adminId);
      active = Math.max(0, active - 1);
      job.reject(new Error("Sync timed out"));
    }
  }
}, 60 * 1000);
