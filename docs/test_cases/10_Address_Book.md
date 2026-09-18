# BÀI LÀM: KIỂM THỬ CHỨC NĂNG 10 - QUẢN LÝ SỔ ĐỊA CHỈ (ADDRESS BOOK MANAGEMENT)

- **Họ và tên sinh viên:** Nguyễn Hoàng Phương
- **Mã số sinh viên (MSSV):** 080205010954
- **Môn học:** Kiểm Chứng Phần Mềm
- **Chủ đề:** Phân hoạch lớp tương đương, phân tích giá trị biên, bảng quyết định, chuyển đổi trạng thái, thiết kế test case và kiểm thử tự động

---

## Bảng phân tích điều kiện kiểm thử (Test Conditions)

| Conditions | Valid Partition | Tag | Invalid Partitions | Tag | Valid Boundaries | Tag |
|---|---|---|---|---|---|---|
| **Độ dài tên người nhận** (`receiverNameLength`) | 2 ≤ receiverNameLength ≤ 50 | V1 | • receiverNameLength < 2<br>• receiverNameLength > 50 | X1<br>X2 | • 2 (min)<br>• 3 (min+)<br>• 15 (nominal)<br>• 49 (max-)<br>• 50 (max) | B1<br>B2<br>B3<br>B4<br>B5 |
| **Độ dài số điện thoại** (`receiverPhoneLength`) | 10 ≤ receiverPhoneLength ≤ 11 | V2 | • receiverPhoneLength < 10<br>• receiverPhoneLength > 11 | X3<br>X4 | • 10 (min)<br>• 10 (min+)<br>• 10 (nominal)<br>• 11 (max-)<br>• 11 (max) | B6<br>B7<br>B8<br>B9<br>B10 |
| **Độ dài địa chỉ chi tiết** (`detailAddressLength`) | 5 ≤ detailAddressLength ≤ 200 | V3 | • detailAddressLength < 5<br>• detailAddressLength > 200 | X5<br>X6 | • 5 (min)<br>• 6 (min+)<br>• 50 (nominal)<br>• 199 (max-)<br>• 200 (max) | B11<br>B12<br>B13<br>B14<br>B15 |
| **Số lượng địa chỉ lưu tối đa** (`addressBookCount`) | 1 ≤ addressBookCount ≤ 9 | V4 | • addressBookCount < 1<br>• addressBookCount ≥ 10 | X7<br>X8 | • 1 (min)<br>• 2 (min+)<br>• 3 (nominal)<br>• 8 (max-)<br>• 9 (max) | B16<br>B17<br>B18<br>B19<br>B20 |

---

## Câu 1. Xác định lớp tương đương

| Biến đầu vào | Lớp hợp lệ | Tag | Lớp không hợp lệ | Tag |
|---|---|---|---|---|
| **Độ dài tên người nhận** (`receiverNameLength`) | 2 ≤ receiverNameLength ≤ 50 | V1 | • receiverNameLength < 2 (Tên quá ngắn)<br>• receiverNameLength > 50 (Tên quá dài) | X1<br>X2 |
| **Độ dài số điện thoại** (`receiverPhoneLength`) | 10 ≤ receiverPhoneLength ≤ 11 | V2 | • receiverPhoneLength < 10 (Số điện thoại thiếu số)<br>• receiverPhoneLength > 11 (Số điện thoại thừa số) | X3<br>X4 |
| **Độ dài địa chỉ chi tiết** (`detailAddressLength`) | 5 ≤ detailAddressLength ≤ 200 | V3 | • detailAddressLength < 5 (Địa chỉ quá ngắn)<br>• detailAddressLength > 200 (Địa chỉ quá dài) | X5<br>X6 |
| **Số lượng địa chỉ lưu tối đa** (`addressBookCount`) | 1 ≤ addressBookCount ≤ 9 (Limit = 10) | V4 | • addressBookCount < 1 (Số âm)<br>• addressBookCount ≥ 10 (Đã đạt trần 10 địa chỉ) | X7<br>X8 |

---

## Câu 2. Phân tích giá trị biên

### 1. Bảng 5 giá trị biên cho từng biến đầu vào

