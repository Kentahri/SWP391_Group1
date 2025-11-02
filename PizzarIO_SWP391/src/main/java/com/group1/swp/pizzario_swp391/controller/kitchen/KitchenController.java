package com.group1.swp.pizzario_swp391.controller.kitchen;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.group1.swp.pizzario_swp391.dto.kitchen.KitchenOrderDTO;
import com.group1.swp.pizzario_swp391.dto.order.OrderItemDTO;
import com.group1.swp.pizzario_swp391.dto.product.ProductResponseDTO;
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

    public KitchenController(ProductService productService, OrderService orderService, 
                           OrderItemService orderItemService, KitchenService kitchenService) {
        this.productService = productService;
        this.orderService = orderService;
        this.orderItemService = orderItemService;
        this.kitchenService = kitchenService;
    }

    @GetMapping({"", "/"})
    public String defaultRedirect() {
        // Điều hướng sang trang dashboard mặc định
        return "redirect:/kitchen/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Get dashboard data from service
        Map<String, Object> dashboardData = kitchenService.getDashboardData();
                    
        // Add all data to model
            model.addAttribute("staffName", "Kitchen Staff");
        model.addAttribute("orderItems", dashboardData.get("orderItems"));
        model.addAttribute("categories", dashboardData.get("categories"));
        model.addAttribute("groupedItems", dashboardData.get("groupedItems"));
        model.addAttribute("totalDishes", dashboardData.get("totalDishes"));
        model.addAttribute("newDishes", dashboardData.get("newDishes"));
        model.addAttribute("preparingDishes", dashboardData.get("preparingDishes"));
        model.addAttribute("completedDishes", dashboardData.get("completedDishes"));
            model.addAttribute("lastUpdateTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            
        return "kitchen-page/kitchen-dashboard";
    }

    @GetMapping("/order-list")
    public String orderList(@RequestParam(value = "status", required = false) String status, 
                           @RequestParam(value = "type", required = false) String type, 
                           Model model) {
        // Get filtered orders
        List<KitchenOrderDTO> orders = orderService.getKitchenOrdersByFilter(status, type);
        
        // Get order counts from service
        Map<String, Integer> counts = kitchenService.getOrderCountsByFilter();
        
        model.addAttribute("orders", orders);
        model.addAttribute("status", status);
        model.addAttribute("type", type);
        model.addAttribute("processingCount", counts.get("processingCount"));
        model.addAttribute("dineInCount", counts.get("dineInCount"));
        model.addAttribute("takeAwayCount", counts.get("takeAwayCount"));
        model.addAttribute("completedCount", counts.get("completedCount"));
        
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
            kitchenService.updateItemStatus(itemId, action);
        return "redirect:/kitchen/dashboard";
    }

}
