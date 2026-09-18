# BÀI LÀM: KIỂM THỬ CHỨC NĂNG 5 - QUY TRÌNH THANH TOÁN VÀ ĐẶT HÀNG (CHECKOUT - ORDER PLACEMENT)

- **Họ và tên sinh viên:** Nguyễn Hoàng Phương
- **Mã số sinh viên (MSSV):** 080205010954
- **Môn học:** Kiểm Chứng Phần Mềm
- **Chủ đề:** Phân hoạch lớp tương đương, phân tích giá trị biên, bảng quyết định, chuyển đổi trạng thái, thiết kế test case và kiểm thử tự động

---

## Bảng phân tích điều kiện kiểm thử (Test Conditions)

| Conditions | Valid Partition | Tag | Invalid Partitions | Tag | Valid Boundaries | Tag |
|---|---|---|---|---|---|---|
| **Độ dài tên người nhận** (`nameLength`) | 1 ≤ nameLength ≤ 255 | V1 | • nameLength < 1<br>• nameLength > 255 | X1<br>X2 | • 1 (min)<br>• 2 (min+)<br>• 50 (nominal)<br>• 254 (max-)<br>• 255 (max) | B1<br>B2<br>B3<br>B4<br>B5 |
| **Độ dài địa chỉ giao hàng** (`addressLength`) | 1 ≤ addressLength ≤ 255 | V2 | • addressLength < 1<br>• addressLength > 255 | X3<br>X4 | • 1 (min)<br>• 2 (min+)<br>• 50 (nominal)<br>• 254 (max-)<br>• 255 (max) | B6<br>B7<br>B8<br>B9<br>B10 |
| **Độ dài email khách hàng** (`emailLength`) | 6 ≤ emailLength ≤ 128 | V3 | • emailLength < 6<br>• emailLength > 128 | X5<br>X6 | • 6 (min)<br>• 7 (min+)<br>• 30 (nominal)<br>• 127 (max-)<br>• 128 (max) | B11<br>B12<br>B13<br>B14<br>B15 |
| **Độ dài số điện thoại** (`phoneLength`) | 1 ≤ phoneLength ≤ 20 | V4 | • phoneLength < 1<br>• phoneLength > 20 | X7<br>X8 | • 1 (min)<br>• 2 (min+)<br>• 10 (nominal)<br>• 19 (max-)<br>• 20 (max) | B16<br>B17<br>B18<br>B19<br>B20 |

---

## Câu 1. Xác định lớp tương đương

| Biến đầu vào | Lớp hợp lệ | Tag | Lớp không hợp lệ | Tag |
|---|---|---|---|---|
| **Độ dài tên người nhận** (`nameLength` - ký tự) | 1 ≤ nameLength ≤ 255 | V1 | • nameLength < 1 (Để trống họ tên)<br>• nameLength > 255 (Vượt quá độ dài tối đa) | X1<br>X2 |
| **Độ dài địa chỉ nhận hàng** (`addressLength` - ký tự) | 1 ≤ addressLength ≤ 255 | V2 | • addressLength < 1 (Để trống địa chỉ)<br>• addressLength > 255 (Vượt quá 255 ký tự) | X3<br>X4 |
| **Độ dài email** (`emailLength` - ký tự) | 6 ≤ emailLength ≤ 128 | V3 | • emailLength < 6 (Email quá ngắn/không đúng chuẩn)<br>• emailLength > 128 (Vượt quá độ dài email tối đa) | X5<br>X6 |
| **Độ dài số điện thoại** (`phoneLength` - ký tự) | 1 ≤ phoneLength ≤ 20 | V4 | • phoneLength < 1 (Để trống số điện thoại)<br>• phoneLength > 20 (Vượt quá 20 ký tự chuẩn) | X7<br>X8 |

---

## Câu 2. Phân tích giá trị biên

### 1. Bảng 5 giá trị biên cho từng biến đầu vào

| Biến đầu vào | min | min+ | nominal | max- | max | Tag biên |
|---|---:|---:|---:|---:|---:|---|
| **Độ dài tên người nhận** (`nameLength`) | 1 | 2 | 50 | 254 | 255 | B1, B2, B3, B4, B5 |
| **Độ dài địa chỉ giao hàng** (`addressLength`) | 1 | 2 | 50 | 254 | 255 | B6, B7, B8, B9, B10 |
| **Độ dài email** (`emailLength`) | 6 | 7 | 30 | 127 | 128 | B11, B12, B13, B14, B15 |
| **Độ dài số điện thoại** (`phoneLength`) | 1 | 2 | 10 | 19 | 20 | B16, B17, B18, B19, B20 |

