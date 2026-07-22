import type { Metadata } from "next";
import { headers } from "next/headers";
import { Inter, Plus_Jakarta_Sans } from "next/font/google";
import Script from "next/script";
import "./globals.css";

const inter = Inter({
  subsets: ["latin", "vietnamese"],
  variable: "--font-inter",
  display: "swap",
});

const jakarta = Plus_Jakarta_Sans({
  subsets: ["latin", "vietnamese"],
  variable: "--font-jakarta",
  display: "swap",
});

export const metadata: Metadata = {
  title: "VieShop",
  description: "VieShop quản lý tài khoản Google Family.",
};

export default async function RootLayout({ children }: { children: React.ReactNode }) {
  const nonce = (await headers()).get("x-nonce") ?? undefined;

  return (
    <html lang="vi" className={`dark ${inter.variable} ${jakarta.variable}`} suppressHydrationWarning>
      <body className="font-sans" suppressHydrationWarning>
        <Script id="disable-browser-console" nonce={nonce} strategy="beforeInteractive">
          {`
            (() => {
              if (typeof window === "undefined") return;
              const noop = () => {};
              const methods = ["log", "info", "warn", "error", "debug", "trace"];
              const consoleRef = window.console || {};
              for (const method of methods) {
                consoleRef[method] = noop;
              }
              window.console = consoleRef;
            })();
          `}
        </Script>
        {children}
      </body>
    </html>
  );
}
