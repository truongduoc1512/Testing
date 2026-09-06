# Bảng Test Case: Chức năng 1 - Đăng nhập & Xác thực (Authentication)
**Người thực hiện:** Lĩnh

## 1. Thông tin Kỹ thuật & Thực thi
- **Kỹ thuật Thiết kế (Test Design):**
  - **Phân hoạch lớp tương đương (EP):** Chia lớp hợp lệ (Đúng tài khoản/mật khẩu, Tài khoản đang Active) và không hợp lệ. Phân hoạch quyền truy cập (Admin vs Customer).
  - **Phân tích giá trị biên (BVA):** Ràng buộc độ dài tối thiểu của mật khẩu (VD: 6 ký tự).
  - **Bảng quyết định (Decision Table):** Sử dụng để xử lý tổ hợp chuỗi kiểm tra tuần tự (Tồn tại -> Mật khẩu đúng -> Không bị khóa -> Quyền hạn).
- **Kỹ thuật Thực thi (Test Execution):** Kiểm thử Tự động Giao diện (UI Automation Testing) sử dụng Selenium WebDriver.
- **File Code Thực thi (Automation Script):** `src/test/java/com/example/demo/ui/AuthenticationUiTest.java`

---

## 2. Phân tích Kỹ thuật Thiết kế (Test Design Analysis)

### 2.1 Bảng Phân hoạch lớp tương đương (EP)
Bảo mật đăng nhập yêu cầu phân tách rõ các lớp trạng thái tài khoản:

| STT | Trường hợp của điều kiện đầu vào | Lớp tương đương Hợp lệ | Lớp tương đương Không hợp lệ |
| :---: | :--- | :--- | :--- |
| 1 | Trạng thái tài khoản | Hoạt động bình thường (ACTIVE) | Đang bị khóa (LOCKED / DISABLED) |
| 2 | Vai trò (Role) sau đăng nhập | `ROLE_ADMIN`, `ROLE_CUSTOMER` | Không xác định (Guest) |

### 2.2 Bảng Phân tích giá trị biên (BVA)
Áp dụng cho Form đăng ký / mật khẩu để chặn lỗi dữ liệu quá ngắn/quá dài:

| Biến đầu vào | Ràng buộc độ dài | Các giá trị biên kiểm thử (BVA Points) |
| :--- | :--- | :--- |
| **Mật khẩu** | $6 \le L \le 20$ | 5 (Lỗi min-), 6 (min), 10 (nom), 20 (max), 21 (Lỗi max+) |

### 2.3 Bảng Quyết định tổng hợp (Collapsed Decision Table)

| Condition/Action | R1 | R2 | R3 | R4 | R5 |
| :--- | :---: | :---: | :---: | :---: | :---: |
| **C1: Username tồn tại trong hệ thống?** | N | Y | Y | Y | Y |
| **C2: Mật khẩu chính xác?** | - | N | Y | Y | Y |
| **C3: Tài khoản có đang bị khóa không?**| - | - | Y | N | N |
| **C4: Có quyền Admin không?** | - | - | - | N | Y |
| **A1: Báo lỗi "Không tìm thấy tài khoản"**| X | - | - | - | - |
| **A2: Báo lỗi "Sai mật khẩu"** | - | X | - | - | - |
| **A3: Báo lỗi "Tài khoản đang bị khóa"**| - | - | X | - | - |
| **A4: Login thành công -> Về Trang chủ** | - | - | - | X | - |
| **A5: Login thành công -> Vào Admin UI**| - | - | - | - | X |
| **Test Case Tương ứng** | TC_AUTH_004 | TC_AUTH_003 | TC_AUTH_005 | TC_AUTH_001 | TC_AUTH_002 |

---

## 3. Bảng Test Case Chi Tiết

| Mã kiểm thử | Kỹ thuật áp dụng | Tiêu đề | Điều kiện tiên quyết | Các bước kiểm tra | Dữ liệu kiểm thử | Kết quả dự kiến | Kết quả thực tế | Trạng thái |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **TC_AUTH_001** | Bảng quyết định (Rule 4) / EP | Đăng nhập tài khoản Customer hợp lệ | Đang ở trang `/login` | 1. Nhập username, password. 2. Bấm Login | User: `employee1`. Pass: `123` | Đăng nhập thành công, chuyển về trang chủ | Đăng nhập thành công, về trang chủ | Pass |
| **TC_AUTH_002** | Bảng quyết định (Rule 5) / EP | Đăng nhập tài khoản Admin hợp lệ | Đang ở trang `/login` | 1. Nhập username, password admin. 2. Bấm Login | User: `manager1`. Pass: `123` | Đăng nhập thành công, vào trang Admin Dashboard | Đăng nhập thành công, vào trang Admin | Pass |
| **TC_AUTH_003** | Bảng quyết định (Rule 2) / EP | Đăng nhập thất bại do sai mật khẩu | Đang ở trang `/login` | 1. Nhập username đúng, pass sai. 2. Bấm Login | User: `employee1`. Pass: `WrongPass123` | Báo lỗi `Invalid credentials` | Báo lỗi `Invalid credentials` | Pass |
| **TC_AUTH_004** | Bảng quyết định (Rule 1) / EP | Chặn đăng nhập tài khoản chưa đăng ký | Đang ở trang `/login` | 1. Nhập username không có trong DB. 2. Bấm Login | User: `nonexist_user`. Pass: `123456` | Báo lỗi không tìm thấy tài khoản | Báo lỗi không tìm thấy tài khoản | Pass |
| **TC_AUTH_005** | Bảng quyết định (Rule 3) / EP | Chặn đăng nhập tài khoản bị khóa | Đang ở trang `/login` | 1. Nhập tài khoản bị khóa. 2. Bấm Login | User: `locked_user`. Pass: `123` | Báo lỗi `Tài khoản đã bị tạm khóa` | Báo lỗi tài khoản bị khóa | Pass |
| **TC_AUTH_006** | BVA | Mật khẩu 5 ký tự (Dưới biên dưới) | Form đổi pass / đăng ký | 1. Nhập mật khẩu 5 ký tự. 2. Submit | Password: `12345` | Báo lỗi mật khẩu tối thiểu 6 ký tự | Báo lỗi tối thiểu 6 ký tự | Pass |

 


