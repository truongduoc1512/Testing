# JIRA TASK: TEST-8 - Design Auth API Test Cases

**Dự án:** ShoeShop Testing & Quality Assurance System  
**Mã tài liệu:** TC-AUTH-API-v1.0  
**Tác giả:** Senior QA/QC Automation Engineer  
**Kỹ thuật áp dụng:** Equivalence Partitioning (EP) & Boundary Value Analysis (BVA)  


## 1. Phân tích Vùng tương đương (EP) & Giá trị biên (BVA)

| Trường dữ liệu | Quy tắc / Giới hạn thiết kế | EP Valid Partition | EP Invalid Partition | BVA Boundary Points (Độ dài / Giá trị) |
| :--- | :--- | :--- | :--- | :--- |
| **Email** | Standard RFC 5322, Min: 5, Max: 255 chars | Đúng format (`user@domain.com`), email chưa tồn tại | Sai format (`user@`, `@domain.com`, chứa space), email đã tồn tại | **0** (Rỗng), **4** (Min-1), **5** (Min), **6** (Min+1), **254** (Max-1), **255** (Max), **256** (Max+1) |
| **Username** | Alphanumeric + `_`, Min: 3, Max: 30 chars | `user_123`, chưa tồn tại | Chứa ký tự đặc biệt (`user@1`), chứa space, trùng lặp | **0** (Rỗng), **2** (Min-1), **3** (Min), **4** (Min+1), **29** (Max-1), **30** (Max), **31** (Max+1) |
| **Password** | Min: 8, Max: 32 chars, gồm: Hoa, Thường, Số, Ký tự đặc biệt (`@#$%^&*`) | `P@ssw0rd123` | Chỉ chữ/chỉ số, thiếu ký tự hoa/đặc biệt, chứa space | **0** (Rỗng), **7** (Min-1), **8** (Min), **9** (Min+1), **31** (Max-1), **32** (Max), **33** (Max+1) |
| **Confirm Password**| Trùng khớp 100% với Password | Giống hệt Password | Khác 1 ký tự, khác hoa/thường, rỗng | N/A (So sánh chuỗi match / mismatch) |
| **OTP Code** | Đúng 6 chữ số, thời gian sống (TTL): 5 phút | `123456` (còn hạn) | Chứa chữ cái, sai số, hết hạn (TTL > 5m), đã sử dụng | **0** (Rỗng), **5 chữ số** (Min-1), **6 chữ số** (Exact), **7 chữ số** (Max+1) |
| **Reset Token** | JWT / UUID hợp lệ | Token chưa bị sửa đổi & còn hạn | Token bị tamper, token hết hạn, token rỗng | N/A (Kiểm thử tính hợp lệ JWT Signature & TTL) |

---

## 2. Bảng Danh sách Test Cases (Markdown Format)

###  Module 1: Đăng ký tài khoản (`POST /api/auth/register`)

