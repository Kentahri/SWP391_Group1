package com.group1.swp.pizzario_swp391.service;

import com.group1.swp.pizzario_swp391.dto.cart.CartItemDTO;
import com.group1.swp.pizzario_swp391.dto.order.OrderItemDTO;
import com.group1.swp.pizzario_swp391.entity.Order;
import com.group1.swp.pizzario_swp391.entity.OrderItem;
import com.group1.swp.pizzario_swp391.mapper.OrderItemMapper;
import com.group1.swp.pizzario_swp391.repository.OrderItemRepository;
import com.group1.swp.pizzario_swp391.repository.OrderRepository;
import com.group1.swp.pizzario_swp391.repository.ProductRepository;
import com.group1.swp.pizzario_swp391.repository.SessionRepository;
import jakarta.servlet.http.HttpSession;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderService{

    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final SessionRepository sessionRepository;
    private final CartService cartService;
    private final OrderItemMapper orderItemMapper;

    public List<OrderItemDTO> getOrderedItemsForView(Long sessionId) {
        List<OrderItemDTO> orderedItems = new ArrayList<>();
        Order order = getOrderForSession(sessionId);

        if (order != null) {
            order.getOrderItems().forEach(item -> orderedItems.add(orderItemMapper.toOrderItemDTO(item)));
        }
        return orderedItems;
    }

    @Transactional
    public void placeOrder(HttpSession session, Long sessionId) {
        Map<Long, CartItemDTO> cart = cartService.getCart(session);
        if (cart.isEmpty()) {
            return; // Cannot place an empty order
        }

        Order order = getOrderForSession(sessionId);
        if (order == null) {
            throw new IllegalStateException("Order not found for this session.");
        }
        cart.values().forEach(item -> {
            OrderItem orderItem = orderItemMapper.toOrderItem(item);
            orderItem.setProduct(productRepository.findById(item.getProductId()).orElse(null));
            orderItem.setOrderItemStatus(OrderItem.OrderItemStatus.PENDING);
            orderItem.setOrderItemType(OrderItem.OrderItemType.DINE_IN);
            order.setTotalPrice(order.getTotalPrice() + item.getTotalPrice());
            orderItemRepository.save(orderItem);
            order.addOrderItem(orderItem);
        });
        orderRepository.save(order);
        cartService.clearCart(session);
    }

    private Order getOrderForSession(Long sessionId) {
        if (sessionId == null) return null;
        return sessionRepository.findById(sessionId)
                .map(com.group1.swp.pizzario_swp391.entity.Session::getOrder)
                .orElse(null);
    }
}
