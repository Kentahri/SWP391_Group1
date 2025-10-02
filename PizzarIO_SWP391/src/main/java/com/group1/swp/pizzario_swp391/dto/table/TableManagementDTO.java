package com.group1.swp.pizzario_swp391.dto.table;

import com.group1.swp.pizzario_swp391.entity.DiningTable.TableCondition;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * DTO cho Manager khi CẬP NHẬT bàn
 * Manager chỉ được cập nhật tableCondition và capacity
 * TableStatus do Cashier quản lý
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TableManagementDTO {

    @NotNull(message = "Tình trạng bàn không được để trống")
    TableCondition tableCondition;

    @NotNull(message = "Sức chứa không được để trống")
    @Min(value = 1, message = "Sức chứa tối thiểu là 1 người")
    int capacity;
}
