# ĐẶC TẢ KIỂM THỬ HỘP TRẮNG: `ProductDAOIntegrationTest`
## Tầng: Data Access Object (DAO) | Phân hệ: Kiểm thử Tích hợp CSDL Thực tế (MySQL Integration)
* **File mã nguồn đối ứng (`src/main`):** [ProductDAO.java](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/src/main/java/com/example/demo/dao/ProductDAO.java)
* **File mã nguồn kiểm thử (`src/test`):** [ProductDAOIntegrationTest.java](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/src/test/java/com/example/demo/dao/ProductDAOIntegrationTest.java)
* **Tổng số Test Case:** **8 Test Cases**
* **Mục đích tài liệu:** Bảng ánh xạ chi tiết bộ kiểm thử tích hợp (Integration Tests) của `ProductDAO.java` chạy trên môi trường CSDL MySQL và Hibernate Session thực tế, phục vụ tra cứu kiểm chứng tính toàn vẹn dữ liệu ACID, cơ chế commit/rollback, lưu trữ ảnh BLOB và cô lập dữ liệu test.

---

## 📌 PHẦN 1: BẢNG TỔNG HỢP CÁC HÀM ĐƯỢC KIỂM THỬ TÍCH HỢP

| STT | Tên hàm trong `ProductDAO.java` | Mục đích kiểm thử tích hợp thực tế | Số Test Case đối ứng |
| :---: | :--- | :--- | :---: |
| 1 | `save(ProductForm form)` | Commit tạo mới sản phẩm và ánh xạ đầy đủ các cột vào bảng `Products` | **1** |
| 2 | `save(ProductForm form)` | Cập nhật thông tin sản phẩm bền vững qua nhiều transaction độc lập | **1** |
| 3 | `save(ProductForm form)` | Chặn sửa sản phẩm của owner khác và bảo toàn dữ liệu gốc trong CSDL | **1** |
| 4 | `deleteProduct(String code)` | Commit xóa mềm (`STATUS = 'INACTIVE'`) và loại khỏi kết quả tìm kiếm active | **1** |
| 5 | `queryProducts(...)` | Thực thi câu HQL phức tạp thật trên MySQL và ánh xạ dữ liệu `PaginationResult` | **1** |
| 6 | `save(ProductForm form)` | Lưu trữ mảng byte file ảnh upload thành kiểu nhị phân BLOB trong CSDL | **1** |
| 7 | `findProductForUpdate(String code)` | Đọc và khóa ghi bản ghi qua Hibernate Session thực tế | **1** |
| 8 | Quản lý vòng đời dữ liệu test | Kiểm tra tính cô lập dữ liệu (Test Isolation), không xóa nhầm sản phẩm mẫu | **1** |

---

## 📑 PHẦN 2: BẢNG MA TRẬN ÁNH XẠ CHI TIẾT 8 TEST CASES TÍCH HỢP

| Mã TC | Hàm ở `src/main` | Hàm kiểm thử ở `src/test` | Dữ liệu Test thực tế trên MySQL | Cơ chế kiểm chứng trên CSDL thật | Kết quả mong đợi (Assertion) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `TC_INT_PRD_01` | `save` | `save_commitsNewProductAndDatabaseAppliesMappedFields` | `code = PREFIX + "CREATE"`, `price = 125.5`, `stock = 9` | Query trực tiếp bằng `JdbcTemplate` lên bảng `Products` | Bản ghi tồn tại trong CSDL, đầy đủ các trường mapped |
| `TC_INT_PRD_02` | `save` | `save_updatesOwnedProductAcrossCommittedTransactions` | Gửi form sửa: `price = 220`, `stock = 12` ở transaction mới | Kiểm tra dữ liệu được ghi đè bền vững sau khi commit | CSDL cập nhật giá mới (220) và tồn kho (12) |
| `TC_INT_PRD_03` | `save` | `save_rejectsDifferentOwnerAndPreservesCommittedRow` | User khác (`other_owner`) cố tình gọi save sửa SP | Transaction bị rollback khi ném `AccessDeniedException` | Ném lỗi bảo mật, dữ liệu gốc trong CSDL được bảo toàn |
| `TC_INT_PRD_04` | `deleteProduct` | `deleteProduct_commitsSoftDeleteAndActiveLookupStopsReturningIt` | Gọi `deleteProduct(code)` trên SP đang active | Kiểm tra cột `STATUS` trong bảng `Products` và gọi lại hàm query | Cột `STATUS` đổi thành `'INACTIVE'`, query active trả về `null` |
| `TC_INT_PRD_05` | `queryProducts` | `queryProducts_executesGeneratedHqlAndReturnsPersistedProjection` | Tạo SP với tên độc bản, gọi lọc HQL với khoảng giá [140, 160] | Thực thi trực tiếp câu HQL trên MySQL và map ra DTO | Trả về đúng 1 bản ghi khớp điều kiện lọc |
| `TC_INT_PRD_06` | `save` | `save_persistsUploadedImageBytesAsBlob` | Upload file ảnh `byte[] {1, 2, 3, 4, 5}` | Query `SELECT IMAGE FROM Products` bằng `JdbcTemplate` | Mảng byte nhị phân BLOB đọc từ CSDL khớp 100% |
| `TC_INT_PRD_07` | `findProductForUpdate` | `findProductForUpdate_readsPersistedProductThroughRealHibernateSession` | Lấy SP kèm khóa `PESSIMISTIC_WRITE` | Khóa ghi Session thực tế trên CSDL MySQL | Đọc thành công entity đang bị lock ghi |
| `TC_INT_PRD_08` | Vòng đời test | `cleanupScope_doesNotMatchExistingNonTestProducts` | Dọn dẹp dữ liệu test theo tiền tố `PREFIX` | Đếm số lượng bản ghi không mang tiền tố test | Dọn sạch dữ liệu test, không ảnh hưởng dữ liệu mẫu có sẵn |

---

## 📊 PHẦN 3: ĐÁNH GIÁ TÍNH TOÀN VẸN CSDL (DATABASE INTEGRITY)

* **Môi trường thực thi:** MySQL 8.0 Test Database kết hợp Spring Boot Test.
* **Cơ chế kiểm soát:** Sử dụng tiền tố ngẫu nhiên `PREFIX = "ITP_" + UUID` và cơ chế dọn dẹp độc lập `@BeforeEach` / `@AfterEach`.
* **Kết quả:** Đảm bảo toàn bộ các thao tác thêm, sửa, xóa mềm, lưu BLOB và khóa ghi bi quan của `ProductDAO` hoạt động hoàn hảo trên hệ quản trị CSDL thực tế.
