package com.group1.swp.pizzario_swp391.repository;

import com.group1.swp.pizzario_swp391.entity.Staff;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Integer> {
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    boolean existsByRole(Staff.Role role);

    boolean existsByEmailAndIdNot(String email, int id);
    boolean existsByPhoneAndIdNot(String phone, int id);

    Optional<Staff> findByEmail(String email);

//    List<Staff> findByRole(Staff.Role role);

    // Đếm số nhân viên có isActive = true (tương ứng cột is_active)
    int countByIsActiveTrue();

    @Query("SELECT s FROM Staff s ORDER BY s.name ASC")
    List<Staff> findTopNOrderByNameAsc(Pageable pageable);
    default List<Staff> findTopNOrderByNameAsc(int limit) {
        return findTopNOrderByNameAsc(PageRequest.of(0, Math.max(1, Math.min(limit, 50))));
    }

    @Query("SELECT s FROM Staff s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :q, '%')) ORDER BY s.name ASC")
    List<Staff> searchTopByName(@Param("q") String q, Pageable pageable);
    default List<Staff> searchTopByName(String q, int limit) {
        return searchTopByName(q, PageRequest.of(0, Math.max(1, Math.min(limit, 50))));
    }
}
