# ĐẶC TẢ KIỂM THỬ HỘP TRẮNG: `CsrfTokenControllerAdvice`
## Tầng: Security & Configuration Layer | Phân hệ: Khởi tạo Sớm CSRF Token (Session Eager Init)
* **Tổng số Test Case:** **3 Test Cases**
* **Mục đích tài liệu:** Bảng ánh xạ chi tiết giữa hàm nghiệp vụ trong `CsrfTokenControllerAdvice.java` và các ca kiểm thử trong `CsrfTokenControllerAdviceTest.java`, phục vụ tra cứu nhanh giá trị test đầu vào, nhánh logic mục tiêu và kết quả mong đợi.

---

## 📌 PHẦN 1: BẢNG TỔNG HỢP CÁC HÀM TRONG `CsrfTokenControllerAdvice.java`

Lớp `CsrfTokenControllerAdvice` kích hoạt việc tạo CSRF token trước khi Thymeleaf render HTML lớn, ngăn lỗi commit response sớm:

| STT | Tên hàm trong `CsrfTokenControllerAdvice.java` | Mục đích nghiệp vụ | Số Test Case đối ứng |
| :---: | :--- | :--- | :---: |
| 1 | `initializeCsrfToken(HttpServletRequest request)` | Kích hoạt sớm CSRF token từ HttpServletRequest attributes | **3** |

---

## 📑 PHẦN 2: BẢNG MA TRẬN ÁNH XẠ CHI TIẾT 3 TEST CASES

| Mã TC | Hàm ở `src/main` | Hàm kiểm thử ở `src/test` | Giá trị Test đầu vào (Inputs / Mock Data) | Nhánh logic / Điều kiện mục tiêu | Kết quả mong đợi (Assertion) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `TC_CSRF_01` | `initializeCsrfToken` | `initializeCsrfToken_ignoresMissingAttribute` | `request.getAttribute(CsrfToken.class.getName()) = null` | Nhánh không tồn tại attribute CSRF trong request | Không ném ngoại lệ, bỏ qua xử lý an toàn |
| `TC_CSRF_02` | `initializeCsrfToken` | `initializeCsrfToken_ignoresWronglyTypedAttribute` | Attribute có giá trị dạng String (`"not-a-token"`), không phải kiểu `CsrfToken` | Nhánh `!(attribute instanceof CsrfToken)` | Không ném ngoại lệ, không ép kiểu sai |
| `TC_CSRF_03` | `initializeCsrfToken` | `initializeCsrfToken_materializesTokenAttribute` | Attribute là đối tượng hợp lệ `CsrfToken` | Nhánh `attribute instanceof CsrfToken` | Gọi `token.getToken()` để hiện thực hóa token vào session |

---

## 📊 PHẦN 3: KẾT QUẢ ĐO LƯỜNG ĐỘ BAO PHỦ (JACOCO METRICS)

* **Lớp kiểm thử:** `com.example.demo.config.CsrfTokenControllerAdvice`
* **Statement Coverage (Instructions):** **100.0%** (16/16 instructions)
* **Branch Coverage (Branches):** **100.0%** (2/2 branches)
* **Line Coverage:** **100.0%** (5/5 lines)
* **Đánh giá:** Đạt độ bao phủ tuyệt đối 100%, đảm bảo ngăn ngừa hoàn toàn lỗi `IllegalStateException: Cannot create a session after the response has been committed`.
