import { NextResponse } from "next/server";
import { prisma } from "@/lib/db";
import { googleHttpReq } from "@/lib/scanner/google-http";
import type { Cookie } from "@/lib/scanner/google-http";
import { gptInviteMembers } from "@/lib/scanner/chatgpt";
import {
  getGoogleAuthOrRefresh,
} from "@/lib/scanner/google-one";
import { apiError } from "@/lib/logger";
import { safeErrorMessage } from "@/lib/safe-error";
import { requireOwnedAdminAccess } from "@/lib/api/admin-route";
import { inviteLimiter } from "@/lib/rate-limit";
import {
  formatAuditAdminLabel,
  formatAuditEmailList,
  writeAuditLog,
} from "@/lib/audit-log";

async function inviteToFamily(
  cookies: Cookie[],
  email: string,
  atToken: string,
  authuser: string,
): Promise<{ success: boolean; inviteId?: string; error?: string }> {
  const innerJson = `[[null,null,["v2",18,null,["googleaccount"]]],[[null,["${email}"],null,3,null,null,null,null,null,1,"${email}"]]]`;
  const freqVal = `[[["xN05r",${JSON.stringify(innerJson)},null,"generic"]]]`;
  const payload = `f.req=${encodeURIComponent(freqVal)}&at=${encodeURIComponent(atToken)}&`;

  const url = `https://myaccount.google.com/u/${authuser}/_/AccountSettingsUi/data/batchexecute?rpcids=xN05r&source-path=%2Fu%2F${authuser}%2Ffamily%2Finvitemembers&hl=vi&soc-app=1&soc-platform=1&soc-device=1&rt=c`;

  try {
    const resp = await googleHttpReq(url, {
      method: "POST",
      cookies,
      headers: {
        "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8",
        Origin: "https://myaccount.google.com",
        Referer: `https://myaccount.google.com/u/${authuser}/family/invitemembers`,
        "X-Same-Domain": "1",
      },
      body: payload,
    });

    const clean = resp.body.replace(/^\)\]\}'\n?/, "");

    if (resp.status !== 200) {
      return { success: false, error: `Provider returned status ${resp.status}` };
    }

    let inviteId: string | undefined;
    let inviteError: string | undefined;
    try {
      const lines = clean.split("\n");
      for (const line of lines) {
        const trimmed = line.trim();
        if (!trimmed.startsWith("[")) continue;
        try {
          const envelope = JSON.parse(trimmed);
          for (const entry of envelope) {
            if (
              Array.isArray(entry) &&
              entry[0] === "wrb.fr" &&
              entry[1] === "xN05r" &&
              entry[2]
            ) {
              const innerData = JSON.parse(entry[2]);
              if (Array.isArray(innerData[2])) {
                for (const item of innerData[2]) {
                  if (
                    Array.isArray(item) &&
                    Array.isArray(item[1]) &&
                    typeof item[1][0] === "number"
                  ) {
                    const errorCode = item[1][0];
                    inviteError = `Provider invite error code: ${errorCode}`;
                  }
                }
              }
              if (!inviteError && Array.isArray(innerData[1])) {
                for (const inv of innerData[1]) {
                  if (
                    Array.isArray(inv) &&
                    typeof inv[0] === "string" &&
                    /^-?\d{10,}$/.test(inv[0])
                  ) {
                    inviteId = inv[0];
                    break;
                  }
                }
              }
            }
          }
        } catch (e) {
          apiError("/api/admin/[id]/invite", e, { context: "JSON parse inner" });
        }
      }
    } catch (e) {
      apiError("/api/admin/[id]/invite", e, { context: "JSON parse outer" });
    }

    if (inviteError) return { success: false, error: inviteError };
    if (inviteId) return { success: true, inviteId };
    if (clean.includes("error")) return { success: false, error: "Provider invite error" };
    return { success: true };
  } catch (e) {
    apiError("/api/admin/[id]/invite", e);
    return { success: false, error: "Network error" };
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
      action: "invite",
      limiter: inviteLimiter,
    });
    if ("response" in guard) return guard.response;
    const { session, adminId: id } = guard;

    const admin = await prisma.adminAccount.findFirst({
      where: { id, createdBy: session.sub },
      include: { session: true },
    });

    if (!admin) return NextResponse.json({ error: "Not found" }, { status: 404 });

    let body: { emails?: string[] };
    try {
      body = await request.json();
    } catch (e) {
      apiError("/api/admin/[id]/invite", e);
      return NextResponse.json({ error: "Invalid JSON" }, { status: 400 });
    }

    const { emails } = body;
    if (!emails?.length) {
      return NextResponse.json({ error: "At least 1 email required" }, { status: 400 });
    }

    const familyType = admin.familyType || "ultra";

    if (familyType === "gpt") {
      const validEmails: string[] = [];
      const invalidResults: { email: string; success: boolean; error: string }[] = [];
      for (const email of emails) {
        const trimmed = email.trim().toLowerCase();
        if (!trimmed || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(trimmed)) {
          invalidResults.push({
            email: trimmed,
            success: false,
            error: "Invalid email",
          });
        } else {
          validEmails.push(trimmed);
        }
      }
      if (validEmails.length === 0) {
        await writeAuditLog({
          userId: session.sub,
          actorEmail: session.email,
          action: "member.invite",
          targetType: "admin",
          targetId: id,
          status: "failure",
          message: `Admin ${formatAuditAdminLabel(admin)} mời member thất bại do không có email hợp lệ`,
          metadata: {
            adminEmail: admin.email,
            adminName: admin.displayName,
            familyType,
            requested: emails.length,
            memberEmails: emails,
          },
        });
        return NextResponse.json({ results: invalidResults });
      }

      const { results } = await gptInviteMembers(
        admin.id,
        validEmails,
        admin.email,
        admin.googlePassword,
        admin.totpSecret,
      );
      const safeResults = [...results, ...invalidResults].map((r) => {
        if (r.success) return r;
        if (r.error) {
          apiError("/api/admin/[id]/invite", new Error(String(r.error)), { context: "gpt-invite" });
        }
        return { ...r, error: "Invite failed" };
      });

      for (const r of safeResults) {
        if (r.success) {
          await prisma.familyMember.upsert({
            where: { adminId_email: { adminId: id, email: r.email } },
            create: {
              adminId: id,
              email: r.email,
              name: "",
              role: "Member",
              status: "invited",
            },
            update: { status: "invited", role: "Member", removedAt: null },
          });
        }
      }

      const memberCount = await prisma.familyMember.count({
        where: { adminId: id, status: "active" },
      });
      await prisma.adminAccount.update({ where: { id }, data: { memberCount } });
      const successCount = safeResults.filter((result) => result.success).length;
      const successEmails = safeResults
        .filter((result) => result.success)
        .map((result) => result.email);
      const failedEmails = safeResults
        .filter((result) => !result.success)
        .map((result) => result.email);
      await writeAuditLog({
        userId: session.sub,
        actorEmail: session.email,
        action: "member.invite",
        targetType: "admin",
        targetId: id,
        status:
          successCount === safeResults.length
            ? "success"
            : successCount > 0
              ? "partial"
              : "failure",
        message:
          successCount > 0
            ? `Admin ${formatAuditAdminLabel(admin)} đã mời member ${formatAuditEmailList(successEmails)}`
            : `Admin ${formatAuditAdminLabel(admin)} mời member thất bại`,
        metadata: {
          adminEmail: admin.email,
          adminName: admin.displayName,
          familyType,
          requested: emails.length,
          successCount,
          failedCount: safeResults.length - successCount,
          memberEmails: safeResults.map((result) => result.email),
          successEmails,
          failedEmails,
        },
      });
      return NextResponse.json({ results: safeResults });
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

    const { atToken, authuser, cookies } = authData;

    const results: {
      email: string;
      success: boolean;
      inviteId?: string;
      error?: string;
    }[] = [];

    for (const email of emails) {
      const trimmed = email.trim().toLowerCase();
      if (!trimmed || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(trimmed)) {
        results.push({ email: trimmed, success: false, error: "Invalid email" });
        continue;
      }

      const result = await inviteToFamily(cookies, trimmed, atToken, authuser);
      if (!result.success && result.error) {
        apiError("/api/admin/[id]/invite", new Error(result.error), { context: "google-invite" });
      }
      results.push({
        email: trimmed,
        success: result.success,
        inviteId: result.inviteId,
        error: result.success ? undefined : "Invite failed",
      });

      if (result.success) {
        await prisma.familyMember.upsert({
          where: { adminId_email: { adminId: id, email: trimmed } },
          create: {
            adminId: id,
            email: trimmed,
            name: "",
            role: "Member",
            status: "invited",
            inviteId: result.inviteId || null,
          },
          update: {
            status: "invited",
            role: "Member",
            removedAt: null,
            inviteId: result.inviteId || null,
          },
        });
      }
    }

    const memberCount = await prisma.familyMember.count({
      where: { adminId: id, status: "active" },
    });
    await prisma.adminAccount.update({ where: { id }, data: { memberCount } });
    const successCount = results.filter((result) => result.success).length;
    const successEmails = results
      .filter((result) => result.success)
      .map((result) => result.email);
    const failedEmails = results
      .filter((result) => !result.success)
      .map((result) => result.email);
    await writeAuditLog({
      userId: session.sub,
      actorEmail: session.email,
      action: "member.invite",
      targetType: "admin",
      targetId: id,
      status:
        successCount === results.length
          ? "success"
          : successCount > 0
            ? "partial"
            : "failure",
      message:
        successCount > 0
          ? `Admin ${formatAuditAdminLabel(admin)} đã mời member ${formatAuditEmailList(successEmails)}`
          : `Admin ${formatAuditAdminLabel(admin)} mời member thất bại`,
      metadata: {
        adminEmail: admin.email,
        adminName: admin.displayName,
        familyType,
        requested: emails.length,
        successCount,
        failedCount: results.length - successCount,
        memberEmails: results.map((result) => result.email),
        successEmails,
        failedEmails,
      },
    });
    return NextResponse.json({
      results,
    });
  } catch (e) {
    apiError("/api/admin/[id]/invite", e);
    return NextResponse.json(
      { error: safeErrorMessage(e, "Server error") },
      { status: 500 },
    );
  }
}
