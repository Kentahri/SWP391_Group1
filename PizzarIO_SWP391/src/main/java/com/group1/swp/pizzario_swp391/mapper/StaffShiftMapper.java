package com.group1.swp.pizzario_swp391.mapper;

import com.group1.swp.pizzario_swp391.dto.staffshift.StaffShiftDTO;
import com.group1.swp.pizzario_swp391.entity.StaffShift;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

// removed unused import

@Mapper(componentModel = "spring")
public interface StaffShiftMapper {

    // Convert Entity to DTO
    @Mapping(target = "staffId", source = "staff.id")
    @Mapping(target = "shiftId", source = "shift.id")
    @Mapping(target = "workDate", source = "workDate")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "checkIn", source = "checkIn")
    @Mapping(target = "checkOut", source = "checkOut")
    @Mapping(target = "hourlyWage", source = "hourlyWage")
    StaffShiftDTO toStaffShiftDTO(StaffShift staffShift);

    // Convert DTO to Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "staff", ignore = true)
    @Mapping(target = "shift", ignore = true)
    @Mapping(target = "workDate", source = "workDate")
    StaffShift toStaffShift(StaffShiftDTO staffShiftDTO);

    // Update Entity from DTO
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "staff", ignore = true)
    @Mapping(target = "shift", ignore = true)
    @Mapping(target = "workDate", source = "workDate")
    void updateStaffShift(@MappingTarget StaffShift staffShift, StaffShiftDTO staffShiftDTO);

}
