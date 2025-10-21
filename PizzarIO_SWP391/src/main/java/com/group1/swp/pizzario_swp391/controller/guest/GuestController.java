package com.group1.swp.pizzario_swp391.controller.guest;

import com.group1.swp.pizzario_swp391.entity.Session;
import com.group1.swp.pizzario_swp391.repository.SessionRepository;
import com.group1.swp.pizzario_swp391.service.*;
import jakarta.servlet.http.HttpSession;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

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

    @GetMapping
    public String guestPage(Model model) {
        model.addAttribute("tables", tableService.getAllTables());
        return "guest-page/guest";
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

    @PostMapping("/cart/add")
    public String addToCart(@RequestParam("sessionId") Long sessionId,
                            @RequestParam("tableId") Integer tableId,
                            @RequestParam("productId") Long productId,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        cartService.addToCart(session, productId, 1);
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

    @PostMapping("/table/release")
    public String releaseTable(@RequestParam("tableId") Integer tableId,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        try {
            tableService.releaseTable(tableId, session);
            // Optionally, add a success message to be displayed on the guest page
            redirectAttributes.addFlashAttribute("releaseSuccess", "Bàn " + tableId + " đã được giải phóng. Cảm ơn quý khách!");
        } catch (Exception e) {
            // Optionally, handle errors
            redirectAttributes.addFlashAttribute("releaseError", "Lỗi: không thể giải phóng bàn.");
        }
        return "redirect:/guest";
    }
}