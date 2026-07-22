import puppeteer, { type Page, type ElementHandle, type Browser } from "puppeteer";
import { prisma } from "@/lib/db";
import {
  googleHttpReq as httpReq,
  getAtTokenAndAuthuser,
  type Cookie,
} from "@/lib/scanner/google-http";
import { encryptValue, safeDecrypt } from "@/lib/crypto";
import { apiError } from "@/lib/logger";

import { hiddenChromeWindowArgs } from "@/lib/scanner/browser-window";
import {
  TOTP_INPUT_SELECTORS,
  findFallbackTotpInput,
  moveToTotpChallenge,
} from "@/lib/scanner/google-auth-challenge";

export interface SyncedMember {
  name: string;
  email: string;
  role: string;
  status: "active" | "invited" | "disabled";
  inviteId?: string;
  googleUserId?: string;
}

export interface SyncResult {
  status: "success" | "failed" | "partial";
  members: SyncedMember[];
  manager: string | null;
  planName: string;
  planExpiresAt: Date | null;
  credits: number | null;
  usedStorageMB?: number | null;
  error: string;
  duration: number;
}

export interface MemberStorageUsage {
  name: string;
  email: string;
  googleUserId?: string;
  usedText: string | null;
  usedBytes: number | null;
}

export interface FamilyStorageUsageResult {
  totalUsedText: string | null;
  totalUsedBytes: number | null;
  members: MemberStorageUsage[];
  error?: string;
}

import { sleep, get2FACode } from "@/lib/scanner/scanner-utils";

const PASSWORD_INPUT_SELECTORS = [
  'input[type="password"]',
  'input[name="Passwd"]',
  'input[name="password"]',
  'input[autocomplete="current-password"]',
];

async function waitAndGet(
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
      } catch { }
    }
    await sleep(500);
  }
  return null;
}

function normalizeChallengeText(text: string): string {
  return text
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .toLowerCase()
    .replace(/\s+/g, " ")
    .trim();
}

async function hasVisiblePasswordInput(page: Page): Promise<boolean> {
  try {
    return await page.evaluate((selectors) => {
      for (const selector of selectors) {
        for (const el of document.querySelectorAll(selector)) {
          const input = el as HTMLInputElement;
          const rect = input.getBoundingClientRect();
          const style = getComputedStyle(input);
          if (
            rect.width > 30 &&
            rect.height > 10 &&
            style.visibility !== "hidden" &&
            style.display !== "none"
          ) {
            return true;
          }
        }
      }
      return false;
    }, PASSWORD_INPUT_SELECTORS);
  } catch {
    return false;
  }
}

function isPasswordErrorText(text: string): boolean {
  return (
    text.includes("wrong password") ||
    text.includes("incorrect password") ||
    text.includes("sai mat khau") ||
    text.includes("mat khau khong chinh xac")
  );
}

export function detectDeadGoogleAuthReason(text: string): string | null {
  const normalized = normalizeChallengeText(text);
  if (normalized.includes("da vo hieu hoa tai khoan")) return "account disabled";
  if (normalized.includes("tai khoan da bi vo hieu hoa")) return "account disabled";
  if (normalized.includes("your account has been disabled")) return "account disabled";
  if (normalized.includes("account has been disabled")) return "account disabled";
  if (normalized.includes("account disabled")) return "account disabled";
  if (normalized.includes("account suspended")) return "account suspended";
  if (normalized.includes("account banned")) return "account banned";
  if (
    normalized.includes("hoat dong bat thuong") &&
    normalized.includes("khoa tai khoan")
  ) {
    return "account locked after suspicious activity";
  }
  if (normalized.includes("co gang khoi phuc") && normalized.includes("khoa")) {
    return "account recovery required";
  }
  return null;
}

export function isDeadGoogleErrorMessage(message: string): boolean {
  const normalized = message.toLowerCase();
  if (normalized.includes("google_account_dead")) return true;
  return detectDeadGoogleAuthReason(message) !== null;
}

async function throwIfDeadGooglePage(page: Page, context: string) {
  const bodyText = await page.evaluate(() => document.body.innerText.substring(0, 2000));
  const reason = detectDeadGoogleAuthReason(bodyText);
  if (reason) throw new Error(`GOOGLE_ACCOUNT_DEAD: ${context} ${reason}`);
}

async function loadSession(
  adminId: string,
): Promise<{ cookies: Cookie[] | null; bearerToken: string | null }> {
  try {
    const session = await prisma.googleSession.findUnique({ where: { adminId } });
    if (!session) return { cookies: null, bearerToken: null };
    let cookies: Cookie[] | null = null;
    try {
      const decryptedCookies = safeDecrypt(session.cookies);
      cookies = JSON.parse(decryptedCookies);
      if (!Array.isArray(cookies) || cookies.length === 0) cookies = null;
    } catch (e) {
      apiError("loadSession", e, { context: "decrypt cookies" });
      cookies = null;
    }
    const bearerToken = session.bearerToken ? safeDecrypt(session.bearerToken) : null;
    return { cookies, bearerToken: bearerToken || null };
  } catch (e) {
    apiError("loadSession", e);
    return { cookies: null, bearerToken: null };
  }
}

async function saveSession(adminId: string, cookies: Cookie[], bearerToken?: string) {
  const data: { cookies: string; bearerToken?: string; tokenSavedAt?: Date } = {
    cookies: encryptValue(JSON.stringify(cookies)),
  };
  if (bearerToken) {
    data.bearerToken = encryptValue(bearerToken);
    data.tokenSavedAt = new Date();
  }
  await prisma.googleSession.upsert({
    where: { adminId },
    update: data,
    create: { adminId, ...data },
  });
}

function findMembers(
  obj: unknown,
  members: SyncedMember[],
  setManager: (m: string) => void,
  depth = 0,
) {
  if (depth > 12) return;
  if (Array.isArray(obj)) {
    if (
      obj.length >= 2 &&
      Array.isArray(obj[0]) &&
      obj[0].length >= 6 &&
      typeof obj[0][5] === "string" &&
      String(obj[0][5]).includes("@") &&
      typeof obj[1] === "number"
    ) {
      const info = obj[0];
      const email = info[5] as string;
      const name = (info[0] as string) || email?.split("@")[0] || "";
      if (email) {
        const role = members.length === 0 ? "FAMILY MANAGER" : "Member";
        const isDisabled = info[0] === null || info[0] === undefined;
        const isInvited = info[1] === null && !isDisabled;
        const status =
          role === "FAMILY MANAGER"
            ? ("active" as const)
            : isDisabled
              ? ("disabled" as const)
              : isInvited
                ? ("invited" as const)
                : ("active" as const);
        let inviteId: string | undefined;
        if (isInvited && Array.isArray(obj[9]) && obj[9][0]) {
          inviteId = String(obj[9][0]);
        }
        const googleUserId = info[1] ? String(info[1]) : undefined;
        if (members.length === 0) setManager(`${name} (${email})`);
        if (!members.find((m) => m.email === email)) {
          members.push({ name, email, role, status, inviteId, googleUserId });
        }
        return;
      }
    }
    if (
      obj.length >= 6 &&
      typeof obj[0] === "string" &&
      typeof obj[5] === "string" &&
      String(obj[5]).includes("@") &&
      obj[0].length > 0
    ) {
      const role = members.length === 0 ? "FAMILY MANAGER" : "Member";
      if (members.length === 0) setManager(`${obj[0]} (${obj[5]})`);
      if (!members.find((m) => m.email === obj[5])) {
        members.push({
          name: obj[0] as string,
          email: obj[5] as string,
          role,
          status: "active",
        });
      }
    } else {
      for (const item of obj) findMembers(item, members, setManager, depth + 1);
    }
  } else if (obj && typeof obj === "object") {
    for (const v of Object.values(obj)) findMembers(v, members, setManager, depth + 1);
  }
}

