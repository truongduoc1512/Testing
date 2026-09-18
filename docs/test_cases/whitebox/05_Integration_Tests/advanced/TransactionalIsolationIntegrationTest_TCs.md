# BẢNG MA TRẬN KIỂM THỬ TÍCH HỢP CÔ LẬP GIAO DỊCH (TRANSACTIONAL ISOLATION)
**Đường dẫn file mã nguồn:** [CartApiController.java](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/src/main/java/com/example/demo/controller/api/CartApiController.java), [OrderDAO.java](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/src/main/java/com/example/demo/dao/OrderDAO.java)  
**Đường dẫn file kiểm thử:** [TransactionalIsolationIntegrationTest.java](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/src/test/java/com/example/demo/integration/TransactionalIsolationIntegrationTest.java)

---

## 1. TỔNG QUAN ĐẶC ĐIỂM KIỂM THỬ TÍCH HỢP
* **Phạm vi kiểm thử:** Tích hợp kiểm thử cơ chế Rollback tự động của Spring Test (`@Transactional`, `@Rollback`) xuyên suốt toàn bộ các tầng: Controller ➔ Service ➔ DAO ➔ CSDL MySQL.
* **Mục tiêu:** Chứng minh rằng mọi thay đổi dữ liệu phát sinh trong quá trình kiểm thử (tạo sản phẩm, thêm giỏ, đặt hàng) đều được cô lập hoàn toàn và tự động rollback sạch sẽ sau khi giao dịch kết thúc (`@AfterTransaction`), không để lại rác dữ liệu làm sai lệch CSDL.

---

## 2. MA TRẬN TEST CASES KIỂM THỬ TÍCH HỢP (1 TEST CASE)

| Mã TC | Chức năng ở src/main | Hàm kiểm thử ở src/test | Giá trị Test đầu vào | Kịch bản tích hợp / Logic mục tiêu | Kết quả mong đợi |
| :---: | :--- | :--- | :--- | :--- | :--- |
| **TC_ITG_TXI_01** | Luồng Full Checkout Transaction | `shouldRollbackControllerServiceDaoAndDatabaseChangesAfterTest()` | Thêm 2 sản phẩm vào giỏ, lưu thông tin khách hàng và checkout đơn hàng trong giao dịch test | Kiểm thử cơ chế cô lập giao dịch: trong transaction thì dữ liệu được ghi nhận bình thường; khi transaction kết thúc (`@AfterTransaction`), Spring tự động Rollback sạch sẽ | Trong transaction: đơn hàng = 1, chi tiết = 1, tồn kho trừ về 6. Sau transaction (`@AfterTransaction`): kiểm tra CSDL số đơn hàng = 0, chi tiết = 0, sản phẩm tạo tạm thời bị xóa sạch |
