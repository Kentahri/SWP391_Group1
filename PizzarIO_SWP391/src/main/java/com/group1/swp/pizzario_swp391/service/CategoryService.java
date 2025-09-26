package com.group1.swp.pizzario_swp391.service;

import com.group1.swp.pizzario_swp391.dto.CategoryDTO;
import com.group1.swp.pizzario_swp391.entity.Category;
import com.group1.swp.pizzario_swp391.mapper.CategoryMapper;
import com.group1.swp.pizzario_swp391.repository.CategoryRepository;
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
public class CategoryService {
    CategoryRepository categoryRepository;
    CategoryMapper categoryMapper;

    static final String CATEGORY_NOT_FOUND = "Category not found";

    public void createCategory(CategoryDTO categoryDTO) {
        LocalDateTime now = LocalDateTime.now();
        categoryDTO.setCreatedAt(now);
        categoryDTO.setUpdatedAt(now);
        Category category = categoryMapper.toCategory(categoryDTO);
        categoryRepository.save(category);
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    public void updateCategory(Long id, CategoryDTO categoryDTO) {
        Category category = categoryRepository.findById(id).orElseThrow(() -> new RuntimeException(CATEGORY_NOT_FOUND));
        categoryMapper.updateCategory(category, categoryDTO);
        categoryRepository.save(category);
    }

    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }
}