export async function checkGoogleOneSharing(
  cookies: Cookie[],
): Promise<{ sharing: boolean | null; error: string }> {
  try {
    const resp = await httpReq("https://myaccount.google.com/family/details", {
      cookies,
    });
    if (
      resp.finalUrl.includes("accounts.google.com/ServiceLogin") ||
      resp.finalUrl.includes("signin")
    ) {
      return { sharing: null, error: "Session expired" };
    }
    if (resp.status !== 200) return { sharing: null, error: `HTTP ${resp.status}` };

    const html = resp.body;
    let atToken: string | null = null;
    let m = html.match(/"SNlM0e"\s*:\s*"([^"]+)"/);
    if (m) atToken = m[1];
    if (!atToken) {
      m = html.match(/'at'\s*:\s*'([^']+)'/);
      if (m) atToken = m[1];
    }
    if (!atToken) return { sharing: null, error: "Cannot find atToken" };

    let fSid = "";
    const sidMatch = html.match(/"FdrFJe"\s*:\s*"(-?\d+)"/);
    if (sidMatch) fSid = sidMatch[1];

    const reqid = Math.floor(Math.random() * 900000) + 100000;
    const batchUrl = `https://myaccount.google.com/_/AccountSettingsUi/data/batchexecute?rpcids=t5583&source-path=%2Ffamily%2Fdetails&f.sid=${fSid}&hl=en&soc-app=1&soc-platform=1&soc-device=1&_reqid=${reqid}&rt=c`;
    const t5583Inner = "[[12],null,null,[null,null,null,null,null,null,null,null]]";
    const t5583Req = JSON.stringify([[["t5583", t5583Inner, null, "1"]]]);
    const postBody = `f.req=${encodeURIComponent(t5583Req)}&at=${encodeURIComponent(atToken)}&`;

    const batchResp = await httpReq(batchUrl, {
      method: "POST",
      cookies,
      headers: {
        "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8",
        Referer: "https://myaccount.google.com/family/details",
        "X-Same-Domain": "1",
        Origin: "https://myaccount.google.com",
      },
      body: postBody,
    });

    if (batchResp.status !== 200)
      return { sharing: null, error: `Batch HTTP ${batchResp.status}` };

    const hasStopSharing = batchResp.body.includes("stopgoogleonesharing");
    const hasStartSharing = batchResp.body.includes("startgoogleonesharing");

    if (hasStopSharing) return { sharing: true, error: "" };
    if (hasStartSharing) return { sharing: false, error: "" };

    return { sharing: null, error: "Cannot determine sharing status" };
  } catch (e) {
    void e;
    return { sharing: null, error: "Failed to read sharing status" };
  }
}

function parseSizeTextToBytes(sizeText: string): number | null {
  const m = sizeText.match(/([0-9]+(?:[.,][0-9]+)?)\s*(KB|MB|GB|TB)/i);
  if (!m) return null;
  const value = Number(m[1].replace(",", "."));
  if (!Number.isFinite(value)) return null;
  const unit = m[2].toUpperCase();
  const scale =
    unit === "TB"
      ? 1024 ** 4
      : unit === "GB"
        ? 1024 ** 3
        : unit === "MB"
          ? 1024 ** 2
          : 1024;
  return Math.round(value * scale);
}

function extractUsageText(raw: string): string | null {
  const patterns = [
    /([0-9]+(?:[.,][0-9]+)?\s*(?:KB|MB|GB|TB)\s*out of\s*[0-9]+(?:[.,][0-9]+)?\s*(?:KB|MB|GB|TB)\s*used\.?)/i,
    /([0-9]+(?:[.,][0-9]+)?\s*(?:KB|MB|GB|TB)\s*đã dùng[^0-9]{1,20}[0-9]+(?:[.,][0-9]+)?\s*(?:KB|MB|GB|TB))/i,
    /([0-9]+(?:[.,][0-9]+)?\s*(?:KB|MB|GB|TB)\s*used)/i,
  ];
  for (const p of patterns) {
    const m = raw.match(p);
    if (m?.[1]) return m[1];
  }
  return null;
}

function extractAtTokenAndSid(html: string): { atToken: string | null; fSid: string } {
  let atToken: string | null = null;
  let m = html.match(/"SNlM0e"\s*:\s*"([^"]+)"/);
  if (m) atToken = m[1];
  if (!atToken) {
    m = html.match(/'at'\s*:\s*'([^']+)'/);
    if (m) atToken = m[1];
  }
  let fSid = "";
  const sidMatch = html.match(/"FdrFJe"\s*:\s*"(-?\d+)"/);
  if (sidMatch) fSid = sidMatch[1];
  return { atToken, fSid };
}

async function fetchFamilyDetailsPayload(
  cookies: Cookie[],
): Promise<{ raw: string | null; totalUsedText: string | null; pendingInvites: SyncedMember[] }> {
  const pageResp = await httpReq("https://myaccount.google.com/family/details", {
    cookies,
  });
  if (
    pageResp.finalUrl.includes("accounts.google.com/ServiceLogin") ||
    pageResp.finalUrl.includes("signin")
  ) {
    return { raw: null, totalUsedText: null, pendingInvites: [] };
  }
  if (pageResp.status !== 200) return { raw: null, totalUsedText: null, pendingInvites: [] };

  const { atToken, fSid } = extractAtTokenAndSid(pageResp.body);
  if (!atToken) return { raw: null, totalUsedText: null, pendingInvites: [] };

  const reqid = Math.floor(Math.random() * 900000) + 100000;
  const batchUrl = `https://myaccount.google.com/_/AccountSettingsUi/data/batchexecute?rpcids=t5583&source-path=%2Ffamily%2Fdetails&f.sid=${fSid}&hl=en&soc-app=1&soc-platform=1&soc-device=1&_reqid=${reqid}&rt=c`;
  const t5583Inner = "[[12],null,null,[null,null,null,null,null,null,null,null]]";
  const t5583Req = JSON.stringify([[["t5583", t5583Inner, null, "1"]]]);
  const postBody = `f.req=${encodeURIComponent(t5583Req)}&at=${encodeURIComponent(atToken)}&`;

  const batchResp = await httpReq(batchUrl, {
    method: "POST",
    cookies,
    headers: {
      "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8",
      Referer: "https://myaccount.google.com/family/details",
      "X-Same-Domain": "1",
      Origin: "https://myaccount.google.com",
    },
    body: postBody,
  });
  if (batchResp.status !== 200) return { raw: null, totalUsedText: null, pendingInvites: [] };

  const totalUsedText = extractUsageText(batchResp.body);
  const t5583Members = parseMembersFromT5583(batchResp.body);
  const pendingInvites: SyncedMember[] = t5583Members
    .filter((m) => m.status === "invited")
    .map((m) => ({ name: m.name, email: m.email, role: m.role, status: m.status as "invited", inviteId: m.inviteId }));
  return { raw: batchResp.body, totalUsedText, pendingInvites };
}

function isManagerRole(roleLabel: string): boolean {
  const lower = roleLabel.toLowerCase();
  return (
    lower.includes("manager") ||
    lower.includes("quản lý") ||
    lower.includes("quan ly")
  );
}

function parseMembersFromT5583(body: string): SyncedMember[] {
  const payload = parseWrbRpcPayload(body, "t5583");
  if (!payload) return [];

  const result: SyncedMember[] = [];

  function walk(obj: unknown, depth: number): void {
    if (depth > 15 || !Array.isArray(obj)) return;

    if (
      obj.length >= 4 &&
      typeof obj[0] === "string" &&
      typeof obj[1] === "string" &&
      Array.isArray(obj[3])
    ) {
      let memberPath: string | null = null;
      try {
        const nav = obj[3];
        if (
          Array.isArray(nav[0]) &&
          Array.isArray(nav[0][0]) &&
          typeof nav[0][0][2] === "string"
        ) {
          memberPath = nav[0][0][2] as string;
        }
      } catch { }

      if (memberPath && memberPath.includes("family/member/")) {
        const displayName = obj[0] as string;
        const roleLabel = obj[1] as string;
        const isInvite = memberPath.includes("/i/");
        const isManager = !isInvite && isManagerRole(roleLabel);

        if (isInvite) {
          const idMatch = memberPath.match(/\/i\/([^?/]+)/);
          const inviteId = idMatch?.[1];
          const email = displayName.includes("@") ? displayName : "";
          if (inviteId && email) {
            result.push({
              name: "",
              email,
              role: "Member",
              status: "invited",
              inviteId,
            });
          }
        } else {
          const idMatch = memberPath.match(/\/g\/([^?/]+)/);
          const googleUserId = idMatch?.[1];
          result.push({
            name: displayName,
            email: "",
            role: isManager ? "FAMILY MANAGER" : "Member",
            status: "active",
            googleUserId,
          });
        }
        return;
      }
    }

    for (const item of obj) {
      if (Array.isArray(item)) walk(item, depth + 1);
    }
  }

  walk(payload as unknown[], 0);
  return result;
}

