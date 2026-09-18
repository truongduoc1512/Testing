# BÀI LÀM: KIỂM THỬ CHỨC NĂNG 8 - QUẢN LÝ ĐƠN HÀNG & SẢN PHẨM ADMIN (ADMIN MANAGEMENT)

- **Họ và tên sinh viên:** Nguyễn Hoàng Phương
- **Mã số sinh viên (MSSV):** 080205010954
- **Môn học:** Kiểm Chứng Phần Mềm
- **Chủ đề:** Phân hoạch lớp tương đương, phân tích giá trị biên, bảng quyết định, chuyển đổi trạng thái, thiết kế test case và kiểm thử tự động

---

## Bảng phân tích điều kiện kiểm thử (Test Conditions)

| Conditions | Valid Partition | Tag | Invalid Partitions | Tag | Valid Boundaries | Tag |
|---|---|---|---|---|---|---|
| **Giá bán sản phẩm** (`productPrice` - k) | 10.0 ≤ productPrice ≤ 50000.0 | V1 | • productPrice < 10.0<br>• productPrice > 50000.0 | X1<br>X2 | • 10.0 (min)<br>• 11.0 (min+)<br>• 1500.0 (nominal)<br>• 49999.0 (max-)<br>• 50000.0 (max) | B1<br>B2<br>B3<br>B4<br>B5 |
| **Số lượng tồn kho** (`stockQty`) | 0 ≤ stockQty ≤ 10000 | V2 | • stockQty < 0<br>• stockQty > 10000 | X3<br>X4 | • 0 (min)<br>• 1 (min+)<br>• 100 (nominal)<br>• 9999 (max-)<br>• 10000 (max) | B6<br>B7<br>B8<br>B9<br>B10 |
| **Độ dài tên sản phẩm** (`productNameLength`) | 5 ≤ productNameLength ≤ 100 | V3 | • productNameLength < 5<br>• productNameLength > 100 | X5<br>X6 | • 5 (min)<br>• 6 (min+)<br>• 25 (nominal)<br>• 99 (max-)<br>• 100 (max) | B11<br>B12<br>B13<br>B14<br>B15 |
| **Quyền quản trị viên** (`adminRoleLevel`) | adminRoleLevel = 1 | V4 | • adminRoleLevel = 0<br>• adminRoleLevel > 1 | X7<br>X8 | • 1 (min)<br>• 1 (min+)<br>• 1 (nominal)<br>• 1 (max-)<br>• 1 (max) | B16<br>B17<br>B18<br>B19<br>B20 |

---

## Câu 1. Xác định lớp tương đương

| Biến đầu vào | Lớp hợp lệ | Tag | Lớp không hợp lệ | Tag |
|---|---|---|---|---|
| **Giá bán sản phẩm** (`productPrice`) | 10.0 ≤ productPrice ≤ 50000.0 | V1 | • productPrice < 10.0 (Giá quá thấp)<br>• productPrice > 50000.0 (Vượt giá trần) | X1<br>X2 |
| **Số lượng tồn kho** (`stockQty`) | 0 ≤ stockQty ≤ 10000 | V2 | • stockQty < 0 (Tồn kho âm)<br>• stockQty > 10000 (Vượt sức chứa kho) | X3<br>X4 |
| **Độ dài tên sản phẩm** (`productNameLength`) | 5 ≤ productNameLength ≤ 100 | V3 | • productNameLength < 5 (Tên quá ngắn)<br>• productNameLength > 100 (Tên quá dài) | X5<br>X6 |
| **Quyền quản trị viên** (`adminRoleLevel`) | adminRoleLevel = 1 (ROLE_ADMIN) | V4 | • adminRoleLevel = 0 (ROLE_USER/EMPLOYEE)<br>• adminRoleLevel ≠ 1 (Không có quyền) | X7<br>X8 |

---

## Câu 2. Phân tích giá trị biên

### 1. Bảng 5 giá trị biên cho từng biến đầu vào

| Biến đầu vào | min | min+ | nominal | max- | max | Tag biên |
|---|---:|---:|---:|---:|---:|---|
| **Giá bán sản phẩm** (`productPrice`) | 10.0 | 11.0 | 1500.0 | 49999.0 | 50000.0 | B1, B2, B3, B4, B5 |
| **Số lượng tồn kho** (`stockQty`) | 0 | 1 | 100 | 9999 | 10000 | B6, B7, B8, B9, B10 |
| **Độ dài tên sản phẩm** (`productNameLength`) | 5 | 6 | 25 | 99 | 100 | B11, B12, B13, B14, B15 |
| **Quyền quản trị viên** (`adminRoleLevel`) | 1 | 1 | 1 | 1 | 1 | B16, B17, B18, B19, B20 |

