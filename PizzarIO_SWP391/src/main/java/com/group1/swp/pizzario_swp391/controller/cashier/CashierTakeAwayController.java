package com.group1.swp.pizzario_swp391.controller.cashier;

import com.group1.swp.pizzario_swp391.annotation.CashierUrl;
import com.group1.swp.pizzario_swp391.dto.payment.PaymentDTO;
import com.group1.swp.pizzario_swp391.entity.Order;
import com.group1.swp.pizzario_swp391.entity.ProductSize;
import com.group1.swp.pizzario_swp391.entity.Staff;
import com.group1.swp.pizzario_swp391.mapper.PaymentMapper;
import com.group1.swp.pizzario_swp391.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@CashierUrl
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CashierTakeAwayController {

    CategoryService categoryService;
    ProductService productService;
    CartService cartService;
    OrderService orderService;
    StaffService staffService;
    PaymentMapper paymentMapper;
    PaymentService paymentService;
    SessionService sessionService;
    ProductSizeService productSizeService;

    @GetMapping("/takeaway")
    public String takeawayMenu(Model model, Principal principal, HttpSession httpSession) {
        Staff staff = staffService.findByEmail(principal.getName());
        model.addAttribute("staff", staff);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("cartItems", cartService.getCartForView(httpSession));
        return "cashier-page/takeaway";
    }

    @GetMapping("/product/detail/{id}")
    public String getProductDetail(@PathVariable("id") Long productId,
                                   Model model) {
        List<ProductSize> productSizes = productSizeService.findByProductId(productId);
        var product = productService.getProductById(productId);
        model.addAttribute("product", product);
        model.addAttribute("productSizes", productSizes);
        return "cashier-page/fragments/product-detail-takeaway :: detail-content-takeaway";
    }

    @PostMapping("/takeaway/rollback")
    public String rollback(HttpSession session) {
        cartService.clearCart(session);
        return "redirect:/cashier";
    }

    @PostMapping("/takeaway/cart/add")
    public String addToCart(@RequestParam("productId") Long productId,
                            @RequestParam(value = "quantity", defaultValue = "1") Integer quantity,
                            @RequestParam(value = "note", defaultValue = "") String note,
                            @RequestParam(value = "productSizeId", required = false) Long productSizeId,
                            HttpSession session) {
        cartService.addToCart(session, productId, quantity, note, productSizeId);
        return "redirect:/cashier/takeaway";
    }

    @PostMapping("/takeaway/cart/update")
    public String updateCartItem(@RequestParam("productId") Long productId,
                                 @RequestParam("quantity") int quantity,
                                 @RequestParam(value = "note", required = false) String note,
                                 @RequestParam(value = "newProductSizeId", required = false) Long newProductSizeId,
                                 @RequestParam(value = "oldProductSizeId", required = false) Long oldProductSizeId,
                                 HttpSession session) {
        cartService.updateCartItem(session, productId, quantity, note, newProductSizeId, oldProductSizeId);
        return "redirect:/cashier/takeaway";
    }

    @PostMapping("/takeaway/cart/remove")
    public String removeFromCart(@RequestParam("productId") Long productId,
                                 @RequestParam(value = "productSizeId", required = false) Long productSizeId,
                                 HttpSession session) {
        cartService.removeFromCart(session, productId, productSizeId);
        return "redirect:/cashier/takeaway";
    }

    @PostMapping("/takeaway/order/place")
    public String placeTakeAway(HttpSession session,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {
        Staff staff = staffService.findByEmail(principal.getName());
        var order = orderService.placeTakeAwayOrder(session, staff);
        if (order == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Giỏ hàng trống.");
            return "redirect:/cashier/takeaway";
        }
        redirectAttributes.addFlashAttribute("successMessage", "Đã tạo đơn Take-away #" + order.getId());
        return "redirect:/cashier/takeaway";
    }

    @PostMapping("/takeaway/order/place-and-payment")
    public String placeAndPayment(HttpSession session,
                                  Principal principal,
                                  RedirectAttributes redirectAttributes) {
        Staff staff = staffService.findByEmail(principal.getName());
        var order = orderService.placeTakeAwayOrder(session, staff);
        if (order == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Giỏ hàng trống.");
            return "redirect:/cashier/takeaway";
        }
        redirectAttributes.addFlashAttribute("successMessage", "Đã tạo đơn Take-away #" + order.getId());
        return "redirect:/cashier/takeaway/order/" + order.getId() + "/payment";
    }

    @GetMapping("/takeaway/order/{orderId}/payment")
    public String showTakeAwayPayment(@PathVariable Long orderId,
                                     Model model,
                                     Principal principal,
                                     RedirectAttributes redirectAttributes,
                                     HttpServletRequest request,
                                     jakarta.servlet.http.HttpServletResponse response) {
        Staff staff = staffService.findByEmail(principal.getName());
        var order = orderService.getOrderById(orderId);
        if (order == null || order.getOrderType() != Order.OrderType.TAKE_AWAY) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đơn hàng Take-away này.");
            return "redirect:/cashier/takeaway";
        }
        
        if (order.getPaymentStatus() == Order.PaymentStatus.PAID) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đơn hàng này đã được thanh toán.");
            return "redirect:/cashier/takeaway";
        }
        
        // Tạo hoặc lấy session cho take-away order
        var session = sessionService.getOrCreateSessionForTakeAwayOrder(order);
        Long sessionId = session.getId();

        // Validate sessionId
        if (sessionId == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: Không thể tạo session cho đơn hàng.");
            return "redirect:/cashier/takeaway";
        }

        // Lấy PaymentDTO từ PaymentService để có đầy đủ thông tin (voucher, points, etc.)
        PaymentDTO paymentDTO = paymentService.getPaymentBySessionId(sessionId);

        // Lấy danh sách voucher có thể áp dụng
        var availableVouchers = paymentService.getAvailableVouchersBySessionId(sessionId);

        // Lấy chi tiết order items
        var orderItems = paymentService.getOrderItemsBySessionId(sessionId);

        // Lấy thông tin membership
        var membership = paymentService.getMembershipBySessionId(sessionId);

        // Tính các giá trị
        double originalTotal = paymentService.calculateOriginalOrderTotal(sessionId);
        double discountAmount = paymentService.calculateDiscountAmount(sessionId);
        double finalTotal = originalTotal - discountAmount;

        // Điều chỉnh PaymentDTO cho take-away
        paymentDTO.setTableNumber(null); // Take-away không có bàn

        model.addAttribute("payment", paymentDTO);
        model.addAttribute("orderItems", orderItems);
        model.addAttribute("order", order); // Thêm order vào model cho template
        model.addAttribute("orderId", orderId);
        model.addAttribute("sessionId", sessionId); // Truyền sessionId để dùng trong forms
        model.addAttribute("tableId", null);

        model.addAttribute("originalTotal", originalTotal);
        model.addAttribute("discountAmount", discountAmount);
        model.addAttribute("finalTotal", finalTotal);

        model.addAttribute("availableVouchers", availableVouchers);
        model.addAttribute("membership", membership);
        model.addAttribute("pointsUsed", paymentDTO != null ? paymentDTO.getPointsUsed() : 0);
        model.addAttribute("maxUsablePoints", paymentService.getMaxUsablePoints(sessionId));

        // Get context path
        String contextPath = request.getContextPath();

        // Add context path to model for template use
        model.addAttribute("contextPath", contextPath);

        // Confirm URL - sử dụng orderId cho cashier flow (không cần sessionId)
        String paymentConfirmUrl = contextPath + "/cashier/takeaway/order/" + orderId + "/payment/confirm";
        model.addAttribute("paymentConfirmUrl", paymentConfirmUrl);

        // Action URL cho voucher/membership/points actions
        String paymentActionUrl = contextPath + "/cashier/takeaway/order/" + orderId + "/payment/action";
        model.addAttribute("paymentActionUrl", paymentActionUrl);
        model.addAttribute("isCashierFlow", true); // Đánh dấu để template biết dùng paymentActionUrl

        // Thông tin staff và back URL
        model.addAttribute("staff", staff);
        model.addAttribute("backUrl", "/cashier/takeaway");
        model.addAttribute("orderTypeLabel", "Take-away");

        // Set no-cache headers to discourage going back to previous state
        try {
            response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
            response.addHeader("Cache-Control", "post-check=0, pre-check=0");
            response.setHeader("Pragma", "no-cache");
            response.setDateHeader("Expires", 0);
        } catch (Exception ignored) {}

        // Tái sử dụng guest payment.html
        return "guest-page/payment";
    }

    @PostMapping("/takeaway/order/{orderId}/payment/confirm")
    public String confirmTakeAwayPayment(@PathVariable Long orderId,
                                        @RequestParam String paymentMethod,
                                        RedirectAttributes redirectAttributes) {
        var order = orderService.getOrderById(orderId);
        if (order == null || order.getOrderType() != Order.OrderType.TAKE_AWAY) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đơn hàng Take-away này.");
            return "redirect:/cashier/takeaway";
        }

        if (order.getPaymentStatus() == Order.PaymentStatus.PAID) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đơn hàng này đã được thanh toán.");
            return "redirect:/cashier/takeaway";
        }

        try {
            // Lấy hoặc tạo session cho order
            var session = sessionService.getOrCreateSessionForTakeAwayOrder(order);
            Long sessionId = session.getId();

            order = orderService.getOrderById(orderId); // Reload order
            if (order.getSession() == null || !order.getSession().getId().equals(sessionId)) {
                throw new RuntimeException("Order chưa được link với session. SessionId: " + sessionId);
            }

            Order.PaymentMethod method = Order.PaymentMethod.valueOf(paymentMethod);
            paymentService.confirmPaymentTakeawayBySessionId(sessionId, method);
            
            order = orderService.getOrderById(orderId);
            orderService.saveOrder(order);

            return "redirect:/cashier/takeaway/order/" + orderId + "/payment/confirmation";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Phương thức thanh toán không hợp lệ.");
            return "redirect:/cashier/takeaway/order/" + orderId + "/payment";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi thanh toán: " + e.getMessage());
            return "redirect:/cashier/takeaway/order/" + orderId + "/payment";
        }
    }

    /**
     * Trang xác nhận thanh toán cho đơn Take-away
     * Tái sử dụng template payment-confirmation.html của guest
     */
    @GetMapping("/takeaway/order/{orderId}/payment/confirmation")
    public String showTakeAwayPaymentConfirmation(@PathVariable Long orderId,
                                                  Model model,
                                                  Principal principal,
                                                  RedirectAttributes redirectAttributes,
                                                  HttpServletRequest request) {
        var order = orderService.getOrderById(orderId);

        if (order == null || order.getOrderType() != Order.OrderType.TAKE_AWAY) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đơn hàng Take-away này.");
            return "redirect:/cashier/takeaway";
        }

        if (order.getPaymentStatus() != Order.PaymentStatus.PAID) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đơn hàng này chưa được thanh toán.");
            return "redirect:/cashier/takeaway/order/" + orderId + "/payment";
        }

        // Lấy session từ order
        var session = sessionService.getSessionByOrder(order);
        Long sessionId = session.getId();

        // Lấy PaymentDTO từ PaymentService để có đầy đủ thông tin
        PaymentDTO paymentDTO = paymentService.getPaymentConfirmationBySessionId(sessionId);

        // Lấy chi tiết order items
        var orderItems = paymentService.getOrderItemsBySessionId(sessionId);

        // Lấy thông tin membership
        var membership = paymentService.getMembershipBySessionId(sessionId);

        // Tính các giá trị
        double originalTotal = paymentService.calculateOriginalOrderTotal(sessionId);
        double discountAmount = paymentService.calculateDiscountAmount(sessionId);
        double finalTotal = originalTotal - discountAmount;

        // Điều chỉnh PaymentDTO cho take-away
        paymentDTO.setTableNumber(null); // Take-away không có bàn

        model.addAttribute("payment", paymentDTO);
        model.addAttribute("orderItems", orderItems);
        model.addAttribute("orderId", orderId);
        model.addAttribute("sessionId", sessionId);
        model.addAttribute("tableId", null);

        model.addAttribute("originalTotal", originalTotal);
        model.addAttribute("discountAmount", discountAmount);
        model.addAttribute("finalTotal", finalTotal);

        model.addAttribute("membership", membership);

        // Thêm backUrl cho cashier flow - về trang cashier dashboard
        // Get context path for backUrl
        String contextPath = request.getContextPath();
        String backUrl = contextPath + "/cashier";
        model.addAttribute("backUrl", backUrl);
        model.addAttribute("isCashierFlow", true); // Set boolean
        model.addAttribute("isCashierFlowStr", "true"); // Set string for JS compatibility
        model.addAttribute("redirectTarget", "cashier"); // Rõ ràng: redirect về cashier cho take-away

        // Debug: log values
        System.out.println("Payment confirmation - backUrl: " + backUrl);
        System.out.println("Payment confirmation - isCashierFlow: true");
        System.out.println("Payment confirmation - redirectTarget: cashier");

        return "guest-page/payment-confirmation";
    }

    /**
     * Xử lý POST request cho payment page (apply/remove voucher, verify membership, apply points)
     * Dựa trên orderId, nhưng sử dụng sessionId để tương thích với PaymentService
     */
    @PostMapping("/takeaway/order/{orderId}/payment/action")
    public String handlePaymentAction(@PathVariable Long orderId,
                                    @RequestParam String action,
                                    @RequestParam(required = false) Long voucherId,
                                    @RequestParam(required = false) String phoneNumber,
                                    @RequestParam(required = false) Integer points,
                                    RedirectAttributes redirectAttributes) {
        var order = orderService.getOrderById(orderId);
        if (order == null || order.getOrderType() != Order.OrderType.TAKE_AWAY) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đơn hàng Take-away này.");
            return "redirect:/cashier/takeaway";
        }

        if (order.getPaymentStatus() == Order.PaymentStatus.PAID) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đơn hàng này đã được thanh toán.");
            return "redirect:/cashier/takeaway";
        }

        // Lấy hoặc tạo session cho order
        var session = sessionService.getOrCreateSessionForTakeAwayOrder(order);
        Long sessionId = session.getId();

        try {
            if ("apply-voucher".equals(action) && voucherId != null) {
                paymentService.applyVoucherBySessionId(sessionId, voucherId);
                redirectAttributes.addFlashAttribute("successMessage", "Voucher đã được áp dụng thành công!");
            } else if ("remove-voucher".equals(action)) {
                paymentService.removeVoucherBySessionId(sessionId);
                redirectAttributes.addFlashAttribute("successMessage", "Voucher đã được hủy áp dụng!");
            } else if ("verify-membership".equals(action) && phoneNumber != null && !phoneNumber.trim().isEmpty()) {
                // Verify membership và gán vào order
                var membership = paymentService.findMembershipByPhoneNumber(phoneNumber.trim());
                if (membership != null) {
                    paymentService.assignMembershipToSession(sessionId, membership.getId());
                    redirectAttributes.addFlashAttribute("successMessage", "Đã xác nhận thành viên thành công!");
                } else {
                    redirectAttributes.addFlashAttribute("errorMessage",
                        "Không tìm thấy thành viên với số điện thoại này. Vui lòng đăng ký thành viên.");
                }
            } else if ("apply-points".equals(action) && points != null) {
                paymentService.applyPoints(sessionId, points);
                redirectAttributes.addFlashAttribute("successMessage", "Đã áp dụng điểm thành công!");
            } else if ("remove-points".equals(action)) {
                paymentService.removePoints(sessionId);
                redirectAttributes.addFlashAttribute("successMessage", "Đã hủy sử dụng điểm!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Hành động không hợp lệ");
            }

            return "redirect:/cashier/takeaway/order/" + orderId + "/payment";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
            return "redirect:/cashier/takeaway/order/" + orderId + "/payment";
        }
    }
}