async function fetchMemberUsageText(
  cookies: Cookie[],
  googleUserId: string,
): Promise<string | null> {
  const memberUrl = `https://myaccount.google.com/family/member/g/${googleUserId}?hl=en`;
  const memberResp = await httpReq(memberUrl, { cookies });
  if (
    memberResp.finalUrl.includes("accounts.google.com/ServiceLogin") ||
    memberResp.finalUrl.includes("signin")
  ) {
    return null;
  }
  if (memberResp.status !== 200) return null;

  const directText = extractUsageText(memberResp.body);
  if (directText) return directText;

  const { atToken, fSid } = extractAtTokenAndSid(memberResp.body);
  if (!atToken) return null;

  const reqid = Math.floor(Math.random() * 900000) + 100000;
  const sourcePath = encodeURIComponent(`/family/member/g/${googleUserId}`);
  const batchUrl = `https://myaccount.google.com/_/AccountSettingsUi/data/batchexecute?rpcids=V2esPe&source-path=${sourcePath}&f.sid=${fSid}&hl=en&soc-app=1&soc-platform=1&soc-device=1&_reqid=${reqid}&rt=c`;
  const postBody = `f.req=${encodeURIComponent('[[["V2esPe","[null,null,1,null,1,1]",null,"1"]]]')}&at=${encodeURIComponent(atToken)}&`;

  const batchResp = await httpReq(batchUrl, {
    method: "POST",
    cookies,
    headers: {
      "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8",
      Referer: memberUrl,
      "X-Same-Domain": "1",
      Origin: "https://myaccount.google.com",
    },
    body: postBody,
  });
  if (batchResp.status !== 200) return null;
  return extractUsageText(batchResp.body);
}

export async function fetchFamilyMemberStorageUsage(
  cookies: Cookie[],
): Promise<FamilyStorageUsageResult> {
  try {
    const base = await syncFamily(cookies);
    if (base.expired) {
      return {
        totalUsedText: null,
        totalUsedBytes: null,
        members: [],
        error: "Session expired",
      };
    }

    const detailsPayload = await fetchFamilyDetailsPayload(cookies);
    const totalUsedText = detailsPayload.totalUsedText;
    const totalUsedBytes = totalUsedText ? parseSizeTextToBytes(totalUsedText) : null;

    const members: MemberStorageUsage[] = [];
    for (const m of base.members) {
      const memberUsageText = m.googleUserId
        ? await fetchMemberUsageText(cookies, m.googleUserId)
        : null;
      members.push({
        name: m.name,
        email: m.email,
        googleUserId: m.googleUserId,
        usedText: memberUsageText,
        usedBytes: memberUsageText ? parseSizeTextToBytes(memberUsageText) : null,
      });
    }

    return {
      totalUsedText,
      totalUsedBytes,
      members,
    };
  } catch (e) {
    apiError("fetchFamilyMemberStorageUsage", e);
    return {
      totalUsedText: null,
      totalUsedBytes: null,
      members: [],
      error: "Failed to read member storage",
    };
  }
}

export async function toggleGoogleOneSharing(
  cookies: Cookie[],
  enable: boolean,
): Promise<{ success: boolean; error: string }> {
  const batchHeaders = {
    "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8",
    "X-Same-Domain": "1",
    Origin: "https://myaccount.google.com",
  };

  const actionPath = enable ? "startgoogleonesharing" : "stopgoogleonesharing";
  const pageUrl = `https://myaccount.google.com/family/${actionPath}`;

  const pageResp = await httpReq(pageUrl, { cookies });
  if (
    pageResp.finalUrl.includes("accounts.google.com/ServiceLogin") ||
    pageResp.finalUrl.includes("signin")
  ) {
    return { success: false, error: "Session expired" };
  }
  if (pageResp.status !== 200)
    return { success: false, error: `HTTP ${pageResp.status}` };

  const html = pageResp.body;
  let atToken: string | null = null;
  let m: RegExpMatchArray | null;
  m = html.match(/"SNlM0e"\s*:\s*"([^"]+)"/);
  if (m) atToken = m[1];
  if (!atToken) {
    m = html.match(/'at'\s*:\s*'([^']+)'/);
    if (m) atToken = m[1];
  }
  if (!atToken) return { success: false, error: "Cannot find atToken on sharing page" };

  let fSid = "";
  const sidMatch = html.match(/"FdrFJe"\s*:\s*"(-?\d+)"/);
  if (sidMatch) fSid = sidMatch[1];

  const afRegex =
    /AF_initDataCallback\(\{[^}]*key:\s*'([^']*)'[^}]*data:([\s\S]*?)\}\);/g;
  let confirmToken: string | null = null;
  let afMatch;

  while ((afMatch = afRegex.exec(html)) !== null) {
    try {
      const data = JSON.parse(afMatch[2].trim());
      const findToken = (obj: unknown, depth = 0): void => {
        if (depth > 10) return;
        if (typeof obj === "string" && obj.length > 20 && obj.length < 200) {
          if (!obj.includes("http") && !obj.includes("google") && !confirmToken) {
            confirmToken = obj;
          }
        }
        if (Array.isArray(obj)) {
          for (const item of obj) findToken(item, depth + 1);
        }
      };
      findToken(data);
    } catch { }
  }

  const possibleRpcIds = ["Wffnob", "FmB5Db", "c5gch", "OXB1be", "ELbMR"];

  let dsMatch;
  const dsData: Record<string, unknown> = {};
  const htmlCopy = html;
  const dsRegex2 =
    /AF_initDataCallback\(\{[^}]*key:\s*'ds:(\d+)'[^}]*data:([\s\S]*?)\}\);/g;
  while ((dsMatch = dsRegex2.exec(htmlCopy)) !== null) {
    try {
      dsData[dsMatch[1]] = JSON.parse(dsMatch[2].trim());
    } catch { }
  }

  const reqid = Math.floor(Math.random() * 900000) + 100000;

  const rpcInner = JSON.stringify([[null, null, ["v2", 18, null, ["googleaccount"]]]]);
  for (const rpcId of possibleRpcIds) {
    const batchUrl = `https://myaccount.google.com/_/AccountSettingsUi/data/batchexecute?rpcids=${rpcId}&source-path=%2Ffamily%2F${actionPath}&f.sid=${fSid}&hl=en&soc-app=1&soc-platform=1&soc-device=1&_reqid=${reqid}&rt=c`;
    const postBody = `f.req=${encodeURIComponent(`[[["${rpcId}",${JSON.stringify(rpcInner)},null,"generic"]]]`)}&at=${encodeURIComponent(atToken)}&`;

    try {
      const resp = await httpReq(batchUrl, {
        method: "POST",
        cookies,
        headers: {
          ...batchHeaders,
          Referer: pageUrl,
        },
        body: postBody,
      });

      if (
        resp.status === 200 &&
        resp.body.includes("wrb.fr") &&
        resp.body.includes(rpcId)
      ) {
        const checkResult = await checkGoogleOneSharing(cookies);
        if (checkResult.sharing === enable) {
          return { success: true, error: "" };
        }
      }
    } catch { }
  }

  return {
    success: false,
    error: `Could not toggle sharing. Need to capture the network log when toggling to identify the exact RPC.`,
  };
}

interface SyncFamilyResult {
  expired: boolean;
  members: SyncedMember[];
  manager: string | null;
  totalUsedText: string | null;
  pendingInvites: SyncedMember[];
}

