# ĐẶC TẢ KIỂM THỬ HỘP TRẮNG: `ProductReviewDAO`
## Tầng: Data Access Object (DAO) | Phân hệ: Đánh giá & Nhận xét sản phẩm
* **Tổng số Test Case:** **17 Test Cases**
* **Mục đích tài liệu:** Bảng ánh xạ chi tiết giữa các hàm nghiệp vụ trong `ProductReviewDAO.java` và các hàm kiểm thử trong `ProductReviewDAOTest.java`, phục vụ tra cứu nhanh giá trị test đầu vào, nhánh logic mục tiêu và kết quả mong đợi.

---

## 📌 PHẦN 1: BẢNG TỔNG HỢP CÁC HÀM TRONG `ProductReviewDAO.java`

Lớp `ProductReviewDAO` quản lý 5 hàm nghiệp vụ chính được bao phủ bởi 17 test case trong `ProductReviewDAOTest.java`:

| STT | Tên hàm trong `ProductReviewDAO.java` | Mục đích nghiệp vụ | Số Test Case đối ứng |
| :---: | :--- | :--- | :---: |
| 1 | `findReview(Long reviewId)` | Tìm kiếm thông tin đánh giá theo ID | **2** |
| 2 | `saveReview(ProductReviewForm, username)` | Lưu đánh giá 1-5 sao, tính toán lại điểm rating trung bình làm tròn | **6** |
| 3 | `updateReview(Long reviewId, username, form)`| Sửa đánh giá (kiểm tra quyền sở hữu và cửa sổ 5 phút cho phép sửa) | **5** |
| 4 | `deleteReview(Long reviewId, username)` | Xóa đánh giá và làm mới lại cache điểm rating trung bình | **3** |
| 5 | `getReviewsByProductCode(String productCode)` | Lấy danh sách toàn bộ đánh giá của một sản phẩm sắp xếp mới nhất | **1** |

---

## 📑 PHẦN 2: BẢNG MA TRẬN ÁNH XẠ CHI TIẾT 17 TEST CASES

| Mã TC | Hàm ở `src/main` | Hàm kiểm thử ở `src/test` | Giá trị Test đầu vào (Inputs / Mock Data) | Nhánh logic / Điều kiện mục tiêu | Kết quả mong đợi (Assertion) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `TC_REV_01` | `findReview` | `findReview_returnsNullForNullId` | `reviewId = null` | Nhánh `if (reviewId == null)` = True | Trả về `null` |
| `TC_REV_02` | `findReview` | `findReview_delegatesLookup` | `reviewId = 1L` | Lệnh `session.find(ProductReview.class, id)` | Trả về entity `ProductReview` |
| `TC_REV_03` | `saveReview` | `saveReview_rejectsEachInvalidField` | `rating = 0` hoặc `rating = 6` | Nhánh kiểm tra rating ngoài $[1, 5]$ | Bị từ chối lưu đánh giá |
| `TC_REV_04` | `saveReview` | `saveReview_rejectsMissingOrNonActiveProduct` | SP không có hoặc `active = false` | Nhánh `if (p == null \|\| !p.isActive())` | Không cho phép đánh giá SP ẩn |
| `TC_REV_05` | `saveReview` | `saveReview_acceptsCaseInsensitiveActiveStatusAndRecalculatesRoundedCache` | Rating 5 sao hợp lệ | Lưu review ➔ tính lại điểm trung bình ➔ làm tròn cache | Cập nhật cache rating SP |
| `TC_REV_06` | `saveReview` | `saveReview_skipsCacheQueryWhenLockedProductDisappears` | SP bị xóa giữa chừng lúc lock | Nhánh `if (lockedProd == null)` | Bỏ qua bước cập nhật cache |
| `TC_REV_07` | `saveReview` | `saveReview_leavesCacheUntouchedWhenAggregateIsNull` | Aggregate trung bình sao = `null` | Nhánh `if (avgRating == null)` | Giữ nguyên cache cũ |
| `TC_REV_08` | `saveReview` | `saveReview_resetsCacheForEmptyOrIncompleteAggregate` | Không còn review nào sau khi xóa | Nhánh reset điểm về 0 | Điểm trung bình về 0.0 |
| `TC_REV_09` | `updateReview` | `updateReview_rejectsInvalidInput` | Rating mới = 7 (Vượt biên trên) | Nhánh kiểm tra biên rating | Báo lỗi dữ liệu không hợp lệ |
| `TC_REV_10` | `updateReview` | `updateReview_returnsFalseWhenReviewMissing` | Review ID không tồn tại | Nhánh `if (review == null)` = True | Trả về `false` |
| `TC_REV_11` | `updateReview` | `updateReview_returnsFalseForDifferentOwner` | User khác cố tình sửa review | Nhánh `!review.getUser().equals(user)` | Chặn sửa review, trả về `false` |
| `TC_REV_12` | `updateReview` | `updateReview_returnsFalseOutsideFiveMinuteWindow` | Đăng cách đây 6 phút ($> 300\text{s}$) | Nhánh `(now - createdDate) > 5 * 60 * 1000` | Quá 5 phút, trả về `false` |
| `TC_REV_13` | `updateReview` | `updateReview_updatesWithinWindowAndRefreshesCache` | Đăng cách đây 2 phút ($< 300\text{s}$) | Nhánh trong vòng 5 phút hợp lệ | Cập nhật review & cache sao |
| `TC_REV_14` | `deleteReview` | `deleteReview_returnsFalseWhenMissing` | ID review không tồn tại | Nhánh `if (review == null)` = True | Trả về `false` |
| `TC_REV_15` | `deleteReview` | `deleteReview_returnsFalseForDifferentOwner` | Xóa review của người khác | Nhánh kiểm tra chủ sở hữu = False | Bị từ chối, trả về `false` |
| `TC_REV_16` | `deleteReview` | `deleteReview_deletesAndRefreshesProductCache` | Xóa review của chính mình | Xóa bản ghi và làm mới cache sao | Xóa thành công, tính lại sao |
| `TC_REV_17` | `getReviewsByProductCode` | `getReviewsByProductCode_bindsCodeAndReturnsRows` | `code = "P01"` | HQL bind đúng mã SP và sort mới nhất | Trả về danh sách review |

---

## 📊 PHẦN 3: KẾT QUẢ ĐO LƯỜNG ĐỘ BAO PHỦ (JACOCO METRICS)

* **Lớp kiểm thử:** `com.example.demo.dao.ProductReviewDAO`
* **Statement Coverage (Instructions):** **242 / 245 (98.8%)**
* **Branch Coverage (Branches):** **28 / 28 (100.0%)**
* **Đánh giá:** Đạt độ phủ nhánh tuyệt đối 100%. Bao phủ trọn vẹn ràng buộc thời gian 5 phút và cập nhật làm tròn điểm rating.
