# ĐẶC TẢ KIỂM THỬ HỘP TRẮNG: `VoucherDAO`
## Tầng: Data Access Object (DAO) | Phân hệ: Quản lý Khuyến mãi & Voucher
* **Tổng số Test Case:** **27 Test Cases**
* **Mục đích tài liệu:** Bảng ánh xạ chi tiết giữa các hàm nghiệp vụ trong `VoucherDAO.java` và các hàm kiểm thử trong `VoucherDAOTest.java`, phục vụ tra cứu nhanh giá trị test đầu vào, nhánh logic mục tiêu và kết quả mong đợi.

---

## 📌 PHẦN 1: BẢNG TỔNG HỢP CÁC HÀM TRONG `VoucherDAO.java`

Lớp `VoucherDAO` quản lý 9 hàm nghiệp vụ chính được bao phủ bởi 27 test case trong `VoucherDAOTest.java`:

| STT | Tên hàm trong `VoucherDAO.java` | Mục đích nghiệp vụ | Số Test Case đối ứng |
| :---: | :--- | :--- | :---: |
| 1 | `findVoucher(String code)` | Tìm voucher theo mã và chuẩn hóa chuỗi hoa/thường | **2** |
| 2 | `listActiveVouchers()` | Lấy danh sách voucher còn hạn và còn quota toàn sàn | **1** |
| 3 | `listAllVouchers()` | Lấy toàn bộ voucher sắp xếp theo ngày tạo mới nhất | **1** |
| 4 | `saveVoucher(VoucherForm form)` | Tạo mới hoặc cập nhật thông tin voucher | **2** |
| 5 | `deleteVoucher(Long id)` | Xóa mềm voucher (`active = false`) | **2** |
| 6 | `getUserVoucherUsageCount(user, code)` | Đếm số lần một user đã sử dụng một mã voucher | **2** |
| 7 | `recordVoucherUsage(code, username)` | Tăng lượt dùng voucher và lưu log sử dụng | **2** |
| 8 | `validateAndApplyVoucher(code, amount, user)` | Kiểm tra điều kiện áp mã và tính toán số tiền chiết khấu | **13** |
| 9 | `validateAndApplyVoucherForCheckout(...)` | Áp dụng voucher lúc checkout kèm khóa bi quan (`PESSIMISTIC_WRITE`) | **2** |

---

## 📑 PHẦN 2: BẢNG MA TRẬN ÁNH XẠ CHI TIẾT 27 TEST CASES

