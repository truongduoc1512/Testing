import { afterEach, beforeEach, describe, expect, it } from "vitest";
import { api, createMockAdmin, deleteAdmin, login } from "./helpers";

describe("GET /api/admin/[id]", () => {
  let cookie = "";
  let adminId = "";

  beforeEach(async () => { cookie = await login(); adminId = await createMockAdmin(cookie); });
  afterEach(async () => { await deleteAdmin(cookie, adminId); });

  it("returns an owned admin", async () => {
    const response = await api(`/api/admin/${adminId}`, cookie);
    expect(response.status).toBe(200);
    expect((await response.json()).id).toBe(adminId);
  });
});
