# ĐẶC TẢ KIỂM THỬ HỘP TRẮNG: `OrderCancelReturnApiController`
## Tầng: REST API Controller | Phân hệ: Quản lý Hủy đơn & Yêu cầu Trả hàng Hoàn tiền (Cancel & Return API)
* **Tổng số Test Case:** **18 Test Cases**
* **Mục đích tài liệu:** Bảng ánh xạ chi tiết giữa hàm nghiệp vụ trong `OrderCancelReturnApiController.java` và các ca kiểm thử trong `OrderCancelReturnApiControllerTest.java`, phục vụ tra cứu nhanh giá trị test đầu vào, nhánh logic mục tiêu và kết quả mong đợi.

---

## 📌 PHẦN 1: BẢNG TỔNG HỢP CÁC HÀM TRONG `OrderCancelReturnApiController.java`

Lớp `OrderCancelReturnApiController` cung cấp các REST endpoints phục vụ luồng hủy đơn hàng và khiếu nại trả hàng:

| STT | Tên hàm trong `OrderCancelReturnApiController.java` | Endpoint HTTP | Mục đích nghiệp vụ | Số Test Case |
| :---: | :--- | :--- | :--- | :---: |
| 1 | `cancelOrder(String orderId)` | `POST /api/v1/orders/{orderId}/cancel` | Khách hàng hủy đơn hàng đang ở trạng thái chờ | **4** |
| 2 | `createReturnRequest(String, ...)` | `POST /api/v1/orders/{orderId}/return` | Khách hàng tạo yêu cầu trả hàng kèm lý do và ảnh bằng chứng | **4** |
| 3 | `getReturnRequest(String orderId)` | `GET /api/v1/orders/{orderId}/return` | Xem chi tiết yêu cầu trả hàng (phân quyền xem của Buyer & Admin) | **6** |
| 4 | `updateReturnStatus(String, ...)` | `PUT /api/v1/admin/orders/{orderId}/return-status`| Quản trị viên duyệt (APPROVE) hoặc từ chối (REJECT) trả hàng | **4** |

---

## 📑 PHẦN 2: BẢNG MA TRẬN ÁNH XẠ CHI TIẾT 18 TEST CASES

