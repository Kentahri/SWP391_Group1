package com.group1.swp.pizzario_swp391.repository;

import com.group1.swp.pizzario_swp391.entity.OtpMail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OtpMailRepository extends JpaRepository<OtpMail, Integer> {

    // Đánh dấu tất cả OTP chưa dùng của 1 staff là used=true
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE OtpMail o SET o.isUsed = true WHERE o.staff.id = :staffId AND o.isUsed = false")
    int markAllUsedByStaff(@Param("staffId") int staffId);

    @Query("SELECT otp From OtpMail otp where otp.staff.id = :staffId order by otp.id desc limit 1")
    OtpMail findLatestOtpByStaffId(@Param("staffId") int staffId);

}
