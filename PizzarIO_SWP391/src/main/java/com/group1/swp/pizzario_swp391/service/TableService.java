package com.group1.swp.pizzario_swp391.service;

import com.group1.swp.pizzario_swp391.config.Setting;
import com.group1.swp.pizzario_swp391.dto.order.OrderDetailDTO;
import com.group1.swp.pizzario_swp391.dto.order.OrderItemDTO;
import com.group1.swp.pizzario_swp391.dto.table.TableCreateDTO;
import com.group1.swp.pizzario_swp391.dto.table.TableDTO;
import com.group1.swp.pizzario_swp391.dto.table.TableForCashierDTO;
import com.group1.swp.pizzario_swp391.dto.table.TableManagementDTO;
import com.group1.swp.pizzario_swp391.dto.websocket.*;
import com.group1.swp.pizzario_swp391.entity.DiningTable;
import com.group1.swp.pizzario_swp391.entity.Order;
import com.group1.swp.pizzario_swp391.entity.Reservation;
import com.group1.swp.pizzario_swp391.entity.Session;
import com.group1.swp.pizzario_swp391.mapper.TableMapper;
import com.group1.swp.pizzario_swp391.repository.OrderRepository;
import com.group1.swp.pizzario_swp391.repository.ReservationRepository;
import com.group1.swp.pizzario_swp391.repository.SessionRepository;
import com.group1.swp.pizzario_swp391.repository.TableRepository;
import jakarta.persistence.OptimisticLockException;
import jakarta.servlet.http.HttpSession;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class TableService{
    TableRepository tableRepository;
    TableMapper tableMapper;
    SessionRepository sessionRepository;
    WebSocketService webSocketService;
    ReservationRepository reservationRepository;
    OrderRepository orderRepository;
    SimpMessagingTemplate simpMessagingTemplate;
    Setting setting;

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
            } catch (OptimisticLockException e) {
                // Another guest selected this table at the same time
                log.warn("Optimistic lock conflict for table {}", request.getTableId());
                sendTableSelectionError(request.getSessionId(), request.getTableId(),
                        "Bàn đã được chọn bởi khách khác", TableSelectionResponse.ResponseType.CONFLICT);
                return;
            }

            // Broadcast to cashier
            webSocketService.broadcastTableStatusToCashier(
                    TableStatusMessage.MessageType.TABLE_OCCUPIED,
                    request.getTableId(),
                    oldStatus,
                    DiningTable.TableStatus.OCCUPIED,
                    "Guest-" + request.getSessionId(),
                    "Bàn " + request.getTableId() + " đã được khách chọn"
            );

            // Broadcast to all guest tablets
            webSocketService.broadcastTableStatusToGuests(
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

            simpMessagingTemplate.convertAndSend(
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

        simpMessagingTemplate.convertAndSend("/queue/guest-" + sessionId, response);
    }

    /**
     * Tạo bàn mới (Manager)
     * Manager chỉ nhập capacity, hệ thống tự set status=AVAILABLE và condition=NEW
     */
    public void createNewTable(TableCreateDTO tableCreateDTO) {
        DiningTable diningTable = tableMapper.toDiningTable(tableCreateDTO);
        LocalDateTime now = LocalDateTime.now();

        // Tự động set các giá trị mặc định
        diningTable.setTableStatus(DiningTable.TableStatus.AVAILABLE);
        diningTable.setTableCondition(DiningTable.TableCondition.NEW);
        diningTable.setCreatedAt(now);
        diningTable.setUpdatedAt(now);

        tableRepository.save(diningTable);
    }

    /**
     * Lọc bàn theo điều kiện cho manager
     */
    public List<TableDTO> findTableByCondition(DiningTable.TableCondition condition) {
        return tableMapper.toTableDTOs(tableRepository.getDiningTableByTableCondition(condition));
    }

    /**
     * Lọc các bàn đang không ở trạng thái RETIRED
     */
    public List<TableDTO> findNonRetiredTables() {
        return tableMapper.toTableDTOs(tableRepository.getDiningTableByTableConditionExceptRetired());
    }

    /**
     * Lấy tất cả bàn cho Manager
     */
    public List<TableDTO> getAllTablesForManager() {
        return tableMapper.toTableDTOs(tableRepository.findAll());
    }

    /**
     * Lấy tất cả bàn cho Guest - không bao gồm bàn retired
     */
    public List<TableDTO> getAllTablesForGuest() {
        return tableMapper.toTableDTOs(tableRepository.getAllTablesForGuest());
    }

    /**
     * Lấy bàn theo ID
     */
    public TableDTO getTableById(Integer id) {
        return tableMapper.toTableDTO(tableRepository.findById(id).
                orElseThrow(() -> new RuntimeException("Table not found")));
    }

    /**
     * Lấy danh sách bàn cho Cashier
     */
    public List<TableForCashierDTO> getTablesForCashier() {
        return tableMapper.toTableForCashierDTOs(tableRepository.getAllTablesForCashier());
    }

    public void lockTableForMerge(int tableId) {
        DiningTable table = tableRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Table not found"));

        if(table.getTableStatus() != DiningTable.TableStatus.AVAILABLE) {
            throw new RuntimeException("Chỉ có thể cập nhật trạng thái bàn khi bàn đang trống (AVAILABLE)");
        }

        DiningTable.TableStatus oldStatus = table.getTableStatus();
        table.setTableStatus(DiningTable.TableStatus.LOCKED);
        tableRepository.save(table);

        webSocketService.broadcastTableStatusToCashier(
                TableStatusMessage.MessageType.TABLE_LOCKED,
                tableId,
                oldStatus,
                DiningTable.TableStatus.LOCKED,
                "Cashier",
                "Trạng thái bàn " + tableId + " đã được cập nhật."
        );
    }

    public void unlockTableFromMerge(int tableId) {
        DiningTable table = tableRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Table not found"));

        if(table.getTableStatus() != DiningTable.TableStatus.LOCKED) {
            throw new RuntimeException("Chỉ có thể mở khóa bàn khi bàn đang ở trạng thái LOCKED");
        }

        DiningTable.TableStatus oldStatus = table.getTableStatus();
        table.setTableStatus(DiningTable.TableStatus.AVAILABLE);
        tableRepository.save(table);

        webSocketService.broadcastTableStatusToCashier(
                TableStatusMessage.MessageType.TABLE_RELEASED,
                tableId,
                oldStatus,
                DiningTable.TableStatus.AVAILABLE,
                "Cashier",
                "Trạng thái bàn " + tableId + " đã được cập nhật."
        );
    }


    public DiningTable add(DiningTable table) {
        return tableRepository.save(table);
    }

    /**
     * Cập nhật bàn (Manager)
     * Manager chỉ cập nhật capacity và tableCondition
     * Manager sẽ chỉ được cập nhật bàn thành trạng thái RETIRED nếu bàn đó đang trống (AVAILABLE)
     */
    @Transactional
    public void updateTable(int id, TableManagementDTO tableManagementDTO) {
        DiningTable table = tableRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Table not found"));
        List<Reservation> reservationList = reservationRepository.getAllReservationsForUpdateTable(table.getId(), LocalDateTime.now());
        if (table.getTableStatus() == DiningTable.TableStatus.AVAILABLE && reservationList.isEmpty()) {
            // Lưu lại condition cũ để kiểm tra có phải retired không
            DiningTable.TableCondition oldCondition = table.getTableCondition();

            tableMapper.updateDiningTable(table, tableManagementDTO);
            table.setUpdatedAt(LocalDateTime.now());
            tableRepository.save(table);

            // Nếu đánh dấu thành RETIRED, gửi WebSocket thông báo
            if (tableManagementDTO.getTableCondition() == DiningTable.TableCondition.RETIRED
                    && oldCondition != DiningTable.TableCondition.RETIRED) {
                webSocketService.broadcastTableRetired(id, "Manager");
            }
        } else {
            throw new RuntimeException("Bàn đang không trống hoặc đã được đặt trước!");
        }

    }


    /**
     * Lấy order detail của bàn (nếu có session và order đang active)
     */
    public OrderDetailDTO getOrderDetailByTableId(Integer tableId) {
        // Tìm session đang active của bàn
        Session activeSession = sessionRepository.findByTableIdAndIsClosedFalse(tableId).orElse(null);

        if (activeSession == null || activeSession.getOrder() == null) {
            return null; // Bàn không có order
        }

        Order order = activeSession.getOrder();

        // Map order items
        List<OrderItemDTO> itemDTOs = order.getOrderItems().stream()
                .map(item -> OrderItemDTO.builder()
                        .id(item.getId())
                        .productName(item.getProduct() != null ? item.getProduct().getName() : "Unknown")
                        .productImageUrl(item.getProduct() != null ? item.getProduct().getImageURL() : null)
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .totalPrice(item.getTotalPrice())
                        .note(item.getNote())
                        .status(item.getOrderItemStatus())
                        .build())
                .toList();

        // Build order detail DTO
        return OrderDetailDTO.builder()
                .orderId(order.getId())
                .sessionId(activeSession.getId())
                .tableId(tableId)
                .tableName("Bàn " + tableId)
                .orderStatus(order.getOrderStatus())
                .orderType(order.getOrderType())
                .paymentStatus(order.getPaymentStatus())
                .paymentMethod(order.getPaymentMethod())
                .totalPrice(order.getTotalPrice())
                .taxRate(order.getTaxRate())
                .note(order.getNote())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .items(itemDTOs)
                .createdByStaffName(order.getStaff() != null ? order.getStaff().getName() : null)
                .voucherCode(order.getVoucher() != null ? order.getVoucher().getCode() : null)
                .discountAmount(order.getVoucher() != null ? order.getVoucher().getValue() : null)
                .customerName(order.getMembership() != null ? order.getMembership().getName() : "Khách vãng lai")
                .customerPhone(order.getMembership() != null ? order.getMembership().getPhoneNumber() : null)
                .build();
    }

    public void releaseTable(Integer tableId, HttpSession session) {
        // Tìm bàn
        DiningTable table = tableRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Table not found with id: " + tableId));

        // Lưu lại trạng thái cũ để gửi broadcast
        DiningTable.TableStatus oldStatus = table.getTableStatus();

        // Tìm session đang active của bàn
        Session activeSession = sessionRepository.findByTableIdAndIsClosedFalse(tableId)
                .orElse(null);

        // Đóng session nếu có
        if (activeSession != null) {
            activeSession.setClosed(true);
            activeSession.setClosedAt(LocalDateTime.now());
            sessionRepository.save(activeSession);
        }

        // Cập nhật trạng thái bàn
        table.setTableStatus(DiningTable.TableStatus.AVAILABLE);
        table.setUpdatedAt(LocalDateTime.now());
        tableRepository.save(table);

        // Gửi broadcast cho tất cả client
        webSocketService.broadcastTableStatusToGuests(tableId, DiningTable.TableStatus.AVAILABLE);
        webSocketService.broadcastTableStatusToCashier(
                com.group1.swp.pizzario_swp391.dto.websocket.TableStatusMessage.MessageType.TABLE_RELEASED,
                tableId,
                oldStatus,
                DiningTable.TableStatus.AVAILABLE,
                "Guest",
                "Bàn " + tableId + " đã được giải phóng."
        );

        // Xóa giỏ hàng trong http session (chỉ khi session không null)
        if (session != null) {
            session.invalidate();
        }
    }

    @Transactional
    public void handleTableRelease(TableReleaseRequest request) {
        try {
            DiningTable table = tableRepository.findById(request.getTableId())
                    .orElseThrow(() -> new RuntimeException("Table not found"));

            if (table.getTableStatus() != DiningTable.TableStatus.OCCUPIED) {
                // Optionally send an error back to the guest if the table is not occupied
                log.warn("Attempt to request payment for table {} which is not OCCUPIED. Current status: {}",
                        request.getTableId(), table.getTableStatus());
                return;
            }

            // Set table to WAITING_PAYMENT status when guest requests payment
            table.setTableStatus(DiningTable.TableStatus.WAITING_PAYMENT);
            table.setUpdatedAt(LocalDateTime.now());
            tableRepository.save(table);

            // Broadcast table status change to all guests
            webSocketService.broadcastTableStatusToGuests(request.getTableId(), DiningTable.TableStatus.WAITING_PAYMENT);

            // Note: Payment is now handled directly by guest, no need to notify cashier

            // Send confirmation back to the guest
            simpMessagingTemplate.convertAndSend(
                    "/queue/guest-" + request.getSessionId(),
                    TableReleaseResponse.builder()
                            .type(TableReleaseResponse.ResponseType.SUCCESS)
                            .tableId(request.getTableId())
                            .message("Bàn đã được chuyển sang trạng thái chờ thanh toán. Bạn có thể thanh toán trực tiếp từ menu.")
                            .build()
            );

            log.info("Guest {} requested payment for table {}", request.getSessionId(), request.getTableId());

        } catch (Exception e) {
            log.error("Error handling payment request for table {}", request.getTableId(), e);
            // Send an error message back to the guest
            simpMessagingTemplate.convertAndSend(
                    "/queue/guest-" + request.getSessionId(),
                    TableReleaseResponse.builder()
                            .type(TableReleaseResponse.ResponseType.ERROR)
                            .tableId(request.getTableId())
                            .message("Lỗi khi gửi yêu cầu thanh toán. Vui lòng thử lại.")
                            .build()
            );
        }
    }
}