# THIẾT KẾ TEST CASE HỘP ĐEN: QUY TRÌNH THANH TOÁN & ĐẶT HÀNG (CHECKOUT - ORDER PLACEMENT)

> **Chức năng:** Quy trình xác thực thông tin giao hàng, kiểm soát tồn kho, áp dụng chiết khấu voucher và hoàn tất đơn hàng (`/shoppingCartCustomer` $\rightarrow$ `/shoppingCartConfirmation` $\rightarrow$ `/shoppingCartFinalize`).  

---

## 1. Xác Định Biến Đầu Vào & Ràng Buộc Nghiệp Vụ (Input Variables)

| Tên Biến | Ý Nghĩa | Kiểu | Ràng Buộc & Miền Giá Trị Hợp Lệ |
| :--- | :--- | :---: | :--- |
| **`cartLines`** | Danh sách sản phẩm trong giỏ | `List<CartLine>` | Bắt buộc khác rỗng (`size > 0`), mỗi dòng có `quantity > 0` |
| **`customerName`** | Họ và tên người nhận | `String` | Độ dài $[1, 255]$ ký tự, không chứa ký tự cấm (`@#$%^&*...`) |
| **`customerAddress`**| Địa chỉ giao hàng | `String` | Độ dài $[1, 255]$ ký tự, không chứa chuỗi injection/script |
| **`customerEmail`** | Địa chỉ email liên hệ | `String` | Độ dài $[6, 128]$ ký tự, đúng định dạng `user@domain.ext` |
| **`customerPhone`** | Số điện thoại nhận hàng | `String` | Độ dài $[1, 20]$ ký tự (chuẩn $[10, 11]$ chữ số), chỉ chứa số |
| **`stockQuantity`** | Tồn kho sản phẩm tại thời điểm chốt | `int` | $stockQuantity \ge quantity$ (Đủ hàng đáp ứng) |
| **`voucherCode`** | Mã giảm giá đính kèm | `String` | Tùy chọn (`null` / rỗng); nếu có: mã phải active, còn hạn, còn lượt |
| **`currentUser`** | Trạng thái đăng nhập | `User` | Khách vãng lai (`Guest`) hoặc Thành viên (`ROLE_CUSTOMER` / `ADMIN`) |

---

## 2. Bảng Phân Hoạch Tương Đương (Equivalence Partitioning - EP)

| STT | Biến / Điều kiện | Lớp hợp lệ (Valid) | Tag | Lớp không hợp lệ (Invalid) | Tag |
| :-: | :--- | :--- | :---: | :--- | :---: |
| **1** | **Giỏ hàng (`cartLines`)** | Giỏ hàng có chứa $\ge 1$ sản phẩm hợp lệ | **V1** | • Giỏ hàng rỗng (`cart = null` hoặc `lines = []`)<br>• Dòng hàng chứa số lượng $\le 0$ | **X1**<br>**X2** |
| **2** | **Họ tên (`customerName`)** | Chuỗi $[1, 255]$ ký tự không chứa ký tự cấm | **V2** | • Để trống / toàn khoảng trắng<br>• Vượt quá 255 ký tự ($L > 255$)<br>• Chứa ký tự cấm (`@#$%^&*<>~{}`) | **X3**<br>**X4**<br>**X5** |
| **3** | **Địa chỉ (`customerAddress`)** | Chuỗi $[1, 255]$ ký tự không chứa mã độc | **V3** | • Để trống / rỗng<br>• Vượt quá 255 ký tự ($L > 255$)<br>• Chứa thẻ HTML / Script injection | **X6**<br>**X7**<br>**X8** |
| **4** | **Email (`customerEmail`)** | Chuỗi $[6, 128]$ ký tự đúng cú pháp email | **V4** | • Để trống<br>• Dưới 6 ký tự ($L < 6$)<br>• Vượt quá 128 ký tự ($L > 128$)<br>• Sai định dạng cú pháp email | **X9**<br>**X10**<br>**X11**<br>**X12** |
| **5** | **Điện thoại (`customerPhone`)** | Chuỗi $[1, 20]$ ký tự (10-11 số hợp lệ) | **V5** | • Để trống<br>• Vượt quá 20 ký tự ($L > 20$)<br>• Chứa chữ cái hoặc ký tự đặc biệt | **X13**<br>**X14**<br>**X15** |
| **6** | **Tồn kho (`stockQuantity`)** | $stockQuantity \ge orderQuantity$ | **V6** | Tồn kho không đủ đáp ứng ($stock < quantity$) | **X16** |
| **7** | **Trạng thái SP (`productState`)** | Sản phẩm `ACTIVE` (đang kinh doanh) | **V7** | Sản phẩm `INACTIVE` / `DRAFT` / Bị xóa | **X17** |
| **8** | **Mã giảm giá (`voucherCode`)** | • Không dùng voucher (`null`)<br>• Voucher hợp lệ thỏa mãn mọi điều kiện | **V8**<br>**V9** | Voucher không tồn tại, hết hạn, hết lượt, hoặc không đủ đơn tối thiểu | **X18** |

---

## 3. Bảng Phân Tích Giá Trị Biên (Robustness BVA - $6n + 1$)

### 3.1 Bảng 7 mốc giá trị biên Robustness BVA cho 5 biến định lượng

*(Ghi chú bộ giá trị danh định chuẩn: `name` $nom = \text{"Nguyễn Hoàng Phương"}$ (20 ký tự), `address` $nom = \text{"123 Lê Lợi, P. Bến Nghé, Q1"}$ (27 ký tự), `email` $nom = \text{"phuong@gmail.com"}$ (16 ký tự), `phone` $nom = \text{"0912345678"}$ (10 số), `qty` $nom = 2$ với tồn kho $stock = 10$).*

