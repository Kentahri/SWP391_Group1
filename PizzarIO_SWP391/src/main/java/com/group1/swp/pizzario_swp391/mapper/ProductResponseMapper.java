package com.group1.swp.pizzario_swp391.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.group1.swp.pizzario_swp391.dto.product.ProductCreateDTO;
import com.group1.swp.pizzario_swp391.dto.product.ProductResponseDTO;
import com.group1.swp.pizzario_swp391.dto.product.ProductUpdateDTO;
import com.group1.swp.pizzario_swp391.entity.Product;

@Mapper(componentModel = "spring")
public interface ProductResponseMapper {

    // Convert Entity to Response DTO
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "categoryId", source = "category.id")
    ProductResponseDTO toResponseDTO(Product product);
    
    // Convert Create DTO to Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Product toEntity(ProductCreateDTO createDTO);
    
    // Update Entity from Update DTO
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget Product product, ProductUpdateDTO updateDTO);
}
