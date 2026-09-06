# TEST-10 — Validator Unit Tests

## 1. Tóm tắt cho review

Jira `TEST-10` yêu cầu phát triển validator unit test bằng JUnit 5 và Mockito.
Phạm vi triển khai gồm ba validator, không mở rộng sang controller hoặc integration test.

| Validator | Input | Dependency được cô lập | Test |
| --- | --- | --- | ---: |
| `CustomerFormValidator` | `CustomerForm` | Không có | 20 |
| `ProductFormValidator` | `ProductForm` | `ProductDAO` | 27 |
| `RegisterFormValidator` | `RegisterForm` | `AccountDAO` | 24 |
| **Tổng** | | | **71** |

TEST-10 khóa các hành vi chính: required field, format, boundary, chuẩn hóa dữ
liệu, duplicate lookup, early return và error code trả về qua Spring `Errors`.

Trạng thái:

- 71/71 TEST-10 tests pass.
- 93/93 targeted regression tests pass.
- Compile, Checkstyle, SpotBugs, Flake8 và Pylint pass.
- Không cần MySQL, Docker hoặc SonarQube để chạy riêng TEST-10.

## 2. Thay đổi đã thực hiện

### 2.1. Refactor tiền đề

Phần refactor được hoàn tất trước khi tạo test để test không khóa hành vi mơ hồ:

| Khu vực | Thay đổi và mục đích |
| --- | --- |
| Customer | Trim dữ liệu; lowercase email; email rỗng chỉ nhận required error. |
| Product | Tách local rules khỏi duplicate lookup; edit không lookup trùng code hiện tại. |
| Product price | Chỉ chấp nhận số hữu hạn lớn hơn `0`; từ chối `NaN` và infinity. |
| Register | Trim username; trim/lowercase email trước lookup. |
| Early return | Không gọi DAO nếu required, format, match, length hoặc local rule đã lỗi. |
| Dependency | Dùng constructor injection cho `ProductDAO` và `AccountDAO`. |
| Reuse | MVC và REST dùng chung validator, không lặp lại validation rule. |
| Exception | Catch đăng ký dùng `DataIntegrityViolationException`, logging và response rõ ràng. |
| Constraint | Validator, entity, message và migration dùng cùng giới hạn. |
| Compatibility | Encode SHA-256 dạng hex không còn phụ thuộc API `HexFormat` của Java 17. |
| Cleanup | Không còn empty catch; không để lại code dead hoặc duplicate trong phạm vi sửa. |

### 2.2. Test source

Ba class `CustomerFormValidatorTest`, `ProductFormValidatorTest` và
`RegisterFormValidatorTest` được tạo trong `src/test/java/com/example/demo/validator/`.

### 2.3. Quality configuration

- Checkstyle quét production và test source, violation có severity `error`.
- Quy tắc tên BDD có dấu `_` chỉ được nới cho `src/test/java`.
- SpotBugs kiểm tra bytecode Java sau compile.
- Flake8 và Pylint quét cả `ai-service/main.py` và `ai-service/app`.
- Intentional re-export `main:app` có suppression Pylint cục bộ kèm lý do.

Các thay đổi quality configuration không làm đổi runtime logic.

## 3. Giải pháp test

### 3.1. Cách cô lập và xác nhận

- Dùng JUnit 5; parameterized test cho required value và boundary value.
- Khởi tạo validator trực tiếp, không dùng `@SpringBootTest` hoặc `@WebMvcTest`.
- Dùng Mockito cho `ProductDAO` và `AccountDAO`.
- Dùng `BeanPropertyBindingResult` thật thay vì mock `Errors`.
- Dùng valid fixture làm baseline; mỗi test chỉ đổi dữ liệu đang cần đo.
- `verify(...)` kiểm tra tham số DAO sau normalize.
- `verifyNoInteractions(...)` kiểm tra early return không query DAO.
- Mỗi test theo Arrange–Act–Assert: tạo input, gọi `validate`, kiểm tra output.

Test quan sát đồng thời form sau normalize, field/error code trong
`BindingResult`, và số lần gọi cùng tham số truyền vào DAO.

