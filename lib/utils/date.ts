const MS_PER_DAY = 86_400_000;
const VN_TIME_ZONE = "Asia/Ho_Chi_Minh";

function getDatePartsInTimeZone(date: Date, timeZone: string) {
  const parts = new Intl.DateTimeFormat("en-US", {
    timeZone,
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
  }).formatToParts(date);

  const year = Number(parts.find((p) => p.type === "year")?.value);
  const month = Number(parts.find((p) => p.type === "month")?.value);
  const day = Number(parts.find((p) => p.type === "day")?.value);

  return { year, month, day };
}

function getTargetDateParts(dateStr: string | Date) {
  if (typeof dateStr === "string") {
    const m = /^(\d{4})-(\d{2})-(\d{2})$/.exec(dateStr);
    if (m) {
      return {
        year: Number(m[1]),
        month: Number(m[2]),
        day: Number(m[3]),
      };
    }
  }

  const d = typeof dateStr === "string" ? new Date(dateStr) : dateStr;
  return getDatePartsInTimeZone(d, VN_TIME_ZONE);
}

export function daysUntil(dateStr: string | Date): number {
  const target = getTargetDateParts(dateStr);
  const now = getDatePartsInTimeZone(new Date(), VN_TIME_ZONE);

  const targetDay = Date.UTC(target.year, target.month - 1, target.day) / MS_PER_DAY;
  const nowDay = Date.UTC(now.year, now.month - 1, now.day) / MS_PER_DAY;
  return targetDay - nowDay;
}

export function formatDateVN(dateStr: string | Date): string {
  const d = typeof dateStr === "string" ? new Date(dateStr) : dateStr;
  return d.toLocaleDateString("vi-VN", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
  });
}

export function expiryColor(daysLeft: number): string {
  if (daysLeft <= 0) return "text-red-400";
  if (daysLeft <= 7) return "text-red-300";
  if (daysLeft <= 30) return "text-orange-400";
  if (daysLeft <= 90) return "text-yellow-400";
  return "text-emerald-400";
}

export function expiryLabel(daysLeft: number): string {
  if (daysLeft <= 0) return `Đã hết hạn ${Math.abs(daysLeft)} ngày`;
  return `Còn ${daysLeft} ngày`;
}
