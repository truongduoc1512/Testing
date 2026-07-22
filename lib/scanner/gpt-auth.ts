const DEAD_AUTH_MARKERS = [
  "account_deactivated",
  "token_revoked",
  "invalidated oauth token",
  "account suspended",
  "account banned",
  "account disabled",
];

const DEAD_AUTH_RESPONSE_MARKERS = [
  "token_revoked",
  "account_deactivated",
  "suspended",
  "banned",
  "disabled",
  "invalidated oauth token",
];

export function detectDeadGptAuthReason(text: string): string | null {
  const normalized = text.toLowerCase();
  if (normalized.includes("account_deactivated")) return "account_deactivated";
  if (/account.*(?:banned|suspended|disabled)/i.test(normalized)) {
    return "account suspended/banned";
  }
  if (normalized.includes("token_revoked")) return "token_revoked";
  if (normalized.includes("invalidated oauth token")) return "token_revoked";
  return null;
}

export function isDeadGptErrorMessage(message: string): boolean {
  const normalized = message.toLowerCase();
  if (normalized.includes("account_dead")) return true;
  return DEAD_AUTH_MARKERS.some((marker) => normalized.includes(marker));
}

export function isDeadGptApiResponse(
  status: number,
  errorCode: string,
  body: string,
): boolean {
  if (status === 401) return true;
  if (status !== 403) return false;

  const normalizedCode = errorCode.toLowerCase();
  const normalizedBody = body.toLowerCase();
  return DEAD_AUTH_RESPONSE_MARKERS.some(
    (marker) => normalizedCode.includes(marker) || normalizedBody.includes(marker),
  );
}
