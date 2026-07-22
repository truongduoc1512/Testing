interface RateLimitEntry {
  timestamps: number[];
}

interface RateLimiterConfig {
  maxRequests: number;
  windowMs: number;
}

const stores = new Map<string, Map<string, RateLimitEntry>>();

function getStore(name: string): Map<string, RateLimitEntry> {
  let store = stores.get(name);
  if (!store) {
    store = new Map();
    stores.set(name, store);
  }
  return store;
}

setInterval(
  () => {
    const now = Date.now();
    for (const [, store] of stores) {
      for (const [key, entry] of store) {
        entry.timestamps = entry.timestamps.filter((t) => now - t < 3600_000);
        if (entry.timestamps.length === 0) store.delete(key);
      }
    }
  },
  5 * 60 * 1000,
);

export function createRateLimiter(name: string, config: RateLimiterConfig) {
  const store = getStore(name);

  return {
    check(key: string): { allowed: boolean; remaining: number; resetMs: number } {
      const now = Date.now();
      const cutoff = now - config.windowMs;
      let entry = store.get(key);

      if (!entry) {
        entry = { timestamps: [] };
        store.set(key, entry);
      }

      entry.timestamps = entry.timestamps.filter((t) => t > cutoff);

      if (entry.timestamps.length >= config.maxRequests) {
        const oldestInWindow = entry.timestamps[0];
        const resetMs = oldestInWindow + config.windowMs - now;
        return {
          allowed: false,
          remaining: 0,
          resetMs: Math.max(0, resetMs),
        };
      }

      entry.timestamps.push(now);
      return {
        allowed: true,
        remaining: config.maxRequests - entry.timestamps.length,
        resetMs: config.windowMs,
      };
    },
  };
}

export const loginLimiter = createRateLimiter("login", {
  maxRequests: 5,
  windowMs: 60 * 1000,
});

export const syncLimiter = createRateLimiter("sync", {
  maxRequests: 3,
  windowMs: 30 * 1000,
});

export const profileLimiter = createRateLimiter("profile", {
  maxRequests: 30,
  windowMs: 60 * 1000,
});

export const membersStorageLimiter = createRateLimiter("members-storage", {
  maxRequests: 3,
  windowMs: 60 * 1000,
});

export const inviteLimiter = createRateLimiter("invite", {
  maxRequests: 2,
  windowMs: 60 * 1000,
});

export const memberMutationLimiter = createRateLimiter("member-mutation", {
  maxRequests: 6,
  windowMs: 60 * 1000,
});

export const sharingLimiter = createRateLimiter("sharing", {
  maxRequests: 6,
  windowMs: 60 * 1000,
});

export const creditActivityLimiter = createRateLimiter("credit-activity", {
  maxRequests: 4,
  windowMs: 60 * 1000,
});

export const backupLimiter = createRateLimiter("backup", {
  maxRequests: 1,
  windowMs: 30 * 60 * 1000,
});

export const credentialsLimiter = createRateLimiter("credentials", {
  maxRequests: 10,
  windowMs: 60 * 1000,
});

export const totpLimiter = createRateLimiter("totp", {
  maxRequests: 30,
  windowMs: 60 * 1000,
});

export const changePasswordLimiter = createRateLimiter("change-password", {
  maxRequests: 5,
  windowMs: 60 * 1000,
});

export const auditLogLimiter = createRateLimiter("audit-log", {
  maxRequests: 20,
  windowMs: 60 * 1000,
});

export function getClientIp(request: Request): string {
  const headers = new Headers(request.headers);
  return (
    headers.get("x-forwarded-for")?.split(",")[0]?.trim() ||
    headers.get("x-real-ip") ||
    "unknown"
  );
}

export function buildRateLimitKey(
  request: Request,
  ...parts: Array<string | number | null | undefined>
): string {
  const safeParts = parts
    .filter((part): part is string | number => part !== null && part !== undefined)
    .map((part) => String(part));
  return [getClientIp(request), ...safeParts].join(":");
}

export function rateLimitResponse(resetMs: number) {
  const retryAfter = Math.ceil(resetMs / 1000);
  return new Response(JSON.stringify({ error: "Quá nhiều yêu cầu", retryAfter }), {
    status: 429,
    headers: {
      "Content-Type": "application/json",
      "Retry-After": String(retryAfter),
    },
  });
}
