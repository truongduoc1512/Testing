# Bảng Test Case: Chức năng 6 - Đánh giá sản phẩm (Review & Rating)
**Người thực hiện:** Được 

## 1. Thông tin Kỹ thuật & Thực thi
- **Kỹ thuật Thiết kế (Test Design):** 
  - **Phân hoạch lớp tương đương (EP):** Để tách biệt quyền Đăng nhập (Khách vãng lai vs Khách hàng).
  - **Phân tích giá trị biên (BVA):** Rất quan trọng để chặn số sao ngoài khoảng [1, 5] (VD: 0 sao, 6 sao).
  - **Bảng quyết định (Decision Table):** Kết hợp các ràng buộc thành 6 quy tắc cốt lõi (R1 - R5b).
- **Kỹ thuật Thực thi (Test Execution):** Kiểm thử API End-to-End (Black-box E2E API Testing).
- **File Code Thực thi (Automation Script):** Toàn bộ kịch bản được tự động hóa bằng phần mềm Postman/Newman và lưu tại file: `docs/Shoeshop_API_Collection.json`.

---

## 2. Phân tích Kỹ thuật Thiết kế (Test Design Analysis)

Theo chuẩn ISTQB, chức năng Đánh giá sản phẩm áp dụng đồng thời 3 kỹ thuật. Dưới đây là phân tích chi tiết cho từng kỹ thuật:

### 2.1 Bảng Phân hoạch lớp tương đương (EP)

| STT | Trường hợp của điều kiện đầu vào | Lớp tương đương Hợp lệ | Lớp tương đương Không hợp lệ |
| :---: | :--- | :--- | :--- |
| 1 | Trạng thái Đăng nhập | Đã đăng nhập (Customer) | Khách vãng lai (Guest) |
| 2 | Trạng thái Mua hàng | Đã mua sản phẩm này | Chưa mua sản phẩm này bao giờ |
| 3 | Nội dung đánh giá | Chuỗi văn bản hợp lệ (Có chữ/số) | Rỗng, Null, hoặc chỉ toàn khoảng trắng |

### 2.2 Bảng Phân tích giá trị biên (BVA)
**Ràng buộc đầu vào:** $1 \le \text{Số sao (Rating)} \le 5$ (Kiểu số nguyên nguyên dương).
Áp dụng công thức Standard BVA để tìm ra các điểm Test Case cốt lõi:

| Case | Số sao (Rating) | Phân loại BVA | Kết quả dự kiến (Expected Output) |
| :---: | :---: | :---: | :--- |
| 1 | 0 | min - 1 | Báo lỗi 400 (Số sao phải $\ge 1$) |
| 2 | 1 | min | Thành công (Lưu đánh giá 1 sao) |
| 3 | 3 | nom (Normal) | Thành công (Lưu đánh giá 3 sao) |
| 4 | 5 | max | Thành công (Lưu đánh giá 5 sao) |
| 5 | 6 | max + 1 | Báo lỗi 400 (Số sao phải $\le 5$) |

### 2.3 Bảng Quyết định tổng hợp (Collapsed Decision Table)
Gộp các vùng dữ liệu Hợp lệ và Không hợp lệ từ EP & BVA bên trên vào một Ma trận Quyết định để đảm bảo không bị sót luồng tích hợp:

| Condition/Action | R1 | R2 | R3 | R4 | R5a | R5b |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: |
| **C1: Đã Đăng nhập?** | N | Y | Y | Y | Y | Y |
| **C2: Số sao >= 1?** | - | N | Y | Y | Y | Y |
| **C3: Số sao <= 5?** | - | - | N | Y | Y | Y |
| **C4: Nội dung hợp lệ?** | - | - | - | N | Y | Y |
| **A1: Báo lỗi 401 (Chưa Login)** | X | - | - | - | - | - |
| **A2: Báo lỗi 400 (Sao < 1)** | - | X | - | - | - | - |
| **A3: Báo lỗi 400 (Sao > 5)** | - | - | X | - | - | - |
| **A4: Báo lỗi 400 (Nội dung trống)**| - | - | - | X | - | - |
| **A5a: Lưu đánh giá 1 sao (Min)** | - | - | - | - | X | - |
| **A5b: Lưu đánh giá 5 sao (Max)** | - | - | - | - | - | X |
| **Test Case Tương ứng** | TC_REV_006 | TC_REV_004 | TC_REV_005 | TC_REV_003 | TC_REV_002 | TC_REV_001 |

