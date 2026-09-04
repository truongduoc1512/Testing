# Ma trận Truy xuất (Traceability Matrix): Chức năng Đánh giá sản phẩm (Review & Rating)

Tài liệu này ánh xạ các Test Case Đặc tả (từ `docs/test_cases/06_Review_Rating.md`) với các Test Script (File Java Automation Test) thực tế trong mã nguồn.

| Mã Test Case | File Test (Test Script Location) | Tên Hàm Test (Method Name) | Trạng thái |
| :--- | :--- | :--- | :--- |
| **TC_REV_001** | `src/test/java/com/example/demo/controller/api/ReviewApiControllerTest.java` | `saveReview_trimsAndReturnsCreatedReview` | Automated |
| **TC_REV_002** | `src/test/java/com/example/demo/dao/ProductReviewDAOTest.java` | `saveReview_acceptsCaseInsensitiveActiveStatusAndRecalculatesRoundedCache` | Automated |
| **TC_REV_003** | `src/test/java/com/example/demo/controller/api/ReviewApiControllerTest.java` | `saveReview_rejectsInvalidForm` | Automated |
| **TC_REV_004** | `src/test/java/com/example/demo/controller/api/ReviewApiControllerTest.java` | `saveReview_rejectsInvalidForm` | Automated |
| **TC_REV_005** | `src/test/java/com/example/demo/controller/api/ReviewApiControllerTest.java` | `saveReview_rejectsInvalidForm` | Automated |
| **TC_REV_006** | `src/test/java/com/example/demo/controller/api/ReviewApiControllerTest.java` | `saveReview_rejectsLoginRequiredAuthentication` | Automated |
| **TC_REV_007** | `src/test/java/com/example/demo/controller/api/ReviewApiControllerTest.java` | `saveReview_rejectsAdminRole` | Automated |
| **TC_REV_008** | `src/test/java/com/example/demo/dao/ProductReviewDAOTest.java` | `saveReview_rejectsMissingOrNonActiveProduct` | Automated |
| **TC_REV_009** | `src/test/java/com/example/demo/dao/ProductReviewDAOTest.java` | `updateReview_returnsFalseForDifferentOwner` | Automated |
| **TC_REV_010** | `src/test/java/com/example/demo/dao/ProductReviewDAOTest.java` | `updateReview_returnsFalseOutsideFiveMinuteWindow` | Automated |
| **TC_REV_011** | `src/test/java/com/example/demo/dao/ProductReviewDAOTest.java` | `deleteReview_deletesAndRefreshesProductCache` | Automated |

---

## Ánh xạ với Hệ thống Postman (E2E API Testing)
Ngoài việc chạy Unit Test bằng JUnit ở mức Backend, toàn bộ các luồng nghiệp vụ trên đều được tự động hóa End-to-End thông qua bộ Postman Collection để mô phỏng chính xác thao tác gửi yêu cầu từ trình duyệt của người dùng.

- **File Thực thi Postman:** `docs/Shoeshop_API_Collection.json`
- **File Môi trường Postman:** `docs/Shoeshop_Postman_Environment.json`

### Bảng Ánh xạ Vị trí Code trong Postman Collection
Dưới đây là vị trí (số dòng - Line Number) chính xác của các đoạn script test API Đánh giá sản phẩm bên trong file `Shoeshop_API_Collection.json` (Tổng cộng 1786 dòng):

| Nhóm API | Request Name (Tên API) | Vị trí (Line Number) | Ánh xạ Test Case |
| :--- | :--- | :--- | :--- |
| **Reviews API** | `GET /api/v1/reviews/product/{code} - Fetch Product Reviews` | Dòng 960 | Xem danh sách đánh giá |
| **Reviews API** | `POST /api/v1/reviews - Submit Product Review & Rating` | Dòng 991 | TC_REV_001 $\rightarrow$ TC_REV_008 |
| **Reviews API** | `PUT /api/v1/reviews/{id} - Update Existing Product Review` | Dòng 1040 | TC_REV_009 $\rightarrow$ TC_REV_010 |
| **Reviews API** | `DELETE /api/v1/reviews/{id} - Delete Product Review` | Dòng 1081 | TC_REV_011 |

> **Lưu ý:** Mặc dù đã map chính xác số dòng, trong thực tế khi maintain source code, việc dựa vào `Request Name` và cấu trúc Folder trong Postman sẽ an toàn và bền vững hơn rất nhiều so với việc dựa vào số dòng (Line number).
