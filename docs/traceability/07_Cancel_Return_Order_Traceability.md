# MA TRẬN TRUY XUẤT (TRACEABILITY MATRIX): CHỨC NĂNG HỦY & TRẢ HÀNG (CANCEL & RETURN)

**Dự án:** ShoeShop Testing & Quality Assurance System  
**Quy mô:** 33 Test Cases  
**Phân hệ:** Quản lý trạng thái Đơn hàng, Tự hủy đơn & Khiếu nại Trả hàng / Hoàn tiền  

Tài liệu này ánh xạ chi tiết 33 Test Cases Đặc tả (từ `docs/test_cases/07_Cancel_Return_Order.md` và Sheet `7. Cancel & Return` trong `Testing.xlsx`) với các Test Script tự động hóa (Java Backend Unit/Integration Tests & Postman E2E).

---

## 1. ÁNH XẠ TEST CASE VỚI BACKEND JUNIT TEST SCRIPTS

| Mã Test Case | Nhóm Kỹ thuật | File Test (Test Script Location) | Tên Hàm Test (Method Name) | Trạng thái |
| :--- | :--- | :--- | :--- | :---: |
| **TC_CAN_ST_001** | State Transition | `src/test/java/com/example/demo/dao/OrderReturnDAOTest.java` | `cancelOrder_acceptsNormalizedPendingAndCancelsOrderWithoutDetails` | Automated (PASS) |
| **TC_CAN_ST_002** | State Transition | `src/test/java/com/example/demo/dao/OrderReturnDAOTest.java` | `createReturnRequest_trimsFieldsPersistsAndTagsOrder` | Automated (PASS) |
| **TC_CAN_ST_003** | State Transition | `src/test/java/com/example/demo/dao/OrderReturnDAOTest.java` | `cancelOrder_rejectsEveryNonPendingStatus` | Automated (PASS) |
| **TC_CAN_ST_004** | State Transition | `src/test/java/com/example/demo/dao/OrderReturnDAOTest.java` | `updateReturnStatus_approveRestoresStockAndMarksReturned` | Automated (PASS) |
| **TC_CAN_ST_005** | State Transition | `src/test/java/com/example/demo/dao/OrderReturnDAOTest.java` | `updateReturnStatus_rejectReturnsOrderToCompletedWithoutStockMutation` | Automated (PASS) |
| **TC_CAN_DT_001** | Decision Table | `src/test/java/com/example/demo/controller/api/OrderCancelReturnApiControllerTest.java` | `cancelOrder_rejectsLoginRequiredAuthentication` | Automated (PASS) |
| **TC_CAN_DT_002** | Decision Table | `src/test/java/com/example/demo/dao/OrderReturnDAOTest.java` | `cancelOrder_rejectsMissingOrder` | Automated (PASS) |
| **TC_CAN_DT_003** | Decision Table | `src/test/java/com/example/demo/dao/OrderReturnDAOTest.java` | `cancelOrder_rejectsMissingOrDifferentCustomer` | Automated (PASS) |
| **TC_CAN_DT_004** | Decision Table | `src/test/java/com/example/demo/dao/OrderReturnDAOTest.java` | `cancelOrder_rejectsEveryNonPendingStatus` | Automated (PASS) |
| **TC_CAN_DT_005** | Decision Table | `src/test/java/com/example/demo/dao/OrderReturnDAOTest.java` | `cancelOrder_acceptsNormalizedPendingAndCancelsOrderWithoutDetails` | Automated (PASS) |
| **TC_CAN_DT_006** | Decision Table | `src/test/java/com/example/demo/dao/OrderReturnDAOTest.java` | `createReturnRequest_trimsFieldsPersistsAndTagsOrder` | Automated (PASS) |
| **TC_CAN_ROB_001** | Robust BVA | `src/test/java/com/example/demo/dao/OrderReturnDAOTest.java` | `createReturnRequest_rejectsEachInvalidFormBoundary` | Automated (PASS) |
| **TC_CAN_BVA_001** | Standard BVA | `src/test/java/com/example/demo/dao/OrderReturnDAOTest.java` | `createReturnRequest_trimsFieldsPersistsAndTagsOrder` | Automated (PASS) |
| **TC_CAN_BVA_002** | Standard BVA | `src/test/java/com/example/demo/dao/OrderReturnDAOTest.java` | `createReturnRequest_trimsFieldsPersistsAndTagsOrder` | Automated (PASS) |
| **TC_CAN_BVA_003** | Standard BVA | `src/test/java/com/example/demo/dao/OrderReturnDAOTest.java` | `createReturnRequest_trimsFieldsPersistsAndTagsOrder` | Automated (PASS) |
| **TC_CAN_BVA_004** | Standard BVA | `src/test/java/com/example/demo/dao/OrderReturnDAOTest.java` | `createReturnRequest_trimsFieldsPersistsAndTagsOrder` | Automated (PASS) |
| **TC_CAN_ROB_002** | Robust BVA | `src/test/java/com/example/demo/dao/OrderReturnDAOTest.java` | `createReturnRequest_rejectsEachInvalidFormBoundary` | Automated (PASS) |
| **TC_CAN_ROB_003** | Robust BVA | `src/test/java/com/example/demo/controller/api/OrderCancelReturnApiControllerTest.java` | `createReturn_returnsCreatedDataForValidImageBoundary` | Automated (PASS) |
| **TC_CAN_BVA_005** | Standard BVA | `src/test/java/com/example/demo/dao/OrderReturnDAOTest.java` | `createReturnRequest_trimsFieldsPersistsAndTagsOrder` | Automated (PASS) |
| **TC_CAN_BVA_006** | Standard BVA | `src/test/java/com/example/demo/dao/OrderReturnDAOTest.java` | `createReturnRequest_trimsFieldsPersistsAndTagsOrder` | Automated (PASS) |
| **TC_CAN_BVA_007** | Standard BVA | `src/test/java/com/example/demo/dao/OrderReturnDAOTest.java` | `createReturnRequest_trimsFieldsPersistsAndTagsOrder` | Automated (PASS) |
| **TC_CAN_BVA_008** | Standard BVA | `src/test/java/com/example/demo/controller/api/OrderCancelReturnApiControllerTest.java` | `createReturn_returnsCreatedDataForValidImageBoundary` | Automated (PASS) |
| **TC_CAN_ROB_004** | Robust BVA | `src/test/java/com/example/demo/dao/OrderReturnDAOTest.java` | `createReturnRequest_rejectsEachInvalidFormBoundary` | Automated (PASS) |
| **TC_CAN_ROB_005** | Inventory BVA | `src/test/java/com/example/demo/dao/OrderReturnDAOTest.java` | `cancelOrder_restoresStockAndNeverMakesSalesNegative` | Automated (PASS) |
| **TC_CAN_ROB_006** | Inventory BVA | `src/test/java/com/example/demo/dao/OrderReturnDAOTest.java` | `cancelOrder_restoresStockAndNeverMakesSalesNegative` | Automated (PASS) |
| **EP_CAN_VAL_01** | Equivalence Part. | `src/test/java/com/example/demo/controller/api/OrderCancelReturnApiControllerTest.java` | `cancelOrder_returnsSuccessfulDaoOutcome` | Automated (PASS) |
| **EP_CAN_VAL_02** | Equivalence Part. | `src/test/java/com/example/demo/controller/api/OrderCancelReturnApiControllerTest.java` | `createReturn_returnsCreatedDataForValidImageBoundary` | Automated (PASS) |
| **EP_CAN_VAL_03** | Equivalence Part. | `src/test/java/com/example/demo/dao/OrderReturnDAOTest.java` | `createReturnRequest_trimsFieldsPersistsAndTagsOrder` | Automated (PASS) |
| **EP_CAN_INV_01** | Equivalence Part. | `src/test/java/com/example/demo/controller/api/OrderCancelReturnApiControllerTest.java` | `cancelOrder_rejectsLoginRequiredAuthentication` | Automated (PASS) |
| **EP_CAN_INV_02** | Equivalence Part. | `src/test/java/com/example/demo/dao/OrderReturnDAOTest.java` | `cancelOrder_rejectsMissingOrder` | Automated (PASS) |
| **EP_CAN_INV_03** | Equivalence Part. | `src/test/java/com/example/demo/dao/OrderReturnDAOTest.java` | `cancelOrder_rejectsMissingOrDifferentCustomer` | Automated (PASS) |
| **EP_CAN_INV_04** | Equivalence Part. | `src/test/java/com/example/demo/dao/OrderReturnDAOTest.java` | `createReturnRequest_trimsFieldsPersistsAndTagsOrder` | Automated (PASS) |
| **EP_CAN_INV_05** | Equivalence Part. | `src/test/java/com/example/demo/dao/OrderReturnDAOTest.java` | `cancelOrder_rejectsEveryNonPendingStatus` | Automated (PASS) |

