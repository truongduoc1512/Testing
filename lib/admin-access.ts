import { prisma } from "@/lib/db";
import { hashKey } from "@/lib/crypto";
import { decodeAdminRef } from "@/lib/admin-ref";

const OBJECT_ID_RE = /^[a-f0-9]{24}$/i;
const PUBLIC_ID_PREFIX = "adm_";
const MEMBER_PUBLIC_ID_PREFIX = "mem_";
const RESOLVE_CACHE_TTL_MS = 5 * 60 * 1000;
const resolveCache = new Map<string, { adminId: string; expiresAt: number }>();

export function adminPublicIdFromInternal(internalId: string): string {
  return `adm_${hashKey(`admin:${internalId}`).slice(0, 24)}`;
}

export function memberPublicIdFromInternal(internalId: string): string {
  return `${MEMBER_PUBLIC_ID_PREFIX}${hashKey(`member:${internalId}`).slice(0, 24)}`;
}

export async function resolveOwnedAdminId(
  ownerUserId: string,
  clientAdminId: string,
): Promise<string | null> {
  const routeId = clientAdminId?.trim();
  if (!routeId) return null;
  const cacheKey = `${ownerUserId}:${routeId}`;
  const cached = resolveCache.get(cacheKey);
  if (cached && cached.expiresAt > Date.now()) {
    return cached.adminId;
  }

  const decodedLegacyId = decodeAdminRef(routeId);
  const objectIdCandidates = [routeId, decodedLegacyId].filter(
    (value): value is string => Boolean(value && OBJECT_ID_RE.test(value)),
  );

  if (objectIdCandidates.length > 0) {
    const admin = await prisma.adminAccount.findFirst({
      where: {
        createdBy: ownerUserId,
        id: { in: objectIdCandidates },
      },
      select: { id: true },
    });
    if (admin?.id) {
      resolveCache.set(cacheKey, {
        adminId: admin.id,
        expiresAt: Date.now() + RESOLVE_CACHE_TTL_MS,
      });
      return admin.id;
    }
  }

  if (!routeId.startsWith(PUBLIC_ID_PREFIX)) return null;

  const ownedAdmins = await prisma.adminAccount.findMany({
    where: { createdBy: ownerUserId },
    select: { id: true },
  });
  const matched = ownedAdmins.find((admin) => adminPublicIdFromInternal(admin.id) === routeId);
  if (!matched?.id) return null;

  resolveCache.set(cacheKey, {
    adminId: matched.id,
    expiresAt: Date.now() + RESOLVE_CACHE_TTL_MS,
  });
  return matched.id;
}
