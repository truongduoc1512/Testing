# BẢNG MA TRẬN KIỂM THỬ TÍCH HỢP ORDER CANCEL & RETURN (SPRING BOOT INTEGRATION)
**Đường dẫn file mã nguồn:** [OrderReturnDAO.java](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/src/main/java/com/example/demo/dao/OrderReturnDAO.java), [OrderDAO.java](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/src/main/java/com/example/demo/dao/OrderDAO.java), [ProductDAO.java](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/src/main/java/com/example/demo/dao/ProductDAO.java)  
**Đường dẫn file kiểm thử:** [OrderCancelReturnTests.java](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/src/test/java/com/example/demo/OrderCancelReturnTests.java)

---

## 1. TỔNG QUAN ĐẶC ĐIỂM KIỂM THỬ TÍCH HỢP
* **Phạm vi kiểm thử:** Tương tác liên tầng CSDL giữa Đơn hàng (`Order`), Tồn kho sản phẩm (`Product`), và Yêu cầu hoàn trả (`OrderReturn`) trong ngữ cảnh Spring Boot `@SpringBootTest` và `@Transactional`.
* **Mục tiêu:** Đảm bảo tính nhất quán dữ liệu (Data Consistency): khi hủy đơn hoặc duyệt hoàn trả, số lượng hàng tồn kho phải được hoàn lại (restore stock) chính xác và trạng thái đơn hàng cập nhật đồng bộ.

---

## 2. MA TRẬN TEST CASES KIỂM THỬ TÍCH HỢP (3 TEST CASES)

| Mã TC | Hàm ở src/main | Hàm kiểm thử ở src/test | Giá trị Test đầu vào | Kịch bản tích hợp / Logic mục tiêu | Kết quả mong đợi |
| :---: | :--- | :--- | :--- | :--- | :--- |
| **TC_ITG_OCR_01** | `cancelOrder(String, String)` | `cancelOrder_restoresInventoryAndMarksOrderCancelled()` | Tạo đơn hàng 2 sản phẩm mã `S001` cho khách `customer88`, sau đó gọi hủy đơn | Tích hợp luồng hủy đơn: chuyển trạng thái đơn hàng thành `CANCELLED` và tự động cộng hoàn trả lại 2 sản phẩm vào tồn kho `Product` | `cancelled == true`, trạng thái đơn là `CANCELLED`, tồn kho sản phẩm phục hồi về đúng số lượng ban đầu |
| **TC_ITG_OCR_02** | `createReturnRequest(String, String, OrderReturnForm)` | `createReturnRequest_rejectsDuplicateForSameOrder()` | Đơn hàng trạng thái `COMPLETED`, gửi yêu cầu hoàn trả lần 1, sau đó gửi tiếp yêu cầu hoàn trả lần 2 cho cùng mã đơn | Ràng buộc nghiệp vụ tích hợp: một đơn hàng chỉ được phép có duy nhất 1 yêu cầu đổi trả đang xử lý | Yêu cầu lần 1 tạo thành công trạng thái `PENDING`; yêu cầu lần 2 ném ngoại lệ `IllegalStateException` |
| **TC_ITG_OCR_03** | `updateReturnStatus(String, String, String, String)` | `approveReturn_restoresInventoryAndMarksOrderReturned()` | Khách tạo yêu cầu trả hàng, người bán `manager1` duyệt yêu cầu với hành động `"APPROVE"` | Tích hợp quy trình duyệt đổi trả: cập nhật trạng thái yêu cầu sang `APPROVED`, cập nhật đơn hàng sang `RETURNED`, và tự động hoàn lại tồn kho sản phẩm trong CSDL | Trạng thái yêu cầu là `APPROVED`, trạng thái đơn là `RETURNED`, tồn kho sản phẩm tăng thêm 1 |
