# BẢNG MA TRẬN KIỂM THỬ GIAO DIỆN ADMIN ORDER (SELENIUM E2E UI)
**Đường dẫn file mã nguồn:** [OrderController.java](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/src/main/java/com/example/demo/controller/OrderController.java)  
**Đường dẫn file kiểm thử:** [AdminOrderUiTest.java](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/src/test/java/com/example/demo/ui/AdminOrderUiTest.java)

---

## 1. TỔNG QUAN ĐẶC ĐIỂM KIỂM THỬ GIAO DIỆN (SELENIUM E2E UI)
* **Phạm vi kiểm thử:** Tự động hóa trình duyệt web thực tế qua Selenium WebDriver và Page Object Pattern (`AdminOrderPage`, `LoginPage`).
* **Mục tiêu:** Kiểm thử trải nghiệm người dùng thực tế của Quản trị viên trong việc theo dõi danh sách đơn hàng và cập nhật tiến độ xử lý đơn hàng (chuyển trạng thái sang `SHIPPING`).

---

## 2. MA TRẬN TEST CASES KIỂM THỬ GIAO DIỆN (2 TEST CASES)

| Mã TC | Chức năng trên UI | Hàm kiểm thử ở src/test | Dữ liệu & Thao tác kiểm thử đầu vào | Kịch bản luồng thao tác người dùng (Steps) | Kết quả mong đợi (Assertions) |
| :---: | :--- | :--- | :--- | :--- | :--- |
| **TC_UI_ORD_01** | Xem danh sách đơn hàng Admin | `TC01_adminViewOrderList()` | User: `manager1`, Pass: `123` | 1. Mở trang `/admin/login`<br>2. Nhập thông tin đăng nhập admin<br>3. Điều hướng đến `/admin/orderList`<br>4. Chờ trang tải hoàn tất | Đăng nhập thành công, chuyển hướng tới `/admin/orderList`, URL trình duyệt chứa đúng đường dẫn danh sách đơn hàng |
| **TC_UI_ORD_02** | Cập nhật trạng thái đơn hàng Admin | `TC02_adminUpdateOrderStatus()` | User: `manager1`, Trạng thái mới: `SHIPPING` | 1. Đăng nhập quyền admin<br>2. Mở danh sách đơn hàng<br>3. Bấm vào liên kết xem chi tiết đơn đầu tiên<br>4. Chọn trạng thái `SHIPPING`<br>5. Bấm nút "Cập nhật trạng thái" | Trình duyệt điều hướng vào trang chi tiết đơn (`orderId=`), badge trạng thái trên UI đổi sang `"SHIPPING"` hoặc hiển thị thông báo cập nhật thành công |
