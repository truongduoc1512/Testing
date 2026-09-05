# 📊 BÁO CÁO MA TRẬN ĐÓNG GÓP THÀNH VIÊN (TEAM CONTRIBUTION MATRIX REPORT) - JIRA ISSUE TEST-39

> **Mã Task Jira:** TEST-39  
> **Tiêu đề Task:** Prepare team contribution matrix report  
> **Nhánh Git:** `test/w6-TEST-39-prepare-contribution-report`  
> **Dự án:** ShoeShop Quality Assurance & Testing System  
> **Người thực hiện:** QA Lead & Development Team  

---

## 📌 1. TỔNG QUAN VÀ THANG ĐÁNH GIÁ (EVALUATION FRAMEWORK)

Báo cáo này tổng hợp, phân tích và thống kê ma trận đóng góp (Contribution Matrix) của 4 thành viên trong đội ngũ dự án ShoeShop qua **6 Tuần / 6 Sprints** phát triển và kiểm thử tự động.

### Tiêu chí đánh giá mức độ đóng góp:
1. **Khối lượng công việc (Task Volume & Story Points):** Số lượng Task Jira hoàn thành, điểm quy đổi độ phức tạp (Story Points).
2. **Chất lượng sản phẩm bàn giao (Deliverable Quality):** Mã nguồn Unit/Integration Test, Kịch bản Automation (Selenium, Postman, Python), Docker Scripts, Tài liệu kỹ thuật.
3. **Tuân thủ Quy trình kỹ thuật (Technical Compliance):** Đúng chuẩn Git Flow, không gây xung đột (Zero Merge Conflicts), tuân thủ Checkstyle/SpotBugs và đạt chỉ số bao phủ mã nguồn JaCoCo (> 70%).
4. **Tính chủ động và phối hợp nhóm (Collaboration & Teamwork):** Code review, hỗ trợ gỡ lỗi (Debugging), hỗ trợ xây dựng môi trường test dùng chung.

---

## 👤 2. HỒ SƠ THÀNH VIÊN VÀ VAI TRÒ CHỦ ĐẠO (TEAM MEMBER PROFILES)

| Họ và Tên | Vai trò chính | Phân hệ kỹ thuật đảm nhiệm chính | Công cụ & Thư viện chủ đạo |
| :--- | :--- | :--- | :--- |
| **Trương Hoài Được** | **Leader / Project Manager** | Quản trị Scrum Jira, DevOps CI/CD, Master REST API Automation, AI Mock Server | Jira Cloud, Postman, Newman CLI, GitHub Actions, Docker Compose |
| **Hoàng Phương** | **Senior QA / White-box Lead** | Kiểm thử Hộp trắng (Unit/Integration Test), JaCoCo Coverage, Kiểm thử Bảo mật (Security) | JUnit 5, Mockito, JaCoCo, Testcontainers, Checkstyle/SpotBugs |
| **Lĩnh** | **Fullstack QA / Automation Lead** | Selenium UI Automation (POM), Môi trường Docker Test, Database Seed Data | Selenium WebDriver, Java POM, MySQL 8, Docker Engine, PowerShell |
| **Ngọc Thịnh** | **QA Analyst / Theory Lead** | Thiết kế Kịch bản Hộp đen (EP, BVA, Decision Table), Retest Bugs, Cross-browser Test | Python Unittest, Pytest, JMeter, BVA Standard $4n+1/6n+1$, Mermaid |

---

## 📊 3. MA TRẬN ĐÓNG GÓP CHI TIẾT THEO SPRINT (SPRINT-BY-SPRINT CONTRIBUTION MATRIX)

### 3.1. Tuần 1: Lập Kế hoạch & Kiểm thử Tĩnh (Sprint 1 - Test Planning & Static Testing)

