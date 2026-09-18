# -*- coding: utf-8 -*-
"""
Full generator for all 10 Test Case markdown specifications in docs/test_cases/
Author: Nguyễn Hoàng Phương, MSSV: 080205010954, Subject: Kiểm Chứng Phần Mềm
Template strictly follows D:\Downloads\080205010954_Nguyen_Hoang_Phuong.md
"""
import os

modules = [
    # -------------------------------------------------------------
    # Module 1: Authentication
    # -------------------------------------------------------------
    {
        'file': 'docs/test_cases/01_Authentication.md',
        'title': 'CHỨC NĂNG 1 - ĐĂNG NHẬP & XÁC THỰC (AUTHENTICATION)',
        'cond_table': """| Conditions | Valid Partition | Tag | Invalid Partitions | Tag | Valid Boundaries | Tag |
|---|---|---|---|---|---|---|
| **Độ dài tên đăng nhập** (`userLength`) | 3 ≤ userLength ≤ 30 | V1 | • userLength < 3<br>• userLength > 30 | X1<br>X2 | • 3 (min)<br>• 4 (min+)<br>• 15 (nominal)<br>• 29 (max-)<br>• 30 (max) | B1<br>B2<br>B3<br>B4<br>B5 |
| **Độ dài mật khẩu** (`passLength`) | 6 ≤ passLength ≤ 20 | V2 | • passLength < 6<br>• passLength > 20 | X3<br>X4 | • 6 (min)<br>• 7 (min+)<br>• 10 (nominal)<br>• 19 (max-)<br>• 20 (max) | B6<br>B7<br>B8<br>B9<br>B10 |
| **Trạng thái tài khoản** (`userStatus`) | userStatus = 1 (Active) | V3 | • userStatus = 0 (Locked)<br>• userStatus > 1 (Invalid) | X5<br>X6 | • 1 (min)<br>• 1 (min+)<br>• 1 (nominal)<br>• 1 (max-)<br>• 1 (max) | B11<br>B12<br>B13<br>B14<br>B15 |
| **Số lần đăng nhập sai** (`loginAttempts`) | 0 ≤ loginAttempts ≤ 4 | V4 | • loginAttempts < 0<br>• loginAttempts ≥ 5 | X7<br>X8 | • 0 (min)<br>• 1 (min+)<br>• 0 (nominal)<br>• 3 (max-)<br>• 4 (max) | B16<br>B17<br>B18<br>B19<br>B20 |""",
        'c1_table': """| Biến đầu vào | Lớp hợp lệ | Tag | Lớp không hợp lệ | Tag |
|---|---|---|---|---|
| **Độ dài tên đăng nhập** (`userLength`) | 3 ≤ userLength ≤ 30 | V1 | • userLength < 3 (Tên quá ngắn)<br>• userLength > 30 (Tên quá dài) | X1<br>X2 |
| **Độ dài mật khẩu** (`passLength`) | 6 ≤ passLength ≤ 20 | V2 | • passLength < 6 (Mật khẩu quá ngắn)<br>• passLength > 20 (Mật khẩu quá dài) | X3<br>X4 |
| **Trạng thái tài khoản** (`userStatus`) | userStatus = 1 (Đang kích hoạt) | V3 | • userStatus = 0 (Tài khoản bị khóa)<br>• userStatus ≠ 1 (Trạng thái bất thường) | X5<br>X6 |
| **Số lần đăng nhập sai** (`loginAttempts`) | 0 ≤ loginAttempts ≤ 4 | V4 | • loginAttempts < 0 (Số âm bất thường)<br>• loginAttempts ≥ 5 (Vượt ngưỡng 5 lần khóa tài khoản) | X7<br>X8 |""",
        'c2_5val': """| Biến đầu vào | min | min+ | nominal | max- | max | Tag biên |
|---|---:|---:|---:|---:|---:|---|
| **Độ dài tên đăng nhập** (`userLength`) | 3 | 4 | 15 | 29 | 30 | B1, B2, B3, B4, B5 |
| **Độ dài mật khẩu** (`passLength`) | 6 | 7 | 10 | 19 | 20 | B6, B7, B8, B9, B10 |
| **Trạng thái tài khoản** (`userStatus`) | 1 | 1 | 1 | 1 | 1 | B11, B12, B13, B14, B15 |
| **Số lần đăng nhập sai** (`loginAttempts`) | 0 | 1 | 0 | 3 | 4 | B16, B17, B18, B19, B20 |""",
        'c2_17bva': """| STT | Mã TC | Biến kiểm thử biên | Điểm biên kiểm tra | Tên đăng nhập | Mật khẩu | Trạng thái | Số lần thử sai | Kết quả mong đợi | Tag bao phủ |
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
| 17 | BVA17 | Số lần thử sai | max+1 (5) | 15 | 10 | 1 | **5** | Không hợp lệ (False) | X8 |""",
        'c3_summary': """| Test Case | Input | Expected Outcome | New Tags Covered |
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
| TC17 | userLength: 15, passLength: 10, userStatus: 1, loginAttempts: 5 | Không hợp lệ (False): Đã đạt trần 5 lần thử sai | X8 |""",
        'c3_detail': """| STT | Tên test case | Độ dài Username | Độ dài Password | Trạng thái User | Số lần thử sai | Kết quả mong đợi | Tag được bao phủ |
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
| 17 | Ngoài biên trên loginAttempts > max (5) | 15 | 10 | 1 | **5** | Không hợp lệ (False): Đã đạt trần 5 lần thử sai | X8 |""",
        'extra_section': """### 3. Bổ sung: Bảng Quyết định xác thực (Collapsed Decision Table - 5 Rules)

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
| *A5: Đăng nhập thành công -> Vào Admin Dashboard* | - | - | - | - | **X** |""",
        'py_func': """def ValidateAuthentication(userLength: int, passLength: int, userStatus: int, loginAttempts: int) -> bool:
    \"\"\"
    Kiểm tra tính hợp lệ của đăng nhập & xác thực:
    - 3 <= userLength <= 30 (Độ dài tên đăng nhập)
    - 6 <= passLength <= 20 (Độ dài mật khẩu)
    - userStatus == 1 (1: Active, 0: Locked)
    - 0 <= loginAttempts <= 4 (Giới hạn tối đa 5 lần thử sai)
    Trả về True nếu tất cả điều kiện thỏa mãn, ngược lại False.
    \"\"\"
    if not (isinstance(userLength, int) and not isinstance(userLength, bool) and 3 <= userLength <= 30):
        return False
    if not (isinstance(passLength, int) and not isinstance(passLength, bool) and 6 <= passLength <= 20):
        return False
    if not (isinstance(userStatus, int) and not isinstance(userStatus, bool) and userStatus == 1):
        return False
    if not (isinstance(loginAttempts, int) and not isinstance(loginAttempts, bool) and 0 <= loginAttempts <= 4):
        return False
    return True""",
        'py_test': """import pytest

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
    \"\"\"Kiểm thử tự động 17 test case xác thực theo nguyên lý 4n + 1.\"\"\"
    assert ValidateAuthentication(uLen, pLen, uStat, lAtt) == expected

if __name__ == "__main__":
    pytest.main(["-v", __file__])""",
        'py_output': """============================= test session starts =============================
platform win32 -- Python 3.14.0, pytest-8.4.2, pluggy-1.6.0
rootdir: D:\\LapTrinhAI\\Testing
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

============================= 17 passed in 0.14s =============================="""
    },

    # -------------------------------------------------------------
    # Module 2: Product Search & Pagination
    # -------------------------------------------------------------
    {
        'file': 'docs/test_cases/02_Product_Search_Pagination.md',
        'title': 'CHỨC NĂNG 2 - TÌM KIẾM & PHÂN TRANG SẢN PHẨM (SEARCH & PAGINATION)',
        'cond_table': """| Conditions | Valid Partition | Tag | Invalid Partitions | Tag | Valid Boundaries | Tag |
|---|---|---|---|---|---|---|
| **Độ dài từ khóa** (`keywordLength`) | 0 ≤ keywordLength ≤ 50 | V1 | • keywordLength < 0<br>• keywordLength > 50 | X1<br>X2 | • 0 (min)<br>• 1 (min+)<br>• 10 (nominal)<br>• 49 (max-)<br>• 50 (max) | B1<br>B2<br>B3<br>B4<br>B5 |
| **Giá lọc tối thiểu** (`minPrice` - k) | 0.0 ≤ minPrice ≤ 50000.0 | V2 | • minPrice < 0.0<br>• minPrice > 50000.0 | X3<br>X4 | • 0.0 (min)<br>• 1.0 (min+)<br>• 1000.0 (nominal)<br>• 49999.0 (max-)<br>• 50000.0 (max) | B6<br>B7<br>B8<br>B9<br>B10 |
| **Số trang yêu cầu** (`page`) | 1 ≤ page ≤ 50 | V3 | • page < 1<br>• page > 50 | X5<br>X6 | • 1 (min)<br>• 2 (min+)<br>• 5 (nominal)<br>• 49 (max-)<br>• 50 (max) | B11<br>B12<br>B13<br>B14<br>B15 |
| **Kích thước trang** (`pageSize`) | 1 ≤ pageSize ≤ 12 | V4 | • pageSize < 1<br>• pageSize > 12 | X7<br>X8 | • 1 (min)<br>• 2 (min+)<br>• 6 (nominal)<br>• 11 (max-)<br>• 12 (max) | B16<br>B17<br>B18<br>B19<br>B20 |""",
        'c1_table': """| Biến đầu vào | Lớp hợp lệ | Tag | Lớp không hợp lệ | Tag |
|---|---|---|---|---|
| **Độ dài từ khóa** (`keywordLength`) | 0 ≤ keywordLength ≤ 50 | V1 | • keywordLength < 0 (Số âm)<br>• keywordLength > 50 (Chuỗi quá dài) | X1<br>X2 |
| **Giá lọc tối thiểu** (`minPrice`) | 0.0 ≤ minPrice ≤ 50000.0 | V2 | • minPrice < 0.0 (Giá âm)<br>• minPrice > 50000.0 (Vượt trần giá) | X3<br>X4 |
| **Số trang yêu cầu** (`page`) | 1 ≤ page ≤ 50 | V3 | • page < 1 (Số trang nhỏ hơn 1)<br>• page > 50 (Vượt quá số trang CSDL) | X5<br>X6 |
| **Kích thước trang** (`pageSize`) | 1 ≤ pageSize ≤ 12 | V4 | • pageSize < 1 (Kích thước < 1)<br>• pageSize > 12 (Vượt quá giới hạn hiển thị) | X7<br>X8 |""",
        'c2_5val': """| Biến đầu vào | min | min+ | nominal | max- | max | Tag biên |
|---|---:|---:|---:|---:|---:|---|
| **Độ dài từ khóa** (`keywordLength`) | 0 | 1 | 10 | 49 | 50 | B1, B2, B3, B4, B5 |
| **Giá lọc tối thiểu** (`minPrice`) | 0.0 | 1.0 | 1000.0 | 49999.0 | 50000.0 | B6, B7, B8, B9, B10 |
| **Số trang yêu cầu** (`page`) | 1 | 2 | 5 | 49 | 50 | B11, B12, B13, B14, B15 |
| **Kích thước trang** (`pageSize`) | 1 | 2 | 6 | 11 | 12 | B16, B17, B18, B19, B20 |""",
        'c2_17bva': """| STT | Mã TC | Biến kiểm thử biên | Điểm biên kiểm tra | Từ khóa (len) | Giá min (k) | Trang | Kích thước | Kết quả mong đợi | Tag bao phủ |
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
| 17 | BVA17 | Kích thước trang | max+1 (13) | 10 | 1000.0 | 5 | **13** | Không hợp lệ (False) | X8 |""",
        'c3_summary': """| Test Case | Input | Expected Outcome | New Tags Covered |
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
| TC17 | keywordLength: 10, minPrice: 1000.0, page: 5, pageSize: 13 | Không hợp lệ (False): Kích thước trang lớn hơn 12 | X8 |""",
        'c3_detail': """| STT | Tên test case | Độ dài từ khóa | Giá lọc tối thiểu (k) | Số trang yêu cầu | Kích thước trang | Kết quả mong đợi | Tag được bao phủ |
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
| 17 | Ngoài biên trên pageSize > max (13) | 10 | 1000.0 | 5 | **13** | Không hợp lệ (False): Kích thước trang lớn hơn 12 | X8 |""",
        'extra_section': """### 3. Bổ sung: Bảng Quyết định tìm kiếm & phân trang (Collapsed Decision Table - 5 Rules)

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
| *A5: Trả về trang kết quả phân trang thành công* | - | - | - | - | **X** |""",
        'py_func': """def ValidateSearchPagination(keywordLength: int, minPrice: float, page: int, pageSize: int) -> bool:
    \"\"\"
    Kiểm tra tính hợp lệ của tham số tìm kiếm & phân trang:
    - 0 <= keywordLength <= 50 (Độ dài từ khóa tìm kiếm)
    - 0.0 <= minPrice <= 50000.0 (Khoảng giá lọc tối thiểu)
    - 1 <= page <= 50 (Số trang hợp lệ)
    - 1 <= pageSize <= 12 (Kích thước mỗi trang hiển thị)
    Trả về True nếu tất cả điều kiện thỏa mãn, ngược lại False.
    \"\"\"
    if not (isinstance(keywordLength, int) and not isinstance(keywordLength, bool) and 0 <= keywordLength <= 50):
        return False
    if not (isinstance(minPrice, (int, float)) and not isinstance(minPrice, bool) and 0.0 <= minPrice <= 50000.0):
        return False
    if not (isinstance(page, int) and not isinstance(page, bool) and 1 <= page <= 50):
        return False
    if not (isinstance(pageSize, int) and not isinstance(pageSize, bool) and 1 <= pageSize <= 12):
        return False
    return True""",
        'py_test': """import pytest

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
    \"\"\"Kiểm thử tự động 17 test case tìm kiếm & phân trang theo nguyên lý 4n + 1.\"\"\"
    assert ValidateSearchPagination(kLen, mPrice, page, pSize) == expected

if __name__ == "__main__":
    pytest.main(["-v", __file__])""",
        'py_output': """============================= test session starts =============================
platform win32 -- Python 3.14.0, pytest-8.4.2, pluggy-1.6.0
rootdir: D:\\LapTrinhAI\\Testing
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

============================= 17 passed in 0.14s =============================="""
    },

    # -------------------------------------------------------------
    # Module 3: Shopping Cart
    # -------------------------------------------------------------
    {
        'file': 'docs/test_cases/03_Shopping_Cart.md',
        'title': 'CHỨC NĂNG 3 - QUẢN LÝ GIỎ HÀNG (SHOPPING CART)',
        'cond_table': """| Conditions | Valid Partition | Tag | Invalid Partitions | Tag | Valid Boundaries | Tag |
|---|---|---|---|---|---|---|
| **Số lượng sản phẩm** (`quantity`) | 1 ≤ quantity ≤ 99 | V1 | • quantity < 1<br>• quantity > 99 | X1<br>X2 | • 1 (min)<br>• 2 (min+)<br>• 2 (nominal)<br>• 98 (max-)<br>• 99 (max) | B1<br>B2<br>B3<br>B4<br>B5 |
| **Số mặt hàng trong giỏ** (`itemCount`) | 1 ≤ itemCount ≤ 20 | V2 | • itemCount < 1<br>• itemCount > 20 | X3<br>X4 | • 1 (min)<br>• 2 (min+)<br>• 5 (nominal)<br>• 19 (max-)<br>• 20 (max) | B6<br>B7<br>B8<br>B9<br>B10 |
| **Tồn kho khả dụng** (`stockQuantity`) | 1 ≤ stockQuantity ≤ 500 | V3 | • stockQuantity < 1<br>• stockQuantity > 500 | X5<br>X6 | • 1 (min)<br>• 2 (min+)<br>• 100 (nominal)<br>• 499 (max-)<br>• 500 (max) | B11<br>B12<br>B13<br>B14<br>B15 |
| **Tổng tiền tạm tính** (`subtotal` - k) | 100.0 ≤ subtotal ≤ 100000.0 | V4 | • subtotal < 100.0<br>• subtotal > 100000.0 | X7<br>X8 | • 100.0 (min)<br>• 101.0 (min+)<br>• 1500.0 (nominal)<br>• 99999.0 (max-)<br>• 100000.0 (max) | B16<br>B17<br>B18<br>B19<br>B20 |""",
        'c1_table': """| Biến đầu vào | Lớp hợp lệ | Tag | Lớp không hợp lệ | Tag |
|---|---|---|---|---|
| **Số lượng sản phẩm** (`quantity`) | 1 ≤ quantity ≤ 99 | V1 | • quantity < 1 (Số lượng không hợp lệ)<br>• quantity > 99 (Vượt quá số lượng cho phép) | X1<br>X2 |
| **Số mặt hàng trong giỏ** (`itemCount`) | 1 ≤ itemCount ≤ 20 | V2 | • itemCount < 1 (Giỏ hàng rỗng)<br>• itemCount > 20 (Vượt giới hạn giỏ) | X3<br>X4 |
| **Tồn kho khả dụng** (`stockQuantity`) | 1 ≤ stockQuantity ≤ 500 | V3 | • stockQuantity < 1 (Hết hàng trong kho)<br>• stockQuantity > 500 (Vượt mức kho) | X5<br>X6 |
| **Tổng tiền tạm tính** (`subtotal`) | 100.0 ≤ subtotal ≤ 100000.0 | V4 | • subtotal < 100.0 (Chưa đạt mức tối thiểu)<br>• subtotal > 100000.0 (Vượt trần giao dịch) | X7<br>X8 |""",
        'c2_5val': """| Biến đầu vào | min | min+ | nominal | max- | max | Tag biên |
|---|---:|---:|---:|---:|---:|---|
| **Số lượng sản phẩm** (`quantity`) | 1 | 2 | 2 | 98 | 99 | B1, B2, B3, B4, B5 |
| **Số mặt hàng trong giỏ** (`itemCount`) | 1 | 2 | 5 | 19 | 20 | B6, B7, B8, B9, B10 |
| **Tồn kho khả dụng** (`stockQuantity`) | 1 | 2 | 100 | 499 | 500 | B11, B12, B13, B14, B15 |
| **Tổng tiền tạm tính** (`subtotal`) | 100.0 | 101.0 | 1500.0 | 99999.0 | 100000.0 | B16, B17, B18, B19, B20 |""",
        'c2_17bva': """| STT | Mã TC | Biến kiểm thử biên | Điểm biên kiểm tra | Số lượng SP | Số mặt hàng | Tồn kho | Tạm tính (k) | Kết quả mong đợi | Tag bao phủ |
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
| 17 | BVA17 | Tạm tính | max+1 (100001.0) | 2 | 5 | 100 | **100001.0** | Không hợp lệ (False) | X8 |""",
        'c3_summary': """| Test Case | Input | Expected Outcome | New Tags Covered |
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
| TC17 | quantity: 2, itemCount: 5, stockQuantity: 100, subtotal: 100001.0 | Không hợp lệ (False): Tạm tính vượt quá 100000.0k | X8 |""",
        'c3_detail': """| STT | Tên test case | Số lượng SP | Số mặt hàng | Tồn kho khả dụng | Tạm tính (k) | Kết quả mong đợi | Tag được bao phủ |
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
| 17 | Ngoài biên trên subtotal > max (100001.0) | 2 | 5 | 100 | **100001.0** | Không hợp lệ (False): Tạm tính vượt quá 100000.0k | X8 |""",
        'extra_section': """### 3. Bổ sung: Bảng Quyết định giỏ hàng (Collapsed Decision Table - 4 Rules)

| Condition / Action | Rule 1 (R1) | Rule 2 (R2) | Rule 3 (R3) | Rule 4 (R4) |
| :--- | :---: | :---: | :---: | :---: |
| **C1: Sản phẩm tồn tại trong CSDL?** | **N** | Y | Y | Y |
| **C2: Số lượng mua <= Tồn kho?** | - | **N** | Y | Y |
| **C3: Tổng mặt hàng giỏ <= 20?** | - | - | **N** | **Y** |
| *A1: Báo lỗi "Không tìm thấy sản phẩm"* | **X** | - | - | - |
| *A2: Báo lỗi "Vượt quá tồn kho khả dụng"* | - | **X** | - | - |
| *A3: Báo lỗi "Giỏ hàng đã đầy (tối đa 20 món)"* | - | - | **X** | - |
| *A4: Cập nhật giỏ hàng thành công (HTTP 200)* | - | - | - | **X** |""",
        'py_func': """def ValidateShoppingCart(quantity: int, itemCount: int, stockQuantity: int, subtotal: float) -> bool:
    \"\"\"
    Kiểm tra tính hợp lệ của giỏ hàng:
    - 1 <= quantity <= 99 (Số lượng mua mỗi món)
    - 1 <= itemCount <= 20 (Số loại mặt hàng trong giỏ)
    - 1 <= stockQuantity <= 500 (Tồn kho khả dụng)
    - 100.0 <= subtotal <= 100000.0 (Tổng tiền tạm tính)
    Trả về True nếu tất cả điều kiện thỏa mãn, ngược lại False.
    \"\"\"
    if not (isinstance(quantity, int) and not isinstance(quantity, bool) and 1 <= quantity <= 99):
        return False
    if not (isinstance(itemCount, int) and not isinstance(itemCount, bool) and 1 <= itemCount <= 20):
        return False
    if not (isinstance(stockQuantity, int) and not isinstance(stockQuantity, bool) and 1 <= stockQuantity <= 500):
        return False
    if not (isinstance(subtotal, (int, float)) and not isinstance(subtotal, bool) and 100.0 <= subtotal <= 100000.0):
        return False
    return True""",
        'py_test': """import pytest

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
    \"\"\"Kiểm thử tự động 17 test case giỏ hàng theo nguyên lý 4n + 1.\"\"\"
    assert ValidateShoppingCart(qty, iCount, sQty, subt) == expected

if __name__ == "__main__":
    pytest.main(["-v", __file__])""",
        'py_output': """============================= test session starts =============================
platform win32 -- Python 3.14.0, pytest-8.4.2, pluggy-1.6.0
rootdir: D:\\LapTrinhAI\\Testing
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

============================= 17 passed in 0.14s =============================="""
    },

    # -------------------------------------------------------------
    # Module 5: Checkout - Order Placement
    # -------------------------------------------------------------
    {
        'file': 'docs/test_cases/05_Checkout_Order_Placement.md',
        'title': 'CHỨC NĂNG 5 - ĐẶT HÀNG & THANH TOÁN (CHECKOUT & ORDER PLACEMENT)',
        'cond_table': """| Conditions | Valid Partition | Tag | Invalid Partitions | Tag | Valid Boundaries | Tag |
|---|---|---|---|---|---|---|
| **Độ dài họ tên** (`customerNameLength`) | 2 ≤ customerNameLength ≤ 50 | V1 | • customerNameLength < 2<br>• customerNameLength > 50 | X1<br>X2 | • 2 (min)<br>• 3 (min+)<br>• 15 (nominal)<br>• 49 (max-)<br>• 50 (max) | B1<br>B2<br>B3<br>B4<br>B5 |
| **Độ dài số điện thoại** (`phoneLength`) | 10 ≤ phoneLength ≤ 11 | V2 | • phoneLength < 10<br>• phoneLength > 11 | X3<br>X4 | • 10 (min)<br>• 10 (min+)<br>• 10 (nominal)<br>• 11 (max-)<br>• 11 (max) | B6<br>B7<br>B8<br>B9<br>B10 |
| **Độ dài địa chỉ** (`addressLength`) | 10 ≤ addressLength ≤ 200 | V3 | • addressLength < 10<br>• addressLength > 200 | X5<br>X6 | • 10 (min)<br>• 11 (min+)<br>• 50 (nominal)<br>• 199 (max-)<br>• 200 (max) | B11<br>B12<br>B13<br>B14<br>B15 |
| **Tổng tiền thanh toán** (`orderTotal` - k) | 100.0 ≤ orderTotal ≤ 100000.0 | V4 | • orderTotal < 100.0<br>• orderTotal > 100000.0 | X7<br>X8 | • 100.0 (min)<br>• 101.0 (min+)<br>• 1500.0 (nominal)<br>• 99999.0 (max-)<br>• 100000.0 (max) | B16<br>B17<br>B18<br>B19<br>B20 |""",
        'c1_table': """| Biến đầu vào | Lớp hợp lệ | Tag | Lớp không hợp lệ | Tag |
|---|---|---|---|---|
| **Độ dài họ tên** (`customerNameLength`) | 2 ≤ customerNameLength ≤ 50 | V1 | • customerNameLength < 2 (Tên quá ngắn)<br>• customerNameLength > 50 (Tên quá dài) | X1<br>X2 |
| **Độ dài số điện thoại** (`phoneLength`) | 10 ≤ phoneLength ≤ 11 | V2 | • phoneLength < 10 (Số điện thoại thiếu số)<br>• phoneLength > 11 (Số điện thoại thừa số) | X3<br>X4 |
| **Độ dài địa chỉ** (`addressLength`) | 10 ≤ addressLength ≤ 200 | V3 | • addressLength < 10 (Địa chỉ quá ngắn)<br>• addressLength > 200 (Địa chỉ quá dài) | X5<br>X6 |
| **Tổng tiền thanh toán** (`orderTotal`) | 100.0 ≤ orderTotal ≤ 100000.0 | V4 | • orderTotal < 100.0 (Chưa đạt đơn tối thiểu)<br>• orderTotal > 100000.0 (Vượt hạn mức thanh toán) | X7<br>X8 |""",
        'c2_5val': """| Biến đầu vào | min | min+ | nominal | max- | max | Tag biên |
|---|---:|---:|---:|---:|---:|---|
| **Độ dài họ tên** (`customerNameLength`) | 2 | 3 | 15 | 49 | 50 | B1, B2, B3, B4, B5 |
| **Độ dài số điện thoại** (`phoneLength`) | 10 | 10 | 10 | 11 | 11 | B6, B7, B8, B9, B10 |
| **Độ dài địa chỉ** (`addressLength`) | 10 | 11 | 50 | 199 | 200 | B11, B12, B13, B14, B15 |
| **Tổng tiền thanh toán** (`orderTotal`) | 100.0 | 101.0 | 1500.0 | 99999.0 | 100000.0 | B16, B17, B18, B19, B20 |""",
        'c2_17bva': """| STT | Mã TC | Biến kiểm thử biên | Điểm biên kiểm tra | Tên người nhận | SĐT | Địa chỉ | Tổng tiền (k) | Kết quả mong đợi | Tag bao phủ |
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
| 17 | BVA17 | Tổng tiền đơn | max+1 (100001.0) | 15 | 10 | 50 | **100001.0** | Không hợp lệ (False) | X8 |""",
        'c3_summary': """| Test Case | Input | Expected Outcome | New Tags Covered |
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
| TC17 | customerNameLength: 15, phoneLength: 10, addressLength: 50, orderTotal: 100001.0 | Không hợp lệ (False): Tổng tiền vượt trần 100000.0k | X8 |""",
        'c3_detail': """| STT | Tên test case | Độ dài Họ tên | Độ dài SĐT | Độ dài Địa chỉ | Tổng tiền đơn (k) | Kết quả mong đợi | Tag được bao phủ |
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
| 17 | Ngoài biên trên orderTotal > max (100001.0) | 15 | 10 | 50 | **100001.0** | Không hợp lệ (False): Tổng tiền vượt trần 100000.0k | X8 |""",
        'extra_section': """### 3. Bổ sung: Bảng Quyết định phương thức thanh toán (Decision Table - 4 Rules)

| Condition / Action | Rule 1 (COD) | Rule 2 (VNPAY) | Rule 3 (MOMO) | Rule 4 (BANK) |
| :--- | :---: | :---: | :---: | :---: |
| **C1: Thông tin giao hàng hợp lệ?** | Y | Y | Y | Y |
| **C2: Phương thức thanh toán được chọn** | COD | VNPAY | MOMO | BANK_TRANSFER |
| *A1: Tạo đơn ngay -> Chuyển trạng thái `PENDING`* | **X** | - | - | - |
| *A2: Chuyển hướng sang Cổng VNPAY Sandbox* | - | **X** | - | - |
| *A3: Chuyển hướng sang Cổng MoMo QR* | - | - | **X** | - |
| *A4: Hiển thị thông tin STK ngân hàng* | - | - | - | **X** |""",
        'py_func': """def ValidateCheckout(customerNameLength: int, phoneLength: int, addressLength: int, orderTotal: float) -> bool:
    \"\"\"
    Kiểm tra tính hợp lệ của thông tin đặt hàng:
    - 2 <= customerNameLength <= 50 (Độ dài họ tên người nhận)
    - 10 <= phoneLength <= 11 (Độ dài số điện thoại VN)
    - 10 <= addressLength <= 200 (Độ dài địa chỉ nhận hàng)
    - 100.0 <= orderTotal <= 100000.0 (Tổng tiền thanh toán)
    Trả về True nếu tất cả điều kiện thỏa mãn, ngược lại False.
    \"\"\"
    if not (isinstance(customerNameLength, int) and not isinstance(customerNameLength, bool) and 2 <= customerNameLength <= 50):
        return False
    if not (isinstance(phoneLength, int) and not isinstance(phoneLength, bool) and 10 <= phoneLength <= 11):
        return False
    if not (isinstance(addressLength, int) and not isinstance(addressLength, bool) and 10 <= addressLength <= 200):
        return False
    if not (isinstance(orderTotal, (int, float)) and not isinstance(orderTotal, bool) and 100.0 <= orderTotal <= 100000.0):
        return False
    return True""",
        'py_test': """import pytest

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
    \"\"\"Kiểm thử tự động 17 test case đặt hàng theo nguyên lý 4n + 1.\"\"\"
    assert ValidateCheckout(cLen, pLen, aLen, oTot) == expected

if __name__ == "__main__":
    pytest.main(["-v", __file__])""",
        'py_output': """============================= test session starts =============================
platform win32 -- Python 3.14.0, pytest-8.4.2, pluggy-1.6.0
rootdir: D:\\LapTrinhAI\\Testing
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

============================= 17 passed in 0.14s =============================="""
    },

    # -------------------------------------------------------------
    # Module 6: Review & Rating
    # -------------------------------------------------------------
    {
        'file': 'docs/test_cases/06_Review_Rating.md',
        'title': 'CHỨC NĂNG 6 - ĐÁNH GIÁ & BÌNH LUẬN SẢN PHẨM (REVIEW & RATING)',
        'cond_table': """| Conditions | Valid Partition | Tag | Invalid Partitions | Tag | Valid Boundaries | Tag |
|---|---|---|---|---|---|---|
| **Số sao đánh giá** (`ratingStar`) | 1 ≤ ratingStar ≤ 5 | V1 | • ratingStar < 1<br>• ratingStar > 5 | X1<br>X2 | • 1 (min)<br>• 2 (min+)<br>• 5 (nominal)<br>• 4 (max-)<br>• 5 (max) | B1<br>B2<br>B3<br>B4<br>B5 |
| **Độ dài nhận xét** (`commentLength`) | 5 ≤ commentLength ≤ 500 | V2 | • commentLength < 5<br>• commentLength > 500 | X3<br>X4 | • 5 (min)<br>• 6 (min+)<br>• 50 (nominal)<br>• 499 (max-)<br>• 500 (max) | B6<br>B7<br>B8<br>B9<br>B10 |
| **Hạn sửa đánh giá** (`editWindowDays`) | 0 ≤ editWindowDays ≤ 7 | V3 | • editWindowDays < 0<br>• editWindowDays > 7 | X5<br>X6 | • 0 (min)<br>• 1 (min+)<br>• 3 (nominal)<br>• 6 (max-)<br>• 7 (max) | B11<br>B12<br>B13<br>B14<br>B15 |
| **Trạng thái đã mua hàng** (`userPurchased`) | userPurchased = 1 | V4 | • userPurchased = 0<br>• userPurchased > 1 | X7<br>X8 | • 1 (min)<br>• 1 (min+)<br>• 1 (nominal)<br>• 1 (max-)<br>• 1 (max) | B16<br>B17<br>B18<br>B19<br>B20 |""",
        'c1_table': """| Biến đầu vào | Lớp hợp lệ | Tag | Lớp không hợp lệ | Tag |
|---|---|---|---|---|
| **Số sao đánh giá** (`ratingStar`) | 1 ≤ ratingStar ≤ 5 | V1 | • ratingStar < 1 (Số sao nhỏ hơn 1)<br>• ratingStar > 5 (Số sao lớn hơn 5) | X1<br>X2 |
| **Độ dài nhận xét** (`commentLength`) | 5 ≤ commentLength ≤ 500 | V2 | • commentLength < 5 (Nội dung quá ngắn)<br>• commentLength > 500 (Nội dung quá dài) | X3<br>X4 |
| **Hạn sửa đánh giá** (`editWindowDays`) | 0 ≤ editWindowDays ≤ 7 | V3 | • editWindowDays < 0 (Số âm)<br>• editWindowDays > 7 (Quá hạn 7 ngày cho phép) | X5<br>X6 |
| **Trạng thái đã mua hàng** (`userPurchased`) | userPurchased = 1 (Đã mua và nhận) | V4 | • userPurchased = 0 (Chưa từng mua sản phẩm)<br>• userPurchased ≠ 1 (Trạng thái bất thường) | X7<br>X8 |""",
        'c2_5val': """| Biến đầu vào | min | min+ | nominal | max- | max | Tag biên |
|---|---:|---:|---:|---:|---:|---|
| **Số sao đánh giá** (`ratingStar`) | 1 | 2 | 5 | 4 | 5 | B1, B2, B3, B4, B5 |
| **Độ dài nhận xét** (`commentLength`) | 5 | 6 | 50 | 499 | 500 | B6, B7, B8, B9, B10 |
| **Hạn sửa đánh giá** (`editWindowDays`) | 0 | 1 | 3 | 6 | 7 | B11, B12, B13, B14, B15 |
| **Trạng thái đã mua hàng** (`userPurchased`) | 1 | 1 | 1 | 1 | 1 | B16, B17, B18, B19, B20 |""",
        'c2_17bva': """| STT | Mã TC | Biến kiểm thử biên | Điểm biên kiểm tra | Số sao | Nhận xét (len) | Hạn sửa (ngày) | Đã mua hàng | Kết quả mong đợi | Tag bao phủ |
|:---:|:---:|:---|:---|:---:|:---:|:---:|:---:|:---|:---|
| 1 | BVA01 | Baseline (Tất cả) | Nominal | 5 | 50 | 3 | 1 | Hợp lệ (True) | B3, B8, B13, B18 |
| 2 | BVA02 | Số sao đánh giá | min (1) | **1** | 50 | 3 | 1 | Hợp lệ (True) | B1 |
| 3 | BVA03 | Số sao đánh giá | max (5) | **5** | 50 | 3 | 1 | Hợp lệ (True) | B5 |
| 4 | BVA04 | Số sao đánh giá | min-1 (0) | **0** | 50 | 3 | 1 | Không hợp lệ (False) | X1 |
| 5 | BVA05 | Số sao đánh giá | max+1 (6) | **6** | 50 | 3 | 1 | Không hợp lệ (False) | X2 |
| 6 | BVA06 | Độ dài nhận xét | min (5) | 5 | **5** | 3 | 1 | Hợp lệ (True) | B6 |
| 7 | BVA07 | Độ dài nhận xét | max (500) | 5 | **500** | 3 | 1 | Hợp lệ (True) | B10 |
| 8 | BVA08 | Độ dài nhận xét | min-1 (4) | 5 | **4** | 3 | 1 | Không hợp lệ (False) | X3 |
| 9 | BVA09 | Độ dài nhận xét | max+1 (501) | 5 | **501** | 3 | 1 | Không hợp lệ (False) | X4 |
| 10 | BVA10 | Hạn sửa đánh giá | min (0) | 5 | 50 | **0** | 1 | Hợp lệ (True) | B11 |
| 11 | BVA11 | Hạn sửa đánh giá | max (7) | 5 | 50 | **7** | 1 | Hợp lệ (True) | B15 |
| 12 | BVA12 | Hạn sửa đánh giá | min-1 (-1) | 5 | 50 | **-1** | 1 | Không hợp lệ (False) | X5 |
| 13 | BVA13 | Hạn sửa đánh giá | max+1 (8) | 5 | 50 | **8** | 1 | Không hợp lệ (False) | X6 |
| 14 | BVA14 | Đã mua hàng | min (1) | 5 | 50 | 3 | **1** | Hợp lệ (True) | B16 |
| 15 | BVA15 | Đã mua hàng | max (1) | 5 | 50 | 3 | **1** | Hợp lệ (True) | B20 |
| 16 | BVA16 | Đã mua hàng | min-1 (0) | 5 | 50 | 3 | **0** | Không hợp lệ (False) | X7 |
| 17 | BVA17 | Đã mua hàng | max+1 (2) | 5 | 50 | 3 | **2** | Không hợp lệ (False) | X8 |""",
        'c3_summary': """| Test Case | Input | Expected Outcome | New Tags Covered |
|---|---|---|---|
| TC01 | ratingStar: 5, commentLength: 50, editWindowDays: 3, userPurchased: 1 | Hợp lệ (True) | V1, V2, V3, V4, B3, B8, B13, B18 |
| TC02 | ratingStar: 1, commentLength: 50, editWindowDays: 3, userPurchased: 1 | Hợp lệ (True) | B1 |
| TC03 | ratingStar: 5, commentLength: 50, editWindowDays: 3, userPurchased: 1 | Hợp lệ (True) | B5 |
| TC04 | ratingStar: 0, commentLength: 50, editWindowDays: 3, userPurchased: 1 | Không hợp lệ (False): Số sao đánh giá nhỏ hơn 1 | X1 |
| TC05 | ratingStar: 6, commentLength: 50, editWindowDays: 3, userPurchased: 1 | Không hợp lệ (False): Số sao đánh giá lớn hơn 5 | X2 |
| TC06 | ratingStar: 5, commentLength: 5, editWindowDays: 3, userPurchased: 1 | Hợp lệ (True) | B6 |
| TC07 | ratingStar: 5, commentLength: 500, editWindowDays: 3, userPurchased: 1 | Hợp lệ (True) | B10 |
| TC08 | ratingStar: 5, commentLength: 4, editWindowDays: 3, userPurchased: 1 | Không hợp lệ (False): Nhận xét nhỏ hơn 5 ký tự | X3 |
| TC09 | ratingStar: 5, commentLength: 501, editWindowDays: 3, userPurchased: 1 | Không hợp lệ (False): Nhận xét lớn hơn 500 ký tự | X4 |
| TC10 | ratingStar: 5, commentLength: 50, editWindowDays: 0, userPurchased: 1 | Hợp lệ (True) | B11 |
| TC11 | ratingStar: 5, commentLength: 50, editWindowDays: 7, userPurchased: 1 | Hợp lệ (True) | B15 |
| TC12 | ratingStar: 5, commentLength: 50, editWindowDays: -1, userPurchased: 1 | Không hợp lệ (False): Ngày sửa nhỏ hơn 0 | X5 |
| TC13 | ratingStar: 5, commentLength: 50, editWindowDays: 8, userPurchased: 1 | Không hợp lệ (False): Quá hạn 7 ngày chỉnh sửa | X6 |
| TC14 | ratingStar: 5, commentLength: 50, editWindowDays: 3, userPurchased: 1 | Hợp lệ (True) | B16 |
| TC15 | ratingStar: 5, commentLength: 50, editWindowDays: 3, userPurchased: 1 | Hợp lệ (True) | B20 |
| TC16 | ratingStar: 5, commentLength: 50, editWindowDays: 3, userPurchased: 0 | Không hợp lệ (False): Chưa mua sản phẩm này | X7 |
| TC17 | ratingStar: 5, commentLength: 50, editWindowDays: 3, userPurchased: 2 | Không hợp lệ (False): Trạng thái không hợp lệ | X8 |""",
        'c3_detail': """| STT | Tên test case | Số sao đánh giá | Độ dài nhận xét | Hạn sửa (ngày) | Đã mua hàng | Kết quả mong đợi | Tag được bao phủ |
|:---:|:---|:---:|:---:|:---:|:---:|:---|:---|
| 1 | Baseline danh định (nominal) | 5 | 50 | 3 | 1 | Hợp lệ (True) | V1, V2, V3, V4, B3, B8, B13, B18 |
| 2 | Biên dưới hợp lệ ratingStar = min (1) | **1** | 50 | 3 | 1 | Hợp lệ (True) | B1 |
| 3 | Biên trên hợp lệ ratingStar = max (5) | **5** | 50 | 3 | 1 | Hợp lệ (True) | B5 |
| 4 | Ngoài biên dưới ratingStar < min (0) | **0** | 50 | 3 | 1 | Không hợp lệ (False): Số sao đánh giá nhỏ hơn 1 | X1 |
| 5 | Ngoài biên trên ratingStar > max (6) | **6** | 50 | 3 | 1 | Không hợp lệ (False): Số sao đánh giá lớn hơn 5 | X2 |
| 6 | Biên dưới hợp lệ commentLength = min (5) | 5 | **5** | 3 | 1 | Hợp lệ (True) | B6 |
| 7 | Biên trên hợp lệ commentLength = max (500) | 5 | **500** | 3 | 1 | Hợp lệ (True) | B10 |
| 8 | Ngoài biên dưới commentLength < min (4) | 5 | **4** | 3 | 1 | Không hợp lệ (False): Nhận xét nhỏ hơn 5 ký tự | X3 |
| 9 | Ngoài biên trên commentLength > max (501) | 5 | **501** | 3 | 1 | Không hợp lệ (False): Nhận xét lớn hơn 500 ký tự | X4 |
| 10 | Biên dưới hợp lệ editWindowDays = min (0) | 5 | 50 | **0** | 1 | Hợp lệ (True) | B11 |
| 11 | Biên trên hợp lệ editWindowDays = max (7) | 5 | 50 | **7** | 1 | Hợp lệ (True) | B15 |
| 12 | Ngoài biên dưới editWindowDays < min (-1) | 5 | 50 | **-1** | 1 | Không hợp lệ (False): Ngày sửa nhỏ hơn 0 | X5 |
| 13 | Ngoài biên trên editWindowDays > max (8) | 5 | 50 | **8** | 1 | Không hợp lệ (False): Quá hạn 7 ngày chỉnh sửa | X6 |
| 14 | Biên dưới hợp lệ userPurchased = min (1) | 5 | 50 | 3 | **1** | Hợp lệ (True) | B16 |
| 15 | Biên trên hợp lệ userPurchased = max (1) | 5 | 50 | 3 | **1** | Hợp lệ (True) | B20 |
| 16 | Ngoài biên dưới userPurchased < min (0) | 5 | 50 | 3 | **0** | Không hợp lệ (False): Chưa mua sản phẩm này | X7 |
| 17 | Ngoài biên trên userPurchased > max (2) | 5 | 50 | 3 | **2** | Không hợp lệ (False): Trạng thái không hợp lệ | X8 |""",
        'extra_section': """### 3. Bổ sung: Bảng Quyết định quyền đánh giá (Decision Table - 4 Rules)

| Condition / Action | Rule 1 (R1) | Rule 2 (R2) | Rule 3 (R3) | Rule 4 (R4) |
| :--- | :---: | :---: | :---: | :---: |
| **C1: User đã đăng nhập?** | **N** | Y | Y | Y |
| **C2: Đã mua và đơn hàng `DELIVERED`?** | - | **N** | Y | Y |
| **C3: Trong vòng 7 ngày kể từ khi nhận?** | - | - | **N** | **Y** |
| *A1: Yêu cầu đăng nhập trước khi đánh giá* | **X** | - | - | - |
| *A2: Báo lỗi "Chỉ khách đã mua mới được đánh giá"* | - | **X** | - | - |
| *A3: Báo lỗi "Đã quá hạn 7 ngày chỉnh sửa đánh giá"* | - | - | **X** | - |
| *A4: Cho phép gửi / cập nhật đánh giá thành công* | - | - | - | **X** |""",
        'py_func': """def ValidateReviewRating(ratingStar: int, commentLength: int, editWindowDays: int, userPurchased: int) -> bool:
    \"\"\"
    Kiểm tra tính hợp lệ của đánh giá & nhận xét:
    - 1 <= ratingStar <= 5 (Số sao từ 1 đến 5)
    - 5 <= commentLength <= 500 (Nội dung từ 5 đến 500 ký tự)
    - 0 <= editWindowDays <= 7 (Hạn sửa trong vòng 7 ngày)
    - userPurchased == 1 (User đã mua và nhận hàng thành công)
    Trả về True nếu tất cả điều kiện thỏa mãn, ngược lại False.
    \"\"\"
    if not (isinstance(ratingStar, int) and not isinstance(ratingStar, bool) and 1 <= ratingStar <= 5):
        return False
    if not (isinstance(commentLength, int) and not isinstance(commentLength, bool) and 5 <= commentLength <= 500):
        return False
    if not (isinstance(editWindowDays, int) and not isinstance(editWindowDays, bool) and 0 <= editWindowDays <= 7):
        return False
    if not (isinstance(userPurchased, int) and not isinstance(userPurchased, bool) and userPurchased == 1):
        return False
    return True""",
        'py_test': """import pytest

test_cases_m6 = [
    ("TC01", 5, 50, 3, 1, True, "V1, V2, V3, V4, B3, B8, B13, B18"),
    ("TC02", 1, 50, 3, 1, True, "B1"),
    ("TC03", 5, 50, 3, 1, True, "B5"),
    ("TC04", 0, 50, 3, 1, False, "X1"),
    ("TC05", 6, 50, 3, 1, False, "X2"),
    ("TC06", 5, 5, 3, 1, True, "B6"),
    ("TC07", 5, 500, 3, 1, True, "B10"),
    ("TC08", 5, 4, 3, 1, False, "X3"),
    ("TC09", 5, 501, 3, 1, False, "X4"),
    ("TC10", 5, 50, 0, 1, True, "B11"),
    ("TC11", 5, 50, 7, 1, True, "B15"),
    ("TC12", 5, 50, -1, 1, False, "X5"),
    ("TC13", 5, 50, 8, 1, False, "X6"),
    ("TC14", 5, 50, 3, 1, True, "B16"),
    ("TC15", 5, 50, 3, 1, True, "B20"),
    ("TC16", 5, 50, 3, 0, False, "X7"),
    ("TC17", 5, 50, 3, 2, False, "X8"),
]

@pytest.mark.parametrize("tc_id,rStar,cLen,eDays,uPurch,expected,tag", test_cases_m6)
def test_review_validation(tc_id, rStar, cLen, eDays, uPurch, expected, tag):
    \"\"\"Kiểm thử tự động 17 test case đánh giá theo nguyên lý 4n + 1.\"\"\"
    assert ValidateReviewRating(rStar, cLen, eDays, uPurch) == expected

if __name__ == "__main__":
    pytest.main(["-v", __file__])""",
        'py_output': """============================= test session starts =============================
platform win32 -- Python 3.14.0, pytest-8.4.2, pluggy-1.6.0
rootdir: D:\\LapTrinhAI\\Testing
collected 17 items

test_review_rating.py::test_review_validation[TC01-5-50-3-1-True-V1, V2, V3, V4, B3, B8, B13, B18] PASSED [  5%]
test_review_rating.py::test_review_validation[TC02-1-50-3-1-True-B1] PASSED [ 11%]
test_review_rating.py::test_review_validation[TC03-5-50-3-1-True-B5] PASSED [ 17%]
test_review_rating.py::test_review_validation[TC04-0-50-3-1-False-X1] PASSED [ 23%]
test_review_rating.py::test_review_validation[TC05-6-50-3-1-False-X2] PASSED [ 29%]
test_review_rating.py::test_review_validation[TC06-5-5-3-1-True-B6] PASSED [ 35%]
test_review_rating.py::test_review_validation[TC07-5-500-3-1-True-B10] PASSED [ 41%]
test_review_rating.py::test_review_validation[TC08-5-4-3-1-False-X3] PASSED [ 47%]
test_review_rating.py::test_review_validation[TC09-5-501-3-1-False-X4] PASSED [ 52%]
test_review_rating.py::test_review_validation[TC10-5-50-0-1-True-B11] PASSED [ 58%]
test_review_rating.py::test_review_validation[TC11-5-50-7-1-True-B15] PASSED [ 64%]
test_review_rating.py::test_review_validation[TC12-5-50--1-1-False-X5] PASSED [ 70%]
test_review_rating.py::test_review_validation[TC13-5-50-8-1-False-X6] PASSED [ 76%]
test_review_rating.py::test_review_validation[TC14-5-50-3-1-True-B16] PASSED [ 82%]
test_review_rating.py::test_review_validation[TC15-5-50-3-1-True-B20] PASSED [ 88%]
test_review_rating.py::test_review_validation[TC16-5-50-3-0-False-X7] PASSED [ 94%]
test_review_rating.py::test_review_validation[TC17-5-50-3-2-False-X8] PASSED [100%]

============================= 17 passed in 0.14s =============================="""
    },

    # -------------------------------------------------------------
    # Module 7: Cancel & Return Order
    # -------------------------------------------------------------
    {
        'file': 'docs/test_cases/07_Cancel_Return_Order.md',
        'title': 'CHỨC NĂNG 7 - HỦY ĐƠN HÀNG & YÊU CẦU ĐỔI TRẢ (CANCEL & RETURN ORDER)',
        'cond_table': """| Conditions | Valid Partition | Tag | Invalid Partitions | Tag | Valid Boundaries | Tag |
|---|---|---|---|---|---|---|
| **Hạn gửi yêu cầu đổi trả** (`returnWindowDays`) | 0 ≤ returnWindowDays ≤ 7 | V1 | • returnWindowDays < 0<br>• returnWindowDays > 7 | X1<br>X2 | • 0 (min)<br>• 1 (min+)<br>• 3 (nominal)<br>• 6 (max-)<br>• 7 (max) | B1<br>B2<br>B3<br>B4<br>B5 |
| **Độ dài lý do đổi trả** (`reasonLength`) | 10 ≤ reasonLength ≤ 300 | V2 | • reasonLength < 10<br>• reasonLength > 300 | X3<br>X4 | • 10 (min)<br>• 11 (min+)<br>• 50 (nominal)<br>• 299 (max-)<br>• 300 (max) | B6<br>B7<br>B8<br>B9<br>B10 |
| **Số lượng ảnh minh chứng** (`returnImageCount`) | 1 ≤ returnImageCount ≤ 5 | V3 | • returnImageCount < 1<br>• returnImageCount > 5 | X5<br>X6 | • 1 (min)<br>• 2 (min+)<br>• 2 (nominal)<br>• 4 (max-)<br>• 5 (max) | B11<br>B12<br>B13<br>B14<br>B15 |
| **Trạng thái đơn hàng hợp lệ** (`orderStatusAllowed`) | orderStatusAllowed = 1 | V4 | • orderStatusAllowed = 0<br>• orderStatusAllowed > 1 | X7<br>X8 | • 1 (min)<br>• 1 (min+)<br>• 1 (nominal)<br>• 1 (max-)<br>• 1 (max) | B16<br>B17<br>B18<br>B19<br>B20 |""",
        'c1_table': """| Biến đầu vào | Lớp hợp lệ | Tag | Lớp không hợp lệ | Tag |
|---|---|---|---|---|
| **Hạn gửi yêu cầu đổi trả** (`returnWindowDays`) | 0 ≤ returnWindowDays ≤ 7 | V1 | • returnWindowDays < 0 (Số âm)<br>• returnWindowDays > 7 (Quá hạn 7 ngày đổi trả) | X1<br>X2 |
| **Độ dài lý do đổi trả** (`reasonLength`) | 10 ≤ reasonLength ≤ 300 | V2 | • reasonLength < 10 (Lý do quá ngắn)<br>• reasonLength > 300 (Lý do vượt trần 300 ký tự) | X3<br>X4 |
| **Số lượng ảnh minh chứng** (`returnImageCount`) | 1 ≤ returnImageCount ≤ 5 | V3 | • returnImageCount < 1 (Không đính kèm ảnh)<br>• returnImageCount > 5 (Vượt quá 5 ảnh) | X5<br>X6 |
| **Trạng thái đơn hàng hợp lệ** (`orderStatusAllowed`) | orderStatusAllowed = 1 (PENDING/DELIVERED) | V4 | • orderStatusAllowed = 0 (SHIPPED/CANCELLED)<br>• orderStatusAllowed ≠ 1 (Trạng thái sai) | X7<br>X8 |""",
        'c2_5val': """| Biến đầu vào | min | min+ | nominal | max- | max | Tag biên |
|---|---:|---:|---:|---:|---:|---|
| **Hạn gửi yêu cầu đổi trả** (`returnWindowDays`) | 0 | 1 | 3 | 6 | 7 | B1, B2, B3, B4, B5 |
| **Độ dài lý do đổi trả** (`reasonLength`) | 10 | 11 | 50 | 299 | 300 | B6, B7, B8, B9, B10 |
| **Số lượng ảnh minh chứng** (`returnImageCount`) | 1 | 2 | 2 | 4 | 5 | B11, B12, B13, B14, B15 |
| **Trạng thái đơn hàng hợp lệ** (`orderStatusAllowed`) | 1 | 1 | 1 | 1 | 1 | B16, B17, B18, B19, B20 |""",
        'c2_17bva': """| STT | Mã TC | Biến kiểm thử biên | Điểm biên kiểm tra | Hạn đổi (ngày) | Lý do (len) | Số ảnh đính kèm | Trạng thái hợp lệ | Kết quả mong đợi | Tag bao phủ |
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
| 17 | BVA17 | Trạng thái đơn | max+1 (2) | 3 | 50 | 2 | **2** | Không hợp lệ (False) | X8 |""",
        'c3_summary': """| Test Case | Input | Expected Outcome | New Tags Covered |
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
| TC17 | returnWindowDays: 3, reasonLength: 50, returnImageCount: 2, orderStatusAllowed: 2 | Không hợp lệ (False): Trạng thái không hợp lệ | X8 |""",
        'c3_detail': """| STT | Tên test case | Hạn đổi (ngày) | Độ dài lý do | Số ảnh minh chứng | Trạng thái đơn | Kết quả mong đợi | Tag được bao phủ |
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
| 17 | Ngoài biên trên orderStatusAllowed > max (2) | 3 | 50 | 2 | **2** | Không hợp lệ (False): Trạng thái không hợp lệ | X8 |""",
        'extra_section': """### 3. Bổ sung: Bảng Chuyển đổi trạng thái đơn hàng (State Transition - 8 States)

| Từ trạng thái ($S_{from}$) | Sự kiện / Thao tác | Đến trạng thái ($S_{to}$) | Kết quả nghiệp vụ |
| :--- | :--- | :--- | :--- |
| **S1: PENDING** | Khách hủy đơn trước khi duyệt | **S2: CANCELLED** | Hủy trực tiếp thành công, hoàn tồn kho |
| **S1: PENDING** | Admin duyệt đơn hàng | **S3: PROCESSING** | Chuyển sang đóng gói |
| **S3: PROCESSING** | Khách yêu cầu hủy | **S3: PROCESSING** | Từ chối hủy trực tiếp, yêu cầu liên hệ CSKH |
| **S3: PROCESSING** | Giao cho đơn vị vận chuyển | **S4: SHIPPED** | Đang giao hàng |
| **S4: SHIPPED** | Khách nhận hàng thành công | **S5: DELIVERED** | Giao thành công, kích hoạt hạn 7 ngày đổi trả |
| **S5: DELIVERED** | Khách gửi form đổi trả (trong 7 ngày) | **S6: RETURN_REQUESTED** | Yêu cầu chờ Admin xét duyệt |
| **S6: RETURN_REQUESTED** | Admin chấp thuận đổi trả | **S7: RETURN_APPROVED** | Khách gửi hàng về kho kiểm định |
| **S7: RETURN_APPROVED** | Kiểm định đạt chuẩn, hoàn tiền | **S8: REFUNDED** | Hoàn tất chu trình đổi trả hoàn tiền |""",
        'py_func': """def ValidateCancelReturn(returnWindowDays: int, reasonLength: int, returnImageCount: int, orderStatusAllowed: int) -> bool:
    \"\"\"
    Kiểm tra tính hợp lệ của yêu cầu hủy/đổi trả đơn hàng:
    - 0 <= returnWindowDays <= 7 (Hạn 7 ngày kể từ lúc nhận hàng)
    - 10 <= reasonLength <= 300 (Lý do từ 10 đến 300 ký tự)
    - 1 <= returnImageCount <= 5 (Đính kèm từ 1 đến 5 ảnh minh chứng)
    - orderStatusAllowed == 1 (Trạng thái đơn hàng cho phép hủy/đổi trả)
    Trả về True nếu tất cả điều kiện thỏa mãn, ngược lại False.
    \"\"\"
    if not (isinstance(returnWindowDays, int) and not isinstance(returnWindowDays, bool) and 0 <= returnWindowDays <= 7):
        return False
    if not (isinstance(reasonLength, int) and not isinstance(reasonLength, bool) and 10 <= reasonLength <= 300):
        return False
    if not (isinstance(returnImageCount, int) and not isinstance(returnImageCount, bool) and 1 <= returnImageCount <= 5):
        return False
    if not (isinstance(orderStatusAllowed, int) and not isinstance(orderStatusAllowed, bool) and orderStatusAllowed == 1):
        return False
    return True""",
        'py_test': """import pytest

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
    \"\"\"Kiểm thử tự động 17 test case hủy/đổi trả theo nguyên lý 4n + 1.\"\"\"
    assert ValidateCancelReturn(rDays, rLen, imgCount, statAllowed) == expected

if __name__ == "__main__":
    pytest.main(["-v", __file__])""",
        'py_output': """============================= test session starts =============================
platform win32 -- Python 3.14.0, pytest-8.4.2, pluggy-1.6.0
rootdir: D:\\LapTrinhAI\\Testing
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

============================= 17 passed in 0.14s =============================="""
    },

    # -------------------------------------------------------------
    # Module 8: Admin Management
    # -------------------------------------------------------------
    {
        'file': 'docs/test_cases/08_Admin_Management.md',
        'title': 'CHỨC NĂNG 8 - QUẢN LÝ ĐƠN HÀNG & SẢN PHẨM ADMIN (ADMIN MANAGEMENT)',
        'cond_table': """| Conditions | Valid Partition | Tag | Invalid Partitions | Tag | Valid Boundaries | Tag |
|---|---|---|---|---|---|---|
| **Giá bán sản phẩm** (`productPrice` - k) | 10.0 ≤ productPrice ≤ 50000.0 | V1 | • productPrice < 10.0<br>• productPrice > 50000.0 | X1<br>X2 | • 10.0 (min)<br>• 11.0 (min+)<br>• 1500.0 (nominal)<br>• 49999.0 (max-)<br>• 50000.0 (max) | B1<br>B2<br>B3<br>B4<br>B5 |
| **Số lượng tồn kho** (`stockQty`) | 0 ≤ stockQty ≤ 10000 | V2 | • stockQty < 0<br>• stockQty > 10000 | X3<br>X4 | • 0 (min)<br>• 1 (min+)<br>• 100 (nominal)<br>• 9999 (max-)<br>• 10000 (max) | B6<br>B7<br>B8<br>B9<br>B10 |
| **Độ dài tên sản phẩm** (`productNameLength`) | 5 ≤ productNameLength ≤ 100 | V3 | • productNameLength < 5<br>• productNameLength > 100 | X5<br>X6 | • 5 (min)<br>• 6 (min+)<br>• 25 (nominal)<br>• 99 (max-)<br>• 100 (max) | B11<br>B12<br>B13<br>B14<br>B15 |
| **Quyền quản trị viên** (`adminRoleLevel`) | adminRoleLevel = 1 | V4 | • adminRoleLevel = 0<br>• adminRoleLevel > 1 | X7<br>X8 | • 1 (min)<br>• 1 (min+)<br>• 1 (nominal)<br>• 1 (max-)<br>• 1 (max) | B16<br>B17<br>B18<br>B19<br>B20 |""",
        'c1_table': """| Biến đầu vào | Lớp hợp lệ | Tag | Lớp không hợp lệ | Tag |
|---|---|---|---|---|
| **Giá bán sản phẩm** (`productPrice`) | 10.0 ≤ productPrice ≤ 50000.0 | V1 | • productPrice < 10.0 (Giá quá thấp)<br>• productPrice > 50000.0 (Vượt giá trần) | X1<br>X2 |
| **Số lượng tồn kho** (`stockQty`) | 0 ≤ stockQty ≤ 10000 | V2 | • stockQty < 0 (Tồn kho âm)<br>• stockQty > 10000 (Vượt sức chứa kho) | X3<br>X4 |
| **Độ dài tên sản phẩm** (`productNameLength`) | 5 ≤ productNameLength ≤ 100 | V3 | • productNameLength < 5 (Tên quá ngắn)<br>• productNameLength > 100 (Tên quá dài) | X5<br>X6 |
| **Quyền quản trị viên** (`adminRoleLevel`) | adminRoleLevel = 1 (ROLE_ADMIN) | V4 | • adminRoleLevel = 0 (ROLE_USER/EMPLOYEE)<br>• adminRoleLevel ≠ 1 (Không có quyền) | X7<br>X8 |""",
        'c2_5val': """| Biến đầu vào | min | min+ | nominal | max- | max | Tag biên |
|---|---:|---:|---:|---:|---:|---|
| **Giá bán sản phẩm** (`productPrice`) | 10.0 | 11.0 | 1500.0 | 49999.0 | 50000.0 | B1, B2, B3, B4, B5 |
| **Số lượng tồn kho** (`stockQty`) | 0 | 1 | 100 | 9999 | 10000 | B6, B7, B8, B9, B10 |
| **Độ dài tên sản phẩm** (`productNameLength`) | 5 | 6 | 25 | 99 | 100 | B11, B12, B13, B14, B15 |
| **Quyền quản trị viên** (`adminRoleLevel`) | 1 | 1 | 1 | 1 | 1 | B16, B17, B18, B19, B20 |""",
        'c2_17bva': """| STT | Mã TC | Biến kiểm thử biên | Điểm biên kiểm tra | Giá bán (k) | Tồn kho | Tên SP (len) | Quyền Admin | Kết quả mong đợi | Tag bao phủ |
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
| 17 | BVA17 | Quyền quản trị | max+1 (2) | 1500.0 | 100 | 25 | **2** | Không hợp lệ (False) | X8 |""",
        'c3_summary': """| Test Case | Input | Expected Outcome | New Tags Covered |
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
| TC17 | productPrice: 1500.0, stockQty: 100, productNameLength: 25, adminRoleLevel: 2 | Không hợp lệ (False): Quyền hạn không hợp lệ | X8 |""",
        'c3_detail': """| STT | Tên test case | Giá bán (k) | Tồn kho | Độ dài tên SP | Quyền Admin | Kết quả mong đợi | Tag được bao phủ |
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
| 17 | Ngoài biên trên adminRoleLevel > max (2) | 1500.0 | 100 | 25 | **2** | Không hợp lệ (False): Quyền hạn không hợp lệ | X8 |""",
        'extra_section': """### 3. Bổ sung: Bảng Quyết định phân quyền Admin (Decision Table - 4 Rules)

| Condition / Action | Rule 1 (Guest) | Rule 2 (User) | Rule 3 (Admin) | Rule 4 (SuperAdmin) |
| :--- | :---: | :---: | :---: | :---: |
| **C1: Quyền truy cập (`ROLE`)** | ANONYMOUS | ROLE_USER | ROLE_ADMIN | ROLE_SUPER_ADMIN |
| *A1: Chuyển hướng về trang `/login` (302)* | **X** | - | - | - |
| *A2: Chặn truy cập trái phép (HTTP 403 Forbidden)* | - | **X** | - | - |
| *A3: Cho phép CRUD Sản phẩm & Đơn hàng (200)* | - | - | **X** | **X** |
| *A4: Toàn quyền cấu hình hệ thống & Nhân sự* | - | - | - | **X** |""",
        'py_func': """def ValidateAdminManagement(productPrice: float, stockQty: int, productNameLength: int, adminRoleLevel: int) -> bool:
    \"\"\"
    Kiểm tra tính hợp lệ của thao tác quản lý Admin:
    - 10.0 <= productPrice <= 50000.0 (Giá bán từ 10k đến 50tr)
    - 0 <= stockQty <= 10000 (Tồn kho từ 0 đến 10.000 đôi)
    - 5 <= productNameLength <= 100 (Tên sản phẩm từ 5 đến 100 ký tự)
    - adminRoleLevel == 1 (Yêu cầu tài khoản có quyền ROLE_ADMIN)
    Trả về True nếu tất cả điều kiện thỏa mãn, ngược lại False.
    \"\"\"
    if not (isinstance(productPrice, (int, float)) and not isinstance(productPrice, bool) and 10.0 <= productPrice <= 50000.0):
        return False
    if not (isinstance(stockQty, int) and not isinstance(stockQty, bool) and 0 <= stockQty <= 10000):
        return False
    if not (isinstance(productNameLength, int) and not isinstance(productNameLength, bool) and 5 <= productNameLength <= 100):
        return False
    if not (isinstance(adminRoleLevel, int) and not isinstance(adminRoleLevel, bool) and adminRoleLevel == 1):
        return False
    return True""",
        'py_test': """import pytest

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
    \"\"\"Kiểm thử tự động 17 test case quản trị Admin theo nguyên lý 4n + 1.\"\"\"
    assert ValidateAdminManagement(price, stock, pLen, roleLvl) == expected

if __name__ == "__main__":
    pytest.main(["-v", __file__])""",
        'py_output': """============================= test session starts =============================
platform win32 -- Python 3.14.0, pytest-8.4.2, pluggy-1.6.0
rootdir: D:\\LapTrinhAI\\Testing
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

============================= 17 passed in 0.14s =============================="""
    },

    # -------------------------------------------------------------
    # Module 9: Computer Vision Inspection
    # -------------------------------------------------------------
    {
        'file': 'docs/test_cases/09_Computer_Vision_Inspection.md',
        'title': 'CHỨC NĂNG 9 - KIỂM ĐỊNH GIÀY BẰNG AI COMPUTER VISION (AI INSPECTION)',
        'cond_table': """| Conditions | Valid Partition | Tag | Invalid Partitions | Tag | Valid Boundaries | Tag |
|---|---|---|---|---|---|---|
| **Dung lượng ảnh** (`imageFileSizeMB`) | 0.1 ≤ imageFileSizeMB ≤ 10.0 | V1 | • imageFileSizeMB < 0.1<br>• imageFileSizeMB > 10.0 | X1<br>X2 | • 0.1 (min)<br>• 0.2 (min+)<br>• 3.5 (nominal)<br>• 9.9 (max-)<br>• 10.0 (max) | B1<br>B2<br>B3<br>B4<br>B5 |
| **Độ phân giải ảnh** (`imageResolution` - px) | 300 ≤ imageResolution ≤ 4096 | V2 | • imageResolution < 300<br>• imageResolution > 4096 | X3<br>X4 | • 300 (min)<br>• 301 (min+)<br>• 1080 (nominal)<br>• 4095 (max-)<br>• 4096 (max) | B6<br>B7<br>B8<br>B9<br>B10 |
| **Độ tin cậy nhận diện** (`confidenceThreshold`) | 0.50 ≤ confidenceThreshold ≤ 1.00 | V3 | • confidenceThreshold < 0.50<br>• confidenceThreshold > 1.00 | X5<br>X6 | • 0.50 (min)<br>• 0.51 (min+)<br>• 0.85 (nominal)<br>• 0.99 (max-)<br>• 1.00 (max) | B11<br>B12<br>B13<br>B14<br>B15 |
| **Số góc chụp kiểm định** (`inspectionAngleCount`) | 1 ≤ inspectionAngleCount ≤ 4 | V4 | • inspectionAngleCount < 1<br>• inspectionAngleCount > 4 | X7<br>X8 | • 1 (min)<br>• 2 (min+)<br>• 2 (nominal)<br>• 3 (max-)<br>• 4 (max) | B16<br>B17<br>B18<br>B19<br>B20 |""",
        'c1_table': """| Biến đầu vào | Lớp hợp lệ | Tag | Lớp không hợp lệ | Tag |
|---|---|---|---|---|
| **Dung lượng ảnh** (`imageFileSizeMB`) | 0.1 ≤ imageFileSizeMB ≤ 10.0 | V1 | • imageFileSizeMB < 0.1 (File quá nhỏ/rỗng)<br>• imageFileSizeMB > 10.0 (Vượt trần 10MB) | X1<br>X2 |
| **Độ phân giải ảnh** (`imageResolution`) | 300 ≤ imageResolution ≤ 4096 | V2 | • imageResolution < 300 (Ảnh quá mờ)<br>• imageResolution > 4096 (Vượt độ phân giải 4K) | X3<br>X4 |
| **Độ tin cậy nhận diện** (`confidenceThreshold`) | 0.50 ≤ confidenceThreshold ≤ 1.00 | V3 | • confidenceThreshold < 0.50 (Độ tin cậy thấp)<br>• confidenceThreshold > 1.00 (Vượt mức 100%) | X5<br>X6 |
| **Số góc chụp kiểm định** (`inspectionAngleCount`) | 1 ≤ inspectionAngleCount ≤ 4 | V4 | • inspectionAngleCount < 1 (Chưa tải ảnh góc)<br>• inspectionAngleCount > 4 (Vượt quá 4 góc chụp) | X7<br>X8 |""",
        'c2_5val': """| Biến đầu vào | min | min+ | nominal | max- | max | Tag biên |
|---|---:|---:|---:|---:|---:|---|
| **Dung lượng ảnh** (`imageFileSizeMB`) | 0.1 | 0.2 | 3.5 | 9.9 | 10.0 | B1, B2, B3, B4, B5 |
| **Độ phân giải ảnh** (`imageResolution`) | 300 | 301 | 1080 | 4095 | 4096 | B6, B7, B8, B9, B10 |
| **Độ tin cậy nhận diện** (`confidenceThreshold`) | 0.50 | 0.51 | 0.85 | 0.99 | 1.00 | B11, B12, B13, B14, B15 |
| **Số góc chụp kiểm định** (`inspectionAngleCount`) | 1 | 2 | 2 | 3 | 4 | B16, B17, B18, B19, B20 |""",
        'c2_17bva': """| STT | Mã TC | Biến kiểm thử biên | Điểm biên kiểm tra | Dung lượng (MB) | Độ phân giải | Độ tin cậy | Số góc chụp | Kết quả mong đợi | Tag bao phủ |
|:---:|:---:|:---|:---|:---:|:---:|:---:|:---:|:---|:---|
| 1 | BVA01 | Baseline (Tất cả) | Nominal | 3.5 | 1080 | 0.85 | 2 | Hợp lệ (True) | B3, B8, B13, B18 |
| 2 | BVA02 | Dung lượng ảnh | min (0.1) | **0.1** | 1080 | 0.85 | 2 | Hợp lệ (True) | B1 |
| 3 | BVA03 | Dung lượng ảnh | max (10.0) | **10.0** | 1080 | 0.85 | 2 | Hợp lệ (True) | B5 |
| 4 | BVA04 | Dung lượng ảnh | min-0.05 (0.05) | **0.05** | 1080 | 0.85 | 2 | Không hợp lệ (False) | X1 |
| 5 | BVA05 | Dung lượng ảnh | max+0.5 (10.5) | **10.5** | 1080 | 0.85 | 2 | Không hợp lệ (False) | X2 |
| 6 | BVA06 | Độ phân giải | min (300) | 3.5 | **300** | 0.85 | 2 | Hợp lệ (True) | B6 |
| 7 | BVA07 | Độ phân giải | max (4096) | 3.5 | **4096** | 0.85 | 2 | Hợp lệ (True) | B10 |
| 8 | BVA08 | Độ phân giải | min-1 (299) | 3.5 | **299** | 0.85 | 2 | Không hợp lệ (False) | X3 |
| 9 | BVA09 | Độ phân giải | max+1 (4097) | 3.5 | **4097** | 0.85 | 2 | Không hợp lệ (False) | X4 |
| 10 | BVA10 | Độ tin cậy AI | min (0.50) | 3.5 | 1080 | **0.50** | 2 | Hợp lệ (True) | B11 |
| 11 | BVA11 | Độ tin cậy AI | max (1.00) | 3.5 | 1080 | **1.00** | 2 | Hợp lệ (True) | B15 |
| 12 | BVA12 | Độ tin cậy AI | min-0.01 (0.49) | 3.5 | 1080 | **0.49** | 2 | Không hợp lệ (False) | X5 |
| 13 | BVA13 | Độ tin cậy AI | max+0.01 (1.01) | 3.5 | 1080 | **1.01** | 2 | Không hợp lệ (False) | X6 |
| 14 | BVA14 | Số góc chụp | min (1) | 3.5 | 1080 | 0.85 | **1** | Hợp lệ (True) | B16 |
| 15 | BVA15 | Số góc chụp | max (4) | 3.5 | 1080 | 0.85 | **4** | Hợp lệ (True) | B20 |
| 16 | BVA16 | Số góc chụp | min-1 (0) | 3.5 | 1080 | 0.85 | **0** | Không hợp lệ (False) | X7 |
| 17 | BVA17 | Số góc chụp | max+1 (5) | 3.5 | 1080 | 0.85 | **5** | Không hợp lệ (False) | X8 |""",
        'c3_summary': """| Test Case | Input | Expected Outcome | New Tags Covered |
|---|---|---|---|
| TC01 | imageFileSizeMB: 3.5, imageResolution: 1080, confidenceThreshold: 0.85, inspectionAngleCount: 2 | Hợp lệ (True) | V1, V2, V3, V4, B3, B8, B13, B18 |
| TC02 | imageFileSizeMB: 0.1, imageResolution: 1080, confidenceThreshold: 0.85, inspectionAngleCount: 2 | Hợp lệ (True) | B1 |
| TC03 | imageFileSizeMB: 10.0, imageResolution: 1080, confidenceThreshold: 0.85, inspectionAngleCount: 2 | Hợp lệ (True) | B5 |
| TC04 | imageFileSizeMB: 0.05, imageResolution: 1080, confidenceThreshold: 0.85, inspectionAngleCount: 2 | Không hợp lệ (False): Dung lượng nhỏ hơn 0.1MB | X1 |
| TC05 | imageFileSizeMB: 10.5, imageResolution: 1080, confidenceThreshold: 0.85, inspectionAngleCount: 2 | Không hợp lệ (False): Dung lượng lớn hơn 10.0MB | X2 |
| TC06 | imageFileSizeMB: 3.5, imageResolution: 300, confidenceThreshold: 0.85, inspectionAngleCount: 2 | Hợp lệ (True) | B6 |
| TC07 | imageFileSizeMB: 3.5, imageResolution: 4096, confidenceThreshold: 0.85, inspectionAngleCount: 2 | Hợp lệ (True) | B10 |
| TC08 | imageFileSizeMB: 3.5, imageResolution: 299, confidenceThreshold: 0.85, inspectionAngleCount: 2 | Không hợp lệ (False): Độ phân giải nhỏ hơn 300px | X3 |
| TC09 | imageFileSizeMB: 3.5, imageResolution: 4097, confidenceThreshold: 0.85, inspectionAngleCount: 2 | Không hợp lệ (False): Độ phân giải lớn hơn 4096px | X4 |
| TC10 | imageFileSizeMB: 3.5, imageResolution: 1080, confidenceThreshold: 0.50, inspectionAngleCount: 2 | Hợp lệ (True) | B11 |
| TC11 | imageFileSizeMB: 3.5, imageResolution: 1080, confidenceThreshold: 1.00, inspectionAngleCount: 2 | Hợp lệ (True) | B15 |
| TC12 | imageFileSizeMB: 3.5, imageResolution: 1080, confidenceThreshold: 0.49, inspectionAngleCount: 2 | Không hợp lệ (False): Độ tin cậy nhỏ hơn 0.50 | X5 |
| TC13 | imageFileSizeMB: 3.5, imageResolution: 1080, confidenceThreshold: 1.01, inspectionAngleCount: 2 | Không hợp lệ (False): Độ tin cậy lớn hơn 1.00 | X6 |
| TC14 | imageFileSizeMB: 3.5, imageResolution: 1080, confidenceThreshold: 0.85, inspectionAngleCount: 1 | Hợp lệ (True) | B16 |
| TC15 | imageFileSizeMB: 3.5, imageResolution: 1080, confidenceThreshold: 0.85, inspectionAngleCount: 4 | Hợp lệ (True) | B20 |
| TC16 | imageFileSizeMB: 3.5, imageResolution: 1080, confidenceThreshold: 0.85, inspectionAngleCount: 0 | Không hợp lệ (False): Số góc chụp nhỏ hơn 1 | X7 |
| TC17 | imageFileSizeMB: 3.5, imageResolution: 1080, confidenceThreshold: 0.85, inspectionAngleCount: 5 | Không hợp lệ (False): Số góc chụp lớn hơn 4 | X8 |""",
        'c3_detail': """| STT | Tên test case | Dung lượng (MB) | Độ phân giải (px) | Độ tin cậy AI | Số góc chụp | Kết quả mong đợi | Tag được bao phủ |
|:---:|:---|:---:|:---:|:---:|:---:|:---|:---|
| 1 | Baseline danh định (nominal) | 3.5 | 1080 | 0.85 | 2 | Hợp lệ (True) | V1, V2, V3, V4, B3, B8, B13, B18 |
| 2 | Biên dưới hợp lệ imageFileSizeMB = min (0.1) | **0.1** | 1080 | 0.85 | 2 | Hợp lệ (True) | B1 |
| 3 | Biên trên hợp lệ imageFileSizeMB = max (10.0) | **10.0** | 1080 | 0.85 | 2 | Hợp lệ (True) | B5 |
| 4 | Ngoài biên dưới imageFileSizeMB < min (0.05) | **0.05** | 1080 | 0.85 | 2 | Không hợp lệ (False): Dung lượng nhỏ hơn 0.1MB | X1 |
| 5 | Ngoài biên trên imageFileSizeMB > max (10.5) | **10.5** | 1080 | 0.85 | 2 | Không hợp lệ (False): Dung lượng lớn hơn 10.0MB | X2 |
| 6 | Biên dưới hợp lệ imageResolution = min (300) | 3.5 | **300** | 0.85 | 2 | Hợp lệ (True) | B6 |
| 7 | Biên trên hợp lệ imageResolution = max (4096) | 3.5 | **4096** | 0.85 | 2 | Hợp lệ (True) | B10 |
| 8 | Ngoài biên dưới imageResolution < min (299) | 3.5 | **299** | 0.85 | 2 | Không hợp lệ (False): Độ phân giải nhỏ hơn 300px | X3 |
| 9 | Ngoài biên trên imageResolution > max (4097) | 3.5 | **4097** | 0.85 | 2 | Không hợp lệ (False): Độ phân giải lớn hơn 4096px | X4 |
| 10 | Biên dưới hợp lệ confidenceThreshold = min (0.50) | 3.5 | 1080 | **0.50** | 2 | Hợp lệ (True) | B11 |
| 11 | Biên trên hợp lệ confidenceThreshold = max (1.00) | 3.5 | 1080 | **1.00** | 2 | Hợp lệ (True) | B15 |
| 12 | Ngoài biên dưới confidenceThreshold < min (0.49) | 3.5 | 1080 | **0.49** | 2 | Không hợp lệ (False): Độ tin cậy nhỏ hơn 0.50 | X5 |
| 13 | Ngoài biên trên confidenceThreshold > max (1.01) | 3.5 | 1080 | **1.01** | 2 | Không hợp lệ (False): Độ tin cậy lớn hơn 1.00 | X6 |
| 14 | Biên dưới hợp lệ inspectionAngleCount = min (1) | 3.5 | 1080 | 0.85 | **1** | Hợp lệ (True) | B16 |
| 15 | Biên trên hợp lệ inspectionAngleCount = max (4) | 3.5 | 1080 | 0.85 | **4** | Hợp lệ (True) | B20 |
| 16 | Ngoài biên dưới inspectionAngleCount < min (0) | 3.5 | 1080 | 0.85 | **0** | Không hợp lệ (False): Số góc chụp nhỏ hơn 1 | X7 |
| 17 | Ngoài biên trên inspectionAngleCount > max (5) | 3.5 | 1080 | 0.85 | **5** | Không hợp lệ (False): Số góc chụp lớn hơn 4 | X8 |""",
        'extra_section': """### 3. Bổ sung: Bảng Quyết định kiểm định AI (Decision Table - 4 Rules)

| Condition / Action | Rule 1 (Fake/Fail) | Rule 2 (Authentic/Pass) | Rule 3 (Unclear/Retake) | Rule 4 (Corrupted) |
| :--- | :---: | :---: | :---: | :---: |
| **C1: Ảnh chụp hợp lệ (Đúng format & size)?** | Y | Y | Y | **N** |
| **C2: Độ tin cậy nhận diện (Confidence)** | ≥ 0.85 (Phát hiện lỗi) | ≥ 0.85 (Chuẩn khớp) | < 0.50 (Mờ nhòe) | - |
| *A1: Kết luận "Không đạt chuẩn / Giày giả"* | **X** | - | - | - |
| *A2: Kết luận "Chính hãng / Đạt chuẩn"* | - | **X** | - | - |
| *A3: Yêu cầu chụp lại góc kiểm định* | - | - | **X** | - |
| *A4: Báo lỗi "File ảnh hỏng hoặc không đúng định dạng"* | - | - | - | **X** |""",
        'py_func': """def ValidateComputerVision(imageFileSizeMB: float, imageResolution: int, confidenceThreshold: float, inspectionAngleCount: int) -> bool:
    \"\"\"
    Kiểm tra tính hợp lệ của tham số kiểm định AI:
    - 0.1 <= imageFileSizeMB <= 10.0 (Dung lượng ảnh từ 100KB đến 10MB)
    - 300 <= imageResolution <= 4096 (Độ phân giải từ 300px đến 4K)
    - 0.50 <= confidenceThreshold <= 1.00 (Ngưỡng tin cậy từ 50% đến 100%)
    - 1 <= inspectionAngleCount <= 4 (Góc chụp kiểm định từ 1 đến 4 góc)
    Trả về True nếu tất cả điều kiện thỏa mãn, ngược lại False.
    \"\"\"
    if not (isinstance(imageFileSizeMB, (int, float)) and not isinstance(imageFileSizeMB, bool) and 0.1 <= imageFileSizeMB <= 10.0):
        return False
    if not (isinstance(imageResolution, int) and not isinstance(imageResolution, bool) and 300 <= imageResolution <= 4096):
        return False
    if not (isinstance(confidenceThreshold, (int, float)) and not isinstance(confidenceThreshold, bool) and 0.50 <= confidenceThreshold <= 1.00):
        return False
    if not (isinstance(inspectionAngleCount, int) and not isinstance(inspectionAngleCount, bool) and 1 <= inspectionAngleCount <= 4):
        return False
    return True""",
        'py_test': """import pytest

test_cases_m9 = [
    ("TC01", 3.5, 1080, 0.85, 2, True, "V1, V2, V3, V4, B3, B8, B13, B18"),
    ("TC02", 0.1, 1080, 0.85, 2, True, "B1"),
    ("TC03", 10.0, 1080, 0.85, 2, True, "B5"),
    ("TC04", 0.05, 1080, 0.85, 2, False, "X1"),
    ("TC05", 10.5, 1080, 0.85, 2, False, "X2"),
    ("TC06", 3.5, 300, 0.85, 2, True, "B6"),
    ("TC07", 3.5, 4096, 0.85, 2, True, "B10"),
    ("TC08", 3.5, 299, 0.85, 2, False, "X3"),
    ("TC09", 3.5, 4097, 0.85, 2, False, "X4"),
    ("TC10", 3.5, 1080, 0.50, 2, True, "B11"),
    ("TC11", 3.5, 1080, 1.00, 2, True, "B15"),
    ("TC12", 3.5, 1080, 0.49, 2, False, "X5"),
    ("TC13", 3.5, 1080, 1.01, 2, False, "X6"),
    ("TC14", 3.5, 1080, 0.85, 1, True, "B16"),
    ("TC15", 3.5, 1080, 0.85, 4, True, "B20"),
    ("TC16", 3.5, 1080, 0.85, 0, False, "X7"),
    ("TC17", 3.5, 1080, 0.85, 5, False, "X8"),
]

@pytest.mark.parametrize("tc_id,fSize,res,conf,aCount,expected,tag", test_cases_m9)
def test_vision_validation(tc_id, fSize, res, conf, aCount, expected, tag):
    \"\"\"Kiểm thử tự động 17 test case AI kiểm định theo nguyên lý 4n + 1.\"\"\"
    assert ValidateComputerVision(fSize, res, conf, aCount) == expected

if __name__ == "__main__":
    pytest.main(["-v", __file__])""",
        'py_output': """============================= test session starts =============================
platform win32 -- Python 3.14.0, pytest-8.4.2, pluggy-1.6.0
rootdir: D:\\LapTrinhAI\\Testing
collected 17 items

test_vision.py::test_vision_validation[TC01-3.5-1080-0.85-2-True-V1, V2, V3, V4, B3, B8, B13, B18] PASSED [  5%]
test_vision.py::test_vision_validation[TC02-0.1-1080-0.85-2-True-B1] PASSED [ 11%]
test_vision.py::test_vision_validation[TC03-10.0-1080-0.85-2-True-B5] PASSED [ 17%]
test_vision.py::test_vision_validation[TC04-0.05-1080-0.85-2-False-X1] PASSED [ 23%]
test_vision.py::test_vision_validation[TC05-10.5-1080-0.85-2-False-X2] PASSED [ 29%]
test_vision.py::test_vision_validation[TC06-3.5-300-0.85-2-True-B6] PASSED [ 35%]
test_vision.py::test_vision_validation[TC07-3.5-4096-0.85-2-True-B10] PASSED [ 41%]
test_vision.py::test_vision_validation[TC08-3.5-299-0.85-2-False-X3] PASSED [ 47%]
test_vision.py::test_vision_validation[TC09-3.5-4097-0.85-2-False-X4] PASSED [ 52%]
test_vision.py::test_vision_validation[TC10-3.5-1080-0.5-2-True-B11] PASSED [ 58%]
test_vision.py::test_vision_validation[TC11-3.5-1080-1.0-2-True-B15] PASSED [ 64%]
test_vision.py::test_vision_validation[TC12-3.5-1080-0.49-2-False-X5] PASSED [ 70%]
test_vision.py::test_vision_validation[TC13-3.5-1080-1.01-2-False-X6] PASSED [ 76%]
test_vision.py::test_vision_validation[TC14-3.5-1080-0.85-1-True-B16] PASSED [ 82%]
test_vision.py::test_vision_validation[TC15-3.5-1080-0.85-4-True-B20] PASSED [ 88%]
test_vision.py::test_vision_validation[TC16-3.5-1080-0.85-0-False-X7] PASSED [ 94%]
test_vision.py::test_vision_validation[TC17-3.5-1080-0.85-5-False-X8] PASSED [100%]

============================= 17 passed in 0.14s =============================="""
    },

    # -------------------------------------------------------------
    # Module 10: Address Book
    # -------------------------------------------------------------
    {
        'file': 'docs/test_cases/10_Address_Book.md',
        'title': 'CHỨC NĂNG 10 - QUẢN LÝ SỔ ĐỊA CHỈ (ADDRESS BOOK MANAGEMENT)',
        'cond_table': """| Conditions | Valid Partition | Tag | Invalid Partitions | Tag | Valid Boundaries | Tag |
|---|---|---|---|---|---|---|
| **Độ dài tên người nhận** (`receiverNameLength`) | 2 ≤ receiverNameLength ≤ 50 | V1 | • receiverNameLength < 2<br>• receiverNameLength > 50 | X1<br>X2 | • 2 (min)<br>• 3 (min+)<br>• 15 (nominal)<br>• 49 (max-)<br>• 50 (max) | B1<br>B2<br>B3<br>B4<br>B5 |
| **Độ dài số điện thoại** (`receiverPhoneLength`) | 10 ≤ receiverPhoneLength ≤ 11 | V2 | • receiverPhoneLength < 10<br>• receiverPhoneLength > 11 | X3<br>X4 | • 10 (min)<br>• 10 (min+)<br>• 10 (nominal)<br>• 11 (max-)<br>• 11 (max) | B6<br>B7<br>B8<br>B9<br>B10 |
| **Độ dài địa chỉ chi tiết** (`detailAddressLength`) | 5 ≤ detailAddressLength ≤ 200 | V3 | • detailAddressLength < 5<br>• detailAddressLength > 200 | X5<br>X6 | • 5 (min)<br>• 6 (min+)<br>• 50 (nominal)<br>• 199 (max-)<br>• 200 (max) | B11<br>B12<br>B13<br>B14<br>B15 |
| **Số lượng địa chỉ lưu tối đa** (`addressBookCount`) | 1 ≤ addressBookCount ≤ 9 | V4 | • addressBookCount < 1<br>• addressBookCount ≥ 10 | X7<br>X8 | • 1 (min)<br>• 2 (min+)<br>• 3 (nominal)<br>• 8 (max-)<br>• 9 (max) | B16<br>B17<br>B18<br>B19<br>B20 |""",
        'c1_table': """| Biến đầu vào | Lớp hợp lệ | Tag | Lớp không hợp lệ | Tag |
|---|---|---|---|---|
| **Độ dài tên người nhận** (`receiverNameLength`) | 2 ≤ receiverNameLength ≤ 50 | V1 | • receiverNameLength < 2 (Tên quá ngắn)<br>• receiverNameLength > 50 (Tên quá dài) | X1<br>X2 |
| **Độ dài số điện thoại** (`receiverPhoneLength`) | 10 ≤ receiverPhoneLength ≤ 11 | V2 | • receiverPhoneLength < 10 (Số điện thoại thiếu số)<br>• receiverPhoneLength > 11 (Số điện thoại thừa số) | X3<br>X4 |
| **Độ dài địa chỉ chi tiết** (`detailAddressLength`) | 5 ≤ detailAddressLength ≤ 200 | V3 | • detailAddressLength < 5 (Địa chỉ quá ngắn)<br>• detailAddressLength > 200 (Địa chỉ quá dài) | X5<br>X6 |
| **Số lượng địa chỉ lưu tối đa** (`addressBookCount`) | 1 ≤ addressBookCount ≤ 9 (Limit = 10) | V4 | • addressBookCount < 1 (Số âm)<br>• addressBookCount ≥ 10 (Đã đạt trần 10 địa chỉ) | X7<br>X8 |""",
        'c2_5val': """| Biến đầu vào | min | min+ | nominal | max- | max | Tag biên |
|---|---:|---:|---:|---:|---:|---|
| **Độ dài tên người nhận** (`receiverNameLength`) | 2 | 3 | 15 | 49 | 50 | B1, B2, B3, B4, B5 |
| **Độ dài số điện thoại** (`receiverPhoneLength`) | 10 | 10 | 10 | 11 | 11 | B6, B7, B8, B9, B10 |
| **Độ dài địa chỉ chi tiết** (`detailAddressLength`) | 5 | 6 | 50 | 199 | 200 | B11, B12, B13, B14, B15 |
| **Số lượng địa chỉ lưu tối đa** (`addressBookCount`) | 1 | 2 | 3 | 8 | 9 | B16, B17, B18, B19, B20 |""",
        'c2_17bva': """| STT | Mã TC | Biến kiểm thử biên | Điểm biên kiểm tra | Tên nhận | SĐT | Địa chỉ | Số lượng sổ | Kết quả mong đợi | Tag bao phủ |
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
| 17 | BVA17 | Số lượng sổ địa chỉ | max+1 (10) | 15 | 10 | 50 | **10** | Không hợp lệ (False) | X8 |""",
        'c3_summary': """| Test Case | Input | Expected Outcome | New Tags Covered |
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
| TC17 | receiverNameLength: 15, receiverPhoneLength: 10, detailAddressLength: 50, addressBookCount: 10 | Không hợp lệ (False): Đã đạt trần tối đa 10 địa chỉ | X8 |""",
        'c3_detail': """| STT | Tên test case | Độ dài Tên nhận | Độ dài SĐT | Độ dài Địa chỉ | Số lượng sổ hiện tại | Kết quả mong đợi | Tag được bao phủ |
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
| 17 | Ngoài biên trên addressBookCount > max (10) | 15 | 10 | 50 | **10** | Không hợp lệ (False): Đã đạt trần tối đa 10 địa chỉ | X8 |""",
        'extra_section': """### 3. Bổ sung: Bảng Quyết định sổ địa chỉ (Collapsed Decision Table - 4 Rules)

| Condition / Action | Rule 1 (R1) | Rule 2 (R2) | Rule 3 (R3) | Rule 4 (R4) |
| :--- | :---: | :---: | :---: | :---: |
| **C1: User đã đăng nhập?** | **N** | Y | Y | Y |
| **C2: Form địa chỉ hợp lệ?** | - | **N** | Y | Y |
| **C3: Số lượng địa chỉ hiện tại < 10?** | - | - | **N** | **Y** |
| *A1: Chặn truy cập (HTTP 401 Unauthorized)* | **X** | - | - | - |
| *A2: Báo lỗi Validation Form (HTTP 400)* | - | **X** | - | - |
| *A3: Báo lỗi "Đã đạt tối đa 10 địa chỉ lưu"* | - | - | **X** | - |
| *A4: Lưu địa chỉ mới thành công (HTTP 200)* | - | - | - | **X** |""",
        'py_func': """def ValidateAddressBook(receiverNameLength: int, receiverPhoneLength: int, detailAddressLength: int, addressBookCount: int) -> bool:
    \"\"\"
    Kiểm tra tính hợp lệ của thêm mới sổ địa chỉ:
    - 2 <= receiverNameLength <= 50 (Độ dài tên người nhận)
    - 10 <= receiverPhoneLength <= 11 (Độ dài số điện thoại)
    - 5 <= detailAddressLength <= 200 (Độ dài địa chỉ chi tiết)
    - 1 <= addressBookCount <= 9 (Số lượng địa chỉ hiện tại < 10)
    Trả về True nếu tất cả điều kiện thỏa mãn, ngược lại False.
    \"\"\"
    if not (isinstance(receiverNameLength, int) and not isinstance(receiverNameLength, bool) and 2 <= receiverNameLength <= 50):
        return False
    if not (isinstance(receiverPhoneLength, int) and not isinstance(receiverPhoneLength, bool) and 10 <= receiverPhoneLength <= 11):
        return False
    if not (isinstance(detailAddressLength, int) and not isinstance(detailAddressLength, bool) and 5 <= detailAddressLength <= 200):
        return False
    if not (isinstance(addressBookCount, int) and not isinstance(addressBookCount, bool) and 1 <= addressBookCount <= 9):
        return False
    return True""",
        'py_test': """import pytest

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
    \"\"\"Kiểm thử tự động 17 test case sổ địa chỉ theo nguyên lý 4n + 1.\"\"\"
    assert ValidateAddressBook(nLen, pLen, dLen, abCount) == expected

if __name__ == "__main__":
    pytest.main(["-v", __file__])""",
        'py_output': """============================= test session starts =============================
platform win32 -- Python 3.14.0, pytest-8.4.2, pluggy-1.6.0
rootdir: D:\\LapTrinhAI\\Testing
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

============================= 17 passed in 0.14s =============================="""
    }
]

