package com.group1.swp.pizzario_swp391.controller;

import com.group1.swp.pizzario_swp391.entity.OtpMail;
import com.group1.swp.pizzario_swp391.entity.Staff;
import com.group1.swp.pizzario_swp391.service.OtpMailService;
import com.group1.swp.pizzario_swp391.service.StaffService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Controller
public class OtpEmailController {

    @Autowired
    private OtpMailService emailService;

    @Autowired
    private StaffService staffService;

    @Autowired
    private OtpMailService otpEmailService;

    @GetMapping("/sendMail")
    public String sendMail(@RequestParam String email, @RequestParam String subject){
        String context = "Hello World";
        emailService.sendEmail(email, subject, context);
        return "send_mail";
    }

    @PostMapping("/missing_pass/send-code")
    public String sendMail(@Valid @ModelAttribute("account") Staff staffs,
                           BindingResult br,
                            @RequestParam("email") String email,
                           Model model) {
        String subject = "Otp Code Mail";

        if (br.hasErrors()) {
            return "missing_pass"; // có account + BindingResult cho Thymeleaf
        }

        Staff staff = staffService.findByEmail(email);

        if(staff == null){
            System.out.println("Toi can in staff co null hay không " + staff);
            model.addAttribute("errorMail", "Mail chưa được đăng kí hoặc chưa tồn tại");
            return "missing_pass";
        }

        // 2. Sinh OTP và gửi mail
        SecureRandom random = new SecureRandom();
        String otp = String.format("%06d", random.nextInt(1_000_000));

        staff.addOtp(new OtpMail(otp, LocalDateTime.now(), LocalDateTime.now().plusMinutes(3)));

        staffService.updateStaff(staff);

        emailService.sendEmail(staff.getEmail(), subject, "Mã OTP của bạn: " + otp);

        model.addAttribute("accountEmail", staff.getEmail());
        model.addAttribute("demoOtp", otp);

        return "send_mail";
    }

    @GetMapping("/missing_pass/verify")
    public String showVerify(@ModelAttribute("accountEmail") String email, Model model) {
        model.addAttribute("accountEmail", email);
        return "send_mail";
    }


    @PostMapping("/missing_pass/verify")
    public String verify(@RequestParam("code") String code,
                         @RequestParam("newPassword") String newPassword,
                         @RequestParam("confirmPassword") String confirmPassword,
                         @RequestParam("email") String email,
                         RedirectAttributes ra) {

        System.out.println("email tôi cần đâu" + email);
//        Staff staff = staffService.findByEmail(email);
//
//        if(br.hasErrors()){
//            return "send_mail";
//        }

//        if (expected == null || email == null) {
//            ra.addFlashAttribute("error", "Phiên đã hết hạn. Vui lòng gửi lại mã.");
//            return "redirect:/missing_pass/start"; // trang nhập email ban đầu
//        }
//        if (!expected.equals(code)) {
//            ra.addFlashAttribute("error", "Mã OTP không đúng.");
//            return "redirect:/missing_pass/verify";
//        }
        if (newPassword == null || newPassword.isBlank() || !newPassword.equals(confirmPassword)) {
            ra.addFlashAttribute("accountEmail", email);
            ra.addFlashAttribute("error", "Mật khẩu xác nhận không khớp.");
            return "redirect:/missing_pass/verify";
        }


        staffService.updatePasswordByEmail(email, newPassword);

        return "redirect:/login";
    }

}
