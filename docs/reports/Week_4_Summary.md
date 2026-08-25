# 📊 BÁO CÁO TỔNG HỢP TIẾN ĐỘ - TUẦN 4
> **Dự án:** ShoeShop Testing & Development  
> **Sprint Jira:** Sprint 4 - UI Automation & Integration Testing  
> **Thời gian:** 17/08/2026 - 24/08/2026  
> **Người tổng hợp (Leader):** Trương Hoài Được  

---

## 🎯 1. MỤC TIÊU TUẦN
- [x] **Mục tiêu 1:** Thiết lập môi trường Integration Test với CSDL thật (Testcontainers MySQL) và kiểm thử các luồng giao dịch/Rollback (`TEST-24`).
- [x] **Mục tiêu 2:** Tự động hóa kiểm thử đa trình duyệt (Cross-browser Testing) bằng Python Selenium trên 5 profile trình duyệt (`TEST-25`).
- [x] **Mục tiêu 3:** Xây dựng quy trình Retest các bug đã fix (Regression Testing) đảm bảo không tái phát lỗi cũ (`TEST-26`).
- [x] **Mục tiêu 4:** Thiết lập nền tảng Automation Test UI với Page Object Model (POM) và kiểm thử module Authentication (`TEST-22`).
- [x] **Mục tiêu 5:** Kiểm thử tự động E2E luồng mua hàng (Checkout Journey) (`TEST-23`).
- [x] **Mục tiêu 6:** Đóng gói bộ test API Postman bằng Newman CLI và cấu hình môi trường tự động sinh báo cáo HTML (`TEST-27`).

---

## 📋 2. BẢNG TỔNG HỢP THỰC THI TASK (JIRA & GITHUB)

| Mã Task Jira | Tên công việc | Người thực hiện | Trạng thái Jira | Nhánh Git (Branch Name) | Loại Task |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `TEST-24` | Integration Testing | Phương | ✅ Done | `test/w4-TEST-24-integration-testing` | `test` |
| `TEST-25` | Cross-browser testing | Thịnh | ✅ Done | `feat/w4-TEST-25-cross-browser-testing` | `test` |
| `TEST-26` | Retest resolved bugs | Thịnh | ✅ Done | `feat/w4-TEST-26-retest-resolved-bugs` | `test` |
| `TEST-22` | Develop UI automation for authentication | Lĩnh | ✅ Done | `test/w4-TEST-22-ui-automation-auth` | `test` |
| `TEST-23` | Develop checkout automation | Lĩnh | ✅ Done | `test/w4-TEST-23-ui-automation-checkout` | `test` |
| `TEST-27` | Package Newman scripts | Hoài Được | ✅ Done | `feat/w4-TEST-27-package-newman-scripts` | `feat` |

---

## 📈 3. THỐNG KÊ CHỈ SỐ TUẦN (METRICS)

- **Tổng số Task cam kết:** 6 tasks
- **Số Task hoàn thành (Done):** 6 / 6 tasks (100%)
- **Số Unit/Integration Tests mới:** 5 lớp Test tích hợp (Kiểm chứng Rollback `@Transactional` và Testcontainers).
- **Môi trường Cross-browser hỗ trợ:** 5 trình duyệt (Chrome, Firefox, Edge, Brave, Chromium).
- **Số lượng Bug cũ được Retest:** 4 kịch bản hồi quy cốt lõi.
- **Tỷ lệ Automation Test UI:** 100% Pass Rate trên luồng Auth và Checkout sử dụng POM.
- **Tỷ lệ API Test bằng Newman CLI:** Chạy thành công toàn bộ 46 APIs sinh report HTML.

---

## 🔍 4. MINH CHỨNG NGHỆM THU THEO THÀNH VIÊN

### Hoài Được — Task: `TEST-27`
- **Sản phẩm bàn giao:** 
  * Cấu hình biến môi trường Postman tự động thông qua `docs/Shoeshop_Postman_Environment.json`.
  * Script Wrapper PowerShell `scripts/run-api-tests.ps1` tự động tải `newman` và `newman-reporter-htmlextra` qua npx.
  * Báo cáo HTML tự động sinh tại thư mục `target/newman-report.html`.

