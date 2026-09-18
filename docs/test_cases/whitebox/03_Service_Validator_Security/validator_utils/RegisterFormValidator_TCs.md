# ĐẶC TẢ KIỂM THỬ HỘP TRẮNG: `RegisterFormValidator`
## Tầng: Validator Layer | Phân hệ: Kiểm tra & Xác thực Đăng ký Tài khoản mới
* **Tổng số Test Case:** **15 Test Cases**
* **Mục đích tài liệu:** Bảng ánh xạ chi tiết giữa hàm nghiệp vụ trong `RegisterFormValidator.java` và các ca kiểm thử trong `RegisterFormValidatorTest.java`, phục vụ tra cứu nhanh giá trị test đầu vào, nhánh logic mục tiêu và kết quả mong đợi.

---

## 📌 PHẦN 1: BẢNG TỔNG HỢP CÁC HÀM TRONG `RegisterFormValidator.java`

Lớp `RegisterFormValidator` thực hiện kiểm tra tính hợp lệ dữ liệu đăng ký thành viên mới:

| STT | Tên hàm trong `RegisterFormValidator.java` | Mục đích nghiệp vụ | Số Test Case đối ứng |
| :---: | :--- | :--- | :---: |
| 1 | `supports(Class<?> clazz)` | Xác định đối tượng form được hỗ trợ (`RegisterForm.class`) | **2** |
| 2 | `validate(Object target, Errors errors)` | Chuẩn hóa chuỗi, kiểm tra bắt buộc, độ dài, định dạng email, khớp mật khẩu, và kiểm tra trùng lặp CSDL | **13** |

---

## 📑 PHẦN 2: BẢNG MA TRẬN ÁNH XẠ CHI TIẾT 15 TEST CASES

| Mã TC | Hàm ở `src/main` | Hàm kiểm thử ở `src/test` | Giá trị Test đầu vào (Inputs / Mock Data) | Nhánh logic / Điều kiện mục tiêu | Kết quả mong đợi (Assertion) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `TC_RFV_01` | `supports` | `supports_registerForm_returnsTrue` | `clazz = RegisterForm.class` | Nhánh `clazz == RegisterForm.class` | Trả về `true` |
| `TC_RFV_02` | `supports` | `supports_otherClass_returnsFalse` | `clazz = ProductForm.class` | Nhánh `clazz != RegisterForm.class` | Trả về `false` |
| `TC_RFV_03` | `validate` | `validate_validRegistration_normalizesInputAndQueriesDao` | `userName = "  alice  "`, `email = "  ALICE@EXAMPLE.COM  "`, `password = confirm = "password123"` | Nhánh đăng ký hợp lệ: trim chuỗi, lowercase email, tra cứu DB | `errors.hasErrors() = false`, gọi `findAccount("alice")` và `findAccountByEmail("alice@example.com")` |
| `TC_RFV_04` | `validate` | `validate_blankRequiredField_rejectsOnlyRequiredCodeAndSkipsDao` | Các trường `userName`, `email`, `password`, `confirmPassword` mang giá trị `null` hoặc `"   "` | Nhánh `rejectIfEmptyOrWhitespace`: ngắt sớm khi thiếu trường bắt buộc | Báo mã lỗi `"NotEmpty.registerForm.<field>"`, không query DB |
| `TC_RFV_05` | `validate` | `validate_invalidEmail_rejectsPatternAndSkipsDao` | `email = "invalid-email"` | Nhánh `!EmailValidator.isValid(email)` | Báo mã lỗi `"Pattern.registerForm.email"`, bỏ qua gọi DB |
| `TC_RFV_06` | `validate` | `validate_passwordMismatch_rejectsConfirmationAndSkipsDao` | `password = "password123"`, `confirmPassword = "different-password"` | Nhánh `!form.getPassword().equals(confirmPassword)` | Báo mã lỗi `"Match.registerForm.confirmPassword"`, bỏ qua DB |
| `TC_RFV_07` | `validate` | `validate_duplicateUsername_rejectsUsername` | `userName = "alice"`<br>Mock: `findAccount("alice") != null` | Nhánh phát hiện trùng username trong CSDL | Báo mã lỗi `"Duplicate.registerForm.userName"` |
| `TC_RFV_08` | `validate` | `validate_duplicateEmail_rejectsEmail` | `email = "alice@example.com"`<br>Mock: `findAccountByEmail("alice@example.com") != null` | Nhánh phát hiện trùng email trong CSDL | Báo mã lỗi `"Duplicate.registerForm.email"` |
| `TC_RFV_09` | `validate` | `validate_duplicateUsernameAndEmail_rejectsBothFields` | Trùng cả username lẫn email trong CSDL | Nhánh cả 2 câu truy vấn CSDL đều tìm thấy bản ghi | Báo đồng thời 2 mã lỗi: `"Duplicate.registerForm.userName"` và `"Duplicate.registerForm.email"` |
| `TC_RFV_10` | `validate` | `validate_usernameAtMaximumLength_hasNoUsernameError` | `userName` dài đúng 50 ký tự (`'u' * 50`) | Nhánh `userName.length() <= 50` | Hợp lệ, không có lỗi trường `userName` |
| `TC_RFV_11` | `validate` | `validate_usernameOverMaximumLength_rejectsLengthAndSkipsDao` | `userName` dài 51 ký tự (`'u' * 51`) | Nhánh `userName.length() > 50` | Báo mã lỗi `"Length.registerForm.userName"`, bỏ qua gọi DB |
| `TC_RFV_12` | `validate` | `validate_passwordAtBoundary_hasNoPasswordError` | Độ dài mật khẩu tại 2 giá trị biên: 8 và 72 ký tự | Nhánh `password.length()` trong khoảng `[8, 72]` | Hợp lệ, không có lỗi trường `password` |
| `TC_RFV_13` | `validate` | `validate_passwordOutsideBoundary_rejectsLengthAndSkipsDao` | Độ dài mật khẩu ngoài biên: 7 và 73 ký tự | Nhánh `password.length() < 8 \|\| password.length() > 72` | Báo mã lỗi `"Length.registerForm.password"` |
| `TC_RFV_14` | `validate` | `validate_emailAtMaximumLength_hasNoEmailError` | `email` có độ dài đúng 128 ký tự biên | Nhánh `email.length() <= 128` | Hợp lệ, không có lỗi trường `email` |
| `TC_RFV_15` | `validate` | `validate_emailOverMaximumLength_rejectsOnlyLengthAndSkipsDao`| `email` có độ dài 129 ký tự | Nhánh `email.length() > 128` | Báo duy nhất mã lỗi `"Length.registerForm.email"` |

---

## 📊 PHẦN 3: KẾT QUẢ ĐO LƯỜNG ĐỘ BAO PHỦ (JACOCO METRICS)

* **Lớp kiểm thử:** `com.example.demo.validator.RegisterFormValidator`
* **Statement Coverage (Instructions):** **100.0%** (163/163 instructions)
* **Branch Coverage (Branches):** **100.0%** (26/26 branches)
* **Line Coverage:** **100.0%** (38/38 lines)
* **Đánh giá:** Đạt độ bao phủ tuyệt đối 100%, bảo vệ an toàn quy trình khởi tạo tài khoản mới với các kiểm tra đa lớp (cục bộ và CSDL).
