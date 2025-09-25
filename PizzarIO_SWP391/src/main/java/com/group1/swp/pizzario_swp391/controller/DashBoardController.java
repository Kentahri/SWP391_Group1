package com.group1.swp.pizzario_swp391.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class DashBoardController {

    @GetMapping("/analytics")
    public String getFormAnalytics(){
        return "analytics";
    }

    @GetMapping("/shifts")
    public String getFormShift(){
        return "shift_management";
    }

}
