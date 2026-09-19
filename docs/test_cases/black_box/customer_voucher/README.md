# THIẾT KẾ TEST CASE HỘP ĐEN: ÁP DỤNG MÃ GIẢM GIÁ (CUSTOMER VOUCHER APPLICATION)

> **Chức năng:** Kiểm tra tính hợp lệ và áp dụng mã giảm giá (Voucher) cho đơn hàng dành cho Khách hàng (`POST /api/v1/vouchers/apply` và `GET /api/v1/vouchers`).  
> **Người thực hiện:** Nguyễn Hoàng Phương (MSSV: `080205010954` / `NguyenHoangPhuong275`)

---

## 1. Xác Định Biến Đầu Vào & Ràng Buộc Nghiệp Vụ (Input Variables)

| Tên Biến | Ý Nghĩa | Kiểu | Ràng Buộc & Miền Giá Trị Hợp Lệ |
| :--- | :--- | :---: | :--- |
| **`voucherCode`** | Mã giảm giá người dùng nhập | `String` | Độ dài $[1, 50]$, không rỗng, tự động UPPERCASE, tồn tại trong CSDL |
| **`orderAmount`** | Tổng giá trị đơn hàng hiện tại | `double` | Số thực $> 0$ (ngưỡng nghiệp vụ $[500.0, 50.000.0]$ nghìn VNĐ) |
| **`discountPercent`**| Tỷ lệ chiết khấu theo % | `double` | Số thực trong đoạn $[1.0, 100.0]\%$ (đối với voucher % discount) |
| **`usedCount`** | Lượt dùng chung toàn hệ thống | `int` | $0 \le usedCount < usageLimit$ (ngưỡng tối đa 50 lượt) |
| **`userUsedCount`**| Lượt dùng cá nhân của User | `int` | $0 \le userUsedCount < perUserLimit$ (ngưỡng tối đa 2 lượt/khách) |
| **`active`** | Trạng thái kích hoạt của mã | `boolean` | Bắt buộc `true` (Hoạt động). Nếu `false` từ chối áp dụng |
| **`expiryDate`** | Hạn sử dụng của mã | `Date` | Ngày trong tương lai ($> \text{thời điểm hiện tại}$) hoặc `null` |
| **`currentUser`** | Trạng thái tài khoản người dùng | `User` | Khách vãng lai (`Guest`) hoặc Thành viên đã đăng nhập (`ROLE_USER`) |

---

## 2. Bảng Phân Hoạch Tương Đương (Equivalence Partitioning - EP)

| STT | Biến / Điều kiện | Lớp hợp lệ (Valid) | Tag | Lớp không hợp lệ (Invalid) | Tag |
| :-: | :--- | :--- | :---: | :--- | :---: |
| **1** | **Mã voucher (`voucherCode`)** | Mã tồn tại trong CSDL, `active = true` | **V1** | • Để trống hoặc rỗng (`null` / `""`)<br>• Mã không tồn tại trong CSDL<br>• Mã đã bị vô hiệu hóa (`active = false`) | **X1**<br>**X2**<br>**X3** |
| **2** | **Giá trị đơn hàng (`orderAmount`)**| $500.0 \le orderAmount \le 50.000.0$ k VNĐ | **V2** | • Chưa đạt mức tối thiểu ($orderAmount < 500.0$ k)<br>• Vượt trần giao dịch ($orderAmount > 50.000.0$ k) | **X4**<br>**X5** |
| **3** | **Chiết khấu (`discountPercent`)** | $1.0 \le discountPercent \le 100.0\%$ | **V3** | • Chiết khấu dưới mức tối thiểu ($< 1.0\%$)<br>• Vượt quá trần 100% ($> 100.0\%$) | **X6**<br>**X7** |
| **4** | **Lượt dùng chung (`usedCount`)** | $0 \le usedCount < usageLimit$ ($0 \to 49$) | **V4** | • Lượt dùng âm ($< 0$)<br>• Đã cạn lượt toàn hệ thống ($usedCount \ge 50$) | **X8**<br>**X9** |
| **5** | **Lượt dùng cá nhân (`userUsedCount`)**| $0 \le userUsedCount < perUserLimit$ ($0 \to 1$) | **V5** | • Lượt dùng âm ($< 0$)<br>• Đã hết lượt cá nhân ($userUsedCount \ge 2$) | **X10**<br>**X11** |
| **6** | **Hạn sử dụng (`expiryDate`)** | Ngày hết hạn còn hiệu lực ($> \text{now}$) | **V6** | Mã đã quá hạn sử dụng ($\le \text{now}$) | **X12** |
| **7** | **Đối tượng áp dụng (`currentUser`)** | Khách vãng lai (Guest) bỏ qua perUserLimit | **V7** | - | - |

