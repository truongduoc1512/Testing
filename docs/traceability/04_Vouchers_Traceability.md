# Ma trận Truy xuất (Traceability Matrix): Chức năng Voucher

Tài liệu này ánh xạ các Test Case Đặc tả (từ `docs/test_cases/04_Vouchers.md`) với các Test Script (File Java Automation Test) thực tế trong mã nguồn.

| Mã Test Case | File Test (Test Script Location) | Tên Hàm Test (Method Name) | Trạng thái |
| :--- | :--- | :--- | :--- |
| **TC_VOU_001** | `src/test/java/com/example/demo/VoucherTests.java` | `testPercentageDiscountWithMaxDiscountCap` | Automated |
| **TC_VOU_002** | `src/test/java/com/example/demo/VoucherTests.java` | `testMinimumOrderValueRejection` | Automated |
| **TC_VOU_003** | `src/test/java/com/example/demo/VoucherTests.java` | `testExpiredVoucherRejection` | Automated |
| **TC_VOU_004** | `src/test/java/com/example/demo/VoucherTests.java` | `testUsageLimitRejection` | Automated |
| **TC_VOU_005** | `src/test/java/com/example/demo/dao/VoucherDAOTest.java` | `validateAndApplyVoucher_rejectsUnknownVoucher` | Automated |
| **TC_VOU_006** | `src/test/java/com/example/demo/VoucherTests.java` | `testFixedDiscountCalculation` | Automated |
| **TC_VOU_007** | `src/test/java/com/example/demo/dao/VoucherDAOTest.java` | `validateAndApplyVoucher_enforcesPerUserUsageBoundary` | Automated |
| **TC_VOU_008** | `src/test/java/com/example/demo/dao/VoucherDAOTest.java` | `validateAndApplyVoucher_skipsPerUserLimitForGuest` | Automated |
| **TC_VOU_009** | `src/test/java/com/example/demo/dao/VoucherDAOTest.java` | `validateAndApplyVoucher_rejectsInactiveVoucher` | Automated |
| **TC_VOU_010** | `src/test/java/com/example/demo/controller/api/VoucherApiControllerTest.java` | `createVoucher_savesAndReturnsCreatedEntity` | Automated |
| **TC_VOU_011** | `src/test/java/com/example/demo/controller/api/VoucherApiControllerTest.java` | `deleteVoucher_returnsSuccessWhenDaoDeletes` | Automated |
| **TC_VOU_012** | `src/test/java/com/example/demo/controller/api/VoucherApiControllerTest.java` | `getActiveVouchers_returnsDaoResult` | Automated |

---

## Ánh xạ với Hệ thống Postman (E2E API Testing)
Ngoài việc chạy Unit Test bằng JUnit ở mức Backend, toàn bộ các luồng nghiệp vụ trên (đặc biệt là 3 luồng gọi API từ bên ngoài là **TC_VOU_010, TC_VOU_011, TC_VOU_012**) đều được tự động hóa End-to-End thông qua bộ Postman Collection.

- **File Thực thi Postman:** `docs/Shoeshop_API_Collection.json`
- **File Môi trường Postman:** `docs/Shoeshop_Postman_Environment.json`

### Bảng Ánh xạ Vị trí Code trong Postman Collection
Dưới đây là vị trí (số dòng - Line Number) chính xác của các đoạn script test API Voucher bên trong file `Shoeshop_API_Collection.json` (Tổng cộng 1786 dòng):

| Nhóm API | Request Name (Tên API) | Vị trí (Line Number) | Ánh xạ Test Case |
| :--- | :--- | :--- | :--- |
| **User API** | `GET /api/v1/vouchers - Get Available Vouchers` | Dòng 1274 | TC_VOU_012 |
| **User API** | `POST /api/v1/vouchers/apply - Apply Voucher Code to Order` | Dòng 1305 | TC_VOU_001 $\rightarrow$ 009 |
| **Admin API** | `GET /api/v1/admin/vouchers - Admin List All Vouchers` | Dòng 1652 | N/A (Xem danh sách) |
| **Admin API** | `POST /api/v1/admin/vouchers - Admin Create New Voucher Code` | Dòng 1677 | TC_VOU_010 |
| **Admin API** | `DELETE /api/v1/admin/vouchers/{code} - Admin Deactivate Voucher Code` | Dòng 1722 | TC_VOU_011 |

> **Lưu ý:** Việc duy trì `Method Name` rõ ràng theo chuẩn BDD (Behavior-Driven Development) trong Code và tổ chức thư mục Folder chuẩn trong Postman giúp người xem (hoặc các công cụ CI/CD) dễ dàng ánh xạ 1-1 với tài liệu mà không bị phụ thuộc vào số dòng code (Line number) - thứ liên tục thay đổi.
