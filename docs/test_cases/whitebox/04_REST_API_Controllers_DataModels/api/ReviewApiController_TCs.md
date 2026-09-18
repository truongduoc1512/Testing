# BẢNG MA TRẬN KIỂM THỬ HỘP TRẮNG REVIEW API CONTROLLER
**Đường dẫn file mã nguồn:** [ReviewApiController.java](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/src/main/java/com/example/demo/controller/api/ReviewApiController.java)  
**Đường dẫn file kiểm thử:** [ReviewApiControllerTest.java](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/src/test/java/com/example/demo/controller/api/ReviewApiControllerTest.java)

---

## 1. TỔNG QUAN ĐỘ BAO PHỦ (JACOCO COVERAGE METRICS)

| File Target | Instruction Coverage | Branch Coverage | Line Coverage | Method Coverage | Trạng Thái |
| :--- | :---: | :---: | :---: | :---: | :---: |
| `ReviewApiController.java` | 98.6% | 96.4% | 100% | 100% | Đạt |

---

## 2. MA TRẬN TEST CASES KIỂM THỬ HỘP TRẮNG (14 TEST CASES)

| Mã TC | Hàm ở src/main | Hàm kiểm thử ở src/test | Giá trị Test đầu vào | Nhánh logic / Điều kiện mục tiêu | Kết quả mong đợi |
| :---: | :--- | :--- | :--- | :--- | :--- |
| **TC_REV_01** | `getReviewsByProductCode(String)` | `getReviews_returnsDaoList()` | `productCode="P1"`, DAO trả về list 2 reviews | Nhánh lấy danh sách bình luận theo mã sản phẩm thành công | HTTP 200 OK, body chứa đúng list review từ DAO |
| **TC_REV_02** | `saveReview(ProductReviewForm)` | `saveReview_rejectsLoginRequiredAuthentication(String, Authentication)` | User chưa đăng nhập (null / unauthenticated / anonymousUser), form review hợp lệ | Nhánh kiểm tra xác thực người dùng thất bại (`auth == null` hoặc chưa authenticated) | HTTP 401 UNAUTHORIZED, body `success: false`, DAO không được gọi |
| **TC_REV_03** | `saveReview(ProductReviewForm)` | `saveReview_rejectsAdminRole()` | User có vai trò `ROLE_ADMIN` | Nhánh chặn tài khoản quản trị viên bình luận (`hasRole("ROLE_ADMIN")`) | HTTP 403 FORBIDDEN, body `success: false`, DAO không được gọi |
| **TC_REV_04** | `saveReview(ProductReviewForm)` | `saveReview_rejectsInvalidForm(String, ProductReviewForm)` | User `buyer` (ROLE_USER), các form lỗi: null, productCode trống/quá dài, comment trống/quá dài, rating < 1 hoặc > 5 | Nhánh kiểm tra tính hợp lệ dữ liệu biểu mẫu review (`isInvalidForm == true`) | HTTP 400 BAD REQUEST, body `success: false`, DAO không được gọi |
| **TC_REV_05** | `saveReview(ProductReviewForm)` | `saveReview_trimsAndReturnsCreatedReview()` | User `buyer` (ROLE_USER), `productCode=" P1 "`, `ratingValue=5`, `comment=" great "` | Nhánh tạo bình luận thành công, trim chuỗi dữ liệu trước khi lưu | HTTP 201 CREATED, body `success: true`, entity đã trim và gán đúng username |
| **TC_REV_06** | `saveReview(ProductReviewForm)` | `saveReview_mapsDomainExceptionToBadRequest()` | Form hợp lệ, DAO throw `IllegalArgumentException("invalid product")` | Nhánh bắt ngoại lệ nghiệp vụ miền dữ liệu (`IllegalArgumentException`) | HTTP 400 BAD REQUEST, body chứa message thông báo lỗi nghiệp vụ |
| **TC_REV_07** | `saveReview(ProductReviewForm)` | `saveReview_mapsUnexpectedExceptionToServerError()` | Form hợp lệ, DAO throw `IllegalStateException("database unavailable")` | Nhánh bắt ngoại lệ hệ thống không mong đợi (`Exception`) | HTTP 500 INTERNAL SERVER ERROR, body `success: false` |
| **TC_REV_08** | `updateReview(Long, Map)` | `updateReview_rejectsLoginRequiredAuthentication(String, Authentication)` | User chưa đăng nhập (null / unauthenticated / anonymousUser), `reviewId=1L` | Nhánh kiểm tra xác thực người dùng khi sửa review | HTTP 401 UNAUTHORIZED, DAO update không được gọi |
| **TC_REV_09** | `updateReview(Long, Map)` | `updateReview_rejectsInvalidPayload(String, Map)` | User `buyer`, payload sửa review lỗi: null, comment không phải text/trống/quá 2000 ký tự, rating NaN/vô cực/số thực/ngoài 1-5 | Nhánh xác thực tính hợp lệ của payload cập nhật review | HTTP 400 BAD REQUEST, DAO update không được gọi |
| **TC_REV_10** | `updateReview(Long, Map)` | `updateReview_returnsUpdatedEntity()` | User `buyer`, `id=1L`, payload `{ratingValue: 5L, comment: " updated "}`, DAO cập nhật thành công | Nhánh cập nhật review thành công và trả về thực thể review mới nhất | HTTP 200 OK, trả về thực thể `ProductReview` đã cập nhật |
| **TC_REV_11** | `updateReview(Long, Map)` | `updateReview_returnsBadRequestWhenDaoRejectsUpdate()` | User `buyer`, `id=2L`, payload hợp lệ nhưng DAO trả về `false` (không tìm thấy hoặc không chính chủ) | Nhánh DAO từ chối cập nhật review (`success == false`) | HTTP 400 BAD REQUEST, body `success: false` |
| **TC_REV_12** | `deleteReview(Long)` | `deleteReview_rejectsLoginRequiredAuthentication(String, Authentication)` | User chưa đăng nhập, `reviewId=1L` | Nhánh kiểm tra xác thực người dùng khi xóa review | HTTP 401 UNAUTHORIZED, DAO delete không được gọi |
| **TC_REV_13** | `deleteReview(Long)` | `deleteReview_returnsSuccessWhenDaoDeletesReview()` | User `buyer`, `id=1L`, DAO xóa thành công trả về `true` | Nhánh xóa review thành công của người dùng sở hữu | HTTP 200 OK, body `success: true` |
| **TC_REV_14** | `deleteReview(Long)` | `deleteReview_returnsBadRequestWhenDaoRejectsDeletion()` | User `buyer`, `id=2L`, DAO trả về `false` (không tìm thấy hoặc không có quyền) | Nhánh DAO từ chối xóa review (`success == false`) | HTTP 400 BAD REQUEST, body `success: false` |
