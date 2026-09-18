import pytest

# Module 1: Authentication
def ValidateAuthentication(userLength: int, passLength: int, userStatus: int, loginAttempts: int) -> bool:
    if not (isinstance(userLength, int) and not isinstance(userLength, bool) and 3 <= userLength <= 30):
        return False
    if not (isinstance(passLength, int) and not isinstance(passLength, bool) and 6 <= passLength <= 20):
        return False
    if not (isinstance(userStatus, int) and not isinstance(userStatus, bool) and userStatus == 1):
        return False
    if not (isinstance(loginAttempts, int) and not isinstance(loginAttempts, bool) and 0 <= loginAttempts <= 4):
        return False
    return True

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
def test_m1_auth(tc_id, uLen, pLen, uStat, lAtt, expected, tag):
    assert ValidateAuthentication(uLen, pLen, uStat, lAtt) == expected


# Module 2: Search & Pagination
def ValidateSearchPagination(keywordLength: int, minPrice: float, page: int, pageSize: int) -> bool:
    if not (isinstance(keywordLength, int) and not isinstance(keywordLength, bool) and 0 <= keywordLength <= 50):
        return False
    if not (isinstance(minPrice, (int, float)) and not isinstance(minPrice, bool) and 0.0 <= minPrice <= 50000.0):
        return False
    if not (isinstance(page, int) and not isinstance(page, bool) and 1 <= page <= 50):
        return False
    if not (isinstance(pageSize, int) and not isinstance(pageSize, bool) and 1 <= pageSize <= 12):
        return False
    return True

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
def test_m2_search_pagination(tc_id, kLen, mPrice, page, pSize, expected, tag):
    assert ValidateSearchPagination(kLen, mPrice, page, pSize) == expected


# Module 3: Shopping Cart
def ValidateShoppingCart(quantity: int, itemCount: int, stockQuantity: int, subtotal: float) -> bool:
    if not (isinstance(quantity, int) and not isinstance(quantity, bool) and 1 <= quantity <= 99):
        return False
    if not (isinstance(itemCount, int) and not isinstance(itemCount, bool) and 1 <= itemCount <= 20):
        return False
    if not (isinstance(stockQuantity, int) and not isinstance(stockQuantity, bool) and 1 <= stockQuantity <= 500):
        return False
    if not (isinstance(subtotal, (int, float)) and not isinstance(subtotal, bool) and 100.0 <= subtotal <= 100000.0):
        return False
    return True

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
def test_m3_cart(tc_id, qty, iCount, sQty, subt, expected, tag):
    assert ValidateShoppingCart(qty, iCount, sQty, subt) == expected


# Module 5: Checkout
def ValidateCheckout(customerNameLength: int, phoneLength: int, addressLength: int, orderTotal: float) -> bool:
    if not (isinstance(customerNameLength, int) and not isinstance(customerNameLength, bool) and 2 <= customerNameLength <= 50):
        return False
    if not (isinstance(phoneLength, int) and not isinstance(phoneLength, bool) and 10 <= phoneLength <= 11):
        return False
    if not (isinstance(addressLength, int) and not isinstance(addressLength, bool) and 10 <= addressLength <= 200):
        return False
    if not (isinstance(orderTotal, (int, float)) and not isinstance(orderTotal, bool) and 100.0 <= orderTotal <= 100000.0):
        return False
    return True

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
def test_m5_checkout(tc_id, cLen, pLen, aLen, oTot, expected, tag):
    assert ValidateCheckout(cLen, pLen, aLen, oTot) == expected


# Module 6: Review Rating
def ValidateReviewRating(ratingStar: int, commentLength: int, editWindowDays: int, userPurchased: int) -> bool:
    if not (isinstance(ratingStar, int) and not isinstance(ratingStar, bool) and 1 <= ratingStar <= 5):
        return False
    if not (isinstance(commentLength, int) and not isinstance(commentLength, bool) and 5 <= commentLength <= 500):
        return False
    if not (isinstance(editWindowDays, int) and not isinstance(editWindowDays, bool) and 0 <= editWindowDays <= 7):
        return False
    if not (isinstance(userPurchased, int) and not isinstance(userPurchased, bool) and userPurchased == 1):
        return False
    return True

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
def test_m6_review(tc_id, rStar, cLen, eDays, uPurch, expected, tag):
    assert ValidateReviewRating(rStar, cLen, eDays, uPurch) == expected


