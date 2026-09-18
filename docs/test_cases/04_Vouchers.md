# BÀI LÀM: KIỂM THỬ CHỨC NĂNG 4 - QUẢN LÝ VÀ ÁP DỤNG MÃ GIẢM GIÁ (VOUCHERS)

- **Họ và tên sinh viên:** Nguyễn Hoàng Phương
- **Mã số sinh viên (MSSV):** 080205010954
- **Môn học:** Kiểm Chứng Phần Mềm
- **Chủ đề:** Phân hoạch lớp tương đương, phân tích giá trị biên, bảng quyết định, chuyển đổi trạng thái, thiết kế test case và kiểm thử tự động

---

## Bảng phân tích điều kiện kiểm thử (Test Conditions)

| Conditions | Valid Partition | Tag | Invalid Partitions | Tag | Valid Boundaries | Tag |
|---|---|---|---|---|---|---|
| **Giá trị đơn hàng** (`orderAmount`) | 500.0 ≤ orderAmount ≤ 50000.0 | V1 | • orderAmount < 500.0<br>• orderAmount > 50000.0 | X1<br>X2 | • 500.0 (min)<br>• 501.0 (min+)<br>• 25000.0 (nominal)<br>• 49999.0 (max-)<br>• 50000.0 (max) | B1<br>B2<br>B3<br>B4<br>B5 |
| **Tỷ lệ chiết khấu** (`discountPercent`) | 1.0 ≤ discountPercent ≤ 100.0 | V2 | • discountPercent < 1.0<br>• discountPercent > 100.0 | X3<br>X4 | • 1.0 (min)<br>• 1.1 (min+)<br>• 20.0 (nominal)<br>• 99.9 (max-)<br>• 100.0 (max) | B6<br>B7<br>B8<br>B9<br>B10 |
| **Lượt dùng chung toàn hệ thống** (`usedCount`) | 0 ≤ usedCount ≤ 49 | V3 | • usedCount < 0<br>• usedCount ≥ 50 | X5<br>X6 | • 0 (min)<br>• 1 (min+)<br>• 25 (nominal)<br>• 48 (max-)<br>• 49 (max) | B11<br>B12<br>B13<br>B14<br>B15 |
| **Lượt dùng cá nhân của User** (`userUsedCount`) | 0 ≤ userUsedCount ≤ 1 | V4 | • userUsedCount < 0<br>• userUsedCount ≥ 2 | X7<br>X8 | • 0 (min)<br>• 1 (min+)<br>• 1 (nominal)<br>• 1 (max-)<br>• 1 (max) | B16<br>B17<br>B18<br>B19<br>B20 |

---

## Câu 1. Xác định lớp tương đương

| Biến đầu vào | Lớp hợp lệ | Tag | Lớp không hợp lệ | Tag |
|---|---|---|---|---|
| **Giá trị đơn hàng** (`orderAmount` - nghìn đồng) | 500.0 ≤ orderAmount ≤ 50000.0 | V1 | • orderAmount < 500.0 (Chưa đạt mốc tối thiểu)<br>• orderAmount > 50000.0 (Vượt trần giao dịch) | X1<br>X2 |
| **Tỷ lệ chiết khấu** (`discountPercent` - %) | 1.0 ≤ discountPercent ≤ 100.0 | V2 | • discountPercent < 1.0 (Tỷ lệ không hợp lệ)<br>• discountPercent > 100.0 (Vượt quá 100%) | X3<br>X4 |
| **Lượt dùng chung** (`usedCount` - lượt) | 0 ≤ usedCount ≤ 49 (Limit = 50) | V3 | • usedCount < 0 (Số âm bất thường)<br>• usedCount ≥ 50 (Đã cạn lượt toàn hệ thống) | X5<br>X6 |
| **Lượt dùng cá nhân** (`userUsedCount` - lượt) | 0 ≤ userUsedCount ≤ 1 (Limit = 2) | V4 | • userUsedCount < 0 (Số âm bất thường)<br>• userUsedCount ≥ 2 (Đã hết hạn mức cá nhân) | X7<br>X8 |