---

## 3. Bảng Phân Tích Giá Trị Biên (Boundary Value Analysis - BVA)

### 3.1 Bảng 7 mốc giá trị biên Robustness BVA cho 4 biến định lượng

*(Ghi chú bộ giá trị danh định: `orderAmount` $nom = 25.000.0$ k VNĐ, `discountPercent` $nom = 20.0\%$, `usedCount` $nom = 25$ lượt, `userUsedCount` $nom = 1$ lượt).*

| Biến Định Lượng | Ngoại biên dưới ($min^-$) | Cận dưới ($min$) | Kề dưới ($min^+$) | Danh định ($nom$) | Kề trên ($max^-$) | Cận trên ($max$) | Ngoại biên trên ($max^+$) | Quy tắc & Giới hạn |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :--- |
| **1. Đơn hàng `orderAmount` (k)** | `499.0` | **`500.0`** | **`501.0`** | **`25.000.0`** | **`49.999.0`** | **`50.000.0`** | `50.001.0` | Miền $[500.0, 50.000.0]$ k VNĐ. Dưới 500k từ chối |
| **2. Tỷ lệ `discountPercent` (%)**| `0.9` | **`1.0`** | **`1.1`** | **`20.0`** | **`99.9`** | **`100.0`** | `100.1` | Miền $[1.0, 100.0]\%$. Ngoài khoảng báo lỗi |
| **3. Lượt chung `usedCount`** | `-1` | **`0`** | **`1`** | **`25`** | **`48`** | **`49`** | `50` | Giới hạn 50 lượt. $\ge 50$ báo hết lượt hệ thống |
| **4. Lượt cá nhân `userUsedCount`**| `-1` | **`0`** | **`1`** | **`1`** | **`1`** | **`1`** | `2` | Giới hạn 2 lượt/khách. $\ge 2$ báo hết lượt cá nhân |

---

### 3.2 Bảng 25 Ca Kiểm Thử Robustness BVA ($6n + 1 = 25$)

