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

### 2.2 Bảng Phân hoạch lớp tương đương (Equivalence Partitioning - EP)

| STT | Điều kiện đầu vào (Input / Condition) | Lớp tương đương Hợp lệ (Valid EP) | Lớp tương đương Không hợp lệ (Invalid EP) | Test Case liên quan |
| :---: | :--- | :--- | :--- | :---: |
| 1 | **Vai trò người dùng (User Role)** | - Khách hàng `ROLE_USER` (Thực hiện Hủy đơn / Tạo yêu cầu Trả hàng)<br>- Quản trị viên `ROLE_ADMIN` (Thực hiện Duyệt / Từ chối đơn trả hàng) | - Khách vãng lai chưa đăng nhập (Báo lỗi 401 Unauthorized)<br>- Khách hàng `ROLE_USER` cố tình gọi API duyệt của Admin (Báo lỗi 403 Forbidden) | TC_CAN_004<br>TC_CAN_010 |
| 2 | **Quyền sở hữu đơn hàng (Order Ownership)** | Khách hàng thực hiện thao tác trên đơn hàng do chính mình sở hữu (`customer = currentUser`) | Khách hàng A thao tác trên mã đơn hàng của Khách hàng B (Báo lỗi 403 Forbidden) | TC_CAN_004 |
| 3 | **Trạng thái đơn hàng khi Hủy (Cancel Order State)** | Đơn hàng đang ở trạng thái Chờ xử lý (`PENDING`) | Đơn hàng đang giao (`SHIPPING`), Đã nhận (`COMPLETED`), Đã hủy (`CANCELLED`), hoặc Đã trả (`RETURNED`) | TC_CAN_001<br>TC_CAN_003 |
| 4 | **Trạng thái đơn hàng khi Trả hàng (Return Order State)** | Đơn hàng đã hoàn tất giao dịch (`COMPLETED`) | Đơn hàng đang chờ (`PENDING`), Đang giao (`SHIPPING`), hoặc Đã hủy (`CANCELLED`) | TC_CAN_005<br>TC_CAN_003 |
| 5 | **Tính duy nhất của yêu cầu (Request Duplicate)** | Đơn hàng chưa có yêu cầu trả hàng nào đang chờ xử lý (`hasPendingReturn = false`) | Đơn hàng đã tồn tại yêu cầu trả hàng đang ở trạng thái chờ duyệt (`RETURN_PENDING`) | TC_CAN_006 |
| 6 | **Tính toàn vẹn dữ liệu Form Trả hàng (Return Form Data)** | Form điền đầy đủ lý do hợp lệ (1 - 2000 ký tự) và URL hình ảnh minh chứng ($\le 500$ ký tự) | - Lý do bị bỏ trống, null, hoặc chỉ toàn khoảng trắng<br>- URL hình ảnh vượt quá 500 ký tự | TC_CAN_007 |

### 2.3 Bảng Phân tích giá trị biên (Boundary Value Analysis - BVA)

| STT | Biến kiểm thử / Ràng buộc logic | Điểm biên BVA | Giá trị kiểm thử | Phân loại BVA | Kết quả dự kiến (Expected Output) | Test Case |
| :---: | :--- | :---: | :---: | :---: | :--- | :---: |
| 1 | **Lượt bán khi Hủy đơn (`salesCount`)**<br>*Ràng buộc: Thuật toán không bao giờ để số lượt bán rớt xuống âm: $Sales = \max(0, Sales - quantity)$* | min - 1 *(Chống lỗi âm)* | 0 lượt (Hủy đơn 1 sp) | Invalid Boundary | Tồn kho phục hồi đầy đủ, lượt bán bị chặn đứng tại `0` (không tụt xuống `-1`) | TC_CAN_002 |
| 2 | **Lượt bán khi Hủy đơn (`salesCount`)** | min | 1 lượt (Hủy đơn 1 sp) | Valid (Biên dưới) | Lượt bán giảm từ 1 về `0` thành công | TC_CAN_002 |
| 3 | **Lượt bán khi Hủy đơn (`salesCount`)** | min + 1 | 2 lượt (Hủy đơn 1 sp) | Valid | Lượt bán giảm từ 2 về `1` thành công | TC_CAN_002 |
| 4 | **Độ dài lý do trả hàng (`reason`)**<br>*Ràng buộc: $1 \le \text{length}(reason) \le 2000$ ký tự* | min - 1 | 0 ký tự (`""` rỗng) | Invalid (Dưới biên) | Báo lỗi 400 Bad Request (Validation Error) | TC_CAN_007 |
| 5 | **Độ dài lý do trả hàng (`reason`)** | min | 1 ký tự | Valid (Biên dưới) | Hợp lệ, chấp nhận lý do | TC_CAN_007 |
| 6 | **Độ dài lý do trả hàng (`reason`)** | max | 2000 ký tự | Valid (Biên trên) | Hợp lệ, chấp nhận lý do | TC_CAN_007 |
| 7 | **Độ dài lý do trả hàng (`reason`)** | max + 1 | 2001 ký tự | Invalid (Vượt biên) | Báo lỗi 400 Bad Request (Lý do quá dài) | TC_CAN_007 |
| 8 | **Độ dài liên kết hình ảnh (`imageUrl`)**<br>*Ràng buộc: $\text{length}(imageUrl) \le 500$ ký tự* | max | 500 ký tự | Valid (Biên trên) | Hợp lệ, lưu trữ URL ảnh minh chứng | TC_CAN_007 |
| 9 | **Độ dài liên kết hình ảnh (`imageUrl`)** | max + 1 | 501 ký tự | Invalid (Vượt biên) | Báo lỗi 400 Bad Request (Đường dẫn ảnh quá dài) | TC_CAN_007 |

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
