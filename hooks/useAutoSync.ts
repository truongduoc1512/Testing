"use client";
import { useState, useEffect, useCallback, useRef } from "react";

export function useAutoSync(onSync: () => Promise<void>, enabled: boolean) {
  const [nextAutoSync, setNextAutoSync] = useState<Date | null>(null);
  const runningRef = useRef(false);

  const runSync = useCallback(async () => {
    if (runningRef.current || !enabled) return;
    runningRef.current = true;
    try {
      await onSync();
    } finally {
      runningRef.current = false;
    }
  }, [onSync, enabled]);

  useEffect(() => {
    let intervalId: ReturnType<typeof setInterval> | null = null;
    let cancelled = false;

    function msUntilNextMark(): number {
      const now = new Date();
      const min = now.getMinutes();
      const target = new Date(now);
      if (min < 30) {
        target.setMinutes(30, 0, 0);
      } else {
        target.setHours(target.getHours() + 1, 0, 0, 0);
      }
      return target.getTime() - now.getTime();
    }

    function updateNext() {
      setNextAutoSync(new Date(Date.now() + msUntilNextMark()));
    }

    updateNext();
    const firstMs = msUntilNextMark();

    const timeoutId = setTimeout(() => {
      if (cancelled) return;
      runSync();
      updateNext();
      intervalId = setInterval(
        () => {
          if (cancelled) return;
          runSync();
          updateNext();
        },
        30 * 60 * 1000,
      );
    }, firstMs);

    return () => {
      cancelled = true;
      clearTimeout(timeoutId);
      if (intervalId) clearInterval(intervalId);
    };
  }, [runSync]);

  return { nextAutoSync };
}
