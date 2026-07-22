export type SubmitState = "idle" | "loading" | "success" | "error";

export interface PasswordStrengthResult {
  score: 0 | 1 | 2 | 3 | 4;
  label: "" | "Yếu" | "Trung bình" | "Tốt" | "Mạnh";
  color: string;
}
