"use client";
import { useState, useCallback, useRef } from "react";

type ToastType = "info" | "success" | "error";

export interface ToastData {
  msg: string;
  type: ToastType;
}

export function useToast(defaultDuration = 3_000) {
  const [toast, setToast] = useState<ToastData | null>(null);
  const timerRef = useRef<ReturnType<typeof setTimeout> | undefined>(undefined);

  const show = useCallback(
    (msg: string, type: ToastType = "info", duration = defaultDuration) => {
      clearTimeout(timerRef.current);
      setToast({ msg, type });
      if (duration > 0) {
        timerRef.current = setTimeout(() => setToast(null), duration);
      }
    },
    [defaultDuration],
  );

  const showPersistent = useCallback((msg: string, type: ToastType = "info") => {
    clearTimeout(timerRef.current);
    setToast({ msg, type });
  }, []);

  const dismiss = useCallback(() => {
    clearTimeout(timerRef.current);
    setToast(null);
  }, []);

  return { toast, show, showPersistent, dismiss };
}
