import { NextResponse } from "next/server";
import { prisma } from "@/lib/db";
import { changeGoogleLanguage } from "@/lib/scanner/google-profile";
import { safeDecrypt } from "@/lib/crypto";
import { getSession } from "@/lib/auth/session";
import { apiError } from "@/lib/logger";
import { safeErrorMessage } from "@/lib/safe-error";
import { resolveOwnedAdminId } from "@/lib/admin-access";
import { buildRateLimitKey, profileLimiter, rateLimitResponse } from "@/lib/rate-limit";

export const maxDuration = 60;

export async function POST(
  request: Request,
  { params }: { params: Promise<{ id: string }> },
) {
  try {
    const session = await getSession();
    if (!session) return NextResponse.json({ error: "Unauthorized" }, { status: 401 });

    const { id: routeId } = await params;
    const adminId = await resolveOwnedAdminId(session.sub, routeId);
    if (!adminId) return NextResponse.json({ error: "Not found" }, { status: 404 });

    const key = buildRateLimitKey(request, session.sub, adminId, "profile-change-language");
    const limit = profileLimiter.check(key);
    if (!limit.allowed) return rateLimitResponse(limit.resetMs);

    const { languageCode } = await request.json();

    if (!languageCode || typeof languageCode !== "string") {
      return NextResponse.json(
        { success: false, message: "Missing languageCode" },
        { status: 400 },
      );
    }

    const adminAccount = await prisma.adminAccount.findFirst({
      where: { id: adminId, createdBy: session.sub },
      include: { session: true },
    });

    if (!adminAccount) {
      return NextResponse.json({ error: "Admin account not found" }, { status: 404 });
    }

    const ft = adminAccount.familyType || "ultra";
    if (ft === "gpt") {
      return NextResponse.json(
        { error: "This feature is only for Google accounts." },
        { status: 400 },
      );
    }

    const googleSession = adminAccount.session;
    if (!googleSession?.cookies) {
      return NextResponse.json(
        { error: "Session expired. Please sign in again." },
        { status: 401 },
      );
    }

    let cookies;
    try {
      const cookiesStr = safeDecrypt(googleSession.cookies);
      cookies = JSON.parse(cookiesStr);
    } catch (e) {
      apiError("/api/admin/[id]/profile/change-language", e);
      cookies = [];
    }

    if (!Array.isArray(cookies) || cookies.length === 0) {
      return NextResponse.json(
        { error: "Session expired. Please sign in again." },
        { status: 401 },
      );
    }

    const result = await changeGoogleLanguage(cookies, languageCode);
    if (result?.success) {
      return NextResponse.json({ success: true, message: "Language updated." });
    }
    apiError(
      "/api/admin/[id]/profile/change-language",
      new Error(result?.message || "change-language failed"),
    );
    return NextResponse.json(
      { success: false, message: "Failed to change language." },
      { status: 500 },
    );
  } catch (error: unknown) {
    apiError("/api/admin/[id]/profile/change-language", error);
    return NextResponse.json(
      {
        success: false,
        message: safeErrorMessage(error, "Failed to change language"),
      },
      { status: 500 },
    );
  }
}
