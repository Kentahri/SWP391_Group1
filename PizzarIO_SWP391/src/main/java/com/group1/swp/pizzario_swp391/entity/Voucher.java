package com.group1.swp.pizzario_swp391.entity;

import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "[Voucher]")
@Data
@ToString
public class Voucher {

    @OneToMany(mappedBy = "voucher")
    private List<Order> orders;

    public Voucher() {
        orders = new ArrayList<>();
    }

    public void addOrder(Order order) {
        orders.add(order);
        order.setVoucher(this);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    private String code;
    private VoucherType type;
    private double value;
    private String description;

    @Column(name = "max_uses", nullable = false)
    private int maxUses;

    @Column(name = "times_used", nullable = false)
    private int timesUsed;

    @Column(name = "min_order_amount", nullable = false)
    private double minOrderAmount;

    @Column(name = "valid_from", nullable = false)
    private LocalDateTime validFrom;

    @Column(name = "valid_to", nullable = false)
    private LocalDateTime validTo;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    public enum VoucherType {
        PERCENTAGE, FIXED_AMOUNT
    }

    public Voucher(String code, VoucherType type, double value, String description, int maxUses, int timesUsed, double minOrderAmount, LocalDateTime validFrom, LocalDateTime validTo, boolean isActive) {
        this.code = code;
        this.type = type;
        this.value = value;
        this.description = description;
        this.maxUses = maxUses;
        this.timesUsed = timesUsed;
        this.minOrderAmount = minOrderAmount;
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.isActive = isActive;
    }
    
}
