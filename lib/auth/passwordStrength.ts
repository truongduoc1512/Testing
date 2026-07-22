import type { PasswordStrengthResult } from "@/types/auth";

export function getPasswordStrength(pw: string): PasswordStrengthResult {
  let score = 0;
  if (pw.length >= 8) score++;
  if (/[A-Z]/.test(pw)) score++;
  if (/[0-9]/.test(pw)) score++;
  if (/[^A-Za-z0-9]/.test(pw)) score++;

  const labels: PasswordStrengthResult["label"][] = [
    "",
    "Yếu",
    "Trung bình",
    "Tốt",
    "Mạnh",
  ];
  const colors = [
    "bg-slate-700",
    "bg-red-500",
    "bg-yellow-500",
    "bg-cyan-400",
    "bg-emerald-400",
  ];

  return {
    score: score as PasswordStrengthResult["score"],
    label: labels[score],
    color: colors[score],
  };
}
