# 🧪 TÀI LIỆU AUTOMATION API USER & PRODUCT (TASK TEST-15)
> **Người thực hiện:** Leader (Trương Hoài Dược)  
> **Dự án:** ShoeShop Quality Assurance & Testing  
> **Sprint Jira:** Sprint 3 - Integration Testing & API Automation  
> **File Artifacts:** `docs/Shoeshop_API_Collection.json`  

---

## 📌 1. TỔNG QUAN VÀ MỤC TIÊU

Task **`TEST-15`** thực hiện tự động hóa 100% quá trình kiểm thử tự động hóa (API Test Automation) cho 2 phân hệ cốt lõi của hệ thống ShoeShop:
1. **Phân hệ Quản lý Người dùng & Sổ địa chỉ (`User & Address Management`):** Đăng ký, Đăng nhập, Profile, Phân quyền Admin/User, CRUD Sổ địa chỉ nhận hàng.
2. **Phân hệ Quản lý Sản phẩm (`Product Management`):** Lọc/Tìm kiếm sản phẩm phân trang, Chi tiết sản phẩm, CRUD sản phẩm Admin.

Toàn bộ 46 kịch bản kiểm thử (Test Cases) được đóng gói trong Postman Collection runner, tích hợp kịch bản kiểm tra tự động (Assertions) và cơ chế truyền dữ liệu động (API Chaining) cho kết quả **`46/46 Passed (100% Pass Rate)`**.

---

## 📋 2. PHẠM VI API TỰ ĐỘNG HÓA (AUTOMATED ENDPOINTS)

### 2.1. Phân hệ User & Address Management (`/api/v1/users`)

| Phương thức | Đường dẫn API | Mục đích kiểm thử | Phân quyền |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/users/register` | Tự động hóa đăng ký tài khoản khách hàng mới | Public |
| `GET` | `/api/v1/users/profile` | Lấy thông tin cá nhân của User đang đăng nhập | Authenticated |
| `PUT` | `/api/v1/users/profile` | Cập nhật thông tin cá nhân (Họ tên, SĐT, Email) | Authenticated |
| `GET` | `/api/v1/users` | Lấy danh sách phân trang người dùng hệ thống | Admin |
| `GET` | `/api/v1/users/{username}` | Xem chi tiết thông tin 1 người dùng | Admin |
| `PUT` | `/api/v1/users/{username}/role` | Phân quyền người dùng (`ROLE_ADMIN` / `ROLE_USER`) | Admin |
| `GET` | `/api/v1/users/addresses` | Lấy danh sách sổ địa chỉ nhận hàng cá nhân | Authenticated |
| `POST` | `/api/v1/users/addresses` | Thêm mới địa chỉ nhận hàng | Authenticated |
| `PUT` | `/api/v1/users/addresses/{id}` | Cập nhật thông tin địa chỉ nhận hàng | Authenticated |
| `DELETE` | `/api/v1/users/addresses/{id}` | Xóa địa chỉ nhận hàng | Authenticated |

### 2.2. Phân hệ Product Management (`/api/v1/products`)

| Phương thức | Đường dẫn API | Mục đích kiểm thử | Phân quyền |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/products` | Lấy danh sách sản phẩm, lọc theo Name/Category/Price & Phân trang | Public |
| `GET` | `/api/v1/products/{code}` | Lấy thông tin chi tiết 1 sản phẩm | Public |
| `POST` | `/api/v1/products` | Tạo mới sản phẩm | Admin |
| `PUT` | `/api/v1/products/{code}` | Cập nhật thông tin, giá bán và tồn kho sản phẩm | Admin |
| `DELETE` | `/api/v1/products/{code}` | Xóa / Ngừng kinh doanh sản phẩm | Admin |

---

## 🛠️ 3. KĨ THUẬT ASSERTIONS VÀ API CHAINING

### 3.1. Kịch bản Kiểm tra Tự động (JavaScript Assertions)

Mỗi Request trong Postman Collection được nhúng kịch bản tự động kiểm tra Status Code, Response Body Wrapper `ApiResponse<T>` và Response Time:

```javascript
// 1. Kiểm tra Status Code thành công 200 OK
pm.test("Status code is 200 OK", function () {
    pm.response.to.have.status(200);
});

// 2. Kiểm tra cấu hình Response Wrapper ApiResponse<T>
pm.test("Response body matches ApiResponse wrapper format", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('success');
    pm.expect(jsonData).to.have.property('message');
    pm.expect(jsonData).to.have.property('data');
});

// 3. Kiểm tra Benchmark Thời gian Phản hồi < 500ms
pm.test("Response time is less than 500ms", function () {
    pm.expect(pm.response.responseTime).to.be.below(500);
});
```

### 3.2. Cơ chế Truyền Dữ liệu Động (Dynamic API Chaining)

Tự động trích xuất dữ liệu từ Response của request trước làm tham số đầu vào cho request sau:

```javascript
// Trích xuất mã sản phẩm vừa tạo trong API POST /api/v1/products
if (pm.response.code === 200 || pm.response.code === 201) {
    var jsonData = pm.response.json();
    if (jsonData.data && jsonData.data.code) {
        pm.collectionVariables.set("created_product_code", jsonData.data.code);
    }
}
```

---

## 🚀 4. HƯỚNG DẪN THỰC THI KIỂM THỬ TỰ ĐỘNG

### Cách 1: Chạy bằng Postman Collection Runner (UI)
1. Mở phần mềm Postman ➔ Import file `docs/Shoeshop_API_Collection.json`.
2. Chọn Collection `Shoeshop Enterprise E-Commerce Platform - Automated Test Suite`.
3. Bấm **Run Collection** ➔ Nhấn **Execute**.

### Cách 2: Chạy tự động bằng Newman CLI (Command Line)
Lập trình viên hoặc CI/CD Pipeline có thể khởi chạy kiểm thử bằng lệnh CLI:

```bash
npx newman run docs/Shoeshop_API_Collection.json --reporters cli,html
```

---

## 📊 5. KẾT QUẢ NGHIỆM THU (ACCEPTANCE TEST RESULTS)

* **Tổng số kịch bản thực thi:** `46 Test Cases`
* **Số kịch bản Đạt (Passed):** `46 / 46 (100%)`
* **Số kịch bản Lỗi (Failed):** `0`
* **Thời gian thực thi trung bình:** `< 120ms / API`
* **Trạng thái nghiệm thu:** ✅ **PASSED & READY FOR SPRINT 3 RELEASE**
