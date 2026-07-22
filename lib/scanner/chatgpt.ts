import puppeteer, { type Page, type Browser, type CookieParam } from "puppeteer";
import { prisma } from "@/lib/db";
import { encryptValue, safeDecrypt } from "@/lib/crypto";
import { sleep, get2FACode } from "@/lib/scanner/scanner-utils";
import { detectDeadGptAuthReason, isDeadGptApiResponse } from "@/lib/scanner/gpt-auth";
import { hiddenChromeWindowArgs } from "@/lib/scanner/browser-window";

export interface GptMember {
  id: string;
  name: string;
  email: string;
  role: string;
  status: "active" | "invited";
  created?: string;
}

export interface GptSyncResult {
  status: "success" | "failed";
  members: GptMember[];
  invites: GptMember[];
  plan: string;
  seatsUsed: number;
  accountId: string;
  orgId: string;
  error: string;
  duration: number;
  planExpiresAt: string | null;
}

type StatusCallback = (msg: string) => void;
const noop: StatusCallback = () => {};

const CHROME_PATH =
  process.env.CHROME_EXECUTABLE_PATH ||
  "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe";

const LAUNCH_ARGS = [
  "--no-sandbox",
  "--disable-setuid-sandbox",
  "--disable-blink-features=AutomationControlled",
  ...hiddenChromeWindowArgs(1280, 900),
];

function launchBrowser() {
  return puppeteer.launch({
    headless: false,
    executablePath: CHROME_PATH,
    args: LAUNCH_ARGS,
    defaultViewport: null,
    ignoreDefaultArgs: ["--enable-automation"],
  });
}

interface GptSession {
  cookies: string;
  accessToken: string;
  accountId: string;
  orgId: string;
}

type JsonRecord = Record<string, unknown>;
type BrowserApiResponse = { status: number; json: unknown; body: string };

function toRecord(value: unknown): JsonRecord | null {
  return typeof value === "object" && value !== null ? (value as JsonRecord) : null;
}

function toStringValue(value: unknown): string {
  return typeof value === "string" ? value : "";
}

function toArray(value: unknown): unknown[] {
  return Array.isArray(value) ? value : [];
}

async function throwIfDeadAuthPage(page: Page, context: string): Promise<void> {
  const pageContent = await page
    .evaluate(() => document.body?.innerText || "")
    .catch(() => "");
  const reason = detectDeadGptAuthReason(pageContent);
  if (reason) throw new Error(`ACCOUNT_DEAD: ${context} ${reason}`);
}

function getApiErrorCode(value: unknown): string {
  const root = toRecord(value);
  const error = toRecord(root?.error);
  return toStringValue(error?.code);
}

function throwIfDeadAuthResponse(response: BrowserApiResponse, context: string): void {
  const code = getApiErrorCode(response.json);
  if (!isDeadGptApiResponse(response.status, code, response.body)) return;
  const codePart = code ? ` code=${code}` : "";
  throw new Error(`ACCOUNT_DEAD: ${context} status=${response.status}${codePart}`);
}

async function loadSession(adminId: string): Promise<GptSession | null> {
  const gs = await prisma.googleSession.findUnique({ where: { adminId } });
  if (!gs?.cookies) return null;
  return {
    cookies: gs.cookies,
    accessToken: gs.bearerToken || "",
    accountId: gs.accountId || "",
    orgId: gs.orgId || "",
  };
}

async function saveSession(
  adminId: string,
  cookies: object[],
  token: string,
  accountId: string,
  orgId: string,
) {
  const data = {
    cookies: encryptValue(JSON.stringify(cookies)),
    bearerToken: encryptValue(token),
    accountId,
    orgId,
    updatedAt: new Date(),
  };
  await prisma.googleSession.upsert({
    where: { adminId },
    update: data,
    create: { adminId, ...data },
  });
}

