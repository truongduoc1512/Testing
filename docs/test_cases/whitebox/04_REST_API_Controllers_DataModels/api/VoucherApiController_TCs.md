# BẢNG MA TRẬN KIỂM THỬ HỘP TRẮNG VOUCHER API CONTROLLER
**Đường dẫn file mã nguồn:** [VoucherApiController.java](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/src/main/java/com/example/demo/controller/api/VoucherApiController.java)  
**Đường dẫn file kiểm thử:** [VoucherApiControllerTest.java](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/src/test/java/com/example/demo/controller/api/VoucherApiControllerTest.java)

---

## 1. TỔNG QUAN ĐỘ BAO PHỦ (JACOCO COVERAGE METRICS)

| File Target | Instruction Coverage | Branch Coverage | Line Coverage | Method Coverage | Trạng Thái |
| :--- | :---: | :---: | :---: | :---: | :---: |
| `VoucherApiController.java` | 100% | 100% | 100% | 100% | Đạt |

---

## 2. MA TRẬN TEST CASES KIỂM THỬ HỘP TRẮNG (9 TEST CASES)

| Mã TC | Hàm ở src/main | Hàm kiểm thử ở src/test | Giá trị Test đầu vào | Nhánh logic / Điều kiện mục tiêu | Kết quả mong đợi |
| :---: | :--- | :--- | :--- | :--- | :--- |
| **TC_VOU_01** | `getActiveVouchers()` | `getActiveVouchers_returnsDaoResult()` | DAO trả về list 2 voucher đang kích hoạt | Nhánh lấy danh sách mã giảm giá còn hiệu lực cho khách hàng | HTTP 200 OK, trả về danh sách voucher từ DAO |
| **TC_VOU_02** | `getAllVouchersAdmin()` | `getAllVouchersAdmin_returnsDaoResult()` | DAO trả về list 3 voucher | Nhánh quản trị viên lấy toàn bộ danh sách mã giảm giá | HTTP 200 OK, trả về danh sách voucher từ DAO |
| **TC_VOU_03** | `applyVoucher(Map, HttpServletRequest)` | `applyVoucher_treatsNullPayloadAsMissingCodeWithoutUsername()` | `payload = null`, request chưa đăng nhập, giỏ trống | Nhánh xử lý payload null, quy đổi thành mã null và tổng tiền 0.0 | HTTP 400 BAD REQUEST, trả về kết quả áp dụng thất bại |
| **TC_VOU_04** | `applyVoucher(Map, HttpServletRequest)` | `applyVoucher_resolvesUsernameForMissingVoucherCode(String, Authentication, String)` | `payload = {}` (thiếu voucherCode), authentication (unauthenticated, anonymousUser, buyer) | Nhánh trích xuất username từ SecurityContext khi payload thiếu code | HTTP 400 BAD REQUEST, gọi validate với username tương ứng |
| **TC_VOU_05** | `applyVoucher(Map, HttpServletRequest)` | `applyVoucher_usesServerCartAmountAndStoresSuccessfulDiscount()` | User `buyer`, giỏ hàng có 2 sản phẩm tổng tiền 100.0, `voucherCode = "SAVE10"` | Nhánh áp dụng mã giảm giá thành công: cập nhật giảm giá vào `CartInfo` session | HTTP 200 OK, `cart.getVoucherCode() == "SAVE10"`, giảm 10.0 |
| **TC_VOU_06** | `createVoucherAdmin(VoucherForm)` | `createVoucher_rejectsInvalidForm(String, VoucherForm)` | Các form vi phạm: code null, code trống `" "`, discountValue <= 0 | Nhánh kiểm tra tính hợp lệ của biểu mẫu voucher (`isInvalidForm == true`) | HTTP 400 BAD REQUEST, body `success: false` |
| **TC_VOU_07** | `createVoucherAdmin(VoucherForm)` | `createVoucher_savesAndReturnsCreatedEntity()` | `code = "save10"`, `discountValue = 10.0` | Nhánh tạo mới voucher thành công, gọi DAO lưu và chuẩn hóa chữ hoa | HTTP 201 CREATED, body `success: true` và chứa thông tin voucher |
| **TC_VOU_08** | `deleteVoucherAdmin(String)` | `deleteVoucher_returnsNotFoundWhenDaoDoesNotDelete()` | `code = "missing"`, DAO trả về `false` | Nhánh xóa voucher thất bại khi không tìm thấy mã trong DB | HTTP 404 NOT FOUND, body `success: false` |
| **TC_VOU_09** | `deleteVoucherAdmin(String)` | `deleteVoucher_returnsSuccessWhenDaoDeletes()` | `code = "SAVE10"`, DAO trả về `true` | Nhánh xóa voucher thành công | HTTP 200 OK, body `success: true` |
