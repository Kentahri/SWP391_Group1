package com.group1.swp.pizzario_swp391.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.group1.swp.pizzario_swp391.dto.websocket.KitchenOrderMessage;
import com.group1.swp.pizzario_swp391.entity.Order;
import com.group1.swp.pizzario_swp391.entity.OrderItem;
import com.group1.swp.pizzario_swp391.repository.OrderItemRepository;
import com.group1.swp.pizzario_swp391.repository.OrderRepository;

/**
 * Service để xử lý các thao tác của kitchen
 * Chỉ cập nhật trạng thái của từng item, không cập nhật order status
 */
@Service
public class KitchenService {

    @Autowired
    private OrderItemRepository orderItemRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private WebSocketService webSocketService;

    /**
     * Cập nhật trạng thái item trong order
     * Kitchen chỉ cập nhật status của từng món, không cập nhật order status
     */
    @Transactional
    public void updateItemStatus(Long itemId, String status, String note) {
        OrderItem item = orderItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Order item not found: " + itemId));

        OrderItem.OrderItemStatus newStatus = OrderItem.OrderItemStatus.valueOf(status);
        item.setOrderItemStatus(newStatus);
        
        if (note != null && !note.trim().isEmpty()) {
            item.setNote(note);
        }
        
        orderItemRepository.save(item);

        // Lấy order mới từ database để có dữ liệu cập nhật
        Long orderId = item.getOrder().getId();
        Order updatedOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        
        // Force refresh từ database - clear cache
        System.out.println("Refreshing order " + orderId + " from database");
        
        // Lấy order items trực tiếp từ database
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
        System.out.println("Order items from DB: " + orderItems.size() + " items");
        for (OrderItem oi : orderItems) {
            System.out.println("Item " + oi.getId() + " status: " + oi.getOrderItemStatus());
        }
        
        // Gửi thông báo cập nhật order về frontend với dữ liệu từ database
        notifyOrderUpdate(updatedOrder, orderItems);

        System.out.println("Kitchen updated item " + itemId + " to status " + status);
    }

    /**
     * Gửi thông báo cập nhật order về frontend
     */
    private void notifyOrderUpdate(Order order, List<OrderItem> orderItems) {
        try {
            // Debug log
            System.out.println("Order " + order.getId() + " updated");
            if (orderItems != null) {
                System.out.println("Order items status: " + orderItems.stream()
                        .map(item -> item.getId() + ":" + item.getOrderItemStatus())
                        .toList());
            }

            KitchenOrderMessage updateMessage = KitchenOrderMessage.builder()
                    .type(KitchenOrderMessage.MessageType.ORDER_UPDATED)
                    .orderId(order.getId())
                    .code(String.format("ORD-%05d", order.getId()))
                    .tableName(order.getSession() != null && order.getSession().getTable() != null ? 
                            "Bàn " + order.getSession().getTable().getId() : "Take away")
                    .orderType(order.getOrderType() != null ? order.getOrderType().toString() : "DINE_IN")
                    .status(order.getOrderStatus() != null ? order.getOrderStatus().toString() : "PREPARING")
                    .priority("NORMAL")
                    .totalPrice(order.getTotalPrice())
                    .note(order.getNote())
                    .message("Order đã được cập nhật")
                    .build();

            webSocketService.broadcastNewOrderToKitchen(updateMessage);
        } catch (Exception e) {
            System.err.println("Error notifying order update: " + e.getMessage());
        }
    }
}