# Module 7: Cancel & Return
def ValidateCancelReturn(returnWindowDays: int, reasonLength: int, returnImageCount: int, orderStatusAllowed: int) -> bool:
    if not (isinstance(returnWindowDays, int) and not isinstance(returnWindowDays, bool) and 0 <= returnWindowDays <= 7):
        return False
    if not (isinstance(reasonLength, int) and not isinstance(reasonLength, bool) and 10 <= reasonLength <= 300):
        return False
    if not (isinstance(returnImageCount, int) and not isinstance(returnImageCount, bool) and 1 <= returnImageCount <= 5):
        return False
    if not (isinstance(orderStatusAllowed, int) and not isinstance(orderStatusAllowed, bool) and orderStatusAllowed == 1):
        return False
    return True

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
def test_m7_cancel_return(tc_id, rDays, rLen, imgCount, statAllowed, expected, tag):
    assert ValidateCancelReturn(rDays, rLen, imgCount, statAllowed) == expected


# Module 8: Admin Management
def ValidateAdminManagement(productPrice: float, stockQty: int, productNameLength: int, adminRoleLevel: int) -> bool:
    if not (isinstance(productPrice, (int, float)) and not isinstance(productPrice, bool) and 10.0 <= productPrice <= 50000.0):
        return False
    if not (isinstance(stockQty, int) and not isinstance(stockQty, bool) and 0 <= stockQty <= 10000):
        return False
    if not (isinstance(productNameLength, int) and not isinstance(productNameLength, bool) and 5 <= productNameLength <= 100):
        return False
    if not (isinstance(adminRoleLevel, int) and not isinstance(adminRoleLevel, bool) and adminRoleLevel == 1):
        return False
    return True

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
def test_m8_admin(tc_id, price, stock, pLen, roleLvl, expected, tag):
    assert ValidateAdminManagement(price, stock, pLen, roleLvl) == expected


# Module 9: Computer Vision Inspection
def ValidateComputerVision(imageFileSizeMB: float, imageResolution: int, confidenceThreshold: float, inspectionAngleCount: int) -> bool:
    if not (isinstance(imageFileSizeMB, (int, float)) and not isinstance(imageFileSizeMB, bool) and 0.1 <= imageFileSizeMB <= 10.0):
        return False
    if not (isinstance(imageResolution, int) and not isinstance(imageResolution, bool) and 300 <= imageResolution <= 4096):
        return False
    if not (isinstance(confidenceThreshold, (int, float)) and not isinstance(confidenceThreshold, bool) and 0.50 <= confidenceThreshold <= 1.00):
        return False
    if not (isinstance(inspectionAngleCount, int) and not isinstance(inspectionAngleCount, bool) and 1 <= inspectionAngleCount <= 4):
        return False
    return True

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
def test_m9_vision(tc_id, fSize, res, conf, aCount, expected, tag):
    assert ValidateComputerVision(fSize, res, conf, aCount) == expected


# Module 10: Address Book
def ValidateAddressBook(receiverNameLength: int, receiverPhoneLength: int, detailAddressLength: int, addressBookCount: int) -> bool:
    if not (isinstance(receiverNameLength, int) and not isinstance(receiverNameLength, bool) and 2 <= receiverNameLength <= 50):
        return False
    if not (isinstance(receiverPhoneLength, int) and not isinstance(receiverPhoneLength, bool) and 10 <= receiverPhoneLength <= 11):
        return False
    if not (isinstance(detailAddressLength, int) and not isinstance(detailAddressLength, bool) and 5 <= detailAddressLength <= 200):
        return False
    if not (isinstance(addressBookCount, int) and not isinstance(addressBookCount, bool) and 1 <= addressBookCount <= 9):
        return False
    return True

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
def test_m10_address_book(tc_id, nLen, pLen, dLen, abCount, expected, tag):
    assert ValidateAddressBook(nLen, pLen, dLen, abCount) == expected
