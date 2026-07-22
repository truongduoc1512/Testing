import crypto from "crypto";
import { CACHE_SECRET, CRYPTO_SALT } from "@/lib/env";

const ALGORITHM = "aes-256-gcm";
const KEY = crypto.scryptSync(CACHE_SECRET, CRYPTO_SALT, 32);

export function hashKey(key: string): string {
  return crypto.createHmac("sha256", KEY).update(key).digest("hex");
}

export function encryptValue(plaintext: string): string {
  if (isEncrypted(plaintext)) return plaintext;
  const iv = crypto.randomBytes(12);
  const cipher = crypto.createCipheriv(ALGORITHM, KEY, iv);
  const encrypted = Buffer.concat([cipher.update(plaintext, "utf8"), cipher.final()]);
  const tag = cipher.getAuthTag();
  return `${iv.toString("hex")}:${tag.toString("hex")}:${encrypted.toString("hex")}`;
}

export function decryptValue(ciphertext: string): string {
  const [ivHex, tagHex, encHex] = ciphertext.split(":");
  if (!ivHex || !tagHex || !encHex) throw new Error("Invalid ciphertext format");
  const iv = Buffer.from(ivHex, "hex");
  const tag = Buffer.from(tagHex, "hex");
  const encrypted = Buffer.from(encHex, "hex");
  const decipher = crypto.createDecipheriv(ALGORITHM, KEY, iv);
  decipher.setAuthTag(tag);
  return Buffer.concat([decipher.update(encrypted), decipher.final()]).toString("utf8");
}

export function isEncrypted(value: string): boolean {
  if (!value) return false;
  const parts = value.split(":");
  if (parts.length !== 3) return false;
  return (
    /^[0-9a-f]{24}$/.test(parts[0]) &&
    /^[0-9a-f]{32}$/.test(parts[1]) &&
    /^[0-9a-f]+$/.test(parts[2])
  );
}

export function safeDecrypt(value: string): string {
  if (!value) return value;
  if (!isEncrypted(value)) return value;
  try {
    return decryptValue(value);
  } catch {
    throw new Error("Decryption failed");
  }
}
