# THIẾT KẾ TEST CASE HỘP ĐEN: QUẢN LÝ SỔ ĐỊA CHỈ (ADDRESS BOOK MANAGEMENT)

> **Chức năng:** Quản lý danh sách sổ địa chỉ giao hàng của người dùng (Thêm, Sửa, Xóa, Đặt làm địa chỉ mặc định) qua REST API `/api/v1/users/addresses`.  
> **Người thực hiện:** Nguyễn Hoàng Phương (MSSV: `080205010954` / `NguyenHoangPhuong275`)

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

## 6. Thiết Kế Bảng Test Cases Chi Tiết Triển Khai (Tối Ưu & Đầy Đủ Bao Phủ)

| STT | Mã Test Case | Tên ca kiểm thử | Dữ liệu kiểm thử (Test Input Data) | Kết quả mong đợi (Expected Output) | Tag được bao phủ |
| :-: | :--- | :--- | :--- | :--- | :---: |
| **1** | **TC_ADR_01** | Thêm địa chỉ đầu tiên thành công với dữ liệu danh định | • `currentUser`: `employee1`<br>• `receiverName`: `"Nguyễn Hoàng Phương"`<br>• `phone`: `"0912345678"`<br>• `streetAddress`: `"Số 123/45 Lê Lợi, P. Bến Nghé, Q1"`<br>• `isDefault`: `false` (gửi lên) | **HTTP 201 Created:** Lưu thành công, hệ thống tự động thăng cấp `isDefault = true` do sổ đang rỗng. | **V1, V2, V3, V4, V5, V6, V7, B1, D5, D6, ST1** |
| **2** | **TC_ADR_02** | Thêm địa chỉ thứ 2 làm địa chỉ phụ | • User đang có 1 địa chỉ mặc định<br>• Payload địa chỉ 2 với `isDefault: false` | **HTTP 201 Created:** Địa chỉ 2 lưu thành công với `isDefault = false`, địa chỉ 1 giữ nguyên mặc định. | **V1, V5, V7, B27, D5, D6, ST2** |
| **3** | **TC_ADR_03** | Thêm địa chỉ hợp lệ tại các cận dưới ($min$) | • `receiverName`: `"A"` (1 ký tự)<br>• `phone`: `"1"` (1 số)<br>• `streetAddress`: `"1"` (1 ký tự)<br>• `province/district/ward`: `"A"` | **HTTP 201 Created:** Lưu thành công bản ghi tại các cận dưới tối thiểu. | **V2, V3, V4, V5, B3, B9, B15, B21, D5, D6** |
| **4** | **TC_ADR_04** | Thêm địa chỉ hợp lệ tại các cận trên ($max$) | • `receiverName`: Chuỗi 100 ký tự<br>• `phone`: Chuỗi 20 ký tự<br>• `streetAddress`: Chuỗi 255 ký tự<br>• `province/district/ward`: Chuỗi 100 ký tự | **HTTP 201 Created:** Lưu thành công bản ghi tại các cận trên tối đa. | **V2, V3, V4, V5, B6, B12, B18, B24, D5, D6** |
| **5** | **TC_ADR_05** | Đổi địa chỉ phụ thành địa chỉ mặc định mới | • Gọi `PUT /api/v1/users/addresses/{id2}/set-default` | **HTTP 200 OK:** Địa chỉ 2 nhận cờ `isDefault = true`, địa chỉ 1 chuyển về `isDefault = false`. | **V1, V6, D7, ST3** |
| **6** | **TC_ADR_06** | Cập nhật thông tin chi tiết địa chỉ | • Gọi `PUT /api/v1/users/addresses/{id}`<br>• `streetAddress`: `"Tòa nhà Landmark 81, 720A Điện Biên Phủ"` | **HTTP 200 OK:** CSDL được cập nhật chính xác, giữ nguyên cờ mặc định hiện tại. | **V1, V5, V6, ST4** |
| **7** | **TC_ADR_07** | Xóa địa chỉ mặc định cũ và tự động thăng cấp địa chỉ phụ | • User có 1 địa chỉ mặc định (ID 1) và 1 địa chỉ phụ (ID 2)<br>• Gọi `DELETE /{id1}` | **HTTP 200 OK:** Xóa ID 1 thành công, địa chỉ ID 2 tự động được thăng cấp thành địa chỉ mặc định mới. | **V1, V6, D8, ST6** |
| **8** | **TC_ADR_08** | Chặn khách vãng lai thao tác Sổ địa chỉ | • `currentUser`: `null` (Chưa đăng nhập)<br>• Gửi `POST /api/v1/users/addresses` | **HTTP 401 Unauthorized:** Chặn truy cập với thông báo: *"Vui lòng đăng nhập để thêm địa chỉ!"*. | **X1, D1** |
| **9** | **TC_ADR_09** | Báo lỗi khi để trống tên người nhận ($min^-$) | • `receiverName`: `""` (rỗng) | **HTTP 400 Bad Request:** *"Vui lòng điền đầy đủ thông tin địa chỉ giao hàng!"*. | **X2, B2, D2** |
| **10**| **TC_ADR_10** | Báo lỗi khi tên người nhận dài 101 ký tự ($max^+$) | • `receiverName`: Chuỗi 101 ký tự | **HTTP 400 Bad Request:** *"Thông tin địa chỉ vượt quá độ dài cho phép!"*. | **X3, B7, D3** |
| **11**| **TC_ADR_11** | Từ chối tên người nhận chứa ký tự đặc biệt cấm | • `receiverName`: `"Phuong#VIP@123"` | **HTTP 400 Bad Request:** *"Tên người nhận không hợp lệ (không được chứa ký tự đặc biệt @#%^&*...)"*. | **X4, D3** |
| **12**| **TC_ADR_12** | Báo lỗi khi số điện thoại để trống ($min^-$) | • `phone`: `""` (rỗng) | **HTTP 400 Bad Request:** *"Vui lòng điền đầy đủ thông tin địa chỉ giao hàng!"*. | **X5, B8, D2** |
| **13**| **TC_ADR_13** | Báo lỗi khi số điện thoại dài 21 ký tự ($max^+$) | • `phone`: Chuỗi 21 chữ số | **HTTP 400 Bad Request:** *"Thông tin địa chỉ vượt quá độ dài cho phép!"*. | **X6, B13, D3** |
| **14**| **TC_ADR_14** | Từ chối số điện thoại chứa chữ cái hoặc ký tự cấm | • `phone`: `"0912abc789@#"` | **HTTP 400 Bad Request:** *"Số điện thoại không đúng định dạng!"*. | **X7, D3** |
| **15**| **TC_ADR_15** | Báo lỗi khi thiếu 1 trong 3 cấp hành chính | • `province`: `"Hà Nội"`, `district`: `""`, `ward`: `"Dịch Vọng"` | **HTTP 400 Bad Request:** *"Vui lòng điền đầy đủ thông tin địa chỉ giao hàng!"*. | **X8, D2** |
| **16**| **TC_ADR_16** | Báo lỗi khi địa chỉ đường phố để trống ($min^-$) | • `streetAddress`: `""` (rỗng) | **HTTP 400 Bad Request:** *"Vui lòng điền đầy đủ thông tin địa chỉ giao hàng!"*. | **X11, B14, D2** |
| **17**| **TC_ADR_17** | Báo lỗi khi địa chỉ đường phố dài 256 ký tự ($max^+$) | • `streetAddress`: Chuỗi 256 ký tự | **HTTP 400 Bad Request:** *"Thông tin địa chỉ vượt quá độ dài cho phép!"*. | **X12, B19, D3** |
| **18**| **TC_ADR_18** | Từ chối địa chỉ đường phố chứa mã độc XSS Script | • `streetAddress`: `"123 Đường <script>alert(1)</script>"` | **HTTP 400 Bad Request:** *"Địa chỉ đường phố chứa ký tự không hợp lệ!"*. | **X13, D3** |
| **19**| **TC_ADR_19** | Chặn User A sửa hoặc xóa địa chỉ thuộc User B | • User A đăng nhập nhưng gửi `PUT` hoặc `DELETE` ID của User B | **HTTP 403 Forbidden:** *"Không tìm thấy địa chỉ hoặc bạn không có quyền chỉnh sửa!"*. | **X14, D4** |
| **20**| **TC_ADR_20** | Chặn thêm mới khi đã đạt giới hạn 10 địa chỉ ($max^+$) | • Tài khoản đã có sẵn 10 địa chỉ trong sổ<br>• Gửi yêu cầu thêm địa chỉ thứ 11 | **HTTP 400 Bad Request:** *"Bạn đã đạt giới hạn tối đa 10 địa chỉ lưu trữ!"*. | **X15, B31** |

