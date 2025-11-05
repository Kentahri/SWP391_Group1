package com.group1.swp.pizzario_swp391.dto.product;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductUpdateDTO{

    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(max = 100, message = "Tên sản phẩm không được vượt quá 100 ký tự")
    String name;

    @Size(max = 1000, message = "Mô tả không được vượt quá 1000 ký tự")
    String description;

    @Pattern(
            regexp = "^(https?://.*\\.(jpg|jpeg|png|gif|webp))?$",
            flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "URL ảnh phải là URL hợp lệ và có đuôi ảnh hợp lệ"
    )
    String imageURL;

    @NotNull(message = "Danh mục không được để trống")
    Long categoryId;

    boolean active;
}