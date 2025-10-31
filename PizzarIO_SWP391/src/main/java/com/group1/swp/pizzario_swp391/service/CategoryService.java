package com.group1.swp.pizzario_swp391.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import org.springframework.stereotype.Service;
import com.group1.swp.pizzario_swp391.dto.category.CategoryCreateDTO;
import com.group1.swp.pizzario_swp391.dto.category.CategoryDetailDTO;
import com.group1.swp.pizzario_swp391.dto.category.CategoryResponseDTO;
import com.group1.swp.pizzario_swp391.dto.category.CategoryUpdateDTO;
import com.group1.swp.pizzario_swp391.entity.Category;
import com.group1.swp.pizzario_swp391.mapper.CategoryResponseMapper;
import com.group1.swp.pizzario_swp391.repository.CategoryRepository;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class CategoryService {
    CategoryRepository categoryRepository;
    CategoryResponseMapper categoryMapper;

    static final String CATEGORY_NOT_FOUND = "Category not found";

    public void createCategory(CategoryCreateDTO createDTO) {
        Category category = categoryMapper.toEntity(createDTO);
        LocalDateTime now = LocalDateTime.now();
        category.setCreatedAt(now);
        category.setUpdatedAt(now);
        categoryRepository.save(category);
    }

    public List<CategoryResponseDTO> getAllActiveCategories() {
        List<Category> categories = categoryRepository.findAll().stream()
                .filter(Category::isActive)
                .collect(Collectors.toList());
        return categories.stream()
                .map(categoryMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public List<CategoryResponseDTO> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream()
                .map(categoryMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public CategoryUpdateDTO getCategoryForUpdate(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(CATEGORY_NOT_FOUND));
        return CategoryUpdateDTO.builder()
                .name(category.getName())
                .description(category.getDescription())
                .active(category.isActive())
                .build();
    }

    public void updateCategory(Long id, CategoryUpdateDTO updateDTO) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(CATEGORY_NOT_FOUND));
        categoryMapper.updateEntity(category, updateDTO);
        category.setUpdatedAt(LocalDateTime.now());
        categoryRepository.save(category);
    }

    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new RuntimeException(CATEGORY_NOT_FOUND);
        }
        categoryRepository.deleteById(id);
    }

    public void toggleCategoryActive(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(CATEGORY_NOT_FOUND));
        category.setActive(!category.isActive());
        category.setUpdatedAt(LocalDateTime.now());
        categoryRepository.save(category);
    }
}
