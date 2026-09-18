# ĐẶC TẢ KIỂM THỬ HỘP TRẮNG: `AccountDAO`
## Tầng: Data Access Object (DAO) | Phân hệ: Quản lý Tài khoản & Xác thực
* **Tổng số Test Case:** **14 Test Cases**
* **Mục đích tài liệu:** Bảng ánh xạ chi tiết giữa các hàm nghiệp vụ trong `AccountDAO.java` và các hàm kiểm thử trong `AccountDAOTest.java`, phục vụ tra cứu nhanh giá trị test đầu vào, nhánh logic mục tiêu và kết quả mong đợi.

---

## 📌 PHẦN 1: BẢNG TỔNG HỢP CÁC HÀM TRONG `AccountDAO.java`

Lớp `AccountDAO` quản lý 8 hàm nghiệp vụ chính được bao phủ bởi 14 test case trong `AccountDAOTest.java`:

| STT | Tên hàm trong `AccountDAO.java` | Mục đích nghiệp vụ | Số Test Case đối ứng |
| :---: | :--- | :--- | :---: |
| 1 | `findAccount(String userName)` | Tìm kiếm tài khoản theo username | **2** |
| 2 | `findAccountByEmail(String email)` | Tìm kiếm tài khoản theo email | **2** |
| 3 | `findAccountByResetToken(String resetToken)` | Tìm tài khoản bằng token SHA-256 còn hạn | **2** |
| 4 | `savePasswordResetToken(Account, token, exp)`| Lưu token reset băm SHA-256 và thời hạn | **2** |
| 5 | `resetPassword(rawToken, encodedPass)` | Cập nhật mật khẩu mới nguyên tử | **3** |
| 6 | `saveAccount(Account account)` | Cập nhật thông tin và ngày sửa `updatedAt` | **1** |
| 7 | `listAccounts(page, maxResult, maxNav)` | Lấy danh sách tài khoản phân trang DESC | **1** |
| 8 | `countActiveAdmins()` | Đếm số lượng tài khoản Admin đang hoạt động | **1** |

---

## 📑 PHẦN 2: BẢNG MA TRẬN ÁNH XẠ CHI TIẾT 14 TEST CASES

| Mã TC | Hàm ở `src/main` | Hàm kiểm thử ở `src/test` | Giá trị Test đầu vào (Inputs / Mock Data) | Nhánh logic / Điều kiện mục tiêu | Kết quả mong đợi (Assertion) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `TC_ACC_01` | `findAccount` | `findAccount_delegatesMissingUsernameWithoutGuard_characterization` | `userName = null` | Lệnh `session.find(Account.class, null)` | Trả về `null` |
| `TC_ACC_02` | `findAccount` | `findAccount_preservesUsernameWithoutNormalization_characterization` | `userName = "Admin_Shoe"` | Bảo toàn chuỗi hoa/thường không can thiệp | Query đúng nguyên vẹn chuỗi |
| `TC_ACC_03` | `findAccountByEmail` | `findAccountByEmail_rejectsMissingEmail` | `email = null` hoặc `email = "   "` | Nhánh `if (email == null \|\| email.trim().isEmpty())` | Trả về `null` |
| `TC_ACC_04` | `findAccountByEmail` | `findAccountByEmail_preservesNonBlankEmailWithoutNormalization_characterization` | `email = "user@example.com"` | Query `WHERE e.email = :email` | Trả về entity `Account` |
| `TC_ACC_05` | `findAccountByResetToken` | `findAccountByResetToken_rejectsMissingToken` | `resetToken = null` hoặc `""` | Nhánh `if (resetToken == null \|\| resetToken.trim().isEmpty())` | Trả về `null` |
| `TC_ACC_06` | `findAccountByResetToken` | `findAccountByResetToken_hashesTrimmedTokenAndUsesCurrentTime` | `resetToken = "my_token"` | Băm SHA-256 token và bind tham số `now` | Tìm thấy account có token khớp và còn hạn |
| `TC_ACC_07` | `savePasswordResetToken` | `savePasswordResetToken_rejectsIncompleteInput` | `account = null` hoặc `token = null` hoặc `expiresAt = null` | Nhánh kiểm tra thiếu bất kỳ trường thông tin nào | Ném ngoại lệ `IllegalArgumentException` |
| `TC_ACC_08` | `savePasswordResetToken` | `savePasswordResetToken_storesHashAndExpiry` | `account` hợp lệ, `rawToken = "tok123"`, `expiresAt = +30m` | Băm token và gán expiry | `account.getResetToken()` lưu mã băm SHA-256 |
| `TC_ACC_09` | `resetPassword` | `resetPassword_returnsFalseForInvalidInput` | `rawToken = null`, `pass = null`, `rawToken = "   "` | Nhánh kiểm tra token hoặc mật khẩu rỗng/null | Trả về `false` |
| `TC_ACC_10` | `resetPassword` | `resetPassword_returnsFalseUnlessExactlyOneTokenIsConsumed` | Token hết hạn / sai token (`executeUpdate() = 0`) | Nhánh `query.executeUpdate() == 1` nhận kết quả False | Trả về `false` |
| `TC_ACC_11` | `resetPassword` | `resetPassword_returnsTrueAndSetsAtomicUpdateParameters` | `rawToken = "valid_tok"`, `encodedPass = "$2a$10..."` | Nhánh `query.executeUpdate() == 1` nhận kết quả True | Trả về `true`, đổi pass và hủy token |
| `TC_ACC_12` | `saveAccount` | `saveAccount_refreshesUpdatedAtAndDelegatesPersistence` | `account` hợp lệ | Lệnh `account.setUpdatedAt(new Date())` | Cập nhật `updatedAt` thành công |
| `TC_ACC_13` | `listAccounts` | `listAccounts_buildsPaginationFromDescendingCreatedDateQuery` | `page = 1, maxResult = 10, maxNav = 5` | Sinh HQL `ORDER BY a.createdAt desc` | Trả về đối tượng `PaginationResult` sắp xếp DESC |
| `TC_ACC_14` | `countActiveAdmins` | `countActiveAdmins_returnsTypedAggregate` | Hệ thống có 2 Admin active | Câu lệnh `query.uniqueResult()` ép kiểu `Long` | Trả về số lượng `2L` |

---

## 📊 PHẦN 3: KẾT QUẢ ĐO LƯỜNG ĐỘ BAO PHỦ (JACOCO METRICS)

* **Lớp kiểm thử:** `com.example.demo.dao.AccountDAO`
* **Statement Coverage (Instructions):** **265 / 272 (97.4%)**
* **Branch Coverage (Branches):** **26 / 26 (100.0%)**
* **Đánh giá:** Đạt độ phủ nhánh tuyệt đối 100%. 7 instructions chưa bao phủ là khối `catch (NoSuchAlgorithmException e)` mang tính phòng vệ khi JVM không hỗ trợ thuật toán SHA-256.
