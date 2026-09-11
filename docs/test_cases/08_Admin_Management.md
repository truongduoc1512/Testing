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
| :--- | :--- | :--- | :--- | :--- | :---: | :---: |
| **Đơn hàng** | `PENDING` | Duyệt giao hàng (Ship) | Admin có Scope quản lý | `SHIPPING` | Hợp lệ | TC_ADM_008 |
| **Đơn hàng** | `SHIPPING` | Hoàn tất giao hàng | Admin có Scope quản lý | `COMPLETED` | Hợp lệ | TC_ADM_008 |
| **Đơn hàng** | `CANCELLED` | Cố tình ép sang Giao hàng | Admin | *(Bị chặn, giữ nguyên `CANCELLED`)* | Báo lỗi 409 Conflict (Sai luồng FSM) | TC_ADM_010 |
| **Đơn hàng** | `RETURN_PENDING` | Phê duyệt trả hàng (Approve) | Admin có Scope quản lý | `RETURNED` | Hợp lệ (Cộng kho, trừ sales) | TC_CAN_008 |
| **Đơn hàng** | `RETURN_PENDING` | Từ chối trả hàng (Reject) | Admin có Scope quản lý | `COMPLETED` | Hợp lệ (Giữ nguyên kho) | TC_CAN_009 |
| **Admin Account** | `ROLE_ADMIN` (Active) | Hạ cấp xuống `ROLE_USER` | Số Admin Active = 1 | *(Bị chặn, giữ nguyên `ROLE_ADMIN`)* | Báo lỗi (Chặn mất Admin cuối cùng) | TC_ADM_001 |
| **Admin Account** | `ROLE_ADMIN` (Active) | Vô hiệu hóa / Khóa | Số Admin Active = 1 | *(Bị chặn, giữ nguyên Active)* | Báo lỗi (Chặn khóa Admin cuối cùng) | TC_ADM_002 |
| **Admin Account** | `ROLE_ADMIN` (Active) | Hạ cấp xuống `ROLE_USER` | Số Admin Active $\ge 2$ | `ROLE_USER` (Active) | Hợp lệ | TC_ADM_003 |

### 2.2 Bảng Phân hoạch lớp tương đương (Equivalence Partitioning - EP)

| STT | Điều kiện đầu vào (Input / Condition) | Lớp tương đương Hợp lệ (Valid EP) | Lớp tương đương Không hợp lệ (Invalid EP) | Test Case liên quan |
| :---: | :--- | :--- | :--- | :---: |
| 1 | **Phân quyền truy cập trang Quản trị (RBAC)** | Tài khoản mang quyền Quản trị viên (`ROLE_ADMIN`) | - Khách vãng lai chưa đăng nhập (Báo lỗi 401 Unauthorized)<br>- Khách hàng thông thường mang quyền `ROLE_USER` (Báo lỗi 403 Forbidden) | TC_ADM_004 |
| 2 | **Quyền sở hữu chéo Sản phẩm (Product Ownership Scope)** | Admin A chỉ thực hiện Sửa (Update) hoặc Xóa (Deactivate) trên Sản phẩm do chính Admin A tạo ra (`owner = adminA`) | Admin A cố tình can thiệp Sửa/Xóa Sản phẩm do Admin B tạo ra (`owner = adminB`) $\rightarrow$ Báo lỗi 403 Forbidden | TC_ADM_005<br>TC_ADM_006 |
| 3 | **Phạm vi Phân quyền Quản lý Đơn hàng (Order Scope)** | Admin thao tác duyệt / cập nhật đơn hàng nằm trong phạm vi được giao phụ trách (`canManageOrder = true`) | Admin thao tác trên đơn hàng nằm ngoài phạm vi phân quyền quản trị (`canManageOrder = false`) $\rightarrow$ Báo lỗi 403 Forbidden | TC_ADM_008 |
| 4 | **Thuật toán Tính lại giá trị Đơn hàng (Amount Recalculation)** | - Admin tự mua hàng đóng vai khách (`isOrderCustomer = true`): Bảo lưu giá trị đơn hàng gốc.<br>- Admin xem đơn của khách hàng (`isOrderCustomer = false`): Tự động kích hoạt thuật toán tính lại giá thực tế từ chi tiết dòng hàng. | N/A (Thuật toán phân định tự động theo chủ sở hữu đơn hàng) | TC_ADM_009 |
| 5 | **Tính toàn vẹn biểu mẫu Sản phẩm (Product Form Validation)** | Form nhập đầy đủ Mã sản phẩm và Tên sản phẩm hợp lệ, Giá bán $> 0$, Tồn kho $\ge 0$ | Bỏ trống Mã sản phẩm (`code` rỗng/null) hoặc Tên sản phẩm (`name` rỗng/null/khoảng trắng) $\rightarrow$ Báo lỗi 400 Bad Request | TC_ADM_007 |

