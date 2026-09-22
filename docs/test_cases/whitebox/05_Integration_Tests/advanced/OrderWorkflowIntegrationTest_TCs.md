# BẢNG MA TRẬN KIỂM THỬ TÍCH HỢP LUỒNG ĐẶT HÀNG (ORDER WORKFLOW PIPELINE)
**Đường dẫn file mã nguồn:** [CartApiController.java](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/src/main/java/com/example/demo/controller/api/CartApiController.java), [OrderDAO.java](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/src/main/java/com/example/demo/dao/OrderDAO.java)  
**Đường dẫn file kiểm thử:** [OrderWorkflowIntegrationTest.java](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/src/test/java/com/example/demo/integration/OrderWorkflowIntegrationTest.java)

---

## 1. TỔNG QUAN ĐẶC ĐIỂM KIỂM THỬ TÍCH HỢP
* **Phạm vi kiểm thử:** Tích hợp toàn diện luồng checkout qua HTTP API `/api/v1/cart/checkout`, xử lý Session giỏ hàng (`MockHttpSession`), tạo bản ghi `Orders`, chi tiết đơn `Order_details`, cập nhật số lượng tồn kho `Products` và xử lý rollback giao dịch khi xảy ra lỗi.
* **Mục tiêu:** Đảm bảo tính nguyên tử (Atomicity) và tính nhất quán (Consistency) tuyệt đối trong nghiệp vụ bán hàng: hoặc toàn bộ đơn hàng và trừ kho thành công, hoặc không có bất kỳ trạng thái dở dang nào được lưu vào CSDL.

---

## 2. MA TRẬN TEST CASES KIỂM THỬ TÍCH HỢP (3 TEST CASES)

| Mã TC | Chức năng ở src/main | Hàm kiểm thử ở src/test | Giá trị Test đầu vào | Kịch bản tích hợp / Logic mục tiêu | Kết quả mong đợi |
| :---: | :--- | :--- | :--- | :--- | :--- |
| **TC_ITG_OWF_01** | `POST /api/v1/cart/checkout` | `shouldCreateOrderDetailsAndDecreaseStockWhenCheckoutRequestIsValid()` | Sản phẩm giá 100K giảm 10% (=90K), tồn kho 10; khách thêm 3 sản phẩm vào giỏ và checkout | Checkout thành công: tạo bản ghi `Orders`, sinh chi tiết `Order_details`, trừ tồn kho sản phẩm từ 10 xuống 7, tăng lượt bán (`salesCount`) từ 4 lên 7, dọn dẹp giỏ active | HTTP 201 Created, `orderedCart.amountTotal = 270.0`, CSDL lưu đầy đủ Order và OrderDetail, tồn kho trừ chính xác, giỏ session lưu sang `lastOrderedCart` |
| **TC_ITG_OWF_02** | `POST /api/v1/cart/checkout` | `shouldNotPersistPartialStateWhenStockChangesBeforeCheckout()` | Khách thêm 2 sản phẩm vào giỏ (khi tồn kho = 2), nhưng trước khi bấm checkout thì tồn kho bị giảm xuống 1 | Kiểm tra xung đột tồn kho trước checkout (Race Condition): phát hiện tồn kho không đủ (cần 2 nhưng chỉ còn 1), từ chối tạo đơn | HTTP 400 Bad Request, không có bản ghi Order hay Detail nào được tạo (`count == 0`), tồn kho giữ nguyên 1, giỏ `myCart` được bảo toàn |
| **TC_ITG_OWF_03** | `POST /api/v1/cart/checkout` | `shouldRollbackOrderDetailsAndStockWhenDatabaseRejectsOrder()` | Giỏ hàng có tên khách hàng vượt quá kích thước cột CSDL (256 ký tự 'X') gây lỗi khi insert Order | Kiểm tra tính toàn vẹn Transactional Rollback: khi quá trình lưu đơn hàng gặp lỗi CSDL, toàn bộ các chi tiết đơn và tồn kho đã trừ phải được hoàn nguyên nguyên trạng | HTTP 400 Bad Request, CSDL không lưu Order hay Detail dở dang, tồn kho phục hồi nguyên vẹn 5, lượt bán giữ nguyên 2 |
