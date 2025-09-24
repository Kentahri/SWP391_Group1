package com.group1.swp.pizzario_swp391.dto;

import java.time.LocalDateTime;

import com.group1.swp.pizzario_swp391.entity.Voucher;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class VoucherDTO {
    
    @NotBlank(message = "Voucher code is required")
    @Size(max = 50, message = "Voucher code must not exceed 50 characters")
    private String code;
    
    @NotNull(message = "Voucher type is required")
    private Voucher.VoucherType type;
    
    @NotNull(message = "Value is required")
    @DecimalMin(value = "0.01", message = "Value must be greater than 0")
    private Double value;
    
    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;
    
    @NotNull(message = "Max uses is required")
    @Min(value = 1, message = "Max uses must be at least 1")
    private Integer maxUses;
    
    @NotNull(message = "Min order amount is required")
    @DecimalMin(value = "0.0", message = "Min order amount cannot be negative")
    private Double minOrderAmount;
    
    @NotNull(message = "Valid from date is required")
    @Future(message = "Valid from date must be in the future")
    private LocalDateTime validFrom;
    
    @NotNull(message = "Valid to date is required")
    @Future(message = "Valid to date must be in the future")
    private LocalDateTime validTo;
    
    private Boolean isActive = true;
    
    // Constructors
    public VoucherDTO() {}
    
    public VoucherDTO(String code, Voucher.VoucherType type, Double value, String description, 
                     Integer maxUses, Double minOrderAmount, LocalDateTime validFrom, 
                     LocalDateTime validTo, Boolean isActive) {
        this.code = code;
        this.type = type;
        this.value = value;
        this.description = description;
        this.maxUses = maxUses;
        this.minOrderAmount = minOrderAmount;
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.isActive = isActive;
    }
    
    // Getters and Setters
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public Voucher.VoucherType getType() {
        return type;
    }
    
    public void setType(Voucher.VoucherType type) {
        this.type = type;
    }
    
    public Double getValue() {
        return value;
    }
    
    public void setValue(Double value) {
        this.value = value;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Integer getMaxUses() {
        return maxUses;
    }
    
    public void setMaxUses(Integer maxUses) {
        this.maxUses = maxUses;
    }
    
    public Double getMinOrderAmount() {
        return minOrderAmount;
    }
    
    public void setMinOrderAmount(Double minOrderAmount) {
        this.minOrderAmount = minOrderAmount;
    }
    
    public LocalDateTime getValidFrom() {
        return validFrom;
    }
    
    public void setValidFrom(LocalDateTime validFrom) {
        this.validFrom = validFrom;
    }
    
    public LocalDateTime getValidTo() {
        return validTo;
    }
    
    public void setValidTo(LocalDateTime validTo) {
        this.validTo = validTo;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    // Builder pattern for easier object creation
    public static VoucherDTOBuilder builder() {
        return new VoucherDTOBuilder();
    }
    
    public static class VoucherDTOBuilder {
        private String code;
        private Voucher.VoucherType type;
        private Double value;
        private String description;
        private Integer maxUses;
        private Double minOrderAmount;
        private LocalDateTime validFrom;
        private LocalDateTime validTo;
        private Boolean isActive;
        
        public VoucherDTOBuilder code(String code) {
            this.code = code;
            return this;
        }
        
        public VoucherDTOBuilder type(Voucher.VoucherType type) {
            this.type = type;
            return this;
        }
        
        public VoucherDTOBuilder value(Double value) {
            this.value = value;
            return this;
        }
        
        public VoucherDTOBuilder description(String description) {
            this.description = description;
            return this;
        }
        
        public VoucherDTOBuilder maxUses(Integer maxUses) {
            this.maxUses = maxUses;
            return this;
        }
        
        public VoucherDTOBuilder minOrderAmount(Double minOrderAmount) {
            this.minOrderAmount = minOrderAmount;
            return this;
        }
        
        public VoucherDTOBuilder validFrom(LocalDateTime validFrom) {
            this.validFrom = validFrom;
            return this;
        }
        
        public VoucherDTOBuilder validTo(LocalDateTime validTo) {
            this.validTo = validTo;
            return this;
        }
        
        public VoucherDTOBuilder isActive(Boolean isActive) {
            this.isActive = isActive;
            return this;
        }
        
        public VoucherDTO build() {
            VoucherDTO dto = new VoucherDTO();
            dto.setCode(this.code);
            dto.setType(this.type);
            dto.setValue(this.value);
            dto.setDescription(this.description);
            dto.setMaxUses(this.maxUses);
            dto.setMinOrderAmount(this.minOrderAmount);
            dto.setValidFrom(this.validFrom);
            dto.setValidTo(this.validTo);
            dto.setIsActive(this.isActive);
            return dto;
        }
    }
}
