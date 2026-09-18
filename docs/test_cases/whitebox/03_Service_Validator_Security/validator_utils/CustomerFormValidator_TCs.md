# ĐẶC TẢ KIỂM THỬ HỘP TRẮNG: `CustomerFormValidator`
## Tầng: Validator Layer | Phân hệ: Kiểm tra & Chuẩn hóa Thông tin Khách hàng đặt hàng
* **Tổng số Test Case:** **13 Test Cases**
* **Mục đích tài liệu:** Bảng ánh xạ chi tiết giữa hàm nghiệp vụ trong `CustomerFormValidator.java` và các ca kiểm thử trong `CustomerFormValidatorTest.java`, phục vụ tra cứu nhanh giá trị test đầu vào, nhánh logic mục tiêu và kết quả mong đợi.

---

## 📌 PHẦN 1: BẢNG TỔNG HỢP CÁC HÀM TRONG `CustomerFormValidator.java`

Lớp `CustomerFormValidator` triển khai interface `Validator` để kiểm tra tính hợp lệ của form thông tin khách hàng (`CustomerForm`):

| STT | Tên hàm trong `CustomerFormValidator.java` | Mục đích nghiệp vụ | Số Test Case đối ứng |
| :---: | :--- | :--- | :---: |
| 1 | `supports(Class<?> clazz)` | Xác định đối tượng form được hỗ trợ (`CustomerForm.class`) | **2** |
| 2 | `validate(Object target, Errors errors)` | Chuẩn hóa dữ liệu (trim, lowercase email) và kiểm tra lỗi rỗng, độ dài biên, định dạng email | **11** |

---

## 📑 PHẦN 2: BẢNG MA TRẬN ÁNH XẠ CHI TIẾT 13 TEST CASES

| Mã TC | Hàm ở `src/main` | Hàm kiểm thử ở `src/test` | Giá trị Test đầu vào (Inputs / Mock Data) | Nhánh logic / Điều kiện mục tiêu | Kết quả mong đợi (Assertion) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `TC_CFV_01` | `supports` | `supports_customerForm_returnsTrue` | `clazz = CustomerForm.class` | Nhánh `clazz == CustomerForm.class` | Trả về `true` |
| `TC_CFV_02` | `supports` | `supports_otherClass_returnsFalse` | `clazz = ProductForm.class` | Nhánh `clazz != CustomerForm.class` | Trả về `false` |
| `TC_CFV_03` | `validate` | `validate_validCustomer_normalizesInputAndHasNoErrors` | `name = "  Alice  "`, `address = "  123 Main Street  "`, `email = "  ALICE@EXAMPLE.COM  "`, `phone = "  0901234567  "` | Nhánh chuẩn hóa `normalize`: trim chuỗi và lowercase email | `errors.hasErrors() = false`, các trường được trim và email = `"alice@example.com"` |
| `TC_CFV_04` | `validate` | `validate_blankRequiredField_rejectsOnlyRequiredCode` | Tham số hóa: từng trường (`name`, `address`, `email`, `phone`) mang giá trị `null` hoặc khoảng trắng `"   "` | Nhánh `ValidationUtils.rejectIfEmptyOrWhitespace` | Trả về mã lỗi `"NotEmpty.customerForm.<field>"` tương ứng |
| `TC_CFV_05` | `validate` | `validate_invalidEmail_rejectsPatternCode` | `email = "invalid-email"` | Nhánh `!emailValidator.isValid(email)` | Báo lỗi `"Pattern.customerForm.email"` |
| `TC_CFV_06` | `validate` | `validate_nameAtStandardBoundary_hasNoNameError` | Phân tích giá trị biên BVA (4n+1): độ dài `name` gồm {1, 2, 50, 254, 255} ký tự | Nhánh `name.length() <= 255` | Hợp lệ, không có lỗi ở trường `name` |
| `TC_CFV_07` | `validate` | `validate_nameOutsideBoundary_rejectsExpectedCode` | Robustness BVA: `length = 0` và `length = 256` | Nhánh biên ngoài: độ dài 0 (rỗng) hoặc > 255 (vượt ngưỡng) | Báo mã lỗi tương ứng: `"NotEmpty.customerForm.name"` hoặc `"Length.customerForm.name"` |
| `TC_CFV_08` | `validate` | `validate_addressAtMaximumLength_hasNoAddressError` | `address` có độ dài đúng 255 ký tự biên | Nhánh `address.length() <= 255` | Hợp lệ, không có lỗi ở trường `address` |
| `TC_CFV_09` | `validate` | `validate_addressOverMaximumLength_rejectsLengthCode` | `address` có độ dài 256 ký tự | Nhánh `address.length() > 255` | Báo mã lỗi `"Length.customerForm.address"` |
| `TC_CFV_10` | `validate` | `validate_emailAtMaximumLength_hasNoEmailError` | `email` hợp lệ có độ dài đúng 128 ký tự biên | Nhánh `email.length() <= 128` | Hợp lệ, không có lỗi trường `email` |
| `TC_CFV_11` | `validate` | `validate_emailOverMaximumLength_rejectsOnlyLengthCode`| `email` có độ dài 129 ký tự | Nhánh `email.length() > 128` | Báo duy nhất mã lỗi `"Length.customerForm.email"` |
| `TC_CFV_12` | `validate` | `validate_phoneAtMaximumLength_hasNoPhoneError` | `phone` có độ dài đúng 128 ký tự biên | Nhánh `phone.length() <= 128` | Hợp lệ, không có lỗi trường `phone` |
| `TC_CFV_13` | `validate` | `validate_phoneOverMaximumLength_rejectsLengthCode` | `phone` có độ dài 129 ký tự | Nhánh `phone.length() > 128` | Báo mã lỗi `"Length.customerForm.phone"` |

---

## 📊 PHẦN 3: KẾT QUẢ ĐO LƯỜNG ĐỘ BAO PHỦ (JACOCO METRICS)

* **Lớp kiểm thử:** `com.example.demo.validator.CustomerFormValidator`
* **Statement Coverage (Instructions):** **100.0%** (159/159 instructions)
* **Branch Coverage (Branches):** **100.0%** (30/30 branches)
* **Line Coverage:** **100.0%** (29/29 lines)
* **Đánh giá:** Đạt độ bao phủ tuyệt đối 100%, áp dụng hoàn chỉnh kỹ thuật phân tích giá trị biên BVA (Boundary Value Analysis) cho các trường text, email và số điện thoại.
