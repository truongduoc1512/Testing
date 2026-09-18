# BÀI LÀM: KIỂM THỬ CHỨC NĂNG 3 - QUẢN LÝ GIỎ HÀNG (SHOPPING CART)

- **Họ và tên sinh viên:** Nguyễn Hoàng Phương
- **Mã số sinh viên (MSSV):** 080205010954
- **Môn học:** Kiểm Chứng Phần Mềm
- **Chủ đề:** Phân hoạch lớp tương đương, phân tích giá trị biên, bảng quyết định, chuyển đổi trạng thái, thiết kế test case và kiểm thử tự động

---

## Bảng phân tích điều kiện kiểm thử (Test Conditions)

| Conditions | Valid Partition | Tag | Invalid Partitions | Tag | Valid Boundaries | Tag |
|---|---|---|---|---|---|---|
| **Số lượng sản phẩm** (`quantity`) | 1 ≤ quantity ≤ 99 | V1 | • quantity < 1<br>• quantity > 99 | X1<br>X2 | • 1 (min)<br>• 2 (min+)<br>• 2 (nominal)<br>• 98 (max-)<br>• 99 (max) | B1<br>B2<br>B3<br>B4<br>B5 |
| **Số mặt hàng trong giỏ** (`itemCount`) | 1 ≤ itemCount ≤ 20 | V2 | • itemCount < 1<br>• itemCount > 20 | X3<br>X4 | • 1 (min)<br>• 2 (min+)<br>• 5 (nominal)<br>• 19 (max-)<br>• 20 (max) | B6<br>B7<br>B8<br>B9<br>B10 |
| **Tồn kho khả dụng** (`stockQuantity`) | 1 ≤ stockQuantity ≤ 500 | V3 | • stockQuantity < 1<br>• stockQuantity > 500 | X5<br>X6 | • 1 (min)<br>• 2 (min+)<br>• 100 (nominal)<br>• 499 (max-)<br>• 500 (max) | B11<br>B12<br>B13<br>B14<br>B15 |
| **Tổng tiền tạm tính** (`subtotal` - k) | 100.0 ≤ subtotal ≤ 100000.0 | V4 | • subtotal < 100.0<br>• subtotal > 100000.0 | X7<br>X8 | • 100.0 (min)<br>• 101.0 (min+)<br>• 1500.0 (nominal)<br>• 99999.0 (max-)<br>• 100000.0 (max) | B16<br>B17<br>B18<br>B19<br>B20 |

---

## Câu 1. Xác định lớp tương đương

| Biến đầu vào | Lớp hợp lệ | Tag | Lớp không hợp lệ | Tag |
|---|---|---|---|---|
| **Số lượng sản phẩm** (`quantity`) | 1 ≤ quantity ≤ 99 | V1 | • quantity < 1 (Số lượng không hợp lệ)<br>• quantity > 99 (Vượt quá số lượng cho phép) | X1<br>X2 |
| **Số mặt hàng trong giỏ** (`itemCount`) | 1 ≤ itemCount ≤ 20 | V2 | • itemCount < 1 (Giỏ hàng rỗng)<br>• itemCount > 20 (Vượt giới hạn giỏ) | X3<br>X4 |
| **Tồn kho khả dụng** (`stockQuantity`) | 1 ≤ stockQuantity ≤ 500 | V3 | • stockQuantity < 1 (Hết hàng trong kho)<br>• stockQuantity > 500 (Vượt mức kho) | X5<br>X6 |
| **Tổng tiền tạm tính** (`subtotal`) | 100.0 ≤ subtotal ≤ 100000.0 | V4 | • subtotal < 100.0 (Chưa đạt mức tối thiểu)<br>• subtotal > 100000.0 (Vượt trần giao dịch) | X7<br>X8 |

---

## Câu 2. Phân tích giá trị biên

### 1. Bảng 5 giá trị biên cho từng biến đầu vào

