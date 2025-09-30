package com.group1.swp.pizzario_swp391.dto.table;

import com.group1.swp.pizzario_swp391.entity.DiningTable.TableCondition;
import com.group1.swp.pizzario_swp391.entity.DiningTable.TableStatus;
import com.group1.swp.pizzario_swp391.entity.DiningTable.TableType;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TableCreateDTO {
    @NotNull(message = "Loại bàn không được để trống")
    TableType tableType;

    TableStatus tableStatus = TableStatus.AVAILABLE; // Mặc định là available

    TableCondition tableCondition = TableCondition.GOOD; // Mặc định là good
}
