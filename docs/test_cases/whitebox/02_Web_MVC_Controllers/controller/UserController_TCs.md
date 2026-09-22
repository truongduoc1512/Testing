# ĐẶC TẢ KIỂM THỬ HỘP TRẮNG: `UserController`
## Tầng: Web MVC Controller | Phân hệ: Xác thực, Hồ sơ cá nhân & Quản trị tài khoản
* **Tổng số Test Case:** **57 Test Cases**
* **Mục đích tài liệu:** Bảng ánh xạ chi tiết giữa các hàm nghiệp vụ trong `UserController.java` và các hàm kiểm thử trong `UserControllerCoverageTest.java`, phục vụ tra cứu nhanh giá trị test đầu vào, nhánh logic mục tiêu và kết quả mong đợi.

---

## 📌 PHẦN 1: BẢNG TỔNG HỢP CÁC HÀM TRONG `UserController.java`

Lớp `UserController` quản lý 10 nhóm hàm nghiệp vụ giao diện web:

| STT | Tên hàm trong `UserController.java` | Mục đích nghiệp vụ | Số Test Case đối ứng |
| :---: | :--- | :--- | :---: |
| 1 | `login(...)` & `initBinder(...)` | Hiển thị trang đăng nhập và đăng ký validator | **2** |
| 2 | `registerPage(...)` & `registerSave(...)` | Đăng ký tài khoản người dùng mới | **4** |
| 3 | `forgotPasswordPage(...)` & `forgotPassword(...)` | Gửi yêu cầu quên mật khẩu, cấp phát reset token | **4** |
| 4 | `resetPasswordPage(...)` & `processResetPassword(...)`| Xác thực token và đặt lại mật khẩu mới | **7** |
| 5 | `accountInfo(...)` | Hiển thị bảng thông tin tài khoản và thống kê cá nhân | **4** |
| 6 | `userProfile(...)` | Hiển thị form cập nhật hồ sơ người dùng hiện tại | **5** |
| 7 | `profileSave(...)` | Cập nhật hồ sơ, đổi mật khẩu (chặn tài khoản Google) | **11** |
| 8 | `userList(...)` | Admin xem danh sách toàn bộ người dùng phân trang | **3** |
| 9 | `userEdit(...)` | Admin nạp form sửa thông tin và quyền hạn người dùng | **5** |
| 10 | `userEditSave(...)` | Admin cập nhật tài khoản (bảo vệ Admin cuối cùng) | **12** |

---

## 📑 PHẦN 2: BẢNG MA TRẬN ÁNH XẠ CHI TIẾT 57 TEST CASES

