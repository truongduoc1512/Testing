export async function register() {
  if (process.env.NEXT_RUNTIME === "nodejs") {
    try {
      // Seed owner account
      const { ACCOUNT_EMAIL, ACCOUNT_PASSWORD } = await import("@/lib/env");
      if (ACCOUNT_EMAIL && ACCOUNT_PASSWORD) {
        const { prisma } = await import("@/lib/db");
        const bcrypt = await import("bcryptjs");

        const existing = await prisma.user.findUnique({ where: { email: ACCOUNT_EMAIL } });
        if (!existing) {
          const hashed = await bcrypt.hash(ACCOUNT_PASSWORD, 12);
          await prisma.user.create({
            data: { fullName: "Owner", email: ACCOUNT_EMAIL, password: hashed },
          });
        }
      }
    } catch (e) {
      void e;
    }

    // Auto backup every 6 hours
    const { createBackup } = await import("@/lib/backup");

    // Initial backup on startup (delayed 30s to let server warm up)
    setTimeout(async () => {
      try {
        await createBackup();
      } catch (e) {
        void e;
      }
    }, 30_000);

    // Recurring backup every 6 hours
    setInterval(
      async () => {
        try {
          await createBackup();
        } catch (e) {
          void e;
        }
      },
      6 * 60 * 60 * 1000,
    );
  }
}
