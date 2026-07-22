"use client";
import { useState } from "react";
import { motion } from "framer-motion";
import {
  X,
  Mail,
  User,
  KeyRound,
  Shield,
  Coins,
  HardDrive,
  StickyNote,
  Plus,
} from "lucide-react";
import { FAMILY_TYPES } from "@/lib/constants";

interface AddAdminModalProps {
  onClose: () => void;
  onSubmit: (data: AdminFormData) => void;
  initialData?: Partial<AdminFormData>;
  mode?: "add" | "edit";
  forcedFamilyType?: string;
}

export interface AdminFormData {
  email: string;
  displayName: string;
  googlePassword: string;
  totpSecret: string;
  monthlyCredit: number;
  storageTB: number;
  familyType: string;
  note: string;
}

export default function AddAdminModal({
  onClose,
  onSubmit,
  initialData,
  mode = "add",
  forcedFamilyType,
}: AddAdminModalProps) {
  const resolvedFamilyType = forcedFamilyType || initialData?.familyType || "ultra";
  const [form, setForm] = useState<AdminFormData>({
    email: initialData?.email || "",
    displayName: initialData?.displayName || "",
    googlePassword: initialData?.googlePassword || "",
    totpSecret: initialData?.totpSecret || "",
    monthlyCredit: initialData?.monthlyCredit ?? 25000,
    storageTB: initialData?.storageTB ?? 30,
    familyType: resolvedFamilyType,
    note: initialData?.note || "",
  });

  const update = (field: keyof AdminFormData, value: string | number) => {
    setForm((prev) => ({ ...prev, [field]: value }));
  };

  const platformName =
    form.familyType === "gpt"
      ? "ChatGPT"
      : form.familyType === "youtube"
        ? "YouTube"
        : "Google";

  const handleSubmit = () => {
    if (!form.email || !form.displayName) return;
    if (mode === "edit") {
      const payload: Partial<AdminFormData> & { email: string; displayName: string } = {
        ...form,
      };
      if (!payload.googlePassword) delete payload.googlePassword;
      if (!payload.totpSecret) delete payload.totpSecret;
      onSubmit(payload as AdminFormData);
    } else {
      onSubmit(form);
    }
  };

  return (
    <div className="fixed inset-0 z-[100] flex items-center justify-center">

      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        exit={{ opacity: 0 }}
        className="absolute inset-0 bg-black/60 backdrop-blur-sm"
        onClick={onClose}
      />

      <motion.div
        initial={{ opacity: 0, scale: 0.95, y: 20 }}
        animate={{ opacity: 1, scale: 1, y: 0 }}
        exit={{ opacity: 0, scale: 0.95, y: 20 }}
        transition={{ duration: 0.25, ease: "easeOut" }}
        className="relative z-10 mx-4 max-h-[85vh] w-full max-w-lg overflow-y-auto rounded-2xl border border-white/[0.08] bg-[#0f1320]/95 p-6 shadow-2xl backdrop-blur-2xl"
      >

        <div className="mb-6 flex items-center justify-between">
          <h2 className="text-xl font-bold text-white">
            {mode === "edit" ? "Sửa Acc Farm" : "Thêm Acc Farm"}
          </h2>
          <button
            onClick={onClose}
            className="rounded-lg p-1.5 text-slate-400 transition-colors hover:bg-white/5 hover:text-white"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        <div className="space-y-5">

          {!forcedFamilyType && (
            <Field
              icon={<span className="h-4 w-4">👑</span>}
              label="Loại Family"
              color="text-pink-400"
            >
              <div className="grid grid-cols-2 gap-2">
                {FAMILY_TYPES.map((opt) => {
                  const Icon = opt.icon;
                  const active = form.familyType === opt.id;
                  return (
                    <button
                      key={opt.id}
                      type="button"
                      onClick={() => update("familyType", opt.id)}
                      className={`flex items-center gap-2 rounded-xl border px-3 py-2.5 text-sm font-medium transition-all ${
                        active
                          ? `${opt.bg} ${opt.color} ring-1 ring-white/20`
                          : "border-white/[0.06] bg-white/[0.02] text-slate-400 hover:bg-white/[0.05]"
                      }`}
                    >
                      <Icon className="h-4 w-4" />
                      {opt.label}
                    </button>
                  );
                })}
              </div>
            </Field>
          )}

          <Field
            icon={<Mail className="h-4 w-4" />}
            label={`Email ${platformName}`}
            color="text-violet-400"
          >
            <input
              type="email"
              placeholder="example@gmail.com"
              value={form.email}
              onChange={(e) => update("email", e.target.value)}
              className="form-input"
            />
          </Field>

          <Field
            icon={<User className="h-4 w-4" />}
            label="Tên hiển thị"
            color="text-blue-400"
          >
            <input
              type="text"
              placeholder="VD: Account 1"
              value={form.displayName}
              onChange={(e) => update("displayName", e.target.value)}
              className="form-input"
            />
          </Field>

          <Field
            icon={<KeyRound className="h-4 w-4" />}
            label={`Mật khẩu ${platformName}`}
            color="text-amber-400"
          >
            <input
              type="password"
              placeholder={`Mật khẩu đăng nhập ${platformName}`}
              value={form.googlePassword}
              onChange={(e) => update("googlePassword", e.target.value)}
              className="form-input"
            />
            <p className="mt-1.5 text-xs text-slate-500">
              Dùng để auto-sync credit/storage (mã hóa AES-256)
            </p>
          </Field>

          <Field
            icon={<Shield className="h-4 w-4" />}
            label="TOTP Secret (2FA)"
            color="text-cyan-400"
          >
            <input
              type="text"
              placeholder="VD: w7ek jhba nrx5 yqfz oonb dnbb d2bq xbrs"
              value={form.totpSecret}
              onChange={(e) => update("totpSecret", e.target.value)}
              className="form-input"
            />
            <p className="mt-1.5 text-xs text-slate-500">Mã secret 2FA (tuỳ chọn)</p>
          </Field>

          {form.familyType === "ultra" && (
            <Field
              icon={<Coins className="h-4 w-4" />}
              label="Credit hàng tháng"
              color="text-emerald-400"
            >
              <input
                type="number"
                value={form.monthlyCredit}
                onChange={(e) => update("monthlyCredit", Number(e.target.value))}
                className="form-input"
              />
            </Field>
          )}

          {form.familyType === "ultra" && (
            <Field
              icon={<HardDrive className="h-4 w-4" />}
              label="Storage (TB)"
              color="text-orange-400"
            >
              <input
                type="number"
                value={form.storageTB}
                onChange={(e) => update("storageTB", Number(e.target.value))}
                className="form-input"
              />
            </Field>
          )}

          <Field
            icon={<StickyNote className="h-4 w-4" />}
            label="Ghi chú"
            color="text-blue-400"
          >
            <input
              type="text"
              placeholder="Ghi chú tuỳ chọn..."
              value={form.note}
              onChange={(e) => update("note", e.target.value)}
              className="form-input"
            />
          </Field>
        </div>

        <div className="mt-8 flex items-center justify-end gap-3">
          <button
            onClick={onClose}
            className="rounded-xl px-5 py-2.5 text-sm font-medium text-slate-400 transition-colors hover:bg-white/5 hover:text-white"
          >
            Huỷ
          </button>
          <button
            onClick={handleSubmit}
            className="flex items-center gap-1.5 rounded-xl bg-gradient-to-r from-violet-500 to-cyan-500 px-5 py-2.5 text-sm font-semibold text-white transition-opacity hover:opacity-90"
          >
            {mode === "edit" ? (
              "💾 Lưu"
            ) : (
              <>
                <Plus className="h-4 w-4" /> Thêm
              </>
            )}
          </button>
        </div>
      </motion.div>
    </div>
  );
}

function Field({
  icon,
  label,
  color,
  children,
}: {
  icon: React.ReactNode;
  label: string;
  color: string;
  children: React.ReactNode;
}) {
  return (
    <div>
      <div className={`mb-1.5 flex items-center gap-2 text-sm font-semibold ${color}`}>
        {icon}
        {label}
      </div>
      {children}
    </div>
  );
}
