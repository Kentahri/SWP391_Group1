package com.group1.swp.pizzario_swp391.controller;

import com.group1.swp.pizzario_swp391.annotation.ManagerUrl;
import com.group1.swp.pizzario_swp391.dto.ShiftDTO;
import com.group1.swp.pizzario_swp391.dto.data_analytics.StatsStaffShiftDTO;
import com.group1.swp.pizzario_swp391.dto.data_analytics.WeekDayDTO;
import com.group1.swp.pizzario_swp391.dto.staff.StaffResponseDTO;
import com.group1.swp.pizzario_swp391.dto.staffshift.ManualCompleteShiftRequest;
import com.group1.swp.pizzario_swp391.dto.staffshift.StaffShiftDTO;
import com.group1.swp.pizzario_swp391.dto.staffshift.StaffShiftResponseDTO;
import com.group1.swp.pizzario_swp391.entity.Shift;
import com.group1.swp.pizzario_swp391.entity.Staff;
import com.group1.swp.pizzario_swp391.entity.StaffShift;
import com.group1.swp.pizzario_swp391.service.*;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.web.csrf.CsrfToken;

import java.time.LocalDate;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@Controller
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@ManagerUrl
@RequiredArgsConstructor
public class StaffShiftController {

    StaffShiftService staffShiftService;

    StaffService staffService;
    ShiftService shiftService;
    // removed unused mapperResponse

    StaffShiftManagementService staffShiftManagementService;
    private final StaffShiftExcelExportService staffShiftExcelExportService;

    @GetMapping("/staff_shifts")
    public String listStaffShifts(
            @RequestParam(required = false) Integer weekOffset,
            @RequestParam(required = false) Long staffId,
            @RequestParam(required = false) Long shiftId,
            Model model,
            CsrfToken token) {

        // Add CSRF token to model
        model.addAttribute("_csrf", token);

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

        List<Integer> years = Arrays.asList(2020, 2021, 2022, 2023, 2024, 2025, 2026);
        List<Integer> months = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12);
        model.addAttribute("exportYears", years);
        model.addAttribute("exportMonths", months);

