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


### 2.1 Bảng Phân hoạch lớp tương đương (Equivalence Partitioning - EP)

| Biến đầu vào / Điều kiện | Lớp tương đương Hợp lệ | Tag | Lớp tương đương Không hợp lệ | Tag |
| :--- | :--- | :---: | :--- | :---: |
| **Tài khoản (`username`)** | Tồn tại trong CSDL | **V1** | Không tồn tại trong CSDL<br>Để trống / null | **X1**<br>**X2** |
| **Mật khẩu (`password`)** | Khớp chính xác với hash trong CSDL | **V2** | Sai mật khẩu | **X3** |
| **Trạng thái tài khoản (`status`)** | Hoạt động bình thường (`ACTIVE = true`) | **V3** | Bị khóa / Vô hiệu hóa (`LOCKED = false`) | **X4** |
| **Phân quyền người dùng (`role`)** | `ROLE_CUSTOMER` hoặc `ROLE_ADMIN` | **V4** | Khách vãng lai chưa xác thực | **X5** |
| **Độ dài mật khẩu (`passLength`)** | $6 \le length \le 20$ ký tự | **V5** | $length < 6$ ký tự (Quá ngắn)<br>$length > 20$ ký tự (Quá dài) | **X6**<br>**X7** |


### 2.2 Bảng Phân tích giá trị biên (Standard Boundary Value Analysis - BVA)

| Biến đầu vào | Miền hợp lệ | min | min+ | nominal | max- | max | Tag biên |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :--- |
| **Độ dài mật khẩu (`passLength`)** | $[6, 20]$ | 6 | 7 | 10 | 19 | 20 | **B1, B2, B3, B4, B5** |
| **Độ dài tên đăng nhập (`userLength`)** | $[3, 30]$ | 3 | 4 | 15 | 29 | 30 | **B6, B7, B8, B9, B10** |

*Ghi chú mở rộng (Robustness BVA):*
- Giá trị ngoài biên dưới độ dài mật khẩu: $L = 5$ (Tag **B0** - Không hợp lệ, kiểm tra tại TC_AUTH_006).
- Giá trị ngoài biên trên độ dài mật khẩu: $L = 21$ (Tag **B11** - Không hợp lệ).
- Giá trị danh định `nominal = 10` được chọn làm giá trị đại diện nằm giữa khoảng hợp lệ $[6, 20]$.


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

| Mã kiểm thử | Kỹ thuật áp dụng | Tiêu đề | Điều kiện tiên quyết | Các bước kiểm tra | Dữ liệu kiểm thử | Kết quả dự kiến | Tag được bao phủ | Kết quả thực tế | Trạng thái |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :---: | :--- | :---: |
| **TC_AUTH_001** | Bảng quyết định (Rule 4) / EP | Đăng nhập tài khoản Customer hợp lệ | Đang ở trang `/login` | 1. Nhập username, password. 2. Bấm Login | User: `employee1`. Pass: `123` | Đăng nhập thành công, chuyển về trang chủ | **V1, V2, V3, V4, B3** | Đăng nhập thành công, về trang chủ | Pass |
| **TC_AUTH_002** | Bảng quyết định (Rule 5) / EP | Đăng nhập tài khoản Admin hợp lệ | Đang ở trang `/login` | 1. Nhập username, password admin. 2. Bấm Login | User: `manager1`. Pass: `123` | Đăng nhập thành công, vào trang Admin Dashboard | **V1, V2, V3, V4, B3** | Đăng nhập thành công, vào trang Admin | Pass |
| **TC_AUTH_003** | Bảng quyết định (Rule 2) / EP | Đăng nhập thất bại do sai mật khẩu | Đang ở trang `/login` | 1. Nhập username đúng, pass sai. 2. Bấm Login | User: `employee1`. Pass: `WrongPass123` | Báo lỗi `Invalid credentials` | **V1, X3, V3** | Báo lỗi `Invalid credentials` | Pass |
| **TC_AUTH_004** | Bảng quyết định (Rule 1) / EP | Chặn đăng nhập tài khoản chưa đăng ký | Đang ở trang `/login` | 1. Nhập username không có trong DB. 2. Bấm Login | User: `nonexist_user`. Pass: `123456` | Báo lỗi không tìm thấy tài khoản | **X1** | Báo lỗi không tìm thấy tài khoản | Pass |
| **TC_AUTH_005** | Bảng quyết định (Rule 3) / EP | Chặn đăng nhập tài khoản bị khóa | Đang ở trang `/login` | 1. Nhập tài khoản bị khóa. 2. Bấm Login | User: `locked_user`. Pass: `123` | Báo lỗi `Tài khoản đã bị tạm khóa` | **V1, V2, X4** | Báo lỗi tài khoản bị khóa | Pass |
| **TC_AUTH_006** | BVA | Mật khẩu 5 ký tự (Dưới biên dưới) | Form đổi pass / đăng ký | 1. Nhập mật khẩu 5 ký tự. 2. Submit | Password: `12345` | Báo lỗi mật khẩu tối thiểu 6 ký tự | **X6, B0** | Báo lỗi tối thiểu 6 ký tự | Pass |


---

## 4. Bảng Đối Chiếu & Ý Nghĩa Nhãn Tag (Tag Traceability Legend)

| Nhóm Tag | Mã Tag | Ý nghĩa nghiệp vụ | Trạng thái |
| :---: | :---: | :--- | :---: |
| **Valid EP** | **V1** | Tên đăng nhập tồn tại trong hệ thống | Hợp lệ |
| | **V2** | Mật khẩu khớp với dữ liệu mã hóa trong CSDL | Hợp lệ |
| | **V3** | Trạng thái tài khoản đang hoạt động (ACTIVE) | Hợp lệ |
| | **V4** | Phân quyền người dùng chuẩn (CUSTOMER hoặc ADMIN) | Hợp lệ |
| | **V5** | Độ dài mật khẩu trong khoảng cho phép [6, 20] ký tự | Hợp lệ |
| **Invalid EP**| **X1** | Tên đăng nhập không tồn tại trong CSDL | Không hợp lệ |
| | **X2** | Tên đăng nhập để trống hoặc null | Không hợp lệ |
| | **X3** | Mật khẩu không chính xác | Không hợp lệ |
| | **X4** | Tài khoản bị khóa (DISABLED / LOCKED) | Không hợp lệ |
| | **X5** | Khách vãng lai chưa xác thực | Không hợp lệ |
| | **X6** | Mật khẩu quá ngắn (< 6 ký tự) | Không hợp lệ |
| | **X7** | Mật khẩu quá dài (> 20 ký tự) | Không hợp lệ |
| **Boundary** | **B1 - B5**| Các điểm biên độ dài mật khẩu: min (6), min+ (7), nom (10), max- (19), max (20) | Hợp lệ |
| | **B0** | Điểm ngoài biên dưới độ dài mật khẩu: min - 1 = 5 ký tự | Không hợp lệ |
| | **B6 - B10**| Các điểm biên độ dài tên đăng nhập: min (3), min+ (4), nom (15), max- (29), max (30) | Hợp lệ |