| Biến đầu vào | min | min+ | nominal | max- | max | Tag biên |
|---|---:|---:|---:|---:|---:|---|
| **Độ dài tên người nhận** (`receiverNameLength`) | 2 | 3 | 15 | 49 | 50 | B1, B2, B3, B4, B5 |
| **Độ dài số điện thoại** (`receiverPhoneLength`) | 10 | 10 | 10 | 11 | 11 | B6, B7, B8, B9, B10 |
| **Độ dài địa chỉ chi tiết** (`detailAddressLength`) | 5 | 6 | 50 | 199 | 200 | B11, B12, B13, B14, B15 |
| **Số lượng địa chỉ lưu tối đa** (`addressBookCount`) | 1 | 2 | 3 | 8 | 9 | B16, B17, B18, B19, B20 |

### 2. Bảng 17 test case Standard BVA (Single Fault Assumption: $4n + 1 = 17$)

Theo kỹ thuật Standard Boundary Value Analysis, với $n = 4$ biến đầu vào, số test case là:
$$4n + 1 = 4 \times 4 + 1 = \mathbf{17\text{ test case}}$$

Giữ $n - 1$ biến tại giá trị danh định (`nominal`), lần lượt thay đổi 1 biến qua 4 giá trị biên (`min`, `min+`, `max-`, `max`):

| STT | Mã TC | Biến kiểm thử biên | Điểm biên kiểm tra | Tên nhận | SĐT | Địa chỉ | Số lượng sổ | Kết quả mong đợi | Tag bao phủ |
|:---:|:---:|:---|:---|:---:|:---:|:---:|:---:|:---|:---|
| 1 | BVA01 | Baseline (Tất cả) | Nominal | 15 | 10 | 50 | 3 | Hợp lệ (True) | B3, B8, B13, B18 |
| 2 | BVA02 | Độ dài tên người nhận | min (2) | **2** | 10 | 50 | 3 | Hợp lệ (True) | B1 |
| 3 | BVA03 | Độ dài tên người nhận | max (50) | **50** | 10 | 50 | 3 | Hợp lệ (True) | B5 |
| 4 | BVA04 | Độ dài tên người nhận | min-1 (1) | **1** | 10 | 50 | 3 | Không hợp lệ (False) | X1 |
| 5 | BVA05 | Độ dài tên người nhận | max+1 (51) | **51** | 10 | 50 | 3 | Không hợp lệ (False) | X2 |
| 6 | BVA06 | Độ dài SĐT | min (10) | 15 | **10** | 50 | 3 | Hợp lệ (True) | B6 |
| 7 | BVA07 | Độ dài SĐT | max (11) | 15 | **11** | 50 | 3 | Hợp lệ (True) | B10 |
| 8 | BVA08 | Độ dài SĐT | min-1 (9) | 15 | **9** | 50 | 3 | Không hợp lệ (False) | X3 |
| 9 | BVA09 | Độ dài SĐT | max+1 (12) | 15 | **12** | 50 | 3 | Không hợp lệ (False) | X4 |
| 10 | BVA10 | Độ dài địa chỉ | min (5) | 15 | 10 | **5** | 3 | Hợp lệ (True) | B11 |
| 11 | BVA11 | Độ dài địa chỉ | max (200) | 15 | 10 | **200** | 3 | Hợp lệ (True) | B15 |
| 12 | BVA12 | Độ dài địa chỉ | min-1 (4) | 15 | 10 | **4** | 3 | Không hợp lệ (False) | X5 |
| 13 | BVA13 | Độ dài địa chỉ | max+1 (201) | 15 | 10 | **201** | 3 | Không hợp lệ (False) | X6 |
| 14 | BVA14 | Số lượng sổ địa chỉ | min (1) | 15 | 10 | 50 | **1** | Hợp lệ (True) | B16 |
| 15 | BVA15 | Số lượng sổ địa chỉ | max (9) | 15 | 10 | 50 | **9** | Hợp lệ (True) | B20 |
| 16 | BVA16 | Số lượng sổ địa chỉ | min-1 (0) | 15 | 10 | 50 | **0** | Không hợp lệ (False) | X7 |
| 17 | BVA17 | Số lượng sổ địa chỉ | max+1 (10) | 15 | 10 | 50 | **10** | Không hợp lệ (False) | X8 |

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
| TC01 | receiverNameLength: 15, receiverPhoneLength: 10, detailAddressLength: 50, addressBookCount: 3 | Hợp lệ (True) | V1, V2, V3, V4, B3, B8, B13, B18 |
| TC02 | receiverNameLength: 2, receiverPhoneLength: 10, detailAddressLength: 50, addressBookCount: 3 | Hợp lệ (True) | B1 |
| TC03 | receiverNameLength: 50, receiverPhoneLength: 10, detailAddressLength: 50, addressBookCount: 3 | Hợp lệ (True) | B5 |
| TC04 | receiverNameLength: 1, receiverPhoneLength: 10, detailAddressLength: 50, addressBookCount: 3 | Không hợp lệ (False): Tên người nhận nhỏ hơn 2 ký tự | X1 |
| TC05 | receiverNameLength: 51, receiverPhoneLength: 10, detailAddressLength: 50, addressBookCount: 3 | Không hợp lệ (False): Tên người nhận lớn hơn 50 ký tự | X2 |
| TC06 | receiverNameLength: 15, receiverPhoneLength: 10, detailAddressLength: 50, addressBookCount: 3 | Hợp lệ (True) | B6 |
| TC07 | receiverNameLength: 15, receiverPhoneLength: 11, detailAddressLength: 50, addressBookCount: 3 | Hợp lệ (True) | B10 |
| TC08 | receiverNameLength: 15, receiverPhoneLength: 9, detailAddressLength: 50, addressBookCount: 3 | Không hợp lệ (False): Số điện thoại nhỏ hơn 10 số | X3 |
| TC09 | receiverNameLength: 15, receiverPhoneLength: 12, detailAddressLength: 50, addressBookCount: 3 | Không hợp lệ (False): Số điện thoại lớn hơn 11 số | X4 |
| TC10 | receiverNameLength: 15, receiverPhoneLength: 10, detailAddressLength: 5, addressBookCount: 3 | Hợp lệ (True) | B11 |
| TC11 | receiverNameLength: 15, receiverPhoneLength: 10, detailAddressLength: 200, addressBookCount: 3 | Hợp lệ (True) | B15 |
| TC12 | receiverNameLength: 15, receiverPhoneLength: 10, detailAddressLength: 4, addressBookCount: 3 | Không hợp lệ (False): Địa chỉ chi tiết nhỏ hơn 5 ký tự | X5 |
| TC13 | receiverNameLength: 15, receiverPhoneLength: 10, detailAddressLength: 201, addressBookCount: 3 | Không hợp lệ (False): Địa chỉ chi tiết lớn hơn 200 ký tự | X6 |
| TC14 | receiverNameLength: 15, receiverPhoneLength: 10, detailAddressLength: 50, addressBookCount: 1 | Hợp lệ (True) | B16 |
| TC15 | receiverNameLength: 15, receiverPhoneLength: 10, detailAddressLength: 50, addressBookCount: 9 | Hợp lệ (True) | B20 |
| TC16 | receiverNameLength: 15, receiverPhoneLength: 10, detailAddressLength: 50, addressBookCount: 0 | Không hợp lệ (False): Số lượng sổ địa chỉ nhỏ hơn 1 | X7 |
| TC17 | receiverNameLength: 15, receiverPhoneLength: 10, detailAddressLength: 50, addressBookCount: 10 | Không hợp lệ (False): Đã đạt trần tối đa 10 địa chỉ | X8 |

