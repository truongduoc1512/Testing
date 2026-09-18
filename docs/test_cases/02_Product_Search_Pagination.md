# BÀI LÀM: KIỂM THỬ CHỨC NĂNG 2 - TÌM KIẾM & PHÂN TRANG SẢN PHẨM (SEARCH & PAGINATION)

- **Họ và tên sinh viên:** Nguyễn Hoàng Phương
- **Mã số sinh viên (MSSV):** 080205010954
- **Môn học:** Kiểm Chứng Phần Mềm
- **Chủ đề:** Phân hoạch lớp tương đương, phân tích giá trị biên, bảng quyết định, chuyển đổi trạng thái, thiết kế test case và kiểm thử tự động

---

## Bảng phân tích điều kiện kiểm thử (Test Conditions)

| Conditions | Valid Partition | Tag | Invalid Partitions | Tag | Valid Boundaries | Tag |
|---|---|---|---|---|---|---|
| **Độ dài từ khóa** (`keywordLength`) | 0 ≤ keywordLength ≤ 50 | V1 | • keywordLength < 0<br>• keywordLength > 50 | X1<br>X2 | • 0 (min)<br>• 1 (min+)<br>• 10 (nominal)<br>• 49 (max-)<br>• 50 (max) | B1<br>B2<br>B3<br>B4<br>B5 |
| **Giá lọc tối thiểu** (`minPrice` - k) | 0.0 ≤ minPrice ≤ 50000.0 | V2 | • minPrice < 0.0<br>• minPrice > 50000.0 | X3<br>X4 | • 0.0 (min)<br>• 1.0 (min+)<br>• 1000.0 (nominal)<br>• 49999.0 (max-)<br>• 50000.0 (max) | B6<br>B7<br>B8<br>B9<br>B10 |
| **Số trang yêu cầu** (`page`) | 1 ≤ page ≤ 50 | V3 | • page < 1<br>• page > 50 | X5<br>X6 | • 1 (min)<br>• 2 (min+)<br>• 5 (nominal)<br>• 49 (max-)<br>• 50 (max) | B11<br>B12<br>B13<br>B14<br>B15 |
| **Kích thước trang** (`pageSize`) | 1 ≤ pageSize ≤ 12 | V4 | • pageSize < 1<br>• pageSize > 12 | X7<br>X8 | • 1 (min)<br>• 2 (min+)<br>• 6 (nominal)<br>• 11 (max-)<br>• 12 (max) | B16<br>B17<br>B18<br>B19<br>B20 |

---

## Câu 1. Xác định lớp tương đương

| Biến đầu vào | Lớp hợp lệ | Tag | Lớp không hợp lệ | Tag |
|---|---|---|---|---|
| **Độ dài từ khóa** (`keywordLength`) | 0 ≤ keywordLength ≤ 50 | V1 | • keywordLength < 0 (Số âm)<br>• keywordLength > 50 (Chuỗi quá dài) | X1<br>X2 |
| **Giá lọc tối thiểu** (`minPrice`) | 0.0 ≤ minPrice ≤ 50000.0 | V2 | • minPrice < 0.0 (Giá âm)<br>• minPrice > 50000.0 (Vượt trần giá) | X3<br>X4 |
| **Số trang yêu cầu** (`page`) | 1 ≤ page ≤ 50 | V3 | • page < 1 (Số trang nhỏ hơn 1)<br>• page > 50 (Vượt quá số trang CSDL) | X5<br>X6 |
| **Kích thước trang** (`pageSize`) | 1 ≤ pageSize ≤ 12 | V4 | • pageSize < 1 (Kích thước < 1)<br>• pageSize > 12 (Vượt quá giới hạn hiển thị) | X7<br>X8 |

---

## Câu 2. Phân tích giá trị biên

### 1. Bảng 5 giá trị biên cho từng biến đầu vào

| Biến đầu vào | min | min+ | nominal | max- | max | Tag biên |
|---|---:|---:|---:|---:|---:|---|
| **Độ dài từ khóa** (`keywordLength`) | 0 | 1 | 10 | 49 | 50 | B1, B2, B3, B4, B5 |
| **Giá lọc tối thiểu** (`minPrice`) | 0.0 | 1.0 | 1000.0 | 49999.0 | 50000.0 | B6, B7, B8, B9, B10 |
| **Số trang yêu cầu** (`page`) | 1 | 2 | 5 | 49 | 50 | B11, B12, B13, B14, B15 |
| **Kích thước trang** (`pageSize`) | 1 | 2 | 6 | 11 | 12 | B16, B17, B18, B19, B20 |

