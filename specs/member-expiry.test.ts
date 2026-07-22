import { beforeEach, describe, expect, it, vi } from "vitest";
import { expiryLabel } from "@/lib/utils";
import { getMemberExpiryMeta } from "@/lib/utils/memberExpiry";

describe("member expiry render logic", () => {
  beforeEach(() => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date("2026-03-01T00:00:00.000Z"));
  });

  it("returns days-left meta for active member", () => {
    const meta = getMemberExpiryMeta({
      endDate: "2026-03-11T00:00:00.000Z",
      role: "Member",
      status: "active",
    });

    expect(meta).toEqual({
      daysLeft: 10,
      colorClass: "text-orange-400",
      badgeClass: "border border-yellow-500/25 bg-yellow-500/10 text-yellow-300",
      label: expiryLabel(10),
    });
  });

  it("returns renewed badge for renewed member", () => {
    const meta = getMemberExpiryMeta({
      endDate: "2026-03-11T00:00:00.000Z",
      role: "Member",
      status: "active",
      renewed: true,
    });

    expect(meta).toEqual({
      daysLeft: 10,
      colorClass: "text-emerald-300",
      badgeClass: "border border-emerald-500/25 bg-emerald-500/10 text-emerald-300",
      label: "Đã gia hạn · còn 10 ngày",
    });
  });

  it("returns renewed label without days when renewed member has no end date", () => {
    const meta = getMemberExpiryMeta({
      endDate: null,
      role: "Member",
      status: "active",
      renewed: true,
    });

    expect(meta).toEqual({
      daysLeft: Number.POSITIVE_INFINITY,
      colorClass: "text-emerald-300",
      badgeClass: "border border-emerald-500/25 bg-emerald-500/10 text-emerald-300",
      label: "Đã gia hạn",
    });
  });

  it("returns null for invited member", () => {
    const meta = getMemberExpiryMeta({
      endDate: "2026-03-11T00:00:00.000Z",
      role: "Member",
      status: "invited",
    });

    expect(meta).toBeNull();
  });

  it("returns null for manager member", () => {
    const meta = getMemberExpiryMeta({
      endDate: "2026-03-11T00:00:00.000Z",
      role: "FAMILY MANAGER",
      status: "active",
    });

    expect(meta).toBeNull();
  });
});
