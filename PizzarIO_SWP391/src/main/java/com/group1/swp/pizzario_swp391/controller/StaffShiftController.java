package com.group1.swp.pizzario_swp391.controller;

import com.group1.swp.pizzario_swp391.dto.ShiftDTO;
import com.group1.swp.pizzario_swp391.dto.staff.StaffResponseDTO;
import com.group1.swp.pizzario_swp391.entity.Shift;
import com.group1.swp.pizzario_swp391.entity.Staff;
import com.group1.swp.pizzario_swp391.entity.StaffShift;
import com.group1.swp.pizzario_swp391.mapper.StaffResponseMapper;
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
import java.time.LocalDateTime;
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
    StaffResponseMapper mapperResponse;

    @GetMapping("/staff_shifts")
    public String listStaffShifts(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Integer shiftId,
            @RequestParam(required = false) Integer staffId,
            Model model) {
        List<StaffShift> rows = staffShiftService.search(from, to, shiftId, staffId);
        model.addAttribute("rows", rows);

        return "admin_page/shift/shift_management";
    }

    @GetMapping("/staff_shifts/create")
    public String getFormStaffShift(
            @RequestParam(required = false) Integer editId,
            Model model) {

        List<StaffResponseDTO> staffs = staffService.getAllStaff();

        List<ShiftDTO> shift = shiftService.getAllShift();
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
    public String createStaffShift() {


        return "redirect:/admin/staff_shifts";
    }

    @PostMapping("/staff_shifts/delete/{id}")
    public String deleteStaffShift(@PathVariable int id, RedirectAttributes ra) {
        staffShiftService.delete(id);
        ra.addFlashAttribute("msg", "Xóa phân công thành công");
        return "redirect:/admin/staff_shifts";
    }

    @GetMapping("/staff_shifts/edit/{id}")
    public String showEditForm(@PathVariable int id, Model model) {
        StaffShift staffShift = staffShiftService.getById(id);
        model.addAttribute("staffs", staffService.getAllStaff());
        model.addAttribute("shifts", shiftService.getAllShift());
        model.addAttribute("staffShift", staffShift);
        return "admin_page/shift/edit";
    }

//    @PostMapping("/staff_shifts/edit/{id}")
//    public String updateStaffShift(
//            @PathVariable int id,
//            @RequestParam Integer staffId,
//            @RequestParam Integer shiftId,
//            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate workDate,
//            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
//            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime,
//            @RequestParam(required = false) Integer hourlyWage,
//            @RequestParam(defaultValue = "ASSIGNED") StaffShift.Status status,
//            RedirectAttributes ra) {
//
//        StaffShift ss = staffShiftService.getById(id);
//        StaffResponseDTO staffDTO = staffService.getStaffById(staffId);
//
//        Staff staff = mapperResponse.toStaff(staffDTO);
//
//        ShiftDTO shift = shiftService.getShiftById(shiftId);
//        if (shift == null) {
//            throw new IllegalArgumentException("Shift not found");
//        }
//
//        // Ghép ngày làm với giờ ca (tránh lệch ngày nếu startTime/endTime của Shift đang là LocalDateTime)
//        LocalDateTime checkIn  = LocalDateTime.of(workDate, shift.getStartTime().toLocalTime());
//        LocalDateTime checkOut = LocalDateTime.of(workDate, shift.getEndTime().toLocalTime());
//
//        ss.setStaff(staff);
//        ss.setShift(shift);
//        ss.setWorkDate(workDate.atStartOfDay());
//        ss.setCheckIn(checkIn);
//        ss.setCheckOut(checkOut);
//        ss.setHourlyWage(hourlyWage != null ? hourlyWage : ss.getHourlyWage());
//        ss.setStatus(status);
//
//        staffShiftService.update(ss);
//        ra.addFlashAttribute("msg", "Cập nhật phân công thành công");
//        return "redirect:/admin/staff_shifts";
//    }


}
