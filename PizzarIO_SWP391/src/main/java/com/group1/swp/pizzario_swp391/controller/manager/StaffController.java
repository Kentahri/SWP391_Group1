package com.group1.swp.pizzario_swp391.controller.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.group1.swp.pizzario_swp391.annotation.ManagerUrl;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.group1.swp.pizzario_swp391.dto.staff.StaffCreateDTO;
import com.group1.swp.pizzario_swp391.dto.staff.StaffResponseDTO;
import com.group1.swp.pizzario_swp391.dto.staff.StaffUpdateDTO;
import com.group1.swp.pizzario_swp391.entity.Staff;
import com.group1.swp.pizzario_swp391.service.ShiftService;
import com.group1.swp.pizzario_swp391.service.StaffService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@ManagerUrl
public class StaffController {
    private final StaffService staffService;
    private final ShiftService shiftService;

    // READ: list all staff
    @GetMapping("/staff")
    public String listStaffs(Model model) {
        // Get staff data
        List<StaffResponseDTO> staffs = staffService.getAllStaff();
        model.addAttribute("staffs", staffs);
        model.addAttribute("roles", Staff.Role.values());

        // Get shift data for salary display
        var shifts = shiftService.findAllDto();
        model.addAttribute("shifts", shifts);

        // Get payroll totals for charts
        Map<String, Long> totals = shiftService.getWeeklyPayrollTotals();
        List<String> labels = new ArrayList<>(totals.keySet());
        List<Long> data = new ArrayList<>(totals.values());
        model.addAttribute("chartLabels", labels);
        model.addAttribute("chartData", data);

        // Calculate stats
        long totalStaff = staffs.size();
        long activeStaff = staffs.stream().filter(StaffResponseDTO::isActive).count();
        model.addAttribute("totalStaff", totalStaff);
        model.addAttribute("activeStaff", activeStaff);

        return "admin_page/staff/list";
    }

    @GetMapping("/staff/create")
    public String createForm(Model model) {
        model.addAttribute("staff", new StaffCreateDTO());
        model.addAttribute("roles", Staff.Role.values());
        model.addAttribute("formTitle", "Create Staff");
        return "admin_page/staff/create";
    }

    // CREATE SUBMIT: POST /staff/create
    @PostMapping("/staff/create")
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

          String errorMsg = staffService.createNewStaff(dto);
        if (errorMsg != null) {
            model.addAttribute("roles", Staff.Role.values());
            model.addAttribute("formTitle", "Create Staff");
            model.addAttribute("error", errorMsg);
            return "admin_page/staff/create";
        }
        ra.addFlashAttribute("success", "Tạo nhân viên mới thành công!");
        return "redirect:/manager/staff";
    }

    // UPDATE: show edit form
    @GetMapping("/staff/edit/{id}")
    public String edit(Model model, @PathVariable int id) {
        StaffUpdateDTO staffUpdateDTO = staffService.getStaffForUpdate(id);
        model.addAttribute("staff", staffUpdateDTO);
        model.addAttribute("staffID", id);
        model.addAttribute("roles", Staff.Role.values());
        return "admin_page/staff/edit";
    }

    // UPDATE: save update
    @PostMapping("/staff/edit/{id}")
    public String updateStaff(@PathVariable int id,
                              @Valid @ModelAttribute("staff") StaffUpdateDTO staffUpdateDTO,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("roles", Staff.Role.values());
            model.addAttribute("staffID", id);
            return "admin_page/staff/edit";
        }
        
        String errorMsg = staffService.updateStaff(id, staffUpdateDTO);
        if (errorMsg != null) {
            model.addAttribute("staff", staffUpdateDTO);
            model.addAttribute("roles", Staff.Role.values());
            model.addAttribute("staffID", id);
            model.addAttribute("error", errorMsg);
            return "admin_page/staff/edit";
        }
        redirectAttributes.addFlashAttribute("success", "Cập nhật thành công!");
        return "redirect:/manager/staff";
    }

    // TOGGLE ACTIVE STATUS
    @GetMapping("/staff/toggle-active/{id}")
    public String toggleActive(@PathVariable int id, RedirectAttributes redirectAttributes) {
        try {
            System.out.println("=== TOGGLE ACTIVE CALLED ===");
            System.out.println("Staff ID: " + id);
            System.out.println("Endpoint: /pizzario/manager/staff/toggle-active/" + id);
            
            staffService.toggleStaffActive(id);
            redirectAttributes.addFlashAttribute("success", "Cập nhật trạng thái nhân viên thành công");
            System.out.println("Toggle successful");
        } catch (RuntimeException e) {
            System.err.println("Error toggling staff active status: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Không thể cập nhật trạng thái nhân viên: " + e.getMessage());
        }
        return "redirect:/manager/staff";
    }

}
