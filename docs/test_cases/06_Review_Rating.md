# BÀI LÀM: KIỂM THỬ CHỨC NĂNG 6 - ĐÁNH GIÁ & BÌNH LUẬN SẢN PHẨM (REVIEW & RATING)

- **Họ và tên sinh viên:** Nguyễn Hoàng Phương
- **Mã số sinh viên (MSSV):** 080205010954
- **Môn học:** Kiểm Chứng Phần Mềm
- **Chủ đề:** Phân hoạch lớp tương đương, phân tích giá trị biên, bảng quyết định, chuyển đổi trạng thái, thiết kế test case và kiểm thử tự động

---

## Bảng phân tích điều kiện kiểm thử (Test Conditions)

| Conditions | Valid Partition | Tag | Invalid Partitions | Tag | Valid Boundaries | Tag |
|---|---|---|---|---|---|---|
| **Số sao đánh giá** (`ratingStar`) | 1 ≤ ratingStar ≤ 5 | V1 | • ratingStar < 1<br>• ratingStar > 5 | X1<br>X2 | • 1 (min)<br>• 2 (min+)<br>• 5 (nominal)<br>• 4 (max-)<br>• 5 (max) | B1<br>B2<br>B3<br>B4<br>B5 |
| **Độ dài nhận xét** (`commentLength`) | 5 ≤ commentLength ≤ 500 | V2 | • commentLength < 5<br>• commentLength > 500 | X3<br>X4 | • 5 (min)<br>• 6 (min+)<br>• 50 (nominal)<br>• 499 (max-)<br>• 500 (max) | B6<br>B7<br>B8<br>B9<br>B10 |
| **Hạn sửa đánh giá** (`editWindowDays`) | 0 ≤ editWindowDays ≤ 7 | V3 | • editWindowDays < 0<br>• editWindowDays > 7 | X5<br>X6 | • 0 (min)<br>• 1 (min+)<br>• 3 (nominal)<br>• 6 (max-)<br>• 7 (max) | B11<br>B12<br>B13<br>B14<br>B15 |
| **Trạng thái đã mua hàng** (`userPurchased`) | userPurchased = 1 | V4 | • userPurchased = 0<br>• userPurchased > 1 | X7<br>X8 | • 1 (min)<br>• 1 (min+)<br>• 1 (nominal)<br>• 1 (max-)<br>• 1 (max) | B16<br>B17<br>B18<br>B19<br>B20 |

---

## Câu 1. Xác định lớp tương đương

| Biến đầu vào | Lớp hợp lệ | Tag | Lớp không hợp lệ | Tag |
|---|---|---|---|---|
| **Số sao đánh giá** (`ratingStar`) | 1 ≤ ratingStar ≤ 5 | V1 | • ratingStar < 1 (Số sao nhỏ hơn 1)<br>• ratingStar > 5 (Số sao lớn hơn 5) | X1<br>X2 |
| **Độ dài nhận xét** (`commentLength`) | 5 ≤ commentLength ≤ 500 | V2 | • commentLength < 5 (Nội dung quá ngắn)<br>• commentLength > 500 (Nội dung quá dài) | X3<br>X4 |
| **Hạn sửa đánh giá** (`editWindowDays`) | 0 ≤ editWindowDays ≤ 7 | V3 | • editWindowDays < 0 (Số âm)<br>• editWindowDays > 7 (Quá hạn 7 ngày cho phép) | X5<br>X6 |
| **Trạng thái đã mua hàng** (`userPurchased`) | userPurchased = 1 (Đã mua và nhận) | V4 | • userPurchased = 0 (Chưa từng mua sản phẩm)<br>• userPurchased ≠ 1 (Trạng thái bất thường) | X7<br>X8 |

---

## Câu 2. Phân tích giá trị biên

### 1. Bảng 5 giá trị biên cho từng biến đầu vào

| Biến đầu vào | min | min+ | nominal | max- | max | Tag biên |
|---|---:|---:|---:|---:|---:|---|
| **Số sao đánh giá** (`ratingStar`) | 1 | 2 | 5 | 4 | 5 | B1, B2, B3, B4, B5 |
| **Độ dài nhận xét** (`commentLength`) | 5 | 6 | 50 | 499 | 500 | B6, B7, B8, B9, B10 |
| **Hạn sửa đánh giá** (`editWindowDays`) | 0 | 1 | 3 | 6 | 7 | B11, B12, B13, B14, B15 |
| **Trạng thái đã mua hàng** (`userPurchased`) | 1 | 1 | 1 | 1 | 1 | B16, B17, B18, B19, B20 |

