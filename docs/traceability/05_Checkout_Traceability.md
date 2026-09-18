# Ma trận Truy xuất (Traceability Matrix): Chức năng 5 - Thanh toán & Đặt hàng (Checkout)

Tài liệu này ánh xạ các Test Case Đặc tả (từ docs/test_cases/05_Checkout_Order_Placement.md và Testing.xlsx Sheet 5) với các Test Script (File Java Automation Test & Newman/Postman Test) thực tế trong mã nguồn.

## 1. Ánh xạ Test Case với Unit / Integration Tests (JUnit 5 & Mockito)

| Mã Test Case | Nhóm Kỹ thuật | File Test (Test Script Location) | Tên Hàm Test (Method Name) | Trạng thái |
| :--- | :--- | :--- | :--- | :---: |
| **TC_CHK_001** | State Transition (B1 -> B2) | com.example.demo.controller.OrderControllerTest | checkout_emptyCart_redirectsToCart | Automated |
| **TC_CHK_002** | State Transition (B2 -> B3) | com.example.demo.validator.CustomerFormValidatorTest | alidate_invalidEmail_rejectsPatternCode | Automated |
| **TC_CHK_003** | State Transition (B2 -> B3) | com.example.demo.validator.CustomerFormValidatorTest | alidate_validCustomer_normalizesInputAndHasNoErrors | Automated |
| **TC_CHK_004** | State Transition (B3 -> B4) | com.example.demo.OrderWorkflowIntegrationTest | checkoutFlow_success_createsOrderAndClearsCart | Automated |
| **TC_CHK_005** | Decision Table (Rule 1: Empty) | com.example.demo.controller.OrderControllerTest | checkout_emptyCart_rejectsOrderCreation | Automated |
| **TC_CHK_006** | Decision Table (Rule 2: Form) | com.example.demo.validator.CustomerFormValidatorTest | alidate_invalidForm_hasErrors | Automated |
| **TC_CHK_007** | Decision Table (Rule 3: Line) | com.example.demo.OrderWorkflowIntegrationTest | checkout_zeroQuantityLine_throwsIllegalArgumentException | Automated |
| **TC_CHK_008** | Decision Table (Rule 4: Inactive) | com.example.demo.OrderWorkflowIntegrationTest | checkout_inactiveProduct_throwsIllegalStateException | Automated |
| **TC_CHK_009** | Decision Table (Rule 5: Stock) | com.example.demo.OrderWorkflowIntegrationTest | checkout_insufficientStock_throwsIllegalStateException | Automated |
| **TC_CHK_010** | Decision Table (Rule 6: Voucher) | com.example.demo.OrderWorkflowIntegrationTest | checkout_invalidVoucher_rejectsBeforeSavingOrder | Automated |
| **TC_CHK_011** | Decision Table (Rule 7: Happy) | com.example.demo.OrderWorkflowIntegrationTest | checkout_happyPath_persistsOrderAndUpdatesInventory | Automated |
| **TC_CHK_012** | Threshold BVA (Threshold 127) | scripts/test_order_bva_127_128.py | 	est_order_name_127_valid | Automated |
| **TC_CHK_013** | BVA Nominal (50, 50, 46, 15) | com.example.demo.validator.CustomerFormValidatorTest | alidate_validCustomer_normalizesInputAndHasNoErrors | Automated |
| **TC_CHK_014 - 017** | Standard BVA (name 1, 2, 254, 255) | com.example.demo.validator.CustomerFormValidatorTest | alidate_nameAtMaximumLength_hasNoNameError | Automated |
| **TC_CHK_ROB_001 - 002** | Robust BVA (name 0, 256) | com.example.demo.validator.CustomerFormValidatorTest | alidate_nameOverMaximumLength_rejectsLengthCode | Automated |
| **TC_CHK_018 - 021** | Standard BVA (addr 1, 2, 254, 255) | com.example.demo.validator.CustomerFormValidatorTest | alidate_addressAtMaximumLength_hasNoAddressError | Automated |
| **TC_CHK_ROB_003 - 004** | Robust BVA (addr 0, 256) | com.example.demo.validator.CustomerFormValidatorTest | alidate_addressOverMaximumLength_rejectsLengthCode | Automated |
| **TC_CHK_022 - 025** | Standard BVA (email 6, 7, 127, 128) | com.example.demo.validator.CustomerFormValidatorTest | alidate_emailAtMaximumLength_hasNoEmailError | Automated |
| **TC_CHK_ROB_005 - 006** | Robust BVA (email 5, 129) | com.example.demo.validator.CustomerFormValidatorTest | alidate_emailOverMaximumLength_rejectsOnlyLengthCode | Automated |
| **TC_CHK_026 - 029** | Standard BVA (phone 1, 2, 127, 128) | com.example.demo.validator.CustomerFormValidatorTest | alidate_phoneAtMaximumLength_hasNoPhoneError | Automated |
| **TC_CHK_ROB_007 - 008** | Robust BVA (phone 0, 129) | com.example.demo.validator.CustomerFormValidatorTest | alidate_phoneOverMaximumLength_rejectsLengthCode | Automated |
| **EP_CHK_VAL_01 - 03** | Valid Equivalence Partitions | com.example.demo.OrderWorkflowIntegrationTest | checkout_validPartitions_success | Automated |
| **EP_CHK_INV_01 - 11** | Invalid Partitions (Ký tự cấm, XSS, SQLi) | com.example.demo.validator.CustomerFormValidatorTest | alidate_dangerousCharacters_rejected | Automated |

---

## 2. Ánh xạ với Hệ thống Postman / Newman (E2E API Testing)

- **File Thực thi Postman:** docs/Shoeshop_API_Collection.json
- **File Môi trường Postman:** docs/Shoeshop_Postman_Environment.json

| Nhóm API | Request Name (Tên API) | Method / Endpoint | Ánh xạ Test Case |
| :--- | :--- | :--- | :--- |
| **Order API** | POST /api/v1/orders/checkout | POST /api/v1/orders/checkout | TC_CHK_001 $\rightarrow$ 011, EP_CHK_VAL_01 $\rightarrow$ 03 |
| **Cart API** | POST /api/v1/cart/add | POST /api/v1/cart/add | TC_CHK_001, TC_CHK_004 |
| **Validation API** | POST /api/v1/orders/validate | POST /api/v1/orders/validate | TC_CHK_012 $\rightarrow$ 038, EP_CHK_INV_01 $\rightarrow$ 11 |
