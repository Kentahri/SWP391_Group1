package com.group1.swp.pizzario_swp391.controller;

import com.group1.swp.pizzario_swp391.annotation.ManagerUrl;
import com.group1.swp.pizzario_swp391.dto.ShiftDTO;
import com.group1.swp.pizzario_swp391.entity.Shift;
import com.group1.swp.pizzario_swp391.service.ShiftService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;

@ManagerUrl
@Controller
@RequiredArgsConstructor
public class ShiftController {

    private final ShiftService service;

    private List<String> getShiftTypes() {
        return Arrays.stream(Shift.ShiftType.values())
                .map(Enum::name)
                .toList();
    }

    @GetMapping("/shifts")
    public String list(Model model) {
        // Get all shifts
        List<ShiftDTO> shifts = service.getAllShift();
        model.addAttribute("shifts", shifts);

        // Add shift form and types for modal
        if (!model.containsAttribute("shiftForm")) {
            model.addAttribute("shiftForm", new ShiftDTO());
        }
        model.addAttribute("shiftTypes", getShiftTypes());

        return "admin_page/shift/shift-list";
    }

    @GetMapping("/shift/create")
    public String create(@RequestParam(required = false, defaultValue = "staff_shifts") String returnPage,
                         RedirectAttributes redirectAttributes) {

        // Prepare empty form and flag to open modal in create mode
        redirectAttributes.addFlashAttribute("shiftForm", new ShiftDTO());
        redirectAttributes.addFlashAttribute("openShiftModal", "create");

        // Redirect back to the page that initiated the action
        if ("shifts".equals(returnPage)) {
            return "redirect:/manager/shifts";
        } else {
            return "redirect:/manager/staff_shifts";
        }
    }

    @GetMapping("/shift/edit/{id}")
    public String update(@PathVariable Integer id,
            @RequestParam(required = false, defaultValue = "staff_shifts") String returnPage,
            RedirectAttributes redirectAttributes) {

        ShiftDTO shift = service.getShiftById(id);

        // Always open modal for edit
        redirectAttributes.addFlashAttribute("shiftForm", shift);
        redirectAttributes.addFlashAttribute("openShiftModal", "edit");

        // Determine which page to return to
        if ("shifts".equals(returnPage)) {
            return "redirect:/manager/shifts";
        } else {
            return "redirect:/manager/staff_shifts";
        }
    }

    // Save endpoint for modal (handles both create and update)
    @PostMapping("/shift/save")
    public String saveShift(@Valid @ModelAttribute("shiftForm") ShiftDTO shiftDTO,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        if (shiftDTO.getStartTime() != null && shiftDTO.getEndTime() != null) {
            if (!shiftDTO.getStartTime().isBefore(shiftDTO.getEndTime())) {
                bindingResult.rejectValue("endTime", "error.shiftForm",
                        "Giờ bắt đầu phải trước giờ kết thúc");
            }
        }

        if (bindingResult.hasErrors()) {
            // If there are validation errors, return to modal with errors
            redirectAttributes.addFlashAttribute("shiftForm", shiftDTO);
            redirectAttributes.addFlashAttribute("openShiftModal", shiftDTO.getId() != null ? "edit" : "create");
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.shiftForm",
                    bindingResult);
            return "redirect:/manager/shifts";
        }

        if (shiftDTO.getId() != null && shiftDTO.getId() > 0) {
            // Update existing shift
            service.updateShift(shiftDTO.getId(), shiftDTO);
            redirectAttributes.addFlashAttribute("message", "Cập nhật ca làm việc thành công");
        } else {
            // Create new shift
            service.createShift(shiftDTO);
            redirectAttributes.addFlashAttribute("message", "Tạo ca làm việc thành công");
        }

        return "redirect:/manager/shifts";
    }

    // Xóa
    @GetMapping("/shift/delete/{id}")
    public String delete(@PathVariable Integer id, RedirectAttributes redirectAttributes) {

        service.deleteShift(id);

        redirectAttributes.addFlashAttribute("message", "Delete thành công");
        return "redirect:/manager/shifts";
    }

}
