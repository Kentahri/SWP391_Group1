package com.group1.swp.pizzario_swp391.controller;

import com.group1.swp.pizzario_swp391.annotation.AdminController;
import com.group1.swp.pizzario_swp391.dto.ShiftDTO;
import com.group1.swp.pizzario_swp391.dto.staff.StaffResponseDTO;
import com.group1.swp.pizzario_swp391.dto.staffshift.StaffShiftDTO;
import com.group1.swp.pizzario_swp391.service.ShiftService;
import com.group1.swp.pizzario_swp391.service.StaffService;
import com.group1.swp.pizzario_swp391.service.StaffShiftService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AdminController
@RequiredArgsConstructor
public class StaffShiftController {

    StaffShiftService staffShiftService;

    StaffService staffService;
    ShiftService shiftService;
    // removed unused mapperResponse

    @GetMapping("/staff_shifts")
    public String listStaffShifts(Model model) {
        var rows = staffShiftService.search(null, null, null, null); // hoặc service.findAll()
        model.addAttribute("rows", rows);
        return "admin_page/staff_shift/shift_management";
    }

    @GetMapping("/staff_shifts/create")
    public String createFormStaffShift(
            @RequestParam(required = false) Integer editId,
            Model model) {

        List<StaffResponseDTO> staffs = staffService.getAllStaff();

        List<ShiftDTO> shift = shiftService.getAllShift();
        model.addAttribute("shifts", shift);
        model.addAttribute("staffs", staffs);

        if (editId != null) {
            // Chế độ edit - load dữ liệu cũ
            StaffShiftDTO staffShift = staffShiftService.getById(editId);
            model.addAttribute("staffShift", staffShift);
            model.addAttribute("editId", editId);
            model.addAttribute("isEdit", true);
        } else {
            // Chế độ tạo mới
            model.addAttribute("staffShift", new StaffShiftDTO());
            model.addAttribute("isEdit", false);
        }

        return "admin_page/staff_shift/edit";
    }

    @PostMapping("/staff_shifts/create")
    public String createStaffShift(@Valid @ModelAttribute("staffShift") StaffShiftDTO staffShiftDTO,
            BindingResult bindingResult,
            RedirectAttributes ra, Model model) {

        model.addAttribute("isEdit", false);

        if (bindingResult.hasErrors()) {
            // Trả về lại form để hiển thị lỗi + giữ dữ liệu đã nhập
            model.addAttribute("staffs", staffService.getAllStaff());
            model.addAttribute("shifts", shiftService.getAllShift());
            return "admin_page/staff_shift/edit";
        }

        staffShiftService.create(staffShiftDTO);

        ra.addFlashAttribute("message", "Create successfully");

        return "redirect:/manager/staff_shifts";
    }

    @PostMapping("/staff_shifts/delete/{id}")
    public String deleteStaffShift(@PathVariable int id, RedirectAttributes ra) {
        staffShiftService.delete(id);
        ra.addFlashAttribute("msg", "Xóa phân công thành công");
        return "redirect:staff_shifts";
    }

    @GetMapping("/staff_shifts/edit/{id}")
    public String showEditForm(@PathVariable int id, Model model) {
        StaffShiftDTO staffShift = staffShiftService.getById(id);
        model.addAttribute("staffs", staffService.getAllStaff());
        model.addAttribute("shifts", shiftService.getAllShift());
        model.addAttribute("staffShift", staffShift);
        return "admin_page/staff_shift/edit";
    }

    @PostMapping("/staff_shifts/edit/{id}")
    public String updateStaffShift(@PathVariable("id") int id,
            @Valid @ModelAttribute("staffShift") StaffShiftDTO staffShiftDTO, BindingResult bindingResult, Model model,
            RedirectAttributes ra) {

        model.addAttribute("isEdit", true);
        model.addAttribute("editId", id);

        if (bindingResult.hasErrors()) {
            model.addAttribute("staffs", staffService.getAllStaff());
            model.addAttribute("shifts", shiftService.getAllShift());
            return "admin_page/staff_shift/edit";
        }

        staffShiftService.update(staffShiftDTO, id);

        ra.addFlashAttribute("message", "Update successfully");

        return "redirect:/manager/staff_shifts";
    }

}
