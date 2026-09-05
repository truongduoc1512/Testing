# ⏱️ THỜI HẠN CHỈNH SỬA VÀ XÓA ĐÁNH GIÁ SẢN PHẨM (24 GIỜ) - JIRA ISSUE TEST-33

> **Mã Task Jira:** TEST-33  
> **Tiêu đề Task:** Product review edit & delete 24-hour time limit enforcement  
> **Nhánh Git:** `test/w5-TEST-33-test-review-edit-time-limit`  
> **Dự án:** ShoeShop Quality Assurance & Backend Development  
> **Người thực hiện:** QA Automation & Backend Developer  

---

## 📌 1. TỔNG QUAN VÀ QUY TẮC NGHIỆP VỤ (BUSINESS RULES)

Tài liệu này ghi nhận việc nâng cấp logic kiểm soát thời gian chỉnh sửa và xóa đánh giá sản phẩm (Product Reviews) cho hệ thống thương mại điện tử ShoeShop theo yêu cầu Jira Issue **TEST-33**.

### Quy tắc nghiệp vụ (Business Rules):
1. **Giới hạn thời gian (24-Hour Time Window):** Bài đánh giá sản phẩm (Product Review) chỉ được phép **chỉnh sửa** hoặc **xóa** trong vòng **24 giờ** kể từ thời điểm tạo bài đăng (`createdAt`).
2. **Mã lỗi HTTP Response & Chuỗi thông báo lỗi:**
   - Trường hợp người dùng gửi request chỉnh sửa bài đánh giá đã quá 24 giờ kể từ lúc tạo, hệ thống phải trả về mã lỗi **HTTP 403 Forbidden**.
   - Chuỗi thông báo lỗi trả về trong JSON response phải chính xác tuyệt đối:  
     `"Chỉ có thể chỉnh sửa đánh giá trong vòng 24 giờ"`

---

## ⚙️ 2. CHI TIẾT THỰC HIỆN CODE (IMPLEMENTATION DETAILS)

### 2.1. Cập nhật `ProductReviewDAO.java`
- Đã nâng cấp hằng số thời hạn kiểm tra từ **5 phút** (`5 * 60 * 1000`) lên **24 giờ** (`24 * 60 * 60 * 1000L` ms = 86,400,000 ms).
- Cập nhật logic trong cả 2 hàm:
  - `updateReview(Long reviewId, String username, int newRating, String newComment)`
  - `deleteReview(Long reviewId, String username)`

### 2.2. Cập nhật `ReviewApiController.java`
- Thêm logic kiểm tra thời gian khởi tạo của `existingReview` cho REST endpoint `PUT /api/v1/reviews/{reviewId}`.
- Khi thời gian chênh lệch (`diff = System.currentTimeMillis() - review.getCreatedAt().getTime()`) vượt quá 24h, controller trả về ngay lập tức:
  - **Status Code:** `HttpStatus.FORBIDDEN` (HTTP 403 Forbidden)
  - **JSON Body:** `ApiResponse.error("Chỉ có thể chỉnh sửa đánh giá trong vòng 24 giờ")`
- Cập nhật OpenAPI / Swagger `@Operation` summary mô tả chính xác quy tắc 24h.

### 2.3. Cập nhật `ReviewController.java`
- Điều chỉnh thông báo flash message hiển thị trên giao diện web MVC từ "5 phút" thành "24 giờ" khi cập nhật review thất bại do quá hạn.

---

## 🧪 3. KỊCH BẢN KIỂM THỬ BIÊN (BOUNDARY VALUE TESTING)

Các test case kiểm thử biên thời gian (Boundary Value Analysis) được triển khai chi tiết dựa trên mốc giới hạn 24 giờ (86,400,000 ms):

