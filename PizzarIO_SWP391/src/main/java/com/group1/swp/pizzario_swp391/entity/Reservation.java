package com.group1.swp.pizzario_swp391.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reservaion")
@Data
@NoArgsConstructor
public class Reservation {

    @ManyToOne
    @JoinColumn(name = "table_id")
    private DiningTable diningTable;

    public Reservation(LocalDateTime startTime, LocalDateTime createdAt, Status status, String phone, String name, int capacityExpected, String note, DiningTable diningTable) {
        this.startTime = startTime;
        this.createdAt = createdAt;
        this.status = status;
        this.phone = phone;
        this.name = name;
        this.note = note;
        this.capacityExpected = capacityExpected;
        this.diningTable = diningTable;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(nullable = false)
    private String phone;

    @Column(columnDefinition = "NVARCHAR(256)", nullable = false)
    private String name;
    
    private int capacityExpected;
    
    @Column(columnDefinition = "NVARCHAR(500)")
    private String note;
    public enum Status {
        CONFIRMED, CANCELLED
    }

}
