"use client";
import { useEffect, useState, useRef } from "react";
import { cn } from "@/lib/utils";

interface ToastProps {
  message: string;
  type?: "success" | "error" | "info";
  duration?: number;
  onClose?: () => void;
}

const icons = {
  success: (
    <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
      <path
        strokeLinecap="round"
        strokeLinejoin="round"
        strokeWidth={2}
        d="M5 13l4 4L19 7"
      />
    </svg>
  ),
  error: (
    <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
      <path
        strokeLinecap="round"
        strokeLinejoin="round"
        strokeWidth={2}
        d="M6 18L18 6M6 6l12 12"
      />
    </svg>
  ),
  info: (
    <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
      <path
        strokeLinecap="round"
        strokeLinejoin="round"
        strokeWidth={2}
        d="M13 16h-1v-4h-1m1-4h.01M12 2a10 10 0 100 20 10 10 0 000-20z"
      />
    </svg>
  ),
};

const colors = {
  success: "from-emerald-500/20 to-emerald-500/5 border-emerald-500/40 text-emerald-300",
  error: "from-red-500/20 to-red-500/5 border-red-500/40 text-red-300",
  info: "from-cyan-500/20 to-cyan-500/5 border-cyan-500/40 text-cyan-300",
};

export function Toast({
  message,
  type = "success",
  duration = 3000,
  onClose,
}: ToastProps) {
  const [visible, setVisible] = useState(false);
  const [leaving, setLeaving] = useState(false);
  const onCloseRef = useRef(onClose);
  onCloseRef.current = onClose;

  useEffect(() => {
    requestAnimationFrame(() => setVisible(true));

    const timer = setTimeout(() => {
      setLeaving(true);
      setTimeout(() => onCloseRef.current?.(), 300);
    }, duration);

    return () => clearTimeout(timer);
  }, [duration]);

  return (
    <div
      className={cn(
        "fixed left-1/2 top-6 z-[9999] -translate-x-1/2 transition-all duration-300",
        visible && !leaving ? "translate-y-0 opacity-100" : "-translate-y-4 opacity-0",
      )}
    >
      <div
        className={cn(
          "flex items-center gap-3 rounded-xl border bg-gradient-to-r px-5 py-3 shadow-2xl backdrop-blur-md",
          colors[type],
        )}
      >
        {icons[type]}
        <span className="text-sm font-medium">{message}</span>
      </div>
    </div>
  );
}
