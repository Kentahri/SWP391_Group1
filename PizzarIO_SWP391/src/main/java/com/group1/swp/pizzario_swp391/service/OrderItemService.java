package com.group1.swp.pizzario_swp391.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.group1.swp.pizzario_swp391.dto.order.OrderItemDTO;
import com.group1.swp.pizzario_swp391.entity.Order;
import com.group1.swp.pizzario_swp391.entity.OrderItem;
import com.group1.swp.pizzario_swp391.mapper.OrderItemMapper;
import com.group1.swp.pizzario_swp391.repository.OrderRepository;

@Service
public class OrderItemService {
    private final OrderRepository orderRepository;
    private final OrderItemMapper orderItemMapper;

    public OrderItemService(OrderRepository orderRepository, OrderItemMapper orderItemMapper) {
        this.orderRepository = orderRepository;
        this.orderItemMapper = orderItemMapper;
    }

    public List<OrderItemDTO> getOrderItemsByOrderId(Long orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null || order.getOrderItems() == null) return new ArrayList<>();
        List<OrderItemDTO> dtos = new ArrayList<>();
        for (OrderItem oi : order.getOrderItems()) {
            dtos.add(orderItemMapper.toOrderItemDTO(oi));
        }
        return dtos;
    }
}
