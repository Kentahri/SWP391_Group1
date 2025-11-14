package com.group1.swp.pizzario_swp391.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShiftDTO {

    Integer id;

    @NotBlank(message = "Tên ca làm việc không được để trống")
    String shiftName;

    // Auto-set from startTimeOnly + LocalDate.now() in controller
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    LocalDateTime startTime;

    // Auto-set from endTimeOnly + LocalDate.now() in controller
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    LocalDateTime endTime;

    LocalDateTime createdAt;

    // Thêm thuộc tính lương cho ca (MapStruct sẽ map tự động từ entity)
    Double salaryPerShift;
}
