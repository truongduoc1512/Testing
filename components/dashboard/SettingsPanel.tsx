"use client";
import { useEffect, useState } from "react";
import { useAuditLogs, useChangePassword, type AuditLogItem } from "@/hooks";
import {
  Lock,
  KeyRound,
  Eye,
  EyeOff,
  Monitor,
  History,
  RefreshCw,
  ScrollText,
  Send,
  Globe,
  MessageCircle,
  Trash2,
  X,
} from "lucide-react";
import { StatusBadge } from "@/components/ui/StatusBadge";
import { ConfirmModal } from "@/components/ui/ConfirmModal";
import AuditPaginationBar from "./AuditPaginationBar";
import {
  formatAuditAction,
  formatAuditStatus,
  getAuditStatusBadgeKind,
} from "@/lib/utils";

export default function SettingsPanel() {
  const { form, show, status, message, setField, toggleVisibility, submit } =
    useChangePassword();
  const [cleanAuditOpen, setCleanAuditOpen] = useState(false);
  const [cleanAuditMessage, setCleanAuditMessage] = useState<string | null>(null);
  const [selectedAuditLog, setSelectedAuditLog] = useState<AuditLogItem | null>(null);
  const {
    logs,
    loading: auditLoading,
    error: auditError,
    refresh: refreshAudit,
    clear: clearAudit,
    clearing: auditClearing,
    clearError: auditClearError,
    page: auditPage,
    hasNext: auditHasNext,
    hasPrev: auditHasPrev,
    goNext: auditGoNext,
    goPrev: auditGoPrev,
  } = useAuditLogs(10);

  return (
    <div className="space-y-6">
      <section className="glass-card rounded-2xl p-6">
        <h2 className="mb-5 flex items-center gap-2 text-base font-semibold text-white">
          <Lock className="h-4 w-4 text-amber-400" />
          Đổi mật khẩu
        </h2>
        <div className="max-w-md space-y-4">
          <PasswordField
            label="Mật khẩu hiện tại"
            placeholder="Nhập mật khẩu hiện tại"
            value={form.oldPass}
            onChange={(v) => setField("oldPass", v)}
            show={show.old}
            onToggle={() => toggleVisibility("old")}
          />
          <PasswordField
            label="Mật khẩu mới"
            placeholder="Tối thiểu 8 ký tự"
            value={form.newPass}
            onChange={(v) => setField("newPass", v)}
            show={show.new}
            onToggle={() => toggleVisibility("new")}
          />
          <PasswordField
            label="Xác nhận mật khẩu mới"
            placeholder="Nhập lại mật khẩu mới"
            value={form.confirmPass}
            onChange={(v) => setField("confirmPass", v)}
            show={show.confirm}
            onToggle={() => toggleVisibility("confirm")}
          />
          <button
            onClick={submit}
            disabled={status === "loading"}
            className="flex items-center gap-2 rounded-xl bg-gradient-to-r from-violet-500 to-cyan-500 px-5 py-2.5 text-sm font-semibold text-white transition-opacity hover:opacity-90 disabled:opacity-50"
          >
            <KeyRound className="h-4 w-4" />
            {status === "loading" ? "Đang xử lý..." : "Đổi mật khẩu"}
          </button>
          {status === "error" && <p className="text-sm text-red-400">{message}</p>}
          {status === "success" && <p className="text-sm text-emerald-400">{message}</p>}
        </div>
      </section>

      <section className="glass-card rounded-2xl p-5">
        <div className="mb-4 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
          <h2 className="section-header">
            <ScrollText className="h-5 w-5 text-violet-400" />
            Nhật ký hoạt động
          </h2>
          <div className="flex flex-wrap gap-2">
            <button
              type="button"
              onClick={() => void refreshAudit()}
              disabled={auditLoading || auditClearing}
              aria-label="Tải lại nhật ký hoạt động"
              className="inline-flex min-h-11 shrink-0 items-center justify-center gap-2 rounded-xl border border-white/[0.08] bg-white/[0.04] px-3 text-xs font-semibold text-slate-300 transition-colors hover:bg-white/[0.08] disabled:opacity-50"
            >
              <RefreshCw
                className={`h-3.5 w-3.5 ${auditLoading ? "animate-spin" : ""}`}
              />
              Tải lại
            </button>
            <button
              type="button"
              onClick={() => setCleanAuditOpen(true)}
              disabled={auditLoading || auditClearing || logs.length === 0}
              aria-label="Clean audit log"
              className="inline-flex min-h-11 shrink-0 items-center justify-center gap-2 rounded-xl border border-red-500/25 bg-red-500/10 px-3 text-xs font-semibold text-red-200 transition-colors hover:bg-red-500/20 disabled:opacity-50"
            >
              <Trash2 className="h-3.5 w-3.5" />
              Clean audit
            </button>
          </div>
        </div>

        {cleanAuditMessage ? (
          <p className="mb-3 rounded-xl border border-emerald-500/20 bg-emerald-500/5 px-3 py-2 text-xs text-emerald-300">
            {cleanAuditMessage}
          </p>
        ) : null}
        {auditClearError ? (
          <p className="mb-3 rounded-xl border border-red-500/20 bg-red-500/5 px-3 py-2 text-xs text-red-300">
            {auditClearError}
          </p>
        ) : null}

        {auditError ? (
          <RetryState message={auditError} onRetry={() => void refreshAudit()} />
        ) : auditLoading && logs.length === 0 ? (
          <SettingsSkeleton rows={4} />
        ) : logs.length === 0 ? (
          <div className="rounded-xl border border-white/[0.06] bg-white/[0.02] px-4 py-8 text-center text-sm text-slate-500">
            Chưa có nhật ký hoạt động.
          </div>
        ) : (
          <div className="space-y-1">
            {logs.map((log) => (
              <AuditLogRow key={log.id} log={log} onOpen={setSelectedAuditLog} />
            ))}
            <AuditPaginationBar
              page={auditPage}
              hasPrev={auditHasPrev}
              hasNext={auditHasNext}
              loading={auditLoading}
              onPrev={() => void auditGoPrev()}
              onNext={() => void auditGoNext()}
            />
          </div>
        )}
      </section>

      <section className="glass-card rounded-2xl p-6">
        <h2 className="mb-4 flex items-center gap-2 text-base font-semibold text-white">
          <Monitor className="h-4 w-4 text-cyan-400" />
          VieShop - Family Management
        </h2>

        <div className="flex flex-wrap items-center gap-x-4 gap-y-2 text-sm">
          <span className="text-slate-500">Hỗ trợ:</span>
          <a
            href="https://t.me/haqfuong2075"
            target="_blank"
            rel="noopener noreferrer"
            className="flex items-center gap-1.5 text-cyan-400 transition-colors hover:text-cyan-300"
          >
            <Send className="h-3.5 w-3.5" />
            Telegram @haqfuong2075
          </a>
          <span className="text-slate-600">|</span>
          <a
            href="https://zalo.me/g/hlstya673"
            target="_blank"
            rel="noopener noreferrer"
            className="flex items-center gap-1.5 text-blue-400 transition-colors hover:text-blue-300"
          >
            <Globe className="h-3.5 w-3.5" />
            Group Zalo
          </a>
          <span className="text-slate-600">|</span>
          <a
            href="https://zalo.me/0325093767"
            target="_blank"
            rel="noopener noreferrer"
            className="flex items-center gap-1.5 text-violet-400 transition-colors hover:text-violet-300"
          >
            <MessageCircle className="h-3.5 w-3.5" />
            Zalo
          </a>
        </div>
      </section>

      <ConfirmModal
        open={cleanAuditOpen}
        title="Clean audit log?"
        description="Bạn chắc chứ?"
        confirmText="Chắc để xóa"
        cancelText="Hủy cancel"
        loading={auditClearing}
        variant="danger"
        onCancel={() => {
          if (!auditClearing) setCleanAuditOpen(false);
        }}
        onConfirm={async () => {
          setCleanAuditMessage(null);
          try {
            const result = await clearAudit();
            setCleanAuditMessage(
              `Đã xóa ${result.deletedCount.toLocaleString("vi-VN")} dòng nhật ký.`,
            );
            setCleanAuditOpen(false);
          } catch (error) {
            void error;
            setCleanAuditOpen(false);
          }
        }}
      />
      {selectedAuditLog ? (
        <AuditLogDetailModal
          log={selectedAuditLog}
          onClose={() => setSelectedAuditLog(null)}
        />
      ) : null}
    </div>
  );
}

