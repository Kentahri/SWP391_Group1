package com.group1.swp.pizzario_swp391.dto.cart;

import com.group1.swp.pizzario_swp391.entity.ProductSize;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

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
    ProductSize productSize;
    List<ProductSize> productSizes;
    String note;
}