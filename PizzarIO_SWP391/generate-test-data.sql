-- ============================================
-- PIZZARIO - GENERATE 200 TEST ORDERS
-- Script tạo 200 đơn hàng phân bố 3 orders/ngày
-- ============================================

USE [pizzario_database];
GO

-- ============================================
-- BƯỚC 1: TẠO STAFF (17 nhân viên)
-- ============================================

PRINT 'Step 1: Generating Staff...';

-- 10 Cashiers
INSERT INTO [Staff] (name, dob, phone, address, password, email, role, isActive) VALUES
(N'Nguyễn Văn A', '1995-03-15', '0901234567', N'123 Lê Lợi, Q1, TP.HCM', '$2a$10$encoded_password', 'nva@pizzario.com', 'CASHIER', 1),
(N'Trần Thị B', '1998-07-20', '0901234568', N'456 Nguyễn Huệ, Q1, TP.HCM', '$2a$10$encoded_password', 'ttb@pizzario.com', 'CASHIER', 1),
(N'Lê Văn C', '1997-01-10', '0901234569', N'789 Hai Bà Trưng, Q3, TP.HCM', '$2a$10$encoded_password', 'lvc@pizzario.com', 'CASHIER', 1),
(N'Phạm Thị D', '1996-11-25', '0901234570', N'321 Pasteur, Q3, TP.HCM', '$2a$10$encoded_password', 'ptd@pizzario.com', 'CASHIER', 1),
(N'Hoàng Văn E', '1999-05-30', '0901234571', N'654 Võ Văn Tần, Q3, TP.HCM', '$2a$10$encoded_password', 'hve@pizzario.com', 'CASHIER', 1),
(N'Vũ Thị F', '1994-09-12', '0901234572', N'987 Cách Mạng Tháng 8, Q10', '$2a$10$encoded_password', 'vtf@pizzario.com', 'CASHIER', 1),
(N'Đặng Văn G', '2000-02-18', '0901234573', N'111 Tôn Thất Tùng, Q1', '$2a$10$encoded_password', 'dvg@pizzario.com', 'CASHIER', 1),
(N'Bùi Thị H', '1998-12-05', '0901234574', N'222 Điện Biên Phủ, Q3', '$2a$10$encoded_password', 'bth@pizzario.com', 'CASHIER', 1),
(N'Ngô Văn I', '1997-08-22', '0901234575', N'333 Lê Văn Sỹ, Q3', '$2a$10$encoded_password', 'nvi@pizzario.com', 'CASHIER', 1),
(N'Trương Thị K', '1995-04-14', '0901234576', N'444 Nguyễn Thị Minh Khai, Q1', '$2a$10$encoded_password', 'ttk@pizzario.com', 'CASHIER', 1);

-- 5 Kitchen Staff
INSERT INTO [Staff] (name, dob, phone, address, password, email, role, isActive) VALUES
(N'Lý Văn Bếp 1', '1992-06-10', '0901234577', N'555 Trần Hưng Đạo, Q5', '$2a$10$encoded_password', 'lvb1@pizzario.com', 'KITCHEN', 1),
(N'Mai Thị Bếp 2', '1993-10-25', '0901234578', N'666 Nguyễn Trãi, Q5', '$2a$10$encoded_password', 'mtb2@pizzario.com', 'KITCHEN', 1),
(N'Đỗ Văn Bếp 3', '1991-03-18', '0901234579', N'777 Lý Thường Kiệt, Q10', '$2a$10$encoded_password', 'dvb3@pizzario.com', 'KITCHEN', 1),
(N'Hồ Thị Bếp 4', '1994-07-30', '0901234580', N'888 Trường Chinh, Tân Bình', '$2a$10$encoded_password', 'htb4@pizzario.com', 'KITCHEN', 1),
(N'Võ Văn Bếp 5', '1990-12-12', '0901234581', N'999 Hoàng Văn Thụ, Tân Bình', '$2a$10$encoded_password', 'vvb5@pizzario.com', 'KITCHEN', 1);

