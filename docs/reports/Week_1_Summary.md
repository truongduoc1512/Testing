# 📊 BÁO CÁO TỔNG HỢP TIẾN ĐỘ - TUẦN 1
> **Dự án:** ShoeShop Testing & Development  
> **Sprint Jira:** Sprint 1 - Test Planning & Static Testing  
> **Thời gian:** 27/07/2026 - 03/08/2026  
> **Người tổng hợp (Leader):** Trương Hoài Dược  

---

## 🎯 1. MỤC TIÊU TUẦN
- [x] **Mục tiêu 1:** Thiết lập thành công Jira Project, Scrum Board, Workflow và Phân quyền nhóm.
- [x] **Mục tiêu 2:** Xây dựng Tài liệu Kế hoạch Kiểm thử toàn diện (Test Plan Document - IEEE 829).
- [x] **Mục tiêu 3:** Cấu hình các công cụ kiểm thử tĩnh (SonarQube, Checkstyle, SpotBugs cho Java và Flake8, Pylint cho Python).
- [x] **Mục tiêu 4:** Thực hiện Rà soát mã nguồn thủ công (Manual Code Review) và tái cấu trúc các lớp Controller.
- [x] **Mục tiêu 5:** Thiết lập & tối ưu Môi trường Docker Test cho 6 dịch vụ (MySQL, App, AI Service, Nginx, phpMyAdmin, Swagger).
- [x] **Mục tiêu 6:** Xây dựng Ma trận Truy xuất Yêu cầu (Requirement Traceability Matrix - RTM).

---

## 📋 2. BẢNG TỔNG HỢP THỰC THI TASK (JIRA & GITHUB)

