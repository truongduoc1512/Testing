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

| Biến đầu vào / Điều kiện | Lớp tương đương Hợp lệ | Tag | Lớp tương đương Không hợp lệ | Tag |
| :--- | :--- | :---: | :--- | :---: |
| **Vai trò người dùng (`userRole`)** | Khách hàng sở hữu đơn (`ROLE_USER`)<br>Quản trị viên duyệt trả (`ROLE_ADMIN`) | **V1**<br>**V2** | Khách vãng lai chưa đăng nhập<br>User gọi API duyệt của Admin<br>User thao tác đơn hàng của người khác | **X1**<br>**X2**<br>**X3** |
| **Trạng thái khi Hủy đơn (`cancel`)**| Đơn hàng đang ở trạng thái `PENDING` | **V3** | `SHIPPING`, `COMPLETED`, `CANCELLED` | **X4** |
| **Trạng thái khi Trả hàng (`return`)**| Đơn hàng đã hoàn tất giao (`COMPLETED`) | **V4** | `PENDING`, `SHIPPING`, `CANCELLED` | **X5** |
| **Tính duy nhất của yêu cầu** | Chưa có yêu cầu trả hàng đang chờ | **V5** | Đã tồn tại yêu cầu `RETURN_PENDING` | **X6** |
| **Độ dài lý do trả hàng (`reason`)** | Từ 1 đến 2000 ký tự | **V6** | Bỏ trống / null / toàn khoảng trắng<br>Vượt quá 2000 ký tự | **X7**<br>**X8** |
| **Độ dài liên kết ảnh (`imageUrl`)** | Không vượt quá 500 ký tự | **V7** | Vượt quá 500 ký tự | **X9** |

### 2.3 Bảng Phân tích giá trị biên (Standard Boundary Value Analysis - BVA)

| Biến đầu vào | Miền hợp lệ | min | min+ | nominal | max- | max | Tag biên |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :--- |
| **Lượt bán khi Hủy (`salesCount`)** | $[0, 100]$ | 0 | 1 | 10 | 99 | 100 | **B1, B2, B3, B4, B5** |
| **Độ dài lý do trả hàng (`reason`)** | $[1, 2000]$ | 1 | 2 | 50 | 1999 | 2000 | **B6, B7, B8, B9, B10** |
| **Độ dài URL ảnh (`imageUrl`)** | $[1, 500]$ | 1 | 2 | 50 | 499 | 500 | **B11, B12, B13, B14, B15** |

*Ghi chú mở rộng (Robustness BVA):*
- Phòng vệ biên lượt bán: Khi sản phẩm có $Sales = 0$, hủy đơn không được làm âm lượt bán ($Sales \ge 0$, chặn đứng tại 0, kiểm tra tại TC_CAN_002).
- Giá trị ngoài biên lý do: $L = 0$ (Tag **B6-1**), $L = 2001$ (Tag **B10+1**).
- Giá trị ngoài biên URL ảnh: $L = 501$ (Tag **B15+1**).

---

## 3. Bảng Test Case Chi Tiết

