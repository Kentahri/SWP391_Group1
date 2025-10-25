package com.group1.swp.pizzario_swp391.controller;

import com.group1.swp.pizzario_swp391.annotation.ManagerUrl;
import com.group1.swp.pizzario_swp391.dto.ShiftDTO;
import com.group1.swp.pizzario_swp391.dto.data_analytics.StatsStaffShiftDTO;
import com.group1.swp.pizzario_swp391.dto.data_analytics.WeekDayDTO;
import com.group1.swp.pizzario_swp391.dto.staff.StaffResponseDTO;
import com.group1.swp.pizzario_swp391.dto.staffshift.StaffShiftDTO;
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
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

@Controller
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@ManagerUrl
@RequiredArgsConstructor
public class StaffShiftController {

    StaffShiftService staffShiftService;
    StaffShiftRepository staffShiftRepository;

    StaffService staffService;
    ShiftService shiftService;

    // =========================
    // LIST CALENDAR PAGE
    // =========================
    @GetMapping("/staff_shifts")
    public String listStaffShifts(
            @RequestParam(required = false) Integer weekOffset,
            @RequestParam(required = false) Long staffId,
            @RequestParam(required = false) Long shiftId,
            Model model,
            CsrfToken token
    ) {
        // CSRF to <meta>
        model.addAttribute("_csrf", token);

        int offset = (weekOffset != null) ? weekOffset : 0;
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.plusWeeks(offset).with(DayOfWeek.MONDAY);
        LocalDate weekEnd   = weekStart.plusDays(6);

        model.addAttribute("weekStartDate", weekStart);
        model.addAttribute("weekEndDate", weekEnd);

        List<StaffResponseDTO> allStaff = staffService.getStaffWithoutManager();
        List<ShiftDTO> allShifts        = shiftService.getAllShift();
        model.addAttribute("allStaff", allStaff);
        model.addAttribute("allShifts", allShifts);
        model.addAttribute("selectedStaffId", staffId);
        model.addAttribute("selectedShiftId", shiftId);

        // Optionally expose shift types
        List<String> shiftTypes = Arrays.stream(Shift.ShiftType.values())
                .map(Enum::name).toList();
        model.addAttribute("shiftTypes", shiftTypes);

        StatsStaffShiftDTO stats = staffShiftService.getAnalyticsStaffShift();
        model.addAttribute("stats", stats);

        List<com.group1.swp.pizzario_swp391.dto.staffshift.StaffShiftResponseDTO> staffShifts =
                staffShiftService.findByDateRangeWithFilters(weekStart, weekEnd, staffId, shiftId);

        List<WeekDayDTO> weekDays = staffShiftService.buildWeekDays(weekStart, weekEnd, staffShifts);
        model.addAttribute("weekDays", weekDays);

        return "admin_page/staff_shift/shift_calendar";
    }

    // =========================
    // GET ONE FOR EDIT (JSON)
    // =========================
    @GetMapping("/staff_shifts/{id}")
    @ResponseBody
    public StaffShiftDTO getOne(@PathVariable int id) {
        return staffShiftService.getById(id);
    }

    // =========================
    // SAVE (CREATE or UPDATE)
    // =========================
    @PostMapping("/staff_shifts/save")
    public String saveStaffShift(
            @Valid @ModelAttribute StaffShiftDTO staffShiftDTO,
            BindingResult bindingResult,
            @RequestParam(required = false) Integer weekOffset,
            @RequestParam(required = false) Long filterStaffId,
            @RequestParam(required = false) Long filterShiftId,
            RedirectAttributes ra
    ) {
        // Build redirect with preserved filters
        StringBuilder redirectUrl = new StringBuilder("redirect:/manager/staff_shifts");
        boolean hasParam = false;
        if (weekOffset != null) { redirectUrl.append("?weekOffset=").append(weekOffset); hasParam = true; }
        if (filterStaffId != null) { redirectUrl.append(hasParam ? "&" : "?").append("staffId=").append(filterStaffId); hasParam = true; }
        if (filterShiftId != null) { redirectUrl.append(hasParam ? "&" : "?").append("shiftId=").append(filterShiftId); }

        boolean isCreate = (staffShiftDTO.getId() == null || staffShiftDTO.getId() <= 0);

        // Validation 1: Ngày trong quá khứ
        if (staffShiftDTO.getWorkDate() != null && staffShiftDTO.getWorkDate().isBefore(LocalDate.now())) {
            bindingResult.rejectValue("workDate", "error.workDate", "Ngày làm không được trước ngày hiện tại");
        }

        // Validation 2: Ca không phù hợp giờ hiện tại (nếu là hôm nay)
        if (staffShiftDTO.getWorkDate() != null && staffShiftDTO.getShiftId() != null) {
            ShiftDTO shiftDTO = shiftService.getShiftById(staffShiftDTO.getShiftId());
            if (staffShiftDTO.getWorkDate().isEqual(LocalDate.now())
                    && shiftDTO.getStartTime().toLocalTime().isBefore(LocalTime.now())) {
                bindingResult.rejectValue("shiftId", "error.shiftId", "Ca làm không phù hợp với giờ hiện tại");
            }
        }

        // Validation 3: Với EDIT, không cho sửa nếu COMPLETED/CANCELLED
        if (!isCreate && staffShiftDTO.getId() != null) {
            try {
                StaffShiftDTO existing = staffShiftService.getById(staffShiftDTO.getId());
                if (existing == null) {
                    bindingResult.rejectValue("id", "error.notFound", "Ca làm việc không tồn tại");
                } else if ("COMPLETED".equals(existing.getStatus()) || "CANCELLED".equals(existing.getStatus())) {
                    bindingResult.rejectValue("status", "error.status", "Không thể chỉnh sửa ca đã hoàn thành/đã hủy");
                }
            } catch (Exception e) {
                bindingResult.rejectValue("id", "error.notFound", "Ca làm việc không tồn tại");
            }
        }

        // Validation 4: Trùng lặp (cùng staff + date + shift)
        if (staffShiftDTO.getWorkDate() != null
                && staffShiftDTO.getStaffId() != null
                && staffShiftDTO.getShiftId() != null) {
            List<StaffShift> existingShifts = staffShiftRepository.findAllShiftsByStaffIdAndDate(
                    staffShiftDTO.getStaffId(), staffShiftDTO.getWorkDate());

            boolean duplicate = existingShifts.stream().anyMatch(ss -> {
                boolean sameShift = (ss.getShift() != null && ss.getShift().getId() == staffShiftDTO.getShiftId());
                boolean differentId = (isCreate || (ss.getId() != staffShiftDTO.getId()));
                return sameShift && differentId;
            });

            if (duplicate) {
                bindingResult.rejectValue("shiftId", "error.duplicate",
                        "Nhân viên này đã có ca làm việc này trong ngày đã chọn");
            }
        }

        // Status luôn là SCHEDULED khi create
        if (isCreate) {
            staffShiftDTO.setStatus("SCHEDULED");
        }

        if (bindingResult.hasErrors()) {
            ra.addFlashAttribute("error", "Vui lòng kiểm tra lại dữ liệu nhập.");
            return redirectUrl.toString();
        }

        if (isCreate) {
            staffShiftService.create(staffShiftDTO);
            ra.addFlashAttribute("message", "Thêm ca làm việc thành công");
        } else {
            staffShiftService.update(staffShiftDTO, staffShiftDTO.getId());
            ra.addFlashAttribute("message", "Cập nhật ca làm việc thành công");
        }

        return redirectUrl.toString();
    }
}
