package com.group1.swp.pizzario_swp391.dto.kitchen;

import com.group1.swp.pizzario_swp391.dto.category.CategoryResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDataDTO {
    private List<DashboardOrderItemDTO> orderItems;
    private List<CategoryResponseDTO> categories;
}
