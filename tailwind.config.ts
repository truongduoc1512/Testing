import type { Config } from "tailwindcss";

const config: Config = {
  content: ["./app/**/*.{ts,tsx}", "./components/**/*.{ts,tsx}", "./lib/**/*.{ts,tsx}"],
  darkMode: "class",
  theme: {
    extend: {
      colors: {
        glass: {
          50: "rgba(255,255,255,0.04)",
          100: "rgba(255,255,255,0.08)",
          200: "rgba(255,255,255,0.12)",
          300: "rgba(255,255,255,0.18)",
          border: "rgba(255,255,255,0.12)",
        },
        brand: {
          violet: "#7c3aed",
          cyan: "#06b6d4",
          pink: "#ec4899",
          emerald: "#10b981",
          blue: "#3b82f6",
        },
      },
      borderRadius: {
        "4xl": "2.5rem",
      },
      keyframes: {
        "fade-up": {
          "0%": { opacity: "0", transform: "translateY(20px)" },
          "100%": { opacity: "1", transform: "translateY(0)" },
        },
      },
      animation: {
        "fade-up": "fade-up 0.8s cubic-bezier(0.16, 1, 0.3, 1) forwards",
      },
    },
  },
  plugins: [],
};

export default config;
