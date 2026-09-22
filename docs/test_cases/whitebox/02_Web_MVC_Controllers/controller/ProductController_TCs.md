# ĐẶC TẢ KIỂM THỬ HỘP TRẮNG: `ProductController`
## Tầng: Web MVC Controller | Phân hệ: Quản lý Sản phẩm & Cổng kiểm duyệt ảnh AI
* **Tổng số Test Case:** **35 Test Cases**
* **Mục đích tài liệu:** Bảng ánh xạ chi tiết giữa các hàm nghiệp vụ trong `ProductController.java` và các hàm kiểm thử trong `ProductControllerCoverageTest.java`, phục vụ tra cứu nhanh giá trị test đầu vào, nhánh logic mục tiêu và kết quả mong đợi.

---

## 📌 PHẦN 1: BẢNG TỔNG HỢP CÁC HÀM TRONG `ProductController.java`

Lớp `ProductController` quản lý 6 hàm nghiệp vụ chính:

| STT | Tên hàm trong `ProductController.java` | Mục đích nghiệp vụ | Số Test Case đối ứng |
| :---: | :--- | :--- | :---: |
| 1 | `initBinder(...)` | Đăng ký Validator riêng cho ProductForm | **1** |
| 2 | `listProductHandler(...)` | Hiển thị danh sách sản phẩm, lọc theo Role và phân trang | **5** |
| 3 | `productDetail(...)` | Xem chi tiết sản phẩm và danh sách đánh giá nhận xét | **2** |
| 4 | `productImage(...)` | Xuất stream ảnh sản phẩm (nhận diện Content-Type PNG/JPEG/Octet-Stream) | **5** |
| 5 | `product(...)` (Edit/Create Form) | Hiển thị form tạo mới hoặc nạp dữ liệu sửa sản phẩm của Owner | **4** |
| 6 | `productSave(...)` & `deleteProduct(...)` | Lưu sản phẩm (qua cổng AI Gate) và xóa sản phẩm | **18** |

---

## 📑 PHẦN 2: BẢNG MA TRẬN ÁNH XẠ CHI TIẾT 35 TEST CASES

