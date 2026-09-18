# ĐẶC TẢ KIỂM THỬ HỘP TRẮNG: `CustomOAuth2UserService`
## Tầng: Service Layer | Phân hệ: Xác thực Đăng nhập Mạng xã hội (Google OAuth2)
* **Tổng số Test Case:** **5 Test Cases**
* **Mục đích tài liệu:** Bảng ánh xạ chi tiết giữa hàm nghiệp vụ trong `CustomOAuth2UserService.java` và các ca kiểm thử trong `CustomOAuth2UserServiceTest.java`, phục vụ tra cứu nhanh giá trị test đầu vào, nhánh logic mục tiêu và kết quả mong đợi.

---

## 📌 PHẦN 1: BẢNG TỔNG HỢP CÁC HÀM TRONG `CustomOAuth2UserService.java`

Lớp `CustomOAuth2UserService` kế thừa `DefaultOAuth2UserService` để xử lý thông tin người dùng sau khi xác thực thành công từ Google OAuth2:

| STT | Tên hàm trong `CustomOAuth2UserService.java` | Mục đích nghiệp vụ | Số Test Case đối ứng |
| :---: | :--- | :--- | :---: |
| 1 | `loadUser(OAuth2UserRequest userRequest)` | Nạp hồ sơ OAuth2, tự động tạo mới tài khoản hoặc cập nhật thông tin tài khoản Google đã có | **5** |

---

## 📑 PHẦN 2: BẢNG MA TRẬN ÁNH XẠ CHI TIẾT 5 TEST CASES

| Mã TC | Hàm ở `src/main` | Hàm kiểm thử ở `src/test` | Giá trị Test đầu vào (Inputs / Mock Data) | Nhánh logic / Điều kiện mục tiêu | Kết quả mong đợi (Assertion) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `TC_OAU_01` | `loadUser` | `loadUser_skipsPersistenceWhenEmailIsMissing` | Case 1: `email = null`, `name = " Google User "`<br>Case 2: `email = "   "`, `name = null`<br>Case 3: Không có key `"name"`, `email = " "` | Nhánh `if (email == null \|\| email.trim().isEmpty())`: bỏ qua lưu CSDL | Trả về `OAuth2User`, không gọi `accountDAO.saveAccount(...)`, fallback tên hiển thị phù hợp |
| `TC_OAU_02` | `loadUser` | `loadUser_createsNewGoogleAccountUsingEmailPrefixAndTrimmedName` | `email = "Buyer@Example.COM"`, `name = " Buyer Name "`, `picture = "avatar.png"`, `sub = "google-sub"`<br>Mock: `findAccountByEmail = null`, `findAccount("Buyer") = null` | Nhánh tài khoản chưa tồn tại: Tạo mới `Account` với username từ prefix email, role `ROLE_USER`, provider `GOOGLE` | `saveAccount` được gọi với username `"Buyer"`, `email = "buyer@example.com"`, `active = true`, `accountNonLocked = true` |
| `TC_OAU_03` | `loadUser` | `loadUser_resolvesUsernameCollisionAndFallsBackToEmailPrefixForNullName` | `email = "buyer@example.com"`, `name = null`<br>Mock: `findAccount("buyer") != null` (trùng username) | Nhánh trùng username khi tạo mới: Thêm suffix ngẫu nhiên dạng `buyer_{timestamp%10000}` và fallback fullName = username | `saveAccount` được gọi với username khớp regex `buyer_[0-9]{1,4}`, `fullName = username` |
| `TC_OAU_04` | `loadUser` | `loadUser_updatesExistingAccountWithLatestNonNullGoogleFields` | `email = "buyer@example.com"`, `name = " New Name "`, `picture = "new.png"`, `sub = "new-sub"`<br>Mock: `findAccountByEmail` trả về Account cũ | Nhánh tài khoản đã tồn tại: Cập nhật fullName mới, avatarUrl mới, providerId và `lastLogin = now()` | `saveAccount` với `fullName = "New Name"`, `avatarUrl = "new.png"`, `providerId = "new-sub"` |
| `TC_OAU_05` | `loadUser` | `loadUser_preservesOptionalExistingFieldsAndUsesUsernameWhenNameIsNull` | `email = "buyer@example.com"`, `name = null`, `picture = null`, `sub = null`<br>Mock: `findAccountByEmail` trả về Account cũ (`old.png`, `old-sub`) | Nhánh cập nhật với các trường Google bị null: Giữ nguyên avatarUrl và providerId cũ, fallback fullName về username | `saveAccount` với `fullName = "buyer"`, giữ nguyên `avatarUrl = "old.png"`, `providerId = "old-sub"` |

---

## 📊 PHẦN 3: KẾT QUẢ ĐO LƯỜNG ĐỘ BAO PHỦ (JACOCO METRICS)

* **Lớp kiểm thử:** `com.example.demo.service.CustomOAuth2UserService`
* **Statement Coverage (Instructions):** **100.0%** (169/169 instructions)
* **Branch Coverage (Branches):** **100.0%** (20/20 branches)
* **Line Coverage:** **100.0%** (37/37 lines)
* **Đánh giá:** Đạt độ bao phủ tuyệt đối 100%, bảo đảm an toàn cho luồng xác thực SSO qua Google và xử lý tránh xung đột dữ liệu tài khoản.
