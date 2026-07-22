import { NextResponse } from "next/server";
import { googleClock } from "@/lib/time/google-clock-instance";
import { getSession } from "@/lib/auth/session";
import { apiError } from "@/lib/logger";
import { safeErrorMessage } from "@/lib/safe-error";

export async function GET() {
  try {
    const session = await getSession();
    if (!session) {
      return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
    }

    await googleClock.ensureFresh();
    const serverTime = googleClock.now();
    return NextResponse.json({
      serverTime,
    });
  } catch (e) {
    apiError("GET /api/time", e);
    return NextResponse.json(
      { error: safeErrorMessage(e, "Failed to read server time") },
      { status: 500 },
    );
  }
}
