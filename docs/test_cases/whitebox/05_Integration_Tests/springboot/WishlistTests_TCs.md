# BẢNG MA TRẬN KIỂM THỬ TÍCH HỢP WISHLIST LIFECYCLE (SPRING BOOT INTEGRATION)
**Đường dẫn file mã nguồn:** [WishlistDAO.java](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/src/main/java/com/example/demo/dao/WishlistDAO.java)  
**Đường dẫn file kiểm thử:** [WishlistTests.java](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/src/test/java/com/example/demo/WishlistTests.java)

---

## 1. TỔNG QUAN ĐẶC ĐIỂM KIỂM THỬ TÍCH HỢP
* **Phạm vi kiểm thử:** Tích hợp vòng đời đầy đủ của thực thể yêu thích (`Wishlist`) trên CSDL thực tế trong môi trường Spring Boot `@SpringBootTest` và `@Transactional`.
* **Mục tiêu:** Đảm bảo khả năng thêm mới, xử lý thêm trùng lặp (idempotent), truy vấn danh sách thông tin sản phẩm (`ProductInfo`), và xóa khỏi danh sách yêu thích một cách toàn vẹn.

---

## 2. MA TRẬN TEST CASES KIỂM THỬ TÍCH HỢP (1 TEST CASE)

| Mã TC | Hàm ở src/main | Hàm kiểm thử ở src/test | Giá trị Test đầu vào | Kịch bản tích hợp / Logic mục tiêu | Kết quả mong đợi |
| :---: | :--- | :--- | :--- | :--- | :--- |
| **TC_ITG_WSH_01** | `addWishlist`, `isFavorite`, `getUserWishlistProducts`, `removeWishlist` | `testAddAndRemoveWishlist()` | User `"test_user_qa"`, sản phẩm `"S001"` | Tích hợp trọn vòng đời yêu thích (Add ➔ Check Favorite ➔ Duplicate Add ➔ List Products ➔ Remove ➔ Check Favorite) | Kiểm tra ban đầu `isFavorite == false`; thêm thành công `added == true`; thêm lại an toàn `addedAgain == true`; danh sách trả về khác null; xóa thành công và trạng thái chuyển về `isFavorite == false` |
