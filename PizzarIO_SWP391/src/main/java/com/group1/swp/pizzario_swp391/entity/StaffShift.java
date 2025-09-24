package com.group1.swp.pizzario_swp391.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "Staff_Shift")
@Data
@NoArgsConstructor
public class StaffShift {

    @ManyToOne
    @JoinColumn(name = "staff_id")
    private Staff staff;

    @ManyToOne
    @JoinColumn(name = "shift_id")
    private Shift shift;

    public StaffShift(LocalDateTime workDate, Status status, LocalDateTime checkIn, LocalDateTime checkOut) {
        this.workDate = workDate;
        this.status = status;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "work_date")
    private LocalDateTime workDate;

    private Status status;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;

    public enum Status{
        SCHEDULED, PRESENT, LATE, ABSENT, LEFT
    }
}