### 2. Bảng test case chi tiết theo đề bài (8 cột)

| STT | Tên test case | Độ dài Tên nhận | Độ dài SĐT | Độ dài Địa chỉ | Số lượng sổ hiện tại | Kết quả mong đợi | Tag được bao phủ |
|:---:|:---|:---:|:---:|:---:|:---:|:---|:---|
| 1 | Baseline danh định (nominal) | 15 | 10 | 50 | 3 | Hợp lệ (True) | V1, V2, V3, V4, B3, B8, B13, B18 |
| 2 | Biên dưới hợp lệ receiverNameLength = min (2) | **2** | 10 | 50 | 3 | Hợp lệ (True) | B1 |
| 3 | Biên trên hợp lệ receiverNameLength = max (50) | **50** | 10 | 50 | 3 | Hợp lệ (True) | B5 |
| 4 | Ngoài biên dưới receiverNameLength < min (1) | **1** | 10 | 50 | 3 | Không hợp lệ (False): Tên người nhận nhỏ hơn 2 ký tự | X1 |
| 5 | Ngoài biên trên receiverNameLength > max (51) | **51** | 10 | 50 | 3 | Không hợp lệ (False): Tên người nhận lớn hơn 50 ký tự | X2 |
| 6 | Biên dưới hợp lệ receiverPhoneLength = min (10) | 15 | **10** | 50 | 3 | Hợp lệ (True) | B6 |
| 7 | Biên trên hợp lệ receiverPhoneLength = max (11) | 15 | **11** | 50 | 3 | Hợp lệ (True) | B10 |
| 8 | Ngoài biên dưới receiverPhoneLength < min (9) | 15 | **9** | 50 | 3 | Không hợp lệ (False): Số điện thoại nhỏ hơn 10 số | X3 |
| 9 | Ngoài biên trên receiverPhoneLength > max (12) | 15 | **12** | 50 | 3 | Không hợp lệ (False): Số điện thoại lớn hơn 11 số | X4 |
| 10 | Biên dưới hợp lệ detailAddressLength = min (5) | 15 | 10 | **5** | 3 | Hợp lệ (True) | B11 |
| 11 | Biên trên hợp lệ detailAddressLength = max (200) | 15 | 10 | **200** | 3 | Hợp lệ (True) | B15 |
| 12 | Ngoài biên dưới detailAddressLength < min (4) | 15 | 10 | **4** | 3 | Không hợp lệ (False): Địa chỉ chi tiết nhỏ hơn 5 ký tự | X5 |
| 13 | Ngoài biên trên detailAddressLength > max (201) | 15 | 10 | **201** | 3 | Không hợp lệ (False): Địa chỉ chi tiết lớn hơn 200 ký tự | X6 |
| 14 | Biên dưới hợp lệ addressBookCount = min (1) | 15 | 10 | 50 | **1** | Hợp lệ (True) | B16 |
| 15 | Biên trên hợp lệ addressBookCount = max (9) | 15 | 10 | 50 | **9** | Hợp lệ (True) | B20 |
| 16 | Ngoài biên dưới addressBookCount < min (0) | 15 | 10 | 50 | **0** | Không hợp lệ (False): Số lượng sổ địa chỉ nhỏ hơn 1 | X7 |
| 17 | Ngoài biên trên addressBookCount > max (10) | 15 | 10 | 50 | **10** | Không hợp lệ (False): Đã đạt trần tối đa 10 địa chỉ | X8 |

