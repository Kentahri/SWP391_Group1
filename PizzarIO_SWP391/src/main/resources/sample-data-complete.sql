-- ================================================
-- PIZZARIO - EXTENDED SAMPLE DATA
-- Thêm dữ liệu mẫu đa dạng và chi tiết
-- ================================================

-- ================================================
-- 1. THÊM CATEGORIES
-- ================================================

INSERT INTO [Category] (name, description, is_active, created_at, updated_at)
VALUES
    (N'Pizza', N'Các loại bánh pizza truyền thống và đặc biệt', 1, GETDATE(), GETDATE()),
    (N'Đồ uống', N'Các loại đồ uống giải khát', 1, GETDATE(), GETDATE()),
    (N'Khai vị', N'Các món ăn nhẹ khai vị', 1, GETDATE(), GETDATE()),
    (N'Tráng miệng', N'Các món tráng miệng ngọt', 1, GETDATE(), GETDATE()),
    (N'Combo', N'Các gói combo tiết kiệm', 1, GETDATE(), GETDATE()),
    (N'Nước ép trái cây', N'Nước ép tươi nguyên chất', 1, GETDATE(), GETDATE()),
    (N'Súp', N'Súp nóng hổi', 1, GETDATE(), GETDATE()),
    (N'Món chính', N'Món ăn chính dành cho bữa trưa/tối', 1, GETDATE(), GETDATE()),
    (N'Đồ chay', N'Thực đơn chay', 1, GETDATE(), GETDATE()),
    (N'Món trẻ em', N'Món dành cho trẻ em', 1, GETDATE(), GETDATE()),
    (N'Pasta', N'Các món mì Ý', 1, GETDATE(), GETDATE());

-- ================================================
-- 2. THÊM PRODUCTS
-- ================================================

-- Đồ uống (Category ID: 2)
INSERT INTO [Product] (name, description, img_url, base_price, flash_sale_price, flash_sale_start, flash_sale_end,
                       is_active, created_at, updated_at, category_id)
VALUES
    (N'Coca Cola', N'Coca Cola lon 330ml', '/images/coca.jpg', 15000, 12000, GETDATE(), DATEADD(DAY, 7, GETDATE()), 1,
     GETDATE(), GETDATE(), 2),
    (N'Pepsi', N'Pepsi lon 330ml', '/images/pepsi.jpg', 15000, 13000, GETDATE(), DATEADD(DAY, 5, GETDATE()), 1, GETDATE(), GETDATE(), 2),
    (N'7Up', N'7Up lon 330ml', '/images/7up.jpg', 15000, 12500, GETDATE(), DATEADD(DAY, 3, GETDATE()), 1, GETDATE(), GETDATE(), 2),
    (N'Trà đào cam sả', N'Trà đào tươi mát', '/images/tra-dao.jpg', 35000, 29000, GETDATE(), DATEADD(DAY, 4, GETDATE()), 1, GETDATE(), GETDATE(), 2),
    (N'Sinh tố bơ', N'Sinh tố bơ sánh mịn', '/images/sinh-to-bo.jpg', 45000, 39000, GETDATE(), DATEADD(DAY, 2, GETDATE()), 1, GETDATE(), GETDATE(), 2);

-- Món khai vị (Category ID: 3)
INSERT INTO [Product] (name, description, img_url, base_price, flash_sale_price, flash_sale_start, flash_sale_end,
                       is_active, created_at, updated_at, category_id)
VALUES
    (N'Gà rán', N'Gà rán giòn tan 6 miếng', '/images/ga-ran.jpg', 89000, 75000, GETDATE(), DATEADD(DAY, 5, GETDATE()), 1, GETDATE(), GETDATE(), 3),
    (N'Khoai tây chiên', N'Khoai tây chiên giòn', '/images/french-fries.jpg', 45000, 39000, GETDATE(),
     DATEADD(DAY, 3, GETDATE()), 1, GETDATE(), GETDATE(), 3),
    (N'Bánh mì bơ tỏi', N'Bánh mì giòn với bơ tỏi thơm', '/images/garlic-bread.jpg', 35000, 29000, GETDATE(), DATEADD(DAY, 6, GETDATE()), 1, GETDATE(),
     GETDATE(), 3),
    (N'Chicken Wings', N'Cánh gà nướng BBQ', '/images/chicken-wings.jpg', 79000, 65000, GETDATE(), DATEADD(DAY, 4, GETDATE()), 1, GETDATE(), GETDATE(), 3),
    (N'Onion Rings', N'Hành tây chiên giòn', '/images/onion-rings.jpg', 49000, 39000, GETDATE(), DATEADD(DAY, 2, GETDATE()), 1, GETDATE(), GETDATE(), 3);

