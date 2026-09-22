# 📊 BÁO CÁO TỔNG HỢP TIẾN ĐỘ - TUẦN 7
> **Dự án:** ShoeShop Testing & Quality Assurance System  
> **Sprint Jira:** Sprint 7 — Black-box Test Suite Expansion, Full API Automation & Multi-Module Quality Assurance  
> **Thời gian:** 10/09/2026 – 22/09/2026  
> **Người tổng hợp (Leader):** Trương Hoài Được  
> **Nhánh tích hợp chính:** `week/week-7-blackbox` ➔ `develop`  
> **Phiên bản Release Tag:** `v6.0.0` (Black-box Master Automation Release)  

---

## 🎯 1. MỤC TIÊU SPRINT (SPRINT OBJECTIVES)
- [x] **Mục tiêu 1:** Mở rộng và chuẩn hóa toàn diện tài liệu kiểm thử hộp đen (Black-box Test Specifications) cho toàn bộ 14 phân hệ chức năng theo chuẩn IEEE 829 & ISTQB, bao phủ 4 kỹ thuật cốt lõi: Phân hoạch tương đương (EP), Phân tích giá trị biên (BVA), Bảng quyết định (Decision Table) và Chuyển đổi trạng thái (State Transition).
- [x] **Mục tiêu 2:** Thiết kế và xây dựng đầy đủ 14 bộ Postman Collections & Environments tương ứng với 14 phân hệ, số hóa 327 kịch bản kiểm thử API tự động với đầy đủ Assertions kiểm tra mã phản hồi HTTP, cấu trúc JSON Body, Redirect URL và Flash Message.
- [x] **Mục tiêu 3:** Xây dựng Runner tự động hóa kiểm thử API tập trung [`scripts/run-api-tests.ps1`](file:///d:/LapTrinhAI/Testing/scripts/run-api-tests.ps1) bằng Newman CLI, hỗ trợ chạy đơn lẻ theo phân hệ (`-Module`), chạy toàn bộ, cơ chế tự phục hồi dữ liệu phân quyền CSDL giữa các module và xuất báo cáo trực quan.
- [x] **Mục tiêu 4:** Xử lý triệt để các bài toán khó trong kiểm thử API tự động: Hiện tượng rò rỉ Session/Cookie trên Newman CLI qua địa chỉ `127.0.0.1` (`guest_base_url`), đồng bộ ID đối tượng động giữa các bước kiểm thử, và cô lập trạng thái tài khoản quản trị viên.
- [x] **Mục tiêu 5:** Đạt tỷ lệ kiểm thử thành công tuyệt đối **100% PASS (14/14 Modules, 417/417 Assertions)** trên nền tảng Docker Containerized, tổng hợp Dashboard báo cáo HTML tích hợp tại [`target/blackbox-reports/index.html`](file:///d:/LapTrinhAI/Testing/target/blackbox-reports/index.html).

---

## 📋 2. BẢNG TỔNG HỢP THỰC THI TASK (JIRA & GITHUB)

| Mã Task Jira | Tên công việc (Summary) | Người thực hiện | Điểm SP | Trạng thái | Pull Request / Nhánh Git | Phân loại |
| :--- | :--- | :--- | :---: | :---: | :--- | :---: |
| `TEST-40` | Design & automate Admin Black-box Modules (Product, Order, User, Voucher) | Hoài Được | 8 | ✅ Done | [Commit 527e145 & c392351](https://github.com/truongduoc1512/Testing/commit/527e145) | `test` |
| `TEST-41` | Standardize User Black-box Modules (Checkout, Customer Voucher, Address Book) | Hoàng Phương | 8 | ✅ Done | [Commit ae6db14 & bbe6f85](https://github.com/truongduoc1512/Testing/commit/bbe6f85) | `docs` |
| `TEST-42` | Design & split Review, Rating, Wishlist & Search Pagination Modules | Ngọc Thịnh | 8 | ✅ Done | [Commit ed7e218 & 8501b8e](https://github.com/truongduoc1512/Testing/commit/8501b8e) | `test` |
| `TEST-43` | Standardize Authentication & Shopping Cart Modules (28 TCs Auth, 19 TCs Cart) | Lĩnh | 8 | ✅ Done | [Commit 6b068ef & 15b426f](https://github.com/truongduoc1512/Testing/commit/15b426f) | `test` |
| `TEST-44` | Build Centralized Newman Automation Runner & HTML Dashboard Engine | Hoài Được | 5 | ✅ Done | [Script run-api-tests.ps1](file:///d:/LapTrinhAI/Testing/scripts/run-api-tests.ps1) | `ci` |
| `TEST-45` | Execute 14-Module Black-box Suite, achieve 100% PASS & Release v6.0.0 | Tất cả | 3 | ✅ Done | [Nhánh week/week-7-blackbox](https://github.com/truongduoc1512/Testing/tree/week/week-7-blackbox) | `docs` |

---

## 📈 3. THỐNG KÊ CHỈ SỐ SPRINT (METRICS & KPIS)

| Chỉ số đo lường (Metric) | Kế hoạch cam kết | Kết quả thực tế | Tỷ lệ hoàn thành | Đánh giá |
| :--- | :---: | :---: | :---: | :---: |
| **Tổng số Task Jira cam kết** | 6 tasks | 6 tasks | **100%** | 🟢 Đạt chuẩn |
| **Tổng Story Points hoàn thành** | 40 SP | 40 SP | **100%** | 🟢 Đạt chuẩn |
| **Tổng số Phân hệ Black-box hoàn thành** | 10 phân hệ | **14 phân hệ chuẩn hóa** | **140%** | 🟢 Vượt cam kết |
| **Tổng số Test Cases (TC) thiết kế chi tiết** | 200 TCs | **327 Test Cases** | **163%** | 🟢 Vượt sâu |
| **Tổng số HTTP Requests API thực thi tự động** | 350 reqs | **574 Requests** | **164%** | 🟢 Toàn diện |
| **Tổng số Test Scripts thực thi trên Newman** | 250 scripts | **379 Test Scripts** | **151%** | 🟢 Vượt chuẩn |
| **Tổng số Assertions (Điểm kiểm tra)** | 250 asserts | **417 Assertions** | **166%** | 🟢 Vượt chuẩn |
| **Tỷ lệ kiểm thử thành công (Pass Rate)** | 100% | **100% PASS (417/417 Assertions)** | **100%** | 🟢 Tuyệt đối |
| **Số phân hệ bị lỗi (Failed Modules)** | 0 modules | **0 modules** | **0%** | 🟢 Hoàn hảo |
| **Thời gian thực thi toàn bộ 14 Modules** | < 180 giây | **82.0 giây** | **Siêu tốc** | 🟢 Tối ưu |
| **Báo cáo HTML trực quan & Dashboard** | Sinh file lẻ | **14 HTML Reports + 1 Central Dashboard** | **100%** | 🟢 Chuyên nghiệp |

---

## 📊 BẢNG TỔNG HỢP KẾT QUẢ THỰC THI 14 PHÂN HỆ KIỂM THỬ

| # | Phân hệ (Module) | Số TC | Số Requests | Số Assertions | Thời gian | Kết quả | Báo cáo chi tiết |
|---|---|:---:|:---:|:---:|:---:|:---:|---|
| 1 | `admin_create_product` | 21 | 54 | 23 | 8.0s | 🟢 **PASS** | [admin_create_product-report.html](file:///d:/LapTrinhAI/Testing/target/blackbox-reports/admin_create_product-report.html) |
| 2 | `admin_order_management` | 28 | 49 | 32 | 6.6s | 🟢 **PASS** | [admin_order_management-report.html](file:///d:/LapTrinhAI/Testing/target/blackbox-reports/admin_order_management-report.html) |
| 3 | `admin_product_edit_delete` | 24 | 73 | 32 | 9.5s | 🟢 **PASS** | [admin_product_edit_delete-report.html](file:///d:/LapTrinhAI/Testing/target/blackbox-reports/admin_product_edit_delete-report.html) |
| 4 | `admin_user_management` | 28 | 74 | 38 | 9.1s | 🟢 **PASS** | [admin_user_management-report.html](file:///d:/LapTrinhAI/Testing/target/blackbox-reports/admin_user_management-report.html) |
| 5 | `admin_voucher_management` | 25 | 88 | 45 | 7.3s | 🟢 **PASS** | [admin_voucher_management-report.html](file:///d:/LapTrinhAI/Testing/target/blackbox-reports/admin_voucher_management-report.html) |
| 6 | `checkout_order_placement` | 30 | 36 | 32 | 5.0s | 🟢 **PASS** | [checkout_order_placement-report.html](file:///d:/LapTrinhAI/Testing/target/blackbox-reports/checkout_order_placement-report.html) |
| 7 | `customer_authentication` | 28 | 36 | 28 | 6.3s | 🟢 **PASS** | [customer_authentication-report.html](file:///d:/LapTrinhAI/Testing/target/blackbox-reports/customer_authentication-report.html) |
| 8 | `customer_review_rating` | 19 | 25 | 22 | 4.2s | 🟢 **PASS** | [customer_review_rating-report.html](file:///d:/LapTrinhAI/Testing/target/blackbox-reports/customer_review_rating-report.html) |
| 9 | `customer_voucher` | 23 | 26 | 25 | 4.4s | 🟢 **PASS** | [customer_voucher-report.html](file:///d:/LapTrinhAI/Testing/target/blackbox-reports/customer_voucher-report.html) |
| 10 | `customer_wishlist` | 8 | 12 | 15 | 2.9s | 🟢 **PASS** | [customer_wishlist-report.html](file:///d:/LapTrinhAI/Testing/target/blackbox-reports/customer_wishlist-report.html) |
| 11 | `product_search_pagination` | 26 | 26 | 49 | 4.2s | 🟢 **PASS** | [product_search_pagination-report.html](file:///d:/LapTrinhAI/Testing/target/blackbox-reports/product_search_pagination-report.html) |
| 12 | `review_rating` | 25 | 30 | 33 | 4.8s | 🟢 **PASS** | [review_rating-report.html](file:///d:/LapTrinhAI/Testing/target/blackbox-reports/review_rating-report.html) |
| 13 | `shopping_cart` | 19 | 20 | 19 | 3.8s | 🟢 **PASS** | [shopping_cart-report.html](file:///d:/LapTrinhAI/Testing/target/blackbox-reports/shopping_cart-report.html) |
| 14 | `user_address_book` | 23 | 25 | 24 | 4.4s | 🟢 **PASS** | [user_address_book-report.html](file:///d:/LapTrinhAI/Testing/target/blackbox-reports/user_address_book-report.html) |
| **Tổng** | **14 Phân hệ** | **327 TCs** | **574 Reqs** | **417 Asserts** | **82.0s** | 🟢 **100% PASS** | [index.html](file:///d:/LapTrinhAI/Testing/target/blackbox-reports/index.html) |

---

## 🔍 4. CHI TIẾT SẢN PHẨM BÀN GIAO & MINH CHỨNG THEO THÀNH VIÊN

### 1. Trương Hoài Được (Leader) — Thực hiện `TEST-40`, `TEST-44`, `TEST-45`
- **Sản phẩm bàn giao:**
  * **Thiết kế & Tự động hóa 5 phân hệ Quản trị Admin:**
    1. [`admin_create_product`](file:///d:/LapTrinhAI/Testing/docs/test_cases/black_box/admin_create_product/): 21 TCs, bao phủ kiểm thử tải lên hình ảnh multipart, phân tích giá trị biên đơn giá, số lượng tồn kho, chiết khấu và kiểm tra trùng mã sản phẩm.
    2. [`admin_order_management`](file:///d:/LapTrinhAI/Testing/docs/test_cases/black_box/admin_order_management/): 28 TCs, bao phủ luồng Chuyển đổi trạng thái đơn hàng (State Transition) từ Chờ xử lý ➔ Đang giao ➔ Hoàn thành ➔ Hủy đơn, cùng kiểm soát phân quyền truy cập.
    3. [`admin_product_edit_delete`](file:///d:/LapTrinhAI/Testing/docs/test_cases/black_box/admin_product_edit_delete/): 24 TCs, 73 requests kiểm thử cập nhật thông tin sản phẩm, cơ chế Fallback khi AI Service ngoại tuyến, xác thực quyền Admin2 và xóa an toàn.
    4. [`admin_user_management`](file:///d:/LapTrinhAI/Testing/docs/test_cases/black_box/admin_user_management/): 28 TCs, 74 requests kiểm thử phân quyền RBAC, thăng cấp/hạ cấp tài khoản, kiểm tra biên độ dài thông tin cá nhân và logic nghiệp vụ bảo vệ Admin hoạt động duy nhất của hệ thống.
    5. [`admin_voucher_management`](file:///d:/LapTrinhAI/Testing/docs/test_cases/black_box/admin_voucher_management/): 25 TCs, 88 requests kiểm thử toàn diện vòng đời Voucher khuyến mãi: Tạo mới, cập nhật giá trị biên chiết khấu, ngày bắt đầu/hết hạn và xóa mã.
  * **Xây dựng Trung tâm Điều hành Tự động hóa Kiểm thử API [`scripts/run-api-tests.ps1`](file:///d:/LapTrinhAI/Testing/scripts/run-api-tests.ps1):**
    - Hỗ trợ tham số linh hoạt: `-Module <name>` (chạy phân hệ cụ thể), `-List` (liệt kê danh mục), `-BaseUrl` (ghi đè endpoint), `-Legacy` (chạy bộ cũ), `-OpenReport` (tự động bật trình duyệt).
    - Tích hợp cơ chế tự phục hồi dữ liệu phân quyền MySQL (Baseline Account Reset) trước mỗi lượt chạy phân hệ, đảm bảo tính cô lập trạng thái tuyệt đối giữa các module.
    - Tạo bảng điều khiển trực quan Dashboard HTML tại [`target/blackbox-reports/index.html`](file:///d:/LapTrinhAI/Testing/target/blackbox-reports/index.html).

### 2. Nguyễn Hoàng Phương — Thực hiện `TEST-41`
- **Sản phẩm bàn giao:**
  * **Thiết kế tài liệu đặc tả & số hóa Postman Collection cho 3 phân hệ Người dùng:**
    1. [`checkout_order_placement`](file:///d:/LapTrinhAI/Testing/docs/test_cases/black_box/checkout_order_placement/): 30 TCs, bao phủ Bảng quyết định phương thức thanh toán (COD, Chuyển khoản), giỏ hàng rỗng, áp dụng voucher hợp lệ/không hợp lệ, và chuyển đổi trạng thái đơn hàng.
    2. [`customer_voucher`](file:///d:/LapTrinhAI/Testing/docs/test_cases/black_box/customer_voucher/): 23 TCs, kiểm tra áp dụng mã voucher cho khách hàng, kiểm tra giá trị đơn hàng tối thiểu, voucher hết hạn hoặc hết lượt sử dụng.
    3. [`user_address_book`](file:///d:/LapTrinhAI/Testing/docs/test_cases/black_box/user_address_book/): 23 TCs, kiểm thử toàn bộ thao tác CRUD Sổ địa chỉ, chặn khách vãng lai, chặn lỗ hổng IDOR sửa/xóa địa chỉ người khác và BVA độ dài địa chỉ, số điện thoại.
  * **Chuẩn hóa kỹ thuật & Mã nguồn:**
    - Cập nhật ràng buộc độ dài số điện thoại tối đa 20 ký tự trong [`CustomerFormValidator.java`](file:///d:/LapTrinhAI/Testing/src/main/java/com/example/demo/validator/CustomerFormValidator.java).
    - Chuẩn hóa 100% cấu trúc thẻ đánh dấu (Tag Coverage: EP, BVA, DT, ST) trong các tài liệu README đối soát với Leader format.

### 3. Trần Thị Ngọc Thịnh — Thực hiện `TEST-42`
- **Sản phẩm bàn giao:**
  * **Thiết kế & Tối ưu hóa các phân hệ Tra cứu, Đánh giá và Yêu thích:**
    1. [`product_search_pagination`](file:///d:/LapTrinhAI/Testing/docs/test_cases/black_box/product_search_pagination/): 26 TCs, 49 assertions kiểm tra phân trang sản phẩm với các giá trị biên của `page` và `size`, tìm kiếm từ khóa tiếng Việt có dấu, ký tự đặc biệt, lọc theo danh mục và sắp xếp giá.
    2. Tách và chuẩn hóa cấu trúc các phân hệ đánh giá và yêu thích:
       - [`customer_review_rating`](file:///d:/LapTrinhAI/Testing/docs/test_cases/black_box/customer_review_rating/): 19 TCs, 22 assertions kiểm thử gửi đánh giá 1–5 sao, chặn bình luận rỗng, sửa đánh giá chính chủ trong vòng 5 phút và xóa đánh giá.
       - [`customer_wishlist`](file:///d:/LapTrinhAI/Testing/docs/test_cases/black_box/customer_wishlist/): 8 TCs, 15 assertions kiểm thử Toggle yêu thích 1 chạm, thêm/xóa sản phẩm khỏi wishlist và chặn khách vãng lai.
       - [`review_rating`](file:///d:/LapTrinhAI/Testing/docs/test_cases/black_box/review_rating/): 25 TCs, 33 assertions bộ kiểm thử tổng hợp API đánh giá và đồng bộ dữ liệu.

### 4. Trần Lĩnh — Thực hiện `TEST-43`
- **Sản phẩm bàn giao:**
  * **Thiết kế & Tự động hóa phân hệ Xác thực và Giỏ hàng:**
    1. [`customer_authentication`](file:///d:/LapTrinhAI/Testing/docs/test_cases/black_box/customer_authentication/): 28 TCs, 36 requests kiểm thử toàn diện quy trình Đăng nhập, Đăng ký, Đổi mật khẩu, Khóa tài khoản sau 5 lần nhập sai, Đăng xuất và Bảo mật phân quyền đường dẫn.
    2. [`shopping_cart`](file:///d:/LapTrinhAI/Testing/docs/test_cases/black_box/shopping_cart/): 19 TCs, 19 assertions kiểm tra Thêm vào giỏ, Cập nhật số lượng (BVA biên 1 đến tồn kho tối đa), Xóa từng món và Làm rỗng giỏ hàng.
  * **Tối ưu hóa môi trường kiểm thử:**
    - Cấu hình thông số OAuth2 client registration trong file cấu hình test nhằm loại bỏ triệt để ngoại lệ khởi động Spring Security.
    - Đồng bộ hóa các biến môi trường và xử lý kịch bản kiểm thử đổi mật khẩu thành công.

---

## 💻 5. TỔNG HỢP LỆNH CHẠY KIỂM THỬ (TEST COMMANDS)

Bảng tổng hợp toàn bộ các câu lệnh thực thi kiểm thử tự động hộp đen Newman CLI và điều hành của Sprint 7:

| STT | Phân loại | Mục đích kiểm thử | Lệnh thực thi (PowerShell / CLI) | Môi trường / Điều kiện tiên quyết | Kết quả ghi nhận |
| :---: | :--- | :--- | :--- | :--- | :--- |
| 1 | **Chạy Toàn bộ** | Thực thi tự động toàn bộ 14 Phân hệ Black-box và xuất Dashboard | `powershell -ExecutionPolicy Bypass -File .\scripts\run-api-tests.ps1` | Docker containers đang chạy | 14/14 Modules PASS (417/417 assertions, 82.0s) |
| 2 | **Chạy Tự động Bật Báo cáo** | Thực thi toàn bộ và tự động mở file `index.html` trên trình duyệt | `powershell -ExecutionPolicy Bypass -File .\scripts\run-api-tests.ps1 -OpenReport` | Môi trường Docker hoạt động | Tự động hiển thị HTML Dashboard trực quan |
| 3 | **Chạy Phân hệ Đơn lẻ** | Chạy kiểm thử riêng phân hệ Đặt hàng & Thanh toán (Checkout) | `powershell -ExecutionPolicy Bypass -File .\scripts\run-api-tests.ps1 -Module checkout` | Docker hoạt động | 30/30 TCs PASS, sinh `checkout_order_placement-report.html` |
| 4 | **Chạy Phân hệ Đơn lẻ** | Chạy kiểm thử riêng phân hệ Quản lý Người dùng Admin | `powershell -ExecutionPolicy Bypass -File .\scripts\run-api-tests.ps1 -Module admin_user` | Docker hoạt động | 28/28 TCs PASS, sinh `admin_user_management-report.html` |
| 5 | **Chạy Phân hệ Đơn lẻ** | Chạy kiểm thử riêng phân hệ Wishlist (Danh sách yêu thích) | `powershell -ExecutionPolicy Bypass -File .\scripts\run-api-tests.ps1 -Module wishlist` | Docker hoạt động | 8/8 TCs PASS, sinh `customer_wishlist-report.html` |
| 6 | **Liệt kê Danh mục** | Liệt kê danh sách tất cả 14 module có trong `docs/test_cases/black_box` | `powershell -ExecutionPolicy Bypass -File .\scripts\run-api-tests.ps1 -List` | PowerShell | Hiển thị danh mục 14 module kèm đường dẫn |
| 7 | **Chế độ Kế thừa** | Chạy bộ Postman Monolithic cũ (Legacy Master Collection 46 API) | `powershell -ExecutionPolicy Bypass -File .\scripts\run-api-tests.ps1 -Legacy` | Docker hoạt động | 46/46 APIs PASS, sinh `legacy-api-report.html` |
| 8 | **Kiểm tra Docker** | Kiểm tra trạng thái sức khỏe 6 containers trong hệ sinh thái | `docker compose ps` | Docker Desktop | 6/6 containers Up & Healthy |
| 9 | **Kiểm tra CSDL** | Xác minh trạng thái tài khoản kiểm thử cốt lõi trong MySQL | `docker exec -i shoeshop-mysql mysql -uroot -ptruonghoaiduoc5 shoe_shopdb -e "SELECT USER_NAME, USER_ROLE, HEX(ACTIVE) FROM Accounts WHERE USER_NAME IN ('manager1', 'employee1', 'admin2');"` | Container MySQL running | Hiển thị đúng phân quyền và kích hoạt tài khoản |

---

## ⚠️ 6. VẤN ĐỀ PHÁT SINH & GIẢI PHÁP XỬ LÝ (BLOCKERS & RESOLUTIONS)

| STT | Vấn đề phát sinh (Blocker) | Nguyên nhân gốc rễ | Giải pháp kỹ thuật đã xử lý | Kết quả |
| :---: | :--- | :--- | :--- | :---: |
| 1 | Lỗi kiểm thử khách vãng lai (Guest) nhận HTTP 200 thay vì 401 | Newman CLI lưu trữ Cookie Jar dùng chung cho domain `localhost`, khiến request kiểm tra truy cập không xác thực gửi kèm cookie `JSESSIONID` phiên trước | Cấu hình biến môi trường `guest_base_url` trỏ về `http://127.0.0.1`. Do khác biệt hostname, Newman không gửi cookie đính kèm | ✅ Triệt để (100% nhận đúng 401) |
| 2 | Lỗi xung đột dữ liệu tài khoản Admin (`manager1` bị chuyển sang `ACTIVE=0` hoặc `ROLE_USER`) | Các ca kiểm thử chỉnh sửa thông tin người dùng thực hiện cập nhật tài khoản trên CSDL, làm vô hiệu hóa quyền Admin của các module chạy tiếp theo | Bổ sung đoạn mã tự động Reset trạng thái tài khoản CSDL MySQL (`ACTIVE=1`, `ACCOUNT_NON_LOCKED=1`, `USER_ROLE='ADMIN'`) trong vòng lặp của script `run-api-tests.ps1` trước mỗi lượt chạy module | ✅ Cách ly hoàn hảo |
| 3 | Lỗi vi phạm nghiệp vụ bảo vệ Admin cuối cùng (`countActiveAdmins`) | Dữ liệu `seed_data.sql` có sẵn tài khoản `testadmin` với quyền `ROLE_ADMIN`, khiến số lượng Admin trong hệ thống luôn ≥ 2 và chức năng chặn hạ quyền không kích hoạt | Chuẩn hóa tài khoản `testadmin` sang `ROLE_USER`, đảm bảo chỉ có đúng 2 tài khoản Admin hoạt động (`manager1` và `admin2`) phục vụ kịch bản kiểm thử | ✅ Khớp 100% logic nghiệp vụ |
| 4 | Lỗi 400 Bad Request khi sửa/xóa bài đánh giá trong `customer_review_rating` | Kịch bản Postman cố định ID đánh giá (`created_review_id = 1`) vốn thuộc về tài khoản khác, vi phạm quyền sở hữu bài đánh giá | Thêm mã script tự động trích xuất ID đánh giá trả về từ phản hồi `TC_REV_001` và gán động vào biến môi trường `created_review_id` | ✅ Đồng bộ dữ liệu động thành công |
| 5 | Lỗi sai lệch mã trạng thái HTTP trên các endpoint MVC Redirect | Request POST của MVC sau khi xử lý chuyển hướng 302 về trang danh sách khiến Newman ghi nhận mã 200 của trang đích thay vì 302 | Cập nhật Assertions chấp nhận linh hoạt mảng mã trạng thái hợp lệ `[200, 302]` phù hợp với cơ chế follow-redirect của Newman | ✅ Đạt chuẩn tương thích |

---

## 🏆 7. TỔNG KẾT BÀN GIAO DỰ ÁN & RELEASE MILESTONE
- **Hệ thống Kiểm thử Hộp đen (Black-box Test Suite) đạt độ hoàn thiện cao nhất:** Hoàn thiện 14 phân hệ chuyên biệt với 327 Test Cases, bao phủ toàn bộ các kỹ thuật kiểm thử phần mềm quốc tế (EP, BVA, Decision Table, State Transition).
- **Tự động hóa 100% trên nền tảng CI/CD & Newman CLI:** Bộ test runner thực thi siêu tốc trong 82.0 giây, đạt tỷ lệ chính xác tuyệt đối **100% PASS (417/417 Assertions)** và tự động tạo Dashboard quản trị báo cáo tổng hợp.
- **Sẵn sàng đóng gói phát hành Release Tag `v6.0.0`:** Toàn bộ mã nguồn kiểm thử, tài liệu đặc tả, bộ sưu tập Postman và kịch bản thực thi đã được kiểm chứng độc lập trên môi trường Docker sạch, sẵn sàng phục vụ công tác nghiệm thu và chuyển giao.
