package com.group1.swp.pizzario_swp391.dto.size;

import com.group1.swp.pizzario_swp391.entity.ProductSize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SizeResponseDTO {
    private Long id;
    private String sizeName;
    @Builder.Default
    private List<ProductSize> productSizes = new ArrayList<>();}