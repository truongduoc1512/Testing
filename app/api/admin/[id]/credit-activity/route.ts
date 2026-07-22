import { NextResponse } from "next/server";
import { prisma } from "@/lib/db";
import { getSession } from "@/lib/auth/session";
import { fetchCreditActivity } from "@/lib/scanner/credit-activity";
import type { Cookie } from "@/lib/scanner/google-http";
import { safeDecrypt } from "@/lib/crypto";
import { apiError } from "@/lib/logger";
import { safeErrorMessage } from "@/lib/safe-error";
import { resolveOwnedAdminId } from "@/lib/admin-access";

export async function GET(
  request: Request,
  { params }: { params: Promise<{ id: string }> },
) {
  try {
    const session = await getSession();
    if (!session) {
      return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
    }

    const { id: routeId } = await params;
    const id = await resolveOwnedAdminId(session.sub, routeId);
    if (!id) return NextResponse.json({ error: "Not found" }, { status: 404 });

    const admin = await prisma.adminAccount.findFirst({
      where: { id, createdBy: session.sub },
    });
    if (!admin) {
      return NextResponse.json({ error: "Not found" }, { status: 404 });
    }

    const gs = await prisma.googleSession.findUnique({ where: { adminId: id } });
    if (!gs?.cookies) {
      return NextResponse.json(
        {
          error: safeErrorMessage(
            "No session, scan first",
            "Session expired. Please sign in again.",
          ),
        },
        { status: 400 },
      );
    }

    let cookies: Cookie[];
    try {
      cookies = JSON.parse(safeDecrypt(gs.cookies));
      if (!Array.isArray(cookies) || cookies.length === 0) {
        return NextResponse.json({ error: "Invalid session" }, { status: 400 });
      }
    } catch (e) {
      apiError("/api/admin/[id]/credit-activity", e);
      return NextResponse.json({ error: "Invalid session data" }, { status: 400 });
    }

    const result = await fetchCreditActivity(cookies);

    if (result.error === "session_expired") {
      return NextResponse.json(
        {
          error: safeErrorMessage(
            "Session expired, rescan needed",
            "Session expired. Please sign in again.",
          ),
        },
        { status: 401 },
      );
    }

    return NextResponse.json(result);
  } catch (e) {
    apiError("/api/admin/[id]/credit-activity", e);
    return NextResponse.json(
      { error: safeErrorMessage(e, "Internal server error") },
      { status: 500 },
    );
  }
}
