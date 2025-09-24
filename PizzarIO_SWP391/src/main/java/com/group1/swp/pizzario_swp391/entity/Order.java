package com.group1.swp.pizzario_swp391.entity;

import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Order")
@Data
public class Order {

    @ManyToOne
    @JoinColumn(name = "created_by")
    private Staff staff;

    @OneToOne(mappedBy = "order")
    private Session session;

    @ManyToOne
    @JoinColumn(name = "voucher_id")
    private Voucher voucher;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Membership membership;

    @OneToMany(mappedBy = "order")
    private List<OrderItem> orderItems;

    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    public void removeOrderItem(OrderItem orderItem) {
        orderItems.remove(orderItem);
        orderItem.setOrder(null);
    }

    public Order() {
        orderItems = new ArrayList<OrderItem>();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String note;

    @Column(name = "total_price")
    private double totalPrice;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "order_status")
    private OrderStatus orderStatus;

    @Column(name = "order_type")
    private OrderType orderType;

    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;

    @Column(name = "payment_status")
    private PaymentStatus paymentStatus;

    @Column(name = "tax-rate")
    private double taxRate;

    public void setVoucher(Voucher voucher) {
    }

    public void setMembership(Membership membership) {
    }

    public void setStaff(Staff staff) {
    }

    public enum OrderType {
        DINE_IN, TAKE_AWAY
    }

    public enum OrderStatus {
        PREPARING, SERVED, COMPLETED, CANCELLED
    }

    public enum PaymentStatus{
        UNPAID, PENDING, PAID
    }

    public enum PaymentMethod{
        QR, CASH, CREDIT_CARD
    }


}