| Case | Đơn hàng (`orderAmount`) | Chiết khấu (`discount`) | Lượt chung (`used`) | Lượt cá nhân (`userUsed`) | Mốc kiểm thử | Kết quả mong đợi (Expected Output) | Tag Biên |
| :-: | :--- | :--- | :--- | :--- | :---: | :--- | :-: |
| **1** | `25.000.0k` *(nom)* | `20.0%` *(nom)* | `25` *(nom)* | `1` *(nom)* | **Tất cả ở nom** | **HTTP 200 OK:** Áp dụng mã thành công, giảm 5.000.000 VNĐ | **B1** |
| **2** | `499.0k` *(min-)* | `20.0%` | `25` | `1` | `amount = min-` | **HTTP 400 Bad Request:** Đơn hàng chưa đạt mức tối thiểu 500k | **B2** |
| **3** | `500.0k` *(min)* | `20.0%` | `25` | `1` | `amount = min` | **HTTP 200 OK:** Áp dụng thành công tại cận dưới tối thiểu | **B3** |
| **4** | `501.0k` *(min+)* | `20.0%` | `25` | `1` | `amount = min+` | **HTTP 200 OK:** Áp dụng thành công tại kề cận dưới | **B4** |
| **5** | `49.999.0k` *(max-)*| `20.0%` | `25` | `1` | `amount = max-` | **HTTP 200 OK:** Áp dụng thành công tại kề cận trên | **B5** |
| **6** | `50.000.0k` *(max)* | `20.0%` | `25` | `1` | `amount = max` | **HTTP 200 OK:** Áp dụng thành công tại cận trên tối đa | **B6** |
| **7** | `50.001.0k` *(max+)*| `20.0%` | `25` | `1` | `amount = max+` | **HTTP 400 Bad Request:** Đơn hàng vượt trần 50.000k VNĐ | **B7** |
| **8** | `25.000.0k` | `0.9%` *(min-)* | `25` | `1` | `discount = min-` | **HTTP 400 Bad Request:** Tỷ lệ chiết khấu nhỏ hơn 1% | **B8** |
| **9** | `25.000.0k` | `1.0%` *(min)* | `25` | `1` | `discount = min` | **HTTP 200 OK:** Áp dụng chiết khấu cận dưới 1% | **B9** |
| **10**| `25.000.0k` | `1.1%` *(min+)* | `25` | `1` | `discount = min+` | **HTTP 200 OK:** Áp dụng chiết khấu kề cận dưới 1.1% | **B10**|
| **11**| `25.000.0k` | `99.9%` *(max-)*| `25` | `1` | `discount = max-` | **HTTP 200 OK:** Áp dụng chiết khấu kề cận trên 99.9% | **B11**|
| **12**| `25.000.0k` | `100.0%` *(max)*| `25` | `1` | `discount = max` | **HTTP 200 OK:** Áp dụng chiết khấu tối đa 100% | **B12**|
| **13**| `25.000.0k` | `100.1%` *(max+)*| `25` | `1` | `discount = max+`| **HTTP 400 Bad Request:** Tỷ lệ chiết khấu vượt quá 100% | **B13**|
| **14**| `25.000.0k` | `20.0%` | `-1` *(min-)* | `1` | `used = min-` | **HTTP 400 Bad Request:** Lượt sử dụng âm không hợp lệ | **B14**|
| **15**| `25.000.0k` | `20.0%` | `0` *(min)* | `1` | `used = min` | **HTTP 200 OK:** Áp dụng thành công cho lượt dùng đầu tiên | **B15**|
| **16**| `25.000.0k` | `20.0%` | `1` *(min+)* | `1` | `used = min+` | **HTTP 200 OK:** Áp dụng thành công cho lượt dùng thứ 2 | **B16**|
| **17**| `25.000.0k` | `20.0%` | `48` *(max-)* | `1` | `used = max-` | **HTTP 200 OK:** Áp dụng thành công tại lượt dùng thứ 49 | **B17**|
| **18**| `25.000.0k` | `20.0%` | `49` *(max)* | `1` | `used = max` | **HTTP 200 OK:** Áp dụng thành công cho lượt dùng cuối cùng 50 | **B18**|
| **19**| `25.000.0k` | `20.0%` | `50` *(max+)* | `1` | `used = max+` | **HTTP 400 Bad Request:** Mã giảm giá đã hết số lượt sử dụng | **B19**|
| **20**| `25.000.0k` | `20.0%` | `25` | `-1` *(min-)* | `userUsed = min-` | **HTTP 400 Bad Request:** Lượt dùng cá nhân không hợp lệ | **B20**|
| **21**| `25.000.0k` | `20.0%` | `25` | `0` *(min)* | `userUsed = min` | **HTTP 200 OK:** Áp dụng thành công lần đầu cho tài khoản | **B21**|
| **22**| `25.000.0k` | `20.0%` | `25` | `1` *(max)* | `userUsed = max` | **HTTP 200 OK:** Áp dụng thành công lần thứ 2 (chạm hạn mức) | **B22**|
| **23**| `25.000.0k` | `20.0%` | `25` | `2` *(max+)* | `userUsed = max+` | **HTTP 400 Bad Request:** Tài khoản của bạn đã dùng hết lượt | **B23**|
| **24**| `25.000.0k` | `20.0%` | `25` | `Guest` | `user = Guest` | **HTTP 200 OK:** Khách vãng lai áp dụng thành công (bỏ qua hạn cá nhân) | **B24**|
| **25**| `25.000.0k` | `Fix: 50.0k` | `25` | `1` | `type = FIXED` | **HTTP 200 OK:** Áp dụng voucher giảm tiền cố định 50k thành công | **B25**|

---

## 4. Kỹ Thuật Bảng Quyết Định (Decision Table Testing)

### 4.1 Bảng Quyết Định Rút Gọn (Collapsed Decision Table - 8 Rules)