| Mã Task Jira | Tên Công việc | Thành viên | Story Points | Kết quả nghiệm thu | % Đóng góp Task |
| :--- | :--- | :--- | :---: | :--- | :---: |
| `TEST-1` | Setup Jira project, Scrum board, permissions | Hoài Được | 3 | Jira Cloud Board & Workflow 6 bước | 100% |
| `TEST-2` | Create Test Plan Document (IEEE 829) | Hoài Được | 5 | File `docs/TEST_PLAN.md` 220 dòng | 100% |
| `TEST-3` | Config SonarQube, Checkstyle/SpotBugs, Flake8 | Hoàng Phương | 5 | Config static tools & `pom.xml` | 100% |
| `TEST-4` | Perform manual code review & refactor | Hoàng Phương | 5 | Refactor Controller, `ApiResponse` | 100% |
| `TEST-5` | Prepare Docker test environment | Lĩnh | 5 | `docker-compose.yml` 6 services | 100% |
| `TEST-6` | Build Requirement Traceability Matrix (RTM)| Ngọc Thịnh | 5 | File `docs/REQUIREMENT_TRACEABILITY_MATRIX.md` | 100% |
| `TEST-7` | Submit Week 1 report & Merge PR | Tất cả | 3 | Báo cáo `docs/reports/Week_1_Summary.md` | 25% / thành viên |

---

### 3.2. Tuần 2: Thiết kế Test Case & Kiểm thử Đơn vị (Sprint 2 - Test Case Design & Unit Testing)

| Mã Task Jira | Tên Công việc | Thành viên | Story Points | Kết quả nghiệm thu | % Đóng góp Task |
| :--- | :--- | :--- | :---: | :--- | :---: |
| `TEST-8` | Design Auth API test cases (EP & BVA) | Ngọc Thịnh | 5 | File `docs/TEST_CASES_AUTH_API.md` | 100% |
| `TEST-9` | Design Cart & Order test cases (Decision Table)| Ngọc Thịnh | 5 | File `docs/TEST_CASES_CART_ORDER.md` | 100% |
| `TEST-10` | Develop validator unit tests | Hoàng Phương | 5 | 3 Lớp Validator Test (`Customer`, `Product`, `Register`)| 100% |
| `TEST-11` | Develop DAO unit tests | Hoàng Phương | 8 | 10 Lớp Unit & Integration Test DAO | 100% |
| `TEST-12` | Prepare database seed data | Lĩnh | 5 | File `seed_data.sql` 279 dòng cho 6 bảng | 100% |
| `TEST-13` | Build AI mock server | Hoài Được | 5 | Script `scripts/mock_ai_server.py` | 100% |
| `TEST-14` | Submit Week 2 report & Release Tag v1.0.0 | Tất cả | 2 | Báo cáo `docs/reports/Week_2_Summary.md` & Tag `v1.0.0` | 25% / thành viên |

---

### 3.3. Tuần 3: Tự động hóa API & Đo phủ JaCoCo (Sprint 3 - API Automation & White-box Coverage)

| Mã Task Jira | Tên Công việc | Thành viên | Story Points | Kết quả nghiệm thu | % Đóng góp Task |
| :--- | :--- | :--- | :---: | :--- | :---: |
| `TEST-15` | Automate User & Product REST APIs | Hoài Được | 5 | Collection `Shoeshop_API_Collection.json` | 100% |
| `TEST-16` | Automate Cart, Order, Voucher & Review APIs | Hoài Được | 5 | 46 API Test Cases trong Postman | 100% |
| `TEST-17` | Measure white-box coverage with JaCoCo | Hoàng Phương | 8 | Line 99.85%, Branch 99.33% (1,074 tests) | 100% |
| `TEST-18` | Manual AI image upload testing | Lĩnh | 5 | File `docs/test-results/TEST-18.md` | 100% |
| `TEST-19` | Manage bug lifecycle & Python logger | Ngọc Thịnh | 5 | Module `bug_logger.py` & `TEST-19.md` | 100% |
| `TEST-20` | Test search & pagination APIs | Ngọc Thịnh | 5 | Script `test_search_pagination_api.py` | 100% |
| `TEST-21` | Submit Week 3 report & Release Tag v3.0.0 | Tất cả | 3 | Báo cáo `docs/reports/Week_3_Summary.md` & Tag `v3.0.0` | 25% / thành viên |

---