---

### 3. Bổ sung: Bảng Quyết định sổ địa chỉ (Collapsed Decision Table - 4 Rules)

| Condition / Action | Rule 1 (R1) | Rule 2 (R2) | Rule 3 (R3) | Rule 4 (R4) |
| :--- | :---: | :---: | :---: | :---: |
| **C1: User đã đăng nhập?** | **N** | Y | Y | Y |
| **C2: Form địa chỉ hợp lệ?** | - | **N** | Y | Y |
| **C3: Số lượng địa chỉ hiện tại < 10?** | - | - | **N** | **Y** |
| *A1: Chặn truy cập (HTTP 401 Unauthorized)* | **X** | - | - | - |
| *A2: Báo lỗi Validation Form (HTTP 400)* | - | **X** | - | - |
| *A3: Báo lỗi "Đã đạt tối đa 10 địa chỉ lưu"* | - | - | **X** | - |
| *A4: Lưu địa chỉ mới thành công (HTTP 200)* | - | - | - | **X** |

---

## Câu 4. Triển khai kiểm thử tự động

```python
def ValidateAddressBook(receiverNameLength: int, receiverPhoneLength: int, detailAddressLength: int, addressBookCount: int) -> bool:
    """
    Kiểm tra tính hợp lệ của thêm mới sổ địa chỉ:
    - 2 <= receiverNameLength <= 50 (Độ dài tên người nhận)
    - 10 <= receiverPhoneLength <= 11 (Độ dài số điện thoại)
    - 5 <= detailAddressLength <= 200 (Độ dài địa chỉ chi tiết)
    - 1 <= addressBookCount <= 9 (Số lượng địa chỉ hiện tại < 10)
    Trả về True nếu tất cả điều kiện thỏa mãn, ngược lại False.
    """
    if not (isinstance(receiverNameLength, int) and not isinstance(receiverNameLength, bool) and 2 <= receiverNameLength <= 50):
        return False
    if not (isinstance(receiverPhoneLength, int) and not isinstance(receiverPhoneLength, bool) and 10 <= receiverPhoneLength <= 11):
        return False
    if not (isinstance(detailAddressLength, int) and not isinstance(detailAddressLength, bool) and 5 <= detailAddressLength <= 200):
        return False
    if not (isinstance(addressBookCount, int) and not isinstance(addressBookCount, bool) and 1 <= addressBookCount <= 9):
        return False
    return True
```