async function browserApiCall(
  page: Page,
  urlPath: string,
  token: string,
  opts: { method?: string; body?: unknown; headers?: Record<string, string> } = {},
): Promise<BrowserApiResponse> {
  try {
    return await page.evaluate(
      async (
        fetchUrl: string,
        bearerToken: string,
        fetchOpts: { method?: string; body?: unknown; headers?: Record<string, string> },
      ) => {
        try {
          const headers: Record<string, string> = {
            "Content-Type": "application/json",
            ...fetchOpts.headers,
          };
          if (bearerToken) headers["Authorization"] = `Bearer ${bearerToken}`;
          const res = await fetch(fetchUrl, {
            method: fetchOpts.method || "GET",
            credentials: "include",
            headers,
            ...(fetchOpts.body ? { body: JSON.stringify(fetchOpts.body) } : {}),
          });
          const text = await res.text();
          let json = null;
          try {
            json = JSON.parse(text);
          } catch {}
          return { status: res.status, json, body: text.slice(0, 5000) };
        } catch (e: unknown) {
          return { status: 0, json: null, body: (e as Error).message };
        }
      },
      `https://chatgpt.com${urlPath}`,
      token,
      opts,
    );
  } catch (e) {
    return { status: 0, json: null, body: (e as Error).message };
  }
}

async function typeInto(page: Page, selectors: string[], text: string, timeout = 10000) {
  for (const sel of selectors) {
    try {
      await page.waitForSelector(sel, { visible: true, timeout });
      await page.click(sel, { clickCount: 3 });
      await page.type(sel, text, { delay: 25 });
      return sel;
    } catch {}
  }
  return null;
}

async function clickBtn(page: Page, selectors: string[], textPattern?: RegExp) {
  for (const sel of selectors) {
    try {
      await page.waitForSelector(sel, { visible: true, timeout: 3000 });
      await page.click(sel);
      return sel;
    } catch {}
  }
  if (textPattern) {
    const btns = await page.$$("button, a[role='button']");
    for (const btn of btns) {
      const text = await page.evaluate((el: Element) => el.textContent?.trim(), btn);
      if (text && textPattern.test(text)) {
        await btn.click();
        return `text:${text}`;
      }
    }
  }
  return null;
}

function findPaidAccount(accountsJson: unknown): {
  accountId: string;
  orgId: string;
  plan: string;
} {
  const root = toRecord(accountsJson);
  const accounts = toRecord(root?.accounts) ?? {};

  for (const [id, val] of Object.entries(accounts)) {
    const accountWrapper = toRecord(val);
    const account = toRecord(accountWrapper?.account);
    const planType = toStringValue(account?.plan_type);
    if (planType && planType !== "free") {
      const structure = toRecord(account?.structure);
      return {
        accountId: id,
        orgId: toStringValue(structure?.organization_id),
        plan: planType || "team",
      };
    }
  }

  const firstId = Object.keys(accounts)[0];
  return { accountId: firstId || "", orgId: "", plan: "team" };
}

