package com.group1.swp.pizzario_swp391.dto.table;

import com.group1.swp.pizzario_swp391.entity.DiningTable;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TableDTO {

     DiningTable.TableType tableType;
     DiningTable.TableStatus tableStatus;
     DiningTable.TableCondition tableCondition;
     LocalDateTime createdAt;
     LocalDateTime updatedAt;
}
