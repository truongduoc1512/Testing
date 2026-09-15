# Bảng Test Case: Chức năng 6 - Đánh giá sản phẩm (Review & Rating)
**Người thực hiện:** Được 

## 1. Thông tin Kỹ thuật & Thực thi
- **Kỹ thuật Thiết kế (Test Design):** 
  - **Phân hoạch lớp tương đương (EP):** Quản lý quyền truy cập (Guest vs User vs Admin), Kiểm tra vòng đời Sản phẩm (Active vs Inactive).
  - **Phân tích giá trị biên (BVA):** Rất quan trọng để chặn số sao ngoài khoảng [1, 5] (VD: 0 sao, 6 sao) và **Giới hạn thời gian sửa bài trong vòng 5 phút**.
  - **Bảng quyết định (Decision Table):** Kết hợp các ràng buộc thành 7 quy tắc cốt lõi bảo vệ hệ thống.
- **Kỹ thuật Thực thi (Test Execution):** 
  - Kiểm thử Tích hợp (Integration Test) & Đơn vị (Unit Test) qua JUnit / Mockito.
  - Kiểm thử API End-to-End (Black-box E2E API Testing) qua Postman.
- **File Code Thực thi (Automation Script):** 
  - Backend Logic: `src/test/java/com/example/demo/dao/ProductReviewDAOTest.java` và `controller/api/ReviewApiControllerTest.java`.
  - Postman API: `docs/Shoeshop_API_Collection.json`.

---

## 2. Phân tích Kỹ thuật Thiết kế (Test Design Analysis)


### 2.1 Bảng Phân hoạch lớp tương đương (Equivalence Partitioning - EP)

| Biến đầu vào / Điều kiện | Lớp tương đương Hợp lệ | Tag | Lớp tương đương Không hợp lệ | Tag |
| :--- | :--- | :---: | :--- | :---: |
| **Vai trò người dùng (`userRole`)** | Khách hàng đã đăng nhập (`ROLE_USER`) | **V1** | Khách vãng lai chưa đăng nhập<br>Quản trị viên `ROLE_ADMIN` (Cấm seeding) | **X1**<br>**X2** |
| **Trạng thái sản phẩm (`product`)** | Đang mở bán (`ACTIVE`) | **V2** | Không tồn tại, `INACTIVE`, hoặc `DRAFT` | **X3** |
| **Quyền sở hữu (`ownership`)** | Tự sửa/xóa bài đánh giá của chính mình | **V3** | Can thiệp bài đánh giá của người khác | **X4** |
| **Số sao đánh giá (`ratingValue`)** | Số nguyên trong khoảng [1, 5] sao | **V4** | ≤ 0 sao (Dưới biên)<br>≥ 6 sao (Vượt biên) | **X5**<br>**X6** |
| **Độ dài bình luận (`comment`)** | Chuỗi văn bản từ 1 đến 2000 ký tự | **V5** | Bỏ trống / null / toàn khoảng trắng<br>Vượt quá 2000 ký tự | **X7**<br>**X8** |
| **Thời hạn sửa bài (`timeWindow`)** | Trong vòng 5 phút (≤ 300,000 ms) | **V6** | Đã quá 5 phút kể từ lúc tạo (> 300,000 ms) | **X9** |


### 2.2 Bảng Phân tích giá trị biên (Standard Boundary Value Analysis - BVA)

| Biến đầu vào | Miền hợp lệ | min | min+ | nominal | max- | max | Tag biên |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :--- |
| **Số sao đánh giá (`ratingValue`)** | $[1, 5]$ sao | 1 | 2 | 3 | 4 | 5 | **B1, B2, B3, B4, B5** |
| **Độ dài bình luận (`comment`)** | $[1, 2000]$ ký tự | 1 | 2 | 50 | 1999 | 2000 | **B6, B7, B8, B9, B10** |
| **Thời hạn sửa bài (`timeWindow`)** | $[0, 300000]$ ms | 0 | 1000 | 150000 | 299000 | 300000 | **B11, B12, B13, B14, B15** |

*Ghi chú mở rộng (Robustness BVA):*
- Giá trị ngoài biên dưới số sao: `0 sao` (Tag **B1-1** / min-1) -> Báo lỗi 400 Bad Request.
- Giá trị ngoài biên trên số sao: `6 sao` (Tag **B5+1** / max+1) -> Báo lỗi 400 Bad Request.
- Giá trị ngoài biên bình luận: `0 ký tự` (Tag **B6-1**), `2001 ký tự` (Tag **B10+1**).
- Giá trị ngoài biên thời gian sửa bài: `301,000 ms` (Tag **B15+1** / quá 5 phút 1 giây) -> Khóa quyền chỉnh sửa.


### 2.3 Bảng Quyết định tổng hợp (Collapsed Decision Table)

