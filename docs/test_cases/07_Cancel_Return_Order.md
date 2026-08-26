# Bảng Test Case: Chức năng 7 - Hủy / Trả hàng (Cancel & Return)
**Người thực hiện:** Được

### 7.1 Xác định các Biến điều kiện (Conditions - C)

| Ký hiệu | Tên biến điều kiện (Condition) | Giải thích logic nghiệp vụ |
| :--- | :--- | :--- |
| C1 | Thuộc sở hữu (Ownership) | Người đang thao tác có phải là Chủ của đơn hàng đó không (hoặc là Admin). |
| C2 | Đúng Trạng thái (Valid State) | Đơn hàng có đang ở đúng trạng thái được phép Hủy/Trả không (VD PENDING mới được Hủy). |
| C3 | Có Lý do (Valid Reason) | Khách hàng có điền lý do Hủy/Trả hàng không (Nội dung không rỗng). |

### 7.2 Bảng Rút Gọn Quy Tắc (Chuyển đổi trạng thái)

| Quy tắc (Rule) | C1: Thuộc sở hữu? | C2: Đúng trạng thái? | C3: Có Lý do không rỗng? | Kết quả / Hành động hệ thống | Số kịch bản lý thuyết đã gộp | Test Case tương ứng |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| Rule 1 (Lỗi quyền) | No | - | - | Chặn thao tác báo lỗi 403 Forbidden | Gộp 4 kịch bản | TC_CAN_005 |
| Rule 2 (Lỗi sai luồng) | Yes | No | - | Chặn thao tác báo lỗi 400 (Trạng thái không hợp lệ) | Gộp 2 kịch bản | TC_CAN_003 |
| Rule 3 (Lỗi thiếu lý do) | Yes | Yes | No | Chặn thao tác báo lỗi 400 (Vui lòng nhập lý do) | Chiếm 1 kịch bản | TC_CAN_004 |
| Rule 4 (Hủy thành công) | Yes | Yes | Yes | Đổi trạng thái sang CANCELLED cộng lại Tồn kho | Tách từ kịch bản Hợp lệ | TC_CAN_001 |
| Rule 5 (Trả thành công) | Yes | Yes | Yes | Đổi trạng thái sang RETURN_PENDING chờ Admin duyệt | Tách từ kịch bản Hợp lệ | TC_CAN_002 |

### 7.3 Bảng Test Case Chi Tiết

| Mã kiểm thử | Kỹ thuật áp dụng | Tiêu đề | Điều kiện tiên quyết | Các bước kiểm tra | Dữ liệu kiểm thử | Kết quả dự kiến | Kết quả thực tế | Trạng thái |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| TC_CAN_001 | Chuyển đổi trạng thái (Luật 4) | Kiểm tra khách hàng Hủy đơn hàng thành công khi đơn đang chờ duyệt | Đơn hàng số 100 đang ở trạng thái PENDING và Người dùng đã đăng nhập đúng tài khoản mua. | Vào Chi tiết đơn hàng sau đó nhập lý do hủy và Bấm nút Hủy đơn hàng. | Action là CANCEL và Lý do là Đổi ý không mua nữa. | Trạng thái đơn hàng chuyển sang CANCELLED và Sản phẩm được cộng lại vào kho. | Chuyển trạng thái thành CANCELLED thành công. | Pass |
| TC_CAN_002 | Chuyển đổi trạng thái (Luật 5) | Kiểm tra khách hàng Yêu cầu Trả hàng thành công khi đã nhận hàng | Đơn hàng số 101 đang ở trạng thái COMPLETED (Đã giao xong). | Vào Chi tiết đơn hàng sau đó nhập lý do trả và Bấm nút Yêu cầu trả hàng. | Action là RETURN và Lý do là Giày bị rách gót. | Trạng thái chuyển sang RETURN_PENDING (Chờ trả hàng) và Gửi thông báo cho Admin duyệt. | Đổi trạng thái thành RETURN_PENDING hợp lệ. | Pass |
| TC_CAN_003 | Chuyển đổi trạng thái (Luật 2 Sai luồng) | Kiểm tra hệ thống chặn Hủy đơn khi hàng đang trên đường giao | Đơn hàng số 102 đang ở trạng thái SHIPPING (Đang giao). | Khách hàng cố tình gửi API Hủy đơn hàng và chờ Server phản hồi. | Action là CANCEL và Trạng thái hiện tại là SHIPPING. | Server chặn lại và báo lỗi Không thể hủy đơn hàng đang được giao. | Báo lỗi 400 Bad Request và không đổi trạng thái. | Pass |
| TC_CAN_004 | Đoán lỗi (Luật 3 Thiếu lý do) | Kiểm tra hệ thống chặn Hủy đơn khi khách hàng bỏ trống lý do | Đơn hàng ở trạng thái PENDING. | Bấm nút Hủy đơn hàng nhưng cố tình bỏ trống ô nhập lý do rồi bấm Xác nhận. | Lý do hủy bị bỏ trống hoàn toàn. | Hệ thống từ chối Hủy và Báo cảnh báo Vui lòng nhập lý do hủy đơn. | Cảnh báo đỏ hiện lên và đơn hàng vẫn giữ nguyên. | Pass |
| TC_CAN_005 | Phân hoạch lớp tương đương (Luật 1 Sai quyền) | Kiểm tra hệ thống chặn Hacker cố tình Hủy đơn hàng của người khác | Người dùng Hacker đăng nhập thành công nhưng cố tình Hủy đơn hàng số 999 (của người khác). | Bắt gói tin API và Đổi ID đơn hàng thành 999 của người khác rồi Gửi lệnh Hủy. | ID đơn hàng của User khác và Action là CANCEL. | Server phát hiện sai chủ sở hữu và Chặn lại rồi báo lỗi 403 Forbidden. | Server trả về 403 và bảo vệ an toàn cho đơn hàng 999. | Pass |
