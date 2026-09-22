# BẢNG MA TRẬN KIỂM THỬ HỘP TRẮNG USER API CONTROLLER
**Đường dẫn file mã nguồn:** [UserApiController.java](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/src/main/java/com/example/demo/controller/api/UserApiController.java)  
**Đường dẫn file kiểm thử:** [UserApiControllerTest.java](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/src/test/java/com/example/demo/controller/api/UserApiControllerTest.java)

---

## 1. TỔNG QUAN ĐỘ BAO PHỦ (JACOCO COVERAGE METRICS)

| File Target | Instruction Coverage | Branch Coverage | Line Coverage | Method Coverage | Trạng Thái |
| :--- | :---: | :---: | :---: | :---: | :---: |
| `UserApiController.java` | 98.4% | 96.2% | 100% | 100% | Đạt |

---

## 2. MA TRẬN TEST CASES KIỂM THỬ HỘP TRẮNG (21 TEST CASES)

| Mã TC | Hàm ở src/main | Hàm kiểm thử ở src/test | Giá trị Test đầu vào | Nhánh logic / Điều kiện mục tiêu | Kết quả mong đợi |
| :---: | :--- | :--- | :--- | :--- | :--- |
| **TC_USR_01** | `getUsers(int)` | `getUsers_usesNormalizedPage(int, int)` | `page = -5` (chuẩn hóa về 1), `page = 2` | Nhánh chuẩn hóa trang `page < 1 ? 1 : page` khi phân trang danh sách tài khoản | Trả về `PaginationResult<Account>` đúng trang tương ứng, gọi DAO với trang đã chuẩn hóa |
| **TC_USR_02** | `getUserByUsername(String)` | `getUserByUsername_returnsNotFoundWhenAccountDoesNotExist()` | `username = "missing"`, DAO trả về `null` | Nhánh tài khoản người dùng không tồn tại (`account == null`) | HTTP 404 NOT FOUND |
| **TC_USR_03** | `getUserByUsername(String)` | `getUserByUsername_returnsExistingAccount()` | `username = "buyer"`, DAO trả về thực thể `Account` | Nhánh tìm thấy thông tin tài khoản hợp lệ | HTTP 200 OK, trả về thực thể `Account` |
| **TC_USR_04** | `registerUser(RegisterForm)` | `register_rejectsInvalidForm(String, RegisterForm)` | Các form lỗi: null, username trống/quá dài, email trống/sai định dạng, password trống/ngắn (<8)/quá dài (>72), confirm sai | Nhánh kiểm tra tính hợp lệ của biểu mẫu đăng ký (`isInvalidForm == true`) | HTTP 400 BAD REQUEST, DAO không được gọi |
| **TC_USR_05** | `registerUser(RegisterForm)` | `register_rejectsExistingUsernameWithoutCheckingEmail()` | `username = "buyer"` đã tồn tại trong DB | Nhánh trùng tên tài khoản (short-circuit không cần kiểm tra email trùng) | HTTP 400 BAD REQUEST, không gọi `findAccountByEmail`, không lưu tài khoản |
| **TC_USR_06** | `registerUser(RegisterForm)` | `register_rejectsExistingNormalizedEmail()` | `username = "other"`, email `Buyer@Example.COM` trùng với email đã tồn tại | Nhánh kiểm tra email đã được đăng ký (`accountDAO.findAccountByEmail(email) != null`) | HTTP 400 BAD REQUEST, không lưu tài khoản |
| **TC_USR_07** | `registerUser(RegisterForm)` | `register_acceptsInclusiveBoundariesAndNormalizesSavedAccount()` | `username` 50 ký tự kèm space thừa, `password` 72 ký tự, email có chữ hoa | Nhánh đăng ký tài khoản thành công ở giá trị biên, chuẩn hóa trim/lowercase và hash mật khẩu | HTTP 201 CREATED, tài khoản lưu với role USER, provider LOCAL, mã hóa BCrypt |
| **TC_USR_08** | `registerUser(RegisterForm)` | `register_returnsServerErrorWhenPersistenceFails()` | Form hợp lệ, DAO throw `IllegalStateException("database unavailable")` | Nhánh xử lý ngoại lệ lỗi cơ sở dữ liệu khi lưu tài khoản mới | HTTP 500 INTERNAL SERVER ERROR, body `success: false` |
| **TC_USR_09** | `getCurrentUserProfile()` | `currentProfile_rejectsLoginRequiredAuthentication(String, Authentication)` | User chưa đăng nhập (null, unauthenticated token, anonymousUser) | Nhánh kiểm tra xác thực người dùng khi xem thông tin cá nhân | HTTP 401 UNAUTHORIZED, không gọi resolve account |
| **TC_USR_10** | `getCurrentUserProfile()` | `currentProfile_returnsNotFoundWhenAuthenticatedAccountCannotBeResolved()` | User đã đăng nhập nhưng `authenticatedAccountService.resolve(auth)` trả về `null` | Nhánh không tìm thấy thông tin tài khoản trong DB tương ứng token | HTTP 404 NOT FOUND |
| **TC_USR_11** | `getCurrentUserProfile()` | `currentProfile_returnsResolvedAccount()` | User `buyer` đã đăng nhập, resolve trả về `Account` | Nhánh lấy thông tin cá nhân của người dùng hiện tại thành công | HTTP 200 OK, trả về thông tin `Account` |
| **TC_USR_12** | `updateUserProfile(UserProfileForm)` | `updateProfile_rejectsLoginRequiredAuthentication(String, Authentication)` | User chưa đăng nhập, form profile hợp lệ | Nhánh cập nhật thông tin cá nhân yêu cầu đăng nhập | HTTP 401 UNAUTHORIZED, không resolve account |
| **TC_USR_13** | `updateUserProfile(UserProfileForm)` | `updateProfile_returnsNotFoundWhenAuthenticatedAccountCannotBeResolved()` | Đã đăng nhập nhưng resolve account trả về `null` | Nhánh không tìm thấy thực thể tài khoản để cập nhật thông tin | HTTP 404 NOT FOUND, không gọi validate |
| **TC_USR_14** | `updateUserProfile(UserProfileForm)` | `updateProfile_returnsValidationErrorWithoutApplyingChanges()` | Form cập nhật vi phạm (service validation trả về thông báo lỗi) | Nhánh kiểm tra dữ liệu hồ sơ không hợp lệ (`validationError != null`) | HTTP 400 BAD REQUEST, không áp dụng thay đổi, không lưu DB |
| **TC_USR_15** | `updateUserProfile(UserProfileForm)` | `updateProfile_appliesAndReturnsPersistedAccount()` | Form hợp lệ, validation qua (`null`), DAO lưu và trả về tài khoản đã cập nhật | Nhánh cập nhật hồ sơ người dùng thành công | HTTP 200 OK, trả về thực thể `Account` đã được cập nhật |
| **TC_USR_16** | `updateUserProfile(UserProfileForm)` | `updateProfile_returnsServerErrorWhenValidationThrows()` | Service validate throw `IllegalStateException("failure")` | Nhánh bắt ngoại lệ không mong muốn trong quá trình cập nhật hồ sơ | HTTP 500 INTERNAL SERVER ERROR, body `success: false` |
| **TC_USR_17** | `changePassword(Map)` | `changePassword_rejectsLoginRequiredAuthentication(String, Authentication)` | User chưa đăng nhập, payload đổi mật khẩu | Nhánh đổi mật khẩu yêu cầu đăng nhập | HTTP 401 UNAUTHORIZED, không resolve account |
| **TC_USR_18** | `changePassword(Map)` | `changePassword_rejectsInvalidPayload(String, Map)` | Payload lỗi: null, thiếu mật khẩu cũ/mới, mật khẩu mới trống/<8 ký tự, confirm không khớp | Nhánh kiểm tra tính hợp lệ dữ liệu yêu cầu đổi mật khẩu | HTTP 400 BAD REQUEST, không resolve account |
| **TC_USR_19** | `changePassword(Map)` | `changePassword_returnsNotFoundWhenAuthenticatedAccountCannotBeResolved()` | Đã đăng nhập nhưng resolve account trả về `null` | Nhánh không tìm thấy tài khoản người dùng đổi mật khẩu | HTTP 404 NOT FOUND |
| **TC_USR_20** | `changePassword(Map)` | `changePassword_rejectsIncorrectOldPassword()` | Mật khẩu cũ nhập vào không khớp với hash trong DB (`matches == false`) | Nhánh kiểm tra mật khẩu hiện tại không chính xác | HTTP 400 BAD REQUEST, body báo sai mật khẩu cũ, không lưu |
| **TC_USR_21** | `changePassword(Map)` | `changePassword_encodesAndSavesNewPassword()` | Mật khẩu cũ đúng, mật khẩu mới hợp lệ và xác nhận trùng khớp | Nhánh đổi mật khẩu thành công, hash mật khẩu mới và lưu vào DB | HTTP 200 OK, body `success: true`, cập nhật encryptedPassword mới |
