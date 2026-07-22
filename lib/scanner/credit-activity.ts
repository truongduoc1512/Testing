import { googleHttpReq, type Cookie } from "@/lib/scanner/google-http";
import { apiError } from "@/lib/logger";

export interface CreditTransaction {
  id: string;
  timestamp: number;
  type: "usage" | "refund";
  amount: number;
}

export interface FamilyMemberCredit {
  name: string;
  avatarUrl: string;
  totalUsed: number;
}

export interface CreditActivityResult {
  remaining: number;
  refreshDate: number | null;
  transactions: CreditTransaction[];
  familyMembers: FamilyMemberCredit[];
  totalTransactions: number;
  error?: string;
}

export async function fetchCreditActivity(
  cookies: Cookie[],
  authuser = "0",
  proxyUrl = process.env.GOOGLE_PROXY_URL || "",
): Promise<CreditActivityResult> {
  const empty: CreditActivityResult = {
    remaining: 0,
    refreshDate: null,
    transactions: [],
    familyMembers: [],
    totalTransactions: 0,
  };

  try {
    const resp = await googleHttpReq(`https://one.google.com/u/${authuser}/ai/activity`, {
      cookies,
      proxyUrl,
    });

    if (resp.status !== 200 || resp.finalUrl.includes("accounts.google.com")) {
      return { ...empty, error: "session_expired" };
    }

    const pattern =
      /AF_initDataCallback\(\{key:\s*'ds:0',\s*hash:\s*'\d+',\s*data:([\s\S]*?)\}\);/;
    const m = resp.body.match(pattern);
    if (!m) {
      return { ...empty, error: "data_not_found" };
    }

    const rawData = m[1].replace(/\s+/g, " ").trim();
    let bracketCount = 0;
    let endIdx = 0;
    for (let i = 0; i < rawData.length; i++) {
      if (rawData[i] === "[") bracketCount++;
      else if (rawData[i] === "]") bracketCount--;
      if (bracketCount === 0 && i > 0) {
        endIdx = i + 1;
        break;
      }
    }
    const jsonStr = rawData.substring(0, endIdx);

    let data: any[];
    try {
      data = JSON.parse(jsonStr);
    } catch {
      return { ...empty, error: "parse_error" };
    }

    let remaining = 0;
    let refreshDate: number | null = null;
    if (data[0] && Array.isArray(data[0][0])) {
      remaining = data[0][0][0] ?? 0;
      if (data[0][0][1]) refreshDate = data[0][0][1][0] ?? null;
    }

    const transactions: CreditTransaction[] = [];
    if (data[1] && Array.isArray(data[1])) {
      for (const entry of data[1]) {
        if (!Array.isArray(entry) || entry.length < 4) continue;
        transactions.push({
          id: entry[0],
          timestamp: entry[1]?.[0] ?? 0,
          type: entry[2] === 4 ? "refund" : "usage",
          amount: entry[3] ?? 0,
        });
      }
    }

    const familyMembers: FamilyMemberCredit[] = [];
    for (let i = 3; i < data.length; i++) {
      if (Array.isArray(data[i]) && data[i].length > 0) {
        const first = data[i][0];
        if (
          Array.isArray(first) &&
          first.length >= 3 &&
          typeof first[0] === "string" &&
          first[0].includes("googleusercontent")
        ) {
          for (const member of data[i]) {
            familyMembers.push({
              avatarUrl: member[0],
              totalUsed: member[1],
              name: member[2],
            });
          }
          break;
        }
      }
    }

    return {
      remaining,
      refreshDate,
      transactions,
      familyMembers,
      totalTransactions: transactions.length,
    };
  } catch (e) {
    apiError("fetchCreditActivity", e);
    return { ...empty, error: "fetch_error" };
  }
}
