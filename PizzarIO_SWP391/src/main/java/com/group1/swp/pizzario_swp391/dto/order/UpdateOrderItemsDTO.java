package com.group1.swp.pizzario_swp391.dto.order;

import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

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
        Long sizeId;
    }
}
