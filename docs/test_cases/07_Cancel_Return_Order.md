# BÀI LÀM: KIỂM THỬ CHỨC NĂNG 7 - HỦY ĐƠN HÀNG & YÊU CẦU ĐỔI TRẢ (CANCEL & RETURN ORDER)

- **Họ và tên sinh viên:** Nguyễn Hoàng Phương
- **Mã số sinh viên (MSSV):** 080205010954
- **Môn học:** Kiểm Chứng Phần Mềm
- **Chủ đề:** Phân hoạch lớp tương đương, phân tích giá trị biên, bảng quyết định, chuyển đổi trạng thái, thiết kế test case và kiểm thử tự động

---

## Bảng phân tích điều kiện kiểm thử (Test Conditions)

| Conditions | Valid Partition | Tag | Invalid Partitions | Tag | Valid Boundaries | Tag |
|---|---|---|---|---|---|---|
| **Hạn gửi yêu cầu đổi trả** (`returnWindowDays`) | 0 ≤ returnWindowDays ≤ 7 | V1 | • returnWindowDays < 0<br>• returnWindowDays > 7 | X1<br>X2 | • 0 (min)<br>• 1 (min+)<br>• 3 (nominal)<br>• 6 (max-)<br>• 7 (max) | B1<br>B2<br>B3<br>B4<br>B5 |
| **Độ dài lý do đổi trả** (`reasonLength`) | 10 ≤ reasonLength ≤ 300 | V2 | • reasonLength < 10<br>• reasonLength > 300 | X3<br>X4 | • 10 (min)<br>• 11 (min+)<br>• 50 (nominal)<br>• 299 (max-)<br>• 300 (max) | B6<br>B7<br>B8<br>B9<br>B10 |
| **Số lượng ảnh minh chứng** (`returnImageCount`) | 1 ≤ returnImageCount ≤ 5 | V3 | • returnImageCount < 1<br>• returnImageCount > 5 | X5<br>X6 | • 1 (min)<br>• 2 (min+)<br>• 2 (nominal)<br>• 4 (max-)<br>• 5 (max) | B11<br>B12<br>B13<br>B14<br>B15 |
| **Trạng thái đơn hàng hợp lệ** (`orderStatusAllowed`) | orderStatusAllowed = 1 | V4 | • orderStatusAllowed = 0<br>• orderStatusAllowed > 1 | X7<br>X8 | • 1 (min)<br>• 1 (min+)<br>• 1 (nominal)<br>• 1 (max-)<br>• 1 (max) | B16<br>B17<br>B18<br>B19<br>B20 |

---

## Câu 1. Xác định lớp tương đương

| Biến đầu vào | Lớp hợp lệ | Tag | Lớp không hợp lệ | Tag |
|---|---|---|---|---|
| **Hạn gửi yêu cầu đổi trả** (`returnWindowDays`) | 0 ≤ returnWindowDays ≤ 7 | V1 | • returnWindowDays < 0 (Số âm)<br>• returnWindowDays > 7 (Quá hạn 7 ngày đổi trả) | X1<br>X2 |
| **Độ dài lý do đổi trả** (`reasonLength`) | 10 ≤ reasonLength ≤ 300 | V2 | • reasonLength < 10 (Lý do quá ngắn)<br>• reasonLength > 300 (Lý do vượt trần 300 ký tự) | X3<br>X4 |
| **Số lượng ảnh minh chứng** (`returnImageCount`) | 1 ≤ returnImageCount ≤ 5 | V3 | • returnImageCount < 1 (Không đính kèm ảnh)<br>• returnImageCount > 5 (Vượt quá 5 ảnh) | X5<br>X6 |
| **Trạng thái đơn hàng hợp lệ** (`orderStatusAllowed`) | orderStatusAllowed = 1 (PENDING/DELIVERED) | V4 | • orderStatusAllowed = 0 (SHIPPED/CANCELLED)<br>• orderStatusAllowed ≠ 1 (Trạng thái sai) | X7<br>X8 |

---

## Câu 2. Phân tích giá trị biên

### 1. Bảng 5 giá trị biên cho từng biến đầu vào

