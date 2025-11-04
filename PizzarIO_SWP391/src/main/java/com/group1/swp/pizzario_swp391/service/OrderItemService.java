package com.group1.swp.pizzario_swp391.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.group1.swp.pizzario_swp391.dto.order.OrderItemDTO;
import com.group1.swp.pizzario_swp391.entity.OrderItem;
import com.group1.swp.pizzario_swp391.mapper.OrderItemMapper;
import com.group1.swp.pizzario_swp391.repository.OrderItemRepository;

@Service
public class OrderItemService {
    private final OrderItemRepository orderItemRepository;
    private final OrderItemMapper orderItemMapper;

    public OrderItemService(OrderItemRepository orderItemRepository, OrderItemMapper orderItemMapper) {
        this.orderItemRepository = orderItemRepository;
        this.orderItemMapper = orderItemMapper;
    }

    public List<OrderItemDTO> getOrderItemsByOrderId(Long orderId) {
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
        if (orderItems == null || orderItems.isEmpty()) return new ArrayList<>();
        return orderItems.stream()
                .map(orderItemMapper::toOrderItemDTO)
                .collect(Collectors.toList());
    }
}
