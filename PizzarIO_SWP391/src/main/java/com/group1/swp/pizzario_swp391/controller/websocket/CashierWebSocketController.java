package com.group1.swp.pizzario_swp391.controller.websocket;

import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;

import com.group1.swp.pizzario_swp391.service.WebSocketService;
import lombok.*;

/**
 * WebSocket Controller for Cashier
 * 
 * Endpoints:
 * - /app/cashier/release-table: Cashier releases a table (RESERVED or WAITING_PAYMENT → AVAILABLE)
 * 
 * Cashier subscribes to:
 * - /topic/tables-cashier: All table status changes
 * - /topic/order-items: Order item updates from kitchen
 * - /queue/cashier-{staffId}: Personal notifications
 */
@Controller
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class CashierWebSocketController {

    WebSocketService webSocketService;


}

