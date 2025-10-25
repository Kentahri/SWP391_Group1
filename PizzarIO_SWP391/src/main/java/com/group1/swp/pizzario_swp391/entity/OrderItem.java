package com.group1.swp.pizzario_swp391.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "[Order_Item]")
@Data
@ToString(exclude = {"order"})
@NoArgsConstructor
public class OrderItem {

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "unit_price")
    private double unitPrice;
    private int quantity;
    @Column(columnDefinition = "NVARCHAR(256)")
    private String note;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_item_status")
    private OrderItemStatus orderItemStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_item_type")
    private OrderItemType orderItemType;

    @Column(name = "total_price")
    private double totalPrice;

    public enum OrderItemStatus {
        PENDING, PREPARING, SERVED, CANCELLED
    }

    public enum OrderItemType {
        DINE_IN, TAKE_AWAY
    }

    public OrderItem(double unitPrice, int quantity, String note, OrderItemStatus orderItemStatus, OrderItemType orderItemType, double totalPrice) {
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.note = note;
        this.orderItemStatus = orderItemStatus;
        this.orderItemType = orderItemType;
        this.totalPrice = totalPrice;
    }
}
