package com.group1.swp.pizzario_swp391.service;

import com.group1.swp.pizzario_swp391.dto.data_analytics.AnalyticsDTO;
import com.group1.swp.pizzario_swp391.dto.data_analytics.ProductStatsDTO;
import com.group1.swp.pizzario_swp391.dto.data_analytics.SalesDTO;
import com.group1.swp.pizzario_swp391.entity.Membership;
import com.group1.swp.pizzario_swp391.entity.Order;
import com.group1.swp.pizzario_swp391.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
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
    private Double calculateDelta(Long current, Long previous) {
        if (previous == 0)
            return 0.0;
        return ((current - previous) * 100.0) / previous;
    }

    // Top sản phẩm
    public List<ProductStatsDTO> getTopProducts(LocalDate start, LocalDate end) {
        // TODO: JOIN order_items, products, GROUP BY product_id
        return new ArrayList<>();
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
        Double revenueDelta = calculateDelta(totalRevenue, prevRevenue);

        // ĐƠN HÀNG
        Long totalOrders = (long) currentOrders.size();
        Long prevOrders = (long) previousOrders.size();
        Double ordersDelta = calculateDelta(totalOrders, prevOrders);

        // KH MỚI
        Long newCustomers = countNewCustomers(currentOrders, startDate);
        Long prevNewCustomers = countNewCustomers(previousOrders, prevStartDate);
        Double newCustomersDelta = calculateDelta(newCustomers, prevNewCustomers);

        Long aov = totalOrders > 0
                ? totalRevenue / totalOrders
                : 0;
        Long prevAov = prevOrders > 0
                ? prevRevenue / prevOrders
                : 0;
        Double aovDelta = calculateDelta(aov, prevAov);

        return new AnalyticsDTO(totalRevenue, revenueDelta, totalOrders, ordersDelta, newCustomers, newCustomersDelta, aov, aovDelta);
    }

    private Long countNewCustomers(List<Order> orders, LocalDate startDate) {
        Set<Long> customerIds = orders.stream()
                .map(Order::getMembership)
                .filter(Objects::nonNull) // bỏ null nếu có
                .map(Membership::getId)
                .collect(Collectors.toSet());

        long count = 0;
        for (Long membershipId : customerIds) {

            Order firstOrder = orderRepository.findFirstByMembership_IdOrderByCreatedAtAsc(membershipId);
            if (firstOrder != null &&
                    !firstOrder.getCreatedAt().toLocalDate().isBefore(startDate)) {

                long totalOrdersByMember = orderRepository.countByMembership_Id(membershipId);
                if (totalOrdersByMember <= 1) {
                    count++;
                }
            }
        }
        return count;
    }

}
