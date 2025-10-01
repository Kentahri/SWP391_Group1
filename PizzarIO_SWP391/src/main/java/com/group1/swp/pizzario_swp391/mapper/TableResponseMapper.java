package com.group1.swp.pizzario_swp391.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.group1.swp.pizzario_swp391.dto.table.TableCreateDTO;
import com.group1.swp.pizzario_swp391.dto.table.TableResponseDTO;
import com.group1.swp.pizzario_swp391.dto.table.TableUpdateDTO;
import com.group1.swp.pizzario_swp391.entity.DiningTable;

@Mapper(componentModel = "spring")
public interface TableResponseMapper {
    
    // Convert Entity to Response DTO
    TableResponseDTO toResponseDTO(DiningTable table);
    
    // Convert Create DTO to Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sessionList", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    DiningTable toEntity(TableCreateDTO createDTO);
    
    // Update Entity from Update DTO
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sessionList", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget DiningTable table, TableUpdateDTO updateDTO);
}
