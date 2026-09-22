# THIẾT KẾ TEST CASE HỘP ĐEN: ĐĂNG TẢI SẢN PHẨM (ADMIN PRODUCT CREATION)

> **Chức năng:** Quản lý và Đăng tải sản phẩm mới dành cho Quản trị viên (`ROLE_ADMIN`).

---

## 1. Xác Định Biến Đầu Vào & Ràng Buộc Nghiệp Vụ (Input Variables)

| Tên Biến | Ý Nghĩa | Kiểu | Ràng Buộc & Miền Giá Trị Hợp Lệ |
| :--- | :--- | :---: | :--- |
| **`currentUserRole`** | Quyền đăng nhập | `Role` | Bắt buộc `ROLE_ADMIN` (`ROLE_USER` / Guest bị từ chối) |
| **`code`** | Mã định danh sản phẩm (SKU) | `String` | Độ dài $[1, 20]$, không rỗng, duy nhất trong CSDL |
| **`name`** | Tên hiển thị sản phẩm | `String` | Độ dài $[1, 255]$, không rỗng |
| **`price`** | Đơn giá niêm yết (VNĐ) | `double` | Số thực $> 0$ (thực tế $[1.000, 100.000.000]$ VNĐ) |
| **`stockQuantity`** | Số lượng hàng nhập kho | `int` | Số nguyên không âm $\ge 0$ (mặc định: `100`, thực tế $[0, 10.000]$) |
| **`discountPercent`** | Giảm giá khuyến mãi (%) | `int` | Số nguyên trong đoạn $[0, 100]\%$ (mặc định: `0%`) |
| **`fileData`** | Tệp hình ảnh đại diện | `MultipartFile` | Tùy chọn (`null`); tệp mới: định dạng ảnh, $\le 10\text{ MB}$, AI duyệt |

---

## 2. Bảng Phân Hoạch Tương Đương (Equivalence Partitioning - EP)

| STT | Biến / Điều kiện | Lớp hợp lệ (Valid) | Tag | Lớp không hợp lệ (Invalid) | Tag |
| :-: | :--- | :--- | :---: | :--- | :---: |
| **1** | **Quyền truy cập (`currentUserRole`)** | Tài khoản Quản trị viên (`ROLE_ADMIN`) | **V1** | • Khách vãng lai chưa đăng nhập (`Guest`)<br>• Tài khoản người dùng thường (`ROLE_USER`) | **X1**<br>**X2** |
| **2** | **Mã sản phẩm (`code`)** | Chuỗi độ dài $[1, 20]$, chưa có trong CSDL | **V2** | • Để trống hoặc rỗng (`null` / `""`)<br>• Vượt quá 20 ký tự ($L > 20$)<br>• Trùng mã SKU đã có trong CSDL | **X3**<br>**X4**<br>**X5** |
| **3** | **Tên sản phẩm (`name`)** | Chuỗi độ dài $[1, 255]$ không rỗng | **V3** | • Để trống hoặc toàn khoảng trắng<br>• Vượt quá 255 ký tự ($L > 255$) | **X6**<br>**X7** |
| **4** | **Giá bán (`price`)** | Số thực hữu hạn $> 0$ ($price \ge 1.000$ VNĐ) | **V4** | • Giá bán bằng $0$<br>• Giá bán âm ($< 0$) | **X8**<br>**X9** |
| **5** | **Tồn kho (`stockQuantity`)** | Số nguyên không âm ($\ge 0$) | **V5** | Số lượng tồn kho âm ($< 0$) | **X10** |
| **6** | **Giảm giá (`discountPercent`)** | Số nguyên trong đoạn $[0, 100]\%$ | **V6** | • Chiết khấu âm ($< 0$)<br>• Chiết khấu vượt trần 100% ($> 100\%$) | **X11**<br>**X12** |
| **7** | **Hình ảnh (`fileData`)** | • Không tải ảnh (`null` / để trống)<br>• Tệp ảnh hợp lệ, AI duyệt (`approved = true`)<br>• Tệp ảnh hợp lệ khi AI offline (Fallback mode) | **V7**<br>**V8**<br>**V9** | • Bị AI Quality Gate từ chối (`approved = false`)<br>• Lỗi đọc byte (`IOException`) | **X13**<br><br>**X14** |

---

## 3. Bảng Phân Tích Giá Trị Biên (Robustness BVA - $6n + 1$)