| Test Case ID | Feature / API Endpoint | Test Technique | Test Description | Input Data | Expected Output |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **TC_AUTH_REG_EP_001** | `POST /api/auth/register` | EP (Valid) | Đăng ký thành công với dữ liệu hợp lệ đầy đủ | `username`: "john_doe", `email`: "john@example.com", `password`: "P@ssw0rd123", `confirmPassword`: "P@ssw0rd123" | `201 Created`<br>`{"status": "success", "message": "User registered successfully", "data": {"userId": 101, "username": "john_doe"}}` |
| **TC_AUTH_REG_EP_002** | `POST /api/auth/register` | EP (Invalid) | Đăng ký thất bại do Email đã tồn tại | `email`: "existing@example.com" (email đã có trong DB), `password`: "P@ssw0rd123" | `409 Conflict`<br>`{"status": "fail", "code": "EMAIL_ALREADY_EXISTS", "message": "Email is already registered"}` |
| **TC_AUTH_REG_EP_003** | `POST /api/auth/register` | EP (Invalid) | Đăng ký thất bại do Username đã tồn tại | `username`: "existing_user" (username đã có trong DB) | `409 Conflict`<br>`{"status": "fail", "code": "USERNAME_ALREADY_EXISTS", "message": "Username is already taken"}` |
| **TC_AUTH_REG_EP_004** | `POST /api/auth/register` | EP (Invalid) | Đăng ký thất bại do Email sai định dạng | `email`: "invalid_email_format.com" | `400 Bad Request`<br>`{"status": "fail", "code": "INVALID_EMAIL_FORMAT", "message": "Email must be a valid email address"}` |
| **TC_AUTH_REG_EP_005** | `POST /api/auth/register` | EP (Invalid) | Đăng ký thất bại do Confirm Password không khớp | `password`: "P@ssw0rd123", `confirmPassword`: "P@ssw0rd124" | `400 Bad Request`<br>`{"status": "fail", "code": "PASSWORD_MISMATCH", "message": "Password and Confirm Password do not match"}` |
| **TC_AUTH_REG_EP_006** | `POST /api/auth/register` | EP (Invalid) | Đăng ký thất bại do Mật khẩu thiếu ký tự đặc biệt | `password`: "Password123" | `400 Bad Request`<br>`{"status": "fail", "code": "WEAK_PASSWORD", "message": "Password must contain at least 1 special character"}` |
| **TC_AUTH_REG_BVA_001** | `POST /api/auth/register` | BVA (Min-1) | Password độ dài 7 ký tự (Min-1) | `password`: "P@ss123" (7 chars) | `400 Bad Request`<br>`{"status": "fail", "code": "INVALID_PASSWORD_LENGTH", "message": "Password length must be between 8 and 32 characters"}` |
| **TC_AUTH_REG_BVA_002** | `POST /api/auth/register` | BVA (Min) | Password độ dài 8 ký tự (Min) | `password`: "P@ssw0r1" (8 chars) | `201 Created`<br>`{"status": "success", "message": "User registered successfully"}` |
| **TC_AUTH_REG_BVA_003** | `POST /api/auth/register` | BVA (Min+1) | Password độ dài 9 ký tự (Min+1) | `password`: "P@ssw0rd1" (9 chars) | `201 Created`<br>`{"status": "success", "message": "User registered successfully"}` |
| **TC_AUTH_REG_BVA_004** | `POST /api/auth/register` | BVA (Max-1) | Password độ dài 31 ký tự (Max-1) | `password`: 31 chars hợp lệ | `201 Created`<br>`{"status": "success", "message": "User registered successfully"}` |
| **TC_AUTH_REG_BVA_005** | `POST /api/auth/register` | BVA (Max) | Password độ dài 32 ký tự (Max) | `password`: 32 chars hợp lệ | `201 Created`<br>`{"status": "success", "message": "User registered successfully"}` |
| **TC_AUTH_REG_BVA_006** | `POST /api/auth/register` | BVA (Max+1) | Password độ dài 33 ký tự (Max+1) | `password`: 33 chars hợp lệ | `400 Bad Request`<br>`{"status": "fail", "code": "INVALID_PASSWORD_LENGTH", "message": "Password length must be between 8 and 32 characters"}` |
| **TC_AUTH_REG_BVA_007** | `POST /api/auth/register` | BVA (Empty) | Gửi request với các trường rỗng `""` | `username`: "", `email`: "", `password`: "" | `400 Bad Request`<br>`{"status": "fail", "errors": [{"field": "email", "message": "Email is required"}, ...]}` |
| **TC_AUTH_REG_BVA_008** | `POST /api/auth/register` | BVA (Username Min-1) | Username độ dài 2 ký tự (Min-1) | `username`: "ab" | `400 Bad Request`<br>`{"status": "fail", "code": "INVALID_USERNAME_LENGTH", "message": "Username must be between 3 and 30 characters"}` |
| **TC_AUTH_REG_BVA_009** | `POST /api/auth/register` | BVA (Username Max+1) | Username độ dài 31 ký tự (Max+1) | `username`: 31 chars | `400 Bad Request`<br>`{"status": "fail", "code": "INVALID_USERNAME_LENGTH", "message": "Username must be between 3 and 30 characters"}` |

---

### Module 2: Đăng nhập (`POST /api/auth/login`)

