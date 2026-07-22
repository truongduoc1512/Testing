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
const GOOGLE_TIMEOUT_MS = 5 * 60 * 1000;
const GPT_TIMEOUT_MS = 3 * 60 * 1000;
const RETRY_BACKOFF_MS = [800, 1800];
const TRANSIENT_ERROR_PATTERNS = [
  "TransientTransactionError",
  "forcibly closed by the remote host",
  "os error 10054",
  "ECONNRESET",
  "timed out",
  "Raw query failed",
];

const googleQueue: SyncJob[] = [];
const gptQueue: SyncJob[] = [];
const running = new Map<string, SyncJob>();
const queued = new Set<string>();

let googleActive = 0;
const GOOGLE_CONCURRENCY = 3;
let gptActive = 0;
const GPT_CONCURRENCY = 2;

function isTransientSyncError(error: unknown): boolean {
  const message = error instanceof Error ? error.message : String(error);
  return TRANSIENT_ERROR_PATTERNS.some((pattern) => message.includes(pattern));
}

function processGoogleQueue() {
  while (googleActive < GOOGLE_CONCURRENCY && googleQueue.length > 0) {
    const job = googleQueue.shift()!;
    queued.delete(job.adminId);
    job.startedAt = Date.now();
    running.set(job.adminId, job);
    googleActive++;
    executeSync(job).finally(() => {
      googleActive--;
      running.delete(job.adminId);
      processGoogleQueue();
    });
  }
}

function processGptQueue() {
  while (gptActive < GPT_CONCURRENCY && gptQueue.length > 0) {
    const job = gptQueue.shift()!;
    queued.delete(job.adminId);
    job.startedAt = Date.now();
    running.set(job.adminId, job);
    gptActive++;
    executeSync(job).finally(() => {
      gptActive--;
      running.delete(job.adminId);
      processGptQueue();
    });
  }
}

async function executeSync(job: SyncJob) {
  try {
    const { executeSyncForAdmin } = await import("@/app/api/admin/[id]/sync/executor");
    const result = await executeSyncForAdmin(job.adminId, job.familyType);
    job.resolve(result);
  } catch (e) {
    const error = e as Error;
    const canRetry = job.retries < MAX_RETRIES && isTransientSyncError(error);
    if (canRetry) {
      job.retries++;
      const backoffMs =
        RETRY_BACKOFF_MS[Math.min(job.retries - 1, RETRY_BACKOFF_MS.length - 1)];
      apiWarn(
        "sync-queue",
        `Retry ${job.retries}/${MAX_RETRIES} in ${backoffMs}ms: ${error.message}`,
        { adminId: job.adminId },
      );
      queued.add(job.adminId);
      setTimeout(() => {
        if (job.familyType === "gpt") {
          gptQueue.push(job);
          processGptQueue();
        } else {
          googleQueue.push(job);
          processGoogleQueue();
        }
      }, backoffMs);
    } else {
      apiError("sync-queue", error, { adminId: job.adminId, retries: MAX_RETRIES });
      job.reject(error);
    }
  }
}

export function enqueueSyncJob(
  adminId: string,
  familyType: string,
): Promise<{ status: number; body: unknown }> {
  if (running.has(adminId) || queued.has(adminId)) {
    return Promise.resolve({
      status: 429,
      body: { error: "Đang đồng bộ, vui lòng chờ..." },
    });
  }

  return new Promise((resolve, reject) => {
    const job: SyncJob = { adminId, familyType, retries: 0, resolve, reject };
    queued.add(adminId);

    if (familyType === "gpt") {
      gptQueue.push(job);
      processGptQueue();
    } else {
      googleQueue.push(job);
      processGoogleQueue();
    }
  });
}

export function getQueueStats() {
  return {
    google: { active: googleActive, queued: googleQueue.length, max: GOOGLE_CONCURRENCY },
    gpt: { active: gptActive, queued: gptQueue.length, max: GPT_CONCURRENCY },
    total: running.size + queued.size,
  };
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

  const gptQueuedIndex = gptQueue.findIndex((job) => job.adminId === adminId);
  if (gptQueuedIndex >= 0) {
    return {
      state: "queued" as const,
      familyType: "gpt" as const,
      position: gptQueuedIndex + 1,
    };
  }

  const googleQueuedIndex = googleQueue.findIndex((job) => job.adminId === adminId);
  if (googleQueuedIndex >= 0) {
    return {
      state: "queued" as const,
      familyType: "google" as const,
      position: googleQueuedIndex + 1,
    };
  }

  return {
    state: "idle" as const,
  };
}

setInterval(() => {
  const now = Date.now();
  for (const [adminId, job] of running) {
    const timeout = job.familyType === "gpt" ? GPT_TIMEOUT_MS : GOOGLE_TIMEOUT_MS;
    if (job.startedAt && now - job.startedAt > timeout) {
      apiError(
        "sync-queue",
        new Error(`Job timed out after ${Math.round(timeout / 1000)}s`),
        { adminId },
      );
      running.delete(adminId);
      if (job.familyType === "gpt") gptActive = Math.max(0, gptActive - 1);
      else googleActive = Math.max(0, googleActive - 1);
      job.reject(new Error("Sync timed out"));
    }
  }
}, 60 * 1000);
