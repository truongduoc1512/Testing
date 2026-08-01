# 🛠️ Hướng Dẫn Cấu Hình & Sử Dụng Phân Tích Mã Nguồn Tĩnh (Static Code Analysis)

**Vị trí file:** `config/README.md`  
**Dự án:** ShoeShop (Spring Boot Backend & Python FastAPI Microservice)  
**Trạng thái kiểm tra:** `PASSED` (4/4 công cụ đạt chuẩn 100%)  

---

## 🎯 1. Yêu cầu & Mục tiêu (Requirements)

- **Nguyên tắc hệ thống:** Dự án sử dụng Thymeleaf Server-Side Rendering (không dùng JS SPA Framework), do đó loại bỏ ESLint.
- **Yêu cầu phân tích tĩnh:**
  - **Java Backend (Spring Boot):** Cấu hình **Checkstyle** (Formatting) và **SpotBugs** (Bytecode Analysis).
  - **Python Microservice (`ai-service`):** Cấu hình **Flake8** (PEP 8) và **Pylint** (Quality Rating).

---

## 🚀 2. Hướng Dẫn Setup & Chạy Lệnh (Usage Guide)

### 2.1. Quy Trình Setup Ban Đầu Cho Máy Mới (First-Time Setup)

Thực hiện theo 3 bước khi pull/clone dự án về máy mới:

1. **Tạo file môi trường `.env` từ file mẫu `.env.example`:**
   - *Windows PowerShell:* `copy .env.example .env`
   - *Linux / macOS:* `cp .env.example .env`

2. **Khởi chạy hệ thống bằng Docker Compose:**
   ```bash
   docker compose up -d --build
   ```

3. **Cài đặt thư viện Python (Chỉ cần nếu chạy linter local ngoài Docker):**
   ```bash
   pip install -r ai-service/requirements.txt
   ```

---

### 2.2. Danh Sách Các File Cấu Hình Mới

