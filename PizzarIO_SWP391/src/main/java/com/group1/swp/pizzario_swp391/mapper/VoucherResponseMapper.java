package com.group1.swp.pizzario_swp391.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.group1.swp.pizzario_swp391.dto.voucher.VoucherCreateDTO;
import com.group1.swp.pizzario_swp391.dto.voucher.VoucherResponseDTO;
import com.group1.swp.pizzario_swp391.dto.voucher.VoucherUpdateDTO;
import com.group1.swp.pizzario_swp391.entity.Voucher;

@Mapper(componentModel = "spring")
public interface VoucherResponseMapper {
    
    // Convert Entity to Response DTO
    VoucherResponseDTO toResponseDTO(Voucher voucher);
    
    // Convert Create DTO to Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orders", ignore = true)
    @Mapping(target = "timesUsed", constant = "0") // Mặc định là 0 khi tạo mới
    Voucher toEntity(VoucherCreateDTO createDTO);
    
    // Update Entity from Update DTO
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orders", ignore = true)
    @Mapping(target = "timesUsed", ignore = true) // Không update timesUsed qua form
    void updateEntity(@MappingTarget Voucher voucher, VoucherUpdateDTO updateDTO);
}