| Mã TC | Hàm ở `src/main` | Hàm kiểm thử ở `src/test` | Giá trị Test đầu vào (Inputs / Mock Data) | Nhánh logic / Điều kiện mục tiêu | Kết quả mong đợi (Assertion) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `TC_OCR_01` | `cancelOrder` | `cancelOrder_rejectsLoginRequiredAuthentication` | Chưa đăng nhập (`auth = null`), token chưa xác thực, hoặc `anonymousUser` | Nhánh bắt buộc đăng nhập để hủy đơn | HTTP `401 Unauthorized`, không gọi DAO |
| `TC_OCR_02` | `cancelOrder` | `cancelOrder_returnsSuccessfulDaoOutcome` | User `buyer` hủy đơn hợp lệ (Mock: `orderReturnDAO.cancelOrder` = `true`) | Nhánh hủy đơn thành công | HTTP `200 OK`, `success = true` |
| `TC_OCR_03` | `cancelOrder` | `cancelOrder_returnsRejectedDaoOutcome` | Đơn hàng không đủ điều kiện hủy (Mock trả về `false`) | Nhánh từ chối hủy đơn | HTTP `200 OK`, `success = false` |
| `TC_OCR_04` | `cancelOrder` | `cancelOrder_mapsDaoException` | Ngoại lệ từ DAO: `IllegalStateException` (sai trạng thái đơn) hoặc lỗi hệ thống | Xử lý ngoại lệ ánh xạ HTTP Status | `409 Conflict` (sai trạng thái) hoặc `500 Internal Server Error` |
| `TC_OCR_05` | `createReturnRequest` | `createReturn_rejectsLoginRequiredAuthentication` | Chưa đăng nhập khi gửi yêu cầu trả hàng | Nhánh kiểm tra xác thực người dùng | HTTP `401 Unauthorized` |
| `TC_OCR_06` | `createReturnRequest` | `createReturn_rejectsInvalidForm` | Form thiếu lý do (null/trắng), hoặc ảnh vượt quá 500 ký tự | Nhánh validate form yêu cầu trả hàng | HTTP `400 Bad Request` |
| `TC_OCR_07` | `createReturnRequest` | `createReturn_returnsCreatedDataForValidImageBoundary` | Lý do hợp lệ, URL ảnh: `null`, rỗng, hoặc đúng biên 500 ký tự | Nhánh dữ liệu form hợp lệ | HTTP `201 Created`, trả về bản ghi `OrderReturn` |
| `TC_OCR_08` | `createReturnRequest` | `createReturn_mapsDaoException` | DAO ném lỗi trạng thái đơn không thể trả hàng | Xử lý ngoại lệ tạo yêu cầu | Ánh xạ sang mã lỗi HTTP tương ứng |
| `TC_OCR_09` | `getReturnRequest` | `getReturn_rejectsLoginRequiredAuthentication` | Chưa đăng nhập cố xem thông tin đổi trả | Nhánh yêu cầu đăng nhập | HTTP `401 Unauthorized` |
| `TC_OCR_10` | `getReturnRequest` | `getReturn_returnsNotFoundWhenRequestDoesNotExist` | Mã đơn không có yêu cầu đổi trả nào | Nhánh `findReturnByOrderId == null` | HTTP `404 Not Found` |
| `TC_OCR_11` | `getReturnRequest` | `getReturn_forbidsRequestOwnedByAnotherUser` | User `buyer` xem yêu cầu trả hàng của user `other` | Nhánh `!orderReturn.getUserName().equals(currentUsername)` | HTTP `403 Forbidden` (chặn truy cập chéo) |
| `TC_OCR_12` | `getReturnRequest` | `getReturn_allowsRequestOwner` | User `buyer` xem đúng yêu cầu trả hàng của mình | Nhánh chủ đơn hợp lệ | HTTP `200 OK`, trả về đúng thông tin trả hàng |
| `TC_OCR_13` | `getReturnRequest` | `getReturn_forbidsAdminOutsideOrderScope` | Admin cố xem đơn đổi trả mà mình không được phân quyền quản lý | Nhánh `isAdmin && !orderDAO.canAccessOrder` | HTTP `403 Forbidden` |
| `TC_OCR_14` | `getReturnRequest` | `getReturn_allowsAdminWithinOrderScope` | Admin có quyền truy cập đơn hàng | Nhánh `isAdmin && orderDAO.canAccessOrder = true` | HTTP `200 OK`, trả về đối tượng `OrderReturn` |
| `TC_OCR_15` | `updateReturnStatus` | `updateStatus_rejectsNonAdminAuthentication` | User thường cố gọi API duyệt đổi trả của Admin | Nhánh `if (!isAdmin())` | HTTP `403 Forbidden` |
| `TC_OCR_16` | `updateReturnStatus` | `updateStatus_rejectsInvalidForm` | Form duyệt: action null/rỗng, action lạ, ghi chú admin quá dài | Nhánh validate form cập nhật trạng thái của Admin | HTTP `400 Bad Request` |
| `TC_OCR_17` | `updateReturnStatus` | `updateStatus_returnsUpdatedRequest` | Admin duyệt: `action = "APPROVE"` hoặc `"REJECT"`, ghi chú hợp lệ | Nhánh cập nhật hợp lệ | HTTP `200 OK`, trả về trạng thái mới cập nhật |
| `TC_OCR_18` | `updateReturnStatus` | `updateStatus_mapsDaoException` | DAO ném lỗi nghiệp vụ khi duyệt | Xử lý ngoại lệ duyệt yêu cầu đổi trả | Ánh xạ sang HTTP Status phù hợp |

---

## 📊 PHẦN 3: KẾT QUẢ ĐO LƯỜNG ĐỘ BAO PHỦ (JACOCO METRICS)

* **Lớp kiểm thử:** `com.example.demo.controller.api.OrderCancelReturnApiController`
* **Statement Coverage (Instructions):** **100.0%** (349/349 instructions)
* **Branch Coverage (Branches):** **100.0%** (52/52 branches)
* **Line Coverage:** **100.0%** (82/82 lines)
* **Đánh giá:** Đạt độ bao phủ tuyệt đối 100%, bảo đảm quy trình hủy đơn và khiếu nại trả hàng - hoàn tiền diễn ra an toàn, minh bạch và bảo mật.
