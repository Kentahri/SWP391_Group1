-- ================================================
-- COMPLETE SAMPLE DATA FOR PIZZARIO APPLICATION
-- ================================================
-- This script creates 10 records for each entity
-- Run this script after application starts
-- ================================================

-- Clean existing data (optional - uncomment if needed)
-- DELETE FROM [Order_Item];
-- DELETE FROM customer_order;
-- DELETE FROM [Session];
-- DELETE FROM [Reservation];
-- DELETE FROM [Staff_Shift];
-- DELETE FROM [Staff];
-- DELETE FROM [Shift];
-- DELETE FROM [Dining_Table];
-- DELETE FROM [Product];
-- DELETE FROM [Category];
-- DELETE FROM [Membership];
-- DELETE FROM Voucher;

-- ================================================
-- 1. INSERT CATEGORIES (10 records)
-- ================================================
INSERT INTO [Category] (name, description, is_active, created_at, updated_at) VALUES
('Pizza', N'Các loại pizza thơm ngon', 1, GETDATE(), GETDATE()),
(N'Đồ uống', N'Nước giải khát', 1, GETDATE(), GETDATE()),
(N'Món khai vị', N'Món ăn khai vị', 1, GETDATE(), GETDATE()),
(N'Tráng miệng', N'Các món tráng miệng', 1, GETDATE(), GETDATE()),
(N'Combo', N'Combo tiết kiệm', 1, GETDATE(), GETDATE()),
(N'Pizza đặc biệt', N'Pizza theo yêu cầu', 1, GETDATE(), GETDATE()),
(N'Đồ ăn nhanh', N'Đồ ăn nhanh', 1, GETDATE(), GETDATE()),
(N'Salad', N'Rau xanh tươi', 1, GETDATE(), GETDATE()),
(N'Pasta', N'Mì Ý thơm ngon', 1, GETDATE(), GETDATE()),
(N'Đồ nướng', N'Đồ nướng BBQ', 1, GETDATE(), GETDATE());

-- ================================================
-- 2. INSERT PRODUCTS (10 records)
-- ================================================
INSERT INTO [Product] (name, description, img_url, base_price, flash_sale_price, flash_sale_start, flash_sale_end, is_active, created_at, updated_at, category_id) VALUES
('Pizza Margherita', N'Pizza phô mai tươi, sốt cà chua, lá húng quế', '/images/pizza-margherita.jpg', 129000, 0, NULL, NULL, 1, GETDATE(), GETDATE(), 1),
('Pizza Pepperoni', N'Pizza xúc xích pepperoni, phô mai mozzarella', '/images/pizza-pepperoni.jpg', 159000, 0, NULL, NULL, 1, GETDATE(), GETDATE(), 1),
(N'Pizza Hải Sản', N'Pizza tôm, mực, nghêu, sốt cocktail', '/images/pizza-seafood.jpg', 189000, 0, NULL, NULL, 1, GETDATE(), GETDATE(), 1),
('Pizza BBQ Chicken', N'Pizza gà BBQ, hành tây, phô mai', '/images/pizza-bbq-chicken.jpg', 179000, 0, NULL, NULL, 1, GETDATE(), GETDATE(), 1),
('Pizza 4 Cheese', N'Pizza 4 loại phô mai', '/images/pizza-4-cheese.jpg', 169000, 0, NULL, NULL, 1, GETDATE(), GETDATE(), 1),
('Pizza Vegetarian', N'Pizza rau củ tươi', '/images/pizza-vegetarian.jpg', 149000, 0, NULL, NULL, 1, GETDATE(), GETDATE(), 1),
('Pizza Supreme', N'Pizza đầy đủ topping', '/images/pizza-supreme.jpg', 199000, 0, NULL, NULL, 1, GETDATE(), GETDATE(), 1),
('Pizza Hawaiian', N'Pizza dứa, giăm bông', '/images/pizza-hawaiian.jpg', 179000, 0, NULL, NULL, 1, GETDATE(), GETDATE(), 1),
('Pizza Meat Lovers', N'Pizza cho người yêu thịt', '/images/pizza-meat-lovers.jpg', 189000, 0, NULL, NULL, 1, GETDATE(), GETDATE(), 1),
('Pizza Deluxe', N'Pizza cao cấp', '/images/pizza-deluxe.jpg', 219000, 0, NULL, NULL, 1, GETDATE(), GETDATE(), 1);

