# Production changes in `src/main`

## Mục đích

Tài liệu này mô tả riêng phần code chạy thật đã thay đổi khi review Product, Order, Cart, User và Review controllers. Nội dung chỉ gồm dữ kiện đầu vào, xử lý, đầu ra, hiệu quả và giới hạn hiện tại.

## Phạm vi thay đổi

- MVC: `ProductController`, `OrderController`, `CartController`, `UserController`, `ReviewController`, `HomeController`.
- REST: Product, Order, Order Cancel/Return, Cart, User, User Address, Review và Voucher API controllers.
- Phần liên quan: security config, DAO, model/entity, service, validator, Thymeleaf template và Flyway migration.
- File mới: 6 Java class và 2 migration.
- Tracked diff trước khi thêm tài liệu: 48 file, 1.739 dòng thêm và 581 dòng xóa.

## Thành phần mới

| Thành phần | Đầu vào | Đầu ra/tác động | Hiệu quả |
|---|---|---|---|
| `CsrfTokenControllerAdvice` | `HttpServletRequest` | Materialize CSRF token trước khi render MVC | Tránh Thymeleaf commit response trước khi token được tạo. |
| `ApiResponse` | Chuỗi message | Map gồm `success`, `message` | Giữ response Cart thống nhất, giảm code lặp. |
| `OrderStatus` | Status hiện tại và status mới | Status chuẩn hóa hoặc kết quả transition | Chặn status lạ/transition ngược; hỗ trợ alias legacy. |
| `AccountProfileService` | Account hiện tại, `UserProfileForm` | Lỗi validation hoặc Account đã cập nhật | Dùng chung validation MVC/REST, không mutate khi input sai. |
| `AuthenticatedAccountService` | Spring `Authentication` | Account tương ứng hoặc `null` | Account lấy từ principal, chặn IDOR qua username client gửi. |
| `PageNumberParser` | Chuỗi page | Số nguyên dương, mặc định `1` | Không phát sinh lỗi parse/page âm hoặc 0. |
| Migration V14 | Bảng `Accounts` | Thêm `RESET_TOKEN_EXPIRES_AT` | Reset token có hạn dùng. |
| Migration V15 | Reset token cũ | Xóa token và expiry cũ | Token plaintext trước migration không còn dùng được. |

Migration chỉ cập nhật database hiện hữu, không tạo database, image hoặc Docker volume mới.

## Quy ước request chung

- Authentication dùng form login/OAuth2 và HTTP session.
- Form/AJAX mutation phải gửi CSRF token; REST registration là ngoại lệ public.
- Product/Review read là public. Cart, Order, profile, Address và Review mutation yêu cầu đăng nhập. Chức năng quản trị yêu cầu `ROLE_ADMIN` theo security config.
- Login thành công trả về `/`; request Wishlist anonymous không được tạo saved request dẫn người dùng sang API.
- Lỗi JSON ở các nhánh đã chuẩn hóa có `success=false` và `message` an toàn, không chứa stack trace.

## Product

| Chức năng | Dữ kiện đầu vào | Kết quả đầu ra |
|---|---|---|
| Danh sách | `name`, `page`, `sort`, `minPrice`, `maxPrice`, `location`, `brand`, `isMall`, `isFavored`, `rating`, `category` | MVC trả `productList`; REST trả `PaginationResult<ProductInfo>`. Page tối thiểu 1, 12 item/trang. |
| Chi tiết | Product `code` | Có Product: `productInfo`, reviews và review form; không có: redirect danh sách. |
| Lưu | `ProductForm`: code, name, price, image, discount, stock | Validation/ownership pass thì save; tạo mới REST trả 201; input sai 400; sai quyền 403. |
| Xóa | Product `code` và principal | Soft-delete Product thuộc owner; giữ Order history. |

Đã sửa filter category/rating không được truyền hoặc sai kiểu, quyền update/delete, Product inactive lọt vào public/Cart, upload lỗi bị bỏ qua và hard delete làm mất liên kết lịch sử.

## Cart và checkout

| Chức năng | Dữ kiện đầu vào | Kết quả đầu ra |
|---|---|---|
| Add/update item | Product code, quantity nguyên dương, session Cart | Cart đã cập nhật; Product lạ trả 404; quantity sai trả 400; vượt kho được cap theo stock. |
| Remove item | Product code, session Cart | Cart sau khi xóa; Product không tồn tại trả 404. |
| Customer | name, address, email, phone | Cart có `CustomerInfo` valid; thiếu/sai format/quá giới hạn trả 400. |
| Checkout | Cart không rỗng, customer hợp lệ, principal/session | Thành công trả 201 với `orderedCart`, xóa Cart hiện tại và lưu `lastOrderedCart`; thất bại trả 400 và giữ Cart. |

