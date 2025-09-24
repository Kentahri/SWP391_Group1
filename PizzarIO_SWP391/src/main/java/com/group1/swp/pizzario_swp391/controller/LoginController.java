package com.group1.swp.pizzario_swp391.controller;


import com.group1.swp.pizzario_swp391.entity.Account;
import com.group1.swp.pizzario_swp391.service.EmailService;
import com.group1.swp.pizzario_swp391.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private EmailService emailService;

    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    // localhost808/login
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

    @PostMapping("/missing_pass/send-code")
    public String sendCodePage(@ModelAttribute Account account, Model model){
        System.out.println("Find account " + account);
        model.addAttribute("account", account);
        return"send_mail";
    }

    @PostMapping("/signIn")
    public String signIn(@Valid @ModelAttribute("account") Account account,BindingResult result ,Model model) {

        if(result.hasErrors()){
            return "login";
        }

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
