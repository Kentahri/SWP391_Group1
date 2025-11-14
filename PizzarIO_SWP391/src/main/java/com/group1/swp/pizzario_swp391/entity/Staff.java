package com.group1.swp.pizzario_swp391.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "[Staff]")
@Data
@ToString
public class Staff {

    @OneToMany(mappedBy = "staff", cascade = CascadeType.ALL)
    private List<OtpMail> otpMails;

    public void addOtpMail(OtpMail otpMail) {
        if (otpMails == null) {
            otpMails = new ArrayList<>();
        }
        otpMails.add(otpMail);
        otpMail.setStaff(this);
    }

    @OneToMany(mappedBy = "staff")
    private List<StaffShift> shifts;


    @OneToMany(mappedBy = "staff")
    private List<Order> orders;

    public Staff() {
        shifts = new ArrayList<>();
        otpMails = new ArrayList<>();
        orders = new ArrayList<>();
    }

    public Staff(String name, LocalDate dateOfBirth, String phone, String address, String password, String email,
            Role role, boolean isActive) {
        this();
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.phone = phone;
        this.address = address;
        this.password = password;
        this.email = email;
        this.role = role;
        this.isActive = isActive;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(columnDefinition = "NVARCHAR(50)")
    private String name;

    @Column(name = "dob")
    private LocalDate dateOfBirth;

    private String phone;

    @Column(columnDefinition = "NVARCHAR(256)")
    private String address;
    private String password;
    private String email;
    @Enumerated(EnumType.STRING)
    private Role role;
    private boolean isActive;

    public enum Role {
        CASHIER, MANAGER, KITCHEN
    }
}