### 2. Bảng 17 test case Standard BVA (Single Fault Assumption: $4n + 1 = 17$)

Theo kỹ thuật Standard Boundary Value Analysis, với $n = 4$ biến đầu vào, số test case là:
$$4n + 1 = 4 \times 4 + 1 = \mathbf{17\text{ test case}}$$

Giữ $n - 1$ biến tại giá trị danh định (`nominal`), lần lượt thay đổi 1 biến qua 4 giá trị biên (`min`, `min+`, `max-`, `max`):

| STT | Mã TC | Biến kiểm thử biên | Điểm biên kiểm tra | Giá bán (k) | Tồn kho | Tên SP (len) | Quyền Admin | Kết quả mong đợi | Tag bao phủ |
|:---:|:---:|:---|:---|:---:|:---:|:---:|:---:|:---|:---|
| 1 | BVA01 | Baseline (Tất cả) | Nominal | 1500.0 | 100 | 25 | 1 | Hợp lệ (True) | B3, B8, B13, B18 |
| 2 | BVA02 | Giá bán sản phẩm | min (10.0) | **10.0** | 100 | 25 | 1 | Hợp lệ (True) | B1 |
| 3 | BVA03 | Giá bán sản phẩm | max (50000.0) | **50000.0** | 100 | 25 | 1 | Hợp lệ (True) | B5 |
| 4 | BVA04 | Giá bán sản phẩm | min-1 (9.0) | **9.0** | 100 | 25 | 1 | Không hợp lệ (False) | X1 |
| 5 | BVA05 | Giá bán sản phẩm | max+1 (50001.0) | **50001.0** | 100 | 25 | 1 | Không hợp lệ (False) | X2 |
| 6 | BVA06 | Tồn kho | min (0) | 1500.0 | **0** | 25 | 1 | Hợp lệ (True) | B6 |
| 7 | BVA07 | Tồn kho | max (10000) | 1500.0 | **10000** | 25 | 1 | Hợp lệ (True) | B10 |
| 8 | BVA08 | Tồn kho | min-1 (-1) | 1500.0 | **-1** | 25 | 1 | Không hợp lệ (False) | X3 |
| 9 | BVA09 | Tồn kho | max+1 (10001) | 1500.0 | **10001** | 25 | 1 | Không hợp lệ (False) | X4 |
| 10 | BVA10 | Độ dài tên SP | min (5) | 1500.0 | 100 | **5** | 1 | Hợp lệ (True) | B11 |
| 11 | BVA11 | Độ dài tên SP | max (100) | 1500.0 | 100 | **100** | 1 | Hợp lệ (True) | B15 |
| 12 | BVA12 | Độ dài tên SP | min-1 (4) | 1500.0 | 100 | **4** | 1 | Không hợp lệ (False) | X5 |
| 13 | BVA13 | Độ dài tên SP | max+1 (101) | 1500.0 | 100 | **101** | 1 | Không hợp lệ (False) | X6 |
| 14 | BVA14 | Quyền quản trị | min (1) | 1500.0 | 100 | 25 | **1** | Hợp lệ (True) | B16 |
| 15 | BVA15 | Quyền quản trị | max (1) | 1500.0 | 100 | 25 | **1** | Hợp lệ (True) | B20 |
| 16 | BVA16 | Quyền quản trị | min-1 (0) | 1500.0 | 100 | 25 | **0** | Không hợp lệ (False) | X7 |
| 17 | BVA17 | Quyền quản trị | max+1 (2) | 1500.0 | 100 | 25 | **2** | Không hợp lệ (False) | X8 |

---

## Câu 3. Thiết kế test case

Dựa trên kết quả Câu 1 và Câu 2, bộ **17 test case** được thiết kế theo nguyên lý **Single Fault Assumption ($4n + 1 = 17$)** để vừa kế thừa chuẩn BVA cho 4 biến đầu vào, vừa thỏa mãn đầy đủ các yêu cầu của đề bài:
- Có test case baseline hợp lệ danh định (nominal).
- Có test case hợp lệ tại biên (`min`, `max`).
- Có test case không hợp lệ ngoài biên (`min - 1`, `max + 1`) kèm lý do chi tiết.
- Bao phủ toàn diện 100% các tag lớp tương đương ($V1 - V4$, $X1 - X8$) và các tag biên trọng yếu.

