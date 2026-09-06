# Tests in `src/test`

## Mục đích

Tài liệu này mô tả riêng dữ kiện đầu vào, cách chạy, kết quả đầu ra, hiệu quả và phần coverage còn thiếu của test hiện tại.

## Cấu trúc

| Vị trí | Nội dung |
|---|---|
| `config` | Security filter chain, route, role và CSRF. |
| `controller` | MVC/template behavior; `controller/api` kiểm tra REST response. |
| `dao` | Persistence behavior qua mock. |
| `service` | Quy tắc service và authority normalization. |
| `utils` | Hàm thuần và input boundary. |
| `com/example/demo` | 5 integration test class cũ dùng Spring context và MySQL thật. |

Các package mirror `src/main/java` để giữ đúng Java package/import và xác định layer được test. Chúng không tạo thêm logic production.

## Targeted suite

### Dữ kiện đầu vào

- 8 test class hiện có trong các package `config`, `controller`, `dao`, `service`, `utils`.
- DAO/service phụ thuộc được mock ở test cô lập.
- Security test dùng MockMvc với anonymous, `ROLE_USER`, `ROLE_ADMIN` và CSRF có/không có.
- Không yêu cầu MySQL.

### Phạm vi và kết quả

| Test class | Test | Kết quả được khóa |
|---|---:|---|
| `WebSecurityConfigTest` | 12 | Public/protected route, role và CSRF trả đúng status. |
| `HomeControllerTest` | 1 | Không đọc file ngoài static root. |
| `ShoppingCartFinalizeTemplateTest` | 1 | Finalize template có binding bắt buộc và markup đóng đúng. |
| `ApiResponseTest` | 2 | Contract `success`/`message` ổn định. |
| `CartApiControllerTest` | 1 | Remove Product không tồn tại trả 404. |
| `WishlistDAOTest` | 1 | Persistence fail không trả success giả. |
| `UserDetailsServiceImplTest` | 2 | Role legacy và role có prefix được chuẩn hóa đúng. |
| `PageNumberParserTest` | 2 | Page sai/nonpositive về 1; page dương được giữ. |
| **Tổng** | **22** | **22 pass, 0 failure, 0 error, 0 skipped**. |

Source hiện không có `ProductDetailTemplateTest`. Kết quả 24 test trước đây đến từ class stale trong `target`; sau `mvn clean`, tổng đúng là 22.

### Cách chạy

```powershell
mvn -q '-Dtest=ApiResponseTest,WebSecurityConfigTest,HomeControllerTest,ShoppingCartFinalizeTemplateTest,CartApiControllerTest,WishlistDAOTest,UserDetailsServiceImplTest,PageNumberParserTest' test
```

Đầu ra cần có trong Surefire:

```text
Test classes: 8
Tests: 22
Failures: 0
Errors: 0
Skipped: 0
Maven exit code: 0
```

## Full suite

### Kết quả hiện tại

Lệnh `mvn -q test` đã chạy 13 class, tổng 35 test:

- 22 pass.
- 0 assertion failure.
- 13 error.
- 0 skipped.
- Maven exit code 1.

| Integration class | Test/error | Test body dự kiến kiểm tra |
|---|---:|---|
| `AddressBookTests` | 3/3 | Default Address và ownership delete. |
| `OrderCancelReturnTests` | 3/3 | Cancel/Return và hoàn tồn kho. |
| `SpringShoppingCart2ApplicationTests` | 1/1 | Spring context load. |
| `VoucherTests` | 5/5 | Discount, minimum value, expiry và usage limit. |
| `WishlistTests` | 1/1 | Add/remove Wishlist với persistence thật. |

### Vì sao có 13 environment error

13 error có cùng một nguyên nhân, không phải 13 lỗi business độc lập:

1. Cả 5 class trên dùng `@SpringBootTest`; 4 class đồng thời dùng `@Transactional`.
2. Project không có `src/test/resources`, `application-test.properties`, `@ActiveProfiles` hoặc datasource override cho test.
3. Vì vậy Spring test nạp datasource mặc định từ `src/main/resources/application.properties`.
4. Khi chạy Maven trên Windows, các biến `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` đều không được đặt.
5. Datasource fallback trỏ đến `localhost:3306/shoeshop_db`.
6. MySQL Docker đang healthy nhưng publish host port `3307` vào container port `3306`, và database Compose có tên `shoe_shopdb`. Như vậy cả port lẫn database name đều không khớp fallback.
7. Kiểm tra TCP tại thời điểm xác minh: `localhost:3306` không kết nối được; `localhost:3307` kết nối được.
8. Flyway đang bật và khởi tạo trước test method. Lần chạy hiện tại dừng ngay ở bước kết nối với SQL state `08S01`, `Communications link failure`, nên chưa đi đến bước kiểm tra database name hoặc chạy migration.
9. Spring `ApplicationContext` không load; Surefire vì thế ghi error cho toàn bộ 13 method trước khi có assertion nào được thực thi.

`@Transactional` không giải quyết được lỗi này vì transaction chỉ bắt đầu sau khi Spring context và datasource đã khởi tạo thành công.

### Điều kiện đầu vào để chạy full suite

Tối thiểu, Maven phải nhận đúng URL/username/password của một MySQL test database và Flyway phải có quyền migrate schema. Với Docker Compose hiện tại, kết nối từ host dùng port `3307` và database `shoe_shopdb`; kết nối từ container cùng network dùng service `shoeshop-mysql`, port `3306`.

Thiết lập biến cho đúng môi trường rồi chạy:

```powershell
$env:SPRING_DATASOURCE_URL='<JDBC URL của test database>'
$env:SPRING_DATASOURCE_USERNAME='<test username>'
$env:SPRING_DATASOURCE_PASSWORD='<test password>'
mvn -q test
```

Không dùng production database cho full suite. Bốn class `@Transactional` rollback dữ liệu test theo transaction, nhưng Flyway vẫn có thể thay đổi schema; `SpringShoppingCart2ApplicationTests` không có `@Transactional`.

Đầu ra đạt yêu cầu sau khi môi trường database đúng:

```text
Tests: 35
Failures: 0
Errors: 0
Skipped: 0
Maven exit code: 0
```

## Hiệu quả và phần còn thiếu

Hiệu quả đã xác nhận:

- 22 targeted test chạy độc lập với database và đều pass.
- Đã khóa security routing, CSRF, helper response, một Cart error branch, Wishlist failure propagation, role normalization, page parsing và finalize template.
- Full-suite failure đã được xác định là datasource port/profile mismatch trước test body, không phải regression assertion.

Coverage còn thiếu:

- Product controller business flow chưa có test riêng.
- Order/Return controller, transition và duplicate processing chưa có test cô lập.
- Cart mới cover remove/not-found; chưa cover add/update/customer/checkout/session/concurrency.
- User/profile/reset-token controller chưa có test riêng.
- Review create/update/delete/ownership/validation chưa có test riêng.
- Chưa có database test profile cô lập hoặc Testcontainers.
- Chưa xuất JaCoCo XML; Sonar đang nhận coverage 0%.
