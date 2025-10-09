package com.group1.swp.pizzario_swp391.controller;

import com.group1.swp.pizzario_swp391.annotation.ManagerUrl;
import com.group1.swp.pizzario_swp391.dto.data_analytics.AnalyticsDTO;
import com.group1.swp.pizzario_swp391.dto.data_analytics.SalesDTO;
import com.group1.swp.pizzario_swp391.service.DataAnalyticsReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@ManagerUrl
@RequiredArgsConstructor
public class DashBoardController {

    private final DataAnalyticsReportService dataAnalyticsReportService;

    @GetMapping
    public String getFormAnalytics(Model model) {

        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(28);

        // Bar Chart - 7 ngày gần nhất
        SalesDTO barChartData = dataAnalyticsReportService.getSalesInRange(LocalDate.now().minusDays(7),
                LocalDate.now());
        model.addAttribute("labels_bar", barChartData.label());
        model.addAttribute("data_bar", barChartData.data());

        // Line Chart - 28 ngày
        SalesDTO lineChartData = dataAnalyticsReportService.getSalesInRange(start, end);
        model.addAttribute("labels_Line", lineChartData.label());
        model.addAttribute("data_Line", lineChartData.data());

        // Analytics Data - 28 ngày
        AnalyticsDTO analytics = dataAnalyticsReportService.getAnalyticsData(start, end);
        model.addAttribute("analytics", analytics);

        return "admin_page/analytics";
    }

    @PostMapping("/analytics")
    public String analytics(@RequestParam(required = false) Integer days,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {
        LocalDate start;
        LocalDate end;
        Integer selectedDays = days;

        if (startDate != null && endDate != null) {
            start = startDate;
            end = endDate;
            selectedDays = null; // custom range
        } else if (days != null) {
            end = LocalDate.now();
            start = end.minusDays(days);
        } else {
            end = LocalDate.now();
            start = end.minusDays(28);
            selectedDays = 28; // default
        }

        SalesDTO salesData = dataAnalyticsReportService.getSalesInRange(start, end);
        model.addAttribute("labels_Line", salesData.label());
        model.addAttribute("data_Line", salesData.data());

        AnalyticsDTO analytics = dataAnalyticsReportService.getAnalyticsData(start, end);
        model.addAttribute("analytics", analytics);

        // Bar Chart - 7 ngày gần nhất
        SalesDTO barChartData = dataAnalyticsReportService.getSalesInRange(LocalDate.now().minusDays(7),
                LocalDate.now());
        model.addAttribute("labels_bar", barChartData.label());
        model.addAttribute("data_bar", barChartData.data());

        // Thông tin khoảng thời gian (hiển thị trên UI)
        model.addAttribute("dateRangeLabel", formatDateRange(start, end, days));
        model.addAttribute("selectedDays", selectedDays); // Để giữ state của dropdown
        model.addAttribute("isCustomRange", startDate != null && endDate != null);

        return "admin_page/analytics";
    }

    private String formatDateRange(LocalDate start, LocalDate end, Integer days) {
        if (days != null) {
            return days + " ngày qua";
        }
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return start.format(fmt) + " - " + end.format(fmt);
    }

}
