package com.group1.swp.pizzario_swp391.controller.cashier;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.group1.swp.pizzario_swp391.entity.Order;
import com.group1.swp.pizzario_swp391.entity.Staff;
import com.group1.swp.pizzario_swp391.service.PaymentService;
import com.group1.swp.pizzario_swp391.service.OrderService;
import com.group1.swp.pizzario_swp391.service.StaffService;
import java.security.Principal;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Controller
@RequestMapping("/cashier/payment")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CashierPaymentController {

    PaymentService paymentService;
    OrderService orderService;
    StaffService staffService;

    @PostMapping("/confirm")
    public String confirmDineInPayment(@RequestParam("sessionId") Long sessionId,
                                       @RequestParam(value = "paymentMethod", required = true) String paymentMethod,
                                       Principal principal,
                                       RedirectAttributes redirectAttributes) {
        try {
            // Validation được thực hiện trong service
            Order.PaymentMethod method = paymentService.validatePaymentMethod(paymentMethod);
            // Attach current cashier (staff) to dine-in order before confirming, so staff_id is not null
            try {
                if (principal != null) {
                    Staff staff = staffService.findByEmail(principal.getName());
                    if (staff != null) {
                        Order order = orderService.getOrderForSession(sessionId);
                        if (order != null && order.getStaff() == null) {
                            order.setStaff(staff);
                            orderService.saveOrder(order);
                        }
                    }
                }
            } catch (Exception ignored) {}
            paymentService.confirmPaymentBySessionId(sessionId, method);

            redirectAttributes.addFlashAttribute("successMessage", "Xác nhận thanh toán thành công cho phiên " + sessionId + ".");
            return "redirect:/cashier";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/cashier";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi xác nhận: " + e.getMessage());
            return "redirect:/cashier";
        }
    }
}
