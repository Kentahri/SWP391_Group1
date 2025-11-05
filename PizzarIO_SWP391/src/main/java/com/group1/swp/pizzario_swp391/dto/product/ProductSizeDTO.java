package com.group1.swp.pizzario_swp391.dto.product;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductSizeDTO {
    Long id;
    Long sizeId;
    String sizeName;
    double basePrice;
    double flashSalePrice;
    boolean onFlashSale;
    double currentPrice;
}