-- Tráng miệng (Category ID: 4)
INSERT INTO [Product] (name, description, img_url, base_price, flash_sale_price, flash_sale_start, flash_sale_end,
                       is_active, created_at, updated_at, category_id)
VALUES
    (N'Kem vani', N'Kem vani Ý', '/images/ice-cream-vanilla.jpg', 25000, 20000, GETDATE(), DATEADD(DAY, 3, GETDATE()), 1, GETDATE(), GETDATE(), 4),
    (N'Bánh tiramisu', N'Bánh tiramisu Ý truyền thống', '/images/tiramisu.jpg', 55000, 49000, GETDATE(),
     DATEADD(DAY, 5, GETDATE()), 1, GETDATE(), GETDATE(), 4),
    (N'Panna Cotta', N'Panna cotta dâu tây', '/images/panna-cotta.jpg', 45000, 38000, GETDATE(), DATEADD(DAY, 4, GETDATE()), 1, GETDATE(), GETDATE(), 4);

-- Combo (Category ID: 5)
INSERT INTO [Product] (name, description, img_url, base_price, flash_sale_price, flash_sale_start, flash_sale_end,
                       is_active, created_at, updated_at, category_id)
VALUES
    (N'Combo Gia Đình', N'2 Pizza cỡ lớn + 4 nước ngọt + Gà rán', '/images/combo-family.jpg', 399000, 349000, GETDATE(), DATEADD(DAY, 7, GETDATE()), 1, GETDATE(), GETDATE(), 5),
    (N'Combo Đôi', N'1 Pizza cỡ vừa + 2 nước ngọt + 1 tráng miệng', '/images/combo-couple.jpg', 249000, 219000, GETDATE(), DATEADD(DAY, 5, GETDATE()), 1, GETDATE(), GETDATE(), 5),
    (N'Combo Trẻ Em', N'1 Pizza nhỏ + 1 nước ngọt + 1 kem', '/images/combo-kids.jpg', 159000, 139000, GETDATE(), DATEADD(DAY, 4, GETDATE()), 1, GETDATE(), GETDATE(), 5);

-- Pizza (Category ID: 1)
INSERT INTO [Product] (name, description, img_url, base_price, flash_sale_price, flash_sale_start, flash_sale_end,
                       is_active, created_at, updated_at, category_id)
VALUES
    (N'Pizza Hải Sản', N'Pizza với hải sản tươi ngon', '/images/seafood-pizza.jpg', 169000, 149000, GETDATE(), DATEADD(DAY, 6, GETDATE()), 1, GETDATE(), GETDATE(), 1),
    (N'Pizza Thịt Xông Khói', N'Pizza với thịt xông khói thơm ngon', '/images/bacon-pizza.jpg', 159000, 139000, GETDATE(), DATEADD(DAY, 5, GETDATE()), 1, GETDATE(), GETDATE(), 1),
    (N'Pizza Rau Củ', N'Pizza chay với nhiều loại rau củ', '/images/veggie-pizza.jpg', 149000, 129000, GETDATE(), DATEADD(DAY, 4, GETDATE()), 1, GETDATE(), GETDATE(), 1),
    (N'Pizza Hawaii', N'Pizza với dứa và giăm bông', '/images/hawaii-pizza.jpg', 159000, 145000, GETDATE(), DATEADD(DAY, 7, GETDATE()), 1, GETDATE(), GETDATE(), 1),
    (N'Pizza Bò', N'Pizza với thịt bò thơm ngon', '/images/beef-pizza.jpg', 179000, 159000, GETDATE(), DATEADD(DAY, 3, GETDATE()), 1, GETDATE(), GETDATE(), 1);