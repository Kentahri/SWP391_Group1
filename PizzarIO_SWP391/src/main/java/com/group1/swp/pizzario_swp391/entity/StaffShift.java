package com.group1.swp.pizzario_swp391.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "[Staff_Shift]")
@Data
@NoArgsConstructor
public class StaffShift {

    @ManyToOne
    @JoinColumn(name = "staff_id")
    private Staff staff;

    @ManyToOne
    @JoinColumn(name = "shift_id")
    private Shift shift;

    public StaffShift(LocalDate workDate, Status status, LocalDateTime checkIn, LocalDateTime checkOut) {
        this.workDate = workDate;
        this.status = status;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "work_date")
    private LocalDate workDate;

    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDateTime checkIn;
    private LocalDateTime checkOut;

    public void setShift(Shift o) {
        this.shift = o;
    }

    public void setStaff(Staff staff) {
        this.staff = staff;
    }

    public enum Status {
        SCHEDULED, PRESENT, LATE, ABSENT, COMPLETED, LEFT_EARLY
    }

    @Column(name = "hourly_wage")
    private int hourlyWage;

    @Column(name = "note", length = 500)
    private String note;

}
