import { prisma } from "@/lib/db";
import { encryptValue } from "@/lib/crypto";
import fs from "fs";
import path from "path";

const BACKUP_DIR = path.join(process.cwd(), "backups");
const MAX_BACKUPS = 10;

function assertEncryptedBackupEnvelope(serializedBackup: string) {
  let parsed: unknown;
  try {
    parsed = JSON.parse(serializedBackup);
  } catch {
    throw new Error("Backup envelope serialization failed");
  }

  const obj = parsed as {
    _meta?: { encrypted?: boolean };
    payload?: unknown;
  };
  if (!obj?._meta?.encrypted || typeof obj.payload !== "string" || obj.payload.length < 32) {
    throw new Error("Backup payload encryption check failed");
  }

  const plaintextCollectionMarkers = [
    '"users":[',
    '"admin_accounts":[',
    '"family_members":[',
    '"google_sessions":[',
    '"cache_storage":[',
    '"credit_logs":[',
    '"googlePassword":',
    '"totpSecret":',
    '"cookies":',
    '"bearerToken":',
  ];
  for (const marker of plaintextCollectionMarkers) {
    if (serializedBackup.includes(marker)) {
      throw new Error("Backup plaintext leak detected");
    }
  }
}

export async function createBackup(): Promise<string> {
  if (!fs.existsSync(BACKUP_DIR)) fs.mkdirSync(BACKUP_DIR, { recursive: true });

  const timestamp = new Date().toISOString().replace(/[:.]/g, "-").slice(0, 19);
  const filename = `backup-${timestamp}.json`;
  const filepath = path.join(BACKUP_DIR, filename);

  const [users, admins, members, sessions, cache, creditLogs] = await Promise.all([
    prisma.user.findMany(),
    prisma.adminAccount.findMany(),
    prisma.familyMember.findMany(),
    prisma.googleSession.findMany(),
    prisma.cacheStorage.findMany(),
    prisma.creditLog.findMany(),
  ]);

  const data = {
    _meta: {
      createdAt: new Date().toISOString(),
      collections: {
        users: users.length,
        admins: admins.length,
        members: members.length,
        sessions: sessions.length,
        cache: cache.length,
        creditLogs: creditLogs.length,
      },
    },
    users,
    admin_accounts: admins,
    family_members: members,
    google_sessions: sessions,
    cache_storage: cache,
    credit_logs: creditLogs,
  };

  const encryptedPayload = encryptValue(JSON.stringify(data));
  // TODO(security): verify in production restore drills that only encrypted envelopes
  // are accepted end-to-end and decrypted payloads are never logged by restore tooling.
  const serializedBackup = JSON.stringify(
    {
      _meta: {
        createdAt: data._meta.createdAt,
        encrypted: true,
        formatVersion: 1,
        collections: data._meta.collections,
      },
      payload: encryptedPayload,
    },
    null,
    2,
  );
  assertEncryptedBackupEnvelope(serializedBackup);
  fs.writeFileSync(filepath, serializedBackup, "utf-8");

  await prisma.databaseBackup.create({
    data: {
      filename,
      data: serializedBackup,
    },
  });

  const dbBackups = await prisma.databaseBackup.findMany({
    orderBy: { createdAt: "desc" },
    select: { id: true },
  });
  if (dbBackups.length > MAX_BACKUPS) {
    const idsToDelete = dbBackups.slice(MAX_BACKUPS).map((b) => b.id);
    await prisma.databaseBackup.deleteMany({
      where: { id: { in: idsToDelete } },
    });
  }

  const files = fs
    .readdirSync(BACKUP_DIR)
    .filter((f) => f.startsWith("backup-") && f.endsWith(".json"))
    .sort()
    .reverse();

  for (const old of files.slice(MAX_BACKUPS)) {
    fs.unlinkSync(path.join(BACKUP_DIR, old));
  }

  return filename;
}
