package com.group1.swp.pizzario_swp391.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.group1.swp.pizzario_swp391.entity.Voucher;

import java.util.List;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    @Query("SELECT v FROM Voucher v ORDER BY v.validFrom ASC")
    List<Voucher> findAllVoucherOrderByValidFromAsc();

    @Query("SELECT sum(v.timesUsed) FROM Voucher v")
    Integer totalUsedVoucher();

    @Query("select count(v) from Voucher v where v.isActive = true")
    Integer countByActiveTrue();
}
