package com.group1.swp.pizzario_swp391.repository;

import com.group1.swp.pizzario_swp391.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoginRepository extends JpaRepository<Staff, Integer> {
    Optional<Staff> findByEmail(String email);
    boolean existsByEmail(String email);
}
