package com.group1.swp.pizzario_swp391.dto.productsize;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSizeCreateDTO {

    private Long id;

    @NotNull(message = "Sản phẩm không được để trống")
    private Long productId;

    @NotNull(message = "Kích thước không được để trống")
    private Long sizeId;

    @Min(value = 0, message = "Giá cơ bản phải ≥ 0")
    @NotNull(message = "Giá cơ bản không được để trống")
    private Double basePrice;

    @DecimalMin(value = "0.0", inclusive = false, message = "Giá flash sale phải > 0")
    private Double flashSalePrice;

    private LocalDateTime flashSaleStart;
    private LocalDateTime flashSaleEnd;

    // === CUSTOM VALIDATION ===
    @AssertTrue(message = "Giá flash sale phải nhỏ hơn giá cơ bản")
    private boolean isFlashSalePriceValid() {
        if (flashSalePrice == null || basePrice == null) return true;
        return flashSalePrice < basePrice;
    }

    @AssertTrue(message = "Thời gian bắt đầu flash sale phải trước thời gian kết thúc")
    private boolean isFlashSaleTimeValid() {
        if (flashSaleStart == null || flashSaleEnd == null) return true;
        return flashSaleStart.isBefore(flashSaleEnd);
    }
    
//    @AssertTrue(message = "Thời gian bắt đầu flash sale phải trong tương lai")
//    private boolean isFlashSaleStartInFuture() {
//        if (flashSaleStart == null) return true;
//        return flashSaleStart.isAfter(LocalDateTime.now());
//    }
}