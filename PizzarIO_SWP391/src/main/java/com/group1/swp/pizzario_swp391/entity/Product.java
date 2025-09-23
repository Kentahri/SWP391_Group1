package com.group1.swp.pizzario_swp391.entity;

import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "Product")
@Data
@NoArgsConstructor
@ToString
public class Product {

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;
    private String description;

    @Column(name = "img_url")
    private String imageURL;

    @Column(name = "base_price")
    private double basePrice;

    @Column(name = "flash_sale_price")
    private double flashSalePrice;

    @Column(name = "flash_sale_start")
    private LocalDateTime flashSaleStart;

    @Column(name = "flash_sale_end")
    private LocalDateTime flashSaleEnd;

    @Column(name = "is_active")
    private boolean isActive;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Product(String name, String description, String imageURL, double basePrice, double flashSalePrice, LocalDateTime flashSaleStart, LocalDateTime flashSaleEnd, boolean isActive, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.name = name;
        this.description = description;
        this.imageURL = imageURL;
        this.basePrice = basePrice;
        this.flashSalePrice = flashSalePrice;
        this.flashSaleStart = flashSaleStart;
        this.flashSaleEnd = flashSaleEnd;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
