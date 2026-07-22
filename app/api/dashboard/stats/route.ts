import { NextRequest, NextResponse } from "next/server";
import { prisma } from "@/lib/db";
import { getSession } from "@/lib/auth/session";
import { hashKey, encryptValue, decryptValue } from "@/lib/crypto";
import { adminPublicIdFromInternal } from "@/lib/admin-access";
import { daysUntil, formatStorage, formatStorageGB, isGoogleType } from "@/lib/utils";
import { apiError } from "@/lib/logger";
import { safeErrorMessage } from "@/lib/safe-error";
import { NEAR_EXPIRY_DAYS } from "@/lib/constants/status";

const CK = hashKey("creditActivities");

type DashboardMemberForExpiry = {
  email: string;
  name: string;
  role: string;
  status: string;
  googleUserId: string | null;
  endDate: Date | null;
  renewed: boolean;
};

function isOwnerMember(member: Pick<DashboardMemberForExpiry, "role">) {
  return member.role.includes("MANAGER");
}

function resolveMemberExpiryCategory(member: DashboardMemberForExpiry) {
  if (member.status === "invited") {
    return { category: "invited" as const, daysLeft: null };
  }

  if (isOwnerMember(member)) {
    return { category: "owner" as const, daysLeft: null };
  }

  if (!member.endDate) {
    return member.renewed
      ? { category: "valid" as const, daysLeft: null }
      : { category: "noExpiry" as const, daysLeft: null };
  }

  const daysLeft = daysUntil(member.endDate);
  if (!Number.isFinite(daysLeft)) {
    return { category: "noExpiry" as const, daysLeft: null };
  }

  if (daysLeft <= 0) return { category: "expired" as const, daysLeft };
  if (daysLeft <= NEAR_EXPIRY_DAYS) return { category: "nearExpiry" as const, daysLeft };
  return { category: "valid" as const, daysLeft };
}

function buildMemberExpirySummary(members: DashboardMemberForExpiry[]) {
  const summary = {
    valid: 0,
    nearExpiry: 0,
    expired: 0,
    renewed: 0,
    invited: 0,
    manager: 0,
    noExpiry: 0,
    tracked: 0,
  };

  members.forEach((member) => {
    const resolved = resolveMemberExpiryCategory(member);
    if (resolved.category === "owner") {
      summary.manager += 1;
      return;
    }

    if (resolved.category === "invited") {
      summary.invited += 1;
      return;
    }

    summary.tracked += 1;
    if (member.renewed) summary.renewed += 1;
    summary[resolved.category] += 1;
  });

  return summary;
}

function buildMemberReports(members: DashboardMemberForExpiry[]) {
  return members.flatMap((member) => {
    const resolved = resolveMemberExpiryCategory(member);
    if (resolved.category === "owner") return [];

    return {
      key: `member_${hashKey(`${member.googleUserId || ""}:${member.email}`).slice(0, 16)}`,
      email: member.email,
      name: member.name,
      role: member.role,
      status: member.status,
      endDate: member.endDate ?? null,
      renewed: member.renewed,
      category: resolved.category,
      daysLeft: resolved.daysLeft,
    };
  });
}