### 3.1 Bảng 7 mốc giá trị biên Robustness BVA cho 5 biến định lượng

*(Ghi chú bộ giá trị danh định chuẩn: `code` $nom = 10\text{ ký tự}$ (`"NK-PEGASUS"`), `name` $nom = 24\text{ ký tự}$ (`"Giày Nike Air Pegasus 40"`), `price` $nom = 1.500.000\text{ VNĐ}$, `stockQuantity` $nom = 50$, `discountPercent` $nom = 10\%$).*

| Biến Định Lượng | Ngoại biên dưới ($min^-$) | Cận dưới ($min$) | Kề dưới ($min^+$) | Danh định ($nom$) | Kề trên ($max^-$) | Cận trên ($max$) | Ngoại biên trên ($max^+$) | Quy tắc & Giới hạn |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :--- |
| **1. Độ dài `code`** | `0` *(rỗng)* | **`1`** | **`2`** | **`10`** | **`19`** | **`20`** | `21` | Miền $[1, 20]$. Rỗng hoặc $> 20$ báo lỗi |
| **2. Độ dài `name`** | `0` *(rỗng)* | **`1`** | **`2`** | **`24`** | **`254`** | **`255`** | `256` | Miền $[1, 255]$. Rỗng hoặc $> 255$ báo lỗi |
| **3. Đơn giá `price`** (VNĐ) | `0` *(hoặc âm)* | **`1.000`** | **`10.000`** | **`1.500.000`** | **`99.990.000`** | **`100.000.000`** | `100.000.001` | Miền $> 0$. $\le 0$ báo lỗi: *"Giá sản phẩm phải lớn hơn 0!"* |
| **4. Tồn kho `stockQuantity`** | `-1` *(âm)* | **`0`** | **`1`** | **`50`** | **`9.999`** | **`10.000`** | `10.001` | Miền $\ge 0$. Âm báo lỗi: *"Số lượng tồn kho không được âm!"* |
| **5. Giảm giá `discountPercent`** (%)| `-1` *(âm)* | **`0`** | **`1`** | **`10`** | **`99`** | **`100`** | `101` | Miền $[0, 100]\%$. $< 0$ hoặc $> 100$ báo lỗi |

---

### 3.2 Bảng Đầy Đủ Robustness BVA Test Cases ($6n + 1 = 31$ Ca Kiểm Thử)

