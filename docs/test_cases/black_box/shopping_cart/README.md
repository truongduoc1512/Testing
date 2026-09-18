# THIẾT KẾ TEST CASE HỘP ĐEN: GIỎ HÀNG (SHOPPING CART CRUD)

> **Chức năng:** Quản lý Giỏ hàng Khách hàng, Thêm mới sản phẩm, Cập nhật số lượng mua, Kiểm soát tồn kho, Xóa sản phẩm và Chuẩn bị Đặt hàng.

---

## 1. Xác Định Biến Đầu Vào & Ràng Buộc Nghiệp Vụ (Input Variables)

| Tên Biến | Ý Nghĩa | Kiểu | Ràng Buộc & Miền Giá Trị Hợp Lệ |
| :--- | :--- | :---: | :--- |
| **`productCode`** | Mã sản phẩm | `String` | Độ dài $[1, 50]$, tồn tại trong CSDL và đang mở bán |
| **`quantity`** | Số lượng mua | `int` | Số nguyên dương $\ge 1$ và không vượt quá tồn kho ($\le Stock$) |
| **`stock`** | Tồn kho hệ thống | `int` | Số lượng tồn kho hiện tại trong CSDL ($\ge 0$) |
| **`cartStatus`** | Trạng thái giỏ hàng | `Enum` | `EMPTY` (Rỗng) hoặc `HAS_ITEMS` (Có sản phẩm) |
| **`currentUserRole`** | Quyền thao tác | `Role` | Yêu cầu `ROLE_CUSTOMER` hoặc `ROLE_ADMIN` đã đăng nhập |

---

## 2. Bảng Phân Hoạch Tương Đương (Equivalence Partitioning - EP)

| STT | Biến / Điều kiện | Lớp hợp lệ (Valid) | Tag | Lớp không hợp lệ (Invalid) | Tag |
| :-: | :--- | :--- | :---: | :--- | :---: |
| **1** | **Mã sản phẩm (`productCode`)** | Mã tồn tại trong CSDL và còn bán | **V1** | • Mã không tồn tại trong CSDL<br>• Để trống hoặc chỉ chứa khoảng trắng | **X1**<br>**X2** |
| **2** | **Số lượng mua (`quantity`)** | Số nguyên dương $1 \le Q \le \text{Stock}$ | **V2** | • Số lượng bằng $0$ ($Q = 0$)<br>• Số lượng âm ($Q < 0$)<br>• Vượt quá số lượng tồn kho ($Q > \text{Stock}$)<br>• Chứa ký tự phi số / số thập phân | **X3**<br>**X4**<br>**X5**<br>**X6** |
| **3** | **Tình trạng dòng hàng** | • Thêm sản phẩm chưa có trong giỏ<br>• Cập nhật số lượng sản phẩm đã có sẵn | **V3**<br>**V4** | Cập nhật / Xóa sản phẩm không nằm trong giỏ | **X7** |
| **4** | **Quyền truy cập (`currentUserRole`)** | Khách hàng đã xác thực session | **V5** | Khách vãng lai chưa đăng nhập (`Guest`) | **X8** |

---

## 3. Bảng Phân Tích Giá Trị Biên (Robustness BVA - $6n + 1$)

### 3.1 Bảng 7 mốc giá trị biên Robustness BVA cho biến `quantity`
*(Ghi chú bộ giá trị danh định: $nom = 5$, Tồn kho hệ thống $Stock = 10$).*

| Biến Định Lượng | Ngoại biên dưới ($min^-$) | Cận dưới ($min$) | Kề dưới ($min^+$) | Danh định ($nom$) | Kề trên ($max^-$) | Cận trên ($max$) | Ngoại biên trên ($max^+$) | Quy tắc & Giới hạn |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :--- |
| **Số lượng `quantity`** | `0` *(Lỗi)* | **`1`** | **`2`** | **`5`** | **`9`** | **`10`** | `11` *(Lỗi)* | Miền $[1, 10]$. $Q \le 0$ báo lỗi; $Q > 10$ chặn vượt kho |

