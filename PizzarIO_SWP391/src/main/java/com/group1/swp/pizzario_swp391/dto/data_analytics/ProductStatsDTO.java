package com.group1.swp.pizzario_swp391.dto.data_analytics;


public record ProductStatsDTO(
        Long productId,
        String productName,
        Integer orderCount,
        Integer quantitySold,
        Long totalRevenue
) {
    // Constructor bổ sung nếu không cần productId
    public ProductStatsDTO(String productName, Integer orderCount, Integer quantitySold, Long totalRevenue) {
        this(null, productName, orderCount, quantitySold, totalRevenue);
    }
}