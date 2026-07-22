import { beforeEach, describe, expect, it, vi } from "vitest";

const { prismaMock, getSessionMock, apiErrorMock, resolveOwnedAdminIdMock } = vi.hoisted(() => ({
  prismaMock: {
    adminAccount: {
      findFirst: vi.fn(),
    },
    familyMember: {
      findFirst: vi.fn(),
      update: vi.fn(),
    },
  },
  getSessionMock: vi.fn(),
  apiErrorMock: vi.fn(),
  resolveOwnedAdminIdMock: vi.fn(),
}));

vi.mock("@/lib/db", () => ({
  prisma: prismaMock,
}));

vi.mock("@/lib/auth/session", () => ({
  getSession: getSessionMock,
}));

vi.mock("@/lib/logger", () => ({
  apiError: apiErrorMock,
}));

vi.mock("@/lib/admin-access", () => ({
  resolveOwnedAdminId: resolveOwnedAdminIdMock,
}));

import { PATCH } from "@/app/api/admin/[id]/members/route";

function makeRequest(body: unknown): Request {
  return new Request("http://localhost/api/admin/admin-1/members", {
    method: "PATCH",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });
}

describe("PATCH /api/admin/[id]/members", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    resolveOwnedAdminIdMock.mockResolvedValue("admin-1");
  });

  it("updates startDate/endDate/renewed for a member", async () => {
    getSessionMock.mockResolvedValue({ sub: "user-1" });
    prismaMock.adminAccount.findFirst.mockResolvedValue({ id: "admin-1" });
    prismaMock.familyMember.findFirst.mockResolvedValue({
      id: "member-1",
      adminId: "admin-1",
    });

    const updatedRow = {
      id: "member-1",
      startDate: new Date("2026-03-25T00:00:00.000Z"),
      endDate: new Date("2026-08-25T00:00:00.000Z"),
      renewed: true,
    };
    prismaMock.familyMember.update.mockResolvedValue(updatedRow);

    const res = await PATCH(
      makeRequest({
        memberEmail: "member-1@example.com",
        startDate: "2026-03-25",
        endDate: "2026-08-25",
        renewed: true,
      }),
      { params: Promise.resolve({ id: "admin-1" }) },
    );

    expect(res.status).toBe(200);
    expect(prismaMock.familyMember.update).toHaveBeenCalledWith({
      where: { id: "member-1" },
      data: {
        startDate: new Date("2026-03-25"),
        endDate: new Date("2026-08-25"),
        renewed: true,
      },
    });

    const json = await res.json();
    expect(json).toEqual({
      startDate: updatedRow.startDate.toISOString(),
      endDate: updatedRow.endDate.toISOString(),
      renewed: true,
    });
  });

  it("returns 400 when payload has invalid date", async () => {
    getSessionMock.mockResolvedValue({ sub: "user-1" });

    const res = await PATCH(
      makeRequest({
        memberEmail: "member-1@example.com",
        startDate: "not-a-date",
      }),
      { params: Promise.resolve({ id: "admin-1" }) },
    );

    expect(res.status).toBe(400);
    expect(prismaMock.familyMember.update).not.toHaveBeenCalled();
  });

  it("returns 401 when user is not authenticated", async () => {
    getSessionMock.mockResolvedValue(null);

    const res = await PATCH(
      makeRequest({
        memberEmail: "member-1@example.com",
        startDate: "2026-03-25",
        endDate: "2026-08-25",
        renewed: true,
      }),
      { params: Promise.resolve({ id: "admin-1" }) },
    );

    expect(res.status).toBe(401);
    expect(prismaMock.adminAccount.findFirst).not.toHaveBeenCalled();
  });
});
