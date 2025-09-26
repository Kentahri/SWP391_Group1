package com.group1.swp.pizzario_swp391.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateProductRequest {
    Long categoryId;
    String name;
    String description;
    String imageURL;
    double basePrice;
    double flashSalePrice;
    LocalDateTime flashSaleStart;
    LocalDateTime flashSaleEnd;
    boolean isActive;
}


