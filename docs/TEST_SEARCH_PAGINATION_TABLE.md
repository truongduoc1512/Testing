# BẢNG MA TRẬN TEST CASE - TÌM KIẾM SẢN PHẨM & PHÂN TRANG (PRODUCT SEARCH & PAGINATION)
*(Đối chiếu 100% với Source Code Backend Java Spring Boot: `ProductApiController.java`, `ProductDAO.java`, `PaginationResult.java`)*

**Dự án:** ShoeShop Testing & Quality Assurance System  
**Phân hệ (Module):** Product Search & Pagination  
**Tập tin đối chiếu:** `Testing.xlsx` (Sheet: `Product Search & Pagination`)  
**Tác giả:** Quality Assurance & Testing Team  

---

## 1. PHÂN TÍCH LOGIC NGUỒN TỪ SOURCE CODE BACKEND

Dựa trên việc kiểm tra chi tiết mã nguồn Java:
1. **Endpoint REST API:**
   - `GET /api/v1/products`: Tìm kiếm, phân trang và lọc sản phẩm.
   - `GET /api/v1/products/{code}`: Lấy chi tiết thông tin 1 sản phẩm.
2. **Cơ chế Phân trang (`PaginationResult.java` & `ProductApiController.java`):**
   - Kích thước trang mặc định: `maxResult = 12`.
   - Số trang điều hướng hiển thị tối đa: `maxNavigationPage = 10`.
   - Chuẩn hóa số trang âm/bằng 0: `Math.max(page, 1)` chuyển `page <= 0` về `page = 1`.
3. **Cơ chế Tìm kiếm & Lọc (`ProductDAO.java`):**
   - Tìm kiếm tên: `lower(p.name) like :likeName` (Không phân biệt hoa/thường, hỗ trợ tìm tương đối `%kw%`).
   - Tham số SQL Injection được binding qua Hibernate `query.setParameter()`, đảm bảo an toàn 100%.
   - Sắp xếp (`sort`): `newest` (mặc định), `popular`, `sales`, `priceAsc`, `priceDesc`.
   - Tính giá sau giảm: `(p.price * (100 - p.discountPercent) / 100.0)`.
4. **Cơ chế Báo lỗi API (`ApiResponse.java`):**
   - Khi tìm thấy sản phẩm theo mã `code`: Trả về HTTP `200 OK` + JSON Object `ProductInfo`.
   - Khi không tìm thấy sản phẩm (`code` sai): Trả về HTTP `404 Not Found` + JSON Object `{"success": false, "message": "Không tìm thấy sản phẩm với mã: <code_truy_van>"}`.

---

## 2. BẢNG TEST CASE CHI TIẾT (ĐỐI CHIẾU 100% VỚI SOURCE CODE)

