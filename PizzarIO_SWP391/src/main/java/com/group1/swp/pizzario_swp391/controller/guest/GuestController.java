package com.group1.swp.pizzario_swp391.controller.guest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/guest")
public class GuestController {
    @GetMapping
    public String guestPage() {
        return "guest-page/guest";
    }
}
