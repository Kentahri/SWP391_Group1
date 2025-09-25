package com.group1.swp.pizzario_swp391.controller;

import com.group1.swp.pizzario_swp391.entity.Staff;
import com.group1.swp.pizzario_swp391.service.LoginService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import jakarta.validation.Valid;

import java.util.Optional;

@Controller
public class LoginController {

    private final LoginService loginService;


    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    // localhost808/login
    @GetMapping("/login")
    public String showLogin(Model model){
        model.addAttribute("account", new Staff());
        return "login";
    }


    @GetMapping("/missing_pass")
    public String missingPassPage(Model model) {
        model.addAttribute("account", new Staff());
        return "missing_pass";
    }

    @PostMapping("/signIn")
    public String signIn(@Valid @ModelAttribute("account") Staff account,BindingResult result ,Model model) { //StaffDTO

        if(result.hasErrors()){
            return "login";
        }

        Optional<Staff> authenticated = loginService.authenticate(account.getEmail(), account.getPassword());

        if (authenticated.isEmpty()) {
            model.addAttribute("loginError", "Email hoặc mật khẩu không đúng");
            return "login";
        }

        Staff authenticate = loginService.authenticate(account.getEmail(), account.getPassword()).orElse(null);

        if (authenticate == null) {

            return "redirect:/login";
        }

        return switch (authenticate.getRole()) {
            case MANAGER -> "dashboard";
            case KITCHEN -> "kitchen";
            case CASHIER -> "cashier";
            default -> "redirect:/login"; // fallback nếu role null hoặc không khớp
        };
    }

}