export async function GET() {
  try {
    const session = await getSession();
    if (!session) {
      return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
    }

    const admins = await prisma.adminAccount.findMany({
      where: { createdBy: session.sub },
      orderBy: { createdAt: "desc" },
      include: {
        members: {
          where: { status: { not: "removed" } },
          select: {
            role: true,
            status: true,
            email: true,
            name: true,
            googleUserId: true,
            endDate: true,
            renewed: true,
          },
        },
      },
    });

    const farmAccounts = admins.length;
    const totalMembers = admins.reduce((s, a) => s + a.memberCount, 0);
    const ultraAdmins = admins.filter((a) => (a.familyType || "ultra") === "ultra");
    const googleAdmins = admins.filter((a) => isGoogleType(a.familyType));
    const totalCredit = ultraAdmins.reduce((s, a) => s + a.monthlyCredit, 0);
    const remainingCredit = ultraAdmins.reduce((s, a) => s + a.remainingCredit, 0);
    const parsePlanToGB = (plan: string, fallbackTB: number): number => {
      if (!plan) return fallbackTB * 1024;
      const m = plan.match(/([\d.]+)\s*(TB|GB)/i);
      if (!m) return fallbackTB * 1024;
      const val = parseFloat(m[1]);
      return m[2].toUpperCase() === "TB" ? val * 1024 : val;
    };
    const totalStorageGB = googleAdmins.reduce(
      (s, a) => s + parsePlanToGB(a.planName, a.storageTB),
      0,
    );
    const usedStorageMB = googleAdmins.reduce((s, a) => s + a.usedStorageMB, 0);

    const adminIds = admins.map((a) => a.id);
    const creditLogs =
      adminIds.length > 0
        ? await prisma.creditLog.findMany({
            where: { adminId: { in: adminIds } },
            orderBy: { createdAt: "desc" },
            take: 50,
            include: { admin: { select: { email: true, displayName: true } } },
          })
        : [];

    let cachedActivities: unknown = null;
    const cached = await prisma.cacheStorage.findUnique({
      where: { userId_key: { userId: session.sub, key: CK } },
    });
    if (cached) {
      try {
        cachedActivities = JSON.parse(decryptValue(cached.value));
      } catch (e) {
        apiError("GET /api/dashboard/stats", e, { context: "cache decrypt" });
      }
    }

    return NextResponse.json({
      summary: {
        farmAccounts,
        totalMembers,
        totalCredit,
        remainingCredit,
        totalStorage: formatStorageGB(totalStorageGB),
        usedStorage: formatStorage(usedStorageMB),
      },
      admins: admins.map((a) => ({
        id: adminPublicIdFromInternal(a.id),
        email: a.email,
        fullName: a.displayName,
        role: "admin",
        memberCount: a.memberCount,
        remainingCredit: a.remainingCredit,
        monthlyCredit: a.monthlyCredit,
        usedStorageMB: a.usedStorageMB,
        storageTB: a.storageTB,
        planName: a.planName,
        planExpiresAt: a.planExpiresAt ?? null,
        has2FA: a.has2FA,
        familyType: a.familyType ?? "ultra",
        lastSyncAt: a.lastSyncAt ?? null,
        lastSyncStatus: a.lastSyncStatus ?? "",
        lastSyncError: a.lastSyncError ?? "",
        accountStatus: a.accountStatus ?? "",
        profileData: a.profileData || "",
        memberExpiry: buildMemberExpirySummary(a.members),
        memberReports: buildMemberReports(a.members),
      })),
      creditLogs: creditLogs.map((l) => ({
        id: `log_${hashKey(l.id).slice(0, 16)}`,
        adminEmail: l.admin.email,
        adminName: l.admin.displayName,
        memberName: l.memberName,
        creditBefore: l.creditBefore,
        creditAfter: l.creditAfter,
        diff: l.diff,
        createdAt: l.createdAt,
      })),
      _c: cachedActivities,
    });
  } catch (e) {
    apiError("GET /api/dashboard/stats", e);
    return NextResponse.json(
      { error: safeErrorMessage(e, "Failed to load dashboard data") },
      { status: 500 },
    );
  }
}

export async function POST(request: NextRequest) {
  try {
    const session = await getSession();
    if (!session) return NextResponse.json({ error: "Unauthorized" }, { status: 401 });

    const { _c } = await request.json();
    if (!_c) return NextResponse.json({ ok: true });

    const encVal = encryptValue(typeof _c === "string" ? _c : JSON.stringify(_c));

    await prisma.cacheStorage.upsert({
      where: { userId_key: { userId: session.sub, key: CK } },
      update: { value: encVal },
      create: { userId: session.sub, key: CK, value: encVal },
    });

    return NextResponse.json({ ok: true });
  } catch (e) {
    apiError("/api/dashboard/stats", e);
    return NextResponse.json(
      { error: safeErrorMessage(e, "Server error") },
      { status: 500 },
    );
  }
}