### 2. Bảng 17 test case Standard BVA (Single Fault Assumption: $4n + 1 = 17$)

Theo kỹ thuật Standard Boundary Value Analysis, với $n = 4$ biến đầu vào, số test case là:
$$4n + 1 = 4 \times 4 + 1 = \mathbf{17\text{ test case}}$$

Giữ $n - 1$ biến tại giá trị danh định (`nominal`), lần lượt thay đổi 1 biến qua 4 giá trị biên (`min`, `min+`, `max-`, `max`):

| STT | Mã TC | Biến kiểm thử biên | Điểm biên kiểm tra | Từ khóa (len) | Giá min (k) | Trang | Kích thước | Kết quả mong đợi | Tag bao phủ |
|:---:|:---:|:---|:---|:---:|:---:|:---:|:---:|:---|:---|
| 1 | BVA01 | Baseline (Tất cả) | Nominal | 10 | 1000.0 | 5 | 6 | Hợp lệ (True) | B3, B8, B13, B18 |
| 2 | BVA02 | Độ dài từ khóa | min (0) | **0** | 1000.0 | 5 | 6 | Hợp lệ (True) | B1 |
| 3 | BVA03 | Độ dài từ khóa | max (50) | **50** | 1000.0 | 5 | 6 | Hợp lệ (True) | B5 |
| 4 | BVA04 | Độ dài từ khóa | min-1 (-1) | **-1** | 1000.0 | 5 | 6 | Không hợp lệ (False) | X1 |
| 5 | BVA05 | Độ dài từ khóa | max+1 (51) | **51** | 1000.0 | 5 | 6 | Không hợp lệ (False) | X2 |
| 6 | BVA06 | Giá lọc tối thiểu | min (0.0) | 10 | **0.0** | 5 | 6 | Hợp lệ (True) | B6 |
| 7 | BVA07 | Giá lọc tối thiểu | max (50000.0) | 10 | **50000.0** | 5 | 6 | Hợp lệ (True) | B10 |
| 8 | BVA08 | Giá lọc tối thiểu | min-0.1 (-0.1) | 10 | **-0.1** | 5 | 6 | Không hợp lệ (False) | X3 |
| 9 | BVA09 | Giá lọc tối thiểu | max+0.1 (50000.1) | 10 | **50000.1** | 5 | 6 | Không hợp lệ (False) | X4 |
| 10 | BVA10 | Số trang | min (1) | 10 | 1000.0 | **1** | 6 | Hợp lệ (True) | B11 |
| 11 | BVA11 | Số trang | max (50) | 10 | 1000.0 | **50** | 6 | Hợp lệ (True) | B15 |
| 12 | BVA12 | Số trang | min-1 (0) | 10 | 1000.0 | **0** | 6 | Không hợp lệ (False) | X5 |
| 13 | BVA13 | Số trang | max+1 (51) | 10 | 1000.0 | **51** | 6 | Không hợp lệ (False) | X6 |
| 14 | BVA14 | Kích thước trang | min (1) | 10 | 1000.0 | 5 | **1** | Hợp lệ (True) | B16 |
| 15 | BVA15 | Kích thước trang | max (12) | 10 | 1000.0 | 5 | **12** | Hợp lệ (True) | B20 |
| 16 | BVA16 | Kích thước trang | min-1 (0) | 10 | 1000.0 | 5 | **0** | Không hợp lệ (False) | X7 |
| 17 | BVA17 | Kích thước trang | max+1 (13) | 10 | 1000.0 | 5 | **13** | Không hợp lệ (False) | X8 |

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
| TC01 | keywordLength: 10, minPrice: 1000.0, page: 5, pageSize: 6 | Hợp lệ (True) | V1, V2, V3, V4, B3, B8, B13, B18 |
| TC02 | keywordLength: 0, minPrice: 1000.0, page: 5, pageSize: 6 | Hợp lệ (True) | B1 |
| TC03 | keywordLength: 50, minPrice: 1000.0, page: 5, pageSize: 6 | Hợp lệ (True) | B5 |
| TC04 | keywordLength: -1, minPrice: 1000.0, page: 5, pageSize: 6 | Không hợp lệ (False): Độ dài từ khóa < 0 | X1 |
| TC05 | keywordLength: 51, minPrice: 1000.0, page: 5, pageSize: 6 | Không hợp lệ (False): Độ dài từ khóa > 50 | X2 |
| TC06 | keywordLength: 10, minPrice: 0.0, page: 5, pageSize: 6 | Hợp lệ (True) | B6 |
| TC07 | keywordLength: 10, minPrice: 50000.0, page: 5, pageSize: 6 | Hợp lệ (True) | B10 |
| TC08 | keywordLength: 10, minPrice: -0.1, page: 5, pageSize: 6 | Không hợp lệ (False): Giá lọc nhỏ hơn 0.0 | X3 |
| TC09 | keywordLength: 10, minPrice: 50000.1, page: 5, pageSize: 6 | Không hợp lệ (False): Giá lọc vượt trần 50000.0 | X4 |
| TC10 | keywordLength: 10, minPrice: 1000.0, page: 1, pageSize: 6 | Hợp lệ (True) | B11 |
| TC11 | keywordLength: 10, minPrice: 1000.0, page: 50, pageSize: 6 | Hợp lệ (True) | B15 |
| TC12 | keywordLength: 10, minPrice: 1000.0, page: 0, pageSize: 6 | Không hợp lệ (False): Số trang nhỏ hơn 1 | X5 |
| TC13 | keywordLength: 10, minPrice: 1000.0, page: 51, pageSize: 6 | Không hợp lệ (False): Số trang vượt quá 50 | X6 |
| TC14 | keywordLength: 10, minPrice: 1000.0, page: 5, pageSize: 1 | Hợp lệ (True) | B16 |
| TC15 | keywordLength: 10, minPrice: 1000.0, page: 5, pageSize: 12 | Hợp lệ (True) | B20 |
| TC16 | keywordLength: 10, minPrice: 1000.0, page: 5, pageSize: 0 | Không hợp lệ (False): Kích thước trang nhỏ hơn 1 | X7 |
| TC17 | keywordLength: 10, minPrice: 1000.0, page: 5, pageSize: 13 | Không hợp lệ (False): Kích thước trang lớn hơn 12 | X8 |

