# THIẾT KẾ TEST CASE HỘP ĐEN: QUẢN LÝ SỔ ĐỊA CHỈ (ADDRESS BOOK MANAGEMENT)

> **Chức năng:** Quản lý danh sách sổ địa chỉ giao hàng của người dùng (Thêm, Sửa, Xóa, Đặt làm địa chỉ mặc định) qua REST API `/api/v1/users/addresses`.  

---

## 1. Xác Định Biến Đầu Vào & Ràng Buộc Nghiệp Vụ (Input Variables)

| Tên Biến | Ý Nghĩa | Kiểu | Ràng Buộc & Miền Giá Trị Hợp Lệ |
| :--- | :--- | :---: | :--- |
| **`currentUser`** | Tài khoản thực hiện | `Authentication`| Bắt buộc đã đăng nhập (`ROLE_USER` / `CUSTOMER`). Khách vãng lai bị chặn `HTTP 401` |
| **`receiverName`** | Tên người nhận hàng | `String` | Độ dài $[1, 100]$ ký tự, không chứa ký tự cấm (`@#$%^&*<>~{}`) |
| **`phone`** | Số điện thoại nhận hàng | `String` | Độ dài $[1, 20]$ ký tự, đúng định dạng số điện thoại |
| **`province`** | Tỉnh / Thành phố | `String` | Độ dài $[1, 100]$ ký tự, không chứa ký tự đặc biệt |
| **`district`** | Quận / Huyện | `String` | Độ dài $[1, 100]$ ký tự, không chứa ký tự đặc biệt |
| **`ward`** | Phường / Xã | `String` | Độ dài $[1, 100]$ ký tự, không chứa ký tự đặc biệt |
| **`streetAddress`** | Địa chỉ đường phố chi tiết | `String` | Độ dài $[1, 255]$ ký tự, cho phép dấu tiếng Việt, số nhà `/`, `-` |
| **`isDefault`** | Cờ địa chỉ mặc định | `boolean` | `true` hoặc `false`. Nếu là địa chỉ đầu tiên, luôn tự động gán `true` |
| **`addressCount`** | Số lượng địa chỉ đã lưu | `int` | Giới hạn tối đa 10 địa chỉ / tài khoản ($1 \le count \le 10$) |

---

## 2. Bảng Phân Hoạch Tương Đương (Equivalence Partitioning - EP)

| STT | Biến / Điều kiện | Lớp hợp lệ (Valid) | Tag | Lớp không hợp lệ (Invalid) | Tag |
| :-: | :--- | :--- | :---: | :--- | :---: |
| **1** | **Xác thực (`currentUser`)** | User đã đăng nhập hợp lệ | **V1** | Khách vãng lai chưa đăng nhập (`Anonymous`) | **X1** |
| **2** | **Tên người nhận (`receiverName`)**| Chuỗi $[1, 100]$ ký tự chữ hợp lệ | **V2** | • Để trống / null<br>• Vượt quá 100 ký tự ($L > 100$)<br>• Chứa ký tự cấm (`@#$%^&*`) | **X2**<br>**X3**<br>**X4** |
| **3** | **Số điện thoại (`phone`)** | Chuỗi $[1, 20]$ ký tự số hợp lệ | **V3** | • Để trống<br>• Vượt quá 20 ký tự ($L > 20$)<br>• Chứa chữ cái hoặc ký tự đặc biệt | **X5**<br>**X6**<br>**X7** |
| **4** | **Địa danh (`province/district/ward`)**| Chuỗi $[1, 100]$ ký tự hợp lệ | **V4** | • Để trống 1 trong 3 trường<br>• Vượt quá 100 ký tự ($L > 100$)<br>• Chứa ký tự nguy hiểm (`<script>`) | **X8**<br>**X9**<br>**X10** |
| **5** | **Địa chỉ chi tiết (`streetAddress`)**| Chuỗi $[1, 255]$ ký tự có dấu/số nhà | **V5** | • Để trống<br>• Vượt quá 255 ký tự ($L > 255$)<br>• Chứa thẻ HTML / Script injection | **X11**<br>**X12**<br>**X13** |
| **6** | **Quyền sở hữu (`isOwner`)** | Thao tác trên địa chỉ của chính mình | **V6** | Cố tình sửa / xóa địa chỉ thuộc User khác | **X14** |
| **7** | **Số lượng lưu (`addressCount`)** | Số lượng hiện tại $< 10$ địa chỉ | **V7** | Đã đạt trần tối đa 10 địa chỉ lưu | **X15** |

---

## 3. Bảng Phân Tích Giá Trị Biên (Robustness BVA - $6n + 1$)

### 3.1 Bảng 7 mốc giá trị biên Robustness BVA cho 5 biến định lượng

*(Ghi chú bộ giá trị danh định: `receiverName` $nom = \text{"Nguyễn Hoàng Phương"}$ (20 ký tự), `phone` $nom = \text{"0912345678"}$ (10 số), `streetAddress` $nom = \text{"123 Lê Lợi, Bến Nghé"}$ (20 ký tự), `province` $nom = \text{"Hồ Chí Minh"}$ (11 ký tự), `addressCount` $nom = 3$ địa chỉ có sẵn).*

