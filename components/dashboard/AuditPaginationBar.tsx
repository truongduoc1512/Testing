"use client";

interface AuditPaginationBarProps {
  page: number;
  hasPrev: boolean;
  hasNext: boolean;
  loading: boolean;
  onPrev: () => void;
  onNext: () => void;
}

const PAGE_BTN_CLASS =
  "inline-flex min-h-9 items-center gap-1.5 rounded-lg border border-white/[0.08] bg-white/[0.04] px-3 text-xs font-semibold text-slate-300 transition-colors hover:bg-white/[0.08] disabled:opacity-30";

export default function AuditPaginationBar({
  page,
  hasPrev,
  hasNext,
  loading,
  onPrev,
  onNext,
}: AuditPaginationBarProps) {
  if (!hasPrev && !hasNext) return null;

  return (
    <div className="mt-3 flex items-center justify-between">
      <button
        type="button"
        onClick={onPrev}
        disabled={!hasPrev || loading}
        className={PAGE_BTN_CLASS}
      >
        ← Trước
      </button>
      <span className="text-xs text-slate-500">Trang {page}</span>
      <button
        type="button"
        onClick={onNext}
        disabled={!hasNext || loading}
        className={PAGE_BTN_CLASS}
      >
        Sau →
      </button>
    </div>
  );
}
