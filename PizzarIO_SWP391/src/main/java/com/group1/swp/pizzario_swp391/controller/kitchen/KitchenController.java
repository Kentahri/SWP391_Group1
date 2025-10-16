package com.group1.swp.pizzario_swp391.controller.kitchen;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.group1.swp.pizzario_swp391.service.ProductService;
import lombok.RequiredArgsConstructor;
import java.util.List;
import com.group1.swp.pizzario_swp391.dto.product.ProductResponseDTO;

@Controller
@RequestMapping("/kitchen")
@RequiredArgsConstructor
public class KitchenController {
    private final ProductService productService;

    @GetMapping({"", "/"})
    public String defaultRedirect() {
        // Điều hướng sang trang order-list mặc định
        return "redirect:/kitchen/order-list";
    }

    @GetMapping("/order-list")
    public String orderList(Model model) {
        // Nếu cần truyền thêm data cho order-list, add vào đây
        return "kitchen-page/kitchen-order-management";
    }

    @GetMapping("/outstock")
    public String outStockProductPage(Model model) {
        List<ProductResponseDTO> products = productService.getAllProducts();
        model.addAttribute("products", products);
        return "kitchen-page/kitchen-outstock-management";
    }
}
