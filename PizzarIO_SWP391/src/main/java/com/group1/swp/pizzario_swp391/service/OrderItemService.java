package com.group1.swp.pizzario_swp391.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.group1.swp.pizzario_swp391.dto.order.OrderItemDTO;
import com.group1.swp.pizzario_swp391.dto.websocket.KitchenOrderMessage;
import com.group1.swp.pizzario_swp391.entity.Order;
import com.group1.swp.pizzario_swp391.entity.OrderItem;
import com.group1.swp.pizzario_swp391.mapper.OrderItemMapper;
import com.group1.swp.pizzario_swp391.repository.OrderItemRepository;
import com.group1.swp.pizzario_swp391.repository.OrderRepository;

@Service
public class OrderItemService {
    private final OrderRepository orderRepository;
    private final OrderItemMapper orderItemMapper;
    private final OrderItemRepository orderItemRepository;
    private final WebSocketService webSocketService;

    public OrderItemService(OrderRepository orderRepository, OrderItemMapper orderItemMapper, OrderItemRepository orderItemRepository, WebSocketService webSocketService) {
        this.orderRepository = orderRepository;
        this.orderItemMapper = orderItemMapper;
        this.orderItemRepository = orderItemRepository;
        this.webSocketService = webSocketService;
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
            String productName = item.getProductSize() != null && item.getProductSize().getProduct() != null 
                    ? item.getProductSize().getProduct().getName() 
                    : "Unknown Product";
            
            order.setTotalPrice(order.getTotalPrice() - item.getTotalPrice());
            order.getOrderItems().remove(item);
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);
            
            // Gửi thông báo đến kitchen qua websocket
            notifyKitchenItemCancelled(order, item, productName);
            
            // Gửi thông báo đến cashier khi guest hủy món
            notifyCashierItemCancelled(order, item, productName);
        }
    }
    
    /**
     * Gửi thông báo khi có item bị hủy đến kitchen
     */
    private void notifyKitchenItemCancelled(Order order, OrderItem cancelledItem, String productName) {
        try {
            // Lấy danh sách items còn lại sau khi hủy
            List<OrderItem> remainingItems = orderItemRepository.findByOrderId(order.getId());
            
            KitchenOrderMessage cancelMessage = KitchenOrderMessage.builder()
                    .type(KitchenOrderMessage.MessageType.ORDER_ITEM_CANCELLED)
                    .orderId(order.getId())
                    .code(String.format("ORD-%05d", order.getId()))
                    .tableName(order.getSession() != null && order.getSession().getTable() != null 
                            ? "Bàn " + order.getSession().getTable().getId() 
                            : "Take away")
                    .orderType(order.getOrderType() != null ? order.getOrderType().toString() : "DINE_IN")
                    .status(order.getOrderStatus() != null ? order.getOrderStatus().toString() : "PREPARING")
                    .priority("NORMAL")
                    .totalPrice(order.getTotalPrice())
                    .totalItems(remainingItems.size())
                    .completedItems((int) remainingItems.stream()
                            .filter(oi -> oi.getOrderItemStatus() == OrderItem.OrderItemStatus.SERVED)
                            .count())
                    .note(order.getNote())
                    .message("Món đã bị hủy: " + productName + " (x" + cancelledItem.getQuantity() + ")")
                    .items(List.of(
                            KitchenOrderMessage.OrderItemInfo.builder()
                                    .itemId(cancelledItem.getId())
                                    .productName(productName)
                                    .quantity(cancelledItem.getQuantity())
                                    .status("CANCELLED")
                                    .note(cancelledItem.getNote())
                                    .price(cancelledItem.getTotalPrice())
                                    .build()
                    ))
                    .build();

            webSocketService.broadcastNewOrderToKitchen(cancelMessage);
        } catch (Exception e) {
            System.err.println("Error notifying kitchen of cancelled item: " + e.getMessage());
        }
    }
    
    /**
     * Gửi thông báo khi có item bị hủy đến cashier
     */
    private void notifyCashierItemCancelled(Order order, OrderItem cancelledItem, String productName) {
        try {
            // Chỉ gửi notification nếu order có session (DINE_IN order)
            if (order.getSession() == null) {
                return;
            }

            // Lấy danh sách items còn lại sau khi hủy
            List<OrderItem> remainingItems = orderItemRepository.findByOrderId(order.getId());
            
            KitchenOrderMessage cancelMessage = KitchenOrderMessage.builder()
                    .type(KitchenOrderMessage.MessageType.ORDER_ITEM_CANCELLED)
                    .orderId(order.getId())
                    .code(String.format("ORD-%05d", order.getId()))
                    .tableName(order.getSession().getTable() != null 
                            ? "Bàn " + order.getSession().getTable().getId() 
                            : "")
                    .orderType(order.getOrderType() != null ? order.getOrderType().toString() : "DINE_IN")
                    .status(order.getOrderStatus() != null ? order.getOrderStatus().toString() : "PREPARING")
                    .totalPrice(order.getTotalPrice())
                    .totalItems(remainingItems.size())
                    .completedItems((int) remainingItems.stream()
                            .filter(oi -> oi.getOrderItemStatus() == OrderItem.OrderItemStatus.SERVED)
                            .count())
                    .message("Guest đã hủy món: " + productName + " (x" + cancelledItem.getQuantity() + ")")
                    .items(List.of(
                            KitchenOrderMessage.OrderItemInfo.builder()
                                    .itemId(cancelledItem.getId())
                                    .productName(productName)
                                    .quantity(cancelledItem.getQuantity())
                                    .status("CANCELLED")
                                    .note(cancelledItem.getNote())
                                    .price(cancelledItem.getTotalPrice())
                                    .build()
                    ))
                    .build();

            System.out.println("Sending order item cancellation to cashier - OrderId: " + order.getId() + ", Item: " + productName);
            webSocketService.broadcastOrderUpdateToCashier(cancelMessage);
        } catch (Exception e) {
            System.err.println("Error notifying cashier of cancelled item: " + e.getMessage());
        }
    }
}
