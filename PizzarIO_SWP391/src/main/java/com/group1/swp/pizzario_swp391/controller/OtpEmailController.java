package com.group1.swp.pizzario_swp391.controller;

import com.group1.swp.pizzario_swp391.entity.OtpMail;
import com.group1.swp.pizzario_swp391.entity.Staff;
import com.group1.swp.pizzario_swp391.service.LoginService;
import com.group1.swp.pizzario_swp391.service.OtpMailService;
import com.group1.swp.pizzario_swp391.service.StaffService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class OtpEmailController {

    @Autowired
    private OtpMailService emailService;

    @Autowired
    private StaffService staffService;

    @Autowired
    private OtpMailService otpEmailService;

    @Autowired
    private LoginService loginService;

    @PostMapping("/missing_pass/send-code")
    public String sendMail(@Valid @ModelAttribute("account") Staff staffs,
                           BindingResult br,
                            @RequestParam("email") String email,
                           Model model) {
        String subject = "Otp Code Mail";

        if (br.hasErrors()) {
            return "authenticate/missing_pass";
        }

        Staff staff = staffService.findByEmailValid(email);

        if(staff == null){
            model.addAttribute("errorMail", "Mail chưa được đăng kí hoặc chưa tồn tại");
            return "authenticate/missing_pass";
        }

        OtpMail otp = otpEmailService.createNewOtp(staff.getId());

        emailService.sendEmail(staff.getEmail(), subject,  "Mã OTP của bạn: " + otp.getOtpCode() + "\n"
                + "Vui lòng không chia sẻ mã này cho bất kỳ ai.\n"
                + "Mã OTP chỉ có hiệu lực trong 3 phút kể từ khi nhận.\n"
                + "Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.\n");
        ;

        model.addAttribute("accountEmail", staff.getEmail());
        model.addAttribute("demoOtp", otp);

        return "authenticate/send_mail";
    }

    @GetMapping("/missing_pass/verify")
    public String showVerify(@ModelAttribute("accountEmail") String email, Model model) {
        model.addAttribute("accountEmail", email);
        return "authenticate/send_mail";
    }


    @PostMapping("/missing_pass/verify")
    public String verify(@RequestParam("code") String code,
                         @RequestParam("newPassword") String newPassword,
                         @RequestParam("confirmPassword") String confirmPassword,
                         @RequestParam("email") String email,
                         RedirectAttributes ra) {


        Staff findStaffByMail = loginService.findByEmail(email);

        OtpMail lastestOtp = otpEmailService.getLatestOtpByStaffId(findStaffByMail.getId());

        if (!lastestOtp.getOtpCode().equals(code)) {
            ra.addFlashAttribute("error", "Mã OTP không đúng.");
            ra.addFlashAttribute("accountEmail", email);
            return "redirect:/missing_pass/verify";
        }
        else{
            if(otpEmailService.checkExpireOtp(lastestOtp)){
                ra.addFlashAttribute("accountEmail", email);
                ra.addFlashAttribute("error", "Otp đã hết hạn. Hãy làm lại bước quên mật khẩu");
                return "redirect:/missing_pass/verify";
            }

            if (otpEmailService.checkUsedOtp(lastestOtp)){
                ra.addFlashAttribute("accountEmail", email);
                ra.addFlashAttribute("error", "Otp đã được dùng");
                return "redirect:/missing_pass/verify";
            }

            if (newPassword == null || newPassword.isBlank() || !newPassword.equals(confirmPassword)) {
                ra.addFlashAttribute("accountEmail", email);
                ra.addFlashAttribute("error", "Mật khẩu xác nhận không khớp.");
                return "redirect:/missing_pass/verify";
            }

            if(otpEmailService.checkValid(newPassword) != null){
                ra.addFlashAttribute("accountEmail", email);
                ra.addFlashAttribute("errorPass", otpEmailService.checkValid(newPassword));
                return "redirect:/missing_pass/verify";
            }

            lastestOtp.setUsed(true);
            otpEmailService.updateOtp(lastestOtp);
            staffService.updatePasswordByEmail(email, newPassword);

            ra.addFlashAttribute("successMessage", "Mật khẩu đã được đổi thành công, vui lòng đăng nhập lại");
            return "redirect:/login";
        }
    }
}
