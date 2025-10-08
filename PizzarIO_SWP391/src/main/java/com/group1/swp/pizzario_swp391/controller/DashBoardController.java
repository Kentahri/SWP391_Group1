package com.group1.swp.pizzario_swp391.controller;

import com.group1.swp.pizzario_swp391.annotation.ManagerUrl;
import com.group1.swp.pizzario_swp391.dto.report.WeeklySalesDTO;
import com.group1.swp.pizzario_swp391.service.DataAnalyticsReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@ManagerUrl
@RequiredArgsConstructor
public class DashBoardController {

    private final DataAnalyticsReportService dataAnalyticsReportService;

    @GetMapping
    public String getFormAnalytics(Model model) {

        WeeklySalesDTO dto = dataAnalyticsReportService.getWeeklySales();

        model.addAttribute("labels", dto.label());
        model.addAttribute("data", dto.data());

        return "admin_page/analytics";
    }

}
