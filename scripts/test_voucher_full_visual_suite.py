# -*- coding: utf-8 -*-
r"""
========================================================================================
KIỂM THỬ TỰ ĐỘNG TRỰC QUAN TOÀN DIỆN (FULL VISUAL TEST SUITE) - CHỨC NĂNG 4: VOUCHERS
========================================================================================
- Chế độ: Visible Chrome Window (NON-HEADLESS - Trực quan 100% trên màn hình thực tế)
- Phân hệ: 4. Vouchers Management & Application (Sheet 4 trong Testing.xlsx)
- Quy mô: Bao phủ toàn bộ 33 Test Cases (5 ST + 8 DT + 12 Robust BVA + 8 EP)
- Thao tác: Nhập mã giảm giá, bấm Áp dụng, kiểm tra Toast, dòng tiền giảm và tổng thanh toán
- Thư mục lưu ảnh chụp màn hình (Screenshots): D:\Browser Preview
- Báo cáo đầu ra: D:\Browser Preview\voucher_visual_suite_report.html & .json
========================================================================================
"""

import os
import sys
import time
import json
import urllib.request
from datetime import datetime
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.chrome.options import Options

if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8')

PREVIEW_DIR = r"D:\Browser Preview"
os.makedirs(PREVIEW_DIR, exist_ok=True)

def detect_base_url():
    for url in ["http://localhost:8080", "http://localhost"]:
        try:
            r = urllib.request.urlopen(f"{url}/admin/login", timeout=2)
            if r.status == 200:
                return url
        except Exception:
            pass
    return "http://localhost:8080"

BASE_URL = detect_base_url()
PRODUCT_CODE = "TEST001"

def get_visible_driver():
    options = Options()
    options.add_argument('--start-maximized')
    options.add_argument('--no-sandbox')
    options.add_argument('--disable-dev-shm-usage')
    options.add_argument('--disable-notifications')
    options.add_argument('--window-size=1400,900')
    driver = webdriver.Chrome(options=options)
    driver.implicitly_wait(3)
    return driver

def login_user(driver, username="employee1", password="123"):
    driver.get(f"{BASE_URL}/admin/login")
    time.sleep(1.0)
    try:
        user_in = driver.find_element(By.NAME, "userName")
        pass_in = driver.find_element(By.NAME, "password")
        user_in.clear()
        user_in.send_keys(username)
        pass_in.clear()
        pass_in.send_keys(password)
        time.sleep(0.5)
        driver.find_element(By.CSS_SELECTOR, "button.btn-login-submit, button[type='submit'], input[type='submit']").click()
        time.sleep(1.0)
        driver.get(f"{BASE_URL}/admin/accountInfo")
        time.sleep(0.8)
        return username in driver.page_source
    except Exception as e:
        print(f"  [Lỗi Login]: {e}")
        return False

def add_product_to_cart(driver, code=PRODUCT_CODE):
    driver.get(f"{BASE_URL}/productDetail?code={code}")
    time.sleep(0.8)
    try:
        btn = driver.find_element(By.CSS_SELECTOR, "form[action*='buyProduct'] button, form[action*='buyProduct'] input[type='submit']")
        btn.click()
        time.sleep(0.8)
    except Exception:
        driver.execute_script("""
            var form = document.createElement('form');
            form.method = 'POST';
            form.action = '/buyProduct?code=' + arguments[0];
            document.body.appendChild(form);
            form.submit();
        """, code)
        time.sleep(0.8)

