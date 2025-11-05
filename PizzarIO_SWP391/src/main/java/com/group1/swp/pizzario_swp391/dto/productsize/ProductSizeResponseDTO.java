package com.group1.swp.pizzario_swp391.dto.productsize;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSizeResponseDTO {
    private Long id;
    private Long productId;
    private String productName;
    private Long sizeId;
    private String sizeName;

    private Double basePrice;
    private String basePriceFormatted;

    private Double flashSalePrice;
    private LocalDateTime flashSaleStart;
    private String flashSaleStartFormatted;
    private LocalDateTime flashSaleEnd;
    private String flashSaleEndFormatted;

    private boolean onFlashSale;
    private Double currentPrice;
    private String currentPriceFormatted;
}