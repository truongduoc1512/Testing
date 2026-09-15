# 📊 BÁO CÁO TỔNG HỢP TIẾN ĐỘ - TUẦN 4
> **Dự án:** ShoeShop Testing & Quality Assurance System  
> **Sprint Jira:** Sprint 4 — UI Automation, Cross-browser & Integration Testing  
> **Thời gian:** 17/08/2026 – 24/08/2026  
> **Người tổng hợp (Leader):** Trương Hoài Được  
> **Nhánh tích hợp chính:** `week/week-4-ui-integration` ➔ `develop`  
> **Phiên bản Release Tag:** `v4.0.0`  

---

## 🎯 1. MỤC TIÊU SPRINT (SPRINT OBJECTIVES)
- [x] **Mục tiêu 1:** Thiết lập kiến trúc Kiểm thử tự động giao diện (UI Automation) chuẩn mực áp dụng mẫu thiết kế **Page Object Model (POM)** bằng Java Selenium WebDriver (`TEST-22`).
- [x] **Mục tiêu 2:** Tự động hóa kiểm thử luồng Người dùng Mua hàng trọn vẹn (End-to-End Checkout Journey), xử lý triệt để tình trạng Flaky Test bằng cơ chế Explicit Wait (`WebDriverWait`) (`TEST-23`).
- [x] **Mục tiêu 3:** Xây dựng môi trường Kiểm thử Tích hợp (Integration Testing) với CSDL thật thông qua thư viện Docker **Testcontainers**, kiểm chứng tính toàn vẹn giao dịch và cơ chế `@Transactional` Rollback (`TEST-24`).
- [x] **Mục tiêu 4:** Tự động hóa kiểm thử chéo đa trình duyệt (Cross-browser Testing) bằng Python Selenium trên 5 profile trình duyệt: Google Chrome, Mozilla Firefox, Microsoft Edge, Brave và Chromium (`TEST-25`).
- [x] **Mục tiêu 5:** Xây dựng quy trình Kiểm thử hồi quy tự động (Regression / Retest Resolved Bugs) xác thực 4 lỗi nghiệp vụ trọng yếu trong vòng đời phát triển (`TEST-26`).
- [x] **Mục tiêu 6:** Đóng gói toàn bộ tập kịch bản API Automation bằng công cụ Newman CLI, tự động xuất báo cáo giao diện HTML trực quan (`newman-reporter-htmlextra`) (`TEST-27`).
- [x] **Mục tiêu 7:** Đóng gói bản phát hành DevOps: Giải quyết xung đột `pom.xml`, gắn tag phiên bản `v4.0.0` và tích hợp nhánh tổng vào `develop`.

---

## 📋 2. BẢNG TỔNG HỢP THỰC THI TASK (JIRA & GITHUB)

