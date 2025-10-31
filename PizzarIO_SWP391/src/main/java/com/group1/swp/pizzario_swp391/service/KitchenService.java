package com.group1.swp.pizzario_swp391.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.group1.swp.pizzario_swp391.dto.kitchen.DashboardOrderItemDTO;
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
     * Get dashboard order items for kitchen dashboard
     */
    public List<DashboardOrderItemDTO> getDashboardOrderItems() {
        List<OrderItem> orderItems = orderItemRepository.findAll().stream()
                .filter(item -> item.getOrder() != null && 
                               item.getOrder().getOrderStatus() != Order.OrderStatus.COMPLETED &&
                               item.getOrder().getOrderStatus() != Order.OrderStatus.CANCELLED)
                .collect(Collectors.toList());

        return orderItems.stream()
                .map(this::convertToDashboardDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convert OrderItem to DashboardOrderItemDTO
     */
    private DashboardOrderItemDTO convertToDashboardDTO(OrderItem item) {
        return DashboardOrderItemDTO.builder()
                .id(item.getId())
                .productName(item.getProduct().getName())
                .categoryId(item.getProduct().getCategory().getId())
                .categoryName(item.getProduct().getCategory().getName())
                .quantity(item.getQuantity())
                .status(item.getOrderItemStatus().name())
                .note(item.getNote())
                .createdAt(item.getOrder().getCreatedAt())
                .orderInfo(DashboardOrderItemDTO.OrderInfo.builder()
                        .code(String.format("ORD-%05d", item.getOrder().getId()))
                        .tableName(item.getOrder().getSession() != null && 
                                  item.getOrder().getSession().getTable() != null ? 
                                  "Bàn " + item.getOrder().getSession().getTable().getId() : "Take away")
                        .orderType(item.getOrder().getOrderType() != null ? 
                                  item.getOrder().getOrderType().toString() : "DINE_IN")
                        .build())
                .productInfo(DashboardOrderItemDTO.ProductInfo.builder()
                        .id(item.getProduct().getId())
                        .name(item.getProduct().getName())
                        .categoryId(item.getProduct().getCategory().getId())
                        .categoryName(item.getProduct().getCategory().getName())
                        .build())
                .build();
    }

    /**
     * Update item status with action (start, complete, undo)
     */
    @Transactional
    public void updateItemStatus(Long itemId, String action) {
        OrderItem item = orderItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Order item not found: " + itemId));

        OrderItem.OrderItemStatus newStatus = switch (action.toLowerCase()) {
            case "start" -> OrderItem.OrderItemStatus.PREPARING;
            case "complete" -> OrderItem.OrderItemStatus.SERVED;
            case "undo" -> OrderItem.OrderItemStatus.PENDING;
            default -> throw new RuntimeException("Invalid action: " + action);
        };

        item.setOrderItemStatus(newStatus);
        orderItemRepository.save(item);

        // Notify update
        Long orderId = item.getOrder().getId();
        Order updatedOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
        notifyOrderUpdate(updatedOrder, orderItems);
    }

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
