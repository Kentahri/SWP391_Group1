package com.group1.swp.pizzario_swp391.dto.data_analytics;

public record StatsStaffShiftDTO(
        Integer totalShifts,      // Tổng số ca làm việc trong tuần
        Integer totalHours,        // Tổng giờ làm việc
        Double totalWage,         // Tổng lương
        Integer completedShifts   // Số ca hoàn thành
) {
}