async function doLogin(
  page: Page,
  email: string,
  password: string,
  totpSecret: string,
  onStatus: StatusCallback,
): Promise<string | null> {
  onStatus("Đang đăng nhập ChatGPT...");

  let accessToken: string | null = null;
  page.on("response", async (res) => {
    try {
      if (res.url().includes("/api/auth/session") && res.status() === 200) {
        const json = await res.json().catch(() => null);
        if (json?.accessToken) accessToken = json.accessToken;
      }
    } catch {}
  });

  await page.goto("https://chatgpt.com/auth/login", {
    waitUntil: "networkidle2",
    timeout: 60000,
  });
  await page
    .waitForFunction(() => !document.querySelector("#challenge-running"), {
      timeout: 20000,
    })
    .catch(() => {});

  let navigatedToAuth = false;
  for (let attempt = 0; attempt < 3; attempt++) {
    await clickBtn(page, ['button[data-testid="login-button"]'], /log\s*in/i);
    await page
      .waitForNavigation({ waitUntil: "networkidle2", timeout: 15000 })
      .catch(() => {});

    const currentUrl = page.url();
    if (
      currentUrl.includes("auth.openai.com") ||
      currentUrl.includes("auth0.openai.com")
    ) {
      navigatedToAuth = true;
      break;
    }

    const bodyText = await page
      .evaluate(() => document.body?.innerText || "")
      .catch(() => "");
    if (/oops|error occurred/i.test(bodyText)) {
      await page.goto("https://chatgpt.com/auth/login", {
        waitUntil: "networkidle2",
        timeout: 60000,
      });
      await page
        .waitForFunction(() => !document.querySelector("#challenge-running"), {
          timeout: 20000,
        })
        .catch(() => {});
      continue;
    }

    await sleep(1000);
    await page
      .waitForFunction(() => !document.querySelector("#challenge-running"), {
        timeout: 15000,
      })
      .catch(() => {});
  }

  if (!navigatedToAuth) {
    throw new Error(`Login navigation failed — stuck at ${page.url()}`);
  }

  onStatus("Nhập email...");
  const emailResult = await typeInto(
    page,
    ['input[name="email"]', 'input[type="email"]'],
    email,
    20000,
  );
  if (!emailResult) throw new Error(`Email input not found — URL: ${page.url()}`);
  await clickBtn(page, ['button[type="submit"]'], /continue/i);
  await sleep(300);

  onStatus("Nhập mật khẩu...");
  const pwResult = await typeInto(
    page,
    ['input[type="password"]', 'input[name="password"]'],
    password,
    20000,
  );
  if (!pwResult) throw new Error(`Password input not found — URL: ${page.url()}`);
  await clickBtn(page, ['button[type="submit"]'], /continue|log\s*in|sign\s*in/i);
  await sleep(500);

  await throwIfDeadAuthPage(page, "after_password_submit");

  if (totpSecret) {
    onStatus("Xác thực 2FA...");

    const otpSelectors = [
      'input[name="code"]',
      'input[name="totp"]',
      'input[type="tel"]',
      'input[autocomplete="one-time-code"]',
      'input[inputmode="numeric"]',
    ];
    let otpField = null;
    const otpStart = Date.now();
    while (!otpField && Date.now() - otpStart < 15000) {
      await throwIfDeadAuthPage(page, "waiting_otp");
      for (const sel of otpSelectors) {
        try {
          const el = await page.$(sel);
          if (el) {
            const visible = await page.evaluate((e: Element) => {
              const r = e.getBoundingClientRect();
              return (
                r.width > 0 && r.height > 0 && getComputedStyle(e).visibility !== "hidden"
              );
            }, el);
            if (visible) {
              otpField = el;
              break;
            }
          }
        } catch {}
      }
      if (!otpField) await sleep(500);
    }

    if (otpField) {
      const totpCode = await get2FACode(totpSecret);
      if (!totpCode) throw new Error("Failed to get 2FA code");
      await otpField.click({ clickCount: 3 });
      await otpField.type(totpCode, { delay: 25 });
      await clickBtn(page, ['button[type="submit"]'], /continue|verify/i);
      await sleep(500);
      await throwIfDeadAuthPage(page, "after_otp_submit");
    } else {
      await throwIfDeadAuthPage(page, "otp_missing");
      throw new Error(`OTP input not found — URL: ${page.url()}`);
    }
  }

  try {
    await page.waitForNavigation({ waitUntil: "networkidle2", timeout: 8000 });
  } catch {}

  const wsStart = Date.now();
  while (Date.now() - wsStart < 10000) {
    await throwIfDeadAuthPage(page, "workspace_navigation");
    const currentUrl = page.url();
    if (currentUrl.includes("/workspace") || currentUrl.includes("accounts.openai.com")) {
      onStatus("Chọn workspace...");
      await sleep(500);
      await page.evaluate(() => {
        const items = document.querySelectorAll(
          "button, a, [role='button'], [data-testid]",
        );
        for (const el of items) {
          const t = (el.textContent || "").trim();
          if (/terms|privacy|chatgpt|log\s*out/i.test(t)) continue;
          if (t.length > 0 && t.length < 100) {
            (el as HTMLElement).click();
            return;
          }
        }
      });
      try {
        await page.waitForNavigation({ waitUntil: "networkidle2", timeout: 10000 });
      } catch {}
      break;
    }
    if (currentUrl.includes("chatgpt.com") && !currentUrl.includes("/auth")) break;
    await sleep(500);
  }

  await page
    .waitForFunction(() => window.location.hostname === "chatgpt.com", { timeout: 10000 })
    .catch(() => {});

  if (!accessToken) {
    const session = await page.evaluate(async () => {
      try {
        const res = await fetch("/api/auth/session", { credentials: "include" });
        return await res.json();
      } catch {
        return null;
      }
    });
    if (session?.accessToken) accessToken = session.accessToken;
  }

  return accessToken;
}

