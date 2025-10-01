package com.group1.swp.pizzario_swp391.dto.voucher;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.group1.swp.pizzario_swp391.entity.Voucher.VoucherType;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VoucherResponseDTO {
    Long id;
    String code;
    VoucherType type;
    double value;
    String description;
    int maxUses;
    int timesUsed;
    double minOrderAmount;
    LocalDateTime validFrom;
    LocalDateTime validTo;
    boolean isActive;
    
    // Formatted fields for display
    public String getValidFromFormatted() {
        return validFrom != null ? validFrom.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "";
    }
    
    public String getValidToFormatted() {
        return validTo != null ? validTo.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "";
    }
    
    public String getTypeText() {
        if (type == null) return "";
        switch (type) {
            case PERCENTAGE: return "Phần trăm";
            case FIXED_AMOUNT: return "Số tiền cố định";
            default: return type.toString();
        }
    }
    
    public String getValueFormatted() {
        if (type == VoucherType.PERCENTAGE) {
            return String.format("%.0f%%", value);
        } else {
            return String.format("%,.0f VND", value);
        }
    }
    
    public String getMinOrderAmountFormatted() {
        return String.format("%,.0f VND", minOrderAmount);
    }
    
    public String getStatusText() {
        return isActive ? "Hoạt động" : "Không hoạt động";
    }
    
    public boolean isExpired() {
        return validTo != null && LocalDateTime.now().isAfter(validTo);
    }
    
    public boolean isUsedUp() {
        return timesUsed >= maxUses;
    }
    
    public boolean isValid() {
        LocalDateTime now = LocalDateTime.now();
        return isActive && !isExpired() && !isUsedUp() 
                && validFrom != null && now.isAfter(validFrom);
    }
    
    public int getRemainingUses() {
        return Math.max(0, maxUses - timesUsed);
    }
}
