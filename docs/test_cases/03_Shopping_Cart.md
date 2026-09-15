# Bảng Test Case: Chức năng 3 - Giỏ hàng (Shopping Cart)
**Người thực hiện:** Lĩnh

## 1. Thông tin Kỹ thuật & Thực thi
- **Kỹ thuật Thiết kế (Test Design):**
  - **Phân hoạch lớp tương đương (EP):** Phân chia trạng thái sản phẩm và số lượng mua hợp lệ.
  - **Phân tích giá trị biên (BVA):** Đảm bảo số lượng mua (Q) nằm trong khoảng hợp lệ $[1, Stock]$.
  - **Bảng quyết định (Decision Table):** Sử dụng để quyết định hành động Thêm mới / Cộng dồn / Báo lỗi dựa trên trạng thái sản phẩm và tồn kho.
- **Kỹ thuật Thực thi (Test Execution):** Kiểm thử Tự động Giao diện (UI Automation Testing) sử dụng Selenium WebDriver.
- **File Code Thực thi (Automation Script):** `src/test/java/com/example/demo/ui/CheckoutUiTest.java`

---

## 2. Phân tích Kỹ thuật Thiết kế (Test Design Analysis)


### 2.1 Bảng Phân hoạch lớp tương đương (Equivalence Partitioning - EP)

| Biến đầu vào / Điều kiện | Lớp tương đương Hợp lệ | Tag | Lớp tương đương Không hợp lệ | Tag |
| :--- | :--- | :---: | :--- | :---: |
| **Số lượng nhập mua ($Q$)** | $Q \ge 1$ (Số nguyên dương) | **V1** | $Q \le 0$ (Bằng 0 hoặc số âm)<br>$Q$ không phải số nguyên / chữ | **X1**<br>**X2** |
| **So sánh với Tồn kho ($Stock$)** | $1 \le Q \le Stock$ (Đủ hàng) | **V2** | $Q > Stock$ (Vượt quá số lượng tồn kho) | **X3** |
| **Trạng thái sản phẩm** | `ACTIVE` (Còn hàng, mở bán) | **V3** | Hết hàng (`Stock = 0`)<br>`INACTIVE` (Ngừng kinh doanh) | **X4**<br>**X5** |
| **Trạng thái giỏ hàng** | Sản phẩm đã có trong giỏ (Cộng dồn) | **V4** | Chưa có trong giỏ (Thêm mới) | **V5** |


### 2.2 Bảng Phân tích giá trị biên (Standard Boundary Value Analysis - BVA)

Giả định kịch bản kiểm thử với sản phẩm có tồn kho chuẩn $Stock = 10$:

| Biến đầu vào | Miền hợp lệ | min | min+ | nominal | max- | max | Tag biên |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :--- |
| **Số lượng đặt mua ($Q$)** | $[1, 10]$ | 1 | 2 | 5 | 9 | 10 | **B1, B2, B3, B4, B5** |

*Ghi chú mở rộng (Robustness BVA):*
- Giá trị ngoài biên dưới: $Q = 0$ (Tag **B0**) -> Hệ thống cảnh báo hoặc xóa sản phẩm khỏi giỏ.
- Giá trị ngoài biên trên: $Q = 11$ ($Stock + 1$, Tag **B6**) -> Báo lỗi vượt quá tồn kho.
- Giá trị danh định `nominal = 5` được chọn làm giá trị đại diện nằm giữa khoảng $[1, 10]$.


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

