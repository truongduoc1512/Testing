# TEST-11 — Develop DAO unit tests

## 1. Thông tin task

- Mã task: `TEST-11`.
- Tên task: `Develop DAO unit tests`.
- Mô tả: `Repository layer unit tests`.
- Mục tiêu: kiểm thử hành vi hiện tại của tầng DAO.
- Phạm vi production: không thay đổi code DAO.
- Phạm vi test: 8 DAO unit suites và 1 ProductDAO integration suite.
- Kết quả acceptance: `338/338` test pass.

## 2. Tổng số test

| DAO | Public methods/overloads | Test methods | Unit invocations | Pass |
|---|---:|---:|---:|---:|
| AccountDAO | 8 | 14 | 28 | 28 |
| OrderDAO | 16 | 38 | 73 | 73 |
| OrderReturnDAO | 4 | 24 | 49 | 49 |
| ProductDAO | 11 | 27 | 49 | 49 |
| ProductReviewDAO | 5 | 17 | 36 | 36 |
| UserAddressDAO | 6 | 23 | 25 | 25 |
| VoucherDAO | 9 | 26 | 44 | 44 |
| WishlistDAO | 6 | 20 | 26 | 26 |
| **Tổng unit** | **65** | **189** | **330** | **330** |

- ProductDAO integration: 8 test methods, 8 invocations.
- Tổng acceptance: `330 unit + 8 integration = 338 tests`.
- Failures: `0`.
- Errors: `0`.
- Skipped: `0`.

## 3. Cách xác định test case

Công thức bao phủ hành vi:

`C = H + G + B + E + X`

- `H`: happy path của public method hoặc overload.
- `G`: guard, null, empty, blank và early return độc lập.
- `B`: nhánh role, status, owner và business rule.
- `E`: boundary trước, tại và sau ngưỡng.
- `X`: persistence, lock, flush, side effect và dependency failure.
- Không nhân Cartesian các input tạo cùng một execution path.
- Một parameterized method có thể tạo nhiều test invocation.

Số unit invocation theo suite:

| Suite | Công thức invocation | Total |
|---|---|---:|
| AccountDAOTest | `8 single + (3+3+3+5+2+4) parameter rows` | 28 |
| OrderDAOTest | `24 single + 49 parameter rows` | 73 |
| OrderReturnDAOTest | `15 single + 34 parameter rows` | 49 |
| ProductDAOTest | `23 single + 26 parameter rows` | 49 |
| ProductReviewDAOTest | `13 single + 23 parameter rows` | 36 |
| UserAddressDAOTest | `22 single + 3 parameter rows` | 25 |
| VoucherDAOTest | `16 single + 28 parameter rows` | 44 |
| WishlistDAOTest | `16 single + 10 parameter rows` | 26 |
| **Tổng** | `137 single + 193 parameter rows` | **330** |

## 4. Input và output

### 4.1. AccountDAO

- Input: username, email, reset token, expiry, page và page size.
- Output: account hoặc null, token hash, boolean, page và admin count.
- Kiểm tra raw username được chuyển nguyên trạng theo code hiện tại.
- Kiểm tra non-blank email được chuyển nguyên trạng theo code hiện tại.
- Kiểm tra token invalid, expired, missing và update count.

### 4.2. OrderDAO

- Input: cart, customer, order lines, stock, voucher, role và status.
- Output: order, details, access result, revenue và count.
- Side effect: stock, sales, voucher usage, persist và flush.
- Kiểm tra invalid quantity, thiếu product và thiếu quyền.

### 4.3. OrderReturnDAO

- Input: username, order ID, return form, action và note.
- Output: return entity, boolean hoặc exception.
- Side effect: status transition và khôi phục stock.
- Kiểm tra owner, role, trạng thái và dữ liệu không tồn tại.

### 4.4. ProductDAO

- Input: product code, form, owner, image, filter, page và sort.
- Output: product, product info hoặc page.
- Side effect: create, update, soft delete và pessimistic lock.
- Kiểm tra owner mismatch, boundary và dependency failure.

### 4.5. ProductReviewDAO

- Input: review, product, rating, comment, time và aggregate.
- Output: review, list hoặc boolean.
- Side effect: rating cache và review count.
- Kiểm tra nullable aggregate và làm tròn rating.

### 4.6. UserAddressDAO

- Input: username, address ID, form và default flag.
- Output: address, list, boolean hoặc null.
- Side effect: unset default, promote default và delete.
- Kiểm tra ownership và address không tồn tại.

### 4.7. VoucherDAO

- Input: voucher code, order amount, expiry, usage và discount type.
- Output: apply result, discount và final amount.
- Side effect: usage persist và write lock.
- Kiểm tra limit, minimum order, date window và code trống.

### 4.8. WishlistDAO

