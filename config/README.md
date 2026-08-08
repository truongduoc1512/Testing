# Hướng dẫn phân tích mã nguồn tĩnh

**Dự án:** ShoeShop — Spring Boot backend và Python FastAPI microservice

**Phạm vi:** Checkstyle, SpotBugs, Flake8, Pylint và SonarQube
**Trạng thái:** Các công cụ đã được cấu hình và chạy thành công; các issue SonarQube chưa được refactor.

---

## 1. Yêu cầu và mục tiêu

Dự án dùng Thymeleaf Server-Side Rendering, không dùng JavaScript SPA framework, vì vậy không cấu hình ESLint.

| Thành phần | Công cụ | Mục đích |
|---|---|---|
| Spring Boot | Checkstyle | Kiểm tra quy tắc định dạng và import Java. |
| Spring Boot | SpotBugs | Phân tích Java bytecode để tìm lỗi tiềm ẩn. |
| `ai-service` | Flake8 | Kiểm tra PEP 8 và định dạng Python. |
| `ai-service` | Pylint | Đánh giá chất lượng và maintainability của Python. |
| Toàn bộ backend Java | SonarQube | Tổng hợp bug, vulnerability, code smell, duplication và Quality Gate. |

---

## 2. Cấu hình đã thêm

### 2.1. Các file cấu hình

| Công cụ | File cấu hình |
|---|---|
| Checkstyle | [`checkstyle/checkstyle.xml`](checkstyle/checkstyle.xml) |
| SpotBugs | [`spotbugs/spotbugs-exclude.xml`](spotbugs/spotbugs-exclude.xml) |
| Flake8 | [`../ai-service/.flake8`](../ai-service/.flake8) |
| Pylint | [`../ai-service/.pylintrc`](../ai-service/.pylintrc) |
| Maven plugins | [`../pom.xml`](../pom.xml) |

### 2.2. Maven plugins

Các plugin sau được khai báo trong `pom.xml`:

| Plugin | Phiên bản |
|---|---:|
| `maven-checkstyle-plugin` | `3.3.1` |
| `spotbugs-maven-plugin` | `4.9.2.0` |
| `sonar-maven-plugin` | `3.10.0.2594` |

### 2.3. Các thay đổi cấu hình liên quan

- Xóa import thừa trong `CartApiController`, `VoucherApiController`, `AccountDAO` và `ProductDAO`.
- Chuẩn hóa UTF-8 khi đọc file trong `HomeController`.
- Cấu hình SpotBugs exclude có phạm vi cho các trường hợp mutable object cần thiết với session, Spring binding và JPA.
- Checkstyle dùng đúng `inputEncoding`, quét cả production và test source, đồng thời fail build khi có vi phạm.
- Quy ước tên test BDD có dấu gạch dưới được giữ bằng suppression chỉ giới hạn trong `src/test/java`; production method vẫn theo camelCase.
- Entrypoint `ai-service/main.py` được đưa vào phạm vi Flake8/Pylint; intentional re-export `main:app` có suppression cục bộ kèm lý do.
- Cấu hình CORS wildcard trong `WebConfiguration` để Swagger UI và client bên ngoài gọi API theo yêu cầu hiện tại.
- Thêm `flake8` và `pylint` vào dependency của `ai-service`.

---

## 3. Hướng dẫn thiết lập

### 3.1. Chuẩn bị dự án

Tạo file môi trường khi cài đặt trên máy mới:

```powershell
Copy-Item .env.example .env
```

Trên Linux hoặc macOS:

```bash
cp .env.example .env
```

Nếu cần chạy toàn bộ ứng dụng:

```bash
docker compose up -d --build
```

Nếu chạy Python linter trực tiếp trên máy:

```bash
pip install -r ai-service/requirements.txt
```

### 3.2. Khởi động SonarQube

SonarQube chạy trong container độc lập và không yêu cầu rebuild image ShoeShop:

```powershell
docker run -d `
  --name sonarqube `
  -p 9000:9000 `
  -e SONAR_ES_BOOTSTRAP_CHECKS_DISABLE=true `
  -v sonarqube_data:/opt/sonarqube/data `
  -v sonarqube_extensions:/opt/sonarqube/extensions `
  -v sonarqube_logs:/opt/sonarqube/logs `
  sonarqube:latest