---

## Câu 2. Phân tích giá trị biên

### 1. Bảng 5 giá trị biên cho từng biến đầu vào

| Biến đầu vào | min | min+ | nominal | max- | max | Tag biên |
|---|---:|---:|---:|---:|---:|---|
| **Giá trị đơn hàng** (`orderAmount`) | 500.0 | 501.0 | 25000.0 | 49999.0 | 50000.0 | B1, B2, B3, B4, B5 |
| **Tỷ lệ chiết khấu** (`discountPercent`) | 1.0 | 1.1 | 20.0 | 99.9 | 100.0 | B6, B7, B8, B9, B10 |
| **Lượt dùng chung** (`usedCount`) | 0 | 1 | 25 | 48 | 49 | B11, B12, B13, B14, B15 |
| **Lượt dùng cá nhân** (`userUsedCount`) | 0 | 1 | 1 | 1 | 1 | B16, B17, B18, B19, B20 |

### 2. Bảng 17 test case Standard BVA (Single Fault Assumption: $4n + 1 = 17$)

Theo kỹ thuật Standard Boundary Value Analysis, với $n = 4$ biến đầu vào, số test case là:
$$4n + 1 = 4 \times 4 + 1 = \mathbf{17\text{ test case}}$$

Giữ $n - 1$ biến tại giá trị danh định (`nominal`), lần lượt thay đổi 1 biến qua 4 giá trị biên (`min`, `min+`, `max-`, `max`):

