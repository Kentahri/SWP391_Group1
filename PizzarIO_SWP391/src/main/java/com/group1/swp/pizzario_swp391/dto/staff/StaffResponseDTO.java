package com.group1.swp.pizzario_swp391.dto.staff;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.group1.swp.pizzario_swp391.entity.Staff.Role;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StaffResponseDTO {
    int id;
    String name;
    LocalDate dateOfBirth;
    String phone;
    String address;
    String email;
    Role role;
    boolean active;
}
