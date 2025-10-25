package com.group1.swp.pizzario_swp391.dto.websocket;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WebSocket message for kitchen order updates
 * Sent to kitchen when new orders are created or order status changes
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KitchenOrderMessage {
    
    private MessageType type;
    private Long orderId;
    private String code;
    private String tableName;
    private String orderType; // DINE_IN, TAKEAWAY
    private String status;
    private String priority; // NORMAL, PRIORITY
    private int totalItems;
    private int completedItems;
    private double totalPrice;
    private String note;
    private LocalDateTime timestamp;
    private String message;
    private List<OrderItemInfo> items;
    
    public enum MessageType {
        NEW_ORDER,          // New order from guest
        ORDER_UPDATED,       // Order status or items updated
        ORDER_CANCELLED,     // Order cancelled
        ORDER_COMPLETED,     // Order completed
        ITEM_STATUS_CHANGED  // Individual item status changed
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemInfo {
        private Long itemId;
        private String productName;
        private int quantity;
        private String status; // PENDING, PREPARING, READY, SERVED
        private String note;
        private double price;
    }
}
