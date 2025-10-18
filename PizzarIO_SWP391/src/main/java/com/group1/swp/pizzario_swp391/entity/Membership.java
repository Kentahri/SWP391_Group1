package com.group1.swp.pizzario_swp391.entity;

import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "[Membership]")
@Data
public class Membership {

    @OneToMany(mappedBy = "membership")
    private List<Order> orders;

    public void addOrder(Order order) {
        orders.add(order);
        order.setMembership(this);
    }

    public Membership() {
        orders = new ArrayList<>();
    }

    public Membership(String phoneNumber, String name, int points, boolean isActive, LocalDateTime joinedAt) {
        this();
        this.phoneNumber = phoneNumber;
        this.name = name;
        this.points = points;
        this.isActive = isActive;
        this.joinedAt = joinedAt;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "phone", unique = true, nullable = false)
    private String phoneNumber;

    @Column(columnDefinition = "NVARCHAR(100)")
    private String name;
    private int points;

    @Column(name = "is_active")
    private boolean isActive;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

}
