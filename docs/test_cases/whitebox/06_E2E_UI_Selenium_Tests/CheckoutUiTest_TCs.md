# BẢNG MA TRẬN KIỂM THỬ GIAO DIỆN THANH TOÁN (SELENIUM E2E UI JOURNEY)
**Đường dẫn file mã nguồn:** [CartController.java](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/src/main/java/com/example/demo/controller/CartController.java)  
**Đường dẫn file kiểm thử:** [CheckoutUiTest.java](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/src/test/java/com/example/demo/ui/CheckoutUiTest.java)

---

## 1. TỔNG QUAN ĐẶC ĐIỂM KIỂM THỬ GIAO DIỆN (SELENIUM E2E UI)
* **Phạm vi kiểm thử:** Tự động hóa toàn diện hành trình mua sắm từ đầu đến cuối (End-to-End User Journey) qua nhiều trang: Đăng nhập ➔ Danh mục sản phẩm ➔ Chi tiết sản phẩm ➔ Giỏ hàng ➔ Điền thông tin giao hàng ➔ Xác nhận đơn hàng.
* **Mục tiêu:** Chứng minh toàn bộ các thành phần giao diện, Session giỏ hàng, và luồng thanh toán liên kết mượt mà trên trình duyệt.

---

## 2. MA TRẬN TEST CASES KIỂM THỬ GIAO DIỆN (1 TEST CASE)

| Mã TC | Chức năng trên UI | Hàm kiểm thử ở src/test | Dữ liệu & Thao tác kiểm thử đầu vào | Kịch bản luồng thao tác người dùng (Steps) | Kết quả mong đợi (Assertions) |
| :---: | :--- | :--- | :--- | :--- | :--- |
| **TC_UI_CHK_01** | Hành trình mua hàng & thanh toán trọn gói | `TC01_endToendCheckoutJourney()` | User: `employee1`, Thông tin giao hàng: "Test User", "test@example.com", "0123456789", "123 Test St" | 1. Đăng nhập người dùng<br>2. Mở `/productList`, bấm chọn sản phẩm đầu tiên<br>3. Tại trang chi tiết, bấm "Thêm vào giỏ"<br>4. Tại giỏ hàng, bấm "Tiến hành thanh toán"<br>5. Điền form thông tin khách hàng và bấm Tiếp tục<br>6. Tại trang xác nhận, bấm "Đặt hàng" | Đơn hàng hoàn tất, trình duyệt chuyển hướng thành công đến trang hoàn tất thanh toán `/shoppingCartFinalize` |