async function tryRestoreSession(
  page: Page,
  adminId: string,
  onStatus: StatusCallback,
): Promise<{ token: string; accountId: string; orgId: string } | null> {
  const saved = await loadSession(adminId);
  if (!saved?.cookies) return null;

  onStatus("Khôi phục session...");

  let cookies: CookieParam[];
  try {
    cookies = JSON.parse(safeDecrypt(saved.cookies)) as CookieParam[];
    if (!Array.isArray(cookies) || cookies.length === 0) return null;
  } catch {
    return null;
  }

  await page.setCookie(...cookies);
  await page.goto("https://chatgpt.com/", {
    waitUntil: "networkidle2",
    timeout: 45000,
  });
  await sleep(4000);

  if (page.url().includes("/auth/login") || page.url().includes("auth.openai.com")) {
    onStatus("Session hết hạn");
    return null;
  }

  onStatus("Lấy token mới...");
  const session = (await page.evaluate(async () => {
    try {
      const res = await fetch("/api/auth/session", { credentials: "include" });
      return await res.json();
    } catch {
      return null;
    }
  })) as { accessToken?: string } | null;

  if (!session?.accessToken) return null;

  const test = await browserApiCall(page, "/backend-api/me", session.accessToken);
  throwIfDeadAuthResponse(test, "restore_session_me");
  if (test.status !== 200 || !test.json) return null;

  onStatus("Session khôi phục thành công");
  return { token: session.accessToken, accountId: saved.accountId, orgId: saved.orgId };
}

async function syncWorkspaceData(
  page: Page,
  token: string,
  onStatus: StatusCallback,
  preAccountId?: string,
): Promise<{
  members: GptMember[];
  invites: GptMember[];
  plan: string;
  seatsUsed: number;
  accountId: string;
  orgId: string;
  planExpiresAt: string | null;
}> {
  let accountId = preAccountId || "";
  let orgId = "";
  let plan = "team";

  if (!accountId) {
    onStatus("Lấy danh sách workspace...");
    const acc = await browserApiCall(
      page,
      "/backend-api/accounts/check/v4-2023-04-27",
      token,
    );
    throwIfDeadAuthResponse(acc, "accounts_check");
    if (acc.status !== 200 || !acc.json) throw new Error("Failed to fetch accounts");
    const found = findPaidAccount(acc.json);
    accountId = found.accountId;
    orgId = found.orgId;
    plan = found.plan;
    if (!accountId) throw new Error("No workspace found");
  }

  onStatus("Lấy thành viên & lời mời...");
  const [membersRes, invitesRes, subRes] = await Promise.all([
    browserApiCall(page, `/backend-api/accounts/${accountId}/users`, token),
    browserApiCall(page, `/backend-api/accounts/${accountId}/invites`, token),
    browserApiCall(page, `/backend-api/subscriptions?account_id=${accountId}`, token, {
      headers: { "ChatGPT-Account-ID": accountId },
    }),
  ]);
  throwIfDeadAuthResponse(membersRes, "members_fetch");
  throwIfDeadAuthResponse(invitesRes, "invites_fetch");
  throwIfDeadAuthResponse(subRes, "subscription_fetch");

  let planExpiresAt: string | null = null;
  if (subRes.status === 200 && subRes.json) {
    const subscription = toRecord(subRes.json);
    const activeUntilVal = subscription?.active_until;
    if (activeUntilVal) {
      if (typeof activeUntilVal === "number") {
        planExpiresAt = new Date(activeUntilVal * 1000).toISOString();
      } else if (typeof activeUntilVal === "string") {
        planExpiresAt = new Date(activeUntilVal).toISOString();
      }
    }
  }

  const members: GptMember[] = [];
  if (membersRes.status === 200 && membersRes.json) {
    const membersResponse = toRecord(membersRes.json);
    const primaryItems = toArray(membersResponse?.items);
    const items =
      primaryItems.length > 0 ? primaryItems : toArray(membersResponse?.users);
    for (const m of items) {
      const member = toRecord(m);
      const user = toRecord(member?.user);
      const created =
        toStringValue(member?.created) || toStringValue(member?.created_time);
      members.push({
        id:
          toStringValue(member?.account_user_id) ||
          toStringValue(user?.id) ||
          toStringValue(member?.id),
        name: toStringValue(member?.name) || toStringValue(user?.name),
        email: toStringValue(member?.email) || toStringValue(user?.email),
        role: toStringValue(member?.role) || "member",
        status: "active",
        created: created || undefined,
      });
    }
  }

  const invites: GptMember[] = [];
  if (invitesRes.status === 200 && invitesRes.json) {
    const invitesResponse = toRecord(invitesRes.json);
    const items = toArray(invitesResponse?.items);
    for (const inv of items) {
      const invite = toRecord(inv);
      invites.push({
        id: toStringValue(invite?.id),
        name: toStringValue(invite?.email_address) || toStringValue(invite?.email),
        email: toStringValue(invite?.email_address) || toStringValue(invite?.email),
        role: toStringValue(invite?.role) || "standard-user",
        status: "invited",
      });
    }
  }

  return {
    members,
    invites,
    plan,
    seatsUsed: members.length,
    accountId,
    orgId,
    planExpiresAt,
  };
}

