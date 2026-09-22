# ĐẶC TẢ KIỂM THỬ HỘP TRẮNG: `ProductDAO`
## Tầng: Data Access Object (DAO) | Phân hệ: Quản lý Sản phẩm & Danh mục
* **Tổng số Test Case:** **31 Test Cases**
* **Mục đích tài liệu:** Bảng ánh xạ chi tiết giữa các hàm nghiệp vụ trong `ProductDAO.java` và các hàm kiểm thử trong `ProductDAOTest.java`, phục vụ tra cứu nhanh giá trị test đầu vào, nhánh logic mục tiêu và kết quả mong đợi.

---

## 📌 PHẦN 1: BẢNG TỔNG HỢP CÁC HÀM TRONG `ProductDAO.java`

Lớp `ProductDAO` quản lý 7 hàm nghiệp vụ chính được bao phủ bởi 31 test case trong `ProductDAOTest.java`:

| STT | Tên hàm trong `ProductDAO.java` | Mục đích nghiệp vụ | Số Test Case đối ứng |
| :---: | :--- | :--- | :---: |
| 1 | `findProduct(String code)` | Tìm entity sản phẩm theo mã Code | **1** |
| 2 | `findActiveProduct(String code)` | Tìm sản phẩm đang ở trạng thái kinh doanh (`active = true`) | **2** |
| 3 | `findProductForUpdate(String code)` | Lấy sản phẩm kèm khóa ghi bi quan (`PESSIMISTIC_WRITE`) | **1** |
| 4 | `findProductInfo(String code)` | Lấy DTO `ProductInfo` để hiển thị trên web | **2** |
| 5 | `save(ProductForm form, Authentication auth)` | Tạo mới hoặc sửa sản phẩm, kiểm tra owner và lưu ảnh | **10** |
| 6 | `queryProducts(...)` | Tìm kiếm và xây dựng câu lệnh HQL lọc đa tiêu chí động | **13** |
| 7 | `deleteProduct(String code)` | Xóa mềm sản phẩm (`active = false`) kèm khóa Session | **2** |

---

## 📑 PHẦN 2: BẢNG MA TRẬN ÁNH XẠ CHI TIẾT 31 TEST CASES

