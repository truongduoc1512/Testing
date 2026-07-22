"use client";
import { LayoutDashboard, ListFilter, Users, Settings, X, UserCircle } from "lucide-react";

interface Props {
  activeNav: string;
  sidebarOpen: boolean;
  onNavChange: (id: string) => void;
  onClose: () => void;
}

export const navItems = [
  { id: "dashboard", label: "Dashboard", icon: LayoutDashboard },
  { id: "family", label: "Family Management", icon: Users },
  { id: "report", label: "Phân loại Fam", icon: ListFilter },
  { id: "profile", label: "Check Profile", icon: UserCircle },
  { id: "settings", label: "Settings", icon: Settings },
] as const;

export default function DashboardSidebar({
  activeNav,
  sidebarOpen,
  onNavChange,
  onClose,
}: Props) {
  return (
    <aside
      className={`
                glass-card fixed inset-y-0 left-0 z-50 flex w-[220px] flex-col
                rounded-none border-r border-white/[0.06]
                transition-transform duration-300
                md:relative md:translate-x-0
                lg:w-[220px] md:w-[72px]
                ${sidebarOpen ? "translate-x-0" : "-translate-x-full"}
            `}
    >

      <div className="flex h-16 items-center gap-3 border-b border-white/[0.06] px-4">
        <div className="flex h-9 w-9 shrink-0 items-center justify-center overflow-hidden">
          <img
            src="/logo.svg"
            alt="VieShop"
            className="h-full w-full scale-[1.8] object-contain"
          />
        </div>
        <span className="kinetic-text text-lg font-bold tracking-tight md:hidden lg:inline">
          VieShop
        </span>
        <button
          className="ml-auto text-slate-400 md:hidden"
          onClick={onClose}
          aria-label="Đóng menu"
        >
          <X className="h-5 w-5" />
        </button>
      </div>

      <nav className="flex-1 space-y-1 px-3 py-4">
        {navItems.map((item) => {
          const Icon = item.icon;
          const active = activeNav === item.id;
          return (
            <button
              key={item.id}
              onClick={() => onNavChange(item.id)}
              title={item.label}
              className={`group nav-tab ${active ? "nav-tab-active" : ""}`}
            >
              <Icon className={`h-5 w-5 shrink-0 ${active ? "text-cyan-400" : ""}`} />
              <span className="md:hidden lg:inline">{item.label}</span>
            </button>
          );
        })}
      </nav>
    </aside>
  );
}
