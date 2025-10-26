package com.group1.swp.pizzario_swp391.controller.guest;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import com.group1.swp.pizzario_swp391.dto.membership.MembershipDTO;
import com.group1.swp.pizzario_swp391.dto.membership.VerifyMembershipDTO;
import com.group1.swp.pizzario_swp391.dto.membership.MembershipRegistrationDTO;
import com.group1.swp.pizzario_swp391.service.MembershipService;

@Controller
@RequestMapping("/guest/membership")
public class MembershipController {
    private final MembershipService membershipService;

    public MembershipController(MembershipService membershipService) {
        this.membershipService = membershipService;
    }

    @GetMapping
    public String showVerifyForm(@RequestParam Long sessionId, Model model) {
        // Bắt buộc phải có sessionId
        if (sessionId == null) {
            return "redirect:/guest?error=" + 
                   URLEncoder.encode("Vui lòng chọn bàn và đặt món trước khi xác thực thành viên", StandardCharsets.UTF_8);
        }
        
        model.addAttribute("verifyMembershipDTO", new VerifyMembershipDTO());
        model.addAttribute("sessionId", sessionId);
        return "guest-page/membership_verify";
    }

    @PostMapping
    public String verify(@Valid VerifyMembershipDTO verifyMembershipDTO, 
                        BindingResult bindingResult, 
                        @RequestParam Long sessionId,
                        Model model) {
        // Bắt buộc phải có sessionId
        if (sessionId == null) {
            return "redirect:/guest?error=" + 
                   URLEncoder.encode("Vui lòng chọn bàn và đặt món trước khi xác thực thành viên", StandardCharsets.UTF_8);
        }
        
        model.addAttribute("sessionId", sessionId);
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("verifyMembershipDTO", verifyMembershipDTO);
            model.addAttribute("error", "Vui lòng nhập số điện thoại hợp lệ.");
            return "guest-page/membership_verify";
        }

        String phone = verifyMembershipDTO.getPhoneNumber().trim();
        var opt = membershipService.verifyByPhone(phone);
        if (opt.isPresent()) {
            // Luôn redirect về trang payment sau khi xác thực thành công
            return "redirect:/guest/payment/session/" + sessionId + "?membershipVerified=true";
        } else {
            model.addAttribute("error", "Không tìm thấy thành viên với số điện thoại: " + phone);
        }
        model.addAttribute("verifyMembershipDTO", new VerifyMembershipDTO());
        return "guest-page/membership_verify";
    }

    // NEW: registration endpoints
    @GetMapping("/register")
    public String showRegistrationForm(@RequestParam Long sessionId, Model model) {
        // Bắt buộc phải có sessionId
        if (sessionId == null) {
            return "redirect:/guest?error=" + 
                   URLEncoder.encode("Vui lòng chọn bàn và đặt món trước khi đăng ký thành viên", StandardCharsets.UTF_8);
        }
        
        model.addAttribute("registrationDTO", new MembershipRegistrationDTO());
        model.addAttribute("sessionId", sessionId);
        return "guest-page/membership_register";
    }

    @PostMapping("/register")
    public String register(@Valid MembershipRegistrationDTO registrationDTO,
                           BindingResult bindingResult,
                           @RequestParam Long sessionId,
                           Model model) {
        // Bắt buộc phải có sessionId
        if (sessionId == null) {
            return "redirect:/guest?error=" + 
                   URLEncoder.encode("Vui lòng chọn bàn và đặt món trước khi đăng ký thành viên", StandardCharsets.UTF_8);
        }
        
        // đảm bảo form object luôn trong model để Thymeleaf hiển thị lỗi trường
        model.addAttribute("registrationDTO", registrationDTO);
        model.addAttribute("sessionId", sessionId);

        // nếu có lỗi validate, trả về từng lỗi cụ thể cho từng field
        if (bindingResult.hasErrors()) {
            if (bindingResult.hasFieldErrors("fullName")) {
                model.addAttribute("fullNameError", bindingResult.getFieldError("fullName").getDefaultMessage());
            }
            if (bindingResult.hasFieldErrors("phoneNumber")) {
                model.addAttribute("phoneNumberError", bindingResult.getFieldError("phoneNumber").getDefaultMessage());
            }
            return "guest-page/membership_register";
        }

        String phone = registrationDTO.getPhoneNumber().trim();
        var result = membershipService.register(registrationDTO);
        if (result.isPresent()) {
            // Luôn redirect về trang payment sau khi đăng ký thành công
            return "redirect:/guest/payment/session/" + sessionId + "?membershipRegistered=true";
        } else {
            model.addAttribute("error", "Số điện thoại đã tồn tại: " + phone);
            model.addAttribute("registrationDTO", registrationDTO);
        }
        return "guest-page/membership_register";
    }
}