| Biến đầu vào | min | min+ | nominal | max- | max | Tag biên |
|---|---:|---:|---:|---:|---:|---|
| **Số lượng sản phẩm** (`quantity`) | 1 | 2 | 2 | 98 | 99 | B1, B2, B3, B4, B5 |
| **Số mặt hàng trong giỏ** (`itemCount`) | 1 | 2 | 5 | 19 | 20 | B6, B7, B8, B9, B10 |
| **Tồn kho khả dụng** (`stockQuantity`) | 1 | 2 | 100 | 499 | 500 | B11, B12, B13, B14, B15 |
| **Tổng tiền tạm tính** (`subtotal`) | 100.0 | 101.0 | 1500.0 | 99999.0 | 100000.0 | B16, B17, B18, B19, B20 |

### 2. Bảng 17 test case Standard BVA (Single Fault Assumption: $4n + 1 = 17$)

Theo kỹ thuật Standard Boundary Value Analysis, với $n = 4$ biến đầu vào, số test case là:
$$4n + 1 = 4 \times 4 + 1 = \mathbf{17\text{ test case}}$$

Giữ $n - 1$ biến tại giá trị danh định (`nominal`), lần lượt thay đổi 1 biến qua 4 giá trị biên (`min`, `min+`, `max-`, `max`):

| STT | Mã TC | Biến kiểm thử biên | Điểm biên kiểm tra | Số lượng SP | Số mặt hàng | Tồn kho | Tạm tính (k) | Kết quả mong đợi | Tag bao phủ |
|:---:|:---:|:---|:---|:---:|:---:|:---:|:---:|:---|:---|
| 1 | BVA01 | Baseline (Tất cả) | Nominal | 2 | 5 | 100 | 1500.0 | Hợp lệ (True) | B3, B8, B13, B18 |
| 2 | BVA02 | Số lượng sản phẩm | min (1) | **1** | 5 | 100 | 1500.0 | Hợp lệ (True) | B1 |
| 3 | BVA03 | Số lượng sản phẩm | max (99) | **99** | 5 | 100 | 1500.0 | Hợp lệ (True) | B5 |
| 4 | BVA04 | Số lượng sản phẩm | min-1 (0) | **0** | 5 | 100 | 1500.0 | Không hợp lệ (False) | X1 |
| 5 | BVA05 | Số lượng sản phẩm | max+1 (100) | **100** | 5 | 100 | 1500.0 | Không hợp lệ (False) | X2 |
| 6 | BVA06 | Số mặt hàng giỏ | min (1) | 2 | **1** | 100 | 1500.0 | Hợp lệ (True) | B6 |
| 7 | BVA07 | Số mặt hàng giỏ | max (20) | 2 | **20** | 100 | 1500.0 | Hợp lệ (True) | B10 |
| 8 | BVA08 | Số mặt hàng giỏ | min-1 (0) | 2 | **0** | 100 | 1500.0 | Không hợp lệ (False) | X3 |
| 9 | BVA09 | Số mặt hàng giỏ | max+1 (21) | 2 | **21** | 100 | 1500.0 | Không hợp lệ (False) | X4 |
| 10 | BVA10 | Tồn kho khả dụng | min (1) | 2 | 5 | **1** | 1500.0 | Hợp lệ (True) | B11 |
| 11 | BVA11 | Tồn kho khả dụng | max (500) | 2 | 5 | **500** | 1500.0 | Hợp lệ (True) | B15 |
| 12 | BVA12 | Tồn kho khả dụng | min-1 (0) | 2 | 5 | **0** | 1500.0 | Không hợp lệ (False) | X5 |
| 13 | BVA13 | Tồn kho khả dụng | max+1 (501) | 2 | 5 | **501** | 1500.0 | Không hợp lệ (False) | X6 |
| 14 | BVA14 | Tạm tính | min (100.0) | 2 | 5 | 100 | **100.0** | Hợp lệ (True) | B16 |
| 15 | BVA15 | Tạm tính | max (100000.0) | 2 | 5 | 100 | **100000.0** | Hợp lệ (True) | B20 |
| 16 | BVA16 | Tạm tính | min-1 (99.0) | 2 | 5 | 100 | **99.0** | Không hợp lệ (False) | X7 |
| 17 | BVA17 | Tạm tính | max+1 (100001.0) | 2 | 5 | 100 | **100001.0** | Không hợp lệ (False) | X8 |

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
| TC01 | quantity: 2, itemCount: 5, stockQuantity: 100, subtotal: 1500.0 | Hợp lệ (True) | V1, V2, V3, V4, B3, B8, B13, B18 |
| TC02 | quantity: 1, itemCount: 5, stockQuantity: 100, subtotal: 1500.0 | Hợp lệ (True) | B1 |
| TC03 | quantity: 99, itemCount: 5, stockQuantity: 100, subtotal: 1500.0 | Hợp lệ (True) | B5 |
| TC04 | quantity: 0, itemCount: 5, stockQuantity: 100, subtotal: 1500.0 | Không hợp lệ (False): Số lượng mua nhỏ hơn 1 | X1 |
| TC05 | quantity: 100, itemCount: 5, stockQuantity: 100, subtotal: 1500.0 | Không hợp lệ (False): Số lượng mua lớn hơn 99 | X2 |
| TC06 | quantity: 2, itemCount: 1, stockQuantity: 100, subtotal: 1500.0 | Hợp lệ (True) | B6 |
| TC07 | quantity: 2, itemCount: 20, stockQuantity: 100, subtotal: 1500.0 | Hợp lệ (True) | B10 |
| TC08 | quantity: 2, itemCount: 0, stockQuantity: 100, subtotal: 1500.0 | Không hợp lệ (False): Giỏ hàng không có sản phẩm | X3 |
| TC09 | quantity: 2, itemCount: 21, stockQuantity: 100, subtotal: 1500.0 | Không hợp lệ (False): Số loại sản phẩm vượt quá 20 | X4 |
| TC10 | quantity: 2, itemCount: 5, stockQuantity: 1, subtotal: 1500.0 | Hợp lệ (True) | B11 |
| TC11 | quantity: 2, itemCount: 5, stockQuantity: 500, subtotal: 1500.0 | Hợp lệ (True) | B15 |
| TC12 | quantity: 2, itemCount: 5, stockQuantity: 0, subtotal: 1500.0 | Không hợp lệ (False): Tồn kho đã hết (0) | X5 |
| TC13 | quantity: 2, itemCount: 5, stockQuantity: 501, subtotal: 1500.0 | Không hợp lệ (False): Tồn kho vượt quá 500 | X6 |
| TC14 | quantity: 2, itemCount: 5, stockQuantity: 100, subtotal: 100.0 | Hợp lệ (True) | B16 |
| TC15 | quantity: 2, itemCount: 5, stockQuantity: 100, subtotal: 100000.0 | Hợp lệ (True) | B20 |
| TC16 | quantity: 2, itemCount: 5, stockQuantity: 100, subtotal: 99.0 | Không hợp lệ (False): Tạm tính nhỏ hơn 100.0k | X7 |
| TC17 | quantity: 2, itemCount: 5, stockQuantity: 100, subtotal: 100001.0 | Không hợp lệ (False): Tạm tính vượt quá 100000.0k | X8 |

