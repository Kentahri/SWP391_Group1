package com.group1.swp.pizzario_swp391.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import org.springframework.stereotype.Service;

import com.group1.swp.pizzario_swp391.dto.product.ProductCreateDTO;
import com.group1.swp.pizzario_swp391.dto.product.ProductResponseDTO;
import com.group1.swp.pizzario_swp391.dto.product.ProductUpdateDTO;
import com.group1.swp.pizzario_swp391.entity.Category;
import com.group1.swp.pizzario_swp391.entity.Product;
import com.group1.swp.pizzario_swp391.mapper.ProductResponseMapper;
import com.group1.swp.pizzario_swp391.repository.CategoryRepository;
import com.group1.swp.pizzario_swp391.repository.ProductRepository;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class ProductService {
    ProductRepository productRepository;
    ProductResponseMapper productMapper;
    CategoryRepository categoryRepository;

    static final String PRODUCT_NOT_FOUND = "Product not found";
    static final String CATEGORY_NOT_FOUND = "Category not found";

    public List<ProductResponseDTO> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return products.stream()
                .map(productMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public ProductResponseDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(PRODUCT_NOT_FOUND));
        return productMapper.toResponseDTO(product);
    }

    public ProductUpdateDTO getProductForUpdate(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(PRODUCT_NOT_FOUND));
        return ProductUpdateDTO.builder()
                .name(product.getName())
                .description(product.getDescription())
                .imageURL(product.getImageURL())
                .basePrice(product.getBasePrice())
                .flashSalePrice(product.getFlashSalePrice())
                .flashSaleStart(product.getFlashSaleStart())
                .flashSaleEnd(product.getFlashSaleEnd())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .active(product.isActive())
                .build();
    }

    public void createProduct(ProductCreateDTO createDTO) {
        Product product = productMapper.toEntity(createDTO);
        LocalDateTime now = LocalDateTime.now();
        product.setCreatedAt(now);
        product.setUpdatedAt(now);

        // Set category
        Category category = categoryRepository.findById(createDTO.getCategoryId())
                .orElseThrow(() -> new RuntimeException(CATEGORY_NOT_FOUND + " with ID: " + createDTO.getCategoryId()));
        product.setCategory(category);

        productRepository.save(product);
    }

    public void updateProduct(Long id, ProductUpdateDTO updateDTO) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(PRODUCT_NOT_FOUND));

        productMapper.updateEntity(product, updateDTO);
        product.setUpdatedAt(LocalDateTime.now());

        // Update category if changed
        if (updateDTO.getCategoryId() != null) {
            Category category = categoryRepository.findById(updateDTO.getCategoryId())
                    .orElseThrow(
                            () -> new RuntimeException(CATEGORY_NOT_FOUND + " with ID: " + updateDTO.getCategoryId()));
            product.setCategory(category);
        }

        productRepository.save(product);
    }

    public void updateProductActive(Long id, Boolean active) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(PRODUCT_NOT_FOUND));
        product.setActive(active != null && active);
        product.setUpdatedAt(java.time.LocalDateTime.now());
        productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException(PRODUCT_NOT_FOUND);
        }
        productRepository.deleteById(id);
    }

    public void toggleProductActive(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(PRODUCT_NOT_FOUND));
        product.setActive(!product.isActive());
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);
    }
}