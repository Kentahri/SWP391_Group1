package com.group1.swp.pizzario_swp391.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.group1.swp.pizzario_swp391.dto.staff.StaffCreateDTO;
import com.group1.swp.pizzario_swp391.dto.staff.StaffResponseDTO;
import com.group1.swp.pizzario_swp391.dto.staff.StaffUpdateDTO;
import com.group1.swp.pizzario_swp391.entity.Staff;

@Mapper(componentModel = "spring")
public interface StaffResponseMapper {
    
    // Convert Entity to Response DTO
    StaffResponseDTO toResponseDTO(Staff staff);
    
    // Convert Create DTO to Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "otpMails", ignore = true)
    @Mapping(target = "shifts", ignore = true)
    @Mapping(target = "orders", ignore = true)
    Staff toEntity(StaffCreateDTO createDTO);
    
    // Update Entity from Update DTO
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true) // Không update password qua update DTO
    @Mapping(target = "otpMails", ignore = true)
    @Mapping(target = "shifts", ignore = true)
    @Mapping(target = "orders", ignore = true)
    void updateEntity(@MappingTarget Staff staff, StaffUpdateDTO updateDTO);
}