-- ================================================
-- 3. INSERT STAFF (10 records)
-- ================================================
INSERT INTO [Staff] (name, dob, phone, address, password, email, role, is_active) VALUES
(N'Nguyễn Văn An', '1990-01-15', '0123456789', N'123 Đường ABC, Quận 1, TP.HCM', 'password123', 'an.nguyen@email.com', 'MANAGER', 1),
(N'Trần Thị Bình', '1992-03-20', '0123456790', N'456 Đường DEF, Quận 2, TP.HCM', 'password123', 'binh.tran@email.com', 'CASHIER', 1),
(N'Lê Văn Cường', '1988-07-10', '0123456791', N'789 Đường GHI, Quận 3, TP.HCM', 'password123', 'cuong.le@email.com', 'KITCHEN', 1),
(N'Phạm Thị Dung', '1995-05-25', '0123456792', N'321 Đường JKL, Quận 4, TP.HCM', 'password123', 'dung.pham@email.com', 'CASHIER', 1),
(N'Hoàng Văn Em', '1991-09-12', '0123456793', N'654 Đường MNO, Quận 5, TP.HCM', 'password123', 'em.hoang@email.com', 'KITCHEN', 1),
(N'Vũ Thị Phương', '1993-11-08', '0123456794', N'987 Đường PQR, Quận 6, TP.HCM', 'password123', 'phuong.vu@email.com', 'CASHIER', 1),
(N'Đặng Văn Giang', '1989-04-18', '0123456795', N'147 Đường STU, Quận 7, TP.HCM', 'password123', 'giang.dang@email.com', 'MANAGER', 1),
(N'Bùi Thị Hoa', '1994-06-30', '0123456796', N'258 Đường VWX, Quận 8, TP.HCM', 'password123', 'hoa.bui@email.com', 'CASHIER', 1),
(N'Ngô Văn Ích', '1990-12-05', '0123456797', N'369 Đường YZA, Quận 9, TP.HCM', 'password123', 'ich.ngo@email.com', 'KITCHEN', 1),
(N'Lý Thị Kim', '1992-08-22', '0123456798', N'741 Đường BCD, Quận 10, TP.HCM', 'password123', 'kim.ly@email.com', 'CASHIER', 1);

-- ================================================
-- 4. INSERT SHIFTS (10 records)
-- ================================================
INSERT INTO [Shift] (shift_name, start_time, end_time, created_at, salary_per_shift) VALUES
(N'SÁNG', '2024-01-01 06:00:00', '2024-01-01 14:00:00', GETDATE(), 150000),
(N'CHIỀU', '2024-01-01 14:00:00', '2024-01-01 22:00:00', GETDATE(), 150000),
(N'TỐI', '2024-01-01 22:00:00', '2024-01-02 06:00:00', GETDATE(), 200000),


-- ================================================
-- 5. INSERT DINING TABLES (10 records)
-- ================================================
INSERT INTO [Dining_Table] (table_status, table_condition, created_at, updated_at, capacity, version) VALUES
('AVAILABLE', 'GOOD', GETDATE(), GETDATE(), 4, 1),
('AVAILABLE', 'GOOD', GETDATE(), GETDATE(), 2, 1),
('AVAILABLE', 'GOOD', GETDATE(), GETDATE(), 6, 1),
('AVAILABLE', 'GOOD', GETDATE(), GETDATE(), 4, 1),
('AVAILABLE', 'GOOD', GETDATE(), GETDATE(), 8, 1),
('AVAILABLE', 'GOOD', GETDATE(), GETDATE(), 2, 1),
('AVAILABLE', 'GOOD', GETDATE(), GETDATE(), 4, 1),
('AVAILABLE', 'GOOD', GETDATE(), GETDATE(), 6, 1),
('AVAILABLE', 'GOOD', GETDATE(), GETDATE(), 4, 1),
('AVAILABLE', 'GOOD', GETDATE(), GETDATE(), 10, 1);

