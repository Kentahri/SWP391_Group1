package com.group1.swp.pizzario_swp391.controller.manager;

import java.util.List;


import com.group1.swp.pizzario_swp391.annotation.ManagerUrl;
import com.group1.swp.pizzario_swp391.exception.ValidationException;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.group1.swp.pizzario_swp391.dto.staff.StaffCreateDTO;
import com.group1.swp.pizzario_swp391.dto.staff.StaffResponseDTO;
import com.group1.swp.pizzario_swp391.dto.staff.StaffUpdateDTO;
import com.group1.swp.pizzario_swp391.entity.Staff;
import com.group1.swp.pizzario_swp391.service.StaffService;

import lombok.RequiredArgsConstructor;


@Controller
@RequiredArgsConstructor
@ManagerUrl
public class StaffController {

    final StaffService staffService;

    // READ: list all staff
    @GetMapping("/staff")
    public String listStaffs(Model model) {
        List<StaffResponseDTO> staffs = staffService.getAllStaff();
        model.addAttribute("staffs", staffs);
        model.addAttribute("roles", Staff.Role.values());
        return "admin_page/staff/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("staff", new StaffCreateDTO());
        model.addAttribute("roles", Staff.Role.values());
        model.addAttribute("formTitle", "Create Staff");
        return "admin_page/staff/create";
    }

    // CREATE SUBMIT: POST /staff/create
    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("staff") StaffCreateDTO dto,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes ra) {

        // ❗ Khi có lỗi validate, QUAY LẠI đúng view "staff/create"
        if (bindingResult.hasErrors()) {
            model.addAttribute("roles", Staff.Role.values());
            model.addAttribute("formTitle", "Create Staff");
            return "admin_page/staff/create";
        }

        try {
            staffService.createNewStaff(dto);
            ra.addFlashAttribute("success", "Created staff successfully");
            return "redirect:staff";
        } catch (ValidationException e) {
            model.addAttribute("roles", Staff.Role.values());
            model.addAttribute("formTitle", "Create Staff");
            model.addAttribute("error", e.getMessage());
            return "admin_page/staff/create";
        }
    }

    // UPDATE: show edit form
    @GetMapping("/edit/{id}")
    public String edit(Model model, @PathVariable int id) {
        StaffUpdateDTO staffUpdateDTO = staffService.getStaffForUpdate(id);
        model.addAttribute("staff", staffUpdateDTO);
        model.addAttribute("staffID", id);
        model.addAttribute("roles", Staff.Role.values());
        return "admin_page/staff/edit";
    }

    // UPDATE: save update
    @PostMapping("/edit/{id}")
    public String updateStaff(@PathVariable int id,
                              @Valid @ModelAttribute StaffUpdateDTO staffUpdateDTO,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("staff", staffUpdateDTO);
            model.addAttribute("roles", Staff.Role.values());
            model.addAttribute("staffID", id);
            return "admin_page/staff/edit";
        }
        
        try {
            staffService.updateStaff(id, staffUpdateDTO);
            redirectAttributes.addFlashAttribute("success", "Cập nhật thành công!");
            return "redirect:staff";
        } catch (ValidationException e) {
            model.addAttribute("staff", staffUpdateDTO);
            model.addAttribute("roles", Staff.Role.values());
            model.addAttribute("staffID", id);
            model.addAttribute("error", e.getMessage());
            return "admin_page/staff/edit";
        }
    }

    // DELETE
    @PostMapping("/delete/{id}")
    public String deleteStaff(@PathVariable int id) {
        staffService.deleteStaffById(id);
        return "redirect:staff";
    }
}
