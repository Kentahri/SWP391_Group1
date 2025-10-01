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
public class TableUpdateDTO {
    @NotNull(message = "Loại bàn không được để trống")
    TableType tableType;

    @NotNull(message = "Trạng thái bàn không được để trống")
    TableStatus tableStatus;

    @NotNull(message = "Tình trạng bàn không được để trống")
    TableCondition tableCondition;
}
