package com.group1.swp.pizzario_swp391.dto.product;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
    String imageURL;
    double basePrice;
    double flashSalePrice;
    LocalDateTime flashSaleStart;
    LocalDateTime flashSaleEnd;
    boolean isActive;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    String categoryName;
    Long categoryId;
    
    // Formatted fields for display
    public String getCreatedAtFormatted() {
        return createdAt != null ? createdAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "";
    }
    
    public String getUpdatedAtFormatted() {
        return updatedAt != null ? updatedAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "";
    }
    
    public String getFlashSaleStartFormatted() {
        return flashSaleStart != null ? flashSaleStart.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "";
    }
    
    public String getFlashSaleEndFormatted() {
        return flashSaleEnd != null ? flashSaleEnd.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "";
    }
    
    public String getStatusText() {
        return isActive ? "Hoạt động" : "Không hoạt động";
    }
    
    public boolean isOnFlashSale() {
        LocalDateTime now = LocalDateTime.now();
        return flashSaleStart != null && flashSaleEnd != null 
                && now.isAfter(flashSaleStart) && now.isBefore(flashSaleEnd);
    }
    
    public double getCurrentPrice() {
        return isOnFlashSale() ? flashSalePrice : basePrice;
    }
    
    public String getCurrentPriceFormatted() {
        return String.format("%,.0f VND", getCurrentPrice());
    }
    
    public String getBasePriceFormatted() {
        return String.format("%,.0f VND", basePrice);
    }
}
