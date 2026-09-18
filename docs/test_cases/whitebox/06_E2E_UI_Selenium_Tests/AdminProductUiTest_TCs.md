# BẢNG MA TRẬN KIỂM THỬ GIAO DIỆN ADMIN PRODUCT (SELENIUM E2E UI)
**Đường dẫn file mã nguồn:** [ProductController.java](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/src/main/java/com/example/demo/controller/ProductController.java)  
**Đường dẫn file kiểm thử:** [AdminProductUiTest.java](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/src/test/java/com/example/demo/ui/AdminProductUiTest.java)

---

## 1. TỔNG QUAN ĐẶC ĐIỂM KIỂM THỬ GIAO DIỆN (SELENIUM E2E UI)
* **Phạm vi kiểm thử:** Tự động hóa trình duyệt qua Selenium WebDriver và `AdminProductPage` để kiểm tra chức năng Quản lý Sản phẩm dành cho Admin.
* **Mục tiêu:** Xác thực quy trình tạo mới sản phẩm có đính kèm file ảnh thực tế từ ổ đĩa và kiểm tra cơ chế chặn form khi người dùng bỏ trống trường mã sản phẩm (SKU code).

---

## 2. MA TRẬN TEST CASES KIỂM THỬ GIAO DIỆN (2 TEST CASES)

| Mã TC | Chức năng trên UI | Hàm kiểm thử ở src/test | Dữ liệu & Thao tác kiểm thử đầu vào | Kịch bản luồng thao tác người dùng (Steps) | Kết quả mong đợi (Assertions) |
| :---: | :--- | :--- | :--- | :--- | :--- |
| **TC_UI_PRD_01** | Tạo sản phẩm mới kèm upload ảnh | `TC01_adminCreateProductWithImageUploadSuccess()` | Mã ngẫu nhiên `UI...`, Tên, Giá `1250000`, Giảm `10%`, Tồn `25`, File ảnh mẫu JPG | 1. Đăng nhập quyền admin<br>2. Mở trang `/admin/product`<br>3. Điền đầy đủ thông tin sản phẩm<br>4. Chọn tệp ảnh thật và tải lên form<br>5. Bấm nút "Lưu sản phẩm" | Sản phẩm được tạo thành công kèm hình ảnh, trình duyệt chuyển hướng về `/productList` hoặc trang chi tiết sản phẩm |
| **TC_UI_PRD_02** | Chặn tạo sản phẩm khi thiếu mã Code | `TC02_createProductWithEmptyCodeShouldFail()` | Bỏ trống trường Code, điền Tên, Giá `990000`, Tồn `10` | 1. Đăng nhập quyền admin<br>2. Mở trang `/admin/product`<br>3. Không nhập trường Code<br>4. Điền các trường khác và bấm Lưu | Hệ thống chặn submit trên trình duyệt hoặc giữ nguyên trang `/admin/product` kèm thông báo lỗi trường bắt buộc |
