import fs from "fs";
import path from "path";

type LogLevel = "error" | "warn" | "info";

const LOG_DIR = path.join(process.cwd(), "logs");
const LOG_FILE = path.join(LOG_DIR, "app.log");

const SENSITIVE_IDENTIFIER_KEYS = new Set([
  "email",
  "adminid",
  "userid",
  "memberid",
  "accountid",
  "inviteid",
  "googleuserid",
  "sub",
]);

function normalizeKey(key: string): string {
  return key.toLowerCase().replace(/[^a-z0-9]/g, "");
}

function isSensitiveKey(key: string): boolean {
  const normalized = normalizeKey(key);
  if (!normalized) return false;
  if (SENSITIVE_IDENTIFIER_KEYS.has(normalized)) return true;
  return (
    normalized.includes("password") ||
    normalized.includes("secret") ||
    normalized.includes("totp") ||
    normalized.includes("otp") ||
    normalized.includes("token") ||
    normalized.includes("authorization") ||
    normalized.includes("cookie") ||
    normalized.includes("apikey")
  );
}

export function redactLogString(value: string): string {
  return value
    .replace(/(bearer\s+)[a-z0-9\-._~+/]+=*/gi, "$1[REDACTED]")
    .replace(
      /([?&](?:token|access_token|refresh_token|otp|otp_code|authorization|cookie|set-cookie)=)[^&\s]+/gi,
      "$1[REDACTED]",
    )
    .replace(
      /(password|googlepassword|secret|totp|totpsecret|otp|otpcode|token|accesstoken|refreshtoken|authorization|cookie|set-cookie|email|user(id)?|member(id)?|account(id)?|invite(id)?|googleuserid|sub)\s*[:=]\s*[^,\s]+/gi,
      "$1=[REDACTED]",
    );
}

export function redactLogValue(value: unknown, depth = 0): unknown {
  if (depth > 4) return "[TRUNCATED]";
  if (typeof value === "string") return redactLogString(value);
  if (Array.isArray(value)) return value.map((v) => redactLogValue(v, depth + 1));
  if (!value || typeof value !== "object") return value;

  const input = value as Record<string, unknown>;
  const output: Record<string, unknown> = {};
  for (const [key, val] of Object.entries(input)) {
    output[key] = isSensitiveKey(key) ? "[REDACTED]" : redactLogValue(val, depth + 1);
  }
  return output;
}

function formatLog(
  level: LogLevel,
  route: string,
  message: string,
  meta?: Record<string, unknown>,
) {
  const ts = new Date().toISOString();
  const safeMessage = redactLogString(message);
  const safeMeta = meta ? redactLogValue(meta) : undefined;
  const metaStr = safeMeta ? ` ${JSON.stringify(safeMeta)}` : "";
  return `[${ts}] [${level.toUpperCase()}] ${route}: ${safeMessage}${metaStr}`;
}

function writeLogLine(line: string) {
  try {
    if (!fs.existsSync(LOG_DIR)) fs.mkdirSync(LOG_DIR, { recursive: true });
    fs.appendFileSync(LOG_FILE, `${line}\n`, "utf8");
  } catch {
    // swallow: logging must never break request flow
  }
}

export function apiError(route: string, error: unknown, meta?: Record<string, unknown>) {
  const message = error instanceof Error ? error.message : String(error);
  writeLogLine(formatLog("error", route, message, meta));
  if (error instanceof Error && error.stack) {
    writeLogLine(redactLogString(error.stack));
  }
}

export function apiWarn(route: string, message: string, meta?: Record<string, unknown>) {
  writeLogLine(formatLog("warn", route, message, meta));
}

export function apiInfo(route: string, message: string, meta?: Record<string, unknown>) {
  writeLogLine(formatLog("info", route, message, meta));
}
