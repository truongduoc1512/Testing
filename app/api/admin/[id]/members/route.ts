import { NextResponse } from "next/server";
import { prisma } from "@/lib/db";
import { z } from "zod";
import { apiError } from "@/lib/logger";
import {
  memberMutationLimiter,
  profileLimiter,
} from "@/lib/rate-limit";
import { safeErrorMessage } from "@/lib/safe-error";
import { requireOwnedAdminAccess } from "@/lib/api/admin-route";

function parseDateOnly(value: string): Date | null {
  const m = /^(\d{4})-(\d{2})-(\d{2})$/.exec(value);
  if (!m) return null;

  const year = Number(m[1]);
  const month = Number(m[2]);
  const day = Number(m[3]);
  const date = new Date(Date.UTC(year, month - 1, day));

  if (
    date.getUTCFullYear() !== year ||
    date.getUTCMonth() !== month - 1 ||
    date.getUTCDate() !== day
  ) {
    return null;
  }

  return date;
}

export async function GET(
  request: Request,
  { params }: { params: Promise<{ id: string }> },
) {
  try {
    const guard = await requireOwnedAdminAccess({
      request,
      params,
      action: "members-get",
      limiter: profileLimiter,
    });
    if ("response" in guard) return guard.response;
    const { session, adminId } = guard;

    const admin = await prisma.adminAccount.findFirst({
      where: { id: adminId, createdBy: session.sub },
    });

    if (!admin) {
      return NextResponse.json({ error: "Admin account not found" }, { status: 404 });
    }

    const members = await prisma.familyMember.findMany({
      where: { adminId },
      orderBy: [{ status: "asc" }, { createdAt: "desc" }],
    });

    return NextResponse.json({
      members: members.map((m) => ({
        email: m.email,
        name: m.name,
        role: m.role,
        status: m.status,
        joinedAt: m.joinedAt,
        removedAt: m.removedAt,
        startDate: m.startDate,
        endDate: m.endDate,
        renewed: m.renewed,
      })),
    });
  } catch (e) {
    apiError("/api/admin/[id]/members", e);
    return NextResponse.json(
      { error: safeErrorMessage(e, "Failed to fetch members") },
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
      action: "members-patch",
      limiter: memberMutationLimiter,
    });
    if ("response" in guard) return guard.response;
    const { session, adminId } = guard;

    let body: unknown;
    try {
      body = await request.json();
    } catch (e) {
      apiError("/api/admin/[id]/members", e);
      return NextResponse.json({ error: "Invalid JSON" }, { status: 400 });
    }

    const patchSchema = z.object({
      memberGoogleId: z.string().min(1).nullable().optional(),
      memberEmail: z.string().email().optional(),
      startDate: z
        .string()
        .nullable()
        .optional()
        .refine((v) => v === null || v === undefined || parseDateOnly(v) !== null, {
          message: "Invalid date",
        }),
      endDate: z
        .string()
        .nullable()
        .optional()
        .refine((v) => v === null || v === undefined || parseDateOnly(v) !== null, {
          message: "Invalid date",
        }),
      renewed: z.boolean().optional(),
    }).refine((v) => Boolean(v.memberGoogleId || v.memberEmail), {
      message: "memberGoogleId or memberEmail is required",
    });
    const parsed = patchSchema.safeParse(body);
    if (!parsed.success)
      return NextResponse.json({ error: "Invalid data" }, { status: 400 });

    const { memberGoogleId, memberEmail, startDate, endDate, renewed } = parsed.data;
    const nextStartDate =
      startDate === undefined ? undefined : startDate === null ? null : parseDateOnly(startDate);
    const nextEndDate =
      endDate === undefined ? undefined : endDate === null ? null : parseDateOnly(endDate);

    if (
      (startDate !== null && startDate !== undefined && !nextStartDate) ||
      (endDate !== null && endDate !== undefined && !nextEndDate)
    ) {
      return NextResponse.json({ error: "Invalid data" }, { status: 400 });
    }

    if (nextStartDate && nextEndDate && nextStartDate.getTime() > nextEndDate.getTime()) {
      return NextResponse.json(
        { error: "startDate must be before or equal to endDate" },
        { status: 400 },
      );
    }

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

    const update: { startDate?: Date | null; endDate?: Date | null; renewed?: boolean } =
      {};
    if (startDate !== undefined) update.startDate = nextStartDate ?? null;
    if (endDate !== undefined) update.endDate = nextEndDate ?? null;
    if (renewed !== undefined) update.renewed = renewed;

    const updated = await prisma.familyMember.update({
      where: { id: member.id },
      data: update,
    });

    return NextResponse.json({
      startDate: updated.startDate,
      endDate: updated.endDate,
      renewed: updated.renewed,
    });
  } catch (e) {
    apiError("/api/admin/[id]/members", e);
    return NextResponse.json(
      { error: safeErrorMessage(e, "Server error") },
      { status: 500 },
    );
  }
}
