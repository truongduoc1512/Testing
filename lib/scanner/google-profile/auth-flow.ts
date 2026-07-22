import { sleep, get2FACode } from "@/lib/scanner/scanner-utils";
import { existsSync } from "node:fs";
import puppeteer, {
  type Page,
  type Browser,
  type ElementHandle,
} from "puppeteer";
import type { Cookie } from "@/lib/scanner/google-http";
import { hiddenChromeWindowArgs } from "@/lib/scanner/browser-window";
import {
  isDetachedFrameError,
  isReauthUrl,
  normalizeLooseText,
} from "@/lib/scanner/google-profile/shared";
export {
  TOTP_INPUT_SELECTORS,
  findFallbackTotpInput,
  hasVisibleTotpInput,
  moveToTotpChallenge,
} from "@/lib/scanner/google-auth-challenge";
import {
  TOTP_INPUT_SELECTORS,
  findFallbackTotpInput,
  hasVisibleTotpInput,
  moveToTotpChallenge,
} from "@/lib/scanner/google-auth-challenge";

export const REAUTH_PASSWORD_NOT_FOUND_MESSAGE =
  "Không tìm thấy ô nhập mật khẩu khi xác minh đóng hồ sơ thanh toán.";

const PASSWORD_SELECTORS = [
  'input[type="password"]',
  'input[name="Passwd"]',
  'input[name="password"]',
  'input[autocomplete="current-password"]',
  '#password input[type="password"]',
];

const PASSWORD_PATH_DIRECT_HINTS = [
  "sử dụng mật khẩu",
  "nhập mật khẩu",
  "thử cách khác",
  "tùy chọn khác",
  "cách đăng nhập khác",
  "dùng mật khẩu",
  "xác minh danh tính",
];

function isPasswordErrorText(text: string): boolean {
  const normalized = normalizeLooseText(text);
  return (
    normalized.includes("wrong password") ||
    normalized.includes("incorrect password") ||
    normalized.includes("sai mat khau") ||
    normalized.includes("mat khau khong chinh xac")
  );
}

export async function waitForReauthPage(
  browser: Browser,
  basePage: Page,
  pagesBeforePopup: Set<Page>,
  timeoutMs = 25000,
): Promise<Page | null> {
  const deadline = Date.now() + timeoutMs;
  const popupByOpenerPromise: Promise<Page | null> = browser
    .waitForTarget(
      (t) => t.type() === "page" && t.opener() === basePage.target(),
      { timeout: Math.min(timeoutMs, 12000) },
    )
    .then(async (t) => {
      try {
        const p = await (t as any).page?.();
        return p || null;
      } catch {
        return null;
      }
    })
    .catch(() => null);

  let openerPopupPage: Page | null = null;

  while (Date.now() < deadline) {
    if (!openerPopupPage) {
      openerPopupPage = await Promise.race([
        popupByOpenerPromise,
        sleep(1).then(() => null),
      ]);
    }
    if (openerPopupPage && !openerPopupPage.isClosed()) {
      try {
        const popupUrl = openerPopupPage.url();
        if (isReauthUrl(popupUrl)) return openerPopupPage;
      } catch (e) {
        if (!isDetachedFrameError(e)) throw e;
      }
    }

    const allPages = await browser.pages();
    let anyAccountsPage: Page | null = null;
    for (const p of allPages) {
      if (p.isClosed()) continue;
      let pUrl = "";
      try {
        pUrl = p.url();
      } catch (e) {
        if (isDetachedFrameError(e)) continue;
        throw e;
      }
      if (!isReauthUrl(pUrl)) continue;
      if (!anyAccountsPage) anyAccountsPage = p;
      if (!pagesBeforePopup.has(p)) {
        return p;
      }
    }
    if (anyAccountsPage) return anyAccountsPage;

    try {
      if (!basePage.isClosed() && isReauthUrl(basePage.url())) {
        return basePage;
      }
    } catch (e) {
      if (!isDetachedFrameError(e)) throw e;
    }

    await sleep(500);
  }

  return null;
}

export async function waitAndGet(
  page: Page,
  selectors: string[],
  timeout = 15000,
): Promise<ElementHandle | null> {
  const start = Date.now();
  while (Date.now() - start < timeout) {
    for (const sel of selectors) {
      try {
        const el = await page.$(sel);
        if (el) {
          const vis = await page.evaluate((e: Element) => {
            const r = e.getBoundingClientRect();
            return (
              r.width > 0 && r.height > 0 && getComputedStyle(e).visibility !== "hidden"
            );
          }, el);
          if (vis) return el;
        }
      } catch {

      }
    }
    await sleep(500);
  }
  return null;
}

