# Bảng Test Case: Chức năng 2 - Tìm kiếm & Phân trang Sản phẩm (Search & Pagination)
**Người thực hiện:** Thịnh

## 1. Thông tin Kỹ thuật & Thực thi
- **Kỹ thuật Thiết kế (Test Design):**
  - **Phân hoạch lớp tương đương (EP):** Chia lớp hợp lệ/không hợp lệ cho các tham số tìm kiếm (`name`, `brand`, `category`) và phân trang (`page`, `size`).
  - **Phân tích giá trị biên (BVA & Worst-Case BVA):** Áp dụng để tìm các lỗi tràn bộ nhớ (Load Test / Performance) khi người dùng nhập số trang khổng lồ.
  - **Bảng quyết định (Decision Table):** Kết hợp các điều kiện lọc để phân định rạch ròi luồng trả về kết quả hoặc báo lỗi.
- **Kỹ thuật Thực thi (Test Execution):** Kiểm thử API Tự động (Black-box API Testing) kết hợp Kiểm thử Tải (Load Testing).
- **File Code Thực thi (Automation Script):** `scripts/test_search_pagination_api.py`

---

## 2. Phân tích Kỹ thuật Thiết kế (Test Design Analysis)

### 2.1 Bảng Phân hoạch lớp tương đương (EP)

| STT | Tham số đầu vào (Input) | Lớp tương đương Hợp lệ | Lớp tương đương Không hợp lệ |
| :---: | :--- | :--- | :--- |
| 1 | Từ khóa (`name`) | Chuỗi ký tự, Tồn tại trong DB | Chứa SQL Injection (`'OR'1'='1`), Không tồn tại |
| 2 | Khoảng giá (`minPrice`, `maxPrice`) | $0 \le min \le max$ | $min < 0$, $min > max$ |
| 3 | Trạng thái Sản phẩm | ACTIVE (Đang kinh doanh) | INACTIVE, DRAFT |

### 2.2 Bảng Phân tích giá trị biên cực đại (Worst-Case BVA & Load Test)
Kiểm thử ranh giới hiệu năng để đảm bảo Server không bị sập (Crash/Timeout) khi nhận tham số phân trang khổng lồ:

| Biến đầu vào | Ràng buộc logic | Các giá trị biên kiểm thử (BVA Points) | Kết quả kỳ vọng (Load/Performance) |
| :--- | :--- | :--- | :--- |
| **Số trang (`page`)** | $page \ge 1$ | -1, 0, 1 (nom), 99999, 999999 (Max+) | Chặn page < 1 (Quy về 1), Giới hạn mảng rỗng nếu vượt quá DB |
| **Kích thước (`size`)** | $size \le 12$ | 0, 1, 12 (nom), 100, 999999 (Max+) | Bắt buộc ép `maxResult = 12`, không cho phép truy vấn hàng vạn dòng gây tràn RAM |

### 2.3 Bảng Quyết định tổng hợp (Collapsed Decision Table)

| Condition/Action | R1 | R2 | R3 | R4 | R5 |
| :--- | :---: | :---: | :---: | :---: | :---: |
| **C1: Mã / Từ khóa tồn tại?** | N | Y | Y | Y | Y |
| **C2: Trạng thái ACTIVE?** | - | N | Y | Y | Y |
| **C3: Khớp bộ lọc (Bộ lọc khác)?** | - | - | N | Y | Y |
| **C4: Page hợp lệ (>= 1)?** | - | - | - | N | Y |
| **A1: Báo lỗi 404 (Không tìm thấy SP)** | X | - | - | - | - |
| **A2: Báo lỗi 404 (Sản phẩm ngừng bán)** | - | X | - | - | - |
| **A3: Status 200, Trả về `list: []`** | - | - | X | - | - |
| **A4: Status 200, Ép `page = 1`** | - | - | - | X | - |
| **A5: Status 200, Trả kết quả phân trang** | - | - | - | - | X |
| **Test Case Tương ứng** | TC_PROD_02 | TC_PROD_03 | TC_SRCH_02 | TC_PAG_03 | TC_PAG_01, SRCH_01 |

---

## 3. Bảng Test Case Chi Tiết

### 2. Danh sách Test Cases

