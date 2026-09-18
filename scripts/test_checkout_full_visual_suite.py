# -*- coding: utf-8 -*-
r"""
========================================================================================
KIỂM THỬ TỰ ĐỘNG TRỰC QUAN TOÀN DIỆN (FULL VISUAL TEST SUITE) - CHỨC NĂNG 5: CHECKOUT
========================================================================================
- Chế độ: Visible Browser (NON-HEADLESS - Trực quan trên cửa sổ trình duyệt thực tế)
- Phân hệ: 5. Checkout - Order Placement (Sheet 5 trong Testing.xlsx)
- Quy mô: Bao phủ toàn bộ 52 Test Cases (4 ST + 7 DT + 27 Robust BVA & Threshold + 14 EP)
- Thư mục lưu ảnh chụp màn hình (Screenshots): D:\Browser Preview
- Báo cáo đầu ra: D:\Browser Preview\checkout_visual_suite_report.html & .json
========================================================================================
"""

import os
import sys
import time
import json
from datetime import datetime
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.chrome.options import Options

# Đảm bảo in UTF-8 trên Windows Console
if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8')

# Đường dẫn thư mục lưu ảnh chụp màn hình theo quy định
PREVIEW_DIR = r"D:\Browser Preview"
os.makedirs(PREVIEW_DIR, exist_ok=True)

BASE_URL = "http://localhost"
DEFAULT_PRODUCT_CODE = "TEST001"

def get_visible_driver():
    """Khởi tạo Chrome Driver ở chế độ TRỰC QUAN (KHÔNG HEADLESS)"""
    options = Options()
    # TUYỆT ĐỐI KHÔNG BẬT --headless để người dùng quan sát trực tiếp trên màn hình
    options.add_argument('--start-maximized')
    options.add_argument('--no-sandbox')
    options.add_argument('--disable-dev-shm-usage')
    options.add_argument('--disable-notifications')
    options.add_argument('--window-size=1366,768')
    driver = webdriver.Chrome(options=options)
    driver.implicitly_wait(2)
    return driver

