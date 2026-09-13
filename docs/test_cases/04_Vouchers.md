# Bảng Test Case: Chức năng 4 - Quản lý và Áp dụng Mã giảm giá (Vouchers)
**Người thực hiện:** Được 

## 1. Thông tin Kỹ thuật & Thực thi
- **Kỹ thuật Thiết kế (Test Design):** 
  - Phân hoạch lớp tương đương (EP)
  - Phân tích giá trị biên (BVA)
  - Bảng quyết định (Decision Table). Sử dụng để tối ưu hóa tổ hợp các ràng buộc validation phức tạp.
- **Kỹ thuật Thực thi (Test Execution):** 
  - Kiểm thử Tích hợp (Integration Test) & Đơn vị (Unit Test) qua JUnit / Mockito.
  - Kiểm thử API End-to-End (Black-box E2E API Testing) qua Postman.
- **File Code Thực thi (Automation Script):** 
  - Backend Logic: `src/test/java/com/example/demo/VoucherTests.java` và `dao/VoucherDAOTest.java`
  - Postman API: `docs/Shoeshop_API_Collection.json`

---

## 2. Phân tích Kỹ thuật Thiết kế (Test Design Analysis)

Theo chuẩn ISTQB, chức năng Voucher áp dụng đồng thời 3 kỹ thuật. Dưới đây là phân tích chi tiết cho từng kỹ thuật:


### 2.1 Bảng Phân hoạch lớp tương đương (Equivalence Partitioning - EP)

| Biến đầu vào / Điều kiện | Lớp tương đương Hợp lệ | Tag | Lớp tương đương Không hợp lệ | Tag |
| :--- | :--- | :---: | :--- | :---: |
| **Mã Voucher (`voucherCode`)** | Mã tồn tại trong CSDL | **V1** | Mã không tồn tại / Mã rác | **X1** |
| **Trạng thái kích hoạt (`active`)** | Đang kích hoạt (`active = true`) | **V2** | Bị vô hiệu hóa (`active = false`) | **X2** |
| **Hạn sử dụng (`expirationDate`)** | Ngày áp dụng ≤ Ngày hết hạn | **V3** | Đã quá ngày hết hạn (date > expiry) | **X3** |
| **Đơn tối thiểu (`minOrderValue`)** | Tổng đơn ≥ minOrderValue | **V4** | Tổng đơn < minOrderValue | **X4** |
| **Lượt dùng chung (`usageLimit`)** | Lượt đã dùng < usageLimit | **V5** | Đã hết lượt (usedCount ≥ usageLimit) | **X5** |
| **Lượt dùng cá nhân (`perUserLimit`)**| Khách đăng nhập: used < perUserLimit<br>Khách vãng lai: Không ràng buộc | **V6**<br>**V7** | Khách đăng nhập: used ≥ perUserLimit | **X6** |
| **Hình thức giảm giá (`discountType`)**| Giảm theo % (`PERCENT`) có chặn trần<br>Giảm trừ tiền cứng (`FIXED`) | **V8**<br>**V9** | N/A | |
| **Quyền Admin CRUD (`role`)** | Tài khoản có quyền `ROLE_ADMIN` | **V10** | Tài khoản không có quyền Admin | **X7** |


### 2.2 Bảng Phân tích giá trị biên (Standard Boundary Value Analysis - BVA)

| Biến đầu vào | Miền hợp lệ | min | min+ | nominal | max- | max | Tag biên |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :--- |
| **Đơn hàng tối thiểu (`orderAmount`)** | $[500, 10000]$ đô | 500 | 501 | 1500 | 9999 | 10000 | **B1, B2, B3, B4, B5** |
| **Lượt dùng chung (`usedCount`)** | $[0, 49]$ lượt *(Limit=50)* | 0 | 1 | 25 | 48 | 49 | **B6, B7, B8, B9, B10** |
| **Lượt dùng cá nhân (`userUsedCount`)**| $[0, 1]$ lượt *(Limit=2)* | 0 | 1 | 1 | 1 | 1 | **B11, B12, B13, B14, B15** |
| **Tỷ lệ chiết khấu % (`percent`)** | $[1, 100]$ % | 1 | 2 | 20 | 99 | 100 | **B16, B17, B18, B19, B20** |

