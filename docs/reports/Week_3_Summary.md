# 📊 BÁO CÁO TỔNG HỢP TIẾN ĐỘ - TUẦN 3
> **Dự án:** ShoeShop Testing & Development  
> **Sprint Jira:** Sprint 3 - API Automation, JaCoCo Coverage & Bug Lifecycle  
> **Thời gian:** 10/08/2026 - 17/08/2026  
> **Người tổng hợp (Leader):** Trương Hoài Được  

---

## 🎯 1. MỤC TIÊU TUẦN
- [x] **Mục tiêu 1:** Tự động hóa kiểm thử REST API phân hệ User & Product bằng Postman (`TEST-15`).
- [x] **Mục tiêu 2:** Tự động hóa kiểm thử REST API phân hệ Cart, Order, Voucher, Review, Wishlist & Return (`TEST-16`).
- [x] **Mục tiêu 3:** Mở rộng bộ test Java lên 1,074 bài test và đo lường độ phủ White-box Coverage JaCoCo (`Line 99.85%`, `Branch 99.33%`) (`TEST-17`).
- [x] **Mục tiêu 4:** Thực hiện kiểm thử thủ công Cổng AI Image Upload trên FastAPI (`TEST-18`).
- [x] **Mục tiêu 5:** Xây dựng quy trình Quản lý Vòng đời Bug & Tool Python Logger tự động (`TEST-19`).
- [x] **Mục tiêu 6:** Tự động hóa kiểm thử API Tìm kiếm & Phân trang (`TEST-20`).
- [x] **Mục tiêu 7:** Hoàn thiện đóng gói DevOps: Đánh tag phiên bản `v3.0.0`, tối ưu `docker-compose.yml`, tích hợp nhánh `week/week-3-api-automation` vào `develop` và gắn Tag GitHub `v3.0.0` (`TEST-21`).

---

## 📋 2. BẢNG TỔNG HỢP THỰC THI TASK (JIRA & GITHUB)

| Mã Task Jira | Tên công việc | Người thực hiện | Trạng thái Jira | Nhánh Git (Branch Name) | Loại Task |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `TEST-15` | Automate User & Product APIs | Hoài Được | ✅ Done | `feat/w3-TEST-15-automate-user-product-apis` | `feat` |
| `TEST-16` | Automate Cart & Order APIs | Hoài Được | ✅ Done | `feat/w3-TEST-15-automate-user-product-apis` | `feat` |
| `TEST-17` | Measure white-box coverage | Phương | ✅ Done | `test/w3-TEST-17-measure-white-box-coverage` | `test` |
| `TEST-18` | Manual AI upload testing | Lĩnh | ✅ Done | `test/w3-TEST-18-manual-ai-upload-testing` | `test` |
| `TEST-19` | Manage bug lifecycle | Thịnh | ✅ Done | `feat/w3-TEST-19-manage-bug-lifecycle` | `feat` |
| `TEST-20` | Test search & pagination APIs | Thịnh | ✅ Done | `feat/w3-TEST-20-test-search-pagination-apis` | `feat` |
| `TEST-21` | Submit Week 3 summary report & merge | Tất cả thành viên | ✅ Done | `week/week-3-api-automation` | `docs` |

---

## 📈 3. THỐNG KÊ CHỈ SỐ TUẦN (METRICS)

- **Tổng số Task cam kết:** 7 tasks
- **Số Task hoàn thành (Done):** 7 / 7 tasks (100%)
- **Tổng Story Points hoàn thành:** 31 points
- **Số kịch bản API Test tự động bằng Postman:** 46 kịch bản (`Shoeshop_API_Collection.json` — 100% Pass Rate)
- **Số bài Unit & Integration Test Java:** 1,074 bài test (34 test files)
- **Tỷ lệ bao phủ dòng code (Line Coverage):** **99.85%** (3,372 / 3,377 lines — Vượt mốc 70%)
- **Tỷ lệ bao phủ nhánh rẽ (Branch Coverage):** **99.33%** (1,776 / 1,788 branches — Vượt mốc 65%)
- **Số kịch bản API Tìm kiếm & Phân trang (Python):** 10 test cases (`test_search_pagination_api.py`)
- **Phiên bản Release Tag đóng gói:** `v3.0.0`
- **Số Pull Request / Branch đã Merge:** 6 Branches

---

## 🔍 4. MINH CHỨNG NGHỆM THU THEO THÀNH VIÊN

### Hoài Được — Task: `TEST-15`, `TEST-16`, `TEST-21`
- **Sản phẩm bàn giao:** 
  * Bộ Master E2E Postman Collection `Shoeshop_API_Collection.json` (1,786 dòng JSON, 46 API Test Cases).
  * Tài liệu báo cáo tự động hóa API [docs/TEST-15.md](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/docs/TEST-15.md).
  * Khắc phục lỗi Spring Security `/admin/logout` (AntPathRequestMatcher) và tối ưu cấu hình `docker-compose.yml` (`restart: unless-stopped`).
  * Thực thi quy trình đóng gói DevOps, đánh tag release `v3.0.0`, merge `week/week-3-api-automation` vào `develop` và gắn Tag `v3.0.0` trên GitHub.

