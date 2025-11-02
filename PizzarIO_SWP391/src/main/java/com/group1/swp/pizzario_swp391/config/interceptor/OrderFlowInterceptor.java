package com.group1.swp.pizzario_swp391.config.interceptor;

import com.group1.swp.pizzario_swp391.entity.Order;
import com.group1.swp.pizzario_swp391.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class OrderFlowInterceptor implements HandlerInterceptor{

    private final OrderService orderService;

    public OrderFlowInterceptor(OrderService orderService) {
        this.orderService = orderService;
    }

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
        if (path.startsWith("/guest") && !path.contains("/menu") && !path.contains("/payment")) {
            Order order = orderService.getOrderForSession(activeSessionId);
            // nếu chưa có order thì cho phép quay về chọn bàn
            if (order == null) {
                session.removeAttribute("sessionId");
                session.removeAttribute("tableId");
                return true;
            }
            double total = order.getTotalPrice();
            // nếu có order và price > 0 thì không cho phép về chọn bàn
            if (total > 0) {
                response.sendRedirect(request.getContextPath() + "/guest/menu?sessionId=" + activeSessionId + "&tableId=" + activeTableId);
                return false;
            } else {
                // Nếu không còn order => clear session
                session.removeAttribute("sessionId");
                session.removeAttribute("tableId");
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
