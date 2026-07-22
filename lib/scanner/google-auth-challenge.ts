import { sleep } from "@/lib/scanner/scanner-utils";
import type { ElementHandle, Page } from "puppeteer";

export const TOTP_INPUT_SELECTORS = [
  "#totpPin",
  'input[name="totpPin"]',
  'input[name="code"]',
  'input[name="Pin"]',
  'input[autocomplete="one-time-code"]',
  'input[inputmode="numeric"]',
  'input[type="tel"]',
  'input[aria-label*="code"]',
  'input[aria-label*="verification"]',
];

function normalizeChallengeText(text: string): string {
  return text
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .toLowerCase()
    .replace(/\s+/g, " ")
    .trim();
}

async function getVisibleBodyText(page: Page): Promise<string> {
  try {
    return await page.evaluate(() => document.body.innerText || "");
  } catch {
    return "";
  }
}

export async function hasVisibleTotpInput(page: Page): Promise<boolean> {
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
            style.display !== "none" &&
            input.type !== "hidden" &&
            input.type !== "checkbox"
          ) {
            return true;
          }
        }
      }
      return false;
    }, TOTP_INPUT_SELECTORS);
  } catch {
    return false;
  }
}

function hasTotpChallengeText(text: string): boolean {
  return (
    text.includes("authenticator") ||
    text.includes("totp") ||
    text.includes("verification code") ||
    text.includes("enter the code") ||
    text.includes("6-digit") ||
    text.includes("ma xac minh") ||
    (text.includes("ung dung") &&
      (text.includes("xac thuc") || text.includes("ma xac minh")))
  );
}

export async function findFallbackTotpInput(
  page: Page,
): Promise<ElementHandle<HTMLInputElement> | null> {
  const text = normalizeChallengeText(await getVisibleBodyText(page));
  const url = page.url();
  const hasTotpContext =
    url.includes("challenge/totp") || hasTotpChallengeText(text);
  if (!hasTotpContext) return null;

  const fallbackHandle = await page.evaluateHandle(() => {
    for (const inp of document.querySelectorAll("input")) {
      const input = inp as HTMLInputElement;
      const rect = input.getBoundingClientRect();
      const style = getComputedStyle(input);
      const type = (input.getAttribute("type") || "text").toLowerCase();
      const name = (input.getAttribute("name") || "").toLowerCase();
      const autocomplete = (input.getAttribute("autocomplete") || "").toLowerCase();
      if (
        rect.width > 30 &&
        rect.height > 10 &&
        style.visibility !== "hidden" &&
        style.display !== "none" &&
        !["hidden", "checkbox", "password", "email", "submit", "button", "radio"].includes(
          type,
        ) &&
        !["passwd", "password", "identifier"].includes(name) &&
        autocomplete !== "current-password"
      ) {
        return input;
      }
    }
    return null;
  });
  const fallbackInput = fallbackHandle.asElement();
  if (fallbackInput) return fallbackInput as ElementHandle<HTMLInputElement>;
  await fallbackHandle.dispose();
  return null;
}

async function clickTextOption(page: Page, needles: string[]): Promise<boolean> {
  return page.evaluate((rawNeedles) => {
    const normalize = (value: string) =>
      value
        .normalize("NFD")
        .replace(/[\u0300-\u036f]/g, "")
        .toLowerCase()
        .replace(/\s+/g, " ")
        .trim();
    const normalizedNeedles = rawNeedles.map(normalize);
    const clickableSelector = "button, [role='button'], a, [tabindex]";
    const candidates: Array<{ target: HTMLElement; score: number }> = [];

    for (const el of document.querySelectorAll(
      "button, [role='button'], a, [tabindex], span, div",
    )) {
      const element = el as HTMLElement;
      const text = normalize(element.innerText || element.textContent || "");
      if (!text) continue;
      const matchedNeedle = normalizedNeedles.find((needle) =>
        text.includes(needle),
      );
      if (!matchedNeedle) continue;

      const target = (element.closest(clickableSelector) || element) as HTMLElement;
      const rect = target.getBoundingClientRect();
      const style = getComputedStyle(target);
      if (
        rect.width <= 0 ||
        rect.height <= 0 ||
        style.visibility === "hidden" ||
        style.display === "none"
      ) {
        continue;
      }

      candidates.push({
        target,
        score:
          (text === matchedNeedle ? 0 : 1000) +
          (target.matches(clickableSelector) ? 0 : 500) +
          text.length,
      });
    }

    candidates.sort((a, b) => a.score - b.score);
    const best = candidates[0];
    if (!best) return false;
    best.target.scrollIntoView({ block: "center", inline: "center" });
    best.target.click();
    best.target.dispatchEvent(
      new MouseEvent("click", { bubbles: true, cancelable: true, view: window }),
    );
    return true;
  }, needles);
}

async function clickTryAnotherWay(page: Page): Promise<boolean> {
  return clickTextOption(page, [
    "try another way",
    "try a different way",
    "thu cach khac",
    "chon cach khac",
    "tuy chon khac",
    "cach dang nhap khac",
  ]);
}

