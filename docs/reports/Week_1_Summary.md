# 📊 BÁO CÁO TỔNG HỢP TIẾN ĐỘ - TUẦN 1
> **Dự án:** ShoeShop Testing & Quality Assurance System  
> **Sprint Jira:** Sprint 1 — Test Planning, Static Testing & Test Environment Setup  
> **Thời gian:** 27/07/2026 – 03/08/2026  
> **Người tổng hợp (Leader):** Trương Hoài Được  
> **Nhánh tích hợp chính:** `week/week-1-test-planning` ➔ `develop`  

---

## 🎯 1. MỤC TIÊU SPRINT (SPRINT OBJECTIVES)
- [x] **Mục tiêu 1:** Thiết lập hoàn chỉnh không gian làm việc Jira Software, cấu hình Scrum Board, luồng Workflow chuẩn và phân quyền 4 thành viên nhóm.
- [x] **Mục tiêu 2:** Xây dựng Tài liệu Kế hoạch Kiểm thử tổng thể (Master Test Plan Document) theo chuẩn quốc tế **IEEE 829-2008**.
- [x] **Mục tiêu 3:** Thiết lập công cụ Kiểm thử tĩnh (Static Analysis): Tích hợp SonarQube, Checkstyle, SpotBugs cho Java Spring Boot và Flake8, Pylint cho Python FastAPI.
- [x] **Mục tiêu 4:** Thực hiện Rà soát mã nguồn thủ công (Manual Code Review), chuẩn hóa kiến trúc MVC và tái cấu trúc tầng Controller xử lý ngoại lệ đồng nhất.
- [x] **Mục tiêu 5:** Thiết lập & đóng gói Môi trường Kiểm thử Container hóa bằng Docker Compose gồm 6 dịch vụ độc lập.
- [x] **Mục tiêu 6:** Xây dựng Ma trận Truy xuất Yêu cầu (Requirement Traceability Matrix - RTM) bao phủ 100% yêu cầu chức năng của hệ thống.

---

## 📋 2. BẢNG TỔNG HỢP THỰC THI TASK (JIRA & GITHUB)