| Biến Định Lượng | Ngoại biên dưới ($min^-$) | Cận dưới ($min$) | Kề dưới ($min^+$) | Danh định ($nom$) | Kề trên ($max^-$) | Cận trên ($max$) | Ngoại biên trên ($max^+$) | Quy tắc & Giới hạn |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :--- |
| **1. Độ dài `customerName`** | `0` *(rỗng)* | **`1`** | **`2`** | **`20`** | **`254`** | **`255`** | `256` | Miền $[1, 255]$. Rỗng hoặc $> 255$ báo lỗi |
| **2. Độ dài `customerAddress`**| `0` *(rỗng)* | **`1`** | **`2`** | **`27`** | **`254`** | **`255`** | `256` | Miền $[1, 255]$. Rỗng hoặc $> 255$ báo lỗi |
| **3. Độ dài `customerEmail`** | `5` | **`6`** | **`7`** | **`16`** | **`127`** | **`128`** | `129` | Miền $[6, 128]$. $< 6$ hoặc $> 128$ báo lỗi |
| **4. Độ dài `customerPhone`** | `0` *(rỗng)* | **`1`** | **`2`** | **`10`** | **`19`** | **`20`** | `21` | Miền $[1, 20]$. Rỗng hoặc $> 20$ báo lỗi |
| **5. Số lượng `orderQuantity`**| `0` *(hủy)* | **`1`** | **`2`** | **`2`** | **`9`** | **`10`** | `11` *(vượt)* | Miền $[1, 10]$ (Tồn kho = 10). $> 10$ báo lỗi thiếu kho |

---

### 3.2 Bảng Đầy Đủ Robustness BVA Test Cases ($6n + 1 = 31$ Ca Kiểm Thử)

