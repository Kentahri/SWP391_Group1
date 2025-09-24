package com.group1.swp.pizzario_swp391.mapper;

import java.util.List;

import com.group1.swp.pizzario_swp391.dto.VoucherDTO;
import com.group1.swp.pizzario_swp391.entity.Voucher;

/**
 * Interface defining the contract for Voucher mapping operations
 * This follows the Interface Segregation Principle
 */
public interface VoucherMapperInterface {
    
    /**
     * Convert Voucher Entity to VoucherDTO
     * @param voucher Entity object
     * @return VoucherDTO object
     */
    VoucherDTO toDTO(Voucher voucher);
    
    /**
     * Convert VoucherDTO to Voucher Entity
     * @param dto VoucherDTO object
     * @return Voucher Entity object
     */
    Voucher toEntity(VoucherDTO dto);
    
    /**
     * Convert list of Voucher entities to list of DTOs
     * @param vouchers List of Voucher entities
     * @return List of VoucherDTO objects
     */
    List<VoucherDTO> toDTOList(List<Voucher> vouchers);
    
    /**
     * Convert list of DTOs to list of Voucher entities
     * @param dtos List of VoucherDTO objects
     * @return List of Voucher entities
     */
    List<Voucher> toEntityList(List<VoucherDTO> dtos);
    
    /**
     * Update existing Voucher Entity with VoucherDTO data
     * @param existingVoucher Existing voucher entity
     * @param dto VoucherDTO with new data
     * @return Updated voucher entity
     */
    Voucher updateEntity(Voucher existingVoucher, VoucherDTO dto);
}
