package com.group1.swp.pizzario_swp391.controller.guest;

import jakarta.servlet.http.HttpServletRequest;
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

import com.group1.swp.pizzario_swp391.dto.membership.MembershipRegistrationDTO;
import com.group1.swp.pizzario_swp391.service.MembershipService;

@Controller
@RequestMapping("/guest/membership")
public class MembershipController {
    private final MembershipService membershipService;

    public MembershipController(MembershipService membershipService) {
        this.membershipService = membershipService;
    }

    // Registration endpoints
    @GetMapping("/register")
    public String showRegistrationForm(@RequestParam Long sessionId,
                                       @RequestParam(required = false) String returnUrl,
                                       Model model,
                                       HttpServletRequest request) {
        try {
            // Validation được thực hiện trong service
            membershipService.validateSessionId(sessionId);
        } catch (IllegalArgumentException e) {
            return "redirect:/guest?error=" + 
                   URLEncoder.encode("Vui lòng chọn bàn và đặt món trước khi đăng ký thành viên", StandardCharsets.UTF_8);
        }
        
        String sanitizedReturnUrl = sanitizeReturnUrl(sessionId, returnUrl);

        model.addAttribute("registrationDTO", new MembershipRegistrationDTO());
        model.addAttribute("sessionId", sessionId);
        model.addAttribute("returnUrl", sanitizedReturnUrl);
        model.addAttribute("returnUrlDisplay", buildDisplayUrl(request, sanitizedReturnUrl));
        return "guest-page/membership_register";
    }

    @PostMapping("/register")
    public String register(@Valid MembershipRegistrationDTO registrationDTO,
                           BindingResult bindingResult,
                           @RequestParam Long sessionId,
                           @RequestParam(required = false) String returnUrl,
                           Model model,
                           HttpServletRequest request) {
        try {
            // Validation được thực hiện trong service
            membershipService.validateSessionId(sessionId);
        } catch (IllegalArgumentException e) {
            return "redirect:/guest?error=" + 
                   URLEncoder.encode("Vui lòng chọn bàn và đặt món trước khi đăng ký thành viên", StandardCharsets.UTF_8);
        }
        
        String sanitizedReturnUrl = sanitizeReturnUrl(sessionId, returnUrl);

        // đảm bảo form object luôn trong model để Thymeleaf hiển thị lỗi trường
        model.addAttribute("registrationDTO", registrationDTO);
        model.addAttribute("sessionId", sessionId);
        model.addAttribute("returnUrl", sanitizedReturnUrl);
        model.addAttribute("returnUrlDisplay", buildDisplayUrl(request, sanitizedReturnUrl));

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

        // Phone number trimming được thực hiện trong service
        var result = membershipService.register(registrationDTO);
        if (result.isPresent()) {
            // Luôn redirect về trang payment sau khi đăng ký thành công
            String redirectTarget = appendQueryParam(sanitizedReturnUrl, "membershipRegistered", "true");
            return "redirect:" + redirectTarget;
        } else {
            model.addAttribute("error", "Số điện thoại đã tồn tại: " + registrationDTO.getPhoneNumber());
            model.addAttribute("registrationDTO", registrationDTO);
        }
        return "guest-page/membership_register";
    }

    private String sanitizeReturnUrl(Long sessionId, String returnUrl) {
        String defaultUrl = "/guest/payment/session/" + sessionId;
        if (returnUrl == null) {
            return defaultUrl;
        }

        String trimmed = returnUrl.trim();
        if (trimmed.isEmpty()) {
            return defaultUrl;
        }

        if (!trimmed.startsWith("/") || trimmed.startsWith("//") || trimmed.contains("://")) {
            return defaultUrl;
        }

        return trimmed;
    }

    private String buildDisplayUrl(HttpServletRequest request, String sanitizedReturnUrl) {
        String contextPath = request.getContextPath();
        if (contextPath == null || contextPath.isBlank() || "/".equals(contextPath)) {
            return sanitizedReturnUrl;
        }
        if (sanitizedReturnUrl.startsWith(contextPath)) {
            return sanitizedReturnUrl;
        }
        return contextPath + sanitizedReturnUrl;
    }

    private String appendQueryParam(String url, String paramName, String paramValue) {
        String encodedValue = URLEncoder.encode(paramValue, StandardCharsets.UTF_8);
        if (url.contains("?")) {
            return url + "&" + paramName + "=" + encodedValue;
        }
        return url + "?" + paramName + "=" + encodedValue;
    }
}