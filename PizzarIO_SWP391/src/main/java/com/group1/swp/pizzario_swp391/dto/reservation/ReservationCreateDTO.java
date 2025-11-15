package com.group1.swp.pizzario_swp391.dto.reservation;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationCreateDTO {
    private Integer tableId;
    private String customerName;
    @Pattern(regexp = "^(?:\\+84|0)\\d{9}$", message = "Số điện thoại không hợp lệ (phải bắt đầu bằng 0 hoặc +84 và có 10-12 số)")
    private String customerPhone;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    @Future(message = "Thời gian đặt bàn phải là thời điểm trong tương lai")
    private LocalDateTime startTime;
    
    @Min(value = 1, message = "Số lượng khách không được ít hơn 1 người")
    @Max(value = 20, message = "Số lượng khách không được vượt quá 20 người")
    private int capacityExpected;
    
    private String note;
}

