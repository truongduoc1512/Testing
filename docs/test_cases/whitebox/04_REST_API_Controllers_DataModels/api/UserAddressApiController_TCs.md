# BẢNG MA TRẬN KIỂM THỬ HỘP TRẮNG USER ADDRESS API CONTROLLER
**Đường dẫn file mã nguồn:** [UserAddressApiController.java](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/src/main/java/com/example/demo/controller/api/UserAddressApiController.java)  
**Đường dẫn file kiểm thử:** [UserAddressApiControllerTest.java](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/src/test/java/com/example/demo/controller/api/UserAddressApiControllerTest.java)

---

## 1. TỔNG QUAN ĐỘ BAO PHỦ (JACOCO COVERAGE METRICS)

| File Target | Instruction Coverage | Branch Coverage | Line Coverage | Method Coverage | Trạng Thái |
| :--- | :---: | :---: | :---: | :---: | :---: |
| `UserAddressApiController.java` | 98.2% | 95.8% | 100% | 100% | Đạt |

---

## 2. MA TRẬN TEST CASES KIỂM THỬ HỘP TRẮNG (16 TEST CASES)

| Mã TC | Hàm ở src/main | Hàm kiểm thử ở src/test | Giá trị Test đầu vào | Nhánh logic / Điều kiện mục tiêu | Kết quả mong đợi |
| :---: | :--- | :--- | :--- | :--- | :--- |
| **TC_UADDR_01** | `getUserAddresses()` | `getAddresses_rejectsLoginRequiredAuthentication(String, Authentication)` | User chưa xác thực (null, unauthenticated token, anonymousUser) | Nhánh kiểm tra xác thực người dùng thất bại (`auth == null` hoặc chưa authenticated) | HTTP 401 UNAUTHORIZED, body `success: false`, DAO không được gọi |
| **TC_UADDR_02** | `getUserAddresses()` | `getAddresses_returnsAuthenticatedUserList()` | User đã đăng nhập `buyer`, DAO trả về list 2 địa chỉ | Nhánh lấy danh sách sổ địa chỉ của user đã đăng nhập | HTTP 200 OK, trả về danh sách thực thể địa chỉ từ DAO |
| **TC_UADDR_03** | `createAddress(UserAddressForm)` | `createAddress_requiresAuthentication()` | Chưa đăng nhập, form hợp lệ | Nhánh tạo địa chỉ mới yêu cầu đăng nhập | HTTP 401 UNAUTHORIZED, DAO không được gọi |
| **TC_UADDR_04** | `createAddress(UserAddressForm)` | `createAddress_rejectsInvalidForm(String, UserAddressForm)` | User `buyer`, các form vi phạm: null, thiếu hoặc trống họ tên/SĐT/tỉnh/huyện/xã/địa chỉ, hoặc vượt độ dài tối đa | Nhánh kiểm tra biểu mẫu địa chỉ không hợp lệ (`isInvalidForm == true`) | HTTP 400 BAD REQUEST, DAO không được gọi |
| **TC_UADDR_05** | `createAddress(UserAddressForm)` | `createAddress_clearsClientIdAndReturnsCreatedEntity()` | User `buyer`, form hợp lệ kèm `id=99L` và note có khoảng trắng thừa | Nhánh tạo địa chỉ thành công, reset id từ client về null để tránh ghi đè, gọi DAO lưu | HTTP 201 CREATED, `form.getId() == null`, body `success: true` và chứa entity đã lưu |
| **TC_UADDR_06** | `updateAddress(Long, UserAddressForm)` | `updateAddress_requiresAuthentication()` | Chưa đăng nhập, `id=1L`, form hợp lệ | Nhánh cập nhật địa chỉ yêu cầu đăng nhập | HTTP 401 UNAUTHORIZED, DAO không được gọi |
| **TC_UADDR_07** | `updateAddress(Long, UserAddressForm)` | `updateAddress_forbidsMissingAddress()` | User `buyer`, `id=1L`, DAO trả về `null` (không tìm thấy địa chỉ) | Nhánh kiểm tra địa chỉ không tồn tại trong DB (`existing == null`) | HTTP 403 FORBIDDEN, DAO save không được gọi |
| **TC_UADDR_08** | `updateAddress(Long, UserAddressForm)` | `updateAddress_forbidsAddressOwnedByAnotherUser()` | User `buyer`, `id=2L`, DAO trả về địa chỉ thuộc sở hữu user `other` | Nhánh kiểm tra vi phạm bảo mật đa người dùng (`!existing.getUsername().equals(currentUsername)`) | HTTP 403 FORBIDDEN, DAO save không được gọi |
| **TC_UADDR_09** | `updateAddress(Long, UserAddressForm)` | `updateAddress_rejectsInvalidOwnedAddressForm()` | User `buyer`, địa chỉ chính chủ hợp lệ nhưng form sửa thiếu số điện thoại (`phone=null`) | Nhánh dữ liệu biểu mẫu cập nhật địa chỉ không hợp lệ | HTTP 400 BAD REQUEST, DAO save không được gọi |
| **TC_UADDR_10** | `updateAddress(Long, UserAddressForm)` | `updateAddress_storesValidOwnedAddress()` | User `buyer`, địa chỉ chính chủ hợp lệ, form cập nhật hợp lệ | Nhánh cập nhật địa chỉ chính chủ thành công, gán `form.setId(1L)` và lưu | HTTP 200 OK, body `success: true` và chứa entity đã cập nhật |
| **TC_UADDR_11** | `deleteAddress(Long)` | `deleteAddress_requiresAuthentication()` | Chưa đăng nhập, `addressId=1L` | Nhánh xóa địa chỉ yêu cầu đăng nhập | HTTP 401 UNAUTHORIZED, DAO không được gọi |
| **TC_UADDR_12** | `deleteAddress(Long)` | `deleteAddress_returnsForbiddenWhenDaoRejectsDeletion()` | User `buyer`, `addressId=1L`, DAO trả về `false` (địa chỉ không tồn tại hoặc không chính chủ) | Nhánh DAO từ chối xóa do không thỏa quyền sở hữu (`deleted == false`) | HTTP 403 FORBIDDEN, body `success: false` |
| **TC_UADDR_13** | `deleteAddress(Long)` | `deleteAddress_returnsSuccessWhenDaoDeletesAddress()` | User `buyer`, `addressId=2L`, DAO trả về `true` | Nhánh xóa địa chỉ chính chủ thành công | HTTP 200 OK, body `success: true` |
| **TC_UADDR_14** | `setDefaultAddress(Long)` | `setDefault_requiresAuthentication()` | Chưa đăng nhập, `addressId=1L` | Nhánh thiết lập địa chỉ mặc định yêu cầu đăng nhập | HTTP 401 UNAUTHORIZED, DAO không được gọi |
| **TC_UADDR_15** | `setDefaultAddress(Long)` | `setDefault_returnsNotFoundWhenDaoRejectsUpdate()` | User `buyer`, `addressId=1L`, DAO trả về `false` | Nhánh DAO không tìm thấy hoặc cập nhật địa chỉ mặc định thất bại (`updated == false`) | HTTP 404 NOT FOUND, body `success: false` |
| **TC_UADDR_16** | `setDefaultAddress(Long)` | `setDefault_returnsSuccessWhenDaoUpdatesAddress()` | User `buyer`, `addressId=2L`, DAO trả về `true` | Nhánh cập nhật địa chỉ mặc định thành công | HTTP 200 OK, body `success: true` |
