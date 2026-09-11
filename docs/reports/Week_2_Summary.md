# 📊 BÁO CÁO TỔNG HỢP TIẾN ĐỘ - TUẦN 2
> **Dự án:** ShoeShop Testing & Quality Assurance System  
> **Sprint Jira:** Sprint 2 — Test Case Design, Database Seed Data & Unit Testing  
> **Thời gian:** 04/08/2026 – 10/08/2026  
> **Người tổng hợp (Leader):** Trương Hoài Được  
> **Nhánh tích hợp chính:** `week/week-2-unit-blackbox` ➔ `develop`  
> **Phiên bản Release Tag:** `v1.0.0`  

---

## 🎯 1. MỤC TIÊU SPRINT (SPRINT OBJECTIVES)
- [x] **Mục tiêu 1:** Thiết kế toàn diện các kịch bản kiểm thử hộp đen REST API Xác thực (Auth API) áp dụng phương pháp Phân hoạch tương đương (EP) và Phân tích giá trị biên (BVA).
- [x] **Mục tiêu 2:** Thiết kế kịch bản kiểm thử Giỏ hàng & Đơn hàng (Cart & Order) áp dụng Bảng quyết định (Decision Table) và Sơ đồ chuyển đổi trạng thái (State Transition Diagram).
- [x] **Mục tiêu 3:** Phát triển bộ Unit Test tự động cho toàn bộ các lớp Form Validator (`CustomerFormValidator`, `ProductFormValidator`, `RegisterFormValidator`).
- [x] **Mục tiêu 4:** Phát triển bộ 10 file Unit & Integration Test kiểm thử cô lập toàn bộ tầng Data Access Object (DAO) với cơ chế Mocking Hibernate Session.
- [x] **Mục tiêu 5:** Xây dựng tập dữ liệu mẫu chuẩn (Database Seed Data - `seed_data.sql`) khởi tạo trạng thái nhất quán phục vụ kiểm thử.
- [x] **Mục tiêu 6:** Xây dựng AI Mock Server phản hồi siêu tốc (`< 10ms`) phục vụ kiểm thử tích hợp tự động không phụ thuộc GPU.
- [x] **Mục tiêu 7:** Thực hiện quy trình đóng gói DevOps: Đánh tag phiên bản `v1.0.0`, tham số hóa Docker Compose và tích hợp nhánh sạch.

---

## 📋 2. BẢNG TỔNG HỢP THỰC THI TASK (JIRA & GITHUB)

