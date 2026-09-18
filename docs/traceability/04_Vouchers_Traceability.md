# Ma trận Truy xuất (Traceability Matrix): Chức năng 4 - Quản lý & Áp dụng Voucher

Tài liệu này ánh xạ toàn bộ 33 Test Cases Đặc tả (từ `docs/test_cases/04_Vouchers.md` và `Testing.xlsx` Sheet 4) với các Test Script (JUnit, Mockito, API Postman và Visual Test) thực tế trong mã nguồn.

## 1. Ánh xạ Test Case với Unit / Integration Tests (JUnit 5 & Mockito)

| Mã Test Case | Nhóm Kỹ thuật | File Test (Test Script Location) | Tên Hàm Test (Method Name) | Trạng thái |
| :--- | :--- | :--- | :--- | :---: |
| **TC_VOU_ST_001** | State Transition (S0 -> S1) | `VoucherApiControllerTest.java` | `createVoucher_savesAndReturnsCreatedEntity` | Automated |
| **TC_VOU_ST_002** | State Transition (S1 -> S1) | `VoucherTests.java` | `testPercentageDiscountWithMaxDiscountCap` | Automated |
| **TC_VOU_ST_003** | State Transition (S1 -> S2) | `VoucherTests.java` | `testUsageLimitRejection` | Automated |
| **TC_VOU_ST_004** | State Transition (S1 -> S3) | `VoucherTests.java` | `testExpiredVoucherRejection` | Automated |
| **TC_VOU_ST_005** | State Transition (S1 -> S4) | `VoucherApiControllerTest.java` | `deleteVoucher_returnsSuccessWhenDaoDeletes` | Automated |
| **TC_VOU_DT_001** | Decision Table (Rule 1: Unknown) | `VoucherDAOTest.java` | `validateAndApplyVoucher_rejectsUnknownVoucher` | Automated |
| **TC_VOU_DT_002** | Decision Table (Rule 2: Inactive) | `VoucherDAOTest.java` | `validateAndApplyVoucher_rejectsInactiveVoucher` | Automated |
| **TC_VOU_DT_003** | Decision Table (Rule 3: Expired) | `VoucherTests.java` | `testExpiredVoucherRejection` | Automated |
| **TC_VOU_DT_004** | Decision Table (Rule 4: MinOrder) | `VoucherTests.java` | `testMinimumOrderValueRejection` | Automated |
| **TC_VOU_DT_005** | Decision Table (Rule 5: Limit) | `VoucherTests.java` | `testUsageLimitRejection` | Automated |
| **TC_VOU_DT_006** | Decision Table (Rule 6: Guest) | `VoucherDAOTest.java` | `validateAndApplyVoucher_skipsPerUserLimitForGuest` | Automated |
| **TC_VOU_DT_007** | Decision Table (Rule 7: PerUser) | `VoucherDAOTest.java` | `validateAndApplyVoucher_enforcesPerUserUsageBoundary` | Automated |
| **TC_VOU_DT_008** | Decision Table (Rule 8: Happy) | `VoucherTests.java` | `testPercentageDiscountWithMaxDiscountCap` | Automated |
| **TC_VOU_BVA_001** | BVA Nominal (500k, 20% max 50k) | `VoucherTests.java` | `testPercentageDiscountWithMaxDiscountCap` | Automated |
| **TC_VOU_ROB_001** | Robust BVA (orderAmount 499k) | `VoucherTests.java` | `testMinimumOrderValueRejection` | Automated |
| **TC_VOU_BVA_002 - 003** | Standard BVA (orderAmount 500k, 501k)| `VoucherDAOTest.java` | `validateAndApplyVoucher_acceptsMinOrderValueBoundary` | Automated |
| **TC_VOU_BVA_004 - 005** | Standard BVA (usedCount 49, 50) | `VoucherDAOTest.java` | `validateAndApplyVoucher_acceptsWithinUsageLimit` | Automated |
| **TC_VOU_ROB_002** | Robust BVA (usedCount 51) | `VoucherTests.java` | `testUsageLimitRejection` | Automated |
| **TC_VOU_BVA_006 - 007** | Standard BVA (userUsed 0, 1) | `VoucherDAOTest.java` | `validateAndApplyVoucher_enforcesPerUserUsageBoundary` | Automated |
| **TC_VOU_ROB_003** | Robust BVA (userUsed 2) | `VoucherDAOTest.java` | `validateAndApplyVoucher_enforcesPerUserUsageBoundary` | Automated |
| **TC_VOU_BVA_008 - 009** | Standard BVA (percent 1%, 100%) | `VoucherDAOTest.java` | `validateAndApplyVoucher_calculatesPercentageDiscount` | Automated |
| **EP_VOU_VAL_01** | EP Valid (% Max Cap) | `VoucherTests.java` | `testPercentageDiscountWithMaxDiscountCap` | Automated |
| **EP_VOU_VAL_02** | EP Valid (Fixed Amount 30k) | `VoucherTests.java` | `testFixedDiscountCalculation` | Automated |
| **EP_VOU_VAL_03** | EP Valid (Trim & Uppercase) | `VoucherDAOTest.java` | `validateAndApplyVoucher_trimsAndUpperCasesCode` | Automated |
| **EP_VOU_VAL_04** | EP Valid (Admin Create API) | `VoucherApiControllerTest.java` | `createVoucher_savesAndReturnsCreatedEntity` | Automated |
| **EP_VOU_VAL_05** | EP Valid (Customer List API) | `VoucherApiControllerTest.java` | `getActiveVouchers_returnsDaoResult` | Automated |
| **EP_VOU_INV_01** | EP Invalid (SQL Injection / XSS) | `VoucherDAOTest.java` | `validateAndApplyVoucher_rejectsUnknownVoucher` | Automated |
| **EP_VOU_INV_02** | EP Invalid (Role Security 403) | `VoucherApiControllerTest.java` | `createVoucher_unauthorized_returnsForbidden` | Automated |
| **EP_VOU_INV_03** | EP Invalid (Invalid Form 400) | `VoucherApiControllerTest.java` | `createVoucher_invalidForm_returnsBadRequest` | Automated |

---

## 2. Ánh xạ với Hệ thống Postman / Newman (E2E API Testing)

- **File Thực thi Postman:** `docs/Shoeshop_API_Collection.json`
- **File Môi trường Postman:** `docs/Shoeshop_Postman_Environment.json`

| Nhóm API | Request Name (Tên API) | Vị trí (Line Number) | Ánh xạ Test Case |
| :--- | :--- | :--- | :--- |
| **User API** | `GET /api/v1/vouchers - Get Available Vouchers` | Dòng 1274 | EP_VOU_VAL_05 |
| **User API** | `POST /api/v1/vouchers/apply - Apply Voucher Code to Order` | Dòng 1305 | TC_VOU_DT_001 $\rightarrow$ 008, BVA, EP |
| **Admin API** | `GET /api/v1/admin/vouchers - Admin List All Vouchers` | Dòng 1652 | N/A (Xem danh sách) |
| **Admin API** | `POST /api/v1/admin/vouchers - Admin Create New Voucher Code` | Dòng 1677 | TC_VOU_ST_001, EP_VOU_VAL_04 |
| **Admin API** | `DELETE /api/v1/admin/vouchers/{code} - Admin Deactivate Voucher Code` | Dòng 1722 | TC_VOU_ST_005 |
