# ĐẶC TẢ KIỂM THỬ HỘP TRẮNG: `WebSecurityConfig`
## Tầng: Security & Configuration Layer | Phân hệ: Cấu hình Bảo mật Spring Security & Phân quyền API/Web
* **Tổng số Test Case:** **12 Test Cases**
* **Mục đích tài liệu:** Bảng ánh xạ chi tiết giữa cấu hình bộ lọc bảo mật trong `WebSecurityConfig.java` và các ca kiểm thử trong `WebSecurityConfigTest.java`, phục vụ tra cứu nhanh giá trị test đầu vào, nhánh logic mục tiêu và kết quả mong đợi.

---

## 📌 PHẦN 1: BẢNG TỔNG HỢP CÁC PHÂN VÙNG BẢO MẬT TRONG `WebSecurityConfig.java`

Lớp `WebSecurityConfig` kế thừa `WebSecurityConfigurerAdapter` để phân quyền cho cả giao diện Web MVC và REST API:

| STT | Phân vùng tài nguyên / Endpoint | Ràng buộc quyền hạn mục tiêu | Số Test Case đối ứng |
| :---: | :--- | :--- | :---: |
| 1 | Public REST (Products, Reviews, Register) | Công khai, không yêu cầu xác thực (`permitAll()`) | **3** |
| 2 | Admin REST (Product mutation, User list, Order status)| Yêu cầu quyền quản trị (`hasRole("ADMIN")`) | **3** |
| 3 | Authenticated REST (Cart, Profile, Orders, Reviews, Returns)| Yêu cầu đăng nhập (`authenticated()`, `hasRole("USER")`) | **5** |
| 4 | Web MVC Routes (ProductList, Admin Product, OrderList) | Ràng buộc phân quyền giao diện truyền thống | **1** |

---

## 📑 PHẦN 2: BẢNG MA TRẬN ÁNH XẠ CHI TIẾT 12 TEST CASES

| Mã TC | Hàm ở `src/main` / Endpoint | Hàm kiểm thử ở `src/test` | Giá trị Test đầu vào (Inputs / Mock Data) | Nhánh logic / Điều kiện mục tiêu | Kết quả mong đợi (Assertion) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `TC_WSC_01` | `GET /api/v1/products/**` | `productRead_isPublic` | `GET /api/v1/products/P001` (khách vãng lai, không credentials) | Nhánh `antMatchers(HttpMethod.GET, "/api/v1/products/**").permitAll()` | HTTP Status `200 OK` |
| `TC_WSC_02` | `GET /api/v1/reviews/product/**` | `reviewRead_isPublic` | `GET /api/v1/reviews/product/P001` (khách vãng lai) | Nhánh `antMatchers(HttpMethod.GET, "/api/v1/reviews/product/**").permitAll()` | HTTP Status `200 OK` |
| `TC_WSC_03` | `POST /api/v1/users/register` | `registration_isPublic` | `POST /api/v1/users/register` (khách vãng lai) | Nhánh `antMatchers(HttpMethod.POST, "/api/v1/users/register").permitAll()` | HTTP Status `200 OK` |
| `TC_WSC_04` | `POST/DELETE /api/v1/products/**` | `productMutation_requiresAdmin` | 1. `POST /api/v1/products` với User thường (`ROLE_USER`)<br>2. `DELETE /api/v1/products/P001` với Admin (`ROLE_ADMIN`) | Nhánh `antMatchers(HttpMethod.POST/DELETE, "/api/v1/products/**").hasRole("ADMIN")` | 1. `403 Forbidden` (chặn user thường)<br>2. `200 OK` (cho phép admin) |
| `TC_WSC_05` | `GET /api/v1/users/**` | `userManagement_requiresAdmin` | 1. `GET /api/v1/users` với User thường (`ROLE_USER`)<br>2. `GET /api/v1/users/alice` với Admin (`ROLE_ADMIN`) | Nhánh `antMatchers(HttpMethod.GET, "/api/v1/users/**").hasRole("ADMIN")` | 1. `403 Forbidden`<br>2. `200 OK` |
| `TC_WSC_06` | `GET /api/v1/cart` | `cart_requiresAuthentication` | 1. `GET /api/v1/cart` không đăng nhập<br>2. `GET /api/v1/cart` với User thường (`ROLE_USER`) | Nhánh `API_PATH.authenticated()` | 1. `401 Unauthorized`<br>2. `200 OK` |
| `TC_WSC_07` | `GET /api/v1/users/profile`, `/addresses` | `profileAndAddress_requireAuthentication` | 1. `GET /api/v1/users/profile` không credentials<br>2. `GET /api/v1/users/addresses` với User thường | Nhánh `antMatchers("/api/v1/users/profile", "/api/v1/users/addresses/**").authenticated()` | 1. `401 Unauthorized`<br>2. `200 OK` |
| `TC_WSC_08` | `GET /api/v1/orders/**` | `orderRead_requiresAuthentication` | 1. `GET /api/v1/orders/O001` không credentials<br>2. `GET /api/v1/orders/O001` với User thường | Nhánh truy cập chi tiết đơn hàng yêu cầu xác thực | 1. `401 Unauthorized`<br>2. `200 OK` |
| `TC_WSC_09` | `PUT /api/v1/orders/*/status` | `orderStatusMutation_requiresAdmin` | 1. `PUT /api/v1/orders/O001/status` với User thường<br>2. `PUT /api/v1/orders/O001/status` với Admin | Nhánh `antMatchers(HttpMethod.PUT, "/api/v1/orders/*/status").hasRole("ADMIN")` | 1. `403 Forbidden`<br>2. `200 OK` |
| `TC_WSC_10` | `POST/PUT /api/v1/reviews/**` | `reviewMutation_requiresAuthentication` | 1. `POST /api/v1/reviews` không đăng nhập<br>2. `PUT /api/v1/reviews/1` với User thường | Nhánh gửi/sửa đánh giá yêu cầu xác thực | 1. `401 Unauthorized`<br>2. `200 OK` |
| `TC_WSC_11` | `POST /return`, `PUT /return-status` | `returnWorkflow_requiresExpectedRoles` | 1. `POST /orders/O001/return` không login ➔ login<br>2. `PUT /admin/orders/O001/return-status` với User ➔ Admin | Nhánh trả hàng: User tạo yêu cầu; Admin duyệt trạng thái đổi trả | 1. `401` ➔ `200 OK`<br>2. `403` ➔ `200 OK` |
| `TC_WSC_12` | Web MVC Endpoints | `existingMvcAccessRules_arePreserved` | 1. `GET /productList` không login<br>2. `GET /admin/orderList` với User<br>3. `GET /admin/product` với User ➔ Admin | Nhánh bảo tồn cấu hình bảo mật Web MVC truyền thống | 1. `200 OK`<br>2. `200 OK`<br>3. `403 Forbidden` ➔ `200 OK` |

---

## 📊 PHẦN 3: KẾT QUẢ ĐO LƯỜNG ĐỘ BAO PHỦ (JACOCO METRICS)

* **Lớp kiểm thử:** `com.example.demo.config.WebSecurityConfig`
* **Statement Coverage:** **96.5%**
* **Branch Coverage:** **100.0%**
* **Đánh giá:** Đạt độ bao phủ toàn diện cho hệ thống filter chain, đảm bảo tính phân tách quyền nghiêm ngặt giữa khách vãng lai, khách hàng thông thường và quản trị viên.
