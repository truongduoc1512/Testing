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

*(Ghi chú bộ giá trị danh định: `receiverName` $nom = 50$, `phone` $nom = 10$, `streetAddress` $nom = 50$, `province` $nom = 30$, `addressCount` $nom = 3$).*

| Biến Định Lượng | Ngoại biên dưới ($min^-$) | Cận dưới ($min$) | Kề dưới ($min^+$) | Danh định ($nom$) | Kề trên ($max^-$) | Cận trên ($max$) | Ngoại biên trên ($max^+$) | Quy tắc & Giới hạn |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :--- |
| **1. Độ dài `receiverName`** | `0` *(rỗng)* | **`1`** | **`2`** | **`50`** | **`99`** | **`100`** | `101` | Miền $[1, 100]$. Rỗng hoặc $> 100$ báo lỗi |
| **2. Độ dài `phone`** | `0` *(rỗng)* | **`1`** | **`2`** | **`10`** | **`19`** | **`20`** | `21` | Miền $[1, 20]$. Rỗng hoặc $> 20$ báo lỗi |
| **3. Độ dài `streetAddress`** | `0` *(rỗng)* | **`1`** | **`2`** | **`50`** | **`254`** | **`255`** | `256` | Miền $[1, 255]$. Rỗng hoặc $> 255$ báo lỗi |
| **4. Độ dài `province`** | `0` *(rỗng)* | **`1`** | **`2`** | **`30`** | **`99`** | **`100`** | `101` | Miền $[1, 100]$. Rỗng hoặc $> 100$ báo lỗi |
| **5. Số lượng `addressCount`**| `0` | **`1`** | **`2`** | **`3`** | **`9`** | **`10`** | `11` *(trần)*| Miền $[1, 10]$ địa chỉ. Vượt trần 10 chặn thêm |

---

### 3.2 Bảng Đầy Đủ Robustness BVA Test Cases ($6n + 1 = 31$ Ca Kiểm Thử)