---

## 2. ÁNH XẠ VỚI HỆ THỐNG POSTMAN (E2E API TESTING)

Toàn bộ các luồng nghiệp vụ trên đều được kiểm thử End-to-End thông qua bộ Postman Collection:
- **File Thực thi Postman:** `docs/Shoeshop_API_Collection.json`
- **File Môi trường Postman:** `docs/Shoeshop_Postman_Environment.json`

### Bảng Ánh xạ Vị trí Request trong Postman Collection

| Nhóm API | Request Name (Tên API) | Vị trí (Line Number) | Ánh xạ Test Case |
| :--- | :--- | :--- | :--- |
| **User API** | `POST /api/v1/orders/{orderId}/cancel - Cancel Pending Order` | Dòng 1111 | `TC_CAN_ST_001`, `TC_CAN_DT_001..005`, `EP_CAN_VAL_01`, `EP_CAN_INV_01..05` |
| **User API** | `POST /api/v1/orders/{orderId}/return - Request Order Return & Refund` | Dòng 1136 | `TC_CAN_ST_002`, `TC_CAN_DT_006`, `TC_CAN_ROB_001..004`, `TC_CAN_BVA_001..008` |
| **User API** | `GET /api/v1/orders/{orderId}/return - View Return Request Details` | Dòng 1177 | Xem thông tin Trả hàng |
| **Admin API** | `PUT /api/v1/admin/orders/{orderId}/return-status - Approve/Reject Order Return Request` | Dòng 1611 | `TC_CAN_ST_004`, `TC_CAN_ST_005` |
