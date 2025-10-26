package com.group1.swp.pizzario_swp391.repository;

import com.group1.swp.pizzario_swp391.entity.StaffShift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
// removed unused import
import java.util.List;
import java.util.Optional;

public interface StaffShiftRepository extends JpaRepository<StaffShift, Integer> {
  // Bảng phẳng để render UI
  @Query("""
        select ss from StaffShift ss
        join fetch ss.staff s
        join fetch ss.shift sh
        where (:from is null or ss.workDate >= :from)
          and (:to   is null or ss.workDate <= :to)
          and (:shiftId is null or sh.id = :shiftId)
          and (:staffId is null or s.id = :staffId)
        order by ss.workDate desc, sh.startTime asc, s.name asc
      """)
  List<StaffShift> search(
      @Param("from") LocalDate from,
      @Param("to") LocalDate to,
      @Param("shiftId") Integer shiftId,
      @Param("staffId") Integer staffId);

  void deleteByShift_Id(Integer shiftId);

  long countByShift_Id(Integer shiftId);

  @Query("""
        select ss from StaffShift ss
        where ss.staff.id = :staffId
              and ss.workDate = :today
      """)
  Optional<StaffShift> findCurrentShiftByStaffId(
      @Param("staffId") Integer staffId,
      @Param("today") LocalDate today);

  // Tổng số giờ làm
  @Query(value = """
          SELECT COALESCE(SUM(DATEDIFF(HOUR, ss.check_in, ss.check_out)), 0)
          FROM [Staff_Shift] ss
          WHERE ss.status IN ('COMPLETED','LEFT_EARLY')
            AND ss.check_in IS NOT NULL
            AND ss.check_out IS NOT NULL
      """, nativeQuery = true)
  Integer totalHours();

  // Tổng tiền lương
  @Query(value = """
          SELECT COALESCE(
              SUM(
                  CAST(DATEDIFF(HOUR, ss.check_in, ss.check_out) AS float)
                  * CAST(ss.hourly_wage AS float)
                  * (100 - COALESCE(ss.penalty_percent, 0)) / 100.0
              ),
              0.0
          )
          FROM [Staff_Shift] ss
          WHERE ss.status IN ('COMPLETED','LEFT_EARLY')
            AND ss.check_in IS NOT NULL
            AND ss.check_out IS NOT NULL
      """, nativeQuery = true)
  Double totalWage();

  @Query("""
          SELECT COUNT(ss)
          FROM StaffShift ss
          WHERE ss.status = 'COMPLETED'
      """)
  Integer completedShift();

  @Query("""
        select ss from StaffShift ss
        join fetch ss.staff s
        join fetch ss.shift sh
        where ss.workDate between :weekStart and :weekEnd
        order by ss.workDate asc, sh.startTime asc, s.name asc
      """)
  List<StaffShift> findByWeekRange(
      @Param("weekStart") LocalDate weekStart,
      @Param("weekEnd") LocalDate weekEnd);

  // NEW: Method để lấy staff shifts theo staff và tuần
  @Query("""
        select ss from StaffShift ss
        join fetch ss.staff s
        join fetch ss.shift sh
        where ss.workDate between :weekStart and :weekEnd
          and (:staffId is null or s.id = :staffId)
        order by ss.workDate asc, sh.startTime asc, s.name asc
      """)
  List<StaffShift> findByWeekRangeAndStaff(
      @Param("weekStart") LocalDate weekStart,
      @Param("weekEnd") LocalDate weekEnd,
      @Param("staffId") Integer staffId);

  // NEW: Method để lấy staff shifts theo shift và tuần
  @Query("""
        select ss from StaffShift ss
        join fetch ss.staff s
        join fetch ss.shift sh
        where ss.workDate between :weekStart and :weekEnd
          and (:shiftId is null or sh.id = :shiftId)
        order by ss.workDate asc, sh.startTime asc, s.name asc
      """)
  List<StaffShift> findByWeekRangeAndShift(
      @Param("weekStart") LocalDate weekStart,
      @Param("weekEnd") LocalDate weekEnd,
      @Param("shiftId") Integer shiftId);

  // NEW: Method để lấy staff shifts theo cả staff và shift trong tuần
  @Query("""
        select ss from StaffShift ss
        join fetch ss.staff s
        join fetch ss.shift sh
        where ss.workDate between :weekStart and :weekEnd
          and (:staffId is null or s.id = :staffId)
          and (:shiftId is null or sh.id = :shiftId)
        order by ss.workDate asc, sh.startTime asc, s.name asc
      """)
  List<StaffShift> findByWeekRangeAndFilters(
      @Param("weekStart") LocalDate weekStart,
      @Param("weekEnd") LocalDate weekEnd,
      @Param("staffId") Integer staffId,
      @Param("shiftId") Integer shiftId);

  @Query("""
          select ss from StaffShift ss
          join fetch ss.shift sh
          join fetch ss.staff
          where ss.staff.id = :staffId
                and ss.workDate = :today
          order by sh.startTime asc
      """)
  List<StaffShift> findAllShiftsByStaffIdAndDate(
      @Param("staffId") Integer staffId,
      @Param("today") LocalDate today);

  List<StaffShift> findByWorkDateBetween(LocalDate start, LocalDate end);
}