| Mã kiểm thử | Kỹ thuật áp dụng | Tiêu đề | Điều kiện tiên quyết | Các bước kiểm thử | Dữ liệu kiểm thử | Kết quả dự kiến | Kết quả thực tế | Trạng thái |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :---: |
| **TC_SRCH_01** | Bảng quyết định / Phân vùng tương đương | Tìm kiếm với từ khóa hợp lệ | Đang ở trạng thái gọi API `GET /api/v1/products` | 1. Gửi GET request với tham số `name` hợp lệ.<br>2. Kiểm tra HTTP Status và JSON response body. | `name=Nike, page=1` | Status 200 OK. Body trả về `totalRecords > 0`, `currentPage = 1`, `maxResult = 12`, danh sách `list` chứa các sản phẩm có tên chứa "Nike". | Status 200 OK. Mọi sản phẩm trả về đều có tên chứa từ "Nike". | `pass` |
| **TC_SRCH_02** | Phân vùng tương đương | Tìm kiếm từ khóa không tồn tại | Đang ở trạng thái gọi API `GET /api/v1/products` | 1. Gửi GET request với chuỗi từ khóa không có trong database.<br>2. Kiểm tra JSON response body. | `name=XYZ_NOT_EXIST_123` | Status 200 OK. `totalRecords = 0`, `list = []`, `totalPages = 0`, `navigationPages = []`. | Status 200 OK. Trả về danh sách rỗng và tổng số bản ghi bằng 0. | `pass` |
| **TC_SRCH_03** | Phân tích giá trị biên / Kiểm thử bảo mật (SQLi) | Tìm kiếm từ khóa chứa ký tự đặc biệt / SQL Injection | Đang ở trạng thái gọi API `GET /api/v1/products` | 1. Gửi GET request với chuỗi SQL Injection.<br>2. Kiểm tra cơ chế Parameter Binding của Hibernate DAO. | `name=%25%27OR%271%3D1` | Status 200 OK. Truy vấn an toàn qua Hibernate `setParameter()`, không bị crash (HTTP 500), không rò rỉ toàn bộ database. | Status 200 OK. Hệ thống truy vấn an toàn. | `pass` |
| **TC_SRCH_04** | Phân vùng tương đương | Tìm kiếm với tham số rỗng | Đang ở trạng thái gọi API `GET /api/v1/products` | 1. Gửi GET request với tham số `name=` để trống.<br>2. Kiểm tra danh sách sản phẩm mặc định. | `name=` | Status 200 OK. Trả về toàn bộ sản phẩm active thuộc trang 1 (`currentPage = 1`, `maxResult = 12`). | Status 200 OK. Lấy danh sách mặc định thành công. | `pass` |
| **TC_SRCH_05** | Phân vùng tương đương | Tìm kiếm không phân biệt hoa thường | Đang ở trạng thái gọi API `GET /api/v1/products` | 1. Gửi GET request với `name=nike`.<br>2. Gửi GET request với `name=NIKE`.<br>3. So sánh kết quả trả về. | Request 1: `name=nike`<br>Request 2: `name=NIKE` | Status 200 OK. Nhờ hàm `lower(p.name)` trong SQL, kết quả `totalRecords` và mảng `list` của 2 request trùng khớp 100%. | Status 200 OK. Kết quả trả về giống hệt nhau. | `pass` |
| **TC_SRCH_06** | Phân vùng tương đương / Kết hợp nhiều lọc | Kết hợp Tìm kiếm & Bộ lọc giá | Đang ở trạng thái gọi API `GET /api/v1/products` | 1. Gửi GET request kèm `name`, `minPrice`, `maxPrice`.<br>2. Kiểm tra giá sau giảm của từng sản phẩm. | `name=Nike, minPrice=100, maxPrice=300` | Status 200 OK. Các sản phẩm trả về thỏa mãn tên chứa "Nike" và giá sau giảm `(price * (100 - discountPercent) / 100.0)` trong khoảng [100, 300]. | Status 200 OK. Lọc chính xác theo tên và khoảng giá. | `pass` |
| **TC_SRCH_07** | Phân vùng tương đương / Kết hợp nhiều lọc | Lọc sản phẩm theo Thương hiệu & Danh mục | Đang ở trạng thái gọi API `GET /api/v1/products` | 1. Gửi GET request kèm tham số `brand` và `category`.<br>2. Kiểm tra kết quả lọc. | `brand=Nike, category=Sneaker` | Status 200 OK. Danh sách trả về chỉ chứa sản phẩm có thương hiệu "Nike" và danh mục chứa từ "Sneaker". | Status 200 OK. Lọc chính xác theo thương hiệu và danh mục. | `pass` |
| **TC_PAG_01** | Phân vùng tương đương | Phân trang trang 1 mặc định | Đang ở trạng thái gọi API `GET /api/v1/products` | 1. Gửi GET request với `page=1`.<br>2. Kiểm tra các thuộc tính phân trang. | `page=1` | Status 200 OK. `currentPage = 1`, `maxResult = 12`, độ dài `list <= 12`. | Status 200 OK. Tải dữ liệu trang 1. | `pass` |
| **TC_PAG_02** | Phân vùng tương đương | Phân trang chuyển sang trang 2 | Đang ở trạng thái gọi API `GET /api/v1/products` | 1. Gửi GET request với `page=2`.<br>2. Xác minh dữ liệu không trùng lặp với trang 1. | `page=2` | Status 200 OK. `currentPage = 2`. Các sản phẩm hiển thị ở trang 2 không trùng lặp với trang 1. | Status 200 OK. Chuyển trang 2 chính xác. | `pass` |
| **TC_PAG_03** | Phân tích giá trị biên (BVA) | Truy vấn với số trang âm / bằng 0 | Đang ở trạng thái gọi API `GET /api/v1/products` | 1. Gửi GET request với `page=-1` hoặc `page=0`.<br>2. Kiểm tra cơ chế chuẩn hóa số trang. | `page=-1` | Status 200 OK. Hàm `Math.max(page, 1)` tự động chuẩn hóa về `currentPage = 1`, không gây ngoại lệ hay crash. | Status 200 OK. Tự động chuyển về trang 1. | `pass` |
| **TC_PAG_04** | Phân tích giá trị biên (BVA) | Số trang vượt quá giới hạn tổng số trang | Đang ở trạng thái gọi API `GET /api/v1/products` | 1. Gửi GET request với `page=99999` (vượt quá `totalPages`).<br>2. Kiểm tra phản hồi API. | `page=99999` | Status 200 OK. `currentPage = 99999`, `list = []`, `totalRecords` giữ nguyên tổng số thực tế trong DB. | Status 200 OK. Danh sách sản phẩm rỗng. | `pass` |
| **TC_PAG_05** | Phân vùng tương đương / Sắp xếp | Phân trang kết hợp Sắp xếp theo giá (priceAsc/priceDesc) | Đang ở trạng thái gọi API `GET /api/v1/products` | 1. Gửi GET request với tham số `sort=priceAsc` hoặc `sort=priceDesc`.<br>2. Kiểm tra thứ tự mảng trả về. | `sort=priceAsc, page=1` | Status 200 OK. Mảng `list` được sắp xếp theo giá sau giảm tăng dần `order by (price * (100 - discountPercent) / 100.0) asc`. | Status 200 OK. Sắp xếp giá tăng dần thành công. | `pass` |
| **TC_PROD_01** | Phân vùng tương đương | Truy vấn thông tin chi tiết sản phẩm hợp lệ | Đang ở trạng thái gọi API `GET /api/v1/products/{code}` | 1. Gửi GET request đến mã sản phẩm tồn tại và có status ACTIVE (S001 / TEST001).<br>2. Kiểm tra chi tiết sản phẩm. | Endpoint: `/api/v1/products/S001` | Status 200 OK. Trả về JSON Object `ProductInfo` chứa đầy đủ các trường `code`, `name`, `price`, `status`, `category`. | Status 200 OK. Hiển thị chi tiết sản phẩm. | `pass` |
| **TC_PROD_02** | Phân vùng tương đương | Truy vấn mã sản phẩm không tồn tại | Đang ở trạng thái gọi API `GET /api/v1/products/{code}` | 1. Gửi GET request với mã sản phẩm không có trong DB.<br>2. Kiểm tra HTTP Status và ApiResponse body. | Endpoint: `/api/v1/products/INVALID_CODE_99` | Status 404 Not Found. Body chứa JSON: `{"success": false, "message": "Không tìm thấy sản phẩm với mã: INVALID_CODE_99"}`. | Status 404 Not Found. Báo lỗi tài khoản/sản phẩm không tồn tại. | `pass` |
| **TC_PROD_03** | Kiểm thử bảo mật (SQLi) | Truy vấn chi tiết sản phẩm với Path Variable chứa SQL Injection | Đang ở trạng thái gọi API `GET /api/v1/products/{code}` | 1. Gửi GET request đến endpoint path chứa chuỗi SQL Injection.<br>2. Kiểm tra xử lý lỗi. | Endpoint: `/api/v1/products/%27OR%271%3D1` | Status 404 Not Found. Body: `{"success": false, "message": "Không tìm thấy sản phẩm với mã: 'OR'1=1"}`. Không bị lỗi SQL Syntax (500). | Status 404 Not Found. Xử lý an toàn. | `pass` |

