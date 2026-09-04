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

### 2.1 Bảng Phân hoạch lớp tương đương (EP)

| STT | Trường hợp của điều kiện đầu vào | Lớp tương đương Hợp lệ | Lớp tương đương Không hợp lệ |
| :---: | :--- | :--- | :--- |
| 1 | Sự tồn tại của mã | Mã có trong DB | Mã rác (Không có trong DB) |
| 2 | Trạng thái kích hoạt | Bật (Active = True) | Bị vô hiệu hóa (Active = False) |
| 3 | Tình trạng hạn sử dụng | Còn hạn sử dụng | Quá hạn sử dụng |
| 4 | Tổng giá trị đơn hàng | Đạt mốc tối thiểu (Min Order Value) | Chưa đạt mốc tối thiểu |
| 5 | Số lượt sử dụng toàn cục | Còn lượt (< Usage Limit) | Đã hết lượt (>= Usage Limit) |
| 6 | Vai trò Người dùng | Khách hàng Đăng nhập, Khách vãng lai (Guest) | N/A |
| 7 | Số lượt dùng cá nhân (Tài khoản) | Chưa dùng hết (< Per User Limit) | Đã dùng quá giới hạn quy định |

### 2.2 Bảng Phân tích giá trị biên (BVA)
**Ràng buộc 1 (Đơn hàng):** Tổng tiền $\ge$ Min Order Value (Giả sử yêu cầu 500 đô).
**Ràng buộc 2 (Tổng lượt):** Số lượt xài chung $<$ Usage Limit (Giả sử giới hạn 50 lượt).
**Ràng buộc 3 (Cá nhân):** Lượt xài user $<$ Per User Limit (Giả sử mỗi người 2 lượt).

| Case | Biến số | Giá trị | Phân loại BVA | Kết quả dự kiến (Expected Output) |
| :---: | :--- | :---: | :---: | :--- |
| 1 | Tổng đơn hàng | 200 đô | min - 1 (Invalid) | Báo lỗi 400 (Chưa đạt giá trị tối thiểu) |
| 2 | Tổng đơn hàng | 500 đô | min (Valid) | Thành công (Áp dụng giảm giá) |
| 3 | Số lượt xài chung | 49 lượt | max - 1 (Valid) | Thành công (Áp dụng giảm giá) |
| 4 | Số lượt xài chung | 50 lượt | max (Invalid) | Báo lỗi 400 (Đã hết lượt sử dụng) |
| 5 | Lượt xài user | 1 lượt | max - 1 (Valid) | Thành công (Áp dụng giảm giá) |
| 6 | Lượt xài user | 2 lượt | max (Invalid) | Báo lỗi 400 (Bạn đã dùng hết số lượt) |

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