| Biến Định Lượng | Ngoại biên dưới ($min^-$) | Cận dưới ($min$) | Kề dưới ($min^+$) | Danh định ($nom$) | Kề trên ($max^-$) | Cận trên ($max$) | Ngoại biên trên ($max^+$) | Quy tắc & Giới hạn |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :--- |
| **1. Độ dài `receiverName`** | `0` *(rỗng)* | **`1`** | **`2`** | **`20`** | **`99`** | **`100`** | `101` | Miền $[1, 100]$. Rỗng hoặc $> 100$ báo lỗi |
| **2. Độ dài `phone`** | `0` *(rỗng)* | **`1`** | **`2`** | **`10`** | **`19`** | **`20`** | `21` | Miền $[1, 20]$. Rỗng hoặc $> 20$ báo lỗi |
| **3. Độ dài `streetAddress`** | `0` *(rỗng)* | **`1`** | **`2`** | **`20`** | **`254`** | **`255`** | `256` | Miền $[1, 255]$. Rỗng hoặc $> 255$ báo lỗi |
| **4. Độ dài `province`** | `0` *(rỗng)* | **`1`** | **`2`** | **`11`** | **`99`** | **`100`** | `101` | Miền $[1, 100]$. Rỗng hoặc $> 100$ báo lỗi |
| **5. Số lượng `addressCount`**| `0` | **`1`** | **`2`** | **`3`** | **`9`** | **`10`** | `11` *(trần)*| Miền $[1, 10]$ địa chỉ. Vượt trần 10 chặn thêm |

---

### 3.2 Bảng Đầy Đủ Robustness BVA Test Cases ($6n + 1 = 31$ Ca Kiểm Thử)

