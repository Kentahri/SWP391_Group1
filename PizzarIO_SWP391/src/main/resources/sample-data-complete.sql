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
    (N'Pepsi', N'Pepsi lon 330ml', '/images/pepsi.jpg', 15000, NULL, NULL, NULL, 1, GETDATE(), GETDATE(), 2),
    (N'7Up', N'7Up lon 330ml', '/images/7up.jpg', 15000, NULL, NULL, NULL, 1, GETDATE(), GETDATE(), 2),
    (N'Trà đào cam sả', N'Trà đào tươi mát', '/images/tra-dao.jpg', 35000, NULL, NULL, NULL, 1, GETDATE(), GETDATE(), 2),
    (N'Sinh tố bơ', N'Sinh tố bơ sánh mịn', '/images/sinh-to-bo.jpg', 45000, NULL, NULL, NULL, 1, GETDATE(), GETDATE(), 2);

-- Món khai vị (Category ID: 3)
INSERT INTO [Product] (name, description, img_url, base_price, flash_sale_price, flash_sale_start, flash_sale_end,
                       is_active, created_at, updated_at, category_id)
VALUES
    (N'Gà rán', N'Gà rán giòn tan 6 miếng', '/images/ga-ran.jpg', 89000, NULL, NULL, NULL, 1, GETDATE(), GETDATE(), 3),
    (N'Khoai tây chiên', N'Khoai tây chiên giòn', '/images/french-fries.jpg', 45000, 39000, GETDATE(),
     DATEADD(DAY, 3, GETDATE()), 1, GETDATE(), GETDATE(), 3),
    (N'Bánh mì bơ tỏi', N'Bánh mì giòn với bơ tỏi thơm', '/images/garlic-bread.jpg', 35000, NULL, NULL, NULL, 1, GETDATE(),
     GETDATE(), 3),
    (N'Chicken Wings', N'Cánh gà nướng BBQ', '/images/chicken-wings.jpg', 79000, NULL, NULL, NULL, 1, GETDATE(), GETDATE(), 3),
    (N'Onion Rings', N'Hành tây chiên giòn', '/images/onion-rings.jpg', 49000, NULL, NULL, NULL, 1, GETDATE(), GETDATE(), 3);

-- Tráng miệng (Category ID: 4)
INSERT INTO [Product] (name, description, img_url, base_price, flash_sale_price, flash_sale_start, flash_sale_end,
                       is_active, created_at, updated_at, category_id)
VALUES
    (N'Kem vani', N'Kem vani Ý', '/images/ice-cream-vanilla.jpg', 25000, NULL, NULL, NULL, 1, GETDATE(), GETDATE(), 4),
    (N'Bánh tiramisu', N'Bánh tiramisu Ý truyền thống', '/images/tiramisu.jpg', 55000, 49000, GETDATE(),
     DATEADD(DAY, 5, GETDATE()), 1, GETDATE(), GETDATE(), 4),
    (N'Panna Cotta', N'Panna cotta dâu tây', '/images/panna-cotta.jpg', 45000, NULL, NULL, NULL, 1, GETDATE(), GETDATE(), 4);

-- Combo (Category ID: 5)
INSERT INTO [Product] (name, description, img_url, base_price, flash_sale_price, flash_sale_start, flash_sale_end,
                       is_active, created_at, updated_at, category_id)
VALUES
    (N'Combo Gia Đình', N'2 Pizza cỡ lớn + 4 nước ngọt + Gà rán', '/images/combo-family.jpg', 499000, 399000, GETDATE(),
     DATEADD(DAY, 10, GETDATE()), 1, GETDATE(), GETDATE(), 5),
    (N'Combo Couple', N'1 Pizza + 2 nước + 1 tráng miệng', '/images/combo-couple.jpg', 259000, NULL, NULL, NULL, 1,
     GETDATE(), GETDATE(), 5),
    (N'Combo Solo', N'1 Pizza nhỏ + 1 nước + 1 khai vị', '/images/combo-solo.jpg', 159000, NULL, NULL, NULL, 1, GETDATE(),
     GETDATE(), 5);

-- Pasta (Category ID: 11)
INSERT INTO [Product] (name, description, img_url, base_price, flash_sale_price, flash_sale_start, flash_sale_end,
                       is_active, created_at, updated_at, category_id)
VALUES
    (N'Spaghetti Carbonara', N'Mì Ý sốt kem trứng', '/images/carbonara.jpg', 89000, NULL, NULL, NULL, 1, GETDATE(),
     GETDATE(), 11),
    (N'Spaghetti Bolognese', N'Mì Ý sốt thịt bò', '/images/bolognese.jpg', 95000, NULL, NULL, NULL, 1, GETDATE(),
     GETDATE(), 11),
    (N'Fettuccine Alfredo', N'Mì dẹt sốt kem phô mai', '/images/alfredo.jpg', 99000, NULL, NULL, NULL, 1, GETDATE(),
     GETDATE(), 11),
    (N'Lasagna', N'Mì lasagna nướng phô mai', '/images/lasagna.jpg', 129000, NULL, NULL, NULL, 1, GETDATE(), GETDATE(), 11);

