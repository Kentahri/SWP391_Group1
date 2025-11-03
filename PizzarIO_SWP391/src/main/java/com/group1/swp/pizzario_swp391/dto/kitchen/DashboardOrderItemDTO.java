package com.group1.swp.pizzario_swp391.dto.kitchen;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardOrderItemDTO {
    private Long id;
    private String productName;
    private Long categoryId;
    private String categoryName;
    private int quantity;
    private String status;
    private String note;
    private LocalDateTime createdAt;
    private OrderInfo orderInfo;
    private ProductInfo productInfo;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderInfo {
        private String code;
        private String tableName;
        private String orderType;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductInfo {
        private Long id;
        private String name;
        private Long categoryId;
        private String categoryName;
    }
}
