package com.group1.swp.pizzario_swp391.dto.size;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SizeCreateDTO {
    private Long id;

    @NotBlank(message = "Tên kích thước không được để trống")
    @Size(min = 1, max = 50, message = "Tên kích thước không vượt quá 50 ký tự")
    @Pattern(regexp = "^[^<>\"';&]*$", message = "Tên kích thước không được chứa ký tự đặc biệt nguy hiểm")
    private String sizeName;

}