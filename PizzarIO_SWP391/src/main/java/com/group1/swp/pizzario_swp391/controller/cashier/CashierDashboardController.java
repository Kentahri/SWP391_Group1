package com.group1.swp.pizzario_swp391.controller.cashier;

import java.security.Principal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.group1.swp.pizzario_swp391.dto.order.OrderDetailDTO;
import com.group1.swp.pizzario_swp391.dto.table.TableDTO;
import com.group1.swp.pizzario_swp391.dto.table.TableForCashierDTO;
import com.group1.swp.pizzario_swp391.entity.Staff;
import com.group1.swp.pizzario_swp391.service.StaffService;
import com.group1.swp.pizzario_swp391.service.TableService;

/**
 * Cashier Dashboard Controller
 * Handle HTTP requests for cashier page and initial data loading
 */
@Controller
@RequestMapping("/cashier")
public class CashierDashboardController {

    private static final Logger log = LoggerFactory.getLogger(CashierDashboardController.class);

    private final TableService tableService;
    private final StaffService staffService;

    @Autowired
    public CashierDashboardController(TableService tableService, StaffService staffService) {
        this.tableService = tableService;
        this.staffService = staffService;
    }

    /**
     * Load cashier dashboard page
     * This is the initial HTTP request to load the page with data
     *
     * @param model     Thymeleaf model
     * @param principal Current authenticated user
     * @return Thymeleaf template name
     */
    @GetMapping
    public String cashierDashboard(Model model, Principal principal) {


        try {
            String name = principal.getName();
            Staff staff = staffService.findByEmail(name);

            List<TableForCashierDTO> tables = tableService.getTablesForCashier();
            model.addAttribute("staff", staff);
            model.addAttribute("tables", tables);

            return "cashier-page/cashier-dashboard";
        } catch (Exception _) {
            model.addAttribute("error", "Không thể tải dữ liệu. Vui lòng thử lại.");
            return "error-page";
        }
    }

}


