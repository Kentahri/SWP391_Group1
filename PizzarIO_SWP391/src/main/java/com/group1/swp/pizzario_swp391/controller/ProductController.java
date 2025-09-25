package com.group1.swp.pizzario_swp391.controller;

import com.group1.swp.pizzario_swp391.dto.CreateProductRequest;
import com.group1.swp.pizzario_swp391.dto.ProductResponse;
import com.group1.swp.pizzario_swp391.dto.UpdateProductRequest;
import com.group1.swp.pizzario_swp391.service.CategoryService;
import com.group1.swp.pizzario_swp391.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("products", productService.getAll());
        return "product/product-list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("product", productService.getById(id));
        return "product/detail";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("product", new CreateProductRequest());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "product/product-create";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute CreateProductRequest request) {
        ProductResponse created = productService.create(request);
        return "redirect:/products";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("product", productService.getById(id));
        model.addAttribute("categories", categoryService.getAllCategories());
        return "product/product-edit";
    }

    @PostMapping("/edit/{id}")
    public String update(@PathVariable Long id, @ModelAttribute UpdateProductRequest request) {
        ProductResponse updated = productService.update(id, request);
        return "redirect:/products";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        productService.delete(id);
        return "redirect:/products";
    }
}
