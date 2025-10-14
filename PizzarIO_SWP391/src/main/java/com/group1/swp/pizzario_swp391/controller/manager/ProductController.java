package com.group1.swp.pizzario_swp391.controller.manager;

import java.util.List;
import java.util.stream.Collectors;

import com.group1.swp.pizzario_swp391.annotation.ManagerUrl;
import lombok.AccessLevel;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
        List<CategoryResponseDTO> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        model.addAttribute("productForm", new ProductCreateDTO()); // Object rỗng cho form
        return "admin_page/product/product-list";
    }

    @GetMapping("/products/new")
    public String newProduct(Model model) {
        List<ProductResponseDTO> products = productService.getAllProducts();
        model.addAttribute("products", products);
        List<CategoryResponseDTO> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        model.addAttribute("productForm", new ProductCreateDTO());
        model.addAttribute("openModal", "create"); // Flag để mở modal
        return "admin_page/product/product-list";
    }

    @GetMapping("/products/detail/{id}")
    public String detail(@PathVariable Long id, Model model) {
        ProductResponseDTO product = productService.getProductById(id);
        model.addAttribute("product", product);
        return "admin_page/product/detail";
    }

    @GetMapping("/products/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        // Lấy dữ liệu product và convert sang ProductCreateDTO để dùng chung form
        ProductUpdateDTO productUpdateDTO = productService.getProductForUpdate(id);

        // Convert sang ProductCreateDTO (có field id)
        ProductCreateDTO productForm = ProductCreateDTO.builder()
                .id(id)
                .name(productUpdateDTO.getName())
                .description(productUpdateDTO.getDescription())
                .imageURL(productUpdateDTO.getImageURL())
                .basePrice(productUpdateDTO.getBasePrice())
                .flashSalePrice(productUpdateDTO.getFlashSalePrice())
                .flashSaleStart(productUpdateDTO.getFlashSaleStart())
                .flashSaleEnd(productUpdateDTO.getFlashSaleEnd())
                .categoryId(productUpdateDTO.getCategoryId())
                .active(productUpdateDTO.isActive())
                .build();

        List<ProductResponseDTO> products = productService.getAllProducts();
        model.addAttribute("products", products);
        List<CategoryResponseDTO> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        model.addAttribute("productForm", productForm);
        model.addAttribute("openModal", "edit"); // Flag để mở modal
        return "admin_page/product/product-list";
    }

    @PostMapping("/products/save")
    public String save(@Valid @ModelAttribute("productForm") ProductCreateDTO productForm) {
        if (productForm.getId() == null) {
            // Create new product
            productService.createProduct(productForm);
        } else {
            // Update existing product
            ProductUpdateDTO updateDTO = ProductUpdateDTO.builder()
                    .name(productForm.getName())
                    .description(productForm.getDescription())
                    .imageURL(productForm.getImageURL())
                    .basePrice(productForm.getBasePrice())
                    .flashSalePrice(productForm.getFlashSalePrice())
                    .flashSaleStart(productForm.getFlashSaleStart())
                    .flashSaleEnd(productForm.getFlashSaleEnd())
                    .categoryId(productForm.getCategoryId())
                    .active(productForm.isActive())
                    .build();
            productService.updateProduct(productForm.getId(), updateDTO);
        }
        return "redirect:/manager/products";
    }

    @PostMapping("/products/delete/{id}")
    public String delete(@PathVariable Long id) {
        productService.deleteProduct(id);
        return "redirect:/manager/products";
    }

    @PostMapping("/products/toggle/{id}")
    public String toggleActive(@PathVariable Long id) {
        productService.toggleProductActive(id);
        return "redirect:/manager/products";
    }

    @GetMapping("/products/search")
    @ResponseBody
    public List<ProductResponseDTO> searchProducts(@RequestParam String query) {
        List<ProductResponseDTO> allProducts = productService.getAllProducts();

        if (query == null || query.trim().isEmpty()) {
            return allProducts;
        }

        String queryLower = query.toLowerCase();

        return allProducts.stream()
                .filter(p -> p.getName().toLowerCase().contains(queryLower) ||
                        (p.getDescription() != null && p.getDescription().toLowerCase().contains(queryLower)) ||
                        p.getCategoryName().toLowerCase().contains(queryLower))
                .collect(Collectors.toList());
    }

    @PostMapping("/active/{id}")
    public String updateActive(@PathVariable Long id, Boolean active) {
        productService.updateProductActive(id, active != null ? active : false);
        return "redirect:/kitchen";
    }
}
