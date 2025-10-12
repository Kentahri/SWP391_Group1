package com.group1.swp.pizzario_swp391.dto.websocket;

import java.util.List;
import lombok.*;
/**
 * Response to guest after table selection attempt
 * Sent to specific guest queue
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableSelectionResponse {
    
    private ResponseType type;
    private int tableId;
    private String message;
    private List<Integer> availableTables; // For CONFLICT case
    private Long sessionId; // Database session ID if successful
    
    public enum ResponseType {
        SUCCESS,           // Table selected successfully
        CONFLICT,          // Table already taken
        INVALID_TABLE,     // Table doesn't exist or not available
        ERROR              // System error
    }
}