### 2.3 Bảng Phân tích giá trị biên (Boundary Value Analysis - BVA)

| STT | Biến kiểm thử / Ràng buộc logic | Điểm biên BVA | Giá trị kiểm thử | Phân loại BVA | Kết quả dự kiến (Expected Output) | Test Case |
| :---: | :--- | :---: | :---: | :---: | :--- | :---: |
| 1 | **Số lượng Admin đang hoạt động (`countActiveAdmins`)**<br>*Ràng buộc: $countActiveAdmins > 1$ mới được phép hạ cấp hoặc khóa tài khoản* | min | 1 tài khoản | Invalid Boundary | Hệ thống chặn đứng hành động hạ cấp / khóa, bảo lưu tài khoản Admin duy nhất | TC_ADM_001<br>TC_ADM_002 |
| 2 | **Số lượng Admin đang hoạt động (`countActiveAdmins`)** | min + 1 | 2 tài khoản | Valid (Biên hợp lệ) | Cho phép hạ cấp hoặc khóa Admin vì vẫn còn ít nhất 1 Admin khác hoạt động | TC_ADM_003 |
| 3 | **Số lượng Admin đang hoạt động (`countActiveAdmins`)** | nom | 3, 5 tài khoản | Valid | Thao tác hạ cấp / khóa diễn ra bình thường | TC_ADM_003 |
| 4 | **Độ dài Mã sản phẩm (`code`)**<br>*Ràng buộc: $1 \le \text{length}(code) \le 20$ ký tự* | min - 1 | 0 ký tự (`""` rỗng) | Invalid (Dưới biên) | Báo lỗi 400 Bad Request (Thiếu mã sản phẩm) | TC_ADM_007 |
| 5 | **Độ dài Mã sản phẩm (`code`)** | min | 1 ký tự | Valid (Biên dưới) | Hợp lệ, tạo sản phẩm thành công | TC_ADM_007 |
| 6 | **Độ dài Mã sản phẩm (`code`)** | max | 20 ký tự | Valid (Biên trên) | Hợp lệ, tạo sản phẩm thành công | TC_ADM_007 |
| 7 | **Độ dài Mã sản phẩm (`code`)** | max + 1 | 21 ký tự | Invalid (Vượt biên) | Báo lỗi 400 Bad Request (Mã sản phẩm quá dài) | TC_ADM_007 |
| 8 | **Độ dài Tên sản phẩm (`name`)**<br>*Ràng buộc: $1 \le \text{length}(name) \le 255$ ký tự* | min - 1 | 0 ký tự (`""` rỗng) | Invalid (Dưới biên) | Báo lỗi 400 Bad Request (Thiếu tên sản phẩm) | TC_ADM_007 |
| 9 | **Độ dài Tên sản phẩm (`name`)** | min | 1 ký tự | Valid (Biên dưới) | Hợp lệ, tạo sản phẩm thành công | TC_ADM_007 |
| 10 | **Độ dài Tên sản phẩm (`name`)** | max | 255 ký tự | Valid (Biên trên) | Hợp lệ, tạo sản phẩm thành công | TC_ADM_007 |
| 11 | **Độ dài Tên sản phẩm (`name`)** | max + 1 | 256 ký tự | Invalid (Vượt biên) | Báo lỗi 400 Bad Request (Tên sản phẩm quá dài) | TC_ADM_007 |

---

## 3. Bảng Test Case Chi Tiết