### 2. Bảng 17 test case Standard BVA (Single Fault Assumption: $4n + 1 = 17$)

Theo kỹ thuật Standard Boundary Value Analysis, với $n = 4$ biến đầu vào, số test case là:
$$4n + 1 = 4 \times 4 + 1 = \mathbf{17\text{ test case}}$$

Giữ $n - 1$ biến tại giá trị danh định (`nominal`), lần lượt thay đổi 1 biến qua 4 giá trị biên (`min`, `min+`, `max-`, `max`):

| STT | Mã TC | Biến kiểm thử biên | Điểm biên kiểm tra | Tên người nhận (ký tự) | Địa chỉ giao (ký tự) | Email (ký tự) | Số điện thoại (ký tự) | Kết quả mong đợi | Tag bao phủ |
|:---:|:---:|:---|:---|:---:|:---:|:---:|:---:|:---|:---|
| 1 | BVA01 | Baseline (Tất cả) | Nominal | 50 | 50 | 30 | 10 | Hợp lệ (True) | B3, B8, B13, B18 |
| 2 | BVA02 | Độ dài tên người nhận | min (1) | **1** | 50 | 30 | 10 | Hợp lệ (True) | B1 |
| 3 | BVA03 | Độ dài tên người nhận | min+ (2) | **2** | 50 | 30 | 10 | Hợp lệ (True) | B2 |
| 4 | BVA04 | Độ dài tên người nhận | max- (254) | **254** | 50 | 30 | 10 | Hợp lệ (True) | B4 |
| 5 | BVA05 | Độ dài tên người nhận | max (255) | **255** | 50 | 30 | 10 | Hợp lệ (True) | B5 |
| 6 | BVA06 | Độ dài địa chỉ giao | min (1) | 50 | **1** | 30 | 10 | Hợp lệ (True) | B6 |
| 7 | BVA07 | Độ dài địa chỉ giao | min+ (2) | 50 | **2** | 30 | 10 | Hợp lệ (True) | B7 |
| 8 | BVA08 | Độ dài địa chỉ giao | max- (254) | 50 | **254** | 30 | 10 | Hợp lệ (True) | B9 |
| 9 | BVA09 | Độ dài địa chỉ giao | max (255) | 50 | **255** | 30 | 10 | Hợp lệ (True) | B10 |
| 10 | BVA10 | Độ dài email | min (6) | 50 | 50 | **6** | 10 | Hợp lệ (True) | B11 |
| 11 | BVA11 | Độ dài email | min+ (7) | 50 | 50 | **7** | 10 | Hợp lệ (True) | B12 |
| 12 | BVA12 | Độ dài email | max- (127) | 50 | 50 | **127** | 10 | Hợp lệ (True) | B14 |
| 13 | BVA13 | Độ dài email | max (128) | 50 | 50 | **128** | 10 | Hợp lệ (True) | B15 |
| 14 | BVA14 | Độ dài số điện thoại | min (1) | 50 | 50 | 30 | **1** | Hợp lệ (True) | B16 |
| 15 | BVA15 | Độ dài số điện thoại | min+ (2) | 50 | 50 | 30 | **2** | Hợp lệ (True) | B17 |
| 16 | BVA16 | Độ dài số điện thoại | max- (19) | 50 | 50 | 30 | **19** | Hợp lệ (True) | B19 |
| 17 | BVA17 | Độ dài số điện thoại | max (20) | 50 | 50 | 30 | **20** | Hợp lệ (True) | B20 |

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
| TC01 | nameLength: 50, addressLength: 50, emailLength: 30, phoneLength: 10 | Hợp lệ (True) | V1, V2, V3, V4, B3, B8, B13, B18 |
| TC02 | nameLength: 1, addressLength: 50, emailLength: 30, phoneLength: 10 | Hợp lệ (True) | B1 |
| TC03 | nameLength: 255, addressLength: 50, emailLength: 30, phoneLength: 10 | Hợp lệ (True) | B5 |
| TC04 | nameLength: 0, addressLength: 50, emailLength: 30, phoneLength: 10 | Không hợp lệ (False): Tên người nhận bị để trống (length < 1) | X1 |
| TC05 | nameLength: 256, addressLength: 50, emailLength: 30, phoneLength: 10 | Không hợp lệ (False): Tên người nhận vượt quá 255 ký tự | X2 |
| TC06 | nameLength: 50, addressLength: 1, emailLength: 30, phoneLength: 10 | Hợp lệ (True) | B6 |
| TC07 | nameLength: 50, addressLength: 255, emailLength: 30, phoneLength: 10 | Hợp lệ (True) | B10 |
| TC08 | nameLength: 50, addressLength: 0, emailLength: 30, phoneLength: 10 | Không hợp lệ (False): Địa chỉ giao hàng bị để trống (length < 1) | X3 |
| TC09 | nameLength: 50, addressLength: 256, emailLength: 30, phoneLength: 10 | Không hợp lệ (False): Địa chỉ giao hàng vượt quá 255 ký tự | X4 |
| TC10 | nameLength: 50, addressLength: 50, emailLength: 6, phoneLength: 10 | Hợp lệ (True) | B11 |
| TC11 | nameLength: 50, addressLength: 50, emailLength: 128, phoneLength: 10 | Hợp lệ (True) | B15 |
| TC12 | nameLength: 50, addressLength: 50, emailLength: 5, phoneLength: 10 | Không hợp lệ (False): Email quá ngắn (dưới 6 ký tự) | X5 |
| TC13 | nameLength: 50, addressLength: 50, emailLength: 129, phoneLength: 10 | Không hợp lệ (False): Email vượt quá 128 ký tự | X6 |
| TC14 | nameLength: 50, addressLength: 50, emailLength: 30, phoneLength: 1 | Hợp lệ (True) | B16 |
| TC15 | nameLength: 50, addressLength: 50, emailLength: 30, phoneLength: 20 | Hợp lệ (True) | B20 |
| TC16 | nameLength: 50, addressLength: 50, emailLength: 30, phoneLength: 0 | Không hợp lệ (False): Số điện thoại bị để trống (length < 1) | X7 |
| TC17 | nameLength: 50, addressLength: 50, emailLength: 30, phoneLength: 21 | Không hợp lệ (False): Số điện thoại vượt quá 20 ký tự | X8 |

