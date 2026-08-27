# Bảng Test Case: Chức năng 8 - Quản lý Đơn hàng & Sản phẩm (Admin Management)
**Người thực hiện:** Được 

## 1. Thông tin Kỹ thuật & Thực thi
- **Kỹ thuật Thiết kế (Test Design):** 
  - **Phân hoạch lớp tương đương (EP):** Đây là kỹ thuật sống còn để test Bảo mật Phân quyền (RBAC). Hệ thống chia ra các lớp: Quản trị viên (Admin) và Người dùng thường (Customer).
  - **Bảng quyết định (Decision Table):** Sử dụng để gộp chung logic Phân quyền + Xác thực thực thể (ID Exists) + Trạng thái/Dữ liệu (Valid Payload) thành một ma trận chặn lỗi thống nhất.
- **Kỹ thuật Thực thi (Test Execution):** Kiểm thử API End-to-End (Black-box E2E API Testing).
- **File Code Thực thi (Automation Script):** Toàn bộ kịch bản được tự động hóa bằng phần mềm Postman/Newman và lưu tại file: `docs/Shoeshop_API_Collection.json`.

---

## 2. Phân tích Kỹ thuật Thiết kế (Test Design Analysis)

### 2.1 Bảng Phân hoạch lớp tương đương (EP - Phân quyền RBAC)
Bài toán quản lý luôn đối mặt với rủi ro leo thang đặc quyền (Privilege Escalation). EP được áp dụng để xác định rõ biên giới quyền hạn:

| STT | Trường hợp của điều kiện đầu vào | Lớp tương đương Hợp lệ | Lớp tương đương Không hợp lệ |
| :---: | :--- | :--- | :--- |
| 1 | JWT Token Role (Phân quyền) | ROLE_ADMIN | ROLE_CUSTOMER hoặc Guest (Không có Token) |
| 2 | ID Đối tượng (Đơn hàng/Sản phẩm)| ID đang tồn tại trong Database | ID ảo, ID đã bị xóa cứng |
| 3 | Payload Dữ liệu thao tác | Giá $> 0$ / Chuyển State đúng luồng | Giá âm / Chuyển State sai luồng |

### 2.2 Ma trận Quyết định tổng hợp (Collapsed Decision Table)
Gộp các vùng dữ liệu Hợp lệ và Không hợp lệ từ Bảng EP bên trên vào một Ma trận Quyết định để che chắn toàn bộ API của Admin:

| Condition/Action | R1 | R2 | R3 | R4 |
| :--- | :---: | :---: | :---: | :---: |
| **C1: Token có quyền Admin?** | N | Y | Y | Y |
| **C2: ID Đối tượng tồn tại?** | - | N | Y | Y |
| **C3: Payload / Status hợp lệ?** | - | - | N | Y |
| **A1: Báo lỗi Bảo mật 401/403** | X | - | - | - |
| **A2: Báo lỗi 404 Not Found** | - | X | - | - |
| **A3: Báo lỗi 400 Bad Request** | - | - | X | - |
| **A4: Thành công (200 OK / 201)** | - | - | - | X |
| **Test Case Tương ứng** | TC_ADM_004 | TC_ADM_005 | TC_ADM_003 | TC_ADM_001, 002 |

---

## 3. Bảng Test Case Chi Tiết

| Mã kiểm thử | Kỹ thuật áp dụng | Tiêu đề | Điều kiện tiên quyết | Các bước kiểm tra | Dữ liệu kiểm thử | Kết quả dự kiến | Kết quả thực tế | Trạng thái |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| TC_ADM_001 | Decision (R4) & State | Kiểm tra Admin phê duyệt đơn hàng thành công (Giao hàng) | Admin đã đăng nhập và Đơn hàng số 200 đang ở trạng thái PENDING. | Gọi API PUT đổi trạng thái đơn hàng sau đó truyền vào trạng thái mới. | Trạng thái mới là SHIPPING | Server chấp nhận và trạng thái đơn đổi thành SHIPPING đồng thời gửi email báo cho khách. | Trả về 200 OK và Đơn hàng chuyển sang Đang giao. | Pass |
| TC_ADM_002 | Decision (R4) & BVA | Kiểm tra Admin tạo sản phẩm mới thành công | Admin đã đăng nhập vào hệ thống quản trị. | Gọi API POST để tạo sản phẩm rồi truyền các thông tin cần thiết của đôi giày mới vào. | Giá tiền bằng 1 (Biên dưới Min) và Tên sản phẩm hợp lệ. | Server khởi tạo sản phẩm thành công và lưu vào Database. | Trả về 201 Created và hiển thị lên trang Catalog. | Pass |
| TC_ADM_003 | Decision (R3) & State | Kiểm tra hệ thống chặn Admin duyệt sai luồng một đơn đã bị hủy | Admin đã đăng nhập và Đơn hàng số 201 đang bị CANCELLED (Đã hủy). | Gọi API PUT để đổi trạng thái đơn hàng nhưng cố tình ép đơn hàng đó sang SHIPPING. | Đơn hàng ID 201 và Trạng thái ép buộc là SHIPPING. | Hệ thống từ chối và báo lỗi Không thể giao một đơn hàng đã bị hủy. | Báo lỗi 400 Bad Request và bảo vệ nguyên vẹn State. | Pass |
| TC_ADM_004 | Decision (R1) & EP | Kiểm tra chặn User bình thường cố tình dùng API xóa Sản phẩm của Admin | Dùng Token đăng nhập của tài khoản Khách hàng thay vì Admin. | Bắt gói tin gọi API DELETE xóa sản phẩm giày rồi truyền ID giày hợp lệ vào để gửi. | ID sản phẩm là 1 và Token JWT của User thường. | Hệ thống phân quyền Spring Security bắt được lỗi sai Role và đá văng lập tức. | Trả về lỗi 403 Forbidden và Giày không bị xóa. | Pass |
| TC_ADM_005 | Decision (R2) & Guess | Kiểm tra Admin duyệt trả hàng cho một đơn hàng không tồn tại | Admin đã đăng nhập hệ thống. | Gọi API PUT phê duyệt trả hàng nhưng truyền vào một ID đơn hàng bậy bạ. | ID đơn hàng bằng 99999999 (Không tồn tại trong DB). | Database quét không ra và báo lỗi Không tìm thấy đơn hàng cần thao tác. | Trả về lỗi 404 Not Found và Server không bị Crash. | Pass |
