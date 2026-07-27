import { beforeEach, describe, expect, it } from "vitest";
import { api, createMockAdmin, login } from "./helpers";

describe("DELETE /api/admin/[id]", () => {
  let cookie = "";
  let adminId = "";

  beforeEach(async () => { cookie = await login(); adminId = await createMockAdmin(cookie); });

  it("deletes an owned admin", async () => {
    const removed = await api(`/api/admin/${adminId}`, cookie, { method: "DELETE" });
    const missing = await api(`/api/admin/${adminId}`, cookie);

    expect(removed.status).toBe(200);
    expect(missing.status).toBe(404);
  });
});