### 2. Bảng test case chi tiết theo đề bài (8 cột)

| STT | Tên test case | Họ tên (ký tự) | Địa chỉ (ký tự) | Email (ký tự) | SĐT (ký tự) | Kết quả mong đợi | Tag được bao phủ |
|:---:|:---|:---:|:---:|:---:|:---:|:---|:---|
| 1 | Baseline danh định (nominal) | 50 | 50 | 30 | 10 | Hợp lệ (True) | V1, V2, V3, V4, B3, B8, B13, B18 |
| 2 | Biên dưới hợp lệ nameLength = min (1) | **1** | 50 | 30 | 10 | Hợp lệ (True) | B1 |
| 3 | Biên trên hợp lệ nameLength = max (255) | **255** | 50 | 30 | 10 | Hợp lệ (True) | B5 |
| 4 | Ngoài biên dưới nameLength < min (0) | **0** | 50 | 30 | 10 | Không hợp lệ (False): Tên người nhận bị để trống | X1 |
| 5 | Ngoài biên trên nameLength > max (256) | **256** | 50 | 30 | 10 | Không hợp lệ (False): Tên người nhận vượt quá 255 ký tự | X2 |
| 6 | Biên dưới hợp lệ addressLength = min (1) | 50 | **1** | 30 | 10 | Hợp lệ (True) | B6 |
| 7 | Biên trên hợp lệ addressLength = max (255) | 50 | **255** | 30 | 10 | Hợp lệ (True) | B10 |
| 8 | Ngoài biên dưới addressLength < min (0) | 50 | **0** | 30 | 10 | Không hợp lệ (False): Địa chỉ giao hàng bị để trống | X3 |
| 9 | Ngoài biên trên addressLength > max (256) | 50 | **256** | 30 | 10 | Không hợp lệ (False): Địa chỉ giao hàng vượt quá 255 ký tự | X4 |
| 10 | Biên dưới hợp lệ emailLength = min (6) | 50 | 50 | **6** | 10 | Hợp lệ (True) | B11 |
| 11 | Biên trên hợp lệ emailLength = max (128) | 50 | 50 | **128** | 10 | Hợp lệ (True) | B15 |
| 12 | Ngoài biên dưới emailLength < min (5) | 50 | 50 | **5** | 10 | Không hợp lệ (False): Email dưới 6 ký tự | X5 |
| 13 | Ngoài biên trên emailLength > max (129) | 50 | 50 | **129** | 10 | Không hợp lệ (False): Email vượt quá 128 ký tự | X6 |
| 14 | Biên dưới hợp lệ phoneLength = min (1) | 50 | 50 | 30 | **1** | Hợp lệ (True) | B16 |
| 15 | Biên trên hợp lệ phoneLength = max (20) | 50 | 50 | 30 | **20** | Hợp lệ (True) | B20 |
| 16 | Ngoài biên dưới phoneLength < min (0) | 50 | 50 | 30 | **0** | Không hợp lệ (False): Số điện thoại bị để trống | X7 |
| 17 | Ngoài biên trên phoneLength > max (21) | 50 | 50 | 30 | **21** | Không hợp lệ (False): Số điện thoại vượt quá 20 ký tự | X8 |

