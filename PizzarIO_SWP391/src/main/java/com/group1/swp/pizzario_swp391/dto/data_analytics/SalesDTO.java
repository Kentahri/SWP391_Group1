package com.group1.swp.pizzario_swp391.dto.data_analytics;

import java.util.List;

public record SalesDTO(
        List<String> label,
        double[] data
){}
