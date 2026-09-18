# BẢNG MA TRẬN KIỂM THỬ GIAO DIỆN ÁP DỤNG VOUCHER (SELENIUM E2E UI)
**Đường dẫn file mã nguồn:** [VoucherApiController.java](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/src/main/java/com/example/demo/controller/api/VoucherApiController.java), [CartController.java](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/src/main/java/com/example/demo/controller/CartController.java)  
**Đường dẫn file kiểm thử:** [VoucherUiTest.java](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/src/test/java/com/example/demo/ui/VoucherUiTest.java)

---

## 1. TỔNG QUAN ĐẶC ĐIỂM KIỂM THỬ GIAO DIỆN (SELENIUM E2E UI)
* **Phạm vi kiểm thử:** Tự động hóa tương tác ô nhập mã khuyến mãi tại trang Giỏ hàng `/shoppingCart` qua Selenium WebDriver và `CartPage`.
* **Mục tiêu:** Kiểm tra phản hồi trực quan trên giao diện người dùng: cập nhật chiết khấu và tổng tiền khi nhập mã hợp lệ, hoặc hiển thị thông báo lỗi màu đỏ khi nhập mã không tồn tại.

---

## 2. MA TRẬN TEST CASES KIỂM THỬ GIAO DIỆN (2 TEST CASES)

| Mã TC | Chức năng trên UI | Hàm kiểm thử ở src/test | Dữ liệu & Thao tác kiểm thử đầu vào | Kịch bản luồng thao tác người dùng (Steps) | Kết quả mong đợi (Assertions) |
| :---: | :--- | :--- | :--- | :--- | :--- |
| **TC_UI_VOU_01** | Áp dụng Voucher hợp lệ | `TC01_applyValidVoucherUpdatesTotal()` | Mã hợp lệ: `"WELCOME50"`, giỏ hàng đã có sản phẩm | 1. Đăng nhập khách hàng<br>2. Thêm sản phẩm vào giỏ<br>3. Mở giỏ hàng `/shoppingCart`<br>4. Nhập mã `WELCOME50` và bấm Áp dụng | Hiển thị thông báo áp dụng thành công, trường tiền giảm giá `#summary-voucher-discount` được cập nhật khác `0 ₫` |
| **TC_UI_VOU_02** | Áp dụng Voucher không hợp lệ | `TC02_applyInvalidVoucherDisplaysError()` | Mã không tồn tại: `"INVALID_VOUCHER_99999"` | 1. Đăng nhập khách hàng<br>2. Thêm sản phẩm vào giỏ<br>3. Mở giỏ hàng<br>4. Nhập mã voucher sai và bấm Áp dụng | Hiển thị thông báo lỗi trên UI, thông báo chứa nội dung lỗi từ chối và tiền giảm giữ nguyên `0 ₫` |
