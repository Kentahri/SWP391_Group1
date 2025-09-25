package com.group1.swp.pizzario_swp391.mapper;

import com.group1.swp.pizzario_swp391.dto.CategoryResponse;
import com.group1.swp.pizzario_swp391.dto.CreateCategoryRequest;
import com.group1.swp.pizzario_swp391.dto.UpdateCategoryRequest;
import com.group1.swp.pizzario_swp391.dto.CreateProductRequest;
import com.group1.swp.pizzario_swp391.dto.ProductResponse;
import com.group1.swp.pizzario_swp391.dto.UpdateProductRequest;
import com.group1.swp.pizzario_swp391.entity.Category;
import com.group1.swp.pizzario_swp391.entity.Product;

public class MapperFactory {
    

    public static CategoryResponse toCategoryResponse(Category category) {
        if (category == null) {
            return null;
        }
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .isActive(category.isActive())
                .build();
    }

    public static Category toCategoryEntity(CreateCategoryRequest request) {
        if (request == null) {
            return null;
        }
        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setActive(request.isActive());
        return category;
    }

    public static void updateCategoryFromRequest(Category category, UpdateCategoryRequest request) {
        if (category == null || request == null) {
            return;
        }
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setActive(request.isActive());
    }

    public static ProductResponse toProductResponse(Product product) {
        if (product == null) {
            return null;
        }
        return ProductResponse.builder()
                .id(product.getId())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .name(product.getName())
                .description(product.getDescription())
                .imageURL(product.getImageURL())
                .basePrice(product.getBasePrice())
                .flashSalePrice(product.getFlashSalePrice())
                .flashSaleStart(product.getFlashSaleStart())
                .flashSaleEnd(product.getFlashSaleEnd())
                .isActive(product.isActive())
                .build();
    }

    public static Product toProductEntity(CreateProductRequest request, Category category) {
        if (request == null) {
            return null;
        }
        Product product = new Product(
                request.getName(),
                request.getDescription(),
                request.getImageURL(),
                request.getBasePrice(),
                request.getFlashSalePrice(),
                request.getFlashSaleStart(),
                request.getFlashSaleEnd(),
                request.isActive(),
                null,
                null
        );
        product.setCategory(category);
        return product;
    }

    public static void updateProductFromRequest(Product product, UpdateProductRequest request, Category category) {
        if (product == null || request == null) {
            return;
        }
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setImageURL(request.getImageURL());
        product.setBasePrice(request.getBasePrice());
        product.setFlashSalePrice(request.getFlashSalePrice());
        product.setFlashSaleStart(request.getFlashSaleStart());
        product.setFlashSaleEnd(request.getFlashSaleEnd());
        product.setActive(request.isActive());
        if (category != null) {
            product.setCategory(category);
        }
    }
}
