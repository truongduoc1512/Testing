# ĐẶC TẢ KIỂM THỬ HỘP TRẮNG: `ModelCoverage`
## Tầng: Data Transfer Models | Phân hệ: Các Lớp Mô hình Dữ liệu Nghiệp vụ (Domain Models & DTOs)
* **Tổng số Test Case:** **34 Test Cases**
* **Mục đích tài liệu:** Bảng ánh xạ chi tiết giữa 8 lớp Model trong `com.example.demo.model` và các ca kiểm thử trong `ModelCoverageTest.java`, phục vụ tra cứu nhanh giá trị test đầu vào, nhánh logic mục tiêu và kết quả mong đợi.

---

## 📌 PHẦN 1: BẢNG TỔNG HỢP CÁC LỚP MODEL TRONG `com.example.demo.model`

Gói `model` chứa các đối tượng truyền dữ liệu (DTOs) và tính toán giỏ hàng, đơn hàng, chiết khấu và trạng thái:

| STT | Tên lớp Model trong `src/main` | Mục đích nghiệp vụ | Số Test Case đối ứng |
| :---: | :--- | :--- | :---: |
| 1 | `ProductInfo.java` | Chứa dữ liệu hiển thị sản phẩm, tính toán giá sau khuyến mãi | **3** |
| 2 | `CartInfo.java` | Quản lý giỏ hàng, thêm/sửa/xóa line items, áp dụng voucher, chặn vượt tồn kho | **17** |
| 3 | `CustomerInfo.java` | Thông tin khách hàng đặt đơn và địa chỉ giao hàng | **2** |
| 4 | `OrderInfo.java` | Thông tin tổng quan đơn hàng, sao chép phòng thủ danh sách chi tiết | **4** |
| 5 | `OrderDetailInfo.java` | Dòng chi tiết đơn hàng (mã sản phẩm, số lượng, đơn giá, thành tiền) | **2** |
| 6 | `OrderStatus.java` | Máy trạng thái đơn hàng (FSM): chuẩn hóa alias, xác định luồng chuyển đổi trạng thái hợp lệ | **4** |
| 7 | `VoucherApplyResult.java` | Kết quả xử lý voucher (thành công kèm số tiền giảm, hoặc thất bại kèm lý do) | **2** |

---

## 📑 PHẦN 2: BẢNG MA TRẬN ÁNH XẠ CHI TIẾT 34 TEST CASES

