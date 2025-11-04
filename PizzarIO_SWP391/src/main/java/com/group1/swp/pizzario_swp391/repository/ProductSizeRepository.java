package com.group1.swp.pizzario_swp391.repository;

import com.group1.swp.pizzario_swp391.entity.ProductSize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductSizeRepository extends JpaRepository<ProductSize, Long>{

    boolean existsByProductIdAndSizeId(Long productId, Long sizeId);

    boolean existsByProductIdAndSizeIdAndIdNot(Long productId, Long sizeId, Long id);

    @Query("SELECT ps FROM ProductSize ps " +
            "JOIN FETCH ps.product p " +
            "JOIN FETCH ps.size s")
    List<ProductSize> findAllWithRelations();

    List<ProductSize> findByProductId(Long productId);
}