*Ghi chú mở rộng (Robustness BVA):*
- Giá trị dưới biên của đơn tối thiểu: $orderAmount = 499$ đô (Tag **B0** / min-1) -> Báo lỗi chưa đạt mốc tối thiểu.
- Giá trị ngoài biên của lượt dùng chung: $usedCount = 50$ (Tag **B10+1**) -> Báo lỗi hết lượt dùng toàn hệ thống.
- Giá trị ngoài biên của lượt dùng cá nhân: $userUsedCount = 2$ (Tag **B15+1**) -> Báo lỗi hết lượt cá nhân.


### 2.3 Bảng Quyết định tổng hợp (Collapsed Decision Table)
Gộp các vùng dữ liệu trên vào Ma trận Quyết định để che phủ luồng Áp dụng Voucher (Luật từ chối theo thứ tự ưu tiên của Backend):

| Condition/Action | R1 | R2 | R3 | R4 | R5 | R6 | R7 | R8 |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| **C1: Tồn tại trong DB?** | N | Y | Y | Y | Y | Y | Y | Y |
| **C2: Trạng thái Active?** | - | N | Y | Y | Y | Y | Y | Y |
| **C3: Còn hạn sử dụng?** | - | - | N | Y | Y | Y | Y | Y |
| **C4: Đạt đơn tối thiểu?** | - | - | - | N | Y | Y | Y | Y |
| **C5: Còn lượt Global?** | - | - | - | - | N | Y | Y | Y |
| **C6: Là Khách vãng lai?** | - | - | - | - | - | Y | N | N |
| **C7: Còn lượt Cá nhân?** | - | - | - | - | - | - | N | Y |
| **A1: Báo lỗi Không tồn tại** | X | - | - | - | - | - | - | - |
| **A2: Báo lỗi Vô hiệu hóa** | - | X | - | - | - | - | - | - |
| **A3: Báo lỗi Quá hạn** | - | - | X | - | - | - | - | - |
| **A4: Báo lỗi Chưa đạt mốc** | - | - | - | X | - | - | - | - |
| **A5: Báo lỗi Hết lượt Global**| - | - | - | - | X | - | - | - |
| **A6: Báo lỗi Hết lượt Cá nhân**| - | - | - | - | - | - | X | - |
| **A7: Áp dụng thành công** | - | - | - | - | - | X | - | X |
| **Test Case Tương ứng** | TC_VOU_005 | TC_VOU_009 | TC_VOU_003 | TC_VOU_002 | TC_VOU_004 | TC_VOU_008 | TC_VOU_007 | TC_VOU_001, 006 |

---


## 3. Bảng Test Case Chi Tiết (Nghiệp vụ Áp Mã & API Admin)

