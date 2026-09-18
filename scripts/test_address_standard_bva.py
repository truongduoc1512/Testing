import pytest

def ValidateAddressBook(receiverNameLength: int, phoneLength: int, streetAddressLength: int, provinceLength: int) -> bool:
    """
    Kiểm tra tính hợp lệ của thông tin Sổ địa chỉ giao hàng (Address Book Form):
    - 1 <= receiverNameLength <= 100 (Độ dài tên người nhận)
    - 1 <= phoneLength <= 20 (Độ dài số điện thoại)
    - 1 <= streetAddressLength <= 255 (Độ dài địa chỉ đường phố)
    - 1 <= provinceLength <= 100 (Độ dài Tỉnh/Thành phố)
    Trả về True nếu tất cả điều kiện thỏa mãn, ngược lại False.
    """
    if not (isinstance(receiverNameLength, int) and not isinstance(receiverNameLength, bool) and 1 <= receiverNameLength <= 100):
        return False
    if not (isinstance(phoneLength, int) and not isinstance(phoneLength, bool) and 1 <= phoneLength <= 20):
        return False
    if not (isinstance(streetAddressLength, int) and not isinstance(streetAddressLength, bool) and 1 <= streetAddressLength <= 255):
        return False
    if not (isinstance(provinceLength, int) and not isinstance(provinceLength, bool) and 1 <= provinceLength <= 100):
        return False
    return True

test_cases_address = [
    # TC01: Baseline nominal (Tất cả biến tại giá trị danh định)
    ("TC01", 50, 10, 50, 30, True, "V1, V2, V3, V4, B3, B8, B13, B18"),
    
    # TC02 - TC05: Biên biến receiverNameLength (min, max, min-1, max+1)
    ("TC02", 1, 10, 50, 30, True, "B1"),
    ("TC03", 100, 10, 50, 30, True, "B5"),
    ("TC04", 0, 10, 50, 30, False, "X1"),
    ("TC05", 101, 10, 50, 30, False, "X2"),
    
    # TC06 - TC09: Biên biến phoneLength (min, max, min-1, max+1)
    ("TC06", 50, 1, 50, 30, True, "B6"),
    ("TC07", 50, 20, 50, 30, True, "B10"),
    ("TC08", 50, 0, 50, 30, False, "X3"),
    ("TC09", 50, 21, 50, 30, False, "X4"),
    
    # TC10 - TC13: Biên biến streetAddressLength (min, max, min-1, max+1)
    ("TC10", 50, 10, 1, 30, True, "B11"),
    ("TC11", 50, 10, 255, 30, True, "B15"),
    ("TC12", 50, 10, 0, 30, False, "X5"),
    ("TC13", 50, 10, 256, 30, False, "X6"),
    
    # TC14 - TC17: Biên biến provinceLength (min, max, min-1, max+1)
    ("TC14", 50, 10, 50, 1, True, "B16"),
    ("TC15", 50, 10, 50, 100, True, "B20"),
    ("TC16", 50, 10, 50, 0, False, "X7"),
    ("TC17", 50, 10, 50, 101, False, "X8"),
]

@pytest.mark.parametrize("tc_id,receiverNameLength,phoneLength,streetAddressLength,provinceLength,expected,tag", test_cases_address)
def test_address_validation(tc_id, receiverNameLength, phoneLength, streetAddressLength, provinceLength, expected, tag):
    """Kiểm thử tự động 17 test case thiết kế cho Phân hệ 10 Address Book theo nguyên lý 4n + 1."""
    assert ValidateAddressBook(receiverNameLength, phoneLength, streetAddressLength, provinceLength) == expected

if __name__ == "__main__":
    pytest.main(["-v", __file__])
