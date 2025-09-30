package com.group1.swp.pizzario_swp391.mapper;

import com.group1.swp.pizzario_swp391.dto.category.CategoryDTO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;


import com.group1.swp.pizzario_swp391.entity.Category;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    Category toCategory(CategoryDTO categoryDTO);
    void updateCategory(@MappingTarget Category category, CategoryDTO categoryDTO);
}