| Case | Tên nhận (`name`) | Địa chỉ (`address`) | Email (`email`) | SĐT (`phone`) | Số lượng (`qty`) | Mốc kiểm thử | Kết quả mong đợi (Expected Output) | Tag Biên |
| :-: | :--- | :--- | :--- | :--- | :---: | :---: | :--- | :-: |
| **1** | `"Nguyễn Hoàng Phương"` *(nom)* | `"123 Lê Lợi, Q1"` *(nom)* | `"phuong@gmail.com"` *(nom)* | `"0912345678"` *(nom)* | `2` *(nom)* | **Tất cả ở nom** | **Hợp lệ:** Xác nhận và đặt hàng thành công | **B1** |
| **2** | `""` *(min-)* | `"123 Lê Lợi, Q1"` | `"phuong@gmail.com"` | `"0912345678"` | `2` | `name = min-` | **Lỗi:** Họ tên không được để trống | **B2** |
| **3** | `"N"` *(min)* | `"123 Lê Lợi, Q1"` | `"phuong@gmail.com"` | `"0912345678"` | `2` | `name = min` | **Hợp lệ:** Tiếp nhận tên 1 ký tự | **B3** |
| **4** | `"Ng"` *(min+)* | `"123 Lê Lợi, Q1"` | `"phuong@gmail.com"` | `"0912345678"` | `2` | `name = min+` | **Hợp lệ:** Tiếp nhận tên 2 ký tự | **B4** |
| **5** | `[254 ký tự]` *(max-)* | `"123 Lê Lợi, Q1"` | `"phuong@gmail.com"` | `"0912345678"` | `2` | `name = max-` | **Hợp lệ:** Tiếp nhận tên 254 ký tự | **B5** |
| **6** | `[255 ký tự]` *(max)* | `"123 Lê Lợi, Q1"` | `"phuong@gmail.com"` | `"0912345678"` | `2` | `name = max` | **Hợp lệ:** Tiếp nhận tên 255 ký tự | **B6** |
| **7** | `[256 ký tự]` *(max+)* | `"123 Lê Lợi, Q1"` | `"phuong@gmail.com"` | `"0912345678"` | `2` | `name = max+` | **Lỗi:** Họ tên tối đa 255 ký tự | **B7** |
| **8** | `"Nguyễn Hoàng Phương"` | `""` *(min-)* | `"phuong@gmail.com"` | `"0912345678"` | `2` | `address = min-` | **Lỗi:** Địa chỉ không được để trống | **B8** |
| **9** | `"Nguyễn Hoàng Phương"` | `"1"` *(min)* | `"phuong@gmail.com"` | `"0912345678"` | `2` | `address = min` | **Hợp lệ:** Tiếp nhận địa chỉ 1 ký tự | **B9** |
| **10**| `"Nguyễn Hoàng Phương"` | `"12"` *(min+)* | `"phuong@gmail.com"` | `"0912345678"` | `2` | `address = min+` | **Hợp lệ:** Tiếp nhận địa chỉ 2 ký tự | **B10**|
| **11**| `"Nguyễn Hoàng Phương"` | `[254 ký tự]` *(max-)* | `"phuong@gmail.com"` | `"0912345678"` | `2` | `address = max-` | **Hợp lệ:** Tiếp nhận địa chỉ 254 ký tự | **B11**|
| **12**| `"Nguyễn Hoàng Phương"` | `[255 ký tự]` *(max)* | `"phuong@gmail.com"` | `"0912345678"` | `2` | `address = max` | **Hợp lệ:** Tiếp nhận địa chỉ 255 ký tự | **B12**|
| **13**| `"Nguyễn Hoàng Phương"` | `[256 ký tự]` *(max+)* | `"phuong@gmail.com"` | `"0912345678"` | `2` | `address = max+` | **Lỗi:** Địa chỉ tối đa 255 ký tự | **B13**|
| **14**| `"Nguyễn Hoàng Phương"` | `"123 Lê Lợi, Q1"` | `"a@b.c"` *(min-)* | `"0912345678"` | `2` | `email = min-` | **Lỗi:** Email tối thiểu 6 ký tự | **B14**|
| **15**| `"Nguyễn Hoàng Phương"` | `"123 Lê Lợi, Q1"` | `"a@b.co"` *(min)* | `"0912345678"` | `2` | `email = min` | **Hợp lệ:** Tiếp nhận email 6 ký tự (`a@b.co`) | **B15**|
| **16**| `"Nguyễn Hoàng Phương"` | `"123 Lê Lợi, Q1"` | `"ab@b.co"` *(min+)* | `"0912345678"` | `2` | `email = min+` | **Hợp lệ:** Tiếp nhận email 7 ký tự | **B16**|
| **17**| `"Nguyễn Hoàng Phương"` | `"123 Lê Lợi, Q1"` | `[127 ký tự]` *(max-)* | `"0912345678"` | `2` | `email = max-` | **Hợp lệ:** Tiếp nhận email 127 ký tự | **B17**|
| **18**| `"Nguyễn Hoàng Phương"` | `"123 Lê Lợi, Q1"` | `[128 ký tự]` *(max)* | `"0912345678"` | `2` | `email = max` | **Hợp lệ:** Tiếp nhận email 128 ký tự | **B18**|
| **19**| `"Nguyễn Hoàng Phương"` | `"123 Lê Lợi, Q1"` | `[129 ký tự]` *(max+)* | `"0912345678"` | `2` | `email = max+` | **Lỗi:** Email tối đa 128 ký tự | **B19**|
| **20**| `"Nguyễn Hoàng Phương"` | `"123 Lê Lợi, Q1"` | `"phuong@gmail.com"` | `""` *(min-)* | `2` | `phone = min-` | **Lỗi:** Số điện thoại không được để trống | **B20**|
| **21**| `"Nguyễn Hoàng Phương"` | `"123 Lê Lợi, Q1"` | `"phuong@gmail.com"` | `"1"` *(min)* | `2` | `phone = min` | **Hợp lệ:** Tiếp nhận SĐT 1 ký tự | **B21**|
| **22**| `"Nguyễn Hoàng Phương"` | `"123 Lê Lợi, Q1"` | `"phuong@gmail.com"` | `"12"` *(min+)* | `2` | `phone = min+` | **Hợp lệ:** Tiếp nhận SĐT 2 ký tự | **B22**|
| **23**| `"Nguyễn Hoàng Phương"` | `"123 Lê Lợi, Q1"` | `"phuong@gmail.com"` | `[19 số]` *(max-)* | `2` | `phone = max-` | **Hợp lệ:** Tiếp nhận SĐT 19 ký tự | **B23**|
| **24**| `"Nguyễn Hoàng Phương"` | `"123 Lê Lợi, Q1"` | `"phuong@gmail.com"` | `[20 số]` *(max)* | `2` | `phone = max` | **Hợp lệ:** Tiếp nhận SĐT 20 ký tự | **B24**|
| **25**| `"Nguyễn Hoàng Phương"` | `"123 Lê Lợi, Q1"` | `"phuong@gmail.com"` | `[21 số]` *(max+)* | `2` | `phone = max+` | **Lỗi:** Số điện thoại tối đa 20 ký tự | **B25**|
| **26**| `"Nguyễn Hoàng Phương"` | `"123 Lê Lợi, Q1"` | `"phuong@gmail.com"` | `"0912345678"` | `0` *(min-)* | `qty = min-` | **Lỗi:** Số lượng đặt phải lớn hơn 0 | **B26**|
| **27**| `"Nguyễn Hoàng Phương"` | `"123 Lê Lợi, Q1"` | `"phuong@gmail.com"` | `"0912345678"` | `1` *(min)* | `qty = min` | **Hợp lệ:** Đặt mua 1 sản phẩm | **B27**|
| **28**| `"Nguyễn Hoàng Phương"` | `"123 Lê Lợi, Q1"` | `"phuong@gmail.com"` | `"0912345678"` | `2` *(min+)* | `qty = min+` | **Hợp lệ:** Đặt mua 2 sản phẩm | **B28**|
| **29**| `"Nguyễn Hoàng Phương"` | `"123 Lê Lợi, Q1"` | `"phuong@gmail.com"` | `"0912345678"` | `9` *(max-)* | `qty = max-` | **Hợp lệ:** Đặt mua 9 sản phẩm | **B29**|
| **30**| `"Nguyễn Hoàng Phương"` | `"123 Lê Lợi, Q1"` | `"phuong@gmail.com"` | `"0912345678"` | `10` *(max)* | `qty = max` | **Hợp lệ:** Đặt mua toàn bộ tồn kho 10 SP | **B30**|
| **31**| `"Nguyễn Hoàng Phương"` | `"123 Lê Lợi, Q1"` | `"phuong@gmail.com"` | `"0912345678"` | `11` *(max+)* | `qty = max+` | **Lỗi:** Không đủ số lượng trong kho | **B31**|

---

## 4. Kỹ Thuật Bảng Quyết Định (Decision Table Testing)

### 4.1 Bảng Quyết Định Rút Gọn (Collapsed Decision Table - 7 Rules)