| Mã kiểm thử | Kỹ thuật áp dụng | Tiêu đề | Điều kiện tiên quyết | Các bước kiểm tra | Dữ liệu kiểm thử | Kết quả dự kiến | Tag được bao phủ | Kết quả thực tế | Trạng thái |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :---: | :--- | :---: |
| **TC_CART_001** | Bảng quyết định (Rule 4) / EP | Thêm mới sản phẩm hợp lệ vào giỏ | Ở trang chi tiết sản phẩm `S001` | 1. Nhập số lượng 1. 2. Bấm "Thêm vào giỏ" | Sản phẩm: `S001`. Số lượng: `1` | Thêm mới thành công, badge giỏ hàng tăng 1 | **V1, V2, V3, V5, B1** | Thêm vào giỏ thành công | Pass |
| **TC_CART_002** | Bảng quyết định (Rule 5) / EP | Cập nhật tăng số lượng đã có trong giỏ | Đã có 1 sản phẩm `S001` trong giỏ | 1. Thêm tiếp 2 sản phẩm `S001`. 2. Bấm thêm vào giỏ | Thêm: `2` | Giỏ hàng tự cộng dồn tổng số lượng thành 3 | **V1, V2, V3, V4, B2** | Số lượng cộng dồn thành 3 | Pass |
| **TC_CART_003** | Bảng quyết định (Rule 2) / EP / BVA | Chặn nhập số lượng mua bằng 0 | Đã có sản phẩm trong giỏ | 1. Nhập số lượng 0. 2. Bấm cập nhật | Số lượng: `0` | Hệ thống cảnh báo hoặc xóa sản phẩm khỏi giỏ | **X1, B0** | Hiển thị xác nhận xóa | Pass |
| **TC_CART_004** | Bảng quyết định (Rule 3) / EP / BVA | Chặn thêm số lượng vượt tồn kho | Sản phẩm `S001` tồn kho 10 | 1. Nhập số lượng 11. 2. Bấm thêm vào giỏ | Số lượng: `11` (Stock=10) | Báo lỗi `Số lượng mua vượt quá số lượng tồn kho` | **X3, B6** | Hiển thị cảnh báo vượt tồn kho | Pass |
| **TC_CART_005** | Bảng quyết định (Rule 1) / EP | Chặn thêm sản phẩm đã hết hàng | Sản phẩm `S005` hết hàng | 1. Bấm vào nút "Thêm vào giỏ" | Sản phẩm: `S005` | Nút bị vô hiệu hóa hoặc báo sản phẩm không khả dụng | **X4** | Nút bị vô hiệu hóa | Pass |


---

## 4. Bảng Đối Chiếu & Ý Nghĩa Nhãn Tag (Tag Traceability Legend)

| Nhóm Tag | Mã Tag | Ý nghĩa nghiệp vụ | Trạng thái |
| :---: | :---: | :--- | :---: |
| **Valid EP** | **V1** | Số lượng nhập mua là số nguyên dương (Q >= 1) | Hợp lệ |
| | **V2** | Số lượng mua không vượt quá số lượng tồn kho (Q <= Stock) | Hợp lệ |
| | **V3** | Sản phẩm đang ở trạng thái kinh doanh mở bán (ACTIVE) | Hợp lệ |
| | **V4** | Sản phẩm đã tồn tại trong giỏ hàng (Kích hoạt cộng dồn) | Hợp lệ |
| | **V5** | Sản phẩm chưa có trong giỏ hàng (Thêm mới dòng hàng) | Hợp lệ |
| **Invalid EP**| **X1** | Số lượng mua nhỏ hơn hoặc bằng 0 (Q <= 0) | Không hợp lệ |
| | **X2** | Số lượng mua không phải số nguyên (chữ hoặc ký tự đặc biệt) | Không hợp lệ |
| | **X3** | Số lượng mua vượt quá tồn kho (Q > Stock) | Không hợp lệ |
| | **X4** | Sản phẩm đã hết hàng trong kho (Stock = 0) | Không hợp lệ |
| | **X5** | Sản phẩm đã bị vô hiệu hóa / ngừng kinh doanh (INACTIVE) | Không hợp lệ |
| **Boundary** | **B1 - B5**| Các điểm biên số lượng mua: min (1), min+ (2), nom (5), max- (9), max (10) | Hợp lệ |
| | **B0** | Điểm ngoài biên dưới: Q = 0 | Không hợp lệ |
| | **B6** | Điểm ngoài biên trên: Q = 11 (Vượt quá Stock = 10) | Không hợp lệ |
