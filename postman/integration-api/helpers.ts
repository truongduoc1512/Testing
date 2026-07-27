import "dotenv/config";

export const baseUrl = process.env.TEST_BASE_URL || "http://localhost:3000";
let sessionCookie = "";

export async function login(): Promise<string> {
  if (sessionCookie) return sessionCookie;
  const response = await fetch(`${baseUrl}/api/auth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      email: process.env.ACCOUNT_EMAIL,
      password: process.env.ACCOUNT_PASSWORD,
    }),
  });

  if (!response.ok) throw new Error(`Login failed: ${response.status}`);
  sessionCookie = response.headers.get("set-cookie")?.split(";")[0] || "";
  return sessionCookie;
}

export async function api(path: string, cookie: string, init: RequestInit = {}) {
  return fetch(`${baseUrl}${path}`, {
    ...init,
    headers: {
      "Content-Type": "application/json",
      Cookie: cookie,
      "X-Forwarded-For": `127.0.0.${Math.floor(Math.random() * 200) + 1}`,
      ...init.headers,
    },
  });
}

export async function createMockAdmin(cookie: string): Promise<string> {
  const unique = `${Date.now()}-${Math.random().toString(36).slice(2, 8)}`;
  const response = await api("/api/admin", cookie, {
    method: "POST",
    body: JSON.stringify({
      email: `integration-${unique}@example.com`,
      displayName: `Integration ${unique}`,
      googlePassword: "mock-password",
      familyType: "ultra",
      monthlyCredit: 100,
      storageTB: 2,
    }),
  });
  const body = await response.json();
  if (response.status !== 201) throw new Error(`Create admin failed: ${response.status}`);
  return body.admin.id;
}

export async function deleteAdmin(cookie: string, adminId: string) {
  await api(`/api/admin/${adminId}`, cookie, { method: "DELETE" });
}

export async function inviteMember(cookie: string, adminId: string, email: string) {
  return api(`/api/admin/${adminId}/invite`, cookie, {
    method: "POST",
    body: JSON.stringify({ emails: [email] }),
  });
}
