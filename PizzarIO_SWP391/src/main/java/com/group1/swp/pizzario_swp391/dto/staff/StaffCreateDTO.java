package com.group1.swp.pizzario_swp391.dto.staff;

import java.time.LocalDate;

import com.group1.swp.pizzario_swp391.entity.Staff.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StaffCreateDTO {
    @NotBlank(message = "Tên nhân viên không được để trống")
    @Size(max = 100, message = "Tên nhân viên không được vượt quá 100 ký tự")
    @Pattern(regexp = "^[\\p{L}\\s]+$", message = "Tên nhân viên chỉ được chứa chữ cái và khoảng trắng, không được chứa số và ký tự đặc biệt")
    String name;

    @NotNull(message = "Ngày sinh không được để trống")
    @Past(message = "Ngày sinh phải là ngày trong quá khứ")
    LocalDate dateOfBirth;

    @Pattern(regexp = "^0\\d{9,10}$", message = "Số điện thoại phải bắt đầu bằng 0 và có 10-11 chữ số")
    String phone;

    @NotBlank(message = "Địa chỉ không được để trống")
    @Size(max = 200, message = "Địa chỉ không được vượt quá 200 ký tự")
    String address;

    @Pattern(regexp = "^(?!\\.)(?!.*\\.\\.)[A-Za-z0-9._-]+(?<!\\.)@gmail\\.com$",message = "Email không đúng định dạng")
    @Size(max = 50, message = "Email chỉ không được vượt quá 50 ký tự")
    String email;

    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    String password;

    @NotNull(message = "Vai trò không được để trống")
    Role role;

    boolean active = true;
}