-- Pizza (Category ID: 1)
INSERT INTO [Product] (name, description, img_url, base_price, flash_sale_price, flash_sale_start, flash_sale_end,
                       is_active, created_at, updated_at, category_id)
VALUES
    (N'Pizza Hải Sản', N'Pizza với hải sản tươi ngon', '/images/pizza-hai-san.jpg', 159000, NULL, NULL, NULL, 1, GETDATE(), GETDATE(), 1),
    (N'Pizza Thịt Xông Khói', N'Pizza với thịt xông khói thơm ngon', '/images/pizza-bacon.jpg', 139000, NULL, NULL, NULL, 1, GETDATE(), GETDATE(), 1),
    (N'Pizza Chay', N'Pizza với rau củ tươi ngon', '/images/pizza-chay.jpg', 119000, NULL, NULL, NULL, 1, GETDATE(), GETDATE(), 1),
    (N'Pizza Pepperoni', N'Pizza với xúc xích cay Pepperoni', '/images/pizza-pepperoni.jpg', 149000, 129000, GETDATE(), DATEADD(DAY, 7, GETDATE()), 1, GETDATE(), GETDATE(), 1),
    (N'Pizza Hawaii', N'Pizza với dứa và thịt nguội', '/images/pizza-hawaii.jpg', 139000, NULL, NULL, NULL, 1, GETDATE(), GETDATE(), 1);

-- Nước ép trái cây (Category ID: 6)
INSERT INTO [Product] (name, description, img_url, base_price, flash_sale_price, flash_sale_start, flash_sale_end,
                       is_active, created_at, updated_at, category_id)
VALUES
    (N'Nước ép cam', N'Nước ép cam tươi', '/images/nuoc-ep-cam.jpg', 35000, NULL, NULL, NULL, 1, GETDATE(), GETDATE(), 6),
    (N'Nước ép dưa hấu', N'Nước ép dưa hấu mát lạnh', '/images/nuoc-ep-dua-hau.jpg', 35000, NULL, NULL, NULL, 1, GETDATE(), GETDATE(), 6),
    (N'Nước ép táo', N'Nước ép táo tươi', '/images/nuoc-ep-tao.jpg', 40000, NULL, NULL, NULL, 1, GETDATE(), GETDATE(), 6);

-- Súp (Category ID: 7)
INSERT INTO [Product] (name, description, img_url, base_price, flash_sale_price, flash_sale_start, flash_sale_end,
                       is_active, created_at, updated_at, category_id)
VALUES
    (N'Súp kem nấm', N'Súp kem nấm thơm ngon', '/images/sup-nam.jpg', 55000, NULL, NULL, NULL, 1, GETDATE(), GETDATE(), 7),
    (N'Súp hải sản', N'Súp hải sản đặc biệt', '/images/sup-hai-san.jpg', 65000, NULL, NULL, NULL, 1, GETDATE(), GETDATE(), 7);

-- Món chính (Category ID: 8)
INSERT INTO [Product] (name, description, img_url, base_price, flash_sale_price, flash_sale_start, flash_sale_end,
                       is_active, created_at, updated_at, category_id)
VALUES
    (N'Steak bò', N'Steak bò Úc thượng hạng', '/images/steak-bo.jpg', 259000, NULL, NULL, NULL, 1, GETDATE(), GETDATE(), 8),
    (N'Cá hồi nướng', N'Cá hồi Na Uy nướng', '/images/ca-hoi-nuong.jpg', 189000, NULL, NULL, NULL, 1, GETDATE(), GETDATE(), 8);

-- Đồ chay (Category ID: 9)
INSERT INTO [Product] (name, description, img_url, base_price, flash_sale_price, flash_sale_start, flash_sale_end,
                       is_active, created_at, updated_at, category_id)
VALUES
    (N'Salad rau củ', N'Salad rau củ tươi', '/images/salad-rau-cu.jpg', 69000, NULL, NULL, NULL, 1, GETDATE(), GETDATE(), 9),
    (N'Đậu hũ sốt cà chua', N'Đậu hũ sốt cà chua cay', '/images/dau-hu-sot.jpg', 79000, NULL, NULL, NULL, 1, GETDATE(), GETDATE(), 9);

-- Món trẻ em (Category ID: 10)
INSERT INTO [Product] (name, description, img_url, base_price, flash_sale_price, flash_sale_start, flash_sale_end,
                       is_active, created_at, updated_at, category_id)
VALUES
    (N'Mì Ý trẻ em', N'Mì Ý sốt cà chua cho trẻ em', '/images/mi-y-tre-em.jpg', 69000, NULL, NULL, NULL, 1, GETDATE(), GETDATE(), 10),
    (N'Mini burger', N'Burger mini cho trẻ em', '/images/mini-burger.jpg', 59000, NULL, NULL, NULL, 1, GETDATE(), GETDATE(), 10);