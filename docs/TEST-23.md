# TEST-23 – Checkout UI Automation

## 1. Equivalence Partitioning (Phân hoạch lớp tương đương)

| Field / Trạng thái | Lớp tương đương | Loại | Dữ liệu đại diện | Ý nghĩa |
|---|---|---|---|---|
| Trạng thái giỏ hàng | Giỏ hàng có sản phẩm | Valid | Giỏ hàng $\ge 1$ item | Đủ điều kiện checkout |
| Trạng thái giỏ hàng | Giỏ hàng rỗng | Invalid | Giỏ hàng 0 item | Không được phép checkout |
| Số lượng mua | Số lượng hợp lệ | Valid | $1 \le Qty \le Stock$ | Đặt hàng thành công |
| Số lượng mua | Số lượng vượt tồn kho | Invalid | $Qty > Stock$ | Báo lỗi không đủ hàng |
| Thông tin nhận hàng | Điền đầy đủ | Valid | Tên, SĐT, Địa chỉ | Tạo đơn hàng thành công |
| Thông tin nhận hàng | Bỏ trống thông tin | Invalid | Địa chỉ `""` | Báo lỗi thiếu trường bắt buộc |

## 2. Formal Test Cases – Checkout

| TC ID | Mô tả | Tiền điều kiện | Các bước | Dữ liệu đầu vào | Kết quả mong đợi | Trạng thái |
|---|---|---|---|---|---|---|
| TC-23-01 | Positive Test – Checkout và đặt hàng thành công | Hệ thống đang chạy; tài khoản `employee1` tồn tại; sản phẩm có sẵn trong kho | 1. Đăng nhập.<br>2. Mở Product List.<br>3. Chọn sản phẩm.<br>4. Click Xem Chi Tiết.<br>5. Add to Cart.<br>6. Mở Cart.<br>7. Proceed to Checkout.<br>8. Confirm Order. | Username: `employee1`; Password: `123456`; Product: sản phẩm còn hàng | Đơn hàng được tạo thành công; URL chuyển sang trang order/success hoặc hiển thị thông báo đặt hàng thành công. | Pass |
| TC-23-02 | Negative Test – Chặn checkout khi giỏ hàng rỗng | Hệ thống đang chạy; đã đăng nhập tài khoản | 1. Đăng nhập.<br>2. Mở Cart khi chưa thêm sản phẩm.<br>3. Kiểm tra nút Checkout. | Giỏ hàng rỗng (0 sản phẩm) | Nút Checkout bị vô hiệu hóa (disabled) hoặc hiển thị thông báo "Giỏ hàng đang trống". | Pass |

## 3. Phân loại Test Case

| Loại | Test Case |
|---|---|
| Positive Test | TC-23-01 – Checkout và đặt hàng thành công |
| Negative Test | TC-23-02 – Chặn checkout khi giỏ hàng rỗng |