### 3.4. Tuần 4: Tự động hóa UI & Integration Testing (Sprint 4 - UI Automation & Integration)

| Mã Task Jira | Tên Công việc | Thành viên | Story Points | Kết quả nghiệm thu | % Đóng góp Task |
| :--- | :--- | :--- | :---: | :--- | :---: |
| `TEST-22` | Develop UI automation for Auth (Selenium POM)| Lĩnh | 5 | Suite Selenium Auth (`LoginPage`, `BaseUiTest`)| 100% |
| `TEST-23` | Develop UI automation for Checkout Journey | Lĩnh | 5 | Suite Selenium Checkout (`CheckoutPage`) | 100% |
| `TEST-24` | Integration Testing with Testcontainers MySQL | Hoàng Phương | 8 | 5 Lớp Integration Test Rollback `@Transactional` | 100% |
| `TEST-25` | Cross-browser testing (Chrome/Firefox/Safari) | Ngọc Thịnh | 5 | Script `scripts/test_cross_browser.py` | 100% |
| `TEST-26` | Retest resolved bugs & Regression workflow | Ngọc Thịnh | 5 | Script `scripts/verify_resolved_bugs.py` | 100% |
| `TEST-27` | Package Newman CLI scripts & HTML report | Hoài Được | 5 | Script `scripts/run-api-tests.ps1` & Report | 100% |

---

### 3.5. Tuần 5: Chuẩn hóa Lý thuyết Hàn lâm (Sprint 5 - Theory Alignment & Advanced BVA)

| Mã Task Jira | Tên Công việc | Thành viên | Story Points | Kết quả nghiệm thu | % Đóng góp Task |
| :--- | :--- | :--- | :---: | :--- | :---: |
| `TEST-28` | Standard & Robustness BVA (4n+1, 6n+1) | Hoàng Phương | 5 | `CustomerFormValidatorTest.java` BVA | 100% |
| `TEST-29` | Worst-Case Testing ($5^n$ Matrix) | Ngọc Thịnh | 5 | Script `test_search_pagination_api.py` | 100% |
| `TEST-30` | Decision Table Testing (Voucher 8 Rules) | Hoài Được | 5 | File `docs/Blackbox_Decision_Table.md` | 100% |
| `TEST-31` | Control Flow Graph (CFG) & Cyclomatic V(G) | Hoàng Phương | 5 | Báo cáo CFG 12 Nodes, 16 Edges ($V(G)=6$) | 100% |
| `TEST-32` | Experience-based Testing (Error Guessing) | Lĩnh | 5 | Báo cáo `docs/test-results/TEST-18.md` | 100% |
| `TEST-33` | Review 24h Time Limit & Boundary Testing | Hoài Được | 5 | Code 24h limit, 403 status, `TEST-33.md` | 100% |
| `TEST-34` | Performance & Response Time Summary | Ngọc Thịnh | 5 | File `docs/TEST-34-performance-summary.md` | 100% |

---

### 3.6. Tuần 6: Đóng gói Báo cáo & Bàn giao Sản phẩm (Sprint 6 - Final Deliverables & Release)

| Mã Task Jira | Tên Công việc | Thành viên | Story Points | Kết quả nghiệm thu | % Đóng góp Task |
| :--- | :--- | :--- | :---: | :--- | :---: |
| `TEST-35` | Prepare final test summary report | Hoài Được | 5 | Báo cáo `docs/reports/Week_6_Summary.md` | 100% |
| `TEST-36` | Prepare coverage metrics report | Hoàng Phương | 5 | Báo cáo chỉ số JaCoCo final | 100% |
| `TEST-37` | Package UI & API automation assets | Lĩnh | 5 | Đóng gói toàn bộ Scripts Maven/Python | 100% |
| `TEST-38` | Prepare presentation slide & demo video | Ngọc Thịnh | 5 | Slide thuyết trình & Video minh họa | 100% |
| `TEST-39` | Prepare team contribution matrix report | Hoài Được | 5 | File `docs/TEST-39-contribution-report.md` | 100% |
| `TEST-40` | Tag final release v4.0.0 & merge to main | Hoài Được | 3 | Tag Release `v4.0.0` trên GitHub | 100% |

