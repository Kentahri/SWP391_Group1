package com.group1.swp.pizzario_swp391.controller.guest;

import com.group1.swp.pizzario_swp391.dto.websocket.TableReleaseRequest;
import com.group1.swp.pizzario_swp391.dto.websocket.TableSelectionRequest;
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
    private final GuestService guestService;

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
        var product = productService.getProductById(productId);
        model.addAttribute("product", product);
        model.addAttribute("sessionId", sessionId);
        model.addAttribute("tableId", tableId);
        return "guest-page/fragments/product_detail :: detail-content";
    }

    @PostMapping("/cart/add")
    public String addToCart(@RequestParam("sessionId") Long sessionId,
                            @RequestParam("tableId") Integer tableId,
                            @RequestParam("productId") Long productId,
                            @RequestParam("quantity") Integer quantity,
                            @RequestParam("note") String note,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        cartService.addToCart(session, productId, quantity, note);
        redirectAttributes.addAttribute("sessionId", sessionId);
        redirectAttributes.addAttribute("tableId", tableId);
        return "redirect:/guest/menu";
    }

    @PostMapping("/cart/update")
    public String updateCartItem(@RequestParam("sessionId") Long sessionId,
                                 @RequestParam("tableId") Integer tableId,
                                 @RequestParam("productId") Long productId,
                                 @RequestParam("quantity") int quantity,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        cartService.updateCartItem(session, productId, quantity);
        redirectAttributes.addAttribute("sessionId", sessionId);
        redirectAttributes.addAttribute("tableId", tableId);
        return "redirect:/guest/menu";
    }

    @PostMapping("/cart/remove")
    public String removeFromCart(@RequestParam("sessionId") Long sessionId,
                                 @RequestParam("tableId") Integer tableId,
                                 @RequestParam("productId") Long productId,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        cartService.removeFromCart(session, productId);
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
}