# ĐẶC TẢ KIỂM THỬ HỘP TRẮNG: `OrderController`
## Tầng: Web MVC Controller | Phân hệ: Quản lý & Điều phối Đơn hàng
* **Tổng số Test Case:** **17 Test Cases**
* **Mục đích tài liệu:** Bảng ánh xạ chi tiết giữa các hàm nghiệp vụ trong `OrderController.java` và các hàm kiểm thử trong `WebControllerCoverageTest.java`, phục vụ tra cứu nhanh giá trị test đầu vào, nhánh logic mục tiêu và kết quả mong đợi.

---

## 📌 PHẦN 1: BẢNG TỔNG HỢP CÁC HÀM TRONG `OrderController.java`

Lớp `OrderController` quản lý 3 hàm nghiệp vụ giao diện web:

| STT | Tên hàm trong `OrderController.java` | Mục đích nghiệp vụ | Số Test Case đối ứng |
| :---: | :--- | :--- | :---: |
| 1 | `orderListHandler(...)` | Hiển thị danh sách đơn hàng theo quyền hạn (Admin xem toàn bộ, User xem đơn mình) | **2** |
| 2 | `orderViewHandler(...)` | Xem chi tiết đơn hàng (kiểm tra quyền truy cập và tính toán lại tiền theo role) | **6** |
| 3 | `updateOrderStatusHandler(...)` | Admin điều phối chuyển trạng thái đơn hàng theo FSM | **9** |

---

## 📑 PHẦN 2: BẢNG MA TRẬN ÁNH XẠ CHI TIẾT 17 TEST CASES

| Mã TC | Hàm ở `src/main` | Hàm kiểm thử ở `src/test` | Giá trị Test đầu vào (Inputs / Mock Data) | Nhánh logic / Điều kiện mục tiêu | Kết quả mong đợi (Assertion) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `TC_ORD_CTL_01` | `orderListHandler` | `orderList_usesBlankScopeForManagerRole` | Role `ROLE_MANAGER` hoặc `ROLE_ADMIN` | Không lọc scope username | Xem toàn bộ danh sách đơn hàng |
| `TC_ORD_CTL_02` | `orderListHandler` | `orderList_fallsBackToFirstPageForInvalidPage` | `page = -1` hoặc page không hợp lệ | Nhánh chuẩn hóa `page <= 0` về 1 | Render trang 1 danh sách đơn |
| `TC_ORD_CTL_03` | `orderViewHandler` | `orderView_redirectsWhenOrderIdIsNull` | `orderId = null` | Nhánh `if (orderId == null)` = True | Redirect về `/orderList` |
| `TC_ORD_CTL_04` | `orderViewHandler` | `orderView_redirectsWhenOrderDoesNotExist` | Đơn hàng không có trong CSDL | Nhánh `if (orderInfo == null)` = True | Redirect về `/orderList` |
| `TC_ORD_CTL_05` | `orderViewHandler` | `orderView_redirectsWhenPrincipalCannotAccessOrder` | User không có quyền xem đơn này | Nhánh `!orderDAO.canAccessOrder(...)` | Chặn xem, redirect về `/orderList` |
| `TC_ORD_CTL_06` | `orderViewHandler` | `orderView_loadsDetailsForUserOrder` | Đúng chủ sở hữu đơn hàng | Nạp danh sách item vào Model | Render view `orderView` |
| `TC_ORD_CTL_07` | `orderViewHandler` | `orderView_recalculatesAmountWhenAdminIsNotCustomer`| Admin xem đơn của khách | Tính lại tổng tiền theo quyền quản trị | Hiển thị đầy đủ tổng tiền đơn |
| `TC_ORD_CTL_08` | `orderViewHandler` | `orderView_preservesAmountWhenAdminIsCustomer` | Admin xem đơn do chính mình mua | Bảo toàn tổng tiền nguyên bản của khách | Hiển thị đúng tiền đơn hàng |
| `TC_ORD_CTL_09` | `updateOrderStatusHandler`| `updateOrderStatus_rejectsMissingAuthentication` | `auth = null` khi bấm chuyển trạng thái | Chặn khách vãng lai | Redirect về trang đăng nhập |
| `TC_ORD_CTL_10` | `updateOrderStatusHandler`| `updateOrderStatus_rejectsNonAdminUser` | Customer thường cố chuyển trạng thái | Nhánh `!hasRole("ROLE_ADMIN")` | Bị từ chối quyền, redirect |
| `TC_ORD_CTL_11` | `updateOrderStatusHandler`| `updateOrderStatus_rejectsOrderOutsideAdminScope` | Đơn nằm ngoài phạm vi quản trị | Nhánh kiểm tra quyền quản lý đơn = False | Chặn cập nhật trạng thái |
| `TC_ORD_CTL_12` | `updateOrderStatusHandler`| `updateOrderStatus_skipsUpdateWhenOrderIdIsNull` | `orderId = null` lúc submit | Nhánh kiểm tra orderId null | Bỏ qua cập nhật, redirect |
| `TC_ORD_CTL_13` | `updateOrderStatusHandler`| `updateOrderStatus_skipsUpdateWhenStatusIsNull` | `status = null` | Nhánh kiểm tra status null | Bỏ qua cập nhật |
| `TC_ORD_CTL_14` | `updateOrderStatusHandler`| `updateOrderStatus_rejectsStatusOutsideAdminTransitions`| Trạng thái không nằm trong FSM cho phép | Nhánh validate trạng thái FSM | Báo lỗi chuyển trạng thái sai |
| `TC_ORD_CTL_15` | `updateOrderStatusHandler`| `updateOrderStatus_normalizesValidStatus` | `status = " confirmed "` hợp lệ | Chuẩn hóa chuỗi và lưu CSDL | Cập nhật trạng thái thành công |
| `TC_ORD_CTL_16` | `updateOrderStatusHandler`| `updateOrderStatus_surfacesInvalidTransitionMessage` | DAO ném lỗi logic chuyển trạng thái | Bắt `IllegalStateException` từ DAO | Hiển thị flash message thông báo lỗi |
| `TC_ORD_CTL_17` | `updateOrderStatusHandler`| `updateOrderStatus_reportsUnexpectedDaoFailure` | Lỗi CSDL bất ngờ khi cập nhật | Bắt ngoại lệ chung `Exception` | Thông báo lỗi hệ thống |

---

## 📊 PHẦN 3: KẾT QUẢ ĐO LƯỜNG ĐỘ BAO PHỦ (JACOCO METRICS)

* **Lớp kiểm thử:** `com.example.demo.controller.OrderController`
* **Statement Coverage (Instructions):** **100.0%**
* **Branch Coverage (Branches):** **100.0%**
* **Đánh giá:** Đạt độ phủ tuyệt đối 100%. Bao phủ toàn bộ các luồng xem chi tiết và máy trạng thái chuyển đổi đơn hàng.
