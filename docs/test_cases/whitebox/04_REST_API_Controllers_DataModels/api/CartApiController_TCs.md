# ĐẶC TẢ KIỂM THỬ HỘP TRẮNG: `CartApiController`
## Tầng: REST API Controller | Phân hệ: Quản lý Giỏ hàng & Đặt hàng RESTful (Cart API)
* **Tổng số Test Case:** **18 Test Cases**
* **Mục đích tài liệu:** Bảng ánh xạ chi tiết giữa hàm nghiệp vụ trong `CartApiController.java` và các ca kiểm thử trong `CartApiControllerTest.java`, phục vụ tra cứu nhanh giá trị test đầu vào, nhánh logic mục tiêu và kết quả mong đợi.

---

## 📌 PHẦN 1: BẢNG TỔNG HỢP CÁC HÀM TRONG `CartApiController.java`

Lớp `CartApiController` cung cấp các REST endpoints phục vụ giỏ hàng, thông tin khách hàng và checkout:

| STT | Tên hàm trong `CartApiController.java` | Endpoint HTTP | Mục đích nghiệp vụ | Số Test Case |
| :---: | :--- | :--- | :--- | :---: |
| 1 | `getCart(HttpServletRequest)` | `GET /api/v1/cart` | Lấy giỏ hàng từ session hiện tại | **1** |
| 2 | `addCartItem(HttpServletRequest, Map)` | `POST /api/v1/cart/items` | Thêm sản phẩm vào giỏ (chặn hết hàng, giới hạn tồn kho) | **4** |
| 3 | `updateCartItemQuantity(...)` | `PUT /api/v1/cart/items` | Cập nhật số lượng sp trong giỏ (tự động cap ở mức max stock) | **5** |
| 4 | `removeCartItem(HttpServletRequest, String)` | `DELETE /api/v1/cart/items/{code}` | Xóa sản phẩm khỏi giỏ hàng | **2** |
| 5 | `saveCustomerInfo(HttpServletRequest, ...)` | `POST /api/v1/cart/customer` | Lưu và chuẩn hóa thông tin khách hàng đặt đơn | **2** |
| 6 | `checkoutOrder(HttpServletRequest)` | `POST /api/v1/cart/checkout` | Tạo đơn hàng, làm sạch giỏ hàng active và lưu đơn hoàn tất | **4** |

---

## 📑 PHẦN 2: BẢNG MA TRẬN ÁNH XẠ CHI TIẾT 18 TEST CASES

