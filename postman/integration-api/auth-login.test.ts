import { describe, expect, it } from "vitest";
import { login } from "./helpers";

describe("POST /api/auth/login", () => {
  it("logs in with the configured owner account", async () => {
    expect(await login()).toMatch(/^vieshop-token=/);
  });
});