| Mã kiểm thử | Kỹ thuật áp dụng | Tiêu đề | Điều kiện tiên quyết | Các bước kiểm tra | Dữ liệu kiểm thử | Kết quả dự kiến | Kết quả thực tế | Trạng thái |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **TC_VOU_001** | Bảng QĐ (R8) / EP / BVA | Kiểm tra áp dụng thành công mã hợp lệ (Giảm %) | Giỏ hàng 500 đô. Khách hàng đã Đăng nhập. | Nhập mã Voucher và Áp dụng. | `TESTPERCENT20` (Giảm 20%, max 50, MinOrder=100) | Hệ thống báo thành công. Tiền giảm chặn ở 50 đô (thay vì 100 đô). Hóa đơn 450 đô. | Khớp với Unit Test. | Pass |
| **TC_VOU_002** | Bảng QĐ (R4) / BVA | Kiểm tra chặn áp mã khi hóa đơn chưa đạt Min Order Value | Giỏ hàng 200 đô. | Nhập mã Voucher và Áp dụng. | `TESTMINORDER` (Yêu cầu hóa đơn từ 500 đô). | Báo lỗi chứa cụm từ "tối thiểu". Tiền giảm 0. | Khớp với Unit Test. | Pass |
| **TC_VOU_003** | Bảng QĐ (R3) / EP | Kiểm tra chặn áp mã khi Voucher đã quá hạn (Expired) | Giỏ hàng 200 đô, đạt Min Order. | Nhập mã Voucher và Áp dụng. | `TESTEXPIRED` (Bị lùi ngày hết hạn về 5 ngày trước). | Báo lỗi chứa cụm từ "hết hạn". Tiền giảm 0. | Khớp với Unit Test. | Pass |
| **TC_VOU_004** | Bảng QĐ (R5) / BVA | Kiểm tra chặn áp mã khi Voucher cạn lượt chung (Usage Limit) | Giỏ hàng 200 đô. | Nhập mã Voucher và Áp dụng. | `TESTLIMITREJECT` (UsageLimit=1, đã bị xài 1 lần). | Báo lỗi chứa cụm từ "hết số lượt". Tiền giảm 0. | Khớp với Unit Test. | Pass |
| **TC_VOU_005** | Bảng QĐ (R1) / EP | Kiểm tra hệ thống chặn mã rác / mã không tồn tại | Giỏ hàng hợp lệ. | Nhập mã Voucher rác. | `MISSING` (Code không có trong DB). | Báo lỗi Mã giảm giá không tồn tại. | Khớp với Unit Test. | Pass |
| **TC_VOU_006** | Bảng QĐ (R8) / EP | Kiểm tra áp dụng thành công mã hợp lệ (Trừ tiền cứng) | Giỏ hàng 200 đô. | Nhập mã Voucher và Áp dụng. | `TESTFIXED30` (Loại trừ tiền cứng 30 đô). | Thành công. Tiền giảm đúng 30 đô. | Khớp với Unit Test. | Pass |
| **TC_VOU_007** | Bảng QĐ (R6) / BVA | Kiểm tra chặn áp mã do giới hạn Cá nhân (Per User Limit) | User `alice` có đơn hàng 100 đô. | Gọi API áp dụng Voucher cho User `alice`. | Mã `SALE10` (Giới hạn cá nhân = 2, `alice` đã xài 2 lần). | API từ chối áp mã. Báo lỗi "đã dùng hết số lượt cho phép". | Khớp với `VoucherDAOTest`. | Pass |
| **TC_VOU_008** | Bảng QĐ (R7) / EP | Khách vãng lai (Guest) không bị ràng buộc giới hạn cá nhân | Khách vãng lai (username trống/null). | Gọi API áp dụng Voucher. | Mã `SALE10` (Giới hạn cá nhân = 0). | API cho phép áp mã thành công. | Khớp với `VoucherDAOTest`. | Pass |
| **TC_VOU_009** | Bảng QĐ (R2) / EP | Kiểm tra chặn áp mã đã bị khóa (Inactive) | Giỏ hàng hợp lệ. | Cố tình nhập mã đã bị khóa. | Mã `SALE10` có trường `active = false`. | Báo lỗi "Mã giảm giá không tồn tại hoặc đã bị vô hiệu hóa". | Khớp với `VoucherDAOTest`. | Pass |
| **TC_VOU_010** | EP / CRUD | Admin tạo mã giảm giá mới qua API (Create) | Tài khoản Admin. | Gửi `POST /api/v1/admin/vouchers` với body hợp lệ. | Body chứa `code=NEWYEAR`, `discountValue=20`, v.v... | Server trả về 200 OK. Mã mới xuất hiện trong DB. | Khớp API Postman. | Pass |
| **TC_VOU_011** | EP / CRUD | Admin vô hiệu hóa mã giảm giá qua API (Deactivate) | Tài khoản Admin. | Gửi `DELETE /api/v1/admin/vouchers/SALE10`. | Endpoint đi kèm Code voucher hợp lệ. | Trả về 200 OK. Cột `active` chuyển thành `false`. | Khớp API Postman. | Pass |
| **TC_VOU_012** | EP / CRUD | Khách hàng lấy danh sách Voucher còn hiệu lực (List Active) | Không cần phân quyền. | Gửi `GET /api/v1/vouchers`. | Bắn Request GET đơn giản. | Trả về danh sách chứa các Voucher thỏa mãn: active=true, còn hạn, còn lượt sử dụng. | Khớp API Postman. | Pass |
