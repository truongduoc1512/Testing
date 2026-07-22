"use client";
import { motion, AnimatePresence } from "framer-motion";
import { ChevronRight, ChevronDown, LogOut, Menu, Search, X } from "lucide-react";
import { useState, useRef, useCallback, useMemo } from "react";
import LiveClock from "./LiveClock";
import { useClickOutside } from "@/hooks/useClickOutside";
import { navItems } from "./DashboardSidebar";
import type { AdminRow } from "@/hooks/useDashboardData";
import { findMatchedAdminMember, searchAdminsByQuery } from "@/lib/utils";

interface User {
  name: string;
  email: string;
}

interface Props {
  user: User;
  activeNav: string;
  nextAutoSync: Date | null;
  admins: AdminRow[];
  onOpenSidebar: () => void;
  onSelectAdmin: (id: string) => void;
}

export default function DashboardTopbar({
  user,
  activeNav,
  nextAutoSync,
  admins,
  onOpenSidebar,
  onSelectAdmin,
}: Props) {
  const [showDropdown, setShowDropdown] = useState(false);
  const [searchQuery, setSearchQuery] = useState("");
  const [mobileSearchOpen, setMobileSearchOpen] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);
  const searchRef = useRef<HTMLDivElement>(null);

  useClickOutside(dropdownRef, () => setShowDropdown(false));
  useClickOutside(searchRef, () => {
    setSearchQuery("");
    setMobileSearchOpen(false);
  });

  const handleLogout = useCallback(async () => {
    await fetch("/api/auth/logout", { method: "POST" });
    window.location.href = "/login";
  }, []);

  const initials = user.name
    .split(" ")
    .map((w) => w[0])
    .join("")
    .toUpperCase()
    .slice(0, 2);
  const searchResults = useMemo(() => {
    const query = searchQuery.trim();
    if (!query) return [];
    return searchAdminsByQuery(admins, query, 8) as AdminRow[];
  }, [admins, searchQuery]);
  const searchOpen = searchQuery.trim().length > 0;

  return (
    <header className="relative z-30 mb-5 grid grid-cols-1 gap-3 xl:grid-cols-[minmax(120px,0.5fr)_minmax(220px,360px)_minmax(0,1.5fr)] xl:items-center 2xl:grid-cols-[minmax(140px,0.65fr)_minmax(280px,420px)_minmax(0,1.35fr)]">
      <div className="flex min-w-0 items-center justify-between gap-3 xl:contents">
        <div className="flex min-w-0 items-center gap-3 xl:col-start-1 xl:row-start-1">
          <button
            className="shrink-0 rounded-lg p-1.5 text-slate-400 hover:bg-white/5 hover:text-white md:hidden"
            onClick={onOpenSidebar}
            aria-label="Mở menu"
          >
            <Menu className="h-5 w-5" />
          </button>

          <div className="flex min-w-0 items-center gap-2 text-sm">
            <span className="shrink-0 text-slate-500">Home</span>
            <ChevronRight className="h-3.5 w-3.5 shrink-0 text-slate-600" />
            <motion.span
              key={activeNav}
              initial={{ opacity: 0, y: -8 }}
              animate={{ opacity: 1, y: 0 }}
              className="truncate whitespace-nowrap font-semibold capitalize text-white"
            >
              {navItems.find((n) => n.id === activeNav)?.label ?? activeNav}
            </motion.span>
          </div>
        </div>

        <div className="flex min-w-0 items-center justify-end gap-2 xl:col-start-3 xl:row-start-1">
          <button
            type="button"
            onMouseDown={(event) => event.stopPropagation()}
            onClick={() => setMobileSearchOpen((open) => !open)}
            aria-label={mobileSearchOpen ? "Đóng tìm kiếm" : "Mở tìm kiếm"}
            className={`flex h-9 w-9 shrink-0 items-center justify-center rounded-xl border transition-colors xl:hidden ${
              mobileSearchOpen
                ? "border-cyan-300/45 bg-cyan-400/15 text-cyan-100 shadow-[0_0_18px_rgba(34,211,238,0.16)]"
                : "border-white/[0.08] bg-white/[0.04] text-slate-400 hover:border-cyan-300/35 hover:bg-cyan-400/10 hover:text-cyan-100"
            }`}
          >
            <Search className="h-4 w-4" />
          </button>

          <LiveClock nextAutoSync={nextAutoSync} />

          <div className="relative min-w-0 shrink" ref={dropdownRef}>
            <button
              onClick={() => setShowDropdown((p) => !p)}
              aria-label="Mở menu tài khoản"
              className="flex max-w-full min-w-0 items-center gap-2 rounded-lg p-1.5 hover:bg-white/5"
            >
              <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-gradient-to-br from-violet-500 to-cyan-500 text-xs font-bold text-white">
                {initials}
              </div>
              <span className="hidden min-w-0 max-w-[110px] truncate whitespace-nowrap text-sm font-medium text-white sm:inline 2xl:max-w-[190px]">
                {user.name}
              </span>
              <ChevronDown className="hidden h-4 w-4 shrink-0 text-slate-400 sm:inline" />
            </button>

            <AnimatePresence>
              {showDropdown && (
                <motion.div
                  initial={{ opacity: 0, scale: 0.95, y: -4 }}
                  animate={{ opacity: 1, scale: 1, y: 0 }}
                  exit={{ opacity: 0, scale: 0.95, y: -4 }}
                  transition={{ duration: 0.15 }}
                  className="absolute right-0 top-12 z-50 w-56 overflow-hidden rounded-xl border border-white/[0.14] bg-[#1a2747]/95 shadow-[0_20px_50px_rgba(0,0,0,0.45)] ring-1 ring-cyan-400/10 backdrop-blur-2xl"
                >
                  <div className="border-b border-white/[0.12] px-4 py-3">
                    <p className="text-sm font-semibold text-white">{user.name}</p>
                    <p className="text-xs text-slate-300">{user.email}</p>
                  </div>
                  <div className="p-1.5">
                    <button
                      onClick={handleLogout}
                      type="button"
                      className="flex w-full items-center gap-2 rounded-lg px-3 py-2 text-sm text-red-300 hover:bg-red-500/15"
                    >
                      <LogOut className="h-4 w-4" />
                      Đăng xuất
                    </button>
                  </div>
                </motion.div>
              )}
            </AnimatePresence>
          </div>
        </div>
      </div>

      <div
        ref={searchRef}
        className={`relative w-full min-w-0 xl:col-start-2 xl:row-start-1 xl:block xl:max-w-[360px] xl:justify-self-center 2xl:max-w-[420px] ${
          mobileSearchOpen ? "block" : "hidden"
        }`}
      >
        <label className="relative block">
          <span className="sr-only">Tìm nhanh admin hoặc member</span>
          <Search className="pointer-events-none absolute left-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-cyan-300/70" />
          <input
            value={searchQuery}
            onChange={(event) => setSearchQuery(event.target.value)}
            className="h-11 w-full rounded-xl border border-white/[0.08] bg-white/[0.035] px-10 text-sm font-medium text-slate-100 outline-none transition-colors placeholder:text-slate-500 focus:border-cyan-400/35 focus:bg-white/[0.055] focus:shadow-[0_0_0_3px_rgba(34,211,238,0.08)]"
            placeholder="Tìm admin, member hoặc email..."
          />
          {(searchQuery || mobileSearchOpen) && (
            <button
              type="button"
              onClick={() => {
                setSearchQuery("");
                setMobileSearchOpen(false);
              }}
              aria-label="Xóa tìm kiếm"
              className="absolute right-2.5 top-1/2 flex h-7 w-7 -translate-y-1/2 items-center justify-center rounded-lg text-slate-500 transition-colors hover:bg-white/[0.06] hover:text-white"
            >
              <X className="h-3.5 w-3.5" />
            </button>
          )}
        </label>

        <AnimatePresence>
          {searchOpen && (
            <motion.div
              initial={{ opacity: 0, y: -4, scale: 0.98 }}
              animate={{ opacity: 1, y: 0, scale: 1 }}
              exit={{ opacity: 0, y: -4, scale: 0.98 }}
              transition={{ duration: 0.14 }}
              className="absolute left-0 right-0 top-[calc(100%+8px)] z-50 overflow-hidden rounded-xl border border-white/[0.12] bg-[#111827]/95 shadow-2xl backdrop-blur-2xl"
            >
              {searchResults.length === 0 ? (
                <div className="px-4 py-5 text-center text-sm text-slate-500">
                  Không tìm thấy admin phù hợp.
                </div>
              ) : (
                <div className="max-h-80 overflow-y-auto p-1.5">
                  {searchResults.map((admin) => {
                    const matchedMember = findMatchedAdminMember(admin, searchQuery);
                    return (
                      <button
                        key={admin.id}
                        type="button"
                        onClick={() => {
                          onSelectAdmin(admin.id);
                          setSearchQuery("");
                          setMobileSearchOpen(false);
                        }}
                        className="flex w-full items-center gap-3 rounded-lg px-3 py-2.5 text-left transition-colors hover:bg-white/[0.06]"
                      >
                        <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg bg-gradient-to-br from-violet-500/70 to-cyan-500/70 text-xs font-bold text-white">
                          {(matchedMember?.name || admin.fullName || admin.email).charAt(0).toUpperCase()}
                        </div>
                        <div className="min-w-0 flex-1">
                          <p className="truncate text-sm font-semibold text-white">
                            {matchedMember?.name || admin.fullName || admin.email}
                          </p>
                          <p className="truncate text-xs text-slate-400">
                            {matchedMember
                              ? `${matchedMember.email} · chủ fam ${admin.email}`
                              : admin.email}
                          </p>
                        </div>
                        <span className="shrink-0 rounded-full border border-white/[0.08] bg-white/[0.04] px-2 py-0.5 text-[10px] font-semibold text-slate-300">
                          {matchedMember ? "member" : admin.familyType || "ultra"}
                        </span>
                      </button>
                    );
                  })}
                </div>
              )}
            </motion.div>
          )}
        </AnimatePresence>
      </div>
    </header>
  );
}
