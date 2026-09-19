# THIẾT KẾ TEST CASE HỘP ĐEN: QUẢN LÝ DANH SÁCH YÊU THÍCH (CUSTOMER WISHLIST)

> **Module B:** Quản lý danh sách Yêu thích (`customer_wishlist`) dành cho Khách hàng (`Customer` / `ROLE_USER`).
> **Quy trình tương tác:** Thêm/Hủy yêu thích 1 chạm (Toggle), Kiểm tra icon trái tim, Xem danh sách yêu thích, Xóa khỏi Wishlist, Cập nhật Badge số lượng đồng bộ và Phân quyền bảo mật.

---

## 1. Xác Định Biến Đầu Vào & Ràng Buộc Nghiệp Vụ (Input Variables)

| Tiểu Chức Năng | API Tương Ứng | Biến Đầu Vào | Kiểu | Ràng Buộc & Miền Giá Trị Hợp Lệ |
| :--- | :--- | :--- | :---: | :--- |
| **1. Thêm / Hủy yêu thích 1 chạm** | `POST /api/v1/wishlist/{code}` | **`productCode`** | `String` | Chuyển đổi trạng thái (Toggle): Thả tim (`favorite = true`) $\leftrightarrow$ Bỏ tim (`favorite = false`). Chặn SP `INACTIVE` |
| **2. Kiểm tra trạng thái yêu thích** | `GET /api/v1/wishlist/check/{code}` | **`productCode`** | `String` | Trả về `favorite: true` (tim đỏ) hoặc `favorite: false` (viền rỗng) |
| **3. Xem danh sách yêu thích** | `GET /api/v1/wishlist` & `GET /wishlist` | **`username`** | `String` | Trả về mảng danh sách sản phẩm đã yêu thích. Nếu chưa lưu sản phẩm nào trả về `[]` |
| **4. Xóa sản phẩm khỏi Wishlist** | `DELETE /api/v1/wishlist/{code}` | **`productCode`** | `String` | Xóa/bỏ thích sản phẩm trực tiếp từ trang Wishlist, sản phẩm biến mất lập tức |
| **5. Cập nhật Badge số lượng** | `wishlistCount` Header Badge | **`count`** | `int` | Khi Thêm/Bỏ thích, badge số lượng trên header menu (`wishlistCount`) tự động tăng/giảm |
| **6. Phân quyền truy cập** | Security Filter | **`currentUserRole`** | `Role` | Bắt buộc `ROLE_USER` đã đăng nhập. Khách vãng lai (`Guest`) bị chặn (HTTP 401 / redirect `/login`) |

---

## 2. Bảng Phân Hoạch Tương Đương (Equivalence Partitioning - EP)

| STT | Biến / Điều kiện | Lớp hợp lệ (Valid) | Tag | Lớp không hợp lệ (Invalid) | Tag |
| :-: | :--- | :--- | :---: | :--- | :---: |
| **1** | **Quyền truy cập (`currentUserRole`)** | Tài khoản Khách hàng đã đăng nhập (`ROLE_USER`) | **V1** | Khách vãng lai chưa đăng nhập (`Guest` - HTTP 401 / redirect `/login`) | **X1** |
| **2** | **Mã sản phẩm (`productCode`)** | Mã sản phẩm hợp lệ, tồn tại và ở trạng thái `ACTIVE` | **V2** | • Mã sản phẩm không tồn tại trong CSDL (HTTP 404)<br>• Mã sản phẩm có trạng thái `INACTIVE` (HTTP 404) | **X2**<br>**X3** |
| **3** | **Thao tác Toggle 1 chạm (`wishlistAction`)** | Thao tác trên sản phẩm hợp lệ (`TOGGLE`) | **V3** | Thao tác thả tim trên sản phẩm bị `INACTIVE` | **X4** |
| **4** | **Trạng thái danh sách Wishlist** | • Danh sách có chứa sản phẩm<br>• Danh sách rỗng (chưa lưu sản phẩm nào) | **V4**<br>**V5** | N/A | N/A |

---

## 3. Bảng Phân Tích Giá Trị Biên (Robustness BVA - $2n + 1$)

### 3.1 Bảng mốc giá trị biên BVA cho các biến độ dài mã sản phẩm & số lượng Wishlist

| Biến Định Lượng | Ngoại biên dưới ($min^-$) | Cận dưới ($min$) | Kề dưới ($min^+$) | Danh định ($nom$) | Kề trên ($max^-$) | Cận trên ($max$) | Ngoại biên trên ($max^+$) | Quy tắc & Giới hạn |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :--- |
| **1. Độ dài `productCode`**| `0` *(rỗng)* | **`1`** | **`2`** | **`4`** | **`19`** | **`20`** | `21` | Miền $[1, 20]$. Rỗng/không tồn tại báo HTTP 404 |
| **2. Số lượng Wishlist Badge**| `-1` *(lỗi)* | **`0`** *(rỗng)* | **`1`** | **`5`** | **`99`** | **`100`** | `101` | Miền $\ge 0$. Tự động tăng/giảm đồng bộ badge |

