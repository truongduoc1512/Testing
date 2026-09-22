# BẢNG MA TRẬN KIỂM THỬ GIAO DIỆN BẢO MẬT & PHÂN QUYỀN (SELENIUM E2E UI)
**Đường dẫn file mã nguồn:** [WebSecurityConfig.java](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/src/main/java/com/example/demo/config/WebSecurityConfig.java)  
**Đường dẫn file kiểm thử:** [SecurityAccessUiTest.java](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/src/test/java/com/example/demo/ui/SecurityAccessUiTest.java)

---

## 1. TỔNG QUAN ĐẶC ĐIỂM KIỂM THỬ GIAO DIỆN (SELENIUM E2E UI)
* **Phạm vi kiểm thử:** Tự động hóa kiểm tra lớp bảo mật Spring Security trên trình duyệt thật (Chrome/Firefox qua Selenium).
* **Mục tiêu:** Xác thực các chính sách kiểm soát truy cập (Access Control List): Khách vãng lai bị chuyển hướng sang Login khi truy cập tài nguyên Admin, và Người dùng thường (`ROLE_USER`) bị chặn 403 Forbidden khi cố tình truy cập các trang nhạy cảm của Quản trị viên (`/admin/product`, `/admin/users`).

---

## 2. MA TRẬN TEST CASES KIỂM THỬ GIAO DIỆN (4 TEST CASES)

| Mã TC | Chức năng trên UI | Hàm kiểm thử ở src/test | Dữ liệu & Thao tác kiểm thử đầu vào | Kịch bản luồng thao tác người dùng (Steps) | Kết quả mong đợi (Assertions) |
| :---: | :--- | :--- | :--- | :--- | :--- |
| **TC_UI_SEC_01** | Chặn khách vãng lai truy cập `/admin/product` | `TC01_anonymousUserAccessAdminProductRedirectsToLogin()` | Người dùng chưa đăng nhập (khách vãng lai) | 1. Mở trực tiếp URL `/admin/product` trên trình duyệt | Spring Security tự động can thiệp, chuyển hướng URL về `/admin/login` |
| **TC_UI_SEC_02** | Chặn khách vãng lai truy cập `/admin/users` | `TC02_anonymousUserAccessAdminUsersRedirectsToLogin()` | Người dùng chưa đăng nhập (khách vãng lai) | 1. Mở trực tiếp URL `/admin/users` trên trình duyệt | Spring Security tự động can thiệp, chuyển hướng URL về `/admin/login` |
| **TC_UI_SEC_03** | Chặn Role thường truy cập Quản trị sản phẩm | `TC03_customerUserAccessAdminProductDenied()` | User có role thường: `employee1` | 1. Đăng nhập tài khoản thường<br>2. Cố tình nhập URL `/admin/product` | Bị chặn quyền truy cập: tiêu đề trang chứa "403" hoặc hiển thị màn hình báo lỗi `/403` Access Denied |
| **TC_UI_SEC_04** | Chặn Role thường truy cập Quản trị người dùng | `TC04_customerUserAccessAdminUsersDenied()` | User có role thường: `employee1` | 1. Đăng nhập tài khoản thường<br>2. Cố tình nhập URL `/admin/users` | Bị chặn quyền truy cập: hiển thị trang lỗi 403 Access Denied, ngăn chặn xem danh sách user |
