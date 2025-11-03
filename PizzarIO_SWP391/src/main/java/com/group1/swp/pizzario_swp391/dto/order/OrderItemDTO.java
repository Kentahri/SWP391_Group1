package com.group1.swp.pizzario_swp391.dto.order;

import com.group1.swp.pizzario_swp391.entity.OrderItem.OrderItemStatus;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
    String sizeName;
    String productImageUrl;
    int quantity;
    double unitPrice;
    double totalPrice;
    String note;
    OrderItemStatus status;
}


