export const GOOGLE_PROFILES_CACHE_KEY = "google_profiles_cache";
export const GOOGLE_MEMBER_STORAGE_CACHE_KEY = "google_member_storage_cache";
export const GOOGLE_SHARING_STATUS_CACHE_KEY = "google_sharing_status_cache";
export const CHECK_PROFILE_SELECTED_ADMIN_ID_KEY = "check_profile_selected_admin_id";

type JsonObject = Record<string, unknown>;

function asObject(value: unknown): JsonObject | null {
  if (!value || typeof value !== "object" || Array.isArray(value)) return null;
  return value as JsonObject;
}

function asString(value: unknown, maxLen = 400): string | undefined {
  if (typeof value !== "string") return undefined;
  const trimmed = value.trim();
  if (!trimmed) return undefined;
  return trimmed.slice(0, maxLen);
}

function asBoolean(value: unknown): boolean | undefined {
  if (typeof value !== "boolean") return undefined;
  return value;
}

function asNullableBoolean(value: unknown): boolean | null | undefined {
  if (value === null) return null;
  if (typeof value !== "boolean") return undefined;
  return value;
}

function asNullableNumber(value: unknown): number | null | undefined {
  if (value === null) return null;
  if (typeof value !== "number" || !Number.isFinite(value)) return undefined;
  return value;
}

function asStringArray(value: unknown, maxItems = 20, maxLen = 400): string[] | undefined {
  if (!Array.isArray(value)) return undefined;
  const out: string[] = [];
  for (const item of value.slice(0, maxItems)) {
    const parsed = asString(item, maxLen);
    if (parsed) out.push(parsed);
  }
  return out;
}

function sanitizeSubscription(input: unknown): JsonObject | undefined {
  const obj = asObject(input);
  if (!obj) return undefined;
  const out: JsonObject = {};
  const planFullName = asString(obj.planFullName, 200);
  const planName = asString(obj.planName, 200);
  const expiresAt = asString(obj.expiresAt, 80);
  if (planFullName) out.planFullName = planFullName;
  if (planName) out.planName = planName;
  if (expiresAt) out.expiresAt = expiresAt;
  return Object.keys(out).length > 0 ? out : undefined;
}

function sanitizePaymentProfile(input: unknown): JsonObject | undefined {
  const obj = asObject(input);
  if (!obj) return undefined;
  const out: JsonObject = {};
  const exists = asBoolean(obj.exists);
  const profileName = asString(obj.profileName, 200);
  const profileId = asString(obj.profileId, 120);
  const profileIdFormatted = asString(obj.profileIdFormatted, 120);
  const country = asString(obj.country, 80);
  const email = asString(obj.email, 200);
  if (exists !== undefined) out.exists = exists;
  if (profileName) out.profileName = profileName;
  if (profileId) out.profileId = profileId;
  if (profileIdFormatted) out.profileIdFormatted = profileIdFormatted;
  if (country) out.country = country;
  if (email) out.email = email;
  return Object.keys(out).length > 0 ? out : undefined;
}

function sanitizeProfileData(input: unknown): JsonObject | undefined {
  const obj = asObject(input);
  if (!obj) return undefined;
  const out: JsonObject = {};
  const success = asBoolean(obj.success);
  const language = asString(obj.language, 80);
  const checkedAt = asString(obj.checkedAt, 80);
  const sharingStatus = asNullableBoolean(obj.sharingStatus);
  const sharingError = asString(obj.sharingError, 300) ?? (obj.sharingError === null ? null : undefined);
  const error = asString(obj.error, 300);
  const errors = asStringArray(obj.errors, 20, 300);
  const subscription = sanitizeSubscription(obj.subscription);
  const paymentProfile = sanitizePaymentProfile(obj.paymentProfile);

  if (success !== undefined) out.success = success;
  if (language) out.language = language;
  if (checkedAt) out.checkedAt = checkedAt;
  if (sharingStatus !== undefined) out.sharingStatus = sharingStatus;
  if (sharingError !== undefined) out.sharingError = sharingError;
  if (error) out.error = error;
  if (errors && errors.length > 0) out.errors = errors;
  if (subscription) out.subscription = subscription;
  if (paymentProfile) out.paymentProfile = paymentProfile;

  return Object.keys(out).length > 0 ? out : undefined;
}

function sanitizeProfilesCache(input: unknown): JsonObject {
  const cache = asObject(input);
  if (!cache) return {};
  const out: JsonObject = {};
  for (const [adminId, rawEntry] of Object.entries(cache)) {
    const entry = asObject(rawEntry);
    if (!entry) continue;
    const safeEntry: JsonObject = {};
    const loading = asBoolean(entry.loading);
    const error = asString(entry.error, 300);
    const data = sanitizeProfileData(entry.data);
    if (loading !== undefined) safeEntry.loading = loading;
    if (error) safeEntry.error = error;
    if (data) safeEntry.data = data;
    if (Object.keys(safeEntry).length > 0) out[adminId] = safeEntry;
  }
  return out;
}