| Test Case ID | Feature / API Endpoint | Test Technique | Test Description | Input Data | Expected Output |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **TC_AUTH_LGN_EP_001** | `POST /api/auth/login` | EP (Valid) | Đăng nhập thành công bằng Email + Password đúng | `usernameOrEmail`: "john@example.com", `password`: "P@ssw0rd123" | `200 OK`<br>`{"status": "success", "data": {"token": "eyJhbGci...", "tokenType": "Bearer", "expiresIn": 86400}}` |
| **TC_AUTH_LGN_EP_002** | `POST /api/auth/login` | EP (Valid) | Đăng nhập thành công bằng Username + Password đúng | `usernameOrEmail`: "john_doe", `password`: "P@ssw0rd123" | `200 OK`<br>`{"status": "success", "data": {"token": "eyJhbGci...", "tokenType": "Bearer", "expiresIn": 86400}}` |
| **TC_AUTH_LGN_EP_003** | `POST /api/auth/login` | EP (Invalid) | Đăng nhập thất bại do Email/Username không tồn tại | `usernameOrEmail`: "notfound@example.com", `password`: "P@ssw0rd123" | `401 Unauthorized`<br>`{"status": "fail", "code": "INVALID_CREDENTIALS", "message": "Invalid username/email or password"}` |
| **TC_AUTH_LGN_EP_004** | `POST /api/auth/login` | EP (Invalid) | Đăng nhập thất bại do Mật khẩu sai | `usernameOrEmail`: "john@example.com", `password`: "WrongP@ss123" | `401 Unauthorized`<br>`{"status": "fail", "code": "INVALID_CREDENTIALS", "message": "Invalid username/email or password"}` |
| **TC_AUTH_LGN_EP_005** | `POST /api/auth/login` | EP (Invalid) | Đăng nhập khi tài khoản bị khóa (Disabled/Inactive) | `usernameOrEmail`: "blocked_user@example.com", `password`: "P@ssw0rd123" | `403 Forbidden`<br>`{"status": "fail", "code": "ACCOUNT_DISABLED", "message": "Account is locked or disabled"}` |
| **TC_AUTH_LGN_BVA_001** | `POST /api/auth/login` | BVA (Empty Body) | Đăng nhập gửi chuỗi rỗng | `usernameOrEmail`: "", `password`: "" | `400 Bad Request`<br>`{"status": "fail", "code": "VALIDATION_ERROR", "message": "Username/Email and Password are required"}` |
| **TC_AUTH_LGN_BVA_002** | `POST /api/auth/login` | BVA (SQLi/XSS payload) | Đăng nhập truyền payload tấn công trong input | `usernameOrEmail`: "' OR '1'='1", `password`: "' OR '1'='1" | `400 Bad Request` hoặc `401 Unauthorized`<br>(Đảm bảo không bị SQL Injection) |

---

###  Module 3: Quên mật khẩu (`POST /api/auth/forgot-password`)

| Test Case ID | Feature / API Endpoint | Test Technique | Test Description | Input Data | Expected Output |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **TC_AUTH_FGT_EP_001** | `POST /api/auth/forgot-password` | EP (Valid) | Yêu cầu OTP quên mật khẩu với Email hợp lệ có trong hệ thống | `email`: "john@example.com" | `200 OK`<br>`{"status": "success", "message": "OTP has been sent to your email"}` |
| **TC_AUTH_FGT_EP_002** | `POST /api/auth/forgot-password` | EP (Invalid) | Yêu cầu OTP với Email không tồn tại trong hệ thống | `email`: "nonexistent@example.com" | `404 Not Found`<br>`{"status": "fail", "code": "EMAIL_NOT_FOUND", "message": "No account associated with this email"}` |
| **TC_AUTH_FGT_EP_003** | `POST /api/auth/forgot-password` | EP (Invalid) | Yêu cầu OTP với Email sai định dạng | `email`: "john_at_example.com" | `400 Bad Request`<br>`{"status": "fail", "code": "INVALID_EMAIL_FORMAT", "message": "Invalid email address"}` |
| **TC_AUTH_FGT_BVA_001** | `POST /api/auth/forgot-password` | BVA (Empty) | Truyền Email rỗng | `email`: "" | `400 Bad Request`<br>`{"status": "fail", "code": "REQUIRED_FIELD_MISSING", "message": "Email field is required"}` |
| **TC_AUTH_FGT_BVA_002** | `POST /api/auth/forgot-password` | BVA (Rate Limit) | Gửi liên tiếp 5 request trong 10 giây (Spam OTP) | `email`: "john@example.com" | `429 Too Many Requests`<br>`{"status": "fail", "code": "RATE_LIMIT_EXCEEDED", "message": "Please wait 60 seconds before requesting a new OTP"}` |

---

###  Module 4: Đặt lại mật khẩu (`POST /api/auth/reset-password`)

