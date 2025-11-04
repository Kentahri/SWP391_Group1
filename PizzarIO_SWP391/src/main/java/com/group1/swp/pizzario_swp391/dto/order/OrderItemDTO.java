package com.group1.swp.pizzario_swp391.dto.order;

import com.group1.swp.pizzario_swp391.entity.OrderItem.OrderItemStatus;
import com.group1.swp.pizzario_swp391.entity.ProductSize;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * DTO cho OrderItem để hiển thị trong order detail
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderItemDTO {
    Long id;
    String productName;
    String productImageUrl;
    int quantity;
    double unitPrice;
    double totalPrice;
    String note;
    ProductSize productSize;
    OrderItemStatus status;
}


