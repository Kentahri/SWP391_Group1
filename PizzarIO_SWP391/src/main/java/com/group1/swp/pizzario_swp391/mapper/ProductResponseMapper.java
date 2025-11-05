package com.group1.swp.pizzario_swp391.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.group1.swp.pizzario_swp391.dto.product.ProductCreateDTO;
import com.group1.swp.pizzario_swp391.dto.product.ProductResponseDTO;
import com.group1.swp.pizzario_swp391.dto.product.ProductSizeDTO;
import com.group1.swp.pizzario_swp391.dto.product.ProductUpdateDTO;
import com.group1.swp.pizzario_swp391.entity.Product;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ProductResponseMapper {

    // Convert Entity to Response DTO
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "basePrice", expression = "java(getMinPrice(product))")
    @Mapping(target = "flashSalePrice", constant = "0.0")
    @Mapping(target = "sizes", expression = "java(mapProductSizes(product))")
    ProductResponseDTO toResponseDTO(Product product);

    // Helper method to get minimum price from product sizes
    default double getMinPrice(Product product) {
        if (product.getProductSizes() == null || product.getProductSizes().isEmpty()) {
            return 0.0;
        }
        return product.getProductSizes().stream()
                .mapToDouble(ps -> ps.getBasePrice())
                .min()
                .orElse(0.0);
    }

    // Helper method to map ProductSize entities to DTOs
    default List<ProductSizeDTO> mapProductSizes(Product product) {
        if (product.getProductSizes() == null || product.getProductSizes().isEmpty()) {
            return List.of();
        }
        return product.getProductSizes().stream()
                .map(ps -> ProductSizeDTO.builder()
                        .id(ps.getId())
                        .sizeId(ps.getSize().getId())
                        .sizeName(ps.getSize().getSizeName())
                        .basePrice(ps.getBasePrice())
                        .flashSalePrice(ps.getFlashSalePrice())
                        .onFlashSale(ps.isOnFlashSale())
                        .currentPrice(ps.getCurrentPrice())
                        .build())
                .collect(Collectors.toList());
    }
    
    // Convert Create DTO to Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Product toEntity(ProductCreateDTO createDTO);
    
    // Update Entity from Update DTO
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget Product product, ProductUpdateDTO updateDTO);
}
