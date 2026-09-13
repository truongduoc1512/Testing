# 📊 BÁO CÁO TỔNG HỢP TIẾN ĐỘ - TUẦN 3
> **Dự án:** ShoeShop Testing & Quality Assurance System  
> **Sprint Jira:** Sprint 3 — API Automation, JaCoCo Coverage & Bug Lifecycle  
> **Thời gian:** 10/08/2026 – 17/08/2026  
> **Người tổng hợp (Leader):** Trương Hoài Được  
> **Nhánh tích hợp chính:** `week/week-3-api-automation` ➔ `develop`  
> **Phiên bản Release Tag:** `v3.0.0`  

---

## 🎯 1. MỤC TIÊU SPRINT (SPRINT OBJECTIVES)
- [x] **Mục tiêu 1:** Tự động hóa kiểm thử REST API cho phân hệ Người dùng (Authentication, Profile, Address) và Danh mục sản phẩm (`TEST-15`).
- [x] **Mục tiêu 2:** Tự động hóa kiểm thử REST API cho phân hệ Giỏ hàng, Đơn hàng, Voucher giảm giá, Đánh giá sản phẩm và Hủy/Đổi trả hàng (`TEST-16`).
- [x] **Mục tiêu 3:** Mở rộng toàn diện bộ test Java lên **1.074 bài kiểm thử** (34 file test), đo lường độ phủ White-box Coverage bằng JaCoCo đạt ngưỡng kỷ lục: **Line 99.85%**, **Branch 99.33%** (`TEST-17`).
- [x] **Mục tiêu 4:** Thực hiện kiểm thử thủ công Cổng AI Computer Vision Upload (`POST /api/v1/analyze`) trên nền FastAPI và YOLOv8 (`TEST-18`).
- [x] **Mục tiêu 5:** Xây dựng Khung quản lý vòng đời Bug (Bug Lifecycle Framework 6 trạng thái) và công cụ tự động bắt lỗi runtime `BugLogger` bằng Python (`TEST-19`).
- [x] **Mục tiêu 6:** Tự động hóa kiểm thử API Tìm kiếm & Phân trang sản phẩm bằng script Python, áp dụng kỹ thuật kiểm thử tổ hợp Worst-Case (5² = 25 cases) (`TEST-20`).
- [x] **Mục tiêu 7:** Đóng gói bản phát hành DevOps: Khắc phục lỗi CSRF logout, gắn tag phiên bản `v3.0.0` và tích hợp vào nhánh `develop` (`TEST-21`).

---

## 📋 2. BẢNG TỔNG HỢP THỰC THI TASK (JIRA & GITHUB)