| Case | Mã SP (`code`) | Tên sản phẩm (`name`) | Đơn giá (`price`) | Tồn kho (`stockQuantity`) | Giảm giá (`discountPercent`) | Mốc kiểm thử | Kết quả mong đợi (Expected Output) | Tag Biên |
| :-: | :--- | :--- | :---: | :---: | :---: | :--- | :--- | :-: |
| **1** | `"NK-PEGASUS"` *(nom)* | `"Giày Nike Air Pegasus 40"` *(nom)* | `1.500.000` *(nom)* | `50` *(nom)* | `10` *(nom)* | **Tất cả ở nom** | **Hợp lệ:** Lưu sản phẩm thành công | **B1** |
| **2** | `""` *(min-)* | `"Giày Nike Air Pegasus 40"` | `1.500.000` | `50` | `10` | `code = min-` | **Lỗi:** Mã sản phẩm không được để trống | **B2** |
| **3** | `"N"` *(min)* | `"Giày Nike Air Pegasus 40"` | `1.500.000` | `50` | `10` | `code = min` | **Hợp lệ:** Lưu sản phẩm thành công | **B3** |
| **4** | `"NK"` *(min+)* | `"Giày Nike Air Pegasus 40"` | `1.500.000` | `50` | `10` | `code = min+` | **Hợp lệ:** Lưu sản phẩm thành công | **B4** |
| **5** | `[19 ký tự]` *(max-)* | `"Giày Nike Air Pegasus 40"` | `1.500.000` | `50` | `10` | `code = max-` | **Hợp lệ:** Lưu sản phẩm thành công | **B5** |
| **6** | `[20 ký tự]` *(max)* | `"Giày Nike Air Pegasus 40"` | `1.500.000` | `50` | `10` | `code = max` | **Hợp lệ:** Lưu sản phẩm thành công | **B6** |
| **7** | `[21 ký tự]` *(max+)* | `"Giày Nike Air Pegasus 40"` | `1.500.000` | `50` | `10` | `code = max+` | **Lỗi:** Mã sản phẩm tối đa 20 ký tự | **B7** |
| **8** | `"NK-PEGASUS"` | `""` *(min-)* | `1.500.000` | `50` | `10` | `name = min-` | **Lỗi:** Tên sản phẩm không được để trống | **B8** |
| **9** | `"NK-PEGASUS"` | `"G"` *(min)* | `1.500.000` | `50` | `10` | `name = min` | **Hợp lệ:** Lưu sản phẩm thành công | **B9** |
| **10**| `"NK-PEGASUS"` | `"Gi"` *(min+)* | `1.500.000` | `50` | `10` | `name = min+` | **Hợp lệ:** Lưu sản phẩm thành công | **B10**|
| **11**| `"NK-PEGASUS"` | `[254 ký tự]` *(max-)* | `1.500.000` | `50` | `10` | `name = max-` | **Hợp lệ:** Lưu sản phẩm thành công | **B11**|
| **12**| `"NK-PEGASUS"` | `[255 ký tự]` *(max)* | `1.500.000` | `50` | `10` | `name = max` | **Hợp lệ:** Lưu sản phẩm thành công | **B12**|
| **13**| `"NK-PEGASUS"` | `[256 ký tự]` *(max+)* | `1.500.000` | `50` | `10` | `name = max+` | **Lỗi:** Tên sản phẩm tối đa 255 ký tự | **B13**|
| **14**| `"NK-PEGASUS"` | `"Giày Nike Air Pegasus 40"` | `0` *(min-)* | `50` | `10` | `price = min-` | **Lỗi:** Giá sản phẩm phải lớn hơn 0! | **B14**|
| **15**| `"NK-PEGASUS"` | `"Giày Nike Air Pegasus 40"` | `1.000` *(min)* | `50` | `10` | `price = min` | **Hợp lệ:** Lưu sản phẩm thành công | **B15**|
| **16**| `"NK-PEGASUS"` | `"Giày Nike Air Pegasus 40"` | `10.000` *(min+)* | `50` | `10` | `price = min+` | **Hợp lệ:** Lưu sản phẩm thành công | **B16**|
| **17**| `"NK-PEGASUS"` | `"Giày Nike Air Pegasus 40"` | `99.990.000` *(max-)*| `50` | `10` | `price = max-` | **Hợp lệ:** Lưu sản phẩm thành công | **B17**|
| **18**| `"NK-PEGASUS"` | `"Giày Nike Air Pegasus 40"` | `100.000.000` *(max)* | `50` | `10` | `price = max` | **Hợp lệ:** Lưu sản phẩm thành công | **B18**|
| **19**| `"NK-PEGASUS"` | `"Giày Nike Air Pegasus 40"` | `100.000.001` *(max+)*| `50` | `10` | `price = max+` | **Cảnh báo:** Tiếp nhận đơn giá lớn | **B19**|
| **20**| `"NK-PEGASUS"` | `"Giày Nike Air Pegasus 40"` | `1.500.000` | `-1` *(min-)* | `10` | `stock = min-` | **Lỗi:** Số lượng tồn kho không được âm! | **B20**|
| **21**| `"NK-PEGASUS"` | `"Giày Nike Air Pegasus 40"` | `1.500.000` | `0` *(min)* | `10` | `stock = min` | **Hợp lệ:** Lưu sản phẩm thành công | **B21**|
| **22**| `"NK-PEGASUS"` | `"Giày Nike Air Pegasus 40"` | `1.500.000` | `1` *(min+)* | `10` | `stock = min+` | **Hợp lệ:** Lưu sản phẩm thành công | **B22**|
| **23**| `"NK-PEGASUS"` | `"Giày Nike Air Pegasus 40"` | `1.500.000` | `9.999` *(max-)* | `10` | `stock = max-` | **Hợp lệ:** Lưu sản phẩm thành công | **B23**|
| **24**| `"NK-PEGASUS"` | `"Giày Nike Air Pegasus 40"` | `1.500.000` | `10.000` *(max)* | `10` | `stock = max` | **Hợp lệ:** Lưu sản phẩm thành công | **B24**|
| **25**| `"NK-PEGASUS"` | `"Giày Nike Air Pegasus 40"` | `1.500.000` | `10.001` *(max+)* | `10` | `stock = max+` | **Cảnh báo:** Tiếp nhận tồn kho lớn | **B25**|
| **26**| `"NK-PEGASUS"` | `"Giày Nike Air Pegasus 40"` | `1.500.000` | `50` | `-1` *(min-)* | `discount = min-` | **Lỗi:** Phần trăm giảm giá phải trong khoảng 0 đến 100! | **B26**|
| **27**| `"NK-PEGASUS"` | `"Giày Nike Air Pegasus 40"` | `1.500.000` | `50` | `0` *(min)* | `discount = min` | **Hợp lệ:** Lưu sản phẩm thành công | **B27**|
| **28**| `"NK-PEGASUS"` | `"Giày Nike Air Pegasus 40"` | `1.500.000` | `50` | `1` *(min+)* | `discount = min+` | **Hợp lệ:** Lưu sản phẩm thành công | **B28**|
| **29**| `"NK-PEGASUS"` | `"Giày Nike Air Pegasus 40"` | `1.500.000` | `50` | `99` *(max-)* | `discount = max-` | **Hợp lệ:** Lưu sản phẩm thành công | **B29**|
| **30**| `"NK-PEGASUS"` | `"Giày Nike Air Pegasus 40"` | `1.500.000` | `50` | `100` *(max)* | `discount = max` | **Hợp lệ:** Lưu sản phẩm thành công | **B30**|
| **31**| `"NK-PEGASUS"` | `"Giày Nike Air Pegasus 40"` | `1.500.000` | `50` | `101` *(max+)* | `discount = max+`| **Lỗi:** Phần trăm giảm giá phải trong khoảng 0 đến 100! | **B31**|

