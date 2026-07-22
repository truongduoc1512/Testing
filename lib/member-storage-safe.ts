import { safeErrorMessage } from "@/lib/safe-error";

export interface SafeMemberStorageUsage {
  name: string;
  email: string;
  usedText: string | null;
  usedBytes: number | null;
}

export interface SafeFamilyMemberStorageUsage {
  totalUsedText: string | null;
  totalUsedBytes: number | null;
  members: SafeMemberStorageUsage[];
  error?: string;
}

export function sanitizeMemberStorageForClient(
  input: unknown,
  fallbackError = "Unable to read member storage.",
): SafeFamilyMemberStorageUsage {
  const obj = input && typeof input === "object" ? (input as Record<string, unknown>) : {};
  const members = Array.isArray(obj.members) ? obj.members : [];

  const safeMembers: SafeMemberStorageUsage[] = members
    .slice(0, 100)
    .map((entry) => {
      if (!entry || typeof entry !== "object") return null;
      const member = entry as Record<string, unknown>;
      return {
        name: typeof member.name === "string" ? member.name : "",
        email: typeof member.email === "string" ? member.email : "",
        usedText: typeof member.usedText === "string" ? member.usedText : null,
        usedBytes:
          typeof member.usedBytes === "number" && Number.isFinite(member.usedBytes)
            ? member.usedBytes
            : null,
      };
    })
    .filter((entry): entry is SafeMemberStorageUsage => Boolean(entry));

  const safe: SafeFamilyMemberStorageUsage = {
    totalUsedText: typeof obj.totalUsedText === "string" ? obj.totalUsedText : null,
    totalUsedBytes:
      typeof obj.totalUsedBytes === "number" && Number.isFinite(obj.totalUsedBytes)
        ? obj.totalUsedBytes
        : null,
    members: safeMembers,
  };

  if (typeof obj.error === "string" && obj.error.trim()) {
    safe.error = safeErrorMessage(obj.error, fallbackError);
  }

  return safe;
}
