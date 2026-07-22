type GoogleClockOptions = {
  syncIntervalMs?: number;
  timeoutMs?: number;
  sourceUrl?: string;
};

type GoogleClock = {
  now: () => number;
  getOffset: () => number;
  ensureFresh: () => Promise<void>;
  start: () => void;
};

const DEFAULT_SYNC_INTERVAL_MS = 5 * 60 * 1000;
const DEFAULT_TIMEOUT_MS = 3000;
const DEFAULT_SOURCE_URL = "https://www.google.com";

export function createGoogleClock(options?: GoogleClockOptions): GoogleClock {
  const syncIntervalMs = options?.syncIntervalMs ?? DEFAULT_SYNC_INTERVAL_MS;
  const timeoutMs = options?.timeoutMs ?? DEFAULT_TIMEOUT_MS;
  const sourceUrl = options?.sourceUrl ?? DEFAULT_SOURCE_URL;

  let timeOffset = 0;
  let lastSyncAt = 0;
  let syncPromise: Promise<void> | null = null;
  let started = false;

  const sync = async (): Promise<void> => {
    if (syncPromise) return syncPromise;

    syncPromise = (async () => {
      try {
        const ctrl = new AbortController();
        const timer = setTimeout(() => ctrl.abort(), timeoutMs);
        const res = await fetch(sourceUrl, {
          method: "HEAD",
          signal: ctrl.signal,
          cache: "no-store",
        });
        clearTimeout(timer);

        const d = res.headers.get("date");
        if (d) {
          timeOffset = new Date(d).getTime() - Date.now();
          lastSyncAt = Date.now();
        }
      } catch {
      }
    })().finally(() => {
      syncPromise = null;
    });

    return syncPromise;
  };

  const ensureFresh = async () => {
    if (Date.now() - lastSyncAt > syncIntervalMs) {
      await sync();
    }
  };

  const start = () => {
    if (started) return;
    started = true;
    void sync();
    const timer = setInterval(() => {
      void sync();
    }, syncIntervalMs);
    const maybeTimer = timer as unknown as { unref?: () => void };
    if (typeof maybeTimer.unref === "function") {
      maybeTimer.unref();
    }
  };

  return {
    now: () => Date.now() + timeOffset,
    getOffset: () => timeOffset,
    ensureFresh,
    start,
  };
}