async function syncFamily(
  cookies: Cookie[],
): Promise<SyncFamilyResult> {
  const emptyResult: SyncFamilyResult = {
    expired: true, members: [], manager: null,
    totalUsedText: null, pendingInvites: [],
  };
  const resp = await httpReq("https://myaccount.google.com/family/details", { cookies });
  if (
    resp.finalUrl.includes("accounts.google.com/ServiceLogin") ||
    resp.finalUrl.includes("signin")
  ) {
    return emptyResult;
  }
  if (resp.status !== 200) return emptyResult;

  const html = resp.body;
  const { atToken, fSid } = extractAtTokenAndSid(html);

  const members: SyncedMember[] = [];
  let manager: string | null = null;

  const afRegex = /AF_initDataCallback\(\{[^}]*key:\s*'[^']*'[^}]*data:([\s\S]*?)\}\);/g;
  let afMatch;
  while ((afMatch = afRegex.exec(html)) !== null) {
    try {
      const data = JSON.parse(afMatch[1].trim());
      findMembers(data, members, (mgr) => {
        if (!manager) manager = mgr;
      });
    } catch { }
  }

  if (members.length === 0 && atToken) {
    const reqid = Math.floor(Math.random() * 900000) + 100000;
    const batchUrl = `https://myaccount.google.com/_/AccountSettingsUi/data/batchexecute?rpcids=V2esPe&hl=en&_reqid=${reqid}&rt=c`;
    const postBody = `f.req=${encodeURIComponent('[[[\"V2esPe\",\"[null,null,1,null,1,1]\",null,\"1\"]]]')}&at=${encodeURIComponent(atToken)}&`;
    const batchResp = await httpReq(batchUrl, {
      method: "POST",
      cookies,
      headers: {
        "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8",
        Referer: "https://myaccount.google.com/family/details",
        "X-Same-Domain": "1",
        Origin: "https://myaccount.google.com",
      },
      body: postBody,
    });
    if (batchResp.status === 200 && batchResp.body) {
      const lines = batchResp.body.split("\n");
      for (let i = 0; i < lines.length; i++) {
        if (/^\d+$/.test(lines[i].trim()) && lines[i + 1]) {
          try {
            const envelope = JSON.parse(lines[i + 1]);
            for (const entry of envelope) {
              if (
                Array.isArray(entry) &&
                entry[0] === "wrb.fr" &&
                entry[1] === "V2esPe" &&
                entry[2]
              ) {
                const rpcData = JSON.parse(entry[2]);
                try {
                  const memberList = rpcData[1][0][1];
                  if (Array.isArray(memberList)) {
                    for (const entry of memberList) {
                      if (!Array.isArray(entry) || entry.length < 2) continue;
                      const info = entry[0];
                      if (!Array.isArray(info) || info.length < 6) continue;
                      const name = info[0];
                      const email = info[5];
                      if (!email || !String(email).includes("@")) continue;
                      const role = members.length === 0 ? "FAMILY MANAGER" : "Member";
                      const isInvited = info[1] === null;
                      const status =
                        role === "FAMILY MANAGER"
                          ? ("active" as const)
                          : isInvited
                            ? ("invited" as const)
                            : ("active" as const);
                      let inviteId: string | undefined;
                      if (isInvited && Array.isArray(entry[9]) && entry[9][0]) {
                        inviteId = String(entry[9][0]);
                      }
                      const googleUserId = info[1] ? String(info[1]) : undefined;
                      if (members.length === 0) manager = `${name} (${email})`;
                      if (!members.find((m) => m.email === email)) {
                        members.push({
                          name,
                          email,
                          role,
                          status,
                          inviteId,
                          googleUserId,
                        });
                      }
                    }
                  }
                } catch {
                  findMembers(rpcData, members, (mgr) => {
                    if (!manager) manager = mgr;
                  });
                }
              }
            }
          } catch { }
          i++;
        }
      }
    }
  }


  let totalUsedText: string | null = null;
  const pendingInvites: SyncedMember[] = [];
  if (atToken) {
    try {
      const reqid = Math.floor(Math.random() * 900000) + 100000;
      const batchUrl = `https://myaccount.google.com/_/AccountSettingsUi/data/batchexecute?rpcids=t5583&source-path=%2Ffamily%2Fdetails&f.sid=${fSid}&hl=en&soc-app=1&soc-platform=1&soc-device=1&_reqid=${reqid}&rt=c`;
      const t5583Inner = "[[12],null,null,[null,null,null,null,null,null,null,null]]";
      const t5583Req = JSON.stringify([[["t5583", t5583Inner, null, "1"]]]);
      const t5583Body = `f.req=${encodeURIComponent(t5583Req)}&at=${encodeURIComponent(atToken)}&`;
      const t5583Resp = await httpReq(batchUrl, {
        method: "POST",
        cookies,
        headers: {
          "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8",
          Referer: "https://myaccount.google.com/family/details",
          "X-Same-Domain": "1",
          Origin: "https://myaccount.google.com",
        },
        body: t5583Body,
      });
      if (t5583Resp.status === 200) {
        totalUsedText = extractUsageText(t5583Resp.body);
        const t5583Members = parseMembersFromT5583(t5583Resp.body);
        for (const t5m of t5583Members) {
          if (t5m.status === "invited") {
            pendingInvites.push({
              name: t5m.name,
              email: t5m.email,
              role: t5m.role,
              status: "invited",
              inviteId: t5m.inviteId,
            });
          } else if (t5m.googleUserId) {
            const existing = members.find((m) => m.googleUserId === t5m.googleUserId);
            if (existing) {
              existing.role = t5m.role;
              if (!existing.name && t5m.name) existing.name = t5m.name;
            }
          }
        }
        if (!manager && t5583Members.length > 0) {
          const mgr = t5583Members.find((m) => m.role === "FAMILY MANAGER");
          if (mgr) {
            const mgrMember = members.find((m) => m.googleUserId === mgr.googleUserId);
            if (mgrMember) manager = `${mgrMember.name} (${mgrMember.email})`;
          }
        }
      }
    } catch (e) {
      apiError("syncFamily", e, { context: "t5583 storage+invites" });
    }
  }

  return { expired: false, members, manager, totalUsedText, pendingInvites };
}

async function createFamilyViaHttp(
  cookies: Cookie[],
): Promise<{ success: boolean; error: string }> {
  const batchHeaders = {
    "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8",
    Referer: "https://myaccount.google.com/family/create",
    "X-Same-Domain": "1",
    Origin: "https://myaccount.google.com",
  };

  const pageResp = await httpReq("https://myaccount.google.com/family/create", {
    cookies,
  });
  if (
    pageResp.finalUrl.includes("accounts.google.com/ServiceLogin") ||
    pageResp.finalUrl.includes("signin")
  ) {
    return { success: false, error: "Session expired" };
  }
  if (pageResp.status !== 200)
    return { success: false, error: `HTTP ${pageResp.status}` };

  const html = pageResp.body;
  let atToken: string | null = null;
  let m: RegExpMatchArray | null;
  m = html.match(/"SNlM0e"\s*:\s*"([^"]+)"/);
  if (m) atToken = m[1];
  if (!atToken) {
    m = html.match(/'at'\s*:\s*'([^']+)'/);
    if (m) atToken = m[1];
  }
  if (!atToken) return { success: false, error: "Cannot find atToken" };

  const reqid1 = Math.floor(Math.random() * 900000) + 100000;
  const wffnobUrl = `https://myaccount.google.com/_/AccountSettingsUi/data/batchexecute?rpcids=Wffnob&_reqid=${reqid1}&rt=c`;
  const wffnobBody = `f.req=${encodeURIComponent('[[[\"Wffnob\",\"[[null,null,[\\\"v2\\\",18,null,[\\\"googleaccount\\\"]]]]\",null,\"generic\"]]]')}&at=${encodeURIComponent(atToken)}&`;

  const wffnobResp = await httpReq(wffnobUrl, {
    method: "POST",
    cookies,
    headers: batchHeaders,
    body: wffnobBody,
  });
  if (wffnobResp.status !== 200)
    return { success: false, error: `Wffnob RPC failed: HTTP ${wffnobResp.status}` };

  let creationToken: string | null = null;
  try {
    const lines = wffnobResp.body.split("\n");
    for (let i = 0; i < lines.length; i++) {
      if (/^\d+$/.test(lines[i].trim()) && lines[i + 1]) {
        const envelope = JSON.parse(lines[i + 1]);
        for (const entry of envelope) {
          if (
            Array.isArray(entry) &&
            entry[0] === "wrb.fr" &&
            entry[1] === "Wffnob" &&
            entry[2]
          ) {
            const rpcData = JSON.parse(entry[2]);
            if (Array.isArray(rpcData) && typeof rpcData[3] === "string") {
              creationToken = rpcData[3];
            }
          }
        }
        i++;
      }
    }
  } catch { }

  if (!creationToken)
    return { success: false, error: "Cannot get creation token from Wffnob" };

  const reqid2 = reqid1 + 1;
  const c5gchUrl = `https://myaccount.google.com/_/AccountSettingsUi/data/batchexecute?rpcids=c5gch&_reqid=${reqid2}&rt=c`;
  const c5gchParam = JSON.stringify([
    [null, null, ["v2", 18, null, ["googleaccount"]]],
    [creationToken],
    [null, []],
  ]);
  const c5gchBody = `f.req=${encodeURIComponent(`[[[\"c5gch\",${JSON.stringify(c5gchParam)},null,\"generic\"]]]`)}&at=${encodeURIComponent(atToken)}&`;

  const c5gchResp = await httpReq(c5gchUrl, {
    method: "POST",
    cookies,
    headers: {
      ...batchHeaders,
      Referer: "https://myaccount.google.com/family/createconfirmation",
    },
    body: c5gchBody,
  });

  if (c5gchResp.status !== 200)
    return { success: false, error: `c5gch RPC failed: HTTP ${c5gchResp.status}` };

  if (c5gchResp.body.includes("wrb.fr") && c5gchResp.body.includes("c5gch")) {
    return { success: true, error: "" };
  }

  return { success: false, error: "Unexpected response from c5gch" };
}

export async function createFamilyForAdmin(
  adminId: string,
  email: string,
  password: string,
  totpSecret = "",
): Promise<{ success: boolean; error: string }> {
  const session = await loadSession(adminId);
  let cookies = session.cookies;

  if (!cookies || cookies.length === 0) {
    if (!password) return { success: false, error: "Chưa có cookies, cần password" };
    const loginResult = await doLogin(email, password, totpSecret, true);
    cookies = loginResult.cookies;
    await saveSession(adminId, cookies, loginResult.bearerToken || undefined);
  }

  let result = await createFamilyViaHttp(cookies);

  if (!result.success && result.error === "Session expired") {
    if (!password) return { success: false, error: "Cookie expired, cần password" };
    const loginResult = await doLogin(email, password, totpSecret, true);
    cookies = loginResult.cookies;
    await saveSession(adminId, cookies, loginResult.bearerToken || undefined);
    result = await createFamilyViaHttp(cookies);
  }

  return result;
}

async function fetchPlanNameViaGI6Jdd(cookies: Cookie[]): Promise<string> {
  try {
    const pageResp = await httpReq("https://one.google.com/", { cookies });
    if (pageResp.status !== 200) return "";

    let atToken: string | null = null;
    let m: RegExpMatchArray | null;
    m = pageResp.body.match(/"SNlM0e"\s*:\s*"([^"]+)"/);
    if (m) atToken = m[1];
    if (!atToken) {
      m = pageResp.body.match(/'at'\s*:\s*'([^']+)'/);
      if (m) atToken = m[1];
    }
    if (!atToken) return "";

    let fSid = "";
    const sidMatch = pageResp.body.match(/"FdrFJe"\s*:\s*"(-?\d+)"/);
    if (sidMatch) fSid = sidMatch[1];

    const reqid = Math.floor(Math.random() * 900000) + 100000;
    const batchUrl = `https://one.google.com/_/SubscriptionsManagementUi/data/batchexecute?rpcids=GI6Jdd&source-path=%2F&f.sid=${fSid}&bl=boq_subscriptionsuiserver_20260226.06_p2&hl=vi&soc-app=727&soc-platform=1&soc-device=1&_reqid=${reqid}&rt=c`;
    const postBody = `f.req=${encodeURIComponent('[[["GI6Jdd","[]",null,"generic"]]]')}&at=${encodeURIComponent(atToken)}&`;

    const resp = await httpReq(batchUrl, {
      method: "POST",
      cookies,
      headers: {
        "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8",
        Referer: "https://one.google.com/",
        "X-Same-Domain": "1",
        Origin: "https://one.google.com",
      },
      body: postBody,
    });

    if (resp.status !== 200) return "";

    const lines = resp.body.split("\n");
    for (let i = 0; i < lines.length; i++) {
      if (/^\d+$/.test(lines[i].trim()) && lines[i + 1]) {
        try {
          const envelope = JSON.parse(lines[i + 1]);
          for (const entry of envelope) {
            if (
              Array.isArray(entry) &&
              entry[0] === "wrb.fr" &&
              entry[1] === "GI6Jdd" &&
              entry[2]
            ) {
              const rpcData = JSON.parse(entry[2]);
              if (Array.isArray(rpcData) && Array.isArray(rpcData[0])) {
                const info = rpcData[0];
                if (typeof info[5] === "string" && info[5].length > 0) {
                  return info[5];
                }
              }
            }
          }
        } catch { }
        i++;
      }
    }
  } catch (e) {
    apiError("fetchPlanName", e);
  }
  return "";
}

