import pytest

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
