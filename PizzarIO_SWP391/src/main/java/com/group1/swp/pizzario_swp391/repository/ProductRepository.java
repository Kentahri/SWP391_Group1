package com.group1.swp.pizzario_swp391.repository;

import com.group1.swp.pizzario_swp391.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("select p from Product p " +
            "where p.active != false " +
            "order by p.basePrice asc " +
            "limit 10")
    List<Product> findCheapestProducts();

    @Query("select p from Product p " +
            "where p.active != false " +
            "order by p.basePrice desc " +
            "limit 10")
    List<Product> findHighestPriceProducts();
}
