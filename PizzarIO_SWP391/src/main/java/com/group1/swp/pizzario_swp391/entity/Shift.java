package com.group1.swp.pizzario_swp391.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "[Shift]")
@Data
public class Shift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "NVARCHAR(50)")
    private ShiftType shiftName;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime createdAt;

    private double salaryPerShift;

    @OneToMany(mappedBy = "shift")
    private List<StaffShift> staffShifts = new ArrayList<>();

    public Shift() {}

    public Shift(ShiftType shiftName, LocalDateTime startTime, LocalDateTime endTime, LocalDateTime createdAt) {
        this.shiftName = shiftName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.createdAt = createdAt;
        setSalaryByShiftType(shiftName);
    }

    public enum ShiftType {
        SÁNG, CHIỀU, TỐI
    }

    public void setShiftName(ShiftType shiftName) {
        this.shiftName = shiftName;
        setSalaryByShiftType(shiftName);
    }

    private void setSalaryByShiftType(ShiftType type) {
        switch (type) {
            case SÁNG -> this.salaryPerShift = 150000;
            case CHIỀU -> this.salaryPerShift = 150000;
            case TỐI -> this.salaryPerShift = 200000;
            default -> this.salaryPerShift = 0;
        }
    }
}
