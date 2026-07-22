import { googleHttpReq as httpReq, type Cookie } from "@/lib/scanner/google-http";
import { scanSubscriptionInfo } from "@/lib/scanner/google-one";
import { safeDecrypt } from "@/lib/crypto";
import { sleep, get2FACode } from "@/lib/scanner/scanner-utils";
import { existsSync, mkdirSync } from "node:fs";
import path from "node:path";
import { type Page, type ElementHandle } from "puppeteer";
import {
  extractAtToken,
  extractFSid,
  isDetachedFrameError,
  isGoogleSignInUrl,
  isReauthUrl,
  normalizeLooseText,
  parseBatchResponse,
} from "@/lib/scanner/google-profile/shared";
import {
  type EvalContext,
  type WipeoutFetchResult,
  type WipeoutParams,
  extractPaymentsUserIndex,
  extractWipeoutParams,
  isWipeoutUrl,
  submitWipeoutViaFetch,
  verifyPaymentProfileClosed,
  waitForWipeoutContext,
} from "@/lib/scanner/google-profile/wipeout";
import {
  REAUTH_PASSWORD_NOT_FOUND_MESSAGE,
  TOTP_INPUT_SELECTORS,
  extractCookies,
  findFallbackTotpInput,
  findPasswordInputAcrossFrames,
  launchStealthBrowser,
  moveToTotpChallenge,
  puppeteerGoogleLogin,
  submitPasswordOnPage,
  waitAndGet,
  waitForReauthPage,
} from "@/lib/scanner/google-profile/auth-flow";

export interface PaymentProfile {
  exists: boolean;
  profileName: string;
  profileId: string;
  profileIdFormatted: string;
  country: string;
  email: string;
  fullName: string;
  avatarUrl: string;
}

export interface FullProfileResult {
  subscription: {
    planName: string;
    planFullName: string;
    expiresAt: string | null;
  };
  paymentProfile: PaymentProfile;
  language: string;
  errors: string[];
}

export interface ClosePaymentResult {
  success: boolean;
  message: string;
  newCookies?: Cookie[];
  flowLogs?: string[];
}

