package com.group1.swp.pizzario_swp391.controller;

import com.group1.swp.pizzario_swp391.entity.Staff;
import com.group1.swp.pizzario_swp391.service.EmailService;
import com.group1.swp.pizzario_swp391.service.StaffService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class EmailController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private StaffService staffService;

    @GetMapping("/sendMail")
    public String sendMail(@RequestParam String email, @RequestParam String subject){
        String context = "Hello World";
        emailService.sendEmail(email, subject, context);
        return "send_mail";
    }

//    @PostMapping("/missing_pass/send-code")
//    public String sendCodePage(@RequestParam Staff staff, HttpSession session, RedirectAttributes ra){
//        System.out.println("Find account " + account);
//        model.addAttribute("account", account);
//        return"send_mail";
//    }

    @PostMapping("/missing_pass/send-code")
    public String sendMail(@RequestParam("email") String email,
                           HttpSession session,
                           Model model) {
        String subject = "Hello World";

        // 1. Tìm staff theo email
        Staff staff = staffService.findByEmail(email);

        // 2. Sinh OTP và gửi mail
        String otp = "123456"; // TODO: generate random
        emailService.sendEmail(staff.getEmail(), subject, "Mã OTP của bạn: " + otp);

        // 3. Lưu OTP + email vào session để verify sau
        session.setAttribute("otp_expected", otp);
        session.setAttribute("email_for_reset", staff.getEmail());

        // 4. Gửi sang view để hiển thị
        model.addAttribute("accountEmail", staff.getEmail());
        model.addAttribute("demoOtp", otp);

        // 5. Render thẳng trang nhập OTP (không cần redirect)
        return "send_mail";  // send_mail.html
    }


    @PostMapping("/missing_pass/verify")
    public String verify(@RequestParam("code") String code,
                         @RequestParam("newPassword") String newPassword,
                         @RequestParam("confirmPassword") String confirmPassword,
                         HttpSession session,
                         RedirectAttributes ra) {

        String expected = (String) session.getAttribute("otp_expected");
        String email    = (String) session.getAttribute("email_for_reset");

        if (expected == null || email == null) {
            ra.addFlashAttribute("error", "Phiên đã hết hạn. Vui lòng gửi lại mã.");
            return "redirect:/missing_pass/start"; // trang nhập email ban đầu
        }
        if (!expected.equals(code)) {
            ra.addFlashAttribute("error", "Mã OTP không đúng.");
            return "redirect:/missing_pass/verify";
        }
        if (newPassword == null || newPassword.isBlank() || !newPassword.equals(confirmPassword)) {
            ra.addFlashAttribute("error", "Mật khẩu xác nhận không khớp.");
            return "redirect:/missing_pass/verify";
        }

        // cập nhật mật khẩu (nên BCrypt)
        staffService.updatePasswordByEmail(email, newPassword);

        // dọn session
        session.removeAttribute("otp_expected");
        session.removeAttribute("email_for_reset");

        ra.addFlashAttribute("message", "Đặt lại mật khẩu thành công. Hãy đăng nhập.");
        return "redirect:/login";
    }

}
