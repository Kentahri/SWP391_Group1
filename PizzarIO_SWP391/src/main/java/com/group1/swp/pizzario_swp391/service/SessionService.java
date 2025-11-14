package com.group1.swp.pizzario_swp391.service;

import com.group1.swp.pizzario_swp391.entity.Order;
import com.group1.swp.pizzario_swp391.entity.Session;
import com.group1.swp.pizzario_swp391.repository.OrderRepository;
import com.group1.swp.pizzario_swp391.repository.SessionRepository;
import com.group1.swp.pizzario_swp391.repository.TableRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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
        // Flush để đảm bảo tất cả changes được persist trước khi query
        orderRepository.flush();
        // Query Order trực tiếp từ session_id để tránh vấn đề lazy load
        Order order = orderRepository.findBySessionId(sessionId);
        if (order == null) {
            throw new RuntimeException("Session không có order. SessionId: " + sessionId);
        }
        return order;
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

        // Giải phóng bàn về trạng thái AVAILABLE
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

    /**
     * Tạo session cho take-away order (không có table)
     * Hoặc trả về session hiện có nếu order đã có session
     */
    @Transactional
    public Session getOrCreateSessionForTakeAwayOrder(Order order) {
        // Nếu order đã có session, refresh và trả về
        if (order.getSession() != null && order.getSession().getId() != null) {
            Session existingSession = sessionRepository.findById(order.getSession().getId())
                    .orElseThrow(() -> new RuntimeException("Session không tồn tại"));
            // Reload order để đảm bảo session có order
            orderRepository.flush(); // Đảm bảo tất cả changes được persist
            return existingSession;
        }
        
        // Tạo session mới cho take-away order
        Session session = new Session();
        session.setClosed(false);
        session.setCreatedAt(LocalDateTime.now());
        session.setTable(null); // Take-away không có table
        
        // Lưu session trước để có ID
        session = sessionRepository.save(session);
        sessionRepository.flush(); // Flush để có ID ngay lập tức
        
        // Link order với session (Order là chủ sở hữu với @JoinColumn)
        order.setSession(session);
        
        // Lưu order (vì Order quản lý foreign key)
        order = orderRepository.save(order);
        orderRepository.flush(); // Flush để đảm bảo foreign key được lưu
        
        // Verify order có session_id trong database
        Order savedOrder = orderRepository.findById(order.getId())
                .orElseThrow(() -> new RuntimeException("Không thể reload order"));
        if (savedOrder.getSession() == null || !savedOrder.getSession().getId().equals(session.getId())) {
            throw new RuntimeException("Order chưa được link với session");
        }
        
        return session;
    }

    /**
     * Lấy session từ order (cho cả take-away và dine-in)
     */
    public Session getSessionByOrder(Order order) {
        if (order.getSession() == null) {
            throw new RuntimeException("Order không có session");
        }
        return order.getSession();
    }
}
