package com.group1.swp.pizzario_swp391.dto.product;

import java.time.LocalDateTime;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductCreateDTO {

    Long id; // Nullable - dùng để phân biệt Create (null) vs Update (có giá trị)

    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(min = 1, max = 100, message = "Tên sản phẩm không được vượt quá 100 ký tự")
    @Pattern(regexp = "^[^<>\"';&]*$", message = "Tên sản phẩm không được chứa ký tự đặc biệt nguy hiểm")
    String name;

    @Size(max = 1000, message = "Mô tả không được vượt quá 1000 ký tự")
    String description;

    @Pattern(regexp = "^https?://.*\\.(jpg|jpeg|png|gif|webp)$", flags = Pattern.Flag.CASE_INSENSITIVE, message = "URL ảnh phải là URL hợp lệ (http/https) và có đuôi .jpg, .jpeg, .png, .gif hoặc .webp")
    String imageURL;

    @NotNull(message = "Giá cơ bản không được để trống")
    @DecimalMin(value = "0.01", inclusive = false, message = "Giá cơ bản phải lớn hơn 0")
    Double basePrice;

    @DecimalMin(value = "0.01", message = "Giá flash sale không được âm")
    Double flashSalePrice;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    LocalDateTime flashSaleStart;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    LocalDateTime flashSaleEnd;

    @NotNull(message = "Danh mục không được để trống")
    Long categoryId;

    @Builder.Default
    boolean active = true;
}
