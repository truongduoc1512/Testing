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

| STT | Điều kiện đầu vào (Input / Condition) | Lớp tương đương Hợp lệ (Valid EP) | Lớp tương đương Không hợp lệ (Invalid EP) | Test Case tương ứng |
| :---: | :--- | :--- | :--- | :---: |
| 1 | **Vai trò & Quyền người dùng (User Role)** | Khách mua hàng đã đăng nhập (`ROLE_USER`) | - Khách vãng lai chưa đăng nhập (Báo lỗi 401 Unauthorized)<br>- Quản trị viên `ROLE_ADMIN` (Báo lỗi 403 Forbidden, cấm seeding ảo) | **TC_REV_006**<br>**TC_REV_007** |
| 2 | **Trạng thái Sản phẩm được đánh giá** | Sản phẩm đang mở bán (`ACTIVE`) | Sản phẩm không tồn tại, bị ẩn (`INACTIVE`), hoặc bản nháp (`DRAFT`) | **TC_REV_008** |
| 3 | **Quyền Sở hữu bài đánh giá (Ownership)** | Người dùng chỉnh sửa hoặc xóa bài đánh giá do chính mình tạo ra (`owner = currentUser`) | Người dùng A can thiệp sửa/xóa bài đánh giá của Người dùng B (Báo lỗi 403 / từ chối cập nhật) | **TC_REV_009** |
| 4 | **Số sao đánh giá (Rating Value EP)** | Số nguyên nằm trong khoảng hợp lệ $[1, 5]$ sao | Số sao $\le 0$ hoặc $\ge 6$ sao | **TC_REV_001**<br>**TC_REV_002**<br>**TC_REV_004**<br>**TC_REV_005** |
| 5 | **Độ dài nội dung bình luận (Comment Length)** | Chuỗi văn bản hợp lệ từ 1 đến 2000 ký tự | Nội dung rỗng, null, chỉ toàn khoảng trắng hoặc vượt quá 2000 ký tự | **TC_REV_003** |
| 6 | **Thời gian chỉnh sửa đánh giá (Time Window)** | Thao tác chỉnh sửa diễn ra trong vòng 5 phút kể từ lúc tạo bài ($\le 300,000$ ms) | Thao tác chỉnh sửa khi bài viết đã tạo quá 5 phút ($> 300,000$ ms) | **TC_REV_010** |
| 7 | **Đồng bộ bộ nhớ đệm điểm số (Rating Cache Sync)** | Khi Thêm mới, Chỉnh sửa hoặc Xóa bài đánh giá, hệ thống tự động tính lại số sao trung bình và tổng lượt review của sản phẩm | Bị sai lệch hoặc không làm mới điểm đánh giá trung bình sau khi bài viết bị xóa | **TC_REV_001**<br>**TC_REV_002**<br>**TC_REV_011** |

### 2.2 Bảng Phân tích giá trị biên (Boundary Value Analysis - BVA)

| STT | Biến kiểm thử / Ràng buộc logic | Điểm biên BVA | Giá trị kiểm thử | Phân loại BVA | Kết quả dự kiến (Expected Output) | Test Case |
| :---: | :--- | :---: | :---: | :---: | :--- | :---: |
| 1 | **Số sao đánh giá (`ratingValue`)**<br>*Ràng buộc: $1 \le ratingValue \le 5$ (Số nguyên)* | min - 1 | 0 sao | Invalid (Dưới biên) | Báo lỗi 400 Bad Request (Số sao phải từ 1 đến 5) | **TC_REV_004** |
| 2 | **Số sao đánh giá (`ratingValue`)** | min | 1 sao | Valid (Biên dưới) | Lưu đánh giá 1 sao thành công, tính lại điểm trung bình | **TC_REV_002** |
| 3 | **Số sao đánh giá (`ratingValue`)** | min + 1 | 2 sao | Valid | Lưu đánh giá 2 sao thành công | **TC_REV_002** |
| 4 | **Số sao đánh giá (`ratingValue`)** | max - 1 | 4 sao | Valid | Lưu đánh giá 4 sao thành công | **TC_REV_001** |
| 5 | **Số sao đánh giá (`ratingValue`)** | max | 5 sao | Valid (Biên trên) | Lưu đánh giá 5 sao thành công, tính lại điểm trung bình | **TC_REV_001** |
| 6 | **Số sao đánh giá (`ratingValue`)** | max + 1 | 6 sao | Invalid (Vượt biên) | Báo lỗi 400 Bad Request (Số sao phải từ 1 đến 5) | **TC_REV_005** |
| 7 | **Thời gian cho phép sửa bài (`timeWindow`)**<br>*Ràng buộc: Thời gian từ lúc tạo $\le 5$ phút ($300,000$ ms)* | max - 1 | 299,000 ms (4p 59s) | Valid | Hợp lệ, cho phép cập nhật nội dung đánh giá | **TC_REV_010** |
| 8 | **Thời gian cho phép sửa bài (`timeWindow`)** | max | 300,000 ms (Đúng 5 phút) | Valid (Biên trên) | Hợp lệ, cho phép cập nhật nội dung đánh giá | **TC_REV_010** |
| 9 | **Thời gian cho phép sửa bài (`timeWindow`)** | max + 1 | 301,000 ms (5p 01s) | Invalid (Quá hạn) | Hệ thống khóa quyền chỉnh sửa, báo lỗi quá 5 phút | **TC_REV_010** |
| 10 | **Độ dài nội dung bình luận (`comment`)**<br>*Ràng buộc: $1 \le \text{length}(comment) \le 2000$ ký tự* | min - 1 | 0 ký tự (`""` rỗng / khoảng trắng) | Invalid (Dưới biên) | Báo lỗi 400 Bad Request (Nội dung không được rỗng) | **TC_REV_003** |
| 11 | **Độ dài nội dung bình luận (`comment`)** | min | 1 ký tự | Valid (Biên dưới) | Lưu bình luận ngắn 1 ký tự thành công | **TC_REV_003** |
| 12 | **Độ dài nội dung bình luận (`comment`)** | max | 2000 ký tự | Valid (Biên trên) | Lưu bình luận tối đa 2000 ký tự thành công | **TC_REV_003** |
| 13 | **Độ dài nội dung bình luận (`comment`)** | max + 1 | 2001 ký tự | Invalid (Vượt biên) | Báo lỗi 400 Bad Request (Bình luận vượt quá 2000 ký tự) | **TC_REV_003** |

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