| Condition/Action | R1 | R2 | R3 | R4 | R5 | R6 | R7 |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| **C1: Quyền truy cập hợp lệ (ROLE_USER)?**| N | Y | Y | Y | Y | Y | Y |
| **C2: SP đang ACTIVE?** | - | N | Y | Y | Y | Y | Y |
| **C3: Số sao [1, 5] & Nội dung OK?** | - | - | N | Y | Y | Y | Y |
| **C4: Là Chủ sở hữu bài Review?** | - | - | - | N | Y | Y | Y |
| **C5: Nằm trong 5 phút vàng (Dành cho Sửa)?**| - | - | - | - | N | Y | Y |
| **A1: Báo lỗi 401/403 (Phân quyền)** | X | - | - | - | - | - | - |
| **A2: Báo lỗi (Sản phẩm không bán)** | - | X | - | - | - | - | - |
| **A3: Báo lỗi 400 (Validation Form)** | - | - | X | - | - | - | - |
| **A4: Báo lỗi Cấm sửa của người khác** | - | - | - | X | - | - | - |
| **A5: Báo lỗi Quá 5 phút cấm sửa** | - | - | - | - | X | - | - |
| **A6: Thành công (Tạo/Sửa/Xóa + Tính lại điểm)**| - | - | - | - | - | X | X |
| **Test Case Tương ứng** | TC_REV_006, 007 | TC_REV_008 | TC_REV_003, 004, 005 | TC_REV_009 | TC_REV_010 | TC_REV_011 | TC_REV_001, 002 |

---


## 3. Bảng Test Case Chi Tiết

| Mã kiểm thử | Kỹ thuật áp dụng | Tiêu đề | Điều kiện tiên quyết | Các bước kiểm tra | Dữ liệu kiểm thử | Kết quả dự kiến | Tag được bao phủ | Kết quả thực tế | Trạng thái |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :---: | :--- | :---: |
| **TC_REV_001** | BVA (Max / R7) | Đánh giá hợp lệ 5 sao và tính điểm tự động | User `buyer` đăng nhập, gọi API POST. | Gửi Body JSON hợp lệ đánh giá 5 sao. | `ratingValue = 5`, Comment = "Giày rất êm". | Lưu Review thành công. Kéo điểm trung bình của Product lên tương ứng. | **V1, V2, V4, V5, B5, B8** | Khớp Unit Test `saveReview_trimsAndReturnsCreatedReview`. | Pass |
| **TC_REV_002** | BVA (Min / R7) | Đánh giá hợp lệ 1 sao và tính điểm tự động | User `buyer` đăng nhập, gọi API POST. | Gửi Body JSON hợp lệ đánh giá 1 sao. | `ratingValue = 1`, Comment = "Giao hàng chậm". | Lưu Review. Điểm trung bình của Product tụt xuống mức thực tế. | **V1, V2, V4, V5, B1, B8** | Khớp DAO `saveReview_acceptsCaseInsensitiveActiveStatus`. | Pass |
| **TC_REV_003** | Đoán lỗi (R3) | Chặn lưu đánh giá khi Comment rỗng hoặc quá 2000 ký tự | User `buyer` đăng nhập. | Gửi Form bình luận nhưng để tham số sai rào cản. | `comment = "   "` hoặc dài $2001$ ký tự `x`. | Báo lỗi Validation 400 Bad Request. | **X7, X8, B6-1, B10+1** | Khớp với Unit Test API & DAO. | Pass |
| **TC_REV_004** | BVA (Min-1 / R3) | Chặn đánh giá 0 sao | Mở Postman gửi API trực tiếp. | Cố tình hack tham số Rating bằng 0. | `ratingValue = 0` | Chặn lại, báo lỗi số sao phải lớn hơn 0. | **X5, B1-1** | Khớp Unit Test API. | Pass |
| **TC_REV_005** | BVA (Max+1 / R3) | Chặn đánh giá 6 sao | Mở Postman gửi API trực tiếp. | Cố tình hack tham số Rating vượt giới hạn. | `ratingValue = 6` | Chặn lại, báo lỗi số sao không vượt quá 5. | **X6, B5+1** | Khớp Unit Test API. | Pass |
| **TC_REV_006** | EP (Guest / R1) | Báo lỗi 401: Khách vãng lai không được Đánh giá | Bắn API không mang theo Token. | Bắn POST `/api/v1/reviews`. | Trạng thái Unauthenticated. | Server từ chối thẳng với mã lỗi HTTP 401. | **X1** | Khớp Unit Test `saveReview_rejectsLoginRequired`. | Pass |
| **TC_REV_007** | EP (Admin / R1) | Báo lỗi 403: Cấm Admin dùng quyền tạo đánh giá ảo | Bắn API bằng tài khoản mang `ROLE_ADMIN`. | Gọi API tạo đánh giá. | User có quyền Admin. | Cấm Admin thao tác để chống Seeding bẩn. Lỗi 403 Forbidden. | **X2** | Khớp Unit Test `saveReview_rejectsAdminRole`. | Pass |
| **TC_REV_008** | EP (Product / R2) | Chặn đánh giá vào Sản phẩm đang bị Tắt (INACTIVE) | Sản phẩm mã `P001` đang bị Admin chuyển sang Inactive. | Gọi API tạo đánh giá cho mã `P001`. | Trạng thái Product là INACTIVE hoặc DRAFT. | Dao chặn lại và văng lỗi IllegalArgumentException. | **X3** | Khớp Unit Test `saveReview_rejectsMissingOrNonActiveProduct`. | Pass |
| **TC_REV_009** | EP (Owner / R4) | Chặn hành vi sửa/xóa Review của người khác | Có 1 bài Review ID=1 của user `alice`. | User `bob` gọi API PUT/DELETE vào ID=1. | `alice` $\neq$ `bob`. | Báo lỗi vì không phải chủ sở hữu bài viết (Ownership). | **X4** | Khớp Unit Test `updateReview_returnsFalseForDifferentOwner`. | Pass |
| **TC_REV_010** | BVA (Time / R5) | Chặn quyền chỉnh sửa Review khi đã quá 5 phút | User `alice` có bài Review ID=1 tạo từ rất lâu. | Kịch bản BVA ném thời điểm tạo lùi về 301,000 milliseconds trước. | Time Window $> 5$ phút. | Mất quyền Sửa. Hệ thống khóa cứng bài đánh giá. | **X9, B15+1** | Khớp Unit Test `updateReview_returnsFalseOutsideFiveMinuteWindow`. | Pass |
| **TC_REV_011** | EP (CRUD / R7) | Xóa thành công Review và Khôi phục điểm Rating gốc | Khách hàng thực thi quyền Xóa đúng bài của mình. | Bắn DELETE API. | Thỏa mãn quyền Owner. | Xóa thành công bài đánh giá, điểm tổng Product cập nhật lại như cũ. | **V1, V3** | Khớp Unit Test `deleteReview_deletesAndRefreshesProductCache`. | Pass |


