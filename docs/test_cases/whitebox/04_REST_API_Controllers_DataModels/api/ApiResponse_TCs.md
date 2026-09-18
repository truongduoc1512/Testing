# ĐẶC TẢ KIỂM THỬ HỘP TRẮNG: `ApiResponse`
## Tầng: REST API Controller | Phân hệ: Chuẩn hóa Cấu trúc Phản hồi JSON (Response Wrapper)
* **Tổng số Test Case:** **2 Test Cases**
* **Mục đích tài liệu:** Bảng ánh xạ chi tiết giữa phương thức trong `ApiResponse.java` và các ca kiểm thử trong `ApiResponseTest.java`, phục vụ tra cứu nhanh giá trị test đầu vào, nhánh logic mục tiêu và kết quả mong đợi.

---

## 📌 PHẦN 1: BẢNG TỔNG HỢP CÁC HÀM TRONG `ApiResponse.java`

Lớp tiện ích `ApiResponse` định hình cấu trúc phản hồi JSON tiêu chuẩn (`success`, `message`) cho toàn bộ các REST API Controller:

| STT | Tên hàm trong `ApiResponse.java` | Mục đích nghiệp vụ | Số Test Case đối ứng |
| :---: | :--- | :--- | :---: |
| 1 | `error(String message)` | Tạo JSON phản hồi lỗi (`success = false`) | **1** |
| 2 | `success(String message)` | Tạo JSON phản hồi thành công (`success = true`) | **1** |

---

## 📑 PHẦN 2: BẢNG MA TRẬN ÁNH XẠ CHI TIẾT 2 TEST CASES

| Mã TC | Hàm ở `src/main` | Hàm kiểm thử ở `src/test` | Giá trị Test đầu vào (Inputs / Mock Data) | Nhánh logic / Điều kiện mục tiêu | Kết quả mong đợi (Assertion) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `TC_APR_01` | `error` | `error_returnsStableErrorContract` | `message = "Dữ liệu không hợp lệ"` | Nhánh `message(false, message)` | Trả về Map gồm 2 phần tử: `success = false`, `message = "Dữ liệu không hợp lệ"` |
| `TC_APR_02` | `success` | `success_returnsStableSuccessContract` | `message = "Thành công"` | Nhánh `message(true, message)` | Trả về Map gồm 2 phần tử: `success = true`, `message = "Thành công"` |

---

## 📊 PHẦN 3: KẾT QUẢ ĐO LƯỜNG ĐỘ BAO PHỦ (JACOCO METRICS)

* **Lớp kiểm thử:** `com.example.demo.controller.api.ApiResponse`
* **Statement Coverage (Instructions):** **100.0%** (25/25 instructions)
* **Branch Coverage:** **100.0%** (không có nhánh rẽ phức tạp)
* **Line Coverage:** **100.0%** (6/6 lines)
* **Đánh giá:** Đạt độ bao phủ tuyệt đối 100%, bảo đảm tính nhất quán cấu trúc hợp đồng dữ liệu JSON trả về cho Client.
