# BẢNG MA TRẬN KIỂM THỬ HỘP TRẮNG FORM DATA TRANSFER OBJECTS (DTOs)
**Đường dẫn package mã nguồn:** [src/main/java/com/example/demo/form/](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/src/main/java/com/example/demo/form/)  
**Đường dẫn file kiểm thử:** [FormCoverageTest.java](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/src/test/java/com/example/demo/form/FormCoverageTest.java)

---

## 1. TỔNG QUAN ĐỘ BAO PHỦ (JACOCO COVERAGE METRICS)

| Form DTO Target | Instruction Coverage | Branch Coverage | Line Coverage | Method Coverage | Trạng Thái |
| :--- | :---: | :---: | :---: | :---: | :---: |
| `CustomerForm.java` | 100% | 100% | 100% | 100% | Đạt |
| `ProductForm.java` | 100% | 100% | 100% | 100% | Đạt |
| `ProductReviewForm.java` | 100% | 100% | 100% | 100% | Đạt |
| `OrderReturnForm.java` | 100% | 100% | 100% | 100% | Đạt |
| `ReturnStatusUpdateForm.java` | 100% | 100% | 100% | 100% | Đạt |
| `RegisterForm.java` | 100% | 100% | 100% | 100% | Đạt |
| `UserProfileForm.java` | 100% | 100% | 100% | 100% | Đạt |
| `UserAddressForm.java` | 100% | 100% | 100% | 100% | Đạt |
| `VoucherForm.java` | 100% | 100% | 100% | 100% | Đạt |

---

## 2. MA TRẬN TEST CASES KIỂM THỬ HỘP TRẮNG (20 TEST CASES)