### Phương — Task: `TEST-24`
- **Sản phẩm bàn giao:** 
  * Xây dựng nền tảng Integration Test với `MySqlIntegrationTestBase` sử dụng Docker Testcontainers, đảm bảo môi trường Database độc lập và vô trùng.
  * 5 Lớp Integration Test chuyên sâu đánh giá độ toàn vẹn giao dịch (Transaction Rollback) và kết nối với Service AI (Mock/Thực tế).
  * Report xác thực việc chia tách Business Logic khỏi Controller xuống Service hoàn toàn đúng đắn.

### Lĩnh — Task: `TEST-22`, `TEST-23`
- **Sản phẩm bàn giao:** 
  * Cài đặt thành công khung kiểm thử Selenium WebDriver UI Test thông qua Maven (`pom.xml`).
  * Khởi tạo kiến trúc Page Object Model (POM) với các class `BaseUiTest`, `LoginPage`, `CartPage`, `CheckoutPage`, `ProductDetailPage`.
  * Hoàn thành 6 kịch bản Test Authentication và Kịch bản Test E2E Checkout Flow.
  * (Đã khắc phục hoàn toàn lỗi lạm dụng `try-catch` và áp dụng Explicit Waits `WebDriverWait` chuyên nghiệp).

### Thịnh — Task: `TEST-25`, `TEST-26`
- **Sản phẩm bàn giao:** 
  * Bộ Script Python kiểm thử chéo đa trình duyệt tự động tải Driver tương ứng: `scripts/cross_browser_test.py`.
  * Công cụ Python xác thực trạng thái vòng đời Bug trên hệ thống Jira mô phỏng: `scripts/verify_resolved_bugs.py` (giúp truy vết 4 kịch bản lỗi hồi quy nổi cộm nhất).

---

## ⚠️ 5. VẤN ĐỀ PHÁT SINH & GIẢI PHÁP (BLOCKERS & SOLUTIONS)

| STT | Vấn đề / Lỗi gặp phải | Nguyên nhân | Giải pháp đã xử lý | Trạng thái |
| :--- | :--- | :--- | :--- | :--- |
| 1 | Merge Conflict toàn bộ file `pom.xml` khi tích hợp TEST-22 vào Week-4 | Chỉnh sửa và format lại toàn bộ cấu trúc file `pom.xml` gốc làm ghi đè các cấu hình cũ của nhánh khác | Revert file `pom.xml` về định dạng gốc, chỉ nối tiếp thêm các dependencies của Selenium xuống cuối file | ✅ Resolved |
| 2 | Kịch bản UI Test của chức năng Checkout (TEST-23) báo "Pass" sai sự thật (Fake Pass) | Sử dụng `try-catch(ignored)` bọc quanh code lỗi và ép lệnh `assertTrue(true)` ở cuối, đồng thời lạm dụng vòng lặp tìm bấm bừa Element | Áp dụng Explicit `WebDriverWait` kết hợp với CSS Selector chính xác để đợi và bấm vào đúng nút đích. Xóa bỏ hoàn toàn code ép Pass | ✅ Resolved |

---

## 🚀 6. KẾ HOẠCH TUẦN TIẾP THEO (WEEK 5 - Theory Alignment & Documentation)

Trong Tuần 5 (Sprint 5) - cũng là tuần cuối cùng của dự án, nhóm sẽ dừng việc code thêm tính năng kiểm thử mới để tập trung toàn lực vào việc **chuẩn hóa học thuật** và **nghiệm thu cuối kỳ**:

- **Chuẩn hóa Lý thuyết Hộp đen (Black-box Testing):** Bổ sung và ánh xạ chính xác các kỹ thuật Phân hoạch lớp tương đương (EP), Phân tích giá trị biên (BVA $4n+1$, $6n+1$, $5^n$), Bảng quyết định và Chuyển đổi trạng thái vào hệ thống tài liệu và code hiện có.
- **Chuẩn hóa Lý thuyết Hộp trắng (White-box Testing):** Xây dựng Đồ thị luồng điều khiển (CFG) và tính toán thủ công độ phức tạp Cyclomatic $V(G)$ để đối chiếu chéo với công cụ JaCoCo.
- **Áp dụng Kiểm thử theo Kinh nghiệm (Experience-Based):** Phân tích các kỹ thuật Error Guessing và Exploratory Testing áp dụng thực tế trên Module AI Computer Vision.
- **Đóng gói & Nghiệm thu:** Hoàn thiện báo cáo tổng kết, hợp nhất nhánh (merge) lên `develop` và đánh tag phiên bản Release cuối cùng (`v4.0.0`) để bàn giao cho Giảng viên.
