# BẢNG MA TRẬN KIỂM THỬ GIAO DIỆN XÁC THỰC NGƯỜI DÙNG (SELENIUM E2E UI)
**Đường dẫn file mã nguồn:** [UserController.java](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/src/main/java/com/example/demo/controller/UserController.java)  
**Đường dẫn file kiểm thử:** [AuthenticationUiTest.java](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/src/test/java/com/example/demo/ui/AuthenticationUiTest.java)

---

## 1. TỔNG QUAN ĐẶC ĐIỂM KIỂM THỬ GIAO DIỆN (SELENIUM E2E UI)
* **Phạm vi kiểm thử:** Tự động hóa kiểm thử giao diện đăng nhập và điều hướng đăng ký trên trình duyệt thực tế qua `LoginPage` Page Object.
* **Mục tiêu:** Xác thực các tình huống đăng nhập thành công (khách hàng, admin), xử lý lỗi sai thông tin đăng nhập, bỏ trống tài khoản/mật khẩu, và chuyển hướng mượt mà sang trang đăng ký thành viên.

---

## 2. MA TRẬN TEST CASES KIỂM THỬ GIAO DIỆN (6 TEST CASES)

| Mã TC | Chức năng trên UI | Hàm kiểm thử ở src/test | Dữ liệu & Thao tác kiểm thử đầu vào | Kịch bản luồng thao tác người dùng (Steps) | Kết quả mong đợi (Assertions) |
| :---: | :--- | :--- | :--- | :--- | :--- |
| **TC_UI_ATH_01** | Đăng nhập tài khoản Nhân viên/Khách hàng | `TC01_customerLoginWithValidCredentials()` | User: `employee1`, Pass: `123` | 1. Mở trang đăng nhập<br>2. Nhập thông tin nhân viên<br>3. Bấm Đăng nhập | Đăng nhập thành công, URL không chứa tham số lỗi `?error`, chuyển hướng vào hệ thống |
| **TC_UI_ATH_02** | Đăng nhập tài khoản Quản trị viên | `TC02_adminLoginWithValidCredentials()` | User: `manager1`, Pass: `123` | 1. Mở trang đăng nhập<br>2. Nhập thông tin admin<br>3. Bấm Đăng nhập | Đăng nhập thành công, chuyển hướng vào trang quản trị |
| **TC_UI_ATH_03** | Đăng nhập sai thông tin | `TC03_invalidLoginShouldDisplayError()` | User: `invalid_user_123`, Pass: `wrongpassword` | 1. Mở trang đăng nhập<br>2. Nhập tài khoản không tồn tại<br>3. Bấm Đăng nhập | Hiển thị thông báo lỗi đăng nhập trên giao diện (`isErrorDisplayed == true`) |
| **TC_UI_ATH_04** | Đăng nhập bỏ trống Tên tài khoản | `TC04_loginWithEmptyUsername()` | User: rỗng, Pass: `123` | 1. Mở trang đăng nhập<br>2. Chỉ nhập mật khẩu<br>3. Bấm Đăng nhập | Trình duyệt giữ nguyên người dùng tại trang đăng nhập `/admin/login` |
| **TC_UI_ATH_05** | Đăng nhập bỏ trống Mật khẩu | `TC05_loginWithEmptyPassword()` | User: `invalid_user_123`, Pass: rỗng | 1. Mở trang đăng nhập<br>2. Chỉ nhập username<br>3. Bấm Đăng nhập | Trình duyệt giữ nguyên người dùng tại trang đăng nhập `/admin/login` |
| **TC_UI_ATH_06** | Điều hướng sang Đăng ký thành viên | `TC06_customerRegistrationNavigation()` | Thao tác bấm nút/liên kết "Đăng ký" | 1. Mở trang đăng nhập<br>2. Nhấp vào liên kết Đăng ký tài khoản mới | Trình duyệt điều hướng thành công sang trang đăng ký `/register` |