| Mã Task Jira | Tên công việc (Summary) | Người thực hiện | Điểm SP | Trạng thái | Pull Request / Nhánh Git | Phân loại |
| :--- | :--- | :--- | :---: | :---: | :--- | :---: |
| `TEST-15` | Automate User & Product APIs (Postman) | Hoài Được | 5 | ✅ Done | [PR feat/w3-TEST-15-automate-user-product-apis](https://github.com/truongduoc1512/Testing/tree/feat/w3-TEST-15-automate-user-product-apis) | `feat` |
| `TEST-16` | Automate Cart, Order, Voucher & Review APIs | Hoài Được | 5 | ✅ Done | [PR feat/w3-TEST-15-automate-user-product-apis](https://github.com/truongduoc1512/Testing/tree/feat/w3-TEST-15-automate-user-product-apis) | `feat` |
| `TEST-17` | Measure white-box coverage (JaCoCo Quality Gate) | Hoàng Phương | 8 | ✅ Done | [PR test/w3-TEST-17-measure-white-box-coverage](https://github.com/truongduoc1512/Testing/tree/test/w3-TEST-17-measure-white-box-coverage) | `test` |
| `TEST-18` | Manual AI upload testing (YOLOv8 Vision Gate) | Lĩnh | 3 | ✅ Done | [PR test/w3-TEST-18-manual-ai-upload-testing](https://github.com/truongduoc1512/Testing/tree/test/w3-TEST-18-manual-ai-upload-testing) | `test` |
| `TEST-19` | Manage bug lifecycle & automated bug logger | Ngọc Thịnh | 5 | ✅ Done | [PR feat/w3-TEST-19-manage-bug-lifecycle](https://github.com/truongduoc1512/Testing/tree/feat/w3-TEST-19-manage-bug-lifecycle) | `feat` |
| `TEST-20` | Test search & pagination APIs (Worst-Case 5ⁿ) | Ngọc Thịnh | 3 | ✅ Done | [PR feat/w3-TEST-20-test-search-pagination-apis](https://github.com/truongduoc1512/Testing/tree/feat/w3-TEST-20-test-search-pagination-apis) | `feat` |
| `TEST-21` | Submit Week 3 summary report & DevOps release `v3.0.0` | Tất cả | 2 | ✅ Done | [PR week/week-3-api-automation](https://github.com/truongduoc1512/Testing/tree/week/week-3-api-automation) | `docs` |

---

## 📈 3. THỐNG KÊ CHỈ SỐ SPRINT (METRICS & KPIS)

| Chỉ số đo lường (Metric) | Kế hoạch cam kết | Kết quả thực tế | Tỷ lệ hoàn thành | Đánh giá |
| :--- | :---: | :---: | :---: | :---: |
| **Tổng số Task Jira cam kết** | 7 tasks | 7 tasks | **100%** | 🟢 Đạt chuẩn |
| **Tổng Story Points hoàn thành** | 31 SP | 31 SP | **100%** | 🟢 Đạt chuẩn |
| **Số kịch bản API Test tự động (Postman)** | 35 requests | **46 kịch bản (100% Pass)** | **131%** | 🟢 Tuyệt vời |
| **Tổng số bài Unit & Integration Tests Java** | 800 tests | **1.074 bài test (34 files)** | **134%** | 🟢 Kỷ lục |
| **Line Coverage (Tỷ lệ bao phủ dòng)** | > 70.0% | **99.85%** (3.372 / 3.377 lines) | **Vượt xa chuẩn** | 🟢 Xuất sắc |
| **Branch Coverage (Tỷ lệ bao phủ nhánh)** | > 65.0% | **99.33%** (1.776 / 1.788 branches) | **Vượt xa chuẩn** | 🟢 Xuất sắc |
| **Instruction Coverage (Bao phủ chỉ lệnh)** | N/A | **99.86%** (14.508 / 14.528 inst.) | **Vượt chuẩn** | 🟢 Xuất sắc |
| **Method Coverage (Bao phủ hàm)** | N/A | **100.0%** (782 / 782 methods) | **Tuyệt đối** | 🟢 Hoàn hảo |
| **Class Coverage (Bao phủ lớp đối tượng)** | N/A | **100.0%** (69 / 69 classes) | **Tuyệt đối** | 🟢 Hoàn hảo |
| **Phiên bản Release Tag đóng gói** | `v3.0.0` | **`v3.0.0` trên GitHub** | **100%** | 🟢 Thành công |

---

## 🔍 4. CHI TIẾT SẢN PHẨM BÀN GIAO & MINH CHỨNG THEO THÀNH VIÊN

### 1. Trương Hoài Được (Leader) — Thực hiện `TEST-15`, `TEST-16`, `TEST-21`
- **Sản phẩm bàn giao:**
  * Bộ Master Postman Collection [`docs/Shoeshop_API_Collection.json`](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/docs/Shoeshop_API_Collection.json) gồm 1.786 dòng JSON chứa 46 kịch bản kiểm thử API chi tiết có pre-request scripts và test scripts kiểm tra status code, response time, JSON schema và data assertions.
  * Bộ Environment Variables Postman [`docs/Shoeshop_Postman_Environment.json`](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/docs/Shoeshop_Postman_Environment.json).
  * Tài liệu báo cáo tự động hóa API [`docs/TEST-15.md`](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/docs/TEST-15.md).
  * Khắc phục lỗi Spring Security `/admin/logout` (AntPathRequestMatcher), tối ưu container restart policy (`unless-stopped`), merge nhánh và gắn tag `v3.0.0` trên GitHub.

### 2. Hoàng Phương — Thực hiện `TEST-17`
- **Sản phẩm bàn giao:**
  * Phát triển bộ 34 file Java Test chứa **1.074 Unit & Integration Tests**, bao phủ toàn bộ Controller, REST API, Service, DAO, Entity, Form và Utility.
  * Script PowerShell tự động đo lường độ bao phủ cô lập CSDL tạm [`scripts/test-coverage.ps1`](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/scripts/test-coverage.ps1) (281 dòng) tự động khởi tạo DB riêng, chạy kiểm thử, đọc counter XML của JaCoCo và xuất bảng tỷ lệ phần trăm trực quan.
  * Tài liệu báo cáo chi tiết JaCoCo White-box Coverage [`docs/TEST-17.md`](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/docs/TEST-17.md).

### 3. Lĩnh — Thực hiện `TEST-18`
- **Sản phẩm bàn giao:**
  * Thực hiện kiểm thử hộp đen thủ công đối với Cổng AI Computer Vision (`POST /api/v1/analyze`).
  * Thực nghiệm kiểm tra định dạng rác (video `.MOV`, file text `.txt`), kiểm tra khả năng phát hiện vật thể lạ (dù `umbrella`, xe hơi `car`) và phát hiện vật phẩm giày hợp lệ (`product_item`) với ngưỡng tự tin `confidence_score >= 0.65`.
  * Tài liệu báo cáo nghiệm thu [`docs/test-results/TEST-18.md`](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/docs/test-results/TEST-18.md).

### 4. Ngọc Thịnh — Thực hiện `TEST-19`, `TEST-20`
- **Sản phẩm bàn giao:**
  * **Framework Quản lý Vòng đời Bug:** Xây dựng quy trình 6 bước (Open ➔ In Progress ➔ Resolved ➔ Under Review ➔ Verified / Retest ➔ Closed) kèm tài liệu [`docs/TEST-19.md`](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/docs/TEST-19.md).
  * Module ghi nhận lỗi tự động [`ai-service/app/bug_logger.py`](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/ai-service/app/bug_logger.py) đóng gói Exception kèm Stacktrace, System Context thành file JSON và template [`bugs_report_template.json`](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/bugs_report_template.json).
  * Script Python kiểm thử API Tìm kiếm & Phân trang [`scripts/test_search_pagination_api.py`](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/scripts/test_search_pagination_api.py) áp dụng kỹ thuật tổ hợp Worst-Case BVA (5² = 25 cases) + Postman Collection `TEST_SEARCH_PAGINATION_COLLECTION.json` + Báo cáo [`docs/TEST-20.md`](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/docs/TEST-20.md).

---

## 💻 5. TỔNG HỢP LỆNH CHẠY KIỂM THỬ (TEST COMMANDS)

Bảng tổng hợp toàn bộ các câu lệnh thực thi đo lường độ phủ JaCoCo, kiểm thử API tự động, Worst-Case và AI Gateway của Sprint 3:

| STT | Phân loại | Mục đích kiểm thử | Lệnh thực thi (CLI / Maven / PowerShell) | Môi trường / Điều kiện tiên quyết | Kết quả ghi nhận |
| :---: | :--- | :--- | :--- | :--- | :--- |
| 1 | **White-box** | Đo lường độ bao phủ JaCoCo & sinh báo cáo HTML chi tiết | `mvn clean test jacoco:report` | JDK 17, Maven, MySQL test container | Line: 99.85%, Branch: 99.33% tại `target/site/jacoco/index.html` |
| 2 | **White-box** | Đo độ phủ cách ly bằng CSDL tạm (PowerShell Safe Runner) | `powershell .\scripts\test-coverage.ps1 -OpenReport` | Quyền chạy PowerShell Script | Tự động tạo DB tạm, đo độ phủ và mở báo cáo trên trình duyệt |
| 3 | **Quality Gate** | Kiểm định ngưỡng gác cổng chất lượng độ phủ JaCoCo | `mvn jacoco:check` | Cấu hình rule Line ≥ 70%, Branch ≥ 65% | Build Success: Vượt xa ngưỡng cam kết |
| 4 | **API Testing** | Chạy kiểm thử tự động 46 kịch bản REST API qua Newman CLI | `npx --yes newman run docs/Shoeshop_API_Collection.json -e docs/Shoeshop_Postman_Environment.json -r cli` | Backend Spring Boot (cổng 8080) đang chạy | 46/46 API requests đạt 100% Pass |
| 5 | **Worst-Case BVA** | Kiểm thử API Tìm kiếm & Phân trang tổ hợp biên (5² = 25 cases) | `python .\scripts\test_search_pagination_api.py` | Python 3.x, Backend đang chạy | 25/25 requests thành công, không phát sinh HTTP 500 |
| 6 | **Manual AI** | Kiểm thử Cổng AI Vision Upload giả lập phân tích ảnh giày | `curl -X POST "http://localhost:8000/api/v1/analyze" -H "accept: application/json" -H "Content-Type: multipart/form-data" -F "file=@docs/test-assets/shoe_sample.jpg"` | Microservice AI FastAPI (cổng 8000) | Nhận diện đúng `product_item`, confidence ≥ 0.65 |
| 7 | **DevOps** | Đóng gói và gắn nhãn Release Milestone `v3.0.0` | `git tag -a v3.0.0 -m "Release v3.0.0" && git push origin v3.0.0` | Nhánh sạch sau khi fix lỗi CSRF logout | Gắn tag `v3.0.0` thành công trên GitHub |

---

## ⚠️ 6. VẤN ĐỀ PHÁT SINH & GIẢI PHÁP XỬ LÝ (BLOCKERS & RESOLUTIONS)

| STT | Vấn đề phát sinh (Blocker) | Nguyên nhân gốc rễ | Giải pháp kỹ thuật đã xử lý | Kết quả |
| :---: | :--- | :--- | :--- | :--- :---: |
| 1 | Lỗi 404 Whitelabel khi bấm nút "Đăng xuất" trên giao diện Web | Spring Security bật CSRF bắt buộc `/admin/logout` phải gửi POST, trong khi menu HTML gửi GET | Cập nhật `WebSecurityConfig.java` dùng `logoutRequestMatcher(new AntPathRequestMatcher("/admin/logout"))` hỗ trợ cả 2 method | ✅ Triệt để |
| 2 | Container `shoeshop-api` bị Crash Loop lặp lại khi mở Docker Desktop | Đặt `restart: always` và app boot trước khi MySQL sẵn sàng | Đổi thành `restart: unless-stopped`, thêm timeout JDBC và nạp tự động `seed_data.sql` | ✅ Ổn định |
| 3 | Postman Runner báo 3 kịch bản FAILED liên quan đến sản phẩm `P5593` | CSDL chưa nạp dữ liệu mẫu khiến sản phẩm không tồn tại | Viết script tự động nạp `seed_data.sql` ngay khi bật container MySQL | ✅ 100% Pass |

---

## 🚀 7. KẾ HOẠCH BÀN GIAO SPRINT TIẾP THEO (SPRINT 4)
Sprint 4 sẽ tập trung vào **Kiểm thử tự động giao diện (UI Automation)** và **Kiểm thử tích hợp chuyên sâu (Integration Testing)**:
- **Lĩnh:** Áp dụng mô hình **Page Object Model (POM)** với Selenium WebDriver cho luồng Authentication (`TEST-22`) và Checkout (`TEST-23`).
- **Phương:** Xây dựng khung kiểm thử Integration Test với Docker Testcontainers và kiểm chứng Transaction Rollback (`TEST-24`).
- **Thịnh:** Kiểm thử chéo đa trình duyệt (Cross-browser) bằng Python Selenium (`TEST-25`) và tự động hóa kiểm thử hồi quy Bug (`TEST-26`).
- **Được (Leader):** Đóng gói bộ test API chạy qua Newman CLI và xuất báo cáo HTML (`TEST-27`).
