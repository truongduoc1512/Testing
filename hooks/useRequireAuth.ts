"use client";
import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";

interface UserData {
  email: string;
  fullName: string;
}

export function useRequireAuth(redirectTo = "/login") {
  const router = useRouter();
  const [user, setUser] = useState<UserData | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;

    fetch("/api/auth/me")
      .then((r) => {
        if (!r.ok) throw new Error("Unauthorized");
        return r.json();
      })
      .then((data) => {
        if (cancelled) return;
        setUser(data.user);
        setLoading(false);
      })
      .catch(() => {
        if (cancelled) return;
        setLoading(false);
        router.replace(redirectTo);
      });

    return () => {
      cancelled = true;
    };
  }, [redirectTo, router]);

  return { user, loading };
}
