package com.group1.swp.pizzario_swp391.controller.kitchen;

import com.group1.swp.pizzario_swp391.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/product")
@RequiredArgsConstructor
public class KitchenProductController {
    
    private final ProductService productService;
    
    @PostMapping("/active/{id}")
    public String updateActive(@PathVariable Long id, Boolean active) {
        // Kitchen cập nhật trạng thái active và broadcast qua WebSocket
        productService.updateProductActive(id, active != null ? active : false, "Kitchen");
        return "redirect:/kitchen/outstock";
    }
}
