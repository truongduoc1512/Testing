import { NextResponse } from "next/server";
import { prisma } from "@/lib/db";
import { encryptValue, safeDecrypt } from "@/lib/crypto";
import { z } from "zod";
import { apiError } from "@/lib/logger";
import { safeErrorMessage } from "@/lib/safe-error";
import {
  memberMutationLimiter,
  profileLimiter,
} from "@/lib/rate-limit";
import { requireOwnedAdminAccess } from "@/lib/api/admin-route";
import {
  adminPublicIdFromInternal,
  memberPublicIdFromInternal,
} from "@/lib/admin-access";
import { formatAuditAdminLabel, writeAuditLog } from "@/lib/audit-log";

export async function GET(
  request: Request,
  { params }: { params: Promise<{ id: string }> },
) {
  try {
    const guard = await requireOwnedAdminAccess({
      request,
      params,
      action: "admin-detail",
      limiter: profileLimiter,
    });
    if ("response" in guard) return guard.response;
    const { session, adminId } = guard;

    const admin = await prisma.adminAccount.findFirst({
      where: { id: adminId, createdBy: session.sub },
      include: {
        members: {
          where: { status: { not: "removed" } },
          orderBy: { createdAt: "asc" },
          select: {
            id: true,
            email: true,
            name: true,
            role: true,
            status: true,
            joinedAt: true,
            inviteId: true,
            googleUserId: true,
            creditLimit: true,
            lastCreditUsed: true,
            startDate: true,
            endDate: true,
            renewed: true,
          },
        },
      },
    });

    if (!admin) return NextResponse.json({ error: "Not found" }, { status: 404 });

    return NextResponse.json({
      id: adminPublicIdFromInternal(admin.id),
      email: admin.email,
      displayName: admin.displayName,
      has2FA: admin.has2FA,
      hasPassword: !!admin.googlePassword,
      memberCount: admin.memberCount,
      accountStatus: admin.accountStatus ?? "",
      monthlyCredit: admin.monthlyCredit,
      remainingCredit: admin.remainingCredit,
      storageTB: admin.storageTB,
      usedStorageMB: admin.usedStorageMB,
      planName: admin.planName,
      planExpiresAt: admin.planExpiresAt ?? null,
      note: admin.note,
      lastSyncAt: admin.lastSyncAt,
      lastSyncStatus: admin.lastSyncStatus ?? "",
      lastSyncError: admin.lastSyncError ?? "",
      members: admin.members.map((member) => ({
        ...member,
        id: memberPublicIdFromInternal(member.id),
      })),
      familyType: admin.familyType ?? "ultra",
    });
  } catch (e) {
    apiError("GET /api/admin/[id]", e);
    return NextResponse.json(
      { error: safeErrorMessage(e, "Server error") },
      { status: 500 },
    );
  }
}

export async function PUT(
  request: Request,
  { params }: { params: Promise<{ id: string }> },
) {
  try {
    const guard = await requireOwnedAdminAccess({
      request,
      params,
      action: "admin-put",
      limiter: memberMutationLimiter,
    });
    if ("response" in guard) return guard.response;
    const { session, adminId } = guard;

    let body: unknown;
    try {
      body = await request.json();
    } catch {
      return NextResponse.json({ error: "Invalid JSON" }, { status: 400 });
    }

    const putSchema = z.object({
      displayName: z.string().trim().max(200).optional(),
      email: z.string().email().optional(),
      googlePassword: z.string().max(500).optional(),
      totpSecret: z.string().max(500).optional(),
      note: z.string().max(1000).optional(),

      monthlyCredit: z.number().int().min(0).optional(),
      storageTB: z.number().min(0).optional(),
    });
    const parsed = putSchema.safeParse(body);
    if (!parsed.success) {
      return NextResponse.json({ error: "Invalid data" }, { status: 400 });
    }
    const data = parsed.data;

    const admin = await prisma.adminAccount.findFirst({
      where: { id: adminId, createdBy: session.sub },
    });
    if (!admin) return NextResponse.json({ error: "Not found" }, { status: 404 });

    if (data.displayName && data.displayName !== admin.displayName) {
      const duplicateName = await prisma.adminAccount.findFirst({
        where: {
          displayName: data.displayName,
          familyType: admin.familyType ?? "ultra",
          createdBy: session.sub,
          id: { not: adminId },
        },
      });
      if (duplicateName) {
        return NextResponse.json(
          { error: `Tên "${data.displayName}" đã tồn tại trong loại family này` },
          { status: 409 },
        );
      }
    }

    if (data.email && data.email !== admin.email) {
      const duplicateEmail = await prisma.adminAccount.findFirst({
        where: {
          email: data.email,
          createdBy: session.sub,
          id: { not: adminId },
        },
      });
      if (duplicateEmail) {
        return NextResponse.json(
          { error: "Admin với email này đã tồn tại" },
          { status: 409 },
        );
      }
    }

    const currentPassword = safeDecrypt(admin.googlePassword);
    const currentTotp = safeDecrypt(admin.totpSecret);

    const newPassword = data.googlePassword || currentPassword;
    const newTotp = data.totpSecret || currentTotp;

    const updated = await prisma.adminAccount.update({
      where: { id: adminId },
      data: {
        displayName: data.displayName ?? admin.displayName,
        email: data.email ?? admin.email,
        googlePassword: encryptValue(newPassword),
        totpSecret: newTotp ? encryptValue(newTotp) : "",
        has2FA: !!newTotp,
        note: data.note ?? admin.note,
        monthlyCredit: data.monthlyCredit ?? admin.monthlyCredit,
        storageTB: data.storageTB ?? admin.storageTB,
      },
    });

    const changedFields = Object.keys(data).filter(
      (field) => field !== "googlePassword" && field !== "totpSecret",
    );
    await writeAuditLog({
      userId: session.sub,
      actorEmail: session.email,
      action: "admin.update",
      targetType: "admin",
      targetId: adminId,
      status: "success",
      message: `Đã cập nhật admin ${formatAuditAdminLabel(admin)}`,
      metadata: {
        adminEmail: admin.email,
        adminName: admin.displayName,
        familyType: admin.familyType,
        changedFields,
      },
    });

    const {
      id: _id,
      googlePassword: _gp,
      totpSecret: _ts,
      createdBy: _cb,
      ...safeAdmin
    } = updated;
    return NextResponse.json({ success: true, admin: safeAdmin });
  } catch (e) {
    apiError("PUT /api/admin/[id]", e);
    return NextResponse.json(
      { error: safeErrorMessage(e, "Server error") },
      { status: 500 },
    );
  }
}