| Test Case ID | Feature / API Endpoint | Test Technique | Test Description | Input Data | Expected Output |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **TC_AUTH_RST_EP_001** | `POST /api/auth/reset-password` | EP (Valid) | Đặt lại mật khẩu thành công với OTP 6 số còn hạn | `email`: "john@example.com", `otp`: "123456", `newPassword`: "NewP@ssw0rd123", `confirmPassword`: "NewP@ssw0rd123" | `200 OK`<br>`{"status": "success", "message": "Password updated successfully"}` |
| **TC_AUTH_RST_EP_002** | `POST /api/auth/reset-password` | EP (Invalid) | Đặt lại mật khẩu thất bại do OTP sai số | `email`: "john@example.com", `otp`: "999999", `newPassword`: "NewP@ssw0rd123" | `400 Bad Request`<br>`{"status": "fail", "code": "INVALID_OTP", "message": "Invalid OTP code"}` |
| **TC_AUTH_RST_EP_003** | `POST /api/auth/reset-password` | EP (Invalid) | Đặt lại mật khẩu thất bại do OTP đã hết hạn (TTL > 5 min) | `email`: "john@example.com", `otp`: "123456" (hết hạn) | `400 Bad Request`<br>`{"status": "fail", "code": "EXPIRED_OTP", "message": "OTP code has expired"}` |
| **TC_AUTH_RST_EP_004** | `POST /api/auth/reset-password` | EP (Invalid) | Đặt lại mật khẩu thất bại do OTP chứa chữ cái | `email`: "john@example.com", `otp`: "12345A" | `400 Bad Request`<br>`{"status": "fail", "code": "INVALID_OTP_FORMAT", "message": "OTP must contain numbers only"}` |
| **TC_AUTH_RST_BVA_001** | `POST /api/auth/reset-password` | BVA (OTP Min-1) | OTP độ dài 5 chữ số (Min-1) | `otp`: "12345" | `400 Bad Request`<br>`{"status": "fail", "code": "INVALID_OTP_LENGTH", "message": "OTP code must be exactly 6 digits"}` |
| **TC_AUTH_RST_BVA_002** | `POST /api/auth/reset-password` | BVA (OTP Max+1) | OTP độ dài 7 chữ số (Max+1) | `otp`: "1234567" | `400 Bad Request`<br>`{"status": "fail", "code": "INVALID_OTP_LENGTH", "message": "OTP code must be exactly 6 digits"}` |
| **TC_AUTH_RST_BVA_003** | `POST /api/auth/reset-password` | BVA (Pass Min-1) | New Password độ dài 7 ký tự (Min-1) | `newPassword`: "N@wP123" (7 chars) | `400 Bad Request`<br>`{"status": "fail", "code": "INVALID_PASSWORD_LENGTH", "message": "Password length must be between 8 and 32 characters"}` |
| **TC_AUTH_RST_BVA_004** | `POST /api/auth/reset-password` | BVA (Pass Max+1) | New Password độ dài 33 ký tự (Max+1) | `newPassword`: 33 chars | `400 Bad Request`<br>`{"status": "fail", "code": "INVALID_PASSWORD_LENGTH", "message": "Password length must be between 8 and 32 characters"}` |

---

## 3. Mẫu Request Body (JSON) & Response minh họa

###  3.1 Scenario PASS Điển hình (`POST /api/auth/register`)

**Request Header:**
```http
POST /api/auth/register HTTP/1.1
Host: api.example.com
Content-Type: application/json
```

**Request Body (JSON):**
```json
{
  "username": "qa_automation_user",
  "email": "qa.engineer@example.com",
  "password": "SecureP@ssw0rd2026",
  "confirmPassword": "SecureP@ssw0rd2026"
}
```

**Response (HTTP Status 201 Created):**
```json
{
  "status": "success",
  "code": 201,
  "message": "User account created successfully",
  "data": {
    "userId": "usr_9f8e7d6c5b4a",
    "username": "qa_automation_user",
    "email": "qa.engineer@example.com",
    "createdAt": "2026-08-06T01:05:16Z"
  }
}
```
---

### 3.2 Scenario FAIL Điển hình (`POST /api/auth/reset-password`)

**Request Header:**
```http
POST /api/auth/reset-password HTTP/1.1
Host: api.example.com
Content-Type: application/json
```

**Request Body (JSON - Vi phạm BVA OTP & Password):**
```json
{
  "email": "qa.engineer@example.com",
  "otp": "12345",
  "newPassword": "short",
  "confirmPassword": "mismatch_password"
}
```

**Response (HTTP Status 400 Bad Request):**
```json
{
  "status": "fail",
  "code": 400,
  "message": "Validation failed for request parameters",
  "errors": [
    {
      "field": "otp",
      "code": "INVALID_OTP_LENGTH",
      "message": "OTP code must be exactly 6 digits"
    },
    {
      "field": "newPassword",
      "code": "INVALID_PASSWORD_LENGTH",
      "message": "Password length must be between 8 and 32 characters"
    },
    {
      "field": "confirmPassword",
      "code": "PASSWORD_MISMATCH",
      "message": "Confirm password does not match new password"
    }
  ]
}
```
