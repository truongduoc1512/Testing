import { NextResponse } from "next/server";
import type { NextRequest } from "next/server";
import { jwtVerify } from "jose";
import { SECRET, COOKIE_NAME, ISSUER, AUDIENCE } from "@/lib/auth/constants";

const publicPaths = ["/login"];

function createNonce() {
  const bytes = crypto.getRandomValues(new Uint8Array(16));
  return btoa(String.fromCharCode(...bytes));
}

function buildCsp(nonce: string) {
  const isDev = process.env.NODE_ENV !== "production";

  return [
    "default-src 'self'",
    `script-src 'self' 'nonce-${nonce}'${isDev ? " 'unsafe-eval'" : ""}`,
    `style-src 'self' ${isDev ? "'unsafe-inline'" : `'nonce-${nonce}'`}`,
    "img-src 'self' data: blob: https:",
    "font-src 'self' data:",
    "connect-src 'self' https:",
    "frame-ancestors 'none'",
    "object-src 'none'",
    "base-uri 'self'",
    "form-action 'self'",
  ].join("; ");
}

function applySecurityHeaders(response: NextResponse, nonce: string) {
  response.headers.set("Content-Security-Policy", buildCsp(nonce));
  response.headers.set("x-nonce", nonce);
  return response;
}

function nextWithSecurityHeaders(request: NextRequest, nonce: string) {
  const requestHeaders = new Headers(request.headers);
  requestHeaders.set("x-nonce", nonce);

  const response = NextResponse.next({
    request: {
      headers: requestHeaders,
    },
  });

  return applySecurityHeaders(response, nonce);
}

export async function middleware(request: NextRequest) {
  const start = Date.now();
  const { pathname } = request.nextUrl;
  const method = request.method;
  const nonce = createNonce();

  const isStatic =
    pathname.startsWith("/_next/static") ||
    pathname.startsWith("/_next/image") ||
    pathname === "/favicon.ico";

  if (publicPaths.some((p) => pathname.startsWith(p))) {
    if (!isStatic) logRequest(method, pathname, 200, start);
    return nextWithSecurityHeaders(request, nonce);
  }

  if (pathname.startsWith("/_next") || pathname.includes(".")) {
    return nextWithSecurityHeaders(request, nonce);
  }

  const publicApiPaths = ["/api/auth/login", "/api/time"];
  if (publicApiPaths.some((p) => pathname.startsWith(p))) {
    return nextWithSecurityHeaders(request, nonce);
  }

  const publicAuthPaths = ["/register", "/api/auth/register"];
  if (publicAuthPaths.some((p) => pathname.startsWith(p))) {
    return nextWithSecurityHeaders(request, nonce);
  }

  if (pathname.startsWith("/api")) {
    const token = request.cookies.get(COOKIE_NAME)?.value;
    if (!token) {
      logRequest(method, pathname, 401, start, "no token");
      return applySecurityHeaders(
        NextResponse.json({ error: "Unauthorized" }, { status: 401 }),
        nonce,
      );
    }
    try {
      await jwtVerify(token, SECRET, { issuer: ISSUER, audience: AUDIENCE });
      return nextWithSecurityHeaders(request, nonce);
    } catch {
      logRequest(method, pathname, 401, start, "invalid token");
      return applySecurityHeaders(
        NextResponse.json({ error: "Unauthorized" }, { status: 401 }),
        nonce,
      );
    }
  }

  const token = request.cookies.get(COOKIE_NAME)?.value;
  if (!token) {
    logRequest(method, pathname, 302, start, "-> /login (no token)");
    return applySecurityHeaders(NextResponse.redirect(new URL("/login", request.url)), nonce);
  }

  try {
    await jwtVerify(token, SECRET, {
      issuer: ISSUER,
      audience: AUDIENCE,
    });
    logRequest(method, pathname, 200, start);
    return nextWithSecurityHeaders(request, nonce);
  } catch {
    logRequest(method, pathname, 302, start, "-> /login (invalid token)");
    const response = NextResponse.redirect(new URL("/login", request.url));
    response.cookies.set(COOKIE_NAME, "", {
      maxAge: 0,
      path: "/",
      sameSite: "strict",
    });
    return applySecurityHeaders(response, nonce);
  }
}

function logRequest(
  method: string,
  path: string,
  status: number,
  start: number,
  note?: string,
) {
  void method;
  void path;
  void status;
  void start;
  void note;
}

export const config = {
  matcher: ["/((?!_next/static|_next/image|favicon.ico).*)"],
};
