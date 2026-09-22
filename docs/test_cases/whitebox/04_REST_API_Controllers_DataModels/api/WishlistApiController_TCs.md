# BẢNG MA TRẬN KIỂM THỬ HỘP TRẮNG WISHLIST API CONTROLLER
**Đường dẫn file mã nguồn:** [WishlistApiController.java](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/src/main/java/com/example/demo/controller/api/WishlistApiController.java)  
**Đường dẫn file kiểm thử:** [WishlistApiControllerTest.java](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/src/test/java/com/example/demo/controller/api/WishlistApiControllerTest.java)

---

## 1. TỔNG QUAN ĐỘ BAO PHỦ (JACOCO COVERAGE METRICS)

| File Target | Instruction Coverage | Branch Coverage | Line Coverage | Method Coverage | Trạng Thái |
| :--- | :---: | :---: | :---: | :---: | :---: |
| `WishlistApiController.java` | 100% | 100% | 100% | 100% | Đạt |

---

## 2. MA TRẬN TEST CASES KIỂM THỬ HỘP TRẮNG (13 TEST CASES)

| Mã TC | Hàm ở src/main | Hàm kiểm thử ở src/test | Giá trị Test đầu vào | Nhánh logic / Điều kiện mục tiêu | Kết quả mong đợi |
| :---: | :--- | :--- | :--- | :--- | :--- |
| **TC_WSH_01** | `getWishlist()` | `getWishlist_rejectsLoginRequiredAuthentication(String, Authentication)` | User chưa đăng nhập (null, unauthenticated token, anonymousUser) | Nhánh lấy danh sách yêu thích yêu cầu xác thực người dùng | HTTP 401 UNAUTHORIZED, body `success: false` |
| **TC_WSH_02** | `getWishlist()` | `getWishlist_returnsAuthenticatedUserProducts()` | User `buyer` đã đăng nhập, DAO trả về list 2 sản phẩm | Nhánh lấy danh sách sản phẩm yêu thích của user thành công | HTTP 200 OK, trả về danh sách `ProductInfo` từ DAO |
| **TC_WSH_03** | `checkWishlist(String)` | `checkWishlist_returnsFalseWithoutAuthenticatedUsername()` | Chưa đăng nhập, `productCode = "P1"` | Nhánh kiểm tra trạng thái yêu thích khi chưa đăng nhập (`username == null`) | Body `{favorite: false}`, không gọi DAO |
| **TC_WSH_04** | `checkWishlist(String)` | `checkWishlist_returnsFalseForNullProductCode()` | User `buyer` đã đăng nhập, `productCode = null` | Nhánh kiểm tra trạng thái khi mã sản phẩm null (`productCode == null`) | Body `{favorite: false}`, không gọi DAO |
| **TC_WSH_05** | `checkWishlist(String)` | `checkWishlist_returnsFalseForProductOutsideWishlist()` | User `buyer`, `productCode = "P1"`, DAO trả về `false` | Nhánh sản phẩm chưa nằm trong danh sách yêu thích của người dùng | Body `{favorite: false}` |
| **TC_WSH_06** | `checkWishlist(String)` | `checkWishlist_returnsTrueForFavoriteProduct()` | User `buyer`, `productCode = "P2"`, DAO trả về `true` | Nhánh sản phẩm đã nằm trong danh sách yêu thích | Body `{favorite: true}` |
| **TC_WSH_07** | `toggleWishlist(String)` | `toggleWishlist_requiresAuthentication()` | Chưa đăng nhập, `productCode = "P1"` | Nhánh bật/tắt yêu thích yêu cầu người dùng phải đăng nhập | HTTP 401 UNAUTHORIZED, không gọi DAO |
| **TC_WSH_08** | `toggleWishlist(String)` | `toggleWishlist_returnsNotFoundWhenProductDoesNotExist()` | User `buyer`, `productCode = "missing"`, Product DAO trả về `null` | Nhánh kiểm tra sản phẩm không tồn tại trong hệ thống | HTTP 404 NOT FOUND, body `success: false` |
| **TC_WSH_09** | `toggleWishlist(String)` | `toggleWishlist_addsProductOutsideWishlist()` | User `buyer`, `productCode = "P1"`, hiện chưa yêu thích (`isFavorite == false`) | Nhánh thêm sản phẩm vào danh sách yêu thích khi chưa có trong danh sách | HTTP 200 OK, body `{favorite: true, wishlistCount: 1}`, gọi `addWishlist` |
| **TC_WSH_10** | `toggleWishlist(String)` | `toggleWishlist_removesFavoriteProduct()` | User `buyer`, `productCode = "P2"`, hiện đã yêu thích (`isFavorite == true`) | Nhánh xóa sản phẩm khỏi danh sách yêu thích khi đã có trong danh sách | HTTP 200 OK, body `{favorite: false, wishlistCount: 0}`, gọi `removeWishlist` |
| **TC_WSH_11** | `removeWishlist(String)` | `removeWishlist_requiresAuthentication()` | Chưa đăng nhập, `productCode = "P1"` | Nhánh xóa trực tiếp khỏi yêu thích yêu cầu đăng nhập | HTTP 401 UNAUTHORIZED, DAO không được gọi |
| **TC_WSH_12** | `removeWishlist(String)` | `removeWishlist_reportsProductWasAlreadyAbsent()` | User `buyer`, `productCode = "P1"`, DAO trả về `false` (sản phẩm vốn không có trong wishlist) | Nhánh xóa sản phẩm không tồn tại trong danh sách yêu thích | HTTP 200 OK, body `{success: true, favorite: false, wishlistCount: 2}` |
| **TC_WSH_13** | `removeWishlist(String)` | `removeWishlist_reportsDeletedProductAndUpdatedCount()` | User `buyer`, `productCode = "P2"`, DAO xóa thành công trả về `true` | Nhánh xóa thành công sản phẩm khỏi danh sách yêu thích | HTTP 200 OK, body `{success: true, wishlistCount: 1}` |
