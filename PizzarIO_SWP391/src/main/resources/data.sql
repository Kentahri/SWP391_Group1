-- ================================================
-- PIZZARIO - SAMPLE DATA (ENTITY UPDATED)
-- ================================================

-- 1️⃣ CATEGORY
INSERT INTO [Category] (name, description, is_active, created_at, updated_at)
VALUES
    (N'Pizza', N'Các loại pizza truyền thống', 1, GETDATE(), GETDATE()),
    (N'Đồ uống', N'Các loại nước giải khát', 1, GETDATE(), GETDATE()),
    (N'Khai vị', N'Món ăn nhẹ trước bữa chính', 1, GETDATE(), GETDATE()),
    (N'Tráng miệng', N'Món ngọt kết thúc bữa ăn', 1, GETDATE(), GETDATE()),
    (N'Combo', N'Gói combo tiết kiệm', 1, GETDATE(), GETDATE());

-- 2️⃣ SIZE
INSERT INTO [Size] (size_name)
VALUES (N'Small'), (N'Medium'), (N'Large');

-- 3️⃣ PRODUCT (chỉ còn thông tin chung, không còn giá)
INSERT INTO [Product] (name, description, img_url, is_active, created_at, updated_at, category_id)
VALUES
    (N'Pizza Hải Sản', N'Pizza với hải sản tươi ngon', '/images/seafood-pizza.jpg', 1, GETDATE(), GETDATE(), 1),
    (N'Pizza Thịt Xông Khói', N'Pizza với thịt xông khói thơm ngon', '/images/bacon-pizza.jpg', 1, GETDATE(), GETDATE(), 1),
    (N'Pizza Rau Củ', N'Pizza chay với rau củ', '/images/veggie-pizza.jpg', 1, GETDATE(), GETDATE(), 1),
    (N'Pizza Hawaii', N'Pizza với dứa và giăm bông', '/images/hawaii-pizza.jpg', 1, GETDATE(), GETDATE(), 1),
    (N'Pizza Bò', N'Pizza bò đặc biệt', '/images/beef-pizza.jpg', 1, GETDATE(), GETDATE(), 1),

    (N'Coca Cola', N'Nước ngọt Coca Cola lon 330ml', '/images/coca.jpg', 1, GETDATE(), GETDATE(), 2),
    (N'Pepsi', N'Pepsi lon 330ml', '/images/pepsi.jpg', 1, GETDATE(), GETDATE(), 2),
    (N'Trà đào cam sả', N'Trà đào tươi mát', '/images/tra-dao.jpg', 1, GETDATE(), GETDATE(), 2),

    (N'Gà rán', N'6 miếng gà rán giòn tan', '/images/ga-ran.jpg', 1, GETDATE(), GETDATE(), 3),
    (N'Khoai tây chiên', N'Khoai tây chiên giòn', '/images/french-fries.jpg', 1, GETDATE(), GETDATE(), 3),

    (N'Kem vani', N'Kem vani Ý truyền thống', '/images/ice-cream-vanilla.jpg', 1, GETDATE(), GETDATE(), 4),
    (N'Bánh tiramisu', N'Tiramisu Ý thơm ngon', '/images/tiramisu.jpg', 1, GETDATE(), GETDATE(), 4),

    (N'Combo Gia Đình', N'2 Pizza lớn + 4 nước + Gà rán', '/images/combo-family.jpg', 1, GETDATE(), GETDATE(), 5),
    (N'Combo Đôi', N'1 Pizza vừa + 2 nước + 1 tráng miệng', '/images/combo-couple.jpg', 1, GETDATE(), GETDATE(), 5),
    (N'Combo Trẻ Em', N'1 Pizza nhỏ + 1 nước + 1 kem', '/images/combo-kids.jpg', 1, GETDATE(), GETDATE(), 5);

-- 4️⃣ PRODUCT_SIZE (chứa giá, flash sale, thời gian)
INSERT INTO [Product_Size] (product_id, size_id, base_price, flash_sale_price, flash_sale_start, flash_sale_end)
VALUES
    -- Pizza Hải Sản
    (1, 1, 139000, 119000, GETDATE(), DATEADD(DAY, 5, GETDATE())),
    (1, 2, 169000, 149000, GETDATE(), DATEADD(DAY, 5, GETDATE())),
    (1, 3, 199000, 179000, GETDATE(), DATEADD(DAY, 5, GETDATE())),

    -- Pizza Thịt Xông Khói
    (2, 1, 129000, 109000, GETDATE(), DATEADD(DAY, 3, GETDATE())),
    (2, 2, 159000, 139000, GETDATE(), DATEADD(DAY, 3, GETDATE())),
    (2, 3, 189000, 159000, GETDATE(), DATEADD(DAY, 3, GETDATE())),

    -- Pizza Rau Củ
    (3, 1, 119000, 99000, GETDATE(), DATEADD(DAY, 4, GETDATE())),
    (3, 2, 149000, 129000, GETDATE(), DATEADD(DAY, 4, GETDATE())),
    (3, 3, 179000, 159000, GETDATE(), DATEADD(DAY, 4, GETDATE())),

    -- Pizza Hawaii
    (4, 1, 125000, 109000, GETDATE(), DATEADD(DAY, 6, GETDATE())),
    (4, 2, 159000, 139000, GETDATE(), DATEADD(DAY, 6, GETDATE())),
    (4, 3, 189000, 159000, GETDATE(), DATEADD(DAY, 6, GETDATE())),

    -- Pizza Bò
    (5, 1, 149000, 129000, GETDATE(), DATEADD(DAY, 2, GETDATE())),
    (5, 2, 179000, 159000, GETDATE(), DATEADD(DAY, 2, GETDATE())),
    (5, 3, 209000, 189000, GETDATE(), DATEADD(DAY, 2, GETDATE())),

    -- Đồ uống
    (6, 1, 15000, 12000, GETDATE(), DATEADD(DAY, 7, GETDATE())),
    (7, 1, 15000, 13000, GETDATE(), DATEADD(DAY, 5, GETDATE())),
    (8, 1, 35000, 29000, GETDATE(), DATEADD(DAY, 4, GETDATE())),

    -- Khai vị
    (9, 2, 89000, 75000, GETDATE(), DATEADD(DAY, 5, GETDATE())),
    (10, 2, 45000, 39000, GETDATE(), DATEADD(DAY, 3, GETDATE())),

    -- Tráng miệng
    (11, 1, 25000, 20000, GETDATE(), DATEADD(DAY, 3, GETDATE())),
    (12, 2, 55000, 49000, GETDATE(), DATEADD(DAY, 5, GETDATE())),

    -- Combo
    (13, 2, 399000, 349000, GETDATE(), DATEADD(DAY, 7, GETDATE())),
    (14, 2, 249000, 219000, GETDATE(), DATEADD(DAY, 5, GETDATE())),
    (15, 2, 159000, 139000, GETDATE(), DATEADD(DAY, 4, GETDATE()));