interface SubscriptionInfo {
  planName: string;
  planFullName: string;
  expiresAt: Date | null;
}

interface SubscriptionScanOptions {
  familyType?: string | null;
}

function parseVietnameseDate(text: string): Date | null {
  const m = text.match(/(\d{1,2})\s*thg\s*(\d{1,2}),?\s*(\d{4})/);
  if (!m) return null;
  const day = parseInt(m[1]);
  const month = parseInt(m[2]) - 1;
  const year = parseInt(m[3]);
  const d = new Date(year, month, day);
  return isNaN(d.getTime()) ? null : d;
}

function extractShortPlanName(fullName: string): string {
  const m = fullName.match(/\((\d+\s*[TGMK]B)\)/i);
  return m ? m[1] : "";
}

function parseLooseDate(text: string): Date | null {
  const vietnamese = parseVietnameseDate(text);
  if (vietnamese) return vietnamese;

  let m = text.match(/\b(\d{1,2})[/-](\d{1,2})[/-](\d{4})\b/);
  if (m) {
    const day = parseInt(m[1], 10);
    const month = parseInt(m[2], 10) - 1;
    const year = parseInt(m[3], 10);
    const d = new Date(year, month, day);
    return isNaN(d.getTime()) ? null : d;
  }

  m = text.match(
    /\b(Jan(?:uary)?|Feb(?:ruary)?|Mar(?:ch)?|Apr(?:il)?|May|Jun(?:e)?|Jul(?:y)?|Aug(?:ust)?|Sep(?:tember)?|Oct(?:ober)?|Nov(?:ember)?|Dec(?:ember)?)\s+(\d{1,2}),?\s+(\d{4})\b/i,
  );
  if (m) {
    const d = new Date(`${m[1]} ${m[2]}, ${m[3]}`);
    return isNaN(d.getTime()) ? null : d;
  }

  return null;
}

function parseYouTubeSubscriptionFromText(rawText: string): SubscriptionInfo {
  const empty: SubscriptionInfo = { planName: "", planFullName: "", expiresAt: null };
  const payload = parseWrbRpcPayload(rawText, "t5583");
  if (!Array.isArray(payload)) return empty;

  function walk(arr: unknown[]): SubscriptionInfo | null {
    for (const item of arr) {
      if (!Array.isArray(item)) continue;
      if (
        typeof item[0] === "string" &&
        /YouTube\s+(?:Premium|Music\s+Premium)/i.test(item[0]) &&
        Array.isArray(item[4])
      ) {
        try {
          const pairs = (item[4] as unknown[][][][][][])?.[0]?.[2]?.[0];
          if (Array.isArray(pairs) && pairs.length >= 2) {
            const appName = item[0] as string;
            const dateStr = String(Array.isArray(pairs[1]) ? pairs[1][0] ?? "" : "");
            return { planName: appName, planFullName: appName, expiresAt: parseLooseDate(dateStr) };
          }
        } catch {}
      }
      const nested = walk(item as unknown[]);
      if (nested) return nested;
    }
    return null;
  }

  return walk(payload as unknown[]) || empty;
}

async function fetchYouTubeMembershipInfo(cookies: Cookie[]): Promise<SubscriptionInfo> {
  const empty: SubscriptionInfo = { planName: "", planFullName: "", expiresAt: null };
  const urls = [
    "https://www.youtube.com/paid_memberships?persist_hl=1&hl=en",
    "https://www.youtube.com/purchases?persist_hl=1&hl=en",
  ];

  for (const url of urls) {
    try {
      const resp = await httpReq(url, {
        cookies,
        headers: {
          Referer: "https://www.youtube.com/",
        },
      });
      if (
        resp.status !== 200 ||
        resp.finalUrl.includes("accounts.google.com") ||
        resp.finalUrl.includes("signin")
      ) {
        continue;
      }
      const parsed = parseYouTubeSubscriptionFromText(resp.body);
      if (parsed.planName) return parsed;
    } catch (e) {
      apiError("fetchYouTubeMembershipInfo", e);
    }
  }

  return empty;
}

interface OneSettingsContext {
  atToken: string;
  fSid: string;
  shareToken: string | null;
}

interface GoogleOneSharingState {
  enabled: boolean | null;
  error?: string;
}

function parseAtTokenFromHtml(html: string): string | null {
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
  return atToken;
}

function parseFSidFromHtml(html: string): string {
  const sidMatch = html.match(/"FdrFJe"\s*:\s*"(-?\d+)"/);
  return sidMatch?.[1] || "";
}

function parseWrbRpcPayload<T = unknown>(body: string, rpcId: string): T | null {
  const clean = body.replace(/^\)\]\}'\n?/, "");
  const lines = clean.split("\n");
  for (const line of lines) {
    const trimmed = line.trim();
    if (!trimmed.startsWith("[")) continue;
    try {
      const envelope = JSON.parse(trimmed);
      for (const entry of envelope) {
        if (
          Array.isArray(entry) &&
          entry[0] === "wrb.fr" &&
          entry[1] === rpcId &&
          entry[2]
        ) {
          return JSON.parse(entry[2]) as T;
        }
      }
    } catch {
      continue;
    }
  }
  return null;
}

