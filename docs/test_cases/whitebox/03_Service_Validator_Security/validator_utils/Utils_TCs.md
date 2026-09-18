# ĐẶC TẢ KIỂM THỬ HỘP TRẮNG: `Utils`
## Tầng: Utility Layer | Phân hệ: Quản lý Trạng thái Giỏ hàng trong HttpSession
* **Tổng số Test Case:** **4 Test Cases**
* **Mục đích tài liệu:** Bảng ánh xạ chi tiết giữa các hàm nghiệp vụ trong `Utils.java` và các ca kiểm thử trong `UtilsTest.java`, phục vụ tra cứu nhanh giá trị test đầu vào, nhánh logic mục tiêu và kết quả mong đợi.

---

## 📌 PHẦN 1: BẢNG TỔNG HỢP CÁC HÀM TRONG `Utils.java`

Lớp tiện ích `Utils` chịu trách nhiệm lưu trữ, trích xuất và giải phóng đối tượng giỏ hàng trong `HttpSession`:

| STT | Tên hàm trong `Utils.java` | Mục đích nghiệp vụ | Số Test Case đối ứng |
| :---: | :--- | :--- | :---: |
| 1 | `Utils()` | Khởi tạo lớp tiện ích | **1** |
| 2 | `getCartInSession(HttpServletRequest request)` | Lấy giỏ hàng từ session; nếu chưa có thì tự động tạo mới `CartInfo` | **1** |
| 3 | `removeCartInSession(HttpServletRequest request)` | Xóa bỏ thuộc tính giỏ hàng khỏi session sau khi đặt hàng thành công | **1** |
| 4 | `storeLastOrderedCartInSession(...)` & `getLastOrderedCartInSession(...)`| Lưu và truy xuất đơn hàng vừa hoàn tất phục vụ trang thông báo cảm ơn | **1** |

---

## 📑 PHẦN 2: BẢNG MA TRẬN ÁNH XẠ CHI TIẾT 4 TEST CASES

| Mã TC | Hàm ở `src/main` | Hàm kiểm thử ở `src/test` | Giá trị Test đầu vào (Inputs / Mock Data) | Nhánh logic / Điều kiện mục tiêu | Kết quả mong đợi (Assertion) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `TC_UTL_01` | `Utils()` | `constructor_isInstantiable` | Khởi tạo instance mới `new Utils()` | Nhánh default constructor | Đối tượng được khởi tạo thành công (`assertNotNull`) |
| `TC_UTL_02` | `getCartInSession` | `getCartInSession_createsAndReusesSessionCart` | `MockHttpServletRequest` mới (chưa có session attribute `"myCart"`) | Nhánh `if (cartInfo == null)`: khởi tạo mới và lưu vào session; lần gọi sau tái sử dụng | Lần đầu: tạo mới `CartInfo` và gán vào session; lần 2: trả về đúng instance đã lưu (`assertSame`) |
| `TC_UTL_03` | `removeCartInSession` | `removeCartInSession_removesExistingSessionCart`| Session đã có sẵn giỏ hàng `"myCart"` | Nhánh `request.getSession().removeAttribute("myCart")` | Thuộc tính `"myCart"` trong session trở về `null` |
| `TC_UTL_04` | `storeLastOrderedCartInSession`, `getLastOrderedCartInSession` | `lastOrderedCart_roundTripsThroughSession` | `CartInfo` với `orderNum = 7` | Nhánh lưu và đọc đối tượng `"lastOrderedCart"` | Đọc ra đúng instance vừa lưu, `orderNum == 7` |

---

## 📊 PHẦN 3: KẾT QUẢ ĐO LƯỜNG ĐỘ BAO PHỦ (JACOCO METRICS)

* **Lớp kiểm thử:** `com.example.demo.utils.Utils`
* **Statement Coverage (Instructions):** **100.0%** (39/39 instructions)
* **Branch Coverage (Branches):** **100.0%** (2/2 branches)
* **Line Coverage:** **100.0%** (11/11 lines)
* **Đánh giá:** Đạt độ bao phủ tuyệt đối 100%, bảo đảm dữ liệu giỏ hàng và đơn hàng vừa đặt được lưu chuyển an toàn qua các phiên làm việc HTTP.
