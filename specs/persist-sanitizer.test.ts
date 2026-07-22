import { describe, expect, it } from "vitest";
import {
  GOOGLE_MEMBER_STORAGE_CACHE_KEY,
  GOOGLE_PROFILES_CACHE_KEY,
  sanitizePersistedPayload,
  sanitizePersistedSelectionId,
} from "@/lib/browser/persist";

describe("sanitizePersistedPayload", () => {
  it("allowlists google profile cache and drops secret-like fields", () => {
    const sanitized = sanitizePersistedPayload(GOOGLE_PROFILES_CACHE_KEY, {
      "admin-1": {
        loading: true,
        data: {
          success: true,
          language: "vi",
          checkedAt: "2026-04-19T10:00:00.000Z",
          subscription: {
            planName: "Google One",
            planFullName: "Google One AI Pro",
            expiresAt: "2026-05-01T00:00:00.000Z",
            token: "must-not-persist",
          },
          paymentProfile: {
            exists: true,
            profileId: "abc",
            email: "safe@example.com",
            secret: "must-not-persist",
          },
          otpCode: "must-not-persist",
          writePersistedValue: "must-not-persist",
          unexpectedNewField: "must-not-persist",
        },
        error: "safe error",
        googlePassword: "must-not-persist",
      },
    }) as Record<string, unknown>;

    const admin = sanitized["admin-1"] as Record<string, unknown>;
    expect(admin).toBeDefined();
    expect(admin).not.toHaveProperty("googlePassword");

    const data = admin.data as Record<string, unknown>;
    expect(data).toMatchObject({
      success: true,
      language: "vi",
      checkedAt: "2026-04-19T10:00:00.000Z",
    });
    expect(data).not.toHaveProperty("otpCode");
    expect(data).not.toHaveProperty("writePersistedValue");
    expect(data).not.toHaveProperty("unexpectedNewField");

    const subscription = data.subscription as Record<string, unknown>;
    expect(subscription).toMatchObject({
      planName: "Google One",
      planFullName: "Google One AI Pro",
      expiresAt: "2026-05-01T00:00:00.000Z",
    });
    expect(subscription).not.toHaveProperty("token");

    const paymentProfile = data.paymentProfile as Record<string, unknown>;
    expect(paymentProfile).toMatchObject({
      exists: true,
      profileId: "abc",
      email: "safe@example.com",
    });
    expect(paymentProfile).not.toHaveProperty("secret");
  });

  it("drops nested sensitive identifiers from member storage cache", () => {
    const sanitized = sanitizePersistedPayload(GOOGLE_MEMBER_STORAGE_CACHE_KEY, {
      "admin-1": {
        totalUsedText: "1 GB / 2 TB",
        totalUsedBytes: 1024,
        members: [
          {
            name: "A",
            email: "a@example.com",
            googleUserId: "must-not-persist",
            usedText: "100 MB",
            usedBytes: 100,
            token: "must-not-persist",
          },
        ],
      },
    }) as Record<string, unknown>;

    const usage = sanitized["admin-1"] as Record<string, unknown>;
    const members = usage.members as Array<Record<string, unknown>>;
    expect(members).toHaveLength(1);
    expect(members[0]).toMatchObject({
      name: "A",
      email: "a@example.com",
      usedText: "100 MB",
      usedBytes: 100,
    });
    expect(members[0]).not.toHaveProperty("googleUserId");
    expect(members[0]).not.toHaveProperty("token");
  });

  it("allowlists persisted selected-admin key format", () => {
    expect(sanitizePersistedSelectionId("admin_123-xyz")).toBe("admin_123-xyz");
    expect(sanitizePersistedSelectionId("  adminA  ")).toBe("adminA");
    expect(sanitizePersistedSelectionId("")).toBeNull();
    expect(sanitizePersistedSelectionId("../admin")).toBeNull();
    expect(sanitizePersistedSelectionId('admin";drop')).toBeNull();
    expect(sanitizePersistedSelectionId({ id: "admin" })).toBeNull();
  });
});
