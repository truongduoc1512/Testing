import { z } from "zod";

const GMAIL_REGEX = /^[a-zA-Z0-9._%+-]+@gmail\.com$/i;

export const sendOtpSchema = z.object({
  email: z
    .string()
    .min(1, "Vui lòng nhập email")
    .email("Email không hợp lệ")
    .refine((email) => GMAIL_REGEX.test(email), "Chỉ chấp nhận địa chỉ Gmail"),
});

export const verifyOtpSchema = z.object({
  email: z
    .string()
    .min(1, "Vui lòng nhập email")
    .email("Email không hợp lệ"),
  code: z
    .string()
    .length(6, "Mã OTP phải có 6 chữ số")
    .regex(/^\d{6}$/, "Mã OTP chỉ chứa số"),
});

export const registerSchema = z.object({
  email: z
    .string()
    .min(1, "Vui lòng nhập email")
    .email("Email không hợp lệ")
    .refine((email) => GMAIL_REGEX.test(email), "Chỉ chấp nhận địa chỉ Gmail"),
  fullName: z
    .string()
    .trim()
    .min(2, "Tên tối thiểu 2 ký tự")
    .max(50, "Tên tối đa 50 ký tự"),
  password: z
    .string()
    .min(8, "Mật khẩu tối thiểu 8 ký tự")
    .max(128, "Mật khẩu quá dài"),
  token: z
    .string()
    .min(1, "Thiếu token xác minh"),
});
