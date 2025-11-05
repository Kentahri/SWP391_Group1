package com.group1.swp.pizzario_swp391.dto.order;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateOrderItemsDTO {
    Integer tableId;
    Integer orderId;
    List<OrderItemUpdate> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class OrderItemUpdate {
        Long productId;
        String productName;
        Integer quantity;
        Double price;
    }
}
