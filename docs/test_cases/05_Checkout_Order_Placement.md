# BÀI LÀM: KIỂM THỬ CHỨC NĂNG 5 - ĐẶT HÀNG & THANH TOÁN (CHECKOUT & ORDER PLACEMENT)

- **Họ và tên sinh viên:** Nguyễn Hoàng Phương
- **Mã số sinh viên (MSSV):** 080205010954
- **Môn học:** Kiểm Chứng Phần Mềm
- **Chủ đề:** Phân hoạch lớp tương đương, phân tích giá trị biên, bảng quyết định, chuyển đổi trạng thái, thiết kế test case và kiểm thử tự động

---

## Bảng phân tích điều kiện kiểm thử (Test Conditions)

| Conditions | Valid Partition | Tag | Invalid Partitions | Tag | Valid Boundaries | Tag |
|---|---|---|---|---|---|---|
| **Độ dài họ tên** (`customerNameLength`) | 2 ≤ customerNameLength ≤ 50 | V1 | • customerNameLength < 2<br>• customerNameLength > 50 | X1<br>X2 | • 2 (min)<br>• 3 (min+)<br>• 15 (nominal)<br>• 49 (max-)<br>• 50 (max) | B1<br>B2<br>B3<br>B4<br>B5 |
| **Độ dài số điện thoại** (`phoneLength`) | 10 ≤ phoneLength ≤ 11 | V2 | • phoneLength < 10<br>• phoneLength > 11 | X3<br>X4 | • 10 (min)<br>• 10 (min+)<br>• 10 (nominal)<br>• 11 (max-)<br>• 11 (max) | B6<br>B7<br>B8<br>B9<br>B10 |
| **Độ dài địa chỉ** (`addressLength`) | 10 ≤ addressLength ≤ 200 | V3 | • addressLength < 10<br>• addressLength > 200 | X5<br>X6 | • 10 (min)<br>• 11 (min+)<br>• 50 (nominal)<br>• 199 (max-)<br>• 200 (max) | B11<br>B12<br>B13<br>B14<br>B15 |
| **Tổng tiền thanh toán** (`orderTotal` - k) | 100.0 ≤ orderTotal ≤ 100000.0 | V4 | • orderTotal < 100.0<br>• orderTotal > 100000.0 | X7<br>X8 | • 100.0 (min)<br>• 101.0 (min+)<br>• 1500.0 (nominal)<br>• 99999.0 (max-)<br>• 100000.0 (max) | B16<br>B17<br>B18<br>B19<br>B20 |

---

## Câu 1. Xác định lớp tương đương

| Biến đầu vào | Lớp hợp lệ | Tag | Lớp không hợp lệ | Tag |
|---|---|---|---|---|
| **Độ dài họ tên** (`customerNameLength`) | 2 ≤ customerNameLength ≤ 50 | V1 | • customerNameLength < 2 (Tên quá ngắn)<br>• customerNameLength > 50 (Tên quá dài) | X1<br>X2 |
| **Độ dài số điện thoại** (`phoneLength`) | 10 ≤ phoneLength ≤ 11 | V2 | • phoneLength < 10 (Số điện thoại thiếu số)<br>• phoneLength > 11 (Số điện thoại thừa số) | X3<br>X4 |
| **Độ dài địa chỉ** (`addressLength`) | 10 ≤ addressLength ≤ 200 | V3 | • addressLength < 10 (Địa chỉ quá ngắn)<br>• addressLength > 200 (Địa chỉ quá dài) | X5<br>X6 |
| **Tổng tiền thanh toán** (`orderTotal`) | 100.0 ≤ orderTotal ≤ 100000.0 | V4 | • orderTotal < 100.0 (Chưa đạt đơn tối thiểu)<br>• orderTotal > 100000.0 (Vượt hạn mức thanh toán) | X7<br>X8 |

---

## Câu 2. Phân tích giá trị biên

### 1. Bảng 5 giá trị biên cho từng biến đầu vào

