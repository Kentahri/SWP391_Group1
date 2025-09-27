package com.group1.swp.pizzario_swp391.controller;

import com.group1.swp.pizzario_swp391.dto.ProductDTO;
import com.group1.swp.pizzario_swp391.entity.Product;
import com.group1.swp.pizzario_swp391.service.CategoryService;
import com.group1.swp.pizzario_swp391.service.ProductService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/product")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class ProductController {

    ProductService productService;

    CategoryService categoryService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        return "admin_page/product/product-list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("product", productService.getById(id).orElseThrow());
        return "admin_page/product/detail";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("product", new ProductDTO());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin_page/product/product-create";
    }

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute ProductDTO productDTO) {
        productService.createProduct(productDTO);
        return "redirect:/product";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Product product = productService.getById(id).orElseThrow(() -> new RuntimeException("Product not found"));;
        ProductDTO productDTO = ProductDTO.builder()
                .name(product.getName())
                .description(product.getDescription())
                .basePrice(product.getBasePrice())
                .flashSalePrice(product.getFlashSalePrice())
                .flashSaleStart(product.getFlashSaleStart())
                .flashSaleEnd(product.getFlashSaleEnd())
                .categoryId(product.getCategory().getId())
                .isActive(product.isActive())
                .imageURL(product.getImageURL())
                .build();
        model.addAttribute("productDTO", productDTO);
        model.addAttribute("productId", product.getId());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin_page/product/product-edit";
    }

    @PostMapping("/edit/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute ProductDTO productDTO) {
        productService.updateProduct(id, productDTO);
        return "redirect:/product";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        productService.deleteProduct(id);
        return "redirect:/product";
    }
}
