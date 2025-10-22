package com.group1.swp.pizzario_swp391.dto.data_analytics;

public record StaffShiftCalendarDTO(
        Integer id,              // ID của StaffShift
        String shiftName,        // Tên ca: "Ca Sáng", "Ca Chiều", "Ca Tối"
        String shiftType,        // CSS class: "morning", "afternoon", "evening"
        String staffName,        // Tên nhân viên
        String timeRange,        // Khung giờ: "06:00 - 14:00"
        String statusText,       // Text hiển thị: "Hoàn thành", "Vắng mặt", "Đã lên lịch"
        String statusClass,      // CSS class: "completed", "absent", "scheduled"
        Double totalWage,    // Tổng lương (hourlyWage * số giờ làm)
        String note  
) {
}
