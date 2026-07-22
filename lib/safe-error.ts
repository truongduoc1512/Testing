const STACK_FRAGMENT_RE =
  /\bat\s+[A-Za-z0-9_$.<>]+\s+\(|\b(?:TypeError|ReferenceError|SyntaxError|RangeError|Error):/i;
const UNSAFE_DETAIL_RE =
  /(authorization|set-cookie|cookie|bearer|token|secret|password|otp|totp|myaccount\.google|batchexecute|accountsettingsui|scanner\/|prisma|postgres|mongodb|redis|internal service|attoken|xsrf|rpc|wffnob|c5gch)/i;
const IDENTIFIER_FRAGMENT_RE =
  /[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}|\b[0-9a-f]{24,}\b|\b\d{10,}\b/i;
const STATUS_CODE_FRAGMENT_RE =
  /\b(?:http|status)\s*[:=]?\s*\d{3}\b/i;

function normalizeMessage(input: string): string {
  return input.replace(/\s+/g, " ").trim();
}

function mapKnownOperationalMessage(message: string): string | null {
  const lower = message.toLowerCase();
  if (lower.includes("session")) return "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.";
  if (lower.includes("invalid json") || lower.includes("invalid data")) return "Yêu cầu không hợp lệ.";
  if (lower.includes("missing ")) return "Yêu cầu không hợp lệ.";
  if (lower.includes("unauthorized") || lower.includes("forbidden")) return "Không có quyền truy cập.";
  if (lower.includes("not found")) return "Không tìm thấy dữ liệu.";
  if (lower.includes("rate limit") || lower.includes("too many")) {
    return "Quá nhiều yêu cầu. Vui lòng thử lại sau.";
  }
  return null;
}

export function safeErrorMessage(
  input: unknown,
  fallback: string,
): string {
  const raw =
    typeof input === "string"
      ? input
      : input instanceof Error
        ? input.message
        : "";
  const normalized = normalizeMessage(raw);
  if (!normalized) return fallback;

  const mapped = mapKnownOperationalMessage(normalized);
  if (mapped) return mapped;

  if (
    STACK_FRAGMENT_RE.test(normalized) ||
    UNSAFE_DETAIL_RE.test(normalized) ||
    STATUS_CODE_FRAGMENT_RE.test(normalized) ||
    IDENTIFIER_FRAGMENT_RE.test(normalized) ||
    normalized.length > 160
  ) {
    return fallback;
  }

  return normalized;
}