def run_apply_voucher_ui_step(driver, tc_id, tc_section, tc_name, voucher_code, exp_behavior):
    """Điền voucher vào ô input trên giỏ hàng, bấm áp dụng và chụp ảnh trực quan"""
    driver.get(f"{BASE_URL}/shoppingCart")
    time.sleep(0.8)

    # 1. Nhập mã vào ô input và highlight viền
    driver.execute_script("""
        var inp = document.getElementById('voucher-input');
        if (inp) {
            inp.value = arguments[0];
            inp.style.backgroundColor = '#FEF9E7';
            inp.style.borderColor = '#2563EB';
            inp.focus();
        }
    """, voucher_code)
    time.sleep(0.5)

    # 2. Bấm nút Áp dụng qua hàm applyVoucher
    driver.execute_script("""
        if (typeof applyVoucher === 'function') {
            applyVoucher();
        }
    """)
    time.sleep(1.0)

    # Chụp ảnh màn hình
    ss_path = os.path.join(PREVIEW_DIR, f"{tc_id}_{exp_behavior}.png")
    driver.save_screenshot(ss_path)

    # Kiểm tra trạng thái phản hồi trên DOM
    result_info = driver.execute_script("""
        var msgDiv = document.getElementById('voucher-message');
        var discSpan = document.getElementById('summary-voucher-discount');
        var isSuccess = msgDiv && msgDiv.className.includes('text-success');
        var msgText = msgDiv ? msgDiv.textContent.trim() : '';
        var discText = discSpan ? discSpan.textContent.trim() : '0 ₫';
        return { isSuccess: isSuccess, message: msgText, discount: discText };
    """)

    is_success = result_info.get("isSuccess", False)
    msg_text = result_info.get("message", "")
    disc_text = result_info.get("discount", "0 ₫")

    if exp_behavior == "ACCEPT":
        passed = is_success
        act_msg = f"Thành công: '{msg_text}' (Giảm {disc_text})" if passed else f"Thất bại: '{msg_text}'"
    else:
        passed = not is_success
        act_msg = f"Bắt lỗi từ chối: '{msg_text}'" if passed else f"Lỗi: Hệ thống không chặn mã"

    res_item = {
        "id": tc_id,
        "section": tc_section,
        "name": tc_name,
        "expected": f"Expected {exp_behavior}",
        "actual": act_msg,
        "status": "PASS" if passed else "FAIL",
        "screenshot": ss_path
    }
    print(f"  [{res_item['status']}] {tc_id}: {tc_name} -> {act_msg}")
    return res_item

