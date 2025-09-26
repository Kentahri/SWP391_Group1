package com.group1.swp.pizzario_swp391.mapper;

import com.group1.swp.pizzario_swp391.dto.CategoryDTO;
import com.group1.swp.pizzario_swp391.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    Category toCategory(CategoryDTO categoryDTO);
    void updateCategory(@MappingTarget Category category, CategoryDTO categoryDTO);
}