-- 1 Manager (Hệ thống chỉ cho phép 1 manager)
INSERT INTO [Staff] (name, dob, phone, address, password, email, role, isActive) VALUES
(N'Nguyễn Quản Lý', '1985-01-20', '0901234582', N'100 Nguyễn Văn Cừ, Q5', '$2a$10$encoded_password', 'manager@pizzario.com', 'MANAGER', 1);

-- 1 Shipper
INSERT INTO [Staff] (name, dob, phone, address, password, email, role, isActive) VALUES
(N'Trần Giao Hàng', '1996-08-10', '0901234583', N'200 Cộng Hòa, Tân Bình', '$2a$10$encoded_password', 'shipper@pizzario.com', 'SHIPPER', 1);

PRINT 'Generated 17 Staff members';

-- ============================================
-- BƯỚC 2: TẠO DINING TABLES (15 bàn)
-- ============================================

PRINT 'Step 2: Generating Dining Tables...';

INSERT INTO [Dining_Table] (version, table_status, table_condition, created_at, updated_at, capacity) VALUES
(0, 'AVAILABLE', 'GOOD', GETDATE(), GETDATE(), 2),
(0, 'AVAILABLE', 'GOOD', GETDATE(), GETDATE(), 2),
(0, 'AVAILABLE', 'GOOD', GETDATE(), GETDATE(), 4),
(0, 'AVAILABLE', 'GOOD', GETDATE(), GETDATE(), 4),
(0, 'AVAILABLE', 'GOOD', GETDATE(), GETDATE(), 4),
(0, 'AVAILABLE', 'NEW', GETDATE(), GETDATE(), 4),
(0, 'AVAILABLE', 'NEW', GETDATE(), GETDATE(), 6),
(0, 'AVAILABLE', 'GOOD', GETDATE(), GETDATE(), 6),
(0, 'AVAILABLE', 'GOOD', GETDATE(), GETDATE(), 6),
(0, 'AVAILABLE', 'WORN', GETDATE(), GETDATE(), 8),
(0, 'AVAILABLE', 'GOOD', GETDATE(), GETDATE(), 8),
(0, 'AVAILABLE', 'GOOD', GETDATE(), GETDATE(), 10),
(0, 'AVAILABLE', 'NEW', GETDATE(), GETDATE(), 10),
(0, 'AVAILABLE', 'GOOD', GETDATE(), GETDATE(), 12),
(0, 'AVAILABLE', 'GOOD', GETDATE(), GETDATE(), 12);

PRINT 'Generated 15 Dining Tables';

-- ============================================
-- BƯỚC 3: TẠO MEMBERSHIPS (100 khách hàng)
-- ============================================

PRINT 'Step 3: Generating Memberships...';

DECLARE @i INT = 1;
WHILE @i <= 100
BEGIN
    INSERT INTO [Membership] (phone, name, is_active, joined_at, points)
    VALUES (
        '09' + RIGHT('00000000' + CAST(@i AS VARCHAR(8)), 8),
        N'Khách hàng ' + CAST(@i AS NVARCHAR(10)),
        1,
        DATEADD(DAY, -FLOOR(RAND()*365), GETDATE()),
        FLOOR(RAND() * 1000)
    );
    SET @i = @i + 1;
END

PRINT 'Generated 100 Memberships';

-- ============================================
-- BƯỚC 4: TẠO VOUCHERS (10 mã giảm giá)
-- ============================================

PRINT 'Step 4: Generating Vouchers...';