### 2. Bảng 17 test case Standard BVA (Single Fault Assumption: $4n + 1 = 17$)

Theo kỹ thuật Standard Boundary Value Analysis, với $n = 4$ biến đầu vào, số test case là:
$$4n + 1 = 4 \times 4 + 1 = \mathbf{17\text{ test case}}$$

Giữ $n - 1$ biến tại giá trị danh định (`nominal`), lần lượt thay đổi 1 biến qua 4 giá trị biên (`min`, `min+`, `max-`, `max`):

| STT | Mã TC | Biến kiểm thử biên | Điểm biên kiểm tra | Số sao | Nhận xét (len) | Hạn sửa (ngày) | Đã mua hàng | Kết quả mong đợi | Tag bao phủ |
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
| 17 | BVA17 | Đã mua hàng | max+1 (2) | 5 | 50 | 3 | **2** | Không hợp lệ (False) | X8 |

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
| TC17 | ratingStar: 5, commentLength: 50, editWindowDays: 3, userPurchased: 2 | Không hợp lệ (False): Trạng thái không hợp lệ | X8 |

### 2. Bảng test case chi tiết theo đề bài (8 cột)

| STT | Tên test case | Số sao đánh giá | Độ dài nhận xét | Hạn sửa (ngày) | Đã mua hàng | Kết quả mong đợi | Tag được bao phủ |
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
| 17 | Ngoài biên trên userPurchased > max (2) | 5 | 50 | 3 | **2** | Không hợp lệ (False): Trạng thái không hợp lệ | X8 |

---

### 3. Bổ sung: Bảng Quyết định quyền đánh giá (Decision Table - 4 Rules)

| Condition / Action | Rule 1 (R1) | Rule 2 (R2) | Rule 3 (R3) | Rule 4 (R4) |
| :--- | :---: | :---: | :---: | :---: |
| **C1: User đã đăng nhập?** | **N** | Y | Y | Y |
| **C2: Đã mua và đơn hàng `DELIVERED`?** | - | **N** | Y | Y |
| **C3: Trong vòng 7 ngày kể từ khi nhận?** | - | - | **N** | **Y** |
| *A1: Yêu cầu đăng nhập trước khi đánh giá* | **X** | - | - | - |
| *A2: Báo lỗi "Chỉ khách đã mua mới được đánh giá"* | - | **X** | - | - |
| *A3: Báo lỗi "Đã quá hạn 7 ngày chỉnh sửa đánh giá"* | - | - | **X** | - |
| *A4: Cho phép gửi / cập nhật đánh giá thành công* | - | - | - | **X** |

---

## Câu 4. Triển khai kiểm thử tự động

```python
def ValidateReviewRating(ratingStar: int, commentLength: int, editWindowDays: int, userPurchased: int) -> bool:
    """
    Kiểm tra tính hợp lệ của đánh giá & nhận xét:
    - 1 <= ratingStar <= 5 (Số sao từ 1 đến 5)
    - 5 <= commentLength <= 500 (Nội dung từ 5 đến 500 ký tự)
    - 0 <= editWindowDays <= 7 (Hạn sửa trong vòng 7 ngày)
    - userPurchased == 1 (User đã mua và nhận hàng thành công)
    Trả về True nếu tất cả điều kiện thỏa mãn, ngược lại False.
    """
    if not (isinstance(ratingStar, int) and not isinstance(ratingStar, bool) and 1 <= ratingStar <= 5):
        return False
    if not (isinstance(commentLength, int) and not isinstance(commentLength, bool) and 5 <= commentLength <= 500):
        return False
    if not (isinstance(editWindowDays, int) and not isinstance(editWindowDays, bool) and 0 <= editWindowDays <= 7):
        return False
    if not (isinstance(userPurchased, int) and not isinstance(userPurchased, bool) and userPurchased == 1):
        return False
    return True
```

```pytest
# thiết kế các test cases từ câu 3.
# Run test case 
import pytest

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
    """Kiểm thử tự động 17 test case đánh giá theo nguyên lý 4n + 1."""
    assert ValidateReviewRating(rStar, cLen, eDays, uPurch) == expected

if __name__ == "__main__":
    pytest.main(["-v", __file__])
```

```kết quả test
============================= test session starts =============================
platform win32 -- Python 3.14.0, pytest-8.4.2, pluggy-1.6.0
rootdir: D:\LapTrinhAI\Testing
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

============================= 17 passed in 0.14s ==============================
```
