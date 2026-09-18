# BÁO CÁO TỔNG QUAN KIỂM THỬ GIAO DIỆN (E2E UI SELENIUM TESTS)
**Thư mục mục tiêu:** `com.example.demo.ui`  
**Tổng số Test Cases:** **17 Test Cases**  
**Tổng số tập tin ma trận chi tiết:** **6 tập tin**

---

## 1. GIỚI THIỆU & MỤC TIÊU PHÂN HỆ KIỂM THỬ GIAO DIỆN

Phân hệ **Kiểm thử Giao diện Người dùng Đầu-Cuối (End-to-End UI Tests)** sử dụng **Selenium WebDriver** kết hợp mô hình thiết kế **Page Object Model (POM)** để kiểm thử hệ thống từ góc nhìn của người dùng thật trên trình duyệt web:
- **Tương tác trực quan:** Tự động hóa các hành động thực tế của người dùng: nhấp chuột (`click`), nhập dữ liệu (`sendKeys`), chọn dropdown (`select`), upload file ảnh thực tế từ ổ đĩa và xử lý các thông báo/badge trạng thái.
- **Xác thực luồng nghiệp vụ E2E:** Kiểm tra khả năng vận hành liền mạch giữa giao diện Frontend (Thymeleaf, CSS, JS, Session) và Backend (Spring Security, Controller, Database).
- **Phân tách theo Page Object:** Tách biệt mã kiểm thử và cấu trúc trang web thông qua các lớp Page Object chuyên biệt trong package `com.example.demo.ui.pages` (`LoginPage`, `AdminProductPage`, `AdminOrderPage`, `CartPage`, `CheckoutPage`, `ProductDetailPage`).

---

## 2. BẢNG TỔNG HỢP 17 TEST CASES GIAO DIỆN E2E

| STT | File Tài Liệu Ma Trận | File Kiểm Thử Tương Ứng | Số TCs | Phạm Vi / Mục Tiêu Nghiệp Vụ Trên Giao Diện | Trạng Thái |
| :---: | :--- | :--- | :---: | :--- | :---: |
| 1 | [AdminOrderUiTest_TCs.md](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/docs/test_cases/whitebox/06_E2E_UI_Selenium_Tests/AdminOrderUiTest_TCs.md) | `AdminOrderUiTest.java` | **2** | Quản trị viên xem danh sách đơn hàng và đổi trạng thái đơn sang `SHIPPING` | ✅ PASS |
| 2 | [AdminProductUiTest_TCs.md](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/docs/test_cases/whitebox/06_E2E_UI_Selenium_Tests/AdminProductUiTest_TCs.md) | `AdminProductUiTest.java` | **2** | Quản trị viên tạo sản phẩm mới có upload ảnh thật và chặn form khi thiếu SKU | ✅ PASS |
| 3 | [AuthenticationUiTest_TCs.md](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/docs/test_cases/whitebox/06_E2E_UI_Selenium_Tests/AuthenticationUiTest_TCs.md) | `AuthenticationUiTest.java` | **6** | Đăng nhập hợp lệ (Admin, User), xử lý thông tin sai, bỏ trống trường và chuyển sang đăng ký | ✅ PASS |
| 4 | [CheckoutUiTest_TCs.md](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/docs/test_cases/whitebox/06_E2E_UI_Selenium_Tests/CheckoutUiTest_TCs.md) | `CheckoutUiTest.java` | **1** | Hành trình mua sắm E2E trọn gói: Xem hàng ➔ Thêm giỏ ➔ Điền địa chỉ ➔ Thanh toán | ✅ PASS |
| 5 | [SecurityAccessUiTest_TCs.md](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/docs/test_cases/whitebox/06_E2E_UI_Selenium_Tests/SecurityAccessUiTest_TCs.md) | `SecurityAccessUiTest.java` | **4** | Chặn phân quyền trình duyệt: chuyển hướng login khi chưa đăng nhập, lỗi 403 khi sai quyền | ✅ PASS |
| 6 | [VoucherUiTest_TCs.md](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/docs/test_cases/whitebox/06_E2E_UI_Selenium_Tests/VoucherUiTest_TCs.md) | `VoucherUiTest.java` | **2** | Tương tác giỏ hàng: áp dụng voucher hợp lệ cập nhật tiền giảm và báo lỗi khi mã sai | ✅ PASS |
| **TỔNG** | **6 tập tin ma trận chi tiết** | **6 Test Classes E2E** | **17** | **Kiểm thử tự động hóa giao diện người dùng Selenium** | **✅ 100% PASS** |
