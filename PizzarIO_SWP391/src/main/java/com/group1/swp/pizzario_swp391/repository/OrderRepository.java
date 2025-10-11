package com.group1.swp.pizzario_swp391.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.group1.swp.pizzario_swp391.entity.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
}