function parseShareEnabledFromGnhH7e(payload: unknown): boolean | null {
  if (!Array.isArray(payload) || !Array.isArray(payload[1])) return null;
  const state = payload[1] as unknown[];

  if (typeof state[1] === "boolean") return state[1];
  if (state[1] === null) return false;
  if (Array.isArray(state[4]) && typeof state[4][0] === "boolean") {
    return state[4][0] as boolean;
  }

  return null;
}

function extractShareTokenFromHtml(html: string): string | null {
  const nearRpc = html.match(/F4mztc[\s\S]{0,2000}?(AFWL[A-Za-z0-9_+\-=/]{40,})/);
  if (nearRpc?.[1]) return nearRpc[1];
  const generic = html.match(/(AFWL[A-Za-z0-9_+\-=/]{40,})/);
  return generic?.[1] || null;
}

async function getOneSettingsContext(
  cookies: Cookie[],
): Promise<OneSettingsContext | null> {
  try {
    const pageResp = await httpReq("https://one.google.com/settings", { cookies });
    if (
      pageResp.status !== 200 ||
      pageResp.finalUrl.includes("accounts.google.com") ||
      pageResp.finalUrl.includes("signin")
    ) {
      return null;
    }

    const atToken = parseAtTokenFromHtml(pageResp.body);
    if (!atToken) return null;

    return {
      atToken,
      fSid: parseFSidFromHtml(pageResp.body),
      shareToken: extractShareTokenFromHtml(pageResp.body),
    };
  } catch (e) {
    apiError("getOneSettingsContext", e);
    return null;
  }
}

async function fetchGnhH7ePayload(
  cookies: Cookie[],
  context: Pick<OneSettingsContext, "atToken" | "fSid">,
): Promise<unknown | null> {
  try {
    const reqid = Math.floor(Math.random() * 900000) + 100000;
    const batchUrl = `https://one.google.com/_/SubscriptionsManagementUi/data/batchexecute?rpcids=GnhH7e&source-path=%2Fsettings&f.sid=${context.fSid}&hl=en&soc-app=727&soc-platform=1&soc-device=1&_reqid=${reqid}&rt=c`;
    const gnhReq = JSON.stringify([[["GnhH7e", "[]", null, "generic"]]]);
    const postBody = `f.req=${encodeURIComponent(gnhReq)}&at=${encodeURIComponent(context.atToken)}&`;
    const resp = await httpReq(batchUrl, {
      method: "POST",
      cookies,
      headers: {
        "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8",
        Referer: "https://one.google.com/settings",
        "X-Same-Domain": "1",
        Origin: "https://one.google.com",
      },
      body: postBody,
    });

    if (resp.status !== 200) return null;
    return parseWrbRpcPayload(resp.body, "GnhH7e");
  } catch (e) {
    apiError("fetchGnhH7ePayload", e);
    return null;
  }
}

function buildF4mztcPayloadCandidates(shareToken: string, enable: boolean): unknown[] {
  if (enable) {
    return [
      [1, [[null, 1]], [null, [shareToken], 1]],
      [true, [[null, 1]], [null, [shareToken], 1]],
      [1, [[null, true]], [null, [shareToken], 1]],
    ];
  }

  return [
    [0, [[null, 1]], [null, [shareToken], 1]],
    [1, [[null, 0]], [null, [shareToken], 1]],
    [1, [[null, 1]], [null, [shareToken], 0]],
    [0, [[null, 0]], [null, [shareToken], 0]],
    [false, [[null, 1]], [null, [shareToken], 1]],
    [1, [[null, false]], [null, [shareToken], 1]],
  ];
}

async function postF4mztcPayload(
  cookies: Cookie[],
  context: OneSettingsContext,
  payload: unknown,
): Promise<boolean> {
  try {
    const reqid = Math.floor(Math.random() * 900000) + 100000;
    const batchUrl = `https://one.google.com/_/SubscriptionsManagementUi/data/batchexecute?rpcids=F4mztc&source-path=%2Fsettings&f.sid=${context.fSid}&hl=en&soc-app=727&soc-platform=1&soc-device=1&_reqid=${reqid}&rt=c`;
    const f4Req = JSON.stringify([
      [["F4mztc", JSON.stringify(payload), null, "generic"]],
    ]);
    const postBody = `f.req=${encodeURIComponent(f4Req)}&at=${encodeURIComponent(context.atToken)}&`;

    const resp = await httpReq(batchUrl, {
      method: "POST",
      cookies,
      headers: {
        "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8",
        Referer: "https://one.google.com/settings",
        "X-Same-Domain": "1",
        Origin: "https://one.google.com",
      },
      body: postBody,
    });

    return resp.status === 200 && resp.body.includes("wrb.fr");
  } catch (e) {
    apiError("postF4mztcPayload", e);
    return false;
  }
}

async function setGoogleOneSharingByF4mztc(
  cookies: Cookie[],
  context: OneSettingsContext,
  enable: boolean,
): Promise<boolean> {
  if (!context.shareToken) return false;
  const payloadCandidates = buildF4mztcPayloadCandidates(context.shareToken, enable);

  for (const payload of payloadCandidates) {
    const posted = await postF4mztcPayload(cookies, context, payload);
    if (!posted) continue;

    await sleep(200);
    const state = await getGoogleOneSharingState(cookies);
    if (state.enabled === enable) return true;
  }

  return false;
}

export async function getGoogleOneSharingState(
  cookies: Cookie[],
): Promise<GoogleOneSharingState> {
  const context = await getOneSettingsContext(cookies);
  if (!context) {
    return {
      enabled: null,
      error: "Cannot load one.google.com/settings (session may be expired)",
    };
  }

  const payload = await fetchGnhH7ePayload(cookies, context);
  if (!payload) {
    return { enabled: null, error: "Cannot fetch GnhH7e share-state payload" };
  }

  return { enabled: parseShareEnabledFromGnhH7e(payload) };
}

export async function ensureGoogleOneSharingEnabled(
  cookies: Cookie[],
): Promise<{ enabled: boolean; changed: boolean; error?: string }> {
  const initial = await getGoogleOneSharingState(cookies);
  if (initial.enabled === true) return { enabled: true, changed: false };
  if (initial.enabled === null) {
    return {
      enabled: false,
      changed: false,
      error: initial.error || "Cannot determine share state",
    };
  }

  const context = await getOneSettingsContext(cookies);
  if (!context) {
    return {
      enabled: false,
      changed: false,
      error: "Cannot load settings context for enable action",
    };
  }
  if (!context.shareToken) {
    return {
      enabled: false,
      changed: false,
      error: "Missing share token (F4mztc token) from settings page",
    };
  }

  const updated = await setGoogleOneSharingByF4mztc(cookies, context, true);
  if (!updated) {
    return {
      enabled: false,
      changed: false,
      error: "Enable-share failed via F4mztc payload variants",
    };
  }

  const after = await getGoogleOneSharingState(cookies);
  if (after.enabled === true) return { enabled: true, changed: true };

  return {
    enabled: false,
    changed: false,
    error: after.error || "Enable-share sent but state is still not enabled",
  };
}

export async function ensureGoogleOneSharingDisabled(
  cookies: Cookie[],
): Promise<{ enabled: boolean; changed: boolean; error?: string }> {
  const initial = await getGoogleOneSharingState(cookies);
  if (initial.enabled === false) return { enabled: false, changed: false };
  if (initial.enabled === null) {
    return {
      enabled: false,
      changed: false,
      error: initial.error || "Cannot determine share state",
    };
  }

  const context = await getOneSettingsContext(cookies);
  if (!context) {
    return {
      enabled: true,
      changed: false,
      error: "Cannot load settings context for disable action",
    };
  }
  if (!context.shareToken) {
    return {
      enabled: true,
      changed: false,
      error: "Missing share token (F4mztc token) from settings page",
    };
  }

  const updated = await setGoogleOneSharingByF4mztc(cookies, context, false);
  if (!updated) {
    return {
      enabled: true,
      changed: false,
      error: "Disable-share failed via F4mztc payload variants",
    };
  }

  const after = await getGoogleOneSharingState(cookies);
  if (after.enabled === false) return { enabled: false, changed: true };

  return {
    enabled: true,
    changed: false,
    error: after.error || "Disable-share sent but state is still enabled",
  };
}

