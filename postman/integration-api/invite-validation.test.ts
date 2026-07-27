import { afterEach, beforeEach, describe, expect, it } from "vitest";
import { api, createMockAdmin, deleteAdmin, login } from "./helpers";

describe("POST /api/admin/[id]/invite validation", () => {
  let cookie = "";
  let adminId = "";

  beforeEach(async () => { cookie = await login(); adminId = await createMockAdmin(cookie); });
  afterEach(async () => { await deleteAdmin(cookie, adminId); });

  it("rejects an empty email list", async () => {
    const response = await api(`/api/admin/${adminId}/invite`, cookie, {
      method: "POST",
      body: JSON.stringify({ emails: [] }),
    });

    expect(response.status).toBe(400);
    expect((await response.json()).error).toBe("At least 1 email required");
  });
});
