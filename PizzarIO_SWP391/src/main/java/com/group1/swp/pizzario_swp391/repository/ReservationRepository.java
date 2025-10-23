package com.group1.swp.pizzario_swp391.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.group1.swp.pizzario_swp391.entity.Reservation;

import jakarta.persistence.LockModeType;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Reservation r where r.id = :id")
    Reservation findByIdWithLock(@Param("id") Long id);

    @Query("SELECT r FROM Reservation r WHERE r.diningTable.id = :tableId AND r.startTime > :startTime AND r.status = 'CONFIRMED' ")
    List<Reservation> getAllReservationsForUpdateTable(@Param("tableId") Integer tableId, @Param("startTime") LocalDateTime startTime);

    /**
     * Tìm các tất cả reservation cho 1 bàn cụ thể
     */
    @Query("SELECT r FROM Reservation r WHERE r.diningTable.id = :tableId ORDER BY r.startTime DESC")
    List<Reservation> findAllReservationsByTableId(@Param("tableId") Integer tableId);

    /**
     * Tìm tất cả reservation
     */
    @Query("SELECT r FROM Reservation r ORDER BY r.startTime DESC")
    List<Reservation> findUpcomingReservations();

    /**
     * Tìm các reservation trong thời gian chính xác cho một bàn
     */
    @Query("SELECT r FROM Reservation r WHERE r.diningTable.id = :tableId AND r.startTime = :startTime")
    Reservation findDuplicateReservation(@Param("tableId") Integer tableId, @Param("startTime") LocalDateTime startTime);

    /**
     * Tìm các reservation CONFIRMED trong 1 bàn theo khoảng thời gian
     */
    @Query("SELECT r FROM Reservation r " +
           "WHERE r.diningTable.id = :tableId " +
           "AND r.status = 'CONFIRMED' " +
           "AND r.startTime BETWEEN :from AND :to")
    List<Reservation> findConflictReservation(@Param("tableId") Integer tableId, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    /**
     * Tìm reservation trong tất cả các bàn theo khoảng thời gian
     */
    @Query("select r from Reservation r where r.status = 'CONFIRMED' " +
            "and r.startTime between :from and :to " +
            "and r.diningTable.tableStatus != 'RESERVED'")
    List<Reservation> findAllUpcomingReservationInRange(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    /**
     * Tìm kiếm reservation theo tên khách hàng, số điện thoại
     */
    @Query("SELECT r FROM Reservation r " +
           "WHERE (LOWER(r.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR r.phone LIKE CONCAT('%', :keyword, '%')) ")
    List<Reservation> searchUpcomingReservations(@Param("now") LocalDateTime now, @Param("keyword") String keyword);


}

