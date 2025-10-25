package com.group1.swp.pizzario_swp391.service;

import com.group1.swp.pizzario_swp391.entity.DiningTable;
import com.group1.swp.pizzario_swp391.entity.Order;
import com.group1.swp.pizzario_swp391.entity.Session;
import com.group1.swp.pizzario_swp391.repository.OrderRepository;
import com.group1.swp.pizzario_swp391.repository.SessionRepository;
import com.group1.swp.pizzario_swp391.repository.TableRepository;
import com.group1.swp.pizzario_swp391.service.TableService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class SessionService {

    private final SessionRepository sessionRepository;
    private final OrderRepository orderRepository;
    private final TableRepository tableRepository;
    private final TableService tableService;

    public SessionService(SessionRepository sessionRepository, 
                         OrderRepository orderRepository, 
                         TableRepository tableRepository, 
                         @Lazy TableService tableService) {
        this.sessionRepository = sessionRepository;
        this.orderRepository = orderRepository;
        this.tableRepository = tableRepository;
        this.tableService = tableService;
    }

    /**
     * Tạo session mới khi khách chọn bàn
     */
    @Transactional
    public Session createSession(Long tableId) {
        // Kiểm tra xem bàn có đang được sử dụng không
        Optional<Session> existingSession = sessionRepository.findByTableIdAndIsClosedFalse(tableId.intValue());
        if (existingSession.isPresent()) {
            throw new RuntimeException("Bàn đang được sử dụng");
        }

        // Tạo session mới
        Session session = new Session();
        session.setClosed(false);
        session.setCreatedAt(LocalDateTime.now());
        
        // Lấy thông tin bàn
        DiningTable table = tableRepository.findById(tableId.intValue())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bàn với ID: " + tableId));
        session.setTable(table);

        // Tạo order mới cho session
        Order order = new Order();
        order.setSession(session);
        order.setOrderType(Order.OrderType.DINE_IN);
        order.setOrderStatus(Order.OrderStatus.PREPARING);
        order.setPaymentStatus(Order.PaymentStatus.UNPAID);
        order.setTotalPrice(0.0);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        // Lưu session và order
        session = sessionRepository.save(session);
        order = orderRepository.save(order);
        
        return session;
    }

    /**
     * Lấy session theo ID
     */
    public Session getSessionById(Long sessionId) {
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy session với ID: " + sessionId));
    }

    /**
     * Lấy order từ session
     */
    public Order getOrderBySessionId(Long sessionId) {
        Session session = getSessionById(sessionId);
        if (session.getOrder() == null) {
            throw new RuntimeException("Session không có order");
        }
        return session.getOrder();
    }

    /**
     * Kiểm tra session có đang mở không
     */
    public boolean isSessionOpen(Long sessionId) {
        Session session = getSessionById(sessionId);
        return !session.isClosed();
    }

    /**
     * Đóng session sau khi thanh toán thành công
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = Exception.class)
    public void closeSession(Long sessionId) {
        Session session = getSessionById(sessionId);
        if (session.isClosed()) {
            throw new RuntimeException("Session đã được đóng");
        }

        // Lấy table ID trước khi đóng session
        Integer tableId = null;
        try {
            tableId = session.getTable().getId();
        } catch (Exception e) {
            System.err.println("Error getting table ID: " + e.getMessage());
            // Không throw exception để không làm gián đoạn quá trình thanh toán
        }

        try {
            session.setClosed(true);
            session.setClosedAt(LocalDateTime.now());
            sessionRepository.save(session);
        } catch (Exception e) {
            System.err.println("Error updating session status: " + e.getMessage());
            // Không throw exception để không làm gián đoạn quá trình thanh toán
        }

        // Cập nhật trạng thái order (không quan trọng, không làm fail transaction chính)
        if (session.getOrder() != null) {
            try {
                Order order = session.getOrder();
                order.setOrderStatus(Order.OrderStatus.COMPLETED);
                order.setUpdatedAt(LocalDateTime.now());
                orderRepository.save(order);
            } catch (Exception e) {
                System.err.println("Error updating order status: " + e.getMessage());
                // Không throw exception để không làm gián đoạn quá trình thanh toán
            }
        }

        // Giải phóng bàn về trạng thái AVAILABLE (không quan trọng, không làm fail transaction chính)
        try {
            tableService.releaseTable(tableId, null); // HttpSession có thể null vì đây là từ payment flow
        } catch (Exception e) {
            // Log error nhưng không throw để không làm gián đoạn quá trình thanh toán
            System.err.println("Error releasing table " + tableId + ": " + e.getMessage());
        }
    }

    /**
     * Lấy session đang mở của bàn
     */
    public Optional<Session> getOpenSessionByTableId(Long tableId) {
        return sessionRepository.findByTableIdAndIsClosedFalse(tableId.intValue());
    }
}
