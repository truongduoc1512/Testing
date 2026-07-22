import { NextResponse } from "next/server";
import { getSession } from "@/lib/auth/session";
import { resolveOwnedAdminId } from "@/lib/admin-access";
import { buildRateLimitKey, rateLimitResponse } from "@/lib/rate-limit";

type LimiterResult = {
  allowed: boolean;
  resetMs: number;
};

type LimiterLike = {
  check(key: string): LimiterResult;
};

type OwnedAdminGuardOptions = {
  request: Request;
  params: Promise<{ id: string }>;
  action?: string;
  limiter?: LimiterLike;
};

type OwnedAdminGuardSuccess = {
  session: NonNullable<Awaited<ReturnType<typeof getSession>>>;
  adminId: string;
};

type OwnedAdminGuardFailure = {
  response: Response;
};

export async function requireOwnedAdminAccess(
  options: OwnedAdminGuardOptions,
): Promise<OwnedAdminGuardSuccess | OwnedAdminGuardFailure> {
  const session = await getSession();
  if (!session) {
    return { response: NextResponse.json({ error: "Unauthorized" }, { status: 401 }) };
  }

  const { id: routeId } = await options.params;
  const adminId = await resolveOwnedAdminId(session.sub, routeId);
  if (!adminId) {
    return { response: NextResponse.json({ error: "Not found" }, { status: 404 }) };
  }

  if (options.limiter && options.action) {
    const key = buildRateLimitKey(options.request, session.sub, adminId, options.action);
    const limit = options.limiter.check(key);
    if (!limit.allowed) {
      return { response: rateLimitResponse(limit.resetMs) };
    }
  }

  return { session, adminId };
}
