import pytest

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
