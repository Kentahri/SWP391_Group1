package com.group1.swp.pizzario_swp391.repository;

import com.group1.swp.pizzario_swp391.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("""
                    select o from Order o
                    where o.orderStatus = 'COMPLETED' AND o.paymentStatus = 'PAID'
                    AND o.createdAt >= :start and o.createdAt < :end
            """)
    List<Order> findInRangeAndPaid(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

}
