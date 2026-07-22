import { NextResponse } from "next/server";
import { prisma } from "@/lib/db";
import { safeDecrypt } from "@/lib/crypto";
import { createFamilyForAdmin, syncFamilyGroup } from "@/lib/scanner/google-one";
import { syncResult } from "@/lib/scanner/sync";
import { apiError } from "@/lib/logger";
import { safeErrorMessage } from "@/lib/safe-error";
import { memberMutationLimiter } from "@/lib/rate-limit";
import { requireOwnedAdminAccess } from "@/lib/api/admin-route";

export async function POST(
  request: Request,
  { params }: { params: Promise<{ id: string }> },
) {
  try {
    const guard = await requireOwnedAdminAccess({
      request,
      params,
      action: "create-family",
      limiter: memberMutationLimiter,
    });
    if ("response" in guard) return guard.response;
    const { session, adminId } = guard;

    const admin = await prisma.adminAccount.findFirst({
      where: { id: adminId, createdBy: session.sub },
    });
    if (!admin) return NextResponse.json({ error: "Not found" }, { status: 404 });
    if ((admin.familyType || "ultra") === "gpt") {
      return NextResponse.json(
        { error: "This feature is only for Google accounts." },
        { status: 400 },
      );
    }

    const password = safeDecrypt(admin.googlePassword);
    const totp = safeDecrypt(admin.totpSecret);

    const result = await createFamilyForAdmin(admin.id, admin.email, password, totp);

    if (!result.success) {
      apiError("/api/admin/[id]/create-family", new Error(result.error || "create-family failed"));
      return NextResponse.json(
        { error: "Failed to create family" },
        { status: 500 },
      );
    }

    const sync = await syncFamilyGroup(
      admin.id,
      admin.email,
      password,
      totp,
      true,
      admin.familyType || "ultra",
    );
    if (sync.members.length > 0) {
      await syncResult(admin.id, sync);
    }

    return NextResponse.json({
      message: "Family group created successfully",
      memberCount: sync.members.length,
    });
  } catch (err) {
    apiError("/api/admin/[id]/create-family", err);
    return NextResponse.json(
      { error: safeErrorMessage(err, "Failed to create family") },
      { status: 500 },
    );
  }
}
