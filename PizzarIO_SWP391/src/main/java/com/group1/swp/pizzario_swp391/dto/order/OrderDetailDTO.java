package com.group1.swp.pizzario_swp391.dto.order;
import java.time.LocalDateTime;
import java.util.List;
import com.group1.swp.pizzario_swp391.entity.Order.OrderStatus;
import com.group1.swp.pizzario_swp391.entity.Order.OrderType;
import com.group1.swp.pizzario_swp391.entity.Order.PaymentMethod;
import com.group1.swp.pizzario_swp391.entity.Order.PaymentStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * DTO cho Order detail để hiển thị trong cashier dashboard
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderDetailDTO {
    Long orderId;
    Long sessionId;
    Integer tableId;
    String tableName;
    OrderStatus orderStatus;
    OrderType orderType;
    PaymentStatus paymentStatus;
    PaymentMethod paymentMethod;
    double totalPrice;
    double taxRate;
    String note;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    List<OrderItemDTO> items;
    String createdByStaffName;
    String voucherCode;
    Double discountAmount;
    String customerName; // Tên khách hàng (từ membership hoặc "Khách vãng lai")
    String customerPhone; // Số điện thoại khách hàng (nếu có membership)
}


