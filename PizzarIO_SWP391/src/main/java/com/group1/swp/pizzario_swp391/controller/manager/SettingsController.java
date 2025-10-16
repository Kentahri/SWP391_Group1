package com.group1.swp.pizzario_swp391.controller.manager;

import com.group1.swp.pizzario_swp391.annotation.ManagerUrl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@ManagerUrl
@RequiredArgsConstructor
public class SettingsController {

    @GetMapping("/settings")
    public String settings(Model model) {
        return "admin_page/settings";
    }
}
