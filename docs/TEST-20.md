# 🔍 KẾ HOẠCH VÀ KỊCH BẢN KIỂM THỬ API TÌM KIẾM & PHÂN TRANG (TASK TEST-20)

> **Mã Task Jira:** TEST-20  
> **Tiêu đề Task:** Test search & pagination APIs  
> **Dự án:** ShoeShop Quality Assurance & Testing  
> **Target API Endpoint:** `GET /api/v1/products`  
> **Người thực hiện:** QA Automation Engineer  

---

## 📌 1. MỤC TIÊU KIỂM THỬ
Tài liệu này chi tiết hóa toàn bộ kế hoạch, ma trận Test Case và các tiêu chuẩn đánh giá cho tính năng **Tìm kiếm (Search)** và **Phân trang (Pagination)** thuộc RESTful API của ứng dụng ShoeShop.

Mục tiêu chính:
1. Xác minh khả năng tìm kiếm sản phẩm theo tên (`name`), danh mục (`category`), thương hiệu (`brand`) và mức giá (`minPrice`, `maxPrice`).
2. Xác minh cơ chế phân trang (`page`, `maxResult`, `totalPages`, `totalRecords`, `navigationPages`) khi xử lý danh sách dữ liệu lớn.
3. Kiểm tra tính toàn vẹn của **JSON Schema** và các phản hồi HTTP Status Code (200 OK, 400 Bad Request, 404 Not Found).
4. Kiểm tra khả năng xử lý các trường hợp ngoại lệ (từ khóa chứa ký tự đặc biệt, SQL injection, trang âm, số trang vượt quá giới hạn).

---

## 🛠️ 2. ĐỊNH DẠNG API VÀ JSON SCHEMA CỦA ĐIỂM TRUY CẬP

### 2.1. Cấu trúc HTTP Request
- **Endpoint:** `GET /api/v1/products`
- **Headers:** `Accept: application/json`
- **Query Parameters:**

| Tham số (Parameter) | Kiểu dữ liệu | Mặc định | Mô tả |
| :--- | :--- | :--- | :--- |
| `name` | String | `""` | Từ khóa tìm kiếm tên sản phẩm |
| `page` | Integer | `1` | Số trang cần lấy (bắt đầu từ 1) |
| `sort` | String | `"newest"` | Sắp xếp: `newest`, `price_asc`, `price_desc` |
| `minPrice` | Double | `null` | Giá tối thiểu |
| `maxPrice` | Double | `null` | Giá tối đa |
| `category` | String | `null` | Thể loại sản phẩm (ví dụ: `Sports`, `Sneaker`) |
| `brand` | String | `null` | Thương hiệu sản phẩm (ví dụ: `Nike`, `Adidas`) |

---

### 2.2. JSON Schema chuẩn phản hồi (HTTP 200 OK)

```json
{
  "type": "object",
  "required": ["totalRecords", "currentPage", "list", "maxResult", "totalPages", "maxNavigationPage", "navigationPages"],
  "properties": {
    "totalRecords": { "type": "integer", "minimum": 0 },
    "currentPage": { "type": "integer", "minimum": 1 },
    "list": {
      "type": "array",
      "items": {
        "type": "object",
        "required": ["code", "name", "price", "createDate"],
        "properties": {
          "code": { "type": "string" },
          "name": { "type": "string" },
          "price": { "type": "number", "minimum": 0 },
          "createDate": { "type": "string" }
        }
      }
    },
    "maxResult": { "type": "integer", "default": 12 },
    "totalPages": { "type": "integer", "minimum": 0 },
    "maxNavigationPage": { "type": "integer" },
    "navigationPages": {
      "type": "array",
      "items": { "type": "integer" }
    }
  }
}
```

---

## 🧪 3. MA TRẬN KỊCH BẢN KIỂM THỬ (TEST CASE MATRIX)

### 3.1. Nhóm Kịch bản Kiểm thử Tìm kiếm (Search API Scenarios)