| Điều kiện / Hành động | Rule 1 (Giỏ rỗng) | Rule 2 (Lỗi Form) | Rule 3 (Lỗi Dòng) | Rule 4 (Lỗi SP) | Rule 5 (Lỗi Kho) | Rule 6 (Lỗi Voucher) | Rule 7 (Thành Công) |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| **C1: Giỏ hàng có dữ liệu (Not Null & Not Empty)?** | **N** | Y | Y | Y | Y | Y | Y |
| **C2: Form giao hàng hợp lệ (4 trường)?** | - | **N** | Y | Y | Y | Y | Y |
| **C3: Dòng hàng CartLine hợp lệ ($qty \ge 1$)?** | - | - | **N** | Y | Y | Y | Y |
| **C4: Sản phẩm đang kinh doanh (`ACTIVE`)?** | - | - | - | **N** | Y | Y | Y |
| **C5: Tồn kho đủ đáp ứng ($stock \ge qty$)?** | - | - | - | - | **N** | Y | Y |
| **C6: Mã Voucher hợp lệ / Không dùng mã?** | - | - | - | - | - | **N** | **Y** |
| *A1: Từ chối & Chuyển hướng về `/shoppingCart`* | **X** | - | - | - | - | - | - |
| *A2: Giữ lại Bước 2 & Hiển thị lỗi Form* | - | **X** | - | - | - | - | - |
| *A3: Ném IllegalArgumentException (Dòng lỗi)* | - | - | **X** | - | - | - | - |
| *A4: Ném IllegalStateException (SP ngừng bán)* | - | - | - | **X** | - | - | - |
| *A5: Ném IllegalStateException (Thiếu kho)* | - | - | - | - | **X** | - | - |
| *A6: Ném IllegalStateException (Voucher lỗi)* | - | - | - | - | - | **X** | - |
| *A7: Tạo Order, Trừ kho, Sang Bước 4 (Finalize)* | - | - | - | - | - | - | **X** |
| **Tag Bảng Quyết Định** | **D1** | **D2** | **D3** | **D4** | **D5** | **D6** | **D7** |

*Giải thích quy tắc nghiệp vụ:*
- **Rule 1 (`D1`):** Giỏ hàng rỗng khi truy cập thanh toán $\to$ Từ chối và chuyển hướng về trang Giỏ hàng `/shoppingCart`.
- **Rule 2 (`D2`):** Thông tin người nhận điền thiếu hoặc sai định dạng $\to$ Giữ lại Bước 2, báo lỗi form.
- **Rule 3 (`D3`):** Dòng sản phẩm có số lượng không hợp lệ $\le 0 \to$ Báo lỗi `IllegalArgumentException`.
- **Rule 4 (`D4`):** Sản phẩm trong giỏ đã bị ẩn hoặc ngừng bán $\to$ Báo lỗi `IllegalStateException`.
- **Rule 5 (`D5`):** Số lượng đặt mua vượt quá tồn kho thực tế $\to$ Báo lỗi `IllegalStateException: Không đủ số lượng trong kho`.
- **Rule 6 (`D6`):** Voucher không hợp lệ hoặc không đủ điều kiện tối thiểu $\to$ Báo lỗi từ chối áp dụng voucher.
- **Rule 7 (`D7`):** Thỏa mãn mọi điều kiện $\to$ Tạo Order, Trừ tồn kho, Xóa giỏ hàng và hoàn tất đơn hàng.

---

## 5. Kỹ Thuật Kiểm Thử Chuyển Đổi Trạng Thái (State Transition Testing - STT)

### 5.1 Sơ đồ chuyển đổi trạng thái quy trình đặt hàng

```mermaid
stateDiagram-v2
    [*] --> Step1_ShoppingCart : Khách có sản phẩm trong giỏ
    Step1_ShoppingCart --> Step2_CustomerForm : ST1: Bấm Tiến hành thanh toán
    Step1_ShoppingCart --> Step1_ShoppingCart : ST2: Giỏ hàng rỗng (Bị chặn chuyển bước)
    Step2_CustomerForm --> Step2_CustomerForm : ST3: Form lỗi / Thiếu trường (Giữ lại Bước 2)
    Step2_CustomerForm --> Step3_Confirmation : ST4: Form hợp lệ, bấm Tiếp tục
    Step3_Confirmation --> Step2_CustomerForm : ST5: Bấm Quay lại sửa thông tin
    Step3_Confirmation --> Step1_ShoppingCart : ST6: Hết tồn kho giữa chừng (Báo lỗi & Giữ giỏ)
    Step3_Confirmation --> Step4_Finalize : ST7: Bấm Xác nhận đặt hàng (Tạo đơn & Trừ kho)
    Step4_Finalize --> [*] : Hoàn tất đơn hàng
```

### 5.2 Bảng phân tích chi tiết các Ca chuyển đổi trạng thái (State Transition Details)

| Mã Transition | Bước hiện tại | Sự kiện / Hành vi người dùng | Bước tiếp theo | Kết quả xử lý hệ thống | Tag |
| :---: | :--- | :--- | :--- | :--- | :---: |
| **ST_01** | `Step 1: ShoppingCart` | Bấm "Tiến hành đặt hàng" (Giỏ có hàng) | `Step 2: CustomerForm` | Điều hướng sang màn hình điền thông tin | **ST1** |
| **ST_02** | `Step 1: ShoppingCart` | Truy cập Checkout khi giỏ hàng rỗng | `Step 1: ShoppingCart` | Chặn chuyển bước, redirect về `/shoppingCart` | **ST2** |
| **ST_03** | `Step 2: CustomerForm` | Submit form giao hàng thiếu/sai trường | `Step 2: CustomerForm` | Giữ nguyên form, hiển thị lỗi validate | **ST3** |
| **ST_04** | `Step 2: CustomerForm` | Submit form giao hàng hợp lệ | `Step 3: Confirmation` | Lưu thông tin tạm, điều hướng trang xác nhận | **ST4** |
| **ST_05** | `Step 3: Confirmation` | Bấm nút "Quay lại" (Back) | `Step 2: CustomerForm` | Quay lại form sửa thông tin, dữ liệu giữ nguyên | **ST5** |
| **ST_06** | `Step 3: Confirmation` | Bấm đặt hàng khi sản phẩm vừa bị mua hết | `Step 1: ShoppingCart` | Báo lỗi thiếu tồn kho, quay về giỏ hàng cập nhật | **ST6** |
| **ST_07** | `Step 3: Confirmation` | Bấm "Xác nhận đặt hàng" thành công | `Step 4: Finalize` | Tạo Order, trừ tồn kho, redirect trang hoàn tất | **ST7** |

