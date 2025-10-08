package com.group1.swp.pizzario_swp391.service;

import com.group1.swp.pizzario_swp391.dto.report.WeeklySalesDTO;
import com.group1.swp.pizzario_swp391.entity.Order;
import com.group1.swp.pizzario_swp391.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RequiredArgsConstructor
@Service
public class DataAnalyticsReportService {

    private final OrderRepository orderRepository;

    public WeeklySalesDTO getWeeklySales() {
        LocalDate endExclusive = LocalDate.now();
        LocalDate startDate = endExclusive.minusDays(7);

        List<Order> orders = orderRepository.findInRangeAndPaid(startDate.atStartOfDay(), endExclusive.atStartOfDay());

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("d MMM", new Locale("vi", "VN"));

        List<String> labels = new ArrayList<>(7);
        for (int i = 0; i < 7; i++) {
            labels.add(startDate.plusDays(i).format(fmt));
        }

        Map<LocalDate, Double> revenueByDate = new HashMap<>();
        for (Order o : orders) {
            LocalDate day = o.getCreatedAt().toLocalDate();
            revenueByDate.merge(day, o.getTotalPrice(), Double::sum);
        }

        double[] data = new double[7];
        for (int i = 0; i < 7; i++) {
            LocalDate day = startDate.plusDays(i);
            data[i] = revenueByDate.getOrDefault(day, 0.0);
        }

        return new WeeklySalesDTO(labels, data);
    }

}
