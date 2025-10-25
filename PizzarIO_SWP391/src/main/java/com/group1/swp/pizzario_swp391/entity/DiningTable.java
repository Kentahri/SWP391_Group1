package com.group1.swp.pizzario_swp391.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.OptimisticLockType;
import org.hibernate.annotations.OptimisticLocking;

@Entity
@Table(name = "[Dining_Table]")
@Data
@ToString(exclude = {"sessionList", "reservations"})
@OptimisticLocking(type = OptimisticLockType.VERSION)
public class DiningTable {

    @OneToMany(mappedBy = "diningTable")
    @Column(nullable = true)
    private List<Reservation> reservations;

    public void addReservation(Reservation reservation) {
        reservations.add(reservation);
        reservation.setDiningTable(this);
    }

    @OneToMany(mappedBy = "table")
    @Column(nullable = true)
    private List<Session> sessionList;

    public void addSession(Session session) {
        sessionList.add(session);
        session.setTable(this);
    }

    public DiningTable() {
        sessionList = new ArrayList<>();
        reservations = new ArrayList<>();
    }

    public DiningTable(TableStatus tableStatus, TableCondition tableCondition, LocalDateTime createdAt, LocalDateTime updatedAt, int capacity, int version) {
        this();
        this.tableStatus = tableStatus;
        this.tableCondition = tableCondition;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.capacity = capacity;
        this.version = version;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Version
    private Integer version;

    @Column(name = "table_status")
    @Enumerated(EnumType.STRING)
    private TableStatus tableStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "table_condition")
    private TableCondition tableCondition;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    private int capacity;

    public enum TableStatus{
        AVAILABLE, OCCUPIED, RESERVED, WAITING_PAYMENT
    }

    public enum TableCondition {
        NEW, GOOD, WORN, DAMAGED, UNDER_REPAIR, RETIRED
    }

}
