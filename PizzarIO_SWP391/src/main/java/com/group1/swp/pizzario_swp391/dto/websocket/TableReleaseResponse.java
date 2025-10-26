package com.group1.swp.pizzario_swp391.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableReleaseResponse {
    private ResponseType type;
    private int tableId;
    private String message;

    public enum ResponseType {
        SUCCESS,
        ERROR
    }
}