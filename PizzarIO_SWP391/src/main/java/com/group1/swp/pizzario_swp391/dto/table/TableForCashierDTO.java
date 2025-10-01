package com.group1.swp.pizzario_swp391.dto.table;

import com.group1.swp.pizzario_swp391.entity.DiningTable.TableStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * DTO cho Cashier hiển thị danh sách bàn
 * Cashier chỉ được xem thông tin cơ bản và cập nhật trạng thái bàn
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TableForCashierDTO {
    
    int id;
    
    TableStatus tableStatus;
    
    int capacity;
}
