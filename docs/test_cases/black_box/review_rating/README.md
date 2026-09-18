# Black-Box API Test Cases: Chức năng 7 - Đánh giá & Wishlist (Review, Rating & Wishlist)

**Module:** `review_rating` (Đánh giá & Wishlist)  
**Dự án:** ShoeShop Enterprise E-Commerce Testing Platform  
**Phương pháp kiểm thử:** Kiểm thử API Hộp đen (Black-Box API Testing) qua Postman / Newman & JUnit Integration.  
**Kỹ thuật Black-Box chủ đạo:** Phân tích giá trị biên (BVA), Bảng quyết định (Decision Table), Kiểm thử chuyển đổi trạng thái (State Transition Testing - STT/FSM).

---

## 1. Structure & Files Overview

Thư mục này chứa toàn bộ tài liệu thiết kế và tập file cấu hình Postman / JSON Test Collections được chia theo từng nhóm testcase nghiệp vụ của Chức năng 7:

```
test_cases/black_box/review_rating/
├── Review_Rating_Postman_Collection.json   # Postman Collection tổng hợp đầy đủ các nhóm testcase
├── Review_Rating_Postman_Environment.json  # Postman Environment chứa các biến môi trường
├── group1_create_review.json               # JSON Collection riêng cho Nhóm 1: Tạo bài đánh giá mới
├── group2_update_review.json               # JSON Collection riêng cho Nhóm 2: Chỉnh sửa bài đánh giá
├── group3_delete_review.json               # JSON Collection riêng cho Nhóm 3: Xóa bài đánh giá
├── group4_query_reviews.json               # JSON Collection riêng cho Nhóm 4: Tra cứu danh sách đánh giá
├── group5_wishlist_operations.json         # JSON Collection riêng cho Nhóm 5: Danh sách yêu thích & State Transition
└── README.md                               # Tài liệu hướng dẫn thực thi và ma trận phủ testcase
```

---

## 2. Test Design Techniques Applied (Kỹ thuật Black-Box chủ đạo)

### 1. Phân tích giá trị biên (Boundary Value Analysis - BVA):
- **Biên số sao đánh giá (`ratingValue`):** $[1, 5]$ sao
  - Biên dưới hợp lệ: `1 sao` (Min) | Biên dưới không hợp lệ: `0 sao` (Min-1 -> `400 Bad Request`).
  - Biên trên hợp lệ: `5 sao` (Max) | Biên trên không hợp lệ: `6 sao` (Max+1 -> `400 Bad Request`).
- **Biên độ dài nhận xét (`comment`):** $[1, 2000]$ ký tự
  - Chuỗi rỗng / toàn khoảng trắng (`0` ký tự -> `400 Bad Request`).
  - Chuỗi cực đại (`2000` ký tự -> `201 Created` / `200 OK`).
- **Cửa sổ thời gian chỉnh sửa bài (Time Window):** $[0, 300,000]$ ms (5 phút)
  - $\le 300,000$ ms (Trong vòng 5 phút -> Cho phép chỉnh sửa).
  - $> 300,000$ ms (Quá 5 phút -> `400 Bad Request` khóa quyền sửa bài).

### 2. Bảng quyết định (Decision Table):
- Tổng hợp 7 quy tắc nghiệp vụ cốt lõi (R1 - R7) kết hợp các điều kiện: Phân quyền tài khoản (`ROLE_USER` vs `Guest` vs `ROLE_ADMIN`), Trạng thái sản phẩm (`ACTIVE` vs `INACTIVE`), Tính hợp lệ của tham số đầu vào, Quyền sở hữu (Ownership) và Cửa sổ thời gian chỉnh sửa.

### 3. Kiểm thử chuyển đổi trạng thái (State Transition Testing - STT / FSM):
- **Chuyển đổi trạng thái Wishlist (Wishlist State Machine):**
  - Trạng thái $S_0$: `UNFAVORITED` (Chưa yêu thích).
  - Chuyển đổi $T_1$: Gọi `POST /api/v1/wishlist/{code}` từ $S_0 \rightarrow S_1$ (`FAVORITED`, `favorite: true`, wishlistCount +1).
  - Chuyển đổi $T_2$: Gọi `POST /api/v1/wishlist/{code}` từ $S_1 \rightarrow S_0$ (`UNFAVORITED`, `favorite: false`, wishlistCount -1).
  - Chuyển đổi $T_3$: Gọi `DELETE /api/v1/wishlist/{code}` từ $S_1 \rightarrow S_0$ (`UNFAVORITED`, `favorite: false`).
  - Chuyển đổi $T_4$: Gọi `GET /api/v1/wishlist/check/{code}` (Truy vấn trạng thái hiện tại).

