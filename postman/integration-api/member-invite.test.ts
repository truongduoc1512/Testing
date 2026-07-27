import { afterEach, beforeEach, describe, expect, it } from "vitest";
import { createMockAdmin, deleteAdmin, inviteMember, login } from "./helpers";

describe("POST /api/admin/[id]/invite", () => {
  let cookie = "";
  let adminId = "";

  beforeEach(async () => { cookie = await login(); adminId = await createMockAdmin(cookie); });
  afterEach(async () => { await deleteAdmin(cookie, adminId); });

  it("invites a mock member", async () => {
    const response = await inviteMember(cookie, adminId, "invite@example.com");
    const body = await response.json();

    expect(response.status).toBe(200);
    expect(body.results[0].success).toBe(true);
  });
});