---

### 3.2 Bảng Đầy Đủ Robustness BVA Test Cases ($6n + 1 = 7$ Ca Kiểm Thử)

| Case | Biến kiểm tra | Giá trị `quantity` | Mốc kiểm thử | Ý nghĩa nghiệp vụ | Kết quả mong đợi (Expected Output) | Tag Biên |
| :-: | :--- | :---: | :---: | :--- | :--- | :-: |
| **1** | `quantity` | `5` | **$nom$** | Giá trị trung bình thông thường | **Hợp lệ:** HTTP 200 OK, cập nhật số lượng 5 | **B1** |
| **2** | `quantity` | `0` | **$min^-$** | Mua 0 sản phẩm | **Báo lỗi:** HTTP 400 Bad Request (Số lượng phải $\ge 1$) | **B2** |
| **3** | `quantity` | `1` | **$min$** | Số lượng tối thiểu cho phép | **Hợp lệ:** HTTP 200 OK, giỏ hàng cập nhật số lượng 1 | **B3** |
| **4** | `quantity` | `2` | **$min^+$** | Kề cận tối thiểu | **Hợp lệ:** HTTP 200 OK, giỏ hàng cập nhật số lượng 2 | **B4** |
| **5** | `quantity` | `9` | **$max^-$** | Kề cận tồn kho tối đa | **Hợp lệ:** HTTP 200 OK, giỏ hàng cập nhật số lượng 9 | **B5** |
| **6** | `quantity` | `10` | **$max$** | Chạm trần số lượng tồn kho | **Hợp lệ:** HTTP 200 OK, mua toàn bộ tồn kho | **B6** |
| **7** | `quantity` | `11` | **$max^+$** | Vượt tồn kho hệ thống | **Chặn / Xử lý:** HTTP 400 hoặc tự giới hạn kèm cảnh báo | **B7** |

---

## 4. Kỹ Thuật Bảng Quyết Định (Decision Table Testing - DTT)

### 4.1 Bối cảnh & Điều kiện logic
Quá trình xử lý Giỏ hàng được chi phối bởi 4 điều kiện nghiệp vụ ($C_1 \to C_4$) và 5 hành động kết quả tương ứng ($A_1 \to A_5$):
* **C1 — Session Hợp Lệ:** Khách hàng đã đăng nhập có Session cookie hợp lệ?
* **C2 — Mã Sản Phẩm Tồn Tại:** Mã sản phẩm có trong hệ thống CSDL?
* **C3 — Số Lượng Hợp Lệ:** Số lượng mua $Q \ge 1$?
* **C4 — Còn Hàng Trong Kho:** Số lượng mua $Q \le \text{Stock}$?

### 4.2 Bảng Quyết Định Chuẩn (Decision Table: 5 Rules - Định dạng Y/N và X/-)

| | Condition/Action | R1 | R2 | R3 | R4 | R5 |
| :---: | :--- | :---: | :---: | :---: | :---: | :---: |
| **C1** | Session hợp lệ (Authenticated) | **Y** | **N** | **Y** | **Y** | **Y** |
| **C2** | Mã sản phẩm tồn tại trong CSDL | **Y** | - | **N** | **Y** | **Y** |
| **C3** | Số lượng mua $Q \ge 1$ | **Y** | - | - | **N** | **Y** |
| **C4** | Số lượng mua không vượt tồn kho ($Q \le Stock$) | **Y** | - | - | - | **N** |
| **A1** | Tiếp nhận và cập nhật vào giỏ hàng (HTTP 200 OK) | **X** | - | - | - | - |
| **A2** | Chặn xác thực (HTTP 401 Unauthorized) | - | **X** | - | - | - |
| **A3** | Báo lỗi không tìm thấy sản phẩm (HTTP 404 Not Found) | - | - | **X** | - | - |
| **A4** | Báo lỗi số lượng không hợp lệ (HTTP 400 Bad Request) | - | - | - | **X** | - |
| **A5** | Báo lỗi hoặc chặn vượt quá tồn kho (HTTP 400/200 Limit) | - | - | - | - | **X** |
| **Tag** | **Tag định danh kiểm thử** | **D1** | **D2** | **D3** | **D4** | **D5** |