| Mã TC | Hàm ở `src/main` | Hàm kiểm thử ở `src/test` | Giá trị Test đầu vào (Inputs / Mock Data) | Nhánh logic / Điều kiện mục tiêu | Kết quả mong đợi (Assertion) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `TC_USR_CTL_01` | `login` | `login_returnsLoginView` | Không yêu cầu tham số | Nhánh hiển thị giao diện đăng nhập | Trả về view name `"login"` |
| `TC_USR_CTL_02` | `initBinder` | `initBinder_registersAppUserValidator` | `WebDataBinder` mới khởi tạo | Nhánh đăng ký custom validator | Binder chứa validator `AppUserValidator` |
| `TC_USR_CTL_03` | `registerPage` | `registerPage_bindsFreshModel` | Chưa có form model trong model map | Nhánh tạo mới form `AppUser` | Model map chứa attribute `"appUser"` |
| `TC_USR_CTL_04` | `registerPage` | `registerPage_reusesExistingModel` | Đã có sẵn model `AppUser` | Nhánh giữ lại model hiện tại | Trả về view `"register"` |
| `TC_USR_CTL_05` | `registerSave` | `registerSave_returnsFormForBindingErrors` | Form đăng ký có lỗi validate | Nhánh `if (result.hasErrors())` = True | Trả về view `register` kèm lỗi |
| `TC_USR_CTL_06` | `registerSave` | `registerSave_normalizesAndPersistsValidAccount` | Form đăng ký hợp lệ | Lưu tài khoản mới vào CSDL | Redirect sang `/login` |
| `TC_USR_CTL_07` | `registerSave` | `registerSave_returnsFormWithErrorWhenAccountSaveFails`| Lỗi CSDL khi lưu tài khoản | Bắt ngoại lệ lưu tài khoản | Trả về form kèm lỗi hệ thống |
| `TC_USR_CTL_08` | `forgotPassword` | `forgotPassword_rejectsMissingEmail` | `email = null` | Nhánh kiểm tra email null | Báo lỗi email bắt buộc |
| `TC_USR_CTL_09` | `forgotPassword` | `forgotPassword_rejectsBlankEmail` | `email = "   "` | Nhánh kiểm tra email rỗng | Báo lỗi email bắt buộc |
| `TC_USR_CTL_10` | `forgotPassword` | `forgotPassword_reportsUnknownAccount` | Email không có trong CSDL | Nhánh `if (account == null)` | Báo lỗi không tìm thấy tài khoản |
| `TC_USR_CTL_11` | `forgotPassword` | `forgotPassword_persistsResetTokenForKnownAccount` | Email hợp lệ | Cấp phát token reset và lưu DB | Redirect sang trang nhập token |
| `TC_USR_CTL_12` | `resetPasswordPage` | `resetPasswordPage_showsFormWithoutToken` | Không truyền token trên URL | Nhánh hiển thị form nhập token | Render view `resetPassword` |
| `TC_USR_CTL_13` | `resetPasswordPage` | `resetPasswordPage_showsFormForBlankToken` | `token = "   "` | Nhánh token trắng | Render view `resetPassword` |
| `TC_USR_CTL_14` | `resetPasswordPage` | `resetPasswordPage_redirectsInvalidToken` | Token không khớp hoặc hết hạn | Nhánh token không hợp lệ | Redirect về `/forgotPassword` |
| `TC_USR_CTL_15` | `resetPasswordPage` | `resetPasswordPage_addsNormalizedValidToken` | Token hợp lệ còn hạn | Nạp token vào model form | Render view với token chuẩn hóa |
| `TC_USR_CTL_16` | `processResetPassword`| `processResetPassword_rejectsInvalidRequest` | Token null/trắng, pass rỗng hoặc confirm pass không khớp | Nhánh validate request reset pass | Báo lỗi validation, render view `resetPassword` |
| `TC_USR_CTL_17` | `processResetPassword`| `processResetPassword_reportsInvalidToken` | Reset mật khẩu với token sai | Nhánh reset thất bại từ DAO | Báo lỗi mã token không hợp lệ |
| `TC_USR_CTL_18` | `processResetPassword`| `processResetPassword_redirectsAfterSuccessfulReset` | Token đúng, pass mới hợp lệ | Đổi mật khẩu thành công | Redirect sang `/login` kèm thông báo |
| `TC_USR_CTL_19` | `accountInfo` | `accountInfo_usesEmptyIdentityWithoutAuthentication` | Chưa đăng nhập | Nhánh `auth == null` | Hiển thị trang info rỗng |
| `TC_USR_CTL_20` | `accountInfo` | `accountInfo_fallsBackToAuthenticationNameForUnresolvedAccount`| Account không tìm thấy trong DB | Lấy tên từ Principal fallback | Hiển thị tên từ token auth |
| `TC_USR_CTL_21` | `accountInfo` | `accountInfo_usesResolvedAccountAndUserStatistics` | User hợp lệ | Nạp thống kê đơn hàng cá nhân | Hiển thị dashboard cá nhân |
| `TC_USR_CTL_22` | `accountInfo` | `accountInfo_recognizesAdminRole` | Tài khoản có `ROLE_ADMIN` | Nhánh nhận diện quyền Admin | Hiển thị dashboard quản trị |
| `TC_USR_CTL_23` | `userProfile` | `userProfile_redirectsWithoutAuthentication` | `auth = null` khi xem profile | Nhánh chưa đăng nhập | Redirect sang `/login` |
| `TC_USR_CTL_24` | `userProfile` | `userProfile_redirectsUnauthenticatedToken` | `auth.isAuthenticated() = false` | Nhánh token chưa xác thực | Redirect sang `/login` |
| `TC_USR_CTL_25` | `userProfile` | `userProfile_redirectsWhenAccountCannotBeResolved` | Không tìm thấy entity User | Nhánh không có trong CSDL | Redirect về `/` |
| `TC_USR_CTL_26` | `userProfile` | `userProfile_usesUsernameWhenFullNameIsMissing` | User chưa cập nhật fullName | Lấy username làm tên hiển thị | Nạp username vào form |
| `TC_USR_CTL_27` | `userProfile` | `userProfile_mapsExistingFullName` | User đã có fullName | Nạp họ tên đầy đủ vào form | Render view `userProfile` |
| `TC_USR_CTL_28` | `profileSave` | `profileSave_redirectsWithoutAuthentication` | `auth = null` lúc submit | Chặn khách vãng lai sửa | Redirect sang `/login` |
| `TC_USR_CTL_29` | `profileSave` | `profileSave_redirectsUnauthenticatedToken` | Token chưa xác thực | Chặn submit trái phép | Redirect sang `/login` |
| `TC_USR_CTL_30` | `profileSave` | `profileSave_reportsUnresolvedAccount` | Không tìm thấy entity User | Báo lỗi tài khoản không tồn tại | Trả về form profile kèm lỗi |
| `TC_USR_CTL_31` | `profileSave` | `profileSave_reportsServiceValidationError` | Service ném lỗi validation | Bắt lỗi nghiệp vụ service | Hiển thị thông báo lỗi trên form |
| `TC_USR_CTL_32` | `profileSave` | `profileSave_rejectsMissingOldPasswordForLocalAccount`| Đổi pass mà không nhập pass cũ | Nhánh kiểm tra pass cũ bắt buộc | Báo lỗi thiếu mật khẩu cũ |
| `TC_USR_CTL_33` | `profileSave` | `profileSave_rejectsWrongOldPasswordForLocalAccount` | Nhập sai mật khẩu cũ | Nhánh so khớp hash mật khẩu cũ | Báo lỗi mật khẩu cũ không đúng |
| `TC_USR_CTL_34` | `profileSave` | `profileSave_rejectsNewPasswordConfirmationMismatch` | Xác nhận mật khẩu mới không khớp | Nhánh `!newPass.equals(confirmPass)` | Báo lỗi xác nhận pass sai |
| `TC_USR_CTL_35` | `profileSave` | `profileSave_updatesLocalPasswordAndProfile` | Nhập đúng pass cũ và pass mới | Cập nhật mật khẩu mã hóa BCrypt | Lưu thành công, thông báo xanh |
| `TC_USR_CTL_36` | `profileSave` | `profileSave_skipsPasswordChangeForGoogleAccount` | Tài khoản đăng nhập bằng Google | Nhánh tài khoản OAuth2 | Bỏ qua bước đổi pass, lưu profile |
| `TC_USR_CTL_37` | `profileSave` | `profileSave_treatsBlankNewPasswordAsUnchanged` | Mật khẩu mới để trống | Nhánh giữ nguyên mật khẩu cũ | Chỉ cập nhật thông tin cá nhân |
| `TC_USR_CTL_38` | `profileSave` | `profileSave_preservesPasswordWhenNoNewPasswordWasSubmitted`| Không nhập gì ở ô pass mới | Giữ nguyên hash mật khẩu | Cập nhật profile thành công |
| `TC_USR_CTL_39` | `userList` | `userList_redirectsWithoutAuthentication` | Chưa login vào trang quản trị user | Nhánh chưa đăng nhập | Redirect sang `/login` |
| `TC_USR_CTL_40` | `userList` | `userList_redirectsNonAdmin` | Customer cố vào trang quản trị user | Nhánh `!hasRole("ROLE_ADMIN")` | Chặn truy cập, redirect sang `/403` |
| `TC_USR_CTL_41` | `userList` | `userList_normalizesInvalidPageForAdmin` | Admin truy cập, `page = -5` | Chuẩn hóa page về trang 1 | Render view `userList` phân trang |
| `TC_USR_CTL_42` | `userEdit` | `userEdit_redirectsWithoutAuthentication` | Chưa login vào trang sửa user | Chặn truy cập | Redirect sang `/login` |
| `TC_USR_CTL_43` | `userEdit` | `userEdit_redirectsNonAdmin` | User thường cố sửa tài khoản | Chặn quyền quản trị | Redirect sang `/403` |
| `TC_USR_CTL_44` | `userEdit` | `userEdit_redirectsMissingAccount` | Username cần sửa không tồn tại | Nhánh tài khoản không tìm thấy | Redirect về `/admin/userList` |
| `TC_USR_CTL_45` | `userEdit` | `userEdit_usesUsernameWhenFullNameIsMissing` | Tài khoản cần sửa chưa có tên | Fallback về username | Nạp username vào form |
| `TC_USR_CTL_46` | `userEdit` | `userEdit_mapsExistingFullName` | Tài khoản có họ tên đầy đủ | Nạp họ tên vào form sửa | Render view `userEdit` |
| `TC_USR_CTL_47` | `userEditSave` | `userEditSave_redirectsWithoutAuthentication` | Chưa login bấm lưu sửa user | Chặn submit | Redirect sang `/login` |
| `TC_USR_CTL_48` | `userEditSave` | `userEditSave_redirectsNonAdmin` | Không phải Admin bấm lưu | Chặn quyền quản trị | Redirect sang `/403` |
| `TC_USR_CTL_49` | `userEditSave` | `userEditSave_reportsMissingAccount` | Tài khoản cần lưu không tồn tại | Báo lỗi tài khoản không tìm thấy | Trả về form kèm thông báo lỗi |
| `TC_USR_CTL_50` | `userEditSave` | `userEditSave_reportsServiceValidationError` | Lỗi validate dữ liệu từ service | Hiển thị lỗi nghiệp vụ | Trả về form sửa |
| `TC_USR_CTL_51` | `userEditSave` | `userEditSave_rejectsInvalidRole` | Gán role lạ không tồn tại | Nhánh kiểm tra role hợp lệ | Báo lỗi vai trò không hợp lệ |
| `TC_USR_CTL_52` | `userEditSave` | `userEditSave_blocksLastActiveAdminFromLosingAdminRole` | Hạ quyền Admin cuối cùng về User | Nhánh bảo vệ Admin cuối (`admins <= 1`) | Chặn hạ quyền Admin duy nhất |
| `TC_USR_CTL_53` | `userEditSave` | `userEditSave_allowsAdminDowngradeWhenAnotherActiveAdminExists` | Hệ thống còn Admin active khác | Cho phép hạ quyền Admin này | Hạ quyền thành công |
| `TC_USR_CTL_54` | `userEditSave` | `userEditSave_deactivatesAdminWhenAnotherActiveAdminExists`| Vô hiệu hóa Admin khi còn Admin khác | Cho phép set `active = false` | Deactivate Admin thành công |
| `TC_USR_CTL_55` | `userEditSave` | `userEditSave_locksAdminWhenAnotherActiveAdminExists` | Khóa Admin khi còn Admin khác | Cho phép khóa tài khoản | Khóa Admin thành công |
| `TC_USR_CTL_56` | `userEditSave` | `userEditSave_updatesNormalUserWithoutCountingAdmins` | Sửa tài khoản User thường | Bỏ qua bước đếm số lượng Admin | Cập nhật user thành công |
| `TC_USR_CTL_57` | `userEditSave` | `userEditSave_keepsActiveUnlockedAdminWithoutCountingAdmins`| Giữ nguyên quyền Admin active | Bỏ qua đếm admin | Cập nhật thông tin thành công |

---

## 📊 PHẦN 3: KẾT QUẢ ĐO LƯỜNG ĐỘ BAO PHỦ (JACOCO METRICS)

* **Lớp kiểm thử:** `com.example.demo.controller.UserController`
* **Statement Coverage (Instructions):** **100.0%**
* **Branch Coverage (Branches):** **100.0%**
* **Đánh giá:** Đạt độ phủ tuyệt đối 100%. Bao phủ toàn bộ các ràng buộc nghiệp vụ bảo vệ Admin cuối cùng và phân quyền tài khoản.