-- ================================================
-- 6. INSERT MEMBERSHIPS (10 records)
-- ================================================
INSERT INTO [Membership] (phone, name, is_active, joined_at, points) VALUES
('0123456789', N'Nguyễn Văn Khách', 1, GETDATE(), 100),
('0123456790', N'Trần Thị Khách', 1, GETDATE(), 150),
('0123456791', N'Lê Văn Khách', 1, GETDATE(), 200),
('0123456792', N'Phạm Thị Khách', 1, GETDATE(), 75),
('0123456793', N'Hoàng Văn Khách', 1, GETDATE(), 300),
('0123456794', N'Vũ Thị Khách', 1, GETDATE(), 125),
('0123456795', N'Đặng Văn Khách', 1, GETDATE(), 250),
('0123456796', N'Bùi Thị Khách', 1, GETDATE(), 180),
('0123456797', N'Ngô Văn Khách', 1, GETDATE(), 90),
('0123456798', N'Lý Thị Khách', 1, GETDATE(), 220);

-- ================================================
-- 7. INSERT VOUCHERS (10 records)
-- ================================================
INSERT INTO Voucher (code, type, value, description, max_uses, times_used, min_order_amount, valid_from, valid_to, is_active) VALUES
('WELCOME10', 'PERCENTAGE', 10, N'Giảm 10% cho khách hàng mới', 100, 0, 100000, GETDATE(), DATEADD(MONTH, 1, GETDATE()), 1),
('SAVE50K', 'FIXED_AMOUNT', 50000, N'Giảm 50,000đ cho đơn hàng từ 200,000đ', 50, 0, 200000, GETDATE(), DATEADD(MONTH, 2, GETDATE()), 1),
('PIZZA20', 'PERCENTAGE', 20, N'Giảm 20% cho pizza', 200, 0, 150000, GETDATE(), DATEADD(MONTH, 3, GETDATE()), 1),
('COMBO15', 'PERCENTAGE', 15, N'Giảm 15% cho combo', 150, 0, 300000, GETDATE(), DATEADD(MONTH, 2, GETDATE()), 1),
('NEW100K', 'FIXED_AMOUNT', 100000, N'Giảm 100,000đ cho đơn hàng từ 500,000đ', 30, 0, 500000, GETDATE(), DATEADD(MONTH, 1, GETDATE()), 1),
('VIP25', 'PERCENTAGE', 25, N'Giảm 25% cho khách VIP', 25, 0, 400000, GETDATE(), DATEADD(MONTH, 6, GETDATE()), 1),
('WEEKEND30', 'PERCENTAGE', 30, N'Giảm 30% cuối tuần', 100, 0, 200000, GETDATE(), DATEADD(MONTH, 1, GETDATE()), 1),
('STUDENT15', 'PERCENTAGE', 15, N'Giảm 15% cho sinh viên', 500, 0, 100000, GETDATE(), DATEADD(MONTH, 4, GETDATE()), 1),
('FAMILY20', 'PERCENTAGE', 20, N'Giảm 20% cho gia đình', 200, 0, 400000, GETDATE(), DATEADD(MONTH, 3, GETDATE()), 1),
('BIRTHDAY50', 'FIXED_AMOUNT', 50000, N'Giảm 50,000đ sinh nhật', 1000, 0, 150000, GETDATE(), DATEADD(YEAR, 1, GETDATE()), 1);

-- ================================================
-- 8. INSERT SESSIONS (10 records)
-- ================================================
INSERT INTO [Session] (table_id, is_closed, created_at, closed_at) VALUES
(1, 0, GETDATE(), NULL),
(2, 0, GETDATE(), NULL),
(3, 1, DATEADD(HOUR, -2, GETDATE()), DATEADD(HOUR, -1, GETDATE())),
(4, 0, GETDATE(), NULL),
(5, 1, DATEADD(HOUR, -3, GETDATE()), DATEADD(HOUR, -1, GETDATE())),
(6, 0, GETDATE(), NULL),
(7, 1, DATEADD(HOUR, -4, GETDATE()), DATEADD(HOUR, -2, GETDATE())),
(8, 0, GETDATE(), NULL),
(9, 1, DATEADD(HOUR, -1, GETDATE()), DATEADD(MINUTE, -30, GETDATE())),
(10, 0, GETDATE(), NULL);

