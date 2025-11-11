package com.group1.swp.pizzario_swp391.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.group1.swp.pizzario_swp391.dto.websocket.TableStatusMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.group1.swp.pizzario_swp391.dto.payment.PaymentDTO;
import com.group1.swp.pizzario_swp391.dto.payment.PaymentPendingMessage;
import com.group1.swp.pizzario_swp391.dto.voucher.VoucherDTO;
import com.group1.swp.pizzario_swp391.dto.websocket.KitchenOrderMessage;
import com.group1.swp.pizzario_swp391.entity.DiningTable;
import com.group1.swp.pizzario_swp391.entity.Membership;
import com.group1.swp.pizzario_swp391.entity.Order;
import com.group1.swp.pizzario_swp391.entity.OrderItem;
import com.group1.swp.pizzario_swp391.entity.Voucher;
import com.group1.swp.pizzario_swp391.mapper.PaymentMapper;
import com.group1.swp.pizzario_swp391.repository.OrderRepository;
import com.group1.swp.pizzario_swp391.repository.VoucherRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final OrderRepository orderRepository;
    private final VoucherRepository voucherRepository;
    private final PaymentMapper paymentMapper;
    private final SessionService sessionService;
    private final com.group1.swp.pizzario_swp391.repository.TableRepository tableRepository;
    private final MembershipService membershipService;
    private final WebSocketService webSocketService;

    // Track used points per session (in-memory)
    private final Map<Long, Integer> pointsUsedBySession = new ConcurrentHashMap<>();

    /**
     * Lấy thông tin payment từ session ID
     */
    public PaymentDTO getPaymentBySessionId(Long sessionId) {
        // Kiểm tra session có đang mở không
        if (!sessionService.isSessionOpen(sessionId)) {
            throw new RuntimeException("Session đã được đóng");
        }

        Order order = sessionService.getOrderBySessionId(sessionId);

        PaymentDTO paymentDTO = paymentMapper.toDTO(order);

        // Set customer name (có thể lấy từ membership hoặc để mặc định)
        if (order.getMembership() != null) {
            paymentDTO.setCustomerName(order.getMembership().getName());
        } else {
            paymentDTO.setCustomerName("Khách vãng lai");
        }

        // Tính và set các giá trị tài chính
        double originalTotal = calculateOriginalOrderTotal(sessionId);
        double discountAmount = calculateDiscountAmount(sessionId);
        double finalTotal = originalTotal - discountAmount;

        paymentDTO.setOriginalTotal(originalTotal);
        paymentDTO.setDiscountAmount(discountAmount);
        paymentDTO.setOrderTotal(finalTotal);

        // Load available vouchers
        paymentDTO.setAvailableVouchers(getAvailableVouchersBySessionId(sessionId));

        // Set pointsUsed
        int pointsUsed = getPointsUsed(sessionId);
        System.out.println("=== PaymentService.getPaymentBySessionId Debug ===");
        System.out.println("sessionId: " + sessionId);
        System.out.println("pointsUsedBySession map contains key: " + pointsUsedBySession.containsKey(sessionId));
        System.out.println("pointsUsedBySession.get(sessionId): " + pointsUsedBySession.get(sessionId));
        System.out.println("getPointsUsed(sessionId) returns: " + pointsUsed);
        paymentDTO.setPointsUsed(pointsUsed);
        System.out.println("paymentDTO.getPointsUsed() after set: " + paymentDTO.getPointsUsed());

        return paymentDTO;
    }

    /**
     * Lấy thông tin payment từ session ID cho trang confirmation (không cần kiểm tra session mở)
     */
    public PaymentDTO getPaymentConfirmationBySessionId(Long sessionId) {
        // Lấy order từ session (không cần kiểm tra session mở vì đã thanh toán)
        Order order = sessionService.getOrderBySessionId(sessionId);

        PaymentDTO paymentDTO = paymentMapper.toDTO(order);

        // Set customer name (có thể lấy từ membership hoặc để mặc định)
        if (order.getMembership() != null) {
            paymentDTO.setCustomerName(order.getMembership().getName());
        } else {
            paymentDTO.setCustomerName("Khách vãng lai");
        }

        // Tính và set các giá trị tài chính
        double originalTotal = calculateOriginalOrderTotal(sessionId);
        double discountAmount = calculateDiscountAmount(sessionId);
        double finalTotal = originalTotal - discountAmount;

        paymentDTO.setOriginalTotal(originalTotal);
        paymentDTO.setDiscountAmount(discountAmount);
        paymentDTO.setOrderTotal(finalTotal);

        return paymentDTO;
    }

    /**
     * Lấy danh sách order items cho session
     */
    public List<OrderItem> getOrderItemsBySessionId(Long sessionId) {
        Order order = sessionService.getOrderBySessionId(sessionId);
        return order.getOrderItems();
    }

    /**
     * Lấy danh sách voucher có thể áp dụng cho session
     */
    public List<VoucherDTO> getAvailableVouchersBySessionId(Long sessionId) {
        // Sử dụng tổng gốc để kiểm tra điều kiện voucher
        double originalOrderTotal = calculateOriginalOrderTotal(sessionId);

        return voucherRepository.findAll().stream()
                .filter(voucher -> voucher.isActive())
                .filter(voucher -> voucher.getValidFrom().isBefore(LocalDateTime.now()))
                .filter(voucher -> voucher.getValidTo().isAfter(LocalDateTime.now()))
                .filter(voucher -> voucher.getTimesUsed() < voucher.getMaxUses())
                .filter(voucher -> voucher.getMinOrderAmount() <= originalOrderTotal)
                .map(voucher -> {
                    VoucherDTO dto = new VoucherDTO();
                    dto.setId(voucher.getId());
                    dto.setCode(voucher.getCode());
                    dto.setType(voucher.getType());
                    dto.setValue(voucher.getValue());
                    dto.setDescription(voucher.getDescription());
                    dto.setMinOrderAmount(voucher.getMinOrderAmount());
                    dto.setValidFrom(voucher.getValidFrom());
                    dto.setValidTo(voucher.getValidTo());
                    dto.setActive(voucher.isActive());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * Áp dụng voucher cho session (ghi đè voucher cũ nếu có)
     */
    @Transactional(rollbackFor = Exception.class)
    public PaymentDTO applyVoucherBySessionId(Long sessionId, Long voucherId) {
        Order order = sessionService.getOrderBySessionId(sessionId);

        Voucher voucher = voucherRepository.findById(voucherId)
                .orElseThrow(() -> new RuntimeException("Voucher not found with id: " + voucherId));

        // Tính tổng giá trị order gốc (trước khi áp dụng voucher)
        double originalTotal = calculateOriginalOrderTotal(sessionId);

        // Kiểm tra điều kiện áp dụng voucher dựa trên tổng gốc
        if (!voucher.isActive() ||
                voucher.getValidFrom().isAfter(LocalDateTime.now()) ||
                voucher.getValidTo().isBefore(LocalDateTime.now()) ||
                voucher.getTimesUsed() >= voucher.getMaxUses() ||
                voucher.getMinOrderAmount() > originalTotal) {
            throw new RuntimeException("Voucher không thể áp dụng");
        }

        // Khi áp dụng voucher, ghi đè trạng thái sử dụng điểm (chỉ 1 phương thức giảm giá)
        pointsUsedBySession.remove(sessionId);
        // Ghi đè voucher cũ (nếu có) bằng voucher mới
        order.setVoucher(voucher);

        // KHÔNG thay đổi totalPrice của order - giữ nguyên giá gốc từ order items
        // Discount sẽ được tính riêng trong DTO

        orderRepository.save(order);

        PaymentDTO paymentDTO = paymentMapper.toDTO(order);

        // Set customer name
        if (order.getMembership() != null) {
            paymentDTO.setCustomerName(order.getMembership().getName());
        } else {
            paymentDTO.setCustomerName("Khách vãng lai");
        }

        // Tính và set các giá trị tài chính
        double newOriginalTotal = calculateOriginalOrderTotal(sessionId);
        double newDiscountAmount = calculateDiscountAmount(sessionId);
        double newFinalTotal = newOriginalTotal - newDiscountAmount;

        paymentDTO.setOriginalTotal(newOriginalTotal);
        paymentDTO.setDiscountAmount(newDiscountAmount);
        paymentDTO.setOrderTotal(newFinalTotal);

        return paymentDTO;
    }

    /**
     * Hủy áp dụng voucher cho session
     */
    @Transactional(rollbackFor = Exception.class)
    public PaymentDTO removeVoucherBySessionId(Long sessionId) {
        Order order = sessionService.getOrderBySessionId(sessionId);

        if (order.getVoucher() != null) {
            // Chỉ hủy voucher, không thay đổi totalPrice
            order.setVoucher(null);
            orderRepository.save(order);
        }

        PaymentDTO paymentDTO = paymentMapper.toDTO(order);

        // Set customer name
        if (order.getMembership() != null) {
            paymentDTO.setCustomerName(order.getMembership().getName());
        } else {
            paymentDTO.setCustomerName("Khách vãng lai");
        }

        // Tính và set các giá trị tài chính
        double newOriginalTotal = calculateOriginalOrderTotal(sessionId);
        double newDiscountAmount = calculateDiscountAmount(sessionId);
        double newFinalTotal = newOriginalTotal - newDiscountAmount;

        paymentDTO.setOriginalTotal(newOriginalTotal);
        paymentDTO.setDiscountAmount(newDiscountAmount);
        paymentDTO.setOrderTotal(newFinalTotal);

        return paymentDTO;
    }

    @Transactional
    public void waitingConfirmPayment(Long sessionId, Order.PaymentMethod paymentMethod) {
        Order order = sessionService.getOrderBySessionId(sessionId);
        DiningTable table = order.getSession().getTable();
        DiningTable.TableStatus currentTableStatus = table.getTableStatus();
        double finalOriginalTotal = calculateOriginalOrderTotal(sessionId);
        double finalDiscountAmount = calculateDiscountAmount(sessionId);
        double finalFinalTotal = finalOriginalTotal - finalDiscountAmount;

        if (paymentMethod == null) {
            throw new IllegalArgumentException("PaymentMethod cannot be null");
        }

        order.setPaymentMethod(paymentMethod);
        order.setPaymentStatus(Order.PaymentStatus.PENDING);
        order.setUpdatedAt(LocalDateTime.now());

        orderRepository.save(order);

        // Cập nhật trạng thái bàn sang WAITING_PAYMENT và lưu DB để UI và trang chi tiết đồng bộ
        try {
            if (table.getTableStatus() != DiningTable.TableStatus.WAITING_PAYMENT) {
                table.setTableStatus(DiningTable.TableStatus.WAITING_PAYMENT);
                tableRepository.save(table);
            }
        } catch (Exception ignored) {
        }

        PaymentPendingMessage message = PaymentPendingMessage.builder()
                .sessionId(sessionId)
                .orderId(order.getId())
                .tableName("Bàn " + order.getSession().getTable().getId())
                .status(DiningTable.TableStatus.WAITING_PAYMENT)
                .orderTotal(finalFinalTotal)
                .paymentMethod(paymentMethod)
                .customerName(order.getMembership() != null ? order.getMembership().getName() : "Khách vãng lai")
                .requestTime(LocalDateTime.now())
                .paymentStatus(Order.PaymentStatus.PENDING)
                .type("PAYMENT_PENDING")
                .build();

        webSocketService.broadcastPaymentPendingToCashier(message);
        webSocketService.broadcastTableStatusToCashier(
                TableStatusMessage.MessageType.TABLE_RELEASED,
                table.getId(),
                currentTableStatus,
                DiningTable.TableStatus.WAITING_PAYMENT,
                "GUEST",
                "Bàn " + table.getId() + " đang chờ thanh toán");
    }

    /**
     * Xác nhận thanh toán và gửi thông báo đến Cashier(Chờ cashier xác nhận)
     */
    @Transactional(rollbackFor = Exception.class)
    public void confirmPaymentBySessionId(Long sessionId, Order.PaymentMethod paymentMethod) {

        try {
            Order order = sessionService.getOrderBySessionId(sessionId);

            double finalOriginalTotal = calculateOriginalOrderTotal(sessionId);
            double finalDiscountAmount = calculateDiscountAmount(sessionId);
            double finalFinalTotal = finalOriginalTotal - finalDiscountAmount;

            if (paymentMethod == null) {
                throw new IllegalArgumentException("PaymentMethod cannot be null");
            }

            // Cập nhật trạng thái thanh toán
            // Lưu payment method thực tế vào database
            order.setPaymentMethod(paymentMethod);
            order.setPaymentStatus(Order.PaymentStatus.PAID);
            order.setOrderStatus(Order.OrderStatus.COMPLETED);
            order.setUpdatedAt(LocalDateTime.now());

            // Cập nhật số lần sử dụng voucher nếu có (không quan trọng, không làm fail transaction chính)
            if (order.getVoucher() != null) {
                try {
                    updateVoucherUsage(order.getVoucher());
                } catch (Exception e) {
                    System.err.println("Error updating voucher usage: " + e.getMessage());
                }
            }

            // Cập nhật điểm thành viên nếu có (không quan trọng, không làm fail transaction chính)
            if (order.getMembership() != null) {
                try {
                    // Deduct used points first
                    int usedPoints = getPointsUsed(sessionId);
                    if (usedPoints > 0) {
                        Membership m = order.getMembership();
                        int current = m.getPoints() != null ? m.getPoints() : 0;
                        m.setPoints(Math.max(0, current - usedPoints));
                        // Persist via membershipService
                        membershipService.save(m);
                    }
                    updateMembershipPointsInNewTransaction(order.getMembership(), finalFinalTotal);
                } catch (Exception e) {
                    System.err.println("Error updating membership points: " + e.getMessage());
                }
            }

            orderRepository.save(order);

            // Gửi thông báo đến kitchen khi order đã hoàn thành (để dashboard refresh)
            try {
                notifyKitchenOrderCompleted(order);
            } catch (Exception e) {
                System.err.println("Error notifying kitchen of completed order: " + e.getMessage());
            }

            // Đóng session sau khi thanh toán thành công (không quan trọng, không làm fail transaction chính)
            try {
                sessionService.closeSession(sessionId);
            } catch (Exception e) {
                System.err.println("Error closing session: " + e.getMessage());
            }

            var paymentDTO = getPaymentConfirmationBySessionId(sessionId);

            PaymentPendingMessage successMsg = PaymentPendingMessage.builder()
                    .sessionId(sessionId)
                    .orderId(paymentDTO.getOrderId())
                    .tableName("Bàn " + paymentDTO.getTableNumber())
                    .paymentStatus(Order.PaymentStatus.PAID)
                    .paymentMethod(paymentMethod)
                    .customerName(paymentDTO.getCustomerName())
                    .orderTotal(paymentDTO.getOrderTotal())
                    .type("CONFIRMED") // Thêm type để frontend biết thanh toán đã được confirm
                    .build();
            webSocketService.sendPaymentConfirmationToGuest(sessionId, successMsg);
        } catch (Exception e) {
            System.err.println("Error in confirmPaymentBySessionId: " + e.getMessage());
            e.printStackTrace();
            throw e; // Re-throw để transaction có thể rollback properly
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void confirmPaymentTakeawayBySessionId(Long sessionId, Order.PaymentMethod paymentMethod) {

        try {
            Order order = sessionService.getOrderBySessionId(sessionId);

            double finalOriginalTotal = calculateOriginalOrderTotal(sessionId);
            double finalDiscountAmount = calculateDiscountAmount(sessionId);
            double finalFinalTotal = finalOriginalTotal - finalDiscountAmount;

            if (paymentMethod == null) {
                throw new IllegalArgumentException("PaymentMethod cannot be null");
            }

            order.setPaymentMethod(paymentMethod);
            order.setPaymentStatus(Order.PaymentStatus.PAID);
            order.setUpdatedAt(LocalDateTime.now());

            if (order.getVoucher() != null) {
                try {
                    updateVoucherUsage(order.getVoucher());
                } catch (Exception e) {
                    System.err.println("Error updating voucher usage: " + e.getMessage());
                }
            }

            if (order.getMembership() != null) {
                try {
                    int usedPoints = getPointsUsed(sessionId);
                    if (usedPoints > 0) {
                        Membership m = order.getMembership();
                            int current = m.getPoints() != null ? m.getPoints() : 0;
                        m.setPoints(Math.max(0, current - usedPoints));
                        // Persist via membershipService
                        membershipService.save(m);
                    }
                    updateMembershipPointsInNewTransaction(order.getMembership(), finalFinalTotal);
                } catch (Exception e) {
                    System.err.println("Error updating membership points: " + e.getMessage());
                }
            }

            orderRepository.save(order);

        } catch (Exception e) {
            System.err.println("Error in confirmPaymentTakeawayBySessionId: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Lấy thông tin membership từ session
     */
    public Membership getMembershipBySessionId(Long sessionId) {
        Order order = sessionService.getOrderBySessionId(sessionId);
        return order.getMembership();
    }

    /**
     * Tìm membership theo số điện thoại
     */
    public Membership findMembershipByPhoneNumber(String phoneNumber) {
        return membershipService.findEntityByPhone(phoneNumber).orElse(null);
    }

    /**
     * Gán membership vào order theo session ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void assignMembershipToSession(Long sessionId, Long membershipId) {
        Order order = sessionService.getOrderBySessionId(sessionId);
        // Tìm membership theo ID thay vì phone number
        Membership membership = membershipService.findEntityById(membershipId);
        if (membership != null) {
            order.setMembership(membership);
            orderRepository.save(order);
        }
    }

    /**
     * Tính tổng giá trị order gốc từ order items (trước khi áp dụng voucher)
     */
    public double calculateOriginalOrderTotal(Long sessionId) {
        Order order = sessionService.getOrderBySessionId(sessionId);

        // Luôn tính tổng từ order items để đảm bảo giá trị chính xác
        double totalFromItems = order.getOrderItems().stream()
                .mapToDouble(OrderItem::getTotalPrice)
                .sum();

        // Cập nhật totalPrice của order nếu cần
        if (order.getTotalPrice() != totalFromItems) {
            order.setTotalPrice(totalFromItems);
            orderRepository.save(order);
        }

        return totalFromItems;
    }

    /**
     * Tính số tiền giảm giá hiện tại từ voucher đã áp dụng
     */
    public double calculateDiscountAmount(Long sessionId) {
        Order order = sessionService.getOrderBySessionId(sessionId);

        // Points discount takes precedence if used (and vouchers are mutually exclusive via validation)
        int usedPoints = getPointsUsed(sessionId);
        if (usedPoints > 0) {
            double originalTotal = calculateOriginalOrderTotal(sessionId);
            double maxDiscount = originalTotal; // cannot exceed original total
            double pointsDiscount = usedPoints * 10000.0;
            return Math.min(pointsDiscount, maxDiscount);
        }

        if (order.getVoucher() != null) {
            double originalTotal = calculateOriginalOrderTotal(sessionId);
            return calculateDiscountAmount(originalTotal, order.getVoucher());
        }

        return 0.0;
    }

    /**
     * Tính số tiền giảm giá từ voucher
     */
    private double calculateDiscountAmount(double orderTotal, Voucher voucher) {
        if (voucher.getType() == Voucher.VoucherType.PERCENTAGE) {
            return orderTotal * (voucher.getValue() / 100);
        } else {
            return Math.min(voucher.getValue(), orderTotal);
        }
    }


    /**
     * Cập nhật số lần sử dụng voucher (method riêng để tránh nested transaction)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = Exception.class)
    public void updateVoucherUsage(Voucher voucher) {
        voucher.setTimesUsed(voucher.getTimesUsed() + 1);
        voucherRepository.save(voucher);
        System.out.println("Updated voucher usage: " + voucher.getTimesUsed());
    }

    /**
     * Cập nhật điểm thành viên trong transaction mới (tránh nested transaction)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = Exception.class)
    public void updateMembershipPointsInNewTransaction(Membership membership, double finalTotal) {
        // Tính điểm tích lũy (1 điểm cho mỗi 100,000 VND)
        int earnedPoints = (int) (finalTotal / 100000);
        membership.setPoints(membership.getPoints() + earnedPoints);
        System.out.println("Updated membership points in new transaction: " + membership.getPoints());
    }

    // ===== Points usage APIs =====
    public int getPointsUsed(Long sessionId) {
        return pointsUsedBySession.getOrDefault(sessionId, 0);
    }

    public int getMaxUsablePoints(Long sessionId) {
        Order order = sessionService.getOrderBySessionId(sessionId);
        if (order.getMembership() == null) return 0;
        int memberPoints = order.getMembership().getPoints() != null ? order.getMembership().getPoints() : 0;
        double originalTotal = calculateOriginalOrderTotal(sessionId);
        int byAmountCap = (int) Math.floor(originalTotal / 10000.0);
        return Math.max(0, Math.min(memberPoints, byAmountCap));
    }

    @Transactional(rollbackFor = Exception.class)
    public void applyPoints(Long sessionId, int points) {
        Order order = sessionService.getOrderBySessionId(sessionId);
        if (order.getMembership() == null) {
            throw new RuntimeException("Vui lòng xác thực thành viên trước khi sử dụng điểm");
        }
        // Nếu đang có voucher, hủy voucher để điểm là phương thức giảm giá duy nhất
        if (order.getVoucher() != null) {
            order.setVoucher(null);
            orderRepository.save(order);
        }
        if (points < 0) {
            throw new RuntimeException("Số điểm không hợp lệ");
        }
        int maxUsable = getMaxUsablePoints(sessionId);
        if (points > maxUsable) {
            throw new RuntimeException("Số điểm vượt quá mức cho phép: tối đa " + maxUsable);
        }
        pointsUsedBySession.put(sessionId, points);
    }

    public void removePoints(Long sessionId) {
        pointsUsedBySession.remove(sessionId);
    }

    /**
     * Gửi thông báo đến kitchen khi order đã hoàn thành
     * Kitchen-dashboard sẽ tự động refresh để order này biến mất khỏi danh sách
     */
    private void notifyKitchenOrderCompleted(Order order) {
        try {
            // Lấy danh sách items để tính toán
            List<OrderItem> orderItems = order.getOrderItems();
            int totalItems = orderItems != null ? orderItems.size() : 0;
            int completedItems = orderItems != null
                    ? (int) orderItems.stream()
                    .filter(item -> item.getOrderItemStatus() == OrderItem.OrderItemStatus.SERVED)
                    .count()
                    : 0;

            KitchenOrderMessage completedMessage = KitchenOrderMessage.builder()
                    .type(KitchenOrderMessage.MessageType.ORDER_COMPLETED)
                    .orderId(order.getId())
                    .code(String.format("ORD-%05d", order.getId()))
                    .tableName(order.getSession() != null && order.getSession().getTable() != null
                            ? "Bàn " + order.getSession().getTable().getId()
                            : "Take away")
                    .orderType(order.getOrderType() != null ? order.getOrderType().toString() : "DINE_IN")
                    .status(order.getOrderStatus() != null ? order.getOrderStatus().toString() : "COMPLETED")
                    .priority("NORMAL")
                    .totalPrice(order.getTotalPrice())
                    .totalItems(totalItems)
                    .completedItems(completedItems)
                    .note(order.getNote())
                    .message("Order " + String.format("ORD-%05d", order.getId()) + " đã hoàn thành và đã thanh toán")
                    .build();

            webSocketService.broadcastNewOrderToKitchen(completedMessage);
        } catch (Exception e) {
            System.err.println("Error notifying kitchen of completed order: " + e.getMessage());
        }
    }
}
