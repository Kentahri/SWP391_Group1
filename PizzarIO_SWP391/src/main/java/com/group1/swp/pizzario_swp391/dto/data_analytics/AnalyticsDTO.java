package com.group1.swp.pizzario_swp391.dto.data_analytics;

/**
 * DTO tổng hợp tất cả các chỉ số KPI của Analytics
 * Bao gồm dữ liệu hiện tại và % thay đổi so với kỳ trước
 */
public record AnalyticsDTO(
        // Doanh thu
        Long totalRevenue,
        Double revenueDelta, // % thay đổi so với kỳ trước

        // Đơn hàng
        Long totalOrders,
        Double ordersDelta,

        // Khách hàng mới
        Long newCustomers,
        Double newCustomersDelta,

        // AOV (Average Order Value)
        Long aov,
        Double aovDelta,

        // Dữ liệu realtime hôm nay
        Long todayOrders,
        Long todayRevenue,

        // Xem chi tiết đơn
        Long oldCustomers,

        // Tỷ lệ giữ chân khách hàng
        Double retentionRate,

        // Thống kê đơn hàng
        Double completedRate,
        Double cancelRate,
        String avgDeliveryTime) {

    public AnalyticsDTO(Long totalRevenue, Double revenueDelta, Long totalOrders, Double ordersDelta, Long newCustomers, Double newCustomersDelta, Long aov, Double aovDelta, Long oldCustomer, Double retentionRate) {
        this(
                totalRevenue, revenueDelta,
                totalOrders,  ordersDelta,
                newCustomers, newCustomersDelta,          // newCustomers, newCustomersDelta
                aov,      aovDelta,   // aov, aovDelta
                0L, 0L,          // todayOrders, todayRevenue
                oldCustomer,            // todayNewCustomers, todayReturningCustomers
                retentionRate,             // retentionRate
                0.0, 0.0,        // completedRate, cancelRate
                "-"              // avgDeliveryTime
        );
    }
}
