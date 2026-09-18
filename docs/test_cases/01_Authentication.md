# BÀI LÀM: KIỂM THỬ CHỨC NĂNG 1 - ĐĂNG NHẬP & XÁC THỰC (AUTHENTICATION)

- **Họ và tên sinh viên:** Nguyễn Hoàng Phương
- **Mã số sinh viên (MSSV):** 080205010954
- **Môn học:** Kiểm Chứng Phần Mềm
- **Chủ đề:** Phân hoạch lớp tương đương, phân tích giá trị biên, bảng quyết định, chuyển đổi trạng thái, thiết kế test case và kiểm thử tự động

---

## Bảng phân tích điều kiện kiểm thử (Test Conditions)

| Conditions | Valid Partition | Tag | Invalid Partitions | Tag | Valid Boundaries | Tag |
|---|---|---|---|---|---|---|
| **Độ dài tên đăng nhập** (`userLength`) | 3 ≤ userLength ≤ 30 | V1 | • userLength < 3<br>• userLength > 30 | X1<br>X2 | • 3 (min)<br>• 4 (min+)<br>• 15 (nominal)<br>• 29 (max-)<br>• 30 (max) | B1<br>B2<br>B3<br>B4<br>B5 |
| **Độ dài mật khẩu** (`passLength`) | 6 ≤ passLength ≤ 20 | V2 | • passLength < 6<br>• passLength > 20 | X3<br>X4 | • 6 (min)<br>• 7 (min+)<br>• 10 (nominal)<br>• 19 (max-)<br>• 20 (max) | B6<br>B7<br>B8<br>B9<br>B10 |
| **Trạng thái tài khoản** (`userStatus`) | userStatus = 1 (Active) | V3 | • userStatus = 0 (Locked)<br>• userStatus > 1 (Invalid) | X5<br>X6 | • 1 (min)<br>• 1 (min+)<br>• 1 (nominal)<br>• 1 (max-)<br>• 1 (max) | B11<br>B12<br>B13<br>B14<br>B15 |
| **Số lần đăng nhập sai** (`loginAttempts`) | 0 ≤ loginAttempts ≤ 4 | V4 | • loginAttempts < 0<br>• loginAttempts ≥ 5 | X7<br>X8 | • 0 (min)<br>• 1 (min+)<br>• 0 (nominal)<br>• 3 (max-)<br>• 4 (max) | B16<br>B17<br>B18<br>B19<br>B20 |

---

## Câu 1. Xác định lớp tương đương

| Biến đầu vào | Lớp hợp lệ | Tag | Lớp không hợp lệ | Tag |
|---|---|---|---|---|
| **Độ dài tên đăng nhập** (`userLength`) | 3 ≤ userLength ≤ 30 | V1 | • userLength < 3 (Tên quá ngắn)<br>• userLength > 30 (Tên quá dài) | X1<br>X2 |
| **Độ dài mật khẩu** (`passLength`) | 6 ≤ passLength ≤ 20 | V2 | • passLength < 6 (Mật khẩu quá ngắn)<br>• passLength > 20 (Mật khẩu quá dài) | X3<br>X4 |
| **Trạng thái tài khoản** (`userStatus`) | userStatus = 1 (Đang kích hoạt) | V3 | • userStatus = 0 (Tài khoản bị khóa)<br>• userStatus ≠ 1 (Trạng thái bất thường) | X5<br>X6 |
| **Số lần đăng nhập sai** (`loginAttempts`) | 0 ≤ loginAttempts ≤ 4 | V4 | • loginAttempts < 0 (Số âm bất thường)<br>• loginAttempts ≥ 5 (Vượt ngưỡng 5 lần khóa tài khoản) | X7<br>X8 |

---

## Câu 2. Phân tích giá trị biên

### 1. Bảng 5 giá trị biên cho từng biến đầu vào

