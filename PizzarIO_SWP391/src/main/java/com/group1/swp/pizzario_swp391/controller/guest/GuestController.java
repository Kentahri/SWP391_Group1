package com.group1.swp.pizzario_swp391.controller.guest;

import com.group1.swp.pizzario_swp391.dto.websocket.TableReleaseRequest;
import com.group1.swp.pizzario_swp391.dto.websocket.TableSelectionRequest;
import com.group1.swp.pizzario_swp391.entity.Order;
import com.group1.swp.pizzario_swp391.entity.ProductSize;
import com.group1.swp.pizzario_swp391.entity.Session;
import com.group1.swp.pizzario_swp391.repository.SessionRepository;
import com.group1.swp.pizzario_swp391.service.*;
import jakarta.servlet.http.HttpSession;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Controller
@RequestMapping("/guest")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class GuestController{

    CategoryService categoryService;
    ProductService productService;
    TableService tableService;
    CartService cartService;
    OrderService orderService;
    SessionRepository sessionRepository;
    ProductSizeService productSizeService;
    private final OrderItemService orderItemService;

    @GetMapping
    public String guestPage(Model model, HttpSession session) {
        // clear gio hang cua khach cu khi co khach moi den
        session.invalidate();
        model.addAttribute("tables", tableService.getAllTablesForGuest());
        return "guest-page/guest";
    }

    /**
     * Handle table selection from guest tablet
     * Guest sends: { tableId, sessionId, guestCount }
     * Response sent to: /queue/guest-{sessionId}
     */
    @MessageMapping("/guest/table/select")
    public void selectTable(TableSelectionRequest request) {
        log.info("Guest {} requesting table {}", request.getSessionId(), request.getTableId());
        tableService.handleTableSelection(request);
    }

    @GetMapping("/menu")
    public String viewMenu(@RequestParam("sessionId") Long sessionId,
                           @RequestParam("tableId") Integer tableId,
                           HttpSession session,
                           Model model, RedirectAttributes redirectAttributes) {
        Optional<Session> sessionOpt = sessionRepository.findById(sessionId);

        // 1. Kiểm tra session có tồn tại không
        if (sessionOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Phiên làm việc không hợp lệ.");
            return "redirect:/guest";
        }

        Session currentSession = sessionOpt.get();

        // 2. Kiểm tra session đã đóng chưa
        if (currentSession.isClosed()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Phiên làm việc đã kết thúc. Vui lòng chọn bàn khác.");
            return "redirect:/guest";
        }

        // 3. Kiểm tra session có đúng của bàn không
        if (currentSession.getTable() == null || currentSession.getTable().getId() != tableId) {
            redirectAttributes.addFlashAttribute("errorMessage", "Phiên làm việc không khớp với bàn này.");
            return "redirect:/guest";
        }
        session.setAttribute("sessionId", sessionId);
        session.setAttribute("tableId", tableId);
        model.addAttribute("sessionId", sessionId);
        model.addAttribute("tableId", tableId);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("cartItems", cartService.getCartForView(session));
        model.addAttribute("orderedItems", orderService.getOrderedItemsForView(sessionId));

        return "guest-page/view_menu";
    }

    @GetMapping("/product/detail/{id}")
    public String getProductDetail(@PathVariable("id") Long productId,
                                   @RequestParam("sessionId") Long sessionId,
                                   @RequestParam("tableId") Integer tableId,
                                   Model model) {
        List<ProductSize> productSizes = productSizeService.findByProductId(productId);
        var product = productService.getProductById(productId);
        model.addAttribute("product", product);
        model.addAttribute("sessionId", sessionId);
        model.addAttribute("productSizes", productSizes);
        model.addAttribute("tableId", tableId);
        return "guest-page/fragments/product_detail :: detail-content";
    }

    @PostMapping("/cart/add")
    public String addToCart(@RequestParam("sessionId") Long sessionId,
                            @RequestParam("tableId") Integer tableId,
                            @RequestParam("productId") Long productId,
                            @RequestParam("quantity") Integer quantity,
                            @RequestParam(value = "productSizeId", required = false) Long productSizeId,
                            @RequestParam("note") String note,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        cartService.addToCart(session, productId, quantity, note, productSizeId);
        redirectAttributes.addAttribute("sessionId", sessionId);
        redirectAttributes.addAttribute("tableId", tableId);
        return "redirect:/guest/menu";
    }

    @PostMapping("/cart/update")
    public String updateCartItem(@RequestParam("sessionId") Long sessionId,
                                 @RequestParam("tableId") Integer tableId,
                                 @RequestParam("productId") Long productId,
                                 @RequestParam("quantity") int quantity,
                                 @RequestParam(value = "note", required = false) String note,
                                 @RequestParam(value = "newProductSizeId", required = false) Long newProductSizeId,
                                 @RequestParam(value = "oldProductSizeId", required = false) Long oldProductSizeId, HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        cartService.updateCartItem(session, productId, quantity, note, newProductSizeId, oldProductSizeId);
        redirectAttributes.addAttribute("sessionId", sessionId);
        redirectAttributes.addAttribute("tableId", tableId);
        return "redirect:/guest/menu";
    }

    @PostMapping("/cart/remove")
    public String removeFromCart(@RequestParam("sessionId") Long sessionId,
                                 @RequestParam("tableId") Integer tableId,
                                 @RequestParam("productId") Long productId,
                                 @RequestParam("productSizeId") Long productSizeId,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        cartService.removeFromCart(session, productId, productSizeId);
        redirectAttributes.addAttribute("sessionId", sessionId);
        redirectAttributes.addAttribute("tableId", tableId);
        return "redirect:/guest/menu";
    }

    @PostMapping("/order/cancel")
    public String cancelOrder(@RequestParam("orderItemId") Long orderItemId,
                              @RequestParam("sessionId") Long sessionId,
                              @RequestParam("tableId") Integer tableId,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        orderItemService.cancelOrderItem(orderItemId);
        model.addAttribute("orderedItems", orderService.getOrderedItemsForView(sessionId));
        redirectAttributes.addAttribute("sessionId", sessionId);
        redirectAttributes.addAttribute("tableId", tableId);
        return "redirect:/guest/menu";
    }

    @PostMapping("/order/place")
    public String placeOrder(@RequestParam("sessionId") Long sessionId,
                             @RequestParam("tableId") Integer tableId,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        orderService.placeOrder(session, sessionId);
        redirectAttributes.addAttribute("sessionId", sessionId);
        redirectAttributes.addAttribute("tableId", tableId);
        return "redirect:/guest/menu";
    }

    @MessageMapping("/guest/table/release")
    public void releaseTable(TableReleaseRequest request) {
        log.info("Guest {} requesting to release table {}", request.getSessionId(), request.getTableId());
        tableService.handleTableRelease(request);
    }

    /**
     * API để lấy orderId hiện tại của session (cho WebSocket filtering)
     */
    @GetMapping("/order/current-order-id")
    @ResponseBody
    public Map<String, Object> getCurrentOrderId(@RequestParam("sessionId") Long sessionId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Order order = orderService.getOrderForSession(sessionId);
            if (order != null) {
                response.put("orderId", order.getId());
                response.put("success", true);
            } else {
                response.put("success", false);
                response.put("message", "No order found");
            }
        } catch (Exception e) {
            log.error("Error getting order ID for session {}", sessionId, e);
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }
        return response;
    }

    /**
     * API để lấy ordered items HTML fragment (cho real-time update)
     */
    @GetMapping("/order/ordered-items")
    public String getOrderedItemsFragment(@RequestParam("sessionId") Long sessionId,
                                         HttpSession session,
                                         Model model) {
        model.addAttribute("orderedItems", orderService.getOrderedItemsForView(sessionId));
        model.addAttribute("sessionId", sessionId);
        // Lấy tableId từ session attribute hoặc từ order
        Integer tableId = (Integer) session.getAttribute("tableId");
        if (tableId == null) {
            Order order = orderService.getOrderForSession(sessionId);
            if (order != null && order.getSession() != null && order.getSession().getTable() != null) {
                tableId = order.getSession().getTable().getId();
            }
        }
        model.addAttribute("tableId", tableId);
        return "guest-page/fragments/view_order :: order-view";
    }
}