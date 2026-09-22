# BẢNG MA TRẬN KIỂM THỬ HỘP TRẮNG DOMAIN ENTITIES
**Đường dẫn package mã nguồn:** [src/main/java/com/example/demo/entity/](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/src/main/java/com/example/demo/entity/)  
**Đường dẫn file kiểm thử:** [EntityCoverageTest.java](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/src/test/java/com/example/demo/entity/EntityCoverageTest.java)

---

## 1. TỔNG QUAN ĐỘ BAO PHỦ (JACOCO COVERAGE METRICS)

| Entity Target | Instruction Coverage | Branch Coverage | Line Coverage | Method Coverage | Trạng Thái |
| :--- | :---: | :---: | :---: | :---: | :---: |
| `Account.java` | 100% | 100% | 100% | 100% | Đạt |
| `Product.java` | 100% | 100% | 100% | 100% | Đạt |
| `UserAddress.java` | 100% | 100% | 100% | 100% | Đạt |
| `Order.java` | 100% | 100% | 100% | 100% | Đạt |
| `OrderDetail.java` | 100% | 100% | 100% | 100% | Đạt |
| `OrderReturn.java` | 100% | 100% | 100% | 100% | Đạt |
| `Voucher.java` | 100% | 100% | 100% | 100% | Đạt |
| `VoucherUsage.java` | 100% | 100% | 100% | 100% | Đạt |
| `Wishlist.java` | 100% | 100% | 100% | 100% | Đạt |
| `ProductReview.java` | 100% | 100% | 100% | 100% | Đạt |

---

## 2. MA TRẬN TEST CASES KIỂM THỬ HỘP TRẮNG (20 TEST CASES)

