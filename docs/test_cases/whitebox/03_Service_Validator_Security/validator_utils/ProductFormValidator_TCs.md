# ĐẶC TẢ KIỂM THỬ HỘP TRẮNG: `ProductFormValidator`
## Tầng: Validator Layer | Phân hệ: Kiểm tra & Xác thực Dữ liệu Sản phẩm (Product Form)
* **Tổng số Test Case:** **18 Test Cases**
* **Mục đích tài liệu:** Bảng ánh xạ chi tiết giữa hàm nghiệp vụ trong `ProductFormValidator.java` và các ca kiểm thử trong `ProductFormValidatorTest.java`, phục vụ tra cứu nhanh giá trị test đầu vào, nhánh logic mục tiêu và kết quả mong đợi.

---

## 📌 PHẦN 1: BẢNG TỔNG HỢP CÁC HÀM TRONG `ProductFormValidator.java`

Lớp `ProductFormValidator` thực hiện kiểm tra tính hợp lệ dữ liệu sản phẩm khi tạo mới hoặc cập nhật trong phần quản trị:

| STT | Tên hàm trong `ProductFormValidator.java` | Mục đích nghiệp vụ | Số Test Case đối ứng |
| :---: | :--- | :--- | :---: |
| 1 | `supports(Class<?> clazz)` | Xác định đối tượng form được hỗ trợ (`ProductForm.class`) | **2** |
| 2 | `validate(Object target, Errors errors)` | Điều phối kiểm tra: quy tắc cục bộ + kiểm tra trùng lặp mã sản phẩm từ CSDL | **6** |
| 3 | `validateLocalRules(ProductForm form, Errors errors)`| Kiểm tra độc lập quy tắc cục bộ: độ dài mã, tên, giá > 0, tồn kho >= 0, giảm giá 0-100% | **10** |

---

## 📑 PHẦN 2: BẢNG MA TRẬN ÁNH XẠ CHI TIẾT 18 TEST CASES

