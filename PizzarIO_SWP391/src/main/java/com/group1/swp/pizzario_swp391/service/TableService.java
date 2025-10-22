package com.group1.swp.pizzario_swp391.service;

import com.group1.swp.pizzario_swp391.dto.order.OrderDetailDTO;
import com.group1.swp.pizzario_swp391.dto.order.OrderItemDTO;
import com.group1.swp.pizzario_swp391.dto.table.TableCreateDTO;
import com.group1.swp.pizzario_swp391.dto.table.TableDTO;
import com.group1.swp.pizzario_swp391.dto.table.TableForCashierDTO;
import com.group1.swp.pizzario_swp391.dto.table.TableManagementDTO;
import com.group1.swp.pizzario_swp391.entity.DiningTable;
import com.group1.swp.pizzario_swp391.entity.Order;
import com.group1.swp.pizzario_swp391.entity.Session;
import com.group1.swp.pizzario_swp391.mapper.TableMapper;
import com.group1.swp.pizzario_swp391.repository.SessionRepository;
import com.group1.swp.pizzario_swp391.repository.TableRepository;
import jakarta.servlet.http.HttpSession;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;


@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class TableService {
    TableRepository tableRepository;
    TableMapper tableMapper;
    SessionRepository sessionRepository;
    WebSocketService webSocketService;
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
     * Lấy tất cả bàn (Manager)
     */
    public List<TableDTO> getAllTables() {
        return tableMapper.toTableDTOs(tableRepository.findAll());
    }

    /**
     * Lấy bàn theo ID
     */
    public TableDTO getTableById(Integer id) {
        return tableMapper.toTableDTO(tableRepository.findById(id).
                orElseThrow(() -> new RuntimeException("Table not found")));
    }

    /**
     * Cập nhật bàn (Manager)
     * Manager chỉ cập nhật capacity và tableCondition
     * TableStatus do Cashier quản lý
     */
    public void updateTable(int id, TableManagementDTO tableManagementDTO) {
        DiningTable table = tableRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Table not found"));
        tableMapper.updateDiningTable(table, tableManagementDTO);
        table.setUpdatedAt(LocalDateTime.now());
        tableRepository.save(table);
    }

    /**
     * Xóa bàn (Manager)
     */
    public void deleteTable(int id) {
        tableRepository.deleteById(id);
    }


    /**
     * Lấy danh sách bàn cho Cashier
     */
    public List<TableForCashierDTO> getTablesForCashier() {
        return tableMapper.toTableForCashierDTOs(tableRepository.findAll());
    }


    public void add(DiningTable table) {
        tableRepository.save(table);
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