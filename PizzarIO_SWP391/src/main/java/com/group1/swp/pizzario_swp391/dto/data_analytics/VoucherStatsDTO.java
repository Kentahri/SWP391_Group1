package com.group1.swp.pizzario_swp391.dto.data_analytics;

public record VoucherStatsDTO(
                Integer totalVouchers,
                Integer activeVouchers,
                Integer totalUsages,
                Double totalSavings) {
}