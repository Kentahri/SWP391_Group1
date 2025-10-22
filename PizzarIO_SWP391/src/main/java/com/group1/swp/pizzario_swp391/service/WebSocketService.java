package com.group1.swp.pizzario_swp391.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.group1.swp.pizzario_swp391.dto.websocket.TableSelectionRequest;
import com.group1.swp.pizzario_swp391.dto.websocket.TableSelectionResponse;
import com.group1.swp.pizzario_swp391.dto.websocket.TableStatusMessage;
import com.group1.swp.pizzario_swp391.entity.*;
import com.group1.swp.pizzario_swp391.repository.OrderRepository;
import com.group1.swp.pizzario_swp391.repository.SessionRepository;
import com.group1.swp.pizzario_swp391.repository.TableRepository;
import jakarta.persistence.OptimisticLockException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}

