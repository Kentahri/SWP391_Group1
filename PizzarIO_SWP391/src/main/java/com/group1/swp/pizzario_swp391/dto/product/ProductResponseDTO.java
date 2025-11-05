package com.group1.swp.pizzario_swp391.dto.product;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductResponseDTO {

    Long id;
    String name;
    String description;
    String imageURL; // Đổi từ imageURL → imgUrl
    boolean active;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    String categoryName;
    Long categoryId;
    List<ProductSizeDTO> sizes;

    // Formatted fields
    public String getCreatedAtFormatted() {
        return createdAt != null ? createdAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "";
    }

    public String getUpdatedAtFormatted() {
        return updatedAt != null ? updatedAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "";
    }

    public String getStatusText() {
        return active ? "Hoạt động" : "Không hoạt động";
    }

    public String getImgUrlOrDefault() {
        return (imageURL != null && !imageURL.isBlank()) ? imageURL : "/images/no-image.png";
    }
}