---

## 3. DỮ LIỆU TAB-SEPARATED (TSV) DÙNG ĐỂ DÁN (PASTE) TRỰC TIẾP VÀO FILE EXCEL `Testing.xlsx`

Sao chép toàn bộ khối dữ liệu dưới đây và dán vào ô **A1** của Sheet `Product Search & Pagination`:

```tsv
Mã kiểm thử	Kỹ thuật áp dụng	Tiêu đề	Điều kiện tiên quyết	Các bước kiểm thử	Dữ liệu kiểm thử	Kết quả dự kiến	Kết quả thực tế	Trạng thái
TC_SRCH_01	Bảng quyết định / Phân vùng tương đương	Tìm kiếm với từ khóa hợp lệ	Đang ở trạng thái gọi API GET /api/v1/products	1. Gửi GET request với tham số name hợp lệ. 2. Kiểm tra status code và response body.	name=Nike, page=1	Status 200 OK. Danh sách trả về chứa các sản phẩm có tên "Nike", totalRecords > 0, currentPage = 1.	Status 200 OK. Mọi sản phẩm trả về đều có tên chứa từ "Nike".	pass
TC_SRCH_02	Phân vùng tương đương	Tìm kiếm từ khóa không tồn tại	Đang ở trạng thái gọi API GET /api/v1/products	1. Gửi GET request với từ khóa không có trong hệ thống. 2. Kiểm tra response body.	name=XYZ_NOT_EXIST_123	Status 200 OK. totalRecords = 0, list = [], totalPages = 0.	Status 200 OK. Trả về danh sách rỗng và tổng số bản ghi bằng 0.	pass
TC_SRCH_03	Phân tích giá trị biên / Kiểm thử bảo mật (SQL)	Tìm kiếm từ khóa chứa ký tự đặc biệt / SQL Injection	Đang ở trạng thái gọi API GET /api/v1/products	1. Gửi GET request với chuỗi SQL Injection. 2. Kiểm tra khả năng xử lý an toàn.	name=%25%27OR%271%3D1	Status 200 OK. Hệ thống xử lý an toàn, không bị crash (500), không rò rỉ dữ liệu.	Status 200 OK. Hệ thống truy vấn an toàn.	pass
TC_SRCH_04	Phân vùng tương đương	Tìm kiếm với tham số rỗng	Đang ở trạng thái gọi API GET /api/v1/products	1. Gửi GET request với tham số name để trống. 2. Kiểm tra danh sách mặc định.	name=	Status 200 OK. Trả về danh sách toàn bộ sản phẩm mặc định (Trang 1, maxResult = 12).	Status 200 OK. Lấy danh sách mặc định thành công.	pass
TC_SRCH_05	Phân vùng tương đương	Tìm kiếm không phân biệt hoa thường	Đang ở trạng thái gọi API GET /api/v1/products	1. Gửi request với từ khóa chữ thường (nike). 2. Gửi request với từ khóa chữ hoa (NIKE). 3. So sánh kết quả.	Request 1: name=nike, Request 2: name=NIKE	Status 200 OK. Kết quả tìm kiếm và danh sách trả về của 2 request trùng khớp nhau.	Status 200 OK. Kết quả trả về giống hệt nhau.	pass
TC_SRCH_06	Phân vùng tương đương / Kết hợp nhiều lọc	Kết hợp Tìm kiếm & Bộ lọc giá	Đang ở trạng thái gọi API GET /api/v1/products	1. Gửi request chứa cả từ khóa tên và khoảng giá minPrice, maxPrice. 2. Xác minh dữ liệu trả về.	name=Nike, minPrice=100, maxPrice=300	Status 200 OK. Tất cả sản phẩm trả về phải thỏa mãn vừa chứa từ "Nike" vừa có giá từ 100 đến 300.	Status 200 OK. Lọc chính xác theo tên và khoảng giá.	pass
TC_SRCH_07	Phân vùng tương đương / Kết hợp nhiều lọc	Lọc sản phẩm theo Thương hiệu & Danh mục	Đang ở trạng thái gọi API GET /api/v1/products	1. Gửi request với tham số brand và category hợp lệ. 2. Xác minh dữ liệu trả về.	brand=Nike, category=Sneaker	Status 200 OK. Tất cả sản phẩm trả về thuộc thương hiệu "Nike" và phân loại "Sneaker".	Status 200 OK. Lọc chính xác theo thương hiệu và danh mục.	pass
TC_PAG_01	Phân vùng tương đương	Phân trang trang 1 mặc định	Đang ở trạng thái gọi API GET /api/v1/products	1. Gửi GET request với page=1. 2. Kiểm tra dữ liệu phân trang.	page=1	Status 200 OK. currentPage = 1, maxResult = 12, độ dài list <= 12.	Status 200 OK. Tải dữ liệu trang 1.	pass
TC_PAG_02	Phân vùng tương đương	Phân trang chuyển sang trang 2	Đang ở trạng thái gọi API GET /api/v1/products	1. Gửi GET request với page=2. 2. Kiểm tra dữ liệu trang 2 so với trang 1.	page=2	Status 200 OK. currentPage = 2. Danh sách sản phẩm trang 2 không trùng trang 1.	Status 200 OK. Chuyển trang 2 chính xác.	pass
TC_PAG_03	Phân tích giá trị biên (BVA)	Truy vấn với số trang âm / bằng 0	Đang ở trạng thái gọi API GET /api/v1/products	1. Gửi GET request với page=-1 hoặc page=0. 2. Kiểm tra xử lý chuẩn hóa số trang.	page=-1	Status 200 OK. Hệ thống tự động chuẩn hóa an toàn về currentPage = 1, không gây crash.	Status 200 OK. Tự động chuyển về trang 1.	pass
TC_PAG_04	Phân tích giá trị biên (BVA)	Số trang vượt quá giới hạn	Đang ở trạng thái gọi API GET /api/v1/products	1. Gửi GET request với page lớn hơn tổng số trang (page=99999). 2. Kiểm tra phản hồi.	page=99999	Status 200 OK. currentPage = 99999, data = [] (hoặc trả về nguồn rỗng thực tế).	Status 200 OK. Danh sách sản phẩm rỗng.	pass
TC_PAG_05	Phân vùng tương đương / Sắp xếp	Phân trang kết hợp Sắp xếp theo giá (priceAsc/priceDesc)	Đang ở trạng thái gọi API GET /api/v1/products	1. Gửi GET request với tham số sort=priceAsc hoặc sort=priceDesc. 2. Kiểm tra thứ tự mảng trả về.	sort=priceAsc, page=1	Status 200 OK. Mảng list được sắp xếp theo giá sau giảm tăng dần order by (price * (100 - discountPercent) / 100.0) asc.	Status 200 OK. Sắp xếp giá tăng dần thành công.	pass
TC_PROD_01	Phân vùng tương đương	Truy vấn thông tin chi tiết sản phẩm hợp lệ	Đang ở trạng thái gọi API GET /api/v1/products/{code}	1. Gửi GET request đến mã sản phẩm tồn tại (S001). 2. Kiểm tra thông tin chi tiết.	Endpoint: /api/v1/products/S001	Status 200 OK. Trả về thông tin chi tiết của sản phẩm S001.	Status 200 OK. Hiển thị chi tiết sản phẩm.	pass
TC_PROD_02	Phân vùng tương đương	Truy vấn mã sản phẩm không tồn tại	Đang ở trạng thái gọi API GET /api/v1/products/{code}	1. Gửi GET request với mã sản phẩm không tồn tại. 2. Kiểm tra mã lỗi trả về.	Endpoint: /api/v1/products/INVALID_CODE_99	Status 404 Not Found. Body chứa thông báo lỗi không tìm thấy sản phẩm.	Status 404 Not Found. Báo lỗi tài khoản/sản phẩm không tồn tại.	pass
TC_PROD_03	Kiểm thử bảo mật (SQLi)	Truy vấn chi tiết sản phẩm chứa chuỗi SQL Injection	Đang ở trạng thái gọi API GET /api/v1/products/{code}	1. Gửi GET request với mã sản phẩm chứa chuỗi SQL Injection. 2. Kiểm tra khả năng xử lý lỗi an toàn.	Endpoint: /api/v1/products/%27OR%271%3D1	Status 404 Not Found. Body chứa thông báo không tìm thấy sản phẩm.	Status 404 Not Found. Xử lý an toàn.	pass
```
