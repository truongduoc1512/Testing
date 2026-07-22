"use client";
import { useState, useCallback, useEffect, useRef } from "react";
import AddAdminModal from "./AddAdminModal";
import type { AdminFormData } from "./AddAdminModal";
import AdminDetailModal from "./AdminDetailModal";
import SettingsPanel from "./SettingsPanel";
import SyncToast from "./SyncToast";
import { useDashboardData } from "@/hooks/useDashboardData";
import type { SummaryCard } from "@/hooks/useDashboardData";
import { useToast } from "@/hooks/useToast";
import { notifyDashboardDataChanged } from "@/lib/browser/dashboard-events";
import { Users, DollarSign, HardDrive } from "lucide-react";

import DashboardSidebar from "./DashboardSidebar";
import DashboardTopbar from "./DashboardTopbar";
import DashboardTab from "./DashboardTab";
import FamilyTab from "./FamilyTab";
import FamilyReportTab from "./FamilyReportTab";
import CheckProfileTab from "./CheckProfileTab";

interface DashboardUser {
  name: string;
  email: string;
}

export default function DashboardPage({ user }: { user: DashboardUser }) {
  const [activeNav, setActiveNav] = useState(() => {
    if (typeof window === "undefined") return "dashboard";
    const params = new URLSearchParams(window.location.search);
    const tab = params.get("tab");
    if (tab === "family" || tab === "report" || tab === "settings" || tab === "profile") {
      return tab;
    }
    return "dashboard";
  });
  const [sidebarOpen, setSidebarOpen] = useState(false);

  const buildCards = useCallback(
    (s: {
      farmAccounts: number;
      totalMembers: number;
      totalCredit: number;
      remainingCredit: number;
      totalStorage: string;
      usedStorage: string;
    }): SummaryCard[] => [
      {
        id: "farm",
        label: "Tài khoản Farm",
        value: String(s.farmAccounts),
        sub: "tài khoản đang quản lý",
        borderColor: "border-t-violet-500",
        valueColor: "text-violet-400",
        icon: <Users className="h-4 w-4" />,
      },
      {
        id: "members",
        label: "Tổng thành viên",
        value: String(s.totalMembers),
        sub: "trong tất cả nhóm",
        borderColor: "border-t-blue-500",
        valueColor: "text-blue-400",
        icon: <Users className="h-4 w-4" />,
      },
      {
        id: "credit",
        label: "Tổng Credit còn lại",
        value: String(s.remainingCredit),
        sub: `Đã dùng ${Math.max(0, s.totalCredit - s.remainingCredit).toLocaleString("vi-VN")} / ${s.totalCredit.toLocaleString("vi-VN")}`,
        borderColor: "border-t-emerald-500",
        valueColor: "text-emerald-400",
        icon: <DollarSign className="h-4 w-4" />,
      },
      {
        id: "storage",
        label: "Tổng Bộ nhớ",
        value: s.totalStorage,
        sub: `Đã dùng ${s.usedStorage}`,
        borderColor: "border-t-amber-500",
        valueColor: "text-amber-400",
        icon: <HardDrive className="h-4 w-4" />,
      },
    ],
    [],
  );

  const {
    cards,
    admins,
    syncing,
    syncAllRef,
    creditActivities,
    creditLogs,
    initialLoading,
    globalToast,
    fetchStats,
    handleSyncAdmin,
    handleSyncAll,
    handleSyncSelected,
    nextAutoSync,
  } = useDashboardData(buildCards);
  const { toast: addAdminToast, show: showAddAdminToast } = useToast();

  const [showAddAdmin, setShowAddAdmin] = useState(false);
  const [selectedAdminId, setSelectedAdminId] = useState<string | null>(null);
  const [activeFamilyTab, setActiveFamilyTab] = useState<string | null>(null);
  const adminDetailNeedsRefreshRef = useRef(false);

  useEffect(() => {
    if (typeof window !== "undefined") {
      const url = new URL(window.location.href);
      if (activeNav !== "dashboard" || url.searchParams.has("tab")) {
        url.searchParams.set("tab", activeNav);
        window.history.replaceState({}, "", url.toString());
      }
    }
  }, [activeNav]);

  useEffect(() => {
    if (activeNav === "dashboard" || activeNav === "family" || activeNav === "report") {
      fetchStats();
    }
  }, [activeNav, fetchStats]);

  const handleNavChange = (id: string) => {
    setActiveNav(id);
    setSidebarOpen(false);
    setSelectedAdminId(null);
    setActiveFamilyTab(null);
    adminDetailNeedsRefreshRef.current = false;
  };

  const markAdminDetailRefreshNeeded = useCallback(() => {
    adminDetailNeedsRefreshRef.current = true;
  }, []);

  const handleCloseAdminDetail = useCallback(() => {
    setSelectedAdminId(null);
    if (adminDetailNeedsRefreshRef.current) {
      adminDetailNeedsRefreshRef.current = false;
      void fetchStats();
    }
  }, [fetchStats]);

  return (
    <div className="auth-bg relative flex min-h-screen text-slate-300 antialiased selection:bg-violet-500/30">

      <div className="pointer-events-none fixed inset-0 z-0 overflow-hidden">
        <div className="orb orb-1" />
        <div className="orb orb-2" />
        <div className="orb orb-3" />
      </div>

      {sidebarOpen && (
        <div
          className="fixed inset-0 z-40 bg-black/60 backdrop-blur-sm md:hidden"
          onClick={() => setSidebarOpen(false)}
        />
      )}

      <DashboardSidebar
        activeNav={activeNav}
        sidebarOpen={sidebarOpen}
        onNavChange={handleNavChange}
        onClose={() => setSidebarOpen(false)}
      />

      <main className="relative z-10 flex min-w-0 flex-1 flex-col">
        <div className="mx-auto w-full max-w-7xl flex-1 px-4 py-6 md:px-8">
          <DashboardTopbar
            user={user}
            activeNav={activeNav}
            nextAutoSync={nextAutoSync}
            admins={admins}
            onOpenSidebar={() => setSidebarOpen(true)}
            onSelectAdmin={setSelectedAdminId}
          />

          <div className="mt-8">
            {activeNav === "dashboard" && (
              <DashboardTab
                cards={cards}
                initialLoading={initialLoading}
                admins={admins}
                creditLogs={creditLogs}
                creditActivities={creditActivities}
                goToFamily={() => handleNavChange("family")}
                setSelectedAdminId={setSelectedAdminId}
                onAddAdmin={(familyType) => {
                  setActiveFamilyTab(familyType);
                  setShowAddAdmin(true);
                }}
              />
            )}

            {activeNav === "family" && (
              <FamilyTab
                initialLoading={initialLoading}
                activeFamilyTab={activeFamilyTab}
                setActiveFamilyTab={setActiveFamilyTab}
                admins={admins}
                syncing={syncing}
                syncAllRef={syncAllRef}
                handleSyncAll={(type) => handleSyncAll(type || undefined)}
                handleSyncSelected={handleSyncSelected}
                handleSyncAdmin={handleSyncAdmin}
                setShowAddAdmin={setShowAddAdmin}
                setSelectedAdminId={setSelectedAdminId}
              />
            )}

            {activeNav === "report" && (
              <FamilyReportTab
                admins={admins}
                initialLoading={initialLoading}
                onAdminClick={setSelectedAdminId}
              />
            )}

            {activeNav === "profile" && <CheckProfileTab admins={admins} />}

            {activeNav === "settings" && <SettingsPanel />}
          </div>
        </div>
      </main>

      {showAddAdmin && (
        <AddAdminModal
          onClose={() => setShowAddAdmin(false)}
          onSubmit={async (data: AdminFormData) => {
            try {
              const res = await fetch("/api/admin", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(data),
              });
              if (!res.ok) {
                const err = await res.json().catch(() => ({}));
                if (err.fields) {
                  const details = Object.entries(err.fields)
                    .map(([k, v]) => `${k}: ${v}`)
                    .join("\n");
                  throw new Error(details);
                }
                throw new Error(err.error || "Lỗi tạo admin");
              }
              setShowAddAdmin(false);
              await fetchStats();
              notifyDashboardDataChanged("admin-add");
            } catch (e: unknown) {
              showAddAdminToast(e instanceof Error ? e.message : "Lỗi tạo admin", "error");
            }
          }}
          forcedFamilyType={activeFamilyTab || "ultra"}
        />
      )}

      {selectedAdminId && (
        <AdminDetailModal
          adminId={selectedAdminId}
          onClose={handleCloseAdminDetail}
          onRefresh={() => fetchStats()}
          onBackgroundRefreshNeeded={markAdminDetailRefreshNeeded}
          onSyncStart={handleSyncAdmin}
          globalSyncing={syncing}
        />
      )}

      <SyncToast log={globalToast || addAdminToast} />
    </div>
  );
}
