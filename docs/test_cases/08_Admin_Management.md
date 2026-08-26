# Bảng Test Case: Chức năng 8 - Quản lý Đơn hàng & Sản phẩm (Admin Management)
**Người thực hiện:** Được

### 8.1 Xác định các Biến điều kiện (Conditions - C)

| Ký hiệu | Tên biến điều kiện (Condition) | Giải thích logic nghiệp vụ |
| :--- | :--- | :--- |
| C1 | Quyền Admin (Role Auth) | Người gửi Request có mang Token hợp lệ của quyền quản trị viên hay không (Tách lớp dữ liệu Admin và User). |
| C2 | Đối tượng tồn tại (ID Exists) | Mã ID của Đơn hàng hoặc ID của Sản phẩm cần thao tác có thực sự tồn tại ở trong Database hay không. |
| C3 | Dữ liệu và Trạng thái hợp lệ (Valid Payload) | Nếu là tạo Sản phẩm thì Giá tiền phải lớn hơn 0. Nếu là duyệt Đơn hàng thì Đơn hàng phải đang ở trạng thái Chờ duyệt. |

### 8.2 Bảng Rút Gọn Quy Tắc (Quyền truy cập & Nghiệp vụ)

| Quy tắc (Rule) | C1: Quyền Admin? | C2: Đối tượng tồn tại? | C3: Dữ liệu/Status hợp lệ? | Kết quả / Hành động hệ thống | Số kịch bản lý thuyết đã gộp | Test Case tương ứng |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| Rule 1 (Lỗ hổng bảo mật) | No | - | - | Chặn thao tác và báo lỗi 401 hoặc 403 Forbidden. | Gộp 4 kịch bản | TC_ADM_004 |
| Rule 2 (Thao tác mã ảo) | Yes | No | - | Chặn thao tác và báo lỗi 404 Not Found. | Gộp 2 kịch bản | TC_ADM_005 |
| Rule 3 (Dữ liệu rác/Sai luồng) | Yes | Yes | No | Chặn thao tác và báo lỗi 400 Bad Request. | Chiếm 1 kịch bản | TC_ADM_003 |
| Rule 4 (Thực thi hoàn hảo) | Yes | Yes | Yes | Xử lý thành công (Mã 200 OK / 201 Created). | Chiếm 1 kịch bản | TC_ADM_001, 002 |

### 8.3 Bảng Test Case Chi Tiết

| Mã kiểm thử | Kỹ thuật áp dụng | Tiêu đề | Điều kiện tiên quyết | Các bước kiểm tra | Dữ liệu kiểm thử | Kết quả dự kiến | Kết quả thực tế | Trạng thái |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| TC_ADM_001 | Chuyển đổi trạng thái (Luật 4) | Kiểm tra Admin phê duyệt đơn hàng thành công (Giao hàng) | Admin đã đăng nhập và Đơn hàng số 200 đang ở trạng thái PENDING. | Gọi API PUT đổi trạng thái đơn hàng sau đó truyền vào trạng thái mới. | Trạng thái mới là SHIPPING | Server chấp nhận và trạng thái đơn đổi thành SHIPPING đồng thời gửi email báo cho khách. | Trả về 200 OK và Đơn hàng chuyển sang Đang giao. | Pass |
| TC_ADM_002 | Phân tích giá trị biên (Luật 4) | Kiểm tra Admin tạo sản phẩm mới thành công | Admin đã đăng nhập vào hệ thống quản trị. | Gọi API POST để tạo sản phẩm rồi truyền các thông tin cần thiết của đôi giày mới vào. | Giá tiền bằng 1 (Biên dưới Min) và Tên sản phẩm hợp lệ. | Server khởi tạo sản phẩm thành công và lưu vào Database. | Trả về 201 Created và hiển thị lên trang Catalog. | Pass |
| TC_ADM_003 | Chuyển đổi trạng thái (Luật 3 Sai luồng) | Kiểm tra hệ thống chặn Admin duyệt sai luồng một đơn đã bị hủy | Admin đã đăng nhập và Đơn hàng số 201 đang bị CANCELLED (Đã hủy). | Gọi API PUT để đổi trạng thái đơn hàng nhưng cố tình ép đơn hàng đó sang SHIPPING. | Đơn hàng ID 201 và Trạng thái ép buộc là SHIPPING. | Hệ thống từ chối và báo lỗi Không thể giao một đơn hàng đã bị hủy. | Báo lỗi 400 Bad Request và bảo vệ nguyên vẹn State. | Pass |
| TC_ADM_004 | Phân hoạch lớp EP (Luật 1 Sai quyền) | Kiểm tra chặn User bình thường cố tình dùng API xóa Sản phẩm của Admin | Dùng Token đăng nhập của tài khoản Khách hàng thay vì Admin. | Bắt gói tin gọi API DELETE xóa sản phẩm giày rồi truyền ID giày hợp lệ vào để gửi. | ID sản phẩm là 1 và Token JWT của User thường. | Hệ thống phân quyền Spring Security bắt được lỗi sai Role và đá văng lập tức. | Trả về lỗi 403 Forbidden và Giày không bị xóa. | Pass |
| TC_ADM_005 | Đoán lỗi (Luật 2 Đối tượng ảo) | Kiểm tra Admin duyệt trả hàng cho một đơn hàng không tồn tại | Admin đã đăng nhập hệ thống. | Gọi API PUT phê duyệt trả hàng nhưng truyền vào một ID đơn hàng bậy bạ. | ID đơn hàng bằng 99999999 (Không tồn tại trong DB). | Database quét không ra và báo lỗi Không tìm thấy đơn hàng cần thao tác. | Trả về lỗi 404 Not Found và Server không bị Crash. | Pass |