| Biến đầu vào | min | min+ | nominal | max- | max | Tag biên |
|---|---:|---:|---:|---:|---:|---|
| **Độ dài tên đăng nhập** (`userLength`) | 3 | 4 | 15 | 29 | 30 | B1, B2, B3, B4, B5 |
| **Độ dài mật khẩu** (`passLength`) | 6 | 7 | 10 | 19 | 20 | B6, B7, B8, B9, B10 |
| **Trạng thái tài khoản** (`userStatus`) | 1 | 1 | 1 | 1 | 1 | B11, B12, B13, B14, B15 |
| **Số lần đăng nhập sai** (`loginAttempts`) | 0 | 1 | 0 | 3 | 4 | B16, B17, B18, B19, B20 |

### 2. Bảng 17 test case Standard BVA (Single Fault Assumption: $4n + 1 = 17$)

Theo kỹ thuật Standard Boundary Value Analysis, với $n = 4$ biến đầu vào, số test case là:
$$4n + 1 = 4 \times 4 + 1 = \mathbf{17\text{ test case}}$$

Giữ $n - 1$ biến tại giá trị danh định (`nominal`), lần lượt thay đổi 1 biến qua 4 giá trị biên (`min`, `min+`, `max-`, `max`):

| STT | Mã TC | Biến kiểm thử biên | Điểm biên kiểm tra | Tên đăng nhập | Mật khẩu | Trạng thái | Số lần thử sai | Kết quả mong đợi | Tag bao phủ |
|:---:|:---:|:---|:---|:---:|:---:|:---:|:---:|:---|:---|
| 1 | BVA01 | Baseline (Tất cả) | Nominal | 15 | 10 | 1 | 0 | Hợp lệ (True) | B3, B8, B13, B18 |
| 2 | BVA02 | Độ dài username | min (3) | **3** | 10 | 1 | 0 | Hợp lệ (True) | B1 |
| 3 | BVA03 | Độ dài username | max (30) | **30** | 10 | 1 | 0 | Hợp lệ (True) | B5 |
| 4 | BVA04 | Độ dài username | min-1 (2) | **2** | 10 | 1 | 0 | Không hợp lệ (False) | X1 |
| 5 | BVA05 | Độ dài username | max+1 (31) | **31** | 10 | 1 | 0 | Không hợp lệ (False) | X2 |
| 6 | BVA06 | Độ dài password | min (6) | 15 | **6** | 1 | 0 | Hợp lệ (True) | B6 |
| 7 | BVA07 | Độ dài password | max (20) | 15 | **20** | 1 | 0 | Hợp lệ (True) | B10 |
| 8 | BVA08 | Độ dài password | min-1 (5) | 15 | **5** | 1 | 0 | Không hợp lệ (False) | X3 |
| 9 | BVA09 | Độ dài password | max+1 (21) | 15 | **21** | 1 | 0 | Không hợp lệ (False) | X4 |
| 10 | BVA10 | Trạng thái user | min (1) | 15 | 10 | **1** | 0 | Hợp lệ (True) | B11 |
| 11 | BVA11 | Trạng thái user | max (1) | 15 | 10 | **1** | 0 | Hợp lệ (True) | B15 |
| 12 | BVA12 | Trạng thái user | min-1 (0) | 15 | 10 | **0** | 0 | Không hợp lệ (False) | X5 |
| 13 | BVA13 | Trạng thái user | max+1 (2) | 15 | 10 | **2** | 0 | Không hợp lệ (False) | X6 |
| 14 | BVA14 | Số lần thử sai | min (0) | 15 | 10 | 1 | **0** | Hợp lệ (True) | B16 |
| 15 | BVA15 | Số lần thử sai | max (4) | 15 | 10 | 1 | **4** | Hợp lệ (True) | B20 |
| 16 | BVA16 | Số lần thử sai | min-1 (-1) | 15 | 10 | 1 | **-1** | Không hợp lệ (False) | X7 |
| 17 | BVA17 | Số lần thử sai | max+1 (5) | 15 | 10 | 1 | **5** | Không hợp lệ (False) | X8 |

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
| TC01 | userLength: 15, passLength: 10, userStatus: 1, loginAttempts: 0 | Hợp lệ (True) | V1, V2, V3, V4, B3, B8, B13, B18 |
| TC02 | userLength: 3, passLength: 10, userStatus: 1, loginAttempts: 0 | Hợp lệ (True) | B1 |
| TC03 | userLength: 30, passLength: 10, userStatus: 1, loginAttempts: 0 | Hợp lệ (True) | B5 |
| TC04 | userLength: 2, passLength: 10, userStatus: 1, loginAttempts: 0 | Không hợp lệ (False): Tên đăng nhập nhỏ hơn 3 ký tự | X1 |
| TC05 | userLength: 31, passLength: 10, userStatus: 1, loginAttempts: 0 | Không hợp lệ (False): Tên đăng nhập lớn hơn 30 ký tự | X2 |
| TC06 | userLength: 15, passLength: 6, userStatus: 1, loginAttempts: 0 | Hợp lệ (True) | B6 |
| TC07 | userLength: 15, passLength: 20, userStatus: 1, loginAttempts: 0 | Hợp lệ (True) | B10 |
| TC08 | userLength: 15, passLength: 5, userStatus: 1, loginAttempts: 0 | Không hợp lệ (False): Mật khẩu nhỏ hơn 6 ký tự | X3 |
| TC09 | userLength: 15, passLength: 21, userStatus: 1, loginAttempts: 0 | Không hợp lệ (False): Mật khẩu lớn hơn 20 ký tự | X4 |
| TC10 | userLength: 15, passLength: 10, userStatus: 1, loginAttempts: 0 | Hợp lệ (True) | B11 |
| TC11 | userLength: 15, passLength: 10, userStatus: 1, loginAttempts: 0 | Hợp lệ (True) | B15 |
| TC12 | userLength: 15, passLength: 10, userStatus: 0, loginAttempts: 0 | Không hợp lệ (False): Tài khoản bị khóa (status = 0) | X5 |
| TC13 | userLength: 15, passLength: 10, userStatus: 2, loginAttempts: 0 | Không hợp lệ (False): Trạng thái tài khoản không hợp lệ | X6 |
| TC14 | userLength: 15, passLength: 10, userStatus: 1, loginAttempts: 0 | Hợp lệ (True) | B16 |
| TC15 | userLength: 15, passLength: 10, userStatus: 1, loginAttempts: 4 | Hợp lệ (True) | B20 |
| TC16 | userLength: 15, passLength: 10, userStatus: 1, loginAttempts: -1 | Không hợp lệ (False): Số lần thử sai nhỏ hơn 0 | X7 |
| TC17 | userLength: 15, passLength: 10, userStatus: 1, loginAttempts: 5 | Không hợp lệ (False): Đã đạt trần 5 lần thử sai | X8 |

