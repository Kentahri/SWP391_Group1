package com.group1.swp.pizzario_swp391.repository;

import com.group1.swp.pizzario_swp391.entity.ProductSize;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductSizeRepository extends JpaRepository<ProductSize, Long>{
    List<ProductSize> findByProductId(Long productId);
}
