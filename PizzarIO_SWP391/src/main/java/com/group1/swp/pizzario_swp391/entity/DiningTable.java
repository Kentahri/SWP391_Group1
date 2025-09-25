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
import lombok.Data;

@Entity
@Table(name = "dining_table")
@Data
public class DiningTable {

    @OneToMany(mappedBy = "table")
    @Column(nullable = true)
    private List<Session> sessionList;

    public void addSession(Session session) {
        sessionList.add(session);
        session.setTable(this);
    }

    public DiningTable() {
        sessionList = new ArrayList<>();
    }

    public DiningTable(TableType tableType, TableStatus tableStatus, TableCondition tableCondition, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this();
        this.tableType = tableType;
        this.tableStatus = tableStatus;
        this.tableCondition = tableCondition;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "table_type")
    @Enumerated(EnumType.STRING)
    private TableType tableType;

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

    public enum TableType{
        SMALL, BIG
    }

    public enum TableStatus{
        AVAILABLE, OCCUPIED, RESERVED, WAITING_PAYMENT
    }

    public enum TableCondition {
        NEW, GOOD, WORN, DAMAGED, UNDER_REPAIR, RETIRED
    }

}