| Biến đầu vào | min | min+ | nominal | max- | max | Tag biên |
|---|---:|---:|---:|---:|---:|---|
| **Độ dài họ tên** (`customerNameLength`) | 2 | 3 | 15 | 49 | 50 | B1, B2, B3, B4, B5 |
| **Độ dài số điện thoại** (`phoneLength`) | 10 | 10 | 10 | 11 | 11 | B6, B7, B8, B9, B10 |
| **Độ dài địa chỉ** (`addressLength`) | 10 | 11 | 50 | 199 | 200 | B11, B12, B13, B14, B15 |
| **Tổng tiền thanh toán** (`orderTotal`) | 100.0 | 101.0 | 1500.0 | 99999.0 | 100000.0 | B16, B17, B18, B19, B20 |

### 2. Bảng 17 test case Standard BVA (Single Fault Assumption: $4n + 1 = 17$)

Theo kỹ thuật Standard Boundary Value Analysis, với $n = 4$ biến đầu vào, số test case là:
$$4n + 1 = 4 \times 4 + 1 = \mathbf{17\text{ test case}}$$

Giữ $n - 1$ biến tại giá trị danh định (`nominal`), lần lượt thay đổi 1 biến qua 4 giá trị biên (`min`, `min+`, `max-`, `max`):

| STT | Mã TC | Biến kiểm thử biên | Điểm biên kiểm tra | Tên người nhận | SĐT | Địa chỉ | Tổng tiền (k) | Kết quả mong đợi | Tag bao phủ |
|:---:|:---:|:---|:---|:---:|:---:|:---:|:---:|:---|:---|
| 1 | BVA01 | Baseline (Tất cả) | Nominal | 15 | 10 | 50 | 1500.0 | Hợp lệ (True) | B3, B8, B13, B18 |
| 2 | BVA02 | Độ dài họ tên | min (2) | **2** | 10 | 50 | 1500.0 | Hợp lệ (True) | B1 |
| 3 | BVA03 | Độ dài họ tên | max (50) | **50** | 10 | 50 | 1500.0 | Hợp lệ (True) | B5 |
| 4 | BVA04 | Độ dài họ tên | min-1 (1) | **1** | 10 | 50 | 1500.0 | Không hợp lệ (False) | X1 |
| 5 | BVA05 | Độ dài họ tên | max+1 (51) | **51** | 10 | 50 | 1500.0 | Không hợp lệ (False) | X2 |
| 6 | BVA06 | Độ dài SĐT | min (10) | 15 | **10** | 50 | 1500.0 | Hợp lệ (True) | B6 |
| 7 | BVA07 | Độ dài SĐT | max (11) | 15 | **11** | 50 | 1500.0 | Hợp lệ (True) | B10 |
| 8 | BVA08 | Độ dài SĐT | min-1 (9) | 15 | **9** | 50 | 1500.0 | Không hợp lệ (False) | X3 |
| 9 | BVA09 | Độ dài SĐT | max+1 (12) | 15 | **12** | 50 | 1500.0 | Không hợp lệ (False) | X4 |
| 10 | BVA10 | Độ dài địa chỉ | min (10) | 15 | 10 | **10** | 1500.0 | Hợp lệ (True) | B11 |
| 11 | BVA11 | Độ dài địa chỉ | max (200) | 15 | 10 | **200** | 1500.0 | Hợp lệ (True) | B15 |
| 12 | BVA12 | Độ dài địa chỉ | min-1 (9) | 15 | 10 | **9** | 1500.0 | Không hợp lệ (False) | X5 |
| 13 | BVA13 | Độ dài địa chỉ | max+1 (201) | 15 | 10 | **201** | 1500.0 | Không hợp lệ (False) | X6 |
| 14 | BVA14 | Tổng tiền đơn | min (100.0) | 15 | 10 | 50 | **100.0** | Hợp lệ (True) | B16 |
| 15 | BVA15 | Tổng tiền đơn | max (100000.0) | 15 | 10 | 50 | **100000.0** | Hợp lệ (True) | B20 |
| 16 | BVA16 | Tổng tiền đơn | min-1 (99.0) | 15 | 10 | 50 | **99.0** | Không hợp lệ (False) | X7 |
| 17 | BVA17 | Tổng tiền đơn | max+1 (100001.0) | 15 | 10 | 50 | **100001.0** | Không hợp lệ (False) | X8 |

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
| TC01 | customerNameLength: 15, phoneLength: 10, addressLength: 50, orderTotal: 1500.0 | Hợp lệ (True) | V1, V2, V3, V4, B3, B8, B13, B18 |
| TC02 | customerNameLength: 2, phoneLength: 10, addressLength: 50, orderTotal: 1500.0 | Hợp lệ (True) | B1 |
| TC03 | customerNameLength: 50, phoneLength: 10, addressLength: 50, orderTotal: 1500.0 | Hợp lệ (True) | B5 |
| TC04 | customerNameLength: 1, phoneLength: 10, addressLength: 50, orderTotal: 1500.0 | Không hợp lệ (False): Tên người nhận nhỏ hơn 2 ký tự | X1 |
| TC05 | customerNameLength: 51, phoneLength: 10, addressLength: 50, orderTotal: 1500.0 | Không hợp lệ (False): Tên người nhận lớn hơn 50 ký tự | X2 |
| TC06 | customerNameLength: 15, phoneLength: 10, addressLength: 50, orderTotal: 1500.0 | Hợp lệ (True) | B6 |
| TC07 | customerNameLength: 15, phoneLength: 11, addressLength: 50, orderTotal: 1500.0 | Hợp lệ (True) | B10 |
| TC08 | customerNameLength: 15, phoneLength: 9, addressLength: 50, orderTotal: 1500.0 | Không hợp lệ (False): Số điện thoại nhỏ hơn 10 số | X3 |
| TC09 | customerNameLength: 15, phoneLength: 12, addressLength: 50, orderTotal: 1500.0 | Không hợp lệ (False): Số điện thoại lớn hơn 11 số | X4 |
| TC10 | customerNameLength: 15, phoneLength: 10, addressLength: 10, orderTotal: 1500.0 | Hợp lệ (True) | B11 |
| TC11 | customerNameLength: 15, phoneLength: 10, addressLength: 200, orderTotal: 1500.0 | Hợp lệ (True) | B15 |
| TC12 | customerNameLength: 15, phoneLength: 10, addressLength: 9, orderTotal: 1500.0 | Không hợp lệ (False): Địa chỉ nhỏ hơn 10 ký tự | X5 |
| TC13 | customerNameLength: 15, phoneLength: 10, addressLength: 201, orderTotal: 1500.0 | Không hợp lệ (False): Địa chỉ lớn hơn 200 ký tự | X6 |
| TC14 | customerNameLength: 15, phoneLength: 10, addressLength: 50, orderTotal: 100.0 | Hợp lệ (True) | B16 |
| TC15 | customerNameLength: 15, phoneLength: 10, addressLength: 50, orderTotal: 100000.0 | Hợp lệ (True) | B20 |
| TC16 | customerNameLength: 15, phoneLength: 10, addressLength: 50, orderTotal: 99.0 | Không hợp lệ (False): Tổng tiền nhỏ hơn 100.0k | X7 |
| TC17 | customerNameLength: 15, phoneLength: 10, addressLength: 50, orderTotal: 100001.0 | Không hợp lệ (False): Tổng tiền vượt trần 100000.0k | X8 |

