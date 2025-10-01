package com.group1.swp.pizzario_swp391.mapper;


import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import com.group1.swp.pizzario_swp391.dto.table.TableCreateDTO;
import com.group1.swp.pizzario_swp391.dto.table.TableDTO;
import com.group1.swp.pizzario_swp391.dto.table.TableForCashierDTO;
import com.group1.swp.pizzario_swp391.dto.table.TableManagementDTO;
import com.group1.swp.pizzario_swp391.entity.DiningTable;

@Mapper(componentModel = "spring")
public interface TableMapper {
    // Base mapper
    DiningTable toDiningTable(TableDTO tableDTO);
    void updateDiningTable(@MappingTarget DiningTable diningTable, TableDTO tableDTO);
    TableDTO toTableDTO(DiningTable diningTable);
    List<TableDTO> toTableDTOs(List<DiningTable> diningTables);
    
    // Create mapper (cho manager tạo bàn - chỉ có capacity)
    DiningTable toDiningTable(TableCreateDTO tableCreateDTO);
    
    // Update mapper (cho manager cập nhật - capacity + condition)
    void updateDiningTable(@MappingTarget DiningTable diningTable, TableManagementDTO tableManagementDTO);
    
    // Cashier mapper (chỉ đọc)
    TableForCashierDTO toTableForCashierDTO(DiningTable diningTable);
    List<TableForCashierDTO> toTableForCashierDTOs(List<DiningTable> diningTables);
}