export async function gptInviteMembers(
  adminId: string,
  emails: string[],
  email: string,
  encryptedPassword: string,
  encryptedTotp: string,
): Promise<{ results: { email: string; success: boolean; error?: string }[] }> {
  const { page, browser, token, accountId } = await getAuthenticatedPage(
    adminId,
    email,
    encryptedPassword,
    encryptedTotp,
    noop,
  );
  try {
    const results: { email: string; success: boolean; error?: string }[] = [];
    const res = await browserApiCall(
      page,
      `/backend-api/accounts/${accountId}/invites`,
      token,
      {
        method: "POST",
        body: {
          email_addresses: emails.map((e) => e.trim().toLowerCase()),
          role: "standard-user",
        },
      },
    );
    if (res.status === 200 && res.json) {
      const data = toRecord(res.json);
      const invited = toArray(data?.account_invites)
        .map((entry) => toRecord(entry))
        .filter((entry): entry is JsonRecord => entry !== null);
      const errored = toArray(data?.errored_emails).map((entry) => {
        const record = toRecord(entry);
        return {
          email_address: toStringValue(record?.email_address),
          error: toStringValue(record?.error),
        };
      });
      const invitedSet = new Set(
        invited
          .map((entry) => toStringValue(entry.email_address).toLowerCase())
          .filter((entry) => entry.length > 0),
      );
      const erroredMap = new Map(
        errored.map((e) => [e.email_address?.toLowerCase(), e.error || "Lỗi từ ChatGPT"]),
      );
      for (const invEmail of emails) {
        const lower = invEmail.trim().toLowerCase();
        if (erroredMap.has(lower)) {
          results.push({ email: lower, success: false, error: erroredMap.get(lower) });
        } else {
          results.push({ email: lower, success: invitedSet.has(lower) });
        }
      }
    } else {
      const detail = toStringValue(toRecord(res.json)?.detail);
      const errMsg = detail || `HTTP ${res.status}`;
      for (const invEmail of emails) {
        results.push({ email: invEmail, success: false, error: String(errMsg) });
      }
    }
    return { results };
  } finally {
    await browser.close();
  }
}

export async function gptRevokeInvite(
  adminId: string,
  inviteId: string,
  memberEmail: string,
  email: string,
  encryptedPassword: string,
  encryptedTotp: string,
): Promise<{ success: boolean; error?: string }> {
  try {
    const { page, browser, token, accountId } = await getAuthenticatedPage(
      adminId,
      email,
      encryptedPassword,
      encryptedTotp,
      noop,
    );
    try {
      const res = await browserApiCall(
        page,
        `/backend-api/accounts/${accountId}/invites`,
        token,
        {
          method: "DELETE",
          body: { email_address: memberEmail },
        },
      );
      if (res.status === 200 || res.status === 204) return { success: true };
      const detail = toStringValue(toRecord(res.json)?.detail);
      return {
        success: false,
        error: detail || `HTTP ${res.status}`,
      };
    } finally {
      await browser.close();
    }
  } catch (e) {
    return { success: false, error: (e as Error).message };
  }
}

const AUTH_TIMEOUT_MS = 90_000;

