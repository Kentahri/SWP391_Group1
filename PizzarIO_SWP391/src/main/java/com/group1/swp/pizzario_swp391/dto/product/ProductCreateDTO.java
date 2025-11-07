package com.group1.swp.pizzario_swp391.dto.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductCreateDTO{

    Long id; // null khi tạo mới, có giá trị khi cập nhật

    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(min = 1, max = 100, message = "Tên sản phẩm không được vượt quá 100 ký tự")
    @Pattern(regexp = "^[^<>\"';&]*$", message = "Tên sản phẩm không được chứa ký tự đặc biệt nguy hiểm")
    String name;

    @Size(max = 1000, message = "Mô tả không được vượt quá 1000 ký tự")
    String description;

    @Pattern(
            regexp = "^(https?://.*\\.(jpg|jpeg|png|gif|webp))?$",
            flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "URL ảnh phải là URL hợp lệ (http/https) và có đuôi .jpg, .jpeg, .png, .gif hoặc .webp"
    )
    String imageURL;

    @NotNull(message = "Danh mục không được để trống")
    Long categoryId;

    @Builder.Default
    boolean active = true;

    // === MỚI: File upload ===
    MultipartFile imageFile;

    // === MỚI: Ảnh hiện tại (khi edit) ===
    String currentImageURL;

    // === COMBO: Giá combo và danh sách thành phần (JSON) ===
    // comboPrice chỉ dùng khi danh mục là Combo
    Double comboPrice;
    // JSON dạng: [{"productSizeId": number, "quantity": number}, ...]
    String comboItemsJson;
}