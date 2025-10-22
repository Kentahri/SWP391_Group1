package com.group1.swp.pizzario_swp391.mapper;

import com.group1.swp.pizzario_swp391.dto.ShiftDTO;
import com.group1.swp.pizzario_swp391.entity.Shift;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ShiftMapper {
    Shift toShift(ShiftDTO shiftDTO);

    void updateShift(@MappingTarget Shift shift, ShiftDTO shiftDTO);

    List<ShiftDTO> toShiftDTOs(List<Shift> shifts);

    ShiftDTO toShiftDTO(Shift shift);
    
}