### 2. Bảng test case chi tiết theo đề bài (8 cột)

| STT | Tên test case | Độ dài Username | Độ dài Password | Trạng thái User | Số lần thử sai | Kết quả mong đợi | Tag được bao phủ |
|:---:|:---|:---:|:---:|:---:|:---:|:---|:---|
| 1 | Baseline danh định (nominal) | 15 | 10 | 1 | 0 | Hợp lệ (True) | V1, V2, V3, V4, B3, B8, B13, B18 |
| 2 | Biên dưới hợp lệ userLength = min (3) | **3** | 10 | 1 | 0 | Hợp lệ (True) | B1 |
| 3 | Biên trên hợp lệ userLength = max (30) | **30** | 10 | 1 | 0 | Hợp lệ (True) | B5 |
| 4 | Ngoài biên dưới userLength < min (2) | **2** | 10 | 1 | 0 | Không hợp lệ (False): Tên đăng nhập nhỏ hơn 3 ký tự | X1 |
| 5 | Ngoài biên trên userLength > max (31) | **31** | 10 | 1 | 0 | Không hợp lệ (False): Tên đăng nhập lớn hơn 30 ký tự | X2 |
| 6 | Biên dưới hợp lệ passLength = min (6) | 15 | **6** | 1 | 0 | Hợp lệ (True) | B6 |
| 7 | Biên trên hợp lệ passLength = max (20) | 15 | **20** | 1 | 0 | Hợp lệ (True) | B10 |
| 8 | Ngoài biên dưới passLength < min (5) | 15 | **5** | 1 | 0 | Không hợp lệ (False): Mật khẩu nhỏ hơn 6 ký tự | X3 |
| 9 | Ngoài biên trên passLength > max (21) | 15 | **21** | 1 | 0 | Không hợp lệ (False): Mật khẩu lớn hơn 20 ký tự | X4 |
| 10 | Biên dưới hợp lệ userStatus = min (1) | 15 | 10 | **1** | 0 | Hợp lệ (True) | B11 |
| 11 | Biên trên hợp lệ userStatus = max (1) | 15 | 10 | **1** | 0 | Hợp lệ (True) | B15 |
| 12 | Ngoài biên dưới userStatus < min (0) | 15 | 10 | **0** | 0 | Không hợp lệ (False): Tài khoản bị khóa (status = 0) | X5 |
| 13 | Ngoài biên trên userStatus > max (2) | 15 | 10 | **2** | 0 | Không hợp lệ (False): Trạng thái tài khoản không hợp lệ | X6 |
| 14 | Biên dưới hợp lệ loginAttempts = min (0) | 15 | 10 | 1 | **0** | Hợp lệ (True) | B16 |
| 15 | Biên trên hợp lệ loginAttempts = max (4) | 15 | 10 | 1 | **4** | Hợp lệ (True) | B20 |
| 16 | Ngoài biên dưới loginAttempts < min (-1) | 15 | 10 | 1 | **-1** | Không hợp lệ (False): Số lần thử sai nhỏ hơn 0 | X7 |
| 17 | Ngoài biên trên loginAttempts > max (5) | 15 | 10 | 1 | **5** | Không hợp lệ (False): Đã đạt trần 5 lần thử sai | X8 |