| Biến đầu vào | min | min+ | nominal | max- | max | Tag biên |
|---|---:|---:|---:|---:|---:|---|
| **Hạn gửi yêu cầu đổi trả** (`returnWindowDays`) | 0 | 1 | 3 | 6 | 7 | B1, B2, B3, B4, B5 |
| **Độ dài lý do đổi trả** (`reasonLength`) | 10 | 11 | 50 | 299 | 300 | B6, B7, B8, B9, B10 |
| **Số lượng ảnh minh chứng** (`returnImageCount`) | 1 | 2 | 2 | 4 | 5 | B11, B12, B13, B14, B15 |
| **Trạng thái đơn hàng hợp lệ** (`orderStatusAllowed`) | 1 | 1 | 1 | 1 | 1 | B16, B17, B18, B19, B20 |

### 2. Bảng 17 test case Standard BVA (Single Fault Assumption: $4n + 1 = 17$)

Theo kỹ thuật Standard Boundary Value Analysis, với $n = 4$ biến đầu vào, số test case là:
$$4n + 1 = 4 \times 4 + 1 = \mathbf{17\text{ test case}}$$

Giữ $n - 1$ biến tại giá trị danh định (`nominal`), lần lượt thay đổi 1 biến qua 4 giá trị biên (`min`, `min+`, `max-`, `max`):

| STT | Mã TC | Biến kiểm thử biên | Điểm biên kiểm tra | Hạn đổi (ngày) | Lý do (len) | Số ảnh đính kèm | Trạng thái hợp lệ | Kết quả mong đợi | Tag bao phủ |
|:---:|:---:|:---|:---|:---:|:---:|:---:|:---:|:---|:---|
| 1 | BVA01 | Baseline (Tất cả) | Nominal | 3 | 50 | 2 | 1 | Hợp lệ (True) | B3, B8, B13, B18 |
| 2 | BVA02 | Hạn đổi trả | min (0) | **0** | 50 | 2 | 1 | Hợp lệ (True) | B1 |
| 3 | BVA03 | Hạn đổi trả | max (7) | **7** | 50 | 2 | 1 | Hợp lệ (True) | B5 |
| 4 | BVA04 | Hạn đổi trả | min-1 (-1) | **-1** | 50 | 2 | 1 | Không hợp lệ (False) | X1 |
| 5 | BVA05 | Hạn đổi trả | max+1 (8) | **8** | 50 | 2 | 1 | Không hợp lệ (False) | X2 |
| 6 | BVA06 | Độ dài lý do | min (10) | 3 | **10** | 2 | 1 | Hợp lệ (True) | B6 |
| 7 | BVA07 | Độ dài lý do | max (300) | 3 | **300** | 2 | 1 | Hợp lệ (True) | B10 |
| 8 | BVA08 | Độ dài lý do | min-1 (9) | 3 | **9** | 2 | 1 | Không hợp lệ (False) | X3 |
| 9 | BVA09 | Độ dài lý do | max+1 (301) | 3 | **301** | 2 | 1 | Không hợp lệ (False) | X4 |
| 10 | BVA10 | Số lượng ảnh | min (1) | 3 | 50 | **1** | 1 | Hợp lệ (True) | B11 |
| 11 | BVA11 | Số lượng ảnh | max (5) | 3 | 50 | **5** | 1 | Hợp lệ (True) | B15 |
| 12 | BVA12 | Số lượng ảnh | min-1 (0) | 3 | 50 | **0** | 1 | Không hợp lệ (False) | X5 |
| 13 | BVA13 | Số lượng ảnh | max+1 (6) | 3 | 50 | **6** | 1 | Không hợp lệ (False) | X6 |
| 14 | BVA14 | Trạng thái đơn | min (1) | 3 | 50 | 2 | **1** | Hợp lệ (True) | B16 |
| 15 | BVA15 | Trạng thái đơn | max (1) | 3 | 50 | 2 | **1** | Hợp lệ (True) | B20 |
| 16 | BVA16 | Trạng thái đơn | min-1 (0) | 3 | 50 | 2 | **0** | Không hợp lệ (False) | X7 |
| 17 | BVA17 | Trạng thái đơn | max+1 (2) | 3 | 50 | 2 | **2** | Không hợp lệ (False) | X8 |

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
| TC01 | returnWindowDays: 3, reasonLength: 50, returnImageCount: 2, orderStatusAllowed: 1 | Hợp lệ (True) | V1, V2, V3, V4, B3, B8, B13, B18 |
| TC02 | returnWindowDays: 0, reasonLength: 50, returnImageCount: 2, orderStatusAllowed: 1 | Hợp lệ (True) | B1 |
| TC03 | returnWindowDays: 7, reasonLength: 50, returnImageCount: 2, orderStatusAllowed: 1 | Hợp lệ (True) | B5 |
| TC04 | returnWindowDays: -1, reasonLength: 50, returnImageCount: 2, orderStatusAllowed: 1 | Không hợp lệ (False): Ngày đổi trả nhỏ hơn 0 | X1 |
| TC05 | returnWindowDays: 8, reasonLength: 50, returnImageCount: 2, orderStatusAllowed: 1 | Không hợp lệ (False): Quá hạn 7 ngày đổi trả | X2 |
| TC06 | returnWindowDays: 3, reasonLength: 10, returnImageCount: 2, orderStatusAllowed: 1 | Hợp lệ (True) | B6 |
| TC07 | returnWindowDays: 3, reasonLength: 300, returnImageCount: 2, orderStatusAllowed: 1 | Hợp lệ (True) | B10 |
| TC08 | returnWindowDays: 3, reasonLength: 9, returnImageCount: 2, orderStatusAllowed: 1 | Không hợp lệ (False): Lý do nhỏ hơn 10 ký tự | X3 |
| TC09 | returnWindowDays: 3, reasonLength: 301, returnImageCount: 2, orderStatusAllowed: 1 | Không hợp lệ (False): Lý do lớn hơn 300 ký tự | X4 |
| TC10 | returnWindowDays: 3, reasonLength: 50, returnImageCount: 1, orderStatusAllowed: 1 | Hợp lệ (True) | B11 |
| TC11 | returnWindowDays: 3, reasonLength: 50, returnImageCount: 5, orderStatusAllowed: 1 | Hợp lệ (True) | B15 |
| TC12 | returnWindowDays: 3, reasonLength: 50, returnImageCount: 0, orderStatusAllowed: 1 | Không hợp lệ (False): Thiếu ảnh minh chứng (0) | X5 |
| TC13 | returnWindowDays: 3, reasonLength: 50, returnImageCount: 6, orderStatusAllowed: 1 | Không hợp lệ (False): Vượt quá 5 ảnh minh chứng | X6 |
| TC14 | returnWindowDays: 3, reasonLength: 50, returnImageCount: 2, orderStatusAllowed: 1 | Hợp lệ (True) | B16 |
| TC15 | returnWindowDays: 3, reasonLength: 50, returnImageCount: 2, orderStatusAllowed: 1 | Hợp lệ (True) | B20 |
| TC16 | returnWindowDays: 3, reasonLength: 50, returnImageCount: 2, orderStatusAllowed: 0 | Không hợp lệ (False): Trạng thái đơn không được phép | X7 |
| TC17 | returnWindowDays: 3, reasonLength: 50, returnImageCount: 2, orderStatusAllowed: 2 | Không hợp lệ (False): Trạng thái không hợp lệ | X8 |

