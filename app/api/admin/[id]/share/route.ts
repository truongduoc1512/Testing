import { NextResponse } from "next/server";
import { prisma } from "@/lib/db";
import {
  ensureGoogleOneSharingEnabled,
  getGoogleAuthOrRefresh,
  getGoogleOneSharingState,
} from "@/lib/scanner/google-one";
import { apiError } from "@/lib/logger";
import { safeErrorMessage } from "@/lib/safe-error";
import { sharingLimiter } from "@/lib/rate-limit";
import { requireOwnedAdminAccess } from "@/lib/api/admin-route";

export async function GET(
  request: Request,
  { params }: { params: Promise<{ id: string }> },
) {
  try {
    const guard = await requireOwnedAdminAccess({
      request,
      params,
      action: "share-get",
      limiter: sharingLimiter,
    });
    if ("response" in guard) return guard.response;
    const { session, adminId } = guard;

    const admin = await prisma.adminAccount.findFirst({
      where: { id: adminId, createdBy: session.sub },
    });
    if (!admin) return NextResponse.json({ error: "Not found" }, { status: 404 });
    if (!["ultra", "pro", "youtube"].includes(admin.familyType || "ultra")) {
      return NextResponse.json(
        { error: "This endpoint is only for Google family types." },
        { status: 400 },
      );
    }

    const authData = await getGoogleAuthOrRefresh(
      admin.id,
      admin.email,
      admin.googlePassword,
      admin.totpSecret,
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

    const state = await getGoogleOneSharingState(authData.cookies);
    return NextResponse.json({
      success: true,
      sharingEnabled: state.enabled,
      error: state.enabled ? null : state.error ? "Unable to read sharing status." : null,
    });
  } catch (e) {
    apiError("GET /api/admin/[id]/share", e);
    return NextResponse.json(
      { error: safeErrorMessage(e, "Server error") },
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
      action: "share-post",
      limiter: sharingLimiter,
    });
    if ("response" in guard) return guard.response;
    const { session, adminId } = guard;

    const admin = await prisma.adminAccount.findFirst({
      where: { id: adminId, createdBy: session.sub },
    });
    if (!admin) return NextResponse.json({ error: "Not found" }, { status: 404 });
    if (!["ultra", "pro", "youtube"].includes(admin.familyType || "ultra")) {
      return NextResponse.json(
        { error: "This endpoint is only for Google family types." },
        { status: 400 },
      );
    }

    const authData = await getGoogleAuthOrRefresh(
      admin.id,
      admin.email,
      admin.googlePassword,
      admin.totpSecret,
    );
    if (!authData) {
      return NextResponse.json(
        { error: "Session expired. Please sign in again." },
        { status: 400 },
      );
    }

    const ensured = await ensureGoogleOneSharingEnabled(authData.cookies);
    if (!ensured.enabled) {
      apiError("POST /api/admin/[id]/share", new Error(ensured.error || "share enable failed"));
      return NextResponse.json(
        { error: "Cannot enable sharing." },
        { status: 400 },
      );
    }

    return NextResponse.json({
      success: true,
      sharingEnabled: true,
      changed: ensured.changed,
    });
  } catch (e) {
    apiError("POST /api/admin/[id]/share", e);
    return NextResponse.json(
      { error: safeErrorMessage(e, "Server error") },
      { status: 500 },
    );
  }
}