### 2. Bảng test case chi tiết theo đề bài (8 cột)

| STT | Tên test case | Độ dài Họ tên | Độ dài SĐT | Độ dài Địa chỉ | Tổng tiền đơn (k) | Kết quả mong đợi | Tag được bao phủ |
|:---:|:---|:---:|:---:|:---:|:---:|:---|:---|
| 1 | Baseline danh định (nominal) | 15 | 10 | 50 | 1500.0 | Hợp lệ (True) | V1, V2, V3, V4, B3, B8, B13, B18 |
| 2 | Biên dưới hợp lệ customerNameLength = min (2) | **2** | 10 | 50 | 1500.0 | Hợp lệ (True) | B1 |
| 3 | Biên trên hợp lệ customerNameLength = max (50) | **50** | 10 | 50 | 1500.0 | Hợp lệ (True) | B5 |
| 4 | Ngoài biên dưới customerNameLength < min (1) | **1** | 10 | 50 | 1500.0 | Không hợp lệ (False): Tên người nhận nhỏ hơn 2 ký tự | X1 |
| 5 | Ngoài biên trên customerNameLength > max (51) | **51** | 10 | 50 | 1500.0 | Không hợp lệ (False): Tên người nhận lớn hơn 50 ký tự | X2 |
| 6 | Biên dưới hợp lệ phoneLength = min (10) | 15 | **10** | 50 | 1500.0 | Hợp lệ (True) | B6 |
| 7 | Biên trên hợp lệ phoneLength = max (11) | 15 | **11** | 50 | 1500.0 | Hợp lệ (True) | B10 |
| 8 | Ngoài biên dưới phoneLength < min (9) | 15 | **9** | 50 | 1500.0 | Không hợp lệ (False): Số điện thoại nhỏ hơn 10 số | X3 |
| 9 | Ngoài biên trên phoneLength > max (12) | 15 | **12** | 50 | 1500.0 | Không hợp lệ (False): Số điện thoại lớn hơn 11 số | X4 |
| 10 | Biên dưới hợp lệ addressLength = min (10) | 15 | 10 | **10** | 1500.0 | Hợp lệ (True) | B11 |
| 11 | Biên trên hợp lệ addressLength = max (200) | 15 | 10 | **200** | 1500.0 | Hợp lệ (True) | B15 |
| 12 | Ngoài biên dưới addressLength < min (9) | 15 | 10 | **9** | 1500.0 | Không hợp lệ (False): Địa chỉ nhỏ hơn 10 ký tự | X5 |
| 13 | Ngoài biên trên addressLength > max (201) | 15 | 10 | **201** | 1500.0 | Không hợp lệ (False): Địa chỉ lớn hơn 200 ký tự | X6 |
| 14 | Biên dưới hợp lệ orderTotal = min (100.0) | 15 | 10 | 50 | **100.0** | Hợp lệ (True) | B16 |
| 15 | Biên trên hợp lệ orderTotal = max (100000.0) | 15 | 10 | 50 | **100000.0** | Hợp lệ (True) | B20 |
| 16 | Ngoài biên dưới orderTotal < min (99.0) | 15 | 10 | 50 | **99.0** | Không hợp lệ (False): Tổng tiền nhỏ hơn 100.0k | X7 |
| 17 | Ngoài biên trên orderTotal > max (100001.0) | 15 | 10 | 50 | **100001.0** | Không hợp lệ (False): Tổng tiền vượt trần 100000.0k | X8 |