INSERT INTO [Voucher] (code, type, value, description, max_uses, times_used, min_order_amount, valid_from, valid_to, is_active) VALUES
('SUMMER2024', 'PERCENTAGE', 10, N'Giảm 10% cho đơn hàng từ 200k', 100, 0, 200000, DATEADD(DAY, -60, GETDATE()), DATEADD(DAY, 30, GETDATE()), 1),
('NEWCUSTOMER', 'FIXED_AMOUNT', 50000, N'Giảm 50k cho khách hàng mới', 200, 0, 100000, DATEADD(DAY, -90, GETDATE()), DATEADD(DAY, 60, GETDATE()), 1),
('WEEKEND20', 'PERCENTAGE', 20, N'Giảm 20% cuối tuần, đơn từ 300k', 50, 0, 300000, DATEADD(DAY, -30, GETDATE()), DATEADD(DAY, 90, GETDATE()), 1),
('COMBO50K', 'FIXED_AMOUNT', 50000, N'Giảm 50k khi mua combo', 150, 0, 250000, DATEADD(DAY, -45, GETDATE()), DATEADD(DAY, 45, GETDATE()), 1),
('HAPPYHOUR', 'PERCENTAGE', 15, N'Giảm 15% giờ vàng 14h-18h', 80, 0, 150000, DATEADD(DAY, -20, GETDATE()), DATEADD(DAY, 40, GETDATE()), 1),
('VIP100K', 'FIXED_AMOUNT', 100000, N'Giảm 100k cho đơn từ 500k', 30, 0, 500000, DATEADD(DAY, -15, GETDATE()), DATEADD(DAY, 75, GETDATE()), 1),
('TAKEAWAY10', 'PERCENTAGE', 10, N'Giảm 10% đơn mang về', 200, 0, 100000, DATEADD(DAY, -50, GETDATE()), DATEADD(DAY, 50, GETDATE()), 1),
('FIRST50K', 'FIXED_AMOUNT', 50000, N'Giảm 50k đơn đầu tiên', 100, 0, 150000, DATEADD(DAY, -70, GETDATE()), DATEADD(DAY, 30, GETDATE()), 1),
('MEMBER15', 'PERCENTAGE', 15, N'Giảm 15% cho thành viên', 120, 0, 200000, DATEADD(DAY, -40, GETDATE()), DATEADD(DAY, 60, GETDATE()), 1),
('FLASH30', 'PERCENTAGE', 30, N'Flash sale giảm 30%', 20, 0, 400000, DATEADD(DAY, -10, GETDATE()), DATEADD(DAY, 20, GETDATE()), 1);

PRINT 'Generated 10 Vouchers';

-- ============================================
-- BƯỚC 5: TẠO 120 SESSIONS (cho DINE_IN orders)
-- ============================================

PRINT 'Step 5: Generating 120 Sessions for DINE_IN orders...';

DECLARE @sessionCount INT = 0;
DECLARE @sessionDate DATETIME;
DECLARE @tableId INT;

WHILE @sessionCount < 120
BEGIN
    -- Random date trong 67 ngày qua
    SET @sessionDate = DATEADD(DAY, -FLOOR(RAND() * 67), GETDATE());

    -- Random giờ trong ngày (6h-22h)
    SET @sessionDate = DATEADD(MINUTE, FLOOR(RAND() * 960), DATEADD(HOUR, 6, CAST(CAST(@sessionDate AS DATE) AS DATETIME)));

    -- Random table (1-15)
    SET @tableId = FLOOR(RAND() * 15) + 1;

    INSERT INTO [Session] (table_id, is_closed, created_at, closed_at)
    VALUES (
        @tableId,
        1, -- All sessions closed (old data)
        @sessionDate,
        DATEADD(HOUR, FLOOR(RAND() * 3) + 1, @sessionDate) -- Closed after 1-4 hours
    );

    SET @sessionCount = @sessionCount + 1;
END

PRINT 'Generated 120 Sessions';

-- ============================================
-- BƯỚC 6: TẠO 200 ORDERS (Phần quan trọng nhất)
-- ============================================

PRINT 'Step 6: Generating 200 Orders (3 orders/day for 67 days)...';

DECLARE @orderCount INT = 0;
DECLARE @currentDate DATE = DATEADD(DAY, -67, CAST(GETDATE() AS DATE));
DECLARE @endDate DATE = CAST(GETDATE() AS DATE);
DECLARE @ordersPerDay INT;
DECLARE @dayOfWeek INT;
DECLARE @j INT;

-- IDs cho random selection
DECLARE @staffId INT;
DECLARE @sessionId BIGINT;
DECLARE @memberId BIGINT;
DECLARE @voucherId BIGINT;
DECLARE @orderType VARCHAR(20);
DECLARE @orderStatus VARCHAR(20);
DECLARE @paymentStatus VARCHAR(20);
DECLARE @paymentMethod VARCHAR(20);
DECLARE @createdAt DATETIME;
DECLARE @totalPrice FLOAT;
DECLARE @taxRate FLOAT = 0.08;

