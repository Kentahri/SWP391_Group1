package com.group1.swp.pizzario_swp391.controller.cashier;

import com.group1.swp.pizzario_swp391.dto.order.OrderDetailDTO;
import com.group1.swp.pizzario_swp391.service.StaffService;
import com.group1.swp.pizzario_swp391.service.TableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cashier")
public class CashierRestController {

    private final TableService tableService;

    @Autowired
    public CashierRestController(TableService tableService, StaffService staffService) {
        this.tableService = tableService;
    }

    @GetMapping("/tables/{id}/order")
    public OrderDetailDTO getTableOrderDetail(@PathVariable("id") Integer tableId) {
        return tableService.getOrderDetailByTableId(tableId);
    }
}