---

## 5. Kỹ Thuật Chuyển Đổi Trạng Thái (State Transition Testing - STT)

### 5.1 Sơ đồ chuyển đổi trạng thái Giỏ Hàng (Cart FSM)
```mermaid
stateDiagram-v2
    [*] --> EMPTY_CART : Khởi tạo phiên làm việc / Giỏ rỗng
    EMPTY_CART --> HAS_ITEMS : ST1 - Thêm sản phẩm đầu tiên vào giỏ
    HAS_ITEMS --> HAS_ITEMS : ST2 - Cập nhật số lượng (Tăng / Giảm)
    HAS_ITEMS --> HAS_ITEMS : ST3 - Xóa bớt 1 dòng hàng (còn sản phẩm khác)
    HAS_ITEMS --> EMPTY_CART : ST4 - Xóa sản phẩm duy nhất còn lại trong giỏ
    HAS_ITEMS --> CHECKOUT : ST5 - Tiến hành Đặt hàng (Checkout)
```

### 5.2 Bảng Chuyển đổi trạng thái (State Transition Table)

| Trạng thái ban đầu ($S_i$) | Sự kiện kích hoạt (Event) | Điều kiện bảo vệ (Guard Condition) | Trạng thái tiếp theo ($S_{i+1}$) | Kết quả hiển thị & Trạng thái | Tag |
| :--- | :--- | :--- | :---: | :--- | :---: |
| **`EMPTY_CART`** | `POST /api/v1/cart/items` | Mã hợp lệ, $Q \ge 1$ | **`HAS_ITEMS`** | HTTP 200, `totalItems = Q` | **ST1** |
| **`HAS_ITEMS`** | `PUT /api/v1/cart/items` | Mã trong giỏ, $1 \le Q_{mới} \le Stock$ | **`HAS_ITEMS`** | HTTP 200, số lượng cập nhật | **ST2** |
| **`HAS_ITEMS`** | `DELETE /api/v1/cart/items/{code}` | Giỏ còn $\ge 2$ sản phẩm | **`HAS_ITEMS`** | HTTP 200, xóa 1 sản phẩm | **ST3** |
| **`HAS_ITEMS`** | `DELETE /api/v1/cart/items/{code}` | Xóa sản phẩm duy nhất còn lại | **`EMPTY_CART`** | HTTP 200, giỏ hàng rỗng | **ST4** |
| **`HAS_ITEMS`** | `POST /shoppingCartCustomer` | Thông tin giao hàng hợp lệ | **`CHECKOUT`** | HTTP 200/302 chuyển thanh toán | **ST5** |

---

## 6. Thiết Kế Bảng Test Cases Chi Tiết Triển Khai (Tối Ưu & Đầy Đủ Bao Phủ)

