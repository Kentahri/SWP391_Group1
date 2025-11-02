package com.group1.swp.pizzario_swp391.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

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
public class ProductSize {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Món ăn
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // Size tương ứng
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "size_id", nullable = false)
    private Size size;

    // Giá thực tế cho món đó với size này
    @Column(name = "price_alt", nullable = false)
    private double priceAlt;

    public ProductSize(Product product, Size size, double priceAlt) {
        this.product = product;
        this.size = size;
        this.priceAlt = priceAlt;
    }
}
