package com.group1.swp.pizzario_swp391.service;

import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.group1.swp.pizzario_swp391.dto.websocket.TableStatusMessage;
import com.group1.swp.pizzario_swp391.dto.websocket.KitchenOrderMessage;
import com.group1.swp.pizzario_swp391.entity.DiningTable;

/**
 * Service to handle WebSocket business logic for Cashier operations
 */
@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class WebSocketService{

    SimpMessagingTemplate messagingTemplate;

    /**
     * Broadcast table status change to cashier
     */
    public void broadcastTableStatusToCashier(
            TableStatusMessage.MessageType type,
            int tableId,
            DiningTable.TableStatus oldStatus,
            DiningTable.TableStatus newStatus,
            String updatedBy,
            String message
    ) {
        TableStatusMessage statusMessage = TableStatusMessage.builder()
                .type(type)
                .tableId(tableId)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .updatedBy(updatedBy)
                .timestamp(LocalDateTime.now())
                .message(message)
                .build();

        messagingTemplate.convertAndSend("/topic/tables-cashier", statusMessage);
    }

    /**
     * Broadcast table status to all guest tablets
     */
    public void broadcastTableStatusToGuests(int tableId, DiningTable.TableStatus newStatus) {
        TableStatusMessage guestMessage = TableStatusMessage.builder()
                .tableId(tableId)
                .newStatus(newStatus)
                .timestamp(LocalDateTime.now())
                .build();

        messagingTemplate.convertAndSend("/topic/tables-guest", guestMessage);
    }

    /**
     * Broadcast table retired status to cashier and guests
     * When manager marks a table as retired, notify all clients to remove it from UI
     */
    public void broadcastTableRetired(int tableId, String updatedBy) {
        TableStatusMessage retiredMessage = TableStatusMessage.builder()
                .type(TableStatusMessage.MessageType.TABLE_RETIRED)
                .tableId(tableId)
                .updatedBy(updatedBy)
                .timestamp(LocalDateTime.now())
                .message("Bàn " + tableId + " đã được đánh dấu là retired và sẽ không hiển thị nữa")
                .build();

        // Send to cashier
        messagingTemplate.convertAndSend("/topic/tables-cashier", retiredMessage);
        
        // Send to guests
        messagingTemplate.convertAndSend("/topic/tables-guest", retiredMessage);
        
        log.info("Broadcasted table {} retired status to all clients", tableId);
    }

    // ==================== KITCHEN WEBSOCKET METHODS ====================

    /**
     * Broadcast new order to kitchen
     * Kitchen chỉ nhận thông tin order mới, không cập nhật order status
     */
    public void broadcastNewOrderToKitchen(KitchenOrderMessage orderMessage) {
        orderMessage.setType(KitchenOrderMessage.MessageType.NEW_ORDER);
        orderMessage.setTimestamp(LocalDateTime.now());
        
        messagingTemplate.convertAndSend("/topic/kitchen-orders", orderMessage);
        log.info("Broadcasted new order {} to kitchen", orderMessage.getCode());
    }

    /**
     * Send personal notification to kitchen
     */
    public void sendKitchenNotification(String message, String type) {
        KitchenOrderMessage notification = KitchenOrderMessage.builder()
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();

        messagingTemplate.convertAndSend("/queue/kitchen-notifications", notification);
        log.info("Sent kitchen notification: {}", message);
    }
}

