package com.group1.swp.pizzario_swp391.controller.manager;

import java.util.List;

import com.group1.swp.pizzario_swp391.annotation.ManagerUrl;
import lombok.AccessLevel;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.group1.swp.pizzario_swp391.dto.category.CategoryResponseDTO;
import com.group1.swp.pizzario_swp391.dto.product.ProductCreateDTO;
import com.group1.swp.pizzario_swp391.dto.product.ProductResponseDTO;
import com.group1.swp.pizzario_swp391.dto.product.ProductUpdateDTO;
import com.group1.swp.pizzario_swp391.service.CategoryService;
import com.group1.swp.pizzario_swp391.service.ProductService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Controller
@ManagerUrl
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class ProductController {

    ProductService productService;

    CategoryService categoryService;

    @GetMapping("/products")
    public String list(Model model) {
        List<ProductResponseDTO> products = productService.getAllProducts();
        model.addAttribute("products", products);
        return "admin_page/product/product-list";
    }

    @GetMapping("/products/detail/{id}")
    public String detail(@PathVariable Long id, Model model) {
        ProductResponseDTO product = productService.getProductById(id);
        model.addAttribute("product", product);
        return "admin_page/product/detail";
    }

    @GetMapping("/products/create")
    public String createForm(Model model) {
        model.addAttribute("product", new ProductCreateDTO());
        List<CategoryResponseDTO> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        return "admin_page/product/product-create";
    }

    @PostMapping("/products/create")
    public String create(@Valid @ModelAttribute ProductCreateDTO productCreateDTO) {
        productService.createProduct(productCreateDTO);
        return "redirect:/manager/products";
    }

    @GetMapping("/products/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        ProductUpdateDTO productUpdateDTO = productService.getProductForUpdate(id);
        model.addAttribute("productDTO", productUpdateDTO);
        model.addAttribute("productId", id);
        List<CategoryResponseDTO> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        return "admin_page/product/product-edit";
    }

    @PostMapping("/products/edit/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute ProductUpdateDTO productUpdateDTO) {
        productService.updateProduct(id, productUpdateDTO);
        return "redirect:/manager/products";
    }

    @PostMapping("/products/delete/{id}")
    public String delete(@PathVariable Long id) {
        productService.deleteProduct(id);
        return "redirect:/manager/products";
    }
}
