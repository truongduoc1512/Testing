import { NextResponse } from "next/server";
import { getSession } from "@/lib/auth/session";
import { buildRateLimitKey, profileLimiter, rateLimitResponse } from "@/lib/rate-limit";
import { apiError } from "@/lib/logger";
import { safeErrorMessage } from "@/lib/safe-error";

export async function GET(request: Request) {
  try {
    const session = await getSession();
    if (!session) {
      return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
    }

    const key = buildRateLimitKey(request, session.sub, "auth-me");
    const limit = profileLimiter.check(key);
    if (!limit.allowed) return rateLimitResponse(limit.resetMs);

    return NextResponse.json({
      user: {
        email: session.email,
        fullName: session.fullName,
      },
    });
  } catch (e) {
    apiError("GET /api/auth/me", e);
    return NextResponse.json(
      { error: safeErrorMessage(e, "Internal server error") },
      { status: 500 },
    );
  }
}
