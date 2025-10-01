package com.group1.swp.pizzario_swp391.dto.voucher;

import java.time.LocalDateTime;

import com.group1.swp.pizzario_swp391.entity.Voucher.VoucherType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VoucherUpdateDTO {
    @NotBlank(message = "Mã voucher không được để trống")
    @Size(max = 20, message = "Mã voucher không được vượt quá 20 ký tự")
    String code;

    @NotNull(message = "Loại voucher không được để trống")
    VoucherType type;

    @NotNull(message = "Giá trị voucher không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá trị voucher phải lớn hơn 0")
    Double value;

    @Size(max = 500, message = "Mô tả không được vượt quá 500 ký tự")
    String description;

    @NotNull(message = "Số lần sử dụng tối đa không được để trống")
    @Min(value = 1, message = "Số lần sử dụng tối đa phải ít nhất là 1")
    Integer maxUses;

    @NotNull(message = "Số tiền đơn hàng tối thiểu không được để trống")
    @DecimalMin(value = "0.0", message = "Số tiền đơn hàng tối thiểu không được âm")
    Double minOrderAmount;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    LocalDateTime validFrom;

    @NotNull(message = "Ngày kết thúc không được để trống")
    LocalDateTime validTo;

    boolean isActive;
}
