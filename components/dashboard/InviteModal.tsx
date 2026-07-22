"use client";
import { motion, AnimatePresence } from "framer-motion";
import { X, Send, RefreshCw } from "lucide-react";

interface Props {
  inviteEmail: string;
  setInviteEmail: (v: string) => void;
  inviting: boolean;
  inviteResult: { type: string; msg: string } | null;
  onClose: () => void;
  onInvite: () => void;
}

export default function InviteModal({
  inviteEmail,
  setInviteEmail,
  inviting,
  inviteResult,
  onClose,
  onInvite,
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
          initial={{ scale: 0.9, opacity: 0 }}
          animate={{ scale: 1, opacity: 1 }}
          exit={{ scale: 0.9, opacity: 0 }}
          transition={{ type: "spring", damping: 25 }}
          onClick={(e) => e.stopPropagation()}
          className="w-full max-w-sm rounded-2xl border border-cyan-500/20 bg-[#0d1320] p-5 shadow-2xl"
        >
          <div className="mb-3 flex items-center justify-between">
            <h3 className="text-sm font-bold text-white">✉️ Mời thành viên</h3>
            <button
              onClick={onClose}
              className="rounded-lg p-1 text-slate-400 hover:bg-white/[0.06] hover:text-white"
            >
              <X className="h-4 w-4" />
            </button>
          </div>
          <div className="flex gap-2">
            <input
              type="email"
              value={inviteEmail}
              onChange={(e) => setInviteEmail(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === "Enter") {
                  e.preventDefault();
                  onInvite();
                }
              }}
              placeholder="email@gmail.com"
              className="flex-1 rounded-lg border border-white/10 bg-white/[0.06] px-3 py-2 text-sm text-white placeholder-slate-500 outline-none focus:border-cyan-500/50"
              disabled={inviting}
              autoFocus
            />
            <button
              onClick={onInvite}
              disabled={inviting || !inviteEmail.trim()}
              className="flex items-center gap-1.5 rounded-lg bg-cyan-500/20 px-4 py-2 text-sm font-semibold text-cyan-300 transition-all hover:bg-cyan-500/30 disabled:opacity-50"
            >
              {inviting ? (
                <RefreshCw className="h-4 w-4 animate-spin" />
              ) : (
                <Send className="h-4 w-4" />
              )}
              Mời
            </button>
          </div>
          {inviting && (
            <div className="mt-3 flex items-center gap-2 text-xs text-blue-400">
              <span className="inline-block h-3 w-3 animate-spin rounded-full border-[1.5px] border-blue-400 border-t-transparent" />
              Đang gửi lời mời...
            </div>
          )}
          {inviteResult && !inviting && (
            <div
              className={`mt-3 rounded-lg px-3 py-2 text-xs font-medium ${
                inviteResult.type === "success"
                  ? "bg-emerald-500/10 text-emerald-400"
                  : "bg-red-500/10 text-red-400"
              }`}
            >
              {inviteResult.msg}
            </div>
          )}
        </motion.div>
      </motion.div>
    </AnimatePresence>
  );
}
