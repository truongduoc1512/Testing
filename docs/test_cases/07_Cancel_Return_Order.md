# Bảng Test Case: Chức năng 7 - Hủy / Trả hàng (Cancel & Return)
**Người thực hiện:** Được 

## 1. Thông tin Kỹ thuật & Thực thi
- **Kỹ thuật Thiết kế (Test Design):** 
  - **Chuyển đổi trạng thái (State Transition):** Mô hình hóa vòng đời khép kín của đơn hàng, chặn đứng mọi thao tác phi logic.
  - **Phân hoạch lớp tương đương (EP):** Quản lý phân quyền (Khách hàng tạo yêu cầu vs Admin duyệt yêu cầu) và Quyền sở hữu (Ownership).
  - **Phân tích giá trị biên (BVA):** Đảm bảo thuật toán trừ lượt Bán (Sales) không bị văng xuống số Âm, và độ dài ảnh/lý do trả hàng.
- **Kỹ thuật Thực thi (Test Execution):** 
  - Kiểm thử Tích hợp & Unit Test bằng JUnit/Mockito (`OrderCancelReturnTests.java`, `OrderReturnDAOTest.java`).
  - Kiểm thử API E2E bằng Postman.

---

## 2. Phân tích Kỹ thuật Thiết kế (Test Design Analysis)

### 2.1 Bảng Chuyển đổi trạng thái (State Transition Table)
Đơn hàng có sơ đồ chuyển trạng thái (FSM) cực kỳ nghiêm ngặt. Bất kỳ sự vượt rào nào đều bị hệ thống chặn đứng:

| Trạng thái hiện tại | Thao tác (Action) | Quyền thực thi | Trạng thái kỳ vọng (Next State) | Tác động Kho hàng | Tính hợp lệ |
| :--- | :---: | :---: | :---: | :--- | :--- |
| **PENDING** | Hủy đơn (Cancel) | Khách hàng | **CANCELLED** | + Trả lại Tồn kho<br>- Giảm lượt Sales | Hợp lệ |
| **COMPLETED** | Xin trả hàng (Return) | Khách hàng | **RETURN_PENDING** | (Không đổi) | Hợp lệ (Chờ duyệt) |
| **RETURN_PENDING**| Duyệt trả (Approve) | Admin | **RETURNED** | + Trả lại Tồn kho<br>- Giảm lượt Sales | Hợp lệ |
| **RETURN_PENDING**| Từ chối trả (Reject)| Admin | **COMPLETED** | (Không đổi) | Hợp lệ |
| **SHIPPING** | Hủy đơn / Xin trả | Khách hàng | *(Giữ nguyên)* | (Không đổi) | Báo lỗi 400 |

### 2.2 Bảng Phân hoạch lớp tương đương (EP)
1. **Phân quyền Role:** Guest (401), Khách hàng ROLE_USER (Chỉ được Hủy/Xin trả), Quản trị viên ROLE_ADMIN (Chỉ được Duyệt/Từ chối).
2. **Quyền sở hữu (Ownership):** Khách hàng chỉ thao tác trên đơn của mình. Admin chỉ thao tác trên đơn được phân quyền quản lý (Scope).
3. **Chống trùng lặp (Duplicate):** Khi đơn đã ở `RETURN_PENDING`, cấm tạo thêm request trả hàng thứ 2.

### 2.3 Bảng Giá trị biên (BVA)
Ràng buộc nguy hiểm: Khi khách Hủy đơn, hệ thống sẽ Trừ lượt Sales của Sản phẩm đó đi tương ứng. 
=> **Câu hỏi:** Nếu Sales hiện tại đang là `0`, mà khách Hủy đơn thì thuật toán có bị lỗi thành `-1` (Âm) không?
- **Biên test:** 0, 1, 2, 3. Kết quả mong đợi: Số Sales không bao giờ được phép rớt xuống dưới 0.
- **Giới hạn văn bản:** Lý do (tối đa 2000 ký tự), Hình ảnh (tối đa 500 ký tự).

---

## 3. Bảng Test Case Chi Tiết

