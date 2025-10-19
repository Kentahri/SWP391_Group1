package com.group1.swp.pizzario_swp391.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.group1.swp.pizzario_swp391.entity.Membership;

@Repository
public interface MembershipRepository extends JpaRepository<Membership, Long> {
    // Trả về Optional để service có thể dùng .map(...) trực tiếp
    Optional<Membership> findByPhoneNumber(String phoneNumber);
}
