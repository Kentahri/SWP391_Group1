package com.group1.swp.pizzario_swp391.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/manager")
public class DashBoardController {

    @GetMapping
    public String getFormAnalytics(){
        return "admin_page/analytics";
    }



}
