export function normalizeLooseText(text: string): string {
  return (text || "")
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .replace(/[\u0111\u0110]/g, "d")
    .replace(/\s+/g, " ")
    .trim()
    .toLowerCase();
}

export function isDetachedFrameError(error: unknown): boolean {
  const msg = String((error as any)?.message || error || "").toLowerCase();
  return (
    msg.includes("detached frame") ||
    msg.includes("execution context was destroyed") ||
    msg.includes("cannot find context with specified id") ||
    msg.includes("detached from document")
  );
}

export function isGoogleSignInUrl(url: string): boolean {
  return (
    url.includes("accounts.google.com") ||
    url.includes("signin") ||
    url.includes("ServiceLogin")
  );
}

export function isReauthUrl(url: string): boolean {
  const u = (url || "").toLowerCase();
  return (
    u.includes("accounts.google.com") ||
    u.includes("/signin/rapt") ||
    u.includes("/reauthpage") ||
    u.includes("accounts.google.com/v3/signin") ||
    u.includes("/challenge/") ||
    (u.includes("google.com") && u.includes("authuser"))
  );
}

export function extractAtToken(html: string): string | null {
  let m = html.match(/"SNlM0e"\s*:\s*"([^"]+)"/);
  if (m) return m[1];
  m = html.match(/'at'\s*:\s*'([^']+)'/);
  if (m) return m[1];
  return null;
}

export function extractFSid(html: string): string {
  const m = html.match(/"FdrFJe"\s*:\s*"(-?\d+)"/);
  return m ? m[1] : "";
}

export function parseBatchResponse(body: string): Map<string, string> {
  const results = new Map<string, string>();
  const clean = body.replace(/^\)\]\}'\s*\n?/, "");
  const regex = /\["wrb\.fr","(\w+)","((?:[^"\\]|\\.)*)"/g;
  let m;
  while ((m = regex.exec(clean)) !== null) {
    const rpcId = m[1];
    let data = m[2];
    try {
      data = JSON.parse(`"${data}"`);
    } catch {

    }
    results.set(rpcId, data);
  }
  return results;
}
