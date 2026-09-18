# BẢNG MA TRẬN KIỂM THỬ TÍCH HỢP ACTUAL FASTAPI SERVICE
**Đường dẫn file mã nguồn:** [ProductController.java](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/src/main/java/com/example/demo/controller/ProductController.java)  
**Đường dẫn file kiểm thử:** [ActualFastApiIntegrationTest.java](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/src/test/java/com/example/demo/integration/ActualFastApiIntegrationTest.java)

---

## 1. TỔNG QUAN ĐẶC ĐIỂM KIỂM THỬ TÍCH HỢP
* **Phạm vi kiểm thử:** Tích hợp trực tiếp với dịch vụ AI FastAPI bên ngoài (qua thuộc tính `test24.actual-ai.url`) khi quản trị viên tạo sản phẩm và tải ảnh lên qua MockMvc.
* **Mục tiêu:** Xác thực hành vi của hệ thống khi dịch vụ AI FastAPI từ chối ảnh (ví dụ: ảnh quá nhỏ dưới 100x100px): hiển thị cảnh báo lỗi trên view và không lưu sản phẩm vào cơ sở dữ liệu MySQL.

---

## 2. MA TRẬN TEST CASES KIỂM THỬ TÍCH HỢP (1 TEST CASE)

| Mã TC | Chức năng ở src/main | Hàm kiểm thử ở src/test | Giá trị Test đầu vào | Kịch bản tích hợp / Logic mục tiêu | Kết quả mong đợi |
| :---: | :--- | :--- | :--- | :--- | :--- |
| **TC_ITG_AFA_01** | `POST /admin/product` (Save Product with AI) | `shouldRenderRejectionAndAvoidPersistenceWhenActualFastApiRejectsSmallImage()` | Multipart file ảnh PNG 1x1 pixel (`TINY_PNG`), thông tin sản phẩm mới hợp lệ, quyền `ROLE_ADMIN` | Tích hợp gửi ảnh sang dịch vụ FastAPI thật: FastAPI từ chối do kích thước ảnh dưới 100x100px | HTTP 200 OK trả về view `"product"`, model có thuộc tính `aiError` chứa `"100x100px"`, truy vấn DB `COUNT(*) == 0` (sản phẩm không bị lưu) |
