    package com.group1.swp.pizzario_swp391.entity;

    import jakarta.persistence.*;
    import jakarta.persistence.Table;
    import lombok.Data;
    import lombok.NoArgsConstructor;
    import lombok.ToString;

    import java.time.LocalDateTime;
    import java.util.ArrayList;
    import java.util.List;

    @Entity
    @Table(name = "[Product]")
    @Data
    @ToString
    public class Product {

        @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
        private List<OrderItem> orderItems;

        public Product() {
            orderItems = new ArrayList<>();
        }

        public void addOrderItem(OrderItem orderItem) {
            orderItems.add(orderItem);
            orderItem.setProduct(this);
        }

        public void removeOrderItem(OrderItem orderItem) {
            orderItems.remove(orderItem);
            orderItem.setProduct(null);
        }

        @ManyToOne
        @JoinColumn(name = "category_id")
        private Category category;

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

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
            this();
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

        public void setCategory(Category category) {
            this.category = category;
        }
    }
