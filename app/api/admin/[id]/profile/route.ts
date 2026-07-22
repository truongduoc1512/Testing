import { NextResponse } from "next/server";
import { prisma } from "@/lib/db";
import { scanFullProfile } from "@/lib/scanner/google-profile";
import {
  fetchFamilyMemberStorageUsage,
  getGoogleAuthOrRefresh,
  getGoogleOneSharingState,
} from "@/lib/scanner/google-one";
import { apiError } from "@/lib/logger";
import { safeErrorMessage } from "@/lib/safe-error";
import { sanitizeMemberStorageForClient } from "@/lib/member-storage-safe";
import { profileLimiter } from "@/lib/rate-limit";
import { requireOwnedAdminAccess } from "@/lib/api/admin-route";
import { formatAuditAdminLabel, writeAuditLog } from "@/lib/audit-log";

export const maxDuration = 120;

export async function GET(
  request: Request,
  { params }: { params: Promise<{ id: string }> },
) {
  try {
    const guard = await requireOwnedAdminAccess({
      request,
      params,
      action: "profile",
      limiter: profileLimiter,
    });
    if ("response" in guard) return guard.response;
    const { session, adminId } = guard;

    const adminAccount = await prisma.adminAccount.findFirst({
      where: { id: adminId, createdBy: session.sub },
    });

    if (!adminAccount) {
      return NextResponse.json({ error: "Admin account not found" }, { status: 404 });
    }

    const ft = adminAccount.familyType || "ultra";
    if (!["ultra", "pro", "youtube"].includes(ft)) {
      return NextResponse.json(
        { error: "This feature is only for Google accounts." },
        { status: 400 },
      );
    }
    const shouldCheckSharing = ft !== "youtube";

    const authData = await getGoogleAuthOrRefresh(
      adminAccount.id,
      adminAccount.email,
      adminAccount.googlePassword,
      adminAccount.totpSecret,
    );
    if (!authData) {
      return NextResponse.json(
        {
          error: safeErrorMessage(
            "Session expired and cannot re-login",
            "Session expired. Please sign in again.",
          ),
        },
        { status: 400 },
      );
    }

    const [profileData, memberStorage, sharingState] = await Promise.all([
      scanFullProfile(authData.cookies, ft),
      fetchFamilyMemberStorageUsage(authData.cookies),
      shouldCheckSharing
        ? getGoogleOneSharingState(authData.cookies)
        : Promise.resolve({ enabled: null, error: null }),
    ]);
    const safeMemberStorage = sanitizeMemberStorageForClient(
      memberStorage,
      "Unable to read member storage.",
    );
    const safeProfileErrors = Array.isArray(profileData.errors)
      ? profileData.errors
          .map((error) => safeErrorMessage(error, "Profile scan issue detected."))
          .filter((error) => Boolean(error))
      : [];
    const profileSnapshot = {
      ...profileData,
      errors: safeProfileErrors,
      memberStorage: safeMemberStorage,
      sharingStatus: shouldCheckSharing ? sharingState.enabled : null,
      sharingError:
        shouldCheckSharing && sharingState.error
          ? safeErrorMessage(sharingState.error, "Unable to read sharing status.")
          : null,
      checkedAt: new Date().toISOString(),
    };
    const usedStorageMB =
      typeof safeMemberStorage.totalUsedBytes === "number" &&
      Number.isFinite(safeMemberStorage.totalUsedBytes)
        ? Math.max(0, Math.round(safeMemberStorage.totalUsedBytes / (1024 * 1024)))
        : null;

    try {
      await prisma.adminAccount.update({
        where: { id: adminId },
        data: {
          profileData: JSON.stringify(profileSnapshot),
          ...(usedStorageMB !== null ? { usedStorageMB } : {}),
        },
      });
    } catch (e) {
      apiError("/api/admin/[id]/profile", e);
    }

    await writeAuditLog({
      userId: session.sub,
      actorEmail: session.email,
      action: "profile.check",
      targetType: "admin",
      targetId: adminId,
      status: safeProfileErrors.length > 0 ? "partial" : "success",
      message: `Đã kiểm tra hồ sơ Google của admin ${formatAuditAdminLabel(adminAccount)}`,
      metadata: {
        adminEmail: adminAccount.email,
        adminName: adminAccount.displayName,
        familyType: ft,
        hasProfileError: safeProfileErrors.length > 0,
        usedStorageUpdated: usedStorageMB !== null,
      },
    });

    return NextResponse.json({
      success: true,
      email: adminAccount.email,
      ...profileSnapshot,
    });
  } catch (error: unknown) {
    apiError("/api/admin/[id]/profile", error);
    return NextResponse.json(
      { error: safeErrorMessage(error, "Failed to check profile") },
      { status: 500 },
    );
  }
}
