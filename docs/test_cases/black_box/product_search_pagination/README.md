# Black-Box API Test Cases: Chức năng 2 - Tìm kiếm & Phân trang Sản phẩm (Product Search & Pagination)

**Module:** `product_search_pagination`  
**Dự án:** ShoeShop Enterprise E-Commerce Testing Platform  
**Phương pháp kiểm thử:** Kiểm thử API Hộp đen (Black-Box API Testing) qua Postman / Newman & Python Automation.

---

## 1. Structure & Files Overview

Thư mục này chứa toàn bộ tài liệu thiết kế và tập file cấu hình Postman / JSON Test Collections được chia theo từng nhóm testcase nghiệp vụ của Chức năng 2:

```
test_cases/black_box/product_search_pagination/
├── Product_Search_Pagination_Postman_Collection.json   # Postman Collection tổng hợp đầy đủ các nhóm testcase
├── Product_Search_Pagination_Postman_Environment.json  # Postman Environment chứa các biến môi trường
├── group1_search_and_filtering.json                   # JSON Collection riêng cho Nhóm 1: Tìm kiếm & Lọc sản phẩm
├── group2_pagination_and_sorting.json                 # JSON Collection riêng cho Nhóm 2: Phân trang & Sắp xếp
├── group3_product_detail_lookup.json                  # JSON Collection riêng cho Nhóm 3: Tra cứu chi tiết sản phẩm
└── README.md                                          # Tài liệu hướng dẫn thực thi và ma trận phủ testcase
```

---

## 2. Test Design Techniques Applied

1. **Phân hoạch lớp tương đương (Equivalence Partitioning - EP):**
   - Phân chia các khoảng giá trị hợp lệ/không hợp lệ cho từ khóa (`name`), khoảng giá (`minPrice`, `maxPrice`), số trang (`page`), kích thước trang (`size`), thương hiệu (`brand`), danh mục (`category`).
2. **Phân tích giá trị biên (Boundary Value Analysis - BVA & Worst-Case BVA):**
   - Áp dụng các điểm biên $[1, 10]$ cho trang và $[1, 12]$ cho số sản phẩm mỗi trang.
   - Kiểm thử giá trị ngoài biên cực đại (`page=999999`, `size=999999`) để đảm bảo hệ thống không bị crash RAM (OOM) hoặc Database Timeout.
3. **Bảng quyết định (Decision Table):**
   - Phân loại rõ ràng các quy tắc nghiệp vụ khi kết hợp từ khóa, bộ lọc giá và trạng thái sản phẩm `ACTIVE`/`INACTIVE`.

---

## 3. Summary of Test Case Groups

### Group 1: Search & Filtering (Nhóm 1 - Tìm kiếm & Lọc sản phẩm)
- **TC_SRCH_01:** Tìm kiếm với từ khóa hợp lệ (`name=Nike`, `page=1`) -> `200 OK`, trả về danh sách sản phẩm khớp từ khóa.
- **TC_SRCH_02:** Tìm kiếm từ khóa không tồn tại (`name=XYZ_NOT_EXIST_123`) -> `200 OK`, `totalRecords = 0`, `list = []`.
- **TC_SRCH_03:** Tìm kiếm với chuỗi SQL Injection (`name=%25%27OR%271%3D1`) -> `200 OK`, xử lý an toàn qua Parameter Binding.
- **TC_SRCH_04:** Tìm kiếm với từ khóa rỗng (`name=`) -> `200 OK`, trả về danh sách mặc định trang 1.
- **TC_SRCH_05:** Tìm kiếm không phân biệt hoa thường (`name=nike` vs `name=NIKE`) -> `200 OK`, kết quả trùng khớp 100%.
- **TC_SRCH_06:** Kết hợp tìm kiếm từ khóa và lọc khoảng giá (`name=Nike`, `minPrice=100`, `maxPrice=300`) -> `200 OK`.
- **TC_SRCH_07:** Lọc theo Thương hiệu & Danh mục (`brand=Nike`, `category=Sneaker`) -> `200 OK`.

