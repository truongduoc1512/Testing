# Bảng Test Case: Chức năng 6 - Đánh giá sản phẩm (Review & Rating)
**Người thực hiện:** Được

### 6.1 Bảng Rút Gọn Điều Kiện (Decision Table & BVA)

| Quy tắc (Rule) | C1: Đã Đăng nhập? | C2: Số sao >= 1? (Biên dưới) | C3: Số sao <= 5? (Biên trên) | C4: Nội dung hợp lệ? (Không rỗng) | Kết quả / Hành động hệ thống | Số kịch bản lý thuyết đã gộp | Test Case tương ứng |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| Rule 1 (Khách vãng lai) | No | - | - | - | Báo lỗi 401 Unauthorized | Gộp 8 kịch bản | TC_REV_006 |
| Rule 2 (Hack 0 sao) | Yes | No | - | - | Báo lỗi 400 (Số sao phải lớn hơn hoặc bằng 1) | Gộp 4 kịch bản | TC_REV_004 |
| Rule 3 (Hack 6 sao) | Yes | Yes | No | - | Báo lỗi 400 (Số sao phải nhỏ hơn hoặc bằng 5) | Gộp 2 kịch bản | TC_REV_005 |
| Rule 4 (Bỏ trống nội dung) | Yes | Yes | Yes | No | Báo lỗi 400 (Nội dung không được để trống) | Chiếm 1 kịch bản | TC_REV_003 |
| Rule 5a (Hợp lệ - Min Sao) | Yes | Yes | Yes | Yes | Lưu đánh giá 1 sao thành công | Tách từ kịch bản hợp lệ cuối | TC_REV_002 |
| Rule 5b (Hợp lệ - Max Sao) | Yes | Yes | Yes | Yes | Lưu đánh giá 5 sao thành công | Tách từ kịch bản hợp lệ cuối | TC_REV_001 |

### 6.2 Bảng Test Case Chi Tiết

| Mã kiểm thử | Kỹ thuật áp dụng | Tiêu đề | Điều kiện tiên quyết | Các bước kiểm tra | Dữ liệu kiểm thử | Kết quả dự kiến | Kết quả thực tế | Trạng thái |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| TC_REV_001 | BVA (Biên trên Max) | Kiểm tra đánh giá hợp lệ với số sao cao nhất | Khách hàng đã mua hàng và đang đăng nhập. | Chọn số sao sau đó nhập bình luận và bấm Gửi. | Số sao bằng 5 và Nội dung là Giày đi rất êm và bền. | Hệ thống lưu đánh giá thành công và hiển thị 5 sao màu vàng lên giao diện. | Lưu vào Database thành công. | Pass |
| TC_REV_002 | BVA (Biên dưới Min) | Kiểm tra đánh giá hợp lệ với số sao thấp nhất | Khách hàng đã mua hàng và đang đăng nhập. | Chọn số sao sau đó nhập bình luận và bấm Gửi. | Số sao bằng 1 và Nội dung là Giao hàng quá chậm. | Hệ thống lưu đánh giá thành công và kéo điểm trung bình của giày xuống. | Lưu đánh giá 1 sao thành công. | Pass |
| TC_REV_003 | Error Guessing (Đoán lỗi) | Kiểm tra chặn lỗi khi khách hàng bỏ trống nội dung bình luận | Khách hàng đã đăng nhập tài khoản. | Chọn 5 sao nhưng cố tình bỏ trống ô nội dung rồi bấm Gửi. | Số sao bằng 5 và Nội dung để trống hoặc chỉ gõ phím cách (Space). | Hệ thống chặn lại và báo lỗi Nội dung không được để trống. | Báo lỗi 400 và không lưu dữ liệu. | Pass |
| TC_REV_004 | BVA (Lỗi biên dưới Min-) | Kiểm tra chặn lỗi khi cố tình hack gửi đánh giá 0 sao | Khách hàng đã đăng nhập. Dùng Postman để gửi API. | Mở Postman gửi POST Request và nhập tham số trái phép. | Số sao bằng 0 và Nội dung là Test lỗi. | API chặn lại và báo lỗi Số sao phải từ 1 đến 5. | Báo lỗi 400 Bad Request hợp lệ. | Pass |
| TC_REV_005 | BVA (Lỗi biên trên Max+) | Kiểm tra chặn lỗi khi cố tình hack gửi đánh giá 6 sao | Khách hàng đã đăng nhập. Dùng Postman để gửi API. | Mở Postman gửi POST Request và nhập tham số trái phép. | Số sao bằng 6 và Nội dung là Test lỗi. | API chặn lại và báo lỗi Số sao không được vượt quá 5. | Báo lỗi 400 Bad Request hợp lệ. | Pass |
| TC_REV_006 | Phân hoạch lớp tương đương (EP) | Kiểm tra chặn tính năng đánh giá đối với Khách vãng lai | Người dùng chưa đăng nhập tài khoản (Guest). | Vào trang chi tiết sản phẩm và thử tìm nút Gửi đánh giá. | Lớp dữ liệu Khách vãng lai (Chưa Auth). | Giao diện không hiển thị form đánh giá và API trả về lỗi 401 Unauthorized. | Giao diện ẩn form và API chặn 401. | Pass |
