package com.group1.swp.pizzario_swp391.dto.report;

import java.util.List;

public record WeeklySalesDTO (
        List<String> label,
        double[] data
){}
