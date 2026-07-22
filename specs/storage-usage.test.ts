import { describe, expect, it } from "vitest";
import { parseUsagePercent, unitToBytes } from "@/lib/utils/storageUsage";

describe("storage usage utils", () => {
  it("converts units to bytes", () => {
    expect(unitToBytes(1, "KB")).toBe(1024);
    expect(unitToBytes(1, "MB")).toBe(1024 ** 2);
    expect(unitToBytes(1, "GB")).toBe(1024 ** 3);
    expect(unitToBytes(1, "TB")).toBe(1024 ** 4);
  });

  it("returns null for invalid unit", () => {
    expect(unitToBytes(1, "XB")).toBeNull();
  });

  it("parses usage text and computes percent", () => {
    const result = parseUsagePercent("512 MB out of 2 GB used.");
    expect(result).toEqual({
      usedLabel: "512 MB",
      totalLabel: "2 GB",
      percent: 25,
    });
  });

  it("returns null on malformed text", () => {
    expect(parseUsagePercent("n/a")).toBeNull();
    expect(parseUsagePercent(null)).toBeNull();
  });
});
