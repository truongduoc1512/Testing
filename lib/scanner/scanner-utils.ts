import * as OTPAuth from "otpauth";
import { googleClock } from "@/lib/time/google-clock-instance";

export function sleep(ms: number) {
  return new Promise((r) => setTimeout(r, ms));
}

export async function get2FACode(
  secret: string,
  timestamp?: number,
): Promise<string | null> {
  const clean = secret.replace(/\s+/g, "").toUpperCase();
  try {
    await googleClock.ensureFresh();
    const totp = new OTPAuth.TOTP({
      algorithm: "SHA1",
      digits: 6,
      period: 30,
      secret: clean,
    });
    return totp.generate({ timestamp: timestamp ?? googleClock.now() });
  } catch {
    return null;
  }
}
