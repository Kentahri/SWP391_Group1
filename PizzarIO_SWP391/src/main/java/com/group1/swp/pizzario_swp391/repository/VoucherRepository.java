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

    @Query("""
    select coalesce(sum(
      case 
        when v.type = 'PERCENT' then
          case 
            when (o.totalPrice * (v.value/100.0)) <= o.totalPrice 
              then (o.totalPrice * (v.value/100.0))
            else o.totalPrice
          end
        else 
          case 
            when v.value <= o.totalPrice then v.value
            else o.totalPrice
          end
      end
    ), 0)
    from Order o
    join o.voucher v
    where o.paymentStatus = 'PAID'
      and o.orderStatus   = 'COMPLETED'
      and (v.minOrderAmount is null or o.totalPrice >= v.minOrderAmount)
      and v.isActive = true
      and o.createdAt between v.validFrom and v.validTo
  """)
    Double totalSavedAllOrders();

}