export async function DELETE(
  request: Request,
  { params }: { params: Promise<{ id: string }> },
) {
  try {
    const guard = await requireOwnedAdminAccess({
      request,
      params,
      action: "admin-delete",
      limiter: memberMutationLimiter,
    });
    if ("response" in guard) return guard.response;
    const { session, adminId } = guard;

    const admin = await prisma.adminAccount.findFirst({
      where: { id: adminId, createdBy: session.sub },
    });
    if (!admin) return NextResponse.json({ error: "Not found" }, { status: 404 });

    await prisma.adminAccount.delete({ where: { id: adminId } });

    await writeAuditLog({
      userId: session.sub,
      actorEmail: session.email,
      action: "admin.delete",
      targetType: "admin",
      targetId: adminId,
      status: "success",
      message: `Đã xóa admin ${formatAuditAdminLabel(admin)}`,
      metadata: {
        adminEmail: admin.email,
        adminName: admin.displayName,
        familyType: admin.familyType,
      },
    });

    return NextResponse.json({ success: true });
  } catch (e) {
    apiError("DELETE /api/admin/[id]", e);
    return NextResponse.json(
      { error: safeErrorMessage(e, "Server error") },
      { status: 500 },
    );
  }
}

export async function PATCH(
  request: Request,
  { params }: { params: Promise<{ id: string }> },
) {
  try {
    const guard = await requireOwnedAdminAccess({
      request,
      params,
      action: "admin-patch",
      limiter: memberMutationLimiter,
    });
    if ("response" in guard) return guard.response;
    const { session, adminId } = guard;

    let body: unknown;
    try {
      body = await request.json();
    } catch {
      return NextResponse.json({ error: "Invalid JSON" }, { status: 400 });
    }

    const patchSchema = z.object({
      memberGoogleId: z.string().min(1).optional().nullable(),
      memberEmail: z.string().email().optional(),
      creditLimit: z.number().int().min(0),
    }).refine((v) => Boolean(v.memberGoogleId || v.memberEmail), {
      message: "memberGoogleId or memberEmail is required",
    });
    const parsed = patchSchema.safeParse(body);
    if (!parsed.success) {
      return NextResponse.json({ error: "Invalid data" }, { status: 400 });
    }
    const { memberGoogleId, memberEmail, creditLimit } = parsed.data;

    const admin = await prisma.adminAccount.findFirst({
      where: { id: adminId, createdBy: session.sub },
    });
    if (!admin) return NextResponse.json({ error: "Not found" }, { status: 404 });

    const member = await prisma.familyMember.findFirst({
      where: {
        adminId,
        OR: [
          ...(memberGoogleId ? [{ googleUserId: memberGoogleId }] : []),
          ...(memberEmail ? [{ email: memberEmail }] : []),
        ],
      },
    });
    if (!member) return NextResponse.json({ error: "Member not found" }, { status: 404 });

    await prisma.familyMember.update({
      where: { id: member.id },
      data: { creditLimit },
    });

    await writeAuditLog({
      userId: session.sub,
      actorEmail: session.email,
      action: "member.credit_limit.update",
      targetType: "member",
      targetId: member.id,
      status: "success",
      message: `Admin ${formatAuditAdminLabel(admin)} đã đặt giới hạn credit ${creditLimit} cho member ${member.email}`,
      metadata: {
        adminId,
        adminEmail: admin.email,
        adminName: admin.displayName,
        familyType: admin.familyType,
        memberEmail: member.email,
        memberName: member.name,
        creditLimit,
      },
    });

    return NextResponse.json({ success: true, creditLimit });
  } catch (e) {
    apiError("PATCH /api/admin/[id]", e);
    return NextResponse.json(
      { error: safeErrorMessage(e, "Server error") },
      { status: 500 },
    );
  }
}
