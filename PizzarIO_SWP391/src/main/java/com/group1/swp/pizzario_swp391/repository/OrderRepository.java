package com.group1.swp.pizzario_swp391.repository;

import com.group1.swp.pizzario_swp391.dto.data_analytics.ProductStatsDTO;
import com.group1.swp.pizzario_swp391.entity.Order;
import com.group1.swp.pizzario_swp391.entity.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>{

    @Query("""
                    select o from Order o
                    where o.orderStatus = 'COMPLETED' AND o.paymentStatus = 'PAID'
                    AND o.createdAt >= :start and o.createdAt < :end
            """)
    List<Order> findInRangeAndPaid(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("""
                    SELECT new com.group1.swp.pizzario_swp391.dto.data_analytics.ProductStatsDTO(
                            p.name,
                            CAST(COUNT(DISTINCT o.id) AS int),
                            CAST(SUM(oi.quantity) AS int),
                            CAST(SUM(oi.totalPrice) AS long)
                    )
                    FROM Order o
                    JOIN o.orderItems oi
                    JOIN oi.productSize.product p
                    WHERE o.orderStatus = 'COMPLETED' AND o.paymentStatus = 'PAID'
                    GROUP BY p.id, p.name
                    ORDER BY SUM(oi.quantity) DESC
            """)
    List<ProductStatsDTO> findTopBestSellingProducts(Pageable pageable);

    @Query("""
                    SELECT new com.group1.swp.pizzario_swp391.dto.data_analytics.ProductStatsDTO(
                            p.name,
                            CAST(COUNT(DISTINCT o.id) AS int),
                            CAST(SUM(oi.quantity) AS int),
                            CAST(SUM(oi.totalPrice) AS long)
                    )
                    FROM Order o
                    JOIN o.orderItems oi
                    JOIN oi.productSize.product p
                    WHERE o.orderStatus = 'COMPLETED' AND o.paymentStatus = 'PAID'
                    AND o.createdAt >= :startDate AND o.createdAt < :endDate
                    GROUP BY p.id, p.name
                    ORDER BY SUM(oi.quantity) DESC
            """)
    List<ProductStatsDTO> findTopBestSellingProductsBetweenDates(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    @Query("""
                    SELECT new com.group1.swp.pizzario_swp391.dto.data_analytics.ProductStatsDTO(
                            p.name,
                            CAST(COUNT(DISTINCT o.id) AS int),
                            CAST(SUM(oi.quantity) AS int),
                            CAST(SUM(oi.totalPrice) AS long)
                    )
                    FROM Order o
                    JOIN o.orderItems oi
                    JOIN oi.productSize.product p
                    WHERE o.orderStatus = 'COMPLETED' AND o.paymentStatus = 'PAID'
                    AND o.createdAt >= :startDate AND o.createdAt < :endDate
                    AND p.category.id = :categoryId
                    GROUP BY p.id, p.name
                    ORDER BY SUM(oi.quantity) DESC
            """)
    List<ProductStatsDTO> findTopBestSellingProductsByDateAndCategory(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("categoryId") Long categoryId,
            Pageable pageable);


    @Query("""
            SELECT p
            FROM Order o
            JOIN o.orderItems oi
            JOIN oi.productSize.product p
            WHERE o.orderStatus = 'COMPLETED'
              AND o.paymentStatus = 'PAID'
            GROUP BY p.id, p.name, p.description,
                     p.imageURL, p.active,
                     p.createdAt, p.updatedAt, p.category
            ORDER BY SUM(oi.quantity) DESC
            """)
    List<Product> findTopBestSellingProductsForGemini(int quantity);

    Order findFirstByMembership_IdOrderByCreatedAtAsc(Long membershipId);

    Long countByMembership_Id(Long membershipId);

    List<Order> findByOrderStatus(com.group1.swp.pizzario_swp391.entity.Order.OrderStatus status);

    List<Order> findByOrderType(com.group1.swp.pizzario_swp391.entity.Order.OrderType type);

    List<Order> findByOrderStatusAndOrderType(com.group1.swp.pizzario_swp391.entity.Order.OrderStatus status,
                                              com.group1.swp.pizzario_swp391.entity.Order.OrderType type);

    @Query("SELECT o FROM Order o WHERE o.session.id = :sessionId")
    Order findBySessionId(@Param("sessionId") Long sessionId);
}