| Mã kiểm thử | Kỹ thuật áp dụng | Tiêu đề | Điều kiện tiên quyết | Các bước kiểm tra | Kết quả dự kiến | Kết quả thực tế | Trạng thái |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **TC_CAN_001** | State (Hợp lệ) | Khách Hủy đơn hàng PENDING thành công | Đơn hàng trạng thái `PENDING`. | Khách hàng gọi API Cancel. | Đơn chuyển thành `CANCELLED`. | Khớp Unit Test API. | Pass |
| **TC_CAN_002** | BVA (Toán học) | Thuật toán Hủy đơn phục hồi Tồn kho nhưng không làm Âm lượt Sales | Đơn hàng `PENDING`. Sản phẩm có `Sales = 0`. | Gọi API Hủy đơn hàng. | Tồn kho (Stock) được cộng trả lại đầy đủ, nhưng Lượt bán (Sales) bị chặn đứng ở mức `0`, không tụt xuống `-1`. | Khớp Unit Test `cancelOrder_restoresStockAndNeverMakesSalesNegative`. | Pass |
| **TC_CAN_003** | State (Báo lỗi) | Chặn Hủy/Trả đơn hàng sai trạng thái logic | Đơn đang `SHIPPING`. | Gọi API Hủy đơn. | Trả về IllegalStateException, cấm hủy đơn đang giao. | Khớp Unit Test. | Pass |
| **TC_CAN_004** | EP (Ownership) | Chặn Hacker thao tác đơn hàng của người khác | Khách A đăng nhập. | Khách A gọi API Hủy/Trả vào ID đơn hàng của Khách B. | API chặn lại, báo lỗi Ownership (Không phải chính chủ). | Khớp Unit Test `cancelOrder_rejectsMissingOrDifferentCustomer`. | Pass |
| **TC_CAN_005** | State (Hợp lệ) | Khách tạo Yêu cầu Trả hàng (Return) thành công | Đơn đã `COMPLETED`. | Gửi form điền lý do hợp lệ. | Đơn chuyển thành `RETURN_PENDING` (Chờ Admin duyệt). | Khớp DAO `createReturnRequest_trimsFieldsPersistsAndTagsOrder`. | Pass |
| **TC_CAN_006** | EP (Duplicate) | Chặn tạo nhiều yêu cầu Trả hàng trùng lặp trên cùng 1 đơn | Đơn đang chờ duyệt (`RETURN_PENDING`). | Cố tình spam gọi API xin trả hàng lần thứ 2. | Báo lỗi IllegalStateException (Đơn đã có yêu cầu xử lý rồi). | Khớp DAO `createReturnRequest_rejectsDuplicateRequest`. | Pass |
| **TC_CAN_007** | BVA (Validation) | Báo lỗi Form Xin trả hàng bỏ trống lý do hoặc ảnh quá dài | Đơn `COMPLETED`. | Điền lý do rỗng `""`, hoặc nhét Link URL hình ảnh dài 501 ký tự. | Văng lỗi Validation 400 Bad Request. | Khớp DAO `createReturnRequest_rejectsEachInvalidFormBoundary`. | Pass |
| **TC_CAN_008** | EP (Admin Role) | Admin Duyệt (Approve) đơn trả hàng thành công | Đơn `RETURN_PENDING`. | Admin gọi API `APPROVE`. | Đơn thành `RETURNED`. Hệ thống tự cộng trả lại Tồn kho kho hàng. | Khớp DAO `updateReturnStatus_approveRestoresStockAndMarksReturned`. | Pass |
| **TC_CAN_009** | EP (Admin Role) | Admin Từ chối (Reject) đơn trả hàng do thiếu bằng chứng | Đơn `RETURN_PENDING`. | Admin gọi API `REJECT` kèm note. | Đơn quay về trạng thái `COMPLETED` cũ. Không đụng chạm gì tới Tồn kho. | Khớp DAO `updateReturnStatus_rejectReturnsOrderToCompletedWithoutStockMutation`. | Pass |
| **TC_CAN_010** | EP (Phân quyền) | Chặn Khách hàng (User) can thiệp vào quyền Duyệt đơn của Admin | Khách hàng đăng nhập. | Tự gọi API `APPROVE` / `REJECT`. | Báo lỗi 403 Forbidden do cố ý xài API của cấp quản lý. | Khớp Unit Test `updateStatus_rejectsNonAdminAuthentication`. | Pass |
