package com.group1.swp.pizzario_swp391.controller;

import com.group1.swp.pizzario_swp391.dto.staff.StaffLoginDTO;
import com.group1.swp.pizzario_swp391.entity.Staff;
import com.group1.swp.pizzario_swp391.service.LoginService;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import jakarta.validation.Valid;


@Controller
public class LoginController {

    private final LoginService loginService;


    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    // localhost808/login
    @GetMapping("/login")
    public String form(Model model, CsrfToken token) {
        model.addAttribute("_csrf", token);
        model.addAttribute("authen", new StaffLoginDTO());
        return "authenticate/login";
    }


    @GetMapping("/missing_pass")
    public String missingPassPage(Model model) {
        model.addAttribute("account", new Staff());
        return "authenticate/missing_pass";
    }

    @PostMapping("/signIn")
    public String signIn(@Valid @ModelAttribute("authen") StaffLoginDTO authenLogin,BindingResult result ,Model model) { //StaffDTO

        if(result.hasErrors()){
            return "authenticate/login";
        }
        return "redirect:manager";
    }

}
