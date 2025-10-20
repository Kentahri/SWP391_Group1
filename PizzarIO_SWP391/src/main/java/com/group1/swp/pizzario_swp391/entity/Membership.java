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

    public Membership(String phoneNumber, String name, boolean isActive, LocalDateTime joinedAt, int points) {
        this();
        this.phoneNumber = phoneNumber;
        this.name = name;
        this.isActive = isActive;
        this.joinedAt = joinedAt;
        this.points = points;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "phone", unique = true, nullable = false)
    private String phoneNumber;

    private String name;

    @Column(name = "is_active")
    private boolean isActive;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    @Column(name = "points", nullable = false)
    private Integer points;

}