function sanitizeMemberStorageMember(input: unknown): JsonObject | undefined {
  const obj = asObject(input);
  if (!obj) return undefined;
  const out: JsonObject = {};
  const name = asString(obj.name, 200);
  const email = asString(obj.email, 200);
  const usedText = asString(obj.usedText, 120) ?? (obj.usedText === null ? null : undefined);
  const usedBytes = asNullableNumber(obj.usedBytes);
  if (name) out.name = name;
  if (email) out.email = email;
  if (usedText !== undefined) out.usedText = usedText;
  if (usedBytes !== undefined) out.usedBytes = usedBytes;
  return Object.keys(out).length > 0 ? out : undefined;
}

function sanitizeMemberStorageCache(input: unknown): JsonObject {
  const cache = asObject(input);
  if (!cache) return {};
  const out: JsonObject = {};
  for (const [adminId, rawUsage] of Object.entries(cache)) {
    const usage = asObject(rawUsage);
    if (!usage) continue;
    const safeUsage: JsonObject = {};
    const totalUsedText =
      asString(usage.totalUsedText, 120) ??
      (usage.totalUsedText === null ? null : undefined);
    const totalUsedBytes = asNullableNumber(usage.totalUsedBytes);
    const error = asString(usage.error, 300);
    const membersRaw = Array.isArray(usage.members) ? usage.members : [];
    const members = membersRaw
      .slice(0, 100)
      .map((member) => sanitizeMemberStorageMember(member))
      .filter((member): member is JsonObject => Boolean(member));
    if (totalUsedText !== undefined) safeUsage.totalUsedText = totalUsedText;
    if (totalUsedBytes !== undefined) safeUsage.totalUsedBytes = totalUsedBytes;
    safeUsage.members = members;
    if (error) safeUsage.error = error;
    out[adminId] = safeUsage;
  }
  return out;
}

function sanitizeSharingStatusCache(input: unknown): JsonObject {
  const cache = asObject(input);
  if (!cache) return {};
  const out: JsonObject = {};
  for (const [adminId, value] of Object.entries(cache)) {
    const safeValue = asNullableBoolean(value);
    if (safeValue !== undefined) out[adminId] = safeValue;
  }
  return out;
}

type PersistSanitizer = (value: unknown) => unknown;

const PERSIST_SANITIZERS: Record<string, PersistSanitizer> = {
  [GOOGLE_PROFILES_CACHE_KEY]: sanitizeProfilesCache,
  [GOOGLE_MEMBER_STORAGE_CACHE_KEY]: sanitizeMemberStorageCache,
  [GOOGLE_SHARING_STATUS_CACHE_KEY]: sanitizeSharingStatusCache,
};

export function sanitizePersistedPayload(key: string, payload: unknown): unknown {
  const sanitizer = PERSIST_SANITIZERS[key];
  if (!sanitizer) return payload;
  return sanitizer(payload);
}

export function sanitizePersistedSelectionId(value: unknown): string | null {
  if (typeof value !== "string") return null;
  const trimmed = value.trim();
  if (!trimmed || trimmed.length > 120) return null;
  if (!/^[A-Za-z0-9_-]+$/.test(trimmed)) return null;
  return trimmed;
}

function sanitizeSerializedValueForKey(key: string, value: string): string | null {
  if (key === CHECK_PROFILE_SELECTED_ADMIN_ID_KEY) {
    return sanitizePersistedSelectionId(value);
  }
  const sanitizer = PERSIST_SANITIZERS[key];
  if (!sanitizer) return value;
  try {
    const parsed = JSON.parse(value);
    const sanitized = sanitizer(parsed);
    return JSON.stringify(sanitized);
  } catch {
    return null;
  }
}

export function readPersistedValue(key: string): string | null {
  if (typeof window === "undefined") return null;
  const raw = window.localStorage.getItem(key) ?? window.sessionStorage.getItem(key);
  if (raw === null) return null;
  const sanitized = sanitizeSerializedValueForKey(key, raw);
  if (sanitized === null) {
    removePersistedValue(key);
    return null;
  }
  if (sanitized !== raw) {
    window.localStorage.setItem(key, sanitized);
    window.sessionStorage.setItem(key, sanitized);
  }
  return sanitized;
}

export function writePersistedValue(key: string, value: string) {
  if (typeof window === "undefined") return;
  const sanitized = sanitizeSerializedValueForKey(key, value);
  if (sanitized === null) {
    removePersistedValue(key);
    return;
  }
  window.localStorage.setItem(key, sanitized);
  window.sessionStorage.setItem(key, sanitized);
}

export function removePersistedValue(key: string) {
  if (typeof window === "undefined") return;
  window.localStorage.removeItem(key);
  window.sessionStorage.removeItem(key);
}
