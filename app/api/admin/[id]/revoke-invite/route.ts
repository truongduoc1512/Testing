import { NextResponse } from "next/server";
import { prisma } from "@/lib/db";
import { getSession } from "@/lib/auth/session";
import { googleHttpReq } from "@/lib/scanner/google-http";
import type { Cookie } from "@/lib/scanner/google-http";
import { getGoogleAuthOrRefresh } from "@/lib/scanner/google-one";
import { apiError } from "@/lib/logger";
import { safeErrorMessage } from "@/lib/safe-error";
import { resolveOwnedAdminId } from "@/lib/admin-access";
import {
  buildRateLimitKey,
  memberMutationLimiter,
  rateLimitResponse,
} from "@/lib/rate-limit";
import { formatAuditAdminLabel, writeAuditLog } from "@/lib/audit-log";

async function revokeGoogleInvite(
  cookies: Cookie[],
  inviteId: string,
  authuser: string,
  atToken: string,
): Promise<{ success: boolean; error?: string }> {
  const innerJson = `[[null,null,["v2",18,null,["googleaccount"]]],"${inviteId}"]`;
  const freqVal = `[[["fijTGe",${JSON.stringify(innerJson)},null,"generic"]]]`;
  const payload = `f.req=${encodeURIComponent(freqVal)}&at=${encodeURIComponent(atToken)}&`;

  const url = `https://myaccount.google.com/u/${authuser}/_/AccountSettingsUi/data/batchexecute?rpcids=fijTGe&source-path=%2Fu%2F${authuser}%2Ffamily%2Fmember%2Fi%2F${inviteId}&hl=vi&soc-app=1&soc-platform=1&soc-device=1&rt=c`;

  try {
    const resp = await googleHttpReq(url, {
      method: "POST",
      cookies,
      headers: {
        "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8",
        Origin: "https://myaccount.google.com",
        Referer: `https://myaccount.google.com/u/${authuser}/family/member/i/${inviteId}`,
        "X-Same-Domain": "1",
      },
      body: payload,
    });

    if (resp.status !== 200) {
      return { success: false, error: `Provider returned status ${resp.status}` };
    }

    const clean = resp.body.replace(/^\)\]\}'\n?/, "");
    if (clean.includes('"error"') && !clean.includes('"wrb.fr"')) {
      return { success: false, error: "Provider returned an error" };
    }

    return { success: true };
  } catch (e) {
    apiError("/api/admin/[id]/revoke-invite", e);
    return { success: false, error: "Network error" };
  }
}

export async function POST(
  request: Request,
  { params }: { params: Promise<{ id: string }> },
) {
  try {
    const session = await getSession();
    if (!session) return NextResponse.json({ error: "Unauthorized" }, { status: 401 });

    const { id: routeId } = await params;
    const id = await resolveOwnedAdminId(session.sub, routeId);
    if (!id) return NextResponse.json({ error: "Not found" }, { status: 404 });

    const key = buildRateLimitKey(request, session.sub, id, "revoke-invite");
    const limit = memberMutationLimiter.check(key);
    if (!limit.allowed) return rateLimitResponse(limit.resetMs);

    let body: { memberGoogleId?: string | null; memberEmail?: string };
    try {
      body = await request.json();
    } catch (e) {
      apiError("/api/admin/[id]/revoke-invite", e);
      return NextResponse.json({ error: "Invalid JSON" }, { status: 400 });
    }

    if (!body.memberGoogleId && !body.memberEmail) {
      return NextResponse.json(
        { error: "Missing memberGoogleId or memberEmail" },
        { status: 400 },
      );
    }
    const { memberGoogleId, memberEmail } = body;

    const admin = await prisma.adminAccount.findFirst({
      where: { id, createdBy: session.sub },
    });
    if (!admin) return NextResponse.json({ error: "Not found" }, { status: 404 });

    const member = await prisma.familyMember.findFirst({
      where: {
        adminId: id,
        role: { not: "FAMILY MANAGER" },
        OR: [
          ...(memberGoogleId ? [{ googleUserId: memberGoogleId }] : []),
          ...(memberEmail ? [{ email: memberEmail }] : []),
        ],
      },
    });
    if (!member) return NextResponse.json({ error: "Member not found" }, { status: 404 });

    const familyType = admin.familyType || "ultra";

    if (!["ultra", "pro", "youtube"].includes(familyType)) {
      return NextResponse.json({ error: "Unsupported account type" }, { status: 400 });
    }

      if (!member.inviteId) {
        return NextResponse.json(
          {
            error: safeErrorMessage(
              "Missing invite id. Please sync before revoking.",
              "Invite metadata unavailable.",
            ),
          },
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

      const result = await revokeGoogleInvite(
        authData.cookies,
        member.inviteId,
        authData.authuser,
        authData.atToken,
      );
      if (!result.success) {
        apiError(
          "/api/admin/[id]/revoke-invite",
          new Error(result.error || "google revoke-invite failed"),
        );
        return NextResponse.json({ error: "Failed to revoke invite" }, { status: 500 });
      }
    await prisma.familyMember.delete({ where: { id: member.id } });

    const memberCount = await prisma.familyMember.count({
      where: { adminId: id, status: "active" },
    });
    await prisma.adminAccount.update({ where: { id }, data: { memberCount } });
    await writeAuditLog({
      userId: session.sub,
      actorEmail: session.email,
      action: "member.invite.revoke",
      targetType: "member",
      targetId: member.id,
      status: "success",
      message: `Admin ${formatAuditAdminLabel(admin)} đã thu hồi lời mời member ${member.email}`,
      metadata: {
        adminId: id,
        adminEmail: admin.email,
        adminName: admin.displayName,
        familyType,
        memberEmail: member.email,
        memberName: member.name,
      },
    });

    return NextResponse.json({ success: true });
  } catch (e) {
    apiError("/api/admin/[id]/revoke-invite", e);
    return NextResponse.json(
      { error: safeErrorMessage(e, "Server error") },
      { status: 500 },
    );
  }
}