---

### 3. Bổ sung: Ma trận Bảng quyết định rút gọn (Collapsed Decision Table - 7 Rules) & Chuyển đổi trạng thái

| Condition/Action | R1 (Lỗi Cart) | R2 (Lỗi Form) | R3 (Lỗi Line) | R4 (Lỗi SP) | R5 (Lỗi Kho) | R6 (Lỗi Mã) | R7 (Hoàn hảo) |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| **C1: Giỏ hàng tồn tại và có sản phẩm?** | **N** | Y | Y | Y | Y | Y | Y |
| **C2: Form giao hàng hợp lệ (Tên, Đ/c, Email, SĐT)?** | - | **N** | Y | Y | Y | Y | Y |
| **C3: CartLine hợp lệ?** | - | - | **N** | Y | Y | Y | Y |
| **C4: Sản phẩm ACTIVE và tồn tại?** | - | - | - | **N** | Y | Y | Y |
| **C5: Tồn kho đủ số lượng đặt hàng?** | - | - | - | - | **N** | Y | Y |
| **C6: Mã Voucher hợp lệ / Không dùng mã?** | - | - | - | - | - | **N** | **Y** |
| *A1: Từ chối Checkout, chuyển về giỏ hàng* | **X** | - | - | - | - | - | - |
| *A2: Báo lỗi validation, yêu cầu nhập lại form* | - | **X** | - | - | - | - | - |
| *A3: Từ chối dòng hàng không hợp lệ* | - | - | **X** | - | - | - | - |
| *A4: Báo sản phẩm ngừng kinh doanh* | - | - | - | **X** | - | - | - |
| *A5: Báo thiếu kho, giữ nguyên giỏ hàng* | - | - | - | - | **X** | - | - |
| *A6: Từ chối voucher, giữ nguyên giỏ hàng* | - | - | - | - | - | **X** | - |
| *A7: Tạo đơn hàng thành công, trừ tồn kho (HTTP 200)* | - | - | - | - | - | - | **X** |

---

## Câu 4. Triển khai kiểm thử tự động

```python
def ValidateCustomerCheckout(nameLength: int, addressLength: int, emailLength: int, phoneLength: int) -> bool:
    """
    Kiểm tra tính hợp lệ của thông tin khách hàng khi thanh toán (Checkout Form):
    - 1 <= nameLength <= 255 (Độ dài tên người nhận)
    - 1 <= addressLength <= 255 (Độ dài địa chỉ giao hàng)
    - 6 <= emailLength <= 128 (Độ dài email khách hàng)
    - 1 <= phoneLength <= 20 (Độ dài số điện thoại liên hệ)
    Trả về True nếu tất cả điều kiện thỏa mãn, ngược lại False.
    """
    if not (isinstance(nameLength, int) and not isinstance(nameLength, bool) and 1 <= nameLength <= 255):
        return False
    if not (isinstance(addressLength, int) and not isinstance(addressLength, bool) and 1 <= addressLength <= 255):
        return False
    if not (isinstance(emailLength, int) and not isinstance(emailLength, bool) and 6 <= emailLength <= 128):
        return False
    if not (isinstance(phoneLength, int) and not isinstance(phoneLength, bool) and 1 <= phoneLength <= 20):
        return False
    return True
```