| STT | Mã TC | Biến kiểm thử biên | Điểm biên kiểm tra | Đơn hàng (k) | Chiết khấu (%) | Lượt dùng chung | Lượt cá nhân | Kết quả mong đợi | Tag bao phủ |
|:---:|:---:|:---|:---|:---:|:---:|:---:|:---:|:---|:---|
| 1 | BVA01 | Baseline (Tất cả) | Nominal | 25000.0 | 20.0 | 25 | 1 | Hợp lệ (True) | B3, B8, B13, B18 |
| 2 | BVA02 | Giá trị đơn hàng | min (500.0) | **500.0** | 20.0 | 25 | 1 | Hợp lệ (True) | B1 |
| 3 | BVA03 | Giá trị đơn hàng | min+ (501.0) | **501.0** | 20.0 | 25 | 1 | Hợp lệ (True) | B2 |
| 4 | BVA04 | Giá trị đơn hàng | max- (49999.0) | **49999.0** | 20.0 | 25 | 1 | Hợp lệ (True) | B4 |
| 5 | BVA05 | Giá trị đơn hàng | max (50000.0) | **50000.0** | 20.0 | 25 | 1 | Hợp lệ (True) | B5 |
| 6 | BVA06 | Tỷ lệ chiết khấu | min (1.0) | 25000.0 | **1.0** | 25 | 1 | Hợp lệ (True) | B6 |
| 7 | BVA07 | Tỷ lệ chiết khấu | min+ (1.1) | 25000.0 | **1.1** | 25 | 1 | Hợp lệ (True) | B7 |
| 8 | BVA08 | Tỷ lệ chiết khấu | max- (99.9) | 25000.0 | **99.9** | 25 | 1 | Hợp lệ (True) | B9 |
| 9 | BVA09 | Tỷ lệ chiết khấu | max (100.0) | 25000.0 | **100.0** | 25 | 1 | Hợp lệ (True) | B10 |
| 10 | BVA10 | Lượt dùng chung | min (0) | 25000.0 | 20.0 | **0** | 1 | Hợp lệ (True) | B11 |
| 11 | BVA11 | Lượt dùng chung | min+ (1) | 25000.0 | 20.0 | **1** | 1 | Hợp lệ (True) | B12 |
| 12 | BVA12 | Lượt dùng chung | max- (48) | 25000.0 | 20.0 | **48** | 1 | Hợp lệ (True) | B14 |
| 13 | BVA13 | Lượt dùng chung | max (49) | 25000.0 | 20.0 | **49** | 1 | Hợp lệ (True) | B15 |
| 14 | BVA14 | Lượt dùng cá nhân | min (0) | 25000.0 | 20.0 | 25 | **0** | Hợp lệ (True) | B16 |
| 15 | BVA15 | Lượt dùng cá nhân | min+ (1) | 25000.0 | 20.0 | 25 | **1** | Hợp lệ (True) | B17 |
| 16 | BVA16 | Lượt dùng cá nhân | max- (1) | 25000.0 | 20.0 | 25 | **1** | Hợp lệ (True) | B19 |
| 17 | BVA17 | Lượt dùng cá nhân | max (1) | 25000.0 | 20.0 | 25 | **1** | Hợp lệ (True) | B20 |

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
| TC01 | orderAmount: 25000.0, discountPercent: 20.0, usedCount: 25, userUsedCount: 1 | Hợp lệ (True) | V1, V2, V3, V4, B3, B8, B13, B18 |
| TC02 | orderAmount: 500.0, discountPercent: 20.0, usedCount: 25, userUsedCount: 1 | Hợp lệ (True) | B1 |
| TC03 | orderAmount: 50000.0, discountPercent: 20.0, usedCount: 25, userUsedCount: 1 | Hợp lệ (True) | B5 |
| TC04 | orderAmount: 499.0, discountPercent: 20.0, usedCount: 25, userUsedCount: 1 | Không hợp lệ (False): Đơn hàng nhỏ hơn 500.0k | X1 |
| TC05 | orderAmount: 50001.0, discountPercent: 20.0, usedCount: 25, userUsedCount: 1 | Không hợp lệ (False): Đơn hàng vượt trần 50000.0k | X2 |
| TC06 | orderAmount: 25000.0, discountPercent: 1.0, usedCount: 25, userUsedCount: 1 | Hợp lệ (True) | B6 |
| TC07 | orderAmount: 25000.0, discountPercent: 100.0, usedCount: 25, userUsedCount: 1 | Hợp lệ (True) | B10 |
| TC08 | orderAmount: 25000.0, discountPercent: 0.9, usedCount: 25, userUsedCount: 1 | Không hợp lệ (False): Tỷ lệ chiết khấu nhỏ hơn 1.0% | X3 |
| TC09 | orderAmount: 25000.0, discountPercent: 100.1, usedCount: 25, userUsedCount: 1 | Không hợp lệ (False): Tỷ lệ chiết khấu lớn hơn 100.0% | X4 |
| TC10 | orderAmount: 25000.0, discountPercent: 20.0, usedCount: 0, userUsedCount: 1 | Hợp lệ (True) | B11 |
| TC11 | orderAmount: 25000.0, discountPercent: 20.0, usedCount: 49, userUsedCount: 1 | Hợp lệ (True) | B15 |
| TC12 | orderAmount: 25000.0, discountPercent: 20.0, usedCount: -1, userUsedCount: 1 | Không hợp lệ (False): Lượt dùng chung nhỏ hơn 0 | X5 |
| TC13 | orderAmount: 25000.0, discountPercent: 20.0, usedCount: 50, userUsedCount: 1 | Không hợp lệ (False): Lượt dùng chung đã đạt tối đa 50 | X6 |
| TC14 | orderAmount: 25000.0, discountPercent: 20.0, usedCount: 25, userUsedCount: 0 | Hợp lệ (True) | B16 |
| TC15 | orderAmount: 25000.0, discountPercent: 20.0, usedCount: 25, userUsedCount: 1 | Hợp lệ (True) | B20 |
| TC16 | orderAmount: 25000.0, discountPercent: 20.0, usedCount: 25, userUsedCount: -1 | Không hợp lệ (False): Lượt cá nhân nhỏ hơn 0 | X7 |
| TC17 | orderAmount: 25000.0, discountPercent: 20.0, usedCount: 25, userUsedCount: 2 | Không hợp lệ (False): Lượt cá nhân đã đạt tối đa 2 | X8 |

### 2. Bảng test case chi tiết theo đề bài (8 cột)

