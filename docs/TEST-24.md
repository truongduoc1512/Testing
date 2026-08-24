# 🧪 TÀI LIỆU KIỂM THỬ TÍCH HỢP (TASK TEST-24)
> **Dự án:** ShoeShop Quality Assurance & Testing
> **Branch yêu cầu:** `test/w4-TEST-24-integration-testing`

---

## 📌 1. MỤC TIÊU

Kiểm tra luồng tích hợp thực tế giữa **Spring Boot Controller**, **Service**, **Hibernate DAO**, **MySQL** và **FastAPI**; đồng thời xác nhận dữ liệu nhất quán và được rollback sau test.

---

## 🔗 2. PHẠM VI TÍCH HỢP

```text
MockMvc -> CartApiController -> OrderCheckoutService
        -> OrderDAO -> Hibernate -> MySQL 8

MockMvc -> ProductController -> ProductImageAnalysisService
        -> FastAPI /api/v1/analyze -> ProductDAO -> MySQL 8
```

- Chạy Spring Boot context và security filter chain.
- DAO, Hibernate và MySQL không bị mock.
- MySQL 8 chạy biệt lập bằng Testcontainers với cổng ngẫu nhiên.
- AI dùng HTTP server kiểm soát và một test gọi FastAPI Python.

---

## 🎯 3. CÁC KỊCH BẢN KIỂM THỬ

| ID | Đầu vào/Kịch bản | Kết quả mong đợi | Kết quả |
| :--- | :--- | :--- | :---: |
| **IT-01** | Stock `10`, mua `3` | HTTP 201; tạo Order/Detail; stock `7`; sales tăng `3` | PASS |
| **IT-02** | Stock đổi còn `1` trước checkout | HTTP 400; không tạo Order/Detail; không trừ thêm stock | PASS |
| **IT-03** | Customer vượt giới hạn cột DB | Rollback Order, Detail, stock và sales | PASS |
| **IT-04** | AI trả `approved=true` | Gửi multipart `file`; Product và ảnh được lưu | PASS |
| **IT-05** | AI trả `approved=false` | Hiển thị reason/metrics; Product không được lưu | PASS |
| **IT-06** | AI trả HTTP 500 | Giữ hành vi fail-open; Product được lưu | PASS |
| **IT-07** | Checkout trong test transaction | Có dữ liệu trong transaction; mất sau `@Rollback` | PASS |
| **IT-08** | PNG `1x1` gửi FastAPI thật | Bị từ chối dưới `100x100`; Product không được lưu | PASS |

---

## 🗄️ 4. KIỂM TRA DATABASE VÀ TRANSACTION

- `JdbcTemplate` kiểm tra `Orders`, `Order_details`, `Products`, stock và sales.
- Stale stock không tạo dữ liệu bán hàng và vẫn giữ giỏ hàng.
- Lỗi constraint phải rollback toàn bộ thay đổi trong giao dịch.
- `@Transactional`, `@Rollback`, `@AfterTransaction` xác nhận cleanup, không tác động DB chính.

---

## 🤖 5. KIỂM TRA AI SERVICE

- Contract: `POST /api/v1/analyze`, `multipart/form-data`, field `file`.
- Kiểm tra ba nhánh: approve, reject và HTTP 500.
- Kiểm tra request; gửi PNG thật đến FastAPI và xác nhận ảnh reject không tạo Product.

---

## 🚀 6. ĐIỀU KIỆN VÀ CÁCH CHẠY

- Java, Maven và Docker Desktop phải hoạt động.

```powershell
mvn -Ptest24 test
```

---

## ✅ 7. KẾT QUẢ XÁC NHẬN

```text
ActualFastApiIntegrationTest:          1/1 PASS
AiServiceIntegrationTest:              3/3 PASS
OrderWorkflowIntegrationTest:          3/3 PASS
TransactionalIsolationIntegrationTest: 1/1 PASS

Tests run: 8, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Regression: controller `82/82` pass; full Maven `1.082` test, `0` failure, `0` error; Checkstyle `0` violation.

---

## 📋 8. THÀNH PHẦN TRIỂN KHAI

- Bốn test class kiểm tra order, AI, rollback và FastAPI.
- `MySqlIntegrationTestBase` cấp MySQL; hai service tạo ranh giới transaction và AI HTTP.

---

## 🎉 9. KẾT LUẬN

1. Kiểm tra đầy đủ luồng Controller → Service → DAO/Hibernate → MySQL.
2. Xác nhận đúng trạng thái Order, Detail, tồn kho và rollback.
3. Xác nhận Spring giao tiếp đúng contract với FastAPI thật.
4. Dữ liệu test được rollback hoặc cleanup, không thay đổi database chính.
5. Container MySQL chỉ là môi trường test tạm; tên ngẫu nhiên không liên quan nghiệp vụ.
