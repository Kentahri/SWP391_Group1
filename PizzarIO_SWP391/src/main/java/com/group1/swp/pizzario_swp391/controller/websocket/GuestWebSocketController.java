package com.group1.swp.pizzario_swp391.controller.websocket;

import com.group1.swp.pizzario_swp391.service.GuestService;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import com.group1.swp.pizzario_swp391.dto.websocket.TableSelectionRequest;
import com.group1.swp.pizzario_swp391.service.WebSocketService;
import lombok.*;
/**
 * WebSocket Controller for Guest (Tablet)
 * 
 * Endpoints:
 * - /app/guest/select-table: Guest selects a table
 * 
 * Guests subscribe to:
 * - /topic/tables-guest: All table status changes
 * - /queue/guest-{sessionId}: Personal messages for this guest
 */
@Controller
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class GuestWebSocketController {

    GuestService guestService;

    /**
     * Handle table selection from guest tablet
     * Guest sends: { tableId, sessionId, guestCount }
     * Response sent to: /queue/guest-{sessionId}
     */
    @MessageMapping("/guest/select-table")
    public void selectTable(TableSelectionRequest request) {
        log.info("Guest {} requesting table {}", request.getSessionId(), request.getTableId());
        guestService.handleTableSelection(request);
    }
}

