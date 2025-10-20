package com.group1.swp.pizzario_swp391.dto.category;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CategoryResponseDTO {
    Long id;
    String name;
    String description;
    boolean active;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    int totalProducts;

    // Formatted date for display
    public String getCreatedAtFormatted() {
        return createdAt != null ? createdAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "";
    }

    public String getUpdatedAtFormatted() {
        return updatedAt != null ? updatedAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "";
    }

    public String getStatusText() {
        return active ? "Hoạt động" : "Không hoạt động";
    }
}
