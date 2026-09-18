# Bảng Test Case: Chức năng 8 - Quản lý Trị sự (Admin Management)
**Người thực hiện:** Được 

## 1. Thông tin Kỹ thuật & Thực thi
- **Kỹ thuật Thiết kế (Test Design):** 
  - **Phân hoạch lớp tương đương (EP):** Quản lý Phân quyền (RBAC), Giới hạn Phạm vi Quản lý (Management Scope) và Quyền sở hữu chéo (Cross-Ownership) giữa các Admin.
  - **Phân tích giá trị biên (BVA) & Chuyển đổi trạng thái:** Giới hạn số lượng tài khoản quyền lực nhất (Super Admin).
- **Kỹ thuật Thực thi (Test Execution):** 
  - Kiểm thử Tích hợp & Unit Test bằng JUnit/Mockito (`UserControllerCoverageTest.java`, `ProductApiControllerTest.java`, `OrderApiControllerTest.java`).
  - Kiểm thử API E2E bằng Postman.

---

## 2. Phân tích Kỹ thuật Thiết kế (Test Design Analysis)

### 2.1 Bảng Chuyển đổi trạng thái (State Transition Table)
Hệ thống kiểm soát chặt chẽ máy trạng thái FSM đối với cả **Vòng đời Đơn hàng** và **Vòng đời Tài khoản Quản trị viên**, ngăn chặn mọi hành vi ép trạng thái sai quy trình:

| Đối tượng | Trạng thái hiện tại | Thao tác (Action) | Quyền thực thi / Điều kiện | Trạng thái kỳ vọng (Next State) | Tính hợp lệ | Test Case liên quan |
| :--- | :--- | :--- | :--- | :--- | :--- | :---: |
| **Đơn hàng** | `PENDING` | Duyệt giao hàng (Ship) | Admin có Scope quản lý | `SHIPPING` | Hợp lệ | TC_ADM_008 |
| **Đơn hàng** | `SHIPPING` | Hoàn tất giao hàng | Admin có Scope quản lý | `COMPLETED` | Hợp lệ | TC_ADM_008 |
| **Đơn hàng** | `CANCELLED` | Cố tình ép sang Giao hàng | Admin | *(Bị chặn, giữ nguyên `CANCELLED`)* | Báo lỗi 409 Conflict (Sai luồng FSM) | TC_ADM_010 |
| **Đơn hàng** | `RETURN_PENDING` | Phê duyệt trả hàng (Approve) | Admin có Scope quản lý | `RETURNED` | Hợp lệ (Cộng kho, trừ sales) | TC_CAN_008 |
| **Đơn hàng** | `RETURN_PENDING` | Từ chối trả hàng (Reject) | Admin có Scope quản lý | `COMPLETED` | Hợp lệ (Giữ nguyên kho) | TC_CAN_009 |
| **Admin Account** | `ROLE_ADMIN` (Active) | Hạ cấp xuống `ROLE_USER` | Số Admin Active = 1 | *(Bị chặn, giữ nguyên `ROLE_ADMIN`)* | Báo lỗi (Chặn mất Admin cuối cùng) | TC_ADM_001 |
| **Admin Account** | `ROLE_ADMIN` (Active) | Vô hiệu hóa / Khóa | Số Admin Active = 1 | *(Bị chặn, giữ nguyên Active)* | Báo lỗi (Chặn khóa Admin cuối cùng) | TC_ADM_002 |
| **Admin Account** | `ROLE_ADMIN` (Active) | Hạ cấp xuống `ROLE_USER` | Số Admin Active ≥ 2 | `ROLE_USER` (Active) | Hợp lệ | TC_ADM_003 |

### 2.2 Bảng Phân hoạch lớp tương đương (Equivalence Partitioning - EP)

