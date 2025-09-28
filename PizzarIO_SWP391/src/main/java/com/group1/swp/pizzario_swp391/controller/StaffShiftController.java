package com.group1.swp.pizzario_swp391.controller;

import com.group1.swp.pizzario_swp391.entity.Shift;
import com.group1.swp.pizzario_swp391.entity.Staff;
import com.group1.swp.pizzario_swp391.entity.StaffShift;
import com.group1.swp.pizzario_swp391.service.ShiftService;
import com.group1.swp.pizzario_swp391.service.StaffService;
import com.group1.swp.pizzario_swp391.service.StaffShiftService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Controller
@RequestMapping("/admin")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class StaffShiftController {

    StaffShiftService staffShiftService;

    StaffService staffService;
    ShiftService shiftService;

    @GetMapping("/staff_shifts")
    public String listStaffShifts(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Integer shiftId,
            @RequestParam(required = false) Integer staffId,
            Model model) {
        List<StaffShift> rows = staffShiftService.search(from, to, shiftId, staffId);
        List<Staff> staffs = staffService.getAllStaff();
        List<Shift> shift = shiftService.getAllShift();
        model.addAttribute("shifts", shift);
        model.addAttribute("rows", rows);
        model.addAttribute("staffs", staffs);

        return "admin_page/shift/shift_management";
    }

    @GetMapping("/staff_shifts/create")
    public String getFormStaffShift(
            @RequestParam(required = false) Integer editId,
            Model model) {
        List<Staff> staffs = staffService.getAllStaff();
        List<Shift> shift = shiftService.getAllShift();
        model.addAttribute("shifts", shift);
        model.addAttribute("staffs", staffs);

        if (editId != null) {
            // Chế độ edit - load dữ liệu cũ
            StaffShift staffShift = staffShiftService.getById(editId);
            model.addAttribute("staffShift", staffShift);
            model.addAttribute("editId", editId);
            model.addAttribute("isEdit", true);
        } else {
            // Chế độ tạo mới
            model.addAttribute("staffShift", new StaffShift());
            model.addAttribute("isEdit", false);
        }

        return "admin_page/shift/create";
    }

    @PostMapping("/staff_shifts/create")
    public String createStaffShift(
            @RequestParam(required = false) Integer editId,
            @RequestParam Integer staffId,
            @RequestParam Integer shiftId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate workDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime,
            @RequestParam(required = false) Integer hourlyWage,
            @RequestParam(defaultValue = "ASSIGNED") StaffShift.Status status,
            RedirectAttributes ra) {
        Staff staff = staffService.getStaffById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff not found"));
        Shift shift = shiftService.getShiftById(shiftId); // nhớ xử lý null nếu có

        StaffShift ss;
        if (editId != null) {
            ss = staffShiftService.getById(editId);
        } else {
            ss = new StaffShift();
        }

        ss.setStaff(staff);
        ss.setShift(shift);
        ss.setWorkDate(workDate.atStartOfDay()); // nếu field entity là LocalDateTime; hoặc đổi entity sang LocalDate
        ss.setHourlyWage(hourlyWage);
        ss.setStatus(status);

        ss.setCheckIn(shift.getStartTime());
        ss.setCheckOut(shift.getEndTime());

        if (editId != null) {
            staffShiftService.update(ss);
            ra.addFlashAttribute("msg", "Cập nhật phân công thành công");
        } else {
            staffShiftService.create(ss);
            ra.addFlashAttribute("msg", "Tạo phân công thành công");
        }

        return "redirect:/admin/staff_shifts"; // giữ prefix /admin khi redirect
    }

    @PostMapping("/staff_shifts/delete/{id}")
    public String deleteStaffShift(@PathVariable int id, RedirectAttributes ra) {
        staffShiftService.delete(id);
        ra.addFlashAttribute("msg", "Xóa phân công thành công");
        return "redirect:/admin/staff_shifts";
    }

    @GetMapping("/staff_shifts/edit/{id}")
    public String updateStaffShift(@PathVariable int id, Model model) {
        return "redirect:/admin/staff_shifts/create?editId=" + id;
    }
}
