package com.group1.swp.pizzario_swp391.controller.guest;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.group1.swp.pizzario_swp391.dto.payment.PaymentDTO;
import com.group1.swp.pizzario_swp391.dto.voucher.VoucherDTO;
import com.group1.swp.pizzario_swp391.entity.Membership;
import com.group1.swp.pizzario_swp391.entity.Order;
import com.group1.swp.pizzario_swp391.entity.OrderItem;
import com.group1.swp.pizzario_swp391.service.PaymentService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/guest/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Test endpoint để kiểm tra mapping
     */
    @GetMapping("/test")
    public String testEndpoint() {
        return "Test endpoint works!";
    }

    /**
     * Test endpoint để kiểm tra session mapping
     */
    @GetMapping("/session/{sessionId}/test")
    public String testSessionEndpoint(@PathVariable Long sessionId) {
        return "Session test endpoint works! SessionId: " + sessionId;
    }

    /**
     * Xử lý trường hợp không có sessionId - redirect về trang chủ
     */
    @GetMapping
    public String showPaymentPageWithoutSession(@RequestParam(required = false) String error,
                                               @RequestParam(required = false) String success,
                                               Model model) {
        // Redirect về trang chủ thay vì membership verify
        return "redirect:/guest?error=" + 
               URLEncoder.encode("Vui lòng chọn bàn và đặt món trước khi thanh toán", StandardCharsets.UTF_8);
    }

    /**
     * Hiển thị trang thanh toán theo session ID với đầy đủ thông tin order và voucher
     */
    @GetMapping("/session/{sessionId}")
    public String showPaymentPageBySession(@PathVariable("sessionId") Long sessionId, 
                                         @RequestParam(required = false) String error,
                                         @RequestParam(required = false) String success,
                                         @RequestParam(required = false) String membershipVerified,
                                         @RequestParam(required = false) String membershipRegistered,
                                         Model model,
                                         HttpServletRequest request) {
        System.out.println("=== PaymentController Debug ===");
        System.out.println("Received sessionId: " + sessionId);
        System.out.println("SessionId type: " + (sessionId != null ? sessionId.getClass().getSimpleName() : "null"));
        System.out.println("Error param: " + error);
        System.out.println("Success param: " + success);
        System.out.println("Request URL: " + request.getRequestURL());
        System.out.println("Request URI: " + request.getRequestURI());
        System.out.println("Query String: " + request.getQueryString());
        
        try {
            // Validate sessionId
            if (sessionId == null) {
                System.out.println("SessionId is null, returning error page");
                model.addAttribute("error", "Session ID không hợp lệ");
                return "error-page";
            }
            // Lấy thông tin payment đầy đủ từ session
            PaymentDTO payment = paymentService.getPaymentBySessionId(sessionId);
            
            // Lấy danh sách voucher có thể áp dụng cho order này
            List<VoucherDTO> availableVouchers = paymentService.getAvailableVouchersBySessionId(sessionId);
            
            // Lấy chi tiết các món đã order
            List<OrderItem> orderItems = paymentService.getOrderItemsBySessionId(sessionId);
            
            // Lấy thông tin khách hàng nếu có membership
            Membership membership = paymentService.getMembershipBySessionId(sessionId);
            
            // Tính tổng giá trị order trước khi áp dụng voucher
            double originalTotal = paymentService.calculateOriginalOrderTotal(sessionId);
            
            // Tính giá trị giảm giá từ voucher (nếu có)
            double discountAmount = paymentService.calculateDiscountAmount(sessionId);
            
            // Tính tổng cuối cùng sau khi áp dụng voucher
            double finalTotal = originalTotal - discountAmount;
            
            // Thêm các thông tin vào model
            model.addAttribute("payment", payment);
            model.addAttribute("availableVouchers", availableVouchers);
            model.addAttribute("orderItems", orderItems);
            model.addAttribute("membership", membership);
            model.addAttribute("sessionId", sessionId);
            model.addAttribute("tableId", payment.getTableNumber()); // Add tableId for reference
            model.addAttribute("orderId", payment.getOrderId());
            model.addAttribute("originalTotal", originalTotal);
            model.addAttribute("discountAmount", discountAmount);
            model.addAttribute("finalTotal", finalTotal);
            
            // Points info for UI - đảm bảo pointsUsed luôn có giá trị (không null)
            Integer pointsUsed = payment.getPointsUsed() != null ? payment.getPointsUsed() : 0;
            System.out.println("=== PaymentController Debug ===");
            System.out.println("sessionId: " + sessionId);
            System.out.println("payment.getPointsUsed(): " + payment.getPointsUsed());
            System.out.println("pointsUsed (after null check): " + pointsUsed);
            model.addAttribute("pointsUsed", pointsUsed);
            model.addAttribute("maxUsablePoints", paymentService.getMaxUsablePoints(sessionId));
            
            // Get context path for template use
            String contextPath = request.getContextPath();
            model.addAttribute("contextPath", contextPath);

            String registerReturnPath = "/guest/payment/session/" + sessionId;
            String membershipRegisterUrl = buildMembershipRegisterUrl(contextPath, sessionId, registerReturnPath);
            model.addAttribute("membershipRegisterUrl", membershipRegisterUrl);
            
            // Flash attributes are automatically added to model by Spring
            // Only add URL params to model if flash attributes don't exist (for backward compatibility)
            if (!model.containsAttribute("errorMessage") && error != null && !error.isEmpty()) {
                model.addAttribute("errorMessage", error);
            }
            
            if (!model.containsAttribute("successMessage") && success != null && !success.isEmpty()) {
                model.addAttribute("successMessage", success);
            }
            
            // Thêm thông báo membership verified
            if (membershipVerified != null && membershipVerified.equals("true")) {
                model.addAttribute("successMessage", "Xác thực thành viên thành công! Bạn có thể tích điểm cho đơn hàng này.");
            }
            
            // Thêm thông báo membership registered
            if (membershipRegistered != null && membershipRegistered.equals("true")) {
                model.addAttribute("successMessage", "Đăng ký thành viên thành công! Bạn có thể tích điểm cho đơn hàng này.");
            }
            
            return "guest-page/payment";
        } catch (Exception e) {
            model.addAttribute("error", "Không thể tải thông tin thanh toán: " + e.getMessage());
            return "error-page";
        }
    }

    /**
     * Xử lý POST request cho payment page (apply/remove voucher, verify membership)
     */
    @PostMapping("/session/{sessionId}")
    public String handlePaymentAction(@PathVariable Long sessionId, 
                                    @RequestParam String action,
                                    @RequestParam(required = false) Long voucherId,
                                    @RequestParam(required = false) String phoneNumber,
                                    @RequestParam(required = false) Integer points,
                                    RedirectAttributes redirectAttributes) {
        try {
            if ("apply-voucher".equals(action) && voucherId != null) {
                paymentService.applyVoucherBySessionId(sessionId, voucherId);
                redirectAttributes.addFlashAttribute("successMessage", "Voucher đã được áp dụng thành công!");
            } else if ("remove-voucher".equals(action)) {
                paymentService.removeVoucherBySessionId(sessionId);
                redirectAttributes.addFlashAttribute("successMessage", "Voucher đã được hủy áp dụng!");
            } else if ("verify-membership".equals(action) && phoneNumber != null && !phoneNumber.trim().isEmpty()) {
                // Verify membership và gán vào session
                Membership membership = paymentService.findMembershipByPhoneNumber(phoneNumber.trim());
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
            return "redirect:/guest/payment/session/" + sessionId;
        } catch (Exception e) {
            e.printStackTrace(); // Log error for debugging
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
            return "redirect:/guest/payment/session/" + sessionId;
        }
    }


    @PostMapping("/session/{sessionId}/confirm")
    public String waitingForCashierConfirmPayment(@PathVariable Long sessionId,
                                                  @RequestParam String paymentMethod,
                                                  Model model,
                                                  HttpServletRequest request){

        try {
            if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
                return "redirect:/guest/payment/session/" + sessionId + "?error=" +
                        URLEncoder.encode("Vui lòng chọn phương thức thanh toán", StandardCharsets.UTF_8);
            }

            Order.PaymentMethod method;
            try {
                method = Order.PaymentMethod.valueOf(paymentMethod);
            } catch (IllegalArgumentException e) {
                return "redirect:/guest/payment/session/" + sessionId + "?error=" +
                        URLEncoder.encode("Phương thức thanh toán không hợp lệ: " + paymentMethod, StandardCharsets.UTF_8);
            }

            paymentService.waitingConfirmPayment(sessionId, method);

            // redirect sang trang waiting
            return "redirect:/guest/payment/session/" + sessionId +"/waiting-confirm";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/guest/payment/session/" + sessionId + "?error=" +
                    URLEncoder.encode("Thanh toán thất bại: " + e.getMessage(), StandardCharsets.UTF_8);
        }
    }

    @GetMapping("/session/{sessionId}/waiting-confirm")
    public String waitingPage(@PathVariable Long sessionId,
                              Model model,
                              HttpSession session) {
        model.addAttribute("sessionId", sessionId);
        try {
            // Lấy thông tin thanh toán hiện tại để hiển thị QR nếu cần
            PaymentDTO payment = paymentService.getPaymentBySessionId(sessionId);
            // Lấy chi tiết các món đã order
            List<OrderItem> orderItems = paymentService.getOrderItemsBySessionId(sessionId);
            // Lấy thông tin khách hàng
            Membership membership = paymentService.getMembershipBySessionId(sessionId);
            // Tính các giá trị để hiển thị
            double originalTotal = paymentService.calculateOriginalOrderTotal(sessionId);
            double discountAmount = paymentService.calculateDiscountAmount(sessionId);
            double finalTotal = originalTotal - discountAmount;
            
            // Thêm thông tin vào model
            model.addAttribute("payment", payment);
            model.addAttribute("orderItems", orderItems);
            model.addAttribute("membership", membership);
            model.addAttribute("paymentMethod", payment.getPaymentMethod());
            model.addAttribute("orderTotal", payment.getOrderTotal());
            model.addAttribute("orderId", payment.getOrderId());
            model.addAttribute("tableNumber", payment.getTableNumber());
            model.addAttribute("originalTotal", originalTotal);
            model.addAttribute("discountAmount", discountAmount);
            model.addAttribute("finalTotal", finalTotal);
        } catch (Exception ignored) {}
        return "guest-page/waiting_confirm";
    }

    @GetMapping("/session/{sessionId}/complete")
    public String completePaymentAndClearSession(@PathVariable Long sessionId,
                                                 HttpSession session) {
        session.invalidate();
        return "redirect:/guest";
    }




    /**
     * Trang xác nhận thanh toán theo session với đầy đủ thông tin
     */
    @GetMapping("/session/{sessionId}/confirmation")
    public String showPaymentConfirmationBySession(@PathVariable Long sessionId, 
                                                   Model model, HttpServletRequest request) {
        System.out.println("=== Payment Confirmation Page Debug ===");
        System.out.println("Received sessionId: " + sessionId);
        System.out.println("Request URL: " + request.getRequestURL());
        System.out.println("Request URI: " + request.getRequestURI());
        HttpSession session = request.getSession();
        try {
            // Lấy thông tin payment từ order đã thanh toán (không cần kiểm tra session mở)
            PaymentDTO payment = paymentService.getPaymentConfirmationBySessionId(sessionId);
            System.out.println("Payment DTO: " + payment);
            
            // Lấy chi tiết các món đã order
            List<OrderItem> orderItems = paymentService.getOrderItemsBySessionId(sessionId);
            System.out.println("Order items count: " + (orderItems != null ? orderItems.size() : "null"));
            
            // Lấy thông tin khách hàng
            Membership membership = paymentService.getMembershipBySessionId(sessionId);
            System.out.println("Membership: " + membership);
            
            // Tính các giá trị để hiển thị
            double originalTotal = paymentService.calculateOriginalOrderTotal(sessionId);
            double discountAmount = paymentService.calculateDiscountAmount(sessionId);
            double finalTotal = originalTotal - discountAmount;
            System.out.println("Totals - Original: " + originalTotal + ", Discount: " + discountAmount + ", Final: " + finalTotal);
            
            // Thêm thông tin vào model
            model.addAttribute("payment", payment);
            model.addAttribute("orderItems", orderItems);
            model.addAttribute("membership", membership);
            model.addAttribute("sessionId", sessionId);
            model.addAttribute("tableId", payment.getTableNumber()); // Add tableId for table release
            model.addAttribute("orderId", payment.getOrderId());
            model.addAttribute("originalTotal", originalTotal);
            model.addAttribute("discountAmount", discountAmount);
            model.addAttribute("finalTotal", finalTotal);
            model.addAttribute("redirectTarget", "guest"); // Rõ ràng: redirect về guest cho dine-in

            // Sau khi hiển thị xác nhận, dọn dẹp trạng thái điểm đã dùng cho session
            try {
                paymentService.removePoints(sessionId);
            } catch (Exception ignored) {}

            session.removeAttribute("sessionId");
            session.removeAttribute("tableId");
            return "guest-page/payment-confirmation";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Không thể tải thông tin xác nhận: " + e.getMessage());
            return "error-page";
        }
    }

    private String buildMembershipRegisterUrl(String contextPath, Long sessionId, String returnPath) {
        String encodedReturnPath = URLEncoder.encode(returnPath, StandardCharsets.UTF_8);
        String basePath = (contextPath != null ? contextPath : "") + "/guest/membership/register";
        return basePath + "?sessionId=" + sessionId + "&returnUrl=" + encodedReturnPath;
    }
}