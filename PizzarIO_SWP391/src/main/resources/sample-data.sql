-- ================================================
-- SAMPLE DATA FOR PIZZARIO APPLICATION
-- ================================================
-- Run this script manually after application starts
-- ================================================

-- Clean existing data (optional - uncomment if needed)
-- DELETE FROM [Order_Item];
-- DELETE FROM customer_order;
-- DELETE FROM [Session];
-- DELETE FROM [Product];
-- DELETE FROM [Category];

-- ================================================
-- 1. INSERT CATEGORIES
-- ================================================
INSERT INTO [Category] (name, description, is_active, created_at, updated_at) VALUES
('Pizza', N'Các loại pizza thơm ngon', 1, GETDATE(), GETDATE()),
(N'Đồ uống', N'Nước giải khát', 1, GETDATE(), GETDATE()),
(N'Món khai vị', N'Món ăn khai vị', 1, GETDATE(), GETDATE());

-- ================================================
-- 2. INSERT PRODUCTS
-- ================================================
-- Pizzas (category_id = 1)
INSERT INTO [Product] (name, description, img_url, base_price, flash_sale_price, flash_sale_start, flash_sale_end, is_active, created_at, updated_at, category_id) VALUES
('Pizza Margherita', N'Pizza phô mai tươi, sốt cà chua, lá húng quế', '/images/pizza-margherita.jpg', 129000, 0, NULL, NULL, 1, GETDATE(), GETDATE(), 1),
('Pizza Pepperoni', N'Pizza xúc xích pepperoni, phô mai mozzarella', '/images/pizza-pepperoni.jpg', 159000, 0, NULL, NULL, 1, GETDATE(), GETDATE(), 1),
(N'Pizza Hải Sản', N'Pizza tôm, mực, nghêu, sốt cocktail', '/images/pizza-seafood.jpg', 189000, 0, NULL, NULL, 1, GETDATE(), GETDATE(), 1);

-- Drinks (category_id = 2)
INSERT INTO [Product] (name, description, img_url, base_price, flash_sale_price, flash_sale_start, flash_sale_end, is_active, created_at, updated_at, category_id) VALUES
('Coca Cola', N'Nước ngọt Coca Cola 330ml', '/images/coca-cola.jpg', 15000, 0, NULL, NULL, 1, GETDATE(), GETDATE(), 2),
('Sprite', N'Nước ngọt Sprite 330ml', '/images/sprite.jpg', 15000, 0, NULL, NULL, 1, GETDATE(), GETDATE(), 2);

-- Appetizers (category_id = 3)
INSERT INTO [Product] (name, description, img_url, base_price, flash_sale_price, flash_sale_start, flash_sale_end, is_active, created_at, updated_at, category_id) VALUES
(N'Bánh Mì Bơ Tỏi', N'Bánh mì nướng bơ tỏi thơm lừng', '/images/garlic-bread.jpg', 35000, 0, NULL, NULL, 1, GETDATE(), GETDATE(), 3),
(N'Cánh Gà Chiên', N'6 cánh gà chiên giòn sốt BBQ', '/images/chicken-wings.jpg', 69000, 0, NULL, NULL, 1, GETDATE(), GETDATE(), 3);

-- ================================================
-- 3. INSERT SESSIONS FOR OCCUPIED TABLES
-- ================================================
-- Session for Table 5 (OCCUPIED)
INSERT INTO [Session] (table_id, is_closed, created_at, closed_at) VALUES
(5, 0, DATEADD(HOUR, -1, GETDATE()), NULL);

-- Session for Table 3 (WAITING_PAYMENT)
INSERT INTO [Session] (table_id, is_closed, created_at, closed_at) VALUES
(3, 0, DATEADD(HOUR, -2, GETDATE()), NULL);

-- Session for Table 9 (WAITING_PAYMENT)
INSERT INTO [Session] (table_id, is_closed, created_at, closed_at) VALUES
(9, 0, DATEADD(MINUTE, -90, GETDATE()), NULL);

-- ================================================
-- 4. INSERT ORDERS
-- ================================================
-- Order for Table 5 (session_id will be 1, staff_id = 1)
INSERT INTO customer_order (session_id, created_by, note, total_price, created_at, updated_at, order_status, order_type, payment_method, payment_status, [tax-rate]) VALUES
(1, 1, N'Khách yêu cầu không hành', 584200, DATEADD(HOUR, -1, GETDATE()), DATEADD(MINUTE, -30, GETDATE()), 'PREPARING', 'DINE_IN', NULL, 'UNPAID', 0.1);

-- Order for Table 3 (session_id will be 2, staff_id = 1)
INSERT INTO customer_order (session_id, created_by, note, total_price, created_at, updated_at, order_status, order_type, payment_method, payment_status, [tax-rate]) VALUES
(2, 1, N'Khách muốn thanh toán', 316800, DATEADD(HOUR, -2, GETDATE()), DATEADD(MINUTE, -10, GETDATE()), 'SERVED', 'DINE_IN', 'CASH', 'PENDING', 0.1);