| Điều kiện & Hành động | Rule 1 | Rule 2 | Rule 3 | Rule 4 | Rule 5 | Rule 6 | Rule 7 | Rule 8 |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| **C1: Mã tồn tại trong CSDL?** | **N** | Y | Y | Y | Y | Y | Y | Y |
| **C2: Đang kích hoạt (`active = true`)?** | - | **N** | Y | Y | Y | Y | Y | Y |
| **C3: Còn hạn dùng (`date <= expiry`)?** | - | - | **N** | Y | Y | Y | Y | Y |
| **C4: Đạt đơn tối thiểu (`total >= min`)?** | - | - | - | **N** | Y | Y | Y | Y |
| **C5: Còn lượt chung (`used < limit`)?** | - | - | - | - | **N** | Y | Y | Y |
| **C6: Là Khách vãng lai (Guest)?** | - | - | - | - | - | **Y** | N | N |
| **C7: Còn lượt User (`userUsed < limit`)?** | - | - | - | - | - | - | **N** | **Y** |
| *A1: Báo lỗi 'Mã không tồn tại / vô hiệu' (HTTP 400)* | **X** | **X** | - | - | - | - | - | - |
| *A2: Báo lỗi 'Mã đã hết hạn sử dụng' (HTTP 400)* | - | - | **X** | - | - | - | - | - |
| *A3: Báo lỗi 'Chưa đạt đơn tối thiểu' (HTTP 400)* | - | - | - | **X** | - | - | - | - |
| *A4: Báo lỗi 'Hết lượt toàn hệ thống' (HTTP 400)* | - | - | - | - | **X** | - | - | - |
| *A5: Báo lỗi 'Tài khoản đã hết lượt' (HTTP 400)* | - | - | - | - | - | - | **X** | - |
| *A6: Áp dụng thành công (HTTP 200 OK)* | - | - | - | - | - | **X** | - | **X** |
| **Tag Bảng Quyết Định** | **D1** | **D2** | **D3** | **D4** | **D5** | **D6** | **D7** | **D8** |

*Giải thích quy tắc nghiệp vụ:*
- **Rule 1 (`D1`):** Mã voucher không tồn tại trong hệ thống $\to$ Báo lỗi `HTTP 400 Bad Request`.
- **Rule 2 (`D2`):** Mã voucher đã bị Quản trị viên tắt kích hoạt (`active = false`) $\to$ Báo lỗi `HTTP 400 Bad Request`.
- **Rule 3 (`D3`):** Mã voucher đã qua ngày hết hạn (`expiryDate < now`) $\to$ Báo lỗi `HTTP 400 Bad Request`.
- **Rule 4 (`D4`):** Tổng giá trị giỏ hàng chưa đạt giá trị tối thiểu của mã $\to$ Báo lỗi `HTTP 400 Bad Request`.
- **Rule 5 (`D5`):** Tổng số lượt dùng chung của mã đã chạm giới hạn (`usedCount >= usageLimit`) $\to$ Báo lỗi `HTTP 400 Bad Request`.
- **Rule 6 (`D6`):** Khách vãng lai (chưa đăng nhập) thỏa mãn mọi điều kiện $\to$ Bỏ qua kiểm tra perUserLimit, áp dụng thành công (`HTTP 200 OK`).
- **Rule 7 (`D7`):** Tài khoản đã đăng nhập đã dùng hết số lượt cá nhân cho phép $\to$ Báo lỗi `HTTP 400 Bad Request`.
- **Rule 8 (`D8`):** Tài khoản đã đăng nhập thỏa mãn mọi điều kiện và còn lượt cá nhân $\to$ Áp dụng thành công (`HTTP 200 OK`).

---

## 5. Kỹ Thuật Kiểm Thử Chuyển Đổi Trạng Thái (State Transition Testing - STT)

### 5.1 Sơ đồ chuyển đổi trạng thái áp dụng Voucher

```mermaid
stateDiagram-v2
    [*] --> S0_NoVoucher : Giỏ hàng ban đầu (Chưa có voucher)
    S0_NoVoucher --> S1_VoucherApplied : ST1: Áp dụng mã hợp lệ thành công
    S1_VoucherApplied --> S1_VoucherApplied : ST2: Đổi sang mã voucher khác hợp lệ
    S0_NoVoucher --> S2_LimitExhausted : ST3: Nhập mã đã cạn lượt dùng (Chặn lỗi)
    S0_NoVoucher --> S3_VoucherExpired : ST4: Nhập mã đã hết hạn sử dụng (Chặn lỗi)
    S1_VoucherApplied --> S4_VoucherRemoved : ST5: Khách bấm Hủy/Gỡ mã khỏi giỏ
    S4_VoucherRemoved --> S1_VoucherApplied : ST1: Nhập lại mã mới
    S1_VoucherApplied --> [*] : Chốt đơn hàng hoàn tất (Order Checkout)
```

