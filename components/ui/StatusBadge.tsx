"use client";

import { STATUS_BADGE_META, type StatusBadgeKind } from "@/lib/constants/status";
import { cn } from "@/lib/utils";

type StatusBadgeProps = {
  kind: StatusBadgeKind;
  label?: string;
  className?: string;
};

export function StatusBadge({ kind, label, className }: StatusBadgeProps) {
  const meta = STATUS_BADGE_META[kind] ?? STATUS_BADGE_META.unknown;

  return (
    <span
      className={cn(
        "inline-flex items-center gap-1 rounded-full border px-2 py-0.5 text-[10px] font-bold",
        meta.className,
        className,
      )}
    >
      <span className="h-1.5 w-1.5 rounded-full bg-current" aria-hidden="true" />
      {label ?? meta.label}
    </span>
  );
}
