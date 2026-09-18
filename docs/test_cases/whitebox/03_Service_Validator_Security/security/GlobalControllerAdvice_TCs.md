# ĐẶC TẢ KIỂM THỬ HỘP TRẮNG: `GlobalControllerAdvice`
## Tầng: Security & Configuration Layer | Phân hệ: Tiêm Dữ liệu Định danh Toàn cục (Global Model Advice)
* **Tổng số Test Case:** **13 Test Cases**
* **Mục đích tài liệu:** Bảng ánh xạ chi tiết giữa hàm nghiệp vụ trong `GlobalControllerAdvice.java` và các ca kiểm thử trong `GlobalControllerAdviceTest.java`, phục vụ tra cứu nhanh giá trị test đầu vào, nhánh logic mục tiêu và kết quả mong đợi.

---

## 📌 PHẦN 1: BẢNG TỔNG HỢP CÁC HÀM TRONG `GlobalControllerAdvice.java`

Lớp `GlobalControllerAdvice` đánh dấu `@ControllerAdvice` để tự động bổ sung ảnh đại diện và tên hiển thị người dùng vào mọi View Model trong ứng dụng:

| STT | Tên hàm trong `GlobalControllerAdvice.java` | Mục đích nghiệp vụ | Số Test Case đối ứng |
| :---: | :--- | :--- | :---: |
| 1 | `getUserAvatarUrl()` | Trích xuất link ảnh đại diện (ưu tiên OAuth2, fallback CSDL Account) | **7** |
| 2 | `getUserDisplayName()` | Trích xuất tên hiển thị (ưu tiên OAuth2 name, fallback fullName trong DB, cuối cùng là username) | **6** |

---

## 📑 PHẦN 2: BẢNG MA TRẬN ÁNH XẠ CHI TIẾT 13 TEST CASES

| Mã TC | Hàm ở `src/main` | Hàm kiểm thử ở `src/test` | Giá trị Test đầu vào (Inputs / Mock Data) | Nhánh logic / Điều kiện mục tiêu | Kết quả mong đợi (Assertion) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `TC_GCA_01` | `getUserAvatarUrl`, `getUserDisplayName` | `attributes_areAbsentWhenAuthenticationIsMissing` | `auth = null` (chưa có SecurityContext) | Nhánh `if (auth == null)` | Cả avatar và displayName đều trả về `null` |
| `TC_GCA_02` | `getUserAvatarUrl`, `getUserDisplayName` | `attributes_areAbsentWhenAuthenticationIsInactive` | `auth.isAuthenticated() = false` | Nhánh `if (!auth.isAuthenticated())` | Cả avatar và displayName đều trả về `null`, không query DB |
| `TC_GCA_03` | `getUserAvatarUrl`, `getUserDisplayName` | `attributes_areAbsentForAnonymousAuthentication` | `auth.getName() = "anonymousUser"` | Nhánh `if ("anonymousUser".equals(auth.getPrincipal()))` | Trả về `null` cho cả avatar và displayName |
| `TC_GCA_04` | `getUserAvatarUrl`, `getUserDisplayName` | `oauthNonBlankAttributes_takePriorityOverStoredProfile` | `OAuth2User`: `picture = "avatar.png"`, `name = "OAuth Name"` | Nhánh OAuth2User có sẵn attribute không rỗng: ưu tiên lấy trực tiếp | `avatarUrl = "https://images.example/avatar.png"`, `displayName = "OAuth Name"` |
| `TC_GCA_05` | `getUserAvatarUrl`, `getUserDisplayName` | `oauthBlankAttributes_fallBackToAccountFoundByUsername` | `OAuth2User`: `picture = "   "`, `name = "   "`<br>Mock: `findAccount("buyer")` trả về Account DB | Nhánh OAuth2 rỗng: fallback sang tìm Account trong DB theo username | Lấy từ DB: `avatar = "stored.png"`, `displayName = "Stored Name"` |
| `TC_GCA_06` | `getUserAvatarUrl` | `oauthNullAttributes_useNormalizedEmailForAvatarWhenUsernameMisses`| `OAuth2User`: `picture = null`, `email = "Buyer@Example.COM"`, Mock `findAccount` rỗng nhưng `findAccountByEmail` có | Nhánh fallback email: chuẩn hóa chữ thường và tìm theo email | Trả về `avatar = "stored.png"`, `displayName = "oauth-user"` |
| `TC_GCA_07` | `getUserAvatarUrl`, `getUserDisplayName` | `oauthMissingEmailAndAccounts_returnsNoAvatarAndUsernameAsDisplayName`| `OAuth2User` không có email và không tìm thấy Account trong DB | Nhánh OAuth2 thất bại tìm kiếm toàn bộ | `avatar = null`, `displayName = "oauth-user"` |
| `TC_GCA_08` | `getUserAvatarUrl`, `getUserDisplayName` | `localAccount_nonBlankProfileValuesAreReturned` | Đăng nhập truyền thống (`buyer`), CSDL có đủ thông tin | Nhánh tài khoản nội bộ hợp lệ | Trả về đầy đủ: `avatar = "local.png"`, `displayName = "Local Name"` |
| `TC_GCA_09` | `getUserAvatarUrl`, `getUserDisplayName` | `localAccountMissing_returnsNullAvatarAndUsernameAsDisplayName`| Tài khoản nội bộ không tồn tại trong DB (`findAccount = null`) | Nhánh fallback khi thiếu entity CSDL | `avatar = null`, `displayName = "buyer"` |
| `TC_GCA_10` | `getUserAvatarUrl`, `getUserDisplayName` | `nullStoredProfileValues_useFallbacks` | Tài khoản trong CSDL có `avatarUrl = null`, `fullName = null` | Nhánh giá trị trong DB bị null | `avatar = null`, `displayName = "buyer"` (lấy username làm fallback) |
| `TC_GCA_11` | `getUserAvatarUrl`, `getUserDisplayName` | `blankStoredProfileValues_useFallbacks` | Tài khoản trong CSDL có `avatarUrl = "   "`, `fullName = "   "` | Nhánh giá trị trong DB là khoảng trắng | `avatar = null`, `displayName = "buyer"` |
| `TC_GCA_12` | `getUserAvatarUrl`, `getUserDisplayName` | `nullUsername_doesNotQueryAccount` | `auth.getName() = null` | Nhánh `if (username == null)` | Trả về `null`, không gọi query DB |
| `TC_GCA_13` | `getUserAvatarUrl`, `getUserDisplayName` | `blankUsername_doesNotQueryAccountAndRemainsDisplayFallback`| `auth.getName() = "   "` | Nhánh username chỉ có khoảng trắng | `avatar = null`, `displayName = "   "`, không query DB |

---

## 📊 PHẦN 3: KẾT QUẢ ĐO LƯỜNG ĐỘ BAO PHỦ (JACOCO METRICS)

* **Lớp kiểm thử:** `com.example.demo.config.GlobalControllerAdvice`
* **Statement Coverage (Instructions):** **100.0%** (156/156 instructions)
* **Branch Coverage (Branches):** **100.0%** (50/50 branches)
* **Line Coverage:** **100.0%** (36/36 lines)
* **Đánh giá:** Đạt độ bao phủ tuyệt đối 100%, bảo đảm dữ liệu định danh người dùng được tiêm an toàn vào giao diện không gây ngoại lệ NullPointerException.