| Mã TC | Thực thể ở src/main | Hàm kiểm thử ở src/test | Giá trị Test đầu vào | Nhánh logic / Điều kiện mục tiêu | Kết quả mong đợi |
| :---: | :--- | :--- | :--- | :--- | :--- |
| **TC_ENT_01** | `Account.java` | `account_exposesProfileSecurityFieldsAndDefensivelyCopiesDates()` | Các trường profile, security role, mật khẩu hash, các mốc Date mutable và null | Nhánh sao chép phòng thủ (defensive copy) ngày tháng và getter/setter trạng thái bảo mật của Account | Các trường dữ liệu toàn vẹn, mutate Date bên ngoài không ảnh hưởng đến Date nội bộ, null an toàn |
| **TC_ENT_02** | `Product.java` | `product_copiesBinaryAndDateValuesAndSupportsCatalogueFields()` | Các trường thông tin sản phẩm, mảng byte ảnh `byte[]`, ngày tạo/cập nhật, null | Nhánh sao chép phòng thủ mảng byte hình ảnh, Date và các trường danh mục sản phẩm | Mảng byte ảnh và Date được cô lập bản sao an toàn; getter/setter hoạt động chính xác |
| **TC_ENT_03** | `UserAddress.java` | `userAddress_constructorPopulatesFieldsAndTimestamps()` | Tham số constructor đầy đủ: username, receiverName, phone, tỉnh/huyện/xã/đường, isDefault=true | Nhánh khởi tạo UserAddress qua full-argument constructor | Tất cả trường được gán chính xác, `createdAt` và `updatedAt` tự động sinh khác null |
| **TC_ENT_04** | `UserAddress.java` | `userAddress_mutablePropertiesAndDatesRoundTrip()` | Thay đổi các thuộc tính địa chỉ qua setter kèm mốc Date | Nhánh sao chép phòng thủ Date và các thuộc tính có thể thay đổi của UserAddress | Getter trả về đúng giá trị vừa gán; Date được copy an toàn; null không throw Exception |
| **TC_ENT_05** | `UserAddress.java` | `userAddress_fullAddressJoinsEveryComponent()` | Địa chỉ đầy đủ 4 thành phần: Street, Ward, District, Province | Nhánh nối toàn bộ các thành phần địa chỉ khi tất cả đều có giá trị | Trả về chuỗi `"Street, Ward, District, Province"` |
| **TC_ENT_06** | `UserAddress.java` | `userAddress_fullAddressIsEmptyWhenEveryComponentIsMissing()` | Địa chỉ có cả 4 thành phần Street, Ward, District, Province đều null | Nhánh xử lý khi tất cả các thành phần địa chỉ đều khuyết thiếu | Trả về chuỗi rỗng `""` |
| **TC_ENT_07** | `UserAddress.java` | `userAddress_fullAddressOmitsMissingComponents()` | Địa chỉ chỉ có Province `"Only province"`, các trường khác null | Nhánh lọc bỏ các thành phần null trong chuỗi địa chỉ | Trả về chuỗi `"Only province"` (không có dấu phẩy thừa) |
| **TC_ENT_08** | `Order.java` | `order_exposesEveryPersistedValueAndDefensivelyCopiesOrderDate()` | Các thuộc tính đơn hàng: id, ngày đặt, số thứ tự, tổng tiền, thông tin khách hàng, trạng thái | Nhánh sao chép phòng thủ Date đơn hàng và getter/setter toàn bộ trường | Đơn hàng bảo toàn thuộc tính, Date được cô lập an toàn, null an toàn |
| **TC_ENT_09** | `OrderDetail.java` | `orderDetail_exposesEveryPersistedValue()` | `OrderDetail` gán Order, Product, số lượng, đơn giá, tổng tiền | Nhánh getter/setter chi tiết đơn hàng | Tất cả liên kết thực thể và giá trị tính toán khớp chính xác |
| **TC_ENT_10** | `OrderReturn.java` | `orderReturn_usesPendingDefault()` | Khởi tạo qua default constructor | Nhánh thiết lập trạng thái mặc định của yêu cầu hoàn trả | Trạng thái mặc định là `"PENDING"` |
| **TC_ENT_11** | `OrderReturn.java` | `orderReturn_constructorPopulatesRequestAndTimestamps()` | Constructor với orderId, username, reason, imageUrls | Nhánh khởi tạo yêu cầu hoàn trả qua constructor nghiệp vụ | Thuộc tính được gán đúng, trạng thái PENDING, timestamps tự động sinh |
| **TC_ENT_12** | `OrderReturn.java` | `orderReturn_mutablePropertiesRoundTrip()` | Thay đổi id, orderId, username, reason, imageUrls, status, adminNote | Nhánh cập nhật các thuộc tính và ghi chú duyệt hoàn trả của admin | Getter/setter phản ánh chính xác các giá trị mới |
| **TC_ENT_13** | `OrderReturn.java` | `orderReturn_defensivelyCopiesAndAcceptsNullDates()` | Gán `createdAt`, `updatedAt` bằng đối tượng Date mutable và null | Nhánh sao chép phòng thủ Date của OrderReturn | Thay đổi Date ngoài không ảnh hưởng Date bên trong, chấp nhận null |
| **TC_ENT_14** | `Voucher.java` | `voucher_normalizesCodesAndCopiesAllDates()` | Khởi tạo với mã `" save10 "`, các trường giảm giá, hạn dùng mutable và null | Nhánh chuẩn hóa mã giảm giá (trim + uppercase) và sao chép phòng thủ hạn sử dụng | Mã chuẩn hóa thành `"SAVE10"`, Date độc lập, các tham số null an toàn |
| **TC_ENT_15** | `VoucherUsage.java` | `voucherUsage_normalizesCodeAndDefensivelyCopiesTimestamp()` | Khởi tạo với mã `" save10 "`, username, orderId, timestamp | Nhánh chuẩn hóa mã voucher sử dụng và phòng thủ thời điểm dùng | Mã chuẩn hóa thành `"SAVE10"`, Date cô lập an toàn |
| **TC_ENT_16** | `Wishlist.java` | `wishlist_exposesPropertiesAndDefensivelyCopiesTimestamp()` | Constructor `(username, productCode)`, sửa đổi id, username, productCode, timestamp | Nhánh sao chép phòng thủ timestamp và getter/setter Wishlist | Thuộc tính lưu trữ chính xác, `createdAt` được bảo vệ độc lập |
| **TC_ENT_17** | `ProductReview.java` | `productReview_defaultsCreatedTimestamp()` | Khởi tạo `ProductReview` qua default constructor | Nhánh tự động gán mốc thời gian tạo review | `createdAt` tự động khởi tạo khác null |
| **TC_ENT_18** | `ProductReview.java` | `productReview_constructorPopulatesReviewAndTimestamp()` | Constructor `(productCode, username, ratingValue, comment)` | Nhánh khởi tạo đánh giá sản phẩm qua business constructor | Đầy đủ thông tin đánh giá, timestamp tự động tạo |
| **TC_ENT_19** | `ProductReview.java` | `productReview_mutablePropertiesRoundTrip()` | Thay đổi id, productCode, username, rating, comment, imageUrl qua setter | Nhánh getter/setter các thuộc tính của đánh giá sản phẩm | Tất cả giá trị phản ánh chính xác các dữ liệu đã gán |
| **TC_ENT_20** | `ProductReview.java` | `productReview_defensivelyCopiesAndAcceptsNullTimestamp()` | Gán `createdAt` bằng Date mutable và gán null | Nhánh sao chép phòng thủ mốc thời gian tạo bình luận | Date cô lập an toàn, chấp nhận giá trị null an toàn |
