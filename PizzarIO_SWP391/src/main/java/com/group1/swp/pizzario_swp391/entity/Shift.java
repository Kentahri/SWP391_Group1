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

    @OneToMany(mappedBy = "shift")
    private List<StaffShift> staffShifts;

    public void addShift(StaffShift shift) {
        staffShifts.add(shift);
        shift.setShift(this);
    }

    public void removeShift(StaffShift shift) {
        staffShifts.remove(shift);
        shift.setShift(null);
    }

    public Shift() {
        staffShifts = new ArrayList<>();
    }

    public Shift(ShiftType shiftName, LocalDateTime startTime, LocalDateTime endTime, LocalDateTime createdAt) {
        this.shiftName = shiftName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.createdAt = createdAt;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "NVARCHAR(50)")
    private ShiftType shiftName;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime createdAt;

    public enum ShiftType {
        SANG, CHIEU, TOI
    }
}