| STT | Tên test case | Giá trị đơn hàng (k) | Tỷ lệ chiết khấu (%) | Lượt dùng chung | Lượt cá nhân | Kết quả mong đợi | Tag được bao phủ |
|:---:|:---|:---:|:---:|:---:|:---:|:---|:---|
| 1 | Baseline danh định (nominal) | 25000.0 | 20.0 | 25 | 1 | Hợp lệ (True) | V1, V2, V3, V4, B3, B8, B13, B18 |
| 2 | Biên dưới hợp lệ orderAmount = min (500.0) | **500.0** | 20.0 | 25 | 1 | Hợp lệ (True) | B1 |
| 3 | Biên trên hợp lệ orderAmount = max (50000.0) | **50000.0** | 20.0 | 25 | 1 | Hợp lệ (True) | B5 |
| 4 | Ngoài biên dưới orderAmount < min (499.0) | **499.0** | 20.0 | 25 | 1 | Không hợp lệ (False): Đơn hàng nhỏ hơn 500.0k | X1 |
| 5 | Ngoài biên trên orderAmount > max (50001.0) | **50001.0** | 20.0 | 25 | 1 | Không hợp lệ (False): Đơn hàng vượt trần 50000.0k | X2 |
| 6 | Biên dưới hợp lệ discountPercent = min (1.0) | 25000.0 | **1.0** | 25 | 1 | Hợp lệ (True) | B6 |
| 7 | Biên trên hợp lệ discountPercent = max (100.0) | 25000.0 | **100.0** | 25 | 1 | Hợp lệ (True) | B10 |
| 8 | Ngoài biên dưới discountPercent < min (0.9) | 25000.0 | **0.9** | 25 | 1 | Không hợp lệ (False): Tỷ lệ chiết khấu nhỏ hơn 1.0% | X3 |
| 9 | Ngoài biên trên discountPercent > max (100.1) | 25000.0 | **100.1** | 25 | 1 | Không hợp lệ (False): Tỷ lệ chiết khấu lớn hơn 100.0% | X4 |
| 10 | Biên dưới hợp lệ usedCount = min (0) | 25000.0 | 20.0 | **0** | 1 | Hợp lệ (True) | B11 |
| 11 | Biên trên hợp lệ usedCount = max (49) | 25000.0 | 20.0 | **49** | 1 | Hợp lệ (True) | B15 |
| 12 | Ngoài biên dưới usedCount < min (-1) | 25000.0 | 20.0 | **-1** | 1 | Không hợp lệ (False): Lượt dùng chung nhỏ hơn 0 | X5 |
| 13 | Ngoài biên trên usedCount > max (50) | 25000.0 | 20.0 | **50** | 1 | Không hợp lệ (False): Lượt dùng chung đã đạt tối đa 50 | X6 |
| 14 | Biên dưới hợp lệ userUsedCount = min (0) | 25000.0 | 20.0 | 25 | **0** | Hợp lệ (True) | B16 |
| 15 | Biên trên hợp lệ userUsedCount = max (1) | 25000.0 | 20.0 | 25 | **1** | Hợp lệ (True) | B20 |
| 16 | Ngoài biên dưới userUsedCount < min (-1) | 25000.0 | 20.0 | 25 | **-1** | Không hợp lệ (False): Lượt cá nhân nhỏ hơn 0 | X7 |
| 17 | Ngoài biên trên userUsedCount > max (2) | 25000.0 | 20.0 | 25 | **2** | Không hợp lệ (False): Lượt cá nhân đã đạt tối đa 2 | X8 |

---

### 3. Bổ sung: Ma trận Bảng quyết định rút gọn (Collapsed Decision Table - 8 Rules)

