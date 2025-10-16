package com.group1.swp.pizzario_swp391.controller;

import com.group1.swp.pizzario_swp391.annotation.ManagerUrl;
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

import java.time.LocalDate;
import java.time.DayOfWeek;
// import java.time.Duration; // Uncomment khi implement calculateStatistics()
// import java.time.LocalTime; // Uncomment khi implement convertToCalendarDTO()
// import java.time.format.DateTimeFormatter; // Uncomment khi format time
import java.util.*;
// import java.util.stream.Collectors; // Uncomment khi implement filters và buildWeekDays()

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

        // TODO: Implement calendar view logic
        // Bạn cần implement các phần sau:

        // 1. Tính toán tuần hiện tại
        int offset = weekOffset != null ? weekOffset : 0;
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.plusWeeks(offset).with(DayOfWeek.SUNDAY);
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

        // 3. TODO: Lấy staff shifts trong tuần
        // List<StaffShiftDTO> staffShifts =
        // staffShiftService.findByDateRange(weekStart, weekEnd);
        // Tạm thời dùng search với null để lấy tất cả
        var staffShifts = staffShiftService.search(null, null, null, null);

        // TODO: Apply filters nếu có
        // if (staffId != null) { ... filter by staffId ... }
        // if (shiftId != null) { ... filter by shiftId ... }

        // 4. TODO: Tính toán statistics
        // ShiftStatisticsDTO stats = calculateStatistics(staffShifts);
        // model.addAttribute("stats", stats);

        // Tạm thời dùng giá trị mặc định
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalShifts", staffShifts.size());
        stats.put("totalHours", 0);
        stats.put("totalWage", 0.0);
        stats.put("completedShifts", 0);
        model.addAttribute("stats", stats);

        // 5. TODO: Tạo weekDays với shift cards
        // List<WeekDayDTO> weekDays = buildWeekDays(weekStart, weekEnd, staffShifts);
        // model.addAttribute("weekDays", weekDays);

        // Tạm thời tạo 7 ngày trống
        List<Map<String, Object>> weekDays = new ArrayList<>();
        String[] dayNames = { "CN", "T2", "T3", "T4", "T5", "T6", "T7" };
        for (int i = 0; i < 7; i++) {
            Map<String, Object> day = new HashMap<>();
            day.put("date", weekStart.plusDays(i));
            day.put("dayName", dayNames[i]);
            day.put("isToday", weekStart.plusDays(i).equals(LocalDate.now()));
            day.put("shifts", new ArrayList<>()); // TODO: Add actual shifts
            weekDays.add(day);
        }
        model.addAttribute("weekDays", weekDays);

        return "admin_page/staff_shift/shift_calendar";
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
