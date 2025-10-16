package com.group1.swp.pizzario_swp391.controller.manager;

import com.group1.swp.pizzario_swp391.annotation.ManagerUrl;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.group1.swp.pizzario_swp391.dto.table.TableCreateDTO;
import com.group1.swp.pizzario_swp391.dto.table.TableManagementDTO;
import com.group1.swp.pizzario_swp391.entity.DiningTable;
import com.group1.swp.pizzario_swp391.service.TableService;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Controller
@ManagerUrl
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class TableController {

    TableService tableService;

    /**
     * Hiển thị trang quản lý bàn
     */
    @GetMapping("/tables")
    public String table(Model model) {
        model.addAttribute("tableCreateDTO", new TableCreateDTO());
        model.addAttribute("tableManagementDTO", new TableManagementDTO());
        model.addAttribute("tableConditions", DiningTable.TableCondition.values());
        model.addAttribute("tables", tableService.getAllTables());
        return "admin_page/table_management";
    }

    /**
     * Tạo bàn mới - chỉ nhập capacity
     * Hệ thống tự set status=AVAILABLE, condition=NEW
     */
    @PostMapping("/tables/add")
    public String createNewTable(@Valid @ModelAttribute TableCreateDTO tableCreateDTO,
            BindingResult result,
            Model model) {
        if (result.hasErrors()) {
            model.addAttribute("tableManagementDTO", new TableManagementDTO());
            model.addAttribute("tableConditions", DiningTable.TableCondition.values());
            model.addAttribute("tables", tableService.getAllTables());
            return "admin_page/table_management";
        }
        tableService.createNewTable(tableCreateDTO);
        return "redirect:/manager/tables";
    }

    /**
     * Cập nhật bàn - chỉ cập nhật capacity và tableCondition
     * TableStatus do Cashier quản lý
     */
    @PostMapping("/tables/update/{id}")
    public String updateTable(@PathVariable int id,
            @Valid @ModelAttribute TableManagementDTO tableManagementDTO,
            BindingResult result,
            Model model) {
        if (result.hasErrors()) {
            model.addAttribute("tableCreateDTO", new TableCreateDTO());
            model.addAttribute("tableConditions", DiningTable.TableCondition.values());
            model.addAttribute("tables", tableService.getAllTables());
            return "admin_page/table_management";
        }
        tableService.updateTable(id, tableManagementDTO);
        return "redirect:/manager/tables";
    }
}
