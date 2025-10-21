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
    TableRepository tableRepository;
    SessionRepository sessionRepository;
    OrderRepository orderRepository;

    /**
     * Guest selects a table
     * - Check if table is available
     * - Use optimistic locking to prevent conflicts
     * - Create session for the table
     * - Broadcast to cashier
     * - Broadcast to all guest tablets
     * - Send confirmation to specific guest
     */
    @Transactional
    public void handleTableSelection(TableSelectionRequest request) {
        try {
            DiningTable table = tableRepository.findById(request.getTableId())
                    .orElseThrow(() -> new RuntimeException("Table not found"));

            if (table.getTableStatus() != DiningTable.TableStatus.AVAILABLE) {
                sendTableSelectionError(request.getSessionId(), request.getTableId(),
                        "Bàn không còn trống", TableSelectionResponse.ResponseType.CONFLICT);
                return;
            }

            //  Tạo mới 1 session của bàn
            Session session = new Session();
            session.setTable(table);
            session.setClosed(false);
            session.setCreatedAt(LocalDateTime.now());
            Session savedSession = sessionRepository.save(session);

            // Tạo mới 1 order và lưu vào DB
            Order newOrder = new Order();
            newOrder.setSession(savedSession);
            newOrder.setCreatedAt(LocalDateTime.now());
            newOrder.setOrderStatus(Order.OrderStatus.PREPARING);
            newOrder.setOrderType(Order.OrderType.DINE_IN);
            newOrder.setPaymentStatus(Order.PaymentStatus.UNPAID);
            newOrder.setNote("");
            newOrder.setTotalPrice(0.0);
            newOrder.setTaxRate(0.1); // 10% tax
            orderRepository.save(newOrder);

            // Update table status with optimistic locking
            DiningTable.TableStatus oldStatus = table.getTableStatus();
            table.setTableStatus(DiningTable.TableStatus.OCCUPIED);

            try {
                tableRepository.save(table); // This will throw OptimisticLockException if version mismatch
            } catch (OptimisticLockException _) {
                // Another guest selected this table at the same time
                log.warn("Optimistic lock conflict for table {}", request.getTableId());
                sendTableSelectionError(request.getSessionId(), request.getTableId(),
                        "Bàn đã được chọn bởi khách khác", TableSelectionResponse.ResponseType.CONFLICT);
                return;
            }

            // Broadcast to cashier
            broadcastTableStatusToCashier(
                    TableStatusMessage.MessageType.TABLE_OCCUPIED,
                    request.getTableId(),
                    oldStatus,
                    DiningTable.TableStatus.OCCUPIED,
                    "Guest-" + request.getSessionId(),
                    "Bàn " + request.getTableId() + " đã được khách chọn"
            );

            // Broadcast to all guest tablets
            broadcastTableStatusToGuests(
                    request.getTableId(),
                    DiningTable.TableStatus.OCCUPIED
            );

            // Send success response to specific guest
            TableSelectionResponse response = TableSelectionResponse.builder()
                    .type(TableSelectionResponse.ResponseType.SUCCESS)
                    .tableId(request.getTableId())
                    .sessionId(savedSession.getId())
                    .message("Bạn đã chọn bàn số " + request.getTableId() + " thành công")
                    .build();

            messagingTemplate.convertAndSend(
                    "/queue/guest-" + request.getSessionId(),
                    response
            );

            log.info("Guest {} selected table {}", request.getSessionId(), request.getTableId());

        } catch (Exception e) {
            log.error("Error handling table selection", e);
            sendTableSelectionError(request.getSessionId(), request.getTableId(),
                    "Lỗi hệ thống, vui lòng thử lại", TableSelectionResponse.ResponseType.ERROR);
        }
    }

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
     * Send error response to guest
     */
    private void sendTableSelectionError(String sessionId, int tableId, String message,
                                         TableSelectionResponse.ResponseType type) {
        // Get available tables
        List<Integer> availableTables = tableRepository.findAll().stream()
                .filter(t -> t.getTableStatus() == DiningTable.TableStatus.AVAILABLE)
                .map(DiningTable::getId)
                .collect(Collectors.toList());

        TableSelectionResponse response = TableSelectionResponse.builder()
                .type(type)
                .tableId(tableId)
                .message(message)
                .availableTables(availableTables)
                .build();

        messagingTemplate.convertAndSend("/queue/guest-" + sessionId, response);
    }
}

