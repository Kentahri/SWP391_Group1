package com.group1.swp.pizzario_swp391.service;

import com.group1.swp.pizzario_swp391.dto.order.OrderItemDTO;
import com.group1.swp.pizzario_swp391.entity.Order;
import com.group1.swp.pizzario_swp391.entity.OrderItem;
import com.group1.swp.pizzario_swp391.mapper.OrderItemMapper;
import com.group1.swp.pizzario_swp391.repository.OrderItemRepository;
import com.group1.swp.pizzario_swp391.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderItemService {
    private final OrderRepository orderRepository;
    private final OrderItemMapper orderItemMapper;
    private final OrderItemRepository orderItemRepository;

    public OrderItemService(OrderRepository orderRepository, OrderItemMapper orderItemMapper, OrderItemRepository orderItemRepository) {
        this.orderRepository = orderRepository;
        this.orderItemMapper = orderItemMapper;
        this.orderItemRepository = orderItemRepository;
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
    @Transactional
    public void cancelOrderItem(Long orderItemId) {
        OrderItem item = orderItemRepository.findById(orderItemId).orElseThrow();
        if (item.getOrderItemStatus() == OrderItem.OrderItemStatus.PENDING) {
            Order order = item.getOrder();
            order.setTotalPrice(order.getTotalPrice() - item.getTotalPrice());
            order.getOrderItems().remove(item);
            order.setUpdatedAt(LocalDateTime.now());
        }
    }
}
