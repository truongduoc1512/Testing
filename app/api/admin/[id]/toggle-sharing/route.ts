import { NextResponse } from "next/server";
import { prisma } from "@/lib/db";
import { apiError } from "@/lib/logger";
import { safeErrorMessage } from "@/lib/safe-error";
import { sharingLimiter } from "@/lib/rate-limit";
import { requireOwnedAdminAccess } from "@/lib/api/admin-route";
import {
  ensureGoogleOneSharingDisabled,
  ensureGoogleOneSharingEnabled,
  getGoogleAuthOrRefresh,
  getGoogleOneSharingState,
} from "@/lib/scanner/google-one";
import { formatAuditAdminLabel, writeAuditLog } from "@/lib/audit-log";

export const maxDuration = 60;

export async function GET(
  request: Request,
  { params }: { params: Promise<{ id: string }> },
) {
  try {
    const guard = await requireOwnedAdminAccess({
      request,
      params,
      action: "toggle-sharing-get",
      limiter: sharingLimiter,
    });
    if ("response" in guard) return guard.response;
    const { session, adminId } = guard;

    const adminAccount = await prisma.adminAccount.findFirst({
      where: { id: adminId, createdBy: session.sub },
    });
    if (!adminAccount)
      return NextResponse.json({ error: "Admin account not found" }, { status: 404 });

    const authData = await getGoogleAuthOrRefresh(
      adminAccount.id,
      adminAccount.email,
      adminAccount.googlePassword,
      adminAccount.totpSecret,
    );
    if (!authData) {
      return NextResponse.json(
        { error: "Session expired. Please sign in again." },
        { status: 400 },
      );
    }

    const state = await getGoogleOneSharingState(authData.cookies);
    return NextResponse.json({
      sharing: state.enabled,
      error: state.enabled ? "" : state.error ? "Unable to read sharing status." : "",
    });
  } catch (error: unknown) {
    apiError("/api/admin/[id]/toggle-sharing GET", error);
    return NextResponse.json(
      { error: safeErrorMessage(error, "Failed to read sharing status") },
      { status: 500 },
    );
  }
}

export async function POST(
  request: Request,
  { params }: { params: Promise<{ id: string }> },
) {
  try {
    const guard = await requireOwnedAdminAccess({
      request,
      params,
      action: "toggle-sharing-post",
      limiter: sharingLimiter,
    });
    if ("response" in guard) return guard.response;
    const { session, adminId } = guard;

    const body = await request.json();
    const enable = body.enable === true;

    const adminAccount = await prisma.adminAccount.findFirst({
      where: { id: adminId, createdBy: session.sub },
    });
    if (!adminAccount)
      return NextResponse.json({ error: "Admin account not found" }, { status: 404 });

    const authData = await getGoogleAuthOrRefresh(
      adminAccount.id,
      adminAccount.email,
      adminAccount.googlePassword,
      adminAccount.totpSecret,
      { allowRelogin: false },
    );
    if (!authData) {
      return NextResponse.json(
        { error: "Session expired. Please sign in again." },
        { status: 400 },
      );
    }

    if (enable) {
      const ensured = await ensureGoogleOneSharingEnabled(authData.cookies);
      if (!ensured.enabled) {
        apiError("/api/admin/[id]/toggle-sharing POST", new Error(ensured.error || "enable failed"));
        return NextResponse.json(
          { success: false, error: "Cannot enable sharing." },
          { status: 400 },
        );
      }
      await writeAuditLog({
        userId: session.sub,
        actorEmail: session.email,
        action: "profile.sharing.enable",
        targetType: "admin",
        targetId: adminId,
        status: "success",
        message: `Đã bật chia sẻ Google One cho admin ${formatAuditAdminLabel(adminAccount)}`,
        metadata: {
          adminEmail: adminAccount.email,
          adminName: adminAccount.displayName,
          familyType: adminAccount.familyType,
          changed: ensured.changed,
        },
      });
      return NextResponse.json({
        success: true,
        changed: ensured.changed,
      });
    }

    const ensured = await ensureGoogleOneSharingDisabled(authData.cookies);
    if (ensured.enabled) {
      apiError("/api/admin/[id]/toggle-sharing POST", new Error(ensured.error || "disable failed"));
      return NextResponse.json(
        { success: false, error: "Cannot disable sharing." },
        { status: 400 },
      );
    }
    await writeAuditLog({
      userId: session.sub,
      actorEmail: session.email,
      action: "profile.sharing.disable",
      targetType: "admin",
      targetId: adminId,
      status: "success",
      message: `Đã tắt chia sẻ Google One cho admin ${formatAuditAdminLabel(adminAccount)}`,
      metadata: {
        adminEmail: adminAccount.email,
        adminName: adminAccount.displayName,
        familyType: adminAccount.familyType,
        changed: ensured.changed,
      },
    });
    return NextResponse.json({
      success: true,
      changed: ensured.changed,
    });
  } catch (error: unknown) {
    apiError("/api/admin/[id]/toggle-sharing POST", error);
    return NextResponse.json(
      { error: safeErrorMessage(error, "Failed to update sharing state") },
      { status: 500 },
    );
  }
}
