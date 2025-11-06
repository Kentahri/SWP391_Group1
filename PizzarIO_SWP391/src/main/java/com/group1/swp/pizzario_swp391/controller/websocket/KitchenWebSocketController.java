package com.group1.swp.pizzario_swp391.controller.websocket;

import com.group1.swp.pizzario_swp391.dto.websocket.KitchenOrderMessage;
import com.group1.swp.pizzario_swp391.service.KitchenService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

/**
 * WebSocket Controller for Kitchen
 * 
 * Endpoints:
 * - /app/kitchen/update-item: Kitchen updates individual item status
 * 
 * Kitchen subscribes to:
 * - /topic/kitchen-orders: New orders from guests
 * - /queue/kitchen-notifications: Personal notifications for kitchen
 */
@Controller
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class KitchenWebSocketController {

    KitchenService kitchenService;

    /**
     * Handle individual item status updates from kitchen
     * Kitchen chỉ cập nhật status của từng món, không cập nhật order status
     */
    @MessageMapping("/kitchen/update-item")
    public void handleItemUpdate(@Payload KitchenOrderMessage message) {
        log.info("Kitchen updating item {}", 
                message.getItems() != null && !message.getItems().isEmpty() ? 
                message.getItems().get(0).getItemId() : "unknown");
        
        try {
            if (message.getItems() != null && !message.getItems().isEmpty()) {
                var item = message.getItems().get(0);
                kitchenService.updateItemStatus(
                    item.getItemId(),
                    item.getStatus()
                );
            }
        } catch (Exception e) {
            log.error("Error updating item status", e);
        }
    }
}
