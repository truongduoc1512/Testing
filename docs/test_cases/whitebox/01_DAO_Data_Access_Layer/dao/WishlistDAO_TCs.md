# ĐẶC TẢ KIỂM THỬ HỘP TRẮNG: `WishlistDAO`
## Tầng: Data Access Object (DAO) | Phân hệ: Sản phẩm yêu thích (Wishlist)
* **Tổng số Test Case:** **20 Test Cases**
* **Mục đích tài liệu:** Bảng ánh xạ chi tiết giữa các hàm nghiệp vụ trong `WishlistDAO.java` và các hàm kiểm thử trong `WishlistDAOTest.java`, phục vụ tra cứu nhanh giá trị test đầu vào, nhánh logic mục tiêu và kết quả mong đợi.

---

## 📌 PHẦN 1: BẢNG TỔNG HỢP CÁC HÀM TRONG `WishlistDAO.java`

Lớp `WishlistDAO` quản lý 6 hàm nghiệp vụ chính được bao phủ bởi 20 test case trong `WishlistDAOTest.java`:

| STT | Tên hàm trong `WishlistDAO.java` | Mục đích nghiệp vụ | Số Test Case đối ứng |
| :---: | :--- | :--- | :---: |
| 1 | `findWishlist(username, productCode)` | Tìm kiếm bản ghi wishlist theo cặp khóa User và SP | **4** |
| 2 | `isFavorite(username, productCode)` | Kiểm tra trạng thái sản phẩm đã được thích hay chưa | **2** |
| 3 | `addWishlist(username, productCode)` | Thêm sản phẩm vào wishlist (bảo toàn tính Idempotent) | **6** |
| 4 | `removeWishlist(username, productCode)` | Xóa sản phẩm khỏi danh sách yêu thích | **2** |
| 5 | `getUserWishlistProducts(String username)` | Lấy toàn bộ danh sách sản phẩm yêu thích của User | **3** |
| 6 | `getWishlistCount(String username)` | Đếm số lượng sản phẩm trong wishlist (ép kiểu an toàn) | **3** |

---

## 📑 PHẦN 2: BẢNG MA TRẬN ÁNH XẠ CHI TIẾT 20 TEST CASES

| Mã TC | Hàm ở `src/main` | Hàm kiểm thử ở `src/test` | Giá trị Test đầu vào (Inputs / Mock Data) | Nhánh logic / Điều kiện mục tiêu | Kết quả mong đợi (Assertion) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `TC_WSH_01` | `findWishlist` | `findWishlist_rejectsNullKeys` | `username = null` hoặc `code = null` | Nhánh kiểm tra null các khóa | Trả về `null` |
| `TC_WSH_02` | `findWishlist` | `findWishlist_returnsNullForEmptyResult` | Không tìm thấy trong CSDL | Nhánh `if (list.isEmpty())` = True | Trả về `null` |
| `TC_WSH_03` | `findWishlist` | `findWishlist_returnsNullWhenProviderReturnsNullList` | Query trả về danh sách `null` | Nhánh `if (list == null)` = True | Trả về `null` an toàn |
| `TC_WSH_04` | `findWishlist` | `findWishlist_returnsFirstMatchingRow` | Tìm thấy bản ghi tương ứng | Lấy phần tử index 0 của list | Trả về đúng entity `Wishlist` |
| `TC_WSH_05` | `isFavorite` | `isFavorite_reflectsWishlistPresence` | Sản phẩm đã có trong wishlist | Biểu thức `return findWishlist(...) != null` | Trả về `true` |
| `TC_WSH_06` | `isFavorite` | `isFavorite_returnsFalseWhenWishlistIsMissing` | Sản phẩm chưa được thích | Biểu thức `findWishlist(...) == null` | Trả về `false` |
| `TC_WSH_07` | `addWishlist` | `addWishlist_rejectsNullKeys` | Thiếu username hoặc productCode | Nhánh chặn null tham số | Bị từ chối thêm |
| `TC_WSH_08` | `addWishlist` | `addWishlist_rejectsUnknownProduct` | Mã SP không có trong CSDL | Nhánh `if (product == null)` = True | Bị từ chối |
| `TC_WSH_09` | `addWishlist` | `addWishlist_isIdempotentForExistingItem` | Đã thích rồi mà bấm thích tiếp | Nhánh `if (existing != null) return true` | Không lưu trùng lặp bản ghi |
| `TC_WSH_10` | `addWishlist` | `addWishlist_persistsNewItem` | SP hợp lệ chưa từng thích | Khối lưu entity `Wishlist` mới | Lưu thành công vào CSDL |
| `TC_WSH_11` | `addWishlist` | `addWishlist_currentlyAcceptsInactiveProduct_characterization` | Thêm SP đang ở trạng thái ẩn | Đặc tính cho phép lưu SP ẩn | Lưu thành công |
| `TC_WSH_12` | `addWishlist` | `addWishlist_propagatesPersistenceFailure` | Lỗi kết nối CSDL khi lưu | Lan truyền ngoại lệ lên tầng trên | Ném lỗi CSDL |
| `TC_WSH_13` | `removeWishlist` | `removeWishlist_returnsFalseWhenMissing` | Xóa SP chưa từng thêm | Nhánh `if (item == null)` = True | Trả về `false` |
| `TC_WSH_14` | `removeWishlist` | `removeWishlist_deletesExistingItem` | Xóa SP đang có trong wishlist | Câu lệnh `session.delete(item)` | Xóa thành công, trả về `true` |
| `TC_WSH_15` | `getUserWishlistProducts` | `getUserWishlistProducts_returnsEmptyForMissingUsername` | `username = null` hoặc `""` | Nhánh `if (username == null \|\| empty)` | Trả về danh sách rỗng |
| `TC_WSH_16` | `getUserWishlistProducts` | `getUserWishlistProducts_handlesNullProviderResult` | Query trả về null list | Nhánh `if (res == null)` chuyển empty | Trả về danh sách rỗng |
| `TC_WSH_17` | `getUserWishlistProducts` | `getUserWishlistProducts_mapsAllProductsIncludingInactive_characterization` | Danh sách có cả SP ẩn | Ánh xạ toàn bộ sang `ProductInfo` | Đầy đủ dữ liệu hiển thị |
| `TC_WSH_18` | `getWishlistCount` | `getWishlistCount_returnsZeroForMissingUsername` | `username = null` khi đếm | Nhánh `if (username == null)` = True | Trả về 0 |
| `TC_WSH_19` | `getWishlistCount` | `getWishlistCount_convertsNullAggregateToZero` | Aggregate count = `null` | Toán tử gán `res == null ? 0 : ...` | Trả về 0 an toàn |
| `TC_WSH_20` | `getWishlistCount` | `getWishlistCount_convertsLongAggregateToInt` | Ép kiểu `Long` sang `int` | Lệnh `((Long) res).intValue()` | Trả về đúng số lượng đếm |

---

## 📊 PHẦN 3: KẾT QUẢ ĐO LƯỜNG ĐỘ BAO PHỦ (JACOCO METRICS)

* **Lớp kiểm thử:** `com.example.demo.dao.WishlistDAO`
* **Statement Coverage (Instructions):** **198 / 198 (100.0%)**
* **Branch Coverage (Branches):** **22 / 22 (100.0%)**
* **Đánh giá:** Đạt độ phủ tuyệt đối 100% toàn bộ câu lệnh và nhánh logic.
