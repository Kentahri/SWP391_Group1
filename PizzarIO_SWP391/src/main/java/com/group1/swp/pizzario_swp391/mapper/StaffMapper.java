package com.group1.swp.pizzario_swp391.mapper;

import com.group1.swp.pizzario_swp391.dto.StaffDTO;
import com.group1.swp.pizzario_swp391.entity.Staff;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface StaffMapper {
    Staff toStaff(StaffDTO staffDTO);
    void updateStaff(@MappingTarget Staff staff,StaffDTO staffDTO);
}
