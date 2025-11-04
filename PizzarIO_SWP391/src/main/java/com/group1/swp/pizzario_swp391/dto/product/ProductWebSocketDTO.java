package com.group1.swp.pizzario_swp391.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductWebSocketDTO {

    private Long id;
    private String name;
    private String description;
    private String imgUrl; // Đổi từ imageURL → imgUrl (theo entity)

    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Thông tin danh mục (chỉ ID và tên)
    private Long categoryId;
    private String categoryName;
}