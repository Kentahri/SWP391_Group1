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
@RequestMapping("kitchen")
@RequiredArgsConstructor
public class KitchenController {
    private final ProductService productService;

    @GetMapping
    public String cashierHome(Model model) {
        List<ProductResponseDTO> products = productService.getAllProducts();
        model.addAttribute("products", products);
        return "kitchen-page/kitchen";
    }
    // Xóa hoặc comment route riêng cho outstock nếu có
    // @GetMapping("/outstock")
    // public String outStockProductPage(Model model) {
    //     model.addAttribute("products", productService.getAllProducts());
    //     return "kitchen-page/kitchen-outstock-management";
    // }
}