---

## 7. Ma Trận Truy Vết & Độ Bao Phủ Kiểm Thử (Traceability Matrix)

| Nhóm Kỹ Thuật | Tổng Số Thẻ (Tags) | Danh Sách Thẻ Định Danh | Tỷ Lệ Bao Phủ |
| :--- | :---: | :--- | :---: |
| **Phân hoạch tương đương (EP)** | **22 Tags** | Hợp lệ: `V1` $\to$ `V7` (7 tags)<br>Không hợp lệ: `X1` $\to$ `X15` (15 tags) | **100% (22/22)** |
| **Phân tích giá trị biên (BVA)** | **31 Tags** | Robustness BVA: `B1` $\to$ `B31` (31 tags) | **100% (31/31)** |
| **Bảng quyết định (Decision Table)**| **8 Rules** | `D1, D2, D3, D4, D5, D6, D7, D8` | **100% (8/8)** |
| **Chuyển đổi trạng thái (State Transition)**| **7 Steps** | `ST1, ST2, ST3, ST4, ST5, ST6, ST7` | **100% (7/7)** |

---

## 8. Hướng Dẫn Thực Thi Với Postman & Newman (Execution Guide)

### 8.1 Chạy trực tiếp trên ứng dụng Postman (Desktop App)
1. Khởi động ứng dụng **Postman**.
2. Chọn **Import** $\to$ Chọn 2 tệp kịch bản kiểm thử:
   - Collection: [`User_Address_Book_Postman_Collection.json`](file:///d:/LapTrinhAI/Testing/docs/test_cases/black_box/user_address_book/User_Address_Book_Postman_Collection.json)
   - Environment: [`User_Address_Book_Postman_Environment.json`](file:///d:/LapTrinhAI/Testing/docs/test_cases/black_box/user_address_book/User_Address_Book_Postman_Environment.json)
3. Chọn môi trường `Shoeshop User Address Book Env`.
4. Nhấn vào Collection $\to$ Chọn **Run Collection** để thực thi tự động toàn bộ 20 test cases (`TC_ADR_01` $\to$ `TC_ADR_20`).

### 8.2 Chạy tự động qua dòng lệnh (CLI) bằng Newman
Thực thi lệnh sau tại thư mục gốc dự án:
```powershell
npx --yes newman run docs/test_cases/black_box/user_address_book/User_Address_Book_Postman_Collection.json `
  -e docs/test_cases/black_box/user_address_book/User_Address_Book_Postman_Environment.json `
  -r 'cli,htmlextra' `
  --reporter-htmlextra-export target/newman-user-address-report.html `
  --insecure
```