---

### 3. Bổ sung: Bảng Quyết định phương thức thanh toán (Decision Table - 4 Rules)

| Condition / Action | Rule 1 (COD) | Rule 2 (VNPAY) | Rule 3 (MOMO) | Rule 4 (BANK) |
| :--- | :---: | :---: | :---: | :---: |
| **C1: Thông tin giao hàng hợp lệ?** | Y | Y | Y | Y |
| **C2: Phương thức thanh toán được chọn** | COD | VNPAY | MOMO | BANK_TRANSFER |
| *A1: Tạo đơn ngay -> Chuyển trạng thái `PENDING`* | **X** | - | - | - |
| *A2: Chuyển hướng sang Cổng VNPAY Sandbox* | - | **X** | - | - |
| *A3: Chuyển hướng sang Cổng MoMo QR* | - | - | **X** | - |
| *A4: Hiển thị thông tin STK ngân hàng* | - | - | - | **X** |

---

## Câu 4. Triển khai kiểm thử tự động

```python
def ValidateCheckout(customerNameLength: int, phoneLength: int, addressLength: int, orderTotal: float) -> bool:
    """
    Kiểm tra tính hợp lệ của thông tin đặt hàng:
    - 2 <= customerNameLength <= 50 (Độ dài họ tên người nhận)
    - 10 <= phoneLength <= 11 (Độ dài số điện thoại VN)
    - 10 <= addressLength <= 200 (Độ dài địa chỉ nhận hàng)
    - 100.0 <= orderTotal <= 100000.0 (Tổng tiền thanh toán)
    Trả về True nếu tất cả điều kiện thỏa mãn, ngược lại False.
    """
    if not (isinstance(customerNameLength, int) and not isinstance(customerNameLength, bool) and 2 <= customerNameLength <= 50):
        return False
    if not (isinstance(phoneLength, int) and not isinstance(phoneLength, bool) and 10 <= phoneLength <= 11):
        return False
    if not (isinstance(addressLength, int) and not isinstance(addressLength, bool) and 10 <= addressLength <= 200):
        return False
    if not (isinstance(orderTotal, (int, float)) and not isinstance(orderTotal, bool) and 100.0 <= orderTotal <= 100000.0):
        return False
    return True
```

