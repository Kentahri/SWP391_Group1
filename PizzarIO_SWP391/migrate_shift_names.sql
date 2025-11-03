-- ========================================
-- Migration Script: Update shift_name values
-- Purpose: Convert Vietnamese diacritics to ASCII
-- Date: 2025-11-03
-- ========================================

USE swp_test;  -- Change to your database name if different
GO

PRINT '========================================';
PRINT 'Step 1: Check current data';
PRINT '========================================';
SELECT shift_name, COUNT(*) as count
FROM [Shift]
GROUP BY shift_name;
GO

PRINT '';
PRINT '========================================';
PRINT 'Step 2: Find and drop old CHECK constraint';
PRINT '========================================';

-- Find the constraint name
DECLARE @ConstraintName NVARCHAR(200);
DECLARE @SQL NVARCHAR(500);

SELECT @ConstraintName = name
FROM sys.check_constraints
WHERE parent_object_id = OBJECT_ID('Shift')
  AND COL_NAME(parent_object_id, parent_column_id) = 'shift_name';

IF @ConstraintName IS NOT NULL
BEGIN
    PRINT 'Found constraint: ' + @ConstraintName;
    SET @SQL = 'ALTER TABLE [Shift] DROP CONSTRAINT [' + @ConstraintName + ']';
    PRINT 'Executing: ' + @SQL;
    EXEC sp_executesql @SQL;
    PRINT 'Constraint dropped successfully.';
END
ELSE
BEGIN
    PRINT 'No CHECK constraint found on shift_name column.';
END
GO

PRINT '';
PRINT '========================================';
PRINT 'Step 3: Update shift names to remove diacritics';
PRINT '========================================';

UPDATE [Shift]
SET shift_name = 'SANG'
WHERE shift_name = N'SÁNG';
PRINT 'Updated SÁNG → SANG: ' + CAST(@@ROWCOUNT AS VARCHAR) + ' rows';

UPDATE [Shift]
SET shift_name = 'CHIEU'
WHERE shift_name = N'CHIỀU';
PRINT 'Updated CHIỀU → CHIEU: ' + CAST(@@ROWCOUNT AS VARCHAR) + ' rows';

UPDATE [Shift]
SET shift_name = 'TOI'
WHERE shift_name = N'TỐI';
PRINT 'Updated TỐI → TOI: ' + CAST(@@ROWCOUNT AS VARCHAR) + ' rows';
GO

PRINT '';
PRINT '========================================';
PRINT 'Step 4: Create new CHECK constraint (without diacritics)';
PRINT '========================================';

ALTER TABLE [Shift]
ADD CONSTRAINT CK_Shift_ShiftName
CHECK (shift_name IN ('SANG', 'CHIEU', 'TOI'));
PRINT 'New CHECK constraint created successfully.';
GO

PRINT '';
PRINT '========================================';
PRINT 'Step 5: Verify migration results';
PRINT '========================================';
SELECT shift_name, COUNT(*) as count
FROM [Shift]
GROUP BY shift_name;
GO

PRINT '';
PRINT '========================================';
PRINT 'Step 6: Display all shifts after migration';
PRINT '========================================';
SELECT id, shift_name, start_time, end_time, salary_per_shift
FROM [Shift]
ORDER BY id;
GO

PRINT '';
PRINT '========================================';
PRINT '=== Migration completed successfully ===';
PRINT '========================================';
GO