| Mã kiểm thử | Kỹ thuật áp dụng | Tiêu đề | Điều kiện tiên quyết | Các bước kiểm tra | Dữ liệu kiểm thử | Kết quả dự kiến | Kết quả thực tế | Trạng thái |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **TC_REV_001** | BVA (Max / R7) | Đánh giá hợp lệ 5 sao và tính điểm tự động | User `buyer` đăng nhập, gọi API POST. | Gửi Body JSON hợp lệ đánh giá 5 sao. | `ratingValue = 5`, Comment = "Giày rất êm". | Lưu Review thành công. Kéo điểm trung bình của Product lên tương ứng. | Khớp Unit Test `saveReview_trimsAndReturnsCreatedReview`. | Pass |
| **TC_REV_002** | BVA (Min / R7) | Đánh giá hợp lệ 1 sao và tính điểm tự động | User `buyer` đăng nhập, gọi API POST. | Gửi Body JSON hợp lệ đánh giá 1 sao. | `ratingValue = 1`, Comment = "Giao hàng chậm". | Lưu Review. Điểm trung bình của Product tụt xuống mức thực tế. | Khớp DAO `saveReview_acceptsCaseInsensitiveActiveStatus`. | Pass |
| **TC_REV_003** | Đoán lỗi (R3) | Chặn lưu đánh giá khi Comment rỗng hoặc quá 2000 ký tự | User `buyer` đăng nhập. | Gửi Form bình luận nhưng để tham số sai rào cản. | `comment = "   "` hoặc dài $2001$ ký tự `x`. | Báo lỗi Validation 400 Bad Request. | Khớp với Unit Test API & DAO. | Pass |
| **TC_REV_004** | BVA (Min-1 / R3) | Chặn đánh giá 0 sao | Mở Postman gửi API trực tiếp. | Cố tình hack tham số Rating bằng 0. | `ratingValue = 0` | Chặn lại, báo lỗi số sao phải lớn hơn 0. | Khớp Unit Test API. | Pass |
| **TC_REV_005** | BVA (Max+1 / R3) | Chặn đánh giá 6 sao | Mở Postman gửi API trực tiếp. | Cố tình hack tham số Rating vượt giới hạn. | `ratingValue = 6` | Chặn lại, báo lỗi số sao không vượt quá 5. | Khớp Unit Test API. | Pass |
| **TC_REV_006** | EP (Guest / R1) | Báo lỗi 401: Khách vãng lai không được Đánh giá | Bắn API không mang theo Token. | Bắn POST `/api/v1/reviews`. | Trạng thái Unauthenticated. | Server từ chối thẳng với mã lỗi HTTP 401. | Khớp Unit Test `saveReview_rejectsLoginRequired`. | Pass |
| **TC_REV_007** | EP (Admin / R1) | Báo lỗi 403: Cấm Admin dùng quyền tạo đánh giá ảo | Bắn API bằng tài khoản mang `ROLE_ADMIN`. | Gọi API tạo đánh giá. | User có quyền Admin. | Cấm Admin thao tác để chống Seeding bẩn. Lỗi 403 Forbidden. | Khớp Unit Test `saveReview_rejectsAdminRole`. | Pass |
| **TC_REV_008** | EP (Product / R2) | Chặn đánh giá vào Sản phẩm đang bị Tắt (INACTIVE) | Sản phẩm mã `P001` đang bị Admin chuyển sang Inactive. | Gọi API tạo đánh giá cho mã `P001`. | Trạng thái Product là INACTIVE hoặc DRAFT. | Dao chặn lại và văng lỗi IllegalArgumentException. | Khớp Unit Test `saveReview_rejectsMissingOrNonActiveProduct`. | Pass |
| **TC_REV_009** | EP (Owner / R4) | Chặn hành vi sửa/xóa Review của người khác | Có 1 bài Review ID=1 của user `alice`. | User `bob` gọi API PUT/DELETE vào ID=1. | `alice` $\neq$ `bob`. | Báo lỗi vì không phải chủ sở hữu bài viết (Ownership). | Khớp Unit Test `updateReview_returnsFalseForDifferentOwner`. | Pass |
| **TC_REV_010** | BVA (Time / R5) | Chặn quyền chỉnh sửa Review khi đã quá 5 phút | User `alice` có bài Review ID=1 tạo từ rất lâu. | Kịch bản BVA ném thời điểm tạo lùi về 301,000 milliseconds trước. | Time Window $> 5$ phút. | Mất quyền Sửa. Hệ thống khóa cứng bài đánh giá. | Khớp Unit Test `updateReview_returnsFalseOutsideFiveMinuteWindow`. | Pass |
| **TC_REV_011** | EP (CRUD / R7) | Xóa thành công Review và Khôi phục điểm Rating gốc | Khách hàng thực thi quyền Xóa đúng bài của mình. | Bắn DELETE API. | Thỏa mãn quyền Owner. | Xóa thành công bài đánh giá, điểm tổng Product cập nhật lại như cũ. | Khớp Unit Test `deleteReview_deletesAndRefreshesProductCache`. | Pass |