-- ================================================
-- 9. INSERT RESERVATIONS (10 records)
-- ================================================
INSERT INTO [Reservation] (table_id, start_time, created_at, status, phone, name, capacity_expected, note) VALUES
(1, DATEADD(HOUR, 2, GETDATE()), GETDATE(), 'CONFIRMED', '0123456789', N'Nguyễn Văn Khách', 4, N'Bàn gần cửa sổ'),
(2, DATEADD(HOUR, 3, GETDATE()), GETDATE(), 'CONFIRMED', '0123456790', N'Trần Thị Khách', 2, N'Bàn yên tĩnh'),
(3, DATEADD(HOUR, 4, GETDATE()), GETDATE(), 'CONFIRMED', '0123456791', N'Lê Văn Khách', 6, N'Bàn lớn cho gia đình'),
(4, DATEADD(HOUR, 5, GETDATE()), GETDATE(), 'CONFIRMED', '0123456792', N'Phạm Thị Khách', 4, N'Bàn gần lối ra'),
(5, DATEADD(HOUR, 6, GETDATE()), GETDATE(), 'CONFIRMED', '0123456793', N'Hoàng Văn Khách', 8, N'Bàn VIP'),
(6, DATEADD(HOUR, 7, GETDATE()), GETDATE(), 'CONFIRMED', '0123456794', N'Vũ Thị Khách', 2, N'Bàn lãng mạn'),
(7, DATEADD(HOUR, 8, GETDATE()), GETDATE(), 'CONFIRMED', '0123456795', N'Đặng Văn Khách', 4, N'Bàn gần quầy bar'),
(8, DATEADD(HOUR, 9, GETDATE()), GETDATE(), 'CONFIRMED', '0123456796', N'Bùi Thị Khách', 6, N'Bàn cho nhóm bạn'),
(9, DATEADD(HOUR, 10, GETDATE()), GETDATE(), 'CONFIRMED', '0123456797', N'Ngô Văn Khách', 4, N'Bàn gần cửa chính'),
(10, DATEADD(HOUR, 11, GETDATE()), GETDATE(), 'CONFIRMED', '0123456798', N'Lý Thị Khách', 10, N'Bàn cho sự kiện');

-- ================================================
-- 10. INSERT STAFF SHIFTS (10 records)
-- ================================================
INSERT INTO [Staff_Shift] (staff_id, shift_id, work_date, status, check_in, check_out, hourly_wage, note, penalty_percent) VALUES
(1, 1, CAST(GETDATE() AS DATE), 'COMPLETED', DATEADD(HOUR, -8, GETDATE()), DATEADD(HOUR, -1, GETDATE()), 150000, N'Ca sáng hoàn thành', 0),
(2, 2, CAST(GETDATE() AS DATE), 'PRESENT', DATEADD(HOUR, -6, GETDATE()), NULL, 150000, N'Đang làm ca chiều', 0),
(3, 3, CAST(GETDATE() AS DATE), 'SCHEDULED', NULL, NULL, 200000, N'Ca tối sắp bắt đầu', 0),
(4, 1, CAST(GETDATE() AS DATE), 'COMPLETED', DATEADD(HOUR, -9, GETDATE()), DATEADD(HOUR, -2, GETDATE()), 150000, N'Ca sáng hoàn thành tốt', 0),
(5, 2, CAST(GETDATE() AS DATE), 'LATE', DATEADD(HOUR, -5, GETDATE()), NULL, 150000, N'Đến muộn 15 phút', 5),
(6, 3, CAST(GETDATE() AS DATE), 'SCHEDULED', NULL, NULL, 200000, N'Ca tối chuẩn bị', 0),
(7, 1, CAST(GETDATE() AS DATE), 'COMPLETED', DATEADD(HOUR, -10, GETDATE()), DATEADD(HOUR, -3, GETDATE()), 150000, N'Ca sáng xuất sắc', 0),
(8, 2, CAST(GETDATE() AS DATE), 'PRESENT', DATEADD(HOUR, -4, GETDATE()), NULL, 150000, N'Đang làm ca chiều', 0),
(9, 3, CAST(GETDATE() AS DATE), 'SCHEDULED', NULL, NULL, 200000, N'Ca tối sẵn sàng', 0),
(10, 1, CAST(GETDATE() AS DATE), 'ABSENT', NULL, NULL, 150000, N'Vắng mặt không lý do', 10);