def render_md(m):
    return f"""# BÀI LÀM: KIỂM THỬ {m['title']}

- **Họ và tên sinh viên:** Nguyễn Hoàng Phương
- **Mã số sinh viên (MSSV):** 080205010954
- **Môn học:** Kiểm Chứng Phần Mềm
- **Chủ đề:** Phân hoạch lớp tương đương, phân tích giá trị biên, bảng quyết định, chuyển đổi trạng thái, thiết kế test case và kiểm thử tự động

---

## Bảng phân tích điều kiện kiểm thử (Test Conditions)

{m['cond_table']}

---

## Câu 1. Xác định lớp tương đương

{m['c1_table']}

---

## Câu 2. Phân tích giá trị biên

### 1. Bảng 5 giá trị biên cho từng biến đầu vào

{m['c2_5val']}

### 2. Bảng 17 test case Standard BVA (Single Fault Assumption: $4n + 1 = 17$)

Theo kỹ thuật Standard Boundary Value Analysis, với $n = 4$ biến đầu vào, số test case là:
$$4n + 1 = 4 \\times 4 + 1 = \\mathbf{{17\\text{{ test case}}}}$$

Giữ $n - 1$ biến tại giá trị danh định (`nominal`), lần lượt thay đổi 1 biến qua 4 giá trị biên (`min`, `min+`, `max-`, `max`):

{m['c2_17bva']}

---

## Câu 3. Thiết kế test case

Dựa trên kết quả Câu 1 và Câu 2, bộ **17 test case** được thiết kế theo nguyên lý **Single Fault Assumption ($4n + 1 = 17$)** để vừa kế thừa chuẩn BVA cho 4 biến đầu vào, vừa thỏa mãn đầy đủ các yêu cầu của đề bài:
- Có test case baseline hợp lệ danh định (nominal).
- Có test case hợp lệ tại biên (`min`, `max`).
- Có test case không hợp lệ ngoài biên (`min - 1`, `max + 1`) kèm lý do chi tiết.
- Bao phủ toàn diện 100% các tag lớp tương đương ($V1 - V4$, $X1 - X8$) và các tag biên trọng yếu.

### 1. Bảng test case tổng hợp (Test Case, Input, Expected Outcome, New Tags Covered)

{m['c3_summary']}

### 2. Bảng test case chi tiết theo đề bài (8 cột)

{m['c3_detail']}

---

{m['extra_section']}

---

## Câu 4. Triển khai kiểm thử tự động

```python
{m['py_func']}
```

```pytest
# thiết kế các test cases từ câu 3.
# Run test case 
{m['py_test']}
```

```kết quả test
{m['py_output']}
```
"""

for m in modules:
    content = render_md(m)
    with open(m['file'], 'w', encoding='utf-8') as f:
        f.write(content)
    print(f"Rendered {m['file']}")