### 5.2 Bảng phân tích chi tiết các Ca chuyển đổi trạng thái (State Transition Details)

| Mã Transition | Trạng thái ban đầu | Hành động / Sự kiện | Trạng thái kế tiếp | Kết quả xử lý hệ thống | Tag |
| :---: | :--- | :--- | :--- | :--- | :---: |
| **ST_01** | `S0_NoVoucher` | Gửi `POST /apply` với mã hợp lệ | `S1_VoucherApplied` | Giảm trừ tiền thành công, gán voucher vào Session Cart | **ST1** |
| **ST_02** | `S1_VoucherApplied` | Gửi `POST /apply` với mã hợp lệ khác | `S1_VoucherApplied` | Ghi đè mã mới, tính lại mức giảm tương ứng | **ST2** |
| **ST_03** | `S0_NoVoucher` | Gửi `POST /apply` với mã hết lượt | `S2_LimitExhausted` | Giữ nguyên giỏ hàng, thông báo lỗi hết lượt | **ST3** |
| **ST_04** | `S0_NoVoucher` | Gửi `POST /apply` với mã hết hạn | `S3_VoucherExpired` | Giữ nguyên giỏ hàng, thông báo lỗi hết hạn | **ST4** |
| **ST_05** | `S1_VoucherApplied` | Xóa mã giảm giá khỏi giỏ hàng | `S4_VoucherRemoved` | Xóa `voucherCode`, đưa `discountAmount = 0`, khôi phục giá gốc | **ST5** |

---

## 6. Thiết Kế Bảng Test Cases Chi Tiết Triển Khai (Tối Ưu & Đầy Đủ Bao Phủ)

