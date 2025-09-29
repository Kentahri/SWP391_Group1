package com.group1.swp.pizzario_swp391.dto;

import com.group1.swp.pizzario_swp391.entity.Staff;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StaffDTO {

    String name;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    LocalDate dateOfBirth;
    String phone;
    String address;
    String username;
    String password;
    String email;
    Staff.Role role;
    boolean active;

}
