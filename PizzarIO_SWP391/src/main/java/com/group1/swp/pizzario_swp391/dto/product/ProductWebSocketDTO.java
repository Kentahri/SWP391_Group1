package com.group1.swp.pizzario_swp391.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductWebSocketDTO {
    private Long id;
    private String name;
    private String description;
    private String imageURL;
    private double basePrice;
    private double flashSalePrice;
    private LocalDateTime flashSaleStart;
    private LocalDateTime flashSaleEnd;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Chỉ gửi thông tin cơ bản của category, không có products
    private Long categoryId;
    private String categoryName;
}