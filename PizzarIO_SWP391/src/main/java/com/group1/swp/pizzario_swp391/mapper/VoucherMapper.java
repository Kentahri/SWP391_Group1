package com.group1.swp.pizzario_swp391.mapper;


import com.group1.swp.pizzario_swp391.dto.voucher.VoucherDTO;
import com.group1.swp.pizzario_swp391.entity.Voucher;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface VoucherMapper {
    Voucher toVoucher(VoucherDTO voucherDTO);
    void updateVoucher(@MappingTarget Voucher voucher, VoucherDTO voucherDTO);
}
