# Module 03: Shopping Cart (Giỏ hàng)

### 1. Biến điều kiện (Conditions)
- **C1:** Sản phẩm có tồn tại và đang mở bán không? (Yes/No)
- **C2:** Số lượng nhập mua Q > 0 (nguyên dương) không? (Yes/No)
- **C3:** Số lượng nhập mua Q <= Số lượng tồn kho (Stock) không? (Yes/No)
- **C4:** Sản phẩm này đã có sẵn trong giỏ hàng từ trước chưa? (Yes/No)

### 2. Danh sách Test Cases

| Mã kiểm thử | Kỹ thuật áp dụng | Tiêu đề | Điều kiện tiên quyết | Các bước kiểm tra | Dữ liệu kiểm thử | Kết quả dự kiến | Kết quả thực tế | Trạng thái |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **TC_CART_001** | Bảng quyết định (Rule 4) | Thêm mới sản phẩm hợp lệ vào giỏ | Ở trang chi tiết sản phẩm `S001` | 1. Nhập số lượng 1<br>2. Bấm "Thêm vào giỏ" | Sản phẩm: `S001`<br>Số lượng: `1` | Thêm mới thành công, badge giỏ hàng tăng 1 | Thêm vào giỏ thành công | Pass |
| **TC_CART_002** | Bảng quyết định (Rule 5) | Cập nhật tăng số lượng đã có trong giỏ | Đã có 1 sản phẩm `S001` trong giỏ | 1. Thêm tiếp 2 sản phẩm `S001`<br>2. Bấm thêm vào giỏ | Thêm: `2` | Giỏ hàng tự cộng dồn tổng số lượng thành 3 | Số lượng cộng dồn thành 3 | Pass |
| **TC_CART_003** | Bảng quyết định (Rule 2) | Chặn nhập số lượng mua bằng 0 | Đã có sản phẩm trong giỏ | 1. Nhập số lượng 0<br>2. Bấm cập nhật | Số lượng: `0` | Hệ thống cảnh báo hoặc xóa sản phẩm khỏi giỏ | Hiển thị xác nhận xóa | Pass |
| **TC_CART_004** | Bảng quyết định (Rule 3) | Chặn thêm số lượng vượt tồn kho | Sản phẩm `S001` tồn kho 10 | 1. Nhập số lượng 11<br>2. Bấm thêm vào giỏ | Số lượng: `11` (Stock=10) | Báo lỗi `Số lượng mua vượt quá số lượng tồn kho` | Hiển thị cảnh báo vượt tồn kho | Pass |
| **TC_CART_005** | Bảng quyết định (Rule 1) | Chặn thêm sản phẩm đã hết hàng | Sản phẩm `S005` hết hàng | 1. Bấm vào nút "Thêm vào giỏ" | Sản phẩm: `S005` | Nút bị vô hiệu hóa hoặc báo sản phẩm không khả dụng | Nút bị vô hiệu hóa | Pass |

### 3. Bảng quyết định (Decision Table)

| Quy tắc (Rules) | C1: Sản phẩm mở bán? | C2: Số lượng Q > 0? | C3: Q <= Tồn kho? | C4: Đã có trong giỏ? | Kết quả / Hành động hệ thống | Số kịch bản gộp | Test Case tương ứng |
| :--- | :---: | :---: | :---: | :---: | :--- | :---: | :---: |
| **Rule 1 (Hết hàng)** | No | - | - | - | Báo lỗi: Sản phẩm ngừng bán / Nút bị vô hiệu hóa | 8 | `TC_CART_005` |
| **Rule 2 (Số lượng <= 0)** | Yes | No | - | - | Báo lỗi số lượng không hợp lệ / Xác nhận xóa | 4 | `TC_CART_003` |
| **Rule 3 (Vượt tồn kho)** | Yes | Yes | No | - | Báo lỗi: Vượt quá số lượng tồn kho hiện có | 2 | `TC_CART_004` |
| **Rule 4 (Thêm mới)** | Yes | Yes | Yes | No | Thêm mới sản phẩm vào giỏ -> Badge giỏ tăng 1 | 1 | `TC_CART_001` |
| **Rule 5 (Cộng dồn)** | Yes | Yes | Yes | Yes | Cộng dồn số lượng vào sản phẩm đã có trong giỏ | 1 | `TC_CART_002` |

- **Automation:** `src/test/java/com/example/demo/ui/CheckoutUiTest.java`
