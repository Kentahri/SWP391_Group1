package com.group1.swp.pizzario_swp391.mapper;

import com.group1.swp.pizzario_swp391.dto.VoucherDTO;
import com.group1.swp.pizzario_swp391.dto.VoucherFormDTO;
import com.group1.swp.pizzario_swp391.entity.Voucher;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class VoucherMapper implements VoucherMapperInterface {
    
    /**
     * Convert Voucher Entity to VoucherDTO
     * @param voucher Entity object
     * @return VoucherDTO object
     */
    public VoucherDTO toDTO(Voucher voucher) {
        if (voucher == null) {
            return null;
        }
        
        VoucherDTO dto = new VoucherDTO();
        dto.setCode(voucher.getCode());
        dto.setType(voucher.getType());
        dto.setValue(voucher.getValue());
        dto.setDescription(voucher.getDescription());
        dto.setMaxUses(voucher.getMaxUses());
        dto.setMinOrderAmount(voucher.getMinOrderAmount());
        dto.setValidFrom(voucher.getValidFrom());
        dto.setValidTo(voucher.getValidTo());
        dto.setIsActive(voucher.isActive());
        
        return dto;
    }
    
    /**
     * Convert VoucherDTO to Voucher Entity
     * @param dto VoucherDTO object
     * @return Voucher Entity object
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
        voucher.setTimesUsed(0); // Initialize times used to 0 for new vouchers
        voucher.setMinOrderAmount(dto.getMinOrderAmount());
        voucher.setValidFrom(dto.getValidFrom());
        voucher.setValidTo(dto.getValidTo());
        voucher.setActive(dto.getIsActive());
        
        return voucher;
    }
    
    /**
     * Update existing Voucher Entity with VoucherDTO data
     * @param existingVoucher Existing voucher entity
     * @param dto VoucherDTO with new data
     * @return Updated voucher entity
     */
    public Voucher updateEntity(Voucher existingVoucher, VoucherDTO dto) {
        if (existingVoucher == null || dto == null) {
            return existingVoucher;
        }
        
        // Update all fields except ID and timesUsed (preserve existing usage count)
        existingVoucher.setCode(dto.getCode());
        existingVoucher.setType(dto.getType());
        existingVoucher.setValue(dto.getValue());
        existingVoucher.setDescription(dto.getDescription());
        existingVoucher.setMaxUses(dto.getMaxUses());
        // Note: timesUsed is not updated to preserve usage history
        existingVoucher.setMinOrderAmount(dto.getMinOrderAmount());
        existingVoucher.setValidFrom(dto.getValidFrom());
        existingVoucher.setValidTo(dto.getValidTo());
        existingVoucher.setActive(dto.getIsActive());
        
        return existingVoucher;
    }
    
    /**
     * Convert list of Voucher entities to list of DTOs
     */
    @Override
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
    @Override
    public List<Voucher> toEntityList(List<VoucherDTO> dtos) {
        if (dtos == null) {
            return null;
        }
        
        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Create VoucherDTO with default values for new voucher creation
     * @return VoucherDTO with default values
     */
    public VoucherDTO createDefaultDTO() {
        VoucherDTO dto = new VoucherDTO();
        dto.setIsActive(true); // Default to active
        dto.setMaxUses(1); // Default max uses
        dto.setMinOrderAmount(0.0); // Default min order amount
        dto.setValue(0.0); // Default value
        return dto;
    }
    
    /**
     * Convert Voucher Entity to VoucherFormDTO (for form handling)
     * @param voucher Entity object
     * @return VoucherFormDTO object
     */
    public VoucherFormDTO toFormDTO(Voucher voucher) {
        if (voucher == null) {
            return null;
        }
        
        VoucherFormDTO formDTO = new VoucherFormDTO();
        formDTO.setCode(voucher.getCode());
        formDTO.setType(voucher.getType());
        formDTO.setValue(voucher.getValue());
        formDTO.setDescription(voucher.getDescription());
        formDTO.setMaxUses(voucher.getMaxUses());
        formDTO.setMinOrderAmount(voucher.getMinOrderAmount());
        formDTO.setIsActive(voucher.isActive());
        
        // Convert LocalDateTime to String for HTML datetime-local input
        if (voucher.getValidFrom() != null) {
            formDTO.setValidFromFromLocalDateTime(voucher.getValidFrom());
        }
        if (voucher.getValidTo() != null) {
            formDTO.setValidToFromLocalDateTime(voucher.getValidTo());
        }
        
        return formDTO;
    }
    
    /**
     * Convert VoucherFormDTO to Voucher Entity
     * @param formDTO VoucherFormDTO object
     * @return Voucher Entity object
     */
    public Voucher toEntityFromFormDTO(VoucherFormDTO formDTO) {
        if (formDTO == null) {
            return null;
        }
        
        Voucher voucher = new Voucher();
        voucher.setCode(formDTO.getCode());
        voucher.setType(formDTO.getType());
        voucher.setValue(formDTO.getValue());
        voucher.setDescription(formDTO.getDescription());
        voucher.setMaxUses(formDTO.getMaxUses());
        voucher.setTimesUsed(0); // Initialize for new vouchers
        voucher.setMinOrderAmount(formDTO.getMinOrderAmount());
        voucher.setActive(formDTO.getIsActive());
        
        // Convert String to LocalDateTime
        if (formDTO.getValidFrom() != null && !formDTO.getValidFrom().trim().isEmpty()) {
            voucher.setValidFrom(formDTO.getValidFromAsLocalDateTime());
        }
        if (formDTO.getValidTo() != null && !formDTO.getValidTo().trim().isEmpty()) {
            voucher.setValidTo(formDTO.getValidToAsLocalDateTime());
        }
        
        return voucher;
    }
    
    /**
     * Update existing Voucher Entity with VoucherFormDTO data
     * @param existingVoucher Existing voucher entity
     * @param formDTO VoucherFormDTO with new data
     * @return Updated voucher entity
     */
    public Voucher updateEntityFromFormDTO(Voucher existingVoucher, VoucherFormDTO formDTO) {
        if (existingVoucher == null || formDTO == null) {
            return existingVoucher;
        }
        
        // Update all fields except ID and timesUsed (preserve existing usage count)
        existingVoucher.setCode(formDTO.getCode());
        existingVoucher.setType(formDTO.getType());
        existingVoucher.setValue(formDTO.getValue());
        existingVoucher.setDescription(formDTO.getDescription());
        existingVoucher.setMaxUses(formDTO.getMaxUses());
        // Note: timesUsed is not updated to preserve usage history
        existingVoucher.setMinOrderAmount(formDTO.getMinOrderAmount());
        existingVoucher.setActive(formDTO.getIsActive());
        
        // Update datetime fields
        if (formDTO.getValidFrom() != null && !formDTO.getValidFrom().trim().isEmpty()) {
            existingVoucher.setValidFrom(formDTO.getValidFromAsLocalDateTime());
        }
        if (formDTO.getValidTo() != null && !formDTO.getValidTo().trim().isEmpty()) {
            existingVoucher.setValidTo(formDTO.getValidToAsLocalDateTime());
        }
        
        return existingVoucher;
    }
}
