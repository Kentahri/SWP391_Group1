package com.group1.swp.pizzario_swp391.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservaion")
@Data
@NoArgsConstructor
public class Reservation {

    @ManyToOne
    @JoinColumn(name = "table_id")
    private DiningTable diningTable;

    public Reservation(LocalDateTime startTime, LocalDateTime endTime, LocalDateTime createdAt, Status status, String phone, String name, DiningTable diningTable) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.createdAt = createdAt;
        this.status = status;
        this.phone = phone;
        this.name = name;
        this.diningTable = diningTable;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(nullable = false)
    private Status status;

    @Column(nullable = false)
    private String phone;

    @Column(columnDefinition = "NVARCHAR(256)", nullable = false)
    private String name;

    public enum Status {
        CONFIRMED, CANCELLED
    }

}
