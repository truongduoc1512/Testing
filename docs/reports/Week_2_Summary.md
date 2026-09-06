# 📊 BÁO CÁO TỔNG HỢP TIẾN ĐỘ - TUẦN 2
> **Dự án:** ShoeShop Testing & Development  
> **Sprint Jira:** Sprint 2 - Test Case Design, Database Seed Data & Unit Testing  
> **Thời gian:** 04/08/2026 - 10/08/2026  
> **Người tổng hợp (Leader):** Trương Hoài Được  

---

## 🎯 1. MỤC TIÊU TUẦN
- [x] **Mục tiêu 1:** Thiết kế kịch bản kiểm thử hộp đen REST API Xác thực (Auth API Test Cases - Kỹ thuật EP & BVA).
- [x] **Mục tiêu 2:** Thiết kế kịch bản kiểm thử hộp đen Giỏ hàng & Đơn hàng (Cart & Order Test Cases - Decision Table & State Transition Diagram).
- [x] **Mục tiêu 3:** Phát triển bộ Unit Test kiểm thử các lớp Form Validator (`CustomerForm`, `ProductForm`, `RegisterForm`).
- [x] **Mục tiêu 4:** Phát triển bộ 10 file Unit & Integration Test kiểm thử toàn bộ tầng Data Access Object (DAO).
- [x] **Mục tiêu 5:** Xây dựng tập dữ liệu mẫu chuẩn (Database Seed Data - `seed_data.sql`) phục vụ kiểm thử.
- [x] **Mục tiêu 6:** Xây dựng AI Mock Server giả lập phản hồi API kiểm duyệt ảnh sản phẩm tự động (`< 10ms`).
- [x] **Mục tiêu 7:** Hoàn thiện quy trình đóng gói DevOps: Đánh tag phiên bản `v1.0.0`, tham số hóa Docker Image và tích hợp nhánh tổng `week/week-2-unit-blackbox`.

---

## 📋 2. BẢNG TỔNG HỢP THỰC THI TASK (JIRA & GITHUB)

