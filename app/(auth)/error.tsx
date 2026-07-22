"use client";
export default function AuthError({
  error: _error,
  reset,
}: {
  error: Error & { digest?: string };
  reset: () => void;
}) {
  void _error;
  return (
    <div className="flex min-h-screen items-center justify-center">
      <div className="glass-card w-full max-w-md rounded-4xl p-6 text-center sm:p-8">
        <h2 className="mb-2 text-xl font-bold text-white">Đã xảy ra lỗi</h2>
        <p className="mb-4 text-sm text-slate-400">Ứng dụng gặp lỗi ngoài dự kiến.</p>
        <button
          onClick={reset}
          className="rounded-xl bg-brand-cyan/20 px-6 py-2 text-sm font-semibold text-brand-cyan transition-colors hover:bg-brand-cyan/30"
        >
          Thử lại
        </button>
      </div>
    </div>
  );
}
