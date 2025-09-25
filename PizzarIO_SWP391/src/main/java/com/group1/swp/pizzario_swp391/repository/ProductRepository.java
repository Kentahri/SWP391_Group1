package com.group1.swp.pizzario_swp391.repository;


import com.group1.swp.pizzario_swp391.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
}