| Mã TC | Hàm ở `src/main` | Hàm kiểm thử ở `src/test` | Giá trị Test đầu vào (Inputs / Mock Data) | Nhánh logic / Điều kiện mục tiêu | Kết quả mong đợi (Assertion) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `TC_PRD_CTL_01` | `initBinder` | `initBinder_setsValidatorOnlyForProductFormTargets` | Target = `ProductForm.class` | Nhánh `validator.supports(target)` | Gán validator thành công |
| `TC_PRD_CTL_02` | `listProductHandler` | `listProduct_propagatesFiltersAndNormalizesGuestPage` | Khách vãng lai, `page = 0` | Chuẩn hóa page về 1, không scope owner | Trả về view `productList` |
| `TC_PRD_CTL_03` | `listProductHandler` | `listProduct_doesNotScopeUnauthenticatedTokenToOwner`| User chưa authenticate | Bỏ qua scope owner | Xem danh sách SP active |
| `TC_PRD_CTL_04` | `listProductHandler` | `listProduct_doesNotScopeAnonymousAuthenticationToOwner`| `anonymousUser` | Bỏ qua scope owner | Xem danh sách SP active |
| `TC_PRD_CTL_05` | `listProductHandler` | `listProduct_doesNotScopeRegularUserToOwner` | `ROLE_USER` thường | Bỏ qua scope owner | Xem SP bình thường |
| `TC_PRD_CTL_06` | `listProductHandler` | `listProduct_scopesAdminToAuthenticatedUsername` | `ROLE_ADMIN` | Lọc `owner = currentAdmin` | Xem được kho SP riêng của mình |
| `TC_PRD_CTL_07` | `productDetail` | `productDetail_redirectsMissingProduct` | Mã SP không tồn tại | Nhánh `if (productInfo == null)` | Redirect về `/productList` |
| `TC_PRD_CTL_08` | `productDetail` | `productDetail_populatesExistingProductAndReviews` | Mã SP hợp lệ | Nạp SP và danh sách review vào Model | Render view `productDetail` |
| `TC_PRD_CTL_09` | `productImage` | `productImage_writesNoBytesForNullCode` | `code = null` | Nhánh kiểm tra mã null | Không ghi byte nào vào response |
| `TC_PRD_CTL_10` | `productImage` | `productImage_writesNoBytesForMissingProduct` | SP không tìm thấy | Nhánh không tìm thấy entity | Không ghi byte nào |
| `TC_PRD_CTL_11` | `productImage` | `productImage_writesNoBytesForProductWithoutImage` | SP có `image = null` | Nhánh SP không có ảnh | Không ghi byte nào |
| `TC_PRD_CTL_12` | `productImage` | `productImage_writesPngBytesWithDetectedContentType` | File ảnh PNG hợp lệ | Nhận diện header ảnh PNG | Ghi mảng byte, Content-Type: image/png |
| `TC_PRD_CTL_13` | `productImage` | `productImage_usesOctetStreamForUnknownImageType` | Định dạng byte ảnh lạ | Nhánh fallback Content-Type | Ghi mảng byte, Content-Type: octet-stream |
| `TC_PRD_CTL_14` | `product` (GET) | `productEdit_buildsNewFormForMissingCode` | `code = null` (Tạo mới) | Nhánh tạo mới form rỗng | Render view `product` với form mới |
| `TC_PRD_CTL_15` | `product` (GET) | `productEdit_buildsNewFormWhenProductLookupIsMissing`| Mã không có trong DB | Nhánh mã không tồn tại | Khởi tạo form mới |
| `TC_PRD_CTL_16` | `product` (GET) | `productEdit_rejectsProductOwnedByAnotherUser` | SP thuộc về người khác | Nhánh kiểm tra quyền owner = False | Ném lỗi hoặc redirect cấm truy cập |
| `TC_PRD_CTL_17` | `product` (GET) | `productEdit_populatesFormForOwner` | Sửa SP của chính mình | Nạp thông tin SP vào form | Render view `product` với dữ liệu SP |
| `TC_PRD_CTL_18` | `productSave` | `productSave_returnsFormForBindingErrors` | Form sai validate | Nhánh `if (result.hasErrors())` | Trả về view `product` kèm lỗi |
| `TC_PRD_CTL_19` | `productSave` | `productSave_rejectsForeignOwner` | Cố tình gán owner khác | Nhánh kiểm tra chủ sở hữu | Chặn lưu SP |
| `TC_PRD_CTL_20` | `productSave` | `productSave_savesFormWithoutImage` | Không upload ảnh mới | Lưu form giữ nguyên ảnh cũ | Redirect về `/productList` |
| `TC_PRD_CTL_21` | `productSave` | `productSave_treatsEmptyUploadAsNoImage` | Upload file ảnh 0 byte | Nhánh `fileData.isEmpty()` | Bỏ qua bước kiểm duyệt ảnh |
| `TC_PRD_CTL_22` | `productSave` | `productSave_returnsFormWhenDaoRejectsInvalidData` | DAO ném lỗi dữ liệu | Bắt `IllegalArgumentException` | Hiển thị thông báo lỗi trên form |
| `TC_PRD_CTL_23` | `productSave` | `productSave_redirectsForbiddenWhenDaoDeniesAccess` | DAO ném lỗi phân quyền | Bắt `AccessDeniedException` | Redirect sang trang 403 |
| `TC_PRD_CTL_24` | `productSave` | `productSave_returnsFormWhenDaoFailsUnexpectedly` | DAO ném ngoại lệ khác | Bắt `Exception` chung | Trả về form kèm lỗi hệ thống |
| `TC_PRD_CTL_25` | `productSave` | `productSave_rejectsImageWhenAiDoesNotApprove` | Cổng AI từ chối ảnh giày dép | Nhánh `aiResult.isApproved() = false` | Chặn lưu, báo lỗi ảnh không hợp lệ |
| `TC_PRD_CTL_26` | `productSave` | `productSave_acceptsApprovedImageAndUsesFallbackFilename`| Cổng AI duyệt ảnh hợp lệ | Nhánh AI phê duyệt thành công | Lưu SP thành công |
| `TC_PRD_CTL_27` | `productSave` | `productSave_allowsMissingAiResponseBody` | Response AI rỗng | Nhánh xử lý response AI null | Vẫn cho phép lưu SP |
| `TC_PRD_CTL_28` | `productSave` | `productSave_allowsUndecidedAiResponse` | AI không chắc chắn | Nhánh fallback quyết định AI | Cho phép lưu với cảnh báo |
| `TC_PRD_CTL_29` | `productSave` | `productSave_warnsAndContinuesWhenAiRequestFails` | Server AI bị sập/lỗi kết nối | Bắt ngoại lệ gọi AI service | Ghi log cảnh báo và vẫn cho phép lưu |
| `TC_PRD_CTL_30` | `deleteProduct` | `deleteProduct_ignoresNullCode` | `code = null` khi xóa | Nhánh kiểm tra code null | Bỏ qua, redirect về `/productList` |
| `TC_PRD_CTL_31` | `deleteProduct` | `deleteProduct_ignoresEmptyCode` | `code = ""` | Nhánh kiểm tra code rỗng | Bỏ qua, redirect về `/productList` |
| `TC_PRD_CTL_32` | `deleteProduct` | `deleteProduct_rejectsForeignOwner` | Xóa SP của người khác | Nhánh kiểm tra owner | Chặn xóa SP |
| `TC_PRD_CTL_33` | `deleteProduct` | `deleteProduct_deletesProductOwnedByCurrentAdmin` | Xóa SP của chính Admin | Gọi `productDAO.deleteProduct(code)` | Xóa mềm thành công, redirect |
| `TC_PRD_CTL_34` | `deleteProduct` | `deleteProduct_deletesCodeWhenProductLookupIsMissing`| SP không có trong CSDL | Nhánh tiếp tục xóa an toàn | Redirect về `/productList` |
| `TC_PRD_CTL_35` | `deleteProduct` | `deleteProduct_reportsDaoFailure` | Lỗi DB khi xóa | Bắt ngoại lệ xóa | Gửi flash message thông báo lỗi |

---

## 📊 PHẦN 3: KẾT QUẢ ĐO LƯỜNG ĐỘ BAO PHỦ (JACOCO METRICS)

* **Lớp kiểm thử:** `com.example.demo.controller.ProductController`
* **Statement Coverage (Instructions):** **100.0%**
* **Branch Coverage (Branches):** **100.0%**
* **Đánh giá:** Đạt độ phủ tuyệt đối 100%. Bao phủ toàn bộ luồng kiểm duyệt ảnh AI và phân quyền quản trị sản phẩm.