| Mã TC | Hàm ở `src/main` | Hàm kiểm thử ở `src/test` | Giá trị Test đầu vào (Inputs / Mock Data) | Nhánh logic / Điều kiện mục tiêu | Kết quả mong đợi (Assertion) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `TC_CAC_01` | `getCart` | `getCart_returnsAndStoresTheSessionCart` | `MockHttpServletRequest` mới | Khởi tạo giỏ hàng rỗng vào session | HTTP `200 OK`, giỏ hàng rỗng `isEmpty() = true` |
| `TC_CAC_02` | `addCartItem` | `addCartItem_rejectsInvalidPayload` | Payload lỗi: thiếu code, code trắng, quantity <= 0, quantity không phải số | Nhánh kiểm tra tính hợp lệ payload đầu vào | HTTP `400 Bad Request`, không gọi DB |
| `TC_CAC_03` | `addCartItem` | `addCartItem_returnsNotFoundWhenProductDoesNotExist` | `code = "missing"`, Mock: `findActiveProduct("missing") = null` | Nhánh sản phẩm không tồn tại hoặc không active | HTTP `404 Not Found` |
| `TC_CAC_04` | `addCartItem` | `addCartItem_rejectsSoldOutProduct` | Sản phẩm có `stockQuantity = 0` (hết hàng) | Nhánh `product.getStockQuantity() <= 0` | HTTP `400 Bad Request` |
| `TC_CAC_05` | `addCartItem` | `addCartItem_acceptsSupportedQuantityRepresentation` | Truyền `quantity` dưới dạng Integer (1), Long (2), hoặc String ("3") | Nhánh ép kiểu linh hoạt `readQuantity` | HTTP `200 OK`, số lượng tích lũy chính xác trong giỏ |
| `TC_CAC_06` | `updateCartItemQuantity` | `updateCartItemQuantity_rejectsInvalidPayload` | Payload update thiếu code hoặc số lượng không hợp lệ | Nhánh validate payload cập nhật | HTTP `400 Bad Request` |
| `TC_CAC_07` | `updateCartItemQuantity` | `updateCartItemQuantity_returnsNotFoundForMissingProduct` | Mã sản phẩm không có trong DB | Nhánh `findActiveProduct == null` | HTTP `404 Not Found` |
| `TC_CAC_08` | `updateCartItemQuantity` | `updateCartItemQuantity_rejectsSoldOutProduct` | Sản phẩm cập nhật có `stockQuantity = 0` | Nhánh tồn kho bằng 0 | HTTP `400 Bad Request` |
| `TC_CAC_09` | `updateCartItemQuantity` | `updateCartItemQuantity_reportsRequestedQuantityWhenStockIsSufficient`| `stock = 5`, yêu cầu cập nhật `quantity = 3` | Nhánh `requestedQuantity <= stock`: chấp nhận số lượng yêu cầu | HTTP `200 OK`, `actualQuantity = 3`, `capped = false` |
| `TC_CAC_10` | `updateCartItemQuantity` | `updateCartItemQuantity_capsRequestedQuantityAtAvailableStock`| `stock = 5`, yêu cầu cập nhật `quantity = 10` | Nhánh `requestedQuantity > stock`: tự động giới hạn ở tồn kho | HTTP `200 OK`, `actualQuantity = 5`, `capped = true` kèm thông báo |
| `TC_CAC_11` | `removeCartItem` | `removeCartItem_returnsNotFoundWithoutChangingCart` | Xóa mã sản phẩm không tồn tại trong giỏ | Nhánh không tìm thấy sản phẩm cần xóa | HTTP `404 Not Found`, giỏ hàng giữ nguyên |
| `TC_CAC_12` | `removeCartItem` | `removeCartItem_removesExistingCartLine` | Xóa mã sản phẩm đang có trong giỏ | Nhánh xóa thành công dòng sản phẩm | HTTP `200 OK`, dòng sản phẩm bị loại bỏ |
| `TC_CAC_13` | `saveCustomerInfo` | `saveCustomerInfo_rejectsInvalidForm` | Form thiếu tên, thiếu email, hoặc thiếu số điện thoại/địa chỉ | Nhánh validate form khách hàng bắt buộc | HTTP `400 Bad Request` |
| `TC_CAC_14` | `saveCustomerInfo` | `saveCustomerInfo_trimsNormalizesAndStoresValidCustomer`| Form hợp lệ kèm khoảng trắng | Chuẩn hóa trim dữ liệu và lưu vào session | HTTP `200 OK`, `isValidCustomer() = true` |
| `TC_CAC_15` | `checkoutOrder` | `checkout_rejectsEmptyCart` | Giỏ hàng rỗng (`cart.isEmpty() = true`) | Nhánh chặn checkout giỏ hàng rỗng | HTTP `400 Bad Request`, message `"Giỏ hàng trống"` |
| `TC_CAC_16` | `checkoutOrder` | `checkout_rejectsCartWithoutValidCustomer` | Khách hàng chưa điền form checkout (`isValidCustomer() = false`) | Nhánh thiếu thông tin khách hàng | HTTP `400 Bad Request`, message `"Chưa có thông tin khách hàng"` |
| `TC_CAC_17` | `checkoutOrder` | `checkout_preservesCartWhenOrderSaveFails` | CSDL ném lỗi khi lưu đơn hàng | Bắt ngoại lệ lưu đơn, giữ nguyên giỏ hàng để user đặt lại | HTTP `400 Bad Request`, giỏ hàng trong session không bị xóa |
| `TC_CAC_18` | `checkoutOrder` | `checkout_storesOrderedCartAndClearsActiveCart` | Đặt hàng thành công | Lưu giỏ hàng vào `lastOrderedCart`, xóa sạch giỏ hàng active `myCart` | HTTP `200 OK`, trả về `orderNum`, xóa giỏ active |

---

## 📊 PHẦN 3: KẾT QUẢ ĐO LƯỜNG ĐỘ BAO PHỦ (JACOCO METRICS)

* **Lớp kiểm thử:** `com.example.demo.controller.api.CartApiController`
* **Statement Coverage (Instructions):** **100.0%** (441/441 instructions)
* **Branch Coverage (Branches):** **100.0%** (78/78 branches)
* **Line Coverage:** **100.0%** (103/103 lines)
* **Đánh giá:** Đạt độ bao phủ tuyệt đối 100%, bảo đảm kiểm soát toàn diện mọi tình huống thêm, sửa, xóa giỏ hàng, ràng buộc tồn kho và thanh toán.
