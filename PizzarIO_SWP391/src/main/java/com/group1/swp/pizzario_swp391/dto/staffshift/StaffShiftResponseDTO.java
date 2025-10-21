package com.group1.swp.pizzario_swp391.dto.staffshift;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffShiftResponseDTO {

    private Integer staffShiftId;

    private LocalDate workDate;

    private String shiftName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private String staffName;
    private Integer staffId;

    private String shiftStatus; 

    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private BigDecimal hourlyWage;


    private String note;
}
