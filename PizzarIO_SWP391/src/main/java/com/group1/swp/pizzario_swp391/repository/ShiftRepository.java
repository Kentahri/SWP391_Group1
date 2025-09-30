package com.group1.swp.pizzario_swp391.repository;

import com.group1.swp.pizzario_swp391.entity.Shift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShiftRepository extends JpaRepository<Shift, Integer> {
    Optional<Shift> findByShiftName(String shiftName);
}
