import { beforeEach, describe, expect, it, vi } from "vitest";
import { daysUntil } from "@/lib/utils/date";

describe("daysUntil (Asia/Ho_Chi_Minh)", () => {
  beforeEach(() => {
    vi.useFakeTimers();
  });

  it("keeps a stable day count within the same VN day", () => {
    vi.setSystemTime(new Date("2026-03-01T00:01:00+07:00"));
    expect(daysUntil("2026-03-11")).toBe(10);

    vi.setSystemTime(new Date("2026-03-01T23:59:00+07:00"));
    expect(daysUntil("2026-03-11")).toBe(10);
  });

  it("accepts Date object values from API/DB", () => {
    vi.setSystemTime(new Date("2026-03-01T12:00:00+07:00"));
    expect(daysUntil(new Date("2026-03-11T00:00:00.000Z"))).toBe(10);
  });
});
