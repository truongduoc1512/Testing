import { afterEach, beforeEach, describe, expect, it } from "vitest";
import { api, createMockAdmin, deleteAdmin, login } from "./helpers";

describe("POST /api/admin", () => {
  let cookie = "";
  let adminId = "";

  beforeEach(async () => { cookie = await login(); });
  afterEach(async () => { if (adminId) await deleteAdmin(cookie, adminId); });

  it("creates a mock admin", async () => {
    adminId = await createMockAdmin(cookie);
    const response = await api(`/api/admin/${adminId}`, cookie);

    expect(adminId).toMatch(/^adm_/);
    expect(response.status).toBe(200);
  });
});
