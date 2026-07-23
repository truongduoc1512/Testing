import { NextResponse } from "next/server";
import { z } from "zod";
import { prisma } from "@/lib/db";
import { getSession } from "@/lib/auth/session";
import { encryptValue } from "@/lib/crypto";
import { parseZodErrors } from "@/lib/validation/parseErrors";
import { apiError } from "@/lib/logger";
import { buildRateLimitKey, memberMutationLimiter, rateLimitResponse } from "@/lib/rate-limit";
import { safeErrorMessage } from "@/lib/safe-error";
import { adminPublicIdFromInternal } from "@/lib/admin-access";

const FAMILY_TYPES = ["ultra", "pro", "youtube"] as const;

const addAdminSchema = z.object({
  email: z.string().email("Email không hợp lệ"),
  displayName: z.string().trim().min(1, "Vui lòng nhập tên hiển thị"),
  googlePassword: z.string().min(1, "Vui lòng nhập mật khẩu Google"),
  totpSecret: z.string().optional().default(""),
  monthlyCredit: z.number().int().min(0).default(0),
  storageTB: z.number().int().min(0).default(0),
  familyType: z.enum(FAMILY_TYPES).default("ultra"),
  note: z.string().optional().default(""),
});

export async function POST(request: Request) {
  try {
    const session = await getSession();
    if (!session) {
      return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
    }

    const key = buildRateLimitKey(request, session.sub, "admin-create");
    const limit = memberMutationLimiter.check(key);
    if (!limit.allowed) return rateLimitResponse(limit.resetMs);

    let body: unknown;
    try {
      body = await request.json();
    } catch {
      return NextResponse.json({ error: "Invalid JSON" }, { status: 400 });
    }
    const result = addAdminSchema.safeParse(body);
    if (!result.success) {
      return NextResponse.json(
        { error: "Validation failed", fields: parseZodErrors(result.error) },
        { status: 400 },
      );
    }

    const data = result.data;

    const existing = await prisma.adminAccount.findFirst({
      where: { email: data.email, createdBy: session.sub },
    });
    if (existing) {
      return NextResponse.json(
        { error: "Admin với email này đã tồn tại" },
        { status: 409 },
      );
    }

    const duplicateName = await prisma.adminAccount.findFirst({
      where: {
        displayName: data.displayName,
        familyType: data.familyType,
        createdBy: session.sub,
      },
    });
    if (duplicateName) {
      return NextResponse.json(
        { error: `Tên "${data.displayName}" đã tồn tại trong loại family này` },
        { status: 409 },
      );
    }

    const isUltra = data.familyType === "ultra";

    const admin = await prisma.adminAccount.create({
      data: {
        email: data.email,
        displayName: data.displayName,
        googlePassword: encryptValue(data.googlePassword),
        totpSecret: data.totpSecret ? encryptValue(data.totpSecret) : "",
        has2FA: !!data.totpSecret,
        monthlyCredit: isUltra ? data.monthlyCredit : 0,
        storageTB: data.storageTB,
        remainingCredit: 0,
        planName: "",
        familyType: data.familyType,
        note: data.note,
        createdBy: session.sub,
      },
    });

    return NextResponse.json(
      {
        message: "Admin account added",
        admin: {
          id: adminPublicIdFromInternal(admin.id),
          email: admin.email,
          displayName: admin.displayName,
          monthlyCredit: admin.monthlyCredit,
          storageTB: admin.storageTB,
        },
      },
      { status: 201 },
    );
  } catch (e) {
    apiError("POST /api/admin", e);
    return NextResponse.json(
      { error: safeErrorMessage(e, "Internal server error") },
      { status: 500 },
    );
  }
}
