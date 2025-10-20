package com.group1.swp.pizzario_swp391.controller;

import com.group1.swp.pizzario_swp391.annotation.ManagerUrl;
import com.group1.swp.pizzario_swp391.dto.ShiftDTO;
import com.group1.swp.pizzario_swp391.dto.data_analytics.StatsStaffShiftDTO;
import com.group1.swp.pizzario_swp391.dto.staff.StaffResponseDTO;
import com.group1.swp.pizzario_swp391.dto.staffshift.StaffShiftDTO;
import com.group1.swp.pizzario_swp391.dto.staffshift.StaffShiftResponseDTO;
import com.group1.swp.pizzario_swp391.entity.Shift;
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

import java.time.LocalDate;
import java.time.DayOfWeek;
import java.util.*;

@Controller
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@ManagerUrl
@RequiredArgsConstructor
public class StaffShiftController {

    StaffShiftService staffShiftService;

    StaffService staffService;
    ShiftService shiftService;
    // removed unused mapperResponse

    @GetMapping("/staff_shifts")
    public String listStaffShifts(
            @RequestParam(required = false) Integer weekOffset,
            @RequestParam(required = false) Long staffId,
            @RequestParam(required = false) Long shiftId,
            Model model) {

        // 1. Tính toán tuần hiện tại (T2 -> CN)
        int offset = weekOffset != null ? weekOffset : 0;
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.plusWeeks(offset).with(DayOfWeek.MONDAY);
        LocalDate weekEnd = weekStart.plusDays(6);

        model.addAttribute("weekStartDate", weekStart);
        model.addAttribute("weekEndDate", weekEnd);

        // 2. Lấy dữ liệu cho filter dropdown
        List<StaffResponseDTO> allStaff = staffService.getAllStaff();
        List<ShiftDTO> allShifts = shiftService.getAllShift();
        model.addAttribute("allStaff", allStaff);
        model.addAttribute("allShifts", allShifts);
        model.addAttribute("selectedStaffId", staffId);
        model.addAttribute("selectedShiftId", shiftId);

        // Thêm danh sách shift types cho shift management section
        model.addAttribute("shifts", allShifts);

        // Add shift form and types for modal (Shift Type)
        if (!model.containsAttribute("shiftForm")) {
            model.addAttribute("shiftForm", new ShiftDTO());
        }
        List<String> shiftTypes = Arrays.stream(Shift.ShiftType.values())
                .map(Enum::name)
                .toList();
        model.addAttribute("shiftTypes", shiftTypes);

        // Add staff shift form for modal (Staff Shift Assignment)
        if (!model.containsAttribute("staffShiftForm")) {
            model.addAttribute("staffShiftForm", new StaffShiftDTO());
        }

        // 3. Lấy staff shifts trong tuần với filters
        List<StaffShiftResponseDTO> staffShifts = staffShiftService.findByDateRangeWithFilters(
                weekStart, weekEnd, staffId, shiftId);

        // 4. Tính toán statistics
        StatsStaffShiftDTO stats = staffShiftService.getAnalyticsStaffShift();
        model.addAttribute("stats", stats);

        // 5. Tạo weekDays với shift cards
        var weekDays = staffShiftService.buildWeekDays(weekStart, weekEnd, staffShifts);
        model.addAttribute("weekDays", weekDays);

        return "admin_page/staff_shift/shift_calendar";
    }

    @PostMapping("/staff_shifts/delete/{id}")
    public String deleteStaffShift(@PathVariable int id, RedirectAttributes ra) {
        staffShiftService.delete(id);
        ra.addFlashAttribute("message", "Xóa phân công thành công");
        return "redirect:/manager/staff_shifts";
    }

    @GetMapping("/staff_shifts/edit/{id}")
    public String showEditForm(@PathVariable int id, RedirectAttributes redirectAttributes) {
        StaffShiftDTO staffShift = staffShiftService.getById(id);

        // Always open modal for edit
        redirectAttributes.addFlashAttribute("staffShiftForm", staffShift);
        redirectAttributes.addFlashAttribute("openStaffShiftModal", "edit");
        return "redirect:/manager/staff_shifts";
    }

    // Save endpoint for modal (handles both create and update)
    @PostMapping("/staff_shifts/save")
    public String saveStaffShift(@Valid @ModelAttribute("staffShiftForm") StaffShiftDTO staffShiftDTO,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes ra) {

        if (bindingResult.hasErrors()) {
            // If there are validation errors, return to modal with errors
            ra.addFlashAttribute("staffShiftForm", staffShiftDTO);
            ra.addFlashAttribute("openStaffShiftModal", staffShiftDTO.getId() != null ? "edit" : "create");
            ra.addFlashAttribute("errors", bindingResult.getAllErrors());
            return "redirect:/manager/staff_shifts";
        }

        if (staffShiftDTO.getId() != null && staffShiftDTO.getId() > 0) {
            // Update existing staff shift
            staffShiftService.update(staffShiftDTO, staffShiftDTO.getId());
            ra.addFlashAttribute("message", "Cập nhật ca làm việc thành công");
        } else {
            // Create new staff shift
            staffShiftService.create(staffShiftDTO);
            ra.addFlashAttribute("message", "Thêm ca làm việc thành công");
        }

        return "redirect:/manager/staff_shifts";
    }

}