| Case | Tên nhận (`name`) | SĐT (`phone`) | Địa chỉ phố (`street`) | Tỉnh thành (`province`) | Số lượng sổ (`count`) | Mốc kiểm thử | Kết quả mong đợi (Expected Output) | Tag Biên |
| :-: | :--- | :--- | :--- | :--- | :---: | :---: | :--- | :-: |
| **1** | `50 ký tự` *(nom)* | `10 số` *(nom)* | `50 ký tự` *(nom)* | `30 ký tự` *(nom)* | `3` *(nom)* | **Tất cả ở nom** | **HTTP 201 Created:** Thêm địa chỉ mới thành công | **B1** |
| **2** | `0 ký tự` *(min-)* | `10 số` | `50 ký tự` | `30 ký tự` | `3` | `name = min-` | **HTTP 400 Bad Request:** Tên người nhận không được để trống | **B2** |
| **3** | `1 ký tự` *(min)* | `10 số` | `50 ký tự` | `30 ký tự` | `3` | `name = min` | **HTTP 201 Created:** Tiếp nhận tên 1 ký tự | **B3** |
| **4** | `2 ký tự` *(min+)* | `10 số` | `50 ký tự` | `30 ký tự` | `3` | `name = min+` | **HTTP 201 Created:** Tiếp nhận tên 2 ký tự | **B4** |
| **5** | `99 ký tự` *(max-)* | `10 số` | `50 ký tự` | `30 ký tự` | `3` | `name = max-` | **HTTP 201 Created:** Tiếp nhận tên 99 ký tự | **B5** |
| **6** | `100 ký tự` *(max)* | `10 số` | `50 ký tự` | `30 ký tự` | `3` | `name = max` | **HTTP 201 Created:** Tiếp nhận tên 100 ký tự | **B6** |
| **7** | `101 ký tự` *(max+)*| `10 số` | `50 ký tự` | `30 ký tự` | `3` | `name = max+` | **HTTP 400 Bad Request:** Tên vượt quá 100 ký tự | **B7** |
| **8** | `50 ký tự` | `0 số` *(min-)* | `50 ký tự` | `30 ký tự` | `3` | `phone = min-` | **HTTP 400 Bad Request:** Số điện thoại không được để trống | **B8** |
| **9** | `50 ký tự` | `1 số` *(min)* | `50 ký tự` | `30 ký tự` | `3` | `phone = min` | **HTTP 201 Created:** Tiếp nhận SĐT 1 ký tự | **B9** |
| **10**| `50 ký tự` | `2 số` *(min+)* | `50 ký tự` | `30 ký tự` | `3` | `phone = min+` | **HTTP 201 Created:** Tiếp nhận SĐT 2 ký tự | **B10**|
| **11**| `50 ký tự` | `19 số` *(max-)*| `50 ký tự` | `30 ký tự` | `3` | `phone = max-` | **HTTP 201 Created:** Tiếp nhận SĐT 19 ký tự | **B11**|
| **12**| `50 ký tự` | `20 số` *(max)* | `50 ký tự` | `30 ký tự` | `3` | `phone = max` | **HTTP 201 Created:** Tiếp nhận SĐT 20 ký tự | **B12**|
| **13**| `50 ký tự` | `21 số` *(max+)*| `50 ký tự` | `30 ký tự` | `3` | `phone = max+` | **HTTP 400 Bad Request:** Số điện thoại tối đa 20 ký tự | **B13**|
| **14**| `50 ký tự` | `10 số` | `0 ký tự` *(min-)* | `30 ký tự` | `3` | `street = min-` | **HTTP 400 Bad Request:** Địa chỉ đường không được để trống | **B14**|
| **15**| `50 ký tự` | `10 số` | `1 ký tự` *(min)* | `30 ký tự` | `3` | `street = min` | **HTTP 201 Created:** Tiếp nhận địa chỉ đường 1 ký tự | **B15**|
| **16**| `50 ký tự` | `10 số` | `2 ký tự` *(min+)* | `30 ký tự` | `3` | `street = min+` | **HTTP 201 Created:** Tiếp nhận địa chỉ đường 2 ký tự | **B16**|
| **17**| `50 ký tự` | `10 số` | `254 ký tự` *(max-)*| `30 ký tự` | `3` | `street = max-` | **HTTP 201 Created:** Tiếp nhận địa chỉ đường 254 ký tự | **B17**|
| **18**| `50 ký tự` | `10 số` | `255 ký tự` *(max)*| `30 ký tự` | `3` | `street = max` | **HTTP 201 Created:** Tiếp nhận địa chỉ đường 255 ký tự | **B18**|
| **19**| `50 ký tự` | `10 số` | `256 ký tự` *(max+)*| `30 ký tự` | `3` | `street = max+` | **HTTP 400 Bad Request:** Địa chỉ đường tối đa 255 ký tự | **B19**|
| **20**| `50 ký tự` | `10 số` | `50 ký tự` | `0 ký tự` *(min-)* | `3` | `province = min-` | **HTTP 400 Bad Request:** Tỉnh thành không được để trống | **B20**|
| **21**| `50 ký tự` | `10 số` | `50 ký tự` | `1 ký tự` *(min)* | `3` | `province = min` | **HTTP 201 Created:** Tiếp nhận Tỉnh thành 1 ký tự | **B21**|
| **22**| `50 ký tự` | `10 số` | `50 ký tự` | `2 ký tự` *(min+)* | `3` | `province = min+` | **HTTP 201 Created:** Tiếp nhận Tỉnh thành 2 ký tự | **B22**|
| **23**| `50 ký tự` | `10 số` | `50 ký tự` | `99 ký tự` *(max-)*| `3` | `province = max-` | **HTTP 201 Created:** Tiếp nhận Tỉnh thành 99 ký tự | **B23**|
| **24**| `50 ký tự` | `10 số` | `50 ký tự` | `100 ký tự` *(max)*| `3` | `province = max` | **HTTP 201 Created:** Tiếp nhận Tỉnh thành 100 ký tự | **B24**|
| **25**| `50 ký tự` | `10 số` | `50 ký tự` | `101 ký tự` *(max+)*| `3` | `province = max+`| **HTTP 400 Bad Request:** Tỉnh thành tối đa 100 ký tự | **B25**|
| **26**| `50 ký tự` | `10 số` | `50 ký tự` | `30 ký tự` | `0` *(min-)* | `count = min-` | **HTTP 201 Created:** Tạo địa chỉ đầu tiên (Gán `isDefault=true`) | **B26**|
| **27**| `50 ký tự` | `10 số` | `50 ký tự` | `30 ký tự` | `1` *(min)* | `count = min` | **HTTP 201 Created:** Thêm địa chỉ thứ 2 (Gán phụ `isDefault=false`) | **B27**|
| **28**| `50 ký tự` | `10 số` | `50 ký tự` | `30 ký tự` | `2` *(min+)* | `count = min+` | **HTTP 201 Created:** Thêm địa chỉ thứ 3 thành công | **B28**|
| **29**| `50 ký tự` | `10 số` | `50 ký tự` | `30 ký tự` | `8` *(max-)* | `count = max-` | **HTTP 201 Created:** Thêm địa chỉ thứ 9 thành công | **B29**|
| **30**| `50 ký tự` | `10 số` | `50 ký tự` | `30 ký tự` | `9` *(max)* | `count = max` | **HTTP 201 Created:** Thêm địa chỉ thứ 10 (Chạm trần) | **B30**|
| **31**| `50 ký tự` | `10 số` | `50 ký tự` | `30 ký tự` | `10` *(max+)*| `count = max+` | **HTTP 400 Bad Request:** Đã đạt tối đa 10 địa chỉ lưu | **B31**|

