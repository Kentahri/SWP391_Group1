package com.group1.swp.pizzario_swp391.dto.product;

import lombok.*;
import lombok.experimental.FieldDefaults;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductDTO {
    @NotNull(message = "ID danh mục không được để trống")
    Long categoryId;

    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(max = 200, message = "Tên sản phẩm không được vượt quá 200 ký tự")
    String name;

    @Size(max = 1000, message = "Mô tả không được vượt quá 1000 ký tự")
    String description;

    @Size(max = 500, message = "URL hình ảnh không được vượt quá 500 ký tự")
    // @URL(message = "URL hình ảnh không hợp lệ") // Có thể thêm nếu bạn muốn kiểm tra định dạng URL cơ bản
    String imageURL;

    @NotNull(message = "Giá cơ bản không được để trống")
    @Min(value = 0, message = "Giá cơ bản phải lớn hơn hoặc bằng 0")
    double basePrice;

    @Min(value = 0, message = "Giá khuyến mãi phải lớn hơn hoặc bằng 0")
    double flashSalePrice;

    // Các kiểm tra liên quan đến flashSaleStart và flashSaleEnd thường được xử lý tốt hơn ở tầng Service
    // hoặc bằng một custom annotation nếu logic phức tạp.
    LocalDateTime flashSaleStart;
    LocalDateTime flashSaleEnd;

    boolean active;
    
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}