package com.group1.swp.pizzario_swp391.controller;

import com.group1.swp.pizzario_swp391.dto.ShiftDTO;
import com.group1.swp.pizzario_swp391.entity.Shift;
import com.group1.swp.pizzario_swp391.service.ShiftService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/shift")
@RequiredArgsConstructor
public class ShiftController {

    private final ShiftService service;

    // Trang duy nhất: hiện danh sách + form (thêm/sửa)
    @GetMapping
    public String list(Model model,
            @RequestParam(value = "editId", required = false) Integer editId) {

        model.addAttribute("shifts", service.getAllShift());

        // Nếu có editId thì nạp form = shift cần sửa, ngược lại form trống
        if (editId != null) {
            Shift shift = service.getShiftById(editId);
            // Convert Shift entity to ShiftDTO for form
            ShiftDTO form = ShiftDTO.builder()
                    .shiftName(shift.getShiftName())
                    .startTime(shift.getStartTime())
                    .endTime(shift.getEndTime())
                    .build();
            form.setId(shift.getId()); // Set ID for update
            model.addAttribute("form", form);
        } else {
            model.addAttribute("form", new ShiftDTO());
        }

        return "admin_page/shift/shift-list";
    }

    // Lưu (thêm hoặc cập nhật) — dùng chung 1 endpoint POST
    @PostMapping
    public String save(@ModelAttribute("form") ShiftDTO form, RedirectAttributes redirectAttributes) {
        try {
            // Validation
            if (form.getShiftName() == null || form.getShiftName().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Tên ca không được để trống!");
                return "redirect:/shift";
            }

            if (form.getStartTime() == null || form.getEndTime() == null) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Thời gian bắt đầu và kết thúc không được để trống!");
                return "redirect:/shift";
            }

            if (form.getStartTime().isAfter(form.getEndTime())) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Thời gian bắt đầu phải trước thời gian kết thúc!");
                return "redirect:/shift";
            }

            // Nếu form.id == null => thêm mới; có id => cập nhật
            if (form.getId() == null || form.getId() == 0) {
                service.createShift(form);
                redirectAttributes.addFlashAttribute("successMessage", "Ca làm việc đã được thêm thành công!");
            } else {
                service.updateShift(form.getId(), form);
                redirectAttributes.addFlashAttribute("successMessage", "Ca làm việc đã được cập nhật thành công!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
        }
        return "redirect:/shift";
    }

    // Xóa
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            service.deleteShift(id);
            redirectAttributes.addFlashAttribute("successMessage", "Ca làm việc đã được xóa thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra khi xóa: " + e.getMessage());
        }
        return "redirect:/shift";
    }

}
