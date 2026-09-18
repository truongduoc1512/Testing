# BÀI LÀM: KIỂM THỬ CHỨC NĂNG 9 - KIỂM ĐỊNH GIÀY BẰNG AI COMPUTER VISION (AI INSPECTION)

- **Họ và tên sinh viên:** Nguyễn Hoàng Phương
- **Mã số sinh viên (MSSV):** 080205010954
- **Môn học:** Kiểm Chứng Phần Mềm
- **Chủ đề:** Phân hoạch lớp tương đương, phân tích giá trị biên, bảng quyết định, chuyển đổi trạng thái, thiết kế test case và kiểm thử tự động

---

## Bảng phân tích điều kiện kiểm thử (Test Conditions)

| Conditions | Valid Partition | Tag | Invalid Partitions | Tag | Valid Boundaries | Tag |
|---|---|---|---|---|---|---|
| **Dung lượng ảnh** (`imageFileSizeMB`) | 0.1 ≤ imageFileSizeMB ≤ 10.0 | V1 | • imageFileSizeMB < 0.1<br>• imageFileSizeMB > 10.0 | X1<br>X2 | • 0.1 (min)<br>• 0.2 (min+)<br>• 3.5 (nominal)<br>• 9.9 (max-)<br>• 10.0 (max) | B1<br>B2<br>B3<br>B4<br>B5 |
| **Độ phân giải ảnh** (`imageResolution` - px) | 300 ≤ imageResolution ≤ 4096 | V2 | • imageResolution < 300<br>• imageResolution > 4096 | X3<br>X4 | • 300 (min)<br>• 301 (min+)<br>• 1080 (nominal)<br>• 4095 (max-)<br>• 4096 (max) | B6<br>B7<br>B8<br>B9<br>B10 |
| **Độ tin cậy nhận diện** (`confidenceThreshold`) | 0.50 ≤ confidenceThreshold ≤ 1.00 | V3 | • confidenceThreshold < 0.50<br>• confidenceThreshold > 1.00 | X5<br>X6 | • 0.50 (min)<br>• 0.51 (min+)<br>• 0.85 (nominal)<br>• 0.99 (max-)<br>• 1.00 (max) | B11<br>B12<br>B13<br>B14<br>B15 |
| **Số góc chụp kiểm định** (`inspectionAngleCount`) | 1 ≤ inspectionAngleCount ≤ 4 | V4 | • inspectionAngleCount < 1<br>• inspectionAngleCount > 4 | X7<br>X8 | • 1 (min)<br>• 2 (min+)<br>• 2 (nominal)<br>• 3 (max-)<br>• 4 (max) | B16<br>B17<br>B18<br>B19<br>B20 |

---

## Câu 1. Xác định lớp tương đương

| Biến đầu vào | Lớp hợp lệ | Tag | Lớp không hợp lệ | Tag |
|---|---|---|---|---|
| **Dung lượng ảnh** (`imageFileSizeMB`) | 0.1 ≤ imageFileSizeMB ≤ 10.0 | V1 | • imageFileSizeMB < 0.1 (File quá nhỏ/rỗng)<br>• imageFileSizeMB > 10.0 (Vượt trần 10MB) | X1<br>X2 |
| **Độ phân giải ảnh** (`imageResolution`) | 300 ≤ imageResolution ≤ 4096 | V2 | • imageResolution < 300 (Ảnh quá mờ)<br>• imageResolution > 4096 (Vượt độ phân giải 4K) | X3<br>X4 |
| **Độ tin cậy nhận diện** (`confidenceThreshold`) | 0.50 ≤ confidenceThreshold ≤ 1.00 | V3 | • confidenceThreshold < 0.50 (Độ tin cậy thấp)<br>• confidenceThreshold > 1.00 (Vượt mức 100%) | X5<br>X6 |
| **Số góc chụp kiểm định** (`inspectionAngleCount`) | 1 ≤ inspectionAngleCount ≤ 4 | V4 | • inspectionAngleCount < 1 (Chưa tải ảnh góc)<br>• inspectionAngleCount > 4 (Vượt quá 4 góc chụp) | X7<br>X8 |

---

## Câu 2. Phân tích giá trị biên

### 1. Bảng 5 giá trị biên cho từng biến đầu vào

| Biến đầu vào | min | min+ | nominal | max- | max | Tag biên |
|---|---:|---:|---:|---:|---:|---|
| **Dung lượng ảnh** (`imageFileSizeMB`) | 0.1 | 0.2 | 3.5 | 9.9 | 10.0 | B1, B2, B3, B4, B5 |
| **Độ phân giải ảnh** (`imageResolution`) | 300 | 301 | 1080 | 4095 | 4096 | B6, B7, B8, B9, B10 |
| **Độ tin cậy nhận diện** (`confidenceThreshold`) | 0.50 | 0.51 | 0.85 | 0.99 | 1.00 | B11, B12, B13, B14, B15 |
| **Số góc chụp kiểm định** (`inspectionAngleCount`) | 1 | 2 | 2 | 3 | 4 | B16, B17, B18, B19, B20 |

