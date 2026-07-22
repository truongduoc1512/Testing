"use client";
import { motion, AnimatePresence } from "framer-motion";
import { X, RefreshCw, ClipboardCopy } from "lucide-react";

interface CreditMember {
  googleUserId?: string;
  name: string;
  email: string;
  currentLimit: number;
}

interface Props {
  member: CreditMember;
  creditInput: string;
  setCreditInput: (v: string) => void;
  settingCredit: boolean;
  monthlyCredit: number;
  onClose: () => void;
  onSubmit: () => void;
}

export default function SetCreditModal({
  member,
  creditInput,
  setCreditInput,
  settingCredit,
  monthlyCredit,
  onClose,
  onSubmit,
}: Props) {
  return (
    <AnimatePresence>
      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 1, pointerEvents: "auto" as const }}
        exit={{ opacity: 0, pointerEvents: "none" as const }}
        className="fixed inset-0 z-[60] flex items-center justify-center bg-black/50 backdrop-blur-sm"
        onClick={onClose}
      >
        <motion.div
          initial={{ scale: 0.95, opacity: 0 }}
          animate={{ scale: 1, opacity: 1 }}
          exit={{ scale: 0.95, opacity: 0 }}
          transition={{ duration: 0.15 }}
          onClick={(e) => e.stopPropagation()}
          className="w-full max-w-sm rounded-2xl border border-amber-500/20 bg-[#0d1320] p-5 shadow-2xl"
        >
          <div className="mb-4 flex items-center justify-between">
            <h3 className="text-sm font-bold text-white">Set Credit tối đa</h3>
            <button
              onClick={onClose}
              className="rounded-lg p-1 text-slate-400 hover:bg-white/[0.06] hover:text-white"
            >
              <X className="h-4 w-4" />
            </button>
          </div>

          <div className="mb-4 rounded-xl border border-white/[0.06] bg-white/[0.03] px-4 py-3">
            <p className="text-sm font-semibold text-white">{member.name}</p>
            <p className="text-xs text-amber-400">
              Giá trị hiện tại: {member.currentLimit}
            </p>
          </div>

          <div className="mb-2">
            <label className="mb-1.5 block text-xs font-medium text-slate-400">
              Credit tối đa
            </label>
            <div className="flex items-center gap-2">
              <input
                type="number"
                min="0"
                value={creditInput}
                onChange={(e) => {
                  const v = parseInt(e.target.value);
                  setCreditInput(String(Math.max(0, isNaN(v) ? 0 : v)));
                }}
                className="flex-1 rounded-lg border border-white/10 bg-white/[0.06] px-3 py-2.5 font-mono text-sm text-white outline-none focus:border-amber-500/50"
                autoFocus
              />
              <button
                onClick={() => setCreditInput(String(monthlyCredit))}
                className="rounded-lg bg-white/[0.06] p-2.5 text-slate-400 transition-colors hover:bg-white/[0.1] hover:text-white"
                title="Đặt bằng credit tối đa"
              >
                <ClipboardCopy className="h-4 w-4" />
              </button>
            </div>
          </div>
          <p className="mb-4 text-[11px] text-slate-500">
            Nhập 0 = không giới hạn · Nhập số dương = giới hạn credit tối đa
          </p>

          <div className="flex justify-end gap-2">
            <button
              onClick={onClose}
              className="rounded-xl border border-white/[0.1] bg-white/[0.04] px-5 py-2 text-sm font-semibold text-white transition-all hover:bg-white/[0.08]"
            >
              Hủy
            </button>
            <button
              onClick={onSubmit}
              disabled={settingCredit}
              className="flex items-center gap-1.5 rounded-xl border border-amber-500/40 bg-amber-500/20 px-5 py-2 text-sm font-semibold text-amber-300 transition-all hover:bg-amber-500/30 disabled:opacity-50"
            >
              {settingCredit ? <RefreshCw className="h-4 w-4 animate-spin" /> : null}
              Xác nhận
            </button>
          </div>
        </motion.div>
      </motion.div>
    </AnimatePresence>
  );
}
