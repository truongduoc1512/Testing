# ĐẶC TẢ KIỂM THỬ HỘP TRẮNG: `AccountProfileService`
## Tầng: Service Layer | Phân hệ: Xác thực & Quản lý Hồ sơ người dùng
* **Tổng số Test Case:** **14 Test Cases**
* **Mục đích tài liệu:** Bảng ánh xạ chi tiết giữa các hàm nghiệp vụ trong `AccountProfileService.java` và các ca kiểm thử trong `AccountProfileServiceTest.java`, phục vụ tra cứu nhanh giá trị test đầu vào, nhánh logic mục tiêu và kết quả mong đợi.

---

## 📌 PHẦN 1: BẢNG TỔNG HỢP CÁC HÀM TRONG `AccountProfileService.java`

Lớp `AccountProfileService` cung cấp các phương thức nghiệp vụ xử lý dữ liệu hồ sơ và chuẩn hóa vai trò người dùng:

| STT | Tên hàm trong `AccountProfileService.java` | Mục đích nghiệp vụ | Số Test Case đối ứng |
| :---: | :--- | :--- | :---: |
| 1 | `validate(Account, UserProfileForm)` | Kiểm tra tính hợp lệ dữ liệu hồ sơ (độ dài, định dạng email, trùng lặp email) | **10** |
| 2 | `apply(Account, UserProfileForm)` | Gán và cập nhật các trường thông tin chuẩn hóa từ form vào entity Account | **2** |
| 3 | `normalizeRole(String)` | Chuẩn hóa chuỗi quyền hạn (loại bỏ prefix ROLE_, kiểm tra tính hợp lệ ADMIN/USER)| **2** |

---

## 📑 PHẦN 2: BẢNG MA TRẬN ÁNH XẠ CHI TIẾT 14 TEST CASES

| Mã TC | Hàm ở `src/main` | Hàm kiểm thử ở `src/test` | Giá trị Test đầu vào (Inputs / Mock Data) | Nhánh logic / Điều kiện mục tiêu | Kết quả mong đợi (Assertion) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `TC_APS_01` | `validate` | `validate_rejectsNullAccount` | `account = null`, `form = validProfileForm()` | Nhánh `if (account == null \|\| form == null)` | Trả về `"Thông tin hồ sơ không hợp lệ!"` |
| `TC_APS_02` | `validate` | `validate_rejectsNullForm` | `account = buyer`, `form = null` | Nhánh `if (account == null \|\| form == null)` | Trả về `"Thông tin hồ sơ không hợp lệ!"` |
| `TC_APS_03` | `validate` | `validate_rejectsFullNameAboveMaximumLength` | `form.fullName` dài 101 ký tự (`'n' * 101`) | Nhánh `fullName.length() > 100` | Trả về `"Thông tin hồ sơ vượt quá độ dài cho phép!"` |
| `TC_APS_04` | `validate` | `validate_rejectsPhoneNumberAboveMaximumLength` | `form.phoneNumber` dài 21 ký tự (`'p' * 21`) | Nhánh `phone.length() > 20` | Trả về `"Thông tin hồ sơ vượt quá độ dài cho phép!"` |
| `TC_APS_05` | `validate` | `validate_rejectsAvatarUrlAboveMaximumLength` | `form.avatarUrl` dài 256 ký tự (`'a' * 256`) | Nhánh `avatar.length() > 255` | Trả về `"Thông tin hồ sơ vượt quá độ dài cho phép!"` |
| `TC_APS_06` | `validate` | `validate_allowsBlankEmail` | `form.email = "   "` (email rỗng hoặc chỉ chứa khoảng trắng) | Nhánh `email.isEmpty()` (cho phép bỏ qua kiểm tra email) | Trả về `null` (hợp lệ) |
| `TC_APS_07` | `validate` | `validate_rejectsInvalidEmail` | Case 1: email dài 101 ký tự<br>Case 2: email sai định dạng (`"not-an-email"`) | Nhánh `!email.isEmpty() && (length > 100 \|\| !isValid)` | Trả về `"Email không hợp lệ!"` |
| `TC_APS_08` | `validate` | `validate_acceptsNormalizedAvailableEmail` | `form.email = " Buyer@Example.COM "`<br>Mock: `accountDAO.findAccountByEmail("buyer@example.com") = null` | Nhánh email hợp lệ, chưa ai sử dụng | Trả về `null` (hợp lệ) |
| `TC_APS_09` | `validate` | `validate_allowsEmailOwnedBySameAccount` | `form.email = "buyer@example.com"`<br>Mock: `findAccountByEmail` trả về cùng username `"buyer"` | Nhánh `emailOwner.getUserName().equals(account.getUserName())` | Trả về `null` (cho phép giữ nguyên email của chính mình) |
| `TC_APS_10` | `validate` | `validate_rejectsEmailOwnedByAnotherAccount` | `form.email = "buyer@example.com"`<br>Mock: `findAccountByEmail` trả về account khác (`"other"`) | Nhánh `!emailOwner.getUserName().equals(account.getUserName())` | Trả về `"Email đã được sử dụng bởi tài khoản khác!"` |
| `TC_APS_11` | `apply` | `apply_mapsBlankValuesToNullAndPreservesBlankAvatar` | `fullName = null`, `email = "   "`, `phone = ""`, `avatarUrl = "   "` | Nhánh dữ liệu rỗng: ánh xạ về `null`, giữ nguyên avatar cũ | `account.fullName = null`, `email = null`, `phone = null`, `avatarUrl = "existing.png"` |
| `TC_APS_12` | `apply` | `apply_trimsAndNormalizesNonBlankValues` | `fullName = " Buyer Name "`, `email = " Buyer@Example.COM "`, `phone = " 0900 "`, `avatar = " avatar.png "` | Nhánh dữ liệu hợp lệ: trim khoảng trắng và lowercase email | `fullName = "Buyer Name"`, `email = "buyer@example.com"`, `phone = "0900"`, `avatarUrl = "avatar.png"` |
| `TC_APS_13` | `normalizeRole` | `normalizeRole_normalizesRecognizedRole` | Case 1: `" role_admin "` ➔ `"ADMIN"`<br>Case 2: `"user"` ➔ `"USER"` | Nhánh vai trò hợp lệ (bóc tách `ROLE_`, trim, uppercase) | Trả về `"ADMIN"` và `"USER"` tương ứng |
| `TC_APS_14` | `normalizeRole` | `normalizeRole_returnsNullForMissingOrUnrecognizedRole` | Case 1: `role = null`<br>Case 2: `role = "manager"` | Nhánh `role == null` hoặc vai trò không nằm trong ADMIN/USER | Trả về `null` |

---

## 📊 PHẦN 3: KẾT QUẢ ĐO LƯỜNG ĐỘ BAO PHỦ (JACOCO METRICS)

* **Lớp kiểm thử:** `com.example.demo.service.AccountProfileService`
* **Statement Coverage (Instructions):** **100.0%** (164/164 instructions)
* **Branch Coverage (Branches):** **100.0%** (38/38 branches)
* **Line Coverage:** **100.0%** (30/30 lines)
* **Đánh giá:** Đạt độ bao phủ tuyệt đối 100%, bảo đảm xác thực an toàn toàn bộ dữ liệu hồ sơ cá nhân và phân quyền người dùng.