| Case | Tên nhận (`name`) | SĐT (`phone`) | Địa chỉ đường (`street`) | Tỉnh thành (`province`) | Sổ hiện có (`count`) | Mốc kiểm thử | Kết quả mong đợi (Expected Output) | Tag Biên |
| :-: | :--- | :--- | :--- | :--- | :---: | :---: | :--- | :-: |
| **1** | `"Nguyễn Hoàng Phương"` *(nom)* | `"0912345678"` *(nom)* | `"123 Lê Lợi, Bến Nghé"` *(nom)* | `"Hồ Chí Minh"` *(nom)* | `3` *(nom)* | **Tất cả ở nom** | **HTTP 201 Created:** Thêm địa chỉ mới thành công | **B1** |
| **2** | `""` *(min-)* | `"0912345678"` | `"123 Lê Lợi, Bến Nghé"` | `"Hồ Chí Minh"` | `3` | `name = min-` | **HTTP 400 Bad Request:** Tên người nhận không được để trống | **B2** |
| **3** | `"N"` *(min)* | `"0912345678"` | `"123 Lê Lợi, Bến Nghé"` | `"Hồ Chí Minh"` | `3` | `name = min` | **HTTP 201 Created:** Tiếp nhận tên 1 ký tự | **B3** |
| **4** | `"Ng"` *(min+)* | `"0912345678"` | `"123 Lê Lợi, Bến Nghé"` | `"Hồ Chí Minh"` | `3` | `name = min+` | **HTTP 201 Created:** Tiếp nhận tên 2 ký tự | **B4** |
| **5** | `[99 ký tự]` *(max-)* | `"0912345678"` | `"123 Lê Lợi, Bến Nghé"` | `"Hồ Chí Minh"` | `3` | `name = max-` | **HTTP 201 Created:** Tiếp nhận tên 99 ký tự | **B5** |
| **6** | `[100 ký tự]` *(max)* | `"0912345678"` | `"123 Lê Lợi, Bến Nghé"` | `"Hồ Chí Minh"` | `3` | `name = max` | **HTTP 201 Created:** Tiếp nhận tên 100 ký tự | **B6** |
| **7** | `[101 ký tự]` *(max+)* | `"0912345678"` | `"123 Lê Lợi, Bến Nghé"` | `"Hồ Chí Minh"` | `3` | `name = max+` | **HTTP 400 Bad Request:** Tên vượt quá 100 ký tự | **B7** |
| **8** | `"Nguyễn Hoàng Phương"` | `""` *(min-)* | `"123 Lê Lợi, Bến Nghé"` | `"Hồ Chí Minh"` | `3` | `phone = min-` | **HTTP 400 Bad Request:** Số điện thoại không được để trống | **B8** |
| **9** | `"Nguyễn Hoàng Phương"` | `"1"` *(min)* | `"123 Lê Lợi, Bến Nghé"` | `"Hồ Chí Minh"` | `3` | `phone = min` | **HTTP 201 Created:** Tiếp nhận SĐT 1 ký tự | **B9** |
| **10**| `"Nguyễn Hoàng Phương"` | `"12"` *(min+)* | `"123 Lê Lợi, Bến Nghé"` | `"Hồ Chí Minh"` | `3` | `phone = min+` | **HTTP 201 Created:** Tiếp nhận SĐT 2 ký tự | **B10**|
| **11**| `"Nguyễn Hoàng Phương"` | `[19 số]` *(max-)* | `"123 Lê Lợi, Bến Nghé"` | `"Hồ Chí Minh"` | `3` | `phone = max-` | **HTTP 201 Created:** Tiếp nhận SĐT 19 ký tự | **B11**|
| **12**| `"Nguyễn Hoàng Phương"` | `[20 số]` *(max)* | `"123 Lê Lợi, Bến Nghé"` | `"Hồ Chí Minh"` | `3` | `phone = max` | **HTTP 201 Created:** Tiếp nhận SĐT 20 ký tự | **B12**|
| **13**| `"Nguyễn Hoàng Phương"` | `[21 số]` *(max+)* | `"123 Lê Lợi, Bến Nghé"` | `"Hồ Chí Minh"` | `3` | `phone = max+` | **HTTP 400 Bad Request:** Số điện thoại tối đa 20 ký tự | **B13**|
| **14**| `"Nguyễn Hoàng Phương"` | `"0912345678"` | `""` *(min-)* | `"Hồ Chí Minh"` | `3` | `street = min-` | **HTTP 400 Bad Request:** Địa chỉ đường không được để trống | **B14**|
| **15**| `"Nguyễn Hoàng Phương"` | `"0912345678"` | `"1"` *(min)* | `"Hồ Chí Minh"` | `3` | `street = min` | **HTTP 201 Created:** Tiếp nhận địa chỉ đường 1 ký tự | **B15**|
| **16**| `"Nguyễn Hoàng Phương"` | `"0912345678"` | `"12"` *(min+)* | `"Hồ Chí Minh"` | `3` | `street = min+` | **HTTP 201 Created:** Tiếp nhận địa chỉ đường 2 ký tự | **B16**|
| **17**| `"Nguyễn Hoàng Phương"` | `"0912345678"` | `[254 ký tự]` *(max-)* | `"Hồ Chí Minh"` | `3` | `street = max-` | **HTTP 201 Created:** Tiếp nhận địa chỉ đường 254 ký tự | **B17**|
| **18**| `"Nguyễn Hoàng Phương"` | `"0912345678"` | `[255 ký tự]` *(max)* | `"Hồ Chí Minh"` | `3` | `street = max` | **HTTP 201 Created:** Tiếp nhận địa chỉ đường 255 ký tự | **B18**|
| **19**| `"Nguyễn Hoàng Phương"` | `"0912345678"` | `[256 ký tự]` *(max+)* | `"Hồ Chí Minh"` | `3` | `street = max+` | **HTTP 400 Bad Request:** Địa chỉ đường tối đa 255 ký tự | **B19**|
| **20**| `"Nguyễn Hoàng Phương"` | `"0912345678"` | `"123 Lê Lợi, Bến Nghé"` | `""` *(min-)* | `3` | `province = min-` | **HTTP 400 Bad Request:** Tỉnh thành không được để trống | **B20**|
| **21**| `"Nguyễn Hoàng Phương"` | `"0912345678"` | `"123 Lê Lợi, Bến Nghé"` | `"H"` *(min)* | `3` | `province = min` | **HTTP 201 Created:** Tiếp nhận Tỉnh thành 1 ký tự | **B21**|
| **22**| `"Nguyễn Hoàng Phương"` | `"0912345678"` | `"123 Lê Lợi, Bến Nghé"` | `"HN"` *(min+)* | `3` | `province = min+` | **HTTP 201 Created:** Tiếp nhận Tỉnh thành 2 ký tự | **B22**|
| **23**| `"Nguyễn Hoàng Phương"` | `"0912345678"` | `"123 Lê Lợi, Bến Nghé"` | `[99 ký tự]` *(max-)* | `3` | `province = max-` | **HTTP 201 Created:** Tiếp nhận Tỉnh thành 99 ký tự | **B23**|
| **24**| `"Nguyễn Hoàng Phương"` | `"0912345678"` | `"123 Lê Lợi, Bến Nghé"` | `[100 ký tự]` *(max)* | `3` | `province = max` | **HTTP 201 Created:** Tiếp nhận Tỉnh thành 100 ký tự | **B24**|
| **25**| `"Nguyễn Hoàng Phương"` | `"0912345678"` | `"123 Lê Lợi, Bến Nghé"` | `[101 ký tự]` *(max+)* | `3` | `province = max+` | **HTTP 400 Bad Request:** Tỉnh thành tối đa 100 ký tự | **B25**|
| **26**| `"Nguyễn Hoàng Phương"` | `"0912345678"` | `"123 Lê Lợi, Bến Nghé"` | `"Hồ Chí Minh"` | `0` *(min-)* | `count = min-` | **HTTP 201 Created:** Tạo địa chỉ đầu tiên (Gán `isDefault=true`) | **B26**|
| **27**| `"Nguyễn Hoàng Phương"` | `"0912345678"` | `"123 Lê Lợi, Bến Nghé"` | `"Hồ Chí Minh"` | `1` *(min)* | `count = min` | **HTTP 201 Created:** Thêm địa chỉ thứ 2 (Gán phụ `isDefault=false`) | **B27**|
| **28**| `"Nguyễn Hoàng Phương"` | `"0912345678"` | `"123 Lê Lợi, Bến Nghé"` | `"Hồ Chí Minh"` | `2` *(min+)* | `count = min+` | **HTTP 201 Created:** Thêm địa chỉ thứ 3 thành công | **B28**|
| **29**| `"Nguyễn Hoàng Phương"` | `"0912345678"` | `"123 Lê Lợi, Bến Nghé"` | `"Hồ Chí Minh"` | `8` *(max-)* | `count = max-` | **HTTP 201 Created:** Thêm địa chỉ thứ 9 thành công | **B29**|
| **30**| `"Nguyễn Hoàng Phương"` | `"0912345678"` | `"123 Lê Lợi, Bến Nghé"` | `"Hồ Chí Minh"` | `9` *(max)* | `count = max` | **HTTP 201 Created:** Thêm địa chỉ thứ 10 (Chạm trần) | **B30**|
| **31**| `"Nguyễn Hoàng Phương"` | `"0912345678"` | `"123 Lê Lợi, Bến Nghé"` | `"Hồ Chí Minh"` | `10` *(max+)* | `count = max+` | **HTTP 400 Bad Request:** Đã đạt tối đa 10 địa chỉ lưu | **B31**|

---

## 4. Kỹ Thuật Bảng Quyết Định (Decision Table Testing)

### 4.1 Bảng Quyết Định Rút Gọn (Collapsed Decision Table - 8 Rules)

