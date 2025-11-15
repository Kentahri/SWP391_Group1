package com.group1.swp.pizzario_swp391.controller.manager;

import com.group1.swp.pizzario_swp391.annotation.ManagerUrl;
import com.group1.swp.pizzario_swp391.dto.productsize.ProductSizeCreateDTO;
import com.group1.swp.pizzario_swp391.dto.productsize.ProductSizeResponseDTO;
import com.group1.swp.pizzario_swp391.dto.productsize.ProductSizeUpdateDTO;
import com.group1.swp.pizzario_swp391.entity.ProductSize;
import com.group1.swp.pizzario_swp391.repository.ProductSizeRepository;
import com.group1.swp.pizzario_swp391.service.ProductService;
import com.group1.swp.pizzario_swp391.service.ProductSizeService;
import com.group1.swp.pizzario_swp391.service.SizeService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@ManagerUrl
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class ProductSizeController{

    ProductSizeService productSizeService;
    ProductService productService;
    SizeService sizeService;
    private final ProductSizeRepository productSizeRepository;

    @GetMapping("/product-sizes")
    public String list(Model model) {
        List<ProductSizeResponseDTO> productSizes = productSizeService.getAllProductSizes();
        model.addAttribute("productSizes", productSizes);
        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("sizes", sizeService.getAllSizesForSelect());
        model.addAttribute("productSizeForm", new ProductSizeCreateDTO());
        return "admin_page/productsize/productsize-list";
    }

    @PostMapping("/product-sizes")
    public String searchProductSize(@RequestParam("query") String query, Model model) {
        List<ProductSizeResponseDTO> productSizes = productSizeService.searchProductSizes(query);
        model.addAttribute("productSizes", productSizes);
        model.addAttribute("query", query);
        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("sizes", sizeService.getAllSizesForSelect());
        model.addAttribute("productSizeForm", new ProductSizeCreateDTO());
        return "admin_page/productsize/productsize-list";
    }

    @GetMapping("/product-sizes/new")
    public String newProductSize(Model model) {
        List<ProductSizeResponseDTO> productSizes = productSizeService.getAllProductSizes();
        model.addAttribute("productSizes", productSizes);
        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("sizes", sizeService.getAllSizesForSelect());
        model.addAttribute("productSizeForm", new ProductSizeCreateDTO());
        model.addAttribute("openModal", "create");
        return "admin_page/productsize/productsize-list";
    }

    @GetMapping("/product-sizes/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        ProductSizeUpdateDTO updateDTO = productSizeService.getProductSizeForUpdate(id);

        ProductSizeCreateDTO form = ProductSizeCreateDTO.builder()
                .id(id)
                .productId(updateDTO.getProductId())
                .sizeId(updateDTO.getSizeId())
                .basePrice(updateDTO.getBasePrice())
                .flashSalePrice(updateDTO.getFlashSalePrice())
                .flashSaleStart(updateDTO.getFlashSaleStart())
                .flashSaleEnd(updateDTO.getFlashSaleEnd())
                .build();

        List<ProductSizeResponseDTO> productSizes = productSizeService.getAllProductSizes();
        model.addAttribute("productSizes", productSizes);
        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("sizes", sizeService.getAllSizesForSelect());
        model.addAttribute("productSizeForm", form);
        model.addAttribute("openModal", "edit");
        return "admin_page/productsize/productsize-list";
    }

    @PostMapping("/product-sizes/save")
    public String save(@Valid @ModelAttribute("productSizeForm") ProductSizeCreateDTO form,
                       BindingResult bindingResult,
                       Model model) {

        // Kiểm tra flash sale end > start
        if (form.getFlashSaleStart() != null && form.getFlashSaleEnd() != null) {
            if (form.getFlashSaleEnd().isBefore(form.getFlashSaleStart())) {
                bindingResult.rejectValue("flashSaleEnd", "error.flashSaleEnd",
                        "Ngày kết thúc phải lớn hơn ngày bắt đầu");
            }
        }

        if (bindingResult.hasErrors()) {
            List<ProductSizeResponseDTO> productSizes = productSizeService.getAllProductSizes();
            model.addAttribute("productSizes", productSizes);
            model.addAttribute("products", productService.getAllProducts());
            model.addAttribute("sizes", sizeService.getAllSizesForSelect());
            model.addAttribute("openModal", form.getId() == null ? "create" : "edit");
            model.addAttribute("hasErrors", true);
            return "admin_page/productsize/productsize-list";
        }

        try {
            if (form.getId() == null) {
                productSizeService.createProductSize(form);
            } else {
                ProductSize productSize = productSizeRepository.findByProductIdAndSizeId(form.getProductId(), form.getSizeId()).orElse(null);
                if (productSize == null) {
                    List<ProductSizeResponseDTO> productSizes = productSizeService.getAllProductSizes();
                    // Lấy tên món và size để hiển thị lỗi thân thiện
                    String productName = productService.getProductById(form.getProductId()).getName();
                    String sizeName = sizeService.getSizeById(form.getSizeId()).getSizeName();
                    model.addAttribute("productSizes", productSizes);
                    model.addAttribute("products", productService.getAllProducts());
                    model.addAttribute("sizes", sizeService.getAllSizesForSelect());
                    model.addAttribute("openModal", form.getId() == null ? "create" : "edit");
                    model.addAttribute("errorMessage", String.format(
                            "Không tìm thấy giá cho món \"%s\" với kích thước \"%s\"",
                            productName, sizeName
                    ));
                    return "admin_page/productsize/productsize-list";
                }
                ProductSizeUpdateDTO updateDTO = ProductSizeUpdateDTO.builder()
                        .productId(form.getProductId())
                        .sizeId(form.getSizeId())
                        .basePrice(form.getBasePrice())
                        .flashSalePrice(form.getFlashSalePrice())
                        .flashSaleStart(form.getFlashSaleStart())
                        .flashSaleEnd(form.getFlashSaleEnd())
                        .build();
                productSizeService.updateProductSize(productSize.getId(), updateDTO);
            }
            return "redirect:/manager/product-sizes";
        } catch (Exception e) {
            List<ProductSizeResponseDTO> productSizes = productSizeService.getAllProductSizes();
            model.addAttribute("productSizes", productSizes);
            model.addAttribute("products", productService.getAllProducts());
            model.addAttribute("sizes", sizeService.getAllSizesForSelect());
            model.addAttribute("openModal", form.getId() == null ? "create" : "edit");
            model.addAttribute("errorMessage", e.getMessage());
            return "admin_page/productsize/productsize-list";
        }
    }

    @GetMapping("/product-sizes/search")
    @ResponseBody
    public List<ProductSizeResponseDTO> searchProductSizes(@RequestParam String query) {
        List<ProductSizeResponseDTO> all = productSizeService.getAllProductSizes();

        if (query == null || query.trim().isEmpty()) {
            return all;
        }

        String q = query.toLowerCase();
        return all.stream()
                .filter(ps -> ps.getProductName().toLowerCase().contains(q) ||
                        ps.getSizeName().toLowerCase().contains(q) ||
                        String.valueOf(ps.getBasePrice()).contains(q))
                .toList();
    }
}