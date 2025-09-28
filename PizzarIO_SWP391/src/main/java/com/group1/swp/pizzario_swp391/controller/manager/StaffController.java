package com.group1.swp.pizzario_swp391.controller.manager;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.group1.swp.pizzario_swp391.dto.staff.StaffCreateDTO;
import com.group1.swp.pizzario_swp391.dto.staff.StaffResponseDTO;
import com.group1.swp.pizzario_swp391.dto.staff.StaffUpdateDTO;
import com.group1.swp.pizzario_swp391.entity.Staff;
import com.group1.swp.pizzario_swp391.service.StaffService;

import lombok.RequiredArgsConstructor;


@Controller
@RequestMapping("/staff")
@RequiredArgsConstructor
public class StaffController {

    final StaffService staffService;

    // READ: list all staff
    @GetMapping
    public String listStaffs(Model model) {
        List<StaffResponseDTO> staffs = staffService.getAllStaff();
        model.addAttribute("staffs", staffs);
        model.addAttribute("roles", Staff.Role.values());
        return "admin_page/staff/list";
    }

    // CREATE: show form
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("staff", new StaffCreateDTO());
        model.addAttribute("roles", Staff.Role.values());
        return "admin_page/staff/create";
    }

    // CREATE: save staff
    @PostMapping("/create")
    public String createStaff(@ModelAttribute StaffCreateDTO staffCreateDTO, RedirectAttributes redirectAttributes) {
        try {
            staffService.createNewStaff(staffCreateDTO);
            redirectAttributes.addFlashAttribute("success", "Tạo staff thành công!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/staff/create";
        }
        return "redirect:/staff";
    }

    // UPDATE: show edit form
    @GetMapping("edit/{id}")
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
                              @ModelAttribute StaffUpdateDTO staffUpdateDTO,
                              RedirectAttributes redirectAttributes) {
        try {
            staffService.updateStaff(id, staffUpdateDTO);
            redirectAttributes.addFlashAttribute("success", "Cập nhật thành công!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/staff/edit/" + id;
        }
        return "redirect:/staff";
    }

    // DELETE
    @PostMapping("/delete/{id}")
    public String deleteStaff(@PathVariable int id) {
        staffService.deleteStaffById(id);
        return "redirect:/staff";
    }
}