export async function scanSubscriptionInfo(
  cookies: Cookie[],
  options: SubscriptionScanOptions = {},
): Promise<SubscriptionInfo> {
  const empty: SubscriptionInfo = { planName: "", planFullName: "", expiresAt: null };
  const isYouTube = (options.familyType || "").toLowerCase() === "youtube";
  try {
    const pageResp = await httpReq(
      "https://myaccount.google.com/payments-and-subscriptions",
      { cookies },
    );
    if (
      pageResp.status !== 200 ||
      pageResp.finalUrl.includes("accounts.google.com") ||
      pageResp.finalUrl.includes("signin")
    ) {
      if (isYouTube) return fetchYouTubeMembershipInfo(cookies);
      const fb = await fetchPlanNameViaGI6Jdd(cookies);
      return { planName: fb, planFullName: "", expiresAt: null };
    }

    let atToken: string | null = null;
    const atMatch = pageResp.body.match(/"SNlM0e"\s*:\s*"([^"]+)"/);
    if (atMatch) atToken = atMatch[1];
    if (!atToken) {
      const m2 = pageResp.body.match(/'at'\s*:\s*'([^']+)'/);
      if (m2) atToken = m2[1];
    }
    if (!atToken) {
      if (isYouTube) return fetchYouTubeMembershipInfo(cookies);
      const fb = await fetchPlanNameViaGI6Jdd(cookies);
      return { planName: fb, planFullName: "", expiresAt: null };
    }

    let fSid = "";
    const sidMatch = pageResp.body.match(/"FdrFJe"\s*:\s*"(-?\d+)"/);
    if (sidMatch) fSid = sidMatch[1];

    const reqid = Math.floor(Math.random() * 900000) + 100000;
    const batchUrl = `https://myaccount.google.com/_/AccountSettingsUi/data/batchexecute?rpcids=t5583&source-path=%2Fpayments-and-subscriptions&f.sid=${fSid}&hl=vi&soc-app=1&soc-platform=1&soc-device=1&_reqid=${reqid}&rt=c`;
    const t5583Inner = "[[6],null,null,[null,null,null,null,null,null,null,null]]";
    const t5583Req = JSON.stringify([[["t5583", t5583Inner, null, "1"]]]);
    const postBody = `f.req=${encodeURIComponent(t5583Req)}&at=${encodeURIComponent(atToken)}&`;

    const resp = await httpReq(batchUrl, {
      method: "POST",
      cookies,
      headers: {
        "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8",
        Referer: "https://myaccount.google.com/payments-and-subscriptions",
        "X-Same-Domain": "1",
        Origin: "https://myaccount.google.com",
      },
      body: postBody,
    });

    if (resp.status !== 200) {
      if (isYouTube) return fetchYouTubeMembershipInfo(cookies);
      const fb = await fetchPlanNameViaGI6Jdd(cookies);
      return { planName: fb, planFullName: "", expiresAt: null };
    }

    const responseStr = resp.body;
    if (isYouTube) {
      const parsedYouTube = parseYouTubeSubscriptionFromText(responseStr);
      if (parsedYouTube.planName) return parsedYouTube;

      const directYouTube = await fetchYouTubeMembershipInfo(cookies);
      if (directYouTube.planName) return directYouTube;

      return empty;
    }

    const result: SubscriptionInfo = { planName: "", planFullName: "", expiresAt: null };

    const fullNameMatch = responseStr.match(
      /Google\s+(?:One|AI)\s+\w+\s*\(\d+\s*[TGMK]B\)/i,
    );
    if (fullNameMatch) {
      result.planFullName = fullNameMatch[0];
      result.planName = extractShortPlanName(fullNameMatch[0]) || fullNameMatch[0];
    }
    if (!result.planName) {
      const tbMatch = responseStr.match(/(\d+)\s*(TB|GB)/);
      if (tbMatch) result.planName = `${tbMatch[1]} ${tbMatch[2]}`;
    }

    const dateMatch = responseStr.match(
      /(?:Hủy|Gia hạn|Hết hạn)\s+vào:?\s+(\d{1,2}\s*thg\s*\d{1,2},?\s*\d{4})/,
    );
    if (dateMatch) {
      result.expiresAt = parseVietnameseDate(dateMatch[1]);
    }

    if (!result.planName) {
      result.planName = await fetchPlanNameViaGI6Jdd(cookies);
    }

    return result;
  } catch (e) {
    apiError("scanSubscriptionInfo", e);
    try {
      if ((options.familyType || "").toLowerCase() === "youtube") {
        return fetchYouTubeMembershipInfo(cookies);
      }
      const fb = await fetchPlanNameViaGI6Jdd(cookies);
      return { planName: fb, planFullName: "", expiresAt: null };
    } catch { }
  }
  return empty;
}

async function fetchCreditsWithToken(token: string): Promise<number | null> {
  try {
    const resp = await fetch(
      `https://aisandbox-pa.googleapis.com/v1/credits?key=${process.env.GOOGLE_AI_API_KEY || ""}`,
      {
        headers: {
          Authorization: `Bearer ${token}`,
          Accept: "application/json",
          Referer: "https://labs.google/fx/vi/tools/flow",
          Origin: "https://labs.google",
        },
      },
    );
    if (resp.ok) {
      const data = await resp.json();
      if (typeof data.credits === "number") return data.credits;
    }
  } catch (e) {
    apiError("fetchCreditsWithToken", e);
  }
  return null;
}

