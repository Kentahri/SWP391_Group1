package com.group1.swp.pizzario_swp391.service;

import com.group1.swp.pizzario_swp391.dto.CreateProductRequest;
import com.group1.swp.pizzario_swp391.dto.ProductResponse;
import com.group1.swp.pizzario_swp391.dto.UpdateProductRequest;
import com.group1.swp.pizzario_swp391.entity.Category;
import com.group1.swp.pizzario_swp391.entity.Product;
import com.group1.swp.pizzario_swp391.mapper.MapperFactory;
import com.group1.swp.pizzario_swp391.repository.CategoryRepository;
import com.group1.swp.pizzario_swp391.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    static final String PRODUCT_NOT_FOUND = "Product not found";
    static final String CATEGORY_NOT_FOUND = "Category not found";

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    public List<ProductResponse> getAll() {
        return productRepository.findAll().stream()
                .map(MapperFactory::toProductResponse)
                .collect(Collectors.toList());
    }

    public ProductResponse getById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(PRODUCT_NOT_FOUND));
        return MapperFactory.toProductResponse(product);
    }

    public ProductResponse create(CreateProductRequest request) {
        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException(CATEGORY_NOT_FOUND));
        }
        Product product = MapperFactory.toProductEntity(request, category);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        Product saved = productRepository.save(product);
        return MapperFactory.toProductResponse(saved);
    }

    public ProductResponse update(Long id, UpdateProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(PRODUCT_NOT_FOUND));
        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException(CATEGORY_NOT_FOUND));
        }
        MapperFactory.updateProductFromRequest(product, request, category);
        product.setUpdatedAt(LocalDateTime.now());
        Product saved = productRepository.save(product);
        return MapperFactory.toProductResponse(saved);
    }

    public void delete(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(PRODUCT_NOT_FOUND));
        productRepository.delete(product);
    }
}
