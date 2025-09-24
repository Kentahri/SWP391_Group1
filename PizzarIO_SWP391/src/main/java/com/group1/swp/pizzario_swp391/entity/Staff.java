package com.group1.swp.pizzario_swp391.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "Staff")
@Data
@ToString
@NoArgsConstructor
public class Staff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;
    private LocalDateTime dateOfBirth;
    private String phone;
    private String address;
    private String username;
    private String password;
    private String email;

    @Enumerated(EnumType.STRING)
    private Role role;
    private boolean isActive;

    @OneToMany(mappedBy = "staff", cascade = CascadeType.ALL)
    private List<OtpMail> otpMails;

    public Staff(String name, LocalDateTime dateOfBirth, String phone, String address, String username, String password, String email, Role role, boolean isActive) {
        this();
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.phone = phone;
        this.address = address;
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
        this.isActive = isActive;
    }

    public enum Role {
        CASHIER, MANAGER, KITCHEN
    }

    public void addOtp(OtpMail otp){
        if(otpMails == null){
            otpMails = new ArrayList<>();
        }

        otpMails.add(otp);
        otp.setStaff(this);
    }
}
