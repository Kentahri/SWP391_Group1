package com.group1.swp.pizzario_swp391.dto.websocket;
import lombok.*;
/**
 * Request from cashier to release a table
 * Only allowed for RESERVED or WAITING_PAYMENT status
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TableReleaseRequest {
    private int tableId;
    private String staffId; // Cashier staff ID
    private String reason;  // Optional: why releasing
}

