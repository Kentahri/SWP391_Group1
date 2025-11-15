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

    private Integer id;

    @NotNull(message = "Vui lòng chọn nhân viên")
    private Integer staffId;

    @NotNull(message = "Vui lòng chọn loại ca")
    private Integer shiftId;

    @NotNull(message = "Vui lòng chọn ngày làm việc")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate workDate;

    // Mặc định là SCHEDULED và không thể thay đổi
    private String status = "SCHEDULED";

    private LocalDateTime checkIn;

    private LocalDateTime checkOut;

    // Lương sẽ được tự động tính từ Shift entity
    // Không cần validation vì sẽ được set tự động
    private BigDecimal hourlyWage;

    @Size(max = 500, message = "Ghi chú không được vượt quá 500 ký tự")
    private String note;

    private Integer penaltyPercent = 0;

    // Để kiểm tra xem ca làm có thể xóa được không (frontend)
    private boolean canDelete = false;
}
