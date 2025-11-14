package com.group1.swp.pizzario_swp391.dto.product;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductUpdateDTO{

    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(min = 1, max = 255, message = "Tên sản phẩm phải từ 1 đến 255 ký tự")
    @Pattern(regexp = "^[^<>\"';&]*$", message = "Tên sản phẩm không được chứa ký tự đặc biệt nguy hiểm")
    String name;

    @Size(max = 500, message = "Mô tả không được vượt quá 500 ký tự")
    @Pattern(regexp = "^[^<>\"';&]*$", message = "Mô tả không được chứa ký tự đặc biệt nguy hiểm")
    String description;

    @Pattern(
            regexp = "^(https?://.*\\.(jpg|jpeg|png|gif|webp))?$",
            flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "URL ảnh phải là URL hợp lệ (http/https) và có đuôi .jpg, .jpeg, .png, .gif hoặc .webp"
    )
    String imageURL;

    @NotNull(message = "Danh mục không được để trống")
    Long categoryId;

    boolean active;

    // === MỚI: File upload ===
    MultipartFile imageFile;

    // === MỚI: Ảnh hiện tại (khi edit) ===
    String currentImageURL;
}