export async function scanFullProfile(
  cookies: Cookie[],
  familyType = "ultra",
): Promise<FullProfileResult> {
  const errors: string[] = [];
  const result: FullProfileResult = {
    subscription: { planName: "", planFullName: "", expiresAt: null },
    paymentProfile: {
      exists: false,
      profileName: "",
      profileId: "",
      profileIdFormatted: "",
      country: "",
      email: "",
      fullName: "",
      avatarUrl: "",
    },
    language: "",
    errors,
  };

  try {
    const subInfo = await scanSubscriptionInfo(cookies, { familyType });
    result.subscription.planName = subInfo.planName || "";
    result.subscription.planFullName = subInfo.planFullName || "";
    result.subscription.expiresAt = subInfo.expiresAt
      ? subInfo.expiresAt.toISOString()
      : null;
    if (
      familyType === "youtube" &&
      !result.subscription.planName &&
      !result.subscription.planFullName
    ) {
      errors.push("YouTube subscription scan: no YouTube Premium data returned");
    }
  } catch (e: any) {
    errors.push(`Subscription scan: ${e.message}`);
  }

  try {
    result.language = await scanGoogleLanguage(cookies);
  } catch (e: any) {
    errors.push(`Language scan: ${e.message}`);
  }

  try {
    let payResp = await httpReq("https://payments.google.com/gp/w/home/paymentmethods", {
      cookies,
    });
    if (
      payResp.status === 200 &&
      !isGoogleSignInUrl(payResp.finalUrl) &&
      !payResp.finalUrl.includes("/signup") &&
      !extractAtToken(payResp.body)
    ) {
      const settingsResp = await httpReq(
        "https://payments.google.com/gp/w/home/settings",
        { cookies },
      );
      if (
        settingsResp.finalUrl.includes("/signup") ||
        extractAtToken(settingsResp.body)
      ) {
        payResp = settingsResp;
      }
    }
    const finalUrl = payResp.finalUrl;

    const isLogin = isGoogleSignInUrl(finalUrl);
    const isSignup = finalUrl.includes("/signup");

    if (isLogin) {
      errors.push("Cookie expired, please sync again.");
    } else if (isSignup) {
      result.paymentProfile.exists = false;
    } else if (payResp.status !== 200) {
      errors.push(`Payments: settings HTTP ${payResp.status}`);
    } else if (payResp.status === 200) {
      const atToken = extractAtToken(payResp.body);
      const fSid = extractFSid(payResp.body);

      if (!atToken) {
        errors.push("Payments: no AT token found");
      } else {
        if (!fSid) {
          errors.push("Payments: no f.sid found");
        }
        const rpcIds = "Z2B9Ef,AtkfFb,YdWR4d";
        const payload = JSON.stringify([
          [
            ["Z2B9Ef", "[]", null, "2"],
            ["AtkfFb", "[]", null, "12"],
            ["YdWR4d", "[]", null, "13"],
          ],
        ]);
        const postBody = `f.req=${encodeURIComponent(payload)}&at=${encodeURIComponent(atToken)}&`;

        const batchResp = await httpReq(
          `https://payments.google.com/gp/w/_/PaymentsPortals/data/batchexecute?rpcids=${encodeURIComponent(rpcIds)}&source-path=%2Fgp%2Fw%2Fhome%2Fpaymentmethods&f.sid=${fSid}&hl=vi&soc-app=1&soc-platform=1&soc-device=1&_reqid=${Math.floor(Math.random() * 900000) + 100000}&rt=c`,
          {
            method: "POST",
            cookies,
            headers: {
              "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8",
              Origin: "https://payments.google.com",
              Referer: "https://payments.google.com/gp/w/home/paymentmethods",
              "X-Same-Domain": "1",
            },
            body: postBody,
          },
        );

        if (batchResp.status !== 200) {
          errors.push(`Payments: profile RPC HTTP ${batchResp.status}`);
        }
        const parsed = parseBatchResponse(batchResp.body);

        const z2Data = parsed.get("Z2B9Ef");
        if (z2Data) {
          try {
            const arr = JSON.parse(z2Data);
            if (arr?.[1]?.[0]) {
              const profile = arr[1][0];
              result.paymentProfile.exists = true;
              result.paymentProfile.profileName = profile[0] || "";
              result.paymentProfile.profileId = profile[1] || "";
              result.paymentProfile.country = profile[4] || "";
            } else {
              errors.push("Payments: Z2B9Ef profile list is empty");
            }
          } catch (e: any) {
            errors.push(`Z2B9Ef parse: ${e.message}`);
          }
        } else {
          errors.push("Payments: no Z2B9Ef data returned");
        }

        const atkData = parsed.get("AtkfFb");
        if (atkData) {
          try {
            const arr = JSON.parse(atkData);
            if (typeof arr?.[1] === "string")
              result.paymentProfile.profileIdFormatted = arr[1];
          } catch {

          }
        }

        const ydData = parsed.get("YdWR4d");
        if (ydData) {
          try {
            const arr = JSON.parse(ydData);
            if (arr?.[0]) {
              result.paymentProfile.fullName = arr[0][3] || "";
              result.paymentProfile.email = arr[0][4] || "";
              result.paymentProfile.avatarUrl = (arr[0][6] || "").replace(
                /\\u003d/g,
                "=",
              );
            }
          } catch {

          }
        }
      }
    }
  } catch (e: any) {
    errors.push(`Payments fetch: ${e.message}`);
  }

  return result;
}

