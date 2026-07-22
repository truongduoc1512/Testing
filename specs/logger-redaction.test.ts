import { describe, expect, it } from "vitest";
import { redactLogString, redactLogValue } from "@/lib/logger";

describe("logger redaction", () => {
  it("redacts sensitive inline values in log strings", () => {
    const input =
      "authorization=Bearer abc123 token=tok123 otpCode=987654 cookie=sid=1";
    const output = redactLogString(input);

    expect(output).not.toContain("abc123");
    expect(output).not.toContain("tok123");
    expect(output).not.toContain("987654");
    expect(output).not.toContain("sid=1");
    expect(output).toContain("[REDACTED]");
  });

  it("redacts nested sensitive fields in metadata objects", () => {
    const redacted = redactLogValue({
      password: "p@ss",
      nested: {
        googlePassword: "gp",
        totpSecret: "totp-secret",
        otpCode: "123456",
        headers: {
          Authorization: "Bearer xyz",
          "set-cookie": "sid=abc",
        },
      },
      tokens: [{ accessToken: "access" }, { refreshToken: "refresh" }],
      member: { userId: "507f1f77bcf86cd799439011", safe: "ok" },
    }) as Record<string, unknown>;

    expect(redacted.password).toBe("[REDACTED]");
    const nested = redacted.nested as Record<string, unknown>;
    expect(nested.googlePassword).toBe("[REDACTED]");
    expect(nested.totpSecret).toBe("[REDACTED]");
    expect(nested.otpCode).toBe("[REDACTED]");

    const headers = nested.headers as Record<string, unknown>;
    expect(headers.Authorization).toBe("[REDACTED]");
    expect(headers["set-cookie"]).toBe("[REDACTED]");

    expect(redacted.tokens).toBe("[REDACTED]");

    const member = redacted.member as Record<string, unknown>;
    expect(member.userId).toBe("[REDACTED]");
    expect(member.safe).toBe("ok");
  });
});