---

### 3. Bổ sung: Bảng Quyết định xác thực (Collapsed Decision Table - 5 Rules)

| Condition / Action | Rule 1 (R1) | Rule 2 (R2) | Rule 3 (R3) | Rule 4 (R4) | Rule 5 (R5) |
| :--- | :---: | :---: | :---: | :---: | :---: |
| **C1: Username tồn tại trong hệ thống?** | **N** | Y | Y | Y | Y |
| **C2: Mật khẩu chính xác?** | - | **N** | Y | Y | Y |
| **C3: Tài khoản có đang hoạt động (Active)?** | - | - | **N** | Y | Y |
| **C4: Vai trò là Admin (`ROLE_ADMIN`)?** | - | - | - | **N** | **Y** |
| *A1: Báo lỗi "Không tìm thấy tài khoản"* | **X** | - | - | - | - |
| *A2: Báo lỗi "Sai mật khẩu"* | - | **X** | - | - | - |
| *A3: Báo lỗi "Tài khoản đang bị khóa"* | - | - | **X** | - | - |
| *A4: Đăng nhập thành công -> Vào Khách hàng UI* | - | - | - | **X** | - |
| *A5: Đăng nhập thành công -> Vào Admin Dashboard* | - | - | - | - | **X** |

---

## Câu 4. Triển khai kiểm thử tự động

```python
def ValidateAuthentication(userLength: int, passLength: int, userStatus: int, loginAttempts: int) -> bool:
    """
    Kiểm tra tính hợp lệ của đăng nhập & xác thực:
    - 3 <= userLength <= 30 (Độ dài tên đăng nhập)
    - 6 <= passLength <= 20 (Độ dài mật khẩu)
    - userStatus == 1 (1: Active, 0: Locked)
    - 0 <= loginAttempts <= 4 (Giới hạn tối đa 5 lần thử sai)
    Trả về True nếu tất cả điều kiện thỏa mãn, ngược lại False.
    """
    if not (isinstance(userLength, int) and not isinstance(userLength, bool) and 3 <= userLength <= 30):
        return False
    if not (isinstance(passLength, int) and not isinstance(passLength, bool) and 6 <= passLength <= 20):
        return False
    if not (isinstance(userStatus, int) and not isinstance(userStatus, bool) and userStatus == 1):
        return False
    if not (isinstance(loginAttempts, int) and not isinstance(loginAttempts, bool) and 0 <= loginAttempts <= 4):
        return False
    return True
```

