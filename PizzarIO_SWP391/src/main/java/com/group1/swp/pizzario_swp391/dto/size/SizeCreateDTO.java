package com.group1.swp.pizzario_swp391.dto.size;

import jakarta.validation.constraints.NotBlank;
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
    private String sizeName;

}