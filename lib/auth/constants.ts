import { JWT_SECRET } from "@/lib/env";

export const COOKIE_NAME = "vieshop-token";

export const SECRET = new TextEncoder().encode(JWT_SECRET);

export const ISSUER = "vieshop";
export const AUDIENCE = "vieshop-web";
export const EXPIRATION = "1d";
export const COOKIE_MAX_AGE = 60 * 60 * 24;