WHILE @currentDate <= @endDate AND @orderCount < 200
BEGIN
    -- Determine orders for this day based on weekday
    SET @dayOfWeek = DATEPART(WEEKDAY, @currentDate);

    IF @dayOfWeek IN (6, 7, 1) -- Fri, Sat, Sun
        SET @ordersPerDay = FLOOR(RAND() * 2) + 4; -- 4-5 orders
    ELSE
        SET @ordersPerDay = FLOOR(RAND() * 2) + 2; -- 2-3 orders

    -- Limit to not exceed 200 total
    IF @orderCount + @ordersPerDay > 200
        SET @ordersPerDay = 200 - @orderCount;

    -- Generate orders for this day
    SET @j = 0;
    WHILE @j < @ordersPerDay
    BEGIN
        -- Random time distribution: 20% morning, 30% afternoon, 50% evening
        DECLARE @rand FLOAT = RAND();
        IF @rand < 0.2
            SET @createdAt = DATEADD(MINUTE, FLOOR(RAND() * 480), DATEADD(HOUR, 6, CAST(@currentDate AS DATETIME))); -- 6h-14h
        ELSE IF @rand < 0.5
            SET @createdAt = DATEADD(MINUTE, FLOOR(RAND() * 240), DATEADD(HOUR, 14, CAST(@currentDate AS DATETIME))); -- 14h-18h
        ELSE
            SET @createdAt = DATEADD(MINUTE, FLOOR(RAND() * 240), DATEADD(HOUR, 18, CAST(@currentDate AS DATETIME))); -- 18h-22h

        -- Order Type: 60% DINE_IN, 40% TAKE_AWAY
        IF RAND() < 0.6
            SET @orderType = 'DINE_IN';
        ELSE
            SET @orderType = 'TAKE_AWAY';

        -- Staff ID: Random cashier (IDs 1-10)
        IF @orderType = 'DINE_IN' OR RAND() < 0.7 -- DINE_IN always has staff, TAKE_AWAY 70%
            SET @staffId = FLOOR(RAND() * 10) + 1;
        ELSE
            SET @staffId = NULL;

        -- Session ID: Only for DINE_IN
        IF @orderType = 'DINE_IN'
        BEGIN
            -- Get a random session from the 120 we created
            SELECT TOP 1 @sessionId = id
            FROM [Session]
            ORDER BY NEWID();
        END
        ELSE
            SET @sessionId = NULL;

        -- Member ID: 50% for DINE_IN, 30% for TAKE_AWAY
        IF (@orderType = 'DINE_IN' AND RAND() < 0.5) OR (@orderType = 'TAKE_AWAY' AND RAND() < 0.3)
            SET @memberId = FLOOR(RAND() * 100) + 1;
        ELSE
            SET @memberId = NULL;

        -- Voucher ID: 30% for DINE_IN, 20% for TAKE_AWAY
        IF (@orderType = 'DINE_IN' AND RAND() < 0.3) OR (@orderType = 'TAKE_AWAY' AND RAND() < 0.2)
            SET @voucherId = FLOOR(RAND() * 10) + 1;
        ELSE
            SET @voucherId = NULL;

        -- Order Status: 85% COMPLETED, 10% CANCELLED, 5% PREPARING (recent orders)
        DECLARE @statusRand FLOAT = RAND();
        IF @statusRand < 0.85
            SET @orderStatus = 'COMPLETED';
        ELSE IF @statusRand < 0.95
            SET @orderStatus = 'CANCELLED';
        ELSE
            SET @orderStatus = 'PREPARING';

        -- Payment Status: COMPLETED → PAID, CANCELLED → random, PREPARING → UNPAID/PENDING
        IF @orderStatus = 'COMPLETED'
            SET @paymentStatus = 'PAID';
        ELSE IF @orderStatus = 'CANCELLED'
        BEGIN
            IF RAND() < 0.5
                SET @paymentStatus = 'UNPAID';
            ELSE
                SET @paymentStatus = 'PAID';
        END
        ELSE -- PREPARING
        BEGIN
            IF RAND() < 0.6
                SET @paymentStatus = 'UNPAID';
            ELSE
                SET @paymentStatus = 'PENDING';
        END

        -- Payment Method: 40% CASH, 30% CREDIT_CARD, 30% QR_BANKING
        DECLARE @pmRand FLOAT = RAND();
        IF @pmRand < 0.4
            SET @paymentMethod = 'CASH';
        ELSE IF @pmRand < 0.7
            SET @paymentMethod = 'CREDIT_CARD';
        ELSE
            SET @paymentMethod = 'QR_BANKING';

        -- Total Price: Realistic distribution
        -- 30% small (50k-150k), 40% medium (150k-300k), 20% large (300k-500k), 10% very large (500k-800k)
        DECLARE @priceRand FLOAT = RAND();
        IF @priceRand < 0.3
            SET @totalPrice = FLOOR(RAND() * 100000) + 50000; -- 50k-150k
        ELSE IF @priceRand < 0.7
            SET @totalPrice = FLOOR(RAND() * 150000) + 150000; -- 150k-300k
        ELSE IF @priceRand < 0.9
            SET @totalPrice = FLOOR(RAND() * 200000) + 300000; -- 300k-500k
        ELSE
            SET @totalPrice = FLOOR(RAND() * 300000) + 500000; -- 500k-800k

        -- Round to nearest 1000
        SET @totalPrice = ROUND(@totalPrice / 1000, 0) * 1000;

        -- Insert Order
        INSERT INTO customer_order (
            staff_id, session_id, voucher_id, member_id,
            note, total_price, created_at, updated_at,
            order_status, order_type, payment_method, payment_status, tax_rate
        ) VALUES (
            @staffId, @sessionId, @voucherId, @memberId,
            N'', @totalPrice, @createdAt, @createdAt,
            @orderStatus, @orderType, @paymentMethod, @paymentStatus, @taxRate
        );

        SET @j = @j + 1;
        SET @orderCount = @orderCount + 1;
    END

    SET @currentDate = DATEADD(DAY, 1, @currentDate);
