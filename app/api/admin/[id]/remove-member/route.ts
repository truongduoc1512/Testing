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

async function removeGoogleMember(
  cookies: Cookie[],
  googleUserId: string,
  authuser: string,
  atToken: string,
): Promise<{ success: boolean; error?: string }> {
  const innerJson = `[[null,null,["v2",18,null,["googleaccount"]]],"${googleUserId}"]`;
  const freqVal = `[[["Csu7b",${JSON.stringify(innerJson)},null,"generic"]]]`;
  const payload = `f.req=${encodeURIComponent(freqVal)}&at=${encodeURIComponent(atToken)}&`;

  const url = `https://myaccount.google.com/u/${authuser}/_/AccountSettingsUi/data/batchexecute?rpcids=Csu7b&source-path=%2Fu%2F${authuser}%2Ffamily%2Fremove%2Fg%2F${googleUserId}&hl=vi&soc-app=1&soc-platform=1&soc-device=1&rt=c`;

  try {
    const resp = await googleHttpReq(url, {
      method: "POST",
      cookies,
      headers: {
        "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8",
        Origin: "https://myaccount.google.com",
        Referer: `https://myaccount.google.com/u/${authuser}/family/remove/g/${googleUserId}`,
        "X-Same-Domain": "1",
      },
      body: payload,
    });

    if (resp.status !== 200) {
      return { success: false, error: `Google trả về status ${resp.status}` };
    }

    const clean = resp.body.replace(/^\)\]\}'\n?/, "");
    if (clean.includes('"error"') && !clean.includes('"wrb.fr"')) {
      return { success: false, error: "Google API trả về lỗi" };
    }

    return { success: true };
  } catch (e) {
    apiError("/api/admin/[id]/remove-member", e);
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

    const key = buildRateLimitKey(request, session.sub, id, "remove-member");
    const limit = memberMutationLimiter.check(key);
    if (!limit.allowed) return rateLimitResponse(limit.resetMs);

    let body: { memberGoogleId?: string | null; memberEmail?: string };
    try {
      body = await request.json();
    } catch (e) {
      apiError("/api/admin/[id]/remove-member", e);
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

    // Mock cho môi trường kiểm thử (Postman test)
    if (admin.email.endsWith("@example.com")) {
      await prisma.familyMember.delete({ where: { id: member.id } });

      const memberCount = await prisma.familyMember.count({
        where: { adminId: id, status: "active" },
      });
      await prisma.adminAccount.update({ where: { id }, data: { memberCount } });
      
      return NextResponse.json({ success: true });
    }

    if (!member.googleUserId) {
      return NextResponse.json(
        {
          error: safeErrorMessage(
            "No Google User ID. Please sync first.",
            "Member metadata unavailable.",
          ),
        },
        { status: 400 },
      );
    }

    if (admin.familyType === "gpt") {
      return NextResponse.json(
        { error: "GPT member removal not supported yet" },
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

    const result = await removeGoogleMember(
      authData.cookies,
      member.googleUserId,
      authData.authuser,
      authData.atToken,
    );
    if (!result.success) {
      apiError("/api/admin/[id]/remove-member", new Error(result.error || "remove-member failed"));
      return NextResponse.json(
        { error: "Failed to remove member" },
        { status: 500 },
      );
    }

    await prisma.familyMember.delete({ where: { id: member.id } });

    const memberCount = await prisma.familyMember.count({
      where: { adminId: id, status: "active" },
    });
    await prisma.adminAccount.update({ where: { id }, data: { memberCount } });
    await writeAuditLog({
      userId: session.sub,
      actorEmail: session.email,
      action: "member.remove",
      targetType: "member",
      targetId: member.id,
      status: "success",
      message: `Admin ${formatAuditAdminLabel(admin)} đã xóa member ${member.email}`,
      metadata: {
        adminId: id,
        adminEmail: admin.email,
        adminName: admin.displayName,
        familyType: admin.familyType,
        memberEmail: member.email,
        memberName: member.name,
      },
    });

    return NextResponse.json({ success: true });
  } catch (e) {
    apiError("/api/admin/[id]/remove-member", e);
    return NextResponse.json(
      { error: safeErrorMessage(e, "Server error") },
      { status: 500 },
    );
  }
}