function PasswordField({
  label,
  placeholder,
  value,
  onChange,
  show,
  onToggle,
}: {
  label: string;
  placeholder: string;
  value: string;
  onChange: (v: string) => void;
  show: boolean;
  onToggle: () => void;
}) {
  return (
    <div>
      <label className="mb-1.5 block text-sm text-slate-400">{label}</label>
      <div className="relative">
        <input
          type={show ? "text" : "password"}
          value={value}
          onChange={(e) => onChange(e.target.value)}
          className="form-input pr-10"
          placeholder={placeholder}
        />
        <button
          type="button"
          onClick={onToggle}
          aria-label={show ? `Ẩn ${label}` : `Hiện ${label}`}
          className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-500 transition-colors hover:text-slate-300"
        >
          {show ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
        </button>
      </div>
    </div>
  );
}

function AuditLogRow({
  log,
  onOpen,
}: {
  log: AuditLogItem;
  onOpen: (log: AuditLogItem) => void;
}) {
  const badgeKind = getAuditStatusBadgeKind(log.status);
  const date = new Date(log.createdAt);
  const displayDate = Number.isNaN(date.getTime())
    ? log.createdAt
    : date.toLocaleDateString("vi-VN");

  return (
    <button
      type="button"
      onClick={() => onOpen(log)}
      aria-label={`Xem chi tiết nhật ký ${formatAuditAction(log.action)}`}
      className="grid w-full grid-cols-[2.25rem_minmax(0,1fr)] gap-3 rounded-lg px-2 py-3 text-left transition-colors hover:bg-white/[0.03] focus:outline-none focus:ring-2 focus:ring-cyan-500/40 sm:grid-cols-[2.25rem_minmax(0,1fr)_auto]"
    >
      <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-violet-500/15 text-violet-300">
        <History className="h-4 w-4" />
      </div>
      <div className="min-w-0 flex-1">
        <div className="flex flex-wrap items-center gap-2">
          <p className="truncate text-sm font-semibold text-white">
            {formatAuditAction(log.action)}
          </p>
          <StatusBadge kind={badgeKind} label={formatAuditStatus(log.status)} />
        </div>
        <p className="mt-1 line-clamp-2 text-xs text-slate-400">
          {log.message || `${log.targetType} · ${log.action}`}
        </p>
      </div>
      <span className="col-start-2 text-[11px] text-slate-500 sm:col-start-auto sm:shrink-0">
        {displayDate}
      </span>
    </button>
  );
}

function AuditLogDetailModal({
  log,
  onClose,
}: {
  log: AuditLogItem;
  onClose: () => void;
}) {
  const badgeKind = getAuditStatusBadgeKind(log.status);
  const timeParts = getAuditTimeParts(log.createdAt);

  useEffect(() => {
    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === "Escape") onClose();
    };
    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [onClose]);

  return (
    <div
      className="fixed inset-0 z-[10000] flex items-end bg-black/65 p-3 backdrop-blur-sm sm:items-center sm:justify-center sm:p-4"
      onClick={onClose}
    >
      <div
        role="dialog"
        aria-modal="true"
        aria-labelledby="audit-log-detail-title"
        className="max-h-[calc(100dvh-1.5rem)] w-full overflow-y-auto rounded-2xl border border-white/[0.08] bg-[#101729] p-4 shadow-2xl sm:max-w-lg sm:p-5"
        onClick={(event) => event.stopPropagation()}
      >
        <div className="flex items-start gap-3">
          <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl border border-violet-500/25 bg-violet-500/10 text-violet-300">
            <History className="h-5 w-5" />
          </div>
          <div className="min-w-0 flex-1">
            <h3
              id="audit-log-detail-title"
              className="break-words text-base font-bold text-white"
            >
              {formatAuditAction(log.action)}
            </h3>
            <div className="mt-2 flex flex-wrap items-center gap-2">
              <StatusBadge kind={badgeKind} label={formatAuditStatus(log.status)} />
              <span className="text-xs text-slate-500">
                {timeParts.time} {timeParts.date}
              </span>
            </div>
          </div>
          <button
            type="button"
            onClick={onClose}
            aria-label="Đóng chi tiết nhật ký"
            className="inline-flex min-h-11 min-w-11 items-center justify-center rounded-xl text-slate-400 transition-colors hover:bg-white/[0.06] hover:text-white"
          >
            <X className="h-4 w-4" />
          </button>
        </div>

        <div className="mt-4 rounded-xl border border-white/[0.06] bg-white/[0.03] p-3">
          <p className="whitespace-pre-wrap break-words text-sm leading-6 text-slate-200">
            {log.message || `${log.targetType} · ${log.action}`}
          </p>
        </div>
      </div>
    </div>
  );
}