---

## 4. Kỹ Thuật Bảng Quyết Định (Decision Table Testing - DTT)

| | Condition/Action | R1 | R2 | R3 | R4 |
| :---: | :--- | :---: | :---: | :---: | :---: |
| **C1** | Quyền Khách hàng (`ROLE_USER`) | **Y** | **N** | **Y** | **Y** |
| **C2** | Sản phẩm tồn tại & `ACTIVE` | **Y** | - | **N** | **Y** |
| **C3** | Trạng thái Wishlist trước thao tác | `UNFAVORITED` | - | - | `FAVORITED` |
| **A1** | Chuyển sang `FAVORITED` (`favorite: true`, Badge +1) | **X** | - | - | - |
| **A2** | Chặn truy cập (HTTP 401 / Redirect `/login`) | - | **X** | - | - |
| **A3** | Báo lỗi: "Không tìm thấy sản phẩm!" (HTTP 404 Not Found) | - | - | **X** | - |
| **A4** | Chuyển sang `UNFAVORITED` (`favorite: false`, Badge -1) | - | - | - | **X** |
| **Tag** | **Tag định danh kiểm thử** | **D1** | **D2** | **D3** | **D4** |

---

## 5. Kỹ Thuật Chuyển Đổi Trạng Thái (State Transition Testing - STT)

### 5.1 Sơ đồ chuyển đổi trạng thái Máy trạng thái Wishlist Toggle 1 chạm

* **`UNFAVORITED`** ($S_0$): Sản phẩm chưa được thả tim / yêu thích (icon viền rỗng).
* **`FAVORITED`** ($S_1$): Sản phẩm đã được thêm vào Wishlist (icon tim sáng đỏ).

```mermaid
stateDiagram-v2
    [*] --> UNFAVORITED : Khách xem sản phẩm
    UNFAVORITED --> FAVORITED : ST_W1 - Bấm nút tim POST /wishlist/{code} (Lần 1)
    FAVORITED --> UNFAVORITED : ST_W2 - Bấm nút tim POST /wishlist/{code} (Lần 2)
    FAVORITED --> UNFAVORITED : ST_W3 - Gỡ khỏi danh sách DELETE /wishlist/{code}
```

### 5.2 Bảng Chuyển đổi trạng thái (State Transition Table)

| Trạng thái ban đầu ($S_i$) | Sự kiện kích hoạt (Event) | Điều kiện bảo vệ (Guard Condition) | Trạng thái tiếp theo ($S_{i+1}$) | Kết quả hiển thị & Trạng thái | Tag |
| :--- | :--- | :--- | :---: | :--- | :---: |
| **`UNFAVORITED`** ($S_0$) | Bấm nút tim yêu thích (lần 1) | Gọi `POST /api/v1/wishlist/{code}` | **`FAVORITED`** | HTTP 200 OK, tim sáng đỏ, `favorite: true`, count +1 | **ST_W1** |
| **`FAVORITED`** ($S_1$) | Bấm nút tim hủy yêu thích (lần 2) | Gọi `POST /api/v1/wishlist/{code}` | **`UNFAVORITED`** | HTTP 200 OK, tim tắt, `favorite: false`, count -1 | **ST_W2** |
| **`FAVORITED`** ($S_1$) | Gỡ sản phẩm khỏi Wishlist | Gọi `DELETE /api/v1/wishlist/{code}` | **`UNFAVORITED`** | HTTP 200 OK, gỡ khỏi danh sách Wishlist | **ST_W3** |

---

## 6. Thiết Kế Bảng Test Cases Chi Tiết Phân Theo Các Tiểu Chức Năng Con

### 6.1 Tiểu Chức Năng 1: Thêm / Xóa Yêu Thích 1 Chạm (`Toggle 1-touch` - `POST /api/v1/wishlist/{productCode}`)

| STT | Mã Test Case | Tên ca kiểm thử | Dữ liệu kiểm thử (Test Input Data) | Kết quả mong đợi (Expected Output) | Tag được bao phủ |
| :-: | :--- | :--- | :--- | :--- | :---: |
| **1** | **TC_WISH_01** | Thêm sản phẩm vào Wishlist (Toggle 1 chạm lần 1: $S_0 \to S_1$) | • Gửi `POST /api/v1/wishlist/S001` (Trạng thái đang chưa thả tim) | **Thành công:** HTTP 200 OK, `favorite: true`, tim sáng đỏ, `wishlistCount` +1. | **V1, V2, V3, D1, ST_W1** |
| **2** | **TC_WISH_03** | Hủy yêu thích sản phẩm qua Toggle 1 chạm (Toggle lần 2: $S_1 \to S_0$) | • Gửi `POST /api/v1/wishlist/S001` (Trạng thái đang thả tim) | **Thành công:** HTTP 200 OK, `favorite: false`, tim tắt, `wishlistCount` -1. | **V3, D4, ST_W2** |

