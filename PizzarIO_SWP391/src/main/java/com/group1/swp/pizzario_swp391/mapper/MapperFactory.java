package com.group1.swp.pizzario_swp391.mapper;

import com.group1.swp.pizzario_swp391.dto.CategoryResponse;
import com.group1.swp.pizzario_swp391.dto.CreateCategoryRequest;
import com.group1.swp.pizzario_swp391.dto.UpdateCategoryRequest;
import com.group1.swp.pizzario_swp391.entity.Category;

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
}