| Mã kiểm thử | Kỹ thuật áp dụng | Tiêu đề | Điều kiện tiên quyết | Các bước kiểm tra | Dữ liệu kiểm thử | Kết quả dự kiến | Tag được bao phủ | Kết quả thực tế | Trạng thái |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :---: | :--- | :---: |
| **TC_VOU_001** | Bảng QĐ (R8) / EP / BVA | Kiểm tra áp dụng thành công mã hợp lệ (Giảm %) | Giỏ hàng 500 đô. Khách hàng đã Đăng nhập. | Nhập mã Voucher và Áp dụng. | `TESTPERCENT20` (Giảm 20%, max 50, MinOrder=100) | Hệ thống báo thành công. Tiền giảm chặn ở 50 đô (thay vì 100 đô). Hóa đơn 450 đô. | **V1, V2, V3, V4, V5, V6, V8, B1, B6, B18** | Khớp với Unit Test. | Pass |
| **TC_VOU_002** | Bảng QĐ (R4) / BVA | Kiểm tra chặn áp mã khi hóa đơn chưa đạt Min Order Value | Giỏ hàng 200 đô. | Nhập mã Voucher và Áp dụng. | `TESTMINORDER` (Yêu cầu hóa đơn từ 500 đô). | Báo lỗi chứa cụm từ "tối thiểu". Tiền giảm 0. | **X4, B0** | Khớp với Unit Test. | Pass |
| **TC_VOU_003** | Bảng QĐ (R3) / EP | Kiểm tra chặn áp mã khi Voucher đã quá hạn (Expired) | Giỏ hàng 200 đô, đạt Min Order. | Nhập mã Voucher và Áp dụng. | `TESTEXPIRED` (Bị lùi ngày hết hạn về 5 ngày trước). | Báo lỗi chứa cụm từ "hết hạn". Tiền giảm 0. | **X3** | Khớp với Unit Test. | Pass |
| **TC_VOU_004** | Bảng QĐ (R5) / BVA | Kiểm tra chặn áp mã khi Voucher cạn lượt chung (Usage Limit) | Giỏ hàng 200 đô. | Nhập mã Voucher và Áp dụng. | `TESTLIMITREJECT` (UsageLimit=1, đã bị xài 1 lần). | Báo lỗi chứa cụm từ "hết số lượt". Tiền giảm 0. | **X5, B10+1** | Khớp với Unit Test. | Pass |
| **TC_VOU_005** | Bảng QĐ (R1) / EP | Kiểm tra hệ thống chặn mã rác / mã không tồn tại | Giỏ hàng hợp lệ. | Nhập mã Voucher rác. | `MISSING` (Code không có trong DB). | Báo lỗi Mã giảm giá không tồn tại. | **X1** | Khớp với Unit Test. | Pass |
| **TC_VOU_006** | Bảng QĐ (R8) / EP | Kiểm tra áp dụng thành công mã hợp lệ (Trừ tiền cứng) | Giỏ hàng 200 đô. | Nhập mã Voucher và Áp dụng. | `TESTFIXED30` (Loại trừ tiền cứng 30 đô). | Thành công. Tiền giảm đúng 30 đô. | **V1, V2, V3, V4, V5, V9, B1** | Khớp với Unit Test. | Pass |
| **TC_VOU_007** | Bảng QĐ (R7) / BVA | Kiểm tra chặn áp mã do giới hạn Cá nhân (Per User Limit) | User `alice` có đơn hàng 100 đô. | Gọi API áp dụng Voucher cho User `alice`. | Mã `SALE10` (Giới hạn cá nhân = 2, `alice` đã xài 2 lần). | API từ chối áp mã. Báo lỗi "đã dùng hết số lượt cho phép". | **X6, B15+1** | Khớp với `VoucherDAOTest`. | Pass |
| **TC_VOU_008** | Bảng QĐ (R6) / EP | Khách vãng lai (Guest) không bị ràng buộc giới hạn cá nhân | Khách vãng lai (username trống/null). | Gọi API áp dụng Voucher. | Mã `SALE10` (Giới hạn cá nhân = 0). | API cho phép áp mã thành công. | **V1, V2, V3, V4, V5, V7, V8** | Khớp với `VoucherDAOTest`. | Pass |
| **TC_VOU_009** | Bảng QĐ (R2) / EP | Kiểm tra chặn áp mã đã bị khóa (Inactive) | Giỏ hàng hợp lệ. | Cố tình nhập mã đã bị khóa. | Mã `SALE10` có trường `active = false`. | Báo lỗi "Mã giảm giá không tồn tại hoặc đã bị vô hiệu hóa". | **X2** | Khớp với `VoucherDAOTest`. | Pass |
| **TC_VOU_010** | EP / CRUD | Admin tạo mã giảm giá mới qua API (Create) | Tài khoản Admin. | Gửi `POST /api/v1/admin/vouchers` với body hợp lệ. | Body chứa `code=NEWYEAR`, `discountValue=20`, v.v... | Server trả về 200 OK. Mã mới xuất hiện trong DB. | **V10** | Khớp API Postman. | Pass |
| **TC_VOU_011** | EP / CRUD | Admin vô hiệu hóa mã giảm giá qua API (Deactivate) | Tài khoản Admin. | Gửi `DELETE /api/v1/admin/vouchers/SALE10`. | Endpoint đi kèm Code voucher hợp lệ. | Trả về 200 OK. Cột `active` chuyển thành `false`. | **V10** | Khớp API Postman. | Pass |
| **TC_VOU_012** | EP / CRUD | Khách hàng lấy danh sách Voucher còn hiệu lực (List Active) | Không cần phân quyền. | Gửi `GET /api/v1/vouchers`. | Bắn Request GET đơn giản. | Trả về danh sách chứa các Voucher thỏa mãn: active=true, còn hạn, còn lượt sử dụng. | **V1, V2, V3, V5** | Khớp API Postman. | Pass |