- **Vòng đời chuyển đổi trạng thái Review (Review Lifecycle State Machine):**
  - Trạng thái $R_0$: `NO_REVIEW` (Chưa có bài đánh giá).
  - Chuyển đổi $A_1$: `POST /api/v1/reviews` $\rightarrow$ Trạng thái $R_1$: `EDITABLE_REVIEW` ($t \le 5$ min).
  - Chuyển đổi $A_2$: `PUT /api/v1/reviews/{id}` ($t \le 5$ min) $\rightarrow$ Trạng thái $R_1$: `EDITABLE_REVIEW` (Cập nhật số sao & comment).
  - Chuyển đổi $A_3$: Quá thời hạn 5 phút ($t > 5$ min) $\rightarrow$ Trạng thái $R_2$: `LOCKED_REVIEW` (Khóa bài đánh giá).
  - Chuyển đổi $A_4$: `DELETE /api/v1/reviews/{id}` $\rightarrow$ Trạng thái $R_0$: `NO_REVIEW` (Xóa bài đánh giá & tính lại điểm Rating sản phẩm).

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

### Group 5: Wishlist Operations & State Transitions (Nhóm 5 - Danh sách yêu thích & State Transition)
- **TC_WISH_01:** Thêm sản phẩm vào Wishlist (Toggle 1 chạm lần 1: Transition $S_0 \rightarrow S_1$) -> `200 OK`, `favorite: true`.
- **TC_WISH_02:** Kiểm tra trạng thái sản phẩm trong Wishlist (`GET /api/v1/wishlist/check/{code}`) -> `200 OK`, `favorite: true/false`.
- **TC_WISH_03:** Hủy yêu thích sản phẩm qua Toggle 1 chạm (Toggle lần 2: Transition $S_1 \rightarrow S_0$) -> `200 OK`, `favorite: false`.
- **TC_WISH_04:** Xóa sản phẩm khỏi Wishlist qua API DELETE (`DELETE /api/v1/wishlist/{code}`) -> `200 OK`, `favorite: false`.
- **TC_WISH_05:** Lấy danh sách sản phẩm yêu thích (`GET /api/v1/wishlist`) -> `200 OK`, trả về mảng `ProductInfo`.
- **TC_WISH_06:** Chặn khách vãng lai chưa đăng nhập thao tác Wishlist -> `401 Unauthorized`.
- **TC_WISH_07:** Thêm sản phẩm không tồn tại vào Wishlist -> `404 Not Found`.

---

## 4. Execution Guide (Hướng dẫn thực thi)

### Cách 1: Chạy trực tiếp trên Postman App
1. Khởi động ứng dụng **Postman**.
2. Chọn **Import** -> Chọn file `Review_Rating_Postman_Collection.json` (hoặc từng file nhóm `group1_...json` đến `group5_...json`).
3. Import file `Review_Rating_Postman_Environment.json`.
4. Chọn môi trường `Review_Rating_Postman_Environment` và nhấn **Run Collection**.

### Cách 2: Chạy tự động qua Newman Command Line (Dùng `npx newman`)
> **Lưu ý:** Nếu gõ `newman` bị báo lỗi *"The term 'newman' is not recognized"*, hãy thêm `npx` vào trước lệnh (vì `npx` được tích hợp sẵn trong Node.js để tự động chạy package).

```powershell
npx newman run docs/test_cases/black_box/review_rating/Review_Rating_Postman_Collection.json `
  -e docs/test_cases/black_box/review_rating/Review_Rating_Postman_Environment.json
```

### Cách 3: Chạy từng nhóm Testcase riêng lẻ
```powershell
# Nhóm 1: Tạo mới đánh giá
npx newman run docs/test_cases/black_box/review_rating/group1_create_review.json `
  -e docs/test_cases/black_box/review_rating/Review_Rating_Postman_Environment.json

# Nhóm 2: Chỉnh sửa bài đánh giá
npx newman run docs/test_cases/black_box/review_rating/group2_update_review.json `
  -e docs/test_cases/black_box/review_rating/Review_Rating_Postman_Environment.json

# Nhóm 3: Xóa bài đánh giá
npx newman run docs/test_cases/black_box/review_rating/group3_delete_review.json `
  -e docs/test_cases/black_box/review_rating/Review_Rating_Postman_Environment.json

# Nhóm 4: Tra cứu danh sách đánh giá
npx newman run docs/test_cases/black_box/review_rating/group4_query_reviews.json `
  -e docs/test_cases/black_box/review_rating/Review_Rating_Postman_Environment.json

# Nhóm 5: Danh sách yêu thích & State Transition
npx newman run docs/test_cases/black_box/review_rating/group5_wishlist_operations.json `
  -e docs/test_cases/black_box/review_rating/Review_Rating_Postman_Environment.json
```
