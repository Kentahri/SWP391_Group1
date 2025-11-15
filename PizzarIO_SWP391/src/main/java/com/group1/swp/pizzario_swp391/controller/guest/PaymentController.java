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
import com.group1.swp.pizzario_swp391.entity.Staff;
import com.group1.swp.pizzario_swp391.service.PaymentService;
import com.group1.swp.pizzario_swp391.service.OrderService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/guest/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final OrderService orderService;

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
        try {
            // Validation được thực hiện trong service
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
            // Validation được thực hiện trong service
            if ("apply-voucher".equals(action) && voucherId != null) {
                paymentService.applyVoucherBySessionId(sessionId, voucherId);
                redirectAttributes.addFlashAttribute("successMessage", "Voucher đã được áp dụng thành công!");
            } else if ("remove-voucher".equals(action)) {
                paymentService.removeVoucherBySessionId(sessionId);
                redirectAttributes.addFlashAttribute("successMessage", "Voucher đã được hủy áp dụng!");
            } else if ("verify-membership".equals(action)) {
                // Validation được thực hiện trong service
                Membership membership = paymentService.findMembershipByPhoneNumber(phoneNumber);
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
                                                  RedirectAttributes redirectAttributes){

        try {
            if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng chọn phương thức thanh toán");
                return "redirect:/guest/payment/session/" + sessionId;
            }

            Order.PaymentMethod method = paymentService.validatePaymentMethod(paymentMethod);
            paymentService.waitingConfirmPayment(sessionId, method);

            return "redirect:/guest/payment/session/" + sessionId +"/waiting-confirm";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Thanh toán thất bại: " + e.getMessage());
            return "redirect:/guest/payment/session/" + sessionId;
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
        HttpSession session = request.getSession();
        try {
            // Lấy thông tin payment từ order đã thanh toán (không cần kiểm tra session mở)
            PaymentDTO payment = paymentService.getPaymentConfirmationBySessionId(sessionId);

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
            model.addAttribute("sessionId", sessionId);
            model.addAttribute("tableId", payment.getTableNumber()); // Add tableId for table release
            model.addAttribute("orderId", payment.getOrderId());
            model.addAttribute("originalTotal", originalTotal);
            model.addAttribute("discountAmount", discountAmount);
            model.addAttribute("finalTotal", finalTotal);
            model.addAttribute("redirectTarget", "guest"); // Rõ ràng: redirect về guest cho dine-in
            model.addAttribute("isCashierFlow", false); // Đánh dấu đây là guest flow

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

    /**
     * Trang in hóa đơn - hiển thị hóa đơn theo format receipt đen trắng
     */
    @GetMapping("/session/{sessionId}/invoice")
    public String showInvoice(@PathVariable Long sessionId, 
                             Model model, HttpServletRequest request) {
        try {
            // Lấy thông tin payment từ order đã thanh toán
            PaymentDTO payment = paymentService.getPaymentConfirmationBySessionId(sessionId);
            
            // Lấy chi tiết các món đã order
            List<OrderItem> orderItems = paymentService.getOrderItemsBySessionId(sessionId);
            
            // Lấy thông tin khách hàng
            Membership membership = paymentService.getMembershipBySessionId(sessionId);
            
            // Tính các giá trị để hiển thị
            double originalTotal = paymentService.calculateOriginalOrderTotal(sessionId);
            double discountAmount = paymentService.calculateDiscountAmount(sessionId);
            double finalTotal = originalTotal - discountAmount;
            double taxAmount = finalTotal * 0.1;
            double totalWithTax = finalTotal + taxAmount;
            
            // Lấy thông tin nhân viên từ order
            Staff staff = null;
            if (payment.getOrderId() != null) {
                var order = orderService.getOrderById(payment.getOrderId());
                if (order != null && order.getStaff() != null) {
                    staff = order.getStaff();
                }
            }
            
            // Thêm thông tin vào model
            model.addAttribute("payment", payment);
            model.addAttribute("orderItems", orderItems);
            model.addAttribute("membership", membership);
            model.addAttribute("staff", staff);
            model.addAttribute("sessionId", sessionId);
            model.addAttribute("orderId", payment.getOrderId());
            model.addAttribute("originalTotal", originalTotal);
            model.addAttribute("discountAmount", discountAmount);
            model.addAttribute("finalTotal", finalTotal);
            model.addAttribute("taxAmount", taxAmount);
            model.addAttribute("totalWithTax", totalWithTax);
            
            return "guest-page/invoice";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Không thể tải thông tin hóa đơn: " + e.getMessage());
            return "error-page";
        }
    }

    private String buildMembershipRegisterUrl(String contextPath, Long sessionId, String returnPath) {
        String encodedReturnPath = URLEncoder.encode(returnPath, StandardCharsets.UTF_8);
        String basePath = (contextPath != null ? contextPath : "") + "/guest/membership/register";
        return basePath + "?sessionId=" + sessionId + "&returnUrl=" + encodedReturnPath;
    }
}