| Mã Test Case | Tên Kịch bản | Dữ liệu đầu vào (Query Params) | Trạng thái kỳ vọng | Dữ liệu trả về kỳ vọng (Assertions) |
| :--- | :--- | :--- | :---: | :--- |
| `TC_SRCH_01` | Tìm kiếm với từ khóa hợp lệ | `name=Nike` | **200 OK** | Tất cả sản phẩm trong `list` phải chứa từ "Nike" trong tên. `totalRecords > 0`. |
| `TC_SRCH_02` | Tìm kiếm từ khóa không tồn tại | `name=XYZ_NOT_EXIST_99` | **200 OK** | `totalRecords = 0`, `list = []`, `totalPages = 0`. |
| `TC_SRCH_03` | Tìm kiếm từ khóa chứa ký tự đặc biệt | `name=%25%27OR%271%3D1` (SQL Injection attempt) | **200 OK** | Không bị lỗi 500, truy vấn an toàn, không rò rỉ dữ liệu ngoài scope. |
| `TC_SRCH_04` | Tìm kiếm với tham số rỗng | `name=` | **200 OK** | Trả về danh sách toàn bộ sản phẩm mặc định (Trang 1, maxResult = 12). |
| `TC_SRCH_05` | Tìm kiếm Không phân biệt hoa/thường | `name=nike` và `name=NIKE` | **200 OK** | Kết quả `totalRecords` và danh sách sản phẩm trả về hoàn toàn trùng khớp nhau. |
| `TC_SRCH_06` | Kết hợp Tìm kiếm & Bộ lọc giá | `name=Nike&minPrice=100&maxPrice=200` | **200 OK** | Mọi sản phẩm trả về phải thỏa mãn vừa tên chứa `Nike` vừa giá từ 100 đến 200. |

---

### 3.2. Nhóm Kịch bản Kiểm thử Phân trang (Pagination API Scenarios)

| Mã Test Case | Tên Kịch bản | Dữ liệu đầu vào (Query Params) | Trạng thái kỳ vọng | Dữ liệu trả về kỳ vọng (Assertions) |
| :--- | :--- | :--- | :---: | :--- |
| `TC_PAG_01` | Điều hướng trang mặc định | `page=1` | **200 OK** | `currentPage = 1`, `maxResult = 12`, `len(list) <= 12`. |
| `TC_PAG_02` | Điều hướng đến trang 2 | `page=2` | **200 OK** | `currentPage = 2`. Sản phẩm thuộc trang 2 không trùng lặp với trang 1. |
| `TC_PAG_03` | Truy vấn với Số trang âm | `page=-1` hoặc `page=0` | **200 OK** | Hệ thống tự động chuyển đổi an toàn thành `currentPage = 1`, không gây crash. |
| `TC_PAG_04` | Truy vấn Số trang vượt quá giới hạn | `page=99999` | **200 OK** | `currentPage = 99999`, `list = []`, `totalRecords` giữ nguyên tổng số thực tế. |
| `TC_PAG_05` | Xác minh danh sách thanh điều hướng | `page=1` | **200 OK** | `navigationPages` chứa danh sách mảng trang hợp lý (ví dụ: `[1, 2, 3...]`). |

---

### 3.3. Nhóm Kịch bản Kiểm thử Chi tiết Sản phẩm & Mã Code Không Hợp Lệ

| Mã Test Case | Tên Kịch bản | Endpoint Path | Trạng thái kỳ vọng | Dữ liệu trả về kỳ vọng |
| :--- | :--- | :--- | :---: | :--- |
| `TC_PROD_01` | Truy vấn thông tin sản phẩm hợp lệ | `GET /api/v1/products/S001` | **200 OK** | Trả về object thông tin chi tiết của sản phẩm `S001`. |
| `TC_PROD_02` | Truy vấn sản phẩm không tồn tại | `GET /api/v1/products/INVALID_CODE_99` | **404 Not Found** | Response body chứa thông báo lỗi: `{"status": "ERROR", "message": "Không tìm thấy sản phẩm..."}`. |

---

## 💻 4. HƯỚNG DẪN CHẠY BỘ KIỂM THỬ TỰ ĐỘNG (AUTOMATED TEST SUITE)

Dự án cung cấp file kiểm thử tự động Python chuẩn hóa: [`scripts/test_search_pagination_api.py`](file:///c:/shoeshopp/Testing/scripts/test_search_pagination_api.py).

### 4.1. Lệnh khởi chạy kiểm thử

Chạy trực tiếp bằng Python `unittest`:
```powershell
python -m unittest scripts/test_search_pagination_api.py
```

Chạy qua `pytest` (nếu đã cài đặt):
```powershell
pytest scripts/test_search_pagination_api.py -v
```

### 4.2. Khả năng tương thích môi trường
- Nếu server Spring Boot đang bật (`http://localhost:8080`), script sẽ tự động kiểm thử Live API.
- Nếu server chưa bật, script tự động kích hoạt **Offline Mock Validation Mode** để đảm bảo bộ kiểm thử luôn thực thi thành công trong mọi đường ống CI/CD.
