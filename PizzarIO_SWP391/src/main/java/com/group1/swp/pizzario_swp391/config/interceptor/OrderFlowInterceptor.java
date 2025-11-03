package com.group1.swp.pizzario_swp391.config.interceptor;

import com.group1.swp.pizzario_swp391.entity.Order;
import com.group1.swp.pizzario_swp391.repository.OrderRepository;
import com.group1.swp.pizzario_swp391.service.OrderService;
import com.group1.swp.pizzario_swp391.service.TableService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)

public class OrderFlowInterceptor implements HandlerInterceptor{

    OrderService orderService;
    TableService tableService;
    OrderRepository orderRepository;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        String path = request.getRequestURI().substring(request.getContextPath().length());
        System.out.println(path);
        // Nếu không có session web => cho vào /guest bình thường
        if (session == null) return true;

        Long activeSessionId = (Long) session.getAttribute("sessionId");
        Integer activeTableId = (Integer) session.getAttribute("tableId");

        // Nếu chưa có bàn đang hoạt động => cho phép truy cập
        if (activeSessionId == null || activeTableId == null) return true;

        // Nếu đang cố quay lại /guest khi còn order chưa thanh toán
        if (path.equals("/guest")) {
            Order order = orderService.getOrderForSession(activeSessionId);
            // nếu chưa có order thì cho phép quay về chọn bàn
            if (order == null) {
                tableService.releaseTable(activeTableId, session);
                return true;
            }
            double total = order.getTotalPrice();
            // nếu có order và price > 0 thì không cho phép về chọn bàn
            if (total > 0) {
                response.sendRedirect(request.getContextPath() + "/guest/menu?sessionId=" + activeSessionId + "&tableId=" + activeTableId);
                return false;
            } else {
                // Nếu không còn order => clear session
                order.setOrderStatus(Order.OrderStatus.CANCELLED);
                order.setUpdatedAt(LocalDateTime.now());
                orderRepository.save(order);
                tableService.releaseTable(activeTableId, session);
                return true;
            }
        }

        // Nếu đang trong menu nhưng sessionId hoặc tableId khác
        String sessionParam = request.getParameter("sessionId");
        String tableParam = request.getParameter("tableId");

        if (path.startsWith("/guest/menu")) {
            if (sessionParam != null && tableParam != null) {
                if (!sessionParam.equals(String.valueOf(activeSessionId)) ||
                        !tableParam.equals(String.valueOf(activeTableId))) {
                    response.sendRedirect(request.getContextPath() + "/guest/menu?sessionId=" + activeSessionId + "&tableId=" + activeTableId);
                    return false;
                }
            }
        }

        return true;
    }
}