export async function closePaymentProfile(
  cookies: Cookie[],
  adminAccount?: any,
): Promise<ClosePaymentResult> {
  const flowLogs: string[] = [];
  const pushFlowStep = (step: string, meta?: unknown) => {
    let line = step;
    if (meta !== undefined) {
      try {
        line += ` ${JSON.stringify(meta)}`;
      } catch {
        line += " [meta_unserializable]";
      }
    }
    flowLogs.push(line);
  };
  pushFlowStep("start_close_payment_profile", { email: adminAccount?.email || "" });

  if (!adminAccount?.email) {
    return { success: false, message: "Thiếu email quản trị.", flowLogs };
  }
  if (!adminAccount?.googlePassword) {
    return {
      success: false,
      message: "Thiếu mật khẩu Google để đóng hồ sơ thanh toán.",
      flowLogs,
    };
  }

  const realPassword = safeDecrypt(adminAccount.googlePassword);
  const totpSecret = adminAccount.totpSecret ? safeDecrypt(adminAccount.totpSecret) : "";

  const browser = await launchStealthBrowser();
  let activePage: Page | null = null;
  const fail = async (
    message: string,
    pageForCookies: Page | null = activePage,
  ): Promise<ClosePaymentResult> => {
    pushFlowStep("fail", { message });
    try {
      if (pageForCookies) {
        const dir = "D:\\Browser Preview";
        if (!existsSync(dir)) mkdirSync(dir, { recursive: true });
        const timeStr = new Date().toISOString().replace(/[:.]/g, "-");
        await pageForCookies.screenshot({ path: path.join(dir, `fail_${timeStr}.png`) });
      }
    } catch { }
    const newCookies = pageForCookies ? await extractCookies(pageForCookies) : undefined;
    return { success: false, message, newCookies, flowLogs };
  };

  try {
    const pages = await browser.pages();
    const page: Page = pages[0] || (await browser.newPage());
    activePage = page;

    await page.evaluateOnNewDocument(() => {
      Object.defineProperty(navigator, "webdriver", { get: () => undefined });
    });

    await puppeteerGoogleLogin(
      page,
      adminAccount.email,
      realPassword,
      totpSecret,
      pushFlowStep,
    );

    const loginCookies = await extractCookies(page);
    if (!loginCookies || loginCookies.length === 0) {
      return await fail("Không thể lấy cookies phiên sau khi đăng nhập.");
    }

    pushFlowStep("skip_language_change_for_close_payment");

    pushFlowStep("open_payment_settings");
    await page.goto("https://payments.google.com/gp/w/home/settings", {
      waitUntil: "domcontentloaded",
      timeout: 30000,
    });
    await sleep(800);

    if (page.url().includes("/signup")) {
      return await fail("Không có hồ sơ thanh toán để đóng.");
    }

    const pagesBeforePopup = await browser.pages();

    const isCloseButtonText = (text: string) => {
      const s = normalizeLooseText(text);
      return (
        s.includes("dong ho so thanh toan") ||
        s.includes("close payments profile") ||
        (s.includes("close") && s.includes("profile"))
      );
    };

    const clickCloseProfileButton = async (): Promise<boolean> => {
      const selectors = [
        "a",
        "div[role='button']",
        "button",
        "span[role='button']",
      ];

      for (const selector of selectors) {
        let elements: ElementHandle[] = [];
        try {
          elements = await page.$$(selector);
        } catch (e) {
          if (isDetachedFrameError(e)) continue;
          throw e;
        }
        for (const el of elements) {
          let text = "";
          try {
            text = await page.evaluate(
              (node) => ((node as HTMLElement).innerText || node.textContent || "").trim(),
              el,
            );
          } catch (e) {
            if (isDetachedFrameError(e)) continue;
            throw e;
          }
          if (!text || !isCloseButtonText(text)) continue;
          let isVisible = false;
          try {
            isVisible = await page.evaluate((node) => {
              const el = node as HTMLElement;
              const rect = el.getBoundingClientRect();
              const style = getComputedStyle(el);
              return (
                rect.width > 2 &&
                rect.height > 2 &&
                style.display !== "none" &&
                style.visibility !== "hidden" &&
                style.opacity !== "0"
              );
            }, el);
          } catch (e) {
            if (!isDetachedFrameError(e)) throw e;
            continue;
          }
          if (!isVisible) continue;
          try {
            await el.evaluate((node) =>
              (node as HTMLElement).scrollIntoView({ block: "center", inline: "center" }),
            );
          } catch (e) {
            if (!isDetachedFrameError(e)) throw e;
            continue;
          }
          await sleep(80);
          try {
            await el.click();
          } catch (e) {
            if (isDetachedFrameError(e)) continue;
            throw e;
          }
          return true;
        }
      }

      let wipeoutLinks: ElementHandle[] = [];
      try {
        wipeoutLinks = await page.$$(`a[href*="wipeout"]`);
      } catch (e) {
        if (!isDetachedFrameError(e)) throw e;
      }
      if (wipeoutLinks.length > 0) {
        try {
          await wipeoutLinks[0].click();
        } catch (e) {
          if (!isDetachedFrameError(e)) throw e;
          return false;
        }
        return true;
      }

      return false;
    };

    let clickedCloseProfile = await clickCloseProfileButton();
    if (!clickedCloseProfile) {
      return await fail("Không tìm thấy nút đóng hồ sơ thanh toán trên trang cài đặt.");
    }
    pushFlowStep("click_close_profile");
    let postCloseState: "dialog" | "reauth" | "wipeout" | null = "dialog";


    const clickContinueInCloseDialog = async (): Promise<boolean> => {
      pushFlowStep("continue_dialog_wait_start");
      const deadline = Date.now() + 12000;
      let lastState: {
        clicked: boolean;
        foundDialog: boolean;
        foundPrimary: boolean;
        foundEnabledPrimary: boolean;
      } | null = null;

      while (Date.now() < deadline) {
        const state = await page.evaluate(() => {
          const normalize = (text: string) =>
            (text || "")
              .normalize("NFD")
              .replace(/[\u0300-\u036f]/g, "")
              .replace(/[\u0111\u0110]/g, "d")
              .replace(/\s+/g, " ")
              .trim()
              .toLowerCase();

          const isPrimaryActionText = (text: string) => {
            const n = normalize(text);
            return (
              n === "tiep" ||
              n === "tiep tuc" ||
              n === "continue" ||
              n === "next" ||
              n === "proceed" ||
              n === "dong y" ||
              n === "dong y tiep tuc" ||
              n === "agree" ||
              n.startsWith("tiep ") ||
              n.startsWith("continue ")
            );
          };

          const isNegativeActionText = (text: string) => {
            const n = normalize(text);
            return (
              n === "huy" ||
              n === "huy bo" ||
              n === "cancel" ||
              n === "close" ||
              n.includes("huy quy trinh")
            );
          };

          const result = {
            clicked: false,
            foundDialog: false,
            foundPrimary: false,
            foundEnabledPrimary: false,
          };

          const getElementLabel = (el: HTMLElement): string => {
            const raw =
              el.innerText ||
              el.textContent ||
              el.getAttribute("aria-label") ||
              (el as HTMLInputElement).value ||
              "";
            return normalize(raw);
          };

          const allDialogs = Array.from(
            document.querySelectorAll('[role="dialog"], [aria-modal="true"]'),
          ) as HTMLElement[];

          const preferredDialog = allDialogs.find((dlg) => {
            const t = normalize(dlg.innerText || "");
            return (
              t.includes("dong ho so thanh toan") ||
              t.includes("close payments profile") ||
              t.includes("xac minh danh tinh cua ban") ||
              t.includes("verify your identity") ||
              t.includes("de tiep tuc, vui long xac minh danh tinh") ||
              t.includes("to continue, please verify your identity")
            );
          });

          const dialogWithAction = allDialogs.find((dlg) => {
            const btns = Array.from(
              dlg.querySelectorAll(
                "button, div[role='button'], span[role='button'], a[role='button']",
              ),
            ) as HTMLElement[];
            return btns.some((btn) => {
              const label = btn.innerText || btn.textContent || "";
              return isPrimaryActionText(label);
            });
          });

          const closeDialog = preferredDialog || dialogWithAction;
          if (!closeDialog) return result;
          result.foundDialog = true;

          const actionButtonsInDialog = Array.from(
            closeDialog.querySelectorAll(
              "button, div[role='button'], span[role='button'], a[role='button']",
            ),
          ) as HTMLElement[];

          const actionButtonsGlobal = Array.from(
            document.querySelectorAll(
              "button, div[role='button'], span[role='button'], a[role='button']",
            ),
          ) as HTMLElement[];

          const candidates = actionButtonsInDialog.length > 0 ? actionButtonsInDialog : actionButtonsGlobal;

          const pickPrimary = (buttons: HTMLElement[]) => {
            let firstEnabledPrimary: HTMLElement | null = null;
            let firstDisabledPrimary: HTMLElement | null = null;
            let foundPrimary = false;
            for (const btn of buttons) {
              const label = getElementLabel(btn);
              if (!label || !isPrimaryActionText(label) || isNegativeActionText(label)) continue;
              foundPrimary = true;

              const rect = btn.getBoundingClientRect();
              const style = window.getComputedStyle(btn);
              const disabled =
                btn.getAttribute("aria-disabled") === "true" ||
                (btn as HTMLButtonElement).disabled === true;
              const visible =
                rect.width > 2 &&
                rect.height > 2 &&
                style.display !== "none" &&
                style.visibility !== "hidden" &&
                style.opacity !== "0";
              if (!visible) continue;

              if (!disabled && !firstEnabledPrimary) firstEnabledPrimary = btn;
              if (disabled && !firstDisabledPrimary) firstDisabledPrimary = btn;
            }
            return { firstEnabledPrimary, firstDisabledPrimary, foundPrimary };
          };

          const { firstEnabledPrimary, firstDisabledPrimary, foundPrimary } = pickPrimary(candidates);
          result.foundPrimary = foundPrimary;
          if (firstEnabledPrimary) {
            result.foundEnabledPrimary = true;
            firstEnabledPrimary.scrollIntoView({ block: "center", inline: "center" });
            firstEnabledPrimary.click();
            result.clicked = true;
            return result;
          }

          const allNodes = [closeDialog, ...Array.from(closeDialog.querySelectorAll("*"))] as HTMLElement[];
          let scrolled = false;
          for (const node of allNodes) {
            if (node.scrollHeight > node.clientHeight + 8) {
              node.scrollTop = node.scrollHeight;
              scrolled = true;
            }
          }
          if (!scrolled) {
            window.scrollBy(0, 700);
          }

          if (firstDisabledPrimary) {
            firstDisabledPrimary.scrollIntoView({ block: "end", inline: "center" });
          }

          return result;
        });
        lastState = state;
        if (state.clicked) {
          pushFlowStep("continue_dialog_clicked", state);
          return true;
        }

        await sleep(300);
      }
      pushFlowStep("continue_dialog_timeout", lastState || {});
      return false;
    };

    const detectPostCloseState = async (): Promise<"dialog" | "reauth" | "wipeout" | null> => {
      try {
        const currentUrl = page.url();
        if (isWipeoutUrl(currentUrl)) return "wipeout";
        if (isReauthUrl(currentUrl)) return "reauth";
      } catch { }

      try {
        const wipeoutFrame = page.frames().find((f) => isWipeoutUrl(f.url()));
        if (wipeoutFrame) return "wipeout";
      } catch { }

      try {
        const hasDialog = await page.evaluate(() => {
          const normalize = (text: string) =>
            (text || "")
              .normalize("NFD")
              .replace(/[\u0300-\u036f]/g, "")
              .replace(/[\u0111\u0110]/g, "d")
              .replace(/\s+/g, " ")
              .trim()
              .toLowerCase();
          const allDialogs = Array.from(
            document.querySelectorAll('[role="dialog"], [aria-modal="true"]'),
          ) as HTMLElement[];
          return allDialogs.some((dlg) => {
            const t = normalize(dlg.innerText || "");
            return (
              t.includes("dong ho so thanh toan") ||
              t.includes("close payments profile") ||
              t.includes("xac minh danh tinh cua ban") ||
              t.includes("verify your identity") ||
              t.includes("de tiep tuc, vui long xac minh danh tinh") ||
              t.includes("to continue, please verify your identity")
            );
          });
        });
        if (hasDialog) return "dialog";
      } catch { }

      return null;
    };

    const waitForPostCloseState = async (
      timeoutMs = 10000,
    ): Promise<"dialog" | "reauth" | "wipeout" | null> => {
      const deadline = Date.now() + timeoutMs;
      while (Date.now() < deadline) {
        const state = await detectPostCloseState();
        if (state) return state;
        await sleep(250);
      }
      return null;
    };

    postCloseState = await waitForPostCloseState(8000);
    if (!postCloseState) {
      for (let retry = 0; retry < 2; retry++) {
        pushFlowStep("click_close_profile_retry", { retry: retry + 1 });
        clickedCloseProfile = await clickCloseProfileButton();
        if (!clickedCloseProfile) continue;
        postCloseState = await waitForPostCloseState(6000);
        if (postCloseState) break;
      }
    }
    pushFlowStep("post_close_state", { state: postCloseState || "none" });

    if (!postCloseState) {
      return await fail("Không thể nhấn nút Tiếp tục trước popup xác thực mật khẩu.");
    }

    let continueClicked = true;
    if (postCloseState === "dialog") {
      continueClicked = await clickContinueInCloseDialog();
    } else {
      pushFlowStep("continue_dialog_skipped", { reason: postCloseState });
    }
    if (!continueClicked) {
      return await fail("Could not click Continue before reauth popup.");
    }

    const existingPages = new Set(pagesBeforePopup);
    let authPage = await waitForReauthPage(browser, page, existingPages, 25000);
    pushFlowStep("reauth_page_lookup_initial", {
      found: !!authPage,
      url: authPage ? authPage.url() : "",
    });
    if (!authPage) {
      try {
        const inlinePwd = await findPasswordInputAcrossFrames(page);
        if (inlinePwd) authPage = page;
      } catch (e) {
        if (!isDetachedFrameError(e)) throw e;
      }
      pushFlowStep("reauth_page_lookup_inline_fallback", {
        found: !!authPage,
        url: authPage ? authPage.url() : "",
      });
    }

    if (!authPage) {
      return await fail("Popup xác thực mật khẩu không xuất hiện sau khi nhấn Tiếp tục.");
    }
    pushFlowStep("popup_password_tab");

    const handleReauth = async (seedPage: Page): Promise<{ ok: boolean; usedPage: Page | null }> => {
      pushFlowStep("reauth_password");
      let activeReauthPage: Page | null = seedPage;

      try {
        if (!seedPage.isClosed()) {
          await seedPage.bringToFront();
          try {
            await seedPage.waitForNavigation({ waitUntil: "domcontentloaded", timeout: 2000 });
          } catch { }
          await sleep(200);
        }
      } catch (e) {
        if (!isDetachedFrameError(e)) throw e;
      }

      for (let cycle = 0; cycle < 1; cycle++) {
        let targetPage = activeReauthPage;

        if (!targetPage) {
          const allPages = await browser.pages();
          for (const p of allPages) {
            if (p.isClosed() || p === page) continue;
            try {
              const hasPwdInput = await findPasswordInputAcrossFrames(p);
              if (hasPwdInput) {
                targetPage = p;
                break;
              }
            } catch (e) {
              if (!isDetachedFrameError(e)) throw e;
            }
          }
        }

        if (!targetPage) {
          try {
            if (!page.isClosed()) {
              const mainHasPwd = await findPasswordInputAcrossFrames(page);
              if (mainHasPwd) targetPage = page;
            }
          } catch (e) {
            if (!isDetachedFrameError(e)) throw e;
          }
        }

        if (!targetPage) {
          pushFlowStep("reauth_no_target");
          return { ok: false, usedPage: activeReauthPage };
        }
        activeReauthPage = targetPage;

        try {
          await targetPage.bringToFront();
        } catch (e) {
          if (isDetachedFrameError(e)) {
            pushFlowStep("reauth_page_detached_before_submit");
            return { ok: true, usedPage: targetPage };
          }
          throw e;
        }

        pushFlowStep("reauth_target_url", { url: targetPage.url().substring(0, 160) });

        const passwordSubmitted = await submitPasswordOnPage(
          targetPage,
          realPassword,
          adminAccount.email,
        );
      if (!passwordSubmitted) {
        pushFlowStep("reauth_pwd_not_found");
        return { ok: false, usedPage: targetPage };
      }
      pushFlowStep("reauth_password_submitted");

      try {
        await targetPage.waitForNavigation({
          waitUntil: "domcontentloaded",
          timeout: 15000,
        });
      } catch { }
      await sleep(500);

        let postPwdUrl = "";
        try {
          postPwdUrl = targetPage.url();
        } catch (e) {
          if (isDetachedFrameError(e)) {
            pushFlowStep("reauth_page_detached_after_submit");
            return { ok: true, usedPage: targetPage };
          }
          throw e;
      }
      pushFlowStep("reauth_after_submit_url", { url: postPwdUrl });

      try {
        const quickWipeoutContext = await waitForWipeoutContext(page, 8000);
        if (quickWipeoutContext) {
          pushFlowStep("reauth_fastpath_main_wipeout_ready", {
            url: quickWipeoutContext.url(),
          });
          return { ok: true, usedPage: targetPage };
        }
      } catch { }

      const needsTotp =
        postPwdUrl.includes("challenge/totp") ||
        postPwdUrl.includes("challenge/ipp") ||
        (postPwdUrl.includes("totp") && !postPwdUrl.includes("challenge/pwd"));
      if (!needsTotp) {
        pushFlowStep("reauth_password_accepted_no_totp");
        return { ok: true, usedPage: targetPage };
      }

        if (!totpSecret) {
          throw new Error("Yêu cầu 2FA nhưng thiếu TOTP secret.");
        }

        await moveToTotpChallenge(targetPage);
        let totpInput = await waitAndGet(targetPage, TOTP_INPUT_SELECTORS, 10000);
        if (!totpInput) {
          totpInput = await findFallbackTotpInput(targetPage);
        }
        if (!totpInput) {
          pushFlowStep("reauth_totp_not_found");
          return { ok: false, usedPage: targetPage };
        }

        const totpCode = await get2FACode(totpSecret);
        if (!totpCode) return { ok: false, usedPage: targetPage };

        pushFlowStep("2fa_reauth");
        try {
          await totpInput.click();
          await sleep(100);
          await totpInput.type(totpCode, { delay: 15 });
        } catch (e) {
          if (isDetachedFrameError(e)) {
            pushFlowStep("reauth_page_detached_during_totp_submit");
            return { ok: true, usedPage: targetPage };
          }
          throw e;
        }

        await sleep(300);
        const totpNext =
          (await targetPage.$("#totpNext")) ||
          (await targetPage.$("#idvPreregisteredPhoneNext"));
        if (totpNext) await totpNext.click();
        else await targetPage.keyboard.press("Enter");
        try {
          await targetPage.waitForNavigation({
            waitUntil: "networkidle2",
            timeout: 15000,
          });
        } catch { }
        await sleep(700);
        return { ok: true, usedPage: targetPage };
      }

      try {
        if (activeReauthPage && !activeReauthPage.isClosed()) {
          const dir = "D:\\Browser Preview";
          if (!existsSync(dir)) mkdirSync(dir, { recursive: true });
          const timeStr = new Date().toISOString().replace(/[:.]/g, "-");
          await activeReauthPage.screenshot({ path: path.join(dir, `reauth_fail_${timeStr}.png`) });
          pushFlowStep(`reauth_fail_screenshot_saved`);
        }
      } catch { }

      return { ok: false, usedPage: activeReauthPage };
    };

    const reauthState = await handleReauth(authPage);
    const reauthOk = reauthState.ok;
    const usedAuthPage = reauthState.usedPage;
    if (!reauthOk) {
      return await fail(REAUTH_PASSWORD_NOT_FOUND_MESSAGE, usedAuthPage || authPage);
    }

    if (usedAuthPage && usedAuthPage !== page && !usedAuthPage.isClosed()) {
      try {
        await usedAuthPage.close();
      } catch { }
    }

    await page.bringToFront();
    await sleep(120);

    pushFlowStep("wait_wipeout_after_reauth");

    let wipeoutContext: EvalContext | null = null;
    try {
      wipeoutContext = await waitForWipeoutContext(page, 15000);
    } catch { }
    pushFlowStep("wait_wipeout_context_result", {
      found: !!wipeoutContext,
      url: wipeoutContext ? wipeoutContext.url() : "",
    });

    let wipeoutParams: WipeoutParams | null = null;

    for (let attempt = 0; attempt < 8; attempt++) {
      try {
        const activeContext = wipeoutContext || page;
        wipeoutParams = await extractWipeoutParams(activeContext);
        pushFlowStep("wipeout_params_attempt", {
          attempt: attempt + 1,
          contextUrl: activeContext.url(),
          hasXsrf: !!wipeoutParams?.xsrf,
          hasMsgToken: !!wipeoutParams?.msgToken,
          xsrfLen: wipeoutParams?.xsrf?.length || 0,
          msgLen: wipeoutParams?.msgToken?.length || 0,
        });
        if (wipeoutParams?.xsrf && wipeoutParams?.msgToken) break;
      } catch { }
      if (!wipeoutContext) {
        try {
          wipeoutContext = await waitForWipeoutContext(page, 2000);
        } catch { }
        if (wipeoutContext) {
          pushFlowStep("wipeout_context_late_found", { url: wipeoutContext.url() });
        }
      }
      await sleep(500);
    }

    if (!wipeoutParams?.xsrf || !wipeoutParams?.msgToken) {
      return await fail("Thiếu tham số wipeout (xsrf/msg token) sau khi xác thực mật khẩu.");
    }
    const activeWipeoutParams = wipeoutParams;
    const submitContext = wipeoutContext || page;

    let submitUserIndex = "0";
    try {
      const contextUrl = submitContext.url();
      const idx = extractPaymentsUserIndex(contextUrl);
      if (idx) submitUserIndex = idx;
    } catch { }
    if (submitUserIndex === "0") {
      try {
        const pageIdx = extractPaymentsUserIndex(page.url());
        if (pageIdx) submitUserIndex = pageIdx;
      } catch { }
    }
    pushFlowStep("submit_user_index_resolved", {
      submitUserIndex,
      submitContextUrl: submitContext.url(),
      pageUrl: page.url(),
    });

    pushFlowStep("fetch_post_close_reason");
    const msgPayload = JSON.stringify([
      [null, wipeoutParams.msgToken, null, [], null, null, "vi", 1],
      [
        null,
        [
          [
            "confirmationForm-1",
            "CCg=",
            [
              [null, []],
              [null, []],
              [null, []],
              [null, []],
              [null, []],
              [
                null,
                [
                  [
                    "closureReasonSelector",
                    "WIPEOUT_REASON_DONT_NEED_ACCOUNT_ANYMORE",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    3009,
                  ],
                ],
              ],
              [null, []],
            ],
            null,
            2006,
          ],
        ],
      ],
    ]);

    const submitUrl = `https://payments.google.com/payments/u/${encodeURIComponent(submitUserIndex)}/wipeout/submit?cn=${encodeURIComponent(activeWipeoutParams.cn)}&eo=${encodeURIComponent("https://payments.google.com")}&hostOrigin=aHR0cHM6Ly9wYXltZW50cy5nb29nbGUuY29t&ipi=${encodeURIComponent(activeWipeoutParams.ipi)}&hl=vi&mm=p&origin=${encodeURIComponent("https://payments.google.com")}&si=${encodeURIComponent(activeWipeoutParams.si)}&style=${encodeURIComponent(":pc=#fff;tn=pc;nav=PT;m2_o")}&cst=${encodeURIComponent(activeWipeoutParams.cst)}&wst=${encodeURIComponent(activeWipeoutParams.wst)}&rt=j&s=1`;
    pushFlowStep("submit_url_built", { submitUrl });

    pushFlowStep("submit_close_profile");
    let wipeoutResult: WipeoutFetchResult = {
      success: false,
      code: -1,
      bodyPreview: "",
    };
    wipeoutResult = await submitWipeoutViaFetch(
      submitContext,
      submitUrl,
      activeWipeoutParams.xsrf,
      msgPayload,
    );
    pushFlowStep("submit_close_profile_result", {
      success: wipeoutResult.success,
      code: wipeoutResult.code,
      bodyPreview: (wipeoutResult.bodyPreview || "").slice(0, 180),
    });
    if (!wipeoutResult.success) {
      return await fail(
        `Gửi yêu cầu đóng hồ sơ thất bại (mã=${wipeoutResult.code}).`,
      );
    }

    const newCookies = await extractCookies(page);
    if (!newCookies || newCookies.length === 0) {
      return await fail("Hồ sơ đã đóng nhưng không lấy được cookies phiên mới.");
    }

    const verifyAfterSubmit =
      process.env.GOOGLE_CLOSE_PAYMENT_VERIFY_AFTER_SUBMIT === "1";
    if (verifyAfterSubmit) {
      pushFlowStep("verify_closed_profile_after_submit_start");
      const verified = await verifyPaymentProfileClosed(newCookies);
      pushFlowStep("verify_closed_profile_after_submit_result", {
        verified:
          verified === null ? "unknown_cookie_expired" : verified ? "closed" : "not_closed",
      });
      if (verified === false) {
        return {
          success: false,
          message: "Đã gửi yêu cầu đóng nhưng chưa xác minh được hồ sơ đã đóng.",
          newCookies,
          flowLogs,
        };
      }
    } else {
      pushFlowStep("verify_closed_profile_after_submit_skipped");
    }

    return {
      success: true,
      message: `Đã đóng hồ sơ thanh toán qua API (mã ${wipeoutResult.code}).`,
      newCookies,
      flowLogs,
    };
  } catch (e: any) {
    const lastStep = flowLogs.length > 0 ? flowLogs[flowLogs.length - 1] : "unknown";
    return await fail(`Puppeteer error at ${lastStep}: ${e?.message || String(e)}`);
  } finally {
    try {
      await browser.close();
    } catch { }
  }
}
function extractBl(html: string): string {
  const m = html.match(/"cfb2h"\s*:\s*"([^"]+)"/);
  return m ? m[1] : "boq_identityaccountsettingsuiserver_20260317.09_p0";
}