| Mã TC | Hàm ở `src/main` | Hàm kiểm thử ở `src/test` | Giá trị Test đầu vào (Inputs / Mock Data) | Nhánh logic / Điều kiện mục tiêu | Kết quả mong đợi (Assertion) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `TC_PFV_01` | `supports` | `supports_productForm_returnsTrue` | `clazz = ProductForm.class` | Nhánh `clazz == ProductForm.class` | Trả về `true` |
| `TC_PFV_02` | `supports` | `supports_otherClass_returnsFalse` | `clazz = CustomerForm.class` | Nhánh `clazz != ProductForm.class` | Trả về `false` |
| `TC_PFV_03` | `validate` | `validate_validNewProduct_normalizesInputAndLooksUpCodeOnce` | `code = "  P001  "`, `name = "  Running Shoe  "`, `newProduct = true`<br>Mock: `productDAO.findProduct("P001") = null` | Nhánh tạo mới hợp lệ: trim dữ liệu và kiểm tra trùng mã CSDL | `hasErrors() = false`, `code = "P001"`, gọi `findProduct("P001")` đúng 1 lần |
| `TC_PFV_04` | `validate` | `validate_validEditedProduct_doesNotLookUpDuplicateCode` | `newProduct = false` (sản phẩm đã tồn tại sửa lại) | Nhánh `!productForm.isNewProduct()`: bỏ qua kiểm tra trùng mã CSDL | `hasErrors() = false`, không gọi `productDAO.findProduct` |
| `TC_PFV_05` | `validateLocalRules` | `validateLocalRules_validProduct_normalizesWithoutDaoLookup`| Gọi trực tiếp `validateLocalRules` với dữ liệu hợp lệ | Nhánh kiểm tra cục bộ không gọi DB | `hasErrors() = false`, chuỗi được chuẩn hóa trim |
| `TC_PFV_06` | `validate` | `validate_blankRequiredField_rejectsThatFieldAndSkipsDao` | `code` hoặc `name` mang giá trị `null` hoặc `"   "` | Nhánh `rejectIfEmptyOrWhitespace`: có lỗi cục bộ ➔ ngắt không gọi DB | Báo lỗi `"NotEmpty.productForm.<field>"`, không gọi `productDAO` |
| `TC_PFV_07` | `validate` | `validate_duplicateNewProduct_rejectsCode` | `newProduct = true`, `code = "P001"`<br>Mock: `findProduct("P001") != null` | Nhánh phát hiện trùng mã sản phẩm trong CSDL | Báo lỗi `"Duplicate.productForm.code"` |
| `TC_PFV_08` | `validate` | `validate_codeAtMaximumLength_hasNoCodeError` | `code` dài đúng 20 ký tự (`'c' * 20`) | Nhánh `code.length() <= 20` | Hợp lệ, không có lỗi trường `code` |
| `TC_PFV_09` | `validate` | `validate_codeOverMaximumLength_rejectsLengthAndSkipsDao` | `code` dài 21 ký tự (`'c' * 21`) | Nhánh `code.length() > 20` | Báo mã lỗi `"Length.productForm.code"`, bỏ qua gọi DAO |
| `TC_PFV_10` | `validate` | `validate_nameAtMaximumLength_hasNoNameError` | `name` dài đúng 255 ký tự (`'n' * 255`) | Nhánh `name.length() <= 255` | Hợp lệ, không có lỗi trường `name` |
| `TC_PFV_11` | `validate` | `validate_nameOverMaximumLength_rejectsLengthAndSkipsDao` | `name` dài 256 ký tự (`'n' * 256`) | Nhánh `name.length() > 255` | Báo mã lỗi `"Length.productForm.name"`, bỏ qua gọi DAO |
| `TC_PFV_12` | `validate` | `validate_invalidPrice_rejectsValueAndSkipsDao` | `price` thuộc các giá trị lỗi: {0.0, -0.01, NaN, +Infinity, -Infinity} | Nhánh `!Double.isFinite(price) \|\| price <= 0` | Báo mã lỗi `"Min.productForm.price"` |
| `TC_PFV_13` | `validateLocalRules` | `validate_positiveFinitePrice_hasNoPriceError` | `price = 0.01` (giá trị dương nhỏ nhất) | Nhánh giá hợp lệ | Không có lỗi trường `price` |
| `TC_PFV_14` | `validate` | `validate_negativeStock_rejectsMinimumAndSkipsDao` | `stockQuantity = -1` | Nhánh `stockQuantity < 0` | Báo mã lỗi `"Min.productForm.stockQuantity"` |
| `TC_PFV_15` | `validateLocalRules` | `validate_zeroStock_hasNoStockError` | `stockQuantity = 0` (hết hàng nhưng không âm) | Nhánh biên `stockQuantity == 0` | Hợp lệ, không có lỗi tồn kho |
| `TC_PFV_16` | `validate` | `validate_invalidDiscount_rejectsRangeAndSkipsDao` | `discountPercent` ngoài khoảng: {-1, 101} | Nhánh `discountPercent < 0 \|\| discountPercent > 100` | Báo mã lỗi `"Range.productForm.discountPercent"` |
| `TC_PFV_17` | `validateLocalRules` | `validate_discountBoundary_hasNoDiscountError` | `discountPercent` tại hai giá trị biên: {0, 100} | Nhánh biên hợp lệ trong khoảng `[0, 100]` | Hợp lệ, không có lỗi giảm giá |
| `TC_PFV_18` | `validateLocalRules` | `validateLocalRules_existingFieldErrorShortCircuitsThatRule`| Trường `price`, `stockQuantity`, `discountPercent` đã có sẵn lỗi `typeMismatch` từ Spring binding | Nhánh `!errors.hasFieldErrors(<field>)` = False (ngắt mạch kiểm tra tiếp) | Giữ nguyên mã lỗi ban đầu `"typeMismatch"`, không ghi đè |

---

## 📊 PHẦN 3: KẾT QUẢ ĐO LƯỜNG ĐỘ BAO PHỦ (JACOCO METRICS)

* **Lớp kiểm thử:** `com.example.demo.validator.ProductFormValidator`
* **Statement Coverage (Instructions):** **100.0%** (149/149 instructions)
* **Branch Coverage (Branches):** **100.0%** (34/34 branches)
* **Line Coverage:** **100.0%** (32/32 lines)
* **Đánh giá:** Đạt độ bao phủ tuyệt đối 100%, bảo đảm kiểm thử chặt chẽ mọi điều kiện biên về mã sản phẩm, giá bán, tồn kho và chiết khấu.