### 2. Bảng 17 test case Standard BVA (Single Fault Assumption: $4n + 1 = 17$)

Theo kỹ thuật Standard Boundary Value Analysis, với $n = 4$ biến đầu vào, số test case là:
$$4n + 1 = 4 \times 4 + 1 = \mathbf{17\text{ test case}}$$

Giữ $n - 1$ biến tại giá trị danh định (`nominal`), lần lượt thay đổi 1 biến qua 4 giá trị biên (`min`, `min+`, `max-`, `max`):

| STT | Mã TC | Biến kiểm thử biên | Điểm biên kiểm tra | Dung lượng (MB) | Độ phân giải | Độ tin cậy | Số góc chụp | Kết quả mong đợi | Tag bao phủ |
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
| 17 | BVA17 | Số góc chụp | max+1 (5) | 3.5 | 1080 | 0.85 | **5** | Không hợp lệ (False) | X8 |

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
| TC17 | imageFileSizeMB: 3.5, imageResolution: 1080, confidenceThreshold: 0.85, inspectionAngleCount: 5 | Không hợp lệ (False): Số góc chụp lớn hơn 4 | X8 |

### 2. Bảng test case chi tiết theo đề bài (8 cột)

| STT | Tên test case | Dung lượng (MB) | Độ phân giải (px) | Độ tin cậy AI | Số góc chụp | Kết quả mong đợi | Tag được bao phủ |
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
| 17 | Ngoài biên trên inspectionAngleCount > max (5) | 3.5 | 1080 | 0.85 | **5** | Không hợp lệ (False): Số góc chụp lớn hơn 4 | X8 |

---

### 3. Bổ sung: Bảng Quyết định kiểm định AI (Decision Table - 4 Rules)

| Condition / Action | Rule 1 (Fake/Fail) | Rule 2 (Authentic/Pass) | Rule 3 (Unclear/Retake) | Rule 4 (Corrupted) |
| :--- | :---: | :---: | :---: | :---: |
| **C1: Ảnh chụp hợp lệ (Đúng format & size)?** | Y | Y | Y | **N** |
| **C2: Độ tin cậy nhận diện (Confidence)** | ≥ 0.85 (Phát hiện lỗi) | ≥ 0.85 (Chuẩn khớp) | < 0.50 (Mờ nhòe) | - |
| *A1: Kết luận "Không đạt chuẩn / Giày giả"* | **X** | - | - | - |
| *A2: Kết luận "Chính hãng / Đạt chuẩn"* | - | **X** | - | - |
| *A3: Yêu cầu chụp lại góc kiểm định* | - | - | **X** | - |
| *A4: Báo lỗi "File ảnh hỏng hoặc không đúng định dạng"* | - | - | - | **X** |

---

## Câu 4. Triển khai kiểm thử tự động

```python
def ValidateComputerVision(imageFileSizeMB: float, imageResolution: int, confidenceThreshold: float, inspectionAngleCount: int) -> bool:
    """
    Kiểm tra tính hợp lệ của tham số kiểm định AI:
    - 0.1 <= imageFileSizeMB <= 10.0 (Dung lượng ảnh từ 100KB đến 10MB)
    - 300 <= imageResolution <= 4096 (Độ phân giải từ 300px đến 4K)
    - 0.50 <= confidenceThreshold <= 1.00 (Ngưỡng tin cậy từ 50% đến 100%)
    - 1 <= inspectionAngleCount <= 4 (Góc chụp kiểm định từ 1 đến 4 góc)
    Trả về True nếu tất cả điều kiện thỏa mãn, ngược lại False.
    """
    if not (isinstance(imageFileSizeMB, (int, float)) and not isinstance(imageFileSizeMB, bool) and 0.1 <= imageFileSizeMB <= 10.0):
        return False
    if not (isinstance(imageResolution, int) and not isinstance(imageResolution, bool) and 300 <= imageResolution <= 4096):
        return False
    if not (isinstance(confidenceThreshold, (int, float)) and not isinstance(confidenceThreshold, bool) and 0.50 <= confidenceThreshold <= 1.00):
        return False
    if not (isinstance(inspectionAngleCount, int) and not isinstance(inspectionAngleCount, bool) and 1 <= inspectionAngleCount <= 4):
        return False
    return True
```

```pytest
# thiết kế các test cases từ câu 3.
# Run test case 
import pytest

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
    """Kiểm thử tự động 17 test case AI kiểm định theo nguyên lý 4n + 1."""
    assert ValidateComputerVision(fSize, res, conf, aCount) == expected

if __name__ == "__main__":
    pytest.main(["-v", __file__])
```

```kết quả test
============================= test session starts =============================
platform win32 -- Python 3.14.0, pytest-8.4.2, pluggy-1.6.0
rootdir: D:\LapTrinhAI\Testing
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

============================= 17 passed in 0.14s ==============================
```
