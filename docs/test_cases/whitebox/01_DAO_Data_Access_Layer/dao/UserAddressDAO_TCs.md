# ĐẶC TẢ KIỂM THỬ HỘP TRẮNG: `UserAddressDAO`
## Tầng: Data Access Object (DAO) | Phân hệ: Sổ địa chỉ giao hàng
* **Tổng số Test Case:** **23 Test Cases**
* **Mục đích tài liệu:** Bảng ánh xạ chi tiết giữa các hàm nghiệp vụ trong `UserAddressDAO.java` và các hàm kiểm thử trong `UserAddressDAOTest.java`, phục vụ tra cứu nhanh giá trị test đầu vào, nhánh logic mục tiêu và kết quả mong đợi.

---

## 📌 PHẦN 1: BẢNG TỔNG HỢP CÁC HÀM TRONG `UserAddressDAO.java`

Lớp `UserAddressDAO` quản lý 6 hàm nghiệp vụ chính được bao phủ bởi 23 test case trong `UserAddressDAOTest.java`:

| STT | Tên hàm trong `UserAddressDAO.java` | Mục đích nghiệp vụ | Số Test Case đối ứng |
| :---: | :--- | :--- | :---: |
| 1 | `getUserAddresses(String username)` | Lấy danh sách địa chỉ và sắp xếp địa chỉ mặc định lên đầu | **2** |
| 2 | `getAddressById(Long addressId)` | Tìm kiếm thông tin địa chỉ theo ID | **2** |
| 3 | `unsetPreviousDefault(String username)` | Bulk update hủy cờ mặc định (`isDefault = false`) toàn bộ địa chỉ cũ | **1** |
| 4 | `saveAddress(UserAddressForm, username)` | Thêm mới hoặc sửa địa chỉ, tự động gán mặc định cho địa chỉ đầu | **10** |
| 5 | `deleteAddress(Long addressId, String user)` | Xóa địa chỉ và tự động thăng hạng địa chỉ còn lại khi xóa default | **5** |
| 6 | `setDefaultAddress(Long addressId, String user)`| Chuyển cờ mặc định sang địa chỉ được chỉ định | **3** |

---

## 📑 PHẦN 2: BẢNG MA TRẬN ÁNH XẠ CHI TIẾT 23 TEST CASES