async function fetchLanguagePage(cookies: Cookie[]) {
  const resp = await httpReq("https://myaccount.google.com/language", { cookies });
  if (isGoogleSignInUrl(resp.finalUrl)) {
    throw new Error("Cookie hết hạn, vui lòng đồng bộ lại.");
  }
  if (resp.status !== 200) {
    throw new Error(`GET /language trả về status ${resp.status}`);
  }
  const atToken = extractAtToken(resp.body);
  if (!atToken) throw new Error("Không tìm thấy AT/XSRF token.");
  return { atToken, fSid: extractFSid(resp.body), bl: extractBl(resp.body), html: resp.body };
}

export async function scanGoogleLanguage(cookies: Cookie[]): Promise<string> {
  const { html } = await fetchLanguagePage(cookies);

  const labelMatch = html.match(/<label lang="([^"]+)">([^<]+)<\/label>/);
  if (labelMatch) {
    return labelMatch[2];
  }

  return "";
}

export async function changeGoogleLanguage(
  cookies: Cookie[],
  languageCode: string,
): Promise<{ success: boolean; message: string }> {
  try {
    const { atToken, fSid, bl } = await fetchLanguagePage(cookies);
    const reqId = Math.floor(Math.random() * 900000) + 100000;
    const resp = await httpReq(
      `https://myaccount.google.com/_/language_update?f.sid=${fSid}&bl=${encodeURIComponent(bl)}&hl=en&soc-app=1&soc-platform=1&soc-device=1&_reqid=${reqId}&rt=j`,
      {
        method: "POST",
        cookies,
        headers: {
          "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8",
          Origin: "https://myaccount.google.com",
          Referer: "https://myaccount.google.com/language",
          "X-Same-Domain": "1",
        },
        body: `f.req=${encodeURIComponent(JSON.stringify([[languageCode]]))}&at=${encodeURIComponent(atToken)}&`,
      },
    );
    if (resp.status === 200) {
      return { success: true, message: `Successfully changed language to "${languageCode}".` };
    }
    return { success: false, message: `language_update status ${resp.status}` };
  } catch (e: any) {
    return { success: false, message: e.message };
  }
}