### 2. Bảng test case chi tiết theo đề bài (8 cột)

| STT | Tên test case | Hạn đổi (ngày) | Độ dài lý do | Số ảnh minh chứng | Trạng thái đơn | Kết quả mong đợi | Tag được bao phủ |
|:---:|:---|:---:|:---:|:---:|:---:|:---|:---|
| 1 | Baseline danh định (nominal) | 3 | 50 | 2 | 1 | Hợp lệ (True) | V1, V2, V3, V4, B3, B8, B13, B18 |
| 2 | Biên dưới hợp lệ returnWindowDays = min (0) | **0** | 50 | 2 | 1 | Hợp lệ (True) | B1 |
| 3 | Biên trên hợp lệ returnWindowDays = max (7) | **7** | 50 | 2 | 1 | Hợp lệ (True) | B5 |
| 4 | Ngoài biên dưới returnWindowDays < min (-1) | **-1** | 50 | 2 | 1 | Không hợp lệ (False): Ngày đổi trả nhỏ hơn 0 | X1 |
| 5 | Ngoài biên trên returnWindowDays > max (8) | **8** | 50 | 2 | 1 | Không hợp lệ (False): Quá hạn 7 ngày đổi trả | X2 |
| 6 | Biên dưới hợp lệ reasonLength = min (10) | 3 | **10** | 2 | 1 | Hợp lệ (True) | B6 |
| 7 | Biên trên hợp lệ reasonLength = max (300) | 3 | **300** | 2 | 1 | Hợp lệ (True) | B10 |
| 8 | Ngoài biên dưới reasonLength < min (9) | 3 | **9** | 2 | 1 | Không hợp lệ (False): Lý do nhỏ hơn 10 ký tự | X3 |
| 9 | Ngoài biên trên reasonLength > max (301) | 3 | **301** | 2 | 1 | Không hợp lệ (False): Lý do lớn hơn 300 ký tự | X4 |
| 10 | Biên dưới hợp lệ returnImageCount = min (1) | 3 | 50 | **1** | 1 | Hợp lệ (True) | B11 |
| 11 | Biên trên hợp lệ returnImageCount = max (5) | 3 | 50 | **5** | 1 | Hợp lệ (True) | B15 |
| 12 | Ngoài biên dưới returnImageCount < min (0) | 3 | 50 | **0** | 1 | Không hợp lệ (False): Thiếu ảnh minh chứng (0) | X5 |
| 13 | Ngoài biên trên returnImageCount > max (6) | 3 | 50 | **6** | 1 | Không hợp lệ (False): Vượt quá 5 ảnh minh chứng | X6 |
| 14 | Biên dưới hợp lệ orderStatusAllowed = min (1) | 3 | 50 | 2 | **1** | Hợp lệ (True) | B16 |
| 15 | Biên trên hợp lệ orderStatusAllowed = max (1) | 3 | 50 | 2 | **1** | Hợp lệ (True) | B20 |
| 16 | Ngoài biên dưới orderStatusAllowed < min (0) | 3 | 50 | 2 | **0** | Không hợp lệ (False): Trạng thái đơn không được phép | X7 |
| 17 | Ngoài biên trên orderStatusAllowed > max (2) | 3 | 50 | 2 | **2** | Không hợp lệ (False): Trạng thái không hợp lệ | X8 |