| Mã kiểm thử | Kỹ thuật áp dụng | Tiêu đề | Điều kiện tiên quyết | Các bước kiểm tra | Dữ liệu kiểm thử | Kết quả dự kiến | Tag được bao phủ | Kết quả thực tế | Trạng thái |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :---: | :--- | :---: |
| **TC_CAN_001** | State (Hợp lệ) | Khách Hủy đơn hàng PENDING thành công | Đơn hàng trạng thái `PENDING`. | Khách hàng gọi API Cancel. | Dữ liệu chuẩn TC_CAN_001 | Đơn chuyển thành `CANCELLED`. | **V1, V3** | Khớp Unit Test API. | Pass |
| **TC_CAN_002** | BVA (Toán học) | Thuật toán Hủy đơn phục hồi Tồn kho nhưng không làm Âm lượt Sales | Đơn hàng `PENDING`. Sản phẩm có `Sales = 0`. | Gọi API Hủy đơn hàng. | Dữ liệu chuẩn TC_CAN_002 | Tồn kho (Stock) được cộng trả lại đầy đủ, nhưng Lượt bán (Sales) bị chặn đứng ở mức `0`, không tụt xuống `-1`. | **V1, V3, B1** | Khớp Unit Test `cancelOrder_restoresStockAndNeverMakesSalesNegative`. | Pass |
| **TC_CAN_003** | State (Báo lỗi) | Chặn Hủy/Trả đơn hàng sai trạng thái logic | Đơn đang `SHIPPING`. | Gọi API Hủy đơn. | Dữ liệu chuẩn TC_CAN_003 | Trả về IllegalStateException, cấm hủy đơn đang giao. | **X4, X5** | Khớp Unit Test. | Pass |
| **TC_CAN_004** | EP (Ownership) | Chặn Hacker thao tác đơn hàng của người khác | Khách A đăng nhập. | Khách A gọi API Hủy/Trả vào ID đơn hàng của Khách B. | Dữ liệu chuẩn TC_CAN_004 | API chặn lại, báo lỗi Ownership (Không phải chính chủ). | **X3** | Khớp Unit Test `cancelOrder_rejectsMissingOrDifferentCustomer`. | Pass |
| **TC_CAN_005** | State (Hợp lệ) | Khách tạo Yêu cầu Trả hàng (Return) thành công | Đơn đã `COMPLETED`. | Gửi form điền lý do hợp lệ. | Dữ liệu chuẩn TC_CAN_005 | Đơn chuyển thành `RETURN_PENDING` (Chờ Admin duyệt). | **V1, V4, V5, V6, B8** | Khớp DAO `createReturnRequest_trimsFieldsPersistsAndTagsOrder`. | Pass |
| **TC_CAN_006** | EP (Duplicate) | Chặn tạo nhiều yêu cầu Trả hàng trùng lặp trên cùng 1 đơn | Đơn đang chờ duyệt (`RETURN_PENDING`). | Cố tình spam gọi API xin trả hàng lần thứ 2. | Dữ liệu chuẩn TC_CAN_006 | Báo lỗi IllegalStateException (Đơn đã có yêu cầu xử lý rồi). | **X6** | Khớp DAO `createReturnRequest_rejectsDuplicateRequest`. | Pass |
| **TC_CAN_007** | BVA (Validation) | Báo lỗi Form Xin trả hàng bỏ trống lý do hoặc ảnh quá dài | Đơn `COMPLETED`. | Điền lý do rỗng `""`, hoặc nhét Link URL hình ảnh dài 501 ký tự. | Dữ liệu chuẩn TC_CAN_007 | Văng lỗi Validation 400 Bad Request. | **X7, X9, B6-1, B15+1** | Khớp DAO `createReturnRequest_rejectsEachInvalidFormBoundary`. | Pass |
| **TC_CAN_008** | EP (Admin Role) | Admin Duyệt (Approve) đơn trả hàng thành công | Đơn `RETURN_PENDING`. | Admin gọi API `APPROVE`. | Dữ liệu chuẩn TC_CAN_008 | Đơn thành `RETURNED`. Hệ thống tự cộng trả lại Tồn kho kho hàng. | **V2** | Khớp DAO `updateReturnStatus_approveRestoresStockAndMarksReturned`. | Pass |
| **TC_CAN_009** | EP (Admin Role) | Admin Từ chối (Reject) đơn trả hàng do thiếu bằng chứng | Đơn `RETURN_PENDING`. | Admin gọi API `REJECT` kèm note. | Dữ liệu chuẩn TC_CAN_009 | Đơn quay về trạng thái `COMPLETED` cũ. Không đụng chạm gì tới Tồn kho. | **V2** | Khớp DAO `updateReturnStatus_rejectReturnsOrderToCompletedWithoutStockMutation`. | Pass |
| **TC_CAN_010** | EP (Phân quyền) | Chặn Khách hàng (User) can thiệp vào quyền Duyệt đơn của Admin | Khách hàng đăng nhập. | Tự gọi API `APPROVE` / `REJECT`. | Dữ liệu chuẩn TC_CAN_010 | Báo lỗi 403 Forbidden do cố ý xài API của cấp quản lý. | **X2** | Khớp Unit Test `updateStatus_rejectsNonAdminAuthentication`. | Pass |


---

## 4. Bảng Đối Chiếu & Ý Nghĩa Nhãn Tag (Tag Traceability Legend)

| Nhóm Tag | Mã Tag | Ý nghĩa nghiệp vụ | Trạng thái |
| :---: | :---: | :--- | :---: |
| **Valid EP** | **V1** | Khách hàng thao tác trên đơn hàng của chính mình | Hợp lệ |
| | **V2** | Quản trị viên (ROLE_ADMIN) duyệt hoặc từ chối trả hàng | Hợp lệ |
| | **V3** | Hủy đơn hàng ở trạng thái cho phép (PENDING) | Hợp lệ |
| | **V4** | Yêu cầu trả hàng ở trạng thái cho phép (COMPLETED) | Hợp lệ |
| | **V5** | Đơn hàng chưa có yêu cầu trả hàng trùng lặp | Hợp lệ |
| | **V6** | Lý do trả hàng hợp lệ từ 1 đến 2000 ký tự | Hợp lệ |
| | **V7** | Đường dẫn ảnh minh chứng hợp lệ <= 500 ký tự | Hợp lệ |
| **Invalid EP**| **X1** | Khách vãng lai chưa đăng nhập thao tác hủy/trả | Không hợp lệ |
| | **X2** | Khách hàng thường can thiệp quyền duyệt của Admin (Báo lỗi 403) | Không hợp lệ |
| | **X3** | Khách hàng thao tác trên đơn hàng của người khác (Báo lỗi 403) | Không hợp lệ |
| | **X4** | Hủy đơn hàng sai trạng thái (đang giao, đã hoàn tất, đã hủy) | Không hợp lệ |
| | **X5** | Trả hàng sai trạng thái (chưa hoàn tất giao dịch) | Không hợp lệ |
| | **X6** | Đơn hàng đã có yêu cầu trả hàng đang chờ duyệt | Không hợp lệ |
| | **X7** | Lý do trả hàng để trống hoặc null | Không hợp lệ |
| | **X8** | Lý do trả hàng quá dài (> 2000 ký tự) | Không hợp lệ |
| | **X9** | Đường dẫn ảnh minh chứng quá dài (> 500 ký tự) | Không hợp lệ |
| **Boundary** | **B1 - B5**| Điểm biên lượt bán: min (0), min+ (1), nom (10), max- (99), max (100) | Hợp lệ |
| | **B6 - B10**| Điểm biên độ dài lý do: min (1), min+ (2), nom (50), max- (1999), max (2000) | Hợp lệ |
| | **B11 - B15**| Điểm biên độ dài URL ảnh: min (1), min+ (2), nom (50), max- (499), max (500) | Hợp lệ |
