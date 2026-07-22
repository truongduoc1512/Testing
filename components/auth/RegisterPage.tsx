"use client";
import { useState, useRef, useCallback } from "react";
import { Eye, EyeOff, Mail, ShieldCheck } from "lucide-react";
import { InputFloating } from "@/components/ui/InputFloating";
import { Button } from "@/components/ui/Button";
import { Toast } from "@/components/ui/Toast";
import { getPasswordStrength } from "@/lib/auth/passwordStrength";
import type { SubmitState, PasswordStrengthResult } from "@/types/auth";

type Step = "form" | "otp";
type ToastState = { message: string; type: "success" | "error" | "info" } | null;

async function postJson<T = Record<string, unknown>>(
  url: string,
  body: unknown,
): Promise<{ ok: boolean; data: T }> {
  const res = await fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });
  const data = (await res.json()) as T;
  return { ok: res.ok, data };
}

export default function RegisterPage() {
  const [step, setStep] = useState<Step>("form");
  const [submitState, setSubmitState] = useState<SubmitState>("idle");
  const [email, setEmail] = useState("");
  const [fullName, setFullName] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [otpCode, setOtpCode] = useState("");
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [toast, setToast] = useState<ToastState>(null);
  const [contact, setContact] = useState<{ url: string; label: string } | null>(null);
  const [cooldown, setCooldown] = useState(0);
  const cooldownRef = useRef<ReturnType<typeof setInterval> | null>(null);
  const otpInputRef = useRef<HTMLInputElement>(null);

  const pwStrength: PasswordStrengthResult = getPasswordStrength(password);
  const normalizedEmail = email.trim().toLowerCase();

  const startCooldown = useCallback(() => {
    setCooldown(60);
    if (cooldownRef.current) clearInterval(cooldownRef.current);
    cooldownRef.current = setInterval(() => {
      setCooldown((prev) => {
        if (prev <= 1) {
          if (cooldownRef.current) clearInterval(cooldownRef.current);
          return 0;
        }
        return prev - 1;
      });
    }, 1000);
  }, []);

  const validateForm = (): boolean => {
    const e: Record<string, string> = {};

    if (!normalizedEmail) e.email = "Vui lòng nhập email";
    else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(normalizedEmail)) e.email = "Email không hợp lệ";
    else if (!normalizedEmail.endsWith("@gmail.com")) e.email = "Chỉ chấp nhận địa chỉ Gmail";

    if (!fullName.trim() || fullName.trim().length < 2) e.fullName = "Tên tối thiểu 2 ký tự";
    if (password.length < 8) e.password = "Mật khẩu tối thiểu 8 ký tự";

    if (!confirmPassword) e.confirmPassword = "Vui lòng xác nhận mật khẩu";
    else if (confirmPassword !== password) e.confirmPassword = "Mật khẩu xác nhận không khớp";

    setErrors(e);
    return Object.keys(e).length === 0;
  };

  const sendOtp = async (): Promise<boolean> => {
    try {
      const { ok, data } = await postJson<{ error?: string; fields?: Record<string, string>; contactUrl?: string; contactLabel?: string }>(
        "/api/auth/register/send-otp",
        { email: normalizedEmail },
      );
      if (!ok) {
        if (data.contactUrl) {
          setContact({ url: data.contactUrl, label: data.contactLabel || "Liên hệ" });
        }
        if (data.fields) setErrors(data.fields);
        else setErrors({ email: data.error || "Lỗi gửi OTP" });
        return false;
      }
      setContact(null);
      return true;
    } catch {
      setErrors({ email: "Lỗi mạng" });
      return false;
    }
  };

  const handleRegisterSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (!validateForm()) return;
    setSubmitState("loading");

    const sent = await sendOtp();
    if (sent) {
      setToast({ message: "Mã xác thực đã gửi đến email của bạn", type: "success" });
      setStep("otp");
      startCooldown();
      setTimeout(() => otpInputRef.current?.focus(), 100);
    }
    setSubmitState("idle");
  };

  const handleResendOtp = async () => {
    if (cooldown > 0) return;
    setSubmitState("loading");

    const sent = await sendOtp();
    if (sent) {
      setToast({ message: "Đã gửi lại mã xác thực", type: "success" });
      startCooldown();
      setOtpCode("");
    }
    setSubmitState("idle");
  };

  const handleVerifyAndCreate = async () => {
    if (otpCode.length !== 6) {
      setErrors({ code: "Nhập đủ 6 số" });
      return;
    }
    setErrors({});
    setSubmitState("loading");

    try {
      const verify = await postJson<{ error?: string; token?: string }>(
        "/api/auth/register/verify-otp",
        { email: normalizedEmail, code: otpCode },
      );
      if (!verify.ok) {
        setErrors({ code: verify.data.error || "Mã không đúng" });
        setSubmitState("idle");
        return;
      }

      const register = await postJson<{ error?: string }>(
        "/api/auth/register",
        { email: normalizedEmail, fullName: fullName.trim(), password, token: verify.data.token },
      );
      if (!register.ok) {
        setErrors({ code: register.data.error || "Đăng ký thất bại" });
        setSubmitState("idle");
        return;
      }

      setSubmitState("success");
      setToast({ message: "Đăng ký thành công! Đang chuyển đến trang đăng nhập...", type: "success" });
      setTimeout(() => { window.location.href = "/login"; }, 2000);
    } catch {
      setErrors({ code: "Lỗi mạng" });
      setSubmitState("idle");
    }
  };

  return (
    <>
      {toast && (
        <Toast message={toast.message} type={toast.type} duration={3000} onClose={() => setToast(null)} />
      )}

      <div className="glass-card animate-fade-up w-full max-w-md rounded-4xl p-6 sm:p-8">
        <div className="mb-4 text-center">
          <div className="mx-auto mb-2 h-16 w-16 overflow-hidden">
            <img src="/logo.svg" alt="VieShop" className="h-full w-full scale-[1.8] object-contain" />
          </div>
          <h2 className="mb-1 text-2xl font-bold tracking-tight text-white">
            {step === "form" ? "Đăng ký VieShop" : "Xác thực email"}
          </h2>
          <p className="text-sm text-slate-400">
            {step === "form" ? "Tạo tài khoản mới để bắt đầu" : `Nhập mã OTP đã gửi đến ${email}`}
          </p>
        </div>

        {step === "form" && (
          <form className="space-y-5" onSubmit={handleRegisterSubmit} noValidate>
            <InputFloating
              id="register-email"
              label="Địa chỉ Gmail"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              error={errors.email}
            />

            <InputFloating
              id="register-fullname"
              label="Họ và tên"
              type="text"
              value={fullName}
              onChange={(e) => setFullName(e.target.value)}
              error={errors.fullName}
            />

            <div>
              <div className="relative">
                <InputFloating
                  id="register-password"
                  label="Mật khẩu"
                  type={showPassword ? "text" : "password"}
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  error={errors.password}
                />
                <button
                  type="button"
                  aria-label={showPassword ? "Ẩn mật khẩu" : "Hiện mật khẩu"}
                  onClick={() => setShowPassword((p) => !p)}
                  className="password-toggle"
                >
                  {showPassword ? <EyeOff className="h-5 w-5" /> : <Eye className="h-5 w-5" />}
                </button>
              </div>
              {password.length > 0 && (
                <div className="mt-1.5 flex items-center gap-3">
                  <div className="flex flex-1 gap-0.5">
                    {[1, 2, 3, 4].map((level) => (
                      <div
                        key={level}
                        className={`h-[3px] flex-1 rounded-full transition-all duration-300 ${
                          pwStrength.score >= level ? pwStrength.color : "bg-white/[0.06]"
                        }`}
                      />
                    ))}
                  </div>
                  {pwStrength.label && (
                    <span className="shrink-0 text-[11px] text-slate-500">{pwStrength.label}</span>
                  )}
                </div>
              )}
            </div>

            <div className="relative">
              <InputFloating
                id="register-confirm-password"
                label="Xác nhận mật khẩu"
                type={showConfirmPassword ? "text" : "password"}
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                error={errors.confirmPassword}
              />
              <button
                type="button"
                aria-label={showConfirmPassword ? "Ẩn mật khẩu" : "Hiện mật khẩu"}
                onClick={() => setShowConfirmPassword((p) => !p)}
                className="password-toggle"
              >
                {showConfirmPassword ? <EyeOff className="h-5 w-5" /> : <Eye className="h-5 w-5" />}
              </button>
            </div>

            <Button id="registerBtn" type="submit" loading={submitState === "loading"} className="w-full">
              Đăng ký
            </Button>

            {contact && (
              <p className="text-center text-sm text-slate-400">
                {errors.email}{" "}
                <a
                  href={contact.url}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="font-semibold text-violet-400 underline transition-colors hover:text-violet-300"
                >
                  {contact.label}
                </a>
              </p>
            )}

            <p className="text-center text-sm text-slate-500">
              Đã có tài khoản?{" "}
              <a href="/login" className="font-semibold text-violet-400 transition-colors hover:text-violet-300">
                Đăng nhập
              </a>
            </p>
          </form>
        )}

        {step === "otp" && (
          <div className="space-y-5">
            <div className="mx-auto flex items-center justify-center gap-2.5 rounded-xl bg-white/[0.04] px-4 py-2.5 ring-1 ring-white/[0.08]">
              <Mail className="h-4 w-4 text-slate-400" />
              <span className="text-sm text-slate-300">{email}</span>
            </div>

            <div>
              <input
                ref={otpInputRef}
                id="register-otp"
                type="text"
                inputMode="numeric"
                maxLength={6}
                value={otpCode}
                onChange={(e) => setOtpCode(e.target.value.replace(/\D/g, ""))}
                onKeyDown={(e) => { if (e.key === "Enter") handleVerifyAndCreate(); }}
                placeholder="------"
                className="w-full rounded-xl border border-white/10 bg-white/[0.03] px-4 py-4 text-center font-mono text-3xl font-bold tracking-[0.6em] text-white placeholder-white/[0.08] outline-none transition-all focus:border-white/20 focus:bg-white/[0.05] focus:ring-2 focus:ring-white/[0.06]"
              />
              {errors.code && (
                <p className="mt-2 flex items-center gap-1.5 text-xs font-medium text-red-400">
                  <span className="inline-block h-1.5 w-1.5 rounded-full bg-red-400" />
                  {errors.code}
                </p>
              )}
            </div>

            <Button
              id="verifyOtpBtn"
              type="button"
              loading={submitState === "loading"}
              className="w-full"
              onClick={handleVerifyAndCreate}
            >
              <ShieldCheck className="h-5 w-5" />
              Xác thực & Tạo tài khoản
            </Button>

            <div className="flex items-center justify-between text-sm">
              <span className="text-slate-600">{cooldown > 0 && `Gửi lại sau ${cooldown}s`}</span>
              <button
                type="button"
                disabled={cooldown > 0 || submitState === "loading"}
                onClick={handleResendOtp}
                className="text-sm font-medium text-slate-500 transition-colors hover:text-white disabled:cursor-not-allowed disabled:text-slate-700"
              >
                Gửi lại mã
              </button>
            </div>

            <button
              type="button"
              onClick={() => { setStep("form"); setOtpCode(""); setErrors({}); }}
              className="w-full py-2.5 text-sm font-medium text-slate-500 transition-colors hover:text-slate-300"
            >
              ← Quay lại
            </button>
          </div>
        )}
      </div>
    </>
  );
}
