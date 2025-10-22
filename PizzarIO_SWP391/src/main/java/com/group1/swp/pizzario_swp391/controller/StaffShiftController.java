package com.group1.swp.pizzario_swp391.controller;

import com.group1.swp.pizzario_swp391.annotation.ManagerUrl;
import com.group1.swp.pizzario_swp391.dto.ShiftDTO;
import com.group1.swp.pizzario_swp391.dto.data_analytics.StatsStaffShiftDTO;
import com.group1.swp.pizzario_swp391.dto.data_analytics.WeekDayDTO;
import com.group1.swp.pizzario_swp391.dto.staff.StaffResponseDTO;
import com.group1.swp.pizzario_swp391.dto.staffshift.StaffShiftDTO;
import com.group1.swp.pizzario_swp391.dto.staffshift.StaffShiftResponseDTO;
import com.group1.swp.pizzario_swp391.entity.Shift;
import com.group1.swp.pizzario_swp391.entity.StaffShift;
import com.group1.swp.pizzario_swp391.repository.StaffShiftRepository;
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
    StaffShiftRepository staffShiftRepository;

    StaffService staffService;
    ShiftService shiftService;
    // removed unused mapperResponse

    @GetMapping("/staff_shifts")
    public String listStaffShifts(
            @RequestParam(required = false) Integer weekOffset,
            @RequestParam(required = false) Long staffId,
            @RequestParam(required = false) Long shiftId,
            Model model) {

        int offset = weekOffset != null ? weekOffset : 0;
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.plusWeeks(offset).with(DayOfWeek.MONDAY);
        LocalDate weekEnd = weekStart.plusDays(6);

        model.addAttribute("weekStartDate", weekStart);
        model.addAttribute("weekEndDate", weekEnd);

        List<StaffResponseDTO> allStaff = staffService.getAllStaff();
        List<ShiftDTO> allShifts = shiftService.getAllShift();
        model.addAttribute("allStaff", allStaff);
        model.addAttribute("allShifts", allShifts);
        model.addAttribute("selectedStaffId", staffId);
        model.addAttribute("selectedShiftId", shiftId);

        model.addAttribute("shifts", allShifts);

        if (!model.containsAttribute("shiftForm")) {
            model.addAttribute("shiftForm", new ShiftDTO());
        }
        List<String> shiftTypes = Arrays.stream(Shift.ShiftType.values())
                .map(Enum::name)
                .toList();
        model.addAttribute("shiftTypes", shiftTypes);

        if (!model.containsAttribute("staffShiftForm")) {
            model.addAttribute("staffShiftForm", new StaffShiftDTO());
        }

        StatsStaffShiftDTO stats = staffShiftService.getAnalyticsStaffShift();
        model.addAttribute("stats", stats);

        List<StaffShiftResponseDTO> staffShifts = staffShiftService.findByDateRangeWithFilters(
                weekStart, weekEnd, staffId, shiftId);

        List<WeekDayDTO> weekDays = staffShiftService.buildWeekDays(weekStart, weekEnd, staffShifts);
        model.addAttribute("weekDays", weekDays);

        return "admin_page/staff_shift/shift_calendar";
    }

    @PostMapping("/staff_shifts/delete/{id}")
    public String deleteStaffShift(
            @PathVariable int id,
            @RequestParam(required = false) Integer weekOffset,
            @RequestParam(required = false) Long filterStaffId,
            @RequestParam(required = false) Long filterShiftId,
            RedirectAttributes ra) {
        staffShiftService.delete(id);
        ra.addFlashAttribute("message", "Xóa phân công thành công");

        // Build redirect URL with preserved parameters
        StringBuilder redirectUrl = new StringBuilder("redirect:/manager/staff_shifts");
        boolean hasParam = false;

        if (weekOffset != null) {
            redirectUrl.append("?weekOffset=").append(weekOffset);
            hasParam = true;
        }
        if (filterStaffId != null) {
            redirectUrl.append(hasParam ? "&" : "?").append("staffId=").append(filterStaffId);
            hasParam = true;
        }
        if (filterShiftId != null) {
            redirectUrl.append(hasParam ? "&" : "?").append("shiftId=").append(filterShiftId);
        }

        return redirectUrl.toString();
    }

    @GetMapping("/staff_shifts/create")
    public String showCreateForm(
            @RequestParam(required = false) Integer weekOffset,
            @RequestParam(required = false) Long filterStaffId,
            @RequestParam(required = false) Long filterShiftId,
            RedirectAttributes redirectAttributes) {
        // Create empty form for create mode
        redirectAttributes.addFlashAttribute("staffShiftForm", new StaffShiftDTO());
        redirectAttributes.addFlashAttribute("openStaffShiftModal", "create");

        // Build redirect URL with preserved parameters
        StringBuilder redirectUrl = new StringBuilder("redirect:/manager/staff_shifts");
        boolean hasParam = false;

        if (weekOffset != null) {
            redirectUrl.append("?weekOffset=").append(weekOffset);
            hasParam = true;
        }
        if (filterStaffId != null) {
            redirectUrl.append(hasParam ? "&" : "?").append("staffId=").append(filterStaffId);
            hasParam = true;
        }
        if (filterShiftId != null) {
            redirectUrl.append(hasParam ? "&" : "?").append("shiftId=").append(filterShiftId);
        }

        return redirectUrl.toString();
    }

    @GetMapping("/staff_shifts/edit/{id}")
    public String showEditForm(
            @PathVariable int id,
            @RequestParam(required = false) Integer weekOffset,
            @RequestParam(required = false) Long filterStaffId,
            @RequestParam(required = false) Long filterShiftId,
            RedirectAttributes redirectAttributes) {
        StaffShiftDTO staffShift = staffShiftService.getById(id);

        // Always open modal for edit
        redirectAttributes.addFlashAttribute("staffShiftForm", staffShift);
        redirectAttributes.addFlashAttribute("openStaffShiftModal", "edit");

        // Build redirect URL with preserved parameters
        StringBuilder redirectUrl = new StringBuilder("redirect:/manager/staff_shifts");
        boolean hasParam = false;

        if (weekOffset != null) {
            redirectUrl.append("?weekOffset=").append(weekOffset);
            hasParam = true;
        }
        if (filterStaffId != null) {
            redirectUrl.append(hasParam ? "&" : "?").append("staffId=").append(filterStaffId);
            hasParam = true;
        }
        if (filterShiftId != null) {
            redirectUrl.append(hasParam ? "&" : "?").append("shiftId=").append(filterShiftId);
        }

        return redirectUrl.toString();
    }

    // Save endpoint for modal (handles both create and update)
    @PostMapping("/staff_shifts/save")
    public String saveStaffShift(
            @Valid @ModelAttribute("staffShiftForm") StaffShiftDTO staffShiftDTO,
            BindingResult bindingResult,
            @RequestParam(required = false) Integer weekOffset,
            @RequestParam(required = false) Long filterStaffId,
            @RequestParam(required = false) Long filterShiftId,
            Model model,
            RedirectAttributes ra) {

        StringBuilder redirectUrl = new StringBuilder("redirect:/manager/staff_shifts");
        boolean hasParam = false;

        if (weekOffset != null) {
            redirectUrl.append("?weekOffset=").append(weekOffset);
            hasParam = true;
        }
        if (filterStaffId != null) {
            redirectUrl.append(hasParam ? "&" : "?").append("staffId=").append(filterStaffId);
            hasParam = true;
        }
        if (filterShiftId != null) {
            redirectUrl.append(hasParam ? "&" : "?").append("shiftId=").append(filterShiftId);
        }

        // Custom validation: Only for CREATE mode (not EDIT), prevent past dates
        if (staffShiftDTO.getId() == null || staffShiftDTO.getId() <= 0) {
            if (staffShiftDTO.getWorkDate() != null && staffShiftDTO.getWorkDate().isBefore(LocalDate.now())) {
                bindingResult.rejectValue("workDate", "error.workDate", "Ngày làm không được trước ngày hiện tại");
            }

            // Check duplicate: Same staff + same date + same shift (only for CREATE)
            if (staffShiftDTO.getWorkDate() != null && staffShiftDTO.getStaffId() != null
                    && staffShiftDTO.getShiftId() != null) {
                List<StaffShift> existingShifts = staffShiftRepository.findAllShiftsByStaffIdAndDate(
                        staffShiftDTO.getStaffId(),
                        staffShiftDTO.getWorkDate());

                boolean hasDuplicate = existingShifts.stream()
                        .anyMatch(ss -> ss.getShift().getId() == staffShiftDTO.getShiftId());

                if (hasDuplicate) {
                    bindingResult.rejectValue("shiftId", "error.duplicate",
                            "Nhân viên này đã có ca làm việc này trong ngày đã chọn");
                }
            }
        }

        if (bindingResult.hasErrors()) {
            // If there are validation errors, return to modal with errors
            ra.addFlashAttribute("staffShiftForm", staffShiftDTO);
            ra.addFlashAttribute("openStaffShiftModal", staffShiftDTO.getId() != null ? "edit" : "create");
            ra.addFlashAttribute("org.springframework.validation.BindingResult.staffShiftForm", bindingResult);
            return redirectUrl.toString();
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

        return redirectUrl.toString();
    }

}