| Mã TC | Lớp / Hàm ở `src/main` | Hàm kiểm thử ở `src/test` | Giá trị Test đầu vào (Inputs / Mock Data) | Nhánh logic / Điều kiện mục tiêu | Kết quả mong đợi (Assertion) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `TC_MDL_01` | `ProductInfo(Product)` | `productInfo_mapsEveryProductEntityField` | `Product`: `code="P1"`, `price=200`, `discount=10`, `isMall=true`, `isFavored=true` | Ánh xạ toàn diện mọi trường từ Entity sang DTO | `price = 180.0` (đã giảm 10%), các trường boolean và thông tin khớp entity |
| `TC_MDL_02` | `ProductInfo(...)` | `productInfo_constructorOverloadsPopulateTheirOptionalFields`| Thử nghiệm 4 constructor quá tải khác nhau | Nạp linh hoạt các trường tùy chọn | Giá bán, chiết khấu, đánh giá và tồn kho được khởi tạo chính xác |
| `TC_MDL_03` | `ProductInfo` getter/setter | `productInfo_mutablePropertiesRoundTrip` | Gán giá trị mới cho tất cả setter | Kiểm tra tính đối ứng của getter/setter | Các getter đọc ra đúng 100% giá trị vừa gán |
| `TC_MDL_04` | `CartInfo()` | `cartInfo_startsEmptyWithZeroTotalsAndNoValidCustomer` | Khởi tạo giỏ hàng mới | Khởi tạo giá trị mặc định | `isEmpty() = true`, `quantityTotal = 0`, `amountTotal = 0.0`, `isValidCustomer() = false` |
| `TC_MDL_05` | `CartInfo.isValidCustomer` | `cartInfo_rejectsInvalidCustomer` | `CustomerInfo.isValid() = false` | Nhánh kiểm tra tính hợp lệ khách hàng | `cart.isValidCustomer() = false` |
| `TC_MDL_06` | `CartInfo` checkout | `cartInfo_exposesCheckoutPropertiesAndAcceptsValidCustomer` | `CustomerInfo.isValid() = true`, `orderNum = 12`, `voucherCode = "SAVE10"` | Nạp dữ liệu checkout hợp lệ | `isValidCustomer() = true`, lưu đúng mã đơn và voucher |
| `TC_MDL_07` | `CartInfo.addProduct` | `cartInfo_addProductAccumulatesQuantityAndCapsItAtStock` | Sản phẩm `stock = 3`, thêm lần 1: 2 cái, lần 2: 10 cái | Nhánh giới hạn tồn kho: số lượng vượt quá tồn kho bị chặn ở mức max stock | `quantity = 3` (bằng stock), số dòng trong giỏ = 1 |
| `TC_MDL_08` | `CartInfo.addProduct` | `cartInfo_addProductRemovesLineWhenQuantityBecomesNonPositive`| Sản phẩm trong giỏ có `quantity = 2`, cộng thêm `-2` cái | Nhánh số lượng tích lũy `<= 0`: tự động xóa khỏi giỏ | Dòng sản phẩm bị xóa khỏi giỏ, `cartLines.isEmpty() = true` |
| `TC_MDL_09` | `CartInfo.addProduct` | `cartInfo_addProductDoesNotRetainANewLineForNegativeQuantity`| Sản phẩm chưa có trong giỏ, truyền số lượng `-1` | Nhánh thêm mới nhưng số lượng âm | Không thêm sản phẩm vào giỏ, giỏ hàng vẫn rỗng |
| `TC_MDL_10` | `CartInfo.updateProduct` | `cartInfo_updateProductIgnoresUnknownCode` | Cập nhật số lượng cho mã sản phẩm không có trong giỏ | Nhánh không tìm thấy sản phẩm | Bỏ qua không lỗi, giỏ hàng giữ nguyên |
| `TC_MDL_11` | `CartInfo.updateProduct` | `cartInfo_updateProductCapsQuantityAtStock` | `stock = 5`, cập nhật số lượng mới = 10 | Nhánh cập nhật vượt tồn kho: chặn ở `stockQuantity` | Số lượng được gán bằng tồn kho tối đa: `5` |
| `TC_MDL_12` | `CartInfo.updateProduct` | `cartInfo_updateProductChangesPositiveQuantity` | Cập nhật sản phẩm từ 1 cái lên 3 cái (trong mức tồn kho) | Nhánh số lượng hợp lệ | Số lượng cập nhật thành `3` |
| `TC_MDL_13` | `CartInfo.updateProduct` | `cartInfo_updateProductRemovesLineForNonPositiveQuantity`| Cập nhật số lượng sản phẩm về `0` hoặc `-3` | Nhánh `newQuantity <= 0`: xóa khỏi giỏ | Dòng sản phẩm bị gỡ khỏi giỏ hàng |
| `TC_MDL_14` | `CartInfo.removeProduct` | `cartInfo_removeProductIgnoresUnknownProduct` | Xóa sản phẩm không tồn tại trong giỏ | Nhánh tìm dòng để xóa không thấy | Giỏ hàng không đổi |
| `TC_MDL_15` | `CartInfo.removeProduct` | `cartInfo_removeProductRemovesMatchingProduct` | Xóa đúng sản phẩm đang có | Nhánh tìm thấy dòng sản phẩm | Xóa thành công, giỏ hàng trở về rỗng |
| `TC_MDL_16` | `CartInfo` tính tiền | `cartInfo_calculatesQuantityAmountAndDiscountedFinalAmount`| 2 sp A (giá 100) + 1 sp B (giá 50), voucher giảm 30 | Nhánh tính toán: `amount = sum(qty*price)`, `finalAmount = amount - discount` | `quantityTotal = 3`, `amountTotal = 250.0`, `finalAmount = 220.0` |
| `TC_MDL_17` | `CartInfo` tính tiền | `cartInfo_clampsFinalAmountAtZero` | Tổng tiền 50.0, áp dụng voucher giảm 100.0 | Nhánh giảm giá vượt tổng tiền: chặn ở mức 0 đồng | `finalAmount = 0.0` (không bao giờ âm tiền) |
| `TC_MDL_18` | `CartInfo.updateQuantity`| `cartInfo_updateQuantityUpdatesOnlyMatchingCartLines` | `CartForm` chứa danh sách cập nhật số lượng mới | Nhánh duyệt đồng bộ form giỏ hàng | Cập nhật chính xác số lượng từng dòng tương ứng |
| `TC_MDL_19` | `CartInfo.updateQuantity`| `cartInfo_updateQuantityIgnoresEmptyForm` | `CartForm` có danh sách rỗng | Nhánh danh sách rỗng | Không thay đổi giỏ hàng |
| `TC_MDL_20` | `CartInfo.updateQuantity`| `cartInfo_updateQuantityIgnoresNullForm` | `cartForm = null` | Nhánh `form == null` | Bỏ qua an toàn, không ném NullPointerException |
| `TC_MDL_21` | `CustomerInfo(CustomerForm)`| `customerInfo_mapsAndNormalizesValidatedForm`| `CustomerForm`: `name = " Alice "`, `email = " ALICE@EXAMPLE.COM "` | Sao chép và trim dữ liệu khách hàng | Gán đúng `name = "Alice"`, `email = "alice@example.com"`, `valid = true` |
| `TC_MDL_22` | `CustomerInfo` getter/setter| `customerInfo_mutablePropertiesRoundTrip` | Gán và đọc các thuộc tính khách hàng | Getter/setter roundtrip | Dữ liệu đọc ra khớp hoàn toàn dữ liệu gán |
| `TC_MDL_23` | `OrderInfo` constructor | `orderInfo_constructorCopiesDateAndExposesSummaryProperties`| Nạp đơn hàng với mã, ngày đặt, số lượng, tổng tiền | Sao chép phòng thủ đối tượng `Date` | Thông tin tổng hợp hiển thị chính xác |
| `TC_MDL_24` | `OrderInfo.setDetails` | `orderInfo_detailsAreDefensivelyCopiedAndAcceptNull` | 1. Truyền list null<br>2. Truyền list và sau đó sửa list bên ngoài | Bản sao phòng thủ (defensive copying) | 1. Gán danh sách rỗng<br>2. Sửa list bên ngoài không làm thay đổi list trong OrderInfo |
| `TC_MDL_25` | `OrderInfo` getter/setter | `orderInfo_mutablePropertiesRoundTrip` | Getter/setter các trường bổ sung (địa chỉ, số điện thoại, voucher) | Kiểm tra thuộc tính đơn hàng | Đọc ra chính xác các giá trị đã gán |
| `TC_MDL_26` | `OrderInfo` null date | `orderInfo_acceptsNullDates` | Khởi tạo đơn hàng với `orderDate = null` | Nhánh xử lý `orderDate == null` | `getOrderDate() = null`, không văng lỗi |
| `TC_MDL_27` | `OrderDetailInfo` constructor| `orderDetailInfo_constructorExposesEveryValue` | Nạp `OrderDetail`: id, mã sp, tên sp, số lượng, đơn giá, thành tiền | Nạp chi tiết dòng đơn hàng | Tất cả các getter trả về đúng giá trị ban đầu |
| `TC_MDL_28` | `OrderDetailInfo` getter/setter| `orderDetailInfo_mutablePropertiesRoundTrip` | Gán và đọc thuộc tính chi tiết đơn hàng | Getter/setter roundtrip | Khớp 100% |
| `TC_MDL_29` | `OrderStatus.normalize` | `orderStatus_normalizesAliasesAndIdentifiesAdminStatuses` | 1. `"pending"` ➔ `PENDING`<br>2. `"CHO_XAC_NHAN"` ➔ `PENDING`<br>3. Kiểm tra các trạng thái admin quản lý | Nhánh chuẩn hóa alias tiếng Anh/Việt | Trả về enum tương ứng, nhận diện đúng admin statuses |
| `TC_MDL_30` | `OrderStatus.canTransitionTo`| `orderStatus_rejectsMissingAndUnsupportedTransitions` | Chuyển đổi từ `COMPLETED` sang `PENDING` (không hợp lệ), hoặc trạng thái null | Nhánh kiểm tra chuyển đổi trạng thái bị cấm | Trả về `false` (chặn chuyển đổi sai quy trình) |
| `TC_MDL_31` | `OrderStatus.canTransitionTo`| `orderStatus_allowsEveryPendingTransition` | Từ `PENDING` chuyển sang `CONFIRMED`, `CANCELLED` | Nhánh chuyển đổi hợp lệ từ trạng thái chờ | Trả về `true` |
| `TC_MDL_32` | `OrderStatus.canTransitionTo`| `orderStatus_validatesApprovedShippingAndTerminalTransitionFamilies`| Kiểm tra các cặp chuyển đổi trạng thái: APPROVED ➔ SHIPPING ➔ COMPLETED | Nhánh máy trạng thái FSM đơn hàng | Trả về `true` cho các cặp luồng hợp lệ |
| `TC_MDL_33` | `VoucherApplyResult` thất bại | `voucherApplyResult_failureConstructorRetainsFailureDetails`| Constructor thất bại: `success = false`, `reason = "Voucher hết hạn"` | Nhánh áp dụng voucher không thành công | `isSuccess() = false`, `getDiscountAmount() = 0`, giữ nguyên message lý do |
| `TC_MDL_34` | `VoucherApplyResult` thành công | `voucherApplyResult_mutableSuccessPayloadExposesEveryValue` | Constructor thành công: số tiền giảm 50.000đ, mã voucher | Nhánh áp dụng voucher thành công | `isSuccess() = true`, `getDiscountAmount() = 50000.0`, thông tin voucher đầy đủ |

---

## 📊 PHẦN 3: KẾT QUẢ ĐO LƯỜNG ĐỘ BAO PHỦ (JACOCO METRICS)

* **Phân hệ kiểm thử:** Toàn bộ 8 lớp Model trong gói `com.example.demo.model`
* **Statement Coverage (Instructions):** **99.9%** (1,024 / 1,025 instructions)
* **Branch Coverage (Branches):** **98.6%** (73 / 74 branches)
* **Line Coverage:** **100.0%** (321 / 321 lines)
* **Đánh giá:** Đạt độ bao phủ gần như tuyệt đối 100%, bảo đảm toàn bộ logic nghiệp vụ cốt lõi của giỏ hàng, tính toán giá trị đơn hàng và máy trạng thái FSM hoạt động chuẩn xác.