| Mã Task Jira | Tên công việc (Summary) | Người thực hiện | Điểm SP | Trạng thái | Pull Request / Nhánh Git | Phân loại |
| :--- | :--- | :--- | :---: | :---: | :--- | :---: |
| `TEST-22` | UI automation for authentication (POM) | Lĩnh | 5 | ✅ Done | [PR test/w4-TEST-22-ui-automation-auth](https://github.com/truongduoc1512/Testing/tree/test/w4-TEST-22-ui-automation-auth) | `test` |
| `TEST-23` | Develop checkout journey automation | Lĩnh | 5 | ✅ Done | [PR test/w4-TEST-23-ui-automation-checkout](https://github.com/truongduoc1512/Testing/tree/test/w4-TEST-23-ui-automation-checkout) | `test` |
| `TEST-24` | Integration Testing with Testcontainers | Hoàng Phương | 8 | ✅ Done | [PR test/w4-TEST-24-integration-testing](https://github.com/truongduoc1512/Testing/tree/test/w4-TEST-24-integration-testing) | `test` |
| `TEST-25` | Cross-browser testing on 5 browsers | Ngọc Thịnh | 5 | ✅ Done | [PR feat/w4-TEST-25-cross-browser-testing](https://github.com/truongduoc1512/Testing/tree/feat/w4-TEST-25-cross-browser-testing) | `test` |
| `TEST-26` | Retest resolved bugs & regression verification | Ngọc Thịnh | 5 | ✅ Done | [PR feat/w4-TEST-26-retest-resolved-bugs](https://github.com/truongduoc1512/Testing/tree/feat/w4-TEST-26-retest-resolved-bugs) | `test` |
| `TEST-27` | Package Newman scripts & HTML report generator | Hoài Được | 3 | ✅ Done | [PR feat/w4-TEST-27-package-newman-scripts](https://github.com/truongduoc1512/Testing/tree/feat/w4-TEST-27-package-newman-scripts) | `feat` |
| `TEST-28` | Submit Week 4 summary report & DevOps release `v4.0.0` | Tất cả | 2 | ✅ Done | [PR week/week-4-ui-integration](https://github.com/truongduoc1512/Testing/tree/week/week-4-ui-integration) | `docs` |

---

## 📈 3. THỐNG KÊ CHỈ SỐ SPRINT (METRICS & KPIS)

| Chỉ số đo lường (Metric) | Kế hoạch cam kết | Kết quả thực tế | Tỷ lệ hoàn thành | Đánh giá |
| :--- | :---: | :---: | :---: | :---: |
| **Tổng số Task Jira cam kết** | 7 tasks | 7 tasks | **100%** | 🟢 Đạt chuẩn |
| **Tổng Story Points hoàn thành** | 33 SP | 33 SP | **100%** | 🟢 Đạt chuẩn |
| **Số lượng Trình duyệt hỗ trợ Cross-browser** | 3 trình duyệt | **5 trình duyệt (Chrome, Firefox, Edge, Brave, Chromium)** | **167%** | 🟢 Vượt cam kết |
| **Tỷ lệ Pass của UI Automation Test (POM)** | 100% | **100% (Loại bỏ hoàn toàn Fake Pass)** | **Đạt chuẩn** | 🟢 Chất lượng cao |
| **Số lớp Integration Tests Testcontainers** | 3 classes | **5 classes chuyên sâu** | **166%** | 🟢 Rất tốt |
| **Số kịch bản Bug hồi quy được Retest tự động** | 4 bugs | **4 bugs cốt lõi (100% Verified)** | **100%** | 🟢 Đạt chuẩn |
| **Báo cáo HTML Newman tự động sinh** | 1 report | **`target/newman-report.html` (46 APIs)** | **100%** | 🟢 Đạt chuẩn |
| **Phiên bản Release Tag đóng gói** | `v4.0.0` | **`v4.0.0` trên GitHub** | **100%** | 🟢 Thành công |

---

## 🔍 4. CHI TIẾT SẢN PHẨM BÀN GIAO & MINH CHỨNG THEO THÀNH VIÊN

### 1. Trương Hoài Được (Leader) — Thực hiện `TEST-27`, `DevOps Release v4.0.0`
- **Sản phẩm bàn giao:**
  * Script PowerShell wrapper tự động [`scripts/run-api-tests.ps1`](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/scripts/run-api-tests.ps1) tự động khởi tạo thư mục báo cáo, tải và thực thi Newman CLI cùng reporter `newman-reporter-htmlextra` với cờ `--insecure`.
  * Bộ xuất dữ liệu báo cáo HTML trực quan tại `target/newman-report.html` cung cấp Dashboard kiểm thử API trực quan cho 46 endpoints.
  * Tái cấu trúc và giải quyết triệt để xung đột merge conflict file `pom.xml`, đồng bộ phiên bản `v4.0.0` trên toàn bộ hệ sinh thái repository.

### 2. Lĩnh — Thực hiện `TEST-22`, `TEST-23`
- **Sản phẩm bàn giao:**
  * **Nền tảng Page Object Model (POM):** Xây dựng các lớp cơ sở [`BaseUiTest`](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/src/test/java/com/example/demo/ui/BaseUiTest.java) và các Page Classes đại diện cho giao diện: [`LoginPage`](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/src/test/java/com/example/demo/ui/pages/LoginPage.java), `CartPage`, `CheckoutPage`, `ProductDetailPage`.
  * **Bộ kịch bản UI Automation:** 
    - [`AuthenticationUiTest`](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/src/test/java/com/example/demo/ui/AuthenticationUiTest.java): Kiểm thử 6 kịch bản đăng nhập (hợp lệ, sai mật khẩu, username không tồn tại, để trống trường, phân quyền chuyển hướng Admin/User) + Báo cáo [`docs/TEST-22.md`](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/docs/TEST-22.md).
    - [`CheckoutUiTest`](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/src/test/java/com/example/demo/ui/CheckoutUiTest.java): Kiểm thử trọn vẹn luồng E2E Checkout Flow + Báo cáo [`docs/TEST-23.md`](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/docs/TEST-23.md).
  * **Chuẩn hóa kỹ thuật:** Loại bỏ hoàn toàn mã `try-catch(ignored)` gây ra lỗi "Fake Pass", thay thế bằng các điều kiện chờ đồng bộ rõ ràng `ExpectedConditions.elementToBeClickable` và `visibilityOfElementLocated`.

### 3. Hoàng Phương — Thực hiện `TEST-24`
- **Sản phẩm bàn giao:**
  * Xây dựng lớp cơ sở `MySqlIntegrationTestBase` tích hợp thư viện **Testcontainers**, tự động khởi tạo container MySQL tạm thời trên Docker cho từng phiên test, đảm bảo tính vô trùng dữ liệu tuyệt đối (Zero Side-Effects).
  * Phát triển 5 lớp Integration Test chuyên sâu đánh giá tính toàn vẹn của nghiệp vụ giao dịch, kiểm chứng cơ chế `@Transactional` tự động rollback khi gặp lỗi, và kiểm thử tích hợp giữa Spring Boot Backend với AI Microservice.
  * Tài liệu báo cáo chi tiết Integration Testing [`docs/TEST-24.md`](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/docs/TEST-24.md).

### 4. Ngọc Thịnh — Thực hiện `TEST-25`, `TEST-26`
- **Sản phẩm bàn giao:**
  * **Kiểm thử chéo đa trình duyệt (TEST-25):** Viết script Python [`scripts/test_cross_browser.py`](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/scripts/test_cross_browser.py) (260 dòng) tự động phát hiện và khởi chạy 5 profile trình duyệt (Chrome, Firefox, Edge, Brave, Chromium) ở chế độ Headless và Normal, chụp ảnh màn hình nghiệm thu + Báo cáo [`docs/TEST-25.md`](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/docs/TEST-25.md).
  * **Kiểm thử hồi quy Bug (TEST-26):** Viết công cụ Python [`scripts/verify_resolved_bugs.py`](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/scripts/verify_resolved_bugs.py) và tệp dữ liệu [`retest_execution_report.json`](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/retest_execution_report.json) tự động tái hiện và xác nhận 4 bug cốt lõi đã được khắc phục hoàn toàn (BUG-01: Session Cart Crash, BUG-02: Negative Quantity, BUG-03: Voucher Case Sensitivity, BUG-04: Review XSS Injection) + Báo cáo [`docs/TEST-26.md`](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/docs/TEST-26.md).

---

## ⚠️ 5. VẤN ĐỀ PHÁT SINH & GIẢI PHÁP XỬ LÝ (BLOCKERS & RESOLUTIONS)

| STT | Vấn đề phát sinh (Blocker) | Nguyên nhân gốc rễ | Giải pháp kỹ thuật đã xử lý | Kết quả |
| :---: | :--- | :--- | :--- | :---: |
| 1 | Xung đột Merge Conflict nghiêm trọng file `pom.xml` khi tích hợp TEST-22 | Chỉnh sửa và format lại toàn bộ cấu trúc file `pom.xml` làm mất cấu hình JaCoCo của TEST-17 | Revert `pom.xml` về bản gốc chuẩn, chỉ bổ sung thêm các dependencies của Selenium (`selenium-java 4.25.0`) xuống cuối | ✅ Hoàn hảo |
| 2 | Kịch bản UI Test Checkout báo Pass giả (Fake Pass) | Sử dụng `try-catch(Exception e) {}` bọc quanh code và ép lệnh `assertTrue(true)` ở cuối | Áp dụng Explicit `WebDriverWait` kết hợp CSS Selector chính xác; loại bỏ hoàn toàn các khối catch nuốt ngoại lệ | ✅ Thực chất |
| 3 | Lỗi đăng nhập chập chờn (Flaky Login) trên CSDL Docker Volume | Tài khoản test `user1` bị khóa do các ca kiểm thử tiêu cực (Invalid Login Test) chạy trước | Cô lập tài khoản test riêng biệt cho ca kiểm thử âm bản (`invalid_user_test`), giữ tài khoản mẫu nguyên vẹn | ✅ Ổn định |

---

## 🚀 6. KẾ HOẠCH BÀN GIAO SPRINT TIẾP THEO (SPRINT 5)
Trong Tuần 5 (Sprint 5), đội ngũ sẽ tập trung toàn lực vào **Chuẩn hóa học thuật theo lý thuyết kiểm thử phần mềm (Theory Alignment)**:
- **Chuẩn hóa Kiểm thử Hộp đen:** Bổ sung và ánh xạ toàn bộ các kỹ thuật Phân hoạch tương đương (EP), Phân tích giá trị biên (BVA 4n+1, 6n+1, 5ⁿ), Bảng quyết định (Decision Table) và Bảng chuyển đổi trạng thái vào hệ thống tài liệu và code.
- **Chuẩn hóa Kiểm thử Hộp trắng:** Xây dựng Đồ thị luồng điều khiển (CFG), tính toán độ phức tạp Cyclomatic V(G) và đối chiếu chéo với chỉ số đo lường thực tế của JaCoCo.
- **Áp dụng Kiểm thử theo Kinh nghiệm:** Phân tích kỹ thuật Error Guessing và Exploratory Testing áp dụng trên phân hệ AI Computer Vision.