### 3.2. Phạm vi test cụ thể

#### Customer — 20 lượt test

| Nhóm | Input đại diện | Kết quả cần khóa | Lượt |
| --- | --- | --- | ---: |
| `supports()` | Form đúng/sai loại | `true`/`false` | 2 |
| Happy path | Whitespace, email chữ hoa | Trim/lowercase, không lỗi | 1 |
| Required | `null`/blank cho 4 field | Required error | 8 |
| Email format | Email sai định dạng | Pattern error | 1 |
| Boundary | Name/address 255–256; email/phone 128–129 | Đúng ngưỡng hợp lệ, vượt ngưỡng lỗi | 8 |

Email rỗng chỉ có required error; email quá dài chỉ có length error.

#### Product — 27 lượt test

| Nhóm | Input đại diện | Kết quả cần khóa | Lượt |
| --- | --- | --- | ---: |
| `supports()` | Form đúng/sai loại | `true`/`false` | 2 |
| Valid create/edit | Dữ liệu hợp lệ | Create lookup một lần; edit không lookup | 2 |
| Local rules | Gọi riêng local validation | Không gọi DAO | 1 |
| Required | Code/name `null` hoặc blank | Required error; không gọi DAO | 4 |
| Duplicate | DAO tìm thấy code | Duplicate error | 1 |
| Text boundary | Code 20–21; name 255–256 | Đúng ngưỡng hợp lệ, vượt ngưỡng lỗi | 4 |
| Price | `0`, âm, `NaN`, ±infinity, `0.01` | Giá không hợp lệ bị chặn | 6 |
| Stock | `0`, `-1` | `0` hợp lệ; âm bị chặn | 2 |
| Discount | `-1`, `0`, `100`, `101` | Chỉ chấp nhận `0..100` | 4 |
| Binding error | Price đã có `typeMismatch` | Không thêm lỗi trùng | 1 |

Mọi local error return trước duplicate lookup; edit không kiểm tra trùng chính nó.

#### Register — 24 lượt test

| Nhóm | Input đại diện | Kết quả cần khóa | Lượt |
| --- | --- | --- | ---: |
| `supports()` | Form đúng/sai loại | `true`/`false` | 2 |
| Happy path | Whitespace, email chữ hoa | Normalize; gọi hai lookup | 1 |
| Required | `null`/blank cho 4 field | Required error; không gọi DAO | 8 |
| Email/match | Email sai; confirm khác password | Đúng field/error; không gọi DAO | 2 |
| Duplicate | Username, email hoặc cả hai đã tồn tại | Trả đúng một hoặc hai lỗi | 3 |
| Boundary | Username 50–51; password 7–8/72–73; email 128–129 | Đúng ngưỡng hợp lệ, vượt ngưỡng lỗi | 8 |

DAO nhận username đã trim và email đã trim/lowercase. Lỗi local chặn lookup.

### 3.3. Error code được khóa

| Form | Error code |
| --- | --- |
| Customer | `NotEmpty.customerForm.*`, `Pattern.customerForm.email`, `Length.customerForm.*` |
| Product | `NotEmpty.productForm.*`, `Duplicate.productForm.code`, `Length.productForm.*`, `Min.productForm.price`, `Min.productForm.stockQuantity`, `Range.productForm.discountPercent` |
| Register | `NotEmpty.registerForm.*`, `Pattern.registerForm.email`, `Match.registerForm.confirmPassword`, `Duplicate.registerForm.*`, `Length.registerForm.*` |

## 4. Dữ liệu vào và kết quả ra

`validate(Object, Errors)` trả về `void`; kết quả nằm trong form đã normalize,
`Errors` và interaction với DAO, không phải JSON hoặc HTTP response.

| Trường hợp | Input | Output quan sát được |
| --- | --- | --- |
| Product sai giá | `code="P001"`, `price=0`, create mode | Lỗi `Min.productForm.price`; DAO không được gọi |
| Register hợp lệ | Username `"  alice  "`, email `"  ALICE@EXAMPLE.COM  "` | Giá trị thành `alice`/`alice@example.com`; không lỗi; DAO nhận dữ liệu đã normalize |

