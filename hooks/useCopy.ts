"use client";
import { useState, useCallback, useRef } from "react";

export function useCopy(resetMs = 1500): [string, (text: string, label: string) => void] {
  const [copied, setCopied] = useState("");
  const timerRef = useRef<ReturnType<typeof setTimeout> | undefined>(undefined);

  const copy = useCallback(
    (text: string, label: string) => {
      navigator.clipboard.writeText(text).catch(() => undefined);
      setCopied(label);
      clearTimeout(timerRef.current);
      timerRef.current = setTimeout(() => setCopied(""), resetMs);
    },
    [resetMs],
  );

  return [copied, copy];
}
