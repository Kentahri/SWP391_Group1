package com.group1.swp.pizzario_swp391.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "Otp_mail")
@NoArgsConstructor
public class OtpEmail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String otpCode;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime expiresAt = this.createAt.plusSeconds(3) ;
    private boolean isUsed = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id", nullable = false)
    private Staff staff;

    public OtpEmail(Staff staff, String otpCode) {
        this.staff = staff;
        this.otpCode = otpCode;
    }

}
