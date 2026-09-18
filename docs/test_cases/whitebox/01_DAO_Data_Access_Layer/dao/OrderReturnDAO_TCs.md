# ĐẶC TẢ KIỂM THỬ HỘP TRẮNG: `OrderReturnDAO`
## Tầng: Data Access Object (DAO) | Phân hệ: Hủy đơn & Hoàn trả đơn hàng
* **Tổng số Test Case:** **25 Test Cases**
* **Mục đích tài liệu:** Bảng ánh xạ chi tiết giữa các hàm nghiệp vụ trong `OrderReturnDAO.java` và các hàm kiểm thử trong `OrderReturnDAOTest.java`, phục vụ tra cứu nhanh giá trị test đầu vào, nhánh logic mục tiêu và kết quả mong đợi.

---

## 📌 PHẦN 1: BẢNG TỔNG HỢP CÁC HÀM TRONG `OrderReturnDAO.java`

Lớp `OrderReturnDAO` quản lý 4 hàm nghiệp vụ chính được bao phủ bởi 25 test case trong `OrderReturnDAOTest.java`:

| STT | Tên hàm trong `OrderReturnDAO.java` | Mục đích nghiệp vụ | Số Test Case đối ứng |
| :---: | :--- | :--- | :---: |
| 1 | `findReturnByOrderId(Long orderId)` | Tìm kiếm thông tin yêu cầu hoàn trả theo mã đơn | **3** |
| 2 | `cancelOrder(Long orderId, String username)` | Hủy đơn hàng PENDING, phục hồi tồn kho và giảm lượt bán | **6** |
| 3 | `createReturnRequest(form, orderId, user)` | Khách hàng tạo yêu cầu hoàn trả cho đơn đã giao thành công | **6** |
| 4 | `updateReturnStatus(orderId, action, note, user)`| Admin/Seller duyệt (APPROVE) hoặc từ chối (REJECT) yêu cầu trả | **10** |

---

## 📑 PHẦN 2: BẢNG MA TRẬN ÁNH XẠ CHI TIẾT 25 TEST CASES