### Group 2: Pagination & Sorting (Nhóm 2 - Phân trang & Sắp xếp)
- **TC_PAG_01:** Phân trang trang 1 mặc định (`page=1`) -> `200 OK`, `currentPage = 1`, `maxResult = 12`.
- **TC_PAG_02:** Phân trang chuyển sang trang 2 (`page=2`) -> `200 OK`, `currentPage = 2`.
- **TC_PAG_03:** Phân trang với số trang âm (`page=-1`) -> `200 OK`, tự động ép về trang 1 (`currentPage = 1`).
- **TC_PAG_04:** Số trang vượt quá tổng số trang (`page=99999`) -> `200 OK`, `list = []`.
- **TC_PAG_05:** Phân trang kết hợp Sắp xếp theo giá (`sort=priceAsc`, `page=1`) -> `200 OK`, danh sách sắp xếp tăng dần.
- **TC_PAG_06:** Worst-Case BVA cực đại (`page=999999`, `size=999999`) -> `200 OK`, hệ thống tự giới hạn `maxResult = 12` an toàn.

### Group 3: Product Detail Lookup (Nhóm 3 - Tra cứu chi tiết sản phẩm)
- **TC_PROD_01:** Tra cứu sản phẩm hợp lệ (`GET /api/v1/products/S001`) -> `200 OK`, hiển thị đầy đủ `ProductInfo`.
- **TC_PROD_02:** Tra cứu mã sản phẩm không tồn tại (`GET /api/v1/products/INVALID_CODE_99`) -> `404 Not Found`.
- **TC_PROD_03:** Tra cứu sản phẩm bị `INACTIVE` (`GET /api/v1/products/INACTIVE_01`) -> `404 Not Found`.

---

## 4. Execution Guide (Hướng dẫn thực thi)

### Cách 1: Chạy trực tiếp trên Postman App
1. Khởi động ứng dụng **Postman**.
2. Chọn **Import** -> Chọn file `Product_Search_Pagination_Postman_Collection.json` (hoặc từng file nhóm `group1_...json`, `group2_...json`, `group3_...json`).
3. Import file `Product_Search_Pagination_Postman_Environment.json` vào mục Environment.
4. Chọn môi trường `Product_Search_Pagination_Postman_Environment` và nhấn **Run Collection**.

### Cách 2: Chạy tự động qua Newman Command Line (Dùng `npx newman`)
> **Lưu ý:** Nếu gõ `newman` bị báo lỗi *"The term 'newman' is not recognized"*, hãy thêm `npx` vào trước lệnh (vì `npx` được tích hợp sẵn trong Node.js để tự động chạy package).

```powershell
npx newman run docs/test_cases/black_box/product_search_pagination/Product_Search_Pagination_Postman_Collection.json `
  -e docs/test_cases/black_box/product_search_pagination/Product_Search_Pagination_Postman_Environment.json
```

### Cách 3: Chạy từng nhóm Testcase riêng lẻ
```powershell
# Nhóm 1: Tìm kiếm & Lọc sản phẩm
npx newman run docs/test_cases/black_box/product_search_pagination/group1_search_and_filtering.json `
  -e docs/test_cases/black_box/product_search_pagination/Product_Search_Pagination_Postman_Environment.json

# Nhóm 2: Phân trang & Sắp xếp
npx newman run docs/test_cases/black_box/product_search_pagination/group2_pagination_and_sorting.json `
  -e docs/test_cases/black_box/product_search_pagination/Product_Search_Pagination_Postman_Environment.json

# Nhóm 3: Tra cứu chi tiết sản phẩm
npx newman run docs/test_cases/black_box/product_search_pagination/group3_product_detail_lookup.json `
  -e docs/test_cases/black_box/product_search_pagination/Product_Search_Pagination_Postman_Environment.json
```