export async function findPasswordInputAcrossFrames(targetPage: Page): Promise<ElementHandle | null> {
  for (const frame of targetPage.frames()) {
    for (const sel of PASSWORD_SELECTORS) {
      try {
        const el = await frame.$(sel);
        if (!el) continue;
        const vis = await frame.evaluate((e: Element) => {
          const r = e.getBoundingClientRect();
          return (
            r.width > 0 &&
            r.height > 0 &&
            getComputedStyle(e).visibility !== "hidden" &&
            getComputedStyle(e).display !== "none"
          );
        }, el);
        if (vis) return el;
      } catch {

      }
    }
  }
  return null;
}

async function clickUsePasswordPath(targetPage: Page): Promise<boolean> {
  const searchHints = Array.from(new Set(PASSWORD_PATH_DIRECT_HINTS.filter(Boolean)));
  if (searchHints.length === 0) return false;

  for (const frame of targetPage.frames()) {
    try {
      const clicked = await frame.evaluate((patterns: string[]) => {
        const normalize = (text: string) =>
          (text || "")
            .normalize("NFD")
            .replace(/[\u0300-\u036f]/g, "")
            .replace(/[\u0111\u0110]/g, "d")
            .replace(/\s+/g, " ")
            .trim()
            .toLowerCase();
        const normalizedPatterns = patterns.map(normalize);
        const canClick = (el: Element) => {
          const htmlEl = el as HTMLElement;
          const r = htmlEl.getBoundingClientRect();
          if (r.width <= 0 || r.height <= 0) return false;
          const style = getComputedStyle(htmlEl);
          if (style.visibility === "hidden" || style.display === "none") return false;
          return true;
        };

        for (const el of document.querySelectorAll("button, [role='button'], a")) {
          if (!canClick(el)) continue;
          const text = normalize((el as HTMLElement).innerText || el.textContent || "");
          if (!text) continue;
          if (normalizedPatterns.some((p) => text.includes(p))) {
            (el as HTMLElement).click();
            return true;
          }
        }
        return false;
      }, searchHints);

      if (clicked) return true;
    } catch {

    }
  }
  return false;
}

export async function submitPasswordOnPage(
  targetPage: Page,
  password: string,
  email: string,
): Promise<boolean> {
  const deadline = Date.now() + 45000;
  let nextTryUsePasswordAt = 0;
  let pwdInput: ElementHandle | null = null;

  while (Date.now() < deadline) {
    pwdInput = await findPasswordInputAcrossFrames(targetPage);
    if (pwdInput) break;

    if (Date.now() >= nextTryUsePasswordAt) {
      await clickUsePasswordPath(targetPage);

      try {
        const frames = targetPage.frames();
        for (const frame of frames) {
          const clickedAccount = await frame.evaluate((email) => {
            if (!email) return false;
            for (const el of document.querySelectorAll("div.BHzsHc, div[data-email], div[data-identifier]")) {
              const text = (el as HTMLElement).innerText || "";
              if (text.toLowerCase().includes(email.toLowerCase())) {
                (el as HTMLElement).click();
                return true;
              }
            }
            return false;
          }, email);
          if (clickedAccount) break;
        }
      } catch { }

      nextTryUsePasswordAt = Date.now() + 2000;
    }
    await sleep(500);
  }

  if (!pwdInput) return false;

  for (let attempt = 0; attempt < 4; attempt++) {
    try {
      await pwdInput.click({ clickCount: 3 });
      await sleep(100);
      await pwdInput.type(password, { delay: 10 + Math.random() * 15 });
      await sleep(300);
      const nextBtn = await targetPage.$("#passwordNext");
      if (nextBtn) await nextBtn.click();
      else await targetPage.keyboard.press("Enter");
      return true;
    } catch (e) {
      if (!isDetachedFrameError(e)) throw e;
      await sleep(400);
      const newInput = await findPasswordInputAcrossFrames(targetPage);
      if (!newInput) return false;
      pwdInput = newInput;
    }
  }

  return false;
}

export async function extractCookies(page: Page): Promise<Cookie[] | undefined> {
  try {
    const cdp = await page.target().createCDPSession();
    const { cookies } = await cdp.send("Network.getAllCookies");
    if (cookies && cookies.length > 0) return cookies as Cookie[];
  } catch { }
  return undefined;
}

