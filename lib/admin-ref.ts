const RAW_ADMIN_ID_RE = /^[a-f0-9]{24}$/i;
const PUBLIC_PREFIX = "a_";

function fromBase64(input: string): string {
  if (typeof Buffer !== "undefined") {
    return Buffer.from(input, "base64").toString("utf8");
  }
  if (typeof atob !== "undefined") {
    return atob(input);
  }
  throw new Error("No base64 decoder available");
}

function fromBase64Url(input: string): string {
  const base64 = input.replace(/-/g, "+").replace(/_/g, "/");
  const padded = base64 + "=".repeat((4 - (base64.length % 4)) % 4);
  return fromBase64(padded);
}

export function decodeAdminRef(value: string): string | null {
  if (!value) return null;
  if (RAW_ADMIN_ID_RE.test(value)) return value;
  if (!value.startsWith(PUBLIC_PREFIX)) return null;

  const encoded = value.slice(PUBLIC_PREFIX.length);
  if (!encoded || !/^[A-Za-z0-9_-]+$/.test(encoded)) return null;

  try {
    const decoded = fromBase64Url(encoded);
    return RAW_ADMIN_ID_RE.test(decoded) ? decoded : null;
  } catch {
    return null;
  }
}
