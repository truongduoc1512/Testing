"use client";
import { useState, useRef, useEffect } from "react";
import { loginSchema } from "@/lib/validation/auth.schema";
import { parseZodErrors } from "@/lib/validation/parseErrors";
import type { SubmitState } from "@/types/auth";
import { Eye, EyeOff } from "lucide-react";
import { InputFloating } from "@/components/ui/InputFloating";
import { Button } from "@/components/ui/Button";
import { Toast } from "@/components/ui/Toast";

export default function LoginPage() {
  const [showPassword, setShowPassword] = useState(false);
  const [submitState, setSubmitState] = useState<SubmitState>("idle");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [showToast, setShowToast] = useState(false);

  const redirectTimer = useRef<ReturnType<typeof setTimeout> | undefined>(undefined);

  useEffect(() => () => clearTimeout(redirectTimer.current), []);

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const result = loginSchema.safeParse({ email, password });
    if (!result.success) {
      setErrors(parseZodErrors(result.error));
      return;
    }
    setErrors({});
    if (submitState === "loading") return;

    setSubmitState("loading");
    try {
      const res = await fetch("/api/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, password }),
      });
      const data = await res.json();

      if (!res.ok) {
        if (data.fields) {
          setErrors(data.fields);
        } else {
          setErrors({ email: data.error || "Đăng nhập thất bại" });
        }
        setSubmitState("idle");
        return;
      }

      setSubmitState("success");
      setShowToast(true);
      redirectTimer.current = setTimeout(() => {
        window.location.href = "/dashboard";
      }, 2000);
    } catch {
      setErrors({ email: "Lỗi mạng. Vui lòng thử lại." });
      setSubmitState("idle");
    }
  };

  return (
    <>
      {showToast && (
        <Toast
          message="Đăng nhập thành công. Đang chuyển đến dashboard..."
          type="success"
          duration={2000}
          onClose={() => setShowToast(false)}
        />
      )}

      <div className="glass-card animate-fade-up w-full max-w-md rounded-4xl p-6 sm:p-8">

        <div className="mb-4 text-center">
          <div className="mx-auto mb-2 h-16 w-16 overflow-hidden">
            <img
              src="/logo.svg"
              alt="VieShop"
              className="h-full w-full scale-[1.8] object-contain"
            />
          </div>
          <h2 className="mb-1 text-2xl font-bold tracking-tight text-white">
            Đăng nhập VieShop
          </h2>
          <p className="text-sm text-slate-400">Đăng nhập vào tài khoản của bạn</p>
        </div>

        <form className="space-y-4" onSubmit={handleSubmit} noValidate>
          <InputFloating
            id="email"
            label="Địa chỉ email"
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            error={errors.email}
          />

          <div>
            <div className="relative">
              <InputFloating
                id="password"
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
                {showPassword ? (
                  <EyeOff className="h-5 w-5" />
                ) : (
                  <Eye className="h-5 w-5" />
                )}
              </button>
            </div>
          </div>

          <Button
            id="submitBtn"
            type="submit"
            loading={submitState === "loading"}
            className="w-full"
          >
            Đăng nhập
          </Button>

          <p className="text-center text-sm text-slate-500">
            Chưa có tài khoản?{" "}
            <a
              href="/register"
              className="font-semibold text-violet-400 transition-colors hover:text-violet-300"
            >
              Đăng ký
            </a>
          </p>
        </form>
      </div>
    </>
  );
}
