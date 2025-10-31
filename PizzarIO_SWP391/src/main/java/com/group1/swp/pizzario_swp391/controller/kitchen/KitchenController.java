package com.group1.swp.pizzario_swp391.controller.kitchen;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.group1.swp.pizzario_swp391.dto.category.CategoryResponseDTO;
import com.group1.swp.pizzario_swp391.dto.kitchen.DashboardOrderItemDTO;
import com.group1.swp.pizzario_swp391.dto.kitchen.KitchenOrderDTO;
import com.group1.swp.pizzario_swp391.dto.order.OrderItemDTO;
import com.group1.swp.pizzario_swp391.dto.product.ProductResponseDTO;
import com.group1.swp.pizzario_swp391.service.CategoryService;
import com.group1.swp.pizzario_swp391.service.KitchenService;
import com.group1.swp.pizzario_swp391.service.OrderItemService;
import com.group1.swp.pizzario_swp391.service.OrderService;
import com.group1.swp.pizzario_swp391.service.ProductService;

@Controller
@RequestMapping("/kitchen")
public class KitchenController {
    private final ProductService productService;
    private final OrderService orderService;
    private final OrderItemService orderItemService;
    private final KitchenService kitchenService;
    private final CategoryService categoryService;

    public KitchenController(ProductService productService, OrderService orderService, 
                           OrderItemService orderItemService, KitchenService kitchenService,
                           CategoryService categoryService) {
        this.productService = productService;
        this.orderService = orderService;
        this.orderItemService = orderItemService;
        this.kitchenService = kitchenService;
        this.categoryService = categoryService;
    }

    @GetMapping({"", "/"})
    public String defaultRedirect() {
        // Điều hướng sang trang dashboard mặc định
        return "redirect:/kitchen/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        try {
            // Load dashboard data
            List<DashboardOrderItemDTO> orderItems = kitchenService.getDashboardOrderItems();
            List<CategoryResponseDTO> categories = categoryService.getAllActiveCategories();
            
            // Calculate statistics
            int totalDishes = orderItems.size();
            int newDishes = (int) orderItems.stream().filter(item -> "PENDING".equals(item.getStatus())).count();
            int preparingDishes = (int) orderItems.stream().filter(item -> "PREPARING".equals(item.getStatus())).count();
            int completedDishes = (int) orderItems.stream().filter(item -> "SERVED".equals(item.getStatus())).count();
            
            // Group items by category - ensure all categories have empty list if no items
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
                    int statusDiff = statusOrder.getOrDefault(a.getStatus(), 3) - statusOrder.getOrDefault(b.getStatus(), 3);
                    if (statusDiff != 0) return statusDiff;
                    
                    // Then sort by creation time (newest first)
                    int timeDiff = b.getCreatedAt().compareTo(a.getCreatedAt());
                    if (timeDiff != 0) return timeDiff;
                    
                    // Finally sort by product name
                    return a.getProductName().compareTo(b.getProductName());
                });
            });
            
            // Add attributes to model
            model.addAttribute("staffName", "Kitchen Staff");
            model.addAttribute("orderItems", orderItems);
            model.addAttribute("categories", categories);
            model.addAttribute("groupedItems", groupedItems);
            model.addAttribute("totalDishes", totalDishes);
            model.addAttribute("newDishes", newDishes);
            model.addAttribute("preparingDishes", preparingDishes);
            model.addAttribute("completedDishes", completedDishes);
            model.addAttribute("lastUpdateTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            
        } catch (Exception e) {
            // Handle error gracefully
            model.addAttribute("staffName", "Kitchen Staff");
            model.addAttribute("orderItems", new ArrayList<>());
            model.addAttribute("categories", new ArrayList<>());
            model.addAttribute("groupedItems", new HashMap<>());
            model.addAttribute("totalDishes", 0);
            model.addAttribute("newDishes", 0);
            model.addAttribute("preparingDishes", 0);
            model.addAttribute("completedDishes", 0);
            model.addAttribute("lastUpdateTime", "--:--:--");
        }
        
        return "kitchen-page/kitchen-dashboard";
    }

    @GetMapping("/order-list")
    public String orderList(@RequestParam(value = "status", required = false) String status, @RequestParam(value = "type", required = false) String type, Model model) {
        // Danh sách theo filter đang chọn
        List<KitchenOrderDTO> orders = orderService.getKitchenOrdersByFilter(status, type);
        // Đếm số lượng từng loại để hiển thị trên từng tab
        int processingCount = orderService.getKitchenOrdersByFilter(null, null).size();
        int dineInCount = orderService.getKitchenOrdersByFilter(null, "DINE_IN").size();
        int takeAwayCount = orderService.getKitchenOrdersByFilter(null, "TAKE_AWAY").size();
        int completedCount = orderService.getKitchenOrdersByFilter("COMPLETED", null).size();
        model.addAttribute("orders", orders);
        model.addAttribute("status", status);
        model.addAttribute("type", type);
        model.addAttribute("processingCount", processingCount);
        model.addAttribute("dineInCount", dineInCount);
        model.addAttribute("takeAwayCount", takeAwayCount);
        model.addAttribute("completedCount", completedCount);
        return "kitchen-page/kitchen-order-management";
    }

    @GetMapping("/outstock")
    public String outStockProductPage(Model model) {
        List<ProductResponseDTO> products = productService.getAllProducts();
        model.addAttribute("products", products);
        return "kitchen-page/kitchen-outstock-management";
    }

    @GetMapping("/order/{orderId}")
    public String viewOrderDetail(@PathVariable Long orderId, Model model) {
        KitchenOrderDTO order = orderService.getKitchenOrderById(orderId);
        List<OrderItemDTO> items = orderItemService.getOrderItemsByOrderId(orderId);
        model.addAttribute("order", order);
        model.addAttribute("items", items);
        return "kitchen-page/kitchen-order-detail";
    }

    /**
     * Update order item status and redirect back to dashboard
     */
    @PostMapping("/update-item-status")
    public String updateItemStatus(@RequestParam Long itemId, @RequestParam String action) {
        try {
            kitchenService.updateItemStatus(itemId, action);
        } catch (Exception e) {
            // Log error but don't break the flow
            System.err.println("Error updating item status: " + e.getMessage());
        }
        return "redirect:/kitchen/dashboard";
    }

}