Checkout reload giá, tồn kho và voucher phía server; transaction và pessimistic lock bảo vệ stock/order number. Trang finalize hiển thị order number, quantity, total và responsive layout.

## Order và Return

| Chức năng | Dữ kiện đầu vào | Kết quả đầu ra |
|---|---|---|
| List/detail | page, orderId, principal/role | Chỉ trả Order trong customer/seller/admin scope; ngoài quyền 403, không tồn tại 404. |
| Update status | orderId, status | 200 với Order mới; status sai 400; sai quyền 403; không có Order 404; transition không hợp lệ 409. |
| Cancel | orderId và principal | Hủy hợp lệ và hoàn kho đúng một lần; state sai 400; không có Order 404. |
| Create Return | reason tối đa 2.000 ký tự, imageUrls tối đa 500 | 201 kèm `OrderReturn`; input/state sai 400; không có Order 404. |
| Process Return | `APPROVE` hoặc `REJECT`, adminNote tối đa 255 | 200 với Return mới; xử lý lặp trả 409. |

Status hợp lệ: `PENDING`, `APPROVED`, `SHIPPING`, `COMPLETED`, `CANCELLED`. Alias `SHIPPED`, `DELIVERED` và `1` được chuẩn hóa. DAO lock Order/Return/Product để tránh hoàn kho hoặc giảm sold count nhiều lần.

## User và Address

| Chức năng | Dữ kiện đầu vào | Kết quả đầu ra |
|---|---|---|
| Register | userName, email, password, confirmPassword | 201 Account; thiếu/sai/trùng trả 400; password được BCrypt encode. |
| Get/update profile | Principal; fullName, email, phoneNumber, avatarUrl | 200 Account; anonymous 401; không tìm thấy 404; validation sai 400. |
| Reset password | Token, password mới | Token được đối chiếu bằng SHA-256, hết hạn sau 15 phút và chỉ consume một lần. |
| Address | receiver, phone, location fields, street, note, default flag | CRUD theo owner; chỉ duy trì một default address. |

Profile luôn chọn Account từ principal, không dùng username trong request. Admin edit có guard ngăn disable/lock active admin cuối cùng.

## Review

| Chức năng | Dữ kiện đầu vào | Kết quả đầu ra |
|---|---|---|
| List | productCode | 200 với danh sách; danh sách rỗng là hợp lệ. |
| Create | productCode tối đa 20, rating nguyên 1–5, comment 1–2.000 | 201 kèm Review; anonymous 401; admin 403; input sai 400. |
| Update | reviewId, rating 1–5, comment, principal | 200 với Review mới; sai owner/quá 5 phút/input sai trả 400. |
| Delete | reviewId và principal | 200 khi owner xóa; không tồn tại/sai owner trả 400. |

Username được lấy từ principal; Product được lock khi cập nhật rating cache.

## Cách chạy và đầu ra kiểm tra

Điều kiện đầu vào: Java/Maven hoạt động và datasource/AI URL được cấu hình cho môi trường chạy. Khi chạy Maven trực tiếp trên host với Docker hiện tại, datasource phải dùng host port `3307` và database `shoe_shopdb`; cấu hình fallback trong `application.properties` dùng `localhost:3306/shoeshop_db` nên không khớp Docker Compose.

```powershell
mvn spring-boot:run
mvn checkstyle:check
mvn compile spotbugs:check
```

Đầu ra đã xác nhận:

- Product UI: `http://localhost/productList`.
- Springdoc UI qua backend/nginx: `http://localhost/swagger-ui/index.html`.
- Swagger UI container riêng: `http://localhost:8082`.
- Checkstyle: 0 violation.
- SpotBugs: 0 bug, 0 error, 0 warning.

## Hiệu quả và giới hạn

Hiệu quả:

- Đã bảo vệ authentication, role, ownership và CSRF ở các controller trong phạm vi.
- Đã chặn quantity/status/payload không hợp lệ và giảm response/validation trùng lặp.
- Checkout, Return và rating cache có transaction/lock ở đường ghi quan trọng.
- Login, Product filter, finalize UI và error response đã khớp lại với luồng người dùng.

Giới hạn còn lại:

- `ProductController.productSave` còn cognitive complexity 19; `/productList` chưa giới hạn GET-only.
- Reset password chưa có email delivery và anti-enumeration hoàn chỉnh.
- AI quality gate chưa có timeout/failure policy riêng.
- Sonar Quality Gate còn `ERROR`; static checks pass không đồng nghĩa toàn bộ project không còn issue.