---

## 4. Thiết Kế Bảng Test Cases Chi Tiết Triển Khai (Tối Ưu & Đầy Đủ Bao Phủ)

| STT | Mã Test Case | Tên ca kiểm thử | Dữ liệu kiểm thử (Test Input Data) | Kết quả mong đợi (Expected Output) | Tag được bao phủ |
| :-: | :--- | :--- | :--- | :--- | :---: |
| **1** | **TC_ADR_01** | Thêm địa chỉ đầu tiên thành công với dữ liệu danh định | • `currentUser`: `employee1`<br>• `receiverName`: `"Nguyễn Hoàng Phương"`<br>• `phone`: `"0912345678"`<br>• `streetAddress`: `"Số 123/45 Lê Lợi, P. Bến Nghé, Q1"`<br>• `isDefault`: `false` (gửi lên) | **HTTP 201 Created:** Lưu thành công, hệ thống tự động thăng cấp `isDefault = true` do sổ đang rỗng. | **V1, V2, V3, V4, V5, V6, B1** |
| **2** | **TC_ADR_02** | Thêm địa chỉ thứ 2 làm địa chỉ phụ | • User đang có 1 địa chỉ mặc định<br>• Payload địa chỉ 2 với `isDefault: false` | **HTTP 201 Created:** Địa chỉ 2 lưu thành công với `isDefault = false`, địa chỉ 1 giữ nguyên mặc định. | **V1, V5, B27** |
| **3** | **TC_ADR_03** | Thêm địa chỉ hợp lệ tại các cận dưới ($min$) | • `receiverName`: `"A"` (1 ký tự)<br>• `phone`: `"1"` (1 số)<br>• `streetAddress`: `"1"` (1 ký tự)<br>• `province/district/ward`: `"A"` | **HTTP 201 Created:** Lưu thành công bản ghi tại các cận dưới tối thiểu. | **V2, V3, V4, V5, B3, B9, B15, B21** |
| **4** | **TC_ADR_04** | Thêm địa chỉ hợp lệ tại các cận trên ($max$) | • `receiverName`: Chuỗi 100 ký tự<br>• `phone`: Chuỗi 20 ký tự<br>• `streetAddress`: Chuỗi 255 ký tự<br>• `province/district/ward`: Chuỗi 100 ký tự | **HTTP 201 Created:** Lưu thành công bản ghi tại các cận trên tối đa. | **V2, V3, V4, V5, B6, B12, B18, B24** |
| **5** | **TC_ADR_05** | Đổi địa chỉ phụ thành địa chỉ mặc định mới | • Gọi `PUT /api/v1/users/addresses/{id2}/set-default` | **HTTP 200 OK:** Địa chỉ 2 nhận cờ `isDefault = true`, địa chỉ 1 chuyển về `isDefault = false`. | **V1, V6** |
| **6** | **TC_ADR_06** | Cập nhật thông tin chi tiết địa chỉ | • Gọi `PUT /api/v1/users/addresses/{id}`<br>• `streetAddress`: `"Tòa nhà Landmark 81, 720A Điện Biên Phủ"` | **HTTP 200 OK:** CSDL được cập nhật chính xác, giữ nguyên cờ mặc định hiện tại. | **V1, V5, V6** |
| **7** | **TC_ADR_07** | Xóa địa chỉ mặc định cũ và tự động thăng cấp địa chỉ phụ | • User có 1 địa chỉ mặc định (ID 1) và 1 địa chỉ phụ (ID 2)<br>• Gọi `DELETE /{id1}` | **HTTP 200 OK:** Xóa ID 1 thành công, địa chỉ ID 2 tự động được thăng cấp thành địa chỉ mặc định mới. | **V1, V6** |
| **8** | **TC_ADR_08** | Chặn khách vãng lai thao tác Sổ địa chỉ | • `currentUser`: `null` (Chưa đăng nhập)<br>• Gửi `POST /api/v1/users/addresses` | **HTTP 401 Unauthorized:** Chặn truy cập với thông báo: *"Vui lòng đăng nhập để thêm địa chỉ!"*. | **X1** |
| **9** | **TC_ADR_09** | Báo lỗi khi để trống tên người nhận ($min^-$) | • `receiverName`: `""` (rỗng) | **HTTP 400 Bad Request:** *"Vui lòng điền đầy đủ thông tin địa chỉ giao hàng!"*. | **X2, B2** |
| **10**| **TC_ADR_10** | Báo lỗi khi tên người nhận dài 101 ký tự ($max^+$) | • `receiverName`: Chuỗi 101 ký tự | **HTTP 400 Bad Request:** *"Thông tin địa chỉ vượt quá độ dài cho phép!"*. | **X3, B7** |
| **11**| **TC_ADR_11** | Từ chối tên người nhận chứa ký tự đặc biệt cấm | • `receiverName`: `"Phuong#VIP@123"` | **HTTP 400 Bad Request:** *"Tên người nhận không hợp lệ (không được chứa ký tự đặc biệt @#%^&*...)"*. | **X4** |
| **12**| **TC_ADR_12** | Báo lỗi khi số điện thoại để trống ($min^-$) | • `phone`: `""` (rỗng) | **HTTP 400 Bad Request:** *"Vui lòng điền đầy đủ thông tin địa chỉ giao hàng!"*. | **X5, B8** |
| **13**| **TC_ADR_13** | Báo lỗi khi số điện thoại dài 21 ký tự ($max^+$) | • `phone`: Chuỗi 21 chữ số | **HTTP 400 Bad Request:** *"Thông tin địa chỉ vượt quá độ dài cho phép!"*. | **X6, B13** |
| **14**| **TC_ADR_14** | Từ chối số điện thoại chứa chữ cái hoặc ký tự cấm | • `phone`: `"0912abc789@#"` | **HTTP 400 Bad Request:** *"Số điện thoại không đúng định dạng!"*. | **X7** |
| **15**| **TC_ADR_15** | Báo lỗi khi thiếu 1 trong 3 cấp hành chính | • `province`: `"Hà Nội"`, `district`: `""`, `ward`: `"Dịch Vọng"` | **HTTP 400 Bad Request:** *"Vui lòng điền đầy đủ thông tin địa chỉ giao hàng!"*. | **X8** |
| **16**| **TC_ADR_16** | Báo lỗi khi địa chỉ đường phố để trống ($min^-$) | • `streetAddress`: `""` (rỗng) | **HTTP 400 Bad Request:** *"Vui lòng điền đầy đủ thông tin địa chỉ giao hàng!"*. | **X11, B14** |
| **17**| **TC_ADR_17** | Báo lỗi khi địa chỉ đường phố dài 256 ký tự ($max^+$) | • `streetAddress`: Chuỗi 256 ký tự | **HTTP 400 Bad Request:** *"Thông tin địa chỉ vượt quá độ dài cho phép!"*. | **X12, B19** |
| **18**| **TC_ADR_18** | Từ chối địa chỉ đường phố chứa mã độc XSS Script | • `streetAddress`: `"123 Đường <script>alert(1)</script>"` | **HTTP 400 Bad Request:** *"Địa chỉ đường phố chứa ký tự không hợp lệ!"*. | **X13** |
| **19**| **TC_ADR_19** | Chặn User A sửa hoặc xóa địa chỉ thuộc User B | • User A đăng nhập nhưng gửi `PUT` hoặc `DELETE` ID của User B | **HTTP 403 Forbidden:** *"Không tìm thấy địa chỉ hoặc bạn không có quyền chỉnh sửa!"*. | **X14** |
| **20**| **TC_ADR_20** | Chặn thêm mới khi đã đạt giới hạn 10 địa chỉ ($max^+$) | • Tài khoản đã có sẵn 10 địa chỉ trong sổ<br>• Gửi yêu cầu thêm địa chỉ thứ 11 | **HTTP 400 Bad Request:** *"Bạn đã đạt giới hạn tối đa 10 địa chỉ lưu trữ!"*. | **X15, B31** |