-- Order for Table 9 (session_id will be 3, staff_id = 1)
INSERT INTO customer_order (session_id, created_by, note, total_price, created_at, updated_at, order_status, order_type, payment_method, payment_status, [tax-rate]) VALUES
(3, 1, NULL, 574200, DATEADD(MINUTE, -90, GETDATE()), DATEADD(MINUTE, -20, GETDATE()), 'SERVED', 'DINE_IN', 'CASH', 'PENDING', 0.1);

-- ================================================
-- 5. INSERT ORDER ITEMS
-- ================================================

-- Order Items for Table 5 (order_id = 1)
-- 2x Pizza Margherita (product_id = 1)
INSERT INTO [Order_Item] (order_id, product_id, unit_price, quantity, note, order_item_status, order_item_type, total_price) VALUES
(1, 1, 129000, 2, NULL, 'PREPARING', 'DINE_IN', 258000);

-- 1x Pizza Pepperoni (product_id = 2)
INSERT INTO [Order_Item] (order_id, product_id, unit_price, quantity, note, order_item_status, order_item_type, total_price) VALUES
(1, 2, 159000, 1, N'Ít cay', 'PREPARING', 'DINE_IN', 159000);

-- 3x Coca Cola (product_id = 4)
INSERT INTO [Order_Item] (order_id, product_id, unit_price, quantity, note, order_item_status, order_item_type, total_price) VALUES
(1, 4, 15000, 3, NULL, 'SERVED', 'DINE_IN', 45000);

-- 2x Bánh Mì Bơ Tỏi (product_id = 6)
INSERT INTO [Order_Item] (order_id, product_id, unit_price, quantity, note, order_item_status, order_item_type, total_price) VALUES
(1, 6, 35000, 2, NULL, 'SERVED', 'DINE_IN', 70000);

-- Order Items for Table 3 (order_id = 2)
-- 1x Pizza Hải Sản (product_id = 3)
INSERT INTO [Order_Item] (order_id, product_id, unit_price, quantity, note, order_item_status, order_item_type, total_price) VALUES
(2, 3, 189000, 1, NULL, 'SERVED', 'DINE_IN', 189000);

-- 2x Sprite (product_id = 5)
INSERT INTO [Order_Item] (order_id, product_id, unit_price, quantity, note, order_item_status, order_item_type, total_price) VALUES
(2, 5, 15000, 2, NULL, 'SERVED', 'DINE_IN', 30000);

-- 1x Cánh Gà Chiên (product_id = 7)
INSERT INTO [Order_Item] (order_id, product_id, unit_price, quantity, note, order_item_status, order_item_type, total_price) VALUES
(2, 7, 69000, 1, NULL, 'SERVED', 'DINE_IN', 69000);

-- Order Items for Table 9 (order_id = 3)
-- 1x Pizza Margherita (product_id = 1)
INSERT INTO [Order_Item] (order_id, product_id, unit_price, quantity, note, order_item_status, order_item_type, total_price) VALUES
(3, 1, 129000, 1, NULL, 'SERVED', 'DINE_IN', 129000);

-- 1x Pizza Pepperoni (product_id = 2)
INSERT INTO [Order_Item] (order_id, product_id, unit_price, quantity, note, order_item_status, order_item_type, total_price) VALUES
(3, 2, 159000, 1, N'Thêm phô mai', 'SERVED', 'DINE_IN', 159000);

-- 4x Coca Cola (product_id = 4)
INSERT INTO [Order_Item] (order_id, product_id, unit_price, quantity, note, order_item_status, order_item_type, total_price) VALUES
(3, 4, 15000, 4, NULL, 'SERVED', 'DINE_IN', 60000);

-- 1x Bánh Mì Bơ Tỏi (product_id = 6)
INSERT INTO [Order_Item] (order_id, product_id, unit_price, quantity, note, order_item_status, order_item_type, total_price) VALUES
(3, 6, 35000, 1, NULL, 'SERVED', 'DINE_IN', 35000);

-- 2x Cánh Gà Chiên (product_id = 7)
INSERT INTO [Order_Item] (order_id, product_id, unit_price, quantity, note, order_item_status, order_item_type, total_price) VALUES
(3, 7, 69000, 2, N'Sốt BBQ riêng', 'SERVED', 'DINE_IN', 138000);

-- ================================================
-- SUMMARY
-- ================================================
-- ✅ Created:
--    - 3 Categories (Pizza, Đồ uống, Món khai vị)
--    - 7 Products
--    - 3 Sessions (for tables 3, 5, 9)
--    - 3 Orders
--    - 12 Order Items
--
-- Tables with Orders:
--    - Table 5 (OCCUPIED): 4 items, total 584,200đ
--    - Table 3 (WAITING_PAYMENT): 3 items, total 316,800đ
--    - Table 9 (WAITING_PAYMENT): 5 items, total 574,200đ
-- ================================================

-- select * from customer_order
-- select * from staff
-- delete from staff
-- where id = 4
