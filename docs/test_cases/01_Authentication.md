# Module 01: Authentication & Authorization (Đăng nhập / Xác thực)

### 1. Biến điều kiện (Conditions)
- **C1:** Tên tài khoản (Username) có tồn tại trong hệ thống không? (Yes/No)
- **C2:** Mật khẩu nhập vào có khớp với DB không? (Yes/No)
- **C3:** Tài khoản có đang bị khóa (Disabled) không? (Yes/No)
- **C4:** Tài khoản có quyền Quản trị viên (Admin/Manager) không? (Yes/No)

### 2. Danh sách Test Cases

| Mã kiểm thử | Kỹ thuật áp dụng | Tiêu đề | Điều kiện tiên quyết | Các bước kiểm tra | Dữ liệu kiểm thử | Kết quả dự kiến | Kết quả thực tế | Trạng thái |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **TC_AUTH_001** | Bảng quyết định (Rule 4) | Đăng nhập tài khoản Customer hợp lệ | Đang ở trang `/login` | 1. Nhập username, password<br>2. Bấm Login | User: `employee1`<br>Pass: `123` | Đăng nhập thành công, chuyển về trang chủ | Đăng nhập thành công, về trang chủ | Pass |
| **TC_AUTH_002** | Bảng quyết định (Rule 5) | Đăng nhập tài khoản Admin hợp lệ | Đang ở trang `/login` | 1. Nhập username, password admin<br>2. Bấm Login | User: `manager1`<br>Pass: `123` | Đăng nhập thành công, vào trang Admin Dashboard | Đăng nhập thành công, vào trang Admin | Pass |
| **TC_AUTH_003** | Bảng quyết định (Rule 2) | Đăng nhập thất bại do sai mật khẩu | Đang ở trang `/login` | 1. Nhập username đúng, pass sai<br>2. Bấm Login | User: `employee1`<br>Pass: `WrongPass123` | Báo lỗi `Invalid credentials` | Báo lỗi `Invalid credentials` | Pass |
| **TC_AUTH_004** | Bảng quyết định (Rule 1) | Chặn đăng nhập tài khoản chưa đăng ký | Đang ở trang `/login` | 1. Nhập username không có trong DB<br>2. Bấm Login | User: `nonexist_user`<br>Pass: `123456` | Báo lỗi không tìm thấy tài khoản | Báo lỗi không tìm thấy tài khoản | Pass |
| **TC_AUTH_005** | Bảng quyết định (Rule 3) | Chặn đăng nhập tài khoản bị khóa | Đang ở trang `/login` | 1. Nhập tài khoản bị khóa<br>2. Bấm Login | User: `locked_user`<br>Pass: `123` | Báo lỗi `Tài khoản đã bị tạm khóa` | Báo lỗi tài khoản bị khóa | Pass |
| **TC_AUTH_006** | BVA | Mật khẩu 5 ký tự (Dưới biên dưới) | Form đổi pass / đăng ký | 1. Nhập mật khẩu 5 ký tự<br>2. Submit | Password: `12345` | Báo lỗi mật khẩu tối thiểu 6 ký tự | Báo lỗi tối thiểu 6 ký tự | Pass |

### 3. Bảng quyết định (Decision Table)

| Quy tắc (Rules) | C1: Tồn tại trong hệ thống? | C2: Mật khẩu chính xác? | C3: Tài khoản bị khóa? | C4: Quyền Admin? | Kết quả / Hành động hệ thống | Số kịch bản gộp | Test Case tương ứng |
| :--- | :---: | :---: | :---: | :---: | :--- | :---: | :---: |
| **Rule 1 (Lỗi tài khoản)** | No | - | - | - | Báo lỗi: Không tìm thấy tài khoản | 8 | `TC_AUTH_004` |
| **Rule 2 (Lỗi mật khẩu)** | Yes | No | - | - | Báo lỗi: Sai mật khẩu (`Invalid credentials`) | 4 | `TC_AUTH_003` |
| **Rule 3 (Lỗi bị khóa)** | Yes | Yes | Yes | - | Báo lỗi: Tài khoản đang bị tạm khóa | 2 | `TC_AUTH_005` |
| **Rule 4 (Đăng nhập User)** | Yes | Yes | No | No | Đăng nhập thành công -> Về Trang chủ | 1 | `TC_AUTH_001` |
| **Rule 5 (Đăng nhập Admin)** | Yes | Yes | No | Yes | Đăng nhập thành công -> Vào Admin Dashboard | 1 | `TC_AUTH_002` |

- **Automation:** `src/test/java/com/example/demo/ui/AuthenticationUiTest.java`
