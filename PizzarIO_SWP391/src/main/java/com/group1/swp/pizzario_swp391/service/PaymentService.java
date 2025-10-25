package com.group1.swp.pizzario_swp391.service;

import com.group1.swp.pizzario_swp391.dto.payment.PaymentDTO;
import com.group1.swp.pizzario_swp391.dto.voucher.VoucherDTO;
import com.group1.swp.pizzario_swp391.entity.*;
import com.group1.swp.pizzario_swp391.mapper.PaymentMapper;
import com.group1.swp.pizzario_swp391.repository.OrderRepository;
import com.group1.swp.pizzario_swp391.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final OrderRepository orderRepository;
    private final VoucherRepository voucherRepository;
    private final PaymentMapper paymentMapper;
    private final SessionService sessionService;
    private final MembershipService membershipService;

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

    /**
     * Xác nhận thanh toán và đóng session
     */
    @Transactional(rollbackFor = Exception.class)
    public PaymentDTO confirmPaymentBySessionId(Long sessionId, Order.PaymentMethod paymentMethod) {
        System.out.println("=== PaymentService Debug ===");
        System.out.println("SessionId: " + sessionId);
        System.out.println("PaymentMethod: " + paymentMethod);
        System.out.println("PaymentMethod type: " + (paymentMethod != null ? paymentMethod.getClass().getSimpleName() : "null"));
        
        try {
            Order order = sessionService.getOrderBySessionId(sessionId);
            
            // Tính các giá trị tài chính trước khi đóng session
            double finalOriginalTotal = calculateOriginalOrderTotal(sessionId);
            double finalDiscountAmount = calculateDiscountAmount(sessionId);
            double finalFinalTotal = finalOriginalTotal - finalDiscountAmount;
            
            // Validate paymentMethod before setting
            if (paymentMethod == null) {
                throw new IllegalArgumentException("PaymentMethod cannot be null");
            }
            
            // Debug: Log payment method details
            System.out.println("Setting payment method: " + paymentMethod);
            System.out.println("Payment method name: " + paymentMethod.name());
            System.out.println("Payment method ordinal: " + paymentMethod.ordinal());
            
            // Cập nhật trạng thái thanh toán
            // Lưu payment method thực tế vào database
            order.setPaymentMethod(paymentMethod);
            order.setPaymentStatus(Order.PaymentStatus.PAID);
            order.setUpdatedAt(LocalDateTime.now());
            
            // Cập nhật số lần sử dụng voucher nếu có (không quan trọng, không làm fail transaction chính)
            if (order.getVoucher() != null) {
                try {
                    updateVoucherUsage(order.getVoucher());
                } catch (Exception e) {
                    System.err.println("Error updating voucher usage: " + e.getMessage());
                    // Không throw exception để không làm gián đoạn quá trình thanh toán
                }
            }
            
            // Cập nhật điểm thành viên nếu có (không quan trọng, không làm fail transaction chính)
            if (order.getMembership() != null) {
                try {
                    updateMembershipPointsInNewTransaction(order.getMembership(), finalFinalTotal);
                } catch (Exception e) {
                    System.err.println("Error updating membership points: " + e.getMessage());
                    // Không throw exception để không làm gián đoạn quá trình thanh toán
                }
            }
            
            orderRepository.save(order);
            
            // Đóng session sau khi thanh toán thành công (không quan trọng, không làm fail transaction chính)
            try {
                sessionService.closeSession(sessionId);
            } catch (Exception e) {
                System.err.println("Error closing session: " + e.getMessage());
                // Không throw exception để không làm gián đoạn quá trình thanh toán
            }
            
            PaymentDTO paymentDTO = paymentMapper.toDTO(order);
            
            // Set customer name
            if (order.getMembership() != null) {
                paymentDTO.setCustomerName(order.getMembership().getName());
            } else {
                paymentDTO.setCustomerName("Khách vãng lai");
            }
            
            // Set các giá trị tài chính đã tính trước đó
            paymentDTO.setOriginalTotal(finalOriginalTotal);
            paymentDTO.setDiscountAmount(finalDiscountAmount);
            paymentDTO.setOrderTotal(finalFinalTotal);
            
            // Debug: Log để kiểm tra giá trị
            System.out.println("PaymentDTO paymentMethod: " + paymentDTO.getPaymentMethod());
            
            return paymentDTO;
        } catch (Exception e) {
            System.err.println("Error in confirmPaymentBySessionId: " + e.getMessage());
            e.printStackTrace();
            throw e; // Re-throw để transaction có thể rollback properly
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
        
        if (order.getVoucher() != null) {
            // Luôn tính dựa trên original total từ order items
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
        // Tính điểm tích lũy (ví dụ: 1 điểm cho mỗi 10,000 VND)
        int earnedPoints = (int) (finalTotal / 10000);
        membership.setPoints(membership.getPoints() + earnedPoints);
        System.out.println("Updated membership points in new transaction: " + membership.getPoints());
    }
}