async function getAuthenticatedPage(
  adminId: string,
  email: string,
  encryptedPassword: string,
  encryptedTotp: string,
  onStatus: StatusCallback,
): Promise<{ page: Page; browser: Browser; token: string; accountId: string }> {
  const password = safeDecrypt(encryptedPassword);
  const totpSecret = safeDecrypt(encryptedTotp);

  let activeBrowser: Browser | null = null;
  const setBrowser = (b: Browser) => {
    activeBrowser = b;
  };

  try {
    return await Promise.race([
      doGetAuthenticatedPage(adminId, email, password, totpSecret, onStatus, setBrowser),
      new Promise<never>((_, reject) =>
        setTimeout(async () => {
          if (activeBrowser) {
            await activeBrowser.close().catch(() => {});
            activeBrowser = null;
          }
          reject(new Error("Auth timeout (90s)"));
        }, AUTH_TIMEOUT_MS),
      ),
    ]);
  } catch (e) {
    if (activeBrowser) {
      await (activeBrowser as Browser).close().catch(() => {});
    }
    throw e;
  }
}

async function doGetAuthenticatedPage(
  adminId: string,
  email: string,
  password: string,
  totpSecret: string,
  onStatus: StatusCallback,
  setBrowser: (b: Browser) => void,
): Promise<{ page: Page; browser: Browser; token: string; accountId: string }> {
  const browser = await launchBrowser();
  setBrowser(browser);
  browser.on("targetcreated", async (target) => {
    if (target.type() !== "page") return;
    try {
      await target.page();
    } catch {}
  });
  const page = await browser.newPage();

  const restored = await tryRestoreSession(page, adminId, onStatus);
  if (restored) {
    return { page, browser, token: restored.token, accountId: restored.accountId };
  }

  onStatus("Session hết hạn, đăng nhập lại...");
  const client = await page.createCDPSession();
  await client.send("Network.clearBrowserCookies");

  const token = await doLogin(page, email, password, totpSecret, onStatus);
  if (!token) {
    await browser.close();
    throw new Error("Login ChatGPT thất bại");
  }

  const acc = await browserApiCall(
    page,
    "/backend-api/accounts/check/v4-2023-04-27",
    token,
  );
  throwIfDeadAuthResponse(acc, "post_login_accounts_check");
  const { accountId, orgId } =
    acc.status === 200 && acc.json
      ? findPaidAccount(acc.json)
      : { accountId: "", orgId: "" };

  await sleep(3000);
  const cookies = await page.cookies();
  await saveSession(adminId, cookies, token, accountId, orgId);

  return { page, browser, token, accountId };
}

export async function syncGptWorkspace(
  adminId: string,
  email: string,
  encryptedPassword: string,
  encryptedTotp: string,
  onStatus: StatusCallback = noop,
): Promise<GptSyncResult> {
  const start = Date.now();
  let browser: Browser | null = null;
  try {
    const auth = await getAuthenticatedPage(
      adminId,
      email,
      encryptedPassword,
      encryptedTotp,
      onStatus,
    );
    browser = auth.browser;

    onStatus("Đang đồng bộ dữ liệu...");
    await sleep(2000);
    const data = await syncWorkspaceData(auth.page, auth.token, onStatus, auth.accountId);

    await sleep(3000);
    const cookies = await auth.page.cookies();
    await browser.close();
    browser = null;

    await saveSession(
      adminId,
      cookies,
      auth.token,
      data.accountId || auth.accountId,
      data.orgId,
    );

    onStatus("Đồng bộ hoàn tất");
    return {
      status: "success",
      members: data.members,
      invites: data.invites,
      plan: data.plan,
      seatsUsed: data.seatsUsed,
      accountId: data.accountId,
      orgId: data.orgId,
      planExpiresAt: data.planExpiresAt,
      error: "",
      duration: Date.now() - start,
    };
  } catch (e) {
    return {
      status: "failed",
      members: [],
      invites: [],
      plan: "",
      seatsUsed: 0,
      accountId: "",
      orgId: "",
      planExpiresAt: null,
      error: (e as Error).message,
      duration: Date.now() - start,
    };
  } finally {
    if (browser) await browser.close().catch(() => {});
  }
}
