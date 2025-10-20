package com.group1.swp.pizzario_swp391.dto.websocket;

import java.time.LocalDateTime;

import com.group1.swp.pizzario_swp391.entity.DiningTable;
import lombok.*;
/**
 * WebSocket message for table status updates
 * Broadcast to cashier and all guest tablets when table status changes
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableStatusMessage {
    
    private MessageType type;
    private int tableId;
    private DiningTable.TableStatus oldStatus;
    private DiningTable.TableStatus newStatus;
    private String updatedBy;
    private LocalDateTime timestamp;
    private String message;
    
    public enum MessageType {
        TABLE_OCCUPIED,      // Guest selected table
        TABLE_RELEASED,      // Cashier released table
        TABLE_RESERVED,      // Table reserved
        TABLE_PAYMENT_PENDING // Waiting for payment
    }
}

