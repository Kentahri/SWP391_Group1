package com.group1.swp.pizzario_swp391.dto.cart;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class CartItemDTO {
    Long productId;
    String productName;
    String productImageUrl;
    int quantity;
    double unitPrice;
    double totalPrice;
    String note;
}