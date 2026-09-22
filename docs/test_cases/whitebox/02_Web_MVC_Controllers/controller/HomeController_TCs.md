# ĐẶC TẢ KIỂM THỬ HỘP TRẮNG: `HomeController`
## Tầng: Web MVC Controller | Phân hệ: Trang chủ & Điều hướng chung
* **Tổng số Test Case:** **8 Test Cases**
* **Mục đích tài liệu:** Bảng ánh xạ chi tiết giữa các hàm nghiệp vụ trong `HomeController.java` và các hàm kiểm thử trong `HomeControllerTest.java`, phục vụ tra cứu nhanh giá trị test đầu vào, nhánh logic mục tiêu và kết quả mong đợi.

---

## 📌 PHẦN 1: BẢNG TỔNG HỢP CÁC HÀM TRONG `HomeController.java`

Lớp `HomeController` quản lý 3 hàm nghiệp vụ chính:

| STT | Tên hàm trong `HomeController.java` | Mục đích nghiệp vụ | Số Test Case đối ứng |
| :---: | :--- | :--- | :---: |
| 1 | `index(Model model, String keyword)` | Trang chủ: nạp từ khóa tìm kiếm và render template `index` | **2** |
| 2 | `accessDenied()` | Điều hướng hiển thị trang lỗi phân quyền 403 (`403Page`) | **1** |
| 3 | `viewFile(String fileName, ...)` | Đọc và render file tĩnh (bảo mật chống tấn công Path Traversal `../`) | **5** |

---

## 📑 PHẦN 2: BẢNG MA TRẬN ÁNH XẠ CHI TIẾT 8 TEST CASES

| Mã TC | Hàm ở `src/main` | Hàm kiểm thử ở `src/test` | Giá trị Test đầu vào (Inputs / Mock Data) | Nhánh logic / Điều kiện mục tiêu | Kết quả mong đợi (Assertion) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `TC_HOME_01` | `index` | `home_addsNonEmptyKeywordAndReturnsIndex` | `keyword = "Nike Air"` | Nhánh `if (keyword != null && !keyword.isEmpty())` | Đưa keyword vào Model, trả về view `index` |
| `TC_HOME_02` | `index` | `home_omitsMissingKeywordAndReturnsIndex` | `keyword = null` | Nhánh không có từ khóa tìm kiếm | Không đưa keyword vào Model, trả về view `index` |
| `TC_HOME_03` | `accessDenied` | `accessDenied_returnsDedicatedView` | Người dùng không đủ quyền truy cập | Xử lý request 403 Forbidden | Trả về tên view `403Page` |
| `TC_HOME_04` | `viewFile` | `viewFile_rejectsMissingFilename` | `fileName = null` hoặc `""` | Nhánh `if (fileName == null \|\| fileName.isEmpty())` | Trả về HTTP 400 Bad Request |
| `TC_HOME_05` | `viewFile` | `viewFile_rejectsPathTraversal` | `fileName = "../../etc/passwd"` | Nhánh phát hiện chuỗi chứa `..` (Path Traversal) | Chặn truy cập, trả về HTTP 400 |
| `TC_HOME_06` | `viewFile` | `viewFile_readsExistingStaticFile` | File tĩnh hợp lệ trong thư mục tĩnh | Đọc stream file tĩnh thành công | Ghi nội dung file ra response stream (HTTP 200) |
| `TC_HOME_07` | `viewFile` | `viewFile_doesNotReadFilesOutsideStaticDirectory` | File nằm ngoài thư mục static được phép | Nhánh kiểm tra đường dẫn chuẩn hóa ngoài thư mục gốc | Chặn đọc file nhạy cảm |
| `TC_HOME_08` | `viewFile` | `viewFile_reportsInvalidPath` | Tên file chứa ký tự đặc biệt không hợp lệ | Nhánh bắt ngoại lệ `InvalidPathException` | Báo lỗi đường dẫn không hợp lệ |

---

## 📊 PHẦN 3: KẾT QUẢ ĐO LƯỜNG ĐỘ BAO PHỦ (JACOCO METRICS)

* **Lớp kiểm thử:** `com.example.demo.controller.HomeController`
* **Statement Coverage (Instructions):** **100.0%**
* **Branch Coverage (Branches):** **100.0%**
* **Đánh giá:** Đạt độ phủ tuyệt đối 100% toàn bộ câu lệnh, xử lý an toàn lỗ hổng bảo mật Path Traversal.