### Phương — Task: `TEST-17`
- **Sản phẩm bàn giao:** 
  * Bộ 34 file Java Test chứa 1,074 Unit & Integration Tests bao phủ Controller, REST API, Service, DAO, Entity, Form và Utility.
  * Script PowerShell tự động đo lường độ phủ cô lập CSDL tạm [`scripts/test-coverage.ps1`](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/scripts/test-coverage.ps1) (281 dòng).
  * Tài liệu báo cáo JaCoCo White-box Coverage [docs/TEST-17.md](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/docs/TEST-17.md) (`Line: 99.85%`, `Branch: 99.33%`).

### Lĩnh — Task: `TEST-18`
- **Sản phẩm bàn giao:** 
  * Báo cáo kiểm thử thủ công Cổng AI Upload (`POST /api/v1/analyze`) trên FastAPI AI Service.
  * Kịch bản nghiệm thu kiểm tra định dạng rác (`.MOV`), bóc tách object (`umbrella`) và duyệt ảnh giày (`product_item`).
  * Tài liệu báo cáo nghiệm thu [docs/test-results/TEST-18.md](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/docs/test-results/TEST-18.md).

### Thịnh — Task: `TEST-19`, `TEST-20`
- **Sản phẩm bàn giao:** 
  * Ma trận Vòng đời Bug (Bug Lifecycle Framework 6 bước) + Tool Python [`ai-service/app/bug_logger.py`](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/ai-service/app/bug_logger.py) + Template JSON [`bugs_report_template.json`](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/bugs_report_template.json) + Báo cáo [docs/TEST-19.md](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/docs/TEST-19.md).
  * Bộ kịch bản và script Python tự động kiểm thử API Tìm kiếm & Phân trang [`scripts/test_search_pagination_api.py`](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/scripts/test_search_pagination_api.py) + Postman Collection `TEST_SEARCH_PAGINATION_COLLECTION.json` + Báo cáo [docs/TEST-20.md](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/docs/TEST-20.md).

---

## ⚠️ 5. VẤN ĐỀ PHÁT SINH & GIẢI PHÁP (BLOCKERS & SOLUTIONS)

| STT | Vấn đề / Lỗi gặp phải | Nguyên nhân | Giải pháp đã xử lý | Trạng thái |
| :--- | :--- | :--- | :--- | :--- |
| 1 | Lỗi Whitelabel Error Page (404) khi người dùng bấm "Đăng xuất" trên giao diện | Spring Security bật CSRF bắt buộc `/admin/logout` phải dùng phương thức HTTP POST, trong khi menu HTML gửi HTTP GET | Cập nhật [WebSecurityConfig.java](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/src/main/java/com/example/demo/config/WebSecurityConfig.java) dùng `logoutRequestMatcher(new AntPathRequestMatcher("/admin/logout"))` hỗ trợ cả GET & POST | ✅ Resolved |
| 2 | Container `shoeshop-api` bị lặp Crash Loop vô hạn (hiện logo SPRING liên tục) mỗi khi mở Docker Desktop | Do đặt `restart: always` và Spring Boot khởi chạy trước khi MySQL đạt trạng thái Healthy hoàn toàn | Đổi thành `restart: unless-stopped`, thêm timeout JDBC và bổ sung tự động nạp `seed_data.sql` vào script [scripts/start-test-env.ps1](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/scripts/start-test-env.ps1) | ✅ Resolved |
| 3 | Postman Runner báo 3 kịch bản FAILED liên quan đến sản phẩm mẫu `P5593` | CSDL chưa được nạp tập dữ liệu mẫu `seed_data.sql` khiến sản phẩm không tồn tại và giỏ hàng bị trống khi checkout | Nạp `seed_data.sql` vào CSDL Docker và tích hợp nạp dữ liệu tự động ngay khi CSDL vừa bật | ✅ Resolved |

---

## 🚀 6. KẾ HOẠCH TUẦN TIẾP THEO (WEEK 4 - UI Automation & Integration Testing)

Trong Tuần 4 (Sprint 4), toàn bộ đội ngũ sẽ chuyển trọng tâm sang Kiểm thử tự động Giao diện (UI) và Kiểm thử tích hợp hệ thống (Integration Testing). Các mục tiêu trọng điểm bao gồm:

- **Thiết lập nền tảng Automation Test UI:** Áp dụng mô hình **Page Object Model (POM)** bằng Selenium WebDriver cho các phân hệ cốt lõi (Authentication & Checkout) (`TEST-22`, `TEST-23`).
- **Kiểm thử chéo đa trình duyệt (Cross-browser Testing):** Viết script Python tự động kiểm thử UI trên 5 profile trình duyệt khác nhau (Chrome, Firefox, Edge, Brave, Chromium) (`TEST-25`).
- **Integration Testing & Testcontainers:** Kiểm chứng độ toàn vẹn giao dịch (Transaction Rollback) với CSDL thật độc lập (`TEST-24`).
- **Kiểm thử Hồi quy (Regression/Retest):** Xây dựng quy trình tự động xác thực trạng thái vòng đời Bug để đảm bảo không tái phát các lỗi đã đóng (`TEST-26`).
- **Đóng gói API Automation:** Viết script Wrapper chạy Postman Collection bằng Newman CLI và tự động xuất HTML Report (`TEST-27`).
