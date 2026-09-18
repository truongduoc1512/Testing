# ĐẶC TẢ KIỂM THỬ HỘP TRẮNG: `PageNumberParser`
## Tầng: Utility Layer | Phân hệ: Tiện ích Phân tích Chỉ số Phân trang (Page Number Parser)
* **Tổng số Test Case:** **2 Test Cases**
* **Mục đích tài liệu:** Bảng ánh xạ chi tiết giữa hàm nghiệp vụ trong `PageNumberParser.java` và các ca kiểm thử trong `PageNumberParserTest.java`, phục vụ tra cứu nhanh giá trị test đầu vào, nhánh logic mục tiêu và kết quả mong đợi.

---

## 📌 PHẦN 1: BẢNG TỔNG HỢP CÁC HÀM TRONG `PageNumberParser.java`

Lớp tiện ích `PageNumberParser` chuẩn hóa chỉ số trang từ chuỗi truy vấn URL:

| STT | Tên hàm trong `PageNumberParser.java` | Mục đích nghiệp vụ | Số Test Case đối ứng |
| :---: | :--- | :--- | :---: |
| 1 | `parsePositivePage(String pageValue)` | Chuyển đổi an toàn chuỗi sang số nguyên dương >= 1, fallback về trang 1 nếu lỗi | **2** |

---

## 📑 PHẦN 2: BẢNG MA TRẬN ÁNH XẠ CHI TIẾT 2 TEST CASES

| Mã TC | Hàm ở `src/main` | Hàm kiểm thử ở `src/test` | Giá trị Test đầu vào (Inputs / Mock Data) | Nhánh logic / Điều kiện mục tiêu | Kết quả mong đợi (Assertion) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `TC_PNP_01` | `parsePositivePage` | `parsePositivePage_usesFirstPageForInvalidOrNonPositiveInput` | 1. `pageValue = null`<br>2. `pageValue = ""` (rỗng)<br>3. `pageValue = "invalid"` (không phải số)<br>4. `pageValue = "0"` (số 0)<br>5. `pageValue = "-5"` (số âm) | Nhánh `pageValue == null`, ngoại lệ `NumberFormatException`, hoặc `Math.max(parsed, 1)` | Tất cả các trường hợp đều trả về trang đầu tiên: `1` |
| `TC_PNP_02` | `parsePositivePage` | `parsePositivePage_preservesValidPositiveInput` | `pageValue = "3"` | Nhánh parse thành công chuỗi số nguyên dương `> 1` | Trả về đúng số trang: `3` |

---

## 📊 PHẦN 3: KẾT QUẢ ĐO LƯỜNG ĐỘ BAO PHỦ (JACOCO METRICS)

* **Lớp kiểm thử:** `com.example.demo.utils.PageNumberParser`
* **Statement Coverage (Instructions):** **100.0%** (13/13 instructions)
* **Branch Coverage (Branches):** **100.0%** (2/2 branches)
* **Line Coverage:** **100.0%** (5/5 lines)
* **Đánh giá:** Đạt độ bao phủ tuyệt đối 100%, bảo đảm hệ thống không bị crash trước các tham số phân trang bất thường do người dùng cố tình nhập trên thanh địa chỉ trình duyệt.
