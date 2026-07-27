import { afterEach, beforeEach, describe, expect, it } from "vitest";
import { api, createMockAdmin, deleteAdmin, inviteMember, login } from "./helpers";

describe("PATCH /api/admin/[id]/members", () => {
  let cookie = "";
  let adminId = "";
  const memberEmail = "update@example.com";

  beforeEach(async () => {
    cookie = await login();
    adminId = await createMockAdmin(cookie);
    await inviteMember(cookie, adminId, memberEmail);
  });
  afterEach(async () => { await deleteAdmin(cookie, adminId); });

  it("updates member dates and renewal", async () => {
    const response = await api(`/api/admin/${adminId}/members`, cookie, {
      method: "PATCH",
      body: JSON.stringify({
        memberEmail,
        startDate: "2026-07-27",
        endDate: "2026-08-27",
        renewed: true,
      }),
    });
    const body = await response.json();

    expect(response.status).toBe(200);
    expect(body.renewed).toBe(true);
    expect(body.startDate).toContain("2026-07-27");
  });
});
