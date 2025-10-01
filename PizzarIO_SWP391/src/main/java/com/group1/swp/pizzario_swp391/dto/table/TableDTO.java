package com.group1.swp.pizzario_swp391.dto.table;

import com.group1.swp.pizzario_swp391.entity.DiningTable;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;

/**
 * Base DTO chứa tất cả các trường của Table trừ ID
 * Dùng chung cho các DTO khác kế thừa
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TableDTO {
     private int id;
     DiningTable.TableStatus tableStatus;
     DiningTable.TableCondition tableCondition;
     LocalDateTime createdAt;
     LocalDateTime updatedAt;
     int capacity;
}
