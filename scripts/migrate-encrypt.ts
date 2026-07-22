import { PrismaClient } from "@prisma/client";
import { encryptValue, isEncrypted } from "../lib/crypto";

const prisma = new PrismaClient();

async function main() {
  const admins = await prisma.adminAccount.findMany({
    select: { id: true, email: true, googlePassword: true, totpSecret: true },
  });

  let adminUpdated = 0;
  for (const admin of admins) {
    const updates: Record<string, string> = {};

    if (admin.googlePassword && !isEncrypted(admin.googlePassword)) {
      updates.googlePassword = encryptValue(admin.googlePassword);
    }
    if (admin.totpSecret && !isEncrypted(admin.totpSecret)) {
      updates.totpSecret = encryptValue(admin.totpSecret);
    }

    if (Object.keys(updates).length > 0) {
      await prisma.adminAccount.update({
        where: { id: admin.id },
        data: updates,
      });
      adminUpdated++;
    }
  }

  const sessions = await prisma.googleSession.findMany({
    select: { id: true, adminId: true, cookies: true, bearerToken: true },
  });

  let sessionUpdated = 0;
  for (const session of sessions) {
    const updates: Record<string, string> = {};

    if (session.cookies && session.cookies !== "[]" && !isEncrypted(session.cookies)) {
      updates.cookies = encryptValue(session.cookies);
    }
    if (
      session.bearerToken &&
      session.bearerToken !== "" &&
      !isEncrypted(session.bearerToken)
    ) {
      updates.bearerToken = encryptValue(session.bearerToken);
    }

    if (Object.keys(updates).length > 0) {
      await prisma.googleSession.update({
        where: { id: session.id },
        data: updates,
      });
      sessionUpdated++;
    }
  }

  void adminUpdated;
  void sessionUpdated;
}

main()
  .catch((e) => {
    void e;
    process.exit(1);
  })
  .finally(() => prisma.$disconnect());
