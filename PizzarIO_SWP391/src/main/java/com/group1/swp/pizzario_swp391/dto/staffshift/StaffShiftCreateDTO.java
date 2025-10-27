package com.group1.swp.pizzario_swp391.dto.staff;

import com.group1.swp.pizzario_swp391.entity.Shift;
import com.group1.swp.pizzario_swp391.entity.Staff;
import lombok.Data;
import java.time.LocalDate;

@Data
public class StaffShiftCreateDTO {
    private Staff staff;
    private Shift shift;
    private LocalDate workDate;
}