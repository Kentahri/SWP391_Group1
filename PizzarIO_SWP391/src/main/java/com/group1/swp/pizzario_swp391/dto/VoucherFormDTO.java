package com.group1.swp.pizzario_swp391.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.group1.swp.pizzario_swp391.entity.Voucher;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO specifically for form handling with String datetime fields
 * This makes it easier to work with HTML datetime-local inputs
 */
public class VoucherFormDTO {
    
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
    
    @NotBlank(message = "Valid from date is required")
    private String validFrom; // String for HTML datetime-local input
    
    @NotBlank(message = "Valid to date is required")
    private String validTo; // String for HTML datetime-local input
    
    private Boolean isActive = true;
    
    // Constructors
    public VoucherFormDTO() {}
    
    public VoucherFormDTO(String code, Voucher.VoucherType type, Double value, String description, 
                         Integer maxUses, Double minOrderAmount, String validFrom, 
                         String validTo, Boolean isActive) {
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
    
    public String getValidFrom() {
        return validFrom;
    }
    
    public void setValidFrom(String validFrom) {
        this.validFrom = validFrom;
    }
    
    public String getValidTo() {
        return validTo;
    }
    
    public void setValidTo(String validTo) {
        this.validTo = validTo;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    // Helper methods to convert to/from LocalDateTime
    public LocalDateTime getValidFromAsLocalDateTime() {
        if (validFrom == null || validFrom.trim().isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(validFrom);
    }
    
    public void setValidFromFromLocalDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            this.validFrom = null;
        } else {
            this.validFrom = dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
    }
    
    public LocalDateTime getValidToAsLocalDateTime() {
        if (validTo == null || validTo.trim().isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(validTo);
    }
    
    public void setValidToFromLocalDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            this.validTo = null;
        } else {
            this.validTo = dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
    }
}
