package com.group1.swp.pizzario_swp391.dto.data_analytics;


public record ProductStatsDTO(
        Long topId,
        String productName,
        Integer orderCount,
        Integer quantitySold,
        Long totalRevenue
) {
    public ProductStatsDTO(String productName, Integer orderCount, Integer quantitySold, Long totalRevenue) {
        this(null, productName, orderCount, quantitySold, totalRevenue);
    }
}