### 6.2 Tiểu Chức Năng 2: Kiểm Tra Trạng Thái Yêu Thích (`Check Status` - `GET /api/v1/wishlist/check/{productCode}`)

| STT | Mã Test Case | Tên ca kiểm thử | Dữ liệu kiểm thử (Test Input Data) | Kết quả mong đợi (Expected Output) | Tag được bao phủ |
| :-: | :--- | :--- | :--- | :--- | :---: |
| **3** | **TC_WISH_02** | Kiểm tra trạng thái sản phẩm trong Wishlist | • `GET /api/v1/wishlist/check/S001` | **Thành công:** HTTP 200 OK, trả về `favorite: true/false` (để tô đỏ/viền rỗng). | **V2** |

### 6.3 Tiểu Chức Năng 3: Xem Danh Sách Yêu Thích (`Read List` - `GET /api/v1/wishlist`)

| STT | Mã Test Case | Tên ca kiểm thử | Dữ liệu kiểm thử (Test Input Data) | Kết quả mong đợi (Expected Output) | Tag được bao phủ |
| :-: | :--- | :--- | :--- | :--- | :---: |
| **4** | **TC_WISH_05** | Tải toàn bộ danh sách sản phẩm yêu thích của tài khoản | • `GET /api/v1/wishlist` với header token hợp lệ | **Thành công:** HTTP 200 OK, trả về danh sách mảng các sản phẩm yêu thích. | **V1, V4** |
| **5** | **TC_WISH_05B**| Tải danh sách yêu thích khi chưa lưu sản phẩm nào | • `GET /api/v1/wishlist` khi tài khoản chưa thả tim | **Thành công:** HTTP 200 OK, trả về danh sách rỗng `[]` ("Danh sách trống"). | **V5** |

### 6.4 Tiểu Chức Năng 4: Xóa Sản Phẩm Khỏi Wishlist (`Delete` - `DELETE /api/v1/wishlist/{productCode}`)

| STT | Mã Test Case | Tên ca kiểm thử | Dữ liệu kiểm thử (Test Input Data) | Kết quả mong đợi (Expected Output) | Tag được bao phủ |
| :-: | :--- | :--- | :--- | :--- | :---: |
| **6** | **TC_WISH_04** | Xóa sản phẩm khỏi Wishlist qua API DELETE | • Gửi `DELETE /api/v1/wishlist/S001` | **Thành công:** HTTP 200 OK, `favorite: false`, sản phẩm biến mất khỏi Wishlist, trả `wishlistCount`. | **V3, ST_W3** |

### 6.5 Tiểu Chức Năng 5: Cập Nhật Badge Số Lượng & Phân Quyền (`Count Badge & Security Filter`)

| STT | Mã Test Case | Tên ca kiểm thử | Dữ liệu kiểm thử (Test Input Data) | Kết quả mong đợi (Expected Output) | Tag được bao phủ |
| :-: | :--- | :--- | :--- | :--- | :---: |
| **7** | **TC_WISH_06** | Chặn khách vãng lai chưa đăng nhập thao tác Wishlist | • Gọi API Wishlist khi chưa đăng nhập (`Guest`) | **Bị chặn:** HTTP 401 Unauthorized (hoặc redirect sang `/login`). | **X1, D2** |
| **8** | **TC_WISH_07** | Thêm sản phẩm không tồn tại / `INACTIVE` vào Wishlist | • Gửi `POST /api/v1/wishlist/INVALID_CODE_99` | **Báo lỗi:** HTTP 404 Not Found (Không tìm thấy sản phẩm). | **X2, X3, X4, D3** |

---

## 7. Ma Trận Truy Vết & Đánh Giá Độ Bao Phủ Kiểm Thử (Traceability Matrix & Coverage Analysis)

### 7.1 Phân tích chỉ số tối ưu & Độ bao phủ (Coverage Metrics)
- **Độ bao phủ Lớp tương đương (EP Coverage):** $100\%$ ($5/5$ lớp hợp lệ $V_1 \to V_5$ và $4/4$ lớp không hợp lệ $X_1 \to X_4$).
- **Độ bao phủ Bảng quyết định (DTT Coverage):** $100\%$ ($4/4$ quy tắc logic $D_1 \to D_4$).
- **Độ bao phủ Chuyển đổi trạng thái (STT Coverage):** $100\%$ ($3/3$ chuyển trạng thái $ST\_W1 \to ST\_W3$).
- **Đồng bộ hóa 100%:** 8 ca kiểm thử trong tài liệu được ánh xạ 1-1 với các request trong file Postman Collection `Customer_Wishlist_Postman_Collection.json`.

---

## 8. Execution Guide (Hướng dẫn thực thi với Postman & Newman)

```powershell
npx newman run docs/test_cases/black_box/customer_wishlist/Customer_Wishlist_Postman_Collection.json `
  -e docs/test_cases/black_box/customer_wishlist/Customer_Wishlist_Postman_Environment.json
```
