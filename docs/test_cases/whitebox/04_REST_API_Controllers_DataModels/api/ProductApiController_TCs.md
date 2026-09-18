# ĐẶC TẢ KIỂM THỬ HỘP TRẮNG: `ProductApiController`
## Tầng: REST API Controller | Phân hệ: Quản lý Sản phẩm RESTful (Product API)
* **Tổng số Test Case:** **13 Test Cases**
* **Mục đích tài liệu:** Bảng ánh xạ chi tiết giữa hàm nghiệp vụ trong `ProductApiController.java` và các ca kiểm thử trong `ProductApiControllerTest.java`, phục vụ tra cứu nhanh giá trị test đầu vào, nhánh logic mục tiêu và kết quả mong đợi.

---

## 📌 PHẦN 1: BẢNG TỔNG HỢP CÁC HÀM TRONG `ProductApiController.java`

Lớp `ProductApiController` cung cấp các REST endpoints phục vụ danh sách, chi tiết, thêm, sửa, xóa mềm sản phẩm:

| STT | Tên hàm trong `ProductApiController.java` | Endpoint HTTP | Mục đích nghiệp vụ | Số Test Case |
| :---: | :--- | :--- | :--- | :---: |
| 1 | `getProducts(...)` | `GET /api/v1/products` | Truy vấn danh sách sản phẩm với bộ lọc đa tiêu chí và phân trang | **2** |
| 2 | `getProductByCode(String code)` | `GET /api/v1/products/{code}` | Lấy chi tiết thông tin sản phẩm theo mã code | **2** |
| 3 | `saveProduct(ProductForm form)` | `POST /api/v1/products` | Thêm mới hoặc cập nhật sản phẩm (kiểm tra quyền sở hữu của Seller) | **5** |
| 4 | `deleteProduct(String code)` | `DELETE /api/v1/products/{code}`| Xóa mềm/deactivate sản phẩm thuộc quyền sở hữu của Seller | **4** |

---

## 📑 PHẦN 2: BẢNG MA TRẬN ÁNH XẠ CHI TIẾT 13 TEST CASES

| Mã TC | Hàm ở `src/main` | Hàm kiểm thử ở `src/test` | Giá trị Test đầu vào (Inputs / Mock Data) | Nhánh logic / Điều kiện mục tiêu | Kết quả mong đợi (Assertion) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `TC_PAC_01` | `getProducts` | `getProducts_normalizesPageAndPassesEveryFilter` | Bộ lọc đầy đủ: `likeName = "shoe"`, `page = -1`, `price`, `location`, `brand`, `isMall`, `rating`, `category` | Chuẩn hóa `page` âm về 1 và truyền đủ 14 tham số lọc xuống DAO | HTTP `200 OK`, trả về `PaginationResult<ProductInfo>` |
| `TC_PAC_02` | `getProducts` | `getProducts_passesEmptyOptionalFiltersOnRequestedPage`| Bộ lọc tối giản: `likeName = ""`, `page = 2`, `sort = "newest"`, các tham số lọc khác mang giá trị `null` | Nhánh bộ lọc tùy chọn để trống | HTTP `200 OK`, trả về đúng trang thứ 2 |
| `TC_PAC_03` | `getProductByCode` | `getProductByCode_returnsNotFoundWhenProductDoesNotExist`| `code = "missing"`, Mock: `productDAO.findProductInfo("missing") = null` | Nhánh không tìm thấy sản phẩm | HTTP `404 Not Found` |
| `TC_PAC_04` | `getProductByCode` | `getProductByCode_returnsExistingProduct` | `code = "P1"`, Mock trả về đối tượng `ProductInfo` | Nhánh tìm thấy sản phẩm hợp lệ | HTTP `200 OK`, trả về đối tượng `ProductInfo` |
| `TC_PAC_05` | `saveProduct` | `saveProduct_rejectsInvalidForm` | Form thiếu `code`, thiếu `name`, hoặc chỉ chứa khoảng trắng | Nhánh validate form dữ liệu đầu vào | HTTP `400 Bad Request` |
| `TC_PAC_06` | `saveProduct` | `saveProduct_createsNormalizedNewProduct` | `code = " P1 "`, `name = " New product "`, Mock: `findProduct("P1") = null` | Nhánh tạo mới: trim dữ liệu, gọi `productDAO.save(form)` | HTTP `201 Created`, `code = "P1"`, `name = "New product"` |
| `TC_PAC_07` | `saveProduct` | `saveProduct_updatesProductOwnedByCurrentPrincipal` | Seller `seller` cập nhật sản phẩm mà mình sở hữu (`ownerUsername = "seller"`) | Nhánh cập nhật sản phẩm chính chủ | HTTP `200 OK`, cập nhật thành công |
| `TC_PAC_08` | `saveProduct` | `saveProduct_forbidsUpdatingForeignProduct` | Seller `other` cố cập nhật sản phẩm của seller khác (`ownerUsername = "seller"`) | Nhánh `!existing.getOwnerUsername().equals(currentUsername)` | HTTP `403 Forbidden` (chặn sửa trộm sản phẩm) |
| `TC_PAC_09` | `saveProduct` | `saveProduct_mapsDaoException` | DAO ném ngoại lệ: `IllegalArgumentException`, `AccessDeniedException`, hoặc `RuntimeException` | Xử lý ngoại lệ ánh xạ mã lỗi HTTP | Tương ứng `400 Bad Request`, `403 Forbidden`, hoặc `500 Server Error` |
| `TC_PAC_10` | `deleteProduct` | `deleteProduct_returnsNotFoundWhenProductDoesNotExist` | `code = "missing"`, Mock: `findProduct("missing") = null` | Nhánh xóa sản phẩm không tồn tại | HTTP `404 Not Found` |
| `TC_PAC_11` | `deleteProduct` | `deleteProduct_forbidsProductOwnedByAnotherPrincipal` | Seller `seller` cố xóa sản phẩm của seller `other` | Nhánh chặn xóa sản phẩm của người khác | HTTP `403 Forbidden` |
| `TC_PAC_12` | `deleteProduct` | `deleteProduct_deactivatesOwnedProduct` | Seller xóa đúng sản phẩm do mình quản lý | Nhánh xóa thành công sản phẩm chính chủ | HTTP `200 OK`, `success = true`, gọi `deleteProduct("owned")` |
| `TC_PAC_13` | `deleteProduct` | `deleteProduct_returnsServerErrorWhenDaoFails` | CSDL ném lỗi khi thực hiện xóa sản phẩm | Bắt ngoại lệ lưu CSDL | HTTP `500 Internal Server Error` |

---

## 📊 PHẦN 3: KẾT QUẢ ĐO LƯỜNG ĐỘ BAO PHỦ (JACOCO METRICS)

* **Lớp kiểm thử:** `com.example.demo.controller.api.ProductApiController`
* **Statement Coverage (Instructions):** **100.0%** (219/219 instructions)
* **Branch Coverage (Branches):** **100.0%** (24/24 branches)
* **Line Coverage:** **100.0%** (51/51 lines)
* **Đánh giá:** Đạt độ bao phủ tuyệt đối 100%, bảo vệ an toàn phân quyền sở hữu sản phẩm giữa các Seller và hỗ trợ bộ lọc truy vấn linh hoạt.
