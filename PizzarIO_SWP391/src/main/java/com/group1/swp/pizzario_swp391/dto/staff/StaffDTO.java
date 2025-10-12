package com.group1.swp.pizzario_swp391.dto.staff;

import com.group1.swp.pizzario_swp391.entity.Staff;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * DTO cho việc hiển thị thông tin staff cơ bản
 * Không chứa validation vì chỉ dùng để hiển thị
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StaffDTO {
    int id;
    String name;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    LocalDate dateOfBirth;
    
    String phone;
    String address;
    String email;
    Staff.Role role;
    boolean active;
}