```

Kiểm tra trạng thái:

```powershell
Invoke-RestMethod http://localhost:9000/api/system/status
```

Chỉ chạy scanner khi API trả về `UP`.

---

## 4. Hướng dẫn chạy kiểm tra

Chạy các lệnh tại thư mục root của dự án.

### 4.1. Checkstyle

```bash
mvn checkstyle:check
```

### 4.2. SpotBugs

```bash
mvn compile spotbugs:check
```

### 4.3. Flake8

```bash
python -m flake8 --config=ai-service/.flake8 ai-service/main.py ai-service/app
```

### 4.4. Pylint

```bash
python -m pylint --rcfile=ai-service/.pylintrc ai-service/main.py ai-service/app
```

### 4.5. SonarQube

```powershell
mvn clean verify sonar:sonar -DskipTests `
  "-Dsonar.host.url=http://localhost:9000" `
  "-Dsonar.token=$env:SONAR_TOKEN"
```

Dashboard cục bộ:

```text
http://localhost:9000/dashboard?id=shoeshop
```

### 4.6. Chạy các static check cục bộ trong một lượt

```powershell
mvn checkstyle:check
mvn compile spotbugs:check
python -m flake8 --config=ai-service/.flake8 ai-service/main.py ai-service/app
python -m pylint --rcfile=ai-service/.pylintrc ai-service/main.py ai-service/app
```

SonarQube được chạy riêng vì cần server ở trạng thái `UP` và cần analysis token.

---

## 5. Kết quả nghiệm thu

### 5.1. Kết quả theo công cụ

| Công cụ | Kết quả thực tế | Trạng thái |
|---|---|---|
| Checkstyle | Production và test source: Maven build thành công, 0 violation. | `PASSED` |
| SpotBugs | Maven build thành công, 0 bug và 0 error. | `PASSED` |
| Flake8 | Toàn bộ `ai-service/main.py` và `ai-service/app`: exit code 0. | `PASSED` |
| Pylint | Toàn bộ `ai-service/main.py` và `ai-service/app`: 10,00/10. | `PASSED` |
| SonarQube | Analysis thành công, Quality Gate `OK`. | `COMPLETED` |

### 5.2. Phạm vi kết quả SonarQube

| Thuộc tính | Kết quả |
|---|---|
| Project | `ShoeShop` |
| Project key | `shoeshop` |
| Maven plugin | `3.10.0.2594` |
| Ngôn ngữ được nhận diện | Java và XML |
| Tổng tệp được lập chỉ mục | 68 |
| Java production | 62 tệp |
| Java test | 5 tệp |

### 5.3. Chỉ số SonarQube

| Chỉ số | Kết quả |
|---|---:|
| Quality Gate | `OK` |
| Tổng issue chưa xử lý | 294 |
| Bug | 1 |
| Vulnerability | 30 |
| Code smell | 263 |
| Security hotspot | 0 |
| Coverage | 0,0% |
| Mật độ dòng trùng lặp | 10,9% |
| Số dòng code | 5.966 |

Quality Gate `OK` chủ yếu phản ánh điều kiện trên **new code**. Kết quả này không có nghĩa 294 issue hiện hữu đã được xử lý.

### 5.4. Issue theo mức độ

| Mức độ | Số lượng |
|---|---:|
| Blocker | 0 |
| High/Critical | 103 |
| Medium/Major | 97 |
| Low/Minor | 71 |
| Info | 23 |

SonarQube đồng thời ghi nhận 30 phát hiện Security, 48 Reliability và 283 Maintainability. Một issue có thể ảnh hưởng nhiều thuộc tính nên tổng ba nhóm lớn hơn 294.

### 5.5. Các rule xuất hiện nhiều nhất

| Rule | Số lượng | Nội dung |
|---|---:|---|
| `java:S1192` | 58 | Chuỗi literal lặp lại; cân nhắc tách thành hằng số. |
| `java:S6813` | 47 | Field injection; cân nhắc constructor injection. |
| `java:S4488` | 35 | Có thể dùng annotation mapping chuyên biệt như `@GetMapping`. |
| `java:S1452` | 32 | Kiểu trả về công khai sử dụng generic wildcard. |
| `java:S2077` | 20 | SQL/HQL được tạo động; cần xác minh nguy cơ injection. |
| `java:S2143` | 18 | API ngày giờ cũ; cân nhắc chuyển sang `java.time`. |
| `java:S7158` | 11 | Có thể dùng `String.isEmpty()`. |
| `java:S1128` | 9 | Import không sử dụng trong mã kiểm thử. |
| `java:S107` | 7 | Constructor hoặc phương thức có nhiều tham số. |
| `java:S1186` | 6 | Phương thức rỗng cần triển khai hoặc giải thích. |
| `java:S3752` | 6 | Endpoint chưa giới hạn rõ HTTP method. |
| `java:S3776` | 6 | Cognitive Complexity cao hơn ngưỡng. |

### 5.6. Các phát hiện cần ưu tiên đánh giá

- `java:S4502`: CSRF đang bị tắt trong `WebSecurityConfig.java`.
- `java:S5122`: CORS wildcard `*` trong `WebConfiguration.java`.
- `java:S2077`: 20 vị trí tạo SQL/HQL động trong các DAO.
- `java:S2245`: Bộ sinh số giả ngẫu nhiên trong `ProductDAO.java`.
- `java:S4507`: Debug hoặc in stack trace cần được rà soát trước production.
- `java:S2184`: Phép tính số học trong `ProductDAO.java` cần kiểm tra kiểu toán hạng.
- `ProductDAO.queryProducts`: phương thức dài, nhiều nhánh và có độ phức tạp cao.

Các phát hiện SonarQube là dữ kiện phân tích tĩnh, không mặc định là lỗi thực. CORS, CSRF và truy vấn dữ liệu phải được đối chiếu với kiến trúc và test hồi quy trước khi thay đổi.

---

## 6. Phần còn thiếu và việc cần làm tiếp

### 6.1. Test và coverage

- Lần quét SonarQube sử dụng `-DskipTests`.
- Chưa có báo cáo JaCoCo XML nên SonarQube ghi nhận coverage 0%.
- Cần chạy unit/integration test khi database test hoạt động.
- Nếu coverage là tiêu chí nghiệm thu, cần cấu hình `jacoco-maven-plugin` và truyền báo cáo XML cho SonarQube.

### 6.2. Python trên SonarQube

- Sonar Maven Scanner chỉ nhận diện Java và XML trong lần quét hiện tại.
- Python trong `ai-service` chưa xuất hiện trên dashboard SonarQube.
- Flake8 và Pylint hiện vẫn là nguồn kiểm tra chính cho Python.
- Nếu cần dashboard chung, phải cấu hình source scope và analyzer Python phù hợp.

### 6.3. Bảo mật và môi trường

- Sonar token phải được lưu trong secret store hoặc biến môi trường, không commit vào repository.
- Embedded database của SonarQube chỉ phù hợp cho đánh giá cục bộ; production cần database được hỗ trợ.
- CORS wildcard và CSRF disabled phải được đánh giá lại trước production.
- 30 vulnerability cần được phân loại thành lỗi thực, false positive hoặc rủi ro được chấp nhận kèm lý do.

### 6.4. Chất lượng mã nguồn

- 294 issue SonarQube mới được ghi nhận, chưa được refactor.
- Nên ưu tiên Security và bug trước các code smell mang tính phong cách.
- Sau mỗi nhóm sửa cần chạy compile, test và toàn bộ công cụ phân tích lại.
- Cần điều chỉnh Quality Gate nếu dự án muốn kiểm soát overall code thay vì chỉ new code.

---

## 7. Tài liệu tham khảo

| Công cụ | Repository | Tài liệu |
|---|---|---|
| Checkstyle | [checkstyle/checkstyle](https://github.com/checkstyle/checkstyle) | [checkstyle.org](https://checkstyle.org/) |
| Maven Checkstyle Plugin | [apache/maven-checkstyle-plugin](https://github.com/apache/maven-checkstyle-plugin) | [Maven plugin documentation](https://maven.apache.org/plugins/maven-checkstyle-plugin/) |
| SpotBugs | [spotbugs/spotbugs](https://github.com/spotbugs/spotbugs) | [spotbugs.github.io](https://spotbugs.github.io/) |
| Flake8 | [PyCQA/flake8](https://github.com/PyCQA/flake8) | [flake8.pycqa.org](https://flake8.pycqa.org/en/latest/) |
| Pylint | [pylint-dev/pylint](https://github.com/pylint-dev/pylint) | [pylint.pycqa.org](https://pylint.pycqa.org/en/latest/) |
| SonarQube | [SonarSource/sonarqube](https://github.com/SonarSource/sonarqube) | [docs.sonarsource.com](https://docs.sonarsource.com/sonarqube/latest/) |

---

## 8. Trạng thái mã nguồn

Tài liệu này ghi nhận cấu hình, hướng dẫn và kết quả kiểm tra. Việc cập nhật README không refactor hoặc thay đổi logic Java/Python.
