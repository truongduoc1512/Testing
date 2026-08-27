# Bảng Test Case: Chức năng 4 - Áp dụng Mã giảm giá (Vouchers)
**Người thực hiện:** Được 

## 1. Thông tin Kỹ thuật & Thực thi
- **Kỹ thuật Thiết kế (Test Design):** Bảng quyết định (Decision Table). Sử dụng để tối ưu hóa $2^4 = 16$ kịch bản tổ hợp thành 5 Quy tắc mấu chốt (Rules).
- **Kỹ thuật Thực thi (Test Execution):** Kiểm thử API End-to-End (Black-box E2E API Testing).
- **File Code Thực thi (Automation Script):** Toàn bộ kịch bản được tự động hóa bằng phần mềm Postman/Newman và lưu tại file: `docs/Shoeshop_API_Collection.json`.

---

## 2. Bảng Rút Gọn Điều Kiện (Collapsed Decision Table)

| Condition/Action | R1 | R2 | R3 | R4 | R5 |
| :--- | :---: | :---: | :---: | :---: | :---: |
| **C1: Tồn tại trong DB?** | N | Y | Y | Y | Y |
| **C2: Còn hạn sử dụng?** | - | N | Y | Y | Y |
| **C3: Còn lượt sử dụng?** | - | - | N | Y | Y |
| **C4: Đạt đơn tối thiểu?** | - | - | - | N | Y |
| **A1: Báo lỗi Không tồn tại** | X | - | - | - | - |
| **A2: Báo lỗi Hết hạn sử dụng** | - | X | - | - | - |
| **A3: Báo lỗi Hết lượt sử dụng** | - | - | X | - | - |
| **A4: Báo lỗi Chưa đạt mốc** | - | - | - | X | - |
| **A5: Áp dụng Voucher thành công** | - | - | - | - | X |
| **Test Case Tương ứng** | TC_VOU_005 | TC_VOU_003 | TC_VOU_004 | TC_VOU_002 | TC_VOU_001 |

---

## 3. Bảng Test Case Chi Tiết

| Mã kiểm thử | Kỹ thuật áp dụng | Tiêu đề | Điều kiện tiên quyết | Các bước kiểm tra | Dữ liệu kiểm thử | Kết quả dự kiến | Kết quả thực tế | Trạng thái |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| TC_VOU_001 | Bảng quyết định (R5) / EP / BVA | Kiểm tra áp dụng thành công mã giảm giá hợp lệ | Giỏ hàng có tổng tiền 150 đô. Khách hàng ở màn hình Checkout. | Nhập mã Voucher rồi bấm nút Áp dụng. | Voucher là SAVE20 (Mã đang có hiệu lực, giảm 20 phần trăm, điều kiện đơn tối thiểu 100 đô). | Hệ thống báo thành công. Tiền giảm giá bằng 30 đô. Tổng tiền thanh toán còn 120 đô. | Áp mã thành công và trừ đúng 30 đô. | Pass |
| TC_VOU_002 | Bảng quyết định (R4) / BVA | Kiểm tra chặn áp mã khi đơn hàng chưa đạt giá trị tối thiểu (Min Order Value) | Giỏ hàng có tổng tiền 80 đô (Thiếu 20 đô để đạt mốc). | Nhập mã Voucher rồi bấm nút Áp dụng. | Voucher là SAVE20 (Mã đang có hiệu lực nhưng yêu cầu hóa đơn phải lớn hơn 100 đô). | Hệ thống từ chối áp mã. Báo lỗi Đơn hàng chưa đạt giá trị tối thiểu 100 đô để sử dụng mã này. Tiền giảm bằng 0 đô. | Trả về lỗi cảnh báo đỏ hợp lệ. | Pass |
| TC_VOU_003 | Bảng quyết định (R2) / EP | Kiểm tra chặn áp mã khi Voucher đã quá hạn sử dụng (Expired) | Giỏ hàng có tổng tiền 200 đô. Đạt đủ điều kiện tối thiểu. | Nhập mã Voucher quá hạn rồi bấm nút Áp dụng. | Voucher là BLACKFRIDAY (Mã đã hết hạn từ tháng trước). | Hệ thống từ chối áp mã. Báo lỗi Mã giảm giá đã hết hạn sử dụng. Tiền giảm bằng 0 đô. | Báo lỗi Hết hạn và không trừ tiền. | Pass |
| TC_VOU_004 | Bảng quyết định (R3) / BVA | Kiểm tra chặn áp mã khi Voucher đã bị xài hết số lượt cho phép (Usage Limit) | Giỏ hàng hợp lệ 200 đô. Mã vẫn còn trong thời hạn sử dụng. | Nhập mã Voucher đã cạn lượt rồi bấm nút Áp dụng. | Voucher là LIMIT50 (Mã cho 50 người, đã có 50 người xài trước đó). | Hệ thống từ chối áp mã. Báo lỗi Mã giảm giá đã hết lượt sử dụng. Tiền giảm bằng 0 đô. | Báo lỗi Hết lượt và không trừ tiền. | Pass |
| TC_VOU_005 | Bảng quyết định (R1) / EP | Kiểm tra hệ thống khi người dùng nhập mã rác (Mã không tồn tại trong DB) | Giỏ hàng hợp lệ. | Nhập 1 mã bậy bạ tự chế rồi bấm nút Áp dụng. | Voucher là HACKER123. | Hệ thống từ chối áp mã. Báo lỗi Mã giảm giá không tồn tại. | Trả về lỗi Không tìm thấy mã. | Pass |
