# BẢNG MA TRẬN KIỂM THỬ TÍCH HỢP VOUCHER DISCOUNT RULES (SPRING BOOT INTEGRATION)
**Đường dẫn file mã nguồn:** [VoucherDAO.java](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/src/main/java/com/example/demo/dao/VoucherDAO.java)  
**Đường dẫn file kiểm thử:** [VoucherTests.java](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/src/test/java/com/example/demo/VoucherTests.java)

---

## 1. TỔNG QUAN ĐẶC ĐIỂM KIỂM THỬ TÍCH HỢP
* **Phạm vi kiểm thử:** Tích hợp quy tắc tính toán chiết khấu voucher, ghi nhận lịch sử sử dụng (`VoucherUsage`), và kiểm tra các ràng buộc nghiệp vụ (hạn mức, giá trị đơn tối thiểu, hạn sử dụng) trên CSDL thực tế.
* **Mục tiêu:** Đảm bảo chính sách khuyến mãi hoạt động chính xác với tiền thật, không cho phép lạm dụng mã giảm giá.

---

## 2. MA TRẬN TEST CASES KIỂM THỬ TÍCH HỢP (5 TEST CASES)

| Mã TC | Hàm ở src/main | Hàm kiểm thử ở src/test | Giá trị Test đầu vào | Kịch bản tích hợp / Logic mục tiêu | Kết quả mong đợi |
| :---: | :--- | :--- | :--- | :--- | :--- |
| **TC_ITG_VOU_01** | `validateAndApplyVoucher(String, double, String)` | `testPercentageDiscountWithMaxDiscountCap()` | Voucher giảm 20%, tối đa 50K; đơn hàng 500K | Tích hợp tính chiết khấu phần trăm capped: 20% của 500K = 100K > 50K ➔ áp dụng mức trần 50K | Áp dụng thành công, `discountAmount = 50.0`, `finalAmount = 450.0` |
| **TC_ITG_VOU_02** | `validateAndApplyVoucher(String, double, String)` | `testFixedDiscountCalculation()` | Voucher giảm cố định 30K; đơn hàng 200K | Tích hợp giảm tiền mặt trực tiếp: 200K - 30K | Áp dụng thành công, `discountAmount = 30.0`, `finalAmount = 170.0` |
| **TC_ITG_VOU_03** | `validateAndApplyVoucher(String, double, String)` | `testMinimumOrderValueRejection()` | Voucher yêu cầu đơn tối thiểu 500K; đơn hàng thử nghiệm 200K | Ràng buộc giá trị đơn hàng tối thiểu: từ chối áp dụng khi đơn hàng chưa đạt điều kiện | Áp dụng thất bại (`isSuccess == false`), thông báo chứa cụm từ `"tối thiểu"` |
| **TC_ITG_VOU_04** | `validateAndApplyVoucher(String, double, String)` | `testExpiredVoucherRejection()` | Voucher cấu hình hạn sử dụng lùi về 5 ngày trước; đơn hàng 200K | Ràng buộc thời gian hiệu lực: từ chối mã giảm giá đã hết hạn sử dụng | Áp dụng thất bại (`isSuccess == false`), thông báo chứa cụm từ `"hết hạn"` |
| **TC_ITG_VOU_05** | `validateAndApplyVoucher(String, double, String)` | `testUsageLimitRejection()` | Voucher có giới hạn 1 lượt dùng (`usageLimit = 1`), đã ghi nhận 1 lần dùng qua `recordVoucherUsage` | Ràng buộc tổng số lượt sử dụng toàn hệ thống: từ chối khi voucher đã hết lượt | Áp dụng thất bại (`isSuccess == false`), thông báo chứa cụm từ `"hết số lượt"` |
