# Ma trận Truy xuất (Traceability Matrix): Chức năng Hủy / Trả hàng (Cancel & Return)

Tài liệu này ánh xạ các Test Case Đặc tả (từ `docs/test_cases/07_Cancel_Return_Order.md`) với các Test Script (File Java Automation Test) thực tế trong mã nguồn.

| Mã Test Case | File Test (Test Script Location) | Tên Hàm Test (Method Name) | Trạng thái |
| :--- | :--- | :--- | :--- |
| **TC_CAN_001** | `src/test/java/com/example/demo/dao/OrderReturnDAOTest.java` | `cancelOrder_acceptsNormalizedPendingAndCancelsOrderWithoutDetails` | Automated |
| **TC_CAN_002** | `src/test/java/com/example/demo/dao/OrderReturnDAOTest.java` | `cancelOrder_restoresStockAndNeverMakesSalesNegative` | Automated |
| **TC_CAN_003** | `src/test/java/com/example/demo/dao/OrderReturnDAOTest.java` | `cancelOrder_rejectsEveryNonPendingStatus` | Automated |
| **TC_CAN_004** | `src/test/java/com/example/demo/dao/OrderReturnDAOTest.java` | `cancelOrder_rejectsMissingOrDifferentCustomer` | Automated |
| **TC_CAN_005** | `src/test/java/com/example/demo/dao/OrderReturnDAOTest.java` | `createReturnRequest_trimsFieldsPersistsAndTagsOrder` | Automated |
| **TC_CAN_006** | `src/test/java/com/example/demo/dao/OrderReturnDAOTest.java` | `createReturnRequest_rejectsDuplicateRequest` | Automated |
| **TC_CAN_007** | `src/test/java/com/example/demo/dao/OrderReturnDAOTest.java` | `createReturnRequest_rejectsEachInvalidFormBoundary` | Automated |
| **TC_CAN_008** | `src/test/java/com/example/demo/dao/OrderReturnDAOTest.java` | `updateReturnStatus_approveRestoresStockAndMarksReturned` | Automated |
| **TC_CAN_009** | `src/test/java/com/example/demo/dao/OrderReturnDAOTest.java` | `updateReturnStatus_rejectReturnsOrderToCompletedWithoutStockMutation` | Automated |
| **TC_CAN_010** | `src/test/java/com/example/demo/controller/api/OrderCancelReturnApiControllerTest.java` | `updateStatus_rejectsNonAdminAuthentication` | Automated |

---

## Ánh xạ với Hệ thống Postman (E2E API Testing)
Ngoài việc chạy Unit Test bằng JUnit ở mức Backend, toàn bộ các luồng nghiệp vụ trên đều được tự động hóa End-to-End thông qua bộ Postman Collection.

- **File Thực thi Postman:** `docs/Shoeshop_API_Collection.json`
- **File Môi trường Postman:** `docs/Shoeshop_Postman_Environment.json`

### Bảng Ánh xạ Vị trí Code trong Postman Collection
Dưới đây là vị trí (số dòng - Line Number) chính xác của các đoạn script test API Hủy/Trả đơn hàng bên trong file `Shoeshop_API_Collection.json` (Tổng cộng 1786 dòng):

| Nhóm API | Request Name (Tên API) | Vị trí (Line Number) | Ánh xạ Test Case |
| :--- | :--- | :--- | :--- |
| **User API** | `POST /api/v1/orders/{orderId}/cancel - Cancel Pending Order` | Dòng 1111 | TC_CAN_001 $\rightarrow$ 004 |
| **User API** | `POST /api/v1/orders/{orderId}/return - Request Order Return & Refund` | Dòng 1136 | TC_CAN_005 $\rightarrow$ 007 |
| **User API** | `GET /api/v1/orders/{orderId}/return - View Return Request Details` | Dòng 1177 | Xem thông tin Trả hàng |
| **Admin API** | `PUT /api/v1/admin/orders/{orderId}/return-status - Approve/Reject Order Return Request` | Dòng 1611 | TC_CAN_008 $\rightarrow$ 010 |

> **Lưu ý:** Việc duy trì `Method Name` rõ ràng theo chuẩn BDD (Behavior-Driven Development) trong Code và tổ chức thư mục Folder chuẩn trong Postman giúp người xem (hoặc các công cụ CI/CD) dễ dàng ánh xạ 1-1 với tài liệu mà không bị phụ thuộc vào số dòng code (Line number) - thứ liên tục thay đổi.
