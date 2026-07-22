import https from "https";
import { HttpsProxyAgent } from "https-proxy-agent";
import { apiError } from "@/lib/logger";

export interface Cookie {
  name: string;
  value: string;
  domain?: string;
  path?: string;
  secure?: boolean;
  httpOnly?: boolean;
  sameSite?: string;
  expirationDate?: number;
}

function isGoogleHost(hostname: string): boolean {
  return (
    hostname === "one.google.com" ||
    hostname.endsWith(".one.google.com") ||
    hostname === "myaccount.google.com" ||
    hostname === "payments.google.com" ||
    hostname.endsWith(".google.com")
  );
}

export function googleHttpReq(
  url: string,
  opts: {
    method?: string;
    headers?: Record<string, string>;
    body?: string | null;
    maxRedirects?: number;
    cookies?: Cookie[];
    proxyUrl?: string;
  } = {},
): Promise<{ status: number; body: string; finalUrl: string }> {
  const {
    method = "GET",
    headers = {},
    body = null,
    maxRedirects = 5,
    cookies = [],
    proxyUrl,
  } = opts;
  let cookieHeader = "";
  const parsed = new URL(url);
  const effectiveProxyUrl =
    typeof proxyUrl === "string"
      ? proxyUrl
      : isGoogleHost(parsed.hostname)
        ? process.env.GOOGLE_PROXY_URL || ""
        : "";

  if (cookies.length > 0) {
    const host = parsed.hostname;
    const filtered = cookies.filter((c) => {
      const d = c.domain || "";
      if (d.startsWith(".")) return host.endsWith(d.substring(1));
      return host === d || host.endsWith("." + d);
    });
    cookieHeader = filtered.map((c) => `${c.name}=${c.value}`).join("; ");
  }

  const reqHeaders: Record<string, string> = {
    "User-Agent":
      "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36",
    Accept: "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
    "Accept-Language": "en-US,en;q=0.9",
    ...headers,
  };
  if (cookieHeader) reqHeaders["Cookie"] = cookieHeader;

  return new Promise((resolve, reject) => {
    const reqOpts: https.RequestOptions = {
      hostname: parsed.hostname,
      port: parsed.port || 443,
      path: parsed.pathname + parsed.search,
      method,
      headers: reqHeaders,
    };
    if (effectiveProxyUrl) {
      reqOpts.agent = new HttpsProxyAgent(effectiveProxyUrl);
    }

    const req = https.request(reqOpts, (res) => {
      if (
        [301, 302, 303, 307, 308].includes(res.statusCode!) &&
        res.headers.location &&
        maxRedirects > 0
      ) {
        const loc = res.headers.location.startsWith("http")
          ? res.headers.location
          : `https://${parsed.hostname}${res.headers.location}`;
        res.resume();
        const redirectMethod = [307, 308].includes(res.statusCode!) ? method : "GET";
        const redirectBody = [307, 308].includes(res.statusCode!) ? body : null;
        return googleHttpReq(loc, {
          method: redirectMethod,
          headers,
          body: redirectBody,
          cookies,
          maxRedirects: maxRedirects - 1,
          proxyUrl,
        })
          .then(resolve)
          .catch(reject);
      }
      res.setEncoding("utf8");
      let d = "";
      res.on("data", (c) => (d += c));
      res.on("end", () =>
        resolve({
          status: res.statusCode!,
          body: d,
          finalUrl: res.headers.location || url,
        }),
      );
    });
    req.setTimeout(30_000, () => {
      req.destroy();
      reject(new Error("Request timeout (30s)"));
    });
    req.on("error", reject);
    if (body) req.write(body);
    req.end();
  });
}

export async function getAtTokenAndAuthuser(
  cookiesJson: string,
): Promise<{ atToken: string; authuser: string } | null> {
  let cookies: Cookie[];
  try {
    cookies = JSON.parse(cookiesJson);
  } catch {
    apiError("getAtTokenAndAuthuser", new Error("malformed cookies JSON"));
    return null;
  }

  try {
    const resp = await googleHttpReq("https://myaccount.google.com/family/details", {
      cookies,
    });

    if (
      resp.finalUrl.includes("accounts.google.com/ServiceLogin") ||
      resp.finalUrl.includes("signin")
    ) {
      return null;
    }
    if (resp.status !== 200) {
      return null;
    }

    const html = resp.body;

    let authuser = "0";
    const auMatch = resp.finalUrl.match(/\/u\/(\d+)\//);
    if (auMatch) authuser = auMatch[1];

    let atToken: string | null = null;
    let m: RegExpMatchArray | null;
    m = html.match(/"SNlM0e"\s*:\s*"([^"]+)"/);
    if (m) atToken = m[1];
    if (!atToken) {
      m = html.match(/WIZ_global_data\s*=\s*\{[^}]*?"at"\s*:\s*"([^"]+)"/);
      if (m) atToken = m[1];
    }
    if (!atToken) {
      m = html.match(/'at'\s*:\s*'([^']+)'/);
      if (m) atToken = m[1];
    }

    if (!atToken) {
      return null;
    }

    return { atToken, authuser };
  } catch (e) {
    apiError("getAtTokenAndAuthuser", e);
    return null;
  }
}
