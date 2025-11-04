package com.group1.swp.pizzario_swp391.dto.staffshift;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for manual completion of staff shift when employee forgets to check out.
 * Used by managers to complete shifts with custom checkout time and penalty.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ManualCompleteShiftRequest {

    @NotNull(message = "Checkout time is required")
    private LocalDateTime checkoutTime;

    @NotBlank(message = "Note is required for manual completion")
    private String note;

    @NotNull(message = "Penalty percent is required")
    @Min(value = 0, message = "Penalty percent must be at least 0")
    @Max(value = 100, message = "Penalty percent cannot exceed 100")
    private Integer penaltyPercent;
}