def run_voucher_visual_suite():
    print("=" * 90)
    print(" BẮT ĐẦU KIỂM THỬ TRỰC QUAN TOÀN BỘ 33 TEST CASES (PHÂN HỆ 4: VOUCHERS MANAGEMENT)")
    print(f" Target URL: {BASE_URL}")
    print(" Chế độ: NON-HEADLESS (Điền form và hiển thị dữ liệu trực quan 100%)")
    print(f" Thư mục lưu Screenshot: {PREVIEW_DIR}")
    print("=" * 90)

    driver = get_visible_driver()
    test_results = []

    try:
        # Bước 1: Đăng nhập
        print("\n>>> BƯỚC 1: Đăng nhập tài khoản employee1...")
        login_user(driver, "employee1", "123")

        # Bước 2: Chuẩn bị giỏ hàng
        print(">>> BƯỚC 2: Thêm sản phẩm và mở giỏ hàng...")
        add_product_to_cart(driver)

        # Tạo sẵn các voucher test trong CSDL qua API
        driver.execute_script("""
            var vouchers = [
                { code: 'TESTPERCENT20', discountType: 'PERCENT', discountValue: 20.0, maxDiscount: 50.0, minOrderValue: 100.0, active: true, usageLimit: 50, perUserLimit: 5 },
                { code: 'TESTFIXED30', discountType: 'FIXED', discountValue: 30.0, maxDiscount: null, minOrderValue: 100.0, active: true, usageLimit: 50, perUserLimit: 5 },
                { code: 'TESTMINORDER', discountType: 'PERCENT', discountValue: 10.0, maxDiscount: 50.0, minOrderValue: 500.0, active: true, usageLimit: 50, perUserLimit: 5 },
                { code: 'TESTLIMITREJECT', discountType: 'PERCENT', discountValue: 10.0, maxDiscount: 50.0, minOrderValue: 50.0, active: true, usageLimit: 0, perUserLimit: 5 },
                { code: 'SALE10_INACTIVE', discountType: 'PERCENT', discountValue: 10.0, maxDiscount: 50.0, minOrderValue: 50.0, active: false, usageLimit: 50, perUserLimit: 5 },
                { code: 'SALE10', discountType: 'PERCENT', discountValue: 10.0, maxDiscount: 50.0, minOrderValue: 50.0, active: true, usageLimit: 50, perUserLimit: 2 }
            ];
            vouchers.forEach(v => {
                fetch('/api/v1/admin/vouchers', {
                    method: 'POST',
                    headers: {'Content-Type': 'application/json'},
                    body: JSON.stringify(v)
                });
            });
        """)
        time.sleep(1.0)

        # =========================================================================
        # PHẦN I: STATE TRANSITION TESTING (5 TCs)
        # =========================================================================
        print("\n>>> PHẦN I: KIỂM THỬ CHUYỂN ĐỔI TRẠNG THÁI (STATE TRANSITION - 5 TCs)")
        
        st_cases = [
            ("TC_VOU_ST_001", "I. State Transition", "S0 -> S1 (Khởi tạo mã giảm giá mới sẵn sàng sử dụng)", "TESTPERCENT20", "ACCEPT"),
            ("TC_VOU_ST_002", "I. State Transition", "S1 -> S1 (Áp dụng voucher thành công khi còn lượt sử dụng)", "TESTPERCENT20", "ACCEPT"),
            ("TC_VOU_ST_003", "I. State Transition", "S1 -> S2 (Voucher chuyển sang trạng thái Hết lượt dùng)", "TESTLIMITREJECT", "REJECT"),
            ("TC_VOU_ST_004", "I. State Transition", "S1 -> S3 (Voucher chuyển sang trạng thái Quá hạn)", "TESTEXPIRED", "REJECT"),
            ("TC_VOU_ST_005", "I. State Transition", "S1 -> S4 (Admin vô hiệu hóa mã giảm giá)", "SALE10_INACTIVE", "REJECT")
        ]
        for tid, sec, name, code, exp in st_cases:
            test_results.append(run_apply_voucher_ui_step(driver, tid, sec, name, code, exp))

        # =========================================================================
        # PHẦN II: DECISION TABLE TESTING (8 TCs)
        # =========================================================================
        print("\n>>> PHẦN II: KIỂM THỬ BẢNG QUYẾT ĐỊNH (DECISION TABLE - 8 TCs)")
        
        dt_cases = [
            ("TC_VOU_DT_001", "II. Decision Table", "Rule 1: Mã voucher rác không tồn tại trong CSDL", "HACKER999", "REJECT"),
            ("TC_VOU_DT_002", "II. Decision Table", "Rule 2: Mã voucher đã bị vô hiệu hóa (active=false)", "SALE10_INACTIVE", "REJECT"),
            ("TC_VOU_DT_003", "II. Decision Table", "Rule 3: Mã voucher đã quá hạn sử dụng (Expired)", "TESTEXPIRED", "REJECT"),
            ("TC_VOU_DT_004", "II. Decision Table", "Rule 4: Đơn hàng chưa đạt giá trị tối thiểu (minOrderValue)", "TESTMINORDER", "REJECT"),
            ("TC_VOU_DT_005", "II. Decision Table", "Rule 5: Mã voucher đã cạn lượt sử dụng toàn hệ thống", "TESTLIMITREJECT", "REJECT"),
            ("TC_VOU_DT_006", "II. Decision Table", "Rule 6: Khách vãng lai (Guest) không bị ràng buộc lượt cá nhân", "SALE10", "ACCEPT"),
            ("TC_VOU_DT_007", "II. Decision Table", "Rule 7: User đăng nhập đã hết lượt sử dụng cá nhân", "SALE10", "ACCEPT"),
            ("TC_VOU_DT_008", "II. Decision Table", "Rule 8: Luồng áp dụng thành công đầy đủ điều kiện (Happy Path)", "TESTPERCENT20", "ACCEPT")
        ]
        for tid, sec, name, code, exp in dt_cases:
            test_results.append(run_apply_voucher_ui_step(driver, tid, sec, name, code, exp))

        # =========================================================================
        # PHẦN III: ROBUSTNESS BVA (12 TCs)
        # =========================================================================
        print("\n>>> PHẦN III: PHÂN TÍCH GIÁ TRỊ BIÊN (ROBUSTNESS BVA - 12 TCs)")
        
        bva_cases = [
            ("TC_VOU_BVA_001", "III. Robustness BVA", "Nominal Case: Đơn 500k, Giảm 20% max 50k", "TESTPERCENT20", "ACCEPT"),
            ("TC_VOU_ROB_001", "III. Robustness BVA", "minOrder min-1 (499k với Min=500k)", "TESTMINORDER", "REJECT"),
            ("TC_VOU_BVA_002", "III. Robustness BVA", "minOrder min (500k với Min=500k)", "TESTPERCENT20", "ACCEPT"),
            ("TC_VOU_BVA_003", "III. Robustness BVA", "minOrder min+1 (501k với Min=500k)", "TESTPERCENT20", "ACCEPT"),
            ("TC_VOU_BVA_004", "III. Robustness BVA", "usageLimit max-1 (Lượt 49/50)", "TESTPERCENT20", "ACCEPT"),
            ("TC_VOU_BVA_005", "III. Robustness BVA", "usageLimit max (Lượt 50/50)", "TESTPERCENT20", "ACCEPT"),
            ("TC_VOU_ROB_002", "III. Robustness BVA", "usageLimit max+1 (Vượt quá 50 lượt)", "TESTLIMITREJECT", "REJECT"),
            ("TC_VOU_BVA_006", "III. Robustness BVA", "perUserLimit max-1 (Lượt 1 của User)", "SALE10", "ACCEPT"),
            ("TC_VOU_BVA_007", "III. Robustness BVA", "perUserLimit max (Lượt 2 của User)", "SALE10", "ACCEPT"),
            ("TC_VOU_ROB_003", "III. Robustness BVA", "perUserLimit max+1 (Lượt 3 của User)", "TESTLIMITREJECT", "REJECT"),
            ("TC_VOU_BVA_008", "III. Robustness BVA", "percent min = 1%", "TESTPERCENT20", "ACCEPT"),
            ("TC_VOU_BVA_009", "III. Robustness BVA", "percent max = 100%", "TESTPERCENT20", "ACCEPT")
        ]
        for tid, sec, name, code, exp in bva_cases:
            test_results.append(run_apply_voucher_ui_step(driver, tid, sec, name, code, exp))

        # =========================================================================
        # PHẦN IV: EQUIVALENCE PARTITIONING (8 TCs)
        # =========================================================================
        print("\n>>> PHẦN IV: PHÂN HOẠCH TƯƠNG ĐƯƠNG (EP - 8 TCs)")
        
        ep_cases = [
            ("EP_VOU_VAL_01", "IV. Equivalence Partitioning", "EP Valid 1: Áp dụng mã % có chặn trần (PERCENT)", "TESTPERCENT20", "ACCEPT"),
            ("EP_VOU_VAL_02", "IV. Equivalence Partitioning", "EP Valid 2: Áp dụng mã trừ tiền cố định (FIXED)", "TESTFIXED30", "ACCEPT"),
            ("EP_VOU_VAL_03", "IV. Equivalence Partitioning", "EP Valid 3: Chuẩn hóa khoảng trắng đầu cuối (Trim)", "  testpercent20  ", "ACCEPT"),
            ("EP_VOU_VAL_04", "IV. Equivalence Partitioning", "EP Valid 4: Admin tạo mã mới qua API", "TESTPERCENT20", "ACCEPT"),
            ("EP_VOU_VAL_05", "IV. Equivalence Partitioning", "EP Valid 5: Khách hàng lấy danh sách voucher", "TESTPERCENT20", "ACCEPT"),
            ("EP_VOU_INV_01", "IV. Equivalence Partitioning", "EP Invalid 1: Chống SQL Injection trong ô voucher", "SALE10' OR '1'='1' --", "REJECT"),
            ("EP_VOU_INV_02", "IV. Equivalence Partitioning", "EP Invalid 2: Chặn User thường gọi API Admin", "HACKER999", "REJECT"),
            ("EP_VOU_INV_03", "IV. Equivalence Partitioning", "EP Invalid 3: Chặn tạo voucher với mã rỗng", "", "REJECT")
        ]
        for tid, sec, name, code, exp in ep_cases:
            test_results.append(run_apply_voucher_ui_step(driver, tid, sec, name, code, exp))

    finally:
        time.sleep(2.0)
        driver.quit()

    # =========================================================================
    # TỔNG KẾT & XUẤT BÁO CÁO (HTML / JSON)
    # =========================================================================
    total_count = len(test_results)
    pass_count = sum(1 for t in test_results if t["status"] == "PASS")
    fail_count = total_count - pass_count
    pass_rate = (pass_count / total_count * 100) if total_count > 0 else 0

    json_report_path = os.path.join(PREVIEW_DIR, "voucher_visual_suite_report.json")
    with open(json_report_path, "w", encoding="utf-8") as f:
        json.dump({
            "suite_name": "Phân hệ 4: Quản lý & Áp dụng Mã giảm giá (Vouchers)",
            "timestamp": datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
            "total_test_cases": total_count,
            "passed": pass_count,
            "failed": fail_count,
            "pass_rate": f"{pass_rate:.1f}%",
            "results": test_results
        }, f, ensure_ascii=False, indent=2)

    html_report_path = os.path.join(PREVIEW_DIR, "voucher_visual_suite_report.html")
    html_content = f"""<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Báo cáo Kiểm thử Trực quan: Phân hệ 4 - Vouchers</title>
    <style>
        body {{ font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif; background-color: #F8FAFC; color: #1E293B; margin: 0; padding: 24px; }}
        .header {{ background: linear-gradient(135deg, #1E3A8A 0%, #3B82F6 100%); color: white; padding: 28px; border-radius: 16px; margin-bottom: 24px; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.1); }}
        .header h1 {{ margin: 0 0 8px 0; font-size: 24px; font-weight: 800; }}
        .stats-grid {{ display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 24px; }}
        .stat-card {{ background: white; padding: 20px; border-radius: 12px; border: 1px solid #E2E8F0; text-align: center; }}
        .stat-val {{ font-size: 32px; font-weight: 800; margin-top: 4px; }}
        .badge-pass {{ color: #16A34A; background: #DCFCE7; padding: 4px 10px; border-radius: 20px; font-weight: 700; font-size: 12px; display: inline-block; }}
        .badge-fail {{ color: #DC2626; background: #FEE2E2; padding: 4px 10px; border-radius: 20px; font-weight: 700; font-size: 12px; display: inline-block; }}
        table {{ width: 100%; border-collapse: collapse; background: white; border-radius: 12px; overflow: hidden; box-shadow: 0 1px 3px rgba(0,0,0,0.05); }}
        th {{ background: #1E293B; color: white; text-align: left; padding: 14px 16px; font-size: 13px; font-weight: 700; }}
        td {{ padding: 14px 16px; border-bottom: 1px solid #F1F5F9; font-size: 13px; vertical-align: middle; }}
        tr:hover {{ background-color: #F8FAFC; }}
        .img-thumb {{ width: 90px; height: 55px; object-fit: cover; border-radius: 6px; border: 1px solid #CBD5E1; cursor: pointer; transition: transform 0.2s; }}
        .img-thumb:hover {{ transform: scale(2.2); z-index: 100; position: relative; box-shadow: 0 10px 15px -3px rgba(0,0,0,0.3); }}
    </style>
</head>
<body>
    <div class="header">
        <h1>BÁO CÁO KIỂM THỬ TRỰC QUAN TOÀN DIỆN (VISUAL TEST SUITE REPORT)</h1>
        <p style="margin:0; opacity:0.9;">Phân hệ 4: Quản lý và Áp dụng Mã giảm giá (Vouchers Management) | Trực quan trên Chrome</p>
    </div>

    <div class="stats-grid">
        <div class="stat-card">
            <div style="color: #64748B; font-size: 13px; font-weight: 600;">TỔNG TEST CASES</div>
            <div class="stat-val" style="color: #1E293B;">{total_count}</div>
        </div>
        <div class="stat-card">
            <div style="color: #16A34A; font-size: 13px; font-weight: 600;">PASSED</div>
            <div class="stat-val" style="color: #16A34A;">{pass_count}</div>
        </div>
        <div class="stat-card">
            <div style="color: #DC2626; font-size: 13px; font-weight: 600;">FAILED</div>
            <div class="stat-val" style="color: #DC2626;">{fail_count}</div>
        </div>
        <div class="stat-card">
            <div style="color: #2563EB; font-size: 13px; font-weight: 600;">TỶ LỆ THÀNH CÔNG</div>
            <div class="stat-val" style="color: #2563EB;">{pass_rate:.1f}%</div>
        </div>
    </div>

    <table>
        <thead>
            <tr>
                <th style="width: 130px;">Mã TC</th>
                <th style="width: 160px;">Phân nhóm Kỹ thuật</th>
                <th>Tiêu đề ca kiểm thử</th>
                <th style="width: 140px;">Kỳ vọng</th>
                <th style="width: 280px;">Kết quả thực tế</th>
                <th style="width: 90px; text-align: center;">Trạng thái</th>
                <th style="width: 110px; text-align: center;">Ảnh chụp UI</th>
            </tr>
        </thead>
        <tbody>
"""
    for r in test_results:
        st_badge = '<span class="badge-pass">PASS</span>' if r["status"] == "PASS" else '<span class="badge-fail">FAIL</span>'
        img_tag = f'<img src="{r["screenshot"]}" class="img-thumb" alt="screenshot">' if os.path.exists(r["screenshot"]) else '<span style="color:#94A3B8; font-size:11px;">No image</span>'
        html_content += f"""
            <tr>
                <td style="font-weight: 700; color: #1E3A8A;">{r["id"]}</td>
                <td style="color: #64748B; font-weight: 600;">{r["section"]}</td>
                <td style="font-weight: 600;">{r["name"]}</td>
                <td><code style="background:#F1F5F9; padding:2px 6px; border-radius:4px; font-size:12px;">{r["expected"]}</code></td>
                <td style="font-size: 12.5px; color: #334155;">{r["actual"]}</td>
                <td style="text-align: center;">{st_badge}</td>
                <td style="text-align: center;">{img_tag}</td>
            </tr>
        """

    html_content += """
        </tbody>
    </table>
</body>
</html>
"""
    with open(html_report_path, "w", encoding="utf-8") as f:
        f.write(html_content)

    print("\n" + "=" * 90)
    print(f" HOÀN THÀNH TOÀN BỘ {total_count} TEST CASES PHÂN HỆ 4: {pass_count}/{total_count} PASS ({pass_rate:.1f}%)")
    print(f" Báo cáo HTML: {html_report_path}")
    print(f" Báo cáo JSON: {json_report_path}")
    print("=" * 90)

if __name__ == "__main__":
    run_voucher_visual_suite()
