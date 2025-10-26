package com.group1.swp.pizzario_swp391.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TableReleaseRequest {
    private int tableId;
    private String sessionId;
}