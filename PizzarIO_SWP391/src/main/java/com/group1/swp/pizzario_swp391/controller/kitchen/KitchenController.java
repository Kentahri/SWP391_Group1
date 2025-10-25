package com.group1.swp.pizzario_swp391.controller.kitchen;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.group1.swp.pizzario_swp391.dto.kitchen.KitchenOrderDTO;
import com.group1.swp.pizzario_swp391.dto.order.OrderItemDTO;
import com.group1.swp.pizzario_swp391.dto.product.ProductResponseDTO;
import com.group1.swp.pizzario_swp391.service.OrderItemService;
import com.group1.swp.pizzario_swp391.service.OrderService;
import com.group1.swp.pizzario_swp391.service.ProductService;

@Controller
@RequestMapping("/kitchen")
public class KitchenController {
    private final ProductService productService;
    private final OrderService orderService;
    private final OrderItemService orderItemService;

    public KitchenController(ProductService productService, OrderService orderService, OrderItemService orderItemService) {
        this.productService = productService;
        this.orderService = orderService;
        this.orderItemService = orderItemService;
    }

    @GetMapping({"", "/"})
    public String defaultRedirect() {
        // Điều hướng sang trang order-list mặc định
        return "redirect:/kitchen/order-list";
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
}

// Không cần API controller vì sử dụng Thymeleaf render
