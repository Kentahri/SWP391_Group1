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



//    @PostMapping("/signIn")
//    public String signIn(@ModelAttribute("account") @Valid Account account){
//
//        boolean ok = loginService.authenticate(account);
//        if(ok){
//            return "dashboard";
//        }
//        return "redirect:/login";
//    }

    @GetMapping("/missing_pass")
    public String missingPassPage(Model model) {
        model.addAttribute("account", new Account());
        return "missing_pass";
    }

    @PostMapping("/signIn")
    public String signIn(@ModelAttribute("account") @Valid Account account) {
        System.out.println(account);



        Account authenticate = loginService.authenticate(account);
        System.out.println(authenticate);
        System.out.println(authenticate.getRole());

            if(authenticate.getRole() != null && authenticate.getRole().equals("MANAGER")) {
                return "dashboard";
            }
            else if(authenticate.getRole() != null && authenticate.getRole().equals("KITCHEN")) {
                return "kitchen";
            }
            else if(authenticate.getRole() != null && authenticate.getRole().equals("CASHIER")) {
                return "cashier";
            }
            else{

                return "redirect:/login";
            }
        }



//    @PostMapping("/missing_pass/send-code")
//    public String sendCode(@Valid @ModelAttribute("forgotForm") Account account,
//                           BindingResult br, RedirectAttributes ra) {
//        if (br.hasErrors()) return "missing_pass";
//
//        ra.addFlashAttribute("successMsg", "Đã gửi mã xác thực tới email của bạn.");
//        return "redirect:/missing_pass";
//    }
}