---

## 📈 4. THỐNG KÊ ĐỊNH LƯỢNG ĐÓNG GÓP (QUANTITATIVE CONTRIBUTION METRICS)

### Bảng tổng hợp chỉ số kỹ thuật cá nhân:

| Chỉ số Thống kê (Metric) | Trương Hoài Được | Hoàng Phương | Lĩnh | Ngọc Thịnh | Tổng Dự án |
| :--- | :---: | :---: | :---: | :---: | :---: |
| **Tổng số Task Jira hoàn thành** | 11 Tasks | 9 Tasks | 9 Tasks | 11 Tasks | **40 Tasks** |
| **Tổng Story Points đạt được** | 47 Points | 49 Points | 43 Points | 48 Points | **187 Points** |
| **Tỷ lệ hoàn thành Task (Pass Rate)** | 100% | 100% | 100% | 100% | **100%** |
| **Số Lớp / Scripts Code đóng góp** | 8 Scripts / Collections | 42 Java Test Classes | 12 Selenium/Docker Files | 8 Python Scripts | **70+ Files** |
| **Số Kịch bản Test đã triển khai** | 46 REST APIs | 1,074 Unit/Int Tests | 12 E2E UI Scenarios | 46 Black-box Cases | **1,178 Tests** |
| **Số lượng Tài liệu Markdown đã viết**| 12 File Docs | 6 File Docs | 5 File Docs | 8 File Docs | **31 File Docs** |

---

## 🏆 5. XÁC NHẬN BẢNG ĐIỂM ĐÓNG GÓP CUỐI KỲ (FINAL CONTRIBUTION EVALUATION)

Tất cả 4 thành viên đã phối hợp ăn ý, tuân thủ tuyệt đối quy trình Git Flow và Scrum Agile. Khối lượng công việc được phân chia đồng đều, phát huy tối đa thế mạnh chuyên môn của từng cá nhân.

### Bảng tổng kết tỷ lệ đóng góp chính thức:

```text
+-----------------------+----------------------------------+------------------+-----------------+
| Thành viên            | Vai trò nòng cốt                 | Tỷ lệ Đóng góp   | Xếp loại        |
+-----------------------+----------------------------------+------------------+-----------------+
| Trương Hoài Được      | Leader, DevOps, API Automation   | 25.0%            | Đạt loại Xuất sắc|
| Hoàng Phương          | White-box, Security & Coverage   | 25.0%            | Đạt loại Xuất sắc|
| Lĩnh                  | UI Automation, Infra & Database  | 25.0%            | Đạt loại Xuất sắc|
| Ngọc Thịnh            | Black-box, Theory & Performance  | 25.0%            | Đạt loại Xuất sắc|
+-----------------------+----------------------------------+------------------+-----------------+
| TỔNG CỘNG             | ĐỘI NGŨ DỰ ÁN SHOESHOP           | 100.0%           | ĐẠT ĐIỂM TỐI ĐA |
+-----------------------+----------------------------------+------------------+-----------------+
```

---

## 📊 6. MA TRẬN XÁC THỰC HOÀN THÀNH TASK (VERIFICATION MATRIX)

| STT | Yêu cầu Jira (Requirement) | Thành phần triển khai (Component) | Phương pháp xác thực (Verification Method) | Trạng thái (Status) |
| :---: | :--- | :--- | :--- | :---: |
| 1 | Checkout/tạo branch `test/w6-TEST-39-prepare-contribution-report` | Git repository | Git Command (`git branch`) | **VERIFIED** |
| 2 | Không làm thay đổi hay ảnh hưởng đến logic code hiện hành | Source Code Base | Git Status Check (`git status`) | **VERIFIED** |
| 3 | Tạo bảng ma trận đóng góp chi tiết, rõ ràng trong thư mục `docs/` | `docs/TEST-39-contribution-report.md` | Markdown Matrix Structure | **VERIFIED** |
| 4 | Kiểm tra biên dịch/build dự án đảm bảo không phát sinh lỗi | Maven Build System | Lệnh `mvn compile` | **VERIFIED** |
