package com.group1.swp.pizzario_swp391.dto.websocket;
import lombok.*;
/**
 * Request from guest to select a table
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TableSelectionRequest {
    private int tableId;
    private String sessionId; // Unique ID for each tablet
    private int guestCount;   // Number of guests
}

