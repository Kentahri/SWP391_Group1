package com.group1.swp.pizzario_swp391.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Dining_Table")
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

    public DiningTable(TableType tableType, TableStatus tableStatus, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this();
        this.tableType = tableType;
        this.tableStatus = tableStatus;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "table_type")
    private TableType tableType;

    @Column(name = "table_status")
    private TableStatus tableStatus;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum TableType{
        SMALL, BIG
    }

    public enum TableStatus{
        AVAILABLE, OCCUPIED, RE
    }
}
