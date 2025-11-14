package com.group1.swp.pizzario_swp391.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CategoryUpdateDTO {
    @NotBlank(message = "Tên danh mục không được để trống")
    @Size(max = 50, message = "Tên danh mục không được vượt quá 50 ký tự")
    @Pattern(regexp = "^[^<>\"';&]*$", message = "Tên danh mục không được chứa ký tự đặc biệt nguy hiểm")
    String name;

    @Size(max = 256, message = "Mô tả không được vượt quá 256 ký tự")
    @Pattern(regexp = "^[^<>\"';&]*$", message = "Mô tả không được chứa ký tự đặc biệt nguy hiểm")
    String description;

    boolean active;
}
