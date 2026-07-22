import { createHash, randomInt, randomBytes } from "crypto";
import { prisma } from "@/lib/db";

const OTP_EXPIRY_MS = 5 * 60 * 1000;
const OTP_COOLDOWN_MS = 60 * 1000;
const MAX_ATTEMPTS = 3;
const LOCKOUT_MS = 60 * 60 * 1000;
const MAX_RESEND = 3;
const RESEND_WINDOW_MS = 24 * 60 * 60 * 1000;
const VERIFIED_EXPIRY_MS = 10 * 60 * 1000;

const verifiedStore = new Map<string, { email: string; expiresAt: number }>();

function hashOtp(code: string): string {
  return createHash("sha256").update(code).digest("hex");
}

export function generateOtp(): string {
  return String(randomInt(100000, 999999));
}

export async function checkLockout(email: string): Promise<{ locked: boolean; minutesLeft: number }> {
  const record = await prisma.otpLockout.findUnique({ where: { email } });
  if (!record) return { locked: false, minutesLeft: 0 };

  const remaining = record.lockedUntil.getTime() - Date.now();
  if (remaining <= 0) {
    await prisma.otpLockout.delete({ where: { email } });
    return { locked: false, minutesLeft: 0 };
  }

  return { locked: true, minutesLeft: Math.ceil(remaining / 60_000) };
}

export async function checkResendLimit(email: string): Promise<{ allowed: boolean; hoursLeft: number }> {
  const record = await prisma.otpResendTracker.findUnique({ where: { email } });
  if (!record) return { allowed: true, hoursLeft: 0 };

  if (Date.now() - record.firstSentAt.getTime() >= RESEND_WINDOW_MS) {
    await prisma.otpResendTracker.delete({ where: { email } });
    return { allowed: true, hoursLeft: 0 };
  }

  if (record.count >= MAX_RESEND) {
    const remaining = record.firstSentAt.getTime() + RESEND_WINDOW_MS - Date.now();
    return { allowed: false, hoursLeft: Math.ceil(remaining / 3_600_000) };
  }

  return { allowed: true, hoursLeft: 0 };
}

export async function checkCooldown(email: string): Promise<boolean> {
  const record = await prisma.otpRecord.findUnique({ where: { email } });
  if (!record) return false;
  return Date.now() - record.createdAt.getTime() < OTP_COOLDOWN_MS;
}

export async function storeOtp(email: string, otp: string): Promise<void> {
  await prisma.otpRecord.upsert({
    where: { email },
    update: {
      hash: hashOtp(otp),
      attempts: 0,
      expiresAt: new Date(Date.now() + OTP_EXPIRY_MS),
      createdAt: new Date(),
    },
    create: {
      email,
      hash: hashOtp(otp),
      expiresAt: new Date(Date.now() + OTP_EXPIRY_MS),
    },
  });

  const existing = await prisma.otpResendTracker.findUnique({ where: { email } });
  if (existing && Date.now() - existing.firstSentAt.getTime() < RESEND_WINDOW_MS) {
    await prisma.otpResendTracker.update({
      where: { email },
      data: { count: existing.count + 1 },
    });
  } else {
    await prisma.otpResendTracker.upsert({
      where: { email },
      update: { count: 1, firstSentAt: new Date() },
      create: { email },
    });
  }
}

export async function verifyOtp(
  email: string,
  code: string,
): Promise<{ success: boolean; error?: string; token?: string }> {
  const lockout = await checkLockout(email);
  if (lockout.locked) {
    return {
      success: false,
      error: `Quá nhiều lần thử sai. Thử lại sau ${lockout.minutesLeft} phút.`,
    };
  }

  const record = await prisma.otpRecord.findUnique({ where: { email } });
  if (!record) return { success: false, error: "Chưa gửi OTP cho email này" };

  if (Date.now() > record.expiresAt.getTime()) {
    await prisma.otpRecord.delete({ where: { email } });
    return { success: false, error: "Mã đã hết hạn. Vui lòng lấy mã mới." };
  }

  if (hashOtp(code) !== record.hash) {
    const newAttempts = record.attempts + 1;
    const remaining = MAX_ATTEMPTS - newAttempts;

    if (remaining <= 0) {
      await prisma.otpRecord.delete({ where: { email } });
      await prisma.otpLockout.upsert({
        where: { email },
        update: { lockedUntil: new Date(Date.now() + LOCKOUT_MS) },
        create: { email, lockedUntil: new Date(Date.now() + LOCKOUT_MS) },
      });
      return { success: false, error: "Sai quá 3 lần. Thử lại sau 60 phút." };
    }

    await prisma.otpRecord.update({
      where: { email },
      data: { attempts: newAttempts },
    });
    return { success: false, error: `Mã không đúng. Còn ${remaining} lần thử.` };
  }

  await prisma.otpRecord.delete({ where: { email } });
  const token = randomBytes(32).toString("hex");
  verifiedStore.set(token, { email, expiresAt: Date.now() + VERIFIED_EXPIRY_MS });

  return { success: true, token };
}

export function consumeVerifiedToken(token: string, email: string): boolean {
  const record = verifiedStore.get(token);
  if (!record) return false;
  if (record.email !== email || Date.now() > record.expiresAt) {
    verifiedStore.delete(token);
    return false;
  }
  verifiedStore.delete(token);
  return true;
}

async function cleanupExpired() {
  const now = new Date();
  await Promise.all([
    prisma.otpRecord.deleteMany({ where: { expiresAt: { lt: now } } }),
    prisma.otpLockout.deleteMany({ where: { lockedUntil: { lt: now } } }),
    prisma.otpResendTracker.deleteMany({
      where: { firstSentAt: { lt: new Date(Date.now() - RESEND_WINDOW_MS) } },
    }),
  ]);
}

setInterval(() => {
  cleanupExpired().catch(() => {});
  const now = Date.now();
  for (const [key, r] of verifiedStore) {
    if (now > r.expiresAt) verifiedStore.delete(key);
  }
}, 60_000);
