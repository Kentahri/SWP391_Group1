package com.group1.swp.pizzario_swp391.controller.cashier;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("cashier")
public class CashierController {

    @GetMapping
    public String cashierHome() {
        return "cashier-page/cashier";
    }



}
