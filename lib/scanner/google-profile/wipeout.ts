import { googleHttpReq as httpReq, type Cookie } from "@/lib/scanner/google-http";
import { sleep } from "@/lib/scanner/scanner-utils";
import type { Frame, Page } from "puppeteer";
import { isGoogleSignInUrl } from "@/lib/scanner/google-profile/shared";

export interface WipeoutParams {
  xsrf: string;
  msgToken: string;
  cn: string;
  ipi: string;
  si: string;
  cst: string;
  wst: string;
}

export type EvalContext = Page | Frame;

export interface WipeoutFetchResult {
  success: boolean;
  code: number;
  bodyPreview: string;
}

export function isWipeoutUrl(url: string): boolean {
  return url.includes("/wipeout");
}

export function extractPaymentsUserIndex(url: string): string | null {
  const m = (url || "").match(/\/u\/(\d+)\//);
  return m ? m[1] : null;
}

export async function waitForWipeoutContext(
  page: Page,
  timeoutMs = 25000,
): Promise<EvalContext | null> {
  const deadline = Date.now() + timeoutMs;
  while (Date.now() < deadline) {
    try {
      if (isWipeoutUrl(page.url())) return page;
      const frame = page.frames().find((f) => isWipeoutUrl(f.url()));
      if (frame) return frame;
    } catch {

    }
    await sleep(500);
  }
  return null;
}

export async function extractWipeoutParams(context: EvalContext): Promise<WipeoutParams | null> {
  try {
    return await context.evaluate(() => {
      const html = document.documentElement.outerHTML || "";

      let xsrf = "";
      const xsrfInput = document.querySelector('input[name="xsrf"]') as HTMLInputElement;
      if (xsrfInput) xsrf = xsrfInput.value;
      if (!xsrf) {
        const m = html.match(/xsrf['":\s]+['"]([A-Za-z0-9_\-]{20,})['"]/);
        if (m) xsrf = m[1];
      }
      if (!xsrf) {
        for (const f of document.querySelectorAll("form")) {
          const h = f.querySelector('input[name="xsrf"]') as HTMLInputElement;
          if (h) {
            xsrf = h.value;
            break;
          }
        }
      }

      let msgToken = "";
      for (const inp of document.querySelectorAll('input[type="hidden"]')) {
        const val = (inp as HTMLInputElement).value || "";
        if (val.startsWith("ACo3") && val.length > 30) {
          msgToken = val;
          break;
        }
      }
      if (!msgToken) {
        const m = html.match(/ACo3[A-Za-z0-9+/=_\-]{30,}/);
        if (m) msgToken = m[0];
      }
      if (!msgToken) {
        const m = html.match(/["']([A-Za-z0-9+/=_\-]{80,})["']/);
        if (m && m[1].length > 80) msgToken = m[1];
      }
      if (!msgToken) {
        try {
          const w = window as any;
          if (w._pageData) {
            const pd = JSON.stringify(w._pageData);
            const m = pd.match(/ACo3[A-Za-z0-9+/=_\-]{30,}/);
            if (m) msgToken = m[0];
          }
        } catch { }
      }

      const urlParams = new URLSearchParams(window.location.search);
      return {
        xsrf,
        msgToken,
        cn: urlParams.get("cn") || "",
        ipi: urlParams.get("ipi") || "",
        si: urlParams.get("si") || "",
        cst: urlParams.get("cst") || "",
        wst: urlParams.get("wst") || "",
      };
    });
  } catch {
    return null;
  }
}

export async function submitWipeoutViaFetch(
  context: EvalContext,
  submitUrl: string,
  xsrf: string,
  msg: string,
): Promise<WipeoutFetchResult> {
  try {
    return await context.evaluate(
      async (url: string, xsrfToken: string, msgPayload: string) => {
        try {
          const body = `xsrf=${encodeURIComponent(xsrfToken)}&msg=${encodeURIComponent(msgPayload)}`;
          const resp = await fetch(url, {
            method: "POST",
            headers: {
              "Content-Type": "application/x-www-form-urlencoded",
              "X-Requested-With": "XmlHttpRequest",
            },
            credentials: "include",
            body,
          });
          const text = await resp.text();

          if (resp.type === "opaqueredirect" || (resp.status === 0 && !text)) {
            return { success: true, code: 302, bodyPreview: "opaqueredirect" };
          }

          let internalCode = 0;
          try {
            let respBody = text;
            if (respBody.startsWith(")]}'"))
              respBody = respBody.substring(respBody.indexOf("\n") + 1);
            const parsed = JSON.parse(respBody);
            if (Array.isArray(parsed) && Array.isArray(parsed[1])) {
              internalCode = parsed[1][0] || 0;
            }
          } catch {
            const cm = text.match(/\[null,\[(\d+),/);
            if (cm) internalCode = parseInt(cm[1]);
          }

          const isClosed =
            text.includes("OR-CAC-13") || text.includes("support.google.com");
          return {
            success: resp.status === 200 && (isClosed || internalCode > 0),
            code: internalCode,
            bodyPreview: `status=${resp.status} type=${resp.type} ` + text.substring(0, 400),
          };
        } catch (e: any) {
          return { success: false, code: -1, bodyPreview: "Error: " + e.message };
        }
      },
      submitUrl,
      xsrf,
      msg,
    );
  } catch (e: any) {
    return { success: false, code: -1, bodyPreview: "Error: " + (e?.message || "") };
  }
}

export async function verifyPaymentProfileClosed(
  cookies: Cookie[] | undefined,
): Promise<boolean | null> {
  if (!cookies || cookies.length === 0) return null;

  for (let attempt = 0; attempt < 3; attempt++) {
    try {
      const settingsResp = await httpReq("https://payments.google.com/gp/w/home/settings", {
        cookies,
      });
      const finalUrl = settingsResp.finalUrl || "";

      if (finalUrl.includes("/signup")) return true;

      const isLogin = isGoogleSignInUrl(finalUrl);
      if (isLogin) return null;
    } catch {
      return null;
    }

    if (attempt < 2) await sleep(1500);
  }

  return false;
}
