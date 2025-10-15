package com.group1.swp.pizzario_swp391.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.group1.swp.pizzario_swp391.entity.Reservation;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    /**
     * Tìm các reservation đang active (CONFIRMED) cho một bàn cụ thể
     */
    @Query("SELECT r FROM Reservation r WHERE r.diningTable.id = :tableId AND r.status = 'CONFIRMED'")
    List<Reservation> findActiveReservationsByTableId(@Param("tableId") Integer tableId);

    /**
     * Tìm các reservation trong thời gian chính xác cho một bàn
     */
    @Query("SELECT r FROM Reservation r WHERE r.diningTable.id = :tableId AND r.startTime = :startTime")
    Reservation findDuplicateReservation(@Param("tableId") Integer tableId, @Param("startTime") LocalDateTime startTime);

    /**
     * Tìm các reservation trong thời gian tương đối cho một bàn(+ 90m)
     */
    @Query("select r from Reservation r where r.diningTable.id = :tableId and r.startTime between :from and :to")
    List<Reservation> findConflictReservation(@Param("tableId") Integer tableId, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    /**
     * Tìm reservation theo số điện thoại
     */
    List<Reservation> findByPhone(String phone);

    /**
     * Tìm tất cả reservation sắp tới
     */
    @Query("SELECT r FROM Reservation r WHERE r.startTime > :now AND r.status = 'CONFIRMED' ORDER BY r.startTime ASC")
    List<Reservation> findUpcomingReservations(@Param("now") LocalDateTime now);
}

