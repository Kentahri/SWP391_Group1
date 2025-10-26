package com.group1.swp.pizzario_swp391.controller.manager;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.group1.swp.pizzario_swp391.annotation.ManagerUrl;
import com.group1.swp.pizzario_swp391.dto.table.TableCreateDTO;
import com.group1.swp.pizzario_swp391.dto.table.TableManagementDTO;
import com.group1.swp.pizzario_swp391.entity.DiningTable;
import com.group1.swp.pizzario_swp391.service.TableService;

import jakarta.validation.Valid;

@Controller
@ManagerUrl
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class TableController {

    TableService tableService;

    /**
     * Hiển thị trang quản lý bàn với filter
     */
    @GetMapping("/tables")
    public String table(@RequestParam(required = false) String condition, Model model) {
        model.addAttribute("tableCreateDTO", new TableCreateDTO());
        model.addAttribute("tableManagementDTO", new TableManagementDTO());
        model.addAttribute("tableConditions", DiningTable.TableCondition.values());
        
        // Xử lý lọc bàn
        if (condition == null || condition.isEmpty() || condition.equals("ALL")) {
            // Hiển thị tất cả bàn
            model.addAttribute("tables", tableService.getAllTablesForManager());
            model.addAttribute("selectedCondition", "ALL");
        } else if (condition.equals("NON_RETIRED")) {
            // Hiển thị các bàn không ở trạng thái RETIRED
            model.addAttribute("tables", tableService.findNonRetiredTables());
            model.addAttribute("selectedCondition", "NON_RETIRED");
        } else {
            // Lọc theo điều kiện cụ thể
            try {
                DiningTable.TableCondition tableCondition = DiningTable.TableCondition.valueOf(condition);
                model.addAttribute("tables", tableService.findTableByCondition(tableCondition));
                model.addAttribute("selectedCondition", condition);
            } catch (IllegalArgumentException e) {
                // Nếu điều kiện không hợp lệ, hiển thị tất cả bàn
                model.addAttribute("tables", tableService.getAllTablesForManager());
                model.addAttribute("selectedCondition", "ALL");
            }
        }
        
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
            model.addAttribute("tables", tableService.getAllTablesForManager());
            model.addAttribute("selectedCondition", "ALL");
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
            model.addAttribute("tables", tableService.getAllTablesForManager());
            model.addAttribute("selectedCondition", "ALL");
            return "admin_page/table_management";
        }
        
        try {
            tableService.updateTable(id, tableManagementDTO);
            return "redirect:/manager/tables";
        } catch (RuntimeException e) {
            // Thêm lỗi vào model để hiển thị trên giao diện
            model.addAttribute("tableCreateDTO", new TableCreateDTO());
            model.addAttribute("tableConditions", DiningTable.TableCondition.values());
            model.addAttribute("tables", tableService.getAllTablesForManager());
            model.addAttribute("selectedCondition", "ALL");
            model.addAttribute("updateError", e.getMessage());
            model.addAttribute("errorTableId", id);
            return "admin_page/table_management";
        }
    }
}
