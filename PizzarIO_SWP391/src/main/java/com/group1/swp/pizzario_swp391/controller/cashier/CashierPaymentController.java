package com.group1.swp.pizzario_swp391.controller.cashier;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.group1.swp.pizzario_swp391.entity.Order;
import com.group1.swp.pizzario_swp391.service.PaymentService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Controller
@RequestMapping("/cashier/payment")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CashierPaymentController {

    PaymentService paymentService;

    @PostMapping("/confirm")
    public String confirmDineInPayment(@RequestParam("sessionId") Long sessionId,
                                       @RequestParam(value = "paymentMethod", required = true) String paymentMethod,
                                       RedirectAttributes redirectAttributes) {
        try {
            Order.PaymentMethod method = Order.PaymentMethod.valueOf(paymentMethod);
            paymentService.confirmPaymentBySessionId(sessionId, method);

            redirectAttributes.addFlashAttribute("successMessage", "Xác nhận thanh toán thành công cho phiên " + sessionId + ".");
            return "redirect:/cashier";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi xác nhận: " + e.getMessage());
            return "redirect:/cashier";
        }
    }
}
