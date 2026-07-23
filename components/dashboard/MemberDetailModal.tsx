"use client";
import { useState, useCallback } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { X, Trash2, Save, RefreshCw, Calendar, CheckCircle, Clock } from "lucide-react";
import { daysUntil, expiryColor, expiryLabel } from "@/lib/utils";
import { notifyDashboardDataChanged } from "@/lib/browser/dashboard-events";
import { ConfirmModal } from "@/components/ui/ConfirmModal";

interface MemberInfo {
  email: string;
  name: string;
  role: string;
  status: string;
  joinedAt: string | null;
  startDate: string | null;
  endDate: string | null;
  renewed: boolean;
  googleUserId?: string;
  creditLimit?: number;
  lastCreditUsed?: number;
}

interface Props {
  adminId: string;
  member: MemberInfo;
  familyType: string;
  onClose: () => void;
  onUpdated: () => void;
}

function fmtDate(iso: string | null): string {
  if (!iso) return "";
  return new Date(iso).toISOString().slice(0, 10);
}

export default function MemberDetailModal({
  adminId,
  member,
  familyType,
  onClose,
  onUpdated,
}: Props) {
  const [startDate, setStartDate] = useState(fmtDate(member.startDate));
  const [endDate, setEndDate] = useState(fmtDate(member.endDate));
  const [renewed, setRenewed] = useState(member.renewed);
  const [saving, setSaving] = useState(false);
  const [removing, setRemoving] = useState(false);
  const [confirmRemove, setConfirmRemove] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const memberGoogleId = member.googleUserId || null;
  const memberEmail = member.email;

  const handleSave = useCallback(async () => {
    setSaving(true);
    setError(null);
    try {
      const res = await fetch(`/api/admin/${adminId}/members`, {
        method: "PATCH",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          memberGoogleId,
          memberEmail,
          startDate: startDate || null,
          endDate: endDate || null,
          renewed,
        }),
      });
      if (res.ok) {
        notifyDashboardDataChanged("member-update");
        onUpdated();
        onClose();
      } else {
        const err = await res.json().catch(() => null);
        setError(err?.error || "Lỗi cập nhật");
      }
    } catch {
      setError("Lỗi kết nối");
    }
    setSaving(false);
  }, [adminId, memberGoogleId, memberEmail, startDate, endDate, renewed, onUpdated, onClose]);

  const handleRemove = useCallback(async () => {
    setRemoving(true);
    setError(null);
    try {
      const res = await fetch(`/api/admin/${adminId}/remove-member`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ memberGoogleId, memberEmail }),
      });
      if (res.ok) {
        notifyDashboardDataChanged("member-remove");
        onUpdated();
        onClose();
      } else {
        const err = await res.json().catch(() => null);
        setError(err?.error || "Lỗi xóa member");
        setConfirmRemove(false);
      }
    } catch {
      setError("Lỗi kết nối");
      setConfirmRemove(false);
    }
    setRemoving(false);
  }, [adminId, memberGoogleId, memberEmail, onUpdated, onClose]);

  const isManager = member.role.includes("MANAGER");
  const isInvited = member.status === "invited";
  const canRemove = !isManager && !isInvited;
  const joinedAtDisplay = member.joinedAt ?? member.startDate;

  const daysLeft = endDate ? daysUntil(endDate) : null;

  return (
    <>
      <AnimatePresence>
      <motion.div
        className="fixed inset-0 z-[60] flex items-center justify-center bg-black/60 backdrop-blur-sm"
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        exit={{ opacity: 0 }}
        onClick={onClose}
      >
        <motion.div
          className="relative w-full max-w-md rounded-2xl border border-white/[0.08] bg-[#0f1729]/95 p-6 shadow-2xl backdrop-blur-xl"
          initial={{ scale: 0.9, opacity: 0, y: 20 }}
          animate={{ scale: 1, opacity: 1, y: 0 }}
          exit={{ scale: 0.9, opacity: 0, y: 20 }}
          transition={{ type: "spring", damping: 25, stiffness: 300 }}
          onClick={(e) => e.stopPropagation()}
          role="dialog"
          aria-modal="true"
          aria-labelledby="member-detail-title"
        >

          <div className="mb-5 flex items-center justify-between">
            <div>
              <h3 id="member-detail-title" className="text-lg font-bold text-white">
                {member.name || member.email}
              </h3>
              {member.name && member.name !== member.email && (
                <p className="text-sm text-slate-400">{member.email}</p>
              )}
            </div>
            <button
              onClick={onClose}
              aria-label="Đóng chi tiết member"
              className="rounded-lg p-1.5 text-slate-400 transition-colors hover:bg-white/[0.06] hover:text-white"
            >
              <X className="h-5 w-5" />
            </button>
          </div>

          <div className="mb-5 flex items-center gap-2">
            <span
              className={`rounded-lg px-3 py-1 text-xs font-semibold ${
                isManager
                  ? "bg-amber-500/15 text-amber-400"
                  : isInvited
                    ? "bg-orange-500/15 text-orange-400"
                    : "bg-emerald-500/15 text-emerald-400"
              }`}
            >
              {isManager ? "Manager" : isInvited ? "Đang chờ" : "Member"}
            </span>
            {joinedAtDisplay && (
              <span className="text-xs text-slate-500">
                Tham gia: {new Date(joinedAtDisplay).toLocaleDateString("vi-VN")}
              </span>
            )}
          </div>

          {!isManager && (
            <div className="space-y-4">

              <div>
                <label className="mb-1.5 flex items-center gap-1.5 text-xs font-semibold text-slate-400">
                  <Calendar className="h-3.5 w-3.5" />
                  Ngày bắt đầu
                </label>
                <input
                  type="date"
                  value={startDate}
                  onChange={(e) => setStartDate(e.target.value)}
                  className="w-full rounded-xl border border-white/[0.08] bg-white/[0.04] px-4 py-2.5 text-sm text-white outline-none transition-all focus:border-cyan-500/40 focus:ring-1 focus:ring-cyan-500/20 [color-scheme:dark]"
                />
              </div>

              <div>
                <label className="mb-1.5 flex items-center gap-1.5 text-xs font-semibold text-slate-400">
                  <Calendar className="h-3.5 w-3.5" />
                  Ngày kết thúc
                </label>
                <input
                  type="date"
                  value={endDate}
                  onChange={(e) => setEndDate(e.target.value)}
                  className="w-full rounded-xl border border-white/[0.08] bg-white/[0.04] px-4 py-2.5 text-sm text-white outline-none transition-all focus:border-cyan-500/40 focus:ring-1 focus:ring-cyan-500/20 [color-scheme:dark]"
                />
                {daysLeft !== null && (
                  <p className={`mt-1 text-xs ${expiryColor(daysLeft)}`}>
                    {daysLeft <= 0
                      ? `⚠️ ${expiryLabel(daysLeft)}`
                      : daysLeft <= 3
                        ? `⏰ ${expiryLabel(daysLeft)}`
                        : expiryLabel(daysLeft)}
                  </p>
                )}
              </div>

              <div>
                <label className="mb-1.5 flex items-center gap-1.5 text-xs font-semibold text-slate-400">
                  <RefreshCw className="h-3.5 w-3.5" />
                  Gia hạn
                </label>
                <button
                  onClick={() => setRenewed(!renewed)}
                  className={`flex w-full items-center justify-between rounded-xl border px-4 py-2.5 text-sm font-medium transition-all ${
                    renewed
                      ? "border-emerald-500/30 bg-emerald-500/10 text-emerald-400"
                      : "border-white/[0.08] bg-white/[0.04] text-slate-400"
                  }`}
                >
                  <span>{renewed ? "Đã gia hạn" : "Chưa gia hạn"}</span>
                  {renewed ? (
                    <CheckCircle className="h-4 w-4" />
                  ) : (
                    <Clock className="h-4 w-4" />
                  )}
                </button>
              </div>
            </div>
          )}

          {error && (
            <div className="mt-4 rounded-xl border border-red-500/20 bg-red-500/10 px-4 py-2.5 text-sm text-red-400">
              {error}
            </div>
          )}

          <div className="mt-6 flex items-center justify-between">
            <div>
              {canRemove && (
                <button
                  onClick={() => setConfirmRemove(true)}
                  className="flex items-center gap-1.5 rounded-xl border border-red-500/20 bg-red-500/10 px-4 py-2 text-sm font-semibold text-red-400 transition-all hover:bg-red-500/20"
                >
                  <Trash2 className="h-4 w-4" />
                  Xóa khỏi Family
                </button>
              )}
            </div>

            {!isManager && (
              <button
                onClick={handleSave}
                disabled={saving}
                className="flex items-center gap-1.5 rounded-xl border border-cyan-500/30 bg-gradient-to-r from-cyan-500/20 to-blue-500/20 px-5 py-2 text-sm font-semibold text-cyan-300 transition-all hover:from-cyan-500/30 hover:to-blue-500/30 disabled:opacity-50"
              >
                {saving ? (
                  <RefreshCw className="h-4 w-4 animate-spin" />
                ) : (
                  <Save className="h-4 w-4" />
                )}
                Lưu
              </button>
            )}
          </div>
        </motion.div>
      </motion.div>
      </AnimatePresence>
      <ConfirmModal
        open={confirmRemove}
        title="Xóa member khỏi family?"
        description="Member sẽ bị gỡ khỏi family hiện tại."
        confirmText="Xóa member"
        cancelText="Hủy"
        loading={removing}
        variant="danger"
        onCancel={() => setConfirmRemove(false)}
        onConfirm={() => {
          void handleRemove();
        }}
      />
    </>
  );
}
