import { beforeEach, describe, expect, it, vi } from "vitest";

const {
  prismaMock,
  getSessionMock,
  resolveOwnedAdminIdMock,
  credentialsLimiterCheckMock,
  totpLimiterCheckMock,
  googleClockMock,
} = vi.hoisted(() => ({
  prismaMock: {
    adminAccount: {
      findFirst: vi.fn(),
    },
  },
  getSessionMock: vi.fn(),
  resolveOwnedAdminIdMock: vi.fn(),
  credentialsLimiterCheckMock: vi.fn(),
  totpLimiterCheckMock: vi.fn(),
  googleClockMock: {
    ensureFresh: vi.fn(),
    now: vi.fn(),
  },
}));

vi.mock("@/lib/db", () => ({
  prisma: prismaMock,
}));

vi.mock("@/lib/auth/session", () => ({
  getSession: getSessionMock,
}));

vi.mock("@/lib/admin-access", () => ({
  resolveOwnedAdminId: resolveOwnedAdminIdMock,
}));

vi.mock("@/lib/rate-limit", () => ({
  buildRateLimitKey: vi.fn(() => "test-key"),
  credentialsLimiter: {
    check: credentialsLimiterCheckMock,
  },
  totpLimiter: {
    check: totpLimiterCheckMock,
  },
  rateLimitResponse: vi.fn(() => new Response(null, { status: 429 })),
}));

vi.mock("@/lib/logger", () => ({
  apiError: vi.fn(),
}));

vi.mock("@/lib/time/google-clock-instance", () => ({
  googleClock: googleClockMock,
}));

import { GET as getCredentials } from "@/app/api/admin/[id]/credentials/route";
import { GET as getTotpStatus } from "@/app/api/admin/[id]/totp/route";
import { GET as getTotpSecretMeta } from "@/app/api/admin/[id]/totp/secret/route";

const ADMIN_ID = "admin-1";

function adminParams(): { params: Promise<{ id: string }> } {
  return { params: Promise.resolve({ id: ADMIN_ID }) };
}

function makeAdminRequest(path: string): Request {
  return new Request(`http://localhost/api/admin/${ADMIN_ID}${path}`);
}

function hasKeyDeep(value: unknown, key: string): boolean {
  if (!value || typeof value !== "object") return false;
  if (Array.isArray(value)) return value.some((item) => hasKeyDeep(item, key));
  const obj = value as Record<string, unknown>;
  if (Object.prototype.hasOwnProperty.call(obj, key)) return true;
  return Object.values(obj).some((item) => hasKeyDeep(item, key));
}

function expectNoSessionKeys(payload: unknown) {
  const forbidden = [
    "otpCode",
    "token",
    "accessToken",
    "refreshToken",
    "authorization",
    "cookie",
  ];
  for (const key of forbidden) {
    expect(hasKeyDeep(payload, key)).toBe(false);
  }
}

describe("credentials/totp endpoints response contract", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    getSessionMock.mockResolvedValue({ sub: "user-1" });
    resolveOwnedAdminIdMock.mockResolvedValue("admin-1");
    credentialsLimiterCheckMock.mockReturnValue({ allowed: true, resetMs: 0 });
    totpLimiterCheckMock.mockReturnValue({ allowed: true, resetMs: 0 });
    googleClockMock.ensureFresh.mockResolvedValue(undefined);
    googleClockMock.now.mockReturnValue(1713500000000);
  });

  it("credentials route returns saved credentials for the owned admin UI", async () => {
    prismaMock.adminAccount.findFirst.mockResolvedValue({
      googlePassword: "encrypted-pass",
      totpSecret: "encrypted-totp",
      has2FA: true,
      updatedAt: new Date("2026-04-19T10:00:00.000Z"),
    });

    const response = await getCredentials(
      makeAdminRequest("/credentials"),
      adminParams(),
    );
    const json = await response.json();

    expect(response.status).toBe(200);
    expect(json).toMatchObject({
      googlePassword: "encrypted-pass",
      totpSecret: "encrypted-totp",
      hasPassword: true,
      hasTotp: true,
      totpEnabled: true,
    });
    expectNoSessionKeys(json);
  });

  it("totp status route does not expose raw secret fields", async () => {
    prismaMock.adminAccount.findFirst.mockResolvedValue({
      totpSecret: "encrypted-totp",
      has2FA: true,
      updatedAt: new Date("2026-04-19T10:00:00.000Z"),
    });

    const response = await getTotpStatus(
      makeAdminRequest("/totp"),
      adminParams(),
    );
    const json = await response.json();

    expect(response.status).toBe(200);
    expect(json).toMatchObject({
      totpEnabled: true,
      period: 30,
    });
    expectNoSessionKeys(json);
  });

  it("totp secret route returns the saved TOTP secret for the owned admin UI", async () => {
    prismaMock.adminAccount.findFirst.mockResolvedValue({
      totpSecret: "encrypted-totp",
      has2FA: false,
      updatedAt: new Date("2026-04-19T10:00:00.000Z"),
    });

    const response = await getTotpSecretMeta(
      makeAdminRequest("/totp/secret"),
      adminParams(),
    );
    const json = await response.json();

    expect(response.status).toBe(200);
    expect(json).toMatchObject({
      hasTotp: true,
      totpEnabled: true,
      totpSecret: "encrypted-totp",
    });
    expectNoSessionKeys(json);
  });
});
