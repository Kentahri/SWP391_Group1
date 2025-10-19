package com.group1.swp.pizzario_swp391.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.group1.swp.pizzario_swp391.dto.membership.MembershipDTO;
import com.group1.swp.pizzario_swp391.dto.membership.MembershipRegistrationDTO;
import com.group1.swp.pizzario_swp391.entity.Membership;
import com.group1.swp.pizzario_swp391.mapper.MembershipMapper;
import com.group1.swp.pizzario_swp391.repository.MembershipRepository;

@Service
@Transactional
public class MembershipService {
    private final MembershipRepository membershipRepository;
    private final MembershipMapper membershipMapper;

    public MembershipService(MembershipRepository membershipRepository, MembershipMapper membershipMapper) {
        this.membershipRepository = membershipRepository;
        this.membershipMapper = membershipMapper;
    }

    public Optional<MembershipDTO> verifyByPhone(String phoneNumber) {
        return membershipRepository.findByPhoneNumber(phoneNumber)
                .map(membershipMapper::toMembershipDTO);
    }

    public Optional<Membership> findEntityByPhone(String phoneNumber) {
        return membershipRepository.findByPhoneNumber(phoneNumber);
    }

    // NEW: đăng ký membership
    public Optional<MembershipDTO> register(MembershipRegistrationDTO registrationDTO) {
        String phone = registrationDTO.getPhoneNumber().trim();
        // kiểm tra tồn tại
        if (membershipRepository.findByPhoneNumber(phone).isPresent()) {
            return Optional.empty();
        }
        Membership entity = membershipMapper.toMembership(registrationDTO);
        entity.setName(registrationDTO.getFullName());
        entity.setPhoneNumber(phone);
        entity.setJoinedAt(LocalDateTime.now());
        Membership saved = membershipRepository.save(entity);
        return Optional.of(membershipMapper.toMembershipDTO(saved));
    }
}
