# Kỹ thuật Bảng Quyết Định (Decision Table Testing) - Áp dụng Voucher

## 1. Mục tiêu
Thiết kế Test Cases cho tính năng **Áp dụng Mã giảm giá (Voucher) khi Thanh toán (Checkout)** sử dụng kỹ thuật Bảng Quyết Định (Decision Table) nhằm xử lý các logic IF-ELSE phức tạp.

## 2. Phân tích Điều kiện và Hành động
Theo quy trình nghiệp vụ, để áp dụng một Voucher thành công, hệ thống cần kiểm tra 3 điều kiện:
- **C1:** Mã Voucher có tồn tại và hợp lệ trong hệ thống không?
- **C2:** Mã Voucher còn hạn sử dụng không?
- **C3:** Tổng giá trị giỏ hàng có đạt điều kiện tối thiểu (>= 500k) không?

Các hành động hệ thống có thể phản hồi:
- **A1:** Áp dụng thành công, trừ tiền giảm giá vào tổng đơn hàng.
- **A2:** Báo lỗi "Mã không hợp lệ hoặc không tồn tại".
- **A3:** Báo lỗi "Mã giảm giá đã hết hạn".
- **A4:** Báo lỗi "Đơn hàng chưa đủ điều kiện áp dụng mã này".

## 3. Tính toán số lượng Luật (Rules)
Vì có 3 điều kiện (C1, C2, C3) và mỗi điều kiện có 2 trạng thái (Yes/No), số lượng quy tắc tối đa (Max Rules) là:
**2^3 = 8 Rules**

## 4. Xây dựng Bảng Quyết Định (Full Decision Table)

| Điều kiện / Hành động | Rule 1 | Rule 2 | Rule 3 | Rule 4 | Rule 5 | Rule 6 | Rule 7 | Rule 8 |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| **C1: Mã hợp lệ?** | Y | Y | Y | Y | N | N | N | N |
| **C2: Còn hạn?** | Y | Y | N | N | Y | Y | N | N |
| **C3: Đơn >= 500k?** | Y | N | Y | N | Y | N | Y | N |
| **A1: Trừ tiền thành công** | X | | | | | | | |
| **A2: Lỗi "Mã không hợp lệ"**| | | | | X | X | X | X |
| **A3: Lỗi "Hết hạn"** | | | X | X | | | | |
| **A4: Lỗi "Chưa đủ ĐK"** | | X | | | | | | |

## 5. Rút gọn Bảng Quyết Định (Reduced Decision Table)
Để tối ưu hóa số lượng Test Case nhưng vẫn đảm bảo độ bao phủ 100%, ta tiến hành rút gọn bảng dựa trên các điều kiện vô nghĩa (Don't care conditions - ký hiệu là `-`):
- Nếu **C1 = N** (Mã không hợp lệ), hệ thống sẽ lập tức văng lỗi A2 mà không cần kiểm tra C2 hay C3. Ta gộp Rule 5, 6, 7, 8 thành một Rule duy nhất.
- Nếu **C1 = Y** nhưng **C2 = N** (Mã hợp lệ nhưng hết hạn), hệ thống sẽ lập tức văng lỗi A3 mà không cần kiểm tra tổng tiền giỏ hàng (C3). Ta gộp Rule 3 và Rule 4 thành một Rule duy nhất.

**Bảng rút gọn:**

| Điều kiện / Hành động | Rule 1 | Rule 2 | Rule 3, 4 | Rule 5, 6, 7, 8 |
| :--- | :---: | :---: | :---: | :---: |
| **C1: Mã hợp lệ?** | Y | Y | Y | N |
| **C2: Còn hạn?** | Y | Y | N | - |
| **C3: Đơn >= 500k?** | Y | N | - | - |
| **A1: Trừ tiền thành công** | X | | | |
| **A2: Lỗi "Mã không hợp lệ"**| | | | X |
| **A3: Lỗi "Hết hạn"** | | | X | |
| **A4: Lỗi "Chưa đủ ĐK"** | | X | | |

*Kết quả:* Số lượng Test Case cần thiết giảm từ 8 xuống còn **4 Test Cases cốt lõi**, giúp tiết kiệm thời gian chạy Automation Test nhưng vẫn phát hiện được mọi lỗi logic tiềm ẩn.