END

PRINT 'Generated ' + CAST(@orderCount AS VARCHAR(10)) + ' Orders';

-- ============================================
-- BƯỚC 7: TẠO ORDER ITEMS (2-3 items per order)
-- ============================================

PRINT 'Step 7: Generating Order Items...';

DECLARE @orderId BIGINT;
DECLARE @orderTotalPrice FLOAT;
DECLARE @itemsCount INT;
DECLARE @productSizeId BIGINT;
DECLARE @unitPrice FLOAT;
DECLARE @quantity INT;
DECLARE @itemTotalPrice FLOAT;
DECLARE @orderItemStatus VARCHAR(20);
DECLARE @orderItemType VARCHAR(20);
DECLARE @accumulatedPrice FLOAT;
DECLARE @k INT;

DECLARE order_cursor CURSOR FOR
    SELECT id, total_price, order_status, order_type FROM customer_order;

OPEN order_cursor;
FETCH NEXT FROM order_cursor INTO @orderId, @orderTotalPrice, @orderStatus, @orderType;

WHILE @@FETCH_STATUS = 0
BEGIN
    -- 2-3 items per order
    SET @itemsCount = FLOOR(RAND() * 2) + 2;
    SET @accumulatedPrice = 0;
    SET @k = 0;

    -- Map order status to item status
    IF @orderStatus = 'COMPLETED'
        SET @orderItemStatus = 'SERVED';
    ELSE IF @orderStatus = 'CANCELLED'
        SET @orderItemStatus = 'CANCELLED';
    ELSE
        SET @orderItemStatus = 'PREPARING';

    SET @orderItemType = @orderType;

    WHILE @k < @itemsCount
    BEGIN
        -- Random ProductSize (assume we have 30+ product sizes)
        SET @productSizeId = FLOOR(RAND() * 30) + 1;

        -- Get base price from ProductSize table
        SELECT @unitPrice = base_price
        FROM [Product_Size]
        WHERE id = @productSizeId;

        -- If not found, use random price
        IF @unitPrice IS NULL
            SET @unitPrice = FLOOR(RAND() * 100000) + 50000;

        -- Quantity: 1-3
        SET @quantity = FLOOR(RAND() * 3) + 1;

        -- Calculate item total (before tax)
        IF @k < @itemsCount - 1
        BEGIN
            -- For all items except last, use random proportion
            SET @itemTotalPrice = @unitPrice * @quantity;
            SET @accumulatedPrice = @accumulatedPrice + @itemTotalPrice;
        END
        ELSE
        BEGIN
            -- Last item: adjust to match order total (accounting for tax)
            SET @itemTotalPrice = (@orderTotalPrice / (1 + @taxRate)) - @accumulatedPrice;
            -- Recalculate quantity to make sense
            IF @itemTotalPrice > 0
                SET @quantity = CAST(@itemTotalPrice / @unitPrice AS INT);
            ELSE
                SET @quantity = 1;
        END

        INSERT INTO [Order_Item] (
            order_id, product_size_id, unit_price, quantity,
            note, order_item_status, order_item_type, total_price
        ) VALUES (
            @orderId, @productSizeId, @unitPrice, @quantity,
            N'', @orderItemStatus, @orderItemType, @itemTotalPrice
        );

        SET @k = @k + 1;
    END

    FETCH NEXT FROM order_cursor INTO @orderId, @orderTotalPrice, @orderStatus, @orderType;
