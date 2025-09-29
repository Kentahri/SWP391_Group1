package com.group1.swp.pizzario_swp391.controller;

import com.group1.swp.pizzario_swp391.dto.StaffDTO;
import com.group1.swp.pizzario_swp391.entity.Staff;
import com.group1.swp.pizzario_swp391.mapper.StaffMapper;
import com.group1.swp.pizzario_swp391.service.StaffService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/staff")
@RequiredArgsConstructor
public class StaffController {

    private final StaffService staffService;
    private final StaffMapper staffMapper;

    // READ: list all staff
    @GetMapping
    public String listStaffs(Model model) {
        List<Staff> staffs = staffService.getAllStaff();
        model.addAttribute("staffs", staffs);
        model.addAttribute("roles", Staff.Role.values());
        return "admin_page/staff/list";
    }

    // CREATE: show form
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("staff", new StaffDTO());
        model.addAttribute("roles", Staff.Role.values());
        return "admin_page/staff/create";
    }

    // CREATE: save staff
    @PostMapping("/create")
    public String createStaff(@ModelAttribute StaffDTO staffDTO, RedirectAttributes redirectAttributes) {
        try {
            staffService.createNewStaff(staffDTO); // service sẽ tự dùng mapper
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
        Staff staff = staffService.getStaffById(id).orElseThrow(() -> new RuntimeException("Staff id " + id + " not found"));
        StaffDTO staffDTO = StaffDTO.builder()
                .name(staff.getName())
                .dateOfBirth(staff.getDateOfBirth())
                .phone(staff.getPhone())
                .address(staff.getAddress())
                .username(staff.getUsername())
                .email(staff.getEmail())
                .role(staff.getRole())
                .active(staff.isActive())
                .build();
        model.addAttribute("staff", staffDTO);
        model.addAttribute("staffID", id);
        model.addAttribute("roles", Staff.Role.values());
        return "admin_page/staff/edit";
    }

    // UPDATE: save update
    @PostMapping("/edit/{id}")
    public String updateStaff(@PathVariable int id,
                              @ModelAttribute StaffDTO staffDTO,
                              RedirectAttributes redirectAttributes) {
        try {
            staffService.updateStaff(id, staffDTO);
            redirectAttributes.addFlashAttribute("success", "Cập nhật thành công!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/staff/edit/" + id; // quay lại form edit
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
