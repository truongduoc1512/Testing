"use client";
import { useLiveClock } from "@/hooks/useLiveClock";
import { Clock } from "lucide-react";

interface LiveClockProps {
  nextAutoSync?: Date | null;
}

export default function LiveClock({ nextAutoSync }: LiveClockProps) {
  const now = useLiveClock();

  const autoSyncDiff = nextAutoSync
    ? Math.max(0, Math.floor((nextAutoSync.getTime() - now.getTime()) / 1000))
    : 0;
  const autoSyncMin = Math.floor(autoSyncDiff / 60);
  const autoSyncSec = autoSyncDiff % 60;

  return (
    <div className="flex min-w-0 shrink items-center gap-2 overflow-hidden">
      <div className="hidden shrink-0 items-center gap-1.5 text-xs text-slate-400 lg:flex">
        <Clock className="h-3.5 w-3.5 shrink-0" />
        <span className="whitespace-nowrap">
          {now.toLocaleDateString("vi-VN", {
            day: "2-digit",
            month: "2-digit",
            year: "numeric",
          })}{" "}
          {now.toLocaleTimeString("vi-VN", {
            hour: "2-digit",
            minute: "2-digit",
            hour12: false,
          })}
        </span>
      </div>
      {nextAutoSync && autoSyncDiff > 0 && (
        <span className="hidden shrink-0 whitespace-nowrap rounded-md bg-white/[0.04] px-2 py-0.5 text-[10px] text-slate-500 2xl:inline-flex">
          Auto sync: {autoSyncMin}:{String(autoSyncSec).padStart(2, "0")}
        </span>
      )}
    </div>
  );
}