### 2. Bảng test case chi tiết theo đề bài (8 cột)

| STT | Tên test case | Số lượng SP | Số mặt hàng | Tồn kho khả dụng | Tạm tính (k) | Kết quả mong đợi | Tag được bao phủ |
|:---:|:---|:---:|:---:|:---:|:---:|:---|:---|
| 1 | Baseline danh định (nominal) | 2 | 5 | 100 | 1500.0 | Hợp lệ (True) | V1, V2, V3, V4, B3, B8, B13, B18 |
| 2 | Biên dưới hợp lệ quantity = min (1) | **1** | 5 | 100 | 1500.0 | Hợp lệ (True) | B1 |
| 3 | Biên trên hợp lệ quantity = max (99) | **99** | 5 | 100 | 1500.0 | Hợp lệ (True) | B5 |
| 4 | Ngoài biên dưới quantity < min (0) | **0** | 5 | 100 | 1500.0 | Không hợp lệ (False): Số lượng mua nhỏ hơn 1 | X1 |
| 5 | Ngoài biên trên quantity > max (100) | **100** | 5 | 100 | 1500.0 | Không hợp lệ (False): Số lượng mua lớn hơn 99 | X2 |
| 6 | Biên dưới hợp lệ itemCount = min (1) | 2 | **1** | 100 | 1500.0 | Hợp lệ (True) | B6 |
| 7 | Biên trên hợp lệ itemCount = max (20) | 2 | **20** | 100 | 1500.0 | Hợp lệ (True) | B10 |
| 8 | Ngoài biên dưới itemCount < min (0) | 2 | **0** | 100 | 1500.0 | Không hợp lệ (False): Giỏ hàng không có sản phẩm | X3 |
| 9 | Ngoài biên trên itemCount > max (21) | 2 | **21** | 100 | 1500.0 | Không hợp lệ (False): Số loại sản phẩm vượt quá 20 | X4 |
| 10 | Biên dưới hợp lệ stockQuantity = min (1) | 2 | 5 | **1** | 1500.0 | Hợp lệ (True) | B11 |
| 11 | Biên trên hợp lệ stockQuantity = max (500) | 2 | 5 | **500** | 1500.0 | Hợp lệ (True) | B15 |
| 12 | Ngoài biên dưới stockQuantity < min (0) | 2 | 5 | **0** | 1500.0 | Không hợp lệ (False): Tồn kho đã hết (0) | X5 |
| 13 | Ngoài biên trên stockQuantity > max (501) | 2 | 5 | **501** | 1500.0 | Không hợp lệ (False): Tồn kho vượt quá 500 | X6 |
| 14 | Biên dưới hợp lệ subtotal = min (100.0) | 2 | 5 | 100 | **100.0** | Hợp lệ (True) | B16 |
| 15 | Biên trên hợp lệ subtotal = max (100000.0) | 2 | 5 | 100 | **100000.0** | Hợp lệ (True) | B20 |
| 16 | Ngoài biên dưới subtotal < min (99.0) | 2 | 5 | 100 | **99.0** | Không hợp lệ (False): Tạm tính nhỏ hơn 100.0k | X7 |
| 17 | Ngoài biên trên subtotal > max (100001.0) | 2 | 5 | 100 | **100001.0** | Không hợp lệ (False): Tạm tính vượt quá 100000.0k | X8 |