### 1. Bảng test case tổng hợp (Test Case, Input, Expected Outcome, New Tags Covered)

| Test Case | Input | Expected Outcome | New Tags Covered |
|---|---|---|---|
| TC01 | productPrice: 1500.0, stockQty: 100, productNameLength: 25, adminRoleLevel: 1 | Hợp lệ (True) | V1, V2, V3, V4, B3, B8, B13, B18 |
| TC02 | productPrice: 10.0, stockQty: 100, productNameLength: 25, adminRoleLevel: 1 | Hợp lệ (True) | B1 |
| TC03 | productPrice: 50000.0, stockQty: 100, productNameLength: 25, adminRoleLevel: 1 | Hợp lệ (True) | B5 |
| TC04 | productPrice: 9.0, stockQty: 100, productNameLength: 25, adminRoleLevel: 1 | Không hợp lệ (False): Giá bán nhỏ hơn 10.0k | X1 |
| TC05 | productPrice: 50001.0, stockQty: 100, productNameLength: 25, adminRoleLevel: 1 | Không hợp lệ (False): Giá bán lớn hơn 50000.0k | X2 |
| TC06 | productPrice: 1500.0, stockQty: 0, productNameLength: 25, adminRoleLevel: 1 | Hợp lệ (True) | B6 |
| TC07 | productPrice: 1500.0, stockQty: 10000, productNameLength: 25, adminRoleLevel: 1 | Hợp lệ (True) | B10 |
| TC08 | productPrice: 1500.0, stockQty: -1, productNameLength: 25, adminRoleLevel: 1 | Không hợp lệ (False): Tồn kho nhỏ hơn 0 | X3 |
| TC09 | productPrice: 1500.0, stockQty: 10001, productNameLength: 25, adminRoleLevel: 1 | Không hợp lệ (False): Tồn kho lớn hơn 10000 | X4 |
| TC10 | productPrice: 1500.0, stockQty: 100, productNameLength: 5, adminRoleLevel: 1 | Hợp lệ (True) | B11 |
| TC11 | productPrice: 1500.0, stockQty: 100, productNameLength: 100, adminRoleLevel: 1 | Hợp lệ (True) | B15 |
| TC12 | productPrice: 1500.0, stockQty: 100, productNameLength: 4, adminRoleLevel: 1 | Không hợp lệ (False): Tên SP nhỏ hơn 5 ký tự | X5 |
| TC13 | productPrice: 1500.0, stockQty: 100, productNameLength: 101, adminRoleLevel: 1 | Không hợp lệ (False): Tên SP lớn hơn 100 ký tự | X6 |
| TC14 | productPrice: 1500.0, stockQty: 100, productNameLength: 25, adminRoleLevel: 1 | Hợp lệ (True) | B16 |
| TC15 | productPrice: 1500.0, stockQty: 100, productNameLength: 25, adminRoleLevel: 1 | Hợp lệ (True) | B20 |
| TC16 | productPrice: 1500.0, stockQty: 100, productNameLength: 25, adminRoleLevel: 0 | Không hợp lệ (False): Không có quyền Admin (HTTP 403) | X7 |
| TC17 | productPrice: 1500.0, stockQty: 100, productNameLength: 25, adminRoleLevel: 2 | Không hợp lệ (False): Quyền hạn không hợp lệ | X8 |

### 2. Bảng test case chi tiết theo đề bài (8 cột)

