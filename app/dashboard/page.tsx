"use client";
import DashboardPage from "@/components/dashboard/DashboardPage";
import { useRequireAuth } from "@/hooks";

export default function DashboardRoute() {
  const { user, loading } = useRequireAuth("/login");

  if (loading || !user) return <div className="fixed inset-0 bg-[#050510]" />;

  return (
    <DashboardPage
      user={{
        name: user.fullName,
        email: user.email,
      }}
    />
  );
}
