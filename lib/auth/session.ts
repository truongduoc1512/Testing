import { cookies } from "next/headers";
import { verifyToken, type JwtPayload } from "./jwt";
import { COOKIE_NAME } from "./constants";
import { prisma } from "@/lib/db";

export async function getSession(): Promise<JwtPayload | null> {
  const cookieStore = await cookies();
  const token = cookieStore.get(COOKIE_NAME)?.value;
  if (!token) return null;
  const payload = await verifyToken(token);
  if (!payload) return null;

  if (payload.tokenVersion !== undefined) {
    const user = await prisma.user.findUnique({
      where: { id: payload.sub },
      select: { tokenVersion: true },
    });
    if (!user || user.tokenVersion !== payload.tokenVersion) return null;
  }

  return payload;
}

export { COOKIE_NAME, COOKIE_MAX_AGE } from "./constants";
