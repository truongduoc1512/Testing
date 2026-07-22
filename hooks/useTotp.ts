"use client";
import { useCallback, useEffect, useRef, useState } from "react";

type TotpStatusResponse = {
  totpEnabled?: boolean;
  code?: string | null;
  nextCode?: string | null;
  remaining?: number | null;
  period?: number;
  serverTime?: number;
};

export function useTotp(adminId: string, enabled: boolean) {
  const [code, setCode] = useState("");
  const [countdown, setCountdown] = useState(30);
  const [period, setPeriod] = useState(30);
  const rafRef = useRef<number>(0);
  const lastSecRef = useRef(-1);
  const barRef = useRef<HTMLDivElement | null>(null);
  const lastPctRef = useRef(100);
  const offsetRef = useRef(0);
  const nextCodeRef = useRef("");

  const progressRef = useCallback((el: HTMLDivElement | null) => {
    barRef.current = el;
    if (el) {
      el.style.transform = `scaleX(${lastPctRef.current / 100})`;
    }
  }, []);

  const now = useCallback(() => Date.now() + offsetRef.current, []);

  const syncServerClock = useCallback(async () => {
    try {
      const before = Date.now();
      const res = await fetch("/api/time");
      const rtt = Date.now() - before;
      if (res.ok) {
        const data = await res.json();
        offsetRef.current = data.serverTime - Date.now() + Math.floor(rtt / 2);
      }
    } catch {
      // noop
    }
  }, []);

  const syncTotpStatus = useCallback(async () => {
    try {
      const res = await fetch(`/api/admin/${adminId}/totp`);
      if (!res.ok) return;
      const data = (await res.json()) as TotpStatusResponse;
      if (typeof data.code === "string") {
        setCode(data.code);
      } else if (data.code === null) {
        setCode("");
      } else {
        setCode("");
      }
      nextCodeRef.current = typeof data.nextCode === "string" ? data.nextCode : "";
      if (typeof data.period === "number" && data.period > 0) {
        setPeriod(data.period);
      }
      if (typeof data.serverTime === "number" && Number.isFinite(data.serverTime)) {
        offsetRef.current = data.serverTime - Date.now();
      }
      if (typeof data.remaining === "number" && data.remaining > 0) {
        setCountdown(data.remaining);
      }
    } catch {
      // noop
    }
  }, [adminId]);

  useEffect(() => {
    if (!enabled) {
      setCode("");
      nextCodeRef.current = "";
      setCountdown(30);
      lastPctRef.current = 100;
      if (barRef.current) {
        barRef.current.style.transform = "scaleX(1)";
      }
      return;
    }

    let cancelled = false;

    const init = async () => {
      await Promise.all([syncServerClock(), syncTotpStatus()]);
    };
    void init();

    const syncInterval = setInterval(() => {
      void syncServerClock();
      void syncTotpStatus();
    }, 10 * 1000);

    const tick = () => {
      if (cancelled) return;

      const ts = now();
      const secs = Math.floor(ts / 1000);
      const currentPeriod = Math.max(1, period);
      const remaining = currentPeriod - (secs % currentPeriod);
      const ms = ts % 1000;
      const smoothRemaining = remaining - ms / 1000;
      const pct = Math.max(0, Math.min(100, (smoothRemaining / currentPeriod) * 100));

      if (Math.abs(pct - lastPctRef.current) >= 0.1) {
        lastPctRef.current = pct;
        if (barRef.current) {
          barRef.current.style.transform = `scaleX(${pct / 100})`;
        }
      }

      if (secs !== lastSecRef.current) {
        lastSecRef.current = secs;
        setCountdown(remaining);
        if (remaining === currentPeriod) {
          if (nextCodeRef.current) {
            setCode(nextCodeRef.current);
            nextCodeRef.current = "";
          }
          void syncTotpStatus();
        }
      }

      rafRef.current = requestAnimationFrame(tick);
    };

    rafRef.current = requestAnimationFrame(tick);

    return () => {
      cancelled = true;
      cancelAnimationFrame(rafRef.current);
      clearInterval(syncInterval);
    };
  }, [enabled, now, period, syncServerClock, syncTotpStatus]);

  return { code, countdown, progressRef };
}
