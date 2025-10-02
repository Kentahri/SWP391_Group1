package com.group1.swp.pizzario_swp391.dto.staffshift;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StaffShiftDTO {
    private Integer staffId;
    private Integer shiftId;
    // Ngày làm, trạng thái ca làm, lương/giờ
    private LocalDate workDate;
    private String status; // ví dụ: SCHEDULED, CHECKED_IN, CHECKED_OUT
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private BigDecimal hourlyWage; // lương/giờ
}
