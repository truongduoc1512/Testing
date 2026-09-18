# Ma trận Truy xuất (Traceability Matrix): Chức năng 10 - Sổ địa chỉ (Address Book)

Tài liệu này ánh xạ các Test Case Đặc tả (từ docs/test_cases/10_Address_Book.md và Testing.xlsx Sheet 10) với các Test Script (File Java Automation Test & Newman/Postman Test) thực tế trong mã nguồn.

## 1. Ánh xạ Test Case với Unit / Integration Tests (JUnit 5 & Mockito)

| Mã Test Case | Nhóm Kỹ thuật | File Test (Test Script Location) | Tên Hàm Test (Method Name) | Trạng thái |
| :--- | :--- | :--- | :--- | :---: |
| **TC_ADR_001** | State Transition (Tạo địa chỉ đầu tiên) | com.example.demo.dao.UserAddressDAOTest | saveAddress_makesFirstAddressDefaultAndUnsetsPreviousDefaults | Automated |
| **TC_ADR_002** | State Transition (Tạo địa chỉ thứ 2) | com.example.demo.dao.UserAddressDAOTest | saveAddress_keepsNonFirstAddressNonDefaultWhenNotRequested | Automated |
| **TC_ADR_003** | State Transition (Đổi địa chỉ mặc định) | com.example.demo.dao.UserAddressDAOTest | setDefaultAddress_unsetsOldDefaultAndUpdatesTarget | Automated |
| **TC_ADR_004** | State Transition (Xóa địa chỉ phụ) | com.example.demo.dao.UserAddressDAOTest | deleteAddress_removesNonDefaultWithoutPromotion | Automated |
| **TC_ADR_005** | State Transition (Xóa địa chỉ mặc định -> Tự thăng cấp) | com.example.demo.dao.UserAddressDAOTest | deleteAddress_promotesFirstRemainingAddress | Automated |
| **TC_ADR_006** | Decision Table (Rule 1: Chưa đăng nhập) | com.example.demo.controller.api.UserAddressApiControllerTest | createAddress_requiresAuthentication | Automated |
| **TC_ADR_007** | Decision Table (Rule 2: Form trống) | com.example.demo.controller.api.UserAddressApiControllerTest | updateAddress_rejectsInvalidOwnedAddressForm | Automated |
| **TC_ADR_008** | Decision Table (Rule 3: Vượt độ dài) | com.example.demo.controller.api.UserAddressApiControllerTest | createAddress_rejectsOverLengthFields | Automated |
| **TC_ADR_009** | Decision Table (Rule 4: Không chính chủ) | com.example.demo.controller.api.UserAddressApiControllerTest | updateAddress_forbidsAddressOwnedByAnotherUser | Automated |
| **TC_ADR_010** | Decision Table (Rule 5: Tạo mới thành công) | com.example.demo.controller.api.UserAddressApiControllerTest | getAddresses_returnsAuthenticatedUserList | Automated |
| **TC_ADR_011** | Decision Table (Rule 6: Cập nhật thành công) | com.example.demo.controller.api.UserAddressApiControllerTest | setDefault_returnsSuccessWhenDaoUpdatesAddress | Automated |
| **TC_ADR_012** | Decision Table (Rule 7: Đổi mặc định) | com.example.demo.dao.UserAddressDAOTest | saveAddress_requestedDefaultUnsetsOldDefault | Automated |
| **TC_ADR_013** | Decision Table (Rule 8: Xóa thành công) | com.example.demo.controller.api.UserAddressApiControllerTest | deleteAddress_returnsSuccessWhenDaoDeletesAddress | Automated |
| **TC_ADR_BVA_001** | BVA Nominal (6 trường chuẩn) | com.example.demo.controller.api.UserAddressApiControllerTest | createAddress_nominalLength_success | Automated |
| **TC_ADR_BVA_002 - 005** | Standard BVA (receiverName 1, 2, 99, 100) | com.example.demo.controller.api.UserAddressApiControllerTest | createAddress_receiverNameBoundaries_success | Automated |
| **TC_ADR_ROB_001 - 002** | Robust BVA (receiverName 0, 101) | com.example.demo.controller.api.UserAddressApiControllerTest | createAddress_receiverNameRobustness_rejected | Automated |
| **TC_ADR_BVA_006 - 009** | Standard BVA (phone 1, 2, 19, 20) | com.example.demo.controller.api.UserAddressApiControllerTest | createAddress_phoneBoundaries_success | Automated |
| **TC_ADR_ROB_003 - 004** | Robust BVA (phone 0, 21) | com.example.demo.controller.api.UserAddressApiControllerTest | createAddress_phoneRobustness_rejected | Automated |
| **TC_ADR_BVA_010 - 013** | Standard BVA (province 1, 2, 99, 100) | com.example.demo.controller.api.UserAddressApiControllerTest | createAddress_provinceBoundaries_success | Automated |
| **TC_ADR_ROB_005 - 006** | Robust BVA (province 0, 101) | com.example.demo.controller.api.UserAddressApiControllerTest | createAddress_provinceRobustness_rejected | Automated |
| **TC_ADR_BVA_014 - 017** | Standard BVA (district 1, 2, 99, 100) | com.example.demo.controller.api.UserAddressApiControllerTest | createAddress_districtBoundaries_success | Automated |
| **TC_ADR_ROB_007 - 008** | Robust BVA (district 0, 101) | com.example.demo.controller.api.UserAddressApiControllerTest | createAddress_districtRobustness_rejected | Automated |
| **TC_ADR_BVA_018 - 021** | Standard BVA (ward 1, 2, 99, 100) | com.example.demo.controller.api.UserAddressApiControllerTest | createAddress_wardBoundaries_success | Automated |
| **TC_ADR_ROB_009 - 010** | Robust BVA (ward 0, 101) | com.example.demo.controller.api.UserAddressApiControllerTest | createAddress_wardRobustness_rejected | Automated |
| **TC_ADR_BVA_022 - 025** | Standard BVA (streetAddress 1, 2, 254, 255) | com.example.demo.controller.api.UserAddressApiControllerTest | createAddress_streetAddressBoundaries_success | Automated |
| **TC_ADR_ROB_011 - 012** | Robust BVA (streetAddress 0, 256) | com.example.demo.controller.api.UserAddressApiControllerTest | createAddress_streetAddressRobustness_rejected | Automated |
| **EP_ADR_VAL_01 - 03** | Valid Partitions (CRUD, Unicode tiếng Việt, Auto Promotion) | com.example.demo.dao.UserAddressDAOTest | saveAddress_updatesOwnedAddressAndTrimsFields | Automated |
| **EP_ADR_INV_01 - 07** | Invalid Partitions (Auth 401, Form 400, Owner 403, Ký tự cấm, SQLi) | com.example.demo.controller.api.UserAddressApiControllerTest | createAddress_dangerousCharacters_rejected | Automated |

