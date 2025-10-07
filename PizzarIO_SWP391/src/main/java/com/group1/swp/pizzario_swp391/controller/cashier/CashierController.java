package com.group1.swp.pizzario_swp391.controller.cashier;

import com.group1.swp.pizzario_swp391.service.TableService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("cashier")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class CashierController {

    TableService tableService;

    @GetMapping
    public String cashierHome(Model model) {
        model.addAttribute("tables", tableService.getTablesForCashier());
        return "cashier-page/cashier";
    }
}