| Mã TC | Hàm ở `src/main` | Hàm kiểm thử ở `src/test` | Giá trị Test đầu vào (Inputs / Mock Data) | Nhánh logic / Điều kiện mục tiêu | Kết quả mong đợi (Assertion) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `TC_ADR_01` | `getUserAddresses` | `getUserAddresses_returnsEmptyForMissingUsername` | `username = null` hoặc `""` | Nhánh `if (username == null \|\| username.isEmpty())` | Trả về danh sách rỗng |
| `TC_ADR_02` | `getUserAddresses` | `getUserAddresses_bindsUsernameAndOrdersDefaultsFirst` | Username có 3 địa chỉ | Sinh HQL `ORDER BY isDefault DESC` | Địa chỉ có `isDefault = true` ở index 0 |
| `TC_ADR_03` | `getAddressById` | `getAddressById_returnsNullForNullId` | `addressId = null` | Nhánh `if (addressId == null)` = True | Trả về `null` |
| `TC_ADR_04` | `getAddressById` | `getAddressById_delegatesLookup` | `addressId = 1L` | Lệnh `session.find(UserAddress.class, id)` | Trả về entity `UserAddress` |
| `TC_ADR_05` | `unsetPreviousDefault` | `unsetPreviousDefault_executesBulkUpdate` | Bulk update username | Câu HQL `UPDATE ... SET isDefault = false` | Toàn bộ địa chỉ cũ mất cờ default |
| `TC_ADR_06` | `saveAddress` | `saveAddress_returnsNullForNullUsername` | `username = null` | Nhánh `if (username == null)` = True | Trả về `null` |
| `TC_ADR_07` | `saveAddress` | `saveAddress_returnsNullForNullForm` | `form = null` | Nhánh `if (form == null)` = True | Trả về `null` |
| `TC_ADR_08` | `saveAddress` | `saveAddress_makesFirstAddressDefaultAndUnsetsPreviousDefaults` | Địa chỉ đầu tiên của User | Nhánh `if (totalCount == 0)` = True | Tự động set `isDefault = true` |
| `TC_ADR_09` | `saveAddress` | `saveAddress_keepsNonFirstAddressNonDefaultWhenNotRequested` | Địa chỉ thứ 2 không chọn default | Nhánh `!isFirst && !requestedDefault` | Giữ nguyên `isDefault = false` |
| `TC_ADR_10` | `saveAddress` | `saveAddress_requestedDefaultUnsetsOldDefault` | Địa chỉ mới có tích chọn default | Hủy cờ cũ ➔ set cờ cho địa chỉ mới | Đổi default thành công |
| `TC_ADR_11` | `saveAddress` | `saveAddress_updatesOwnedAddressAndTrimsFields` | Sửa địa chỉ của chính mình | Cắt khoảng trắng các trường ➔ lưu entity | Cập nhật thông tin địa chỉ |
| `TC_ADR_12` | `saveAddress` | `saveAddress_rejectsForeignAddress` | Sửa địa chỉ của User khác | Nhánh `!addr.getUser().equals(currUser)` | Bị từ chối cập nhật |
| `TC_ADR_13` | `saveAddress` | `saveAddress_currentlyUnsetsDefaultsBeforeRejectingForeignAddress_characterization` | Kiểm thử đặc tính hủy default | Ghi nhận đúng thứ tự thực thi mã nguồn | Khớp đặc tính hệ thống |
| `TC_ADR_14` | `saveAddress` | `saveAddress_createsNewAddressWhenRequestedIdDoesNotExist` | ID form gửi không có trong DB | Nhánh `if (existing == null)` tạo mới | Tạo bản ghi mới |
| `TC_ADR_15` | `saveAddress` | `saveAddress_currentlyThrowsForNullRequiredField_characterization` | Thiếu trường bắt buộc | Ngoại lệ validation tầng CSDL | Ném ngoại lệ |
| `TC_ADR_16` | `deleteAddress` | `deleteAddress_returnsFalseWhenMissing` | ID không tồn tại | Nhánh `if (addr == null)` = True | Trả về `false` |
| `TC_ADR_17` | `deleteAddress` | `deleteAddress_returnsFalseForForeignOwner` | Xóa địa chỉ của User khác | Nhánh kiểm tra chủ sở hữu = False | Trả về `false` |
| `TC_ADR_18` | `deleteAddress` | `deleteAddress_removesNonDefaultWithoutPromotion` | Xóa địa chỉ phụ (`isDefault = false`) | Nhánh xóa không thăng hạng | Xóa thành công, giữ nguyên default |
| `TC_ADR_19` | `deleteAddress` | `deleteAddress_doesNotPromoteWhenNoAddressRemains` | Xóa địa chỉ duy nhất còn lại | Nhánh `if (remainingList.isEmpty())` | Số địa chỉ về 0 |
| `TC_ADR_20` | `deleteAddress` | `deleteAddress_promotesFirstRemainingAddress` | Xóa đúng địa chỉ default | Thuật toán thăng hạng địa chỉ còn lại | Địa chỉ còn lại tự thành default |
| `TC_ADR_21` | `setDefaultAddress` | `setDefaultAddress_returnsFalseWhenMissing` | ID không tồn tại | Nhánh `if (addr == null)` = True | Trả về `false` |
| `TC_ADR_22` | `setDefaultAddress` | `setDefaultAddress_returnsFalseForForeignOwner` | Đặt default cho địa chỉ người khác | Nhánh kiểm tra quyền chủ sở hữu | Trả về `false` |
| `TC_ADR_23` | `setDefaultAddress` | `setDefaultAddress_unsetsOldDefaultAndUpdatesTarget` | Địa chỉ hợp lệ của User | Hủy cờ cũ ➔ set cờ cho bản ghi chỉ định | Đổi default thành công |

---

## 📊 PHẦN 3: KẾT QUẢ ĐO LƯỜNG ĐỘ BAO PHỦ (JACOCO METRICS)

* **Lớp kiểm thử:** `com.example.demo.dao.UserAddressDAO`
* **Statement Coverage (Instructions):** **280 / 284 (98.6%)**
* **Branch Coverage (Branches):** **38 / 38 (100.0%)**
* **Đánh giá:** Đạt độ phủ nhánh tuyệt đối 100%. Bao phủ trọn vẹn thuật toán hoán đổi và tự thăng hạng cờ `isDefault`.