---

## 2. Ánh xạ với Hệ thống Postman / Newman (E2E API Testing)

- **File Thực thi Postman:** docs/Shoeshop_API_Collection.json
- **File Môi trường Postman:** docs/Shoeshop_Postman_Environment.json

| Nhóm API | Request Name (Tên API) | Method / Endpoint | Ánh xạ Test Case |
| :--- | :--- | :--- | :--- |
| **Address API** | GET /api/v1/user/addresses - List All Addresses | GET /api/v1/user/addresses | TC_ADR_001, TC_ADR_010, EP_ADR_VAL_01 |
| **Address API** | POST /api/v1/user/addresses - Create New Address | POST /api/v1/user/addresses | TC_ADR_001 $\rightarrow$ 002, BVA/ROB TCs, EP_ADR_INV_04 $\rightarrow$ 07 |
| **Address API** | PUT /api/v1/user/addresses/{id} - Update Address | PUT /api/v1/user/addresses/{id} | TC_ADR_007 $\rightarrow$ 009, TC_ADR_011 |
| **Address API** | PUT /api/v1/user/addresses/{id}/default - Set Default Address | PUT /api/v1/user/addresses/{id}/default | TC_ADR_003, TC_ADR_012 |
| **Address API** | DELETE /api/v1/user/addresses/{id} - Delete Address | DELETE /api/v1/user/addresses/{id} | TC_ADR_004 $\rightarrow$ 005, TC_ADR_013 |