| STT | Tên test case | Giá bán (k) | Tồn kho | Độ dài tên SP | Quyền Admin | Kết quả mong đợi | Tag được bao phủ |
|:---:|:---|:---:|:---:|:---:|:---:|:---|:---|
| 1 | Baseline danh định (nominal) | 1500.0 | 100 | 25 | 1 | Hợp lệ (True) | V1, V2, V3, V4, B3, B8, B13, B18 |
| 2 | Biên dưới hợp lệ productPrice = min (10.0) | **10.0** | 100 | 25 | 1 | Hợp lệ (True) | B1 |
| 3 | Biên trên hợp lệ productPrice = max (50000.0) | **50000.0** | 100 | 25 | 1 | Hợp lệ (True) | B5 |
| 4 | Ngoài biên dưới productPrice < min (9.0) | **9.0** | 100 | 25 | 1 | Không hợp lệ (False): Giá bán nhỏ hơn 10.0k | X1 |
| 5 | Ngoài biên trên productPrice > max (50001.0) | **50001.0** | 100 | 25 | 1 | Không hợp lệ (False): Giá bán lớn hơn 50000.0k | X2 |
| 6 | Biên dưới hợp lệ stockQty = min (0) | 1500.0 | **0** | 25 | 1 | Hợp lệ (True) | B6 |
| 7 | Biên trên hợp lệ stockQty = max (10000) | 1500.0 | **10000** | 25 | 1 | Hợp lệ (True) | B10 |
| 8 | Ngoài biên dưới stockQty < min (-1) | 1500.0 | **-1** | 25 | 1 | Không hợp lệ (False): Tồn kho nhỏ hơn 0 | X3 |
| 9 | Ngoài biên trên stockQty > max (10001) | 1500.0 | **10001** | 25 | 1 | Không hợp lệ (False): Tồn kho lớn hơn 10000 | X4 |
| 10 | Biên dưới hợp lệ productNameLength = min (5) | 1500.0 | 100 | **5** | 1 | Hợp lệ (True) | B11 |
| 11 | Biên trên hợp lệ productNameLength = max (100) | 1500.0 | 100 | **100** | 1 | Hợp lệ (True) | B15 |
| 12 | Ngoài biên dưới productNameLength < min (4) | 1500.0 | 100 | **4** | 1 | Không hợp lệ (False): Tên SP nhỏ hơn 5 ký tự | X5 |
| 13 | Ngoài biên trên productNameLength > max (101) | 1500.0 | 100 | **101** | 1 | Không hợp lệ (False): Tên SP lớn hơn 100 ký tự | X6 |
| 14 | Biên dưới hợp lệ adminRoleLevel = min (1) | 1500.0 | 100 | 25 | **1** | Hợp lệ (True) | B16 |
| 15 | Biên trên hợp lệ adminRoleLevel = max (1) | 1500.0 | 100 | 25 | **1** | Hợp lệ (True) | B20 |
| 16 | Ngoài biên dưới adminRoleLevel < min (0) | 1500.0 | 100 | 25 | **0** | Không hợp lệ (False): Không có quyền Admin (HTTP 403) | X7 |
| 17 | Ngoài biên trên adminRoleLevel > max (2) | 1500.0 | 100 | 25 | **2** | Không hợp lệ (False): Quyền hạn không hợp lệ | X8 |

---

### 3. Bổ sung: Bảng Quyết định phân quyền Admin (Decision Table - 4 Rules)

| Condition / Action | Rule 1 (Guest) | Rule 2 (User) | Rule 3 (Admin) | Rule 4 (SuperAdmin) |
| :--- | :---: | :---: | :---: | :---: |
| **C1: Quyền truy cập (`ROLE`)** | ANONYMOUS | ROLE_USER | ROLE_ADMIN | ROLE_SUPER_ADMIN |
| *A1: Chuyển hướng về trang `/login` (302)* | **X** | - | - | - |
| *A2: Chặn truy cập trái phép (HTTP 403 Forbidden)* | - | **X** | - | - |
| *A3: Cho phép CRUD Sản phẩm & Đơn hàng (200)* | - | - | **X** | **X** |
| *A4: Toàn quyền cấu hình hệ thống & Nhân sự* | - | - | - | **X** |

---

## Câu 4. Triển khai kiểm thử tự động

```python
def ValidateAdminManagement(productPrice: float, stockQty: int, productNameLength: int, adminRoleLevel: int) -> bool:
    """
    Kiểm tra tính hợp lệ của thao tác quản lý Admin:
    - 10.0 <= productPrice <= 50000.0 (Giá bán từ 10k đến 50tr)
    - 0 <= stockQty <= 10000 (Tồn kho từ 0 đến 10.000 đôi)
    - 5 <= productNameLength <= 100 (Tên sản phẩm từ 5 đến 100 ký tự)
    - adminRoleLevel == 1 (Yêu cầu tài khoản có quyền ROLE_ADMIN)
    Trả về True nếu tất cả điều kiện thỏa mãn, ngược lại False.
    """
    if not (isinstance(productPrice, (int, float)) and not isinstance(productPrice, bool) and 10.0 <= productPrice <= 50000.0):
        return False
    if not (isinstance(stockQty, int) and not isinstance(stockQty, bool) and 0 <= stockQty <= 10000):
        return False
    if not (isinstance(productNameLength, int) and not isinstance(productNameLength, bool) and 5 <= productNameLength <= 100):
        return False
    if not (isinstance(adminRoleLevel, int) and not isinstance(adminRoleLevel, bool) and adminRoleLevel == 1):
        return False
    return True
```

