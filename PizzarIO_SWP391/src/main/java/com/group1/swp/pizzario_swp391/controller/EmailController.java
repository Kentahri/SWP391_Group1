package com.group1.swp.pizzario_swp391.controller;

import com.group1.swp.pizzario_swp391.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class EmailController {

    @Autowired
    private EmailService emailService;

    @GetMapping("/sendMail")
    public String sendMail(@RequestParam String email, @RequestParam String subject){
        String context = "Hello World";
        emailService.sendEmail(email, subject, context);
        return "send_mail";
    }


}
