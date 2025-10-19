package com.group1.swp.pizzario_swp391.controller.guest;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
    public String showVerifyForm(Model model) {
        model.addAttribute("verifyMembershipDTO", new VerifyMembershipDTO());
        return "guest-page/membership_verify";
    }

    @PostMapping
    public String verify(@Valid VerifyMembershipDTO verifyMembershipDTO, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("verifyMembershipDTO", verifyMembershipDTO);
            model.addAttribute("error", "Vui lòng nhập số điện thoại hợp lệ.");
            return "guest-page/membership_verify";
        }

        String phone = verifyMembershipDTO.getPhoneNumber().trim();
        var opt = membershipService.verifyByPhone(phone);
        if (opt.isPresent()) {
            MembershipDTO membershipDTO = opt.get();
            model.addAttribute("membership", membershipDTO);
        } else {
            model.addAttribute("error", "Không tìm thấy thành viên với số điện thoại: " + phone);
        }
        model.addAttribute("verifyMembershipDTO", new VerifyMembershipDTO());
        return "guest-page/membership_verify";
    }

    // NEW: registration endpoints
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("registrationDTO", new MembershipRegistrationDTO());
        return "guest-page/membership_register";
    }

    @PostMapping("/register")
    public String register(@Valid MembershipRegistrationDTO registrationDTO, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("registrationDTO", registrationDTO);
            model.addAttribute("error", "Vui lòng kiểm tra lại thông tin.");
            return "guest-page/membership_register";
        }

        String phone = registrationDTO.getPhoneNumber().trim();
        var result = membershipService.register(registrationDTO);
        if (result.isPresent()) {
            model.addAttribute("success", "Đăng ký thành công.");
            model.addAttribute("membership", result.get());
            model.addAttribute("registrationDTO", new MembershipRegistrationDTO());
        } else {
            model.addAttribute("error", "Số điện thoại đã tồn tại: " + phone);
            model.addAttribute("registrationDTO", registrationDTO);
        }
        return "guest-page/membership_register";
    }
}