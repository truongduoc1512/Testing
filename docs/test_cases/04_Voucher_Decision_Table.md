# Bảng Test Case: Chức năng 4 - Áp dụng Mã giảm giá (Vouchers)
**Người thực hiện:** Được (Thiết kế Bảng quyết định và API E2E)

### 4.1 Bảng Rút Gọn Điều Kiện (Collapsed Decision Table)

| Quy tắc (Rule) | C1: Tồn tại trong hệ thống? | C2: Còn hạn sử dụng? | C3: Còn lượt sử dụng? | C4: Đạt đơn tối thiểu? | Kết quả / Hành động hệ thống | Số kịch bản lý thuyết đã gộp | Test Case tương ứng |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| Rule 1 (Lỗi mã ảo) | No | - | - | - | Báo lỗi Không tồn tại | 8 kịch bản | TC_VOU_005 |
| Rule 2 (Lỗi hết hạn) | Yes | No | - | - | Báo lỗi Hết hạn sử dụng | 4 kịch bản | TC_VOU_003 |
| Rule 3 (Lỗi hết lượt) | Yes | Yes | No | - | Báo lỗi Hết lượt sử dụng | 2 kịch bản | TC_VOU_004 |
| Rule 4 (Lỗi chưa đủ tiền) | Yes | Yes | Yes | No | Báo lỗi Chưa đạt mốc tối thiểu | 1 kịch bản | TC_VOU_002 |
| Rule 5 (Đường đi hoàn hảo) | Yes | Yes | Yes | Yes | Áp dụng Voucher thành công | 1 kịch bản | TC_VOU_001 |

### 4.2 Bảng Test Case Chi Tiết

| Mã kiểm thử | Kỹ thuật áp dụng | Tiêu đề | Điều kiện tiên quyết | Các bước kiểm tra | Dữ liệu kiểm thử | Kết quả dự kiến | Kết quả thực tế | Trạng thái |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| TC_VOU_001 | Bảng quyết định (Luật 1 Hoàn hảo) | Kiểm tra áp dụng thành công mã giảm giá hợp lệ | Giỏ hàng có tổng tiền 150 đô. Khách hàng ở màn hình Checkout. | Nhập mã Voucher rồi bấm nút Áp dụng. | Voucher là SAVE20 (Mã đang có hiệu lực, giảm 20 phần trăm, điều kiện đơn tối thiểu 100 đô). | Hệ thống báo thành công. Tiền giảm giá bằng 30 đô. Tổng tiền thanh toán còn 120 đô. | Áp mã thành công và trừ đúng 30 đô. | Pass |
| TC_VOU_002 | Bảng quyết định (Luật 2 Lỗi chưa đủ điều kiện) | Kiểm tra chặn áp mã khi đơn hàng chưa đạt giá trị tối thiểu (Min Order Value) | Giỏ hàng có tổng tiền 80 đô (Thiếu 20 đô để đạt mốc). | Nhập mã Voucher rồi bấm nút Áp dụng. | Voucher là SAVE20 (Mã đang có hiệu lực nhưng yêu cầu hóa đơn phải lớn hơn 100 đô). | Hệ thống từ chối áp mã. Báo lỗi Đơn hàng chưa đạt giá trị tối thiểu 100 đô để sử dụng mã này. Tiền giảm bằng 0 đô. | Trả về lỗi cảnh báo đỏ hợp lệ. | Pass |
| TC_VOU_003 | Bảng quyết định (Luật 3 Lỗi hết hạn) | Kiểm tra chặn áp mã khi Voucher đã quá hạn sử dụng (Expired) | Giỏ hàng có tổng tiền 200 đô. Đạt đủ điều kiện tối thiểu. | Nhập mã Voucher quá hạn rồi bấm nút Áp dụng. | Voucher là BLACKFRIDAY (Mã đã hết hạn từ tháng trước). | Hệ thống từ chối áp mã. Báo lỗi Mã giảm giá đã hết hạn sử dụng. Tiền giảm bằng 0 đô. | Báo lỗi Hết hạn và không trừ tiền. | Pass |
| TC_VOU_004 | Bảng quyết định (Luật 4 Lỗi hết lượt) | Kiểm tra chặn áp mã khi Voucher đã bị xài hết số lượt cho phép (Usage Limit) | Giỏ hàng hợp lệ 200 đô. Mã vẫn còn trong thời hạn sử dụng. | Nhập mã Voucher đã cạn lượt rồi bấm nút Áp dụng. | Voucher là LIMIT50 (Mã cho 50 người, đã có 50 người xài trước đó). | Hệ thống từ chối áp mã. Báo lỗi Mã giảm giá đã hết lượt sử dụng. Tiền giảm bằng 0 đô. | Báo lỗi Hết lượt và không trừ tiền. | Pass |
| TC_VOU_005 | Phân hoạch lớp tương đương (EP) | Kiểm tra hệ thống khi người dùng nhập mã rác (Mã không tồn tại trong DB) | Giỏ hàng hợp lệ. | Nhập 1 mã bậy bạ tự chế rồi bấm nút Áp dụng. | Voucher là HACKER123. | Hệ thống từ chối áp mã. Báo lỗi Mã giảm giá không tồn tại. | Trả về lỗi Không tìm thấy mã. | Pass |
