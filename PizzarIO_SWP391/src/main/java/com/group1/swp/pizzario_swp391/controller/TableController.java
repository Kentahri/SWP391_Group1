package com.group1.swp.pizzario_swp391.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.group1.swp.pizzario_swp391.dto.TableDTO;
import com.group1.swp.pizzario_swp391.entity.DiningTable;
import com.group1.swp.pizzario_swp391.service.TableService;

@Controller
@RequestMapping("/table")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class TableController {

    TableService tableService;

    @GetMapping
    public String table(Model model) {
        model.addAttribute("tableTypes", DiningTable.TableType.values());
        model.addAttribute("tableDTO", new TableDTO());
        model.addAttribute("tableStatuses", DiningTable.TableStatus.values());
        model.addAttribute("tableConditions", DiningTable.TableCondition.values());
        model.addAttribute("tables", tableService.getAllTables());
        return "table";
    }

    @PostMapping("/add")
    public String createNewTable(@ModelAttribute TableDTO tableDTO) {
        tableService.createNewTable(tableDTO);
        return "redirect:/table";
    }

    @PostMapping("/delete/{id}")
    public String deleteTable(@PathVariable int id) {
        tableService.deleteTable(id);
        return "redirect:/table";
    }

    @PostMapping("/update/{id}")
    public String updateTable(@PathVariable int id, @ModelAttribute TableDTO tableDTO) {
        tableService.updateTable(id, tableDTO);
        return "redirect:/table";
    }
}
