package com.group1.swp.pizzario_swp391.service;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.group1.swp.pizzario_swp391.dto.order.OrderDetailDTO;
import com.group1.swp.pizzario_swp391.dto.order.OrderItemDTO;
import com.group1.swp.pizzario_swp391.dto.table.TableCreateDTO;
import com.group1.swp.pizzario_swp391.dto.table.TableDTO;
import com.group1.swp.pizzario_swp391.dto.table.TableForCashierDTO;
import com.group1.swp.pizzario_swp391.dto.table.TableManagementDTO;
import com.group1.swp.pizzario_swp391.entity.DiningTable;
import com.group1.swp.pizzario_swp391.entity.Order;
import com.group1.swp.pizzario_swp391.entity.Reservation;
import com.group1.swp.pizzario_swp391.entity.Session;
import com.group1.swp.pizzario_swp391.mapper.TableMapper;
import com.group1.swp.pizzario_swp391.repository.ReservationRepository;
import com.group1.swp.pizzario_swp391.repository.SessionRepository;
import com.group1.swp.pizzario_swp391.repository.TableRepository;

import jakarta.servlet.http.HttpSession;


@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class TableService {
    TableRepository tableRepository;
    TableMapper tableMapper;
    SessionRepository sessionRepository;
    WebSocketService webSocketService;
    ReservationRepository reservationRepository;
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


    public void add(DiningTable table) {
        tableRepository.save(table);
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
        if(table.getTableStatus() == DiningTable.TableStatus.AVAILABLE && reservationList.isEmpty()) {
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
        }else {
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

        // Xóa giỏ hàng trong http session
        session.invalidate();
    }
}