| Biến đầu vào / Điều kiện | Lớp tương đương Hợp lệ | Tag | Lớp tương đương Không hợp lệ | Tag |
| :--- | :--- | :---: | :--- | :---: |
| **Số Admin hoạt động (`countAdmin`)**| Số lượng Admin Active ≥ 2 | **V1** | Số lượng Admin Active = 1 (Chặn mất quyền) | **X1** |
| **Quyền hạn tài khoản (`userRole`)** | Tài khoản mang quyền `ROLE_ADMIN` | **V2** | `ROLE_USER` thường<br>Khách vãng lai chưa xác thực | **X2**<br>**X3** |
| **Quyền sở hữu sản phẩm (`owner`)** | Admin thao tác trên SP do chính mình tạo | **V3** | Admin can thiệp sửa/xóa SP của Admin khác | **X4** |
| **Phạm vi quản lý đơn hàng (`scope`)**| Đơn hàng thuộc Scope quản lý phụ trách | **V4** | Đơn hàng nằm ngoài Scope phân quyền | **X5** |
| **Biểu mẫu sản phẩm (`productForm`)** | Mã $[1, 20]$ ký tự, Tên $[1, 255]$ ký tự | **V5** | Bỏ trống Mã (`code` null/rỗng)<br>Bỏ trống Tên (`name` null/rỗng) | **X6**<br>**X7** |
| **Chu trình FSM Đơn hàng (`state`)** | Chuyển trạng thái hợp lệ theo quy trình | **V6** | Ép trạng thái sai chu trình (VD: CANCELLED -> SHIPPING) | **X8** |

### 2.3 Bảng Phân tích giá trị biên (Standard Boundary Value Analysis - BVA)

| Biến đầu vào | Miền hợp lệ | min | min+ | nominal | max- | max | Tag biên |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :--- |
| **Số Admin Active (`countAdmin`)** | $[2, 10]$ tài khoản | 2 | 3 | 5 | 9 | 10 | **B1, B2, B3, B4, B5** |
| **Độ dài Mã sản phẩm (`code`)** | $[1, 20]$ ký tự | 1 | 2 | 6 | 19 | 20 | **B6, B7, B8, B9, B10** |
| **Độ dài Tên sản phẩm (`name`)** | $[1, 255]$ ký tự | 1 | 2 | 50 | 254 | 255 | **B11, B12, B13, B14, B15** |

*Ghi chú mở rộng (Robustness BVA):*
- Ngoài biên dưới của số Admin: $count = 1$ (Tag **B0** / min-1) -> Hệ thống chặn đứng hành vi hạ cấp hoặc khóa tài khoản, bảo toàn quản trị viên duy nhất.
- Ngoài biên dưới của mã/tên sản phẩm: $L = 0$ (Tag **B6-1**, **B11-1**) -> Báo lỗi 400 Bad Request.
- Ngoài biên trên của mã/tên sản phẩm: $L = 21$ (Tag **B10+1**), $L = 256$ (Tag **B15+1**) -> Báo lỗi quá độ dài.

---

## 3. Bảng Test Case Chi Tiết

