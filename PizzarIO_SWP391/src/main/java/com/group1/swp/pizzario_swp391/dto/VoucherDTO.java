package com.group1.swp.pizzario_swp391.dto;

import com.group1.swp.pizzario_swp391.entity.Voucher;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VoucherDTO {
    String code;
    Voucher.VoucherType type;
    double value;
    String description;
    int maxUses;
    int timesUsed;
    double minOrderAmount;
    LocalDateTime validFrom;
    LocalDateTime validTo;
    boolean isActive;
}
