"use client";
import { useState, useRef, useEffect, useCallback } from "react";

type PasswordStatus = "idle" | "loading" | "success" | "error";
type PasswordField = "oldPass" | "newPass" | "confirmPass";
type VisibilityField = "old" | "new" | "confirm";

interface PasswordForm {
  oldPass: string;
  newPass: string;
  confirmPass: string;
}

interface PasswordVisibility {
  old: boolean;
  new: boolean;
  confirm: boolean;
}

export function useChangePassword() {
  const [form, setForm] = useState<PasswordForm>({
    oldPass: "",
    newPass: "",
    confirmPass: "",
  });
  const [show, setShow] = useState<PasswordVisibility>({
    old: false,
    new: false,
    confirm: false,
  });
  const [status, setStatus] = useState<PasswordStatus>("idle");
  const [message, setMessage] = useState("");
  const timerRef = useRef<ReturnType<typeof setTimeout> | undefined>(undefined);

  useEffect(() => {
    return () => clearTimeout(timerRef.current);
  }, []);

  const setField = useCallback((field: PasswordField, value: string) => {
    setForm((prev) => ({ ...prev, [field]: value }));
  }, []);

  const toggleVisibility = useCallback((field: VisibilityField) => {
    setShow((prev) => ({ ...prev, [field]: !prev[field] }));
  }, []);

  const submit = useCallback(async () => {
    if (!form.oldPass || !form.newPass || !form.confirmPass) return;

    if (form.newPass !== form.confirmPass) {
      setStatus("error");
      setMessage("Mật khẩu xác nhận không khớp");
      return;
    }

    setStatus("loading");
    try {
      const res = await fetch("/api/auth/change-password", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          oldPassword: form.oldPass,
          newPassword: form.newPass,
          confirmPassword: form.confirmPass,
        }),
      });
      const data = await res.json();
      if (!res.ok) {
        setStatus("error");
        setMessage(data.fields?.oldPassword || data.error || "Đổi mật khẩu thất bại");
        return;
      }

      setStatus("success");
      setMessage("Đổi mật khẩu thành công!");
      setForm({ oldPass: "", newPass: "", confirmPass: "" });
      clearTimeout(timerRef.current);
      timerRef.current = setTimeout(() => setStatus("idle"), 3000);
    } catch (error) {
      void error;
      setStatus("error");
      setMessage("Lỗi mạng, vui lòng thử lại");
    }
  }, [form]);

  return {
    form,
    show,
    status,
    message,
    setField,
    toggleVisibility,
    submit,
  };
}
