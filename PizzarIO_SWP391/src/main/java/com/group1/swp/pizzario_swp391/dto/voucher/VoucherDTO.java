package com.group1.swp.pizzario_swp391.dto.voucher;

import com.group1.swp.pizzario_swp391.entity.Voucher;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VoucherDTO {
    Long id; // Nullable - dùng để phân biệt Create (null) vs Update (có giá trị)

    @NotBlank(message = "Mã voucher không được để trống")
    @Size(max = 20, message = "Mã voucher không được vượt quá 20 ký tự")
    @Pattern(regexp = "^[A-Z0-9_-]+$", message = "Mã voucher chỉ được chứa chữ hoa, số, gạch ngang và gạch dưới")
    String code;

    @NotNull(message = "Loại voucher không được để trống")
    Voucher.VoucherType type;

    @NotNull(message = "Giá trị voucher không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá trị voucher phải lớn hơn 0")
    @DecimalMax(value = "1000000.0", message = "Giá trị voucher không được vượt quá 1,000,000")
    Double value;

    @Size(max = 500, message = "Mô tả không được vượt quá 500 ký tự")
    @NotBlank(message = "Mô tả không được để trống")
    String description;

    @NotNull(message = "Số lần sử dụng tối đa không được để trống")
    @Min(value = 1, message = "Số lần sử dụng tối đa phải ít nhất là 1")
    @Max(value = 10000, message = "Số lần sử dụng tối đa không được vượt quá 10,000")
    Integer maxUses;

    Integer timesUsed;

    @NotNull(message = "Số tiền đơn hàng tối thiểu không được để trống")
    @DecimalMin(value = "0.0", message = "Số tiền đơn hàng tối thiểu không được âm")
    @DecimalMax(value = "10000000.0", message = "Số tiền đơn hàng tối thiểu không được vượt quá 10,000,000")
    Double minOrderAmount;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    @NotNull(message = "Ngày bắt đầu không được để trống")
    LocalDateTime validFrom;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    @NotNull(message = "Ngày kết thúc không được để trống")
    LocalDateTime validTo;

    boolean active;

    @AssertTrue(message = "Ngày kết thúc phải sau ngày bắt đầu")
    public boolean isValidDateRange() {
        if (validFrom == null || validTo == null) {
            return true; // Let @NotNull handle null validation
        }
        return validTo.isAfter(validFrom);
    }

    @AssertTrue(message = "Giá trị voucher không hợp lệ")
    public boolean isValidValue() {
        if (type == null || value == null) {
            return true; // Let @NotNull handle null validation
        }
        if (type == Voucher.VoucherType.PERCENTAGE) {
            return value > 0 && value <= 100;
        }
        return value > 0;
    }
}
