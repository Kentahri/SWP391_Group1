package com.group1.swp.pizzario_swp391.controller.manager;

import java.io.IOException;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.group1.swp.pizzario_swp391.annotation.ManagerUrl;
import com.group1.swp.pizzario_swp391.config.Setting;
import com.group1.swp.pizzario_swp391.service.YamlService;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@ManagerUrl
@FieldDefaults(makeFinal = true, level = lombok.AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class SettingController {

    YamlService yamlService;
    Setting setting;

    @GetMapping("/settings")
    public String showSettings(Model model) {
        model.addAttribute("setting", setting);
        return "admin_page/settings";
    }

    @PostMapping("/settings")
    public String updateSettings(@ModelAttribute("setting") Setting settingForm, 
                                RedirectAttributes redirectAttributes) {
        try {
            setting.setConflictReservationMinutes(settingForm.getConflictReservationMinutes());
            setting.setAutoLockReservationMinutes(settingForm.getAutoLockReservationMinutes());
            setting.setNoShowWaitMinutes(settingForm.getNoShowWaitMinutes());
            setting.setNoAbsentCheckMinutes(settingForm.getNoAbsentCheckMinutes());
            setting.setNoCheckoutCheckMinutes(settingForm.getNoCheckoutCheckMinutes());
            setting.setReLoginTimeoutMinutes(settingForm.getReLoginTimeoutMinutes());
            setting.setReLoginServerCollapse(settingForm.getReLoginServerCollapse());
            
            yamlService.persit();
            
            redirectAttributes.addFlashAttribute("successMessage", "Cài đặt đã được cập nhật thành công!");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi lưu cài đặt: " + e.getMessage());
        }
        
        return "redirect:/manager/settings";
    }
}
