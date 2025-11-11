package com.group1.swp.pizzario_swp391.service;

import com.group1.swp.pizzario_swp391.dto.data_analytics.AnalyticsDTO;
import com.group1.swp.pizzario_swp391.dto.data_analytics.ProductStatsDTO;
import com.group1.swp.pizzario_swp391.dto.data_analytics.SalesDTO;
import com.group1.swp.pizzario_swp391.entity.Membership;
import com.group1.swp.pizzario_swp391.entity.Order;
import com.group1.swp.pizzario_swp391.repository.OrderRepository;
import com.group1.swp.pizzario_swp391.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class DataAnalyticsReportService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public SalesDTO getSalesInRange(LocalDate start, LocalDate end) {

        long days = ChronoUnit.DAYS.between(start, end);

        List<Order> orders = orderRepository.findInRangeAndPaid(start.atStartOfDay(), end.atStartOfDay());

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("d MMM", new Locale("vi", "VN"));

        List<String> labels = new ArrayList<>(Integer.valueOf((int) days));
        for (int i = 0; i < days; i++) {
            labels.add(start.plusDays(i).format(fmt));
        }

        Map<LocalDate, Double> revenueByDate = new HashMap<>();
        for (Order o : orders) {
            LocalDate day = o.getCreatedAt().toLocalDate();
            revenueByDate.merge(day, o.getTotalPrice(), Double::sum);
        }

        double[] data = new double[Integer.valueOf((int) days)];
        for (int i = 0; i < days; i++) {
            LocalDate day = start.plusDays(i);
            data[i] = revenueByDate.getOrDefault(day, 0.0);
        }

        return new SalesDTO(labels, data);
    }

    private Long calculateTotalRevenue(List<Order> orders) {

        Long sum = 0L;

        for (Order order : orders) {
            sum += Long.valueOf((long) order.getTotalPrice());
        }
        return sum;
    }

    // Tính % thay đổi
    private Double calculateDelta(Double current, Double previous) {
        if (previous == 0)
            return 0.0;
        return ((current - previous) * 100.0) / previous;
    }

    public List<ProductStatsDTO> getTopBestSellingProducts() {
        Pageable topFive = PageRequest.of(0, 5);
        List<ProductStatsDTO> products = orderRepository.findTopBestSellingProducts(topFive);

        List<ProductStatsDTO> result = new ArrayList<>();
        for (int i = 0; i < products.size(); i++) {
            ProductStatsDTO product = products.get(i);

            ProductStatsDTO newProduct = new ProductStatsDTO(
                    (long) (i + 1), // topId: thứ hạng
                    product.productName(),
                    product.orderCount(),
                    product.quantitySold(),
                    product.totalRevenue());
            result.add(newProduct);
        }

        return result;
    }

    public AnalyticsDTO getAnalyticsData(LocalDate startDate, LocalDate endDate) {
        long dayBetween = ChronoUnit.DAYS.between(startDate, endDate);
        // 16 -> 23
        // 9 -> 16
        LocalDate prevStartDate = startDate.minusDays(dayBetween);
        LocalDate prevEndDate = startDate;

        // Query dữ liệu KỲ HIỆN TẠI
        List<Order> currentOrders = orderRepository.findInRangeAndPaid(
                startDate.atStartOfDay(),
                endDate.atStartOfDay());

        // Query dữ liệu KỲ TRƯỚC
        List<Order> previousOrders = orderRepository.findInRangeAndPaid(
                prevStartDate.atStartOfDay(),
                prevEndDate.atStartOfDay());

        Long totalRevenue = calculateTotalRevenue(currentOrders);
        Long prevRevenue = calculateTotalRevenue(previousOrders);
        Double revenueDelta = calculateDelta(totalRevenue.doubleValue(), prevRevenue.doubleValue());

        // ĐƠN HÀNG
        Long totalOrders = (long) currentOrders.size();
        Long prevOrders = (long) previousOrders.size();
        Double ordersDelta = calculateDelta(totalOrders.doubleValue(), prevOrders.doubleValue());

        // KH MỚI
        Long newCustomers = countNewCustomers(currentOrders, startDate, endDate);
        Long prevNewCustomers = countNewCustomers(previousOrders, prevStartDate, prevEndDate);
        Double newCustomersDelta = calculateDelta(newCustomers.doubleValue(), prevNewCustomers.doubleValue());

        Double aov = totalOrders > 0 ? (double) totalRevenue / totalOrders : 0.0;
        Double prevAov = prevOrders > 0 ? (double) prevRevenue / prevOrders : 0.0;
        Double aovDelta = calculateDelta(aov, prevAov);

        // Tính tổng số khách hàng trong kỳ hiện tại
        Set<Long> currentCustomerIds = currentOrders.stream()
                .map(Order::getMembership)
                .filter(Objects::nonNull)
                .map(Membership::getId)
                .collect(Collectors.toSet());

        Long totalCustomers = (long) currentCustomerIds.size();
        Long oldCustomers = totalCustomers - newCustomers;

        // Tính retention rate chính xác
        Double retentionRate = totalCustomers > 0 ? (oldCustomers * 100.0) / totalCustomers : 0.0;

        // ========== THÊM: LẤY SỐ LIỆU HÔM NAY ==========
        Long todayOrders = orderRepository.countTodayOrders();
        Double todayRevenueDouble = orderRepository.calculateTodayRevenue();
        Long todayRevenue = todayRevenueDouble != null ? todayRevenueDouble.longValue() : 0L;

        // Đảm bảo không null
        if (todayOrders == null) todayOrders = 0L;
        // ===============================================

        return new AnalyticsDTO(
                totalRevenue, revenueDelta,
                totalOrders, ordersDelta,
                newCustomers, newCustomersDelta,
                aov, aovDelta,
                todayOrders, todayRevenue, // ← TRUYỀN VÀO CONSTRUCTOR MỚI
                oldCustomers, retentionRate);
    }

    private Long countNewCustomers(List<Order> orders, LocalDate startDate, LocalDate endDate) {
        Set<Long> customerIds = orders.stream()
                .map(Order::getMembership)
                .filter(Objects::nonNull) // bỏ null nếu có
                .map(Membership::getId)
                .collect(Collectors.toSet());

        long count = 0;
        for (Long membershipId : customerIds) {
            // Lấy đơn hàng đầu tiên của khách hàng này
            Order firstOrder = orderRepository.findFirstByMembership_IdOrderByCreatedAtAsc(membershipId);

            if (firstOrder != null) {
                LocalDate firstOrderDate = firstOrder.getCreatedAt().toLocalDate();
                // Kiểm tra xem đơn hàng đầu tiên có nằm trong khoảng [startDate, endDate) không
                // Khách hàng được coi là "mới" nếu đơn hàng đầu tiên của họ nằm trong khoảng thời gian này
                if (!firstOrderDate.isBefore(startDate) && firstOrderDate.isBefore(endDate)) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Get top products với filter theo date range và category
     *
     * @param dateRange Preset: "7", "30", "90", "all", "custom"
     * @param fromDate  Custom start date (chỉ dùng khi dateRange="custom")
     * @param toDate    Custom end date (chỉ dùng khi dateRange="custom")
     * @param categoryId Category filter (nullable)
     * @param limit     Số lượng products trả về
     * @return List of ProductStatsDTO
     */
    public List<ProductStatsDTO> getTopProductsWithFilters(
            String dateRange,
            LocalDate fromDate,
            LocalDate toDate,
            Long categoryId,
            int limit) {

        // Calculate date range
        LocalDate startDate;
        LocalDate endDate;

        if ("all".equals(dateRange)) {
            // All time - lấy từ ngày đầu tiên có order
            startDate = LocalDate.of(2000, 1, 1);
            endDate = LocalDate.now();
        } else if ("custom".equals(dateRange)) {
            if (fromDate == null || toDate == null) {
                throw new IllegalArgumentException("fromDate and toDate are required for custom range");
            }
            if (fromDate.isAfter(toDate)) {
                throw new IllegalArgumentException("fromDate must be before or equal to toDate");
            }
            startDate = fromDate;
            endDate = toDate;
        } else {
            // Preset: 7, 30, 90 days
            try {
                int days = Integer.parseInt(dateRange);
                endDate = LocalDate.now();
                startDate = endDate.minusDays(days);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid dateRange value: " + dateRange);
            }
        }

        // Fetch products
        Pageable pageable = PageRequest.of(0, limit);
        List<ProductStatsDTO> products;

        try {
            if (categoryId != null && categoryId > 0) {
                // Filter by category
                products = orderRepository.findTopBestSellingProductsByDateAndCategory(
                        startDate.atStartOfDay(),
                        endDate.plusDays(1).atStartOfDay(),
                        categoryId,
                        pageable);
            } else {
                // No category filter
                products = orderRepository.findTopBestSellingProductsBetweenDates(
                        startDate.atStartOfDay(),
                        endDate.plusDays(1).atStartOfDay(),
                        pageable);
            }

            if (products == null || products.isEmpty()) {
                return new ArrayList<>();
            }

            // Add ranking (topId)
            List<ProductStatsDTO> result = new ArrayList<>();
            for (int i = 0; i < products.size(); i++) {
                ProductStatsDTO product = products.get(i);
                ProductStatsDTO ranked = new ProductStatsDTO(
                        (long) (i + 1),
                        product.productName(),
                        product.orderCount(),
                        product.quantitySold(),
                        product.totalRevenue());
                result.add(ranked);
            }

            return result;

        } catch (Exception e) {
            System.err.println("Error in getTopProductsWithFilters: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
