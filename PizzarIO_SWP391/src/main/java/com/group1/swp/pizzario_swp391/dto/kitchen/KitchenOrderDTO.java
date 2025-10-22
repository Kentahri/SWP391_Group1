package com.group1.swp.pizzario_swp391.dto.kitchen;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import com.group1.swp.pizzario_swp391.entity.Order;

public class KitchenOrderDTO {
    private Long id;
    private String code;
    private String status;
    private String priority;
    private String tableName;
    private String type;
    private String timeAgo;
    private Integer totalItems;
    private Integer completedItems;
    private Integer processPercent;
    private Double totalPrice;
    private String note;
    private LocalDateTime createdAt;

    // Constructors
    public KitchenOrderDTO() {}

    public KitchenOrderDTO(Long id, String code, String status, String priority, String tableName, 
                          String type, String timeAgo, Integer totalItems, Integer completedItems, 
                          Integer processPercent, Double totalPrice, String note, LocalDateTime createdAt) {
        this.id = id;
        this.code = code;
        this.status = status;
        this.priority = priority;
        this.tableName = tableName;
        this.type = type;
        this.timeAgo = timeAgo;
        this.totalItems = totalItems;
        this.completedItems = completedItems;
        this.processPercent = processPercent;
        this.totalPrice = totalPrice;
        this.note = note;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTimeAgo() { return timeAgo; }
    public void setTimeAgo(String timeAgo) { this.timeAgo = timeAgo; }

    public Integer getTotalItems() { return totalItems; }
    public void setTotalItems(Integer totalItems) { this.totalItems = totalItems; }

    public Integer getCompletedItems() { return completedItems; }
    public void setCompletedItems(Integer completedItems) { this.completedItems = completedItems; }

    public Integer getProcessPercent() { return processPercent; }
    public void setProcessPercent(Integer processPercent) { this.processPercent = processPercent; }

    public Double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(Double totalPrice) { this.totalPrice = totalPrice; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static KitchenOrderDTO fromOrder(Order order) {
        // Generate order code
        String orderCode = "ORD-" + String.format("%06d", order.getId());
        
        // Calculate time ago
        String timeAgo = calculateTimeAgo(order.getCreatedAt());
        
        // Get table name
        String tableName = null;
        if (order.getSession() != null && order.getSession().getTable() != null) {
            tableName = "Bàn " + order.getSession().getTable().getId();
        }
        
        // Get order type display
        String typeDisplay = order.getOrderType() == Order.OrderType.DINE_IN ? "Tại bàn" : "Take away";
        
        // Calculate progress
        int totalItems = order.getOrderItems() != null ? order.getOrderItems().size() : 0;
        int completedItems = 0;
        if (order.getOrderItems() != null) {
            completedItems = (int) order.getOrderItems().stream()
                    .filter(item -> item.getOrderItemStatus() == com.group1.swp.pizzario_swp391.entity.OrderItem.OrderItemStatus.SERVED)
                    .count();
        }
        int processPercent = totalItems > 0 ? (completedItems * 100) / totalItems : 0;
        
        return new KitchenOrderDTO(
                order.getId(),
                orderCode,
                order.getOrderStatus().name(),
                "NORMAL", // Default priority, can be enhanced later
                tableName,
                typeDisplay,
                timeAgo,
                totalItems,
                completedItems,
                processPercent,
                order.getTotalPrice(),
                order.getNote(),
                order.getCreatedAt()
        );
    }
    
    private static String calculateTimeAgo(LocalDateTime createdAt) {
        if (createdAt == null) return "Không xác định";
        
        LocalDateTime now = LocalDateTime.now();
        long minutes = ChronoUnit.MINUTES.between(createdAt, now);
        
        if (minutes < 1) {
            return "Vừa xong";
        } else if (minutes < 60) {
            return minutes + " phút trước";
        } else {
            long hours = ChronoUnit.HOURS.between(createdAt, now);
            return hours + " giờ trước";
        }
    }
}
