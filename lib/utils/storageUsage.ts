export function unitToBytes(value: number, unitRaw: string): number | null {
  if (!Number.isFinite(value)) return null;
  const unit = unitRaw.toUpperCase();
  const scale =
    unit === "TB"
      ? 1024 ** 4
      : unit === "GB"
        ? 1024 ** 3
        : unit === "MB"
          ? 1024 ** 2
          : unit === "KB"
            ? 1024
            : null;
  if (!scale) return null;
  return value * scale;
}

export interface UsagePercent {
  usedLabel: string;
  totalLabel: string;
  percent: number;
}

export function parseUsagePercent(totalUsedText: string | null): UsagePercent | null {
  if (!totalUsedText) return null;
  const match = totalUsedText.match(
    /([0-9]+(?:[.,][0-9]+)?)\s*(KB|MB|GB|TB)\s*out of\s*([0-9]+(?:[.,][0-9]+)?)\s*(KB|MB|GB|TB)\s*used\.?/i,
  );
  if (!match) return null;

  const usedValue = Number(match[1].replace(",", "."));
  const totalValue = Number(match[3].replace(",", "."));
  if (!Number.isFinite(usedValue) || !Number.isFinite(totalValue) || totalValue <= 0) {
    return null;
  }

  const usedBytes = unitToBytes(usedValue, match[2]);
  const totalBytes = unitToBytes(totalValue, match[4]);
  if (usedBytes === null || totalBytes === null || totalBytes <= 0) return null;

  const percent = Math.min(100, Math.max(0, (usedBytes / totalBytes) * 100));
  return {
    usedLabel: `${match[1]} ${match[2].toUpperCase()}`,
    totalLabel: `${match[3]} ${match[4].toUpperCase()}`,
    percent,
  };
}
