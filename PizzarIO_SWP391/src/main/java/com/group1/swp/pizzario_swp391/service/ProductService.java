package com.group1.swp.pizzario_swp391.service;

import com.group1.swp.pizzario_swp391.dto.ProductDTO;
import com.group1.swp.pizzario_swp391.entity.Category; // Thêm import này
import com.group1.swp.pizzario_swp391.entity.Product;
import com.group1.swp.pizzario_swp391.mapper.ProductMapper;
import com.group1.swp.pizzario_swp391.repository.CategoryRepository; // Thêm import này
import com.group1.swp.pizzario_swp391.repository.ProductRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class ProductService {
    ProductRepository productRepository;
    ProductMapper productMapper;
    CategoryRepository categoryRepository; // Thêm CategoryRepository

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> getById(Long id) {
        return productRepository.findById(id);
    }

    public void createProduct(ProductDTO productDTO) {
        LocalDateTime now = LocalDateTime.now();
        productDTO.setCreatedAt(now);
        productDTO.setUpdatedAt(now);

        Product product = productMapper.toProduct(productDTO);

        // Tìm kiếm Category dựa trên categoryId từ DTO
        Category category = categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found with ID: " + productDTO.getCategoryId()));
        product.setCategory(category); // Gán Category vào Product

        productRepository.save(product);
    }

    public void updateProduct(Long id, ProductDTO productDTO) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        productDTO.setUpdatedAt(LocalDateTime.now());
        productMapper.updateProduct(product,productDTO);

        // Cập nhật Category nếu categoryId được cung cấp và khác với category hiện tại
        if (productDTO.getCategoryId() != null && !productDTO.getCategoryId().equals(product.getCategory().getId())) {
            Category category = categoryRepository.findById(productDTO.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found with ID: " + productDTO.getCategoryId()));
            product.setCategory(category);
        }

        productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
}