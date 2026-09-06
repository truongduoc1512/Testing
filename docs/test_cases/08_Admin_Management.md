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

### 2.1 Bảng Phân hoạch lớp tương đương (EP) - Luật "Vị Vua Cuối Cùng" (Last Active Admin Rule)
Một trong những lỗi chí mạng của các hệ thống quản trị là "Vô tình khóa mất tài khoản Admin duy nhất". Lập trình viên đã thiết lập một chốt chặn rất hay để giải quyết vấn đề này. 
Hệ thống sẽ đếm tổng số Admin đang hoạt động (`countActiveAdmins`). Nếu con số này là **1**, hệ thống cấm tuyệt đối các thao tác sau lên tài khoản Admin đó:
- Đổi Role từ `ROLE_ADMIN` xuống `ROLE_USER` (Hạ cấp).
- Đổi trạng thái `isActive = false` (Vô hiệu hóa).
- Đổi trạng thái `isAccountNonLocked = false` (Khóa tài khoản).

### 2.2 Phân quyền Sở hữu (Ownership EP) - Tính độc lập của Admin
Không phải cứ có quyền Admin là có quyền sinh sát tất cả mọi thứ. Hệ thống giới hạn "Phạm vi quản lý" (Scope) cực kỳ chặt chẽ:
- **Quản lý Sản phẩm (Product Scope):** Admin A **không được phép** Sửa (Update) hoặc Xóa (Delete/Deactivate) Sản phẩm do Admin B tạo ra. API sẽ bắn lỗi 403 Forbidden.
- **Quản lý Đơn hàng (Order Scope):** Admin chỉ được phép Cập nhật trạng thái những Đơn hàng nằm trong vùng quản lý của mình (`canManageOrder`). Xóa rào vùng này sẽ bị đá văng bằng 403.

### 2.3 Thuật toán Ảo - Tính lại giá Đơn hàng
Lập trình viên viết một logic ẩn rất thú vị khi Admin truy vấn xem Chi tiết một đơn hàng:
- Nếu Admin đang xem Đơn hàng do chính Admin đó tự mua đóng vai khách (`isOrderCustomer = true`): Giá trị đơn hàng (Amount) được bảo lưu nguyên gốc.
- Nếu Admin xem Đơn hàng của Khách hàng khác mua: Hệ thống sẽ kích hoạt hàm **Tính toán lại giá trị** (Recalculate Amount) dựa trên các Detail chứ không lấy giá lưu cứng.

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
