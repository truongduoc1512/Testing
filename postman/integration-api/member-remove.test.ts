import { afterEach, beforeEach, describe, expect, it } from "vitest";
import { api, createMockAdmin, deleteAdmin, inviteMember, login } from "./helpers";

describe("POST /api/admin/[id]/remove-member", () => {
  let cookie = "";
  let adminId = "";
  const memberEmail = "remove@example.com";

  beforeEach(async () => {
    cookie = await login();
    adminId = await createMockAdmin(cookie);
    await inviteMember(cookie, adminId, memberEmail);
  });
  afterEach(async () => { await deleteAdmin(cookie, adminId); });

  it("removes a mock member", async () => {
    const response = await api(`/api/admin/${adminId}/remove-member`, cookie, {
      method: "POST",
      body: JSON.stringify({ memberEmail }),
    });

    expect(response.status).toBe(200);
    expect((await response.json()).success).toBe(true);
  });
});
