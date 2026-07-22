import { prisma } from "@/lib/db";
import { apiWarn, redactLogValue } from "@/lib/logger";

type AuditStatus = "success" | "failure" | "partial" | "info";

type AuditInput = {
  userId: string;
  actorEmail?: string;
  action: string;
  targetType: string;
  targetId?: string | null;
  status: AuditStatus;
  message?: string;
  metadata?: Record<string, unknown>;
};

const MAX_METADATA_LENGTH = 4_000;

function sanitizeMetadata(metadata?: Record<string, unknown>) {
  if (!metadata) return "{}";
  const redacted = redactLogValue(metadata);
  const serialized = JSON.stringify(redacted);
  if (serialized.length <= MAX_METADATA_LENGTH) return serialized;
  return JSON.stringify({ truncated: true, preview: serialized.slice(0, MAX_METADATA_LENGTH) });
}

export async function writeAuditLog(input: AuditInput) {
  try {
    await prisma.auditLog.create({
      data: {
        userId: input.userId,
        actorEmail: input.actorEmail || "",
        action: input.action,
        targetType: input.targetType,
        targetId: input.targetId || "",
        status: input.status,
        message: input.message || "",
        metadata: sanitizeMetadata(input.metadata),
      },
    });
  } catch (error) {
    apiWarn("audit-log", "Failed to write audit log", {
      action: input.action,
      targetType: input.targetType,
      status: input.status,
      error,
    });
  }
}

export function parseAuditMetadata(value: string) {
  try {
    return JSON.parse(value) as Record<string, unknown>;
  } catch (error) {
    void error;
    return {};
  }
}

export function formatAuditAdminLabel(admin: {
  displayName?: string | null;
  email?: string | null;
}) {
  const name = admin.displayName?.trim();
  const email = admin.email?.trim();
  if (name && email && name !== email) return `${name} (${email})`;
  return name || email || "admin không rõ";
}

export function formatAuditEmailList(emails: string[], maxVisible = 4) {
  const normalized = [...new Set(emails.map((email) => email.trim()).filter(Boolean))];
  if (normalized.length === 0) return "không có email";
  const visible = normalized.slice(0, maxVisible).join(", ");
  const remaining = normalized.length - maxVisible;
  return remaining > 0 ? `${visible} và ${remaining} email khác` : visible;
}
