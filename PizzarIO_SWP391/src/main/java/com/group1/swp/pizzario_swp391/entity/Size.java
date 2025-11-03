package com.group1.swp.pizzario_swp391.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "[Size]")
@Data
@NoArgsConstructor
@ToString(exclude = {"productSizes"})
public class Size {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "size_name", columnDefinition = "NVARCHAR(50)", nullable = false)
    private String sizeName;

    // Một size có thể áp dụng cho nhiều ProductSize
    @OneToMany(mappedBy = "size", cascade = CascadeType.ALL)
    private List<ProductSize> productSizes = new ArrayList<>();

    public Size(String sizeName) {
        this.sizeName = sizeName;
    }
}