| Điều kiện & Hành động | Rule 1 (R1) | Rule 2 (R2) | Rule 3 (R3) | Rule 4 (R4) | Rule 5 (R5) | Rule 6 (R6) | Rule 7 (R7) | Rule 8 (R8) |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| **C1: Mã tồn tại trong CSDL?** | **N** | Y | Y | Y | Y | Y | Y | Y |
| **C2: Đang kích hoạt (`active = true`)?** | - | **N** | Y | Y | Y | Y | Y | Y |
| **C3: Còn hạn dùng (`date <= expiry`)?** | - | - | **N** | Y | Y | Y | Y | Y |
| **C4: Đạt đơn tối thiểu (`total >= min`)?** | - | - | - | **N** | Y | Y | Y | Y |
| **C5: Còn lượt chung (`used < limit`)?** | - | - | - | - | **N** | Y | Y | Y |
| **C6: Là Khách vãng lai (Guest)?** | - | - | - | - | - | **Y** | N | N |
| **C7: Còn lượt User (`userUsed < limit`)?** | - | - | - | - | - | - | **N** | **Y** |
| *A1: Báo lỗi 'Mã không tồn tại'* | **X** | - | - | - | - | - | - | - |
| *A2: Báo lỗi 'Mã đã bị vô hiệu hóa'* | - | **X** | - | - | - | - | - | - |
| *A3: Báo lỗi 'Mã đã hết hạn dùng'* | - | - | **X** | - | - | - | - | - |
| *A4: Báo lỗi 'Chưa đạt đơn tối thiểu'* | - | - | - | **X** | - | - | - | - |
| *A5: Báo lỗi 'Mã đã hết lượt sử dụng'* | - | - | - | - | **X** | - | - | - |
| *A6: Báo lỗi 'Bạn đã hết lượt dùng'* | - | - | - | - | - | - | **X** | - |
| *A7: Áp dụng mã thành công (HTTP 200)* | - | - | - | - | - | **X** | - | **X** |

---

## Câu 4. Triển khai kiểm thử tự động

```python
def ValidateVoucher(orderAmount: float, discountPercent: float, usedCount: int, userUsedCount: int) -> bool:
    """
    Kiểm tra tính hợp lệ của yêu cầu áp dụng mã giảm giá (Voucher):
    - 500.0 <= orderAmount <= 50000.0 (Giá trị đơn hàng: 500.000đ - 50.000.000đ)
    - 1.0 <= discountPercent <= 100.0 (Tỷ lệ chiết khấu: 1% - 100%)
    - 0 <= usedCount <= 49 (Số lượt toàn hệ thống đã dùng < limit 50)
    - 0 <= userUsedCount <= 1 (Số lượt cá nhân đã dùng < perUserLimit 2)
    Trả về True nếu tất cả điều kiện thỏa mãn, ngược lại False.
    """
    if not (isinstance(orderAmount, (int, float)) and not isinstance(orderAmount, bool) and 500.0 <= orderAmount <= 50000.0):
        return False
    if not (isinstance(discountPercent, (int, float)) and not isinstance(discountPercent, bool) and 1.0 <= discountPercent <= 100.0):
        return False
    if not (isinstance(usedCount, int) and not isinstance(usedCount, bool) and 0 <= usedCount <= 49):
        return False
    if not (isinstance(userUsedCount, int) and not isinstance(userUsedCount, bool) and 0 <= userUsedCount <= 1):
        return False
    return True
```

