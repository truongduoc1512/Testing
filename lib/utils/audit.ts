import type { StatusBadgeKind } from "@/lib/constants/status";

export function formatAuditStatus(status: string) {
  if (status === "success") return "Thành công";
  if (status === "failure") return "Thất bại";
  if (status === "partial") return "Một phần";
  if (status === "info") return "Thông tin";
  return status;
}

export function getAuditStatusBadgeKind(status: string): StatusBadgeKind {
  if (status === "success") return "active";
  if (status === "partial") return "needAction";
  if (status === "info") return "syncing";
  return "syncFailed";
}

export function formatAuditAction(action: string) {
  const labels: Record<string, string> = {
    "admin.update": "Sửa admin",
    "admin.delete": "Xóa admin",
    "auth.password.change": "Đổi mật khẩu",
    "backup.create": "Tạo backup",
    "member.invite": "Mời member",
    "member.invite.revoke": "Thu hồi lời mời",
    "member.remove": "Xóa member",
    "member.credit_limit.update": "Đổi giới hạn credit member",
    "profile.check": "Check profile",
    "profile.payment.close": "Đóng hồ sơ thanh toán",
    "profile.sharing.disable": "Tắt chia sẻ Google One",
    "profile.sharing.enable": "Bật chia sẻ Google One",
  };
  return labels[action] || action;
}