| STT | Mã Test Case | Tên ca kiểm thử | Dữ liệu kiểm thử (Test Input Data) | Kết quả mong đợi (Expected Output) | Tag được bao phủ |
| :-: | :--- | :--- | :--- | :--- | :---: |
| **1** | **TC_VOU_01** | Áp dụng thành công mã voucher % với dữ liệu danh định | • `voucherCode`: `"SALE20"` (Giảm 20%, max 50k)<br>• `orderAmount`: `25.000.000đ` (nom)<br>• `currentUser`: `employee1` (userUsed = 0) | **HTTP 200 OK:** Áp dụng thành công, chiết khấu `50.000đ` (chạm trần maxDiscount), lưu vào session cart. | **V1, V2, V3, V4, V5, V6, B1, D8, ST1** |
| **2** | **TC_VOU_02** | Áp dụng thành công mã voucher tiền mặt cố định (FIXED) | • `voucherCode`: `"FIXED50"` (Giảm 50.000đ)<br>• `orderAmount`: `1.000.000đ`<br>• `currentUser`: `employee1` | **HTTP 200 OK:** Áp dụng thành công, trừ đúng `50.000đ` vào tổng tiền thanh toán. | **V1, V2, B25, D8, ST1** |
| **3** | **TC_VOU_03** | Khách vãng lai (Guest) áp dụng mã voucher thành công | • `currentUser`: `null` (Khách vãng lai)<br>• `voucherCode`: `"SALE10"`, `orderAmount`: `1.000.000đ` | **HTTP 200 OK:** Bỏ qua kiểm tra giới hạn tài khoản, giảm `100.000đ` thành công. | **V1, V7, B24, D6, ST1** |
| **4** | **TC_VOU_04** | Áp dụng thành công tại cận dưới đơn hàng ($min = 500k$) | • `voucherCode`: `"MIN500"`, `orderAmount`: `500.000đ`<br>• `minOrderValue`: `500.000đ` | **HTTP 200 OK:** Thỏa mãn ngưỡng đơn tối thiểu tại cận dưới, áp dụng thành công. | **V2, B3, D8** |
| **5** | **TC_VOU_05** | Áp dụng thành công tại cận trên đơn hàng ($max = 50.000k$) | • `voucherCode`: `"SALE10"`, `orderAmount`: `50.000.000đ`<br>• `currentUser`: `employee1` | **HTTP 200 OK:** Tiếp nhận đơn hàng lớn tại cận trên tối đa, tính giảm giá chính xác. | **V2, B6, D8** |
| **6** | **TC_VOU_06** | Áp dụng thành công tại chiết khấu cận dưới ($min = 1\%$) | • `voucherCode`: `"MIN1PCT"` (Giảm 1%)<br>• `orderAmount`: `25.000.000đ` | **HTTP 200 OK:** Giảm `250.000đ` (1% giá trị đơn hàng), áp dụng thành công. | **V3, B9, D8** |
| **7** | **TC_VOU_07** | Áp dụng thành công tại chiết khấu tối đa ($max = 100\%$) | • `voucherCode`: `"FREE100"` (Giảm 100%, max 100k)<br>• `orderAmount`: `100.000đ` | **HTTP 200 OK:** Giảm toàn bộ `100.000đ`, tổng tiền phải trả bằng `0đ`. | **V3, B12, D8** |
| **8** | **TC_VOU_08** | Áp dụng thành công tại lượt dùng chung cuối cùng ($used = 49$) | • `voucherCode`: `"LASTCALL"` (Limit = 50, used = 49)<br>• `orderAmount`: `1.000.000đ` | **HTTP 200 OK:** Tiếp nhận lượt sử dụng thứ 50 (hết lượt sau ca này). | **V4, B18, D8** |
| **9** | **TC_VOU_09** | Áp dụng thành công tại lượt dùng cá nhân thứ 2 ($userUsed = 1$) | • `voucherCode`: `"VIP2TIMES"` (PerUserLimit = 2, userUsed = 1)<br>• `currentUser`: `employee1` | **HTTP 200 OK:** Cho phép áp dụng lượt cuối cùng dành cho tài khoản. | **V5, B22, D8** |
| **10**| **TC_VOU_10** | Thay đổi voucher trong giỏ sang mã khác (Ghi đè mã mới) | • Giỏ hàng đang có mã `"SALE10"`<br>• Gửi `POST /apply` mã mới `"SALE20"` | **HTTP 200 OK:** Hủy mã cũ, áp dụng mã mới, cập nhật lại tiền giảm chính xác. | **V1, ST2** |
| **11**| **TC_VOU_11** | Báo lỗi khi để trống mã giảm giá ($min^-$) | • `voucherCode`: `""` (Rỗng hoặc toàn khoảng trắng) | **HTTP 400 Bad Request:** Thông báo lỗi: *"Vui lòng nhập mã giảm giá!"*. | **X1** |
| **12**| **TC_VOU_12** | Báo lỗi khi mã giảm giá không tồn tại trong CSDL | • `voucherCode`: `"NOT_EXIST_CODE_999"` | **HTTP 400 Bad Request:** *"Mã giảm giá không tồn tại hoặc đã bị vô hiệu hóa!"*. | **X2, D1** |
| **13**| **TC_VOU_13** | Báo lỗi khi mã giảm giá đã bị vô hiệu hóa (`active = false`) | • `voucherCode`: `"INACTIVE_VOUCHER"` (`active = false`) | **HTTP 400 Bad Request:** *"Mã giảm giá không tồn tại hoặc đã bị vô hiệu hóa!"*. | **X3, D2** |
| **14**| **TC_VOU_14** | Báo lỗi khi đơn hàng chưa đạt giá trị tối thiểu ($min^-$) | • `voucherCode`: `"MIN500"` (Đơn tối thiểu 500k)<br>• `orderAmount`: `499.000đ` ($min^-$) | **HTTP 400 Bad Request:** *"Đơn hàng tối thiểu phải từ 500.000 ₫ để áp dụng mã này!"*. | **X4, B2, D4** |
| **15**| **TC_VOU_15** | Báo lỗi khi đơn hàng vượt trần quy định ($max^+$) | • `voucherCode`: `"SALE10"`<br>• `orderAmount`: `50.001.000đ` ($max^+$) | **HTTP 400 Bad Request:** *"Giá trị đơn hàng vượt quá ngưỡng xử lý!"*. | **X5, B7** |
| **16**| **TC_VOU_16** | Báo lỗi khi mã đã cạn số lượt toàn hệ thống ($max^+$) | • `voucherCode`: `"SOLDOUT"` (`usedCount = 50`, `limit = 50`) | **HTTP 400 Bad Request:** *"Mã giảm giá đã hết số lượt sử dụng!"*. | **X9, B19, D5, ST3** |
| **17**| **TC_VOU_17** | Báo lỗi khi tài khoản đã dùng hết hạn mức cá nhân ($max^+$) | • `voucherCode`: `"ONCE"` (`userUsedCount = 2`, `perUserLimit = 2`)<br>• `currentUser`: `employee1` | **HTTP 400 Bad Request:** *"Tài khoản của bạn đã dùng hết số lượt cho phép (2 lần) đối với mã này!"*. | **X11, B23, D7** |
| **18**| **TC_VOU_18** | Báo lỗi khi mã giảm giá đã hết hạn sử dụng | • `voucherCode`: `"EXPIRED_VOU"` (`expiryDate < now`) | **HTTP 400 Bad Request:** *"Mã giảm giá đã hết hạn sử dụng!"*. | **X12, D3, ST4** |
| **19**| **TC_VOU_19** | Hủy/Gỡ mã giảm giá khỏi giỏ hàng thành công | • Giỏ hàng đang có mã áp dụng<br>• Người dùng bấm Gỡ mã hoặc gửi payload rỗng | **HTTP 200 OK:** Gỡ mã thành công, đưa `discountAmount` về `0đ`, khôi phục giá ban đầu. | **ST5** |
| **20**| **TC_VOU_20** | Lấy danh sách các mã giảm giá đang kích hoạt (Customer API) | • Thao tác: Gọi `GET /api/v1/vouchers` | **HTTP 200 OK:** Trả về danh sách JSON các voucher còn hạn, đang active để người dùng chọn. | **V1, V6** |