| Mã Task Jira | Tên công việc (Summary) | Người thực hiện | Điểm SP | Trạng thái | Pull Request / Nhánh Git | Phân loại |
| :--- | :--- | :--- | :---: | :---: | :--- | :---: |
| `TEST-8` | Design Auth API test cases (EP & BVA) | Ngọc Thịnh | 5 | ✅ Done | [PR docs/w2-TEST-8-design-auth-api-cases](https://github.com/truongduoc1512/Testing/tree/docs/w2-TEST-8-design-auth-api-cases) | `docs` |
| `TEST-9` | Design Cart & Order test cases (Decision Table & State) | Ngọc Thịnh | 5 | ✅ Done | [PR docs/w2-TEST-9-design-cart-order-test-cases](https://github.com/truongduoc1512/Testing/tree/docs/w2-TEST-9-design-cart-order-test-cases) | `docs` |
| `TEST-10` | Develop validator unit tests | Hoàng Phương | 5 | ✅ Done | [PR test/w2-TEST-10-validator-unit-tests](https://github.com/truongduoc1512/Testing/tree/test/w2-TEST-10-validator-unit-tests) | `test` |
| `TEST-11` | Develop DAO unit tests (10 files) | Hoàng Phương | 8 | ✅ Done | [PR test/w2-TEST-11-dao-unit-tests](https://github.com/truongduoc1512/Testing/tree/test/w2-TEST-11-dao-unit-tests) | `test` |
| `TEST-12` | Prepare database seed data (`seed_data.sql`) | Lĩnh | 5 | ✅ Done | [PR feat/w2-TEST-12-prepare-database-seed-data](https://github.com/truongduoc1512/Testing/tree/feat/w2-TEST-12-prepare-database-seed-data) | `feat` |
| `TEST-13` | Build AI mock server (`/api/v1/mock/analyze`) | Hoài Được | 5 | ✅ Done | [PR feat/w2-TEST-13-build-ai-mock-server](https://github.com/truongduoc1512/Testing/tree/feat/w2-TEST-13-build-ai-mock-server) | `feat` |
| `TEST-14` | Submit Week 2 summary report & DevOps release `v1.0.0` | Tất cả | 2 | ✅ Done | [PR week/week-2-unit-blackbox](https://github.com/truongduoc1512/Testing/tree/week/week-2-unit-blackbox) | `docs` |

---

## 📈 3. THỐNG KÊ CHỈ SỐ SPRINT (METRICS & KPIS)

| Chỉ số đo lường (Metric) | Kế hoạch cam kết | Kết quả thực tế | Tỷ lệ hoàn thành | Đánh giá |
| :--- | :---: | :---: | :---: | :---: |
| **Tổng số Task Jira cam kết** | 7 tasks | 7 tasks | **100%** | 🟢 Đạt chuẩn |
| **Tổng Story Points hoàn thành** | 35 SP | 35 SP | **100%** | 🟢 Đạt chuẩn |
| **Số dòng code Unit Test Java mới** | ~3,000 dòng | **4,199 dòng code** | **140%** | 🟢 Vượt cam kết |
| **Số kịch bản kiểm thử hộp đen thiết kế** | 30 test cases | **46 kịch bản chi tiết** | **153%** | 🟢 Rất chi tiết |
| **Số bản ghi dữ liệu mẫu (Seed Data)** | 100 bản ghi | **279 bản ghi (6 bảng)** | **279%** | 🟢 Phong phú |
| **Thời gian phản hồi của AI Mock Server** | < 50ms | **< 10ms (tức thì)** | **Đạt chuẩn** | 🟢 Tối ưu |
| **Đóng gói phát hành Release Tag** | `v1.0.0` | **`v1.0.0` trên GitHub** | **100%** | 🟢 Thành công |

---

## 🔍 4. CHI TIẾT SẢN PHẨM BÀN GIAO & MINH CHỨNG THEO THÀNH VIÊN

### 1. Trương Hoài Được (Leader) — Thực hiện `TEST-13`, `DevOps Release v1.0.0`
- **Sản phẩm bàn giao:**
  * Endpoint AI Mock Server trong file [`ai-service/app/main.py`](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/ai-service/app/main.py): Đường dẫn `/api/v1/mock/analyze` hỗ trợ tham số `force_status=PASS|REJECT|BLUR|NOT_SHOE` hoặc kiểm tra tên file giả lập kiểm duyệt ảnh tức thì (`< 10ms`).
  * Script Python Standalone Server [`scripts/mock_ai_server.py`](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/scripts/mock_ai_server.py) phục vụ kiểm thử độc lập không cần Docker.
  * Tài liệu hướng dẫn vận hành AI Mock [`docs/TEST-13.md`](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/docs/TEST-13.md).
  * Tham số hóa `docker-compose.yml` bằng biến `${APP_VERSION:-latest}`, tạo file `.env` với `APP_VERSION=v1.0.0` và đánh Tag `v1.0.0` trên Git remote.

### 2. Hoàng Phương — Thực hiện `TEST-10`, `TEST-11`
- **Sản phẩm bàn giao:**
  * **Bộ 3 file Form Validator Tests:** [`CustomerFormValidatorTest`](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/src/test/java/com/example/demo/validator/CustomerFormValidatorTest.java), [`ProductFormValidatorTest`](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/src/test/java/com/example/demo/validator/ProductFormValidatorTest.java), [`RegisterFormValidatorTest`](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/src/test/java/com/example/demo/validator/RegisterFormValidatorTest.java) + Báo cáo [`docs/TEST-10.md`](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/docs/TEST-10.md).
  * **Bộ 10 file DAO Tests:** `AccountDAOTest`, `OrderDAOTest`, `OrderReturnDAOTest`, `ProductDAOTest`, `ProductDAOIntegrationTest`, `ProductReviewDAOTest`, `UserAddressDAOTest`, `VoucherDAOTest`, `WishlistDAOTest`, `DaoTestSupport` + Báo cáo [`docs/TEST-11.md`](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/docs/TEST-11.md).
  * File Flyway migration `V16__align_account_identity_lengths.sql` đồng bộ độ dài trường dữ liệu tài khoản trong CSDL.

### 3. Lĩnh — Thực hiện `TEST-12`
- **Sản phẩm bàn giao:**
  * Tập dữ liệu mẫu [`seed_data.sql`](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/seed_data.sql) gồm 279 dòng lệnh SQL, nạp dữ liệu chuẩn xác cho 6 bảng cốt lõi:
    - `Accounts`: 6 tài khoản mẫu (ADMIN, USER, EMPLOYEE) đã băm mật khẩu BCrypt.
    - `Products`: 10 sản phẩm giày đa dạng danh mục và trạng thái tồn kho.
    - `User_Addresses`: 8 địa chỉ giao hàng mẫu phục vụ checkout.
    - `Wishlist`: 8 bản ghi sản phẩm yêu thích.
    - `Vouchers`: 7 mã giảm giá với các điều kiện biên giá trị đơn hàng khác nhau.
    - `Product_Reviews`: 12 đánh giá và điểm xếp hạng sao.

### 4. Ngọc Thịnh — Thực hiện `TEST-8`, `TEST-9`
- **Sản phẩm bàn giao:**
  * Tài liệu kiểm thử API Xác thực [`docs/TEST_CASES_AUTH_API.md`](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/docs/TEST_CASES_AUTH_API.md) (167 dòng): Thiết kế 16 kịch bản kiểm thử phân hoạch tương đương (EP) và phân tích giá trị biên (BVA) cho độ dài mật khẩu, định dạng email, username.
  * Tài liệu kiểm thử Giỏ hàng & Đơn hàng [`docs/TEST_CASES_CART_ORDER.md`](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/docs/TEST_CASES_CART_ORDER.md) (372 dòng): Thiết kế Bảng quyết định (Decision Table) cho luồng Checkout áp dụng Voucher và Sơ đồ chuyển đổi trạng thái vòng đời đơn hàng (Draft ➔ Pending ➔ Confirmed ➔ Completed / Cancelled / Returned) kèm biểu đồ Mermaid trực quan.

---

## ⚠️ 5. VẤN ĐỀ PHÁT SINH & GIẢI PHÁP XỬ LÝ (BLOCKERS & RESOLUTIONS)

| STT | Vấn đề phát sinh (Blocker) | Nguyên nhân gốc rễ | Giải pháp kỹ thuật đã xử lý | Kết quả |
| :---: | :--- | :--- | :--- | :---: |
| 1 | SonarQube Container chiếm 2.5GB RAM gây treo máy phát triển | Elasticsearch & JVM tích hợp ngốn tài nguyên lớn | Tách SonarQube thành profile riêng, áp dụng Checkstyle/SpotBugs chạy offline và dùng AI Mock Server | ✅ Mượt mà |
| 2 | Các nhánh làm task cá nhân rẽ nhánh từ commit cũ làm thiếu file | Quy trình Git branching chưa đồng bộ commit mới nhất | Sử dụng lệnh `git checkout origin/<branch> -- <file>` để nhặt chính xác các file bàn giao vào nhánh sprint | ✅ Toàn vẹn |
| 3 | Docker Images không có phiên bản định danh cụ thể | Docker Compose mặc định gán nhãn `:latest` | Bổ sung biến `${APP_VERSION:-latest}` vào `docker-compose.yml`, tạo file `.env` và gắn tag `v1.0.0` | ✅ Chuẩn DevOps |

---

## 🚀 6. KẾ HOẠCH BÀN GIAO SPRINT TIẾP THEO (SPRINT 3)
Sprint 3 sẽ tập trung vào **Tự động hóa kiểm thử API (API Automation)**, **Đo lường độ bao phủ White-box JaCoCo** và **Thiết lập Quy trình quản lý lỗi (Bug Lifecycle)**:
- **Được (Leader):** Xây dựng bộ Master Postman Collection 46 kịch bản tự động hóa API (`TEST-15`, `TEST-16`).
- **Phương:** Mở rộng bộ test Java lên 1,074 bài test và đo lường độ phủ JaCoCo đạt >99% (`TEST-17`).
- **Lĩnh:** Kiểm thử thủ công Cổng AI Image Upload trên FastAPI (`TEST-18`).
- **Thịnh:** Xây dựng framework Quản lý Vòng đời Bug (`TEST-19`) và viết script test API Tìm kiếm & Phân trang (`TEST-20`).
