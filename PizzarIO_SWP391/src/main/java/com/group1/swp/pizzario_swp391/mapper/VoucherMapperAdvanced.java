package com.group1.swp.pizzario_swp391.mapper;

import com.group1.swp.pizzario_swp391.dto.VoucherDTO;
import com.group1.swp.pizzario_swp391.entity.Voucher;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Advanced VoucherMapper with additional utility methods
 * This demonstrates more sophisticated mapping patterns
 */
@Component
public class VoucherMapperAdvanced {
    
    /**
     * Convert Voucher Entity to VoucherDTO with null safety
     */
    public VoucherDTO toDTO(Voucher voucher) {
        if (voucher == null) {
            return null;
        }
        
        return VoucherDTO.builder()
                .code(voucher.getCode())
                .type(voucher.getType())
                .value(voucher.getValue())
                .description(voucher.getDescription())
                .maxUses(voucher.getMaxUses())
                .minOrderAmount(voucher.getMinOrderAmount())
                .validFrom(voucher.getValidFrom())
                .validTo(voucher.getValidTo())
                .isActive(voucher.isActive())
                .build();
    }
    
    /**
     * Convert VoucherDTO to Voucher Entity with null safety
     */
    public Voucher toEntity(VoucherDTO dto) {
        if (dto == null) {
            return null;
        }
        
        Voucher voucher = new Voucher();
        voucher.setCode(dto.getCode());
        voucher.setType(dto.getType());
        voucher.setValue(dto.getValue());
        voucher.setDescription(dto.getDescription());
        voucher.setMaxUses(dto.getMaxUses());
        voucher.setTimesUsed(0); // Initialize for new vouchers
        voucher.setMinOrderAmount(dto.getMinOrderAmount());
        voucher.setValidFrom(dto.getValidFrom());
        voucher.setValidTo(dto.getValidTo());
        voucher.setActive(dto.getIsActive());
        
        return voucher;
    }
    
    /**
     * Convert list of Voucher entities to list of DTOs
     */
    public List<VoucherDTO> toDTOList(List<Voucher> vouchers) {
        if (vouchers == null) {
            return null;
        }
        
        return vouchers.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Convert list of DTOs to list of Voucher entities
     */
    public List<Voucher> toEntityList(List<VoucherDTO> dtos) {
        if (dtos == null) {
            return null;
        }
        
        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Update existing entity with DTO data (preserves ID and usage count)
     */
    public void updateEntity(Voucher existingVoucher, VoucherDTO dto) {
        if (existingVoucher == null || dto == null) {
            return;
        }
        
        existingVoucher.setCode(dto.getCode());
        existingVoucher.setType(dto.getType());
        existingVoucher.setValue(dto.getValue());
        existingVoucher.setDescription(dto.getDescription());
        existingVoucher.setMaxUses(dto.getMaxUses());
        // Preserve timesUsed - don't update usage count
        existingVoucher.setMinOrderAmount(dto.getMinOrderAmount());
        existingVoucher.setValidFrom(dto.getValidFrom());
        existingVoucher.setValidTo(dto.getValidTo());
        existingVoucher.setActive(dto.getIsActive());
    }
    
    /**
     * Create a copy of voucher with updated fields
     */
    public Voucher copyWithUpdates(Voucher original, VoucherDTO updates) {
        if (original == null) {
            return toEntity(updates);
        }
        
        Voucher copy = new Voucher();
        copy.setId(original.getId()); // Preserve ID
        copy.setCode(updates.getCode() != null ? updates.getCode() : original.getCode());
        copy.setType(updates.getType() != null ? updates.getType() : original.getType());
        copy.setValue(updates.getValue() != null ? updates.getValue() : original.getValue());
        copy.setDescription(updates.getDescription() != null ? updates.getDescription() : original.getDescription());
        copy.setMaxUses(updates.getMaxUses() != null ? updates.getMaxUses() : original.getMaxUses());
        copy.setTimesUsed(original.getTimesUsed()); // Preserve usage count
        copy.setMinOrderAmount(updates.getMinOrderAmount() != null ? updates.getMinOrderAmount() : original.getMinOrderAmount());
        copy.setValidFrom(updates.getValidFrom() != null ? updates.getValidFrom() : original.getValidFrom());
        copy.setValidTo(updates.getValidTo() != null ? updates.getValidTo() : original.getValidTo());
        copy.setActive(updates.getIsActive() != null ? updates.getIsActive() : original.isActive());
        
        return copy;
    }
}
