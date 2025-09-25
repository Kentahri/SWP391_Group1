package com.group1.swp.pizzario_swp391.service;

import com.group1.swp.pizzario_swp391.dto.CategoryResponse;
import com.group1.swp.pizzario_swp391.dto.CreateCategoryRequest;
import com.group1.swp.pizzario_swp391.dto.UpdateCategoryRequest;
import com.group1.swp.pizzario_swp391.entity.Category;
import com.group1.swp.pizzario_swp391.mapper.MapperFactory;
import com.group1.swp.pizzario_swp391.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    static final String CATEGORY_NOT_FOUND = "Category not found";
    @Autowired
    private CategoryRepository categoryRepository;

    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(MapperFactory::toCategoryResponse)
                .collect(Collectors.toList());
    }

    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(CATEGORY_NOT_FOUND));
        return MapperFactory.toCategoryResponse(category);
    }

    public CategoryResponse createCategory(CreateCategoryRequest dto) {
        Category category = MapperFactory.toCategoryEntity(dto);
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        Category saved = categoryRepository.save(category);
        return MapperFactory.toCategoryResponse(saved);
    }

    public CategoryResponse updateCategory(Long id, UpdateCategoryRequest updated) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(CATEGORY_NOT_FOUND));
        MapperFactory.updateCategoryFromRequest(category, updated);
        category.setUpdatedAt(LocalDateTime.now());
        Category saved = categoryRepository.save(category);
        return MapperFactory.toCategoryResponse(saved);
    }

    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(CATEGORY_NOT_FOUND));
        categoryRepository.delete(category);
    }
}