function getAuditTimeParts(createdAt: string) {
  const date = new Date(createdAt);
  if (Number.isNaN(date.getTime())) {
    return { time: createdAt || "Không có dữ liệu", date: "Không có dữ liệu" };
  }

  return {
    time: date.toLocaleTimeString("vi-VN", {
      hour: "2-digit",
      minute: "2-digit",
      second: "2-digit",
      hour12: false,
    }),
    date: date.toLocaleDateString("vi-VN"),
  };
}

function RetryState({ message, onRetry }: { message: string; onRetry: () => void }) {
  return (
    <div className="rounded-xl border border-red-500/20 bg-red-500/5 px-4 py-4">
      <p className="text-sm text-red-300">{message}</p>
      <button
        type="button"
        onClick={onRetry}
        className="mt-3 inline-flex min-h-10 items-center gap-2 rounded-xl border border-red-500/25 bg-red-500/10 px-4 text-sm font-semibold text-red-200 transition-colors hover:bg-red-500/20"
      >
        <RefreshCw className="h-4 w-4" />
        Thử lại
      </button>
    </div>
  );
}

function SettingsSkeleton({ rows }: { rows: number }) {
  return (
    <div className="space-y-2">
      {Array.from({ length: rows }).map((_, index) => (
        <div key={index} className="h-14 animate-pulse rounded-xl bg-white/[0.04]" />
      ))}
    </div>
  );
}
