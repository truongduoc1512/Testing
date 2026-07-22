function requiredEnv(name: string): string {
  const value = process.env[name];
  if (value) return value;

  const isTestEnv = process.env.NODE_ENV === "test" || process.env.VITEST === "true";
  if (isTestEnv) return `test-${name.toLowerCase()}`;

  throw new Error(`Missing required environment variable: ${name}`);
}

function optionalEnv(name: string, fallback: string): string {
  return process.env[name] || fallback;
}

export const JWT_SECRET = requiredEnv("JWT_SECRET");

export const ACCOUNT_EMAIL = process.env.ACCOUNT_EMAIL || "";
export const ACCOUNT_PASSWORD = process.env.ACCOUNT_PASSWORD || "";

export const CACHE_SECRET = requiredEnv("CACHE_SECRET");
export const CRYPTO_SALT = optionalEnv("CRYPTO_SALT", "cache-salt");

export const CHROME_EXECUTABLE_PATH = optionalEnv(
  "CHROME_EXECUTABLE_PATH",
  "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe",
);
export const GOOGLE_AI_API_KEY = process.env.GOOGLE_AI_API_KEY || "";
export const GOOGLE_PROXY_URL = process.env.GOOGLE_PROXY_URL || "";