| Mã TC | Hàm ở `src/main` | Hàm kiểm thử ở `src/test` | Giá trị Test đầu vào (Inputs / Mock Data) | Nhánh logic / Điều kiện mục tiêu | Kết quả mong đợi (Assertion) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `TC_VOU_01` | `findVoucher` | `findVoucher_rejectsMissingCode` | `code = null` | Nhánh `if (code == null)` = True | Trả về `null` |
| `TC_VOU_02` | `findVoucher` | `findVoucher_normalizesCode` | `code = "  sale10  "` | Xử lý `code.trim().toUpperCase()` | Tìm đúng mã `"SALE10"` |
| `TC_VOU_03` | `listActiveVouchers` | `listActiveVouchers_appliesActiveExpiryAndUsageFilters` | Ngày hiện tại | Điều kiện lọc: active, chưa hết hạn, còn lượt | Trả về danh sách voucher khả dụng |
| `TC_VOU_04` | `listAllVouchers` | `listAllVouchers_returnsDescendingCreatedRows` | Toàn bộ voucher | Sinh HQL `ORDER BY createdDate DESC` | Danh sách sắp xếp mới nhất |
| `TC_VOU_05` | `saveVoucher` | `saveVoucher_createsAndCopiesEveryFormField` | Form voucher mới | Khối tạo mới entity và gán mọi trường | Lưu voucher thành công |
| `TC_VOU_06` | `saveVoucher` | `saveVoucher_updatesExistingEntity` | Sửa voucher đã có | Nhánh cập nhật thuộc tính entity cũ | Cập nhật bản ghi cũ, không tạo mới |
| `TC_VOU_07` | `deleteVoucher` | `deleteVoucher_returnsFalseWhenMissing` | ID không tồn tại | Nhánh `if (voucher == null)` = True | Trả về `false` |
| `TC_VOU_08` | `deleteVoucher` | `deleteVoucher_softDeletesExistingVoucher` | ID hợp lệ | Lệnh gán `voucher.setActive(false)` | Xóa mềm thành công |
| `TC_VOU_09` | `getUserVoucherUsageCount` | `getUserVoucherUsageCount_returnsZeroForNullKey` | Username hoặc code null | Nhánh kiểm tra null tham số đầu vào | Trả về 0 |
| `TC_VOU_10` | `getUserVoucherUsageCount` | `getUserVoucherUsageCount_mapsNullableAggregate` | Aggregate count = `null` | Toán tử gán `count == null ? 0 : count` | Trả về 0 an toàn |
| `TC_VOU_11` | `recordVoucherUsage` | `recordVoucherUsage_doesNothingForUnknownVoucher` | Mã không tồn tại | Nhánh `if (v == null)` = True | Không làm gì, không lỗi |
| `TC_VOU_12` | `recordVoucherUsage` | `recordVoucherUsage_incrementsVoucherAndPersistsUsage` | Mã hợp lệ | Lệnh `v.setUsageCount(count + 1)` + lưu log | Tăng lượt dùng & lưu log |
| `TC_VOU_13` | `validateAndApplyVoucher` | `validateAndApplyVoucher_rejectsMissingCode` | `code = null` hoặc `""` | Nhánh `if (code == null \|\| code.trim().isEmpty())` | Báo lỗi mã rỗng |
| `TC_VOU_14` | `validateAndApplyVoucher` | `validateAndApplyVoucher_rejectsUnknownVoucher` | `code = "NOT_EXIST"` | Nhánh `if (voucher == null)` = True | Báo lỗi voucher không tồn tại |
| `TC_VOU_15` | `validateAndApplyVoucher` | `validateAndApplyVoucher_rejectsInactiveVoucher` | `active = false` | Nhánh `if (!voucher.isActive())` = True | Báo lỗi voucher bị vô hiệu hóa |
| `TC_VOU_16` | `validateAndApplyVoucher` | `validateAndApplyVoucher_rejectsExpiredVoucher` | Ngày hết hạn = Hôm qua | Nhánh `expiryDate.before(now)` = True | Báo lỗi voucher đã hết hạn |
| `TC_VOU_17` | `validateAndApplyVoucher` | `validateAndApplyVoucher_acceptsVoucherWithFutureExpiry` | Ngày hết hạn = Ngày mai | Nhánh `expiryDate.before(now)` = False | Chấp nhận voucher còn hạn |
| `TC_VOU_18` | `validateAndApplyVoucher` | `validateAndApplyVoucher_rejectsAmountOneUnitBelowMinimum` | `order = 99k`, `min = 100k` | Nhánh `orderAmount < min` = True | Báo lỗi chưa đạt giá trị tối thiểu |
| `TC_VOU_19` | `validateAndApplyVoucher` | `validateAndApplyVoucher_acceptsAmountAtMinimumBoundary` | `order = 100k`, `min = 100k` | Nhánh `orderAmount < min` = False | Áp dụng thành công mã giảm giá |
| `TC_VOU_20` | `validateAndApplyVoucher` | `validateAndApplyVoucher_enforcesGlobalUsageBoundary` | `used = 100`, `max = 100` | Nhánh `usage >= maxUsage` = True | Báo lỗi hết lượt toàn sàn |
| `TC_VOU_21` | `validateAndApplyVoucher` | `validateAndApplyVoucher_enforcesPerUserUsageBoundary` | `userUsed = 1`, `limit = 1` | Nhánh `userCount >= limit` = True | Báo lỗi tài khoản đã hết lượt |
| `TC_VOU_22` | `validateAndApplyVoucher` | `validateAndApplyVoucher_skipsPerUserLimitForGuest` | `username = null` (Guest) | Nhánh `if (username != null)` = False | Bỏ qua kiểm tra quota user |
| `TC_VOU_23` | `validateAndApplyVoucher` | `validateAndApplyVoucher_calculatesPercentDiscount` | Đơn 200k, giảm 10% | Lệnh `discount = total * value / 100` | Giảm đúng 20k |
| `TC_VOU_24` | `validateAndApplyVoucher` | `validateAndApplyVoucher_capsFixedDiscountAtOrderAmount` | Đơn 50k, giảm tiền mặt 70k | Nhánh `Math.min(value, total)` | Giảm tối đa 50k (đơn về 0đ) |
| `TC_VOU_25` | `validateAndApplyVoucher` | `validateAndApplyVoucher_unknownDiscountTypeSucceedsWithZeroDiscount` | Kiểu giảm giá lạ | Khối `switch/case` default | Giảm 0đ (không văng lỗi) |
| `TC_VOU_26` | `validateAndApplyVoucherForCheckout` | `validateAndApplyVoucherForCheckout_usesPessimisticWriteLock` | Checkout đơn hàng | Gọi `LockMode.PESSIMISTIC_WRITE` | Khóa voucher chống race condition |
| `TC_VOU_27` | `validateAndApplyVoucherForCheckout` | `validateAndApplyVoucherForCheckout_rejectsMissingCodeWithoutDatabaseLookup` | Mã rỗng lúc checkout | Nhánh kiểm tra rỗng trước khi query DB | Trả về rỗng không query CSDL |

---

## 📊 PHẦN 3: KẾT QUẢ ĐO LƯỜNG ĐỘ BAO PHỦ (JACOCO METRICS)

* **Lớp kiểm thử:** `com.example.demo.dao.VoucherDAO`
* **Statement Coverage (Instructions):** **312 / 312 (100.0%)**
* **Branch Coverage (Branches):** **36 / 36 (100.0%)**
* **Đánh giá:** Đạt độ phủ tuyệt đối 100% toàn bộ câu lệnh và nhánh logic kiểm tra điều kiện áp dụng mã giảm giá.
