# Black-Box API Test Cases: Chức năng 6 - Đánh giá sản phẩm (Review & Rating)

**Module:** `review_rating`  
**Dự án:** ShoeShop Enterprise E-Commerce Testing Platform  
**Phương pháp kiểm thử:** Kiểm thử API Hộp đen (Black-Box API Testing) qua Postman / Newman & JUnit Integration.

---

## 1. Structure & Files Overview

Thư mục này chứa toàn bộ tài liệu thiết kế và tập file cấu hình Postman / JSON Test Collections được chia theo từng nhóm testcase nghiệp vụ của Chức năng 6:

```
test_cases/black_box/review_rating/
├── Review_Rating_Postman_Collection.json   # Postman Collection tổng hợp đầy đủ các nhóm testcase
├── Review_Rating_Postman_Environment.json  # Postman Environment chứa các biến môi trường
├── group1_create_review.json               # JSON Collection riêng cho Nhóm 1: Tạo bài đánh giá mới
├── group2_update_review.json               # JSON Collection riêng cho Nhóm 2: Chỉnh sửa bài đánh giá
├── group3_delete_review.json               # JSON Collection riêng cho Nhóm 3: Xóa bài đánh giá
├── group4_query_reviews.json               # JSON Collection riêng cho Nhóm 4: Tra cứu danh sách đánh giá
└── README.md                               # Tài liệu hướng dẫn thực thi và ma trận phủ testcase
```

---

## 2. Test Design Techniques Applied

1. **Phân hoạch lớp tương đương (Equivalence Partitioning - EP):**
   - Phân định quyền truy cập giữa Khách vãng lai (`Guest` -> `401 Unauthorized`), Khách hàng đã đăng nhập (`ROLE_USER` -> Cho phép), Quản trị viên (`ROLE_ADMIN` -> `403 Forbidden` để chống Seeding).
   - Kiểm tra trạng thái sản phẩm (`ACTIVE` vs `INACTIVE`/`DRAFT`).
   - Ràng buộc quyền sở hữu (Chủ bài viết vs Người dùng khác).
2. **Phân tích giá trị biên (Boundary Value Analysis - BVA):**
   - Biên số sao đánh giá $[1, 5]$ sao: Thử nghiệm điểm biên hợp lệ (1 sao, 5 sao) và ngoài biên (0 sao -> `400 Bad Request`, 6 sao -> `400 Bad Request`).
   - Biên độ dài comment $[1, 2000]$ ký tự.
   - **Cửa sổ thời gian chỉnh sửa (5-minute time window):** Ràng buộc thời gian $\le 300,000$ ms (cho phép sửa) và $> 300,000$ ms (khóa quyền sửa -> `400 Bad Request`).
3. **Bảng quyết định (Decision Table):**
   - Tổng hợp 7 quy tắc cốt lõi (R1 - R7) để bảo vệ toàn vẹn dữ liệu đánh giá và tự động tính lại điểm trung bình của sản phẩm.

---

## 3. Summary of Test Case Groups

### Group 1: Create Review (Nhóm 1 - Gửi bài đánh giá mới)
- **TC_REV_001:** Đánh giá 5 sao hợp lệ (BVA Max) -> `201 Created`, tính lại điểm sản phẩm.
- **TC_REV_002:** Đánh giá 1 sao hợp lệ (BVA Min) -> `201 Created`, tính lại điểm sản phẩm.
- **TC_REV_003:** Chặn comment rỗng hoặc chỉ có khoảng trắng -> `400 Bad Request`.
- **TC_REV_004:** Chặn đánh giá 0 sao (BVA Min-1) -> `400 Bad Request`.
- **TC_REV_005:** Chặn đánh giá 6 sao (BVA Max+1) -> `400 Bad Request`.
- **TC_REV_006:** Báo lỗi 401: Khách vãng lai chưa đăng nhập -> `401 Unauthorized`.
- **TC_REV_007:** Báo lỗi 403: Cấm Admin dùng quyền tạo đánh giá ảo -> `403 Forbidden`.
- **TC_REV_008:** Chặn đánh giá sản phẩm INACTIVE / không tồn tại -> `400 Bad Request` / `404 Not Found`.

### Group 2: Update Review (Nhóm 2 - Chỉnh sửa bài đánh giá)
- **TC_REV_009:** Chặn hành vi sửa Review của người khác (Ownership Check) -> `400 Bad Request` / `403 Forbidden`.
- **TC_REV_010:** Chặn sửa Review khi đã quá 5 phút kể từ lúc đăng -> `400 Bad Request`.
- **TC_REV_010B:** Sửa thành công bài đánh giá trong vòng 5 phút (Chính chủ) -> `200 OK`.

### Group 3: Delete Review (Nhóm 3 - Xóa bài đánh giá)
- **TC_REV_011:** Xóa thành công Review chính chủ và khôi phục lại điểm Rating gốc của sản phẩm -> `200 OK`.
- **TC_REV_011B:** Thử xóa lại review không tồn tại -> `400 Bad Request` / `404 Not Found`.

### Group 4: Query Reviews (Nhóm 4 - Tra cứu danh sách đánh giá)
- **TC_REV_GET_01:** Tra cứu danh sách đánh giá của sản phẩm theo mã Code (`GET /api/v1/reviews/product/{code}`) -> `200 OK`.

---

## 4. Execution Guide (Hướng dẫn thực thi)

### Cách 1: Chạy trực tiếp trên Postman App
1. Khởi động ứng dụng **Postman**.
2. Select **Import** -> Chọn `Review_Rating_Postman_Collection.json` (hoặc từng file nhóm `group1_...json`, `group2_...json`, `group3_...json`, `group4_...json`).
3. Import file `Review_Rating_Postman_Environment.json`.
4. Chọn môi trường `Review_Rating_Postman_Environment` và nhấn **Run Collection**.

### Cách 2: Chạy tự động qua Newman Command Line (Dùng `npx newman`)
> **Lưu ý:** Nếu gõ `newman` bị báo lỗi *"The term 'newman' is not recognized"*, hãy thêm `npx` vào trước lệnh (vì `npx` được tích hợp sẵn trong Node.js để tự động chạy package).

```powershell
npx newman run test_cases/black_box/review_rating/Review_Rating_Postman_Collection.json `
  -e test_cases/black_box/review_rating/Review_Rating_Postman_Environment.json
```

### Cách 3: Chạy từng nhóm Testcase riêng lẻ
```powershell
# Nhóm 1: Tạo mới đánh giá
npx newman run test_cases/black_box/review_rating/group1_create_review.json `
  -e test_cases/black_box/review_rating/Review_Rating_Postman_Environment.json

# Nhóm 2: Chỉnh sửa bài đánh giá
npx newman run test_cases/black_box/review_rating/group2_update_review.json `
  -e test_cases/black_box/review_rating/Review_Rating_Postman_Environment.json

# Nhóm 3: Xóa bài đánh giá
npx newman run test_cases/black_box/review_rating/group3_delete_review.json `
  -e test_cases/black_box/review_rating/Review_Rating_Postman_Environment.json

# Nhóm 4: Tra cứu danh sách đánh giá
npx newman run test_cases/black_box/review_rating/group4_query_reviews.json `
  -e test_cases/black_box/review_rating/Review_Rating_Postman_Environment.json
```
