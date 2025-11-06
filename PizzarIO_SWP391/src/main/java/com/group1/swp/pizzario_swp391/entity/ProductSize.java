package com.group1.swp.pizzario_swp391.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(
        name = "[Product_Size]",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"product_id", "size_id"})
        }
)
@Data
@NoArgsConstructor
@ToString(exclude = {"product", "size"})
@AllArgsConstructor
public class ProductSize{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Món ăn
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // Size tương ứng
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "size_id", nullable = false)
    private Size size;

    @Column(name = "base_price")
    private double basePrice;

    @Column(name = "flash_sale_price")
    private Double flashSalePrice;

    @Column(name = "flash_sale_start")
    private LocalDateTime flashSaleStart;

    @Column(name = "flash_sale_end")
    private LocalDateTime flashSaleEnd;

    public String getFlashSaleStartFormatted() {
        return flashSaleStart != null ? flashSaleStart.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "";
    }

    public String getFlashSaleEndFormatted() {
        return flashSaleEnd != null ? flashSaleEnd.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "";
    }

    public boolean isOnFlashSale() {
        LocalDateTime now = LocalDateTime.now();
        return flashSaleStart != null && flashSaleEnd != null
                && now.isAfter(flashSaleStart) && now.isBefore(flashSaleEnd);
    }

    public double getCurrentPrice() {
        if (isOnFlashSale() && flashSalePrice != null) {
            return flashSalePrice;
        }
        return basePrice;
    }

    public String getCurrentPriceFormatted() {
        return String.format("%,.0f VND", getCurrentPrice());
    }

    public String getBasePriceFormatted() {
        return String.format("%,.0f VND", basePrice);
    }

}