| Mã Task Jira | Tên công việc | Người thực hiện | Trạng thái Jira | Pull Request (PR) | Loại Task |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `TEST-1` | Setup Jira project, Scrum board, and team permissions | Trương Hoài Dược | ✅ Done | Nhánh `main` | `infra` |
| `TEST-2` | Create Test Plan Document | Trương Hoài Dược | ✅ Done | [PR docs/w1-TEST-2-create-test-plan](https://github.com/truongduoc1512/Testing/tree/docs/w1-TEST-2-create-test-plan) | `docs` |
| `TEST-3` | Configure SonarQube, Checkstyle/SpotBugs, Flake8 | Hoàng Phương Nguyễn | ✅ Done | [PR test/w1-TEST-3-config-sonarqube-checkstyle](https://github.com/truongduoc1512/Testing/tree/test/w1-TEST-3-config-sonarqube-checkstyle) | `test` |
| `TEST-4` | Perform manual code review | Hoàng Phương Nguyễn | ✅ Done | [PR feature/w1-TEST-4-review-controllers](https://github.com/truongduoc1512/Testing/tree/feature/w1-TEST-4-review-controllers) | `feature` |
| `TEST-5` | Prepare Docker test environment | Bạn Lĩnh | ✅ Done | [PR feature/w1-TEST-5-prepare-docker-test-environment](https://github.com/truongduoc1512/Testing/tree/feature/w1-TEST-5-prepare-docker-test-environment) | `infra` |
| `TEST-6` | Build Requirement Traceability Matrix | Nguyễn Hoài Thịnh | ✅ Done | [PR docs/w1-TEST-6-build-requirement-traceability-matrix](https://github.com/truongduoc1512/Testing/tree/docs/w1-TEST-6-build-requirement-traceability-matrix) | `docs` |
| `TEST-7` | Submit Week 1 report and merge PR | Tất cả thành viên | ✅ Done | [PR week/week-1-test-planning](https://github.com/truongduoc1512/Testing/tree/week/week-1-test-planning) | `docs` |

---

## 📈 3. THỐNG KÊ CHỈ SỐ TUẦN (METRICS)
- **Tổng số Task cam kết:** 7 tasks
- **Số Task hoàn thành (Done):** 7 / 7 tasks (100%)
- **Tổng Story Points hoàn thành:** 28 points
- **Số Pull Request đã Merge:** 6 PRs
- **Số Bug phát hiện / tạo mới trên Jira:** 0 bugs (Giai đoạn Kiểm thử tĩnh & Thiết lập môi trường)

---

## 🔍 4. MINH CHỨNG NGHỆM THU THEO THÀNH VIÊN

### Leader (Trương Hoài Dược) - Task: `TEST-1`, `TEST-2`
- **Sản phẩm bàn giao:** 
  * Dự án Jira Scrum Board `ShoeShop Testing & Development` phân quyền đủ 4 thành viên.
  * File tài liệu [docs/TEST_PLAN.md](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/docs/TEST_PLAN.md) chuẩn IEEE 829 định nghĩa đầy đủ 6 tuần kiểm thử.

### Phương (Hoàng Phương Nguyễn) - Task: `TEST-3`, `TEST-4`
- **Sản phẩm bàn giao:** 
  * File [pom.xml](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/pom.xml) gắn plugin Checkstyle, SpotBugs, SonarQube; cấu hình `.flake8`, `.pylintrc` cho Python.
  * File [config/README.md](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/config/README.md) báo cáo 294 Sonar issues; file `src/main/README.md`, `src/test/README.md`.
  * Tái cấu trúc Controller, bổ sung `ApiResponse`, `CsrfTokenControllerAdvice` và 8 file Unit Test mới.

### Lĩnh (Bạn Lĩnh) - Task: `TEST-5`
- **Sản phẩm bàn giao:** 
  * File [docker-compose.yml](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/docker-compose.yml) chạy 6 container (MySQL, App, AI Service, Nginx, phpMyAdmin, Swagger).
  * File tài liệu [docs/DOCKER_TEST_ENV.md](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/docs/DOCKER_TEST_ENV.md) và script tiện ích [scripts/start-test-env.ps1](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/scripts/start-test-env.ps1).

### Thịnh (Nguyễn Hoài Thịnh) - Task: `TEST-6`
- **Sản phẩm bàn giao:** 
  * File tài liệu [docs/REQUIREMENT_TRACEABILITY_MATRIX.md](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/docs/REQUIREMENT_TRACEABILITY_MATRIX.md) chứa ma trận bao phủ 100% (10 phân hệ nghiệp vụ lớn).

---

## ⚠️ 5. VẤN ĐỀ PHÁT SINH & GIẢI PHÁP (BLOCKERS & SOLUTIONS)

| STT | Vấn đề / Lỗi gặp phải | Nguyên nhân | Giải pháp đã xử lý | Trạng thái |
| :--- | :--- | :--- | :--- | :--- |
| 1 | Container MySQL bị ngắt kết nối khi Spring Boot vừa khởi chạy | Spring Boot khởi động nhanh hơn MySQL | Bổ sung `healthcheck` (`mysqladmin ping`) và `depends_on` với `condition: service_healthy` trong Docker Compose | ✅ Resolved |
| 2 | Đặt công cụ ESLint không phù hợp với dự án Thymeleaf SSR | ESLint dành riêng cho JS SPA | Đổi sang Checkstyle/SpotBugs cho Java và Flake8 cho Python | ✅ Resolved |
| 3 | Lặp thư mục con `Testing-main/` khi tạo PR task TEST-6 | Giải nén đè folder gốc | Khởi tạo lại nhánh sạch từ `week/week-1-test-planning` và `git push --force` | ✅ Resolved |

---

## 🚀 6. KẾ HOẠCH TUẦN TIẾP THEO (WEEK 2: Thiết kế Test Case & Unit Testing)
- [ ] **Được (Leader):** Build AI mock server (`TEST-13`)
- [ ] **Phương:** Develop validator unit tests (`TEST-10`) & Develop DAO unit tests (`TEST-11`)
- [ ] **Lĩnh:** Prepare database seed data (`TEST-12`)
- [ ] **Thịnh:** Design Auth API test cases (`TEST-8`) & Design Cart & Order test cases (`TEST-9`)