Khi test fail:

- `Failure`: kiểm tra rule, fixture, normalize và mock state.
- `Error`: kiểm tra null, Mockito stubbing và dependency runtime.
- Lỗi database từ full suite không đồng nghĩa validator unit test bị lỗi.

## 5. Cách chạy

Chạy các lệnh từ repository root bằng JDK 11 và Maven 3.9.x. Riêng TEST-10
không cần Spring context, MySQL, Docker hoặc SonarQube.

### 5.1. Chạy test

Chạy riêng một validator (đổi tên class khi cần):

```powershell
mvn "-Dtest=CustomerFormValidatorTest" test
```

Chạy toàn bộ TEST-10:

```powershell
mvn "-Dtest=CustomerFormValidatorTest,ProductFormValidatorTest,RegisterFormValidatorTest" test
```

Kết quả thành công: `Tests run: 71`, không failure/error/skipped và `BUILD SUCCESS`.

Chạy targeted regression 93 test không cần database:

```powershell
mvn -q "-Dtest=ApiResponseTest,WebSecurityConfigTest,HomeControllerTest,ShoppingCartFinalizeTemplateTest,CartApiControllerTest,WishlistDAOTest,UserDetailsServiceImplTest,PageNumberParserTest,CustomerFormValidatorTest,ProductFormValidatorTest,RegisterFormValidatorTest" test
```

### 5.2. Quality checks

```powershell
mvn -q -DskipTests compile
mvn checkstyle:check
mvn -q -DskipTests spotbugs:check
python -m flake8 --config=ai-service/.flake8 ai-service/main.py ai-service/app
python -m pylint --rcfile=ai-service/.pylintrc ai-service/main.py ai-service/app
git diff --check
```

## 6. Kết quả và hiệu quả

| Kiểm tra | Kết quả cục bộ |
| --- | --- |
| TEST-10 | 71 pass, 0 failure/error/skipped |
| Targeted regression | 93 pass, 0 failure/error/skipped |
| Compile | Pass |
| Checkstyle production + test | 0 violation |
| SpotBugs | 0 bug |
| Flake8 | Exit code 0 |
| Pylint | 10.00/10 |
| Empty catch scan | 0 |
| `git diff --check` | Pass |

Thiết kế này cho test chạy nhanh, không phụ thuộc Spring context hoặc database;
fixture và parameterized test giảm duplicate; Mockito khóa cả kết quả lẫn side
effect; dùng `BindingResult` thật giảm nguy cơ test pass giả.

Cảnh báo Maven từ `javassist` hoặc `sun.misc.Unsafe` thuộc tooling, không phải validator failure.

## 7. Giới hạn và khác biệt cần lưu ý

- TEST-8 cũ ghi username tối đa 30, production hiện dùng 50.
- TEST-8 cũ ghi password tối đa 32, production hiện dùng `8..72`.
- Validator chưa bắt buộc password có chữ hoa, chữ thường, số và ký tự đặc biệt.
- TEST-10 khóa hành vi production hiện tại, không tự thêm business rule chưa được duyệt.
- Chưa kiểm tra HTTP response end-to-end, UI/Thymeleaf, migration MySQL thật hoặc toàn bộ Spring integration suite.
- Chưa báo cáo phần trăm coverage vì dự án chưa cấu hình JaCoCo XML report.
- Full `mvn test` có integration test cần datasource/MySQL; dùng lệnh TEST-10 khi database chưa sẵn sàng.

## 8. File liên quan

- Test/production: `src/test/java/com/example/demo/validator/*ValidatorTest.java` và `src/main/java/com/example/demo/validator/*Validator.java`.
- Message/config: `src/main/resources/validation.properties`, `pom.xml`, `ai-service/.flake8`, `ai-service/.pylintrc`.
- Tài liệu TEST-10 cũ đã được hợp nhất vào file này để tránh sai lệch khi bảo trì.
