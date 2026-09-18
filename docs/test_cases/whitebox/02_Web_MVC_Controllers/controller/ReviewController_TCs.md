# ĐẶC TẢ KIỂM THỬ HỘP TRẮNG: `ReviewController`
## Tầng: Web MVC Controller | Phân hệ: Đánh giá & Nhận xét sản phẩm
* **Tổng số Test Case:** **13 Test Cases**
* **Mục đích tài liệu:** Bảng ánh xạ chi tiết giữa các hàm nghiệp vụ trong `ReviewController.java` và các hàm kiểm thử trong `WebControllerCoverageTest.java`, phục vụ tra cứu nhanh giá trị test đầu vào, nhánh logic mục tiêu và kết quả mong đợi.

---

## 📌 PHẦN 1: BẢNG TỔNG HỢP CÁC HÀM TRONG `ReviewController.java`

Lớp `ReviewController` quản lý 3 hàm nghiệp vụ giao diện web được bao phủ bởi 13 test case:

| STT | Tên hàm trong `ReviewController.java` | Mục đích nghiệp vụ | Số Test Case đối ứng |
| :---: | :--- | :--- | :---: |
| 1 | `saveReview(...)` | Khách hàng gửi đánh giá mới (validate đăng nhập, role, rating [1, 5], độ dài comment) | **6** |
| 2 | `editReview(...)` | Sửa đánh giá đã đăng (ràng buộc quyền sở hữu và thời hạn 5 phút) | **4** |
| 3 | `deleteReview(...)` | Xóa đánh giá của chính mình (chặn xóa của người khác hoặc khi chưa đăng nhập) | **3** |

---

## 📑 PHẦN 2: BẢNG MA TRẬN ÁNH XẠ CHI TIẾT 13 TEST CASES

| Mã TC | Hàm ở `src/main` | Hàm kiểm thử ở `src/test` | Giá trị Test đầu vào (Inputs / Mock Data) | Nhánh logic / Điều kiện mục tiêu | Kết quả mong đợi (Assertion) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `TC_REV_CTL_01` | `saveReview` | `saveReview_redirectsLoginRequiredAuthentication` | `auth = null` hoặc `anonymousUser` | Nhánh kiểm tra chưa đăng nhập | Redirect về `/admin/login` |
| `TC_REV_CTL_02` | `saveReview` | `saveReview_rejectsManagementRole` | User có role `ROLE_MANAGER` | Nhánh chặn tài khoản quản trị đánh giá | Redirect về `/403` hoặc chặn gửi |
| `TC_REV_CTL_03` | `saveReview` | `saveReview_rejectsInvalidContent` | `code = null`, `comment = null`, comment > 2.000 ký tự | Nhánh validate dữ liệu form review | Báo lỗi nội dung không hợp lệ |
| `TC_REV_CTL_04` | `saveReview` | `saveReview_rejectsRatingOutsideOneToFive` | `rating = 0` hoặc `rating = 6` | Nhánh kiểm tra rating ngoài khoảng $[1, 5]$ | Báo lỗi số sao không hợp lệ |
| `TC_REV_CTL_05` | `saveReview` | `saveReview_savesValidTrimmedReview` | Đánh giá hợp lệ, comment có khoảng trắng | Chuẩn hóa trim comment và lưu DAO | Lưu thành công, redirect về trang chi tiết SP |
| `TC_REV_CTL_06` | `saveReview` | `saveReview_redirectsToProductListWhenDaoRejectsProduct`| SP không tồn tại hoặc inactive | Nhánh DAO từ chối lưu review | Redirect về `/productList` |
| `TC_REV_CTL_07` | `editReview` | `editReview_redirectsLoginRequiredAuthentication` | `auth = null` lúc submit sửa review | Nhánh chưa đăng nhập | Redirect về `/admin/login` |
| `TC_REV_CTL_08` | `editReview` | `editReview_rejectsInvalidContentOrRating` | Comment rỗng hoặc rating sai | Nhánh validate dữ liệu sửa review | Báo lỗi dữ liệu không hợp lệ |
| `TC_REV_CTL_09` | `editReview` | `editReview_reportsSuccessfulUpdate` | Sửa trong vòng 5 phút hợp lệ | Nhánh DAO cập nhật thành công | Redirect kèm thông báo sửa thành công |
| `TC_REV_CTL_10` | `editReview` | `editReview_reportsRejectedUpdate` | Quá 5 phút hoặc sai chủ sở hữu | Nhánh DAO từ chối cập nhật | Redirect kèm flash message báo lỗi |
| `TC_REV_CTL_11` | `deleteReview` | `deleteReview_redirectsLoginRequiredAuthentication` | `auth = null` lúc bấm xóa | Nhánh chưa đăng nhập | Redirect về `/admin/login` |
| `TC_REV_CTL_12` | `deleteReview` | `deleteReview_reportsSuccessfulDeletion` | Xóa review của chính mình | Nhánh DAO xóa thành công | Redirect kèm thông báo xóa thành công |
| `TC_REV_CTL_13` | `deleteReview` | `deleteReview_reportsRejectedDeletion` | Xóa review của người khác | Nhánh DAO từ chối xóa | Báo lỗi không có quyền xóa |

---

## 📊 PHẦN 3: KẾT QUẢ ĐO LƯỜNG ĐỘ BAO PHỦ (JACOCO METRICS)

* **Lớp kiểm thử:** `com.example.demo.controller.ReviewController`
* **Statement Coverage (Instructions):** **100.0%**
* **Branch Coverage (Branches):** **100.0%**
* **Đánh giá:** Đạt độ phủ tuyệt đối 100% toàn bộ câu lệnh và nhánh logic của luồng đánh giá sản phẩm.
