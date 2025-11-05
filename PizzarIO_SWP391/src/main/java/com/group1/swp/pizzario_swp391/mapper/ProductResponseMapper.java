package com.group1.swp.pizzario_swp391.mapper;

import com.group1.swp.pizzario_swp391.entity.ProductSize;
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
    @Mapping(target = "sizes", expression = "java(mapProductSizes(product))")
    ProductResponseDTO toResponseDTO(Product product);
    
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

    // Helper method to map ProductSizes to ProductSizeDTOs
    default List<ProductSizeDTO> mapProductSizes(Product product) {
        if (product == null || product.getProductSizes() == null) {
            return List.of();
        }
        return product.getProductSizes().stream()
                .map(this::toProductSizeDTO)
                .collect(Collectors.toList());
    }

    // Helper method to convert ProductSize entity to ProductSizeDTO
    default ProductSizeDTO toProductSizeDTO(ProductSize productSize) {
        if (productSize == null) {
            return null;
        }
        return ProductSizeDTO.builder()
                .id(productSize.getId())
                .sizeId(productSize.getSize() != null ? productSize.getSize().getId() : null)
                .sizeName(productSize.getSize() != null ? productSize.getSize().getSizeName() : null)
                .basePrice(productSize.getBasePrice())
                .flashSalePrice(productSize.getFlashSalePrice())
                .onFlashSale(productSize.isOnFlashSale())
                .currentPrice(productSize.getCurrentPrice())
                .build();
    }
}
