# Bảng Test Case: Chức năng 7 - Hủy / Trả hàng (Cancel & Return)
**Người thực hiện:** Được 

## 1. Thông tin Kỹ thuật & Thực thi
- **Kỹ thuật Thiết kế (Test Design):** 
  - **Chuyển đổi trạng thái (State Transition):** Mô hình hóa vòng đời của đơn hàng để chặn các thao tác chuyển đổi phi logic (Sai luồng).
  - **Bảng quyết định (Decision Table):** Sử dụng để kết hợp 3 ràng buộc bảo mật và nghiệp vụ (Quyền sở hữu, Trạng thái, Lý do).
- **Kỹ thuật Thực thi (Test Execution):** Kiểm thử API End-to-End (Black-box E2E API Testing).
- **File Code Thực thi (Automation Script):** Toàn bộ kịch bản được tự động hóa bằng phần mềm Postman/Newman và lưu tại file: `docs/Shoeshop_API_Collection.json`.

---

## 2. Phân tích Kỹ thuật Thiết kế (Test Design Analysis)

### 2.1 Bảng Chuyển đổi trạng thái (State Transition Table)
Đơn hàng trong hệ thống có vòng đời cơ bản là: `PENDING` (Chờ xử lý) ➔ `SHIPPING` (Đang giao) ➔ `COMPLETED` (Hoàn thành). Việc Hủy/Trả phải tuân theo sơ đồ chuyển đổi mút trạng thái nghiêm ngặt:

| Trạng thái hiện tại (Current State) | Hành động kích hoạt (Input/Action) | Trạng thái kỳ vọng (Expected Next State) | Tính hợp lệ (Validity) |
| :--- | :---: | :---: | :--- |
| **PENDING** | Yêu cầu Hủy (Cancel) | **CANCELLED** | Hợp lệ (Hủy thành công) |
| **COMPLETED** | Yêu cầu Trả hàng (Return) | **RETURN_PENDING** | Hợp lệ (Chờ Admin duyệt trả) |
| **SHIPPING** | Yêu cầu Hủy (Cancel) | *(Giữ nguyên SHIPPING)* | Không hợp lệ (Lỗi: Hàng đang giao) |
| **PENDING** | Yêu cầu Trả hàng (Return) | *(Giữ nguyên PENDING)* | Không hợp lệ (Lỗi: Chưa nhận hàng) |

### 2.2 Ma trận Quyết định tổng hợp (Collapsed Decision Table)
Kết hợp bài toán Trạng thái (ở trên) với quyền Sở hữu và Lý do để ra được Ma trận Test:

| Condition/Action | R1 | R2 | R3 | R4 | R5 |
| :--- | :---: | :---: | :---: | :---: | :---: |
| **C1: Thuộc sở hữu (Chính chủ)?** | N | Y | Y | Y | Y |
| **C2: Đúng trạng thái hợp lệ? (Bảng 2.1)** | - | N | Y | Y | Y |
| **C3: Có điền lý do Hủy/Trả?** | - | - | N | Y | Y |
| **C4: Hành động (Action) là gì?** | - | - | - | Cancel | Return |
| **A1: Báo lỗi 403 Forbidden (Sai quyền)** | X | - | - | - | - |
| **A2: Báo lỗi 400 (Sai luồng trạng thái)** | - | X | - | - | - |
| **A3: Báo lỗi 400 (Bỏ trống lý do)** | - | - | X | - | - |
| **A4: Đổi trạng thái ➔ CANCELLED** | - | - | - | X | - |
| **A5: Đổi trạng thái ➔ RETURN_PENDING**| - | - | - | - | X |
| **Test Case Tương ứng** | TC_CAN_005 | TC_CAN_003 | TC_CAN_004 | TC_CAN_001 | TC_CAN_002 |

---

## 3. Bảng Test Case Chi Tiết

| Mã kiểm thử | Kỹ thuật áp dụng | Tiêu đề | Điều kiện tiên quyết | Các bước kiểm tra | Dữ liệu kiểm thử | Kết quả dự kiến | Kết quả thực tế | Trạng thái |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| TC_CAN_001 | State & Decision (R4) | Kiểm tra khách hàng Hủy đơn hàng thành công khi đơn đang chờ duyệt | Đơn hàng số 100 đang ở trạng thái PENDING và Người dùng đã đăng nhập đúng tài khoản mua. | Vào Chi tiết đơn hàng sau đó nhập lý do hủy và Bấm nút Hủy đơn hàng. | Action là CANCEL và Lý do là Đổi ý không mua nữa. | Trạng thái đơn hàng chuyển sang CANCELLED và Sản phẩm được cộng lại vào kho. | Chuyển trạng thái thành CANCELLED thành công. | Pass |
| TC_CAN_002 | State & Decision (R5) | Kiểm tra khách hàng Yêu cầu Trả hàng thành công khi đã nhận hàng | Đơn hàng số 101 đang ở trạng thái COMPLETED (Đã giao xong). | Vào Chi tiết đơn hàng sau đó nhập lý do trả và Bấm nút Yêu cầu trả hàng. | Action là RETURN và Lý do là Giày bị rách gót. | Trạng thái chuyển sang RETURN_PENDING (Chờ trả hàng) và Gửi thông báo cho Admin duyệt. | Đổi trạng thái thành RETURN_PENDING hợp lệ. | Pass |
| TC_CAN_003 | State & Decision (R2) | Kiểm tra hệ thống chặn Hủy đơn khi hàng đang trên đường giao | Đơn hàng số 102 đang ở trạng thái SHIPPING (Đang giao). | Khách hàng cố tình gửi API Hủy đơn hàng và chờ Server phản hồi. | Action là CANCEL và Trạng thái hiện tại là SHIPPING. | Server chặn lại và báo lỗi Không thể hủy đơn hàng đang được giao. | Báo lỗi 400 Bad Request và không đổi trạng thái. | Pass |
| TC_CAN_004 | State & Decision (R3) | Kiểm tra hệ thống chặn Hủy đơn khi khách hàng bỏ trống lý do | Đơn hàng ở trạng thái PENDING. | Bấm nút Hủy đơn hàng nhưng cố tình bỏ trống ô nhập lý do rồi bấm Xác nhận. | Lý do hủy bị bỏ trống hoàn toàn. | Hệ thống từ chối Hủy và Báo cảnh báo Vui lòng nhập lý do hủy đơn. | Cảnh báo đỏ hiện lên và đơn hàng vẫn giữ nguyên. | Pass |
| TC_CAN_005 | EP & Decision (R1) | Kiểm tra hệ thống chặn Hacker cố tình Hủy đơn hàng của người khác | Người dùng Hacker đăng nhập thành công nhưng cố tình Hủy đơn hàng số 999 (của người khác). | Bắt gói tin API và Đổi ID đơn hàng thành 999 của người khác rồi Gửi lệnh Hủy. | ID đơn hàng của User khác và Action là CANCEL. | Server phát hiện sai chủ sở hữu và Chặn lại rồi báo lỗi 403 Forbidden. | Server trả về 403 và bảo vệ an toàn cho đơn hàng 999. | Pass |
