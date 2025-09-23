package com.group1.swp.pizzario_swp391.entity;

import jakarta.persistence.*;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Membership")
@Data
@ToString
@NoArgsConstructor
public class Membership {

    @OneToMany(mappedBy = "")
    private List<Order> orders;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "phone", unique = true, nullable = false)
    private String phoneNumber;

    private String name;

    private String email;

    @Column(name = "membership_tier")
    private MembershipTier membershipTier;

    private int points;

    @Column(name = "is_active")
    private boolean isActive;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    public enum MembershipTier{

    }
}