async function doLogin(
  email: string,
  password: string,
  secret2fa: string,
  skipCreditFlow = false,
): Promise<{ cookies: Cookie[]; credits: number | null; bearerToken: string | null }> {
  const browser: Browser = await puppeteer.launch({
    headless: false,
    executablePath:
      process.env.CHROME_EXECUTABLE_PATH ||
      "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe",
    args: [
      "--no-sandbox",
      "--disable-setuid-sandbox",
      "--disable-blink-features=AutomationControlled",
      ...hiddenChromeWindowArgs(1366, 768),
    ],
    defaultViewport: null,
    ignoreDefaultArgs: ["--enable-automation"],
  });
  const pages = await browser.pages();
  const page: Page = pages[0] || (await browser.newPage());
  await page.evaluateOnNewDocument(() => {
    Object.defineProperty(navigator, "webdriver", { get: () => undefined });
  });

  try {
    await page.goto(
      "https://accounts.google.com/v3/signin/identifier?continue=https%3A%2F%2Fmyaccount.google.com%2F&service=accountsettings&flowName=GlifWebSignIn&flowEntry=ServiceLogin",
      { waitUntil: "networkidle2", timeout: 30000 },
    );

    const emailInput = await waitAndGet(page, ['input[type="email"]', "#identifierId"]);
    if (!emailInput) throw new Error("Email input not found");
    await emailInput.click({ clickCount: 3 });
    await emailInput.type(email, { delay: 10 + Math.random() * 15 });
    await sleep(300);
    let nextBtn = await page.$("#identifierNext");
    if (nextBtn) await nextBtn.click();
    else await page.keyboard.press("Enter");

    await sleep(1500);
    const pwdInput = await waitAndGet(page, ['input[type="password"]'], 20000);
    if (!pwdInput) {
      await throwIfDeadGooglePage(page, "password_input");
      throw new Error("Password input not found");
    }
    await sleep(300);
    await pwdInput.click();
    await sleep(100);
    await pwdInput.type(password, { delay: 10 + Math.random() * 15 });
    await sleep(300);
    nextBtn = await page.$("#passwordNext");
    if (nextBtn) await nextBtn.click();
    else await page.keyboard.press("Enter");
    await sleep(2000);
    await throwIfDeadGooglePage(page, "password_step");

    let afterPwdText = normalizeChallengeText(
      await page.evaluate(() => document.body.innerText.substring(0, 1000)),
    );
    if (isPasswordErrorText(afterPwdText)) throw new Error("Sai mật khẩu!");
    if (await hasVisiblePasswordInput(page)) {
      await sleep(3000);
      afterPwdText = normalizeChallengeText(
        await page.evaluate(() => document.body.innerText.substring(0, 1000)),
      );
      if (isPasswordErrorText(afterPwdText)) throw new Error("Sai mật khẩu!");
      if (await hasVisiblePasswordInput(page)) {
        throw new Error("Password step did not complete");
      }
    }

    if (new URL(page.url()).hostname === "myaccount.google.com") {
      const cdp = await page.target().createCDPSession();
      const { cookies } = await cdp.send("Network.getAllCookies");
      await browser.close();
      return { cookies: cookies as Cookie[], credits: null, bearerToken: null };
    }

    await sleep(1000);
    await moveToTotpChallenge(page);

    let totpInput = await waitAndGet(page, TOTP_INPUT_SELECTORS, 15000);
    if (!totpInput) {
      totpInput = await findFallbackTotpInput(page);
    }
    if (!totpInput) {
      await throwIfDeadGooglePage(page, "totp_input");
      throw new Error("TOTP input not found");
    }

    const totpCode = await get2FACode(secret2fa);
    if (!totpCode) throw new Error("Không lấy được TOTP");

    await totpInput.click();
    await sleep(100);
    await totpInput.type(totpCode, { delay: 15 });
    await sleep(300);
    nextBtn = (await page.$("#totpNext")) || (await page.$("#idvPreregisteredPhoneNext"));
    if (nextBtn) await nextBtn.click();
    else await page.keyboard.press("Enter");

    try {
      await page.waitForNavigation({ waitUntil: "networkidle2", timeout: 15000 });
    } catch { }
    await sleep(1000);

    let needRetry = false;
    try {
      const curUrl = page.url();
      if (curUrl.includes("challenge") || curUrl.includes("signin")) {
        const bodyText = await page.evaluate(() =>
          document.body.innerText.substring(0, 500),
        );
        if (
          bodyText.includes("Wrong code") ||
          bodyText.includes("incorrect") ||
          bodyText.includes("Try again")
        )
          needRetry = true;
      }
    } catch { }

    if (needRetry) {
      throw new Error("Đăng nhập thất bại — OTP sai");
    }

    let fetchedCredits: number | null = null;
    let capturedToken: string | null = null;
    if (!skipCreditFlow) {
      try {
        page.on("request", (req) => {
          const auth = req.headers()["authorization"] || "";
          if (auth.startsWith("Bearer ya29.")) {
            capturedToken = auth.replace("Bearer ", "");
          }
        });
        page.on("response", async (resp) => {
          try {
            if (
              resp.url().includes("aisandbox-pa.googleapis.com/v1/credits") &&
              resp.status() === 200
            ) {
              const data = await resp.json();
              if (typeof data.credits === "number") fetchedCredits = data.credits;
            }
          } catch { }
        });

        await page.goto("https://labs.google/fx/api/auth/signin/google", {
          waitUntil: "networkidle2",
          timeout: 30000,
        });
        await page.goto("https://labs.google/fx/vi/tools/flow", {
          waitUntil: "networkidle2",
          timeout: 30000,
        });
        await sleep(2000);

        const createBtnHandle = await page.evaluateHandle(() => {
          for (const el of document.querySelectorAll("a, button, [role='button']")) {
            const text = (el as HTMLElement).innerText || "";
            if (text.includes("Create") && text.includes("Flow")) return el;
          }
          for (const el of document.querySelectorAll(
            "a[href*='create'], a[href*='flow']",
          )) {
            return el;
          }
          return null;
        });
        const createBtn = createBtnHandle.asElement() as ElementHandle<Element> | null;
        if (createBtn) {
          await createBtn.click();
          try {
            await page.waitForNavigation({ waitUntil: "networkidle2", timeout: 20000 });
          } catch { }
        } else {
          await createBtnHandle.dispose();
        }
        await sleep(3000);
      } catch (e) {
        apiError("doLogin", e, { context: "credits fetch" });
      }
    }

    const cdpFinal = await page.target().createCDPSession();
    const { cookies } = await cdpFinal.send("Network.getAllCookies");
    await browser.close();
    return {
      cookies: cookies as Cookie[],
      credits: fetchedCredits ?? 0,
      bearerToken: capturedToken,
    };
  } catch (err) {
    apiError("doLogin", err, { context: "login failed" });
    await browser.close();
    throw err;
  }
}

export async function syncFamilyGroup(
  adminId: string,
  email: string,
  password: string,
  totpSecret = "",
  skipCreditFlow = false,
  familyType = "ultra",
): Promise<SyncResult> {
  const startTime = Date.now();
  try {
    const session = await loadSession(adminId);
    let cookies = session.cookies;

    let credits: number | null = null;
    if (!skipCreditFlow && session.bearerToken) {
      credits = await fetchCreditsWithToken(session.bearerToken);
    }

    if (!cookies || cookies.length === 0) {
      if (!password) {
        return {
          status: "failed",
          members: [],
          manager: null,
          planName: "",
          planExpiresAt: null,
          credits: null,
          error: "Chưa có cookies, cần password để login",
          duration: Date.now() - startTime,
        };
      }
      const loginResult = await doLogin(email, password, totpSecret, skipCreditFlow);
      cookies = loginResult.cookies;
      if (credits === null) credits = loginResult.credits;
      await saveSession(adminId, cookies, loginResult.bearerToken || undefined);
    }

    let result = await syncFamily(cookies);

    if (result.expired) {
      if (!password) {
        return {
          status: "failed",
          members: [],
          manager: null,
          planName: "",
          planExpiresAt: null,
          credits,
          error: "Cookie expired, cần password để login lại",
          duration: Date.now() - startTime,
        };
      }
      const loginResult = await doLogin(email, password, totpSecret, skipCreditFlow);
      cookies = loginResult.cookies;
      if (credits === null) credits = loginResult.credits;
      await saveSession(adminId, cookies, loginResult.bearerToken || undefined);
      result = await syncFamily(cookies);
    }

    const subInfo = await scanSubscriptionInfo(cookies, { familyType });
    const planName = subInfo.planName;
    const planExpiresAt = subInfo.expiresAt;
    let usedStorageMB: number | null = null;
    try {
      const usedBytes = result.totalUsedText
        ? parseSizeTextToBytes(result.totalUsedText)
        : null;
      if (typeof usedBytes === "number" && Number.isFinite(usedBytes)) {
        usedStorageMB = Math.max(0, Math.round(usedBytes / (1024 * 1024)));
      }
    } catch (e) {
      apiError("syncFamilyGroup", e, { context: "compute usedStorageMB" });
    }

    if (result.pendingInvites.length > 0) {
      const existingEmails = new Set(result.members.map((m) => m.email));
      for (const invite of result.pendingInvites) {
        if (!existingEmails.has(invite.email)) {
          result.members.push(invite);
        }
      }
    }

    return {
      status: result.members.length > 0 ? "success" : "partial",
      members: result.members,
      manager: result.manager,
      planName,
      planExpiresAt,
      credits,
      usedStorageMB,
      error: result.members.length === 0 ? "Không tìm thấy thành viên" : "",
      duration: Date.now() - startTime,
    };
  } catch (err) {
    apiError("syncFamilyGroup", err);
    const message = err instanceof Error ? err.message : String(err);
    return {
      status: "failed",
      members: [],
      manager: null,
      planName: "",
      planExpiresAt: null,
      credits: null,
      error: isDeadGoogleErrorMessage(message) ? message : "Sync failed",
      duration: Date.now() - startTime,
    };
  }
}

export interface GoogleAuthData {
  atToken: string;
  authuser: string;
  cookies: Cookie[];
}

export async function getGoogleAuthOrRefresh(
  adminId: string,
  adminEmail: string,
  encryptedPassword: string,
  encryptedTotp: string,
  options: { allowRelogin?: boolean } = {},
): Promise<GoogleAuthData | null> {
  const allowRelogin = options.allowRelogin !== false;
  const gs = await prisma.googleSession.findUnique({ where: { adminId } });
  if (!gs?.cookies) {
    if (!allowRelogin) return null;
    return await refreshAndRetry(adminId, adminEmail, encryptedPassword, encryptedTotp);
  }

  const decrypted = safeDecrypt(gs.cookies);
  const authData = await getAtTokenAndAuthuser(decrypted);
  if (authData) return { ...authData, cookies: JSON.parse(decrypted) };

  if (!allowRelogin) return null;
  return await refreshAndRetry(adminId, adminEmail, encryptedPassword, encryptedTotp);
}

async function refreshAndRetry(
  adminId: string,
  adminEmail: string,
  encryptedPassword: string,
  encryptedTotp: string,
): Promise<GoogleAuthData | null> {
  try {
    const adminAccount = await prisma.adminAccount.findUnique({
      where: { id: adminId },
      select: { familyType: true },
    });
    await syncFamilyGroup(
      adminId,
      adminEmail,
      safeDecrypt(encryptedPassword),
      safeDecrypt(encryptedTotp),
      true,
      adminAccount?.familyType || "ultra",
    );

    const gs = await prisma.googleSession.findUnique({ where: { adminId } });
    if (!gs?.cookies) return null;

    const decrypted = safeDecrypt(gs.cookies);
    const authData = await getAtTokenAndAuthuser(decrypted);
    if (!authData) return null;

    return { ...authData, cookies: JSON.parse(decrypted) };
  } catch (e) {
    apiError("refreshAndRetry", e);
    return null;
  }
}