---

### 3. Bổ sung: Bảng Quyết định giỏ hàng (Collapsed Decision Table - 4 Rules)

| Condition / Action | Rule 1 (R1) | Rule 2 (R2) | Rule 3 (R3) | Rule 4 (R4) |
| :--- | :---: | :---: | :---: | :---: |
| **C1: Sản phẩm tồn tại trong CSDL?** | **N** | Y | Y | Y |
| **C2: Số lượng mua <= Tồn kho?** | - | **N** | Y | Y |
| **C3: Tổng mặt hàng giỏ <= 20?** | - | - | **N** | **Y** |
| *A1: Báo lỗi "Không tìm thấy sản phẩm"* | **X** | - | - | - |
| *A2: Báo lỗi "Vượt quá tồn kho khả dụng"* | - | **X** | - | - |
| *A3: Báo lỗi "Giỏ hàng đã đầy (tối đa 20 món)"* | - | - | **X** | - |
| *A4: Cập nhật giỏ hàng thành công (HTTP 200)* | - | - | - | **X** |

---

## Câu 4. Triển khai kiểm thử tự động

```python
def ValidateShoppingCart(quantity: int, itemCount: int, stockQuantity: int, subtotal: float) -> bool:
    """
    Kiểm tra tính hợp lệ của giỏ hàng:
    - 1 <= quantity <= 99 (Số lượng mua mỗi món)
    - 1 <= itemCount <= 20 (Số loại mặt hàng trong giỏ)
    - 1 <= stockQuantity <= 500 (Tồn kho khả dụng)
    - 100.0 <= subtotal <= 100000.0 (Tổng tiền tạm tính)
    Trả về True nếu tất cả điều kiện thỏa mãn, ngược lại False.
    """
    if not (isinstance(quantity, int) and not isinstance(quantity, bool) and 1 <= quantity <= 99):
        return False
    if not (isinstance(itemCount, int) and not isinstance(itemCount, bool) and 1 <= itemCount <= 20):
        return False
    if not (isinstance(stockQuantity, int) and not isinstance(stockQuantity, bool) and 1 <= stockQuantity <= 500):
        return False
    if not (isinstance(subtotal, (int, float)) and not isinstance(subtotal, bool) and 100.0 <= subtotal <= 100000.0):
        return False
    return True
```