```pytest
# thiết kế các test cases từ câu 3.
# Run test case 
import pytest

test_cases_m1 = [
    ("TC01", 15, 10, 1, 0, True, "V1, V2, V3, V4, B3, B8, B13, B18"),
    ("TC02", 3, 10, 1, 0, True, "B1"),
    ("TC03", 30, 10, 1, 0, True, "B5"),
    ("TC04", 2, 10, 1, 0, False, "X1"),
    ("TC05", 31, 10, 1, 0, False, "X2"),
    ("TC06", 15, 6, 1, 0, True, "B6"),
    ("TC07", 15, 20, 1, 0, True, "B10"),
    ("TC08", 15, 5, 1, 0, False, "X3"),
    ("TC09", 15, 21, 1, 0, False, "X4"),
    ("TC10", 15, 10, 1, 0, True, "B11"),
    ("TC11", 15, 10, 1, 0, True, "B15"),
    ("TC12", 15, 10, 0, 0, False, "X5"),
    ("TC13", 15, 10, 2, 0, False, "X6"),
    ("TC14", 15, 10, 1, 0, True, "B16"),
    ("TC15", 15, 10, 1, 4, True, "B20"),
    ("TC16", 15, 10, 1, -1, False, "X7"),
    ("TC17", 15, 10, 1, 5, False, "X8"),
]

@pytest.mark.parametrize("tc_id,uLen,pLen,uStat,lAtt,expected,tag", test_cases_m1)
def test_authentication_validation(tc_id, uLen, pLen, uStat, lAtt, expected, tag):
    """Kiểm thử tự động 17 test case xác thực theo nguyên lý 4n + 1."""
    assert ValidateAuthentication(uLen, pLen, uStat, lAtt) == expected

if __name__ == "__main__":
    pytest.main(["-v", __file__])
```

```kết quả test
============================= test session starts =============================
platform win32 -- Python 3.14.0, pytest-8.4.2, pluggy-1.6.0
rootdir: D:\LapTrinhAI\Testing
collected 17 items

test_authentication.py::test_authentication_validation[TC01-15-10-1-0-True-V1, V2, V3, V4, B3, B8, B13, B18] PASSED [  5%]
test_authentication.py::test_authentication_validation[TC02-3-10-1-0-True-B1] PASSED [ 11%]
test_authentication.py::test_authentication_validation[TC03-30-10-1-0-True-B5] PASSED [ 17%]
test_authentication.py::test_authentication_validation[TC04-2-10-1-0-False-X1] PASSED [ 23%]
test_authentication.py::test_authentication_validation[TC05-31-10-1-0-False-X2] PASSED [ 29%]
test_authentication.py::test_authentication_validation[TC06-15-6-1-0-True-B6] PASSED [ 35%]
test_authentication.py::test_authentication_validation[TC07-15-20-1-0-True-B10] PASSED [ 41%]
test_authentication.py::test_authentication_validation[TC08-15-5-1-0-False-X3] PASSED [ 47%]
test_authentication.py::test_authentication_validation[TC09-15-21-1-0-False-X4] PASSED [ 52%]
test_authentication.py::test_authentication_validation[TC10-15-10-1-0-True-B11] PASSED [ 58%]
test_authentication.py::test_authentication_validation[TC11-15-10-1-0-True-B15] PASSED [ 64%]
test_authentication.py::test_authentication_validation[TC12-15-10-0-0-False-X5] PASSED [ 70%]
test_authentication.py::test_authentication_validation[TC13-15-10-2-0-False-X6] PASSED [ 76%]
test_authentication.py::test_authentication_validation[TC14-15-10-1-0-True-B16] PASSED [ 82%]
test_authentication.py::test_authentication_validation[TC15-15-10-1-4-True-B20] PASSED [ 88%]
test_authentication.py::test_authentication_validation[TC16-15-10-1--1-False-X7] PASSED [ 94%]
test_authentication.py::test_authentication_validation[TC17-15-10-1-5-False-X8] PASSED [100%]

============================= 17 passed in 0.14s ==============================
```