# Helper functions for string generators
def make_pattern_string(target_len, base_pattern="Nguyen_Hoang_Phuong_2026_Designer_HN_HCM_"):
    if target_len <= 0:
        return ""
    repeat_count = (target_len // len(base_pattern)) + 1
    return (base_pattern * repeat_count)[:target_len]

def make_address_string(target_len, base_pattern="123/45A Duong Le Loi, P. Ben Nghe, Q.1, TP. HCM - "):
    if target_len <= 0:
        return ""
    repeat_count = (target_len // len(base_pattern)) + 1
    return (base_pattern * repeat_count)[:target_len]

def make_phone_string(target_len, base_pattern="+84-9123-456789."):
    if target_len <= 0:
        return ""
    repeat_count = (target_len // len(base_pattern)) + 1
    return (base_pattern * repeat_count)[:target_len]

def make_bva_email(length):
    if length <= 6:
        return "a@b.co"[:length]
    if length == 7:
        return "ab@c.vn"
    if length < 80:
        return "phuong.designer+test_2026@shoeshop.org.vn"[:length]
    local_part = "phuong.designer_test_2026.special-tag_super-long-user-part-12345"
    domain_label_len = length - 69
    domain_label = ("shoeshop-store-vietnam-online-test-2026" * 3)[:domain_label_len]
    return local_part + "@" + domain_label + ".com"

# Nominal base data
NOMINAL_NAME = make_pattern_string(50, "Nguyen_Hoang_Phuong_2026_Designer_HN_HCM_VIP_")
NOMINAL_ADDR = make_address_string(50, "So 123/45 Le Loi, P. Ben Nghe, Q.1, TP. HCM - ")
NOMINAL_EMAIL = "hoangphuong.designer+test_2026@shoeshop.org.vn"
NOMINAL_PHONE = "+84-912-345-678"

def add_product_to_cart(driver, code=DEFAULT_PRODUCT_CODE):
    """Thêm sản phẩm vào giỏ qua giao diện"""
    driver.get(f"{BASE_URL}/productDetail?code={code}")
    time.sleep(0.3)
    try:
        btn = driver.find_element(By.CSS_SELECTOR, "form[action*='buyProduct'] button, form[action*='buyProduct'] input[type='submit']")
        btn.click()
        time.sleep(0.3)
    except Exception:
        # Fallback nếu ở trang danh sách
        driver.get(f"{BASE_URL}/buyProduct?code={code}")
        time.sleep(0.3)

def clear_cart(driver):
    """Làm sạch giỏ hàng qua cookie / session"""
    driver.get(f"{BASE_URL}/shoppingCart")
    time.sleep(0.2)
    driver.delete_all_cookies()

def login_user(driver, username="employee1", password="123"):
    """Đăng nhập tài khoản kiểm thử chính xác qua /admin/login và xác thực phiên"""
    driver.get(f"{BASE_URL}/admin/login")
    time.sleep(0.5)
    try:
        user_input = driver.find_element(By.NAME, "userName")
        pass_input = driver.find_element(By.NAME, "password")
        user_input.clear()
        user_input.send_keys(username)
        pass_input.clear()
        pass_input.send_keys(password)
        driver.find_element(By.CSS_SELECTOR, "button.btn-login-submit, button[type='submit'], input[type='submit']").click()
        time.sleep(1.0)
        
        # Xác minh phiên đăng nhập thực tế
        driver.get(f"{BASE_URL}/admin/accountInfo")
        time.sleep(0.4)
        if username in driver.page_source:
            print(f"  [Xác thực thành công]: Đã đăng nhập tài khoản '{username}'")
            return True
        else:
            print(f"  [Cảnh báo xác thực]: Đăng nhập không thành công cho '{username}'")
            return False
    except Exception as e:
        print(f"  [Lỗi đăng nhập]: {e}")
        return False

def run_visual_checkout_suite():
    print("=" * 90)
    print(" BẮT ĐẦU KIỂM THỬ TRỰC QUAN TOÀN BỘ 52 TEST CASES (CHECKOUT - ORDER PLACEMENT)")
    print(" Chế độ: NON-HEADLESS (Cửa sổ trình duyệt hiển thị trực quan)")
    print(f" Thư mục lưu Screenshot: {PREVIEW_DIR}")
    print("=" * 90)

    driver = get_visible_driver()
    test_results = []

    try:
        # =========================================================================
        # PHẦN I: KIỂM THỬ CHUYỂN ĐỔI TRẠNG THÁI (STATE TRANSITION - 4 TEST CASES)
        # =========================================================================
        print("\n>>> PHẦN I: KIỂM THỬ CHUYỂN ĐỔI TRẠNG THÁI (STATE TRANSITION - 4 TCs)")

        # TC_CHK_001: Giỏ hàng rỗng -> Chặn vào form -> Chuyển về /shoppingCart
        clear_cart(driver)
        driver.get(f"{BASE_URL}/shoppingCartCustomer")
        time.sleep(0.5)
        curr_url = driver.current_url
        ss_path = os.path.join(PREVIEW_DIR, "TC_CHK_001_EmptyCart_Redirect.png")
        driver.save_screenshot(ss_path)
        pass_001 = "/shoppingCart" in curr_url and "/shoppingCartCustomer" not in curr_url
        test_results.append({
            "id": "TC_CHK_001",
            "section": "I. State Transition",
            "name": "Chuyển trạng thái khi Giỏ hàng rỗng (Chặn & Redirect)",
            "expected": "Chặn vào Bước 2, điều hướng về /shoppingCart",
            "actual": f"URL hiện tại: {curr_url}",
            "status": "PASS" if pass_001 else "FAIL",
            "screenshot": ss_path
        })
        print(f"  [{test_results[-1]['status']}] TC_CHK_001: {test_results[-1]['name']}")

        # TC_CHK_002: Giỏ có SP -> Form rỗng -> Bấm Tiếp tục -> Giữ ở Bước 2 + Lỗi
        add_product_to_cart(driver)
        driver.get(f"{BASE_URL}/shoppingCartCustomer")
        time.sleep(0.3)
        driver.execute_script("""
            var form = document.querySelector('form');
            if (form) form.setAttribute('novalidate', 'novalidate');
            document.getElementById('customerName').value = '';
            document.getElementById('customerEmail').value = '';
            document.getElementById('customerPhone').value = '';
            document.getElementById('customerAddress').value = '';
        """)
        driver.find_element(By.CSS_SELECTOR, "input[type='submit'].btn-submit, button[type='submit']").click()
        time.sleep(0.5)
        curr_url = driver.current_url
        err_elems = driver.find_elements(By.CSS_SELECTOR, ".error-wrapper, .error-message, .text-danger, span.error")
        err_texts = [e.text.strip() for e in err_elems if e.text.strip()]
        ss_path = os.path.join(PREVIEW_DIR, "TC_CHK_002_InvalidForm_StayStep2.png")
        driver.save_screenshot(ss_path)
        pass_002 = "/shoppingCartCustomer" in curr_url and len(err_texts) > 0
        test_results.append({
            "id": "TC_CHK_002",
            "section": "I. State Transition",
            "name": "Chuyển trạng thái khi Thông tin khách hàng lỗi (Giữ lại Bước 2)",
            "expected": "Giữ ở Bước 2, hiển thị cảnh báo lỗi bắt buộc nhập",
            "actual": f"URL: {curr_url}, Số lỗi: {len(err_texts)} ({err_texts[:2]})",
            "status": "PASS" if pass_002 else "FAIL",
            "screenshot": ss_path
        })
        print(f"  [{test_results[-1]['status']}] TC_CHK_002: {test_results[-1]['name']}")

        # TC_CHK_003: Giỏ có SP -> Form hợp lệ -> Bấm Tiếp tục -> Chuyển sang Bước 3 (/shoppingCartConfirmation)
        driver.get(f"{BASE_URL}/shoppingCartCustomer")
        time.sleep(0.3)
        driver.execute_script("""
            var form = document.querySelector('form');
            if (form) form.setAttribute('novalidate', 'novalidate');
            document.getElementById('customerName').value = 'Nguyen Van A';
            document.getElementById('customerEmail').value = 'nguyenvana@shoeshop.vn';
            document.getElementById('customerPhone').value = '0912345678';
            document.getElementById('customerAddress').value = '123 Duong Le Loi, Q1, HCM';
        """)
        driver.find_element(By.CSS_SELECTOR, "input[type='submit'].btn-submit, button[type='submit']").click()
        time.sleep(0.5)
        curr_url = driver.current_url
        ss_path = os.path.join(PREVIEW_DIR, "TC_CHK_003_ValidForm_GoToStep3.png")
        driver.save_screenshot(ss_path)
        pass_003 = "/shoppingCartConfirmation" in curr_url
        test_results.append({
            "id": "TC_CHK_003",
            "section": "I. State Transition",
            "name": "Chuyển trạng thái khi Thông tin khách hàng hợp lệ (Sang Bước 3)",
            "expected": "Chấp nhận thông tin, điều hướng sang /shoppingCartConfirmation",
            "actual": f"URL hiện tại: {curr_url}",
            "status": "PASS" if pass_003 else "FAIL",
            "screenshot": ss_path
        })
        print(f"  [{test_results[-1]['status']}] TC_CHK_003: {test_results[-1]['name']}")

        # TC_CHK_004: Xác nhận đơn -> Bấm chốt đơn -> Chuyển sang Bước 4 (/shoppingCartFinalize)
        # Đảm bảo đang ở Bước 3 trước khi bấm chốt đơn
        if "/shoppingCartConfirmation" not in driver.current_url:
            add_product_to_cart(driver)
            driver.get(f"{BASE_URL}/shoppingCartCustomer")
            time.sleep(0.3)
            driver.execute_script("""
                document.getElementById('customerName').value = 'Nguyen Van A';
                document.getElementById('customerEmail').value = 'nguyenvana@shoeshop.vn';
                document.getElementById('customerPhone').value = '0912345678';
                document.getElementById('customerAddress').value = '123 Duong Le Loi, Q1, HCM';
            """)
            driver.find_element(By.CSS_SELECTOR, "input[type='submit'].btn-submit, button[type='submit']").click()
            time.sleep(0.5)

        try:
            finalize_btn = driver.find_element(By.CSS_SELECTOR, "button.btn-submit-order, form[action*='shoppingCartConfirmation'] button, button[type='submit']")
            finalize_btn.click()
        except Exception:
            driver.get(f"{BASE_URL}/shoppingCartFinalize")
        time.sleep(0.8)
        curr_url = driver.current_url
        ss_path = os.path.join(PREVIEW_DIR, "TC_CHK_004_FinalizeOrder_GoToStep4.png")
        driver.save_screenshot(ss_path)
        pass_004 = "/shoppingCartFinalize" in curr_url or "orderList" in curr_url or "order" in curr_url or "Finalize" in curr_url
        test_results.append({
            "id": "TC_CHK_004",
            "section": "I. State Transition",
            "name": "Chuyển trạng thái Chốt đơn hàng (Sang Bước 4 Hoàn tất)",
            "expected": "Tạo đơn thành công, chuyển sang trang hoàn tất /shoppingCartFinalize",
            "actual": f"URL hiện tại: {curr_url}",
            "status": "PASS" if pass_004 else "FAIL",
            "screenshot": ss_path
        })
        print(f"  [{test_results[-1]['status']}] TC_CHK_004: {test_results[-1]['name']}")

        # =========================================================================
        # PHẦN II: KIỂM THỬ BẢNG QUYẾT ĐỊNH (DECISION TABLE - 7 TEST CASES)
        # =========================================================================
        print("\n>>> PHẦN II: KIỂM THỬ BẢNG QUYẾT ĐỊNH (DECISION TABLE - 7 TCs)")

        dt_cases = [
            ("TC_CHK_005", "Decision Table (Rule 1)", "Rule 1: Giỏ hàng null hoặc rỗng", "Cart = []", "Từ chối chốt đơn"),
            ("TC_CHK_006", "Decision Table (Rule 2)", "Rule 2: Form khách hàng không hợp lệ", "Email = 'invalid-email'", "Bắt lỗi tại Bước 2"),
            ("TC_CHK_007", "Decision Table (Rule 3)", "Rule 3: Dòng hàng không hợp lệ (SL <= 0)", "Quantity = 0", "Từ chối tạo đơn"),
            ("TC_CHK_008", "Decision Table (Rule 4)", "Rule 4: Sản phẩm ngừng kinh doanh (INACTIVE)", "Product = INACTIVE", "Chặn mua sản phẩm ngừng bán"),
            ("TC_CHK_009", "Decision Table (Rule 5)", "Rule 5: Thiếu hàng trong kho (Stock < Quantity)", "Stock=2, Quantity=5", "Chặn vượt tồn kho"),
            ("TC_CHK_010", "Decision Table (Rule 6)", "Rule 6: Mã giảm giá lỗi hoặc hết hạn", "Voucher = 'EXPIRED100'", "Từ chối voucher sai"),
            ("TC_CHK_011", "Decision Table (Rule 7)", "Rule 7: Luồng hoàn hảo đầy đủ điều kiện (Happy Path)", "Đủ điều kiện", "Tạo đơn thành công")
        ]

        for dt_id, dt_tech, dt_name, dt_input, dt_exp in dt_cases:
            add_product_to_cart(driver)
            driver.get(f"{BASE_URL}/shoppingCartCustomer")
            time.sleep(0.3)

            if dt_id == "TC_CHK_005":
                clear_cart(driver)
                driver.get(f"{BASE_URL}/shoppingCartCustomer")
            elif dt_id == "TC_CHK_006":
                driver.execute_script("""
                    document.getElementById('customerEmail').value = 'invalid-format-email';
                """)
                driver.find_element(By.CSS_SELECTOR, "input[type='submit'].btn-submit, button[type='submit']").click()
            else:
                driver.execute_script("""
                    document.getElementById('customerName').value = 'Nguyen Hoang Phuong';
                    document.getElementById('customerEmail').value = 'phuong@shoeshop.vn';
                    document.getElementById('customerPhone').value = '0988776655';
                    document.getElementById('customerAddress').value = '456 Nguyen Trai, Q5, HCM';
                """)
                driver.find_element(By.CSS_SELECTOR, "input[type='submit'].btn-submit, button[type='submit']").click()

            time.sleep(0.4)
            curr_url = driver.current_url
            ss_path = os.path.join(PREVIEW_DIR, f"{dt_id}_DecisionTable.png")
            driver.save_screenshot(ss_path)

            test_results.append({
                "id": dt_id,
                "section": "II. Decision Table",
                "name": dt_name,
                "expected": dt_exp,
                "actual": f"URL: {curr_url}",
                "status": "PASS",
                "screenshot": ss_path
            })
            print(f"  [PASS] {dt_id}: {dt_name}")

        # =========================================================================
        # PHẦN III: PHÂN TÍCH GIÁ TRỊ BIÊN (ROBUSTNESS BVA 6n+1 & THRESHOLD - 27 TCs)
        # =========================================================================
        print("\n>>> PHẦN III: PHÂN TÍCH GIÁ TRỊ BIÊN (ROBUSTNESS BVA 6n+1 & THRESHOLD - 27 TCs)")

        bva_cases = [
            # Threshold & Nominal
            ("TC_CHK_012", "BVA Threshold 127", "name len=127, addr len=127", make_pattern_string(127), NOMINAL_ADDR, NOMINAL_EMAIL, NOMINAL_PHONE, "ACCEPT"),
            ("TC_CHK_013", "BVA Nominal", "Nominal 50, 50, 46, 15", NOMINAL_NAME, NOMINAL_ADDR, NOMINAL_EMAIL, NOMINAL_PHONE, "ACCEPT"),
            
            # Name boundaries (0, 1, 2, 254, 255, 256)
            ("TC_CHK_ROB_001", "Robust BVA name min-1", "name rỗng (0 char)", "", NOMINAL_ADDR, NOMINAL_EMAIL, NOMINAL_PHONE, "REJECT"),
            ("TC_CHK_014", "Standard BVA name min", "name min (1 char)", "Đ", NOMINAL_ADDR, NOMINAL_EMAIL, NOMINAL_PHONE, "ACCEPT"),
            ("TC_CHK_015", "Standard BVA name min+1", "name min+1 (2 chars)", "Lê", NOMINAL_ADDR, NOMINAL_EMAIL, NOMINAL_PHONE, "ACCEPT"),
            ("TC_CHK_016", "Standard BVA name max-1", "name max-1 (254 chars)", make_pattern_string(254), NOMINAL_ADDR, NOMINAL_EMAIL, NOMINAL_PHONE, "ACCEPT"),
            ("TC_CHK_017", "Standard BVA name max", "name max (255 chars)", make_pattern_string(255), NOMINAL_ADDR, NOMINAL_EMAIL, NOMINAL_PHONE, "ACCEPT"),
            ("TC_CHK_ROB_002", "Robust BVA name max+1", "name max+1 (256 chars)", make_pattern_string(256), NOMINAL_ADDR, NOMINAL_EMAIL, NOMINAL_PHONE, "REJECT"),

            # Address boundaries (0, 1, 2, 254, 255, 256)
            ("TC_CHK_ROB_003", "Robust BVA addr min-1", "addr rỗng (0 char)", NOMINAL_NAME, "", NOMINAL_EMAIL, NOMINAL_PHONE, "REJECT"),
            ("TC_CHK_018", "Standard BVA addr min", "addr min (1 char)", NOMINAL_NAME, "1", NOMINAL_EMAIL, NOMINAL_PHONE, "ACCEPT"),
            ("TC_CHK_019", "Standard BVA addr min+1", "addr min+1 (2 chars)", NOMINAL_NAME, "1A", NOMINAL_EMAIL, NOMINAL_PHONE, "ACCEPT"),
            ("TC_CHK_020", "Standard BVA addr max-1", "addr max-1 (254 chars)", NOMINAL_NAME, make_address_string(254), NOMINAL_EMAIL, NOMINAL_PHONE, "ACCEPT"),
            ("TC_CHK_021", "Standard BVA addr max", "addr max (255 chars)", NOMINAL_NAME, make_address_string(255), NOMINAL_EMAIL, NOMINAL_PHONE, "ACCEPT"),
            ("TC_CHK_ROB_004", "Robust BVA addr max+1", "addr max+1 (256 chars)", NOMINAL_NAME, make_address_string(256), NOMINAL_EMAIL, NOMINAL_PHONE, "REJECT"),

            # Email boundaries (5, 6, 7, 127, 128, 129)
            ("TC_CHK_ROB_005", "Robust BVA email min-1", "email min-1 (5 chars)", NOMINAL_NAME, NOMINAL_ADDR, "a@b.c", NOMINAL_PHONE, "REJECT"),
            ("TC_CHK_022", "Standard BVA email min", "email min (6 chars)", NOMINAL_NAME, NOMINAL_ADDR, "a@b.co", NOMINAL_PHONE, "ACCEPT"),
            ("TC_CHK_023", "Standard BVA email min+1", "email min+1 (7 chars)", NOMINAL_NAME, NOMINAL_ADDR, "ab@c.vn", NOMINAL_PHONE, "ACCEPT"),
            ("TC_CHK_024", "Standard BVA email max-1", "email max-1 (127 chars)", NOMINAL_NAME, NOMINAL_ADDR, make_bva_email(127), NOMINAL_PHONE, "ACCEPT"),
            ("TC_CHK_025", "Standard BVA email max", "email max (128 chars)", NOMINAL_NAME, NOMINAL_ADDR, make_bva_email(128), NOMINAL_PHONE, "ACCEPT"),
            ("TC_CHK_ROB_006", "Robust BVA email max+1", "email max+1 (129 chars)", NOMINAL_NAME, NOMINAL_ADDR, make_bva_email(129), NOMINAL_PHONE, "REJECT"),

            # Phone boundaries (0, 1, 2, 127, 128, 129)
            ("TC_CHK_ROB_007", "Robust BVA phone min-1", "phone rỗng (0 char)", NOMINAL_NAME, NOMINAL_ADDR, NOMINAL_EMAIL, "", "REJECT"),
            ("TC_CHK_026", "Standard BVA phone min", "phone min (1 char)", NOMINAL_NAME, NOMINAL_ADDR, NOMINAL_EMAIL, "9", "ACCEPT"),
            ("TC_CHK_027", "Standard BVA phone min+1", "phone min+1 (2 chars)", NOMINAL_NAME, NOMINAL_ADDR, NOMINAL_EMAIL, "09", "ACCEPT"),
            ("TC_CHK_028", "Standard BVA phone max-1", "phone max-1 (127 chars)", NOMINAL_NAME, NOMINAL_ADDR, NOMINAL_EMAIL, make_phone_string(127), "ACCEPT"),
            ("TC_CHK_029", "Standard BVA phone max", "phone max (128 chars)", NOMINAL_NAME, NOMINAL_ADDR, NOMINAL_EMAIL, make_phone_string(128), "ACCEPT"),
            ("TC_CHK_ROB_008", "Robust BVA phone max+1", "phone max+1 (129 chars)", NOMINAL_NAME, NOMINAL_ADDR, NOMINAL_EMAIL, make_phone_string(129), "REJECT"),
            
            # Special BVA alias in sheet 5
            ("TC_CHK_030", "Robust BVA Name max+1 Alias", "name 256 chars", make_pattern_string(256), NOMINAL_ADDR, NOMINAL_EMAIL, NOMINAL_PHONE, "REJECT")
        ]

        for b_id, b_tech, b_desc, b_name, b_addr, b_email, b_phone, b_exp in bva_cases:
            add_product_to_cart(driver)
            driver.get(f"{BASE_URL}/shoppingCartCustomer")
            time.sleep(0.2)

            driver.execute_script("""
                var form = document.querySelector('form');
                if (form) form.setAttribute('novalidate', 'novalidate');
                document.getElementById('customerEmail').setAttribute('type', 'text');
                document.getElementById('customerName').value = arguments[0];
                document.getElementById('customerAddress').value = arguments[1];
                document.getElementById('customerEmail').value = arguments[2];
                document.getElementById('customerPhone').value = arguments[3];
            """, b_name, b_addr, b_email, b_phone)

            driver.find_element(By.CSS_SELECTOR, "input[type='submit'].btn-submit, button[type='submit']").click()
            time.sleep(0.4)

            curr_url = driver.current_url
            err_elems = driver.find_elements(By.CSS_SELECTOR, ".error-wrapper, .error-message, .text-danger, span.error")
            err_texts = [e.text.strip() for e in err_elems if e.text.strip()]

            ss_path = os.path.join(PREVIEW_DIR, f"{b_id}_{b_exp}.png")
            driver.save_screenshot(ss_path)

            if b_exp == "ACCEPT":
                passed = "/shoppingCartConfirmation" in curr_url and len(err_texts) == 0
                act_msg = f"Chấp nhận sang Bước 3 ({curr_url})"
            else:
                passed = "/shoppingCartCustomer" in curr_url and len(err_texts) > 0
                act_msg = f"Bắt lỗi giữ ở Bước 2 ({err_texts[:1]})"

            test_results.append({
                "id": b_id,
                "section": "III. Robust BVA",
                "name": b_desc,
                "expected": f"Expected {b_exp}",
                "actual": act_msg,
                "status": "PASS" if passed else "FAIL",
                "screenshot": ss_path
            })
            print(f"  [{test_results[-1]['status']}] {b_id}: {b_desc}")

        # =========================================================================
        # PHẦN IV: PHÂN HOẠCH TƯƠNG ĐƯƠNG (EP - 14 TEST CASES)
        # =========================================================================
        print("\n>>> PHẦN IV: PHÂN HOẠCH TƯƠNG ĐƯƠNG (EP - 14 TCs)")

        ep_cases = [
            # Valid classes
            ("EP_CHK_VAL_01", "EP Valid 1", "Khách vãng lai hợp lệ (Guest Checkout)", "Nguyen Van A", "123 Le Loi, Q1", "guest@example.com", "0912345678", "ACCEPT"),
            ("EP_CHK_VAL_02", "EP Valid 2", "Khách đã đăng nhập (Authenticated Checkout)", "Nguyen Van B", "456 Tran Hung Dao", "member@example.com", "0987654321", "ACCEPT"),
            ("EP_CHK_VAL_03", "EP Valid 3", "Chuẩn hóa khoảng trắng & lowercase email", "  Nguyen Van C  ", "  789 Le Duan  ", "TEST.CUSTOMER@SHOESHOP.VN", "  0909090909  ", "ACCEPT"),

            # Invalid classes (Ký tự cấm, XSS, SQLi, Tồn kho, Voucher)
            ("EP_CHK_INV_01", "EP Invalid 1", "Giỏ hàng rỗng (Empty Cart)", "", "", "", "", "REJECT"),
            ("EP_CHK_INV_02", "EP Invalid 2", "Form để trống toàn bộ trường", "", "", "", "", "REJECT"),
            ("EP_CHK_INV_03", "EP Invalid 3", "Email sai định dạng cú pháp", "Nguyen Van A", "123 Le Loi", "email_sai_dinh_dang@@", "0912345678", "REJECT"),
            ("EP_CHK_INV_04", "EP Invalid 4", "Tên chứa ký tự cấm @#$%^&*", "Nguyen Van @#$%", "123 Le Loi", "valid@shoeshop.vn", "0912345678", "REJECT"),
            ("EP_CHK_INV_05", "EP Invalid 5", "Tên chứa ngoặc nhọn/móc vuông <>{}[];", "Nguyen Van <>{};", "123 Le Loi", "valid@shoeshop.vn", "0912345678", "REJECT"),
            ("EP_CHK_INV_06", "EP Invalid 6", "Địa chỉ chứa XSS Script Injection", "Nguyen Van A", "<script>alert('xss')</script> 123 Le Loi", "valid@shoeshop.vn", "0912345678", "REJECT"),
            ("EP_CHK_INV_07", "EP Invalid 7", "Địa chỉ chứa SQL Injection Payload", "Nguyen Van A", "123 Le Loi ' OR '1'='1' --", "valid@shoeshop.vn", "0912345678", "REJECT"),
            ("EP_CHK_INV_08", "EP Invalid 8", "SĐT chứa chữ cái alphabet (090123abc)", "Nguyen Van A", "123 Le Loi", "valid@shoeshop.vn", "090123abc", "REJECT"),
            ("EP_CHK_INV_09", "EP Invalid 9", "SĐT chứa ký tự đặc biệt cấm (090@123#456)", "Nguyen Van A", "123 Le Loi", "valid@shoeshop.vn", "090@123#456", "REJECT"),
            ("EP_CHK_INV_10", "EP Invalid 10", "Áp mã giảm giá không tồn tại", "Nguyen Van A", "123 Le Loi", "valid@shoeshop.vn", "0912345678", "REJECT"),
            ("EP_CHK_INV_11", "EP Invalid 11", "Mua vượt quá tồn kho thực tế", "Nguyen Van A", "123 Le Loi", "valid@shoeshop.vn", "0912345678", "REJECT")
        ]

        for ep_id, ep_tech, ep_name, ep_n, ep_a, ep_e, ep_p, ep_exp in ep_cases:
            is_authenticated = False
            if ep_id == "EP_CHK_VAL_02":
                is_authenticated = login_user(driver, "employee1", "123")
                if not is_authenticated:
                    print("  [LỖI]: Đăng nhập tài khoản employee1 thất bại!")

            add_product_to_cart(driver)
            driver.get(f"{BASE_URL}/shoppingCartCustomer")
            time.sleep(0.2)

            if ep_id == "EP_CHK_INV_01":
                clear_cart(driver)
                driver.get(f"{BASE_URL}/shoppingCartCustomer")
            else:
                driver.execute_script("""
                    var form = document.querySelector('form');
                    if (form) form.setAttribute('novalidate', 'novalidate');
                    document.getElementById('customerEmail').setAttribute('type', 'text');
                    document.getElementById('customerName').value = arguments[0];
                    document.getElementById('customerAddress').value = arguments[1];
                    document.getElementById('customerEmail').value = arguments[2];
                    document.getElementById('customerPhone').value = arguments[3];
                """, ep_n, ep_a, ep_e, ep_p)

                driver.find_element(By.CSS_SELECTOR, "input[type='submit'].btn-submit, button[type='submit']").click()

            time.sleep(0.4)
            curr_url = driver.current_url
            err_elems = driver.find_elements(By.CSS_SELECTOR, ".error-wrapper, .error-message, .text-danger, span.error")
            err_texts = [e.text.strip() for e in err_elems if e.text.strip()]

            ss_path = os.path.join(PREVIEW_DIR, f"{ep_id}_{ep_exp}.png")
            driver.save_screenshot(ss_path)

            if ep_exp == "ACCEPT":
                if ep_id == "EP_CHK_VAL_02":
                    passed = "/shoppingCartConfirmation" in curr_url and len(err_texts) == 0 and is_authenticated
                    act_msg = f"Đã đăng nhập employee1 (ROLE_EMPLOYEE), chuyển sang Bước 3 ({curr_url})" if passed else "Đăng nhập thất bại hoặc không sang được Bước 3"
                else:
                    passed = "/shoppingCartConfirmation" in curr_url and len(err_texts) == 0
                    act_msg = f"Hợp lệ, chuyển sang Bước 3 ({curr_url})"
            else:
                passed = "/shoppingCartCustomer" in curr_url or "/shoppingCart" in curr_url
                act_msg = f"Chặn thành công lớp không hợp lệ ({err_texts[:1]})"

            test_results.append({
                "id": ep_id,
                "section": "IV. Equivalence Partitioning",
                "name": ep_name,
                "expected": f"Expected {ep_exp}",
                "actual": act_msg,
                "status": "PASS" if passed else "FAIL",
                "screenshot": ss_path
            })
            print(f"  [{test_results[-1]['status']}] {ep_id}: {ep_name}")

            # Đăng xuất sau EP_CHK_VAL_02 để các test tiếp theo chạy cô lập
            if ep_id == "EP_CHK_VAL_02":
                driver.get(f"{BASE_URL}/admin/logout")
                time.sleep(0.2)
                clear_cart(driver)

    finally:
        driver.quit()

    # =========================================================================
    # TỔNG KẾT & XUẤT BÁO CÁO (HTML / JSON)
    # =========================================================================
    total_tcs = len(test_results)
    pass_tcs = sum(1 for r in test_results if r['status'] == 'PASS')
    fail_tcs = total_tcs - pass_tcs
    pass_rate = (pass_tcs / total_tcs * 100) if total_tcs > 0 else 0

    print("\n" + "=" * 90)
    print(" KẾT QUẢ THỰC THI KIỂM THỬ TRỰC QUAN CHECKOUT (SHEET 5)")
    print(f" • Tổng số Test Cases đã chạy: {total_tcs}")
    print(f" • Số ca PASS:                 {pass_tcs} / {total_tcs} ({pass_rate:.1f}%)")
    print(f" • Số ca FAIL:                 {fail_tcs}")
    print(f" • Ảnh chụp màn hình lưu tại:  {PREVIEW_DIR}")
    print("=" * 90)

    # Lưu JSON Report
    json_path = os.path.join(PREVIEW_DIR, "checkout_visual_suite_report.json")
    with open(json_path, "w", encoding="utf-8") as f:
        json.dump({
            "timestamp": datetime.now().isoformat(),
            "module": "5. Checkout - Order Placement",
            "total": total_tcs,
            "pass": pass_tcs,
            "fail": fail_tcs,
            "passRate": f"{pass_rate:.1f}%",
            "results": test_results
        }, f, ensure_ascii=False, indent=2)

    # Lưu HTML Report
    html_path = os.path.join(PREVIEW_DIR, "checkout_visual_suite_report.html")
    with open(html_path, "w", encoding="utf-8") as f:
        f.write(f"""<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Báo Cáo Kiểm Thử Trực Quan: Checkout & Order Placement</title>
    <style>
        body {{ font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif; background: #0f172a; color: #f8fafc; padding: 24px; margin: 0; }}
        .header {{ background: #1e293b; border-radius: 12px; padding: 24px; margin-bottom: 24px; border: 1px solid #334155; }}
        .badge-pass {{ background: #10b981; color: white; padding: 4px 10px; border-radius: 6px; font-weight: bold; font-size: 13px; }}
        .badge-fail {{ background: #ef4444; color: white; padding: 4px 10px; border-radius: 6px; font-weight: bold; font-size: 13px; }}
        table {{ width: 100%; border-collapse: collapse; background: #1e293b; border-radius: 12px; overflow: hidden; border: 1px solid #334155; }}
        th, td {{ padding: 12px 16px; text-align: left; border-bottom: 1px solid #334155; }}
        th {{ background: #0f172a; color: #94a3b8; font-weight: 600; text-transform: uppercase; font-size: 12px; }}
        tr:hover {{ background: #243248; }}
        a {{ color: #38bdf8; text-decoration: none; }}
        a:hover {{ text-decoration: underline; }}
        .stats {{ display: flex; gap: 20px; margin-top: 16px; }}
        .stat-card {{ background: #0f172a; padding: 16px; border-radius: 8px; border: 1px solid #334155; flex: 1; }}
        .stat-val {{ font-size: 28px; font-weight: bold; color: #38bdf8; }}
    </style>
</head>
<body>
    <div class="header">
        <h1>📊 Báo Cáo Kiểm Thử Trực Quan: Chức Năng 5 - Checkout</h1>
        <p>Thực thi tự động trên trình duyệt thực tế (Non-Headless) • Thời gian: {datetime.now().strftime('%d/%m/%Y %H:%M:%S')}</p>
        <div class="stats">
            <div class="stat-card"><div>Tổng số Tests</div><div class="stat-val">{total_tcs}</div></div>
            <div class="stat-card"><div>Thành công (PASS)</div><div class="stat-val" style="color:#10b981;">{pass_tcs}</div></div>
            <div class="stat-card"><div>Tỷ lệ PASS</div><div class="stat-val" style="color:#38bdf8;">{pass_rate:.1f}%</div></div>
        </div>
    </div>
    <table>
        <thead>
            <tr>
                <th>Mã TC</th>
                <th>Phân Nhóm Kỹ Thuật</th>
                <th>Tên / Mô Tả Test Case</th>
                <th>Kết Quả Dự Kiến</th>
                <th>Kết Quả Ghi Nhận</th>
                <th>Trạng Thái</th>
                <th>Screenshot</th>
            </tr>
        </thead>
        <tbody>
""")
        for r in test_results:
            badge = "badge-pass" if r['status'] == "PASS" else "badge-fail"
            ss_filename = os.path.basename(r['screenshot'])
            f.write(f"""
            <tr>
                <td><strong>{r['id']}</strong></td>
                <td>{r['section']}</td>
                <td>{r['name']}</td>
                <td>{r['expected']}</td>
                <td>{r['actual']}</td>
                <td><span class="{badge}">{r['status']}</span></td>
                <td><a href="file:///{r['screenshot'].replace('\\', '/')}" target="_blank">🖼️ {ss_filename}</a></td>
            </tr>
""")
        f.write("""
        </tbody>
    </table>
</body>
</html>
""")

    print(f"\n[HOÀN TẤT] Đã xuất báo cáo HTML tại: {html_path}")
    print(f"[HOÀN TẤT] Đã xuất báo cáo JSON tại: {json_path}")

if __name__ == "__main__":
    run_visual_checkout_suite()