```pytest
# thiết kế các test cases từ câu 3.
# Run test case 
import pytest

test_cases_voucher = [
    # TC01: Baseline nominal (Tất cả biến tại giá trị danh định)
    ("TC01", 25000.0, 20.0, 25, 1, True, "V1, V2, V3, V4, B3, B8, B13, B18"),
    
    # TC02 - TC05: Biên biến orderAmount (min, max, min-1, max+1)
    ("TC02", 500.0, 20.0, 25, 1, True, "B1"),
    ("TC03", 50000.0, 20.0, 25, 1, True, "B5"),
    ("TC04", 499.0, 20.0, 25, 1, False, "X1"),
    ("TC05", 50001.0, 20.0, 25, 1, False, "X2"),
    
    # TC06 - TC09: Biên biến discountPercent (min, max, min-0.1, max+0.1)
    ("TC06", 25000.0, 1.0, 25, 1, True, "B6"),
    ("TC07", 25000.0, 100.0, 25, 1, True, "B10"),
    ("TC08", 25000.0, 0.9, 25, 1, False, "X3"),
    ("TC09", 25000.0, 100.1, 25, 1, False, "X4"),
    
    # TC10 - TC13: Biên biến usedCount (min, max, min-1, max+1)
    ("TC10", 25000.0, 20.0, 0, 1, True, "B11"),
    ("TC11", 25000.0, 20.0, 49, 1, True, "B15"),
    ("TC12", 25000.0, 20.0, -1, 1, False, "X5"),
    ("TC13", 25000.0, 20.0, 50, 1, False, "X6"),
    
    # TC14 - TC17: Biên biến userUsedCount (min, max, min-1, max+1)
    ("TC14", 25000.0, 20.0, 25, 0, True, "B16"),
    ("TC15", 25000.0, 20.0, 25, 1, True, "B20"),
    ("TC16", 25000.0, 20.0, 25, -1, False, "X7"),
    ("TC17", 25000.0, 20.0, 25, 2, False, "X8"),
]

@pytest.mark.parametrize("tc_id,orderAmount,discountPercent,usedCount,userUsedCount,expected,tag", test_cases_voucher)
def test_voucher_validation(tc_id, orderAmount, discountPercent, usedCount, userUsedCount, expected, tag):
    """Kiểm thử tự động toàn bộ 17 test case thiết kế cho Phân hệ 4 Voucher theo nguyên lý 4n + 1."""
    assert ValidateVoucher(orderAmount, discountPercent, usedCount, userUsedCount) == expected

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

scripts/test_voucher_standard_bva.py::test_voucher_validation[TC01-25000.0-20.0-25-1-True-V1, V2, V3, V4, B3, B8, B13, B18] PASSED [  5%]
scripts/test_voucher_standard_bva.py::test_voucher_validation[TC02-500.0-20.0-25-1-True-B1] PASSED [ 11%]
scripts/test_voucher_standard_bva.py::test_voucher_validation[TC03-50000.0-20.0-25-1-True-B5] PASSED [ 17%]
scripts/test_voucher_standard_bva.py::test_voucher_validation[TC04-499.0-20.0-25-1-False-X1] PASSED [ 23%]
scripts/test_voucher_standard_bva.py::test_voucher_validation[TC05-50001.0-20.0-25-1-False-X2] PASSED [ 29%]
scripts/test_voucher_standard_bva.py::test_voucher_validation[TC06-25000.0-1.0-25-1-True-B6] PASSED [ 35%]
scripts/test_voucher_standard_bva.py::test_voucher_validation[TC07-25000.0-100.0-25-1-True-B10] PASSED [ 41%]
scripts/test_voucher_standard_bva.py::test_voucher_validation[TC08-25000.0-0.9-25-1-False-X3] PASSED [ 47%]
scripts/test_voucher_standard_bva.py::test_voucher_validation[TC09-25000.0-100.1-25-1-False-X4] PASSED [ 52%]
scripts/test_voucher_standard_bva.py::test_voucher_validation[TC10-25000.0-20.0-0-1-True-B11] PASSED [ 58%]
scripts/test_voucher_standard_bva.py::test_voucher_validation[TC11-25000.0-20.0-49-1-True-B15] PASSED [ 64%]
scripts/test_voucher_standard_bva.py::test_voucher_validation[TC12-25000.0-20.0--1-1-False-X5] PASSED [ 70%]
scripts/test_voucher_standard_bva.py::test_voucher_validation[TC13-25000.0-20.0-50-1-False-X6] PASSED [ 76%]
scripts/test_voucher_standard_bva.py::test_voucher_validation[TC14-25000.0-20.0-25-0-True-B16] PASSED [ 82%]
scripts/test_voucher_standard_bva.py::test_voucher_validation[TC15-25000.0-20.0-25-1-True-B20] PASSED [ 88%]
scripts/test_voucher_standard_bva.py::test_voucher_validation[TC16-25000.0-20.0-25--1-False-X7] PASSED [ 94%]
scripts/test_voucher_standard_bva.py::test_voucher_validation[TC17-25000.0-20.0-25-2-False-X8] PASSED [100%]

============================= 17 passed in 0.14s ==============================
```