---

## 4. Bảng Đối Chiếu & Ý Nghĩa Nhãn Tag (Tag Traceability Legend)

| Nhóm Tag | Mã Tag | Ý nghĩa nghiệp vụ | Trạng thái |
| :---: | :---: | :--- | :---: |
| **Valid EP** | **V1** | Mã voucher tồn tại trong CSDL | Hợp lệ |
| | **V2** | Trạng thái kích hoạt (active = true) | Hợp lệ |
| | **V3** | Voucher còn hạn sử dụng | Hợp lệ |
| | **V4** | Tổng đơn hàng đạt giá trị tối thiểu (orderAmount >= minOrderValue) | Hợp lệ |
| | **V5** | Lượt dùng toàn hệ thống còn khả dụng (usedCount < usageLimit) | Hợp lệ |
| | **V6** | Khách hàng đã đăng nhập chưa vượt giới hạn cá nhân | Hợp lệ |
| | **V7** | Khách vãng lai không bị kiểm tra giới hạn cá nhân | Hợp lệ |
| | **V8** | Hình thức chiết khấu phần trăm (PERCENT) | Hợp lệ |
| | **V9** | Hình thức chiết khấu số tiền cố định (FIXED) | Hợp lệ |
| | **V10**| Quyền hạn Quản trị viên (ROLE_ADMIN) quản lý mã | Hợp lệ |
| **Invalid EP**| **X1** | Mã voucher không tồn tại trong CSDL | Không hợp lệ |
| | **X2** | Mã voucher đã bị vô hiệu hóa (active = false) | Không hợp lệ |
| | **X3** | Mã voucher đã hết hạn sử dụng | Không hợp lệ |
| | **X4** | Đơn hàng chưa đạt giá trị tối thiểu | Không hợp lệ |
| | **X5** | Mã voucher đã hết số lượt sử dụng toàn hệ thống | Không hợp lệ |
| | **X6** | Người dùng đã sử dụng hết số lượt cá nhân cho phép | Không hợp lệ |
| | **X7** | Người dùng không có quyền Admin cố tình gọi API quản trị | Không hợp lệ |
| **Boundary** | **B1 - B5**| Điểm biên đơn tối thiểu: min (500), min+ (501), nom (1500), max- (9999), max (10000) | Hợp lệ |
| | **B0** | Điểm ngoài biên dưới đơn tối thiểu: orderAmount = 499 đô | Không hợp lệ |
| | **B6 - B10**| Điểm biên lượt dùng chung: min (0), min+ (1), nom (25), max- (48), max (49) | Hợp lệ |
| | **B10+1** | Điểm ngoài biên trên lượt dùng chung: usedCount = 50 | Không hợp lệ |
| | **B11 - B15**| Điểm biên lượt dùng cá nhân: min (0), min+ (1), nom (1), max- (1), max (1) | Hợp lệ |
| | **B15+1** | Điểm ngoài biên trên lượt cá nhân: userUsedCount = 2 | Không hợp lệ |
| | **B16 - B20**| Điểm biên phần trăm giảm: min (1), min+ (2), nom (20), max- (99), max (100) | Hợp lệ |
