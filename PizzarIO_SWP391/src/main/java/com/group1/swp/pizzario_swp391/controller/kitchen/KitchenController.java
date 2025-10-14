package com.group1.swp.pizzario_swp391.controller.kitchen;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("kitchen")
public class KitchenController {

    @GetMapping
    public String cashierHome() {
        return "kitchen-page/kitchen";
    }

}
