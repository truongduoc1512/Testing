"use client";
import { useMemo, useState } from "react";
import { motion } from "framer-motion";
import {
  Users,
  ArrowLeft,
  RefreshCw,
  Plus,
  CheckSquare,
  Square,
  Check,
} from "lucide-react";
import AdminCard from "./AdminCard";
import type { AdminRow } from "@/hooks/useDashboardData";
import { FAMILY_TYPES } from "@/lib/constants";
import { isSyncableAccountStatus } from "@/lib/utils";
import { useBulkSelection } from "@/hooks";
import { ConfirmModal } from "@/components/ui/ConfirmModal";

interface Props {
  initialLoading: boolean;
  activeFamilyTab: string | null;
  setActiveFamilyTab: (tab: string | null) => void;
  admins: AdminRow[];
  syncing: boolean;
  syncAllRef: React.MutableRefObject<boolean>;
  handleSyncAll: (type: string | null) => void;
  handleSyncSelected: (ids: string[]) => void;
  handleSyncAdmin: (id: string) => void;
  setShowAddAdmin: (show: boolean) => void;
  setSelectedAdminId: (id: string) => void;
}

export const FAMILY_TABS = FAMILY_TYPES;

export default function FamilyTab({
  initialLoading,
  activeFamilyTab,
  setActiveFamilyTab,
  admins,
  syncing,
  syncAllRef,
  handleSyncAll,
  handleSyncSelected,
  handleSyncAdmin,
  setShowAddAdmin,
  setSelectedAdminId,
}: Props) {
  const [confirmMode, setConfirmMode] = useState<null | "family" | "selected">(null);
  const scopedAdmins = useMemo(
    () =>
      activeFamilyTab === null
        ? admins
        : admins.filter((a) => (a.familyType || "ultra") === activeFamilyTab),
    [activeFamilyTab, admins],
  );
  const filtered = scopedAdmins;
  const bulk = useBulkSelection(filtered);
  const syncableCount = filtered.filter((admin) =>
    isSyncableAccountStatus(admin.accountStatus),
  ).length;
  const selectedSyncableIds = bulk.selectedItems
    .filter((admin) => isSyncableAccountStatus(admin.accountStatus))
    .map((admin) => admin.id);

  const handleConfirmSync = () => {
    if (confirmMode === "selected") {
      handleSyncSelected(selectedSyncableIds);
      bulk.clear();
    } else if (confirmMode === "family") {
      handleSyncAll(activeFamilyTab);
    }
    setConfirmMode(null);
  };

  if (initialLoading) {
    return (
      <>
        <div className="glass-card mb-6 animate-pulse rounded-2xl p-5">
          <div className="flex items-center justify-between">
            <div className="h-4 w-28 rounded bg-white/[0.06]" />
          </div>
        </div>
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          {Array.from({ length: 4 }).map((_, i) => (
            <div key={i} className="glass-card animate-pulse rounded-2xl p-8">
              <div className="mb-4 flex justify-center">
                <div className="h-12 w-12 rounded-2xl bg-white/[0.08]" />
              </div>
              <div className="mx-auto h-4 w-24 rounded bg-white/[0.06]" />
            </div>
          ))}
        </div>
      </>
    );
  }

  if (activeFamilyTab === null) {
    return (
      <>
        <div className="glass-card mb-4 rounded-2xl p-5">
          <h2 className="text-sm font-semibold text-slate-300">Quản lý Family</h2>
        </div>

        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          {FAMILY_TABS.map((tab, i) => {
            const Icon = tab.icon;
            const count = admins.filter(
              (a) => (a.familyType || "ultra") === tab.id,
            ).length;
            return (
              <motion.button
                key={tab.id}
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.25, delay: i * 0.06 }}
                onClick={() => setActiveFamilyTab(tab.id)}
                className="group family-tab"
              >
                <div
                  className={`flex h-14 w-14 items-center justify-center rounded-2xl border border-white/[0.08] bg-white/[0.04] transition-all duration-300 group-hover:scale-110 group-hover:border-white/[0.15] group-hover:bg-white/[0.08]`}
                >
                  <Icon
                    className={`h-7 w-7 ${tab.color} transition-transform duration-300 group-hover:scale-110`}
                  />
                </div>
                <div className="text-center">
                  <p className="text-sm font-bold text-white">{tab.label}</p>
                  <p className="mt-1 text-xs text-slate-500">
                    {count > 0 ? `${count} tài khoản` : "Chưa có tài khoản"}
                  </p>
                </div>
                {count > 0 && (
                  <span
                    className={`absolute right-3 top-3 rounded-full px-2 py-0.5 text-[10px] font-bold ${tab.activeClass}`}
                  >
                    {count}
                  </span>
                )}
              </motion.button>
            );
          })}
        </div>
      </>
    );
  }

  return (
    <>

      <div className="glass-card mb-6 rounded-2xl p-5">
        <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
          <div className="flex items-center gap-3">
            <button onClick={() => setActiveFamilyTab(null)} className="btn-back">
              <ArrowLeft className="h-4 w-4" />
            </button>
            <div>
              <h2 className="section-header text-sm">
                {(() => {
                  const tab = FAMILY_TABS.find((t) => t.id === activeFamilyTab);
                  if (!tab) return "Family";
                  const TabIcon = tab.icon;
                  return (
                    <>
                      <TabIcon className={`h-4 w-4 ${tab.color}`} />
                      {tab.label}
                    </>
                  );
                })()}
              </h2>
              <p className="text-[11px] text-slate-500">Quản lý tài khoản</p>
            </div>
          </div>
          <div className="flex items-center gap-3">
            <button
              onClick={() => setConfirmMode("family")}
              disabled={syncing || syncableCount === 0}
              className="group btn-emerald"
            >
              <RefreshCw
                className={`h-3.5 w-3.5 transition-transform duration-500 ${syncAllRef.current ? "animate-spin" : "group-hover:rotate-180"}`}
              />
              {syncAllRef.current ? "Đang quét..." : "Đồng bộ tất cả"}
            </button>
            <button onClick={() => setShowAddAdmin(true)} className="group btn-violet">
              <Plus className="h-3.5 w-3.5 transition-transform duration-200 group-hover:rotate-90" />
              Thêm Admin
            </button>
          </div>
        </div>
      </div>

      <div className="glass-card mb-6 rounded-2xl p-4">
        <div className="flex flex-col gap-2 text-xs text-slate-400 sm:flex-row sm:items-center sm:justify-between">
          <span>
            <span className="font-semibold text-slate-200">{filtered.length}</span> tài khoản
          </span>
          <div className="flex flex-wrap gap-2">
            <button
              type="button"
              onClick={bulk.allSelected ? bulk.clear : bulk.selectAll}
              disabled={filtered.length === 0}
              className="inline-flex min-h-9 items-center gap-1.5 rounded-lg border border-white/[0.08] bg-white/[0.03] px-3 font-semibold text-slate-300 transition-colors hover:bg-white/[0.07] disabled:opacity-50"
            >
              {bulk.allSelected ? <CheckSquare className="h-4 w-4" /> : <Square className="h-4 w-4" />}
              {bulk.allSelected ? "Bỏ chọn" : "Chọn tất cả"}
            </button>
            <button
              type="button"
              onClick={() => setConfirmMode("selected")}
              disabled={syncing || selectedSyncableIds.length === 0}
              className="inline-flex min-h-9 items-center gap-1.5 rounded-lg border border-emerald-500/25 bg-emerald-500/10 px-3 font-semibold text-emerald-300 transition-colors hover:bg-emerald-500/20 disabled:opacity-50"
            >
              <RefreshCw className="h-4 w-4" />
              Đồng bộ đã chọn ({selectedSyncableIds.length})
            </button>
          </div>
        </div>
      </div>

      {filtered.length === 0 ? (
        <div className="flex flex-col items-center justify-center py-24 text-slate-500">
          <Users className="mb-4 h-14 w-14 text-slate-600" />
          <p className="mb-1 text-lg font-semibold text-slate-400">
            Chưa có Admin Account
          </p>
          <p className="mb-6 text-sm text-slate-500">
            Thêm tài khoản {FAMILY_TABS.find((t) => t.id === activeFamilyTab)?.label} đầu
            tiên
          </p>
          <button
            onClick={() => setShowAddAdmin(true)}
            className="group flex items-center gap-2 rounded-2xl border border-violet-500/30 bg-violet-500/10 px-8 py-4 text-sm font-semibold text-violet-300 backdrop-blur-sm transition-all duration-200 hover:border-violet-400/50 hover:bg-violet-500/20 hover:text-white"
          >
            <Plus className="h-5 w-5 transition-transform duration-200 group-hover:rotate-90" />
            Thêm Admin
          </button>
        </div>
      ) : (
        <div className="grid grid-cols-1 gap-4 lg:grid-cols-2 xl:grid-cols-3">
          {filtered.map((admin) => (
            <div key={admin.id} className="group relative">
              <button
                type="button"
                onClick={() => bulk.toggle(admin.id)}
                aria-label={`Chọn ${admin.fullName || admin.email}`}
                className={`absolute -left-2 -top-2 z-20 flex h-8 w-8 items-center justify-center rounded-full border backdrop-blur-xl transition-all ${
                  bulk.selectedIds.has(admin.id)
                    ? "border-cyan-300/70 bg-cyan-400/20 text-cyan-100 shadow-[0_0_18px_rgba(34,211,238,0.22)]"
                    : "border-white/[0.12] bg-[#111827]/75 text-slate-500 shadow-lg hover:border-cyan-300/50 hover:bg-cyan-400/10 hover:text-cyan-100"
                }`}
              >
                <span
                  className={`flex h-4 w-4 items-center justify-center rounded-[5px] border transition-colors ${
                    bulk.selectedIds.has(admin.id)
                      ? "border-cyan-200 bg-cyan-300 text-[#0f1729]"
                      : "border-slate-400/70"
                  }`}
                >
                  {bulk.selectedIds.has(admin.id) && <Check className="h-3 w-3 stroke-[3]" />}
                </span>
              </button>
              <AdminCard
                admin={admin}
                globalSyncing={syncing}
                onSync={handleSyncAdmin}
                onClick={() => setSelectedAdminId(admin.id)}
              />
            </div>
          ))}
        </div>
      )}
      <ConfirmModal
        open={confirmMode !== null}
        title={confirmMode === "selected" ? "Đồng bộ các tài khoản đã chọn?" : "Đồng bộ toàn bộ family?"}
        description={
          confirmMode === "selected"
            ? `Sẽ đồng bộ ${selectedSyncableIds.length} tài khoản có thể đồng bộ trong danh sách hiện tại.`
            : `Sẽ đồng bộ ${syncableCount} tài khoản có thể đồng bộ trong family này.`
        }
        confirmText="Đồng bộ"
        cancelText="Hủy"
        loading={syncing}
        onCancel={() => setConfirmMode(null)}
        onConfirm={handleConfirmSync}
      />
    </>
  );
}