```pytest
# thiết kế các test cases từ câu 3.
# Run test case 
import pytest

test_cases_m3 = [
    ("TC01", 2, 5, 100, 1500.0, True, "V1, V2, V3, V4, B3, B8, B13, B18"),
    ("TC02", 1, 5, 100, 1500.0, True, "B1"),
    ("TC03", 99, 5, 100, 1500.0, True, "B5"),
    ("TC04", 0, 5, 100, 1500.0, False, "X1"),
    ("TC05", 100, 5, 100, 1500.0, False, "X2"),
    ("TC06", 2, 1, 100, 1500.0, True, "B6"),
    ("TC07", 2, 20, 100, 1500.0, True, "B10"),
    ("TC08", 2, 0, 100, 1500.0, False, "X3"),
    ("TC09", 2, 21, 100, 1500.0, False, "X4"),
    ("TC10", 2, 5, 1, 1500.0, True, "B11"),
    ("TC11", 2, 5, 500, 1500.0, True, "B15"),
    ("TC12", 2, 5, 0, 1500.0, False, "X5"),
    ("TC13", 2, 5, 501, 1500.0, False, "X6"),
    ("TC14", 2, 5, 100, 100.0, True, "B16"),
    ("TC15", 2, 5, 100, 100000.0, True, "B20"),
    ("TC16", 2, 5, 100, 99.0, False, "X7"),
    ("TC17", 2, 5, 100, 100001.0, False, "X8"),
]

@pytest.mark.parametrize("tc_id,qty,iCount,sQty,subt,expected,tag", test_cases_m3)
def test_cart_validation(tc_id, qty, iCount, sQty, subt, expected, tag):
    """Kiểm thử tự động 17 test case giỏ hàng theo nguyên lý 4n + 1."""
    assert ValidateShoppingCart(qty, iCount, sQty, subt) == expected

if __name__ == "__main__":
    pytest.main(["-v", __file__])
```

```kết quả test
============================= test session starts =============================
platform win32 -- Python 3.14.0, pytest-8.4.2, pluggy-1.6.0
rootdir: D:\LapTrinhAI\Testing
collected 17 items

test_shopping_cart.py::test_cart_validation[TC01-2-5-100-1500.0-True-V1, V2, V3, V4, B3, B8, B13, B18] PASSED [  5%]
test_shopping_cart.py::test_cart_validation[TC02-1-5-100-1500.0-True-B1] PASSED [ 11%]
test_shopping_cart.py::test_cart_validation[TC03-99-5-100-1500.0-True-B5] PASSED [ 17%]
test_shopping_cart.py::test_cart_validation[TC04-0-5-100-1500.0-False-X1] PASSED [ 23%]
test_shopping_cart.py::test_cart_validation[TC05-100-5-100-1500.0-False-X2] PASSED [ 29%]
test_shopping_cart.py::test_cart_validation[TC06-2-1-100-1500.0-True-B6] PASSED [ 35%]
test_shopping_cart.py::test_cart_validation[TC07-2-20-100-1500.0-True-B10] PASSED [ 41%]
test_shopping_cart.py::test_cart_validation[TC08-2-0-100-1500.0-False-X3] PASSED [ 47%]
test_shopping_cart.py::test_cart_validation[TC09-2-21-100-1500.0-False-X4] PASSED [ 52%]
test_shopping_cart.py::test_cart_validation[TC10-2-5-1-1500.0-True-B11] PASSED [ 58%]
test_shopping_cart.py::test_cart_validation[TC11-2-5-500-1500.0-True-B15] PASSED [ 64%]
test_shopping_cart.py::test_cart_validation[TC12-2-5-0-1500.0-False-X5] PASSED [ 70%]
test_shopping_cart.py::test_cart_validation[TC13-2-5-501-1500.0-False-X6] PASSED [ 76%]
test_shopping_cart.py::test_cart_validation[TC14-2-5-100-100.0-True-B16] PASSED [ 82%]
test_shopping_cart.py::test_cart_validation[TC15-2-5-100-100000.0-True-B20] PASSED [ 88%]
test_shopping_cart.py::test_cart_validation[TC16-2-5-100-99.0-False-X7] PASSED [ 94%]
test_shopping_cart.py::test_cart_validation[TC17-2-5-100-100001.0-False-X8] PASSED [100%]

============================= 17 passed in 0.14s ==============================
```