### A. Quản lý Tài khoản (Account Management)
| Mã kiểm thử | Kỹ thuật | Tiêu đề | Điều kiện tiên quyết | Các bước kiểm tra | Kết quả dự kiến | Trạng thái |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **TC_ADM_001** | BVA / EP | Chặn hạ cấp (Downgrade) quyền của Admin duy nhất còn hoạt động | Trong DB chỉ còn 1 tài khoản `ROLE_ADMIN` đang Active. | Vào trang Edit Admin đó, đổi Role thành `ROLE_USER` và Lưu. | Hệ thống chặn lại và báo lỗi. Tài khoản vẫn giữ quyền `ROLE_ADMIN`. (Khớp `userEditSave_blocksLastActiveAdminFromLosingAdminRole`) | Pass |
| **TC_ADM_002** | BVA / EP | Chặn Khóa/Vô hiệu hóa Admin duy nhất còn hoạt động | Trong DB chỉ còn 1 tài khoản `ROLE_ADMIN` đang Active. | Đổi checkbox Tình trạng hoạt động thành `false`. | Hệ thống chặn lại và báo lỗi. Trạng thái không bị thay đổi. | Pass |
| **TC_ADM_003** | EP | Cho phép hạ cấp Admin nếu vẫn còn Admin khác | Trong DB có >= 2 Admin đang Active. | Hạ cấp một Admin xuống `ROLE_USER`. | Thành công. | Pass |
| **TC_ADM_004** | Quyền | Chặn User thường cố tình vào xem Danh sách User của Admin | Khách hàng User đăng nhập. | Cố tình gõ URL `/admin/users`. | Bị đá văng sang trang 403 Forbidden. | Pass |

### B. Quản lý Sản phẩm (Product Management)
| Mã kiểm thử | Kỹ thuật | Tiêu đề | Điều kiện tiên quyết | Các bước kiểm tra | Kết quả dự kiến | Trạng thái |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **TC_ADM_005** | Ownership | Chặn Admin sửa sản phẩm của Admin khác | Admin A đăng nhập. Sản phẩm X thuộc sở hữu của Admin B. | Admin A gọi API Sửa sản phẩm X. | Báo lỗi 403 Forbidden. Cấm cập nhật Foreign Product. (Khớp `saveProduct_forbidsUpdatingForeignProduct`) | Pass |
| **TC_ADM_006** | Ownership | Chặn Admin xóa (deactivate) sản phẩm của Admin khác | Admin A đăng nhập. Sản phẩm X của Admin B. | Admin A gọi API Xóa sản phẩm X. | Báo lỗi 403 Forbidden. (Khớp `deleteProduct_forbidsProductOwnedByAnotherPrincipal`) | Pass |
| **TC_ADM_007** | Validation | Báo lỗi khi tạo sản phẩm thiếu Mã Code hoặc Tên | Admin thêm sản phẩm mới. | Điền Form nhưng bỏ trống Code hoặc Name (Dấu cách). | Báo lỗi Validation 400 Bad Request. | Pass |

### C. Quản lý Đơn hàng (Order Management)
| Mã kiểm thử | Kỹ thuật | Tiêu đề | Điều kiện tiên quyết | Các bước kiểm tra | Kết quả dự kiến | Trạng thái |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **TC_ADM_008** | Scope EP | Chặn Admin thao tác đơn hàng nằm ngoài phạm vi quản lý | Admin đăng nhập. Đơn hàng Y không thuộc Scope của Admin này. | Admin cố tình gọi API Update Status Đơn hàng Y. | Báo lỗi 403 Forbidden. Chặn đổi trạng thái. (Khớp `updateStatus_rejectsPrincipalOutsideManagementScope`) | Pass |
| **TC_ADM_009** | Algorithm | Tự động tính lại giá trị (Recalculate) khi xem đơn của Khách | Admin đăng nhập. | Admin xem chi tiết Đơn hàng của User. | Hệ thống chạy lệnh `Recalculate Amount` để ra giá thực tế thay vì lấy giá lưu cứng. (Khớp `getOrder_recalculatesAmountWhenAdminIsNotOrderCustomer`) | Pass |
| **TC_ADM_010** | State | Chặn Admin ép trạng thái đơn hàng sai luồng | Admin duyệt Đơn hàng Z. | Đơn đang bị Hủy (`CANCELLED`), cố tình bắn API ép thành Giao Hàng (`SHIPPING`). | Báo lỗi 409 Conflict. State FSM từ chối Invalid Transition. | Pass |
