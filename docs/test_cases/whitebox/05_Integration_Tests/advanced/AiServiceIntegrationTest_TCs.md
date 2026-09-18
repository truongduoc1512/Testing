# BẢNG MA TRẬN KIỂM THỬ TÍCH HỢP AI SERVICE (IMAGE ANALYSIS PIPELINE)
**Đường dẫn file mã nguồn:** [ProductController.java](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/src/main/java/com/example/demo/controller/ProductController.java)  
**Đường dẫn file kiểm thử:** [AiServiceIntegrationTest.java](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/src/test/java/com/example/demo/integration/AiServiceIntegrationTest.java)

---

## 1. TỔNG QUAN ĐẶC ĐIỂM KIỂM THỬ TÍCH HỢP
* **Phạm vi kiểm thử:** Tích hợp kiểm thử luồng phân tích hình ảnh qua HTTP Server AI giả lập (`HttpServer` tại `/api/v1/analyze`), xử lý Multipart form data, và lưu trữ vào CSDL MySQL thực tế.
* **Mục tiêu:** Đảm bảo hệ thống phản ứng chính xác trong 3 trường hợp: AI phê duyệt (lưu sản phẩm), AI từ chối ảnh lỗi (chặn lưu, trả về thông báo lỗi), và AI gặp sự cố 500 (fallback cảnh báo nhưng vẫn cho phép lưu sản phẩm an toàn).

---

## 2. MA TRẬN TEST CASES KIỂM THỬ TÍCH HỢP (3 TEST CASES)

| Mã TC | Chức năng ở src/main | Hàm kiểm thử ở src/test | Giá trị Test đầu vào | Kịch bản tích hợp / Logic mục tiêu | Kết quả mong đợi |
| :---: | :--- | :--- | :--- | :--- | :--- |
| **TC_ITG_AIS_01** | `POST /admin/product` (Analyze & Save) | `shouldSendMultipartImageAndPersistProductWhenAiApproves()` | File ảnh `shoe.png` (bytes hợp lệ), form sản phẩm hợp lệ, AI Server phản hồi `approved` | AI phê duyệt ảnh thành công: gửi đúng định dạng multipart form data và lưu trữ bản ghi sản phẩm vào DB | HTTP 302 Redirect về `/productList`, flash attribute `message`, DB lưu đủ tên, giá, số lượng, quyền sở hữu và mảng byte ảnh |
| **TC_ITG_AIS_02** | `POST /admin/product` (Analyze & Save) | `shouldRejectProductAndPreserveDatabaseWhenAiRejectsImage()` | File ảnh `shoe.png`, AI Server cấu hình trả về `rejected` (`"Image is blurred"`) | AI từ chối do ảnh mờ: hệ thống chặn thao tác lưu vào CSDL và trả về thông tin lỗi phân tích | HTTP 200 OK view `"product"`, model chứa `aiError = "Image is blurred"`, DB không có bản ghi sản phẩm (`count == 0`) |
| **TC_ITG_AIS_03** | `POST /admin/product` (Analyze & Save) | `shouldWarnAndPersistProductWhenAiReturnsServerError()` | File ảnh `shoe.png`, AI Server cấu hình phản hồi lỗi HTTP 500 (`serverError`) | Cơ chế chịu lỗi (Fault Tolerance): khi dịch vụ AI gặp sự cố, hệ thống không gián đoạn luồng nghiệp vụ tạo sản phẩm của admin | HTTP 302 Redirect về `/productList`, bản ghi sản phẩm vẫn được tạo thành công trong DB (`count == 1`) |