| Mã Task Jira | Tên công việc | Người thực hiện | Trạng thái Jira | Pull Request (PR) | Loại Task |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `TEST-8` | Design Auth API test cases | Ngọc Thịnh | ✅ Done | [PR docs/w2-TEST-8-design-auth-api-cases](https://github.com/truongduoc1512/Testing/tree/docs/w2-TEST-8-design-auth-api-cases) | `docs` |
| `TEST-9` | Design Cart & Order test cases | Ngọc Thịnh | ✅ Done | [PR docs/w2-TEST-9-design-cart-order-test-cases](https://github.com/truongduoc1512/Testing/tree/docs/w2-TEST-9-design-cart-order-test-cases) | `docs` |
| `TEST-10` | Develop validator unit tests | Hoàng Phương | ✅ Done | [PR test/w2-TEST-10-validator-unit-tests](https://github.com/truongduoc1512/Testing/tree/test/w2-TEST-10-validator-unit-tests) | `test` |
| `TEST-11` | Develop DAO unit tests | Hoàng Phương | ✅ Done | [PR test/w2-TEST-11-dao-unit-tests](https://github.com/truongduoc1512/Testing/tree/test/w2-TEST-11-dao-unit-tests) | `test` |
| `TEST-12` | Prepare database seed data | Lĩnh | ✅ Done | [PR feat/w2-TEST-12-prepare-database-seed-data](https://github.com/truongduoc1512/Testing/tree/feat/w2-TEST-12-prepare-database-seed-data) | `feat` |
| `TEST-13` | Build AI mock server | Hoài Được | ✅ Done | [PR feat/w2-TEST-13-build-ai-mock-server](https://github.com/truongduoc1512/Testing/tree/feat/w2-TEST-13-build-ai-mock-server) | `feat` |
| `TEST-14` | Submit Week 2 summary report & merge | Tất cả thành viên | ✅ Done | [PR week/week-2-unit-blackbox](https://github.com/truongduoc1512/Testing/tree/week/week-2-unit-blackbox) | `docs` |

---

## 📈 3. THỐNG KÊ CHỈ SỐ TUẦN (METRICS)
- **Tổng số Task cam kết:** 7 tasks
- **Số Task hoàn thành (Done):** 7 / 7 tasks (100%)
- **Tổng Story Points hoàn thành:** 35 points
- **Số dòng code Unit Test Java mới:** 4,199 dòng (Validator Tests & DAO Tests)
- **Số kịch bản Kiểm thử hộp đen thiết kế:** 46 kịch bản (EP, BVA, Decision Table, State Transition)
- **Số bản ghi dữ liệu Seed SQL:** 279 dòng (`seed_data.sql` khởi tạo 6 bảng chính)
- **Phiên bản Release Tag đóng gói:** `v1.0.0`
- **Số Pull Request đã Merge:** 6 PRs

---

## 🔍 4. MINH CHỨNG NGHỆM THU THEO THÀNH VIÊN

### Hoài Được - Task: `TEST-13`, `DevOps Release`
- **Sản phẩm bàn giao:** 
  * Endpoint AI Mock Server trong file [ai-service/app/main.py](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/ai-service/app/main.py) giả lập kiểm duyệt ảnh tức thì (`< 10ms`).
  * Script Python Standalone Server [scripts/mock_ai_server.py](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/scripts/mock_ai_server.py).
  * Tài liệu hướng dẫn vận hành AI Mock [docs/TEST-13.md](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/docs/TEST-13.md).
  * Thực thi quy trình đóng gói DevOps, đánh tag release `v1.0.0` và tham số hóa `docker-compose.yml`.

### Phương - Task: `TEST-10`, `TEST-11`
- **Sản phẩm bàn giao:** 
  * Bộ 3 file Unit Test cho Form Validator (`CustomerFormValidatorTest`, `ProductFormValidatorTest`, `RegisterFormValidatorTest`) + Báo cáo [docs/TEST-10.md](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/docs/TEST-10.md).
  * Bộ 10 file Unit & Integration Test DAO (`AccountDAOTest`, `OrderDAOTest`, `OrderReturnDAOTest`, `ProductDAOTest`, `ProductDAOIntegrationTest`, `ProductReviewDAOTest`, `UserAddressDAOTest`, `VoucherDAOTest`, `WishlistDAOTest`, `DaoTestSupport`) + Báo cáo [docs/TEST-11.md](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/docs/TEST-11.md).
  * File Flyway migration `V16__align_account_identity_lengths.sql`.

### Lĩnh - Task: `TEST-12`
- **Sản phẩm bàn giao:** 
  * File SQL Seed Data [seed_data.sql](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/seed_data.sql) 279 dòng khởi tạo dữ liệu mẫu cho 6 bảng hệ thống (`Accounts`, `Products`, `User_Addresses`, `Wishlist`, `Vouchers`, `Product_Reviews`).

### Ngọc Thịnh - Task: `TEST-8`, `TEST-9`
- **Sản phẩm bàn giao:** 
  * File kịch bản kiểm thử API Xác thực [docs/TEST_CASES_AUTH_API.md](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/docs/TEST_CASES_AUTH_API.md) (167 dòng) áp dụng EP & BVA.
  * File kịch bản kiểm thử Giỏ hàng & Đơn hàng [docs/TEST_CASES_CART_ORDER.md](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/docs/TEST_CASES_CART_ORDER.md) (372 dòng) áp dụng Bảng quyết định & Sơ đồ trạng thái (kèm Mermaid diagram).

---

## ⚠️ 5. VẤN ĐỀ PHÁT SINH & GIẢI PHÁP (BLOCKERS & SOLUTIONS)

| STT | Vấn đề / Lỗi gặp phải | Nguyên nhân | Giải pháp đã xử lý | Trạng thái |
| :--- | :--- | :--- | :--- | :--- |
| 1 | SonarQube Container chiếm RAM quá lớn (2-3GB) gây giật lag máy cá nhân 12GB RAM | Elasticsearch & Java Server của SonarQube ngốn tài nguyên | Gỡ SonarQube khỏi `docker-compose.yml` chính, chuyển sang dùng Checkstyle/SpotBugs offline và AI Mock Server | ✅ Resolved |
| 2 | Các nhánh làm task cá nhân rẽ nhánh từ commit cũ bị lỡ file mới | Git merge mặc định có thể xoá nhầm tài sản | Sử dụng kỹ thuật `git checkout origin/<branch> -- <file>` trực tiếp vào nhánh `week/week-2-unit-blackbox` để bảo toàn 100% dữ liệu | ✅ Resolved |
| 3 | Image Docker không có nhãn phiên bản cụ thể | Mặc định Docker gán tag `:latest` gây khó quản lý | Bổ sung biến `${APP_VERSION:-latest}` vào `docker-compose.yml`, khai báo `APP_VERSION=v1.0.0` trong `.env` và đẩy tag `v1.0.0` lên GitHub | ✅ Resolved |

---

## 🚀 6. KẾ HOẠCH TUẦN TIẾP THEO (WEEK 3: Integration Testing & Test Automation)
- [ ] **Được (Leader):** Execute E2E Postman collection & automated API testing.
- [ ] **Phương:** Develop Integration Tests for REST Controllers & Services.
- [ ] **Lĩnh:** Setup Selenium UI Automation Test suite.
- [ ] **Thịnh:** Configure JMeter Load Testing scripts & performance benchmarks.
