# ĐẶC TẢ KIỂM THỬ HỘP TRẮNG: `WishlistController`
## Tầng: Web MVC Controller | Phân hệ: Sản phẩm yêu thích (HTML View)
* **Tổng số Test Case:** **4 Kịch bản kiểm thử (2 Test Methods)**
* **Mục đích tài liệu:** Bảng ánh xạ chi tiết giữa các hàm nghiệp vụ trong `WishlistController.java` và các hàm kiểm thử trong `WebControllerCoverageTest.java`, phục vụ tra cứu nhanh giá trị test đầu vào, nhánh logic mục tiêu và kết quả mong đợi.

---

## 📌 PHẦN 1: BẢNG TỔNG HỢP CÁC HÀM TRONG `WishlistController.java`

Lớp `WishlistController` quản lý 1 hàm nghiệp vụ hiển thị giao diện danh sách yêu thích:

| STT | Tên hàm trong `WishlistController.java` | Mục đích nghiệp vụ | Số Test Case đối ứng |
| :---: | :--- | :--- | :---: |
| 1 | `wishlistPage(Model model)` | Hiển thị danh sách sản phẩm yêu thích (yêu cầu đăng nhập, chặn anonymous/unauthenticated) | **4** |

---

## 📑 PHẦN 2: BẢNG MA TRẬN ÁNH XẠ CHI TIẾT 4 TEST CASES

| Mã TC | Hàm ở `src/main` | Hàm kiểm thử ở `src/test` | Giá trị Test đầu vào (Inputs / Mock Data) | Nhánh logic / Điều kiện mục tiêu | Kết quả mong đợi (Assertion) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `TC_WSH_CTL_01` | `wishlistPage` | `wishlistPage_redirectsLoginRequiredAuthentication` | `auth = null` | Nhánh `if (auth == null)` = True | Redirect về `/admin/login` |
| `TC_WSH_CTL_02` | `wishlistPage` | `wishlistPage_redirectsLoginRequiredAuthentication` | `auth.isAuthenticated() = false` | Nhánh `if (!auth.isAuthenticated())` = True | Redirect về `/admin/login` |
| `TC_WSH_CTL_03` | `wishlistPage` | `wishlistPage_redirectsLoginRequiredAuthentication` | `auth.getName() = "anonymousUser"` | Nhánh `if ("anonymousUser".equals(auth.getName()))` | Redirect về `/admin/login` |
| `TC_WSH_CTL_04` | `wishlistPage` | `wishlistPage_showsProductsForAuthenticatedUser` | User đã đăng nhập (`buyer`), wishlist có 2 SP | Nhánh xác thực hợp lệ, nạp wishlist vào Model | Render view `wishlist`, `wishlistCount = 2` |

---

## 📊 PHẦN 3: KẾT QUẢ ĐO LƯỜNG ĐỘ BAO PHỦ (JACOCO METRICS)

* **Lớp kiểm thử:** `com.example.demo.controller.WishlistController`
* **Statement Coverage (Instructions):** **100.0%**
* **Branch Coverage (Branches):** **100.0%**
* **Đánh giá:** Đạt độ phủ tuyệt đối 100% toàn bộ câu lệnh và nhánh logic xác thực của trang danh sách yêu thích.