| Mã kiểm thử | Kỹ thuật áp dụng | Tiêu đề | Điều kiện tiên quyết | Các bước kiểm tra | Dữ liệu kiểm thử | Kết quả dự kiến | Kết quả thực tế | Trạng thái |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :---: |
| **TC_SRCH_01** | Bảng quyết định (Rule 5) / Phân vùng tương đương | Tìm kiếm với từ khóa hợp lệ | Đang ở trạng thái gọi API `GET /api/v1/products` | 1. Gửi GET request với tham số `name` hợp lệ.. 2. Kiểm tra HTTP Status Code và JSON response body. | `name=Nike, page=1` | Status 200 OK. Body trả về `totalRecords > 0`, `currentPage = 1`, `maxResult = 12`, danh sách `list` chứa các sản phẩm có tên chứa "Nike". | Status 200 OK. Mọi sản phẩm trả về đều có tên chứa từ "Nike". | Pass |
| **TC_SRCH_02** | Bảng quyết định (Rule 3) / Phân vùng tương đương | Tìm kiếm từ khóa không tồn tại | Đang ở trạng thái gọi API `GET /api/v1/products` | 1. Gửi GET request với chuỗi từ khóa không có trong database.. 2. Kiểm tra JSON response body. | `name=XYZ_NOT_EXIST_123` | Status 200 OK. `totalRecords = 0`, `list = []`, `totalPages = 0`, `navigationPages = []`. | Status 200 OK. Trả về danh sách rỗng và tổng số bản ghi bằng 0. | Pass |
| **TC_SRCH_03** | BVA / Kiểm thử bảo mật (SQLi) | Tìm kiếm từ khóa chứa ký tự đặc biệt / SQL Injection | Đang ở trạng thái gọi API `GET /api/v1/products` | 1. Gửi GET request với chuỗi SQL Injection.. 2. Kiểm tra cơ chế Parameter Binding của Hibernate DAO. | `name=%25%27OR%271%3D1` | Status 200 OK. Truy vấn an toàn qua Hibernate `setParameter()`, không bị crash (HTTP 500), không rò rỉ toàn bộ database. | Status 200 OK. Hệ thống truy vấn an toàn. | Pass |
| **TC_SRCH_04** | Phân vùng tương đương | Tìm kiếm với tham số rỗng | Đang ở trạng thái gọi API `GET /api/v1/products` | 1. Gửi GET request với tham số `name=` để trống.. 2. Kiểm tra danh sách sản phẩm mặc định. | `name=` | Status 200 OK. Trả về toàn bộ sản phẩm active thuộc trang 1 (`currentPage = 1`, `maxResult = 12`). | Status 200 OK. Lấy danh sách mặc định thành công. | Pass |
| **TC_SRCH_05** | Phân vùng tương đương | Tìm kiếm không phân biệt hoa thường | Đang ở trạng thái gọi API `GET /api/v1/products` | 1. Gửi GET request với `name=nike`.. 2. Gửi GET request với `name=NIKE`.. 3. So sánh kết quả trả về. | Request 1: `name=nike`. Request 2: `name=NIKE` | Status 200 OK. Nhờ hàm `lower(p.name)` trong SQL, kết quả `totalRecords` và mảng `list` của 2 request trùng khớp 100%. | Status 200 OK. Kết quả trả về giống hệt nhau. | Pass |
| **TC_SRCH_06** | Phân vùng tương đương / Kết hợp nhiều lọc | Kết hợp Tìm kiếm & Bộ lọc giá | Đang ở trạng thái gọi API `GET /api/v1/products` | 1. Gửi GET request kèm `name`, `minPrice`, `maxPrice`.. 2. Kiểm tra giá sau giảm của từng sản phẩm. | `name=Nike, minPrice=100, maxPrice=300` | Status 200 OK. Các sản phẩm trả về thỏa mãn tên chứa "Nike" và giá sau giảm `(price * (100 - discountPercent) / 100.0)` trong khoảng [100, 300]. | Status 200 OK. Lọc chính xác theo tên và khoảng giá. | Pass |
| **TC_SRCH_07** | Phân vùng tương đương / Kết hợp nhiều lọc | Lọc sản phẩm theo Thương hiệu & Danh mục | Đang ở trạng thái gọi API `GET /api/v1/products` | 1. Gửi GET request kèm tham số `brand` và `category`.. 2. Kiểm tra kết quả lọc. | `brand=Nike, category=Sneaker` | Status 200 OK. Danh sách trả về chỉ chứa sản phẩm có thương hiệu "Nike" và danh mục chứa từ "Sneaker". | Status 200 OK. Lọc chính xác theo thương hiệu và danh mục. | Pass |
| **TC_PAG_01** | Bảng quyết định (Rule 5) / Phân vùng tương đương | Phân trang trang 1 mặc định | Đang ở trạng thái gọi API `GET /api/v1/products` | 1. Gửi GET request với `page=1`.. 2. Kiểm tra các thuộc tính phân trang. | `page=1` | Status 200 OK. `currentPage = 1`, `maxResult = 12`, độ dài `list <= 12`. | Status 200 OK. Tải dữ liệu trang 1. | Pass |
| **TC_PAG_02** | Phân vùng tương đương | Phân trang chuyển sang trang 2 | Đang ở trạng thái gọi API `GET /api/v1/products` | 1. Gửi GET request với `page=2`.. 2. Xác minh dữ liệu không trùng lặp với trang 1. | `page=2` | Status 200 OK. `currentPage = 2`. Các sản phẩm hiển thị ở trang 2 không trùng lặp với trang 1. | Status 200 OK. Chuyển trang 2 chính xác. | Pass |
| **TC_PAG_03** | Bảng quyết định (Rule 4) / BVA | Truy vấn với số trang âm / bằng 0 | Đang ở trạng thái gọi API `GET /api/v1/products` | 1. Gửi GET request với `page=-1` hoặc `page=0`.. 2. Kiểm tra cơ chế chuẩn hóa số trang. | `page=-1` | Status 200 OK. Hàm `Math.max(page, 1)` tự động chuẩn hóa về `currentPage = 1`, không gây ngoại lệ hay crash. | Status 200 OK. Tự động chuyển về trang 1. | Pass |
| **TC_PAG_04** | BVA | Số trang vượt quá giới hạn tổng số trang | Đang ở trạng thái gọi API `GET /api/v1/products` | 1. Gửi GET request với `page=99999` (vượt quá `totalPages`).. 2. Kiểm tra phản hồi API. | `page=99999` | Status 200 OK. `currentPage = 99999`, `list = []`, `totalRecords` giữ nguyên tổng số thực tế trong DB. | Status 200 OK. Danh sách sản phẩm rỗng. | Pass |
| **TC_PAG_05** | Phân vùng tương đương / Sắp xếp | Phân trang kết hợp Sắp xếp theo giá (priceAsc/priceDesc) | Đang ở trạng thái gọi API `GET /api/v1/products` | 1. Gửi GET request với tham số `sort=priceAsc` hoặc `sort=priceDesc`.. 2. Kiểm tra thứ tự mảng trả về. | `sort=priceAsc, page=1` | Status 200 OK. Mảng `list` được sắp xếp theo giá sau giảm tăng dần `order by (price * (100 - discountPercent) / 100.0) asc`. | Status 200 OK. Sắp xếp giá tăng dần thành công. | Pass |
| **TC_PAG_06** | Worst-Case Boundary Value Analysis (BVA $5^n$) | Kiểm thử giá trị biên cực đại (Worst-Case BVA) với `page` và `size` cực lớn | Đang ở trạng thái gọi API `GET /api/v1/products` | 1. Gửi GET request với `page=999999` và `size=999999`.. 2. Kiểm tra Response Time và HTTP Status Code.. 3. Kiểm tra số lượng bản ghi trong mảng `list`. | `page=999999, size=999999` | Status 200 OK. Hệ thống tự động giới hạn `maxResult = 12` (hoặc mảng `list = []`), không bị quá tải, rò rỉ bộ nhớ (RAM Crash 500) hay treo Database (Timeout). | Status 200 OK. Phản hồi mảng `list: []` an toàn, giới hạn `maxResult = 12`, không quá tải Server. | Pass |
| **TC_PROD_01** | Bảng quyết định (Rule 5) | Truy vấn thông tin chi tiết sản phẩm hợp lệ | Đang ở trạng thái gọi API `GET /api/v1/products/{code}` | 1. Gửi GET request đến mã sản phẩm tồn tại và có status ACTIVE (S001 / TEST001).. 2. Kiểm tra chi tiết sản phẩm. | Endpoint: `/api/v1/products/S001` | Status 200 OK. Trả về JSON Object `ProductInfo` chứa đầy đủ các trường `code`, `name`, `price`, `status`, `category`. | Status 200 OK. Hiển thị chi tiết sản phẩm. | Pass |
| **TC_PROD_02** | Bảng quyết định (Rule 1) | Truy vấn mã sản phẩm không tồn tại | Đang ở trạng thái gọi API `GET /api/v1/products/{code}` | 1. Gửi GET request với mã sản phẩm không có trong DB.. 2. Kiểm tra HTTP Status và ApiResponse body. | Endpoint: `/api/v1/products/INVALID_CODE_99` | Status 404 Not Found. Body chứa JSON: `{"success": false, "message": "Không tìm thấy sản phẩm với mã: INVALID_CODE_99"}`. | Status 404 Not Found. Báo lỗi tài khoản/sản phẩm không tồn tại. | Pass |
| **TC_PROD_03** | Bảng quyết định (Rule 2) / Kiểm thử bảo mật | Truy vấn sản phẩm bị ngừng kinh doanh (INACTIVE) / SQLi | Đang ở trạng thái gọi API `GET /api/v1/products/{code}` | 1. Gửi GET request với mã sản phẩm có status INACTIVE hoặc chuỗi SQL Injection.. 2. Kiểm tra xử lý lỗi. | Endpoint: `/api/v1/products/INACTIVE_01` | Status 404 Not Found. Body: `{"success": false, "message": "Không tìm thấy sản phẩm..."}`. Hệ thống xử lý an toàn. | Status 404 Not Found. Xử lý an toàn. | Pass |

 