```pytest
# thiết kế các test cases từ câu 3.
# Run test case 
import pytest

test_cases_m8 = [
    ("TC01", 1500.0, 100, 25, 1, True, "V1, V2, V3, V4, B3, B8, B13, B18"),
    ("TC02", 10.0, 100, 25, 1, True, "B1"),
    ("TC03", 50000.0, 100, 25, 1, True, "B5"),
    ("TC04", 9.0, 100, 25, 1, False, "X1"),
    ("TC05", 50001.0, 100, 25, 1, False, "X2"),
    ("TC06", 1500.0, 0, 25, 1, True, "B6"),
    ("TC07", 1500.0, 10000, 25, 1, True, "B10"),
    ("TC08", 1500.0, -1, 25, 1, False, "X3"),
    ("TC09", 1500.0, 10001, 25, 1, False, "X4"),
    ("TC10", 1500.0, 100, 5, 1, True, "B11"),
    ("TC11", 1500.0, 100, 100, 1, True, "B15"),
    ("TC12", 1500.0, 100, 4, 1, False, "X5"),
    ("TC13", 1500.0, 100, 101, 1, False, "X6"),
    ("TC14", 1500.0, 100, 25, 1, True, "B16"),
    ("TC15", 1500.0, 100, 25, 1, True, "B20"),
    ("TC16", 1500.0, 100, 25, 0, False, "X7"),
    ("TC17", 1500.0, 100, 25, 2, False, "X8"),
]

@pytest.mark.parametrize("tc_id,price,stock,pLen,roleLvl,expected,tag", test_cases_m8)
def test_admin_validation(tc_id, price, stock, pLen, roleLvl, expected, tag):
    """Kiểm thử tự động 17 test case quản trị Admin theo nguyên lý 4n + 1."""
    assert ValidateAdminManagement(price, stock, pLen, roleLvl) == expected

if __name__ == "__main__":
    pytest.main(["-v", __file__])
```

```kết quả test
============================= test session starts =============================
platform win32 -- Python 3.14.0, pytest-8.4.2, pluggy-1.6.0
rootdir: D:\LapTrinhAI\Testing
collected 17 items

test_admin.py::test_admin_validation[TC01-1500.0-100-25-1-True-V1, V2, V3, V4, B3, B8, B13, B18] PASSED [  5%]
test_admin.py::test_admin_validation[TC02-10.0-100-25-1-True-B1] PASSED [ 11%]
test_admin.py::test_admin_validation[TC03-50000.0-100-25-1-True-B5] PASSED [ 17%]
test_admin.py::test_admin_validation[TC04-9.0-100-25-1-False-X1] PASSED [ 23%]
test_admin.py::test_admin_validation[TC05-50001.0-100-25-1-False-X2] PASSED [ 29%]
test_admin.py::test_admin_validation[TC06-1500.0-0-25-1-True-B6] PASSED [ 35%]
test_admin.py::test_admin_validation[TC07-1500.0-10000-25-1-True-B10] PASSED [ 41%]
test_admin.py::test_admin_validation[TC08-1500.0--1-25-1-False-X3] PASSED [ 47%]
test_admin.py::test_admin_validation[TC09-1500.0-10001-25-1-False-X4] PASSED [ 52%]
test_admin.py::test_admin_validation[TC10-1500.0-100-5-1-True-B11] PASSED [ 58%]
test_admin.py::test_admin_validation[TC11-1500.0-100-100-1-True-B15] PASSED [ 64%]
test_admin.py::test_admin_validation[TC12-1500.0-100-4-1-False-X5] PASSED [ 70%]
test_admin.py::test_admin_validation[TC13-1500.0-100-101-1-False-X6] PASSED [ 76%]
test_admin.py::test_admin_validation[TC14-1500.0-100-25-1-True-B16] PASSED [ 82%]
test_admin.py::test_admin_validation[TC15-1500.0-100-25-1-True-B20] PASSED [ 88%]
test_admin.py::test_admin_validation[TC16-1500.0-100-25-0-False-X7] PASSED [ 94%]
test_admin.py::test_admin_validation[TC17-1500.0-100-25-2-False-X8] PASSED [100%]

============================= 17 passed in 0.14s ==============================
```
