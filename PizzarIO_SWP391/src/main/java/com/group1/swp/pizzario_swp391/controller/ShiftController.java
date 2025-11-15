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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        List<ShiftDTO> shifts = service.getAllShift();
        model.addAttribute("shifts", shifts);

        Map<Integer, Boolean> canDelete = new HashMap<>();
        for (ShiftDTO shift : shifts) {
            canDelete.put(shift.getId(), !service.hasStaffShifts(shift.getId()));
        }
        model.addAttribute("canDelete", canDelete);

        if (!model.containsAttribute("shiftForm")) {
            model.addAttribute("shiftForm", new ShiftDTO());
        }
        model.addAttribute("shiftTypes", getShiftTypes());

        return "admin_page/shift/shift-list";
    }

    @GetMapping("/shift/create")
    public String create(@RequestParam(required = false, defaultValue = "staff_shifts") String returnPage,
            RedirectAttributes redirectAttributes) {

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

    @PostMapping("/shift/save")
    public String saveShift(@Valid @ModelAttribute("shiftForm") ShiftDTO shiftDTO,
            @RequestParam(required = false) String startTimeOnly,
            @RequestParam(required = false) String endTimeOnly,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        // Auto-combine time with today's date
        if (startTimeOnly != null && !startTimeOnly.isEmpty()) {
            try {
                java.time.LocalTime startTime = java.time.LocalTime.parse(startTimeOnly);
                shiftDTO.setStartTime(java.time.LocalDate.now().atTime(startTime));
            } catch (Exception e) {
                bindingResult.rejectValue("startTime", "error.shiftForm", "Giờ bắt đầu không hợp lệ");
            }
        }

        if (endTimeOnly != null && !endTimeOnly.isEmpty()) {
            try {
                java.time.LocalTime endTime = java.time.LocalTime.parse(endTimeOnly);
                shiftDTO.setEndTime(java.time.LocalDate.now().atTime(endTime));
            } catch (Exception e) {
                bindingResult.rejectValue("endTime", "error.shiftForm", "Giờ kết thúc không hợp lệ");
            }
        }

        if (shiftDTO.getStartTime() == null) {
            bindingResult.rejectValue("startTime", "error.shiftForm",
                    "Giờ bắt đầu không được để trống");
        }
        if (shiftDTO.getEndTime() == null) {
            bindingResult.rejectValue("endTime", "error.shiftForm",
                    "Giờ kết thúc không được để trống");
        }

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

    @GetMapping("/shift/delete/{id}")
    public String delete(@PathVariable Integer id, RedirectAttributes redirectAttributes) {

        if (service.hasStaffShifts(id)) {
            redirectAttributes.addFlashAttribute("error",
                    "Không thể xóa ca làm việc này vì đã có nhân viên được phân công. Vui lòng xóa các phân công trước.");
            return "redirect:/manager/shifts";
        }

        service.deleteShift(id);
        redirectAttributes.addFlashAttribute("message", "Xóa ca làm việc thành công");
        return "redirect:/manager/shifts";
    }

}