function resolveChromePath(): string {
  if (process.env.CHROME_EXECUTABLE_PATH) return process.env.CHROME_EXECUTABLE_PATH;
  if (process.platform === "win32") return "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe";
  const linuxPaths = [
    "/usr/bin/google-chrome-stable",
    "/usr/bin/google-chrome",
    "/usr/bin/chromium-browser",
    "/usr/bin/chromium",
    "/snap/bin/chromium",
  ];
  for (const p of linuxPaths) {
    try { if (existsSync(p)) return p; } catch { }
  }
  return "/usr/bin/google-chrome-stable";
}

export async function launchStealthBrowser(): Promise<Browser> {
  const launchArgs = [
    "--disable-blink-features=AutomationControlled",
    "--disable-popup-blocking",
    "--disable-gpu",
    "--disable-dev-shm-usage",
    ...hiddenChromeWindowArgs(1366, 768),
  ];
  if (process.platform !== "win32") {
    launchArgs.push("--no-sandbox", "--disable-setuid-sandbox");
  }

  return puppeteer.launch({
    headless: false,
    executablePath: resolveChromePath(),
    args: launchArgs,
    defaultViewport: { width: 1366, height: 768 },
    ignoreDefaultArgs: ["--enable-automation"],
  });
}

export async function puppeteerGoogleLogin(
  page: Page,
  email: string,
  password: string,
  totpSecret: string,
  onFlowStep?: (step: string) => void,
): Promise<void> {
  await page.goto(
    "https://accounts.google.com/v3/signin/identifier?continue=https%3A%2F%2Fmyaccount.google.com%2F&service=accountsettings&flowName=GlifWebSignIn&flowEntry=ServiceLogin",
    { waitUntil: "networkidle2", timeout: 30000 },
  );

  const emailInput = await waitAndGet(page, ['input[type="email"]', "#identifierId"]);
  if (!emailInput) {
    if (page.url().includes("myaccount.google.com")) return;
    throw new Error("Không tìm thấy ô nhập email trên trang đăng nhập Google.");
  }

  await emailInput.click({ clickCount: 3 });
  await emailInput.type(email, { delay: 10 + Math.random() * 15 });
  await sleep(300);
  const nextBtn = await page.$("#identifierNext");
  if (nextBtn) await nextBtn.click();
  else await page.keyboard.press("Enter");
  onFlowStep?.("email");
  await sleep(1500);

  const pwdOk = await submitPasswordOnPage(page, password, email);
  if (!pwdOk) {
    throw new Error(`Không tìm thấy ô nhập mật khẩu. URL=${page.url()}`);
  }
  onFlowStep?.("password");
  await sleep(2000);

  let afterPwdText = await page.evaluate(() =>
    document.body.innerText.substring(0, 1000),
  );
  if (isPasswordErrorText(afterPwdText)) {
    throw new Error("Sai mật khẩu Google!");
  }

  if (await findPasswordInputAcrossFrames(page)) {
    await sleep(3000);
    afterPwdText = await page.evaluate(() =>
      document.body.innerText.substring(0, 1000),
    );
    if (isPasswordErrorText(afterPwdText)) {
      throw new Error("Sai mật khẩu Google!");
    }
    if (await findPasswordInputAcrossFrames(page)) {
      throw new Error("Password step did not complete");
    }
  }

  await sleep(1000);
  await moveToTotpChallenge(page);
  const url2fa = page.url();
  if (!url2fa.includes("challenge") && !(await hasVisibleTotpInput(page))) {
    onFlowStep?.("2fa_not_required");
    await sleep(3000);
    return;
  }

  if (!totpSecret) {
    throw new Error("Tài khoản yêu cầu 2FA nhưng chưa thiết lập TOTP secret.");
  }

  let totpInput = await waitAndGet(page, TOTP_INPUT_SELECTORS, 15000);
  if (!totpInput) {
    totpInput = await findFallbackTotpInput(page);
  }
  if (totpInput) {
    const totpCode = await get2FACode(totpSecret);
    if (totpCode) {
      onFlowStep?.("2fa");
      await totpInput.click();
      await sleep(100);
      await totpInput.type(totpCode, { delay: 15 });
      await sleep(300);
      const totpNext =
        (await page.$("#totpNext")) || (await page.$("#idvPreregisteredPhoneNext"));
      if (totpNext) await totpNext.click();
      else await page.keyboard.press("Enter");
      try {
        await page.waitForNavigation({ waitUntil: "networkidle2", timeout: 15000 });
      } catch {

      }
      await sleep(1000);
    }
  }
  await sleep(3000);
}
