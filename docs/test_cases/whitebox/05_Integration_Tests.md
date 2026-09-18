# BÁO CÁO TỔNG QUAN KIỂM THỬ TÍCH HỢP (INTEGRATION TESTS)
**Thư mục mục tiêu:** `com.example.demo` (root test), `com.example.demo.integration`  
**Tổng số Test Cases:** **23 Test Cases**  
**Tổng số tập tin ma trận chi tiết:** **9 tập tin**

---

## 1. GIỚI THIỆU & MỤC TIÊU PHÂN HỆ KIỂM THỬ TÍCH HỢP

Khác với các tầng Unit Test sử dụng Mockito để cô lập từng hàm đơn lẻ, **Phân hệ Kiểm thử Tích hợp (Integration Tests)** kiểm chứng sự phối hợp thực tế giữa nhiều thành phần của hệ thống phần mềm trong môi trường Spring Boot:
1. **Spring Boot Feature Integration (`springboot/` - 15 Test Cases / 5 files):**
   - Sử dụng `@SpringBootTest` kết hợp `@Transactional` để kiểm thử tương tác thực tế giữa tầng DAO, Hibernate EntityManager và cơ sở dữ liệu.
   - Kiểm tra các ràng buộc nghiệp vụ phức tạp: tính toán giảm giá voucher (cố định, phần trăm capped, hạn dùng), xử lý chuyển giao cờ địa chỉ mặc định, hoàn kho khi hủy/duyệt trả đơn hàng, khởi động ApplicationContext và hàm `main`.
2. **Advanced Integration Testing (`advanced/` - 8 Test Cases / 4 files):**
   - Tích hợp liên tầng hoàn chỉnh từ Controller (MockMvc) ➔ Service ➔ DAO ➔ Cơ sở dữ liệu MySQL thật (`MySqlIntegrationTestBase`).
   - Tích hợp dịch vụ bên ngoài: pipeline phân tích ảnh AI qua HTTP mock server (`HttpServer`) và FastAPI service thật (`test24.actual-ai.url`).
   - Kiểm tra tính nguyên tử (Atomicity), tính nhất quán (Consistency), xử lý tranh chấp tồn kho (Race conditions) và cơ chế tự động Rollback giao dịch sau khi chạy test (`@Rollback`, `@AfterTransaction`).

---

## 2. BẢNG TỔNG HỢP 23 TEST CASES TÍCH HỢP

| STT | Nhóm Kiểm Thử | File Tài Liệu Ma Trận | File Kiểm Thử Tương Ứng | Số TCs | Phạm Vi / Mục Tiêu Nghiệp Vụ | Trạng Thái |
| :---: | :--- | :--- | :--- | :---: | :--- | :---: |
| 1 | **SpringBoot Feature** | [AddressBookTests_TCs.md](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/docs/test_cases/whitebox/05_Integration_Tests/springboot/AddressBookTests_TCs.md) | `AddressBookTests.java` | **4** | Quản lý sổ địa chỉ, địa chỉ mặc định, bảo vệ chống xóa chéo | ✅ PASS |
| 2 | **SpringBoot Feature** | [OrderCancelReturnTests_TCs.md](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/docs/test_cases/whitebox/05_Integration_Tests/springboot/OrderCancelReturnTests_TCs.md) | `OrderCancelReturnTests.java` | **3** | Hủy đơn và duyệt trả hàng, hoàn lại số lượng tồn kho tự động | ✅ PASS |
| 3 | **SpringBoot Feature** | [VoucherTests_TCs.md](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/docs/test_cases/whitebox/05_Integration_Tests/springboot/VoucherTests_TCs.md) | `VoucherTests.java` | **5** | Quy tắc giảm giá voucher, trần giảm giá, đơn tối thiểu, hết hạn/hết lượt | ✅ PASS |
| 4 | **SpringBoot Feature** | [WishlistTests_TCs.md](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/docs/test_cases/whitebox/05_Integration_Tests/springboot/WishlistTests_TCs.md) | `WishlistTests.java` | **1** | Vòng đời thêm, kiểm tra trạng thái yêu thích, xóa sản phẩm yêu thích | ✅ PASS |
| 5 | **SpringBoot Feature** | [SpringShoppingCart2Application_TCs.md](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/docs/test_cases/whitebox/05_Integration_Tests/springboot/SpringShoppingCart2Application_TCs.md) | `SpringShoppingCart2Application*.java` | **2** | Khởi động Spring context (`contextLoads`) và bootstrap hàm `main` | ✅ PASS |
| 6 | **Advanced Integration** | [ActualFastApiIntegrationTest_TCs.md](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/docs/test_cases/whitebox/05_Integration_Tests/advanced/ActualFastApiIntegrationTest_TCs.md) | `ActualFastApiIntegrationTest.java` | **1** | Tích hợp dịch vụ FastAPI AI thật, từ chối ảnh quá nhỏ (<100px) | ✅ PASS |
| 7 | **Advanced Integration** | [AiServiceIntegrationTest_TCs.md](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/docs/test_cases/whitebox/05_Integration_Tests/advanced/AiServiceIntegrationTest_TCs.md) | `AiServiceIntegrationTest.java` | **3** | Pipeline gửi multipart ảnh sang AI mock server, xử lý duyệt/từ chối/500 | ✅ PASS |
| 8 | **Advanced Integration** | [OrderWorkflowIntegrationTest_TCs.md](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/docs/test_cases/whitebox/05_Integration_Tests/advanced/OrderWorkflowIntegrationTest_TCs.md) | `OrderWorkflowIntegrationTest.java` | **3** | Luồng checkout API trọn vẹn, trừ tồn kho, phát hiện hết hàng, rollback | ✅ PASS |
| 9 | **Advanced Integration** | [TransactionalIsolationIntegrationTest_TCs.md](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/docs/test_cases/whitebox/05_Integration_Tests/advanced/TransactionalIsolationIntegrationTest_TCs.md) | `TransactionalIsolationIntegrationTest.java` | **1** | Cô lập giao dịch xuyên suốt Controller-DB, tự động dọn sạch dữ liệu test | ✅ PASS |
| **TỔNG** | **2 Phân nhóm tích hợp** | **9 tập tin ma trận chi tiết** | **10 Test Classes** | **23** | **Kiểm thử tích hợp toàn diện hệ thống backend** | **✅ 100% PASS** |