| Điều kiện / Hành động | Rule 1 | Rule 2 | Rule 3 | Rule 4 | Rule 5 | Rule 6 | Rule 7 | Rule 8 |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| **C1: Đã xác thực đăng nhập (`Authenticated`)?** | **N** | Y | Y | Y | Y | Y | Y | Y |
| **C2: Đầy đủ 6 trường thông tin bắt buộc?** | - | **N** | Y | Y | Y | Y | Y | Y |
| **C3: Độ dài & định dạng 6 trường hợp lệ?** | - | - | **N** | Y | Y | Y | Y | Y |
| **C4: Người dùng là chủ sở hữu bản ghi (`isOwner`)?** | - | - | - | **N** | Y | Y | Y | Y |
| **C5: Thiết lập làm mặc định (`isDefault == true`)?**| - | - | - | - | **N** | **Y** | - | - |
| **C6: Thao tác yêu cầu?** | - | - | - | - | Tạo mới | Tạo mới | Đổi Default | Xóa ID |
| *A1: Trả về HTTP 401 Unauthorized* | **X** | - | - | - | - | - | - | - |
| *A2: Trả về HTTP 400 "Điền đầy đủ thông tin"* | - | **X** | - | - | - | - | - | - |
| *A3: Trả về HTTP 400 "Dữ liệu không hợp lệ"* | - | - | **X** | - | - | - | - | - |
| *A4: Trả về HTTP 403 "Không có quyền thao tác"* | - | - | - | **X** | - | - | - | - |
| *A5: Tạo mới thành công (HTTP 201 Created)* | - | - | - | - | **X** | **X** | - | - |
| *A6: Đổi mặc định thành công (HTTP 200 OK)* | - | - | - | - | - | - | **X** | - |
| *A7: Xóa địa chỉ thành công (HTTP 200 OK)* | - | - | - | - | - | - | - | **X** |
| **Tag Bảng Quyết Định** | **D1** | **D2** | **D3** | **D4** | **D5** | **D6** | **D7** | **D8** |

*Giải thích quy tắc nghiệp vụ:*
- **Rule 1 (`D1`):** Người dùng chưa đăng nhập gọi API Sổ địa chỉ $\to$ Chặn `HTTP 401 Unauthorized`.
- **Rule 2 (`D2`):** Người dùng gửi form thiếu các trường bắt buộc $\to$ Báo lỗi `HTTP 400 Bad Request`.
- **Rule 3 (`D3`):** Dữ liệu vượt độ dài biên, sai định dạng số điện thoại hoặc chứa script $\to$ Báo lỗi `HTTP 400 Bad Request`.
- **Rule 4 (`D4`):** Thao tác sửa hoặc xóa địa chỉ thuộc người dùng khác $\to$ Bị chặn `HTTP 403 Forbidden`.
- **Rule 5 (`D5`):** Tạo mới địa chỉ phụ thông thường (`isDefault = false`) $\to$ Trả về `HTTP 201 Created`.
- **Rule 6 (`D6`):** Tạo mới địa chỉ và thiết lập làm mặc định (hoặc địa chỉ đầu tiên) $\to$ Trả về `HTTP 201 Created`.
- **Rule 7 (`D7`):** Gọi API chuyển địa chỉ phụ thành địa chỉ mặc định mới $\to$ Trả về `HTTP 200 OK`.
- **Rule 8 (`D8`):** Xóa địa chỉ hợp lệ khỏi sổ $\to$ Trả về `HTTP 200 OK`.

---

## 5. Kỹ Thuật Kiểm Thử Chuyển Đổi Trạng Thái (State Transition Testing - STT)

### 5.1 Sơ đồ chuyển đổi trạng thái sổ địa chỉ

```mermaid
stateDiagram-v2
    [*] --> S0_Empty : Khởi tạo tài khoản
    S0_Empty --> S1_SingleDefault : ST1: Thêm địa chỉ 1 (Auto isDefault=true)
    S1_SingleDefault --> S2_MultiAddress : ST2: Thêm địa chỉ 2 (isDefault=false)
    S2_MultiAddress --> S3_SwappedDefault : ST3: Gọi set-default (Đổi cờ mặc định)
    S3_SwappedDefault --> S4_Updated : ST4: Cập nhật thông tin chi tiết
    S4_Updated --> S2_MultiAddress : ST5: Xóa 1 địa chỉ phụ
    S2_MultiAddress --> S1_SingleDefault : ST6: Xóa địa chỉ mặc định (Tự thăng cấp phụ)
    S1_SingleDefault --> S0_Empty : ST7: Xóa địa chỉ cuối cùng
```

### 5.2 Bảng phân tích chi tiết các Ca chuyển đổi trạng thái (State Transition Details)

| Mã Transition | Trạng thái hiện tại | Sự kiện / Hành động | Trạng thái tiếp theo | Kết quả nghiệp vụ | Tag |
| :---: | :--- | :--- | :--- | :--- | :---: |
| **ST_01** | `S0_Empty` (Chưa có địa chỉ) | Thêm địa chỉ đầu tiên | `S1_SingleDefault` | Tự động gán `isDefault = true` | **ST1** |
| **ST_02** | `S1_SingleDefault` (Có 1 địa chỉ mặc định) | Thêm địa chỉ thứ 2 | `S2_MultiAddress` | Lưu địa chỉ phụ (`isDefault = false`) | **ST2** |
| **ST_03** | `S2_MultiAddress` (Có nhiều địa chỉ) | Gọi API `set-default` địa chỉ phụ | `S3_SwappedDefault` | Thăng cấp phụ lên mặc định, hạ mặc định cũ | **ST3** |
| **ST_04** | `S3_SwappedDefault` | Sửa thông tin chi tiết | `S4_Updated` | Cập nhật thông tin thành công | **ST4** |
| **ST_05** | `S4_Updated` | Xóa 1 địa chỉ phụ | `S2_MultiAddress` | Xóa thành công, giữ nguyên mặc định | **ST5** |
| **ST_06** | `S2_MultiAddress` | Xóa địa chỉ đang mặc định | `S1_SingleDefault` | Tự động thăng cấp địa chỉ phụ kế tiếp làm mặc định | **ST6** |
| **ST_07** | `S1_SingleDefault` | Xóa địa chỉ duy nhất còn lại | `S0_Empty` | Sổ địa chỉ trở về rỗng | **ST7** |

