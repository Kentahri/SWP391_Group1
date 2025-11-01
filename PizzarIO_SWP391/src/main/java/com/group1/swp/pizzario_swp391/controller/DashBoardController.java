package com.group1.swp.pizzario_swp391.controller;

import com.group1.swp.pizzario_swp391.annotation.ManagerUrl;
import com.group1.swp.pizzario_swp391.dto.category.CategoryResponseDTO;
import com.group1.swp.pizzario_swp391.dto.data_analytics.AnalyticsDTO;
import com.group1.swp.pizzario_swp391.dto.data_analytics.ProductStatsDTO;
import com.group1.swp.pizzario_swp391.dto.data_analytics.SalesDTO;
import com.group1.swp.pizzario_swp391.service.CategoryService;
import com.group1.swp.pizzario_swp391.service.DataAnalyticsReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.beans.PropertyEditorSupport;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;

@ManagerUrl
@RequiredArgsConstructor
public class DashBoardController {

    private final DataAnalyticsReportService dataAnalyticsReportService;
    private final CategoryService categoryService;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(LocalDate.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) throws IllegalArgumentException {
                try {
                    setValue(LocalDate.parse(text, DateTimeFormatter.ISO_LOCAL_DATE));
                } catch (DateTimeParseException e) {
                    setValue(null);
                }
            }
        });
    }

    @GetMapping("/analytics")
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

        // Top sản phẩm bán chạy nhất (tất cả thời gian)
        List<ProductStatsDTO> topProducts = dataAnalyticsReportService.getTopBestSellingProducts();
        model.addAttribute("topProducts", topProducts);

        // Mặc định 28 ngày
        model.addAttribute("selectedDays", 28);

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
            // Tính số ngày giữa startDate và endDate
            selectedDays = (int) ChronoUnit.DAYS.between(start, end);
        } else if (days != null) {
            end = LocalDate.now();
            start = end.minusDays(days);
            selectedDays = days;
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

        // Top 5 sản phẩm bán chạy nhất (tất cả thời gian)
        List<ProductStatsDTO> topProducts = dataAnalyticsReportService.getTopBestSellingProducts();
        model.addAttribute("topProducts", topProducts);

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

    // ==================== NEW API ENDPOINTS ====================

    /**
     * API endpoint: Get top products với filters (AJAX)
     *
     * @param dateRange  Preset: "7", "30", "90", "all", "custom"
     * @param fromDate   Custom start date (optional, required if dateRange="custom")
     * @param toDate     Custom end date (optional, required if dateRange="custom")
     * @param categoryId Category filter (optional)
     * @param limit      Number of products to return (default: 5)
     * @return JSON response với success, data, message
     */
    @GetMapping("/analytics/api/top-products")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getTopProductsAPI(
            @RequestParam(defaultValue = "7") String dateRange,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "5") int limit) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Validate limit
            if (limit < 1 || limit > 50) {
                response.put("success", false);
                response.put("message", "Limit phải từ 1 đến 50");
                return ResponseEntity.badRequest().body(response);
            }

            // Call service
            List<ProductStatsDTO> products = dataAnalyticsReportService.getTopProductsWithFilters(
                    dateRange, fromDate, toDate, categoryId, limit);

            response.put("success", true);
            response.put("data", products);
            response.put("total", products.size());
            response.put("message", "Lấy dữ liệu thành công");

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            response.put("data", new ArrayList<>());
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            System.err.println("Error in getTopProductsAPI: " + e.getMessage());
            e.printStackTrace();

            response.put("success", false);
            response.put("message", "Có lỗi xảy ra khi xử lý yêu cầu");
            response.put("data", new ArrayList<>());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * API endpoint: Get all categories (AJAX)
     *
     * @return JSON array of categories
     */
    @GetMapping("/analytics/api/categories")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getCategoriesAPI() {
        try {
            List<CategoryResponseDTO> categories = categoryService.getAllCategories();

            List<Map<String, Object>> result = new ArrayList<>();
            for (CategoryResponseDTO cat : categories) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", cat.getId());
                item.put("name", cat.getName());
                result.add(item);
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            System.err.println("Error in getCategoriesAPI: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(new ArrayList<>());
        }
    }

}