---

## 3. Bảng Test Case Chi Tiết

| Mã kiểm thử | Kỹ thuật áp dụng | Tiêu đề | Điều kiện tiên quyết | Các bước kiểm tra | Dữ liệu kiểm thử | Kết quả dự kiến | Kết quả thực tế | Trạng thái |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| TC_REV_001 | BVA (Biên trên Max / R5b) | Kiểm tra đánh giá hợp lệ với số sao cao nhất | Khách hàng đã mua hàng và đang đăng nhập. | Chọn số sao sau đó nhập bình luận và bấm Gửi. | Số sao bằng 5 và Nội dung là Giày đi rất êm và bền. | Hệ thống lưu đánh giá thành công và hiển thị 5 sao màu vàng lên giao diện. | Lưu vào Database thành công. | Pass |
| TC_REV_002 | BVA (Biên dưới Min / R5a) | Kiểm tra đánh giá hợp lệ với số sao thấp nhất | Khách hàng đã mua hàng và đang đăng nhập. | Chọn số sao sau đó nhập bình luận và bấm Gửi. | Số sao bằng 1 và Nội dung là Giao hàng quá chậm. | Hệ thống lưu đánh giá thành công và kéo điểm trung bình của giày xuống. | Lưu đánh giá 1 sao thành công. | Pass |
| TC_REV_003 | Đoán lỗi (R4) | Kiểm tra chặn lỗi khi khách hàng bỏ trống nội dung bình luận | Khách hàng đã đăng nhập tài khoản. | Chọn 5 sao nhưng cố tình bỏ trống ô nội dung rồi bấm Gửi. | Số sao bằng 5 và Nội dung để trống hoặc chỉ gõ phím cách (Space). | Hệ thống chặn lại và báo lỗi Nội dung không được để trống. | Báo lỗi 400 và không lưu dữ liệu. | Pass |
| TC_REV_004 | BVA (Lỗi biên dưới Min- / R2) | Kiểm tra chặn lỗi khi cố tình hack gửi đánh giá 0 sao | Khách hàng đã đăng nhập. Dùng Postman để gửi API. | Mở Postman gửi POST Request và nhập tham số trái phép. | Số sao bằng 0 và Nội dung là Test lỗi. | API chặn lại và báo lỗi Số sao phải từ 1 đến 5. | Báo lỗi 400 Bad Request hợp lệ. | Pass |
| TC_REV_005 | BVA (Lỗi biên trên Max+ / R3) | Kiểm tra chặn lỗi khi cố tình hack gửi đánh giá 6 sao | Khách hàng đã đăng nhập. Dùng Postman để gửi API. | Mở Postman gửi POST Request và nhập tham số trái phép. | Số sao bằng 6 và Nội dung là Test lỗi. | API chặn lại và báo lỗi Số sao không được vượt quá 5. | Báo lỗi 400 Bad Request hợp lệ. | Pass |
| TC_REV_006 | Phân hoạch EP (R1) | Kiểm tra chặn tính năng đánh giá đối với Khách vãng lai | Người dùng chưa đăng nhập tài khoản (Guest). | Vào trang chi tiết sản phẩm và thử tìm nút Gửi đánh giá. | Lớp dữ liệu Khách vãng lai (Chưa Auth). | Giao diện không hiển thị form đánh giá và API trả về lỗi 401 Unauthorized. | Giao diện ẩn form và API chặn 401. | Pass |