async function clickTotpChallengeOption(page: Page): Promise<boolean> {
  const challengeTypePoint = await page.evaluate(() => {
    const option = document.querySelector('[data-challengetype="6"]');
    if (!option) return null;
    const target = (option.closest(
      "button, [role='button'], a, li, [tabindex], [jsaction]",
    ) || option) as HTMLElement;
    target.scrollIntoView({ block: "center", inline: "center" });
    const rect = target.getBoundingClientRect();
    const style = getComputedStyle(target);
    if (
      rect.width <= 0 ||
      rect.height <= 0 ||
      style.visibility === "hidden" ||
      style.display === "none"
    ) {
      return null;
    }
    return { x: rect.left + rect.width / 2, y: rect.top + rect.height / 2 };
  });
  if (challengeTypePoint) {
    await page.mouse.click(challengeTypePoint.x, challengeTypePoint.y);
    return true;
  }

  const textPoint = await page.evaluate(() => {
    const normalize = (value: string) =>
      value
        .normalize("NFD")
        .replace(/[\u0300-\u036f]/g, "")
        .toLowerCase()
        .replace(/\s+/g, " ")
        .trim();
    const isTotpText = (text: string) =>
      text.includes("google authenticator") ||
      text.includes("authenticator app") ||
      text.includes("verify with authenticator") ||
      text.includes("verification code from the google authenticator") ||
      text.includes("totp") ||
      (text.includes("ung dung") &&
        (text.includes("xac thuc") || text.includes("ma xac minh")));
    const isOtherChallengeText = (text: string) =>
      text.includes("tap yes") ||
      text.includes("try another way") ||
      text.includes("phone or tablet") ||
      text.includes("thu cach khac") ||
      text.includes("chon cach khac");
    const isVisible = (element: HTMLElement) => {
      const rect = element.getBoundingClientRect();
      const style = getComputedStyle(element);
      return (
        rect.width > 0 &&
        rect.height > 0 &&
        style.visibility !== "hidden" &&
        style.display !== "none"
      );
    };
    const pickRowTarget = (element: HTMLElement): HTMLElement => {
      let best = element;
      let current: HTMLElement | null = element;
      for (let depth = 0; current && depth < 8; depth++) {
        if (current === document.body) break;
        if (!isVisible(current)) {
          current = current.parentElement;
          continue;
        }
        const text = normalize(current.innerText || current.textContent || "");
        const rect = current.getBoundingClientRect();
        const isOnlyTotpRow =
          isTotpText(text) &&
          !isOtherChallengeText(text) &&
          text.length <= 240 &&
          rect.width >= 160 &&
          rect.height >= 24 &&
          rect.height <= 180;
        if (isOnlyTotpRow) best = current;
        current = current.parentElement;
      }
      return best;
    };
    const candidates: Array<{ target: HTMLElement; score: number }> = [];

    for (const el of document.querySelectorAll(
      "button, [role='button'], a, li, div, span, strong, b",
    )) {
      const element = el as HTMLElement;
      if (!isVisible(element)) continue;
      const text = normalize(element.innerText || element.textContent || "");
      if (!isTotpText(text) || isOtherChallengeText(text)) continue;
      const target = pickRowTarget(element);
      const rect = target.getBoundingClientRect();
      candidates.push({
        target,
        score:
          text.length +
          (target.matches("button, [role='button'], a, li, [tabindex], [jsaction]")
            ? 0
            : 50) +
          (rect.width < 160 ? 500 : 0),
      });
    }

    candidates.sort((a, b) => a.score - b.score);
    const best = candidates[0];
    if (!best) return null;
    best.target.scrollIntoView({ block: "center", inline: "center" });
    const rect = best.target.getBoundingClientRect();
    return { x: rect.left + rect.width / 2, y: rect.top + rect.height / 2 };
  });
  if (textPoint) {
    await page.mouse.click(textPoint.x, textPoint.y);
    return true;
  }

  return false;
}

export async function moveToTotpChallenge(page: Page): Promise<void> {
  const deadline = Date.now() + 20000;
  while (Date.now() < deadline) {
    const url = page.url();
    if (url.includes("challenge/totp")) return;
    try {
      if (new URL(url).hostname === "myaccount.google.com") return;
    } catch {
      // Continue through transitional browser URLs.
    }
    if (await hasVisibleTotpInput(page)) return;

    const text = normalizeChallengeText(await getVisibleBodyText(page));
    const hasTotpOption = hasTotpChallengeText(text);
    const isChallengeSelection =
      url.includes("challenge/selection") ||
      hasTotpOption ||
      text.includes("choose how you want to sign in") ||
      text.includes("chon cach") ||
      text.includes("cach dang nhap khac");
    if (isChallengeSelection && (await clickTotpChallengeOption(page))) {
      await sleep(1500);
      continue;
    }

    const isDevicePrompt =
      url.includes("challenge/dp") ||
      (!hasTotpOption &&
        (text.includes("try another way") ||
          text.includes("thu cach khac") ||
          text.includes("tuy chon khac")));
    if (isDevicePrompt && (await clickTryAnotherWay(page))) {
      await sleep(1500);
      continue;
    }

    await sleep(1000);
  }
}