```pytest
# thiết kế các test cases từ câu 3.
# Run test case 
import pytest

test_cases_m10 = [
    ("TC01", 15, 10, 50, 3, True, "V1, V2, V3, V4, B3, B8, B13, B18"),
    ("TC02", 2, 10, 50, 3, True, "B1"),
    ("TC03", 50, 10, 50, 3, True, "B5"),
    ("TC04", 1, 10, 50, 3, False, "X1"),
    ("TC05", 51, 10, 50, 3, False, "X2"),
    ("TC06", 15, 10, 50, 3, True, "B6"),
    ("TC07", 15, 11, 50, 3, True, "B10"),
    ("TC08", 15, 9, 50, 3, False, "X3"),
    ("TC09", 15, 12, 50, 3, False, "X4"),
    ("TC10", 15, 10, 5, 3, True, "B11"),
    ("TC11", 15, 10, 200, 3, True, "B15"),
    ("TC12", 15, 10, 4, 3, False, "X5"),
    ("TC13", 15, 10, 201, 3, False, "X6"),
    ("TC14", 15, 10, 50, 1, True, "B16"),
    ("TC15", 15, 10, 50, 9, True, "B20"),
    ("TC16", 15, 10, 50, 0, False, "X7"),
    ("TC17", 15, 10, 50, 10, False, "X8"),
]

@pytest.mark.parametrize("tc_id,nLen,pLen,dLen,abCount,expected,tag", test_cases_m10)
def test_address_book_validation(tc_id, nLen, pLen, dLen, abCount, expected, tag):
    """Kiểm thử tự động 17 test case sổ địa chỉ theo nguyên lý 4n + 1."""
    assert ValidateAddressBook(nLen, pLen, dLen, abCount) == expected

if __name__ == "__main__":
    pytest.main(["-v", __file__])
```

```kết quả test
============================= test session starts =============================
platform win32 -- Python 3.14.0, pytest-8.4.2, pluggy-1.6.0
rootdir: D:\LapTrinhAI\Testing
collected 17 items

test_address_book.py::test_address_book_validation[TC01-15-10-50-3-True-V1, V2, V3, V4, B3, B8, B13, B18] PASSED [  5%]
test_address_book.py::test_address_book_validation[TC02-2-10-50-3-True-B1] PASSED [ 11%]
test_address_book.py::test_address_book_validation[TC03-50-10-50-3-True-B5] PASSED [ 17%]
test_address_book.py::test_address_book_validation[TC04-1-10-50-3-False-X1] PASSED [ 23%]
test_address_book.py::test_address_book_validation[TC05-51-10-50-3-False-X2] PASSED [ 29%]
test_address_book.py::test_address_book_validation[TC06-15-10-50-3-True-B6] PASSED [ 35%]
test_address_book.py::test_address_book_validation[TC07-15-11-50-3-True-B10] PASSED [ 41%]
test_address_book.py::test_address_book_validation[TC08-15-9-50-3-False-X3] PASSED [ 47%]
test_address_book.py::test_address_book_validation[TC09-15-12-50-3-False-X4] PASSED [ 52%]
test_address_book.py::test_address_book_validation[TC10-15-10-5-3-True-B11] PASSED [ 58%]
test_address_book.py::test_address_book_validation[TC11-15-10-200-3-True-B15] PASSED [ 64%]
test_address_book.py::test_address_book_validation[TC12-15-10-4-3-False-X5] PASSED [ 70%]
test_address_book.py::test_address_book_validation[TC13-15-10-201-3-False-X6] PASSED [ 76%]
test_address_book.py::test_address_book_validation[TC14-15-10-50-1-True-B16] PASSED [ 82%]
test_address_book.py::test_address_book_validation[TC15-15-10-50-9-True-B20] PASSED [ 88%]
test_address_book.py::test_address_book_validation[TC16-15-10-50-0-False-X7] PASSED [ 94%]
test_address_book.py::test_address_book_validation[TC17-15-10-50-10-False-X8] PASSED [100%]

============================= 17 passed in 0.14s ==============================
```
