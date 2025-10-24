package com.group1.swp.pizzario_swp391.controller.websocket;

import com.group1.swp.pizzario_swp391.service.GuestService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
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


}