---

## 4. Bảng Đối Chiếu & Ý Nghĩa Nhãn Tag (Tag Traceability Legend)

| Nhóm Tag | Mã Tag | Ý nghĩa nghiệp vụ | Trạng thái |
| :---: | :---: | :--- | :---: |
| **Valid EP** | **V1** | Khách hàng đã đăng nhập có quyền đánh giá | Hợp lệ |
| | **V2** | Sản phẩm được đánh giá đang mở bán (ACTIVE) | Hợp lệ |
| | **V3** | Người thao tác là chủ sở hữu bài review | Hợp lệ |
| | **V4** | Số sao nằm trong khoảng hợp lệ [1, 5] | Hợp lệ |
| | **V5** | Nội dung bình luận hợp lệ từ 1 đến 2000 ký tự | Hợp lệ |
| | **V6** | Thao tác sửa bài trong vòng 5 phút (<= 300,000 ms) | Hợp lệ |
| **Invalid EP**| **X1** | Khách vãng lai chưa đăng nhập (Báo lỗi 401) | Không hợp lệ |
| | **X2** | Admin cố tình tạo đánh giá ảo (Báo lỗi 403) | Không hợp lệ |
| | **X3** | Sản phẩm không tồn tại hoặc đã ngừng bán | Không hợp lệ |
| | **X4** | Người dùng can thiệp bài review của người khác (Báo lỗi 403) | Không hợp lệ |
| | **X5** | Số sao đánh giá nhỏ hơn 1 (<= 0 sao) | Không hợp lệ |
| | **X6** | Số sao đánh giá lớn hơn 5 (>= 6 sao) | Không hợp lệ |
| | **X7** | Bình luận để trống, null hoặc chỉ có khoảng trắng | Không hợp lệ |
| | **X8** | Bình luận vượt quá 2000 ký tự | Không hợp lệ |
| | **X9** | Bài review đã quá hạn 5 phút cho phép chỉnh sửa | Không hợp lệ |
| **Boundary** | **B1 - B5**| Điểm biên số sao: min (1), min+ (2), nom (3), max- (4), max (5) | Hợp lệ |
| | **B1-1** | Ngoài biên dưới số sao: 0 sao | Không hợp lệ |
| | **B5+1** | Ngoài biên trên số sao: 6 sao | Không hợp lệ |
| | **B6 - B10**| Điểm biên độ dài bình luận: min (1), min+ (2), nom (50), max- (1999), max (2000) | Hợp lệ |
| | **B11 - B15**| Điểm biên thời hạn sửa bài: min (0ms), min+ (1s), nom (2.5p), max- (4p59s), max (5p) | Hợp lệ |
| | **B15+1** | Ngoài biên trên thời hạn sửa bài: 301,000 ms (5 phút 1 giây) | Không hợp lệ |