| STT | Mã Test Case | Tên ca kiểm thử | Dữ liệu kiểm thử (Test Input Data) | Kết quả mong đợi (Expected Output) | Tag được bao phủ |
| :-: | :--- | :--- | :--- | :--- | :---: |
| **1** | **TC_CART_00** | Khởi tạo phiên xác thực Customer hợp lệ | • `userName`: `"employee1"`<br>• `password`: `"123"` | **Thành công:** Tạo session cookie hợp lệ. | **V5, D1** |
| **2** | **TC_CART_01** | Thêm mới sản phẩm hợp lệ vào giỏ ($min: Q=1$) | • `code`: `"S001"`<br>• `quantity`: `1` | **Thêm thành công:** HTTP 200 OK. | **V1, V2, V3, B3, D1, ST1** |
| **3** | **TC_CART_02** | Cộng dồn số lượng sản phẩm đã có sẵn ($min+: Q=2$) | • `code`: `"S001"`<br>• `quantity`: `2` | **Cộng dồn thành công:** HTTP 200 OK. | **V1, V2, V4, B4, ST2** |
| **4** | **TC_CART_03** | Cập nhật số lượng danh định ($nom: Q=5$) | • `code`: `"S001"`<br>• `quantity`: `5` | **Cập nhật thành công:** HTTP 200 OK. | **V1, V2, B1, ST2** |
| **5** | **TC_CART_04** | Cập nhật số lượng bằng đúng tồn kho ($max: Q=10$) | • `code`: `"S001"`<br>• `quantity`: `10` | **Chạm trần thành công:** HTTP 200 OK. | **V1, V2, B6, ST2** |
| **6** | **TC_CART_05** | Chặn cập nhật số lượng mua bằng 0 ($min-: Q=0$) | • `code`: `"S001"`<br>• `quantity`: `0` | **Báo lỗi:** HTTP 400 Bad Request. | **X3, B2, D4** |
| **7** | **TC_CART_06** | Xử lý số lượng mua vượt tồn kho ($max+: Q=11$) | • `code`: `"S001"`<br>• `quantity`: `11` | **Kiểm soát:** HTTP 400 hoặc chặn vượt kho. | **X5, B7, D5** |
| **8** | **TC_CART_07** | Chặn thêm sản phẩm với mã không tồn tại | • `code`: `"NON_EXISTENT_CODE_999"` | **Báo lỗi:** HTTP 404 Not Found. | **X1, D3** |
| **9** | **TC_CART_08** | Xóa một dòng hàng khỏi giỏ hàng | • Gửi request `DELETE /api/v1/cart/items/S001` | **Xóa thành công:** HTTP 200 OK. | **ST3, ST4** |
| **10**| **TC_CART_09** | Xem giỏ hàng sau khi đã xóa sạch (`EMPTY_CART`) | • Gửi request `GET /api/v1/cart` | **Thành công:** HTTP 200 OK (Giỏ hàng rỗng). | **ST4** |

---

## 7. Ma Trận Truy Xoá & Đánh Giá Độ Bao Phủ (Traceability Matrix)

| Kỹ thuật kiểm thử | Số lượng Tag | Danh sách Tags | Test Cases phụ trách kiểm thử |
| :--- | :---: | :--- | :--- |
| **EP (Lớp hợp lệ)** | 5 | $V_1 \to V_5$ | TC_CART_00, TC_CART_01, TC_CART_02, TC_CART_03, TC_CART_04 |
| **EP (Lớp không hợp lệ)** | 8 | $X_1 \to X_8$ | TC_CART_05, TC_CART_06, TC_CART_07 |
| **Robustness BVA** | 7 | $B_1 \to B_7$ | TC_CART_01, TC_CART_02, TC_CART_03, TC_CART_04, TC_CART_05, TC_CART_06 |
| **Decision Table** | 5 | $D_1 \to D_5$ | TC_CART_00, TC_CART_01, TC_CART_05, TC_CART_06, TC_CART_07 |
| **State Transition** | 5 | $ST_1 \to ST_5$ | TC_CART_01, TC_CART_02, TC_CART_08, TC_CART_09 |

---

## 8. Hướng Dẫn Thực Thi Với Postman & Newman

### Cách 1: Chạy trực tiếp trên ứng dụng Postman (Gom 1 file JSON duy nhất)
1. Khởi động ứng dụng **Postman**.
2. Bấm phím tắt **`Ctrl + O`** hoặc chọn **Import** $\to$ Chọn file [`Shopping_Cart_Postman_Collection.json`](file:///d:/New%20folder/Testing/docs/test_cases/black_box/shopping_cart/Shopping_Cart_Postman_Collection.json).
3. Nhấn vào Collection $\to$ Chọn **Run** $\to$ Bấm **Run Shopping_Cart_Postman_Collection** để chạy toàn bộ kịch bản tự động.

### Cách 2: Chạy tự động qua Newman Command Line
```powershell
npx newman run docs/test_cases/black_box/shopping_cart/Shopping_Cart_Postman_Collection.json --insecure
```
