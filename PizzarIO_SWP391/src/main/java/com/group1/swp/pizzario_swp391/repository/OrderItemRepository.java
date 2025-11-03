package com.group1.swp.pizzario_swp391.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.group1.swp.pizzario_swp391.entity.OrderItem;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    
    @Query("SELECT oi FROM OrderItem oi " +
           "JOIN FETCH oi.productSize ps " +
           "JOIN FETCH ps.product p " +
           "JOIN FETCH ps.size s " +
           "JOIN FETCH p.category " +
           "WHERE oi.order.id = :orderId")
    List<OrderItem> findByOrderId(@Param("orderId") Long orderId);
    
    @EntityGraph(attributePaths = {"productSize", "productSize.product", "productSize.size", "productSize.product.category", "order"})
    @Query("SELECT oi FROM OrderItem oi")
    List<OrderItem> findAllWithRelations();
}