---

### 3. Bổ sung: Bảng Chuyển đổi trạng thái đơn hàng (State Transition - 8 States)

| Từ trạng thái ($S_{from}$) | Sự kiện / Thao tác | Đến trạng thái ($S_{to}$) | Kết quả nghiệp vụ |
| :--- | :--- | :--- | :--- |
| **S1: PENDING** | Khách hủy đơn trước khi duyệt | **S2: CANCELLED** | Hủy trực tiếp thành công, hoàn tồn kho |
| **S1: PENDING** | Admin duyệt đơn hàng | **S3: PROCESSING** | Chuyển sang đóng gói |
| **S3: PROCESSING** | Khách yêu cầu hủy | **S3: PROCESSING** | Từ chối hủy trực tiếp, yêu cầu liên hệ CSKH |
| **S3: PROCESSING** | Giao cho đơn vị vận chuyển | **S4: SHIPPED** | Đang giao hàng |
| **S4: SHIPPED** | Khách nhận hàng thành công | **S5: DELIVERED** | Giao thành công, kích hoạt hạn 7 ngày đổi trả |
| **S5: DELIVERED** | Khách gửi form đổi trả (trong 7 ngày) | **S6: RETURN_REQUESTED** | Yêu cầu chờ Admin xét duyệt |
| **S6: RETURN_REQUESTED** | Admin chấp thuận đổi trả | **S7: RETURN_APPROVED** | Khách gửi hàng về kho kiểm định |
| **S7: RETURN_APPROVED** | Kiểm định đạt chuẩn, hoàn tiền | **S8: REFUNDED** | Hoàn tất chu trình đổi trả hoàn tiền |

---

## Câu 4. Triển khai kiểm thử tự động

```python
def ValidateCancelReturn(returnWindowDays: int, reasonLength: int, returnImageCount: int, orderStatusAllowed: int) -> bool:
    """
    Kiểm tra tính hợp lệ của yêu cầu hủy/đổi trả đơn hàng:
    - 0 <= returnWindowDays <= 7 (Hạn 7 ngày kể từ lúc nhận hàng)
    - 10 <= reasonLength <= 300 (Lý do từ 10 đến 300 ký tự)
    - 1 <= returnImageCount <= 5 (Đính kèm từ 1 đến 5 ảnh minh chứng)
    - orderStatusAllowed == 1 (Trạng thái đơn hàng cho phép hủy/đổi trả)
    Trả về True nếu tất cả điều kiện thỏa mãn, ngược lại False.
    """
    if not (isinstance(returnWindowDays, int) and not isinstance(returnWindowDays, bool) and 0 <= returnWindowDays <= 7):
        return False
    if not (isinstance(reasonLength, int) and not isinstance(reasonLength, bool) and 10 <= reasonLength <= 300):
        return False
    if not (isinstance(returnImageCount, int) and not isinstance(returnImageCount, bool) and 1 <= returnImageCount <= 5):
        return False
    if not (isinstance(orderStatusAllowed, int) and not isinstance(orderStatusAllowed, bool) and orderStatusAllowed == 1):
        return False
    return True
```