| Mã Test Case | Mốc thời gian (Creation Delta) | Hành động | Kết quả kỳ vọng (Expected Result) | Kết quả thực tế (Actual Result) | Trạng thái |
| :--- | :--- | :--- | :--- | :--- | :---: |
| `TC_REV_BOUND_01` | **23h 59m** (86,340,000 ms ago) | Chỉnh sửa Review | **Cho phép (Allowed / Success)**<br>HTTP Status: 200 OK<br>Response Body: Object review đã được cập nhật | HTTP 200 OK<br>Review updated | **PASS** |
| `TC_REV_BOUND_02` | **24h 01m** (86,460,000 ms ago) | Chỉnh sửa Review | **Từ chối (Forbidden)**<br>HTTP Status: 403 Forbidden<br>Message: `"Chỉ có thể chỉnh sửa đánh giá trong vòng 24 giờ"` | HTTP 403 Forbidden<br>Message: `"Chỉ có thể chỉnh sửa đánh giá trong vòng 24 giờ"` | **PASS** |
| `TC_REV_BOUND_03` | **23h 59m** (86,340,000 ms ago) | Xóa Review | **Cho phép (Allowed / Success)**<br>DAO trả về `true`<br>Thực hiện xóa và cập nhật rating cache | DAO returned `true`<br>Review deleted | **PASS** |
| `TC_REV_BOUND_04` | **24h 01m** (86,460,000 ms ago) | Xóa Review | **Từ chối (Rejected)**<br>DAO trả về `false`<br>Không thay đổi dữ liệu trong Database | DAO returned `false`<br>DB untouched | **PASS** |

---

## 📊 4. MA TRẬN XÁC THỰC (VERIFICATION MATRIX)

Bảng ma trận đối chiếu yêu cầu từ Jira Issue **TEST-33** và thành phần triển khai / xác thực tương ứng:

| STT | Yêu cầu Jira (Requirement) | Thành phần triển khai (Component) | Phương pháp xác thực (Verification Method) | Trạng thái (Status) |
| :---: | :--- | :--- | :--- | :---: |
| 1 | Checkout/tạo branch `test/w5-TEST-33-test-review-edit-time-limit` | Git repository | Git Command (`git status`) | **VERIFIED** |
| 2 | Đánh giá sản phẩm chỉ được sửa/xóa trong vòng 24 giờ | `ProductReviewDAO.java` | Unit Test (`ProductReviewDAOTest`) | **VERIFIED** |
| 3 | Chỉnh sửa quá hạn 24 giờ trả về mã lỗi HTTP 403 Forbidden | `ReviewApiController.java` | Integration API Test (`ReviewApiControllerTest`) | **VERIFIED** |
| 4 | Thông báo lỗi chính xác: `"Chỉ có thể chỉnh sửa đánh giá trong vòng 24 giờ"` | `ReviewApiController.java` | Response Body Assertion | **VERIFIED** |
| 5 | Boundary Value Test ở mốc 23h59m (Cho phép) | Unit & API Test Suite | `updateReview_boundary_23h59m_allowed` | **VERIFIED** |
| 6 | Boundary Value Test ở mốc 24h01m (Trả về lỗi 403) | Unit & API Test Suite | `updateReview_boundary_24h01m_forbidden` | **VERIFIED** |
| 7 | Tạo và ghi nhận kết quả tại `docs/TEST-33.md` | `docs/TEST-33.md` | Tài liệu kỹ thuật kiểm thử | **VERIFIED** |

---

## 💻 5. KẾT QUẢ THỰC THI AUTOMATED TESTS

Đã chạy kiểm thử tự động toàn bộ Unit Tests & Integration Tests cho module Review bằng JUnit 5 & Mockito Framework:

- `com.example.demo.dao.ProductReviewDAOTest`: **100% PASS**
- `com.example.demo.controller.api.ReviewApiControllerTest`: **100% PASS**

```text
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.example.demo.dao.ProductReviewDAOTest
[INFO] Tests run: 18, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.example.demo.controller.api.ReviewApiControllerTest
[INFO] Tests run: 21, Failures: 0, Errors: 0, Skipped: 0
[INFO] -------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] -------------------------------------------------------
```
