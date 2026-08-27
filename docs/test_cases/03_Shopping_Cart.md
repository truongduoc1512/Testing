# Bảng Test Case: Chức năng 3 - Giỏ hàng (Shopping Cart)
**Người thực hiện:** Cả nhóm

## 1. Thông tin Kỹ thuật & Thực thi
- **Kỹ thuật Thiết kế (Test Design):**
  - **Phân hoạch lớp tương đương (EP):** Phân chia trạng thái sản phẩm và số lượng mua hợp lệ.
  - **Phân tích giá trị biên (BVA):** Đảm bảo số lượng mua (Q) nằm trong khoảng hợp lệ $[1, Stock]$.
  - **Bảng quyết định (Decision Table):** Sử dụng để quyết định hành động Thêm mới / Cộng dồn / Báo lỗi dựa trên trạng thái sản phẩm và tồn kho.
- **Kỹ thuật Thực thi (Test Execution):** Kiểm thử Tự động Giao diện (UI Automation Testing) sử dụng Selenium WebDriver.
- **File Code Thực thi (Automation Script):** `src/test/java/com/example/demo/ui/CheckoutUiTest.java`

---

## 2. Phân tích Kỹ thuật Thiết kế (Test Design Analysis)

### 2.1 Bảng Phân hoạch lớp tương đương (EP)

| STT | Trường hợp của điều kiện đầu vào | Lớp tương đương Hợp lệ | Lớp tương đương Không hợp lệ |
| :---: | :--- | :--- | :--- |
| 1 | Số lượng nhập mua ($Q$) | $Q \ge 1$ (nguyên dương) | $Q \le 0$ hoặc không phải số nguyên |
| 2 | So sánh với Tồn kho ($Stock$) | $Q \le Stock$ | $Q > Stock$ |
| 3 | Trạng thái Sản phẩm | Đang mở bán (ACTIVE) | Hết hàng / Ngừng bán (INACTIVE) |

### 2.2 Bảng Phân tích giá trị biên (BVA)
Áp dụng cho trường nhập số lượng ($Q$) dựa trên số lượng tồn kho (giả sử $Stock = 10$):

| Biến đầu vào | Ràng buộc logic | Các giá trị biên kiểm thử (BVA Points) |
| :--- | :--- | :--- |
| **Q (Số lượng)** | $1 \le Q \le Stock$ | 0 (Lỗi min-), 1 (min), 5 (nom), 10 (max), 11 (Lỗi max+) |

### 2.3 Bảng Quyết định tổng hợp (Collapsed Decision Table)

| Condition/Action | R1 | R2 | R3 | R4 | R5 |
| :--- | :---: | :---: | :---: | :---: | :---: |
| **C1: Sản phẩm đang mở bán?** | N | Y | Y | Y | Y |
| **C2: Số lượng $Q \ge 1$?** | - | N | Y | Y | Y |
| **C3: Số lượng $Q \le Stock$?**| - | - | N | Y | Y |
| **C4: Đã có trong giỏ chưa?** | - | - | - | N | Y |
| **A1: Nút vô hiệu hóa / Báo hết hàng** | X | - | - | - | - |
| **A2: Xác nhận xóa khỏi giỏ hàng** | - | X | - | - | - |
| **A3: Báo lỗi vượt quá số lượng kho** | - | - | X | - | - |
| **A4: Thêm mới sản phẩm ➔ Badge tăng 1** | - | - | - | X | - |
| **A5: Cộng dồn số lượng ➔ Badge giữ nguyên**| - | - | - | - | X |
| **Test Case Tương ứng** | TC_CART_005 | TC_CART_003 | TC_CART_004 | TC_CART_001 | TC_CART_002 |

---

## 3. Bảng Test Case Chi Tiết

### 2. Danh sách Test Cases

| Mã kiểm thử | Kỹ thuật áp dụng | Tiêu đề | Điều kiện tiên quyết | Các bước kiểm tra | Dữ liệu kiểm thử | Kết quả dự kiến | Kết quả thực tế | Trạng thái |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **TC_CART_001** | Bảng quyết định (Rule 4) / EP | Thêm mới sản phẩm hợp lệ vào giỏ | Ở trang chi tiết sản phẩm `S001` | 1. Nhập số lượng 1. 2. Bấm "Thêm vào giỏ" | Sản phẩm: `S001`. Số lượng: `1` | Thêm mới thành công, badge giỏ hàng tăng 1 | Thêm vào giỏ thành công | Pass |
| **TC_CART_002** | Bảng quyết định (Rule 5) / EP | Cập nhật tăng số lượng đã có trong giỏ | Đã có 1 sản phẩm `S001` trong giỏ | 1. Thêm tiếp 2 sản phẩm `S001`. 2. Bấm thêm vào giỏ | Thêm: `2` | Giỏ hàng tự cộng dồn tổng số lượng thành 3 | Số lượng cộng dồn thành 3 | Pass |
| **TC_CART_003** | Bảng quyết định (Rule 2) / EP / BVA | Chặn nhập số lượng mua bằng 0 | Đã có sản phẩm trong giỏ | 1. Nhập số lượng 0. 2. Bấm cập nhật | Số lượng: `0` | Hệ thống cảnh báo hoặc xóa sản phẩm khỏi giỏ | Hiển thị xác nhận xóa | Pass |
| **TC_CART_004** | Bảng quyết định (Rule 3) / EP / BVA | Chặn thêm số lượng vượt tồn kho | Sản phẩm `S001` tồn kho 10 | 1. Nhập số lượng 11. 2. Bấm thêm vào giỏ | Số lượng: `11` (Stock=10) | Báo lỗi `Số lượng mua vượt quá số lượng tồn kho` | Hiển thị cảnh báo vượt tồn kho | Pass |
| **TC_CART_005** | Bảng quyết định (Rule 1) / EP | Chặn thêm sản phẩm đã hết hàng | Sản phẩm `S005` hết hàng | 1. Bấm vào nút "Thêm vào giỏ" | Sản phẩm: `S005` | Nút bị vô hiệu hóa hoặc báo sản phẩm không khả dụng | Nút bị vô hiệu hóa | Pass |

 

