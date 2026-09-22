# ĐẶC TẢ KIỂM THỬ HỘP TRẮNG: `AuthenticatedAccountService`
## Tầng: Service Layer | Phân hệ: Nhận diện & Trích xuất Người dùng Xác thực
* **Tổng số Test Case:** **7 Test Cases**
* **Mục đích tài liệu:** Bảng ánh xạ chi tiết giữa hàm nghiệp vụ trong `AuthenticatedAccountService.java` và các ca kiểm thử trong `AuthenticatedAccountServiceTest.java`, phục vụ tra cứu nhanh giá trị test đầu vào, nhánh logic mục tiêu và kết quả mong đợi.

---

## 📌 PHẦN 1: BẢNG TỔNG HỢP CÁC HÀM TRONG `AuthenticatedAccountService.java`

Lớp `AuthenticatedAccountService` chịu trách nhiệm giải quyết (resolve) thực thể `Account` từ đối tượng bảo mật `Authentication` của Spring Security:

| STT | Tên hàm trong `AuthenticatedAccountService.java` | Mục đích nghiệp vụ | Số Test Case đối ứng |
| :---: | :--- | :--- | :---: |
| 1 | `resolve(Authentication authentication)` | Phân giải Account từ thông tin đăng nhập truyền thống hoặc OAuth2 User | **7** |

---

## 📑 PHẦN 2: BẢNG MA TRẬN ÁNH XẠ CHI TIẾT 7 TEST CASES

| Mã TC | Hàm ở `src/main` | Hàm kiểm thử ở `src/test` | Giá trị Test đầu vào (Inputs / Mock Data) | Nhánh logic / Điều kiện mục tiêu | Kết quả mong đợi (Assertion) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `TC_AAS_01` | `resolve` | `resolve_returnsNullForNullAuthentication` | `authentication = null` | Nhánh `if (authentication == null)` | Trả về `null`, không gọi DB |
| `TC_AAS_02` | `resolve` | `resolve_returnsNullForUnauthenticatedAuthentication` | `UsernamePasswordAuthenticationToken("buyer", "n/a")` chưa xác thực (`isAuthenticated() = false`) | Nhánh `if (!authentication.isAuthenticated())` | Trả về `null`, không gọi DB |
| `TC_AAS_03` | `resolve` | `resolve_returnsNullForAnonymousAuthentication` | Mock `Authentication`: `isAuthenticated() = true`, `getName() = "anonymousUser"` | Nhánh `if ("anonymousUser".equals(authentication.getName()))` | Trả về `null`, không gọi DB |
| `TC_AAS_04` | `resolve` | `resolve_returnsDirectUsernameAccountWithoutInspectingPrincipal` | `auth.getName() = "buyer"`, Mock `accountDAO.findAccount("buyer") = account` | Nhánh tìm thấy tài khoản trực tiếp qua `findAccount(username)` | Trả về đúng `account`, không đọc `OAuth2User.getPrincipal()` |
| `TC_AAS_05` | `resolve` | `resolve_returnsNullForMissingNonOAuthPrincipal` | `auth.getName() = "missing"`, `auth.getPrincipal() = "missing"` (không phải OAuth2User), Mock `findAccount("missing") = null` | Nhánh không tìm thấy account và principal không phải kiểu `OAuth2User` | Trả về `null`, không gọi `findAccountByEmail` |
| `TC_AAS_06` | `resolve` | `resolve_returnsNullForOAuthPrincipalWithoutEmail` | `auth.getName() = "Google User"`, `principal instanceof OAuth2User`, Mock `principal.getAttribute("email") = null` | Nhánh OAuth2User nhưng attribute `"email"` bị null | Trả về `null`, không gọi `findAccountByEmail` |
| `TC_AAS_07` | `resolve` | `resolve_normalizesOAuthEmailBeforeLookup` | `principal instanceof OAuth2User`, `principal.getAttribute("email") = " Buyer@Example.COM "`<br>Mock: `accountDAO.findAccountByEmail("buyer@example.com") = account` | Nhánh OAuth2User hợp lệ: trim và chuyển email về chữ thường | Trả về đúng `account` khớp với email |

---

## 📊 PHẦN 3: KẾT QUẢ ĐO LƯỜNG ĐỘ BAO PHỦ (JACOCO METRICS)

* **Lớp kiểm thử:** `com.example.demo.service.AuthenticatedAccountService`
* **Statement Coverage (Instructions):** **100.0%** (47/47 instructions)
* **Branch Coverage (Branches):** **100.0%** (12/12 branches)
* **Line Coverage:** **100.0%** (9/9 lines)
* **Đánh giá:** Đạt độ bao phủ tuyệt đối 100%, kiểm thử toàn diện mọi luồng phân giải thông tin người dùng từ phiên xác thực nội bộ và mạng xã hội.