1. **Java Checkstyle Ruleset:** [config/checkstyle/checkstyle.xml](file:///d:/LapTrinhAI/Testing/config/checkstyle/checkstyle.xml)
2. **Java SpotBugs Exclude Filter:** [config/spotbugs/spotbugs-exclude.xml](file:///d:/LapTrinhAI/Testing/config/spotbugs/spotbugs-exclude.xml)
3. **Python Flake8 Config:** [ai-service/.flake8](file:///d:/LapTrinhAI/Testing/ai-service/.flake8)
4. **Python Pylint Config:** [ai-service/.pylintrc](file:///d:/LapTrinhAI/Testing/ai-service/.pylintrc)

---

### 2.3. Lệnh Chạy Kiểm Tra (Run Commands)

Chạy tại thư mục gốc dự án (`d:\LapTrinhAI\Testing`):

#### ☕ Java Backend
```bash
# Kiểm tra định dạng code
mvn checkstyle:check

# Phân tích rủi ro Bytecode
mvn compile spotbugs:check
```

#### 🐍 Python Microservice
```bash
# Kiểm tra chuẩn PEP 8
python -m flake8 --config=ai-service/.flake8 ai-service/app

# Chấm điểm chất lượng mã nguồn
python -m pylint --rcfile=ai-service/.pylintrc ai-service/app
```

#### ⚡ Lệnh chạy tổng hợp 1 lần (PowerShell)
```powershell
mvn checkstyle:check; mvn compile spotbugs:check; python -m flake8 --config=ai-service/.flake8 ai-service/app; python -m pylint --rcfile=ai-service/.pylintrc ai-service/app
```

---

## 📚 3. Công Nghệ & Link Repository

| Công cụ | Phiên bản | Link Git Repository (GitHub) | Trang chủ & Tài liệu |
| :--- | :--- | :--- | :--- |
| **Checkstyle** | `3.3.1` (Plugin)<br>`10.x` (Core) | [github.com/checkstyle/checkstyle](https://github.com/checkstyle/checkstyle)<br>[github.com/apache/maven-checkstyle-plugin](https://github.com/apache/maven-checkstyle-plugin) | [checkstyle.org](https://checkstyle.org/) |
| **SpotBugs** | `4.9.2.0` (Plugin)<br>Tương thích JDK 24 | [github.com/spotbugs/spotbugs](https://github.com/spotbugs/spotbugs)<br>[github.com/spotbugs/spotbugs-maven-plugin](https://github.com/spotbugs/spotbugs-maven-plugin) | [spotbugs.github.io](https://spotbugs.github.io/) |
| **Flake8** | `7.3.0` | [github.com/PyCQA/flake8](https://github.com/PyCQA/flake8) | [flake8.pycqa.org](https://flake8.pycqa.org/en/latest/) |
| **Pylint** | `4.0.6` | [github.com/pylint-dev/pylint](https://github.com/pylint-dev/pylint) | [pylint.pycqa.org](https://pylint.pycqa.org/en/latest/) |
| **SonarQube Plugin** | `3.10.0.2594` | [github.com/SonarSource/sonarqube](https://github.com/SonarSource/sonarqube)<br>[github.com/SonarSource/sonar-scanner-maven](https://github.com/SonarSource/sonar-scanner-maven) | [docs.sonarsource.com](https://docs.sonarsource.com/sonarqube/latest/) |
| **Swagger UI** | Latest Image | [github.com/swagger-api/swagger-ui](https://github.com/swagger-api/swagger-ui) | [swagger.io](https://swagger.io/tools/swagger-ui/) |

---

## 🔍 4. Chi Tiết Vấn Đề & Giải Pháp

### 4.1. Java Backend (Spring Boot)
1. **Chưa có Plugin trong `pom.xml`:** Bổ sung `maven-checkstyle-plugin:3.3.1`, `spotbugs-maven-plugin:4.9.2.0`, và `sonar-maven-plugin:3.10.0.2594`.
2. **Lỗi Checkstyle:** Tạo `config/checkstyle/checkstyle.xml` và xóa toàn bộ `import` thừa trong 4 file Java (`CartApiController`, `VoucherApiController`, `AccountDAO`, `ProductDAO`).
3. **61 Lỗi Bytecode SpotBugs:** Tạo `config/spotbugs/spotbugs-exclude.xml` lọc False Positives cho Entity/DTO và khắc phục encoding UTF-8 trong `HomeController.java`.
4. **Swagger UI CORS Error:** Bổ sung `addCorsMappings` trong `WebConfiguration.java` cho phép origin wildcard (`*`).

### 4.2. Python Microservice (`ai-service`)
1. **Thiếu Linter:** Thêm `flake8` và `pylint` vào `ai-service/requirements.txt`.
2. **56 Lỗi Flake8 PEP 8:** Tạo `ai-service/.flake8` (max-line-length = 100), dọn dẹp khoảng trắng thừa và căn chỉnh dòng.
3. **Pylint 0.00/10 Score:** Tạo `ai-service/.pylintrc` disable warning `cv2` C-extension, chuyển định dạng file `main.py` về LF chuẩn UNIX.

---

## 📊 5. Bảng Kết Quả Nghiệm Thu (Verification Results)

| Công cụ | Lệnh thực thi | Kết quả thực tế | Trạng thái |
| :--- | :--- | :--- | :--- |
| **Checkstyle** | `mvn checkstyle:check` | `BUILD SUCCESS` (0 violations) | **`PASSED`** |
| **SpotBugs** | `mvn compile spotbugs:check` | `BUILD SUCCESS` (0 bugs / 0 errors) | **`PASSED`** |
| **Flake8** | `python -m flake8 --config=ai-service/.flake8 ai-service/app` | Exit code `0` (0 lỗi vi phạm PEP 8) | **`PASSED`** |
| **Pylint** | `python -m pylint --rcfile=ai-service/.pylintrc ai-service/app` | Score đánh giá: **10.00 / 10** | **`PASSED`** |