```pytest
# thiết kế các test cases từ câu 3.
# Run test case 
import pytest

test_cases_m5 = [
    ("TC01", 15, 10, 50, 1500.0, True, "V1, V2, V3, V4, B3, B8, B13, B18"),
    ("TC02", 2, 10, 50, 1500.0, True, "B1"),
    ("TC03", 50, 10, 50, 1500.0, True, "B5"),
    ("TC04", 1, 10, 50, 1500.0, False, "X1"),
    ("TC05", 51, 10, 50, 1500.0, False, "X2"),
    ("TC06", 15, 10, 50, 1500.0, True, "B6"),
    ("TC07", 15, 11, 50, 1500.0, True, "B10"),
    ("TC08", 15, 9, 50, 1500.0, False, "X3"),
    ("TC09", 15, 12, 50, 1500.0, False, "X4"),
    ("TC10", 15, 10, 10, 1500.0, True, "B11"),
    ("TC11", 15, 10, 200, 1500.0, True, "B15"),
    ("TC12", 15, 10, 9, 1500.0, False, "X5"),
    ("TC13", 15, 10, 201, 1500.0, False, "X6"),
    ("TC14", 15, 10, 50, 100.0, True, "B16"),
    ("TC15", 15, 10, 50, 100000.0, True, "B20"),
    ("TC16", 15, 10, 50, 99.0, False, "X7"),
    ("TC17", 15, 10, 50, 100001.0, False, "X8"),
]

@pytest.mark.parametrize("tc_id,cLen,pLen,aLen,oTot,expected,tag", test_cases_m5)
def test_checkout_validation(tc_id, cLen, pLen, aLen, oTot, expected, tag):
    """Kiểm thử tự động 17 test case đặt hàng theo nguyên lý 4n + 1."""
    assert ValidateCheckout(cLen, pLen, aLen, oTot) == expected

if __name__ == "__main__":
    pytest.main(["-v", __file__])
```

```kết quả test
============================= test session starts =============================
platform win32 -- Python 3.14.0, pytest-8.4.2, pluggy-1.6.0
rootdir: D:\LapTrinhAI\Testing
collected 17 items

test_checkout.py::test_checkout_validation[TC01-15-10-50-1500.0-True-V1, V2, V3, V4, B3, B8, B13, B18] PASSED [  5%]
test_checkout.py::test_checkout_validation[TC02-2-10-50-1500.0-True-B1] PASSED [ 11%]
test_checkout.py::test_checkout_validation[TC03-50-10-50-1500.0-True-B5] PASSED [ 17%]
test_checkout.py::test_checkout_validation[TC04-1-10-50-1500.0-False-X1] PASSED [ 23%]
test_checkout.py::test_checkout_validation[TC05-51-10-50-1500.0-False-X2] PASSED [ 29%]
test_checkout.py::test_checkout_validation[TC06-15-10-50-1500.0-True-B6] PASSED [ 35%]
test_checkout.py::test_checkout_validation[TC07-15-11-50-1500.0-True-B10] PASSED [ 41%]
test_checkout.py::test_checkout_validation[TC08-15-9-50-1500.0-False-X3] PASSED [ 47%]
test_checkout.py::test_checkout_validation[TC09-15-12-50-1500.0-False-X4] PASSED [ 52%]
test_checkout.py::test_checkout_validation[TC10-15-10-10-1500.0-True-B11] PASSED [ 58%]
test_checkout.py::test_checkout_validation[TC11-15-10-200-1500.0-True-B15] PASSED [ 64%]
test_checkout.py::test_checkout_validation[TC12-15-10-9-1500.0-False-X5] PASSED [ 70%]
test_checkout.py::test_checkout_validation[TC13-15-10-201-1500.0-False-X6] PASSED [ 76%]
test_checkout.py::test_checkout_validation[TC14-15-10-50-100.0-True-B16] PASSED [ 82%]
test_checkout.py::test_checkout_validation[TC15-15-10-50-100000.0-True-B20] PASSED [ 88%]
test_checkout.py::test_checkout_validation[TC16-15-10-50-99.0-False-X7] PASSED [ 94%]
test_checkout.py::test_checkout_validation[TC17-15-10-50-100001.0-False-X8] PASSED [100%]

============================= 17 passed in 0.14s ==============================
```