| Mã TC | Lớp Form ở src/main | Hàm kiểm thử ở src/test | Giá trị Test đầu vào | Nhánh logic / Điều kiện mục tiêu | Kết quả mong đợi |
| :---: | :--- | :--- | :--- | :--- | :--- |
| **TC_FRM_01** | `CustomerForm.java` | `customerForm_acceptsNullSource()` | Constructor nhận đối tượng nguồn `null` | Nhánh khởi tạo `CustomerForm` khi đối tượng `CustomerInfo` nguồn là null | Khởi tạo an toàn, thuộc tính không gây `NullPointerException` |
| **TC_FRM_02** | `CustomerForm.java` | `customerForm_mapsCustomerInfo()` | `CustomerInfo` hợp lệ (name, address, email, phone) | Nhánh ánh xạ (mapping) thuộc tính từ `CustomerInfo` sang biểu mẫu `CustomerForm` | Toàn bộ các trường sao chép chính xác |
| **TC_FRM_03** | `CustomerForm.java` | `customerForm_mutablePropertiesRoundTrip()` | Gán name, address, email, phone, valid qua setter | Nhánh getter/setter các thuộc tính của `CustomerForm` | Getter trả về chính xác giá trị đã thiết lập |
| **TC_FRM_04** | `ProductForm.java` | `productForm_usesNewProductDefaults()` | Khởi tạo qua default constructor | Nhánh giá trị mặc định cho biểu mẫu tạo sản phẩm mới | `isNewProduct() == true`, `stockQuantity == 100` |
| **TC_FRM_05** | `ProductForm.java` | `productForm_mapsProductEntity()` | Thực thể `Product` có sẵn trong DB (code, name, price, discount, stock) | Nhánh ánh xạ từ thực thể `Product` sang `ProductForm` phục vụ chỉnh sửa | `isNewProduct() == false`, các giá trị thuộc tính ánh xạ đúng |
| **TC_FRM_06** | `ProductForm.java` | `productForm_mutablePropertiesAndUploadedFileRoundTrip()` | Thay đổi thuộc tính form và file đính kèm `MockMultipartFile` | Nhánh lưu giữ dữ liệu file ảnh upload và các thuộc tính form | Getter/setter phản ánh chính xác thông tin sản phẩm và multipart file |
| **TC_FRM_07** | `ProductReviewForm.java` | `productReviewForm_usesDefaultRatingAndBindsConstructorProductCode()` | Default constructor và constructor nhận `productCode="P1"` | Nhánh thiết lập số sao mặc định và constructor nhận mã sản phẩm | Số sao mặc định `ratingValue == 5`, mã sản phẩm gán đúng |
| **TC_FRM_08** | `ProductReviewForm.java` | `productReviewForm_mutablePropertiesRoundTrip()` | Setter productCode, ratingValue, comment, username | Nhánh getter/setter biểu mẫu đánh giá sản phẩm | Thuộc tính lưu trữ chính xác |
| **TC_FRM_09** | `OrderReturnForm.java` | `orderReturnForm_constructorExposesValues()` | Constructor `(reason, imageUrls)` | Nhánh khởi tạo `OrderReturnForm` qua constructor tiện ích | Reason và imageUrls được gán đúng |
| **TC_FRM_10** | `OrderReturnForm.java` | `orderReturnForm_mutablePropertiesRoundTrip()` | Default constructor và các setter reason, imageUrls | Nhánh cập nhật thuộc tính lý do trả hàng và ảnh minh chứng | Getter phản ánh đúng dữ liệu cập nhật |
| **TC_FRM_11** | `ReturnStatusUpdateForm.java` | `returnStatusUpdateForm_constructorExposesValues()` | Constructor `(action, adminNote)` | Nhánh khởi tạo biểu mẫu duyệt đơn trả hàng của admin | Action và ghi chú admin gán chính xác |
| **TC_FRM_12** | `ReturnStatusUpdateForm.java` | `returnStatusUpdateForm_mutablePropertiesRoundTrip()` | Gán action và adminNote qua setter | Nhánh getter/setter biểu mẫu cập nhật trạng thái trả hàng | Getter phản ánh chính xác giá trị gán |
| **TC_FRM_13** | `RegisterForm.java` | `registerForm_constructorExposesValues()` | Constructor 4 tham số: username, email, password, confirmPassword | Nhánh khởi tạo biểu mẫu đăng ký tài khoản | Tất cả thuộc tính được thiết lập đúng |
| **TC_FRM_14** | `RegisterForm.java` | `registerForm_mutablePropertiesRoundTrip()` | Setter username, email, password, confirmPassword | Nhánh getter/setter biểu mẫu đăng ký tài khoản | Getter trả về chính xác giá trị đã gán |
| **TC_FRM_15** | `UserProfileForm.java` | `userProfileForm_usesActiveUnlockedDefaults()` | Khởi tạo qua default constructor | Nhánh thiết lập trạng thái mặc định của hồ sơ người dùng | `isActive() == true`, `isAccountNonLocked() == true` |
| **TC_FRM_16** | `UserProfileForm.java` | `userProfileForm_retainsEveryEditableValue()` | Gán họ tên, email, phone, avatar, role, provider, flags và các trường đổi mật khẩu | Nhánh lưu giữ đầy đủ các trường thông tin cá nhân và bảo mật | Tất cả trường lưu trữ toàn vẹn |
| **TC_FRM_17** | `UserAddressForm.java` | `userAddressForm_retainsEveryEditableValue()` | Gán id, receiverName, phone, tỉnh, huyện, xã, đường, ghi chú, isDefault | Nhánh lưu giữ thông tin biểu mẫu địa chỉ giao hàng | Toàn bộ thuộc tính phản ánh chính xác dữ liệu đã gán |
| **TC_FRM_18** | `VoucherForm.java` | `voucherForm_usesExpectedDefaults()` | Khởi tạo qua default constructor | Nhánh thiết lập giá trị mặc định cho biểu mẫu voucher mới | Loại PERCENT, active=true, giới hạn 100 lượt, 1 lượt/user, expiryDate=null |
| **TC_FRM_19** | `VoucherForm.java` | `voucherForm_mutableDiscountLimitAndStateFieldsRoundTrip()` | Gán code, discountType, discountValue, maxDiscount, minOrder, active, usageLimit, perUserLimit | Nhánh cập nhật các giới hạn chiết khấu và điều kiện áp dụng voucher | Getter phản ánh đúng toàn bộ các tham số cấu hình |
| **TC_FRM_20** | `VoucherForm.java` | `voucherForm_defensivelyCopiesAndAcceptsNullExpiryDate()` | Gán hạn dùng `expiryDate` bằng Date mutable và gán null | Nhánh sao chép phòng thủ Date hạn dùng voucher của VoucherForm | Độc lập với biến Date bên ngoài, chấp nhận null an toàn |
