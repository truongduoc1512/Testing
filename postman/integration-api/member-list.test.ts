import { afterEach, beforeEach, describe, expect, it } from "vitest";
import { api, createMockAdmin, deleteAdmin, inviteMember, login } from "./helpers";

describe("GET /api/admin/[id]/members", () => {
  let cookie = "";
  let adminId = "";
  const memberEmail = "list@example.com";

  beforeEach(async () => {
    cookie = await login();
    adminId = await createMockAdmin(cookie);
    await inviteMember(cookie, adminId, memberEmail);
  });
  afterEach(async () => { await deleteAdmin(cookie, adminId); });

  it("lists the invited member", async () => {
    const response = await api(`/api/admin/${adminId}/members`, cookie);
    const body = await response.json();

    expect(response.status).toBe(200);
    expect(body.members).toContainEqual(expect.objectContaining({ email: memberEmail }));
  });
});
