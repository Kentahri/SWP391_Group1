package com.group1.swp.pizzario_swp391.controller.manager;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.group1.swp.pizzario_swp391.annotation.ManagerUrl;
import com.group1.swp.pizzario_swp391.dto.category.CategoryResponseDTO;
import com.group1.swp.pizzario_swp391.dto.product.ProductCreateDTO;
import com.group1.swp.pizzario_swp391.dto.product.ProductResponseDTO;
import com.group1.swp.pizzario_swp391.dto.product.ProductUpdateDTO;
import com.group1.swp.pizzario_swp391.service.CategoryService;
import com.group1.swp.pizzario_swp391.service.ProductService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@ManagerUrl
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class ProductController{

    ProductService productService;

    CategoryService categoryService;

    @Autowired
    private Cloudinary cloudinary;

    // Global handler cho lỗi upload
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<String> handleMaxSizeException(MaxUploadSizeExceededException exc) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body("File quá lớn! Vui lòng chọn ảnh nhỏ hơn 5MB.");
    }

    private String saveUploadedFile(MultipartFile file) throws IOException {
        // Upload lên Cloudinary
        Map uploadResult = cloudinary.uploader()
                .upload(file.getBytes(),
                        ObjectUtils.asMap(
                                "folder", "pizzario/products",  // Thư mục trên cloud
                                "resource_type", "image",
                                "public_id", UUID.randomUUID().toString() + "_" + file.getOriginalFilename()
                        ));

        // Trả về URL từ cloud
        return (String) uploadResult.get("secure_url");  // https://res.cloudinary.com/.../pizzario/products/abc.jpg
    }

    @GetMapping("/products")
    public String list(Model model) {
        List<ProductResponseDTO> products = productService.getAllProducts();
        model.addAttribute("products", products);
        List<CategoryResponseDTO> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        model.addAttribute("productForm", new ProductCreateDTO()); // Object rỗng cho form
        return "admin_page/product/product-list";
    }

    @PostMapping("/products")
    public String searchProduct(@RequestParam("query") String query, Model model) {
        List<ProductResponseDTO> products = productService.searchProducts(query);
        model.addAttribute("products", products);
        model.addAttribute("query", query);
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
                .currentImageURL(productUpdateDTO.getCurrentImageURL())
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
    public String save(
            @Valid @ModelAttribute("productForm") ProductCreateDTO productForm,
            BindingResult bindingResult,
            Model model) {

        // === XỬ LÝ ẢNH ===
        String finalImageURL = null;

        // 1. Có file mới → upload
        if (productForm.getImageFile() != null && !productForm.getImageFile().isEmpty()) {
            try {
                finalImageURL = saveUploadedFile(productForm.getImageFile());
            } catch (Exception e) {
                bindingResult.rejectValue("imageFile", "error.imageFile",
                        "Lỗi upload ảnh: " + e.getMessage());
            }
        }
        // 2. Không có file mới + đang edit → giữ ảnh cũ
        else if (productForm.getId() != null && productForm.getCurrentImageURL() != null) {
            finalImageURL = productForm.getCurrentImageURL();
        }

        // Gán URL cuối cùng
        productForm.setImageURL(finalImageURL);

        // === KIỂM TRA LỖI VALIDATION ===
        if (bindingResult.hasErrors()) {
            model.addAttribute("products", productService.getAllProducts());
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("openModal", productForm.getId() == null ? "create" : "edit");
            model.addAttribute("hasErrors", true);
            return "admin_page/product/product-list";
        }

        // === LƯU VÀO DB ===
        try {
            if (productForm.getId() == null) {
                productService.createProduct(productForm);
            } else {
                ProductUpdateDTO updateDTO = ProductUpdateDTO.builder()
                        .name(productForm.getName())
                        .description(productForm.getDescription())
                        .imageURL(productForm.getImageURL())
                        .categoryId(productForm.getCategoryId())
                        .active(productForm.isActive())
                        .build();
                productService.updateProduct(productForm.getId(), updateDTO);
            }
            return "redirect:/manager/products";
        } catch (Exception e) {
            model.addAttribute("products", productService.getAllProducts());
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("openModal", productForm.getId() == null ? "create" : "edit");
            model.addAttribute("errorMessage", e.getMessage());
            return "admin_page/product/product-list";
        }
    }

    @PostMapping("/products/delete/{id}")
    public String delete(@PathVariable Long id) {
        productService.deleteProduct(id);
        return "redirect:/manager/products";
    }

    @PostMapping("/products/toggle/{id}")
    public String toggleActive(@PathVariable Long id) {
        // Manager toggle trạng thái active và broadcast qua WebSocket
        productService.toggleProductActive(id, "Manager");
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

}
