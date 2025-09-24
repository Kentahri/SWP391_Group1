package com.group1.swp.pizzario_swp391.entity;

import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "[Session]")
@Data
@NoArgsConstructor
public class Session {

    @ManyToOne
    @JoinColumn(name = "table_id")
    private DiningTable table;

    @OneToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "is_closed")
    private boolean isClosed;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    public Session(boolean isClosed, LocalDateTime createdAt, LocalDateTime closedAt) {
        this.isClosed = isClosed;
        this.createdAt = createdAt;
        this.closedAt = closedAt;
    }

    public void setTable(DiningTable diningTable) {
    }
}
