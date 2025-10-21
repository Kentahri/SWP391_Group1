package com.group1.swp.pizzario_swp391.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.group1.swp.pizzario_swp391.entity.Session;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {
    Optional<Session> findByTableIdAndIsClosedFalse(int tableId);
}

