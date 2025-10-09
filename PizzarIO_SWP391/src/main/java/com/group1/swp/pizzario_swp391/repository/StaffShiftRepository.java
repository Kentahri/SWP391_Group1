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

  @Query("""
        select ss from StaffShift ss
        where ss.staff.id = :staffId
              and ss.workDate = :today
      """)
  Optional<StaffShift> findCurrentShiftByStaffId(
      @Param("staffId") Integer staffId,
      @Param("today") LocalDate today);

}
