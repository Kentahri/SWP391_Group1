package com.group1.swp.pizzario_swp391.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.group1.swp.pizzario_swp391.dto.category.CategoryCreateDTO;
import com.group1.swp.pizzario_swp391.dto.category.CategoryDetailDTO;
import com.group1.swp.pizzario_swp391.dto.category.CategoryResponseDTO;
import com.group1.swp.pizzario_swp391.dto.category.CategoryUpdateDTO;
import com.group1.swp.pizzario_swp391.entity.Category;
import org.springframework.beans.factory.annotation.Qualifier;

@Mapper(componentModel = "spring")
public interface CategoryResponseMapper {
    
    // Convert Entity to Response DTO
    @Mapping(target = "totalProducts", expression = "java(category.getProducts() != null ? category.getProducts().size() : 0)")
    CategoryResponseDTO toResponseDTO(Category category);
    
    // Convert Entity to Detail DTO
    @Mapping(target = "totalProducts", expression = "java(category.getProducts() != null ? category.getProducts().size() : 0)")
    @Mapping(target = "productNames", expression = "java(mapProductNames(category))")
    CategoryDetailDTO toDetailDTO(Category category);
    
    // Convert Create DTO to Entity
    Category toEntity(CategoryCreateDTO createDTO);
    
    // Update Entity from Update DTO
    void updateEntity(@MappingTarget Category category, CategoryUpdateDTO updateDTO);
    
    // Helper method to map product names
    default List<String> mapProductNames(Category category) {
        if (category.getProducts() == null) {
            return List.of();
        }
        return category.getProducts().stream()
                .map(product -> product.getName())
                .collect(Collectors.toList());
    }
}