| Mã TC | Hàm ở `src/main` | Hàm kiểm thử ở `src/test` | Giá trị Test đầu vào (Inputs / Mock Data) | Nhánh logic / Điều kiện mục tiêu | Kết quả mong đợi (Assertion) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `TC_RET_01` | `findReturnByOrderId` | `findReturnByOrderId_returnsNullForNullId` | `orderId = null` | Nhánh `if (orderId == null)` = True | Trả về `null` |
| `TC_RET_02` | `findReturnByOrderId` | `findReturnByOrderId_returnsNullForEmptyRows` | Không tìm thấy yêu cầu hoàn trả | Nhánh `if (rows.isEmpty())` = True | Trả về `null` |
| `TC_RET_03` | `findReturnByOrderId` | `findReturnByOrderId_returnsFirstRow` | Có bản ghi hoàn trả | Lấy phần tử index 0 của danh sách | Trả về đúng entity `OrderReturn` |
| `TC_RET_04` | `cancelOrder` | `cancelOrder_rejectsMissingOrder` | `orderId = 999L` (Không tồn tại) | Nhánh `if (order == null)` = True | Bị từ chối hủy đơn |
| `TC_RET_05` | `cancelOrder` | `cancelOrder_rejectsMissingOrDifferentCustomer` | User B cố tình hủy đơn của User A | Nhánh `!order.getCustomer().equals(user)` | Chặn truy cập trái phép |
| `TC_RET_06` | `cancelOrder` | `cancelOrder_rejectsEveryNonPendingStatus` | Đơn đang ở trạng thái `SHIPPING` | Nhánh `if (!"PENDING".equals(status))` | Chỉ cho phép hủy khi PENDING |
| `TC_RET_07` | `cancelOrder` | `cancelOrder_acceptsNormalizedPendingAndCancelsOrderWithoutDetails` | Đơn PENDING không có item chi tiết | Cập nhật trạng thái `CANCELLED` | Hủy đơn thành công |
| `TC_RET_08` | `cancelOrder` | `cancelOrder_stillCancelsOrderWhenProductWasDeleted` | Sản phẩm trong đơn đã bị xóa mềm | Nhánh bỏ qua phục hồi kho nếu SP đã xóa | Vẫn hủy đơn thành công |
| `TC_RET_09` | `cancelOrder` | `cancelOrder_restoresStockAndNeverMakesSalesNegative` | Đơn hủy chứa 2 sản phẩm | Dòng lệnh: `stock = stock + qty; sales = sales - qty` | Phục hồi kho +2, giảm sales -2 |
| `TC_RET_10` | `createReturnRequest` | `createReturnRequest_rejectsEachInvalidFormBoundary` | Form lý do trả hàng rỗng/quá dài | Nhánh kiểm tra tính hợp lệ của form lý do | Từ chối tạo yêu cầu trả hàng |
| `TC_RET_11` | `createReturnRequest` | `createReturnRequest_rejectsMissingOrder` | Mã đơn không tồn tại | Nhánh `if (order == null)` = True | Ném lỗi không tìm thấy đơn |
| `TC_RET_12` | `createReturnRequest` | `createReturnRequest_rejectsMissingOrDifferentOwner` | Không phải người mua của đơn hàng | Nhánh kiểm tra quyền chủ sở hữu đơn | Chặn tạo yêu cầu trả hàng |
| `TC_RET_13` | `createReturnRequest` | `createReturnRequest_rejectsNonCompletedOrder` | Đơn chưa hoàn tất (`status != DELIVERED`) | Nhánh `if (!"DELIVERED".equals(status))` | Chỉ cho phép trả đơn DELIVERED |
| `TC_RET_14` | `createReturnRequest` | `createReturnRequest_rejectsDuplicateRequest` | Đơn đã gửi yêu cầu trả trước đó | Nhánh `if (existingReturn != null)` | Chặn gửi trùng lặp yêu cầu |
| `TC_RET_15` | `createReturnRequest` | `createReturnRequest_trimsFieldsPersistsAndTagsOrder` | Form lý do hợp lệ | Cắt khoảng trắng lý do, lưu entity `OrderReturn` | Đơn chuyển sang `RETURN_PENDING` |
| `TC_RET_16` | `updateReturnStatus` | `updateReturnStatus_rejectsInvalidActionOrNote` | Action không hợp lệ (khác APPROVE/REJECT) | Nhánh kiểm tra hành động duyệt | Từ chối xử lý |
| `TC_RET_17` | `updateReturnStatus` | `updateReturnStatus_rejectsMissingOrder` | Đơn không tồn tại khi Admin duyệt | Nhánh `if (order == null)` = True | Báo lỗi không tìm thấy đơn |
| `TC_RET_18` | `updateReturnStatus` | `updateReturnStatus_rejectsSellerWithoutWholeOrderOwnership` | Seller không sở hữu toàn bộ đơn hàng | Nhánh kiểm tra quyền sở hữu Seller | Chặn quyền duyệt trả hàng |
| `TC_RET_19` | `updateReturnStatus` | `updateReturnStatus_rejectsMissingReturnRequest` | Thiếu bản ghi yêu cầu hoàn trả | Nhánh `if (returnReq == null)` = True | Báo lỗi thiếu request hoàn trả |
| `TC_RET_20` | `updateReturnStatus` | `updateReturnStatus_rejectsAlreadyProcessedRequest` | Yêu cầu đã được duyệt/từ chối trước đó | Nhánh `if (!"PENDING".equals(status))` | Chặn xử lý lại yêu cầu cũ |
| `TC_RET_21` | `updateReturnStatus` | `updateReturnStatus_rejectsOrderOutsideReturnPendingState` | Đơn không ở trạng thái `RETURN_PENDING` | Nhánh kiểm tra trạng thái đơn hợp lệ | Chặn duyệt sai quy trình |
| `TC_RET_22` | `updateReturnStatus` | `updateReturnStatus_approveRestoresStockAndMarksReturned` | Action = `APPROVE` | Cập nhật `RETURNED` + cộng lại kho | Trạng thái RETURNED, hoàn kho |
| `TC_RET_23` | `updateReturnStatus` | `updateReturnStatus_approveSkipsDeletedProduct` | Sản phẩm đã bị xóa vĩnh viễn khỏi CSDL | Nhánh `if (prod == null)` bỏ qua hoàn kho | Hoàn trả không văng lỗi |
| `TC_RET_24` | `updateReturnStatus` | `updateReturnStatus_rejectReturnsOrderToCompletedWithoutStockMutation` | Action = `REJECT` | Chuyển lại trạng thái cũ `DELIVERED` | Đơn về cũ, không đổi tồn kho |
| `TC_RET_25` | `updateReturnStatus` | `updateReturnStatus_rejectAllowsMissingAdminNote` | Admin từ chối không kèm lý do | Nhánh cho phép `adminNote == null` | Vẫn cập nhật từ chối thành công |

---

## 📊 PHẦN 3: KẾT QUẢ ĐO LƯỜNG ĐỘ BAO PHỦ (JACOCO METRICS)

* **Lớp kiểm thử:** `com.example.demo.dao.OrderReturnDAO`
* **Statement Coverage (Instructions):** **415 / 420 (98.8%)**
* **Branch Coverage (Branches):** **52 / 52 (100.0%)**
* **Đánh giá:** Đạt độ phủ nhánh tuyệt đối 100%. Bao phủ trọn vẹn máy trạng thái vòng đời hủy đơn và trả hàng (FSM: PENDING ➔ CANCELLED, DELIVERED ➔ RETURN_PENDING ➔ RETURNED / DELIVERED).
