# BẢNG MA TRẬN KIỂM THỬ TÍCH HỢP ADDRESS BOOK (SPRING BOOT INTEGRATION)
**Đường dẫn file mã nguồn:** [UserAddressDAO.java](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/src/main/java/com/example/demo/dao/UserAddressDAO.java)  
**Đường dẫn file kiểm thử:** [AddressBookTests.java](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/src/test/java/com/example/demo/AddressBookTests.java)

---

## 1. TỔNG QUAN ĐẶC ĐIỂM KIỂM THỬ TÍCH HỢP
* **Phạm vi kiểm thử:** Tích hợp tầng DAO với Hibernate Session/EntityManager và CSDL quan hệ trong ngữ cảnh Spring Boot (`@SpringBootTest`, `@Transactional`).
* **Mục tiêu:** Xác thực tính toàn vẹn dữ liệu, cơ chế tự động chuyển giao trạng thái địa chỉ mặc định (`defaultAddress`), và cơ chế bảo mật cô lập dữ liệu người dùng khi xóa.

---

## 2. MA TRẬN TEST CASES KIỂM THỬ TÍCH HỢP (4 TEST CASES)

| Mã TC | Hàm ở src/main | Hàm kiểm thử ở src/test | Giá trị Test đầu vào | Kịch bản tích hợp / Logic mục tiêu | Kết quả mong đợi |
| :---: | :--- | :--- | :--- | :--- | :--- |
| **TC_ITG_ADB_01** | `saveAddress(String, UserAddressForm)` | `saveAddress_marksFirstAddressAsDefault()` | `username = "unit_user_1"`, form có `defaultAddress = false` | Lưu địa chỉ đầu tiên của user: hệ thống tự động gán làm địa chỉ mặc định nếu user chưa có địa chỉ nào | Lưu thành công, `saved != null` và `saved.isDefault() == true` |
| **TC_ITG_ADB_02** | `saveAddress(String, UserAddressForm)` | `saveAddress_unsetsPreviousDefaultAddress()` | `username = "unit_user_2"`, lưu địa chỉ 1 làm mặc định, sau đó lưu tiếp địa chỉ 2 làm mặc định | Tích hợp Persistence Context (`flush & clear`): khi địa chỉ mới được đặt làm mặc định, địa chỉ mặc định cũ tự động bị hủy cờ mặc định | Địa chỉ 1 có `isDefault() == false`, địa chỉ 2 có `isDefault() == true` |
| **TC_ITG_ADB_03** | `deleteAddress(String, Long)` | `deleteAddress_rejectsNonOwnerAndPreservesAddress()` | Địa chỉ thuộc sở hữu của `owner_user`, thực hiện lệnh xóa bởi `intruder_user` | Kiểm tra bảo vệ quyền sở hữu đa người dùng (IDOR) trực tiếp trong CSDL: chặn xóa dữ liệu của user khác | Trả về `false`, địa chỉ của `owner_user` vẫn tồn tại nguyên vẹn trong DB |
| **TC_ITG_ADB_04** | `deleteAddress(String, Long)` | `deleteAddress_allowsOwnerAndRemovesAddress()` | Địa chỉ thuộc sở hữu của `owner_user`, thực hiện lệnh xóa bởi chính `owner_user` | Người dùng chính chủ xóa địa chỉ của mình khỏi CSDL | Trả về `true`, truy vấn lại địa chỉ theo Id trả về `null` |
