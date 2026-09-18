# ĐẶC TẢ KIỂM THỬ HỘP TRẮNG: `OrderApiController`
## Tầng: REST API Controller | Phân hệ: Quản lý Đơn hàng RESTful (Order API)
* **Tổng số Test Case:** **12 Test Cases**
* **Mục đích tài liệu:** Bảng ánh xạ chi tiết giữa hàm nghiệp vụ trong `OrderApiController.java` và các ca kiểm thử trong `OrderApiControllerTest.java`, phục vụ tra cứu nhanh giá trị test đầu vào, nhánh logic mục tiêu và kết quả mong đợi.

---

## 📌 PHẦN 1: BẢNG TỔNG HỢP CÁC HÀM TRONG `OrderApiController.java`

Lớp `OrderApiController` cung cấp các REST endpoints xem danh sách đơn hàng, xem chi tiết và cập nhật trạng thái:

| STT | Tên hàm trong `OrderApiController.java` | Endpoint HTTP | Mục đích nghiệp vụ | Số Test Case |
| :---: | :--- | :--- | :--- | :---: |
| 1 | `getOrders(int page)` | `GET /api/v1/orders` | Danh sách đơn hàng phân trang theo phạm vi quyền (Customer chỉ xem đơn mình, Admin xem tất cả) | **1** |
| 2 | `getOrderById(String orderId)` | `GET /api/v1/orders/{orderId}` | Xem chi tiết đơn hàng (kiểm tra quyền sở hữu, tính toán lại giá trị nếu Admin không phải chủ đơn) | **6** |
| 3 | `updateOrderStatus(String, Map)` | `PUT /api/v1/orders/{orderId}/status` | Admin cập nhật trạng thái đơn (PENDING, SHIPPING, COMPLETED, CANCELLED) | **5** |

---

## 📑 PHẦN 2: BẢNG MA TRẬN ÁNH XẠ CHI TIẾT 12 TEST CASES

| Mã TC | Hàm ở `src/main` | Hàm kiểm thử ở `src/test` | Giá trị Test đầu vào (Inputs / Mock Data) | Nhánh logic / Điều kiện mục tiêu | Kết quả mong đợi (Assertion) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `TC_OAC_01` | `getOrders` | `getOrders_normalizesPageAndResolvesPrincipalScope` | Phân trang: `page = -1` (chuẩn hóa về 1), User quyền `ROLE_USER`, Admin `ROLE_ADMIN`, hoặc role không hỗ trợ | Chuẩn hóa `pageIndex` và phân giải vai trò `currentRole` | HTTP `200 OK`, gọi đúng tham số `orderDAO.listOrderInfo(expectedPage, 10, 10, username, expectedRole)` |
| `TC_OAC_02` | `getOrderById` | `getOrder_returnsNotFoundBeforeAuthorization` | `orderId = "missing"`, Mock: `orderDAO.getOrderInfo("missing") = null` | Nhánh đơn hàng không tồn tại: ngắt sớm trước khi kiểm tra quyền | HTTP `404 Not Found`, không kiểm tra `canAccessOrder` |
| `TC_OAC_03` | `getOrderById` | `getOrder_rejectsLoginRequiredAuthentication` | Chưa đăng nhập (`auth = null`), token chưa xác thực, hoặc `anonymousUser` | Nhánh bắt buộc đăng nhập để xem đơn hàng | HTTP `403 Forbidden` |
| `TC_OAC_04` | `getOrderById` | `getOrder_rejectsAuthenticatedPrincipalOutsideOrderScope`| User `buyer` cố truy cập đơn hàng của người khác (Mock: `canAccessOrder = false`) | Nhánh chặn truy cập chéo đơn hàng của user khác | HTTP `403 Forbidden` |
| `TC_OAC_05` | `getOrderById` | `getOrder_loadsUserDetailsWithoutRecalculatingAmount` | User `buyer` truy cập đơn hàng của chính mình (Mock: `canAccessOrder = true`) | Nhánh khách hàng xem đơn của mình: giữ nguyên `order.amount` | HTTP `200 OK`, trả về đúng `order` và danh sách chi tiết |
| `TC_OAC_06` | `getOrderById` | `getOrder_preservesAmountWhenAdminIsOrderCustomer` | Admin truy cập đơn hàng mà chính Admin là người mua (`isOrderCustomer = true`) | Nhánh Admin là chủ sở hữu đơn hàng | HTTP `200 OK`, giữ nguyên `amount` ban đầu |
| `TC_OAC_07` | `getOrderById` | `getOrder_recalculatesAmountWhenAdminIsNotOrderCustomer`| Admin duyệt đơn hàng của khách hàng (`isOrderCustomer = false`) | Nhánh Admin quản lý đơn người khác: tính lại tổng tiền từ chi tiết | HTTP `200 OK`, tổng tiền `amount` được tính lại bằng tổng các dòng chi tiết |
| `TC_OAC_08` | `updateOrderStatus` | `updateStatus_rejectsInvalidPayload` | Payload update: `null`, rỗng, status trắng, hoặc status không do admin quản lý (`"RETURNED"`) | Nhánh validate payload trạng thái hợp lệ | HTTP `400 Bad Request` |
| `TC_OAC_09` | `updateOrderStatus` | `updateStatus_returnsNotFoundWhenOrderDoesNotExist` | `orderId = "missing"` | Nhánh không tìm thấy đơn hàng cần cập nhật | HTTP `404 Not Found` |
| `TC_OAC_10` | `updateOrderStatus` | `updateStatus_rejectsPrincipalOutsideManagementScope` | Admin không có quyền quản lý đơn này (Mock: `canManageOrder = false`) | Nhánh chặn quyền cập nhật trạng thái đơn | HTTP `403 Forbidden` |
| `TC_OAC_11` | `updateOrderStatus` | `updateStatus_normalizesAndReturnsUpdatedOrder` | Cập nhật `status = " shipped "` | Chuẩn hóa trim/uppercase status ➔ `OrderStatus.SHIPPING` | HTTP `200 OK`, gọi `updateOrderStatus(id, SHIPPING)` và trả về đơn mới |
| `TC_OAC_12` | `updateOrderStatus` | `updateStatus_mapsDaoException` | Case 1: ném `IllegalStateException` (sai quy trình chuyển đổi trạng thái)<br>Case 2: lỗi CSDL `RuntimeException` | Xử lý ngoại lệ từ DAO: ánh xạ sang HTTP Status phù hợp | Case 1: `409 Conflict`<br>Case 2: `500 Internal Server Error` |

---

## 📊 PHẦN 3: KẾT QUẢ ĐO LƯỜNG ĐỘ BAO PHỦ (JACOCO METRICS)

* **Lớp kiểm thử:** `com.example.demo.controller.api.OrderApiController`
* **Statement Coverage (Instructions):** **100.0%** (252/252 instructions)
* **Branch Coverage (Branches):** **100.0%** (30/30 branches)
* **Line Coverage:** **100.0%** (54/54 lines)
* **Đánh giá:** Đạt độ bao phủ tuyệt đối 100%, bảo vệ an toàn phân quyền truy cập đơn hàng, chống xem trộm đơn và quản lý chuyển đổi trạng thái đơn chặt chẽ.