-- ================================================
-- 11. INSERT ORDERS (10 records)
-- ================================================
INSERT INTO customer_order (staff_id, session_id, voucher_id, member_id, note, total_price, created_at, updated_at, order_status, order_type, payment_method, payment_status, tax_rate) VALUES
(2, 1, NULL, 1, N'Không hành tây', 258000, GETDATE(), GETDATE(), 'PREPARING', 'DINE_IN', 'CASH', 'UNPAID', 0.1),
(4, 2, 1, 2, N'Thêm phô mai', 318000, GETDATE(), GETDATE(), 'SERVED', 'DINE_IN', 'CREDIT_CARD', 'PAID', 0.1),
(6, 4, 2, 3, N'Ít cay', 378000, GETDATE(), GETDATE(), 'COMPLETED', 'DINE_IN', 'QR_BANKING', 'PAID', 0.1),
(8, 6, NULL, 4, N'Không ớt', 298000, GETDATE(), GETDATE(), 'PREPARING', 'TAKE_AWAY', 'CASH', 'UNPAID', 0.1),
(2, 8, 3, 5, N'Thêm rau', 418000, GETDATE(), GETDATE(), 'SERVED', 'DINE_IN', 'CREDIT_CARD', 'PAID', 0.1),
(4, 10, NULL, 6, N'Ít muối', 338000, GETDATE(), GETDATE(), 'PREPARING', 'DINE_IN', 'CASH', 'UNPAID', 0.1),
(6, 1, 4, 7, N'Không tỏi', 458000, GETDATE(), GETDATE(), 'COMPLETED', 'DINE_IN', 'QR_BANKING', 'PAID', 0.1),
(8, 2, NULL, 8, N'Thêm sốt', 278000, GETDATE(), GETDATE(), 'SERVED', 'TAKE_AWAY', 'CASH', 'PAID', 0.1),
(2, 4, 5, 9, N'Ít dầu', 398000, GETDATE(), GETDATE(), 'PREPARING', 'DINE_IN', 'CREDIT_CARD', 'UNPAID', 0.1),
(4, 6, NULL, 10, N'Không hành', 358000, GETDATE(), GETDATE(), 'COMPLETED', 'DINE_IN', 'QR_BANKING', 'PAID', 0.1);

-- ================================================
-- 12. INSERT ORDER ITEMS (10 records)
-- ================================================
INSERT INTO [Order_Item] (order_id, product_id, quantity, unit_price, total_price) VALUES
(1, 1, 2, 129000, 258000),
(2, 2, 2, 159000, 318000),
(3, 3, 2, 189000, 378000),
(4, 4, 2, 179000, 358000),
(5, 5, 2, 169000, 338000),
(6, 6, 2, 149000, 298000),
(7, 7, 2, 199000, 398000),
(8, 8, 2, 179000, 358000),
(9, 9, 2, 189000, 378000),
(10, 10, 2, 219000, 438000);

-- ================================================
-- SUMMARY
-- ================================================
-- ✅ Created sample data:
--    - 10 Categories
--    - 10 Products  
--    - 10 Staff members
--    - 10 Shifts
--    - 10 Dining Tables
--    - 10 Memberships
--    - 10 Vouchers
--    - 10 Sessions
--    - 10 Reservations
--    - 10 Staff Shifts
--    - 10 Orders
--    - 10 Order Items
-- ================================================
