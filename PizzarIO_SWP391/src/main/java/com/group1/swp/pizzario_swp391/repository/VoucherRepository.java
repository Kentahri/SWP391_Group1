package com.group1.swp.pizzario_swp391.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.group1.swp.pizzario_swp391.entity.Voucher;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    
    Optional<Voucher> findByCode(String code);
    
    List<Voucher> findByIsActiveTrue();
    
    List<Voucher> findByIsActiveTrueAndValidFromBeforeAndValidToAfter(LocalDateTime now, LocalDateTime now2);
    
    @Query("SELECT v FROM Voucher v WHERE v.isActive = true AND v.validFrom <= :now AND v.validTo >= :now")
    List<Voucher> findActiveVouchers(@Param("now") LocalDateTime now);
    
    @Query("SELECT v FROM Voucher v WHERE v.code = :code AND v.isActive = true AND v.validFrom <= :now AND v.validTo >= :now")
    Optional<Voucher> findActiveVoucherByCode(@Param("code") String code, @Param("now") LocalDateTime now);
    
    boolean existsByCode(String code);
}

