"use client";
import { motion, AnimatePresence } from "framer-motion";
import { createPortal } from "react-dom";
import { useEffect, useState } from "react";

type ToastType = "info" | "success" | "error";

interface SyncToastProps {
  log: { msg: string; type: ToastType } | null;
}

const typeStyles: Record<ToastType, string> = {
  success: "border border-emerald-500/30 bg-emerald-950/90 text-emerald-300",
  error: "border border-red-500/30 bg-red-950/90 text-red-300",
  info: "border border-blue-500/30 bg-blue-950/90 text-blue-300",
};

export default function SyncToast({ log }: SyncToastProps) {
  const [mounted, setMounted] = useState(false);
  useEffect(() => setMounted(true), []);

  if (!mounted) return null;

  return createPortal(
    <AnimatePresence>
      {log && (
        <motion.div
          initial={{ y: -80, opacity: 0 }}
          animate={{ y: 0, opacity: 1 }}
          exit={{ y: -80, opacity: 0 }}
          transition={{ type: "spring", damping: 25, stiffness: 300 }}
          className={`fixed bottom-6 right-6 z-[9999] flex max-w-[420px] items-center gap-3 rounded-xl px-5 py-3 shadow-2xl backdrop-blur-xl ${typeStyles[log.type]}`}
        >
          {log.type === "info" && (
            <span className="inline-block h-4 w-4 flex-shrink-0 animate-spin rounded-full border-2 border-blue-400 border-t-transparent" />
          )}
          <span className="text-sm font-medium">
            {log.msg.replace(/\.{3}$/, "")}
            {log.type === "info" && <AnimatedDots />}
          </span>
        </motion.div>
      )}
    </AnimatePresence>,
    document.body,
  );
}

function AnimatedDots() {
  return (
    <span className="inline-flex w-5 ml-0.5">
      {[0, 1, 2].map((i) => (
        <motion.span
          key={i}
          animate={{ opacity: [0, 1, 0] }}
          transition={{ duration: 1.2, repeat: Infinity, delay: i * 0.3 }}
          className="text-sm"
        >
          .
        </motion.span>
      ))}
    </span>
  );
}