- Input: username, product code, provider rows và count.
- Output: wishlist, boolean, product list hoặc count.
- Side effect: persist hoặc delete.
- Kiểm tra duplicate, missing product và missing account.

## 5. ProductDAO integration

Integration suite dùng Spring Boot, Hibernate và MySQL thật.

1. Create form → row được commit và mapped fields đúng.
2. Update cùng owner → transaction sau đọc được dữ liệu mới.
3. Update khác owner → access bị từ chối và row không đổi.
4. Delete code → status thành `INACTIVE`.
5. Query filter/sort → projection và effective price đúng.
6. Image bytes → blob đọc lại giống input.
7. Find for update → đọc entity qua pessimistic write lock.
8. Cleanup scope → không match product ngoài prefix test.

## 6. Giải pháp triển khai

- Mockito cô lập SessionFactory, Session, Query và dependency DAO.
- ArgumentCaptor xác nhận entity và side effect được persist.
- Parameterized tests bao phủ input tương đương theo nhóm.
- Characterization tests khóa hành vi đang tồn tại của DAO.
- Integration test được opt-in bằng system property.
- Datasource lấy từ Spring environment.
- Prefix product sinh bằng UUID cho từng JVM.
- Test code không vượt giới hạn 20 ký tự của `Products.CODE`.
- Cleanup chạy trước và sau từng integration test.
- Cleanup dùng so sánh prefix chính xác bằng `LEFT`.
- Không disable test và không bỏ assertion để đạt kết quả pass.
- Không sửa production DAO trong phạm vi TEST-11.

## 7. Điều kiện chạy

- Chạy lệnh tại root project.
- Java và Maven phải có trong `PATH`.
- Unit suite không cần Docker hoặc database.
- Integration suite cần MySQL đã khởi động và migrate schema.
- Docker Compose hiện expose MySQL local tại cổng `3307`.
- Database local Compose là `shoe_shopdb`.
- Máy khác có thể truyền datasource bằng environment.
- Username/password dùng cấu hình local, không hard-code vào test.

## 8. Cách chạy

### 8.1. Unit — 330 tests

```powershell
mvn "-Dtest=AccountDAOTest,OrderDAOTest,OrderReturnDAOTest,ProductDAOTest,ProductReviewDAOTest,UserAddressDAOTest,VoucherDAOTest,WishlistDAOTest" test
```

### 8.2. Integration — 8 tests

```powershell
$env:SPRING_DATASOURCE_URL='jdbc:mysql://localhost:3307/shoe_shopdb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC'
mvn "-Ddao.integration.enabled=true" "-Dtest=ProductDAOIntegrationTest" test
```

### 8.3. Acceptance — 338 tests

```powershell
$env:SPRING_DATASOURCE_URL='jdbc:mysql://localhost:3307/shoe_shopdb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC'
mvn "-Ddao.integration.enabled=true" "-Dtest=AccountDAOTest,OrderDAOTest,OrderReturnDAOTest,ProductDAOTest,ProductDAOIntegrationTest,ProductReviewDAOTest,UserAddressDAOTest,VoucherDAOTest,WishlistDAOTest" test
```

## 9. Output mong đợi

Unit:

```text
Tests run: 330, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Integration:

```text
Tests run: 8, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Acceptance:

```text
Tests run: 338, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## 10. Kết quả thực tế

Unit run:

- Start: `2026-08-10 00:44:34.476 +07:00`.
- End: `2026-08-10 00:44:44.339 +07:00`.
- Wall-clock: `9.863 s`.
- Maven time: `8.372 s`.
- Result: `330/330` pass.

Acceptance run:

- Start: `2026-08-10 00:45:07.920 +07:00`.
- End: `2026-08-10 00:45:31.225 +07:00`.
- Wall-clock: `23.305 s`.
- Maven time: `21.839 s`.
- Product integration suite: `15.746 s`.
- Result: `338/338` pass.
- Failures/errors/skipped: `0/0/0`.

## 11. Database result

- Test product prefix: `ITP_` kết hợp UUID riêng cho mỗi JVM.
- Truy vấn hậu kiểm sau acceptance: `0` product test.
- Không còn dữ liệu integration do TEST-11 tạo.
- Cleanup chỉ tác động row có prefix của lần chạy.
- Dữ liệu product ngoài prefix không thuộc phạm vi cleanup.

## 12. Kết luận

- Task đáp ứng repository layer unit tests cho 8 DAO.
- Unit suite chạy độc lập trên thiết bị không có database.
- Integration suite xác nhận hành vi persistence quan trọng của ProductDAO.
- Acceptance suite xác nhận 338 test cùng chạy thành công.
- Database sạch sau lần chạy acceptance.
- Production logic hiện tại không bị thay đổi.
