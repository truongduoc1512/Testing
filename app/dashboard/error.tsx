"use client";
export default function DashboardError({
  error: _error,
  reset,
}: {
  error: Error & { digest?: string };
  reset: () => void;
}) {
  void _error;
  return (
    <div className="flex min-h-screen items-center justify-center bg-[#050510] p-4">
      <div className="w-full max-w-md rounded-2xl border border-white/[0.06] bg-[#0f1729] p-8 text-center shadow-2xl">
        <h2 className="mb-2 text-lg font-bold text-white">Lỗi dashboard</h2>
        <p className="mb-6 text-sm text-slate-400">Không tải được dashboard.</p>
        <button
          onClick={reset}
          className="rounded-xl border border-violet-500/40 bg-violet-500/20 px-6 py-2.5 text-sm font-semibold text-violet-300 transition-all hover:bg-violet-500/30"
        >
          Thử lại
        </button>
      </div>
    </div>
  );
}
