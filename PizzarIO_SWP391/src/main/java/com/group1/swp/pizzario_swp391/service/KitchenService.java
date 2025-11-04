package com.group1.swp.pizzario_swp391.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.group1.swp.pizzario_swp391.dto.category.CategoryResponseDTO;
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

    @Autowired
    private OrderService orderService;
    
    @Autowired
    private CategoryService categoryService;

    /**
     * Get dashboard order items for kitchen dashboard
     */
    public List<DashboardOrderItemDTO> getDashboardOrderItems() {
        List<OrderItem> orderItems = orderItemRepository.findAllWithRelations().stream()
                .filter(item -> item.getOrder() != null && 
                               item.getOrder().getOrderStatus() != Order.OrderStatus.COMPLETED &&
                               item.getOrder().getOrderStatus() != Order.OrderStatus.CANCELLED)
                .collect(Collectors.toList());

        return orderItems.stream()
                .map(this::convertToDashboardDTO)
                .collect(Collectors.toList());
    }

    /**
     * Calculate dashboard statistics
     */
    public Map<String, Integer> getDashboardStatistics(List<DashboardOrderItemDTO> orderItems) {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("totalDishes", orderItems.size());
        stats.put("newDishes", (int) orderItems.stream()
                .filter(item -> "PENDING".equals(item.getStatus()))
                .count());
        stats.put("preparingDishes", (int) orderItems.stream()
                .filter(item -> "PREPARING".equals(item.getStatus()))
                .count());
        stats.put("completedDishes", (int) orderItems.stream()
                .filter(item -> "SERVED".equals(item.getStatus()))
                .count());
        return stats;
    }

    /**
     * Group and sort items by category
     */
    public Map<String, List<DashboardOrderItemDTO>> groupAndSortItemsByCategory(
            List<DashboardOrderItemDTO> orderItems, 
            List<CategoryResponseDTO> categories) {
        Map<String, List<DashboardOrderItemDTO>> groupedItems = new HashMap<>();
        
        // Initialize all categories with empty lists
        categories.forEach(category -> {
            groupedItems.put(category.getName(), new ArrayList<>());
        });
        
        // Add actual items to their categories
        orderItems.forEach(item -> {
            String categoryName = item.getCategoryName();
            groupedItems.computeIfAbsent(categoryName, _ -> new ArrayList<>()).add(item);
        });
        
        // Sort items within each category
        groupedItems.forEach((_, items) -> {
            items.sort((a, b) -> {
                // First sort by status (PENDING -> PREPARING -> SERVED)
                Map<String, Integer> statusOrder = Map.of("PENDING", 0, "PREPARING", 1, "SERVED", 2);
                int statusDiff = statusOrder.getOrDefault(a.getStatus(), 3) - 
                                statusOrder.getOrDefault(b.getStatus(), 3);
                if (statusDiff != 0) return statusDiff;
                
                // Then sort by creation time (newest first)
                int timeDiff = b.getCreatedAt().compareTo(a.getCreatedAt());
                if (timeDiff != 0) return timeDiff;
                
                // Finally sort by product name
                return a.getProductName().compareTo(b.getProductName());
            });
        });
        
        return groupedItems;
    }

    /**
     * Get complete dashboard data
     */
    public Map<String, Object> getDashboardData() {
        Map<String, Object> dashboardData = new HashMap<>();
        
        try {
            List<DashboardOrderItemDTO> orderItems = getDashboardOrderItems();
            List<CategoryResponseDTO> categories = categoryService.getAllActiveCategories();
            Map<String, Integer> statistics = getDashboardStatistics(orderItems);
            Map<String, List<DashboardOrderItemDTO>> groupedItems = 
                    groupAndSortItemsByCategory(orderItems, categories);
            
            dashboardData.put("orderItems", orderItems);
            dashboardData.put("categories", categories);
            dashboardData.put("groupedItems", groupedItems);
            dashboardData.put("totalDishes", statistics.get("totalDishes"));
            dashboardData.put("newDishes", statistics.get("newDishes"));
            dashboardData.put("preparingDishes", statistics.get("preparingDishes"));
            dashboardData.put("completedDishes", statistics.get("completedDishes"));
        } catch (Exception e) {
            // Return empty data on error
            dashboardData.put("orderItems", new ArrayList<>());
            dashboardData.put("categories", new ArrayList<>());
            dashboardData.put("groupedItems", new HashMap<>());
            dashboardData.put("totalDishes", 0);
            dashboardData.put("newDishes", 0);
            dashboardData.put("preparingDishes", 0);
            dashboardData.put("completedDishes", 0);
        }
        
        return dashboardData;
    }

    /**
     * Get order counts by filter for order list page
     */
    public Map<String, Integer> getOrderCountsByFilter() {
        Map<String, Integer> counts = new HashMap<>();
        counts.put("processingCount", orderService.getKitchenOrdersByFilter(null, null).size());
        counts.put("dineInCount", orderService.getKitchenOrdersByFilter(null, "DINE_IN").size());
        counts.put("takeAwayCount", orderService.getKitchenOrdersByFilter(null, "TAKE_AWAY").size());
        counts.put("completedCount", orderService.getKitchenOrdersByFilter("COMPLETED", null).size());
        return counts;
    }

    /**
     * Convert OrderItem to DashboardOrderItemDTO
     */
    private DashboardOrderItemDTO convertToDashboardDTO(OrderItem item) {
        return DashboardOrderItemDTO.builder()
                .id(item.getId())
                .productName(item.getProductSize().getProduct().getName())
                .sizeName(item.getProductSize() != null && item.getProductSize().getSize() != null ? 
                         item.getProductSize().getSize().getSizeName() : null)
                .categoryId(item.getProductSize().getProduct().getCategory().getId())
                .categoryName(item.getProductSize().getProduct().getCategory().getName())
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
                        .id(item.getProductSize().getProduct().getId())
                        .name(item.getProductSize().getProduct().getName())
                        .categoryId(item.getProductSize().getProduct().getCategory().getId())
                        .categoryName(item.getProductSize().getProduct().getCategory().getName())
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
