package com.group1.swp.pizzario_swp391.dto.data_analytics;

import java.time.LocalDate;
import java.util.List;

public record WeekDayDTO(
        LocalDate date,                    // Ngày (LocalDate)
        String dayName,                    // Tên ngày: CN, T2, T3, T4, T5, T6, T7
        Boolean isToday,                   // Có phải ngày hôm nay không
        List<StaffShiftCalendarDTO> shifts // Danh sách ca làm việc trong ngày
) {
}
