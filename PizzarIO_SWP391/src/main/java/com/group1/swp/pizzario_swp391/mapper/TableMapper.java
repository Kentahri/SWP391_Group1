package com.group1.swp.pizzario_swp391.mapper;


import com.group1.swp.pizzario_swp391.dto.table.TableDTO;
import com.group1.swp.pizzario_swp391.entity.DiningTable;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TableMapper {
    DiningTable toDiningTable(TableDTO tableDTO);
    void updateDiningTable(@MappingTarget DiningTable diningTable, TableDTO tableDTO);
}