```pytest
# thiết kế các test cases từ câu 3.
# Run test case 
import pytest

test_cases_m7 = [
    ("TC01", 3, 50, 2, 1, True, "V1, V2, V3, V4, B3, B8, B13, B18"),
    ("TC02", 0, 50, 2, 1, True, "B1"),
    ("TC03", 7, 50, 2, 1, True, "B5"),
    ("TC04", -1, 50, 2, 1, False, "X1"),
    ("TC05", 8, 50, 2, 1, False, "X2"),
    ("TC06", 3, 10, 2, 1, True, "B6"),
    ("TC07", 3, 300, 2, 1, True, "B10"),
    ("TC08", 3, 9, 2, 1, False, "X3"),
    ("TC09", 3, 301, 2, 1, False, "X4"),
    ("TC10", 3, 50, 1, 1, True, "B11"),
    ("TC11", 3, 50, 5, 1, True, "B15"),
    ("TC12", 3, 50, 0, 1, False, "X5"),
    ("TC13", 3, 50, 6, 1, False, "X6"),
    ("TC14", 3, 50, 2, 1, True, "B16"),
    ("TC15", 3, 50, 2, 1, True, "B20"),
    ("TC16", 3, 50, 2, 0, False, "X7"),
    ("TC17", 3, 50, 2, 2, False, "X8"),
]

@pytest.mark.parametrize("tc_id,rDays,rLen,imgCount,statAllowed,expected,tag", test_cases_m7)
def test_cancel_return_validation(tc_id, rDays, rLen, imgCount, statAllowed, expected, tag):
    """Kiểm thử tự động 17 test case hủy/đổi trả theo nguyên lý 4n + 1."""
    assert ValidateCancelReturn(rDays, rLen, imgCount, statAllowed) == expected

if __name__ == "__main__":
    pytest.main(["-v", __file__])
```

```kết quả test
============================= test session starts =============================
platform win32 -- Python 3.14.0, pytest-8.4.2, pluggy-1.6.0
rootdir: D:\LapTrinhAI\Testing
collected 17 items

test_cancel_return.py::test_cancel_return_validation[TC01-3-50-2-1-True-V1, V2, V3, V4, B3, B8, B13, B18] PASSED [  5%]
test_cancel_return.py::test_cancel_return_validation[TC02-0-50-2-1-True-B1] PASSED [ 11%]
test_cancel_return.py::test_cancel_return_validation[TC03-7-50-2-1-True-B5] PASSED [ 17%]
test_cancel_return.py::test_cancel_return_validation[TC04--1-50-2-1-False-X1] PASSED [ 23%]
test_cancel_return.py::test_cancel_return_validation[TC05-8-50-2-1-False-X2] PASSED [ 29%]
test_cancel_return.py::test_cancel_return_validation[TC06-3-10-2-1-True-B6] PASSED [ 35%]
test_cancel_return.py::test_cancel_return_validation[TC07-3-300-2-1-True-B10] PASSED [ 41%]
test_cancel_return.py::test_cancel_return_validation[TC08-3-9-2-1-False-X3] PASSED [ 47%]
test_cancel_return.py::test_cancel_return_validation[TC09-3-301-2-1-False-X4] PASSED [ 52%]
test_cancel_return.py::test_cancel_return_validation[TC10-3-50-1-1-True-B11] PASSED [ 58%]
test_cancel_return.py::test_cancel_return_validation[TC11-3-50-5-1-True-B15] PASSED [ 64%]
test_cancel_return.py::test_cancel_return_validation[TC12-3-50-0-1-False-X5] PASSED [ 70%]
test_cancel_return.py::test_cancel_return_validation[TC13-3-50-6-1-False-X6] PASSED [ 76%]
test_cancel_return.py::test_cancel_return_validation[TC14-3-50-2-1-True-B16] PASSED [ 82%]
test_cancel_return.py::test_cancel_return_validation[TC15-3-50-2-1-True-B20] PASSED [ 88%]
test_cancel_return.py::test_cancel_return_validation[TC16-3-50-2-0-False-X7] PASSED [ 94%]
test_cancel_return.py::test_cancel_return_validation[TC17-3-50-2-2-False-X8] PASSED [100%]

============================= 17 passed in 0.14s ==============================
```
