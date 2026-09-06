# Ma trận Truy xuất (Traceability Matrix): Chức năng Quản lý Trị sự (Admin Management)

Tài liệu này ánh xạ các Test Case Đặc tả (từ `docs/test_cases/08_Admin_Management.md`) với các Test Script (File Java Automation Test) thực tế trong mã nguồn.

| Mã Test Case | Phân hệ (Module) | File Test (Test Script Location) | Tên Hàm Test (Method Name) | Trạng thái |
| :--- | :--- | :--- | :--- | :--- |
| **TC_ADM_001** | Account | `src/test/java/com/example/demo/controller/UserControllerCoverageTest.java` | `userEditSave_blocksLastActiveAdminFromLosingAdminRole` | Automated |
| **TC_ADM_002** | Account | `src/test/java/com/example/demo/controller/UserControllerCoverageTest.java` | `userEditSave_deactivatesAdminWhenAnotherActiveAdminExists` | Automated |
| **TC_ADM_003** | Account | `src/test/java/com/example/demo/controller/UserControllerCoverageTest.java` | `userEditSave_allowsAdminDowngradeWhenAnotherActiveAdminExists` | Automated |
| **TC_ADM_004** | Account | `src/test/java/com/example/demo/controller/UserControllerCoverageTest.java` | `userList_redirectsNonAdmin` | Automated |
| **TC_ADM_005** | Product | `src/test/java/com/example/demo/controller/api/ProductApiControllerTest.java` | `saveProduct_forbidsUpdatingForeignProduct` | Automated |
| **TC_ADM_006** | Product | `src/test/java/com/example/demo/controller/api/ProductApiControllerTest.java` | `deleteProduct_forbidsProductOwnedByAnotherPrincipal` | Automated |
| **TC_ADM_007** | Product | `src/test/java/com/example/demo/controller/api/ProductApiControllerTest.java` | `saveProduct_rejectsInvalidForm` | Automated |
| **TC_ADM_008** | Order | `src/test/java/com/example/demo/controller/api/OrderApiControllerTest.java` | `updateStatus_rejectsPrincipalOutsideManagementScope` | Automated |
| **TC_ADM_009** | Order | `src/test/java/com/example/demo/controller/api/OrderApiControllerTest.java` | `getOrder_recalculatesAmountWhenAdminIsNotOrderCustomer` | Automated |
| **TC_ADM_010** | Order | `src/test/java/com/example/demo/controller/api/OrderApiControllerTest.java` | `updateStatus_mapsDaoException` | Automated |

---

## Ánh xạ với Hệ thống Postman (E2E API Testing)
Ngoài việc chạy Unit Test bằng JUnit ở mức Backend, một số luồng nghiệp vụ API quan trọng của Admin đã được tự động hóa End-to-End thông qua bộ Postman Collection.

- **File Thực thi Postman:** `docs/Shoeshop_API_Collection.json`
- **File Môi trường Postman:** `docs/Shoeshop_Postman_Environment.json`

### Bảng Ánh xạ Vị trí Code trong Postman Collection
Dưới đây là vị trí (số dòng - Line Number) chính xác của các đoạn script test API Admin bên trong file `Shoeshop_API_Collection.json` (Tổng cộng 1786 dòng):

| Nhóm API | Request Name (Tên API) | Vị trí (Line Number) | Ánh xạ Test Case |
| :--- | :--- | :--- | :--- |
| **Admin Login** | `POST /j_spring_security_check - Authenticate Administrator Account` | Dòng 1341 | Đăng nhập Admin |
| **Admin Dashboard** | `GET /admin/accountInfo - Access Admin Dashboard Overview` | Dòng 1401 | Xem Dashboard |
| **Admin Product** | `POST /api/v1/products - Create/Update Product via REST API` | Dòng 1541 | TC_ADM_005, 007 |
| **Admin Product** | `DELETE /api/v1/products/{code} - Delete Product Code` | Dòng 1586 | TC_ADM_006 |
| **Admin Order** | `GET /admin/orderList - Access Admin Order Management List` | Dòng 1426 | Xem danh sách đơn |

> **Lưu ý:** Việc duy trì `Method Name` rõ ràng theo chuẩn BDD (Behavior-Driven Development) trong Code và tổ chức thư mục Folder chuẩn trong Postman giúp người xem (hoặc các công cụ CI/CD) dễ dàng ánh xạ 1-1 với tài liệu mà không bị phụ thuộc vào số dòng code (Line number) - thứ liên tục thay đổi.
