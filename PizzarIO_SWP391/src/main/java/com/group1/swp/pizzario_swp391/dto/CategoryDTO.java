package com.group1.swp.pizzario_swp391.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CategoryDTO {
    Long id;
    String name;
    String description;
    boolean isActive;
}

