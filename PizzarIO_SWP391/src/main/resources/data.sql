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
VALUES (N'Small'), (N'Medium'), (N'Large'), (N'Default');

-- 3️⃣ PRODUCT (chỉ còn thông tin chung, không còn giá)
INSERT INTO [Product] (name, description, img_url, is_active, created_at, updated_at, category_id)
VALUES
    (N'Pizza Hải Sản', N'Pizza với hải sản tươi ngon', 'https://res.cloudinary.com/dp3whqfs1/image/upload/v1762327457/pizzario/products/ffccda80-63d7-4373-bf69-00d243baca24_pizzahaisan.png.png', 1, GETDATE(), GETDATE(), 1),
    (N'Pizza Thịt Xông Khói', N'Pizza với thịt xông khói thơm ngon', 'https://res.cloudinary.com/dp3whqfs1/image/upload/v1762327506/pizzario/products/38c9b30d-7038-441a-b2b4-e2c10f3c9043_pizzathithunkhoi.png.png', 1, GETDATE(), GETDATE(), 1),
    (N'Pizza Rau Củ', N'Pizza chay với rau củ', 'https://res.cloudinary.com/dp3whqfs1/image/upload/v1762327520/pizzario/products/d717a8aa-4d86-4907-a1c3-998f92ab329d_pizzaraucu.png.png', 1, GETDATE(), GETDATE(), 1),
    (N'Pizza Hawaii', N'Pizza với dứa và giăm bông', 'https://res.cloudinary.com/dp3whqfs1/image/upload/v1762327534/pizzario/products/81a466f9-35ed-4d0a-98d3-47ba1581f6ba_pizzahawai.png.png', 1, GETDATE(), GETDATE(), 1),
    (N'Pizza Bò', N'Pizza bò đặc biệt', 'https://res.cloudinary.com/dp3whqfs1/image/upload/v1762327711/pizzario/products/56ccf511-a9ce-4b02-9aa6-c09f1c17c9fa_pizzabo.png.png', 1, GETDATE(), GETDATE(), 1),

    (N'Coca Cola', N'Nước ngọt Coca Cola lon 330ml', 'https://res.cloudinary.com/dp3whqfs1/image/upload/v1762327814/pizzario/products/ae942854-6a79-4290-b997-5e26d453dc57_cocacola.png.png', 1, GETDATE(), GETDATE(), 2),
    (N'Pepsi', N'Pepsi lon 330ml', 'https://res.cloudinary.com/dp3whqfs1/image/upload/v1762327837/pizzario/products/a5fbea03-617a-4fd7-a3d4-549c9ba41967_pepsi.png.png', 1, GETDATE(), GETDATE(), 2),
    (N'Trà đào cam sả', N'Trà đào tươi mát', 'https://res.cloudinary.com/dp3whqfs1/image/upload/v1762327896/pizzario/products/acb5e930-fc34-493e-bc9e-d695642e4310_tradaocamsa.png.png', 1, GETDATE(), GETDATE(), 2),

    (N'Gà rán', N'6 miếng gà rán giòn tan', 'https://res.cloudinary.com/dp3whqfs1/image/upload/v1762328048/pizzario/products/af9a8c34-31eb-4c3d-a6a0-34e319d9ac9b_garan.png.png', 1, GETDATE(), GETDATE(), 3),
    (N'Khoai tây chiên', N'Khoai tây chiên giòn', 'https://res.cloudinary.com/dp3whqfs1/image/upload/v1762328095/pizzario/products/b6e486b0-83a3-4589-ba6c-e24ae85ed425_khoaitaychien.png.png', 1, GETDATE(), GETDATE(), 3),

    (N'Kem vani', N'Kem vani Ý truyền thống', 'https://res.cloudinary.com/dp3whqfs1/image/upload/v1762328249/pizzario/products/3fc2a9f9-0a41-4128-9a21-de5d447f8f4d_kemvani.png.png', 1, GETDATE(), GETDATE(), 4),
    (N'Bánh tiramisu', N'Tiramisu Ý thơm ngon', 'https://res.cloudinary.com/dp3whqfs1/image/upload/v1762328312/pizzario/products/41dc87e9-a718-4dbd-9799-306d9566bce1_tiramisu.png.png', 1, GETDATE(), GETDATE(), 4),

    (N'Combo Gia Đình', N'2 Pizza lớn + 4 nước + Gà rán', 'https://res.cloudinary.com/dp3whqfs1/image/upload/v1762328379/pizzario/products/6d18454b-807e-471a-97cf-d3681c96d457_combogiadinh.png.png', 1, GETDATE(), GETDATE(), 5),
    (N'Combo Đôi', N'1 Pizza vừa + 2 nước + 1 tráng miệng', 'https://res.cloudinary.com/dp3whqfs1/image/upload/v1762328405/pizzario/products/91eebf9b-ac3a-4405-b7e3-c2f1e037c81e_combodoi.png.png', 1, GETDATE(), GETDATE(), 5),
    (N'Combo Trẻ Em', N'1 Pizza nhỏ + 1 nước + 1 kem', 'https://res.cloudinary.com/dp3whqfs1/image/upload/v1762328459/pizzario/products/382e9877-cff1-4430-8d05-efda1f761fc9_combotreem.png.png', 1, GETDATE(), GETDATE(), 5);

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
    (13, 4, 399000, 349000, GETDATE(), DATEADD(DAY, 7, GETDATE())),
    (14, 4, 249000, 219000, GETDATE(), DATEADD(DAY, 5, GETDATE())),
    (15, 4, 159000, 139000, GETDATE(), DATEADD(DAY, 4, GETDATE()));