### 2. Bảng test case chi tiết theo đề bài (8 cột)

| STT | Tên test case | Độ dài từ khóa | Giá lọc tối thiểu (k) | Số trang yêu cầu | Kích thước trang | Kết quả mong đợi | Tag được bao phủ |
|:---:|:---|:---:|:---:|:---:|:---:|:---|:---|
| 1 | Baseline danh định (nominal) | 10 | 1000.0 | 5 | 6 | Hợp lệ (True) | V1, V2, V3, V4, B3, B8, B13, B18 |
| 2 | Biên dưới hợp lệ keywordLength = min (0) | **0** | 1000.0 | 5 | 6 | Hợp lệ (True) | B1 |
| 3 | Biên trên hợp lệ keywordLength = max (50) | **50** | 1000.0 | 5 | 6 | Hợp lệ (True) | B5 |
| 4 | Ngoài biên dưới keywordLength < min (-1) | **-1** | 1000.0 | 5 | 6 | Không hợp lệ (False): Độ dài từ khóa < 0 | X1 |
| 5 | Ngoài biên trên keywordLength > max (51) | **51** | 1000.0 | 5 | 6 | Không hợp lệ (False): Độ dài từ khóa > 50 | X2 |
| 6 | Biên dưới hợp lệ minPrice = min (0.0) | 10 | **0.0** | 5 | 6 | Hợp lệ (True) | B6 |
| 7 | Biên trên hợp lệ minPrice = max (50000.0) | 10 | **50000.0** | 5 | 6 | Hợp lệ (True) | B10 |
| 8 | Ngoài biên dưới minPrice < min (-0.1) | 10 | **-0.1** | 5 | 6 | Không hợp lệ (False): Giá lọc nhỏ hơn 0.0 | X3 |
| 9 | Ngoài biên trên minPrice > max (50000.1) | 10 | **50000.1** | 5 | 6 | Không hợp lệ (False): Giá lọc vượt trần 50000.0 | X4 |
| 10 | Biên dưới hợp lệ page = min (1) | 10 | 1000.0 | **1** | 6 | Hợp lệ (True) | B11 |
| 11 | Biên trên hợp lệ page = max (50) | 10 | 1000.0 | **50** | 6 | Hợp lệ (True) | B15 |
| 12 | Ngoài biên dưới page < min (0) | 10 | 1000.0 | **0** | 6 | Không hợp lệ (False): Số trang nhỏ hơn 1 | X5 |
| 13 | Ngoài biên trên page > max (51) | 10 | 1000.0 | **51** | 6 | Không hợp lệ (False): Số trang vượt quá 50 | X6 |
| 14 | Biên dưới hợp lệ pageSize = min (1) | 10 | 1000.0 | 5 | **1** | Hợp lệ (True) | B16 |
| 15 | Biên trên hợp lệ pageSize = max (12) | 10 | 1000.0 | 5 | **12** | Hợp lệ (True) | B20 |
| 16 | Ngoài biên dưới pageSize < min (0) | 10 | 1000.0 | 5 | **0** | Không hợp lệ (False): Kích thước trang nhỏ hơn 1 | X7 |
| 17 | Ngoài biên trên pageSize > max (13) | 10 | 1000.0 | 5 | **13** | Không hợp lệ (False): Kích thước trang lớn hơn 12 | X8 |

---

### 3. Bổ sung: Bảng Quyết định tìm kiếm & phân trang (Collapsed Decision Table - 5 Rules)

| Condition / Action | Rule 1 (R1) | Rule 2 (R2) | Rule 3 (R3) | Rule 4 (R4) | Rule 5 (R5) |
| :--- | :---: | :---: | :---: | :---: | :---: |
| **C1: Từ khóa tồn tại trong DB?** | **N** | Y | Y | Y | Y |
| **C2: Sản phẩm đang mở bán (Active)?** | - | **N** | Y | Y | Y |
| **C3: Thỏa mãn bộ lọc giá?** | - | - | **N** | Y | Y |
| **C4: Số trang hợp lệ (page >= 1)?** | - | - | - | **N** | **Y** |
| *A1: Trả về danh sách rỗng (`[]`)* | **X** | - | - | - | - |
| *A2: Ẩn sản phẩm khỏi kết quả* | - | **X** | - | - | - |
| *A3: Không có sản phẩm phù hợp bộ lọc* | - | - | **X** | - | - |
| *A4: Tự động ép về `page = 1`* | - | - | - | **X** | - |
| *A5: Trả về trang kết quả phân trang thành công* | - | - | - | - | **X** |

---

## Câu 4. Triển khai kiểm thử tự động

```python
def ValidateSearchPagination(keywordLength: int, minPrice: float, page: int, pageSize: int) -> bool:
    """
    Kiểm tra tính hợp lệ của tham số tìm kiếm & phân trang:
    - 0 <= keywordLength <= 50 (Độ dài từ khóa tìm kiếm)
    - 0.0 <= minPrice <= 50000.0 (Khoảng giá lọc tối thiểu)
    - 1 <= page <= 50 (Số trang hợp lệ)
    - 1 <= pageSize <= 12 (Kích thước mỗi trang hiển thị)
    Trả về True nếu tất cả điều kiện thỏa mãn, ngược lại False.
    """
    if not (isinstance(keywordLength, int) and not isinstance(keywordLength, bool) and 0 <= keywordLength <= 50):
        return False
    if not (isinstance(minPrice, (int, float)) and not isinstance(minPrice, bool) and 0.0 <= minPrice <= 50000.0):
        return False
    if not (isinstance(page, int) and not isinstance(page, bool) and 1 <= page <= 50):
        return False
    if not (isinstance(pageSize, int) and not isinstance(pageSize, bool) and 1 <= pageSize <= 12):
        return False
    return True
```

```pytest
# thiết kế các test cases từ câu 3.
# Run test case 
import pytest

test_cases_m2 = [
    ("TC01", 10, 1000.0, 5, 6, True, "V1, V2, V3, V4, B3, B8, B13, B18"),
    ("TC02", 0, 1000.0, 5, 6, True, "B1"),
    ("TC03", 50, 1000.0, 5, 6, True, "B5"),
    ("TC04", -1, 1000.0, 5, 6, False, "X1"),
    ("TC05", 51, 1000.0, 5, 6, False, "X2"),
    ("TC06", 10, 0.0, 5, 6, True, "B6"),
    ("TC07", 10, 50000.0, 5, 6, True, "B10"),
    ("TC08", 10, -0.1, 5, 6, False, "X3"),
    ("TC09", 10, 50000.1, 5, 6, False, "X4"),
    ("TC10", 10, 1000.0, 1, 6, True, "B11"),
    ("TC11", 10, 1000.0, 50, 6, True, "B15"),
    ("TC12", 10, 1000.0, 0, 6, False, "X5"),
    ("TC13", 10, 1000.0, 51, 6, False, "X6"),
    ("TC14", 10, 1000.0, 5, 1, True, "B16"),
    ("TC15", 10, 1000.0, 5, 12, True, "B20"),
    ("TC16", 10, 1000.0, 5, 0, False, "X7"),
    ("TC17", 10, 1000.0, 5, 13, False, "X8"),
]

@pytest.mark.parametrize("tc_id,kLen,mPrice,page,pSize,expected,tag", test_cases_m2)
def test_search_pagination_validation(tc_id, kLen, mPrice, page, pSize, expected, tag):
    """Kiểm thử tự động 17 test case tìm kiếm & phân trang theo nguyên lý 4n + 1."""
    assert ValidateSearchPagination(kLen, mPrice, page, pSize) == expected

if __name__ == "__main__":
    pytest.main(["-v", __file__])
```

```kết quả test
============================= test session starts =============================
platform win32 -- Python 3.14.0, pytest-8.4.2, pluggy-1.6.0
rootdir: D:\LapTrinhAI\Testing
collected 17 items

test_search_pagination.py::test_search_pagination_validation[TC01-10-1000.0-5-6-True-V1, V2, V3, V4, B3, B8, B13, B18] PASSED [  5%]
test_search_pagination.py::test_search_pagination_validation[TC02-0-1000.0-5-6-True-B1] PASSED [ 11%]
test_search_pagination.py::test_search_pagination_validation[TC03-50-1000.0-5-6-True-B5] PASSED [ 17%]
test_search_pagination.py::test_search_pagination_validation[TC04--1-1000.0-5-6-False-X1] PASSED [ 23%]
test_search_pagination.py::test_search_pagination_validation[TC05-51-1000.0-5-6-False-X2] PASSED [ 29%]
test_search_pagination.py::test_search_pagination_validation[TC06-10-0.0-5-6-True-B6] PASSED [ 35%]
test_search_pagination.py::test_search_pagination_validation[TC07-10-50000.0-5-6-True-B10] PASSED [ 41%]
test_search_pagination.py::test_search_pagination_validation[TC08-10--0.1-5-6-False-X3] PASSED [ 47%]
test_search_pagination.py::test_search_pagination_validation[TC09-10-50000.1-5-6-False-X4] PASSED [ 52%]
test_search_pagination.py::test_search_pagination_validation[TC10-10-1000.0-1-6-True-B11] PASSED [ 58%]
test_search_pagination.py::test_search_pagination_validation[TC11-10-1000.0-50-6-True-B15] PASSED [ 64%]
test_search_pagination.py::test_search_pagination_validation[TC12-10-1000.0-0-6-False-X5] PASSED [ 70%]
test_search_pagination.py::test_search_pagination_validation[TC13-10-1000.0-51-6-False-X6] PASSED [ 76%]
test_search_pagination.py::test_search_pagination_validation[TC14-10-1000.0-5-1-True-B16] PASSED [ 82%]
test_search_pagination.py::test_search_pagination_validation[TC15-10-1000.0-5-12-True-B20] PASSED [ 88%]
test_search_pagination.py::test_search_pagination_validation[TC16-10-1000.0-5-0-False-X7] PASSED [ 94%]
test_search_pagination.py::test_search_pagination_validation[TC17-10-1000.0-5-13-False-X8] PASSED [100%]

============================= 17 passed in 0.14s ==============================
```