---

## 5. Bảng Quyết Định & Máy Trạng Thái (Decision Table & State Transition)

### 5.1 Bảng Quyết Định Rút Gọn (Collapsed Decision Table - 8 Rules)

| Condition / Action | Rule 1 | Rule 2 | Rule 3 | Rule 4 | Rule 5 | Rule 6 | Rule 7 | Rule 8 |
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

### 5.2 Máy Trạng Thái Chuyển Đổi Sổ Địa Chỉ (State Transition)

```mermaid
stateDiagram-v2
    [*] --> S0_Empty : Khởi tạo tài khoản
    S0_Empty --> S1_SingleDefault : Thêm địa chỉ 1 (Auto isDefault=true)
    S1_SingleDefault --> S2_MultiAddress : Thêm địa chỉ 2 (isDefault=false)
    S2_MultiAddress --> S3_SwappedDefault : Gọi set-default (Đổi cờ mặc định)
    S3_SwappedDefault --> S4_Updated : Cập nhật thông tin chi tiết
    S4_Updated --> S2_MultiAddress : Xóa 1 địa chỉ phụ
    S2_MultiAddress --> S1_SingleDefault : Xóa địa chỉ mặc định (Tự thăng cấp phụ)
    S1_SingleDefault --> S0_Empty : Xóa địa chỉ cuối cùng
```