### A. Quản lý Tài khoản (Account Management)
| Mã kiểm thử | Kỹ thuật áp dụng | Tiêu đề | Điều kiện tiên quyết | Các bước kiểm tra | Dữ liệu kiểm thử | Kết quả dự kiến | Tag được bao phủ | Kết quả thực tế | Trạng thái |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :---: | :--- | :---: |
| **TC_ADM_001** | BVA / EP | Chặn hạ cấp (Downgrade) quyền của Admin duy nhất còn hoạt động | Trong DB chỉ còn 1 tài khoản `ROLE_ADMIN` đang Active. | Vào trang Edit Admin đó, đổi Role thành `ROLE_USER` và Lưu. | Dữ liệu chuẩn TC_ADM_001 | Hệ thống chặn lại và báo lỗi. Tài khoản vẫn giữ quyền `ROLE_ADMIN`. (Khớp `userEditSave_blocksLastActiveAdminFromLosingAdminRole`) | **X1, B0** | Khớp với kết quả kiểm thử | Pass |
| **TC_ADM_002** | BVA / EP | Chặn Khóa/Vô hiệu hóa Admin duy nhất còn hoạt động | Trong DB chỉ còn 1 tài khoản `ROLE_ADMIN` đang Active. | Đổi checkbox Tình trạng hoạt động thành `false`. | Dữ liệu chuẩn TC_ADM_002 | Hệ thống chặn lại và báo lỗi. Trạng thái không bị thay đổi. | **X1, B0** | Khớp với kết quả kiểm thử | Pass |
| **TC_ADM_003** | EP | Cho phép hạ cấp Admin nếu vẫn còn Admin khác | Trong DB có >= 2 Admin đang Active. | Hạ cấp một Admin xuống `ROLE_USER`. | Dữ liệu chuẩn TC_ADM_003 | Thành công. | **V1, B1** | Khớp với kết quả kiểm thử | Pass |
| **TC_ADM_004** | Quyền | Chặn User thường cố tình vào xem Danh sách User của Admin | Khách hàng User đăng nhập. | Cố tình gõ URL `/admin/users`. | Dữ liệu chuẩn TC_ADM_004 | Bị đá văng sang trang 403 Forbidden. | **X2** | Khớp với kết quả kiểm thử | Pass |

### B. Quản lý Sản phẩm (Product Management)
| Mã kiểm thử | Kỹ thuật áp dụng | Tiêu đề | Điều kiện tiên quyết | Các bước kiểm tra | Dữ liệu kiểm thử | Kết quả dự kiến | Tag được bao phủ | Kết quả thực tế | Trạng thái |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :---: | :--- | :---: |
| **TC_ADM_005** | Ownership | Chặn Admin sửa sản phẩm của Admin khác | Admin A đăng nhập. Sản phẩm X thuộc sở hữu của Admin B. | Admin A gọi API Sửa sản phẩm X. | Dữ liệu chuẩn TC_ADM_005 | Báo lỗi 403 Forbidden. Cấm cập nhật Foreign Product. (Khớp `saveProduct_forbidsUpdatingForeignProduct`) | **X4** | Khớp với kết quả kiểm thử | Pass |
| **TC_ADM_006** | Ownership | Chặn Admin xóa (deactivate) sản phẩm của Admin khác | Admin A đăng nhập. Sản phẩm X của Admin B. | Admin A gọi API Xóa sản phẩm X. | Dữ liệu chuẩn TC_ADM_006 | Báo lỗi 403 Forbidden. (Khớp `deleteProduct_forbidsProductOwnedByAnotherPrincipal`) | **X4** | Khớp với kết quả kiểm thử | Pass |
| **TC_ADM_007** | Validation | Báo lỗi khi tạo sản phẩm thiếu Mã Code hoặc Tên | Admin thêm sản phẩm mới. | Điền Form nhưng bỏ trống Code hoặc Name (Dấu cách). | Dữ liệu chuẩn TC_ADM_007 | Báo lỗi Validation 400 Bad Request. | **X6, X7, B6-1, B11-1** | Khớp với kết quả kiểm thử | Pass |

