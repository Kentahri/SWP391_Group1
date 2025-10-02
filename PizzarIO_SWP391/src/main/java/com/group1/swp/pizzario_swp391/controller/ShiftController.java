package com.group1.swp.pizzario_swp391.controller;

import com.group1.swp.pizzario_swp391.annotation.AdminController;
import com.group1.swp.pizzario_swp391.dto.ShiftDTO;
import com.group1.swp.pizzario_swp391.entity.Shift;
import com.group1.swp.pizzario_swp391.service.ShiftService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;

@AdminController
@Controller
@RequiredArgsConstructor
public class ShiftController {

    private final ShiftService service;

    @GetMapping("/shifts")
    public String list(Model model) {

        model.addAttribute("shifts", service.getAllShift());


        return "admin_page/shift/shift-list";
    }

    @GetMapping("/shift/create")
    public String formCreate(Model model){

        model.addAttribute("shift", new ShiftDTO());

        List<String> labels = Arrays.stream(Shift.ShiftType.values())
                .map(Enum::name)
                .toList();

        model.addAttribute("label", labels);

        return "admin_page/shift/create";
    }

    @PostMapping("/shift/create")
    public String save(@ModelAttribute("shift") ShiftDTO shift, RedirectAttributes redirectAttributes) {

        service.createShift(shift);

        redirectAttributes.addFlashAttribute("message", "Tạo thành công");

        return "redirect:/admin/shifts";

    }

    @GetMapping("/shift/edit/{id}")
    public String update(@PathVariable Integer id, Model model) {

        ShiftDTO shift = service.getShiftById(id);

        List<String> labels = Arrays.stream(Shift.ShiftType.values())
                .map(Enum::name)
                .toList();

        model.addAttribute("label", labels);

        model.addAttribute("shift", shift);
        System.out.println(shift);

        return "admin_page/shift/create";
    }

    @PostMapping("/shift/edit/{id}")
    public String updateShift(@ModelAttribute("shift") ShiftDTO shiftDTO,@PathVariable int id, RedirectAttributes redirectAttributes){

        service.updateShift(id, shiftDTO);

        redirectAttributes.addFlashAttribute("message", "Update thành công");

        return "redirect:/admin/shifts";
    }
    // Xóa
    @GetMapping("/shift/delete/{id}")
    public String delete(@PathVariable Integer id, RedirectAttributes redirectAttributes) {

        service.deleteShift(id);

        redirectAttributes.addFlashAttribute("message", "Delete thành công");
        return "redirect:/admin/shifts";
    }

}