---


## 6. Thiết Kế Bảng Test Cases Chi Tiết Triển Khai (Đầy Đủ Không Rút Gọn - 68 Ca Kiểm Thử)

| STT | Mã Test Case | Tên ca kiểm thử | Dữ liệu kiểm thử (Test Input Data) | Kết quả mong đợi (Expected Output) | Tag được bao phủ |
| :-: | :--- | :--- | :--- | :--- | :---: |
| **1** | **TC_ADR_01** | Người dùng đã đăng nhập thực hiện thêm địa chỉ thành công | • `currentUser`: `employee1` (Đã xác thực)<br>• 6 trường hợp lệ danh định | **HTTP 201 Created:** Tạo mới bản ghi địa chỉ thành công. | **V1** |
| **2** | **TC_ADR_02** | Thêm địa chỉ với tên người nhận hợp lệ | • `receiverName`: `"Nguyễn Hoàng Phương"` (20 ký tự chữ) | **HTTP 201 Created:** Họ tên người nhận được tiếp nhận chính xác. | **V2** |
| **3** | **TC_ADR_03** | Thêm địa chỉ với số điện thoại hợp lệ | • `phone`: `"0912345678"` (10 chữ số) | **HTTP 201 Created:** Số điện thoại được lưu vào sổ địa chỉ. | **V3** |
| **4** | **TC_ADR_04** | Thêm địa chỉ với địa danh tỉnh/huyện/xã hợp lệ | • `province`: `"Hồ Chí Minh"`, `district`: `"Quận 1"`, `ward`: `"Bến Nghé"` | **HTTP 201 Created:** Tiếp nhận bộ ba địa danh hành chính chuẩn. | **V4** |
| **5** | **TC_ADR_05** | Thêm địa chỉ với chi tiết đường phố có dấu tiếng Việt và số nhà | • `streetAddress`: `"Số 123/45 Lê Lợi, P. Bến Nghé"` | **HTTP 201 Created:** Lưu trữ nguyên vẹn địa chỉ tiếng Việt và ký tự `/`, `-`. | **V5** |
| **6** | **TC_ADR_06** | Thao tác trên địa chỉ thuộc quyền sở hữu của chính mình | • User A thao tác cập nhật/đổi mặc định/xóa địa chỉ do chính User A tạo | **HTTP 200 OK:** Quyền sở hữu hợp lệ (`isOwner = true`), thực thi thành công. | **V6** |
| **7** | **TC_ADR_07** | Thêm địa chỉ mới khi sổ địa chỉ chưa đạt trần tối đa | • Số địa chỉ hiện có = 3 ($count < 10$ địa chỉ tối đa) | **HTTP 201 Created:** Cho phép thêm địa chỉ mới vào sổ. | **V7** |
| **8** | **TC_ADR_08** | Chặn khách vãng lai (chưa đăng nhập) thao tác sổ địa chỉ | • Request không kèm session xác thực (`Anonymous`) | **HTTP 401 Unauthorized:** Chặn truy cập người dùng chưa đăng nhập. | **X1** |
| **9** | **TC_ADR_09** | Báo lỗi khi để trống tên người nhận | • `receiverName`: `""` (rỗng) | **HTTP 400 Bad Request:** *"Họ và tên người nhận không được để trống!"*. | **X2** |
| **10** | **TC_ADR_10** | Báo lỗi khi tên người nhận vượt quá 100 ký tự | • `receiverName`: Chuỗi dài 101 ký tự | **HTTP 400 Bad Request:** *"Họ và tên người nhận tối đa 100 ký tự!"*. | **X3** |
| **11** | **TC_ADR_11** | Từ chối tên người nhận chứa ký tự đặc biệt cấm | • `receiverName`: `"Phương @#$% VIP"` | **HTTP 400 Bad Request:** *"Tên chứa ký tự không hợp lệ!"*. | **X4** |
| **12** | **TC_ADR_12** | Báo lỗi khi để trống số điện thoại | • `phone`: `""` (rỗng) | **HTTP 400 Bad Request:** *"Số điện thoại không được để trống!"*. | **X5** |
| **13** | **TC_ADR_13** | Báo lỗi khi số điện thoại vượt quá 20 ký tự | • `phone`: Chuỗi 21 số | **HTTP 400 Bad Request:** *"Số điện thoại tối đa 20 ký tự!"*. | **X6** |
| **14** | **TC_ADR_14** | Từ chối số điện thoại chứa chữ cái hoặc ký tự cấm | • `phone`: `"0912abc888@"` | **HTTP 400 Bad Request:** *"Số điện thoại chứa ký tự không hợp lệ!"*. | **X7** |
| **15** | **TC_ADR_15** | Báo lỗi khi để trống 1 trong 3 trường địa danh hành chính | • `province`: `""` (hoặc `district`/`ward` rỗng) | **HTTP 400 Bad Request:** *"Tỉnh thành, quận huyện, phường xã không được để trống!"*. | **X8** |
| **16** | **TC_ADR_16** | Báo lỗi khi địa danh hành chính vượt quá 100 ký tự | • `province`: Chuỗi dài 101 ký tự | **HTTP 400 Bad Request:** *"Tỉnh/Thành phố tối đa 100 ký tự!"*. | **X9** |
| **17** | **TC_ADR_17** | Từ chối địa danh chứa mã script độc hại | • `province`: `"Hà Nội <script>alert(1)</script>"` | **HTTP 400 Bad Request:** *"Địa danh chứa ký tự không hợp lệ!"*. | **X10** |
| **18** | **TC_ADR_18** | Báo lỗi khi để trống địa chỉ đường phố chi tiết | • `streetAddress`: `""` (rỗng) | **HTTP 400 Bad Request:** *"Địa chỉ đường phố không được để trống!"*. | **X11** |
| **19** | **TC_ADR_19** | Báo lỗi khi địa chỉ đường phố vượt quá 255 ký tự | • `streetAddress`: Chuỗi dài 256 ký tự | **HTTP 400 Bad Request:** *"Địa chỉ đường phố tối đa 255 ký tự!"*. | **X12** |
| **20** | **TC_ADR_20** | Từ chối địa chỉ đường phố chứa mã độc XSS Script | • `streetAddress`: `"123 Đường <script>alert(1)</script>"` | **HTTP 400 Bad Request:** *"Địa chỉ đường phố chứa ký tự không hợp lệ!"*. | **X13** |
| **21** | **TC_ADR_21** | Chặn người dùng thao tác trên địa chỉ của tài khoản khác (IDOR) | • User A gửi `PUT` hoặc `DELETE` mã địa chỉ thuộc User B | **HTTP 403 Forbidden:** *"Không tìm thấy địa chỉ hoặc bạn không có quyền chỉnh sửa!"*. | **X14** |
| **22** | **TC_ADR_22** | Chặn thêm mới khi đã đạt giới hạn tối đa 10 địa chỉ | • Sổ địa chỉ đã có 10 bản ghi, gửi thêm địa chỉ thứ 11 | **HTTP 400 Bad Request:** *"Bạn đã đạt giới hạn tối đa 10 địa chỉ lưu trữ!"*. | **X15** |
| **23** | **TC_ADR_23** | Robustness BVA: Tất cả 6 trường ở giá trị danh định chuẩn | • `receiverName` (20 ký tự), `phone` (10 số), `street` (20 ký tự), `province` (11 ký tự), `count` = 3 | **HTTP 201 Created:** Lưu thành công bản ghi tại giá trị danh định. | **B1** |
| **24** | **TC_ADR_24** | Robustness BVA: Độ dài tên người nhận tại ngoại biên dưới min- | • `receiverName`: `""` (0 ký tự, rỗng) | **HTTP 400 Bad Request:** Họ tên người nhận không được để trống. | **B2** |
| **25** | **TC_ADR_25** | Robustness BVA: Độ dài tên người nhận tại cận dưới min | • `receiverName`: `"A"` (1 ký tự) | **HTTP 201 Created:** Tiếp nhận tên 1 ký tự thành công. | **B3** |
| **26** | **TC_ADR_26** | Robustness BVA: Độ dài tên người nhận tại kề cận dưới min+ | • `receiverName`: `"An"` (2 ký tự) | **HTTP 201 Created:** Tiếp nhận tên 2 ký tự thành công. | **B4** |
| **27** | **TC_ADR_27** | Robustness BVA: Độ dài tên người nhận tại kề cận trên max- | • `receiverName`: Chuỗi dài 99 ký tự | **HTTP 201 Created:** Tiếp nhận tên 99 ký tự thành công. | **B5** |
| **28** | **TC_ADR_28** | Robustness BVA: Độ dài tên người nhận tại cận trên max | • `receiverName`: Chuỗi dài 100 ký tự | **HTTP 201 Created:** Tiếp nhận tên 100 ký tự thành công. | **B6** |
| **29** | **TC_ADR_29** | Robustness BVA: Độ dài tên người nhận tại ngoại biên trên max+ | • `receiverName`: Chuỗi dài 101 ký tự | **HTTP 400 Bad Request:** Họ tên người nhận tối đa 100 ký tự. | **B7** |
| **30** | **TC_ADR_30** | Robustness BVA: Độ dài số điện thoại tại ngoại biên dưới min- | • `phone`: `""` (0 số, rỗng) | **HTTP 400 Bad Request:** Số điện thoại không được để trống. | **B8** |
| **31** | **TC_ADR_31** | Robustness BVA: Độ dài số điện thoại tại cận dưới min | • `phone`: `"1"` (1 chữ số) | **HTTP 201 Created:** Tiếp nhận SĐT 1 ký tự thành công. | **B9** |
| **32** | **TC_ADR_32** | Robustness BVA: Độ dài số điện thoại tại kề cận dưới min+ | • `phone`: `"12"` (2 chữ số) | **HTTP 201 Created:** Tiếp nhận SĐT 2 ký tự thành công. | **B10** |
| **33** | **TC_ADR_33** | Robustness BVA: Độ dài số điện thoại tại kề cận trên max- | • `phone`: Chuỗi 19 chữ số | **HTTP 201 Created:** Tiếp nhận SĐT 19 chữ số thành công. | **B11** |
| **34** | **TC_ADR_34** | Robustness BVA: Độ dài số điện thoại tại cận trên max | • `phone`: Chuỗi 20 chữ số | **HTTP 201 Created:** Tiếp nhận SĐT 20 chữ số thành công. | **B12** |
| **35** | **TC_ADR_35** | Robustness BVA: Độ dài số điện thoại tại ngoại biên trên max+ | • `phone`: Chuỗi 21 chữ số | **HTTP 400 Bad Request:** Số điện thoại tối đa 20 ký tự. | **B13** |
| **36** | **TC_ADR_36** | Robustness BVA: Độ dài địa chỉ đường phố tại ngoại biên dưới min- | • `streetAddress`: `""` (0 ký tự, rỗng) | **HTTP 400 Bad Request:** Địa chỉ đường phố không được để trống. | **B14** |
| **37** | **TC_ADR_37** | Robustness BVA: Độ dài địa chỉ đường phố tại cận dưới min | • `streetAddress`: `"1"` (1 ký tự) | **HTTP 201 Created:** Tiếp nhận địa chỉ đường phố 1 ký tự thành công. | **B15** |
| **38** | **TC_ADR_38** | Robustness BVA: Độ dài địa chỉ đường phố tại kề cận dưới min+ | • `streetAddress`: `"12"` (2 ký tự) | **HTTP 201 Created:** Tiếp nhận địa chỉ đường phố 2 ký tự thành công. | **B16** |
| **39** | **TC_ADR_39** | Robustness BVA: Độ dài địa chỉ đường phố tại kề cận trên max- | • `streetAddress`: Chuỗi dài 254 ký tự | **HTTP 201 Created:** Tiếp nhận địa chỉ đường phố 254 ký tự thành công. | **B17** |
| **40** | **TC_ADR_40** | Robustness BVA: Độ dài địa chỉ đường phố tại cận trên max | • `streetAddress`: Chuỗi dài 255 ký tự | **HTTP 201 Created:** Tiếp nhận địa chỉ đường phố 255 ký tự thành công. | **B18** |
| **41** | **TC_ADR_41** | Robustness BVA: Độ dài địa chỉ đường phố tại ngoại biên trên max+ | • `streetAddress`: Chuỗi dài 256 ký tự | **HTTP 400 Bad Request:** Địa chỉ đường phố tối đa 255 ký tự. | **B19** |
| **42** | **TC_ADR_42** | Robustness BVA: Độ dài địa danh tỉnh/thành tại ngoại biên dưới min- | • `province`: `""` (0 ký tự, rỗng) | **HTTP 400 Bad Request:** Tỉnh/Thành phố không được để trống. | **B20** |
| **43** | **TC_ADR_43** | Robustness BVA: Độ dài địa danh tỉnh/thành tại cận dưới min | • `province`: `"A"` (1 ký tự) | **HTTP 201 Created:** Tiếp nhận tỉnh thành 1 ký tự thành công. | **B21** |
| **44** | **TC_ADR_44** | Robustness BVA: Độ dài địa danh tỉnh/thành tại kề cận dưới min+ | • `province`: `"HN"` (2 ký tự) | **HTTP 201 Created:** Tiếp nhận tỉnh thành 2 ký tự thành công. | **B22** |
| **45** | **TC_ADR_45** | Robustness BVA: Độ dài địa danh tỉnh/thành tại kề cận trên max- | • `province`: Chuỗi dài 99 ký tự | **HTTP 201 Created:** Tiếp nhận tỉnh thành 99 ký tự thành công. | **B23** |
| **46** | **TC_ADR_46** | Robustness BVA: Độ dài địa danh tỉnh/thành tại cận trên max | • `province`: Chuỗi dài 100 ký tự | **HTTP 201 Created:** Tiếp nhận tỉnh thành 100 ký tự thành công. | **B24** |
| **47** | **TC_ADR_47** | Robustness BVA: Độ dài địa danh tỉnh/thành tại ngoại biên trên max+ | • `province`: Chuỗi dài 101 ký tự | **HTTP 400 Bad Request:** Tỉnh/Thành phố tối đa 100 ký tự. | **B25** |
| **48** | **TC_ADR_48** | Robustness BVA: Số lượng địa chỉ đã lưu tại cận dưới min | • Sổ địa chỉ rỗng ($count = 0$), thêm địa chỉ đầu tiên | **HTTP 201 Created:** Lưu thành công, tự động thăng cấp `isDefault = true`. | **B26** |
| **49** | **TC_ADR_49** | Robustness BVA: Số lượng địa chỉ đã lưu tại kề cận dưới min+ | • Sổ địa chỉ đang có 1 bản ghi ($count = 1$), thêm địa chỉ thứ 2 | **HTTP 201 Created:** Lưu thành công địa chỉ thứ 2 dạng phụ (`isDefault = false`). | **B27** |
| **50** | **TC_ADR_50** | Robustness BVA: Số lượng địa chỉ đã lưu tại mốc danh định nom | • Sổ địa chỉ đang có 3 bản ghi ($count = 3$), thêm địa chỉ thứ 4 | **HTTP 201 Created:** Lưu thành công địa chỉ thứ 4 dạng phụ. | **B28** |
| **51** | **TC_ADR_51** | Robustness BVA: Số lượng địa chỉ đã lưu tại kề cận trên max- | • Sổ địa chỉ đang có 8 bản ghi ($count = 8$), thêm địa chỉ thứ 9 | **HTTP 201 Created:** Lưu thành công địa chỉ thứ 9 dạng phụ. | **B29** |
| **52** | **TC_ADR_52** | Robustness BVA: Số lượng địa chỉ đã lưu tại cận trên max | • Sổ địa chỉ đang có 9 bản ghi ($count = 9$), thêm địa chỉ thứ 10 | **HTTP 201 Created:** Lưu thành công địa chỉ thứ 10, chạm trần tối đa. | **B30** |
| **53** | **TC_ADR_53** | Robustness BVA: Số lượng địa chỉ đã lưu tại ngoại biên trên max+ | • Sổ địa chỉ đã có đủ 10 bản ghi ($count = 10$), cố tình thêm thứ 11 | **HTTP 400 Bad Request:** Báo lỗi đạt giới hạn tối đa 10 địa chỉ lưu trữ. | **B31** |
| **54** | **TC_ADR_54** | Decision Table: Rule 1 - Khách vãng lai thao tác sổ địa chỉ | • Chưa đăng nhập, gửi request quản lý địa chỉ | **HTTP 401 Unauthorized:** Từ chối truy cập người dùng chưa xác thực. | **D1** |
| **55** | **TC_ADR_55** | Decision Table: Rule 2 - Thêm địa chỉ thiếu trường bắt buộc | • Form thiếu 1 trong các trường bắt buộc | **HTTP 400 Bad Request:** Báo lỗi không được để trống trường bắt buộc. | **D2** |
| **56** | **TC_ADR_56** | Decision Table: Rule 3 - Dữ liệu trường vượt quá độ dài cho phép | • 1 trường có độ dài vượt trần quy định | **HTTP 400 Bad Request:** Báo lỗi độ dài trường vượt quá giới hạn. | **D3** |
| **57** | **TC_ADR_57** | Decision Table: Rule 4 - Người dùng thao tác trên địa chỉ của User khác | • User A cố tình gửi ID địa chỉ của User B | **HTTP 403 Forbidden:** Báo lỗi không có quyền thao tác trên bản ghi. | **D4** |
| **58** | **TC_ADR_58** | Decision Table: Rule 5 - Thêm địa chỉ đầu tiên vào sổ rỗng | • Sổ rỗng ($count = 0$), thêm địa chỉ hợp lệ | **HTTP 201 Created:** Lưu thành công, tự động đặt làm mặc định (`isDefault = true`). | **D5** |
| **59** | **TC_ADR_59** | Decision Table: Rule 6 - Thêm địa chỉ mới vào sổ đã có địa chỉ | • Sổ đã có địa chỉ, thêm địa chỉ mới hợp lệ | **HTTP 201 Created:** Lưu thành công địa chỉ phụ (`isDefault = false`). | **D6** |
| **60** | **TC_ADR_60** | Decision Table: Rule 7 - Đặt địa chỉ phụ làm mặc định | • Gọi API `set-default` trên địa chỉ phụ | **HTTP 200 OK:** Đổi địa chỉ này thành mặc định, hạ mặc định cũ thành phụ. | **D7** |
| **61** | **TC_ADR_61** | Decision Table: Rule 8 - Xóa địa chỉ hợp lệ khỏi sổ địa chỉ | • Gọi API `DELETE` trên địa chỉ hợp lệ | **HTTP 200 OK:** Xóa thành công bản ghi khỏi CSDL. | **D8** |
| **62** | **TC_ADR_62** | State Transition: ST1 - Chuyển S0 sang S1 (Thêm địa chỉ đầu tiên) | • Sổ rỗng (`S0_Empty`) thêm địa chỉ đầu tiên | **Chuyển trạng thái:** Sang `S1_SingleDefault`, tự động gán mặc định. | **ST1** |
| **63** | **TC_ADR_63** | State Transition: ST2 - Chuyển S1 sang S2 (Thêm địa chỉ thứ hai) | • Đang có 1 địa chỉ (`S1_SingleDefault`) thêm địa chỉ thứ 2 | **Chuyển trạng thái:** Sang `S2_MultiAddress`, lưu địa chỉ phụ. | **ST2** |
| **64** | **TC_ADR_64** | State Transition: ST3 - Chuyển S2 sang S3 (Đổi địa chỉ mặc định) | • Đang có nhiều địa chỉ (`S2_MultiAddress`) đổi mặc định | **Chuyển trạng thái:** Sang `S3_SwappedDefault`, hoán đổi cờ mặc định. | **ST3** |
| **65** | **TC_ADR_65** | State Transition: ST4 - Chuyển S3 sang S4 (Cập nhật thông tin chi tiết) | • Đang ở `S3_SwappedDefault` gửi cập nhật thông tin | **Chuyển trạng thái:** Sang `S4_Updated`, cập nhật thành công dữ liệu. | **ST4** |
| **66** | **TC_ADR_66** | State Transition: ST5 - Chuyển S4 sang S2 (Xóa 1 địa chỉ phụ) | • Từ `S4_Updated` gửi xóa 1 địa chỉ phụ | **Chuyển trạng thái:** Quay về `S2_MultiAddress`, giữ nguyên mặc định. | **ST5** |
| **67** | **TC_ADR_67** | State Transition: ST6 - Chuyển S2 sang S1 (Xóa địa chỉ đang mặc định) | • Có 2 địa chỉ, xóa địa chỉ đang mặc định | **Chuyển trạng thái:** Tự động thăng cấp địa chỉ phụ còn lại làm mặc định (`S1_SingleDefault`). | **ST6** |
| **68** | **TC_ADR_68** | State Transition: ST7 - Chuyển S1 sang S0 (Xóa địa chỉ duy nhất còn lại) | • Có đúng 1 địa chỉ, gửi lệnh xóa | **Chuyển trạng thái:** Xóa thành công, sổ trở về rỗng `S0_Empty`. | **ST7** |