---

## 4. Thiết Kế Bảng Test Cases Chi Tiết Triển Khai (Tối Ưu & Đầy Đủ Bao Phủ)

| STT | Mã Test Case | Tên ca kiểm thử | Dữ liệu kiểm thử (Test Input Data) | Kết quả mong đợi (Expected Output) | Tag được bao phủ |
| :-: | :--- | :--- | :--- | :--- | :---: |
| **1** | **TC_PROD_01** | Tạo sản phẩm hợp lệ với dữ liệu danh định đầy đủ | • `currentUserRole`: `ROLE_ADMIN`<br>• `code`: `"NK-PEGASUS"` (10 ký tự)<br>• `name`: `"Giày Nike Air Pegasus 40"` (24 ký tự)<br>• `price`: `1.500.000`<br>• `stockQuantity`: `50`<br>• `discountPercent`: `10`<br>• `fileData`: Tệp ảnh `nike_pegasus.jpg` (hợp lệ, AI duyệt) | **Hợp lệ:** Chuyển hướng `/productList`, flash: *"Lưu sản phẩm thành công!"*. | **V1, V2, V3, V4, V5, V6, V8, B1** |
| **2** | **TC_PROD_02** | Tạo sản phẩm hợp lệ tại tất cả các cận dưới ($min$) | • `code`: `"N"` (1 ký tự)<br>• `name`: `"G"` (1 ký tự)<br>• `price`: `1.000`<br>• `stockQuantity`: `0`<br>• `discountPercent`: `0`<br>• `fileData`: `null` (không tải tệp) | **Hợp lệ:** Lưu thành công tại các cận dưới, flash: *"Lưu sản phẩm thành công!"*. | **V2, V3, V4, V5, V6, V7, B3, B9, B15, B21, B27** |
| **3** | **TC_PROD_03** | Tạo sản phẩm hợp lệ tại tất cả các cận trên ($max$) | • `code`: Chuỗi 20 ký tự<br>• `name`: Chuỗi 255 ký tự<br>• `price`: `100.000.000`<br>• `stockQuantity`: `10.000`<br>• `discountPercent`: `100`<br>• `fileData`: `null` | **Hợp lệ:** Lưu thành công tại các cận trên, flash: *"Lưu sản phẩm thành công!"*. | **V2, V3, V4, V5, V6, V7, B6, B12, B18, B24, B30** |
| **4** | **TC_PROD_04** | Tạo sản phẩm hợp lệ tại các kề cận dưới ($min^+$) | • `code`: `"NK"` (2 ký tự)<br>• `name`: `"Gi"` (2 ký tự)<br>• `price`: `10.000`<br>• `stockQuantity`: `1`<br>• `discountPercent`: `1`<br>• `fileData`: `null` | **Hợp lệ:** Lưu thành công tại các kề cận dưới, flash: *"Lưu sản phẩm thành công!"*. | **V2, V3, V4, V5, V6, V7, B4, B10, B16, B22, B28** |
| **5** | **TC_PROD_05** | Tạo sản phẩm hợp lệ tại các kề cận trên ($max^-$) | • `code`: Chuỗi 19 ký tự<br>• `name`: Chuỗi 254 ký tự<br>• `price`: `99.990.000`<br>• `stockQuantity`: `9.999`<br>• `discountPercent`: `99`<br>• `fileData`: `null` | **Hợp lệ:** Lưu thành công tại các kề cận trên, flash: *"Lưu sản phẩm thành công!"*. | **V2, V3, V4, V5, V6, V7, B5, B11, B17, B23, B29** |
| **6** | **TC_PROD_06** | Tạo sản phẩm hợp lệ tại các ngoại biên trên ($max^+$) chấp nhận được khi không tải ảnh | • `currentUserRole`: `ROLE_ADMIN`<br>• `code`: `"NK-MAXPLUS-01"`<br>• `name`: `"Giày Nike Air Max Plus"`<br>• `price`: `100.000.001`<br>• `stockQuantity`: `10.001`<br>• `discountPercent`: `10`<br>• `fileData`: `null` | **Hợp lệ:** Lưu thành công khi đơn giá và tồn kho lớn ($max^+$), hiển thị ảnh placeholder mặc định, flash: *"Lưu sản phẩm thành công!"*. | **V2, V3, V6, V7, B19, B25** |
| **7** | **TC_PROD_07** | Tạo sản phẩm khi AI Service ngoại tuyến (Fallback mode) | • `code`: `"NK-AI-FALLBACK"`<br>• `name`: `"Giày Nike AI Offline Fallback"`<br>• `price`: `1.500.000`, `stockQuantity`: `50`, `discountPercent`: `10`<br>• `fileData`: Tệp ảnh hợp lệ khi AI offline (`IOException`) | **Hợp lệ (Fallback):** Ghi log cảnh báo `aiWarning`, bỏ qua kiểm duyệt AI, lưu sản phẩm thành công, flash: *"Lưu sản phẩm thành công!"*. | **V9** |
| **8** | **TC_PROD_08** | Báo lỗi khi mã sản phẩm để trống ($min^-$) | • `code`: `""` (rỗng hoặc khoảng trắng)<br>• Các trường khác điền giá trị danh định hợp lệ | **Lỗi:** Chặn submit, giữ nguyên form `product.html`, hiển thị lỗi: *"Mã sản phẩm không được để trống"*. | **X3, B2** |
| **9** | **TC_PROD_09** | Báo lỗi khi mã sản phẩm dài 21 ký tự ($max^+$) | • `code`: Chuỗi 21 ký tự (`"PROD-CODE-MAXPLUS-21C"`)<br>• Các trường khác điền giá trị danh định hợp lệ | **Lỗi:** Chặn submit, hiển thị thông báo lỗi: *"Mã sản phẩm tối đa 20 ký tự"*. | **X4, B7** |
| **10**| **TC_PROD_10** | Báo lỗi khi mã sản phẩm đã tồn tại trong CSDL (Trùng SKU) | • `code`: `"S001"` (Đã tồn tại trong CSDL)<br>• `isNewProduct`: `true`<br>• Các trường khác điền giá trị danh định hợp lệ | **Lỗi:** Kiểm tra trùng lặp thất bại, hiển thị thông báo: *"Mã sản phẩm đã tồn tại"*. | **X5** |
| **11**| **TC_PROD_11** | Báo lỗi khi tên sản phẩm để trống ($min^-$) | • `code`: `"NK-NEW-01"`<br>• `name`: `""` (rỗng hoặc khoảng trắng)<br>• Các trường khác điền giá trị danh định hợp lệ | **Lỗi:** Chặn submit, hiển thị thông báo lỗi: *"Tên sản phẩm không được để trống"*. | **X6, B8** |
| **12**| **TC_PROD_12** | Báo lỗi khi tên sản phẩm dài 256 ký tự ($max^+$) | • `code`: `"NK-NEW-02"`<br>• `name`: Chuỗi dài 256 ký tự<br>• Các trường khác điền giá trị danh định hợp lệ | **Lỗi:** Chặn submit, hiển thị thông báo lỗi: *"Tên sản phẩm tối đa 255 ký tự"*. | **X7, B13** |
| **13**| **TC_PROD_13** | Báo lỗi khi đơn giá sản phẩm bằng 0 ($min^-$) | • `code`: `"NK-NEW-03"`, `name`: `"Giày Nike Test Giá 0"`<br>• `price`: `0`<br>• Các trường khác điền giá trị danh định hợp lệ | **Lỗi:** Chặn submit, hiển thị thông báo lỗi: *"Giá sản phẩm phải lớn hơn 0!"*. | **X8, B14** |
| **14**| **TC_PROD_14** | Báo lỗi khi đơn giá sản phẩm là số âm ($min^{--}$) | • `code`: `"NK-NEW-04"`, `name`: `"Giày Nike Test Giá Âm"`<br>• `price`: `-50.000`<br>• Các trường khác điền giá trị danh định hợp lệ | **Lỗi:** Chặn submit, hiển thị thông báo lỗi: *"Giá sản phẩm phải lớn hơn 0!"*. | **X9** |
| **15**| **TC_PROD_15** | Báo lỗi khi số lượng tồn kho là số âm ($min^-$) | • `code`: `"NK-NEW-05"`, `name`: `"Giày Nike Test Tồn Âm"`<br>• `stockQuantity`: `-1`<br>• Các trường khác điền giá trị danh định hợp lệ | **Lỗi:** Chặn submit, hiển thị thông báo lỗi: *"Số lượng tồn kho không được âm!"*. | **X10, B20** |
| **16**| **TC_PROD_16** | Báo lỗi khi phần trăm giảm giá là số âm ($min^-$) | • `code`: `"NK-NEW-06"`, `name`: `"Giày Nike Test Giảm Âm"`<br>• `discountPercent`: `-1`<br>• Các trường khác điền giá trị danh định hợp lệ | **Lỗi:** Chặn submit, hiển thị thông báo lỗi: *"Phần trăm giảm giá phải trong khoảng 0 đến 100!"*. | **X11, B26** |
| **17**| **TC_PROD_17** | Báo lỗi khi phần trăm giảm giá vượt 100% ($max^+$) | • `code`: `"NK-NEW-07"`, `name`: `"Giày Nike Test Giảm Quá 100"`<br>• `discountPercent`: `101`<br>• Các trường khác điền giá trị danh định hợp lệ | **Lỗi:** Chặn submit, hiển thị thông báo lỗi: *"Phần trăm giảm giá phải trong khoảng 0 đến 100!"*. | **X12, B31** |
| **18**| **TC_PROD_18** | Báo lỗi khi tệp ảnh bị AI Quality Gate từ chối | • `code`: `"NK-AI-REJECT"`<br>• `name`: `"Giày Nike Ảnh Không Chuẩn"`<br>• `fileData`: Tệp ảnh `blurry_dark.png` (AI trả về `approved = false`) | **Lỗi:** Chặn lưu, giữ nguyên form `product.html`, hiển thị cảnh báo `aiError` và các chỉ số `aiMetrics`. | **X13** |
| **19**| **TC_PROD_19** | Báo lỗi khi tệp ảnh bị lỗi đọc dữ liệu byte (I/O Error) | • `code`: `"NK-IO-ERR"`<br>• `name`: `"Giày Nike Lỗi File"`<br>• `fileData`: Tệp hỏng gây `IOException` | **Lỗi:** Hệ thống bắt ngoại lệ, hiển thị thông báo lỗi: *"Không thể đọc file ảnh sản phẩm."*. | **X14** |
| **20**| **TC_PROD_20** | Chặn khách vãng lai chưa đăng nhập truy cập tạo sản phẩm | • `currentUserRole`: `Guest` (Chưa đăng nhập)<br>• Thao tác: Gửi yêu cầu `GET` hoặc `POST /admin/product` | **Bị chặn:** Spring Security chuyển hướng (HTTP 302) về trang Đăng nhập (`/admin/login`). | **X1** |
| **21**| **TC_PROD_21** | Chặn tài khoản người dùng thường truy cập tạo sản phẩm | • `currentUserRole`: `ROLE_USER`<br>• Thao tác: Gửi yêu cầu `GET` hoặc `POST /admin/product` | **Bị chặn:** Spring Security từ chối truy cập, trả về mã lỗi **`HTTP 403 Forbidden`**. | **X2** |