---


## 6. Thiết Kế Bảng Test Cases Chi Tiết Triển Khai (Đầy Đủ Không Rút Gọn - 72 Ca Kiểm Thử)

| STT | Mã Test Case | Tên ca kiểm thử | Dữ liệu kiểm thử (Test Input Data) | Kết quả mong đợi (Expected Output) | Tag được bao phủ |
| :-: | :--- | :--- | :--- | :--- | :---: |
| **1** | **TC_CHK_01** | Đặt hàng thành công với giỏ hàng có chứa sản phẩm hợp lệ | • `cartLines`: 1 sản phẩm `S001` (số lượng 2)<br>• Các trường giao hàng danh định hợp lệ | **Hợp lệ:** Tiếp nhận giỏ hàng, chuyển sang bước điền thông tin thành công. | **V1** |
| **2** | **TC_CHK_02** | Đặt hàng thành công với họ tên người nhận hợp lệ | • `customerName`: `"Nguyễn Văn A"` (12 ký tự chữ)<br>• Các trường khác hợp lệ | **Hợp lệ:** Họ tên được chấp nhận, dữ liệu lưu vào đơn hàng. | **V2** |
| **3** | **TC_CHK_03** | Đặt hàng thành công với địa chỉ nhận hàng hợp lệ | • `customerAddress`: `"123 Lê Lợi, P. Bến Nghé, Q1"`<br>• Các trường khác hợp lệ | **Hợp lệ:** Địa chỉ giao hàng được ghi nhận chính xác. | **V3** |
| **4** | **TC_CHK_04** | Đặt hàng thành công với email người nhận hợp lệ | • `customerEmail`: `"nguyenvana@gmail.com"`<br>• Các trường khác hợp lệ | **Hợp lệ:** Email được lưu vào đơn và dùng gửi thông báo. | **V4** |
| **5** | **TC_CHK_05** | Đặt hàng thành công với số điện thoại hợp lệ | • `customerPhone`: `"0912345678"` (10 chữ số)<br>• Các trường khác hợp lệ | **Hợp lệ:** Số điện thoại được lưu vào thông tin người nhận. | **V5** |
| **6** | **TC_CHK_06** | Đặt hàng thành công khi số lượng mua nhỏ hơn hoặc bằng tồn kho | • `orderQuantity`: 2, `stockQuantity`: 10 ($stock \ge qty$)<br>• Đặt hàng hợp lệ | **Hợp lệ:** Cho phép đặt hàng, tự động trừ 2 sản phẩm trong kho. | **V6** |
| **7** | **TC_CHK_07** | Đặt hàng thành công với sản phẩm đang hoạt động kinh doanh | • Sản phẩm trong giỏ có trạng thái `ACTIVE` trong CSDL | **Hợp lệ:** Tiến hành xử lý đơn hàng bình thường. | **V7** |
| **8** | **TC_CHK_08** | Đặt hàng thành công khi không sử dụng mã voucher | • `voucherCode`: `null` (hoặc để trống)<br>• Giỏ hàng thanh toán nguyên giá | **Hợp lệ:** Tính tiền đúng theo tổng giá niêm yết, không giảm trừ. | **V8** |
| **9** | **TC_CHK_09** | Đặt hàng thành công khi áp dụng mã voucher hợp lệ | • `voucherCode`: `"WELCOME50"` (Giảm 20%, đơn đạt tối thiểu)<br>• Áp dụng voucher | **Hợp lệ:** Giảm giá thành công, tạo đơn và ghi nhận vào `Voucher_Usages`. | **V9** |
| **10** | **TC_CHK_10** | Chặn thanh toán khi giỏ hàng rỗng (Null hoặc rỗng) | • `cartLines`: `[]` (Rỗng)<br>• Thao tác: Truy cập `/shoppingCartCustomer` | **Lỗi:** Chặn chuyển bước, tự động chuyển hướng về `/shoppingCart`. | **X1** |
| **11** | **TC_CHK_11** | Chặn đặt hàng khi dòng hàng có số lượng không hợp lệ | • Dòng sản phẩm có `quantity = 0` (hoặc âm) | **Lỗi:** Ném `IllegalArgumentException: Số lượng phải lớn hơn 0`. | **X2** |
| **12** | **TC_CHK_12** | Báo lỗi khi để trống họ tên người nhận | • `customerName`: `""` (rỗng)<br>• Các trường khác danh định | **Lỗi:** Giữ nguyên ở Bước 2, báo lỗi: *"Tên người nhận không được để trống"*. | **X3** |
| **13** | **TC_CHK_13** | Báo lỗi khi họ tên người nhận vượt quá 255 ký tự | • `customerName`: Chuỗi dài 256 ký tự | **Lỗi:** Giữ nguyên ở Bước 2, báo lỗi: *"Tên người nhận tối đa 255 ký tự"*. | **X4** |
| **14** | **TC_CHK_14** | Từ chối họ tên người nhận chứa ký tự đặc biệt cấm | • `customerName`: `"Nguyễn Văn A @#$%^&*<>"` | **Lỗi:** Báo lỗi: *"Tên người nhận không được chứa ký tự đặc biệt cấm"*. | **X5** |
| **15** | **TC_CHK_15** | Báo lỗi khi để trống địa chỉ giao hàng | • `customerAddress`: `""` (rỗng) | **Lỗi:** Giữ nguyên ở Bước 2, báo lỗi: *"Địa chỉ giao hàng không được để trống"*. | **X6** |
| **16** | **TC_CHK_16** | Báo lỗi khi địa chỉ giao hàng vượt quá 255 ký tự | • `customerAddress`: Chuỗi dài 256 ký tự | **Lỗi:** Giữ nguyên ở Bước 2, báo lỗi: *"Địa chỉ giao hàng tối đa 255 ký tự"*. | **X7** |
| **17** | **TC_CHK_17** | Từ chối địa chỉ chứa mã độc XSS Script Injection | • `customerAddress`: `"123 Đường <script>alert(1)</script>"` | **Lỗi:** Bắt lỗi bảo mật ký tự nguy hiểm, chặn submit an toàn. | **X8** |
| **18** | **TC_CHK_18** | Báo lỗi khi để trống địa chỉ email người nhận | • `customerEmail`: `""` (rỗng) | **Lỗi:** Giữ nguyên ở Bước 2, báo lỗi: *"Email không được để trống"*. | **X9** |
| **19** | **TC_CHK_19** | Báo lỗi khi email người nhận dưới 6 ký tự | • `customerEmail`: `"a@b.c"` (5 ký tự) | **Lỗi:** Báo lỗi: *"Email phải có độ dài từ 6 đến 128 ký tự"*. | **X10** |
| **20** | **TC_CHK_20** | Báo lỗi khi email người nhận vượt quá 128 ký tự | • `customerEmail`: Chuỗi email dài 129 ký tự | **Lỗi:** Báo lỗi: *"Email tối đa 128 ký tự"*. | **X11** |
| **21** | **TC_CHK_21** | Từ chối email sai định dạng cú pháp | • `customerEmail`: `"nguyenvana_invalid_at_domain.com"` | **Lỗi:** Báo lỗi: *"Email không đúng định dạng"*. | **X12** |
| **22** | **TC_CHK_22** | Báo lỗi khi để trống số điện thoại người nhận | • `customerPhone`: `""` (rỗng) | **Lỗi:** Giữ nguyên ở Bước 2, báo lỗi: *"Số điện thoại không được để trống"*. | **X13** |
| **23** | **TC_CHK_23** | Báo lỗi khi số điện thoại vượt quá 20 ký tự | • `customerPhone`: Chuỗi 21 chữ số | **Lỗi:** Giữ nguyên ở Bước 2, báo lỗi: *"Số điện thoại tối đa 20 ký tự"*. | **X14** |
| **24** | **TC_CHK_24** | Từ chối số điện thoại chứa chữ cái hoặc ký tự cấm | • `customerPhone`: `"0912abc789@#"` | **Lỗi:** Báo lỗi: *"Số điện thoại chứa ký tự không hợp lệ"*. | **X15** |
| **25** | **TC_CHK_25** | Chặn đặt hàng khi số lượng mua vượt quá tồn kho | • `orderQuantity`: 11 (Tồn kho thực tế = 10) | **Lỗi:** Ném `IllegalStateException: Không đủ số lượng trong kho`. | **X16** |
| **26** | **TC_CHK_26** | Từ chối đặt hàng với sản phẩm ngừng kinh doanh | • Giỏ hàng chứa sản phẩm trạng thái `INACTIVE` | **Lỗi:** Ném `IllegalStateException: Sản phẩm không còn được bán`. | **X17** |
| **27** | **TC_CHK_27** | Từ chối mã voucher không hợp lệ / hết hạn / không đủ min | • `voucherCode`: `"INVALID_CODE_XYZ"` | **Lỗi:** Hệ thống báo lỗi: *"Mã giảm giá không tồn tại!"*. | **X18** |
| **28** | **TC_CHK_28** | Robustness BVA: Tất cả 5 biến định lượng ở mốc danh định | • Tất cả trường: `name` (20 ký tự), `address` (27 ký tự), `email` (16 ký tự), `phone` (10 số), `qty` = 2 | **Hợp lệ:** Xác nhận và đặt hàng thành công. | **B1** |
| **29** | **TC_CHK_29** | Robustness BVA: Họ tên người nhận tại ngoại biên dưới min- | • `customerName`: `""` (0 ký tự, rỗng) | **Lỗi:** Báo lỗi họ tên không được để trống. | **B2** |
| **30** | **TC_CHK_30** | Robustness BVA: Họ tên người nhận tại cận dưới min | • `customerName`: `"N"` (1 ký tự) | **Hợp lệ:** Tiếp nhận tên 1 ký tự thành công. | **B3** |
| **31** | **TC_CHK_31** | Robustness BVA: Họ tên người nhận tại kề cận dưới min+ | • `customerName`: `"Ng"` (2 ký tự) | **Hợp lệ:** Tiếp nhận tên 2 ký tự thành công. | **B4** |
| **32** | **TC_CHK_32** | Robustness BVA: Họ tên người nhận tại kề cận trên max- | • `customerName`: Chuỗi dài 254 ký tự | **Hợp lệ:** Tiếp nhận tên 254 ký tự thành công. | **B5** |
| **33** | **TC_CHK_33** | Robustness BVA: Họ tên người nhận tại cận trên max | • `customerName`: Chuỗi dài 255 ký tự | **Hợp lệ:** Tiếp nhận tên 255 ký tự thành công. | **B6** |
| **34** | **TC_CHK_34** | Robustness BVA: Họ tên người nhận tại ngoại biên trên max+ | • `customerName`: Chuỗi dài 256 ký tự | **Lỗi:** Báo lỗi họ tên tối đa 255 ký tự. | **B7** |
| **35** | **TC_CHK_35** | Robustness BVA: Địa chỉ nhận hàng tại ngoại biên dưới min- | • `customerAddress`: `""` (0 ký tự, rỗng) | **Lỗi:** Báo lỗi địa chỉ không được để trống. | **B8** |
| **36** | **TC_CHK_36** | Robustness BVA: Địa chỉ nhận hàng tại cận dưới min | • `customerAddress`: `"1"` (1 ký tự) | **Hợp lệ:** Tiếp nhận địa chỉ 1 ký tự thành công. | **B9** |
| **37** | **TC_CHK_37** | Robustness BVA: Địa chỉ nhận hàng tại kề cận dưới min+ | • `customerAddress`: `"12"` (2 ký tự) | **Hợp lệ:** Tiếp nhận địa chỉ 2 ký tự thành công. | **B10** |
| **38** | **TC_CHK_38** | Robustness BVA: Địa chỉ nhận hàng tại kề cận trên max- | • `customerAddress`: Chuỗi dài 254 ký tự | **Hợp lệ:** Tiếp nhận địa chỉ 254 ký tự thành công. | **B11** |
| **39** | **TC_CHK_39** | Robustness BVA: Địa chỉ nhận hàng tại cận trên max | • `customerAddress`: Chuỗi dài 255 ký tự | **Hợp lệ:** Tiếp nhận địa chỉ 255 ký tự thành công. | **B12** |
| **40** | **TC_CHK_40** | Robustness BVA: Địa chỉ nhận hàng tại ngoại biên trên max+ | • `customerAddress`: Chuỗi dài 256 ký tự | **Lỗi:** Báo lỗi địa chỉ tối đa 255 ký tự. | **B13** |
| **41** | **TC_CHK_41** | Robustness BVA: Email khách hàng tại ngoại biên dưới min- | • `customerEmail`: `"a@b.c"` (5 ký tự) | **Lỗi:** Báo lỗi email tối thiểu 6 ký tự. | **B14** |
| **42** | **TC_CHK_42** | Robustness BVA: Email khách hàng tại cận dưới min | • `customerEmail`: `"a@b.co"` (6 ký tự) | **Hợp lệ:** Tiếp nhận email 6 ký tự thành công. | **B15** |
| **43** | **TC_CHK_43** | Robustness BVA: Email khách hàng tại kề cận dưới min+ | • `customerEmail`: `"ab@b.co"` (7 ký tự) | **Hợp lệ:** Tiếp nhận email 7 ký tự thành công. | **B16** |
| **44** | **TC_CHK_44** | Robustness BVA: Email khách hàng tại kề cận trên max- | • `customerEmail`: Chuỗi email dài 127 ký tự | **Hợp lệ:** Tiếp nhận email 127 ký tự thành công. | **B17** |
| **45** | **TC_CHK_45** | Robustness BVA: Email khách hàng tại cận trên max | • `customerEmail`: Chuỗi email dài 128 ký tự | **Hợp lệ:** Tiếp nhận email 128 ký tự thành công. | **B18** |
| **46** | **TC_CHK_46** | Robustness BVA: Email khách hàng tại ngoại biên trên max+ | • `customerEmail`: Chuỗi email dài 129 ký tự | **Lỗi:** Báo lỗi email tối đa 128 ký tự. | **B19** |
| **47** | **TC_CHK_47** | Robustness BVA: Số điện thoại tại ngoại biên dưới min- | • `customerPhone`: `""` (0 ký tự, rỗng) | **Lỗi:** Báo lỗi số điện thoại không được để trống. | **B20** |
| **48** | **TC_CHK_48** | Robustness BVA: Số điện thoại tại cận dưới min | • `customerPhone`: `"1"` (1 ký tự) | **Hợp lệ:** Tiếp nhận SĐT 1 ký tự thành công. | **B21** |
| **49** | **TC_CHK_49** | Robustness BVA: Số điện thoại tại kề cận dưới min+ | • `customerPhone`: `"12"` (2 ký tự) | **Hợp lệ:** Tiếp nhận SĐT 2 ký tự thành công. | **B22** |
| **50** | **TC_CHK_50** | Robustness BVA: Số điện thoại tại kề cận trên max- | • `customerPhone`: Chuỗi 19 chữ số | **Hợp lệ:** Tiếp nhận SĐT 19 ký tự thành công. | **B23** |
| **51** | **TC_CHK_51** | Robustness BVA: Số điện thoại tại cận trên max | • `customerPhone`: Chuỗi 20 chữ số | **Hợp lệ:** Tiếp nhận SĐT 20 ký tự thành công. | **B24** |
| **52** | **TC_CHK_52** | Robustness BVA: Số điện thoại tại ngoại biên trên max+ | • `customerPhone`: Chuỗi 21 chữ số | **Lỗi:** Báo lỗi số điện thoại tối đa 20 ký tự. | **B25** |
| **53** | **TC_CHK_53** | Robustness BVA: Số lượng đặt hàng tại ngoại biên dưới min- | • `orderQuantity`: 0 (Không hợp lệ) | **Lỗi:** Báo lỗi số lượng đặt phải lớn hơn 0. | **B26** |
| **54** | **TC_CHK_54** | Robustness BVA: Số lượng đặt hàng tại cận dưới min | • `orderQuantity`: 1 sản phẩm | **Hợp lệ:** Tiếp nhận đặt mua 1 sản phẩm thành công. | **B27** |
| **55** | **TC_CHK_55** | Robustness BVA: Số lượng đặt hàng tại kề cận dưới min+ | • `orderQuantity`: 2 sản phẩm | **Hợp lệ:** Tiếp nhận đặt mua 2 sản phẩm thành công. | **B28** |
| **56** | **TC_CHK_56** | Robustness BVA: Số lượng đặt hàng tại kề cận trên max- | • `orderQuantity`: 9 sản phẩm (Tồn kho = 10) | **Hợp lệ:** Tiếp nhận đặt mua 9 sản phẩm thành công. | **B29** |
| **57** | **TC_CHK_57** | Robustness BVA: Số lượng đặt hàng tại cận trên max | • `orderQuantity`: 10 sản phẩm (Mua hết kho) | **Hợp lệ:** Tiếp nhận đặt mua 10 sản phẩm, tồn kho về 0. | **B30** |
| **58** | **TC_CHK_58** | Robustness BVA: Số lượng đặt hàng tại ngoại biên trên max+ | • `orderQuantity`: 11 sản phẩm (Vượt tồn kho 10) | **Lỗi:** Ném lỗi không đủ số lượng trong kho. | **B31** |
| **59** | **TC_CHK_59** | Decision Table: Rule 1 - Giỏ hàng null hoặc rỗng | • `cartLines`: `[]` (Rỗng) | **Lỗi:** Từ chối và chuyển hướng về `/shoppingCart`. | **D1** |
| **60** | **TC_CHK_60** | Decision Table: Rule 2 - Form thông tin giao hàng lỗi | • Form giao hàng thiếu trường bắt buộc hoặc sai định dạng | **Lỗi:** Giữ lại Bước 2 và hiển thị thông báo lỗi form. | **D2** |
| **61** | **TC_CHK_61** | Decision Table: Rule 3 - Dòng sản phẩm số lượng không hợp lệ | • Dòng hàng có `quantity <= 0` | **Lỗi:** Ném `IllegalArgumentException`, chặn tạo đơn. | **D3** |
| **62** | **TC_CHK_62** | Decision Table: Rule 4 - Sản phẩm ngừng kinh doanh | • Giỏ hàng chứa sản phẩm trạng thái `INACTIVE` | **Lỗi:** Ném `IllegalStateException: Sản phẩm ngừng bán`. | **D4** |
| **63** | **TC_CHK_63** | Decision Table: Rule 5 - Vượt quá số lượng tồn kho thực tế | • Số lượng đặt mua lớn hơn tồn kho trong CSDL | **Lỗi:** Ném `IllegalStateException: Không đủ tồn kho`. | **D5** |
| **64** | **TC_CHK_64** | Decision Table: Rule 6 - Mã giảm giá voucher không hợp lệ | • Voucher không tồn tại, hết hạn, hoặc không đủ điều kiện | **Lỗi:** Báo lỗi từ chối voucher trước khi tạo đơn. | **D6** |
| **65** | **TC_CHK_65** | Decision Table: Rule 7 - Luồng hoàn hảo đầy đủ điều kiện hợp lệ | • Tất cả điều kiện giỏ, form, kho, voucher đều hợp lệ | **Hợp lệ:** Tạo đơn hàng thành công, trừ kho, chuyển Bước 4 hoàn tất. | **D7** |
| **66** | **TC_CHK_66** | State Transition: ST1 - Bấm Tiến hành thanh toán khi giỏ có hàng | • Tại `Step 1: ShoppingCart` (có hàng) bấm 'Tiến hành đặt hàng' | **Chuyển trạng thái:** Điều hướng sang `Step 2: CustomerForm`. | **ST1** |
| **67** | **TC_CHK_67** | State Transition: ST2 - Chuyển bước khi giỏ hàng rỗng | • Tại `Step 1: ShoppingCart` (giỏ rỗng) cố truy cập thanh toán | **Chặn chuyển bước:** Tự động redirect về `/shoppingCart`. | **ST2** |
| **68** | **TC_CHK_68** | State Transition: ST3 - Submit form giao hàng bị lỗi/thiếu trường | • Tại `Step 2: CustomerForm` submit form thiếu thông tin | **Giữ trạng thái:** Giữ nguyên ở Bước 2, hiển thị cảnh báo lỗi. | **ST3** |
| **69** | **TC_CHK_69** | State Transition: ST4 - Submit form giao hàng hợp lệ | • Tại `Step 2: CustomerForm` điền đầy đủ thông tin hợp lệ | **Chuyển trạng thái:** Điều hướng sang `Step 3: Confirmation`. | **ST4** |
| **70** | **TC_CHK_70** | State Transition: ST5 - Bấm Quay lại từ màn hình xác nhận | • Tại `Step 3: Confirmation` bấm nút 'Quay lại' (Back) | **Chuyển trạng thái:** Quay về `Step 2: CustomerForm`, giữ nguyên dữ liệu. | **ST5** |
| **71** | **TC_CHK_71** | State Transition: ST6 - Hết tồn kho giữa chừng tại bước xác nhận | • Tại `Step 3: Confirmation` bấm đặt khi sản phẩm vừa bị mua hết | **Quay lại:** Báo lỗi thiếu tồn kho, quay về `Step 1: ShoppingCart`. | **ST6** |
| **72** | **TC_CHK_72** | State Transition: ST7 - Xác nhận đặt hàng thành công | • Tại `Step 3: Confirmation` bấm nút 'Xác nhận đặt hàng' | **Chuyển trạng thái:** Tạo đơn, trừ kho, điều hướng sang `Step 4: Finalize`. | **ST7** |
