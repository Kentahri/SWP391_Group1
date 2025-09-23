package com.group1.swp.pizzario_swp391.controller;


import com.group1.swp.pizzario_swp391.entity.Account;
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

    @GetMapping("/login")
    public String showLogin(Model model){
        model.addAttribute("account", new Account());
        return "login";
    }


    @GetMapping("/missing_pass")
    public String missingPassPage(Model model) {
        model.addAttribute("account", new Account());
        return "missing_pass";
    }

    @PostMapping("/signIn")
    public String signIn(@ModelAttribute("account") @Valid Account account, Model model) {
        System.out.println(account);

        Optional<Account> authenticated = loginService.authenticate(account.getEmail(), account.getPassword());

        if (authenticated.isEmpty()) {
            model.addAttribute("loginError", "Email hoặc mật khẩu không đúng");
            return "login";
        }

        Account authenticate = loginService.authenticate(account.getEmail(), account.getPassword()).orElse(null);

        if(authenticate.getRole().equals("MANAGER")) {
            return "dashboard";
        }
        else if(authenticate.getRole().equals("KITCHEN")) {
            return "kitchen";
        }
        else {
            return "cashier";
        }

    }

}
