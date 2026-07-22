import { NextResponse } from "next/server";
import { prisma } from "@/lib/db";
import { apiError } from "@/lib/logger";
import { safeErrorMessage } from "@/lib/safe-error";
import { sanitizeMemberStorageForClient } from "@/lib/member-storage-safe";
import { membersStorageLimiter } from "@/lib/rate-limit";
import { requireOwnedAdminAccess } from "@/lib/api/admin-route";
import {
  fetchFamilyMemberStorageUsage,
  getGoogleAuthOrRefresh,
} from "@/lib/scanner/google-one";

export const maxDuration = 90;

export async function GET(
  request: Request,
  { params }: { params: Promise<{ id: string }> },
) {
  try {
    const guard = await requireOwnedAdminAccess({
      request,
      params,
      action: "members-storage",
      limiter: membersStorageLimiter,
    });
    if ("response" in guard) return guard.response;
    const { session, adminId } = guard;

    const adminAccount = await prisma.adminAccount.findFirst({
      where: { id: adminId, createdBy: session.sub },
    });
    if (!adminAccount) {
      return NextResponse.json({ error: "Admin account not found" }, { status: 404 });
    }

    if (!["ultra", "pro", "youtube"].includes(adminAccount.familyType || "ultra")) {
      return NextResponse.json(
        { error: "This endpoint is only for Google family types." },
        { status: 400 },
      );
    }

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

    const usage = await fetchFamilyMemberStorageUsage(authData.cookies);
    const safeUsage = sanitizeMemberStorageForClient(
      usage,
      "Unable to read member storage.",
    );
    return NextResponse.json({
      success: !safeUsage.error,
      ...safeUsage,
    });
  } catch (error: unknown) {
    apiError("/api/admin/[id]/members-storage GET", error);
    return NextResponse.json(
      {
        error: safeErrorMessage(error, "Failed to fetch member storage"),
      },
      { status: 500 },
    );
  }
}
