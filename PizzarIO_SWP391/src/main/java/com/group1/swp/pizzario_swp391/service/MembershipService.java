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
public class MembershipService {
    private final MembershipRepository membershipRepository;
    private final MembershipMapper membershipMapper;

    public MembershipService(MembershipRepository membershipRepository, MembershipMapper membershipMapper) {
        this.membershipRepository = membershipRepository;
        this.membershipMapper = membershipMapper;
    }

    /**
     * Validate sessionId for membership operations
     */
    public void validateSessionId(Long sessionId) {
        if (sessionId == null) {
            throw new IllegalArgumentException("Session ID không được để trống");
        }
    }

    public Optional<Membership> findEntityByPhone(String phoneNumber) {
        return membershipRepository.findByPhoneNumber(phoneNumber);
    }

    public Membership findEntityById(Long id) {
        return membershipRepository.findById(id).orElse(null);
    }

    // Đăng ký membership (gán points nếu có, mặc định 0)
    @Transactional
    public Optional<MembershipDTO> register(MembershipRegistrationDTO registrationDTO) {
        String phone = registrationDTO.getPhoneNumber().trim();
        if (membershipRepository.findByPhoneNumber(phone).isPresent()) {
            return Optional.empty();
        }

        Membership entity = membershipMapper.toMembership(registrationDTO);
        entity.setPhoneNumber(phone);
        // gán joinedAt nếu entity dùng thuộc tính này
        try {
            entity.getClass().getMethod("setJoinedAt", LocalDateTime.class)
                  .invoke(entity, LocalDateTime.now());
        } catch (Exception ignored) {
            // nếu entity không có setJoinedAt, bỏ qua
        }

        // gán points: nếu DTO null thì mặc định 0 (đã khởi tạo trong DTO), nhưng đảm bảo không null
        Integer pts = registrationDTO.getPoints() != null ? registrationDTO.getPoints() : 0;
        try {
            entity.getClass().getMethod("setPoints", Integer.class)
                  .invoke(entity, pts);
        } catch (NoSuchMethodException e) {
            // try primitive int setter
            try {
                entity.getClass().getMethod("setPoints", int.class)
                      .invoke(entity, pts);
            } catch (Exception ignored) {}
        } catch (Exception ignored) {}

        Membership saved = membershipRepository.save(entity);
        return Optional.of(membershipMapper.toMembershipDTO(saved));
    }

    @Transactional
    public void save(Membership membership) {
        membershipRepository.save(membership);
    }
}