END

CLOSE order_cursor;
DEALLOCATE order_cursor;

PRINT 'Generated Order Items';

-- ============================================
-- BƯỚC 8: CẬP NHẬT STATISTICS
-- ============================================

PRINT 'Step 8: Updating Statistics...';

-- Update Voucher times_used
UPDATE v
SET v.times_used = (
    SELECT COUNT(*)
    FROM customer_order o
    WHERE o.voucher_id = v.id
)
FROM [Voucher] v;

-- Update Membership points (1 point per 10,000 VND)
UPDATE m
SET m.points = m.points + (
    SELECT ISNULL(SUM(FLOOR(o.total_price / 10000)), 0)
    FROM customer_order o
    WHERE o.member_id = m.id
)
FROM [Membership] m;

PRINT 'Updated Statistics';

-- ============================================
-- VERIFICATION QUERIES
-- ============================================

PRINT '';
PRINT '========================================';
PRINT 'VERIFICATION RESULTS';
PRINT '========================================';

SELECT 'Total Orders' AS Metric, COUNT(*) AS Count FROM customer_order;
SELECT 'Total Order Items' AS Metric, COUNT(*) AS Count FROM [Order_Item];
SELECT 'Orders by Status' AS Metric, order_status AS Value, COUNT(*) AS Count FROM customer_order GROUP BY order_status;
SELECT 'Orders by Type' AS Metric, order_type AS Value, COUNT(*) AS Count FROM customer_order GROUP BY order_type;
SELECT 'Orders by Payment Status' AS Metric, payment_status AS Value, COUNT(*) AS Count FROM customer_order GROUP BY payment_status;
SELECT 'Orders by Payment Method' AS Metric, payment_method AS Value, COUNT(*) AS Count FROM customer_order GROUP BY payment_method;
SELECT 'Revenue Today (Test)' AS Metric, SUM(total_price) AS Amount
FROM customer_order
WHERE order_status = 'COMPLETED' AND payment_status = 'PAID'
  AND CAST(created_at AS DATE) = CAST(GETDATE() AS DATE);

SELECT 'Orders per Day (Last 10 days)' AS Metric;
SELECT
    CAST(created_at AS DATE) AS OrderDate,
    COUNT(*) AS OrderCount,
    SUM(CASE WHEN order_status = 'COMPLETED' AND payment_status = 'PAID' THEN total_price ELSE 0 END) AS Revenue
FROM customer_order
WHERE created_at >= DATEADD(DAY, -10, GETDATE())
GROUP BY CAST(created_at AS DATE)
ORDER BY CAST(created_at AS DATE) DESC;

PRINT '';
PRINT '========================================';
PRINT 'SCRIPT COMPLETED SUCCESSFULLY!';
PRINT '========================================';
PRINT '200 orders generated with 3 orders/day distribution';
PRINT 'Check your analytics dashboard to see the results!';
PRINT '========================================';

GO