```pytest
# thiết kế các test cases từ câu 3.
# Run test case 
import pytest

test_cases_checkout = [
    # TC01: Baseline nominal (Tất cả biến tại giá trị danh định)
    ("TC01", 50, 50, 30, 10, True, "V1, V2, V3, V4, B3, B8, B13, B18"),
    
    # TC02 - TC05: Biên biến nameLength (min, max, min-1, max+1)
    ("TC02", 1, 50, 30, 10, True, "B1"),
    ("TC03", 255, 50, 30, 10, True, "B5"),
    ("TC04", 0, 50, 30, 10, False, "X1"),
    ("TC05", 256, 50, 30, 10, False, "X2"),
    
    # TC06 - TC09: Biên biến addressLength (min, max, min-1, max+1)
    ("TC06", 50, 1, 30, 10, True, "B6"),
    ("TC07", 50, 255, 30, 10, True, "B10"),
    ("TC08", 50, 0, 30, 10, False, "X3"),
    ("TC09", 50, 256, 30, 10, False, "X4"),
    
    # TC10 - TC13: Biên biến emailLength (min, max, min-1, max+1)
    ("TC10", 50, 50, 6, 10, True, "B11"),
    ("TC11", 50, 50, 128, 10, True, "B15"),
    ("TC12", 50, 50, 5, 10, False, "X5"),
    ("TC13", 50, 50, 129, 10, False, "X6"),
    
    # TC14 - TC17: Biên biến phoneLength (min, max, min-1, max+1)
    ("TC14", 50, 50, 30, 1, True, "B16"),
    ("TC15", 50, 50, 30, 20, True, "B20"),
    ("TC16", 50, 50, 30, 0, False, "X7"),
    ("TC17", 50, 50, 30, 21, False, "X8"),
]

@pytest.mark.parametrize("tc_id,nameLength,addressLength,emailLength,phoneLength,expected,tag", test_cases_checkout)
def test_checkout_validation(tc_id, nameLength, addressLength, emailLength, phoneLength, expected, tag):
    """Kiểm thử tự động 17 test case thiết kế cho Phân hệ 5 Checkout theo nguyên lý 4n + 1."""
    assert ValidateCustomerCheckout(nameLength, addressLength, emailLength, phoneLength) == expected

if __name__ == "__main__":
    pytest.main(["-v", __file__])
```

```kết quả test
============================= test session starts =============================
platform win32 -- Python 3.14.0, pytest-8.4.2, pluggy-1.6.0
rootdir: D:\LapTrinhAI\Testing
plugins: anyio-4.12.1, Faker-40.1.2, asyncio-0.26.0, cov-7.1.0
asyncio: mode=Mode.STRICT, asyncio_default_fixture_loop_scope=None, asyncio_default_test_loop_scope=function
collected 17 items

scripts/test_checkout_standard_bva.py::test_checkout_validation[TC01-50-50-30-10-True-V1, V2, V3, V4, B3, B8, B13, B18] PASSED [  5%]
scripts/test_checkout_standard_bva.py::test_checkout_validation[TC02-1-50-30-10-True-B1] PASSED [ 11%]
scripts/test_checkout_standard_bva.py::test_checkout_validation[TC03-255-50-30-10-True-B5] PASSED [ 17%]
scripts/test_checkout_standard_bva.py::test_checkout_validation[TC04-0-50-30-10-False-X1] PASSED [ 23%]
scripts/test_checkout_standard_bva.py::test_checkout_validation[TC05-256-50-30-10-False-X2] PASSED [ 29%]
scripts/test_checkout_standard_bva.py::test_checkout_validation[TC06-50-1-30-10-True-B6] PASSED [ 35%]
scripts/test_checkout_standard_bva.py::test_checkout_validation[TC07-50-255-30-10-True-B10] PASSED [ 41%]
scripts/test_checkout_standard_bva.py::test_checkout_validation[TC08-50-0-30-10-False-X3] PASSED [ 47%]
scripts/test_checkout_standard_bva.py::test_checkout_validation[TC09-50-256-30-10-False-X4] PASSED [ 52%]
scripts/test_checkout_standard_bva.py::test_checkout_validation[TC10-50-50-6-10-True-B11] PASSED [ 58%]
scripts/test_checkout_standard_bva.py::test_checkout_validation[TC11-50-50-128-10-True-B15] PASSED [ 64%]
scripts/test_checkout_standard_bva.py::test_checkout_validation[TC12-50-50-5-10-False-X5] PASSED [ 70%]
scripts/test_checkout_standard_bva.py::test_checkout_validation[TC13-50-50-129-10-False-X6] PASSED [ 76%]
scripts/test_checkout_standard_bva.py::test_checkout_validation[TC14-50-50-30-1-True-B16] PASSED [ 82%]
scripts/test_checkout_standard_bva.py::test_checkout_validation[TC15-50-50-30-20-True-B20] PASSED [ 88%]
scripts/test_checkout_standard_bva.py::test_checkout_validation[TC16-50-50-30-0-False-X7] PASSED [ 94%]
scripts/test_checkout_standard_bva.py::test_checkout_validation[TC17-50-50-30-21-False-X8] PASSED [100%]

============================= 17 passed in 0.14s ==============================
```
