package com.group1.swp.pizzario_swp391.dto.payment;

import com.group1.swp.pizzario_swp391.entity.DiningTable;
import com.group1.swp.pizzario_swp391.entity.Order;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class PaymentPendingMessage {
    private Long sessionId;
    private Long orderId;
    private String tableName;
    private DiningTable.TableStatus status;
    private Double orderTotal;
    private Order.PaymentMethod paymentMethod;
    private String customerName;
    private LocalDateTime requestTime;
    private Order.PaymentStatus paymentStatus;
    private String type; // For WebSocket message type (e.g., "CONFIRMED")
}
