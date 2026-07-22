const RESEND_API_KEY = process.env.RESEND_API_KEY || "";
const FROM_EMAIL = "VieShop <no-reply@kenjaku.online>";

function buildOtpHtml(otp: string, email: string): string {
  const maskedEmail = email.replace(
    /^(.{2})(.*)(@.*)$/,
    (_, a: string, b: string, c: string) =>
      a + "•".repeat(Math.min(b.length, 5)) + c,
  );

  return `
    <!DOCTYPE html>
    <html><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1.0">
    <title>Mã xác thực VieShop</title></head>
    <body style="margin:0;padding:0;background:#ffffff;font-family:-apple-system,'Segoe UI','Helvetica Neue',Arial,sans-serif;color:#1a1a1a">

      <div style="display:none;font-size:1px;color:#ffffff;line-height:1px;max-height:0;max-width:0;opacity:0;overflow:hidden">Mã xác thực của bạn: ${otp}. Có hiệu lực trong 5 phút.&#847;&zwnj;&nbsp;&#8199;&shy;&#847;&zwnj;&nbsp;&#8199;&shy;&#847;&zwnj;&nbsp;&#8199;&shy;</div>

      <table width="100%" cellpadding="0" cellspacing="0" border="0">
        <tr><td align="center" style="padding:48px 20px">
          <table width="100%" cellpadding="0" cellspacing="0" border="0" style="max-width:520px;text-align:left">
            <tr><td align="center" style="padding-bottom:16px">
              <img src="https://iili.io/BLcEH2p.png" alt="VieShop" width="240" style="display:block;margin:0 auto;height:auto" />
            </td></tr>
            <tr><td style="border:1px solid #ede9fe;border-radius:8px;padding:36px 32px">
              <p style="margin:0 0 8px;font-size:14px;color:#1a1a1a">Xác thực email của bạn</p>
              <p style="margin:0 0 28px;font-size:14px;color:#6b7280;line-height:1.5">Nhập mã bên dưới để xác nhận địa chỉ <strong style="color:#1a1a1a">${maskedEmail}</strong></p>
              <p style="margin:0 0 28px;text-align:center;font-size:40px;font-weight:800;letter-spacing:12px;color:#1a1a1a">${otp}</p>
              <p style="margin:0 0 16px;font-size:13px;color:#6b7280;line-height:1.5">Mã có hiệu lực trong <strong style="color:#1a1a1a">5 phút</strong> và chỉ được dùng một lần.</p>
              <p style="margin:0 0 24px;font-size:13px;color:#6b7280;line-height:1.5"><strong style="color:#1a1a1a">Không chia sẻ mã này với bất kỳ ai.</strong></p>
              <p style="margin:0 0 4px;font-size:13px;color:#6b7280">Trân trọng,</p>
              <p style="margin:0;font-size:13px;font-weight:700;color:#1a1a1a">VieShop Team</p>
            </td></tr>
            <tr><td style="padding:24px 0 0;text-align:center">
              <p style="margin:0;font-size:11px;color:#9ca3af;line-height:1.6">Bạn nhận email này vì có yêu cầu đăng ký tài khoản VieShop. Nếu không phải bạn, hãy bỏ qua email này.</p>
              <p style="margin:12px 0 0;font-size:11px;color:#d1d5db">© ${new Date().getFullYear()} VieShop · kenjaku.online</p>
            </td></tr>
          </table>
        </td></tr>
      </table>
    </body></html>
  `;
}

export async function sendOtpEmail(
  email: string,
  otp: string,
): Promise<{ success: boolean; error?: string }> {
  try {
    const res = await fetch("https://api.resend.com/emails", {
      method: "POST",
      headers: {
        Authorization: `Bearer ${RESEND_API_KEY}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        from: FROM_EMAIL,
        to: email,
        subject: "Mã xác thực VieShop",
        html: buildOtpHtml(otp, email),
      }),
    });

    if (!res.ok) {
      const data = await res.json().catch(() => ({}));
      return {
        success: false,
        error: (data as Record<string, string>).message || "Lỗi gửi email",
      };
    }

    return { success: true };
  } catch {
    return { success: false, error: "Không thể gửi email" };
  }
}
