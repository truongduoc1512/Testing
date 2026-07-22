import { describe, expect, it } from "vitest";
import { NextRequest } from "next/server";

import { middleware } from "@/middleware";

function makeRequest(path: string, method: string): NextRequest {
  return new NextRequest(`http://localhost${path}`, { method });
}

describe("register access", () => {
  it.each(["GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"])(
    "blocks %s /api/auth/register",
    async (method) => {
      const res = await middleware(makeRequest("/api/auth/register", method));

      expect(res.status).toBe(404);
    },
  );

  it.each(["GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"])(
    "blocks %s /register",
    async (method) => {
      const res = await middleware(makeRequest("/register", method));

      expect(res.status).toBe(404);
    },
  );
});