---

## 7. Ma Trận Truy Vết & Độ Bao Phủ Kiểm Thử (Traceability Matrix)

| Nhóm Kỹ Thuật | Tổng Số Thẻ (Tags) | Danh Sách Thẻ Định Danh | Tỷ Lệ Bao Phủ |
| :--- | :---: | :--- | :---: |
| **Phân hoạch tương đương (EP)** | **19 Tags** | Hợp lệ: `V1` $\to$ `V7` (7 tags)<br>Không hợp lệ: `X1` $\to$ `X12` (12 tags) | **100% (19/19)** |
| **Phân tích giá trị biên (BVA)** | **25 Tags** | Robustness BVA: `B1` $\to$ `B25` (25 tags) | **100% (25/25)** |
| **Bảng quyết định (Decision Table)**| **8 Rules** | `D1, D2, D3, D4, D5, D6, D7, D8` | **100% (8/8)** |
| **Chuyển đổi trạng thái (State Transition)**| **5 Steps** | `ST1, ST2, ST3, ST4, ST5` | **100% (5/5)** |

---

## 8. Hướng Dẫn Thực Thi Với Postman & Newman (Execution Guide)

### 8.1 Chạy trực tiếp trên ứng dụng Postman (Desktop App)
1. Khởi động ứng dụng **Postman**.
2. Chọn **Import** $\to$ Chọn 2 tệp kịch bản kiểm thử:
   - Collection: [`Customer_Voucher_Postman_Collection.json`](file:///d:/LapTrinhAI/Testing/docs/test_cases/black_box/customer_voucher/Customer_Voucher_Postman_Collection.json)
   - Environment: [`Customer_Voucher_Postman_Environment.json`](file:///d:/LapTrinhAI/Testing/docs/test_cases/black_box/customer_voucher/Customer_Voucher_Postman_Environment.json)
3. Chọn môi trường `Shoeshop Customer Voucher Env`.
4. Nhấn vào Collection $\to$ Chọn **Run Collection** để thực thi tự động toàn bộ 20 test cases (`TC_VOU_01` $\to$ `TC_VOU_20`).

### 8.2 Chạy tự động qua dòng lệnh (CLI) bằng Newman
Thực thi lệnh sau tại thư mục gốc dự án:
```powershell
npx --yes newman run docs/test_cases/black_box/customer_voucher/Customer_Voucher_Postman_Collection.json `
  -e docs/test_cases/black_box/customer_voucher/Customer_Voucher_Postman_Environment.json `
  -r 'cli,htmlextra' `
  --reporter-htmlextra-export target/newman-customer-voucher-report.html `
  --insecure
```