### C. Quản lý Đơn hàng (Order Management)
| Mã kiểm thử | Kỹ thuật áp dụng | Tiêu đề | Điều kiện tiên quyết | Các bước kiểm tra | Dữ liệu kiểm thử | Kết quả dự kiến | Tag được bao phủ | Kết quả thực tế | Trạng thái |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :---: | :--- | :---: |
| **TC_ADM_008** | Scope EP | Chặn Admin thao tác đơn hàng nằm ngoài phạm vi quản lý | Admin đăng nhập. Đơn hàng Y không thuộc Scope của Admin này. | Admin cố tình gọi API Update Status Đơn hàng Y. | Dữ liệu chuẩn TC_ADM_008 | Báo lỗi 403 Forbidden. Chặn đổi trạng thái. (Khớp `updateStatus_rejectsPrincipalOutsideManagementScope`) | **X5** | Khớp với kết quả kiểm thử | Pass |
| **TC_ADM_009** | Algorithm | Tự động tính lại giá trị (Recalculate) khi xem đơn của Khách | Admin đăng nhập. | Admin xem chi tiết Đơn hàng của User. | Dữ liệu chuẩn TC_ADM_009 | Hệ thống chạy lệnh `Recalculate Amount` để ra giá thực tế thay vì lấy giá lưu cứng. (Khớp `getOrder_recalculatesAmountWhenAdminIsNotOrderCustomer`) | **V2, V4** | Khớp với kết quả kiểm thử | Pass |
| **TC_ADM_010** | State | Chặn Admin ép trạng thái đơn hàng sai luồng | Admin duyệt Đơn hàng Z. | Đơn đang bị Hủy (`CANCELLED`), cố tình bắn API ép thành Giao Hàng (`SHIPPING`). | Dữ liệu chuẩn TC_ADM_010 | Báo lỗi 409 Conflict. State FSM từ chối Invalid Transition. | **X8** | Khớp với kết quả kiểm thử | Pass |


---

## 4. Bảng Đối Chiếu & Ý Nghĩa Nhãn Tag (Tag Traceability Legend)

| Nhóm Tag | Mã Tag | Ý nghĩa nghiệp vụ | Trạng thái |
| :---: | :---: | :--- | :---: |
| **Valid EP** | **V1** | Hệ thống còn >= 2 Admin hoạt động, cho phép hạ cấp | Hợp lệ |
| | **V2** | Tài khoản mang quyền Quản trị viên (ROLE_ADMIN) | Hợp lệ |
| | **V3** | Admin thao tác trên sản phẩm do chính mình tạo ra | Hợp lệ |
| | **V4** | Admin thao tác trong phạm vi đơn hàng được phân quyền | Hợp lệ |
| | **V5** | Form sản phẩm hợp lệ đầy đủ mã và tên | Hợp lệ |
| | **V6** | Chuyển đổi trạng thái đơn hàng đúng quy trình máy trạng thái | Hợp lệ |
| **Invalid EP**| **X1** | Cố tình hạ cấp/khóa Admin duy nhất còn lại của hệ thống | Không hợp lệ |
| | **X2** | User thông thường cố tình truy cập khu vực Admin (Báo lỗi 403) | Không hợp lệ |
| | **X3** | Khách vãng lai cố tình truy cập trang Admin (Báo lỗi 401) | Không hợp lệ |
| | **X4** | Admin can thiệp sửa/xóa sản phẩm của Admin khác (Báo lỗi 403) | Không hợp lệ |
| | **X5** | Admin thao tác đơn hàng ngoài phạm vi quản lý (Báo lỗi 403) | Không hợp lệ |
| | **X6** | Mã sản phẩm bị bỏ trống hoặc null | Không hợp lệ |
| | **X7** | Tên sản phẩm bị bỏ trống hoặc null | Không hợp lệ |
| | **X8** | Ép trạng thái đơn hàng sai luồng FSM (Báo lỗi 409) | Không hợp lệ |
| **Boundary** | **B1 - B5**| Điểm biên số Admin Active: min (2), min+ (3), nom (5), max- (9), max (10) | Hợp lệ |
| | **B0** | Điểm ngoài biên dưới số Admin: count = 1 tài khoản | Không hợp lệ |
| | **B6 - B10**| Điểm biên độ dài Mã sản phẩm: min (1), min+ (2), nom (6), max- (19), max (20) | Hợp lệ |
| | **B11 - B15**| Điểm biên độ dài Tên sản phẩm: min (1), min+ (2), nom (50), max- (254), max (255) | Hợp lệ |
