USE shoe_shopdb;

-- =========================================
-- TEST-12: DATABASE SEED DATA
-- =========================================

SET FOREIGN_KEY_CHECKS = 0;

-- =========================================
-- 1. ACCOUNTS
-- Password demo: 123456
-- BCrypt hash lấy từ dữ liệu gốc của project
-- =========================================

INSERT IGNORE INTO Accounts
(
    USER_NAME,
    ACTIVE,
    ENCRYTED_PASSWORD,
    USER_ROLE,
    FULL_NAME,
    EMAIL,
    PHONE_NUMBER,
    ACCOUNT_NON_LOCKED,
    FAILED_ATTEMPTS,
    PROVIDER
)
VALUES
(
    'testuser',
    b'1',
    '$2a$10$PrI5Gk9L.tSZiW9FXhTS8O8Mz9E97k2FZbFvGFFaSsiTUIl.TCrFu',
    'ROLE_USER',
    'Test User',
    'testuser@gmail.com',
    '0900000001',
    TRUE,
    0,
    'LOCAL'
),
(
    'testadmin',
    b'1',
    '$2a$10$PrI5Gk9L.tSZiW9FXhTS8O8Mz9E97k2FZbFvGFFaSsiTUIl.TCrFu',
    'ROLE_ADMIN',
    'Test Admin',
    'testadmin@gmail.com',
    '0900000002',
    TRUE,
    0,
    'LOCAL'
);

-- =========================================
-- 2. PRODUCTS
-- =========================================

INSERT IGNORE INTO Products
(
    CODE,
    IMAGE,
    NAME,
    PRICE,
    CREATE_DATE,
    OWNER_USERNAME,
    DISCOUNT_PERCENT,
    SALES_COUNT,
    LOCATION,
    BRAND,
    RATING,
    IS_MALL,
    IS_FAVORED,
    STOCK_QUANTITY,
    CATEGORY,
    STATUS,
    REVIEW_COUNT
)
VALUES
(
    'TEST001',
    NULL,
    'Test Sneaker Basic',
    500000,
    NOW(),
    'testadmin',
    10,
    0,
    'Ho Chi Minh',
    'Nike',
    5,
    TRUE,
    FALSE,
    100,
    'Giày Sneaker',
    'ACTIVE',
    0
),
(
    'TEST002',
    NULL,
    'Test Sneaker Premium',
    1000000,
    NOW(),
    'testadmin',
    20,
    0,
    'Ho Chi Minh',
    'Adidas',
    5,
    TRUE,
    FALSE,
    50,
    'Giày Sneaker',
    'ACTIVE',
    0
),
(
    'TEST003',
    NULL,
    'Test Running Shoes',
    750000,
    NOW(),
    'testadmin',
    0,
    0,
    'Ha Noi',
    'Puma',
    4,
    FALSE,
    FALSE,
    30,
    'Giày Running',
    'ACTIVE',
    0
);

-- =========================================
-- 3. USER ADDRESSES
-- =========================================

INSERT INTO User_Addresses
(
    username,
    receiver_name,
    phone,
    province,
    district,
    ward,
    street_address,
    note,
    is_default
)
VALUES
(
    'testuser',
    'Test User',
    '0900000001',
    'Ho Chi Minh',
    'Quan 1',
    'Ben Nghe',
    '123 Nguyen Hue',
    'Giao trong gio hanh chinh',
    TRUE
),
(
    'testuser',
    'Test User 2',
    '0900000001',
    'Ho Chi Minh',
    'Quan 7',
    'Tan Phong',
    '456 Nguyen Huu Tho',
    'Goi truoc khi giao',
    FALSE
);

-- =========================================
-- 4. WISHLIST
-- =========================================

INSERT IGNORE INTO Wishlist
(
    username,
    product_code
)
VALUES
(
    'testuser',
    'TEST001'
),
(
    'testuser',
    'TEST002'
);

-- =========================================
-- 5. VOUCHERS
-- =========================================

INSERT INTO Vouchers
(
    code,
    discount_type,
    discount_value,
    max_discount,
    min_order_value,
    expiry_date,
    active,
    usage_limit,
    used_count,
    per_user_limit
)
VALUES
(
    'TEST20',
    'PERCENT',
    20,
    100000,
    300000,
    DATE_ADD(NOW(), INTERVAL 30 DAY),
    TRUE,
    100,
    0,
    1
),
(
    'TEST50K',
    'FIXED',
    50000,
    NULL,
    300000,
    DATE_ADD(NOW(), INTERVAL 30 DAY),
    TRUE,
    100,
    0,
    1
)
ON DUPLICATE KEY UPDATE active = TRUE;

-- =========================================
-- 6. PRODUCT REVIEWS
-- =========================================

INSERT INTO Product_Reviews
(
    Product_Code,
    Username,
    Rating_Value,
    Comment,
    Image_Url
)
VALUES
(
    'TEST001',
    'testuser',
    5,
    'San pham tot, chat luong.',
    NULL
),
(
    'TEST002',
    'testuser',
    4,
    'Giay dep va dung mo ta.',
    NULL
);

SET FOREIGN_KEY_CHECKS = 1;

-- =========================================
-- CHECK DATA
-- =========================================

SELECT * FROM Accounts;
SELECT * FROM Products;
SELECT * FROM User_Addresses;
SELECT * FROM Wishlist;
SELECT * FROM Vouchers;
SELECT * FROM Product_Reviews;