        if (!model.containsAttribute("staffShiftForm")) {
            StaffShiftDTO newForm = new StaffShiftDTO();
            newForm.setStatus("SCHEDULED"); // Mặc định là SCHEDULED
            model.addAttribute("staffShiftForm", newForm);
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
        // Create empty form for create mode với status mặc định
        StaffShiftDTO newForm = new StaffShiftDTO();
        newForm.setStatus("SCHEDULED");
        redirectAttributes.addFlashAttribute("staffShiftForm", newForm);
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

        // Debug logging
        System.out.println("=== StaffShift Save Debug ===");
        System.out.println("StaffShiftDTO: " + staffShiftDTO);
        System.out.println("BindingResult has errors: " + bindingResult.hasErrors());
        if (bindingResult.hasErrors()) {
            bindingResult.getAllErrors()
                    .forEach(error -> System.out.println("Validation error: " + error.getDefaultMessage()));
        }

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

        // Custom validation for both CREATE and EDIT modes
        boolean isCreateMode = (staffShiftDTO.getId() == null || staffShiftDTO.getId() <= 0);

        // Validation 1: Prevent past dates (for both CREATE and EDIT)
        if (staffShiftDTO.getWorkDate() != null && staffShiftDTO.getWorkDate().isBefore(LocalDate.now())) {
            bindingResult.rejectValue("workDate", "error.workDate", "Ngày làm không được trước ngày hiện tại");
        }

        // Validation 2: Check if shift time is appropriate for current time (for both
        // CREATE and EDIT)
        if (staffShiftDTO.getWorkDate() != null && staffShiftDTO.getShiftId() != null) {
            ShiftDTO shiftDTO = shiftService.getShiftById(staffShiftDTO.getShiftId());
            if (staffShiftDTO.getWorkDate().isEqual(LocalDate.now())
                    && shiftDTO.getStartTime().toLocalTime().isBefore(LocalTime.now())) {
                bindingResult.rejectValue("shiftId", "error.shiftId", "Ca làm không phù hợp với giờ hiện tại");
            }
        }

        // Validation 3: For EDIT mode, check if the staff shift exists and belongs to
        // the current user's scope
        if (!isCreateMode && staffShiftDTO.getId() != null) {
            try {
                StaffShiftDTO existingShift = staffShiftService.getById(staffShiftDTO.getId());
                if (existingShift == null) {
                    bindingResult.rejectValue("id", "error.notFound", "Ca làm việc không tồn tại");
                } else {
                    // Additional validation for EDIT: Check if the shift is in a state that allows
                    // editing
                    if ("COMPLETED".equals(existingShift.getStatus())
                            || "CANCELLED".equals(existingShift.getStatus())) {
                        bindingResult.rejectValue("status", "error.status",
                                "Không thể chỉnh sửa ca làm việc đã hoàn thành hoặc đã hủy");
                    }
                }
            } catch (Exception e) {
                bindingResult.rejectValue("id", "error.notFound", "Ca làm việc không tồn tại");
            }
        }

        // Validation 4: Check duplicate - Same staff + same date + same shift
        if (staffShiftDTO.getWorkDate() != null && staffShiftDTO.getStaffId() != null
                && staffShiftDTO.getShiftId() != null) {
            List<StaffShift> existingShifts = staffShiftService.findAllShiftsByStaffIdAndDate(
                    staffShiftDTO.getStaffId(),
                    staffShiftDTO.getWorkDate());

            boolean hasDuplicate;
            if (isCreateMode) {
                // For CREATE: Check if any shift with same staff, date, and shift type exists
                hasDuplicate = existingShifts.stream()
                        .anyMatch(ss -> ss.getShift().getId() == staffShiftDTO.getShiftId());
            } else {
                // For EDIT: Check if any OTHER shift (not the current one being edited) with
                // same staff, date, and shift type exists
                hasDuplicate = existingShifts.stream()
                        .anyMatch(ss -> ss.getShift().getId() == staffShiftDTO.getShiftId()
                                && ss.getId() != staffShiftDTO.getId());
            }

            if (hasDuplicate) {
                bindingResult.rejectValue("shiftId", "error.duplicate",
                        "Nhân viên này đã có ca làm việc này trong ngày đã chọn");
            }
        }

        // Đảm bảo status luôn là SCHEDULED cho create mode
        if (isCreateMode) {
            staffShiftDTO.setStatus("SCHEDULED");
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
            staffShiftManagementService.createStaffShiftFromDTO(staffShiftDTO);
            ra.addFlashAttribute("message", "Thêm ca làm việc thành công");
        }

        return redirectUrl.toString();
    }

    @GetMapping("/staff_shifts/api/staff_search")
    @ResponseBody
    public List<Map<String, Object>> searchStaff(
            @RequestParam(required = false, defaultValue = "") String q,
            @RequestParam(required = false, defaultValue = "20") int limit) {
        String term = q == null ? "" : q.trim();
        int top = Math.max(1, Math.min(limit, 50)); // giới hạn 1..50
        List<Staff> list = staffService.searchByName(term, top);
        List<Map<String,Object>> result = new ArrayList<>();
        for (Staff s : list) {
            Map<String,Object> m = new HashMap<>();
            m.put("id", s.getId());
            m.put("name", s.getName());
            result.add(m);
        }
        return result;
    }

    @PostMapping("/staff_shifts/export")
    public ResponseEntity<byte[]> exportMonthlyExcel(
            @RequestParam int year,
            @RequestParam int month) {
        try {
            byte[] excelData = staffShiftExcelExportService.generateMonthlyReport(year, month);

            String fileName = String.format("Luong_Thang_%02d_%d.xlsx", month, year);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", fileName);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelData);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/staff_shifts/manual-complete/{id}")
    public String manualCompleteShift(
            @PathVariable int id,
            @Valid @ModelAttribute ManualCompleteShiftRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error",
                    "Invalid input: " + bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/manager/staff_shifts";
        }

        try {
            staffShiftService.manuallyCompleteShift(id, request);
            redirectAttributes.addFlashAttribute("success",
                    "Shift completed successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error",
                    "Failed to complete shift: " + e.getMessage());
        }

        return "redirect:/manager/staff_shifts";
    }

}
