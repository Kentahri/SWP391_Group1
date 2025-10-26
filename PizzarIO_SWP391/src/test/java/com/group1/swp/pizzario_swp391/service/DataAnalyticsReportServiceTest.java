package com.group1.swp.pizzario_swp391.service;

import com.group1.swp.pizzario_swp391.dto.data_analytics.AnalyticsDTO;
import com.group1.swp.pizzario_swp391.dto.data_analytics.ProductStatsDTO;
import com.group1.swp.pizzario_swp391.dto.data_analytics.SalesDTO;
import com.group1.swp.pizzario_swp391.entity.Membership;
import com.group1.swp.pizzario_swp391.entity.Order;
import com.group1.swp.pizzario_swp391.entity.Staff;
import com.group1.swp.pizzario_swp391.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataAnalyticsReportServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private DataAnalyticsReportService dataAnalyticsReportService;

    private Order testOrder1;
    private Order testOrder2;
    private Order testOrder3;
    private Membership testMembership1;
    private Membership testMembership2;
    private Staff testStaff;

    @BeforeEach
    void setUp() {
        // Setup test data
        testStaff = new Staff();
        testStaff.setId(1);
        testStaff.setName("Test Staff");

        testMembership1 = new Membership();
        testMembership1.setId(1L);
        testMembership1.setName("Customer 1");
        testMembership1.setPhoneNumber("0123456789");

        testMembership2 = new Membership();
        testMembership2.setId(2L);
        testMembership2.setName("Customer 2");
        testMembership2.setPhoneNumber("0987654321");

        testOrder1 = new Order();
        testOrder1.setId(1L);
        testOrder1.setTotalPrice(100.0);
        testOrder1.setCreatedAt(LocalDateTime.of(2024, 1, 15, 10, 0));
        testOrder1.setOrderStatus(Order.OrderStatus.COMPLETED);
        testOrder1.setPaymentStatus(Order.PaymentStatus.PAID);
        testOrder1.setMembership(testMembership1);
        testOrder1.setStaff(testStaff);

        testOrder2 = new Order();
        testOrder2.setId(2L);
        testOrder2.setTotalPrice(150.0);
        testOrder2.setCreatedAt(LocalDateTime.of(2024, 1, 16, 14, 30));
        testOrder2.setOrderStatus(Order.OrderStatus.COMPLETED);
        testOrder2.setPaymentStatus(Order.PaymentStatus.PAID);
        testOrder2.setMembership(testMembership2);
        testOrder2.setStaff(testStaff);

        testOrder3 = new Order();
        testOrder3.setId(3L);
        testOrder3.setTotalPrice(200.0);
        testOrder3.setCreatedAt(LocalDateTime.of(2024, 1, 17, 18, 45));
        testOrder3.setOrderStatus(Order.OrderStatus.COMPLETED);
        testOrder3.setPaymentStatus(Order.PaymentStatus.PAID);
        testOrder3.setMembership(testMembership1);
        testOrder3.setStaff(testStaff);
    }

    @Test
    void testGetSalesInRange_EmptyData() {
        // Given
        LocalDate start = LocalDate.of(2024, 1, 15);
        LocalDate end = LocalDate.of(2024, 1, 18);
        when(orderRepository.findInRangeAndPaid(any(), any())).thenReturn(Collections.emptyList());

        // When
        SalesDTO result = dataAnalyticsReportService.getSalesInRange(start, end);

        // Then
        assertNotNull(result);
        assertEquals(3, result.label().size()); // 3 days
        assertEquals(3, result.data().length);
        assertArrayEquals(new double[] { 0.0, 0.0, 0.0 }, result.data());
        assertEquals("15 thg 1", result.label().get(0));
        assertEquals("16 thg 1", result.label().get(1));
        assertEquals("17 thg 1", result.label().get(2));
    }

    @Test
    void testGetSalesInRange_SingleDay() {
        // Given
        LocalDate start = LocalDate.of(2024, 1, 15);
        LocalDate end = LocalDate.of(2024, 1, 16);
        when(orderRepository.findInRangeAndPaid(any(), any())).thenReturn(Arrays.asList(testOrder1));

        // When
        SalesDTO result = dataAnalyticsReportService.getSalesInRange(start, end);

        // Then
        assertNotNull(result);
        assertEquals(1, result.label().size());
        assertEquals(1, result.data().length);
        assertEquals(100.0, result.data()[0], 0.01);
        assertEquals("15 thg 1", result.label().get(0));
    }

    @Test
    void testGetSalesInRange_MultipleDays() {
        // Given
        LocalDate start = LocalDate.of(2024, 1, 15);
        LocalDate end = LocalDate.of(2024, 1, 18);
        when(orderRepository.findInRangeAndPaid(any(), any()))
                .thenReturn(Arrays.asList(testOrder1, testOrder2, testOrder3));

        // When
        SalesDTO result = dataAnalyticsReportService.getSalesInRange(start, end);

        // Then
        assertNotNull(result);
        assertEquals(3, result.label().size());
        assertEquals(3, result.data().length);

        // Day 1 (15th): 100.0
        assertEquals(100.0, result.data()[0], 0.01);
        // Day 2 (16th): 150.0
        assertEquals(150.0, result.data()[1], 0.01);
        // Day 3 (17th): 200.0
        assertEquals(200.0, result.data()[2], 0.01);

        assertEquals("15 thg 1", result.label().get(0));
        assertEquals("16 thg 1", result.label().get(1));
        assertEquals("17 thg 1", result.label().get(2));
    }

    @Test
    void testGetSalesInRange_MultipleOrdersSameDay() {
        // Given
        Order sameDayOrder = new Order();
        sameDayOrder.setId(4L);
        sameDayOrder.setTotalPrice(50.0);
        sameDayOrder.setCreatedAt(LocalDateTime.of(2024, 1, 15, 20, 0));
        sameDayOrder.setOrderStatus(Order.OrderStatus.COMPLETED);
        sameDayOrder.setPaymentStatus(Order.PaymentStatus.PAID);
        sameDayOrder.setMembership(testMembership1);
        sameDayOrder.setStaff(testStaff);

        LocalDate start = LocalDate.of(2024, 1, 15);
        LocalDate end = LocalDate.of(2024, 1, 16);
        when(orderRepository.findInRangeAndPaid(any(), any())).thenReturn(Arrays.asList(testOrder1, sameDayOrder));

        // When
        SalesDTO result = dataAnalyticsReportService.getSalesInRange(start, end);

        // Then
        assertNotNull(result);
        assertEquals(1, result.label().size());
        assertEquals(1, result.data().length);
        // Should sum both orders: 100.0 + 50.0 = 150.0
        assertEquals(150.0, result.data()[0], 0.01);
    }

    @Test
    void testGetTopBestSellingProducts_EmptyResult() {
        // Given
        when(orderRepository.findTopBestSellingProducts(any())).thenReturn(Collections.emptyList());

        // When
        List<ProductStatsDTO> result = dataAnalyticsReportService.getTopBestSellingProducts();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetTopBestSellingProducts_WithData() {
        // Given
        ProductStatsDTO product1 = new ProductStatsDTO("Pizza Margherita", 10, 15, 500L);
        ProductStatsDTO product2 = new ProductStatsDTO("Pizza Pepperoni", 8, 12, 400L);
        ProductStatsDTO product3 = new ProductStatsDTO("Pizza Hawaiian", 5, 8, 300L);

        when(orderRepository.findTopBestSellingProducts(any())).thenReturn(Arrays.asList(product1, product2, product3));

        // When
        List<ProductStatsDTO> result = dataAnalyticsReportService.getTopBestSellingProducts();

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());

        // Check ranking
        assertEquals(1L, result.get(0).topId());
        assertEquals("Pizza Margherita", result.get(0).productName());
        assertEquals(10, result.get(0).orderCount());
        assertEquals(15, result.get(0).quantitySold());
        assertEquals(500L, result.get(0).totalRevenue());

        assertEquals(2L, result.get(1).topId());
        assertEquals("Pizza Pepperoni", result.get(1).productName());

        assertEquals(3L, result.get(2).topId());
        assertEquals("Pizza Hawaiian", result.get(2).productName());
    }

    @Test
    void testGetAnalyticsData_EmptyData() {
        // Given
        LocalDate startDate = LocalDate.of(2024, 1, 15);
        LocalDate endDate = LocalDate.of(2024, 1, 18);
        when(orderRepository.findInRangeAndPaid(any(), any())).thenReturn(Collections.emptyList());
        // No need to mock these methods for empty data test

        // When
        AnalyticsDTO result = dataAnalyticsReportService.getAnalyticsData(startDate, endDate);

        // Then
        assertNotNull(result);
        assertEquals(0L, result.totalRevenue());
        assertEquals(0.0, result.revenueDelta());
        assertEquals(0L, result.totalOrders());
        assertEquals(0.0, result.ordersDelta());
        assertEquals(0L, result.newCustomers());
        assertEquals(0.0, result.newCustomersDelta());
        assertEquals(0.0, result.aov());
        assertEquals(0.0, result.aovDelta());
        assertEquals(0L, result.oldCustomers());
        assertEquals(0.0, result.retentionRate());
    }

    @Test
    void testGetAnalyticsData_WithData() {
        // Given
        LocalDate startDate = LocalDate.of(2024, 1, 15);
        LocalDate endDate = LocalDate.of(2024, 1, 18);

        // Current period orders
        when(orderRepository.findInRangeAndPaid(
                LocalDateTime.of(2024, 1, 15, 0, 0),
                LocalDateTime.of(2024, 1, 18, 0, 0))).thenReturn(Arrays.asList(testOrder1, testOrder2, testOrder3));

        // Previous period orders (empty for simplicity)
        // For startDate=2024-01-15, endDate=2024-01-18, dayBetween=3
        // prevStartDate = 2024-01-15 - 3 = 2024-01-12
        // prevEndDate = 2024-01-15
        when(orderRepository.findInRangeAndPaid(
                LocalDateTime.of(2024, 1, 12, 0, 0),
                LocalDateTime.of(2024, 1, 15, 0, 0))).thenReturn(Collections.emptyList());

        // Mock customer data
        when(orderRepository.findFirstByMembership_IdOrderByCreatedAtAsc(1L)).thenReturn(testOrder1);
        when(orderRepository.findFirstByMembership_IdOrderByCreatedAtAsc(2L)).thenReturn(testOrder2);
        when(orderRepository.countByMembership_Id(1L)).thenReturn(2L); // Customer 1 has 2 orders
        when(orderRepository.countByMembership_Id(2L)).thenReturn(1L); // Customer 2 has 1 order

        // When
        AnalyticsDTO result = dataAnalyticsReportService.getAnalyticsData(startDate, endDate);

        // Then
        assertNotNull(result);

        // Total revenue: 100 + 150 + 200 = 450
        assertEquals(450L, result.totalRevenue());

        // Revenue delta: (450 - 0) / 0 = 0 (handled by calculateDelta)
        assertEquals(0.0, result.revenueDelta());

        // Total orders: 3
        assertEquals(3L, result.totalOrders());

        // Orders delta: (3 - 0) / 0 = 0
        assertEquals(0.0, result.ordersDelta());

        // AOV: 450 / 3 = 150
        assertEquals(150.0, result.aov(), 0.01);

        // New customers: Customer 2 (first order in period, total orders = 1)
        assertEquals(1L, result.newCustomers());

        // Old customers: Customer 1 (has orders before this period)
        assertEquals(1L, result.oldCustomers());

        // Retention rate: 1 / 2 = 50%
        assertEquals(50.0, result.retentionRate(), 0.01);
    }

    @Test
    void testGetAnalyticsData_NewCustomersOnly() {
        // Given
        LocalDate startDate = LocalDate.of(2024, 1, 15);
        LocalDate endDate = LocalDate.of(2024, 1, 18);

        // Current period orders
        when(orderRepository.findInRangeAndPaid(
                LocalDateTime.of(2024, 1, 15, 0, 0),
                LocalDateTime.of(2024, 1, 18, 0, 0))).thenReturn(Arrays.asList(testOrder1, testOrder2));

        // Previous period orders (empty)
        when(orderRepository.findInRangeAndPaid(
                LocalDateTime.of(2024, 1, 12, 0, 0),
                LocalDateTime.of(2024, 1, 15, 0, 0))).thenReturn(Collections.emptyList());

        // Mock customer data - both are new customers
        when(orderRepository.findFirstByMembership_IdOrderByCreatedAtAsc(1L)).thenReturn(testOrder1);
        when(orderRepository.findFirstByMembership_IdOrderByCreatedAtAsc(2L)).thenReturn(testOrder2);
        when(orderRepository.countByMembership_Id(1L)).thenReturn(1L); // Customer 1 has 1 total order
        when(orderRepository.countByMembership_Id(2L)).thenReturn(1L); // Customer 2 has 1 total order

        // When
        AnalyticsDTO result = dataAnalyticsReportService.getAnalyticsData(startDate, endDate);

        // Then
        assertNotNull(result);

        // Total revenue: 100 + 150 = 250
        assertEquals(250L, result.totalRevenue());

        // Total orders: 2
        assertEquals(2L, result.totalOrders());

        // AOV: 250 / 2 = 125
        assertEquals(125.0, result.aov(), 0.01);

        // New customers: 2
        assertEquals(2L, result.newCustomers());

        // Old customers: 0
        assertEquals(0L, result.oldCustomers());

        // Retention rate: 0 / 2 = 0%
        assertEquals(0.0, result.retentionRate(), 0.01);
    }

    @Test
    void testGetAnalyticsData_EdgeCaseZeroOrders() {
        // Given
        LocalDate startDate = LocalDate.of(2024, 1, 15);
        LocalDate endDate = LocalDate.of(2024, 1, 18);

        // Current period orders (empty)
        when(orderRepository.findInRangeAndPaid(
                LocalDateTime.of(2024, 1, 15, 0, 0),
                LocalDateTime.of(2024, 1, 18, 0, 0))).thenReturn(Collections.emptyList());

        // Previous period orders (empty)
        when(orderRepository.findInRangeAndPaid(
                LocalDateTime.of(2024, 1, 12, 0, 0),
                LocalDateTime.of(2024, 1, 15, 0, 0))).thenReturn(Collections.emptyList());

        // When
        AnalyticsDTO result = dataAnalyticsReportService.getAnalyticsData(startDate, endDate);

        // Then
        assertNotNull(result);
        assertEquals(0L, result.totalRevenue());
        assertEquals(0.0, result.revenueDelta());
        assertEquals(0L, result.totalOrders());
        assertEquals(0.0, result.ordersDelta());
        assertEquals(0L, result.newCustomers());
        assertEquals(0.0, result.newCustomersDelta());
        assertEquals(0.0, result.aov());
        assertEquals(0.0, result.aovDelta());
        assertEquals(0L, result.oldCustomers());
        assertEquals(0.0, result.retentionRate());
    }

    @Test
    void testGetAnalyticsData_EdgeCaseZeroPreviousOrders() {
        // Given
        LocalDate startDate = LocalDate.of(2024, 1, 15);
        LocalDate endDate = LocalDate.of(2024, 1, 18);

        // Current period orders
        when(orderRepository.findInRangeAndPaid(
                LocalDateTime.of(2024, 1, 15, 0, 0),
                LocalDateTime.of(2024, 1, 18, 0, 0))).thenReturn(Arrays.asList(testOrder1));

        // Previous period orders (empty)
        when(orderRepository.findInRangeAndPaid(
                LocalDateTime.of(2024, 1, 12, 0, 0),
                LocalDateTime.of(2024, 1, 15, 0, 0))).thenReturn(Collections.emptyList());

        // Mock customer data
        when(orderRepository.findFirstByMembership_IdOrderByCreatedAtAsc(1L)).thenReturn(testOrder1);
        when(orderRepository.countByMembership_Id(1L)).thenReturn(1L);

        // When
        AnalyticsDTO result = dataAnalyticsReportService.getAnalyticsData(startDate, endDate);

        // Then
        assertNotNull(result);

        // Total revenue: 100
        assertEquals(100L, result.totalRevenue());

        // Revenue delta: (100 - 0) / 0 = 0 (handled by calculateDelta when previous is
        // 0)
        assertEquals(0.0, result.revenueDelta());

        // Total orders: 1
        assertEquals(1L, result.totalOrders());

        // Orders delta: (1 - 0) / 0 = 0
        assertEquals(0.0, result.ordersDelta());

        // AOV: 100 / 1 = 100
        assertEquals(100.0, result.aov(), 0.01);

        // AOV delta: (100 - 0) / 0 = 0
        assertEquals(0.0, result.aovDelta());

        // New customers: 1
        assertEquals(1L, result.newCustomers());

        // Old customers: 0
        assertEquals(0L, result.oldCustomers());

        // Retention rate: 0 / 1 = 0%
        assertEquals(0.0, result.retentionRate(), 0.01);
    }

    @Test
    void testGetAnalyticsData_EdgeCaseZeroCurrentOrders() {
        // Given
        LocalDate startDate = LocalDate.of(2024, 1, 15);
        LocalDate endDate = LocalDate.of(2024, 1, 18);

        // Current period orders (empty)
        when(orderRepository.findInRangeAndPaid(
                LocalDateTime.of(2024, 1, 15, 0, 0),
                LocalDateTime.of(2024, 1, 18, 0, 0))).thenReturn(Collections.emptyList());

        // Previous period orders
        Order prevOrder = new Order();
        prevOrder.setId(10L);
        prevOrder.setTotalPrice(200.0);
        prevOrder.setCreatedAt(LocalDateTime.of(2024, 1, 10, 10, 0));
        prevOrder.setOrderStatus(Order.OrderStatus.COMPLETED);
        prevOrder.setPaymentStatus(Order.PaymentStatus.PAID);
        prevOrder.setMembership(testMembership1);
        prevOrder.setStaff(testStaff);

        when(orderRepository.findInRangeAndPaid(
                LocalDateTime.of(2024, 1, 12, 0, 0),
                LocalDateTime.of(2024, 1, 15, 0, 0))).thenReturn(Arrays.asList(prevOrder));

        // When
        AnalyticsDTO result = dataAnalyticsReportService.getAnalyticsData(startDate, endDate);

        // Then
        assertNotNull(result);

        // Total revenue: 0
        assertEquals(0L, result.totalRevenue());

        // Revenue delta: (0 - 200) / 200 * 100 = -100%
        assertEquals(-100.0, result.revenueDelta(), 0.01);

        // Total orders: 0
        assertEquals(0L, result.totalOrders());

        // Orders delta: (0 - 1) / 1 * 100 = -100%
        assertEquals(-100.0, result.ordersDelta(), 0.01);

        // AOV: 0 / 0 = 0 (handled by division by zero check)
        assertEquals(0.0, result.aov(), 0.01);

        // AOV delta: (0 - 200) / 200 * 100 = -100%
        assertEquals(-100.0, result.aovDelta(), 0.01);

        // New customers: 0
        assertEquals(0L, result.newCustomers());

        // Old customers: 0
        assertEquals(0L, result.oldCustomers());

        // Retention rate: 0 / 0 = 0 (handled by division by zero check)
        assertEquals(0.0, result.retentionRate(), 0.01);
    }

    @Test
    void testGetAnalyticsData_ComplexScenario() {
        // Given
        LocalDate startDate = LocalDate.of(2024, 1, 15);
        LocalDate endDate = LocalDate.of(2024, 1, 18);

        // Create additional test data
        Membership testMembership3 = new Membership();
        testMembership3.setId(3L);
        testMembership3.setName("Customer 3");
        testMembership3.setPhoneNumber("0555555555");

        Order testOrder4 = new Order();
        testOrder4.setId(4L);
        testOrder4.setTotalPrice(300.0);
        testOrder4.setCreatedAt(LocalDateTime.of(2024, 1, 16, 12, 0));
        testOrder4.setOrderStatus(Order.OrderStatus.COMPLETED);
        testOrder4.setPaymentStatus(Order.PaymentStatus.PAID);
        testOrder4.setMembership(testMembership3);
        testOrder4.setStaff(testStaff);

        // Previous period orders
        Order prevOrder1 = new Order();
        prevOrder1.setId(10L);
        prevOrder1.setTotalPrice(80.0);
        prevOrder1.setCreatedAt(LocalDateTime.of(2024, 1, 10, 10, 0));
        prevOrder1.setOrderStatus(Order.OrderStatus.COMPLETED);
        prevOrder1.setPaymentStatus(Order.PaymentStatus.PAID);
        prevOrder1.setMembership(testMembership1);
        prevOrder1.setStaff(testStaff);

        Order prevOrder2 = new Order();
        prevOrder2.setId(11L);
        prevOrder2.setTotalPrice(120.0);
        prevOrder2.setCreatedAt(LocalDateTime.of(2024, 1, 12, 14, 0));
        prevOrder2.setOrderStatus(Order.OrderStatus.COMPLETED);
        prevOrder2.setPaymentStatus(Order.PaymentStatus.PAID);
        prevOrder2.setMembership(testMembership2);
        prevOrder2.setStaff(testStaff);

        // Current period orders
        when(orderRepository.findInRangeAndPaid(
                LocalDateTime.of(2024, 1, 15, 0, 0),
                LocalDateTime.of(2024, 1, 18, 0, 0)))
                .thenReturn(Arrays.asList(testOrder1, testOrder2, testOrder3, testOrder4));

        // Previous period orders
        when(orderRepository.findInRangeAndPaid(
                LocalDateTime.of(2024, 1, 12, 0, 0),
                LocalDateTime.of(2024, 1, 15, 0, 0))).thenReturn(Arrays.asList(prevOrder1, prevOrder2));

        // Mock customer data
        when(orderRepository.findFirstByMembership_IdOrderByCreatedAtAsc(1L)).thenReturn(prevOrder1);
        when(orderRepository.findFirstByMembership_IdOrderByCreatedAtAsc(2L)).thenReturn(prevOrder2);
        when(orderRepository.findFirstByMembership_IdOrderByCreatedAtAsc(3L)).thenReturn(testOrder4);
        // Only Customer 3 needs countByMembership_Id since only Customer 3's first
        // order is in the period
        when(orderRepository.countByMembership_Id(3L)).thenReturn(1L); // Customer 3 has 1 total order

        // When
        AnalyticsDTO result = dataAnalyticsReportService.getAnalyticsData(startDate, endDate);

        // Then
        assertNotNull(result);

        // Total revenue: 100 + 150 + 200 + 300 = 750
        assertEquals(750L, result.totalRevenue());

        // Revenue delta: (750 - 200) / 200 * 100 = 275%
        assertEquals(275.0, result.revenueDelta(), 0.01);

        // Total orders: 4
        assertEquals(4L, result.totalOrders());

        // Orders delta: (4 - 2) / 2 * 100 = 100%
        assertEquals(100.0, result.ordersDelta(), 0.01);

        // AOV: 750 / 4 = 187.5
        assertEquals(187.5, result.aov(), 0.01);

        // AOV delta: (187.5 - 100) / 100 * 100 = 87.5%
        assertEquals(87.5, result.aovDelta(), 0.01);

        // New customers: 1 (Customer 3)
        assertEquals(1L, result.newCustomers());

        // Old customers: 2 (Customer 1 and 2)
        assertEquals(2L, result.oldCustomers());

        // Retention rate: 2 / 3 = 66.67%
        assertEquals(66.67, result.retentionRate(), 0.01);
    }

    @Test
    void testCountNewCustomers_FirstOrderExactlyOnStartDate() {
        // Given
        LocalDate startDate = LocalDate.of(2024, 1, 15);
        LocalDate endDate = LocalDate.of(2024, 1, 18);

        // Create order with first order exactly on start date
        Order firstOrder = new Order();
        firstOrder.setId(10L);
        firstOrder.setTotalPrice(50.0);
        firstOrder.setCreatedAt(LocalDateTime.of(2024, 1, 15, 0, 0)); // Exactly on start date
        firstOrder.setOrderStatus(Order.OrderStatus.COMPLETED);
        firstOrder.setPaymentStatus(Order.PaymentStatus.PAID);
        firstOrder.setMembership(testMembership1);
        firstOrder.setStaff(testStaff);

        when(orderRepository.findInRangeAndPaid(any(), any())).thenReturn(Arrays.asList(testOrder1));
        when(orderRepository.findFirstByMembership_IdOrderByCreatedAtAsc(1L)).thenReturn(firstOrder);
        when(orderRepository.countByMembership_Id(1L)).thenReturn(1L); // Has 1 total order

        // When
        AnalyticsDTO result = dataAnalyticsReportService.getAnalyticsData(startDate, endDate);

        // Then
        // Should count as new customer since first order is not before start date
        assertEquals(1L, result.newCustomers());
        assertEquals(0L, result.oldCustomers());
    }

    @Test
    void testCountNewCustomers_MultipleOrdersButFirstInPeriod() {
        // Given
        LocalDate startDate = LocalDate.of(2024, 1, 15);
        LocalDate endDate = LocalDate.of(2024, 1, 18);

        // Create order with first order in period
        Order firstOrder = new Order();
        firstOrder.setId(10L);
        firstOrder.setTotalPrice(50.0);
        firstOrder.setCreatedAt(LocalDateTime.of(2024, 1, 16, 10, 0)); // In period
        firstOrder.setOrderStatus(Order.OrderStatus.COMPLETED);
        firstOrder.setPaymentStatus(Order.PaymentStatus.PAID);
        firstOrder.setMembership(testMembership1);
        firstOrder.setStaff(testStaff);

        when(orderRepository.findInRangeAndPaid(any(), any())).thenReturn(Arrays.asList(testOrder1));
        when(orderRepository.findFirstByMembership_IdOrderByCreatedAtAsc(1L)).thenReturn(firstOrder);
        when(orderRepository.countByMembership_Id(1L)).thenReturn(3L); // Has 3 total orders

        // When
        AnalyticsDTO result = dataAnalyticsReportService.getAnalyticsData(startDate, endDate);

        // Then
        // Should not count as new customer since has multiple orders
        assertEquals(0L, result.newCustomers());
        assertEquals(1L, result.oldCustomers());
    }

    @Test
    void testCountNewCustomers_NullMembership() {
        // Given
        LocalDate startDate = LocalDate.of(2024, 1, 15);
        LocalDate endDate = LocalDate.of(2024, 1, 18);

        // Create order with null membership
        Order orderWithNullMembership = new Order();
        orderWithNullMembership.setId(10L);
        orderWithNullMembership.setTotalPrice(100.0);
        orderWithNullMembership.setCreatedAt(LocalDateTime.of(2024, 1, 16, 10, 0));
        orderWithNullMembership.setOrderStatus(Order.OrderStatus.COMPLETED);
        orderWithNullMembership.setPaymentStatus(Order.PaymentStatus.PAID);
        orderWithNullMembership.setMembership(null); // Null membership
        orderWithNullMembership.setStaff(testStaff);

        when(orderRepository.findInRangeAndPaid(any(), any())).thenReturn(Arrays.asList(orderWithNullMembership));

        // When
        AnalyticsDTO result = dataAnalyticsReportService.getAnalyticsData(startDate, endDate);

        // Then
        // Should not count as new customer since membership is null
        assertEquals(0L, result.newCustomers());
        assertEquals(0L, result.oldCustomers());
    }

    @Test
    void testCalculateTotalRevenue_EmptyList() {
        // Given
        LocalDate startDate = LocalDate.of(2024, 1, 15);
        LocalDate endDate = LocalDate.of(2024, 1, 18);

        when(orderRepository.findInRangeAndPaid(any(), any())).thenReturn(Collections.emptyList());

        // When
        AnalyticsDTO result = dataAnalyticsReportService.getAnalyticsData(startDate, endDate);

        // Then
        assertEquals(0L, result.totalRevenue());
    }

    @Test
    void testCalculateTotalRevenue_SingleOrder() {
        // Given
        LocalDate startDate = LocalDate.of(2024, 1, 15);
        LocalDate endDate = LocalDate.of(2024, 1, 18);

        when(orderRepository.findInRangeAndPaid(any(), any())).thenReturn(Arrays.asList(testOrder1));

        // When
        AnalyticsDTO result = dataAnalyticsReportService.getAnalyticsData(startDate, endDate);

        // Then
        assertEquals(100L, result.totalRevenue());
    }

    @Test
    void testCalculateTotalRevenue_MultipleOrders() {
        // Given
        LocalDate startDate = LocalDate.of(2024, 1, 15);
        LocalDate endDate = LocalDate.of(2024, 1, 18);

        when(orderRepository.findInRangeAndPaid(any(), any()))
                .thenReturn(Arrays.asList(testOrder1, testOrder2, testOrder3));

        // When
        AnalyticsDTO result = dataAnalyticsReportService.getAnalyticsData(startDate, endDate);

        // Then
        assertEquals(450L, result.totalRevenue()); // 100 + 150 + 200
    }

    @Test
    void testCalculateTotalRevenue_ZeroTotalPrice() {
        // Given
        LocalDate startDate = LocalDate.of(2024, 1, 15);
        LocalDate endDate = LocalDate.of(2024, 1, 18);

        Order zeroPriceOrder = new Order();
        zeroPriceOrder.setId(10L);
        zeroPriceOrder.setTotalPrice(0.0);
        zeroPriceOrder.setCreatedAt(LocalDateTime.of(2024, 1, 16, 10, 0));
        zeroPriceOrder.setOrderStatus(Order.OrderStatus.COMPLETED);
        zeroPriceOrder.setPaymentStatus(Order.PaymentStatus.PAID);
        zeroPriceOrder.setMembership(testMembership1);
        zeroPriceOrder.setStaff(testStaff);

        when(orderRepository.findInRangeAndPaid(any(), any())).thenReturn(Arrays.asList(zeroPriceOrder));

        // When
        AnalyticsDTO result = dataAnalyticsReportService.getAnalyticsData(startDate, endDate);

        // Then
        assertEquals(0L, result.totalRevenue());
    }

    @Test
    void testCalculateDelta_PreviousIsZero() {
        // Given
        LocalDate startDate = LocalDate.of(2024, 1, 15);
        LocalDate endDate = LocalDate.of(2024, 1, 18);

        // Current period has orders, previous period is empty
        when(orderRepository.findInRangeAndPaid(
                LocalDateTime.of(2024, 1, 15, 0, 0),
                LocalDateTime.of(2024, 1, 18, 0, 0))).thenReturn(Arrays.asList(testOrder1));

        when(orderRepository.findInRangeAndPaid(
                LocalDateTime.of(2024, 1, 12, 0, 0),
                LocalDateTime.of(2024, 1, 15, 0, 0))).thenReturn(Collections.emptyList());

        // When
        AnalyticsDTO result = dataAnalyticsReportService.getAnalyticsData(startDate, endDate);

        // Then
        // Delta should be 0 when previous is 0
        assertEquals(0.0, result.revenueDelta());
        assertEquals(0.0, result.ordersDelta());
        assertEquals(0.0, result.aovDelta());
    }

    @Test
    void testCalculateDelta_CurrentIsZero() {
        // Given
        LocalDate startDate = LocalDate.of(2024, 1, 15);
        LocalDate endDate = LocalDate.of(2024, 1, 18);

        // Current period is empty, previous period has orders
        when(orderRepository.findInRangeAndPaid(
                LocalDateTime.of(2024, 1, 15, 0, 0),
                LocalDateTime.of(2024, 1, 18, 0, 0))).thenReturn(Collections.emptyList());

        when(orderRepository.findInRangeAndPaid(
                LocalDateTime.of(2024, 1, 12, 0, 0),
                LocalDateTime.of(2024, 1, 15, 0, 0))).thenReturn(Arrays.asList(testOrder1));

        // When
        AnalyticsDTO result = dataAnalyticsReportService.getAnalyticsData(startDate, endDate);

        // Then
        // Delta should be -100% when current is 0
        assertEquals(-100.0, result.revenueDelta(), 0.01);
        assertEquals(-100.0, result.ordersDelta(), 0.01);
        assertEquals(-100.0, result.aovDelta(), 0.01);
    }

    @Test
    void testCalculateDelta_BothAreZero() {
        // Given
        LocalDate startDate = LocalDate.of(2024, 1, 15);
        LocalDate endDate = LocalDate.of(2024, 1, 18);

        // Both periods are empty
        when(orderRepository.findInRangeAndPaid(any(), any())).thenReturn(Collections.emptyList());

        // When
        AnalyticsDTO result = dataAnalyticsReportService.getAnalyticsData(startDate, endDate);

        // Then
        // Delta should be 0 when both are 0
        assertEquals(0.0, result.revenueDelta());
        assertEquals(0.0, result.ordersDelta());
        assertEquals(0.0, result.aovDelta());
    }

    @Test
    void testCalculateDelta_PositiveDelta() {
        // Given
        LocalDate startDate = LocalDate.of(2024, 1, 15);
        LocalDate endDate = LocalDate.of(2024, 1, 18);

        // Current period: 2 orders, 300 revenue
        when(orderRepository.findInRangeAndPaid(
                LocalDateTime.of(2024, 1, 15, 0, 0),
                LocalDateTime.of(2024, 1, 18, 0, 0))).thenReturn(Arrays.asList(testOrder1, testOrder2));

        // Previous period: 1 order, 100 revenue
        when(orderRepository.findInRangeAndPaid(
                LocalDateTime.of(2024, 1, 12, 0, 0),
                LocalDateTime.of(2024, 1, 15, 0, 0))).thenReturn(Arrays.asList(testOrder3));

        // When
        AnalyticsDTO result = dataAnalyticsReportService.getAnalyticsData(startDate, endDate);

        // Then
        // Revenue delta: (250 - 200) / 200 * 100 = 25%
        assertEquals(25.0, result.revenueDelta(), 0.01);
        // Orders delta: (2 - 1) / 1 * 100 = 100%
        assertEquals(100.0, result.ordersDelta(), 0.01);
        // AOV delta: (125 - 200) / 200 * 100 = -37.5%
        assertEquals(-37.5, result.aovDelta(), 0.01);
    }

    @Test
    void testCalculateDelta_NegativeDelta() {
        // Given
        LocalDate startDate = LocalDate.of(2024, 1, 15);
        LocalDate endDate = LocalDate.of(2024, 1, 18);

        // Current period: 1 order, 100 revenue
        when(orderRepository.findInRangeAndPaid(
                LocalDateTime.of(2024, 1, 15, 0, 0),
                LocalDateTime.of(2024, 1, 18, 0, 0))).thenReturn(Arrays.asList(testOrder1));

        // Previous period: 2 orders, 350 revenue
        when(orderRepository.findInRangeAndPaid(
                LocalDateTime.of(2024, 1, 12, 0, 0),
                LocalDateTime.of(2024, 1, 15, 0, 0))).thenReturn(Arrays.asList(testOrder2, testOrder3));

        // When
        AnalyticsDTO result = dataAnalyticsReportService.getAnalyticsData(startDate, endDate);

        // Then
        // Revenue delta: (100 - 350) / 350 * 100 = -71.43%
        assertEquals(-71.43, result.revenueDelta(), 0.01);
        // Orders delta: (1 - 2) / 2 * 100 = -50%
        assertEquals(-50.0, result.ordersDelta(), 0.01);
        // AOV delta: (100 - 175) / 175 * 100 = -42.86%
        assertEquals(-42.86, result.aovDelta(), 0.01);
    }

    @Test
    void testDateRange_SameStartAndEndDate() {
        // Given
        LocalDate sameDate = LocalDate.of(2024, 1, 15);
        when(orderRepository.findInRangeAndPaid(any(), any())).thenReturn(Arrays.asList(testOrder1));

        // When
        SalesDTO result = dataAnalyticsReportService.getSalesInRange(sameDate, sameDate);

        // Then
        // Same start and end date should result in 0 days, so empty result
        assertEquals(0, result.label().size());
        assertEquals(0, result.data().length);
    }

    @Test
    void testDateRange_SingleDayRange() {
        // Given
        LocalDate startDate = LocalDate.of(2024, 1, 15);
        LocalDate endDate = LocalDate.of(2024, 1, 16);
        when(orderRepository.findInRangeAndPaid(any(), any())).thenReturn(Arrays.asList(testOrder1));

        // When
        SalesDTO result = dataAnalyticsReportService.getSalesInRange(startDate, endDate);

        // Then
        assertEquals(1, result.label().size());
        assertEquals(1, result.data().length);
        assertEquals(100.0, result.data()[0], 0.01);
    }

    @Test
    void testDateRange_LeapYearDates() {
        // Given
        LocalDate startDate = LocalDate.of(2024, 2, 28); // 2024 is leap year
        LocalDate endDate = LocalDate.of(2024, 3, 1);
        when(orderRepository.findInRangeAndPaid(any(), any())).thenReturn(Collections.emptyList());

        // When
        SalesDTO result = dataAnalyticsReportService.getSalesInRange(startDate, endDate);

        // Then
        assertEquals(2, result.label().size()); // 28 Feb + 1 Mar
        assertEquals(2, result.data().length);
        assertArrayEquals(new double[] { 0.0, 0.0 }, result.data());
    }

    @Test
    void testDateRange_MonthBoundaries() {
        // Given
        LocalDate startDate = LocalDate.of(2024, 1, 31);
        LocalDate endDate = LocalDate.of(2024, 2, 2);
        when(orderRepository.findInRangeAndPaid(any(), any())).thenReturn(Collections.emptyList());

        // When
        SalesDTO result = dataAnalyticsReportService.getSalesInRange(startDate, endDate);

        // Then
        assertEquals(2, result.label().size()); // 31 Jan + 1 Feb
        assertEquals(2, result.data().length);
    }

    @Test
    void testDateRange_YearBoundaries() {
        // Given
        LocalDate startDate = LocalDate.of(2023, 12, 31);
        LocalDate endDate = LocalDate.of(2024, 1, 2);
        when(orderRepository.findInRangeAndPaid(any(), any())).thenReturn(Collections.emptyList());

        // When
        SalesDTO result = dataAnalyticsReportService.getSalesInRange(startDate, endDate);

        // Then
        assertEquals(2, result.label().size()); // 31 Dec + 1 Jan
        assertEquals(2, result.data().length);
    }

    @Test
    void testDataConsistency_RetentionRateCalculation() {
        // Given
        LocalDate startDate = LocalDate.of(2024, 1, 15);
        LocalDate endDate = LocalDate.of(2024, 1, 18);

        // Create 3 customers: 1 new, 2 old
        Membership newCustomer = new Membership();
        newCustomer.setId(3L);
        newCustomer.setName("New Customer");
        newCustomer.setPhoneNumber("0333333333");

        Order newCustomerOrder = new Order();
        newCustomerOrder.setId(10L);
        newCustomerOrder.setTotalPrice(100.0);
        newCustomerOrder.setCreatedAt(LocalDateTime.of(2024, 1, 16, 10, 0));
        newCustomerOrder.setOrderStatus(Order.OrderStatus.COMPLETED);
        newCustomerOrder.setPaymentStatus(Order.PaymentStatus.PAID);
        newCustomerOrder.setMembership(newCustomer);
        newCustomerOrder.setStaff(testStaff);

        when(orderRepository.findInRangeAndPaid(any(), any()))
                .thenReturn(Arrays.asList(testOrder1, testOrder2, newCustomerOrder));
        when(orderRepository.findFirstByMembership_IdOrderByCreatedAtAsc(1L)).thenReturn(testOrder1);
        when(orderRepository.findFirstByMembership_IdOrderByCreatedAtAsc(2L)).thenReturn(testOrder2);
        when(orderRepository.findFirstByMembership_IdOrderByCreatedAtAsc(3L)).thenReturn(newCustomerOrder);
        when(orderRepository.countByMembership_Id(1L)).thenReturn(2L);
        when(orderRepository.countByMembership_Id(2L)).thenReturn(2L);
        when(orderRepository.countByMembership_Id(3L)).thenReturn(1L);

        // When
        AnalyticsDTO result = dataAnalyticsReportService.getAnalyticsData(startDate, endDate);

        // Then
        // Total customers: 3
        // New customers: 1
        // Old customers: 2
        // Retention rate: 2/3 = 66.67%
        assertEquals(1L, result.newCustomers());
        assertEquals(2L, result.oldCustomers());
        assertEquals(66.67, result.retentionRate(), 0.01);

        // Verify: newCustomers + oldCustomers = totalCustomers
        assertEquals(result.newCustomers() + result.oldCustomers(), 3L);
    }

    @Test
    void testDataConsistency_AOVCalculation() {
        // Given
        LocalDate startDate = LocalDate.of(2024, 1, 15);
        LocalDate endDate = LocalDate.of(2024, 1, 18);

        when(orderRepository.findInRangeAndPaid(any(), any()))
                .thenReturn(Arrays.asList(testOrder1, testOrder2, testOrder3));

        // When
        AnalyticsDTO result = dataAnalyticsReportService.getAnalyticsData(startDate, endDate);

        // Then
        // Total revenue: 100 + 150 + 200 = 450
        // Total orders: 3
        // AOV: 450 / 3 = 150
        assertEquals(450L, result.totalRevenue());
        assertEquals(3L, result.totalOrders());
        assertEquals(150.0, result.aov(), 0.01);

        // Verify: AOV * totalOrders = totalRevenue
        assertEquals(result.aov() * result.totalOrders(), result.totalRevenue(), 0.01);
    }

    @Test
    void testDataConsistency_DeltaCalculationFormula() {
        // Given
        LocalDate startDate = LocalDate.of(2024, 1, 15);
        LocalDate endDate = LocalDate.of(2024, 1, 18);

        // Current period: 2 orders, 250 revenue
        when(orderRepository.findInRangeAndPaid(
                LocalDateTime.of(2024, 1, 15, 0, 0),
                LocalDateTime.of(2024, 1, 18, 0, 0))).thenReturn(Arrays.asList(testOrder1, testOrder2));

        // Previous period: 1 order, 200 revenue
        when(orderRepository.findInRangeAndPaid(
                LocalDateTime.of(2024, 1, 12, 0, 0),
                LocalDateTime.of(2024, 1, 15, 0, 0))).thenReturn(Arrays.asList(testOrder3));

        // When
        AnalyticsDTO result = dataAnalyticsReportService.getAnalyticsData(startDate, endDate);

        // Then
        // Revenue delta: (250 - 200) / 200 * 100 = 25%
        assertEquals(25.0, result.revenueDelta(), 0.01);

        // Orders delta: (2 - 1) / 1 * 100 = 100%
        assertEquals(100.0, result.ordersDelta(), 0.01);

        // AOV delta: (125 - 200) / 200 * 100 = -37.5%
        assertEquals(-37.5, result.aovDelta(), 0.01);

        // Verify delta calculation formula: (current - previous) / previous * 100
        double expectedRevenueDelta = (250.0 - 200.0) / 200.0 * 100.0;
        double expectedOrdersDelta = (2.0 - 1.0) / 1.0 * 100.0;
        double expectedAovDelta = (125.0 - 200.0) / 200.0 * 100.0;

        assertEquals(expectedRevenueDelta, result.revenueDelta(), 0.01);
        assertEquals(expectedOrdersDelta, result.ordersDelta(), 0.01);
        assertEquals(expectedAovDelta, result.aovDelta(), 0.01);
    }

    @Test
    void testErrorHandling_NullRepositoryResponses() {
        // Given
        LocalDate startDate = LocalDate.of(2024, 1, 15);
        LocalDate endDate = LocalDate.of(2024, 1, 18);

        when(orderRepository.findInRangeAndPaid(any(), any())).thenReturn(null);

        // When & Then
        assertThrows(NullPointerException.class, () -> {
            dataAnalyticsReportService.getAnalyticsData(startDate, endDate);
        });
    }

    @Test
    void testErrorHandling_InvalidDateRange() {
        // Given
        LocalDate startDate = LocalDate.of(2024, 1, 18);
        LocalDate endDate = LocalDate.of(2024, 1, 15); // End before start

        when(orderRepository.findInRangeAndPaid(any(), any())).thenReturn(Collections.emptyList());

        // When & Then
        // Should throw exception for invalid date range
        assertThrows(IllegalArgumentException.class, () -> {
            dataAnalyticsReportService.getSalesInRange(startDate, endDate);
        });
    }

    @Test
    void testErrorHandling_CorruptedOrderData() {
        // Given
        LocalDate startDate = LocalDate.of(2024, 1, 15);
        LocalDate endDate = LocalDate.of(2024, 1, 18);

        // Create order with null membership
        Order corruptedOrder = new Order();
        corruptedOrder.setId(10L);
        corruptedOrder.setTotalPrice(100.0);
        corruptedOrder.setCreatedAt(LocalDateTime.of(2024, 1, 16, 10, 0));
        corruptedOrder.setOrderStatus(Order.OrderStatus.COMPLETED);
        corruptedOrder.setPaymentStatus(Order.PaymentStatus.PAID);
        corruptedOrder.setMembership(null); // Null membership
        corruptedOrder.setStaff(testStaff);

        when(orderRepository.findInRangeAndPaid(any(), any())).thenReturn(Arrays.asList(corruptedOrder));

        // When
        AnalyticsDTO result = dataAnalyticsReportService.getAnalyticsData(startDate, endDate);

        // Then
        // Should handle null membership gracefully
        assertEquals(100L, result.totalRevenue());
        assertEquals(1L, result.totalOrders());
        assertEquals(0L, result.newCustomers());
        assertEquals(0L, result.oldCustomers());
    }

    // TODO: Test edge cases for specific business logic
    @Test
    void testBusinessLogicEdgeCases() {
        // TODO: Test with orders having different statuses
        // TODO: Test with orders having different payment statuses
        // TODO: Test with very large numbers (overflow protection)
        // TODO: Test with very small numbers (precision)
    }

    @Test
    void testBusinessLogic_OrdersWithDifferentStatuses() {
        // Given
        LocalDate startDate = LocalDate.of(2024, 1, 15);
        LocalDate endDate = LocalDate.of(2024, 1, 18);

        // Create orders with different statuses
        Order pendingOrder = new Order();
        pendingOrder.setId(10L);
        pendingOrder.setTotalPrice(100.0);
        pendingOrder.setCreatedAt(LocalDateTime.of(2024, 1, 16, 10, 0));
        pendingOrder.setOrderStatus(Order.OrderStatus.PREPARING); // Not COMPLETED
        pendingOrder.setPaymentStatus(Order.PaymentStatus.PAID);
        pendingOrder.setMembership(testMembership1);
        pendingOrder.setStaff(testStaff);

        Order unpaidOrder = new Order();
        unpaidOrder.setId(11L);
        unpaidOrder.setTotalPrice(150.0);
        unpaidOrder.setCreatedAt(LocalDateTime.of(2024, 1, 16, 11, 0));
        unpaidOrder.setOrderStatus(Order.OrderStatus.COMPLETED);
        unpaidOrder.setPaymentStatus(Order.PaymentStatus.UNPAID); // Not PAID
        unpaidOrder.setMembership(testMembership2);
        unpaidOrder.setStaff(testStaff);

        // Only completed and paid orders should be included
        when(orderRepository.findInRangeAndPaid(any(), any())).thenReturn(Arrays.asList(testOrder1));

        // When
        AnalyticsDTO result = dataAnalyticsReportService.getAnalyticsData(startDate, endDate);

        // Then
        // Should only include completed and paid orders
        assertEquals(100L, result.totalRevenue());
        assertEquals(1L, result.totalOrders());
    }

    @Test
    void testBusinessLogic_VeryLargeNumbers() {
        // Given
        LocalDate startDate = LocalDate.of(2024, 1, 15);
        LocalDate endDate = LocalDate.of(2024, 1, 18);

        // Create order with very large total price (but within Long range)
        Order largeOrder = new Order();
        largeOrder.setId(10L);
        largeOrder.setTotalPrice(1_000_000_000.0); // 1 billion
        largeOrder.setCreatedAt(LocalDateTime.of(2024, 1, 16, 10, 0));
        largeOrder.setOrderStatus(Order.OrderStatus.COMPLETED);
        largeOrder.setPaymentStatus(Order.PaymentStatus.PAID);
        largeOrder.setMembership(testMembership1);
        largeOrder.setStaff(testStaff);

        when(orderRepository.findInRangeAndPaid(any(), any())).thenReturn(Arrays.asList(largeOrder));

        // When
        AnalyticsDTO result = dataAnalyticsReportService.getAnalyticsData(startDate, endDate);

        // Then
        // Should handle very large numbers
        assertEquals(1_000_000_000L, result.totalRevenue());
        assertEquals(1L, result.totalOrders());
        assertEquals(1_000_000_000.0, result.aov(), 0.01);
    }

    @Test
    void testBusinessLogic_VerySmallNumbers() {
        // Given
        LocalDate startDate = LocalDate.of(2024, 1, 15);
        LocalDate endDate = LocalDate.of(2024, 1, 18);

        // Create order with very small total price
        Order smallOrder = new Order();
        smallOrder.setId(10L);
        smallOrder.setTotalPrice(0.01);
        smallOrder.setCreatedAt(LocalDateTime.of(2024, 1, 16, 10, 0));
        smallOrder.setOrderStatus(Order.OrderStatus.COMPLETED);
        smallOrder.setPaymentStatus(Order.PaymentStatus.PAID);
        smallOrder.setMembership(testMembership1);
        smallOrder.setStaff(testStaff);

        when(orderRepository.findInRangeAndPaid(any(), any())).thenReturn(Arrays.asList(smallOrder));

        // When
        AnalyticsDTO result = dataAnalyticsReportService.getAnalyticsData(startDate, endDate);

        // Then
        // Should handle very small numbers with precision
        assertEquals(0L, result.totalRevenue()); // 0.01 becomes 0 when cast to Long
        assertEquals(1L, result.totalOrders());
        assertEquals(0.0, result.aov(), 0.01);
    }
}
