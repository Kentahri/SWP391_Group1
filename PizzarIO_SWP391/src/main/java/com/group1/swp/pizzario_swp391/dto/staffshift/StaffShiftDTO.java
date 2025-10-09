package com.group1.swp.pizzario_swp391.dto.staffshift;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StaffShiftDTO {

    @NotNull(message = "Staff ID is required")
    private Integer staffId;

    @NotNull(message = "Shift ID is required")
    private Integer shiftId;

    @NotNull(message = "Work date is required")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @FutureOrPresent(message = "Work date cannot be in the past")
    private LocalDate workDate;

    @NotBlank(message = "Status is required")
    @Pattern(regexp = "SCHEDULED|CHECKED_IN|CHECKED_OUT",
            message = "Status must be one of: SCHEDULED, CHECKED_IN, CHECKED_OUT")
    private String status;

    private LocalDateTime checkIn;

    private LocalDateTime checkOut;

    @NotNull(message = "Hourly wage is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Hourly wage must be greater than 0")
    private BigDecimal hourlyWage;
}