| Mã TC | Hàm ở `src/main` | Hàm kiểm thử ở `src/test` | Giá trị Test đầu vào (Inputs / Mock Data) | Nhánh logic / Điều kiện mục tiêu | Kết quả mong đợi (Assertion) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `TC_PRD_01` | `findProduct` | `findProduct_delegatesLookupWithoutNormalization` | `code = "p01"` | Lệnh `session.find(Product.class, code)` | Trả về entity `Product` |
| `TC_PRD_02` | `findActiveProduct` | `findActiveProduct_returnsNullForMissingOrNonActiveProduct` | `active = false` | Nhánh `if (!p.isActive())` = True | Trả về `null` |
| `TC_PRD_03` | `findActiveProduct` | `findActiveProduct_acceptsStatusCaseInsensitively` | `status = "AcTiVe"` | `equalsIgnoreCase("ACTIVE")` = True | Trả về sản phẩm |
| `TC_PRD_04` | `findProductForUpdate` | `findProductForUpdate_usesPessimisticWriteLock` | `code = "P01"` | Gọi `LockMode.PESSIMISTIC_WRITE` | Khóa ghi Session thành công |
| `TC_PRD_05` | `findProductInfo` | `findProductInfo_returnsNullForUnavailableProduct` | Mã không có trong CSDL | Nhánh `if (p == null)` = True | Trả về `null` |
| `TC_PRD_06` | `findProductInfo` | `findProductInfo_mapsActiveProduct` | Entity sản phẩm active | Ánh xạ toàn bộ trường Entity ➔ DTO | Trả về DTO `ProductInfo` |
| `TC_PRD_07` | `save` | `save_rejectsEveryInvalidFormBoundary` | `price = -1`, `stock = -1` | Nhánh kiểm tra giá trị form âm | Ném `IllegalArgumentException` |
| `TC_PRD_08` | `save` | `save_rejectsMissingAuthentication` | `auth = null` | Nhánh `if (auth == null)` = True | Ném lỗi bảo mật |
| `TC_PRD_09` | `save` | `save_rejectsUnauthenticatedPrincipal` | `auth.isAuthenticated() = false` | Nhánh `if (!auth.isAuthenticated())` | Bị từ chối lưu SP |
| `TC_PRD_10` | `save` | `save_rejectsAnonymousPrincipal` | `principal = "anonymousUser"` | Nhánh chặn khách vãng lai | Bị từ chối lưu SP |
| `TC_PRD_11` | `save` | `save_createsProductWithNormalizedFieldsOwnerAndBoundedMetadata` | Form hợp lệ, User đã login | Khối lệnh khởi tạo entity mới | Lưu SP vào CSDL với đúng owner |
| `TC_PRD_12` | `save` | `save_updatesOwnedProductWithoutPersistingAgain` | Sửa SP do chính mình tạo | Nhánh `owner.equals(currentUser)` | Cập nhật bản ghi cũ, không tạo mới |
| `TC_PRD_13` | `save` | `save_rejectsUpdateByDifferentOwner` | Sửa SP thuộc về người khác | Nhánh `!owner.equals(currentUser)` | Ném lỗi cấm truy cập |
| `TC_PRD_14` | `save` | `save_setsImageOnlyWhenUploadedBytesAreNonEmpty` | Upload file ảnh 0 byte | `bytes != null && bytes.length > 0` = False | Giữ nguyên ảnh cũ |
| `TC_PRD_15` | `save` | `save_wrapsImageReadFailureAsIllegalArgument` | Stream file ảnh bị lỗi đọc | Bắt `IOException` ➔ ném `IllegalArgument` | Ném `IllegalArgumentException` |
| `TC_PRD_16` | `save` | `save_samplesBothRandomBooleanMetadataOutcomesWithinSafetyLimit` | Random boolean flags | Cả 2 nhánh `true/false` của cờ metadata | Bao phủ đủ cả 2 nhánh |
| `TC_PRD_17` | `queryProducts` | `queryProducts_withoutOwnerRestrictsToActiveAndUsesDefaultSort` | `owner = null` | Sinh `WHERE p.active = true ORDER BY p.code` | Query đúng HQL |
| `TC_PRD_18` | `queryProducts` | `queryProducts_withOwnerIncludesInactiveOwnerInventory` | `owner = "seller1"` | Bỏ qua điều kiện `active = true` | Xem cả SP ẩn của Seller |
| `TC_PRD_19` | `queryProducts` | `queryProducts_likeNameOnlyOverloadDelegatesWithActiveScope` | `likeName = "Nike"` | HQL sinh `WHERE lower(name) LIKE :likeName` | Lọc đúng từ khóa tên SP |
| `TC_PRD_20` | `queryProducts` | `queryProducts_withoutCategoryOverloadDelegatesAllFilters` | Bỏ qua tham số category | Bỏ qua điều kiện lọc category trong WHERE | Query không chứa category |
| `TC_PRD_21` | `queryProducts` | `queryProducts_bindsNameCategoryPriceAndBooleanFilters` | Tên, Giá [100, 500], Category | Kết hợp đồng thời 4 điều kiện lọc | Mọi tham số đều bind vào HQL |
| `TC_PRD_22` | `queryProducts` | `queryProducts_treatsEmptyOrBlankOptionalTextAsAbsent` | `keyword = "   "` | Kiểm tra chuỗi rỗng/khoảng trắng | Không sinh mệnh đề WHERE |
| `TC_PRD_23` | `queryProducts` | `queryProducts_bindsEachNonEmptyLocationToken` | `location = "HN, SG"` | Vòng lặp tách token vị trí | Bind từng token vào query |
| `TC_PRD_24` | `queryProducts` | `queryProducts_ignoresLocationContainingOnlySeparators` | `location = ", ;"` | Không có token hợp lệ nào | Bỏ qua lọc vị trí |
| `TC_PRD_25` | `queryProducts` | `queryProducts_normalizesEverySupportedLocationAlias` | Alias `"HN"`, `"HCM"` | Map alias ➔ Tên đầy đủ chuẩn | Ánh xạ chuẩn tên địa lý |
| `TC_PRD_26` | `queryProducts` | `queryProducts_bindsSingleBrandAsScalar` | 1 brand `"Adidas"` | Nhánh `brands.size() == 1` | Bind biến đơn lẻ |
| `TC_PRD_27` | `queryProducts` | `queryProducts_bindsMultipleBrandsAsList` | Nhiều brand `["Nike", "Puma"]` | Nhánh `brands.size() > 1` | Bind `IN (:brands)` |
| `TC_PRD_28` | `queryProducts` | `queryProducts_ignoresBrandContainingOnlySeparators` | Brand rỗng/phân cách | Nhánh danh sách brand rỗng | Bỏ qua điều kiện brand |
| `TC_PRD_29` | `queryProducts` | `queryProducts_selectsRequestedSort` | `sort = "price_asc"` | Cấu trúc `switch/case` kiểu sắp xếp | Sinh `ORDER BY p.price ASC` |
| `TC_PRD_30` | `deleteProduct` | `deleteProduct_doesNothingWhenProductMissing` | `code = "MISSING"` | Nhánh `if (product == null)` = True | Không làm gì, không ném lỗi |
| `TC_PRD_31` | `deleteProduct` | `deleteProduct_softDeletesUnderWriteLock` | `code = "P01"` | Gán `product.setActive(false)` kèm khóa | Xóa mềm thành công |

---

## 📊 PHẦN 3: KẾT QUẢ ĐO LƯỜNG ĐỘ BAO PHỦ (JACOCO METRICS)

* **Lớp kiểm thử:** `com.example.demo.dao.ProductDAO`
* **Statement Coverage (Instructions):** **882 / 895 (98.5%)**
* **Branch Coverage (Branches):** **102 / 104 (98.1%)**
* **Đánh giá:** Đạt độ phủ rất cao. 2 nhánh chưa phủ là các trường hợp ngoại lệ hiếm của stream đọc file ảnh hỏng ở mức hệ thống I/O.