| Mã Task Jira | Tên công việc (Summary) | Người thực hiện | Điểm SP | Trạng thái | Pull Request / Nhánh Git | Phân loại |
| :--- | :--- | :--- | :---: | :---: | :--- | :---: |
| `TEST-1` | Setup Jira project, Scrum board, and team permissions | Hoài Được | 3 | ✅ Done | Nhánh `main` | `infra` |
| `TEST-2` | Create Master Test Plan Document (IEEE 829) | Hoài Được | 5 | ✅ Done | [PR docs/w1-TEST-2-create-test-plan](https://github.com/truongduoc1512/Testing/tree/docs/w1-TEST-2-create-test-plan) | `docs` |
| `TEST-3` | Configure SonarQube, Checkstyle, SpotBugs, Flake8 | Hoàng Phương | 5 | ✅ Done | [PR test/w1-TEST-3-config-sonarqube-checkstyle](https://github.com/truongduoc1512/Testing/tree/test/w1-TEST-3-config-sonarqube-checkstyle) | `test` |
| `TEST-4` | Perform manual code review & refactor controllers | Hoàng Phương | 5 | ✅ Done | [PR feature/w1-TEST-4-review-controllers](https://github.com/truongduoc1512/Testing/tree/feature/w1-TEST-4-review-controllers) | `feature` |
| `TEST-5` | Prepare Docker test environment (6 containers) | Lĩnh | 5 | ✅ Done | [PR feature/w1-TEST-5-prepare-docker-test-environment](https://github.com/truongduoc1512/Testing/tree/feature/w1-TEST-5-prepare-docker-test-environment) | `infra` |
| `TEST-6` | Build Requirement Traceability Matrix (RTM) | Ngọc Thịnh | 3 | ✅ Done | [PR docs/w1-TEST-6-build-requirement-traceability-matrix](https://github.com/truongduoc1512/Testing/tree/docs/w1-TEST-6-build-requirement-traceability-matrix) | `docs` |
| `TEST-7` | Submit Week 1 summary report and merge PR | Tất cả | 2 | ✅ Done | [PR week/week-1-test-planning](https://github.com/truongduoc1512/Testing/tree/week/week-1-test-planning) | `docs` |

---

## 📈 3. THỐNG KÊ CHỈ SỐ SPRINT (METRICS & KPIS)

| Chỉ số đo lường (Metric) | Kế hoạch cam kết | Kết quả thực tế | Tỷ lệ hoàn thành | Đánh giá |
| :--- | :---: | :---: | :---: | :---: |
| **Tổng số Task Jira cam kết** | 7 tasks | 7 tasks | **100%** | 🟢 Đạt chuẩn |
| **Tổng Story Points hoàn thành** | 28 SP | 28 SP | **100%** | 🟢 Đạt chuẩn |
| **Độ bao phủ yêu cầu nghiệp vụ (RTM)** | 10 Phân hệ | 10 Phân hệ | **100%** | 🟢 Tuyệt đối |
| **Số lỗi phát hiện qua Kiểm thử tĩnh (Sonar)** | N/A | 294 issues (Code Smells/Bugs) | Đã phân loại & khoanh vùng | 🟢 Hoàn thành |
| **Số Container kiểm thử khởi tạo thành công** | 6 containers | 6 containers | **100%** | 🟢 Đạt chuẩn |
| **Số Pull Request đã review & merge** | 6 PRs | 6 PRs | **100%** | 🟢 Hoàn thành |

---

## 🔍 4. CHI TIẾT SẢN PHẨM BÀN GIAO & MINH CHỨNG THEO THÀNH VIÊN

### 1. Trương Hoài Được (Leader) — Thực hiện `TEST-1`, `TEST-2`, `TEST-7`
- **Sản phẩm bàn giao:**
  * Thiết lập dự án Jira `ShoeShop Testing & Development`, tạo Scrum Board, cấu hình Swimlanes, phân quyền `Admin/Developer/Tester` cho 4 thành viên.
  * Tác giả chính tài liệu [docs/TEST_PLAN.md](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/docs/TEST_PLAN.md) (hơn 400 dòng) chuẩn quốc tế **IEEE 829-2008**, định nghĩa phạm vi kiểm thử (Scope), chiến lược kiểm thử (Testing Strategy), tiêu chí nghiệm thu (Entry/Exit Criteria), kế hoạch quản lý rủi ro và lộ trình chi tiết 6 tuần.
  * Quản lý Git Flow, review code toàn bộ các PR của Sprint 1 và hợp nhất nhánh sạch vào `develop`.

### 2. Hoàng Phương — Thực hiện `TEST-3`, `TEST-4`
- **Sản phẩm bàn giao:**
  * Tích hợp plugin kiểm thử tĩnh vào [pom.xml](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/pom.xml): `sonar-maven-plugin`, `maven-checkstyle-plugin`, `spotbugs-maven-plugin`. Cấu hình `.flake8` và `.pylintrc` cho Python service.
  * Xuất báo cáo phân tích tĩnh tại `config/README.md` với 294 issues được phân loại chi tiết (Bugs, Vulnerabilities, Code Smells).
  * Rà soát mã nguồn thủ công 6 Controller cốt lõi: Tách biệt response sang định dạng chuẩn `ApiResponse<T>`, tích hợp `CsrfTokenControllerAdvice`, bổ sung 8 file Unit Test khởi đầu xác minh tầng Web.

### 3. Lĩnh — Thực hiện `TEST-5`
- **Sản phẩm bàn giao:**
  * Xây dựng hệ sinh thái Docker hoàn chỉnh trong [docker-compose.yml](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/docker-compose.yml) gồm 6 containers liên kết qua mạng `shoeshop-net`:
    1. `shoeshop-mysql`: CSDL MySQL 8.0 (cổng 3307).
    2. `shoeshop-api`: Backend Spring Boot (cổng 8080).
    3. `shoeshop-ai`: Microservice FastAPI YOLOv8 (cổng 8000).
    4. `shoeshop-nginx`: Reverse Proxy cân bằng tải & định tuyến (cổng 80).
    5. `shoeshop-phpmyadmin`: Trực quan hóa CSDL (cổng 8081).
    6. `shoeshop-swagger`: Swagger UI tài liệu hóa REST API (cổng 8082).
  * Viết tài liệu hướng dẫn [docs/DOCKER_TEST_ENV.md](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/docs/DOCKER_TEST_ENV.md) và script tự động [scripts/start-test-env.ps1](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/scripts/start-test-env.ps1).

### 4. Ngọc Thịnh — Thực hiện `TEST-6`
- **Sản phẩm bàn giao:**
  * Xây dựng Ma trận Truy xuất Yêu cầu [docs/REQUIREMENT_TRACEABILITY_MATRIX.md](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/docs/REQUIREMENT_TRACEABILITY_MATRIX.md) liên kết chặt chẽ giữa Yêu cầu nghiệp vụ (Business Requirements - BR) với Thiết kế hệ thống và Kịch bản kiểm thử tương ứng trên 10 phân hệ nghiệp vụ lớn: Xác thực, Danh mục sản phẩm, Giỏ hàng, Đơn hàng, Voucher, Đánh giá, Hủy/Đổi trả hàng, Quản trị Admin, Cổng AI kiểm duyệt và Thông báo người dùng.

---

## 💻 5. TỔNG HỢP LỆNH CHẠY KIỂM THỬ (TEST COMMANDS)

Bảng tổng hợp toàn bộ các câu lệnh thực thi kiểm thử, phân tích tĩnh và khởi tạo môi trường phục vụ Sprint 1:

| STT | Phân loại | Mục đích kiểm thử | Lệnh thực thi (CLI / Maven / PowerShell) | Môi trường / Điều kiện tiên quyết | Kết quả ghi nhận |
| :---: | :--- | :--- | :--- | :--- | :--- |
| 1 | **Môi trường** | Khởi động toàn bộ cụm 6 Docker containers kiểm thử | `docker compose up -d --build` | Đã cài đặt Docker Desktop, RAM ≥ 8GB | 6 containers (`mysql`, `api`, `ai`, `nginx`, `pma`, `swagger`) chạy thành công |
| 2 | **Môi trường** | Khởi động tự động môi trường test qua PowerShell script | `powershell .\scripts\start-test-env.ps1` | Quyền thực thi PowerShell Scripts | Tự động kiểm tra Docker, khởi động và nạp dữ liệu |
| 3 | **Kiểm thử tĩnh** | Quét tuân thủ quy tắc lập trình Java (Checkstyle) | `mvn checkstyle:check` | Cấu hình `checkstyle.xml` | Báo cáo chi tiết tại `target/checkstyle-result.xml` |
| 4 | **Kiểm thử tĩnh** | Phân tích bytecode tìm lỗi tiềm ẩn Java (SpotBugs) | `mvn spotbugs:check` | Cấu hình `config/spotbugs/spotbugs-exclude.xml` | Phát hiện và phân loại nguy cơ tại `target/spotbugsXml.xml` |
| 5 | **Kiểm thử tĩnh** | Kiểm tra chuẩn mã nguồn Python AI Service (Flake8) | `flake8 ai-service/` | Môi trường Python 3.10+ | Kiểm tra PEP8 và cú pháp sạch (0 errors) |
| 6 | **Unit Test** | Chạy toàn bộ các ca kiểm thử đơn vị khởi đầu (Controllers) | `mvn test` | CSDL MySQL test container đã bật | 8 file test ban đầu vượt qua 100% |

---

## ⚠️ 6. VẤN ĐỀ PHÁT SINH & GIẢI PHÁP XỬ LÝ (BLOCKERS & RESOLUTIONS)

| STT | Vấn đề phát sinh (Blocker) | Nguyên nhân gốc rễ | Giải pháp kỹ thuật đã xử lý | Kết quả |
| :---: | :--- | :--- | :--- | :---: |
| 1 | Container `shoeshop-api` bị ngắt kết nối MySQL khi vừa boot | Spring Boot khởi động nhanh hơn CSDL MySQL khởi tạo schema | Bổ sung cơ chế `healthcheck` (`mysqladmin ping`) cho MySQL và khai báo `depends_on: condition: service_healthy` cho app trong Docker Compose | ✅ Triệt để |
| 2 | ESLint không tương thích với dự án giao diện SSR | Mã nguồn Frontend sử dụng Thymeleaf HTML5 kết hợp jQuery cổ điển, không dùng React/Vue | Chuyển trọng tâm Static Analysis sang Checkstyle/SpotBugs cho Java và Flake8 cho Python | ✅ Phù hợp |
| 3 | Lỗi lặp thư mục con `Testing-main/` khi tạo PR task TEST-6 | Thành viên clone zip và giải nén đè thư mục gốc | Hướng dẫn rebase, checkout nhánh sạch từ `week/week-1-test-planning` và force push lại | ✅ Chuẩn hóa |

---

## 🚀 7. KẾ HOẠCH BÀN GIAO SPRINT TIẾP THEO (SPRINT 2)
Sprint 2 sẽ chuyển dịch trọng tâm sang **Thiết kế kịch bản kiểm thử chức năng (Black-box)** và **Phát triển bộ Unit Test tự động (White-box)**:
- **Được (Leader):** Xây dựng AI Mock Server phản hồi nhanh (`TEST-13`).
- **Phương:** Phát triển Unit Test cho Validator (`TEST-10`) và toàn bộ tầng DAO (`TEST-11`).
- **Lĩnh:** Chuẩn bị CSDL mẫu chuẩn (`seed_data.sql`) phục vụ kiểm thử (`TEST-12`).
- **Thịnh:** Thiết kế kịch bản kiểm thử API Auth (`TEST-8`) và Cart/Order (`TEST-9`) áp dụng kỹ thuật EP